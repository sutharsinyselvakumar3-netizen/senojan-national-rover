package com.example.data.network

import com.example.data.model.CommandResponse
import com.example.data.model.RobotStatus
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class Esp32ApiClient {

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val robotStatusAdapter = moshi.adapter(RobotStatus::class.java)
    private val commandResponseAdapter = moshi.adapter(CommandResponse::class.java)

    private val client = OkHttpClient.Builder()
        .connectTimeout(2500, TimeUnit.MILLISECONDS)
        .readTimeout(2500, TimeUnit.MILLISECONDS)
        .writeTimeout(2500, TimeUnit.MILLISECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun getStatus(baseUrl: String): Result<RobotStatus> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/api/status")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
                }
                val bodyString = response.body?.string()
                    ?: return@withContext Result.failure(Exception("Empty response body"))
                val status = robotStatusAdapter.fromJson(bodyString)
                    ?: return@withContext Result.failure(Exception("Failed to parse status JSON"))
                Result.success(status)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setMode(baseUrl: String, mode: String): Result<CommandResponse> = withContext(Dispatchers.IO) {
        try {
            val json = "{\"mode\":\"$mode\"}"
            val request = Request.Builder()
                .url("$baseUrl/api/mode")
                .post(json.toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string() ?: ""
                val cmdResp = try {
                    commandResponseAdapter.fromJson(bodyString)
                } catch (_: Exception) {
                    null
                } ?: CommandResponse(
                    success = response.isSuccessful,
                    mode = mode,
                    message = if (response.isSuccessful) "Mode command accepted" else "HTTP error ${response.code}"
                )
                Result.success(cmdResp)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendRobotMovement(baseUrl: String, action: String, speed: String): Result<CommandResponse> = withContext(Dispatchers.IO) {
        try {
            val json = "{\"action\":\"$action\",\"speed\":\"$speed\"}"
            val request = Request.Builder()
                .url("$baseUrl/api/robot")
                .post(json.toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string() ?: ""
                val cmdResp = try {
                    commandResponseAdapter.fromJson(bodyString)
                } catch (_: Exception) {
                    null
                } ?: CommandResponse(
                    success = response.isSuccessful,
                    message = if (response.isSuccessful) "Movement sent: $action" else "HTTP error ${response.code}"
                )
                Result.success(cmdResp)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setServo4Angle(baseUrl: String, angle: Int): Result<CommandResponse> = withContext(Dispatchers.IO) {
        try {
            val clampedAngle = angle.coerceIn(0, 45)
            val json = "{\"angle\":$clampedAngle}"
            val request = Request.Builder()
                .url("$baseUrl/api/servo4")
                .post(json.toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string() ?: ""
                val cmdResp = try {
                    commandResponseAdapter.fromJson(bodyString)
                } catch (_: Exception) {
                    null
                } ?: CommandResponse(
                    success = response.isSuccessful,
                    message = if (response.isSuccessful) "Servo 4 set to $clampedAngle°" else "HTTP error ${response.code}"
                )
                Result.success(cmdResp)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setRelay(baseUrl: String, relayNum: Int, state: Boolean): Result<CommandResponse> = withContext(Dispatchers.IO) {
        if (relayNum == 1) {
            return@withContext Result.failure(IllegalStateException("Relay 1 (Drill) cannot be manually toggled. Auto-workflow only!"))
        }
        try {
            val json = "{\"relay\":$relayNum,\"state\":$state}"
            val request = Request.Builder()
                .url("$baseUrl/api/relay")
                .post(json.toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string() ?: ""
                val cmdResp = try {
                    commandResponseAdapter.fromJson(bodyString)
                } catch (_: Exception) {
                    null
                } ?: CommandResponse(
                    success = response.isSuccessful,
                    message = if (response.isSuccessful) "Relay $relayNum state set to $state" else "HTTP error ${response.code}"
                )
                Result.success(cmdResp)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendCameraPanTilt(baseUrl: String, pan: Int, tilt: Int): Result<CommandResponse> = withContext(Dispatchers.IO) {
        try {
            val clampedPan = pan.coerceIn(0, 180)
            val clampedTilt = tilt.coerceIn(0, 180)
            val json = "{\"pan\":$clampedPan,\"tilt\":$clampedTilt}"
            val request = Request.Builder()
                .url("$baseUrl/api/camera/pantilt")
                .post(json.toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string() ?: ""
                val cmdResp = try {
                    commandResponseAdapter.fromJson(bodyString)
                } catch (_: Exception) {
                    null
                } ?: CommandResponse(
                    success = response.isSuccessful,
                    message = if (response.isSuccessful) "Camera pan/tilt set to ($clampedPan°, $clampedTilt°)" else "HTTP error ${response.code}"
                )
                Result.success(cmdResp)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendDetectionWorkflow(baseUrl: String, detected: String, x: Float, y: Float, confidence: Float): Result<CommandResponse> = withContext(Dispatchers.IO) {
        try {
            val json = "{\"detected\":\"$detected\",\"x\":$x,\"y\":$y,\"confidence\":$confidence}"
            val request = Request.Builder()
                .url("$baseUrl/api/detection")
                .post(json.toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string() ?: ""
                val cmdResp = try {
                    commandResponseAdapter.fromJson(bodyString)
                } catch (_: Exception) {
                    null
                } ?: CommandResponse(
                    success = response.isSuccessful,
                    message = if (response.isSuccessful) "Detection reported to ESP32" else "HTTP error ${response.code}"
                )
                Result.success(cmdResp)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendEmergencyStop(baseUrl: String): Result<CommandResponse> = withContext(Dispatchers.IO) {
        try {
            val json = "{}"
            val request = Request.Builder()
                .url("$baseUrl/api/emergency_stop")
                .post(json.toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string() ?: ""
                val cmdResp = try {
                    commandResponseAdapter.fromJson(bodyString)
                } catch (_: Exception) {
                    null
                } ?: CommandResponse(
                    success = response.isSuccessful,
                    message = if (response.isSuccessful) "EMERGENCY STOP EXECUTED" else "HTTP error ${response.code}"
                )
                Result.success(cmdResp)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
