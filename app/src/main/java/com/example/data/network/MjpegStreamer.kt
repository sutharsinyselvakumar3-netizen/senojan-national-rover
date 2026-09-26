package com.example.data.network

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.concurrent.TimeUnit

class MjpegStreamer {

    private val client = OkHttpClient.Builder()
        .connectTimeout(3000, TimeUnit.MILLISECONDS)
        .readTimeout(5000, TimeUnit.MILLISECONDS)
        .build()

    private var streamJob: Job? = null
    private var activeCall: okhttp3.Call? = null

    private val _currentFrame = MutableStateFlow<Bitmap?>(null)
    val currentFrame: StateFlow<Bitmap?> = _currentFrame.asStateFlow()

    private val _isStreaming = MutableStateFlow(false)
    val isStreaming: StateFlow<Boolean> = _isStreaming.asStateFlow()

    private val _streamError = MutableStateFlow<String?>(null)
    val streamError: StateFlow<String?> = _streamError.asStateFlow()

    private val _fps = MutableStateFlow(0)
    val fps: StateFlow<Int> = _fps.asStateFlow()

    private var frameCounter = 0
    private var lastFpsTimestamp = System.currentTimeMillis()

    fun startStream(scope: CoroutineScope, streamUrl: String) {
        stopStream()

        streamJob = scope.launch(Dispatchers.IO) {
            _streamError.value = null
            _isStreaming.value = true
            frameCounter = 0
            lastFpsTimestamp = System.currentTimeMillis()

            try {
                val request = Request.Builder()
                    .url(streamUrl)
                    .addHeader("Accept", "multipart/x-mixed-replace")
                    .build()

                val call = client.newCall(request)
                activeCall = call
                val response: Response = call.execute()

                if (!response.isSuccessful) {
                    _streamError.value = "STREAM NOT AVAILABLE (HTTP ${response.code})"
                    _isStreaming.value = false
                    return@launch
                }

                val inputStream: InputStream = response.body?.byteStream()
                    ?: throw Exception("No stream payload received")

                readMjpegStream(inputStream)
            } catch (e: CancellationException) {
                // Normal cancellation
            } catch (e: Exception) {
                Log.w("MjpegStreamer", "Stream error: ${e.message}")
                _streamError.value = "CAMERA OFFLINE: ${e.localizedMessage ?: "Connection failed"}"
                _isStreaming.value = false
            } finally {
                _isStreaming.value = false
                activeCall = null
            }
        }
    }

    private fun readMjpegStream(inputStream: InputStream) {
        val buffer = ByteArray(4096)
        val frameBuffer = ByteArrayOutputStream()
        var prevByte = -1

        var inJpeg = false

        while (streamJob?.isActive == true) {
            val bytesRead = inputStream.read(buffer)
            if (bytesRead == -1) break

            for (i in 0 until bytesRead) {
                val b = buffer[i].toInt() and 0xFF

                if (!inJpeg) {
                    if (prevByte == 0xFF && b == 0xD8) { // JPEG SOI
                        inJpeg = true
                        frameBuffer.reset()
                        frameBuffer.write(0xFF)
                        frameBuffer.write(0xD8)
                    }
                } else {
                    frameBuffer.write(b)
                    if (prevByte == 0xFF && b == 0xD9) { // JPEG EOI
                        inJpeg = false
                        val jpegBytes = frameBuffer.toByteArray()
                        val bitmap = BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size)
                        if (bitmap != null) {
                            _currentFrame.value = bitmap
                            frameCounter++
                            val now = System.currentTimeMillis()
                            if (now - lastFpsTimestamp >= 1000) {
                                _fps.value = frameCounter
                                frameCounter = 0
                                lastFpsTimestamp = now
                            }
                        }
                    }
                }
                prevByte = b
            }
        }
    }

    fun stopStream() {
        try {
            activeCall?.cancel()
            streamJob?.cancel()
        } catch (_: Exception) {}
        activeCall = null
        streamJob = null
        _isStreaming.value = false
    }

    fun restartStream(scope: CoroutineScope, streamUrl: String) {
        stopStream()
        startStream(scope, streamUrl)
    }

    suspend fun captureSnapshot(captureUrl: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(captureUrl)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val bytes = response.body?.bytes() ?: return@withContext null
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            }
        } catch (e: Exception) {
            Log.w("MjpegStreamer", "Snapshot capture error: ${e.message}")
            null
        }
    }
}
