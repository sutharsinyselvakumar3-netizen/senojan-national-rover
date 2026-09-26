package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.AppSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("agribot_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private fun loadSettings(): AppSettings {
        return AppSettings(
            esp32Ip = prefs.getString("esp32Ip", "192.168.1.50") ?: "192.168.1.50",
            esp32Port = prefs.getInt("esp32Port", 80),
            cameraIp = prefs.getString("cameraIp", "192.168.1.51") ?: "192.168.1.51",
            cameraStreamPort = prefs.getInt("cameraStreamPort", 81),
            cameraStreamEndpoint = prefs.getString("cameraStreamEndpoint", "/stream") ?: "/stream",
            cameraCaptureEndpoint = prefs.getString("cameraCaptureEndpoint", "/capture") ?: "/capture",
            hotspotSsid = prefs.getString("hotspotSsid", "AgriBot_Hotspot") ?: "AgriBot_Hotspot",
            hotspotPassword = prefs.getString("hotspotPassword", "agribot2026") ?: "agribot2026",
            connectionTimeoutMs = prefs.getLong("connectionTimeoutMs", 2500L),
            retryCount = prefs.getInt("retryCount", 3),
            pollingIntervalMs = prefs.getLong("pollingIntervalMs", 1000L),
            panMin = prefs.getInt("panMin", 0),
            panMax = prefs.getInt("panMax", 180),
            panCenter = prefs.getInt("panCenter", 90),
            tiltMin = prefs.getInt("tiltMin", 0),
            tiltMax = prefs.getInt("tiltMax", 180),
            tiltCenter = prefs.getInt("tiltCenter", 90),
            cameraHeightCm = prefs.getFloat("cameraHeightCm", 25.0f),
            cameraTiltAngleDeg = prefs.getFloat("cameraTiltAngleDeg", 45.0f),
            groundScaleFactor = prefs.getFloat("groundScaleFactor", 0.12f),
            aiConfidenceThreshold = prefs.getFloat("aiConfidenceThreshold", 0.70f),
            consecutiveDetectionsRequired = prefs.getInt("consecutiveDetectionsRequired", 3),
            soilDryThreshold = prefs.getInt("soilDryThreshold", 30),
            soilCriticalThreshold = prefs.getInt("soilCriticalThreshold", 15),
            soilSoundEnabled = prefs.getBoolean("soilSoundEnabled", true),
            soilHapticEnabled = prefs.getBoolean("soilHapticEnabled", true),
            soilNotificationEnabled = prefs.getBoolean("soilNotificationEnabled", true),
            soilSirenEnabled = prefs.getBoolean("soilSirenEnabled", false),
            waterLowThreshold = prefs.getInt("waterLowThreshold", 35),
            waterCriticalThreshold = prefs.getInt("waterCriticalThreshold", 15),
            waterPumpLockoutEnabled = prefs.getBoolean("waterPumpLockoutEnabled", true),
            waterSirenEnabled = prefs.getBoolean("waterSirenEnabled", true),
            waterNotificationEnabled = prefs.getBoolean("waterNotificationEnabled", true),
            waterHapticEnabled = prefs.getBoolean("waterHapticEnabled", true),
            waterSoundEnabled = prefs.getBoolean("waterSoundEnabled", true),
            vibrationSensorEnabled = prefs.getBoolean("vibrationSensorEnabled", true),
            vibrationThreshold = prefs.getFloat("vibrationThreshold", 1.5f),
            vibrationDebounceMs = prefs.getLong("vibrationDebounceMs", 300L),
            vibrationCriticalDurationMs = prefs.getLong("vibrationCriticalDurationMs", 1500L),
            vibrationHapticEnabled = prefs.getBoolean("vibrationHapticEnabled", true),
            vibrationSoundEnabled = prefs.getBoolean("vibrationSoundEnabled", true),
            vibrationNotificationEnabled = prefs.getBoolean("vibrationNotificationEnabled", true),
            vibrationSirenEnabled = prefs.getBoolean("vibrationSirenEnabled", true),
            vibrationAutoStopEnabled = prefs.getBoolean("vibrationAutoStopEnabled", true),
            batteryLowThreshold = prefs.getFloat("batteryLowThreshold", 11.2f).toDouble(),
            batteryCriticalThreshold = prefs.getFloat("batteryCriticalThreshold", 10.5f).toDouble(),
            mpuPitchThresholdDeg = prefs.getFloat("mpuPitchThresholdDeg", 25.0f).toDouble(),
            mpuRollThresholdDeg = prefs.getFloat("mpuRollThresholdDeg", 25.0f).toDouble(),
            simulationModeEnabled = prefs.getBoolean("simulationModeEnabled", false)
        )
    }

    fun updateSettings(newSettings: AppSettings) {
        prefs.edit().apply {
            putString("esp32Ip", newSettings.esp32Ip)
            putInt("esp32Port", newSettings.esp32Port)
            putString("cameraIp", newSettings.cameraIp)
            putInt("cameraStreamPort", newSettings.cameraStreamPort)
            putString("cameraStreamEndpoint", newSettings.cameraStreamEndpoint)
            putString("cameraCaptureEndpoint", newSettings.cameraCaptureEndpoint)
            putString("hotspotSsid", newSettings.hotspotSsid)
            putString("hotspotPassword", newSettings.hotspotPassword)
            putLong("connectionTimeoutMs", newSettings.connectionTimeoutMs)
            putInt("retryCount", newSettings.retryCount)
            putLong("pollingIntervalMs", newSettings.pollingIntervalMs)
            putInt("panMin", newSettings.panMin)
            putInt("panMax", newSettings.panMax)
            putInt("panCenter", newSettings.panCenter)
            putInt("tiltMin", newSettings.tiltMin)
            putInt("tiltMax", newSettings.tiltMax)
            putInt("tiltCenter", newSettings.tiltCenter)
            putFloat("cameraHeightCm", newSettings.cameraHeightCm)
            putFloat("cameraTiltAngleDeg", newSettings.cameraTiltAngleDeg)
            putFloat("groundScaleFactor", newSettings.groundScaleFactor)
            putFloat("aiConfidenceThreshold", newSettings.aiConfidenceThreshold)
            putInt("consecutiveDetectionsRequired", newSettings.consecutiveDetectionsRequired)
            putInt("soilDryThreshold", newSettings.soilDryThreshold)
            putInt("soilCriticalThreshold", newSettings.soilCriticalThreshold)
            putBoolean("soilSoundEnabled", newSettings.soilSoundEnabled)
            putBoolean("soilHapticEnabled", newSettings.soilHapticEnabled)
            putBoolean("soilNotificationEnabled", newSettings.soilNotificationEnabled)
            putBoolean("soilSirenEnabled", newSettings.soilSirenEnabled)
            putInt("waterLowThreshold", newSettings.waterLowThreshold)
            putInt("waterCriticalThreshold", newSettings.waterCriticalThreshold)
            putBoolean("waterPumpLockoutEnabled", newSettings.waterPumpLockoutEnabled)
            putBoolean("waterSirenEnabled", newSettings.waterSirenEnabled)
            putBoolean("waterNotificationEnabled", newSettings.waterNotificationEnabled)
            putBoolean("waterHapticEnabled", newSettings.waterHapticEnabled)
            putBoolean("waterSoundEnabled", newSettings.waterSoundEnabled)
            putBoolean("vibrationSensorEnabled", newSettings.vibrationSensorEnabled)
            putFloat("vibrationThreshold", newSettings.vibrationThreshold)
            putLong("vibrationDebounceMs", newSettings.vibrationDebounceMs)
            putLong("vibrationCriticalDurationMs", newSettings.vibrationCriticalDurationMs)
            putBoolean("vibrationHapticEnabled", newSettings.vibrationHapticEnabled)
            putBoolean("vibrationSoundEnabled", newSettings.vibrationSoundEnabled)
            putBoolean("vibrationNotificationEnabled", newSettings.vibrationNotificationEnabled)
            putBoolean("vibrationSirenEnabled", newSettings.vibrationSirenEnabled)
            putBoolean("vibrationAutoStopEnabled", newSettings.vibrationAutoStopEnabled)
            putFloat("batteryLowThreshold", newSettings.batteryLowThreshold.toFloat())
            putFloat("batteryCriticalThreshold", newSettings.batteryCriticalThreshold.toFloat())
            putFloat("mpuPitchThresholdDeg", newSettings.mpuPitchThresholdDeg.toFloat())
            putFloat("mpuRollThresholdDeg", newSettings.mpuRollThresholdDeg.toFloat())
            putBoolean("simulationModeEnabled", newSettings.simulationModeEnabled)
            apply()
        }
        _settings.value = newSettings
    }
}
