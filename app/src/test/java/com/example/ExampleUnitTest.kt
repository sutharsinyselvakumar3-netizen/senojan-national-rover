package com.example

import com.example.data.model.AppSettings
import com.example.data.model.DetectionResult
import com.example.data.model.RobotMode
import com.example.data.model.RobotStatus
import com.example.data.model.TargetClass
import com.example.service.AiVisionEngine
import com.example.service.KinematicsCalculator
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    // 1. API Status JSON Parsing (Section 38 & 60)
    @Test
    fun testApiStatusParsing_handlesFullJson() {
        val json = """
        {
          "mode": "MANUAL",
          "batteryVoltage": 12.4,
          "batteryPercent": 85,
          "soilMoisture": 62,
          "waterLevel": 78,
          "soilSensorValid": true,
          "waterSensorValid": true,
          "mpuStable": true,
          "pitch": 1.2,
          "roll": -0.8,
          "vibrationSensorValid": true,
          "vibrationDetected": false,
          "vibrationCritical": false,
          "vibrationAlert": false,
          "armReady": true,
          "armState": "HOME",
          "drillOn": false,
          "relay2": false,
          "relay3": false,
          "relay4": false,
          "sirenOn": false,
          "alarmState": "NORMAL",
          "alarmReason": "",
          "cameraPan": 90,
          "cameraTilt": 90,
          "servo4Angle": 22,
          "safety": "SAFE"
        }
        """.trimIndent()

        val adapter = moshi.adapter(RobotStatus::class.java)
        val status = adapter.fromJson(json)

        assertNotNull(status)
        assertEquals("MANUAL", status?.mode)
        assertEquals(RobotMode.MANUAL, status?.confirmedMode)
        assertEquals("12.4 V", status?.batteryVoltageDisplay)
        assertEquals("85%", status?.batteryPercentDisplay)
        assertEquals("62%", status?.soilMoistureDisplay)
        assertEquals("78%", status?.waterLevelDisplay)
        assertEquals("NORMAL", status?.waterState)
        assertFalse(status?.isWaterCritical ?: true)
        assertFalse(status?.isBatteryCritical ?: true)
        assertFalse(status?.isMpuFault ?: true)
        assertEquals(22, status?.soilServoAngle)
    }

    // 2. Tolerance for Missing Fields -> UNKNOWN (Section 38 & 50)
    @Test
    fun testApiStatusParsing_toleratesMissingFields() {
        val emptyJson = "{}"
        val adapter = moshi.adapter(RobotStatus::class.java)
        val status = adapter.fromJson(emptyJson)

        assertNotNull(status)
        assertEquals(RobotMode.MANUAL, status?.confirmedMode)
        assertEquals("UNKNOWN", status?.batteryVoltageDisplay)
        assertEquals("UNKNOWN", status?.soilMoistureDisplay)
        assertEquals("UNKNOWN", status?.waterLevelDisplay)
        assertEquals("UNKNOWN", status?.waterState)
        assertEquals("UNKNOWN", status?.safetyStatusText)
        assertEquals(90, status?.panAngle)
        assertEquals(90, status?.tiltAngle)
        assertEquals(0, status?.soilServoAngle)
    }

    // 3. Mode Parsing: Test A & Test B (Section 61)
    @Test
    fun testModeParsing_manualAndAuto() {
        val manualStatus = RobotStatus(mode = "MANUAL")
        assertEquals(RobotMode.MANUAL, manualStatus.confirmedMode)

        val autoStatus = RobotStatus(mode = "AUTO")
        assertEquals(RobotMode.AUTO, autoStatus.confirmedMode)

        val lowerAutoStatus = RobotStatus(mode = "auto")
        assertEquals(RobotMode.AUTO, lowerAutoStatus.confirmedMode)
    }

    // 4. Servo 4 Strict Limit (0 - 45 degrees) (Section 3, 18, 60)
    @Test
    fun testServo4Limits() {
        val statusOver = RobotStatus(servo4Angle = 90)
        assertEquals(45, statusOver.soilServoAngle)

        val statusUnder = RobotStatus(servo4Angle = -10)
        assertEquals(0, statusUnder.soilServoAngle)

        val statusValid = RobotStatus(servo4Angle = 30)
        assertEquals(30, statusValid.soilServoAngle)
    }

    // 5. Bounding-Box Center Calculation (Section 22 & 60)
    @Test
    fun testBoundingBoxCenterCalculation() {
        val detection = DetectionResult(
            targetClass = TargetClass.WEED,
            confidence = 0.92f,
            x = 147f,
            y = 115f,
            width = 55f,
            height = 70f
        )
        // centerX = 147 + 55 / 2 = 174.5
        // centerY = 115 + 70 / 2 = 150.0
        assertEquals(174.5f, detection.centerX, 0.001f)
        assertEquals(150.0f, detection.centerY, 0.001f)
    }

    // 6. Inverse Kinematics Calculation (Section 22 & 60)
    @Test
    fun testKinematicsCalculation() {
        val settings = AppSettings()
        val angles = KinematicsCalculator.calculateArmSolution(
            pixelCenterX = 320f,
            pixelCenterY = 240f,
            imageWidth = 640f,
            imageHeight = 480f,
            settings = settings
        )
        // Optical center should yield straight ahead base angle (90 deg)
        assertEquals(90, angles.servo1BaseAngle)
        assertTrue("Shoulder angle within physical bounds", angles.servo2ShoulderAngle in 0..180)
        assertTrue("Wrist angle within physical bounds", angles.servo3WristAngle in 0..180)
    }

    // 7. Weed Confirmation Consecutive Counting & Filter (Section 23 & 60)
    @Test
    fun testWeedConfirmationWorkflow() {
        val engine = AiVisionEngine()
        val weedList = listOf(
            DetectionResult(targetClass = TargetClass.WEED, confidence = 0.85f, x = 100f, y = 100f, width = 50f, height = 40f)
        )

        // 1st detection
        var status = engine.processWeedConfirmation(weedList, requiredConsecutive = 3)
        assertEquals(1, status.currentCount)
        assertFalse(status.isConfirmed)

        // 2nd detection
        status = engine.processWeedConfirmation(weedList, requiredConsecutive = 3)
        assertEquals(2, status.currentCount)
        assertFalse(status.isConfirmed)

        // 3rd detection -> CONFIRMED
        status = engine.processWeedConfirmation(weedList, requiredConsecutive = 3)
        assertEquals(3, status.currentCount)
        assertTrue(status.isConfirmed)
        assertNotNull(status.confirmedWeed)

        // Absence of weed resets counter
        status = engine.processWeedConfirmation(emptyList(), requiredConsecutive = 3)
        assertEquals(0, status.currentCount)
        assertFalse(status.isConfirmed)
    }

    // 8. Onion Protection & Unknown Targets (Section 23 & 60)
    @Test
    fun testOnionProtectionAndUnknown() {
        val onion = DetectionResult(targetClass = TargetClass.ONION, confidence = 0.95f, x = 100f, y = 100f, width = 40f, height = 120f)
        assertEquals("ONION PROTECTED", onion.actionDescription)

        val unknown = DetectionResult(targetClass = TargetClass.UNKNOWN, confidence = 0.50f, x = 100f, y = 100f, width = 40f, height = 40f)
        assertEquals("UNKNOWN TARGET - IGNORED", unknown.actionDescription)
    }

    // 9. Water Pump Lockout when Critical Water (Section 19, 30, 60)
    @Test
    fun testWaterPumpLockout() {
        val normalWaterStatus = RobotStatus(waterLevel = 60)
        assertFalse(normalWaterStatus.isWaterCritical)

        val lowWaterStatus = RobotStatus(waterLevel = 25)
        assertEquals("LOW", lowWaterStatus.waterState)
        assertFalse(lowWaterStatus.isWaterCritical)

        val criticalWaterStatus = RobotStatus(waterLevel = 10)
        assertEquals("CRITICAL", criticalWaterStatus.waterState)
        assertTrue(criticalWaterStatus.isWaterCritical)
        assertTrue(criticalWaterStatus.isSafetyCritical)
    }

    // 10. Battery Safety Cutoff (Section 33 & 60)
    @Test
    fun testBatterySafety() {
        val normalBattery = RobotStatus(batteryVoltage = 12.6, batteryPercent = 90)
        assertFalse(normalBattery.isBatteryCritical)

        val criticalBattery = RobotStatus(batteryVoltage = 10.2, batteryPercent = 8)
        assertTrue(criticalBattery.isBatteryCritical)
        assertTrue(criticalBattery.isSafetyCritical)
    }

    // 11. MPU6050 Safety (Section 34 & 60)
    @Test
    fun testMpuSafety() {
        val stableMpu = RobotStatus(mpuStable = true)
        assertFalse(stableMpu.isMpuFault)

        val unstableMpu = RobotStatus(mpuStable = false)
        assertTrue(unstableMpu.isMpuFault)
        assertTrue(unstableMpu.isSafetyCritical)
    }

    // 12. Vibration Safety (Section 31, 32, 60)
    @Test
    fun testVibrationSafety() {
        val normalVib = RobotStatus(vibrationCritical = false, vibrationDetected = false)
        assertFalse(normalVib.isVibrationCriticalState)

        val critVib = RobotStatus(vibrationCritical = true)
        assertTrue(critVib.isVibrationCriticalState)
        assertTrue(critVib.isSafetyCritical)
    }

    // 13. Camera & ESP32 Base URL generation (Section 27 & 60)
    @Test
    fun testAppSettingsUrls() {
        val settings = AppSettings(
            esp32Ip = "192.168.1.50",
            esp32Port = 80,
            cameraIp = "192.168.1.51",
            cameraStreamPort = 81,
            cameraStreamEndpoint = "/stream"
        )
        assertEquals("http://192.168.1.50", settings.esp32BaseUrl)
        assertEquals("http://192.168.1.51:81/stream", settings.cameraStreamUrl)

        val customPortSettings = AppSettings(
            esp32Ip = "10.0.0.45",
            esp32Port = 8080
        )
        assertEquals("http://10.0.0.45:8080", customPortSettings.esp32BaseUrl)
    }
}
