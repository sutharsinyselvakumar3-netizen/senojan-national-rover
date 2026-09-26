package com.example.data.model

import java.util.UUID

enum class TargetClass {
    WEED,
    ONION,
    UNKNOWN
}

data class DetectionResult(
    val id: String = UUID.randomUUID().toString(),
    val targetClass: TargetClass,
    val confidence: Float,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val timestamp: Long = System.currentTimeMillis()
) {
    val centerX: Float get() = x + (width / 2f)
    val centerY: Float get() = y + (height / 2f)

    val label: String
        get() = when (targetClass) {
            TargetClass.WEED -> "WEED"
            TargetClass.ONION -> "ONION"
            TargetClass.UNKNOWN -> "UNKNOWN"
        }

    val actionDescription: String
        get() = when (targetClass) {
            TargetClass.WEED -> "TARGET IDENTIFIED - PENDING CONFIRMATION"
            TargetClass.ONION -> "ONION PROTECTED"
            TargetClass.UNKNOWN -> "UNKNOWN TARGET - IGNORED"
        }
}
