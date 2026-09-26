package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RobotStatus(
    @Json(name = "mode") val mode: String? = null,
    @Json(name = "batteryVoltage") val batteryVoltage: Double? = null,
    @Json(name = "batteryPercent") val batteryPercent: Int? = null,
    @Json(name = "soilMoisture") val soilMoisture: Int? = null,
    @Json(name = "waterLevel") val waterLevel: Int? = null,
    @Json(name = "soilSensorValid") val soilSensorValid: Boolean? = null,
    @Json(name = "waterSensorValid") val waterSensorValid: Boolean? = null,
    @Json(name = "mpuStable") val mpuStable: Boolean? = null,
    @Json(name = "pitch") val pitch: Double? = null,
    @Json(name = "roll") val roll: Double? = null,
    @Json(name = "vibrationSensorValid") val vibrationSensorValid: Boolean? = null,
    @Json(name = "vibrationDetected") val vibrationDetected: Boolean? = null,
    @Json(name = "vibrationCritical") val vibrationCritical: Boolean? = null,
    @Json(name = "vibrationAlert") val vibrationAlert: Boolean? = null,
    @Json(name = "armReady") val armReady: Boolean? = null,
    @Json(name = "armState") val armState: String? = null,
    @Json(name = "drillOn") val drillOn: Boolean? = null,
    @Json(name = "relay2") val relay2: Boolean? = null,
    @Json(name = "relay3") val relay3: Boolean? = null,
    @Json(name = "relay4") val relay4: Boolean? = null,
    @Json(name = "sirenOn") val sirenOn: Boolean? = null,
    @Json(name = "alarmState") val alarmState: String? = null,
    @Json(name = "alarmReason") val alarmReason: String? = null,
    @Json(name = "cameraPan") val cameraPan: Int? = null,
    @Json(name = "cameraTilt") val cameraTilt: Int? = null,
    @Json(name = "servo4Angle") val servo4Angle: Int? = null,
    @Json(name = "safety") val safety: String? = null
) {
    val confirmedMode: RobotMode
        get() = if (mode?.equals("AUTO", ignoreCase = true) == true) RobotMode.AUTO else RobotMode.MANUAL

    val batteryVoltageDisplay: String
        get() = batteryVoltage?.let { String.format(java.util.Locale.US, "%.1f V", it) } ?: "UNKNOWN"

    val batteryPercentDisplay: String
        get() = batteryPercent?.let { "$it%" } ?: "UNKNOWN"

    val soilMoistureDisplay: String
        get() = soilMoisture?.let { "$it%" } ?: "UNKNOWN"

    val waterLevelDisplay: String
        get() = waterLevel?.let { "$it%" } ?: "UNKNOWN"

    val waterState: String
        get() = when {
            waterLevel == null -> "UNKNOWN"
            waterLevel <= 15 -> "CRITICAL"
            waterLevel <= 35 -> "LOW"
            else -> "NORMAL"
        }

    val isWaterCritical: Boolean
        get() = (waterLevel != null && waterLevel <= 15)

    val isBatteryCritical: Boolean
        get() = (batteryVoltage != null && batteryVoltage <= 10.5) || (batteryPercent != null && batteryPercent <= 10)

    val isMpuFault: Boolean
        get() = mpuStable == false

    val isVibrationCriticalState: Boolean
        get() = vibrationCritical == true

    val isSafetyCritical: Boolean
        get() = safety?.equals("CRITICAL", ignoreCase = true) == true ||
                isWaterCritical || isBatteryCritical || isMpuFault || isVibrationCriticalState

    val safetyStatusText: String
        get() = when {
            safety != null -> safety.uppercase()
            isSafetyCritical -> "CRITICAL"
            isMpuFault || (vibrationDetected == true) || waterState == "LOW" -> "WARNING"
            batteryVoltage != null -> "SAFE"
            else -> "UNKNOWN"
        }

    val pitchDisplay: String
        get() = pitch?.let { String.format(java.util.Locale.US, "%.1f°", it) } ?: "UNKNOWN"

    val rollDisplay: String
        get() = roll?.let { String.format(java.util.Locale.US, "%.1f°", it) } ?: "UNKNOWN"

    val panAngle: Int
        get() = cameraPan ?: 90

    val tiltAngle: Int
        get() = cameraTilt ?: 90

    val soilServoAngle: Int
        get() = (servo4Angle ?: 0).coerceIn(0, 45)
}
