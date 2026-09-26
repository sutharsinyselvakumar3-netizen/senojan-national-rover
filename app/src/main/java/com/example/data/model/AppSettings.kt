package com.example.data.model

data class AppSettings(
    val esp32Ip: String = "192.168.1.50",
    val esp32Port: Int = 80,
    val cameraIp: String = "192.168.1.51",
    val cameraStreamPort: Int = 81,
    val cameraStreamEndpoint: String = "/stream",
    val cameraCaptureEndpoint: String = "/capture",
    val hotspotSsid: String = "AgriBot_Hotspot",
    val hotspotPassword: String = "agribot2026",
    val connectionTimeoutMs: Long = 2500L,
    val retryCount: Int = 3,
    val pollingIntervalMs: Long = 1000L,

    // Camera Pan/Tilt limits
    val panMin: Int = 0,
    val panMax: Int = 180,
    val panCenter: Int = 90,
    val panHome: Int = 90,
    val tiltMin: Int = 0,
    val tiltMax: Int = 180,
    val tiltCenter: Int = 90,
    val tiltHome: Int = 90,

    // Calibration
    val cameraHeightCm: Float = 25.0f,
    val cameraTiltAngleDeg: Float = 45.0f,
    val groundScaleFactor: Float = 0.12f,

    // AI thresholds
    val aiConfidenceThreshold: Float = 0.70f,
    val consecutiveDetectionsRequired: Int = 3,

    // Soil thresholds
    val soilDryThreshold: Int = 30,
    val soilCriticalThreshold: Int = 15,
    val soilSoundEnabled: Boolean = true,
    val soilHapticEnabled: Boolean = true,
    val soilNotificationEnabled: Boolean = true,
    val soilSirenEnabled: Boolean = false,

    // Water thresholds & safety
    val waterLowThreshold: Int = 35,
    val waterCriticalThreshold: Int = 15,
    val waterPumpLockoutEnabled: Boolean = true,
    val waterSirenEnabled: Boolean = true,
    val waterNotificationEnabled: Boolean = true,
    val waterHapticEnabled: Boolean = true,
    val waterSoundEnabled: Boolean = true,

    // Vibration thresholds & safety
    val vibrationSensorEnabled: Boolean = true,
    val vibrationThreshold: Float = 1.5f,
    val vibrationDebounceMs: Long = 300L,
    val vibrationCriticalDurationMs: Long = 1500L,
    val vibrationHapticEnabled: Boolean = true,
    val vibrationSoundEnabled: Boolean = true,
    val vibrationNotificationEnabled: Boolean = true,
    val vibrationSirenEnabled: Boolean = true,
    val vibrationAutoStopEnabled: Boolean = true,

    // Notifications
    val notificationsEnabled: Boolean = true,

    // Battery thresholds
    val batteryLowThreshold: Double = 11.2,
    val batteryCriticalThreshold: Double = 10.5,

    // MPU thresholds
    val mpuPitchThresholdDeg: Double = 25.0,
    val mpuRollThresholdDeg: Double = 25.0,

    // Developer / Simulation mode (strictly false by default)
    val simulationModeEnabled: Boolean = false
) {
    val esp32BaseUrl: String
        get() = if (esp32Port == 80) "http://$esp32Ip" else "http://$esp32Ip:$esp32Port"

    val cameraStreamUrl: String
        get() = "http://$cameraIp:$cameraStreamPort$cameraStreamEndpoint"

    val cameraCaptureUrl: String
        get() = "http://$cameraIp$cameraCaptureEndpoint"
}
