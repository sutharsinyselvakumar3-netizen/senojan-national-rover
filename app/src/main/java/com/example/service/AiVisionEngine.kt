package com.example.service

import android.graphics.Bitmap
import android.graphics.Color
import com.example.data.model.AppSettings
import com.example.data.model.DetectionResult
import com.example.data.model.TargetClass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min

class AiVisionEngine {

    private var consecutiveWeedCount = 0
    private var lastTargetId: String? = null

    /**
     * Performs computer vision analysis on a real camera Bitmap frame.
     * Detects vegetation clusters using Excess Green Index (ExG = 2G - R - B)
     * and categorizes morphology between upright onion stalks and broadleaf weeds.
     */
    suspend fun analyzeFrame(bitmap: Bitmap, settings: AppSettings): List<DetectionResult> = withContext(Dispatchers.Default) {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= 0 || height <= 0) return@withContext emptyList()

        val results = mutableListOf<DetectionResult>()

        // Subsample for fast real-time frame processing
        val step = max(4, min(width, height) / 80)
        var minX = width
        var maxX = 0
        var minY = height
        var maxY = 0
        var greenPixelCount = 0
        var totalSamples = 0

        // Cluster detection accumulator
        for (y in 0 until height step step) {
            for (x in 0 until width step step) {
                totalSamples++
                val pixel = bitmap.getPixel(x, y)
                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)

                // Excess Green index
                val exG = (2 * g) - r - b
                if (exG > 30 && g > 70) {
                    greenPixelCount++
                    if (x < minX) minX = x
                    if (x > maxX) maxX = x
                    if (y < minY) minY = y
                    if (y > maxY) maxY = y
                }
            }
        }

        // Check if sufficient plant presence detected
        if (greenPixelCount >= 8 && minX < maxX && minY < maxY) {
            val boxW = (maxX - minX + step).toFloat().coerceAtLeast(40f)
            val boxH = (maxY - minY + step).toFloat().coerceAtLeast(40f)
            val boxX = minX.toFloat()
            val boxY = minY.toFloat()

            val aspectRatio = boxH / boxW
            val coverage = greenPixelCount.toFloat() / (totalSamples * 0.15f).coerceAtLeast(1f)
            val confidence = min(0.98f, max(0.55f, 0.65f + (coverage * 0.25f)))

            val targetClass = when {
                confidence < settings.aiConfidenceThreshold -> TargetClass.UNKNOWN
                aspectRatio >= 1.75f -> TargetClass.ONION // Tall, vertical onion stalk
                else -> TargetClass.WEED // Broadleaf rosette or irregular weed patch
            }

            results.add(
                DetectionResult(
                    targetClass = targetClass,
                    confidence = confidence,
                    x = boxX,
                    y = boxY,
                    width = boxW,
                    height = boxH
                )
            )
        }

        results
    }

    /**
     * Updates consecutive weed counter and returns confirmed state:
     * e.g., (currentCount, targetCount, isConfirmed)
     */
    fun processWeedConfirmation(
        detections: List<DetectionResult>,
        requiredConsecutive: Int
    ): WeedConfirmationStatus {
        val weed = detections.firstOrNull { it.targetClass == TargetClass.WEED }
        if (weed != null) {
            consecutiveWeedCount++
            val confirmed = consecutiveWeedCount >= requiredConsecutive
            return WeedConfirmationStatus(
                currentCount = consecutiveWeedCount,
                requiredCount = requiredConsecutive,
                isConfirmed = confirmed,
                confirmedWeed = if (confirmed) weed else null
            )
        } else {
            // Reset counter if no weed detected in this frame
            consecutiveWeedCount = 0
            return WeedConfirmationStatus(
                currentCount = 0,
                requiredCount = requiredConsecutive,
                isConfirmed = false,
                confirmedWeed = null
            )
        }
    }

    fun resetConsecutiveCounter() {
        consecutiveWeedCount = 0
    }
}

data class WeedConfirmationStatus(
    val currentCount: Int,
    val requiredCount: Int,
    val isConfirmed: Boolean,
    val confirmedWeed: DetectionResult?
)
