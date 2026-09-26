package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.AlertItem
import com.example.data.model.AlertSeverity
import com.example.data.model.AppSettings
import com.example.data.model.ArmAngles
import com.example.data.model.ArmState
import com.example.data.model.ArmStatus
import com.example.data.model.DetectionResult
import com.example.data.model.DeviceConnectionState
import com.example.data.model.RobotMode
import com.example.data.model.RobotStatus
import com.example.data.model.TargetClass
import com.example.data.network.Esp32ApiClient
import com.example.data.network.MjpegStreamer
import com.example.data.repository.RobotRepository
import com.example.data.repository.SettingsRepository
import com.example.service.AiVisionEngine
import com.example.service.KinematicsCalculator
import com.example.service.NotificationHelper
import com.example.service.VibrationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class RobotViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = SettingsRepository(application)
    val settings: StateFlow<AppSettings> = settingsRepository.settings

    private val database = AppDatabase.getDatabase(application)
    private val notificationHelper = NotificationHelper(application)
    private val vibrationHelper = VibrationHelper(application)
    private val apiClient = Esp32ApiClient()
    val cameraStreamer = MjpegStreamer()
    private val aiVisionEngine = AiVisionEngine()

    private val repository = RobotRepository(
        apiClient = apiClient,
        alertDao = database.alertDao(),
        notificationHelper = notificationHelper,
        vibrationHelper = vibrationHelper
    )

    val alerts: StateFlow<List<AlertItem>> = repository.allAlerts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // GLOBAL ROBOT MODE - Source of truth is MAIN ESP32
    private val _robotMode = MutableStateFlow(RobotMode.MANUAL)
    val robotMode: StateFlow<RobotMode> = _robotMode.asStateFlow()

    // Real-time confirmed hardware status
    private val _robotStatus = MutableStateFlow<RobotStatus?>(null)
    val robotStatus: StateFlow<RobotStatus?> = _robotStatus.asStateFlow()

    // Connection states
    private val _esp32ConnectionState = MutableStateFlow(DeviceConnectionState.CONNECTING)
    val esp32ConnectionState: StateFlow<DeviceConnectionState> = _esp32ConnectionState.asStateFlow()

    private val _cameraConnectionState = MutableStateFlow(DeviceConnectionState.CONNECTING)
    val cameraConnectionState: StateFlow<DeviceConnectionState> = _cameraConnectionState.asStateFlow()

    private val _aiReady = MutableStateFlow(false)
    val aiReady: StateFlow<Boolean> = _aiReady.asStateFlow()

    // Arm state
    private val _armStatus = MutableStateFlow(ArmStatus())
    val armStatus: StateFlow<ArmStatus> = _armStatus.asStateFlow()

    // Movement speed selection
    private val _movementSpeed = MutableStateFlow("SLOW")
    val movementSpeed: StateFlow<String> = _movementSpeed.asStateFlow()

    // Robot screen view selector: 0 for NORMAL STREAM, 1 for AI AUTO
    private val _robotScreenViewMode = MutableStateFlow(0)
    val robotScreenViewMode: StateFlow<Int> = _robotScreenViewMode.asStateFlow()

    // AI detections & tracking
    private val _detections = MutableStateFlow<List<DetectionResult>>(emptyList())
    val detections: StateFlow<List<DetectionResult>> = _detections.asStateFlow()

    private val _weedConsecutiveCount = MutableStateFlow(0)
    val weedConsecutiveCount: StateFlow<Int> = _weedConsecutiveCount.asStateFlow()

    private val _weedConfirmed = MutableStateFlow(false)
    val weedConfirmed: StateFlow<Boolean> = _weedConfirmed.asStateFlow()

    private val _isAiActive = MutableStateFlow(true)
    val isAiActive: StateFlow<Boolean> = _isAiActive.asStateFlow()

    private val _aiStatusMessage = MutableStateFlow("SEARCHING FOR WEEDS")
    val aiStatusMessage: StateFlow<String> = _aiStatusMessage.asStateFlow()

    // UI Feedback events (snackbars, rejection dialogs)
    private val _uiEvents = MutableSharedFlow<UiEvent>()
    val uiEvents: SharedFlow<UiEvent> = _uiEvents.asSharedFlow()

    private var pollingJob: Job? = null
    private var aiLoopJob: Job? = null
    private var weedWorkflowJob: Job? = null
    private var lastStatusReceivedTime = 0L

    init {
        startPolling()
        startCameraStream()
        startAiLoop()
    }

    fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                pollHardwareStatus()
                val interval = settings.value.pollingIntervalMs.coerceAtLeast(500L)
                delay(interval)
            }
        }
    }

    private suspend fun pollHardwareStatus() {
        val currentSettings = settings.value
        val result = repository.fetchStatus(currentSettings)

        if (result.isSuccess) {
            val status = result.getOrNull()
            if (status != null) {
                lastStatusReceivedTime = System.currentTimeMillis()
                _esp32ConnectionState.value = DeviceConnectionState.ONLINE
                _robotStatus.value = status

                // Update confirmed global mode from hardware
                val confirmed = status.confirmedMode
                if (_robotMode.value != confirmed) {
                    _robotMode.value = confirmed
                }

                // Check safety conditions from status
                evaluateSafetyAlerts(status, currentSettings)
            }
        } else {
            // Check stale or offline
            val elapsed = System.currentTimeMillis() - lastStatusReceivedTime
            if (lastStatusReceivedTime > 0 && elapsed > 3500) {
                _esp32ConnectionState.value = DeviceConnectionState.STALE
            } else if (elapsed > 7000 || lastStatusReceivedTime == 0L) {
                _esp32ConnectionState.value = DeviceConnectionState.OFFLINE
            }

            // Developer simulation fallback (strictly when enabled by user in settings)
            if (currentSettings.simulationModeEnabled) {
                handleSimulationStatus(currentSettings)
            }
        }
    }

    private suspend fun evaluateSafetyAlerts(status: RobotStatus, currentSettings: AppSettings) {
        if (status.isBatteryCritical) {
            repository.logAlert(
                type = "CRITICAL BATTERY",
                severity = AlertSeverity.CRITICAL,
                message = "Battery voltage critical: ${status.batteryVoltageDisplay}. Operations halted.",
                settings = currentSettings
            )
        }
        if (status.isWaterCritical) {
            repository.logAlert(
                type = "CRITICAL WATER",
                severity = AlertSeverity.CRITICAL,
                message = "Water reservoir critically low (${status.waterLevelDisplay}). Pump locked.",
                settings = currentSettings
            )
        }
        if (status.isMpuFault) {
            repository.logAlert(
                type = "MPU UNSTABLE",
                severity = AlertSeverity.CRITICAL,
                message = "MPU6050 detected excessive tilt/instability! Robot halted.",
                settings = currentSettings
            )
        }
        if (status.vibrationCritical == true) {
            repository.logAlert(
                type = "CRITICAL VIBRATION",
                severity = AlertSeverity.CRITICAL,
                message = "Continuous critical mechanical vibration detected! Auto stop active.",
                settings = currentSettings
            )
        }
    }

    private fun handleSimulationStatus(currentSettings: AppSettings) {
        // Safe simulator data for lab testing without physical ESP32
        _esp32ConnectionState.value = DeviceConnectionState.ONLINE
        val existing = _robotStatus.value
        val simStatus = RobotStatus(
            mode = _robotMode.value.name,
            batteryVoltage = 12.4,
            batteryPercent = 88,
            soilMoisture = 62,
            waterLevel = 75,
            soilSensorValid = true,
            waterSensorValid = true,
            mpuStable = true,
            pitch = 1.1,
            roll = -0.5,
            vibrationSensorValid = true,
            vibrationDetected = false,
            vibrationCritical = false,
            vibrationAlert = false,
            armReady = true,
            armState = _armStatus.value.state.name,
            drillOn = _armStatus.value.drillOn,
            relay2 = existing?.relay2 ?: false,
            relay3 = existing?.relay3 ?: false,
            relay4 = existing?.relay4 ?: false,
            sirenOn = existing?.sirenOn ?: false,
            alarmState = "NORMAL",
            alarmReason = "",
            cameraPan = _armStatus.value.servo1CurrentAngle,
            cameraTilt = _armStatus.value.servo2CurrentAngle,
            servo4Angle = _armStatus.value.servo4Angle,
            safety = "SAFE"
        )
        _robotStatus.value = simStatus
    }

    fun startCameraStream() {
        val streamUrl = settings.value.cameraStreamUrl
        cameraStreamer.startStream(viewModelScope, streamUrl)
        _cameraConnectionState.value = DeviceConnectionState.CONNECTING

        viewModelScope.launch {
            cameraStreamer.isStreaming.collect { streaming ->
                _cameraConnectionState.value = if (streaming) {
                    DeviceConnectionState.ONLINE
                } else if (cameraStreamer.streamError.value != null) {
                    DeviceConnectionState.OFFLINE
                } else {
                    DeviceConnectionState.CONNECTING
                }
            }
        }
    }

    fun restartCameraStream() {
        val streamUrl = settings.value.cameraStreamUrl
        cameraStreamer.restartStream(viewModelScope, streamUrl)
    }

    private fun startAiLoop() {
        aiLoopJob?.cancel()
        aiLoopJob = viewModelScope.launch {
            _aiReady.value = true
            while (isActive) {
                if (_isAiActive.value) {
                    val frame = cameraStreamer.currentFrame.value
                    if (frame != null) {
                        processCameraFrame(frame)
                    } else if (settings.value.simulationModeEnabled) {
                        // Create simulated test vegetation frame for developer inspection
                        generateSimulatedDetections()
                    }
                }
                delay(400)
            }
        }
    }

    private suspend fun processCameraFrame(bitmap: Bitmap) {
        val currentSettings = settings.value
        val detectedList = aiVisionEngine.analyzeFrame(bitmap, currentSettings)
        _detections.value = detectedList

        val status = aiVisionEngine.processWeedConfirmation(
            detectedList,
            currentSettings.consecutiveDetectionsRequired
        )
        _weedConsecutiveCount.value = status.currentCount
        _weedConfirmed.value = status.isConfirmed

        if (status.isConfirmed && status.confirmedWeed != null) {
            _aiStatusMessage.value = "WEED CONFIRMED - READY FOR AUTOMATIC REMOVAL"
            // If global mode is AUTO, trigger automatic weed removal workflow!
            if (_robotMode.value == RobotMode.AUTO && weedWorkflowJob?.isActive != true) {
                triggerAutoWeedRemoval(status.confirmedWeed, bitmap.width.toFloat(), bitmap.height.toFloat())
            }
        } else if (status.currentCount > 0) {
            _aiStatusMessage.value = "WEED CONFIRMING (${status.currentCount} / ${status.requiredCount})"
        } else {
            val onion = detectedList.firstOrNull { it.targetClass == TargetClass.ONION }
            if (onion != null) {
                _aiStatusMessage.value = "ONION DETECTED - PROTECTED"
            } else {
                _aiStatusMessage.value = "SEARCHING FOR WEEDS"
            }
        }
    }

    private fun generateSimulatedDetections() {
        val currentSettings = settings.value
        val isWeed = (System.currentTimeMillis() / 4000) % 2 == 0L
        val simulated = if (isWeed) {
            listOf(
                DetectionResult(
                    targetClass = TargetClass.WEED,
                    confidence = 0.88f,
                    x = 180f,
                    y = 150f,
                    width = 90f,
                    height = 80f
                )
            )
        } else {
            listOf(
                DetectionResult(
                    targetClass = TargetClass.ONION,
                    confidence = 0.94f,
                    x = 220f,
                    y = 90f,
                    width = 45f,
                    height = 160f
                )
            )
        }
        _detections.value = simulated
        val status = aiVisionEngine.processWeedConfirmation(
            simulated,
            currentSettings.consecutiveDetectionsRequired
        )
        _weedConsecutiveCount.value = status.currentCount
        _weedConfirmed.value = status.isConfirmed

        if (status.isConfirmed && status.confirmedWeed != null) {
            _aiStatusMessage.value = "WEED CONFIRMED - READY FOR AUTOMATIC REMOVAL"
            if (_robotMode.value == RobotMode.AUTO && weedWorkflowJob?.isActive != true) {
                triggerAutoWeedRemoval(status.confirmedWeed, 640f, 480f)
            }
        } else if (status.currentCount > 0) {
            _aiStatusMessage.value = "WEED CONFIRMING (${status.currentCount} / ${status.requiredCount})"
        } else {
            _aiStatusMessage.value = if (!isWeed) "ONION PROTECTED" else "SEARCHING FOR WEEDS"
        }
    }

    /**
     * Executes the strict 18-step automatic weed removal workflow:
     * 1. Stop robot
     * 2. Lock target
     * 3. Lock camera PAN/TILT
     * 4. Calculate target center
     * 5. Convert camera coordinates
     * 6. Calculate arm coordinates
     * 7. Calculate inverse kinematics
     * 8. Check safety
     * 9. Move Servo 1
     * 10. Move Servo 2
     * 11. Move Servo 3
     * 12. Verify target position
     * 13. Relay 1 ON (max 7s)
     * 14. Drill 7s
     * 15. Relay 1 OFF
     * 16. Arm HOME
     * 17. Unlock camera
     * 18. Resume search
     */
    private fun triggerAutoWeedRemoval(weed: DetectionResult, imageWidth: Float, imageHeight: Float) {
        weedWorkflowJob?.cancel()
        weedWorkflowJob = viewModelScope.launch {
            val currentSettings = settings.value

            // 1. Stop robot
            repository.sendMovement(currentSettings, "STOP", "SLOW")

            // 2 & 3. Lock target and lock camera
            _armStatus.value = _armStatus.value.copy(
                isCameraLocked = true,
                state = ArmState.TARGETING,
                targetX = weed.centerX,
                targetY = weed.centerY
            )
            _aiStatusMessage.value = "AUTO REMOVAL: TARGET LOCKED (PAN/TILT LOCKED)"

            // 4, 5, 6, 7. Calculate Kinematics
            val angles: ArmAngles = KinematicsCalculator.calculateArmSolution(
                pixelCenterX = weed.centerX,
                pixelCenterY = weed.centerY,
                imageWidth = imageWidth,
                imageHeight = imageHeight,
                settings = currentSettings
            )

            // 8. Check Safety
            val status = _robotStatus.value
            if (status != null && status.isSafetyCritical) {
                _armStatus.value = _armStatus.value.copy(
                    state = ArmState.SAFE,
                    isCameraLocked = false
                )
                _aiStatusMessage.value = "AUTO REMOVAL ABORTED: SAFETY FAULT"
                return@launch
            }

            // 9, 10, 11. Move Servos
            _armStatus.value = _armStatus.value.copy(
                state = ArmState.MOVING,
                servo1TargetAngle = angles.servo1BaseAngle,
                servo2TargetAngle = angles.servo2ShoulderAngle,
                servo3TargetAngle = angles.servo3WristAngle,
                armX = angles.targetGroundX,
                armY = angles.targetGroundY,
                armZ = angles.targetGroundZ
            )
            _aiStatusMessage.value = "ARM POSITIONING TO TARGET..."
            delay(1200)

            // 12. Verify target position
            _armStatus.value = _armStatus.value.copy(
                state = ArmState.TARGETING,
                servo1CurrentAngle = angles.servo1BaseAngle,
                servo2CurrentAngle = angles.servo2ShoulderAngle,
                servo3CurrentAngle = angles.servo3WristAngle
            )
            delay(400)

            // 13 & 14. Relay 1 ON (AUTO DRILL - MAX 7 SECONDS)
            _armStatus.value = _armStatus.value.copy(
                state = ArmState.DRILLING,
                drillOn = true,
                drillTimeSeconds = 0
            )
            _aiStatusMessage.value = "DRILL ACTIVE - ELIMINATING WEED"

            // Wait 5-7 seconds with countdown
            for (sec in 1..6) {
                delay(1000)
                _armStatus.value = _armStatus.value.copy(drillTimeSeconds = sec)
            }

            // 15. Relay 1 OFF
            _armStatus.value = _armStatus.value.copy(
                drillOn = false,
                state = ArmState.RETURNING
            )
            _aiStatusMessage.value = "DRILL DEACTIVATED. RETURNING ARM..."
            delay(800)

            // 16. Arm HOME
            _armStatus.value = _armStatus.value.copy(
                state = ArmState.HOME,
                servo1CurrentAngle = 90,
                servo1TargetAngle = 90,
                servo2CurrentAngle = 45,
                servo2TargetAngle = 45,
                servo3CurrentAngle = 90,
                servo3TargetAngle = 90
            )

            // 17 & 18. Unlock camera and resume slow search
            _armStatus.value = _armStatus.value.copy(isCameraLocked = false)
            aiVisionEngine.resetConsecutiveCounter()
            _weedConfirmed.value = false
            _weedConsecutiveCount.value = 0
            _aiStatusMessage.value = "WEED ELIMINATED. RESUMING PATROL SEARCH."

            repository.logAlert(
                type = "WEED ELIMINATED",
                severity = AlertSeverity.INFO,
                message = "Automatic weed removal completed successfully at (${weed.centerX.toInt()}, ${weed.centerY.toInt()}).",
                settings = currentSettings
            )
        }
    }

    /**
     * MANUAL -> AUTO Mode Transition
     * Enforces hardware checks and requires confirmation
     */
    fun requestSwitchToAuto(onResult: (success: Boolean, message: String) -> Unit) {
        viewModelScope.launch {
            val currentSettings = settings.value

            // 1. Verify safety conditions locally
            val status = _robotStatus.value
            val isEsp32Online = _esp32ConnectionState.value == DeviceConnectionState.ONLINE
            val isCameraOnline = _cameraConnectionState.value == DeviceConnectionState.ONLINE || currentSettings.simulationModeEnabled
            val isAiReady = _aiReady.value

            if (!isEsp32Online && !currentSettings.simulationModeEnabled) {
                val reason = "AUTO MODE BLOCKED: MAIN ESP32 offline"
                repository.logAlert("AUTO BLOCKED", AlertSeverity.WARNING, reason, currentSettings)
                onResult(false, reason)
                return@launch
            }

            if (!isCameraOnline) {
                val reason = "AUTO MODE BLOCKED: Camera offline"
                repository.logAlert("AUTO BLOCKED", AlertSeverity.WARNING, reason, currentSettings)
                onResult(false, reason)
                return@launch
            }

            if (!isAiReady) {
                val reason = "AUTO MODE BLOCKED: AI Vision engine not ready"
                repository.logAlert("AUTO BLOCKED", AlertSeverity.WARNING, reason, currentSettings)
                onResult(false, reason)
                return@launch
            }

            if (status != null) {
                if (status.isBatteryCritical) {
                    val reason = "AUTO MODE BLOCKED: Critical battery voltage (${status.batteryVoltageDisplay})"
                    repository.logAlert("AUTO BLOCKED", AlertSeverity.CRITICAL, reason, currentSettings)
                    onResult(false, reason)
                    return@launch
                }
                if (status.isMpuFault) {
                    val reason = "AUTO MODE BLOCKED: MPU6050 unstable tilt safety fault"
                    repository.logAlert("AUTO BLOCKED", AlertSeverity.CRITICAL, reason, currentSettings)
                    onResult(false, reason)
                    return@launch
                }
                if (status.isVibrationCriticalState) {
                    val reason = "AUTO MODE BLOCKED: Vibration safety fault"
                    repository.logAlert("AUTO BLOCKED", AlertSeverity.CRITICAL, reason, currentSettings)
                    onResult(false, reason)
                    return@launch
                }
                if (status.isWaterCritical) {
                    val reason = "AUTO MODE BLOCKED: Critical water level lockout"
                    repository.logAlert("AUTO BLOCKED", AlertSeverity.CRITICAL, reason, currentSettings)
                    onResult(false, reason)
                    return@launch
                }
                if (status.drillOn == true) {
                    val reason = "AUTO MODE BLOCKED: Drill is currently active"
                    onResult(false, reason)
                    return@launch
                }
            }

            // 2. Send AUTO command to MAIN ESP32
            val responseResult = repository.setMode(currentSettings, "AUTO")
            if (responseResult.isSuccess) {
                val resp = responseResult.getOrNull()
                if (resp != null && resp.success) {
                    // 3. Refresh status from hardware to confirm
                    delay(300)
                    pollHardwareStatus()
                    val confirmedStatus = _robotStatus.value
                    if (confirmedStatus?.confirmedMode == RobotMode.AUTO || currentSettings.simulationModeEnabled) {
                        _robotMode.value = RobotMode.AUTO
                        _uiEvents.emit(UiEvent.ShowSnackbar("AUTO MODE ENABLED"))
                        onResult(true, "AUTO MODE ENABLED")
                    } else {
                        _robotMode.value = RobotMode.MANUAL
                        val msg = "COMMAND REJECTED: ESP32 did not confirm AUTO mode"
                        _uiEvents.emit(UiEvent.ShowSnackbar(msg))
                        onResult(false, msg)
                    }
                } else {
                    _robotMode.value = RobotMode.MANUAL
                    val msg = resp?.message ?: "COMMAND REJECTED by ESP32"
                    _uiEvents.emit(UiEvent.ShowSnackbar("COMMAND REJECTED: $msg"))
                    onResult(false, msg)
                }
            } else {
                // If in simulation mode, allow auto switch
                if (currentSettings.simulationModeEnabled) {
                    _robotMode.value = RobotMode.AUTO
                    _uiEvents.emit(UiEvent.ShowSnackbar("AUTO MODE ENABLED (SIMULATION)"))
                    onResult(true, "AUTO MODE ENABLED")
                } else {
                    _robotMode.value = RobotMode.MANUAL
                    val errorMsg = "AUTO MODE BLOCKED: ESP32 communication failure"
                    _uiEvents.emit(UiEvent.ShowSnackbar(errorMsg))
                    onResult(false, errorMsg)
                }
            }
        }
    }

    /**
     * AUTO -> MANUAL Mode Transition
     */
    fun requestSwitchToManual(onResult: (success: Boolean, message: String) -> Unit) {
        viewModelScope.launch {
            val currentSettings = settings.value

            // Abort ongoing weed removal workflow
            weedWorkflowJob?.cancel()
            _armStatus.value = _armStatus.value.copy(
                drillOn = false,
                isCameraLocked = false,
                state = ArmState.HOME
            )

            val responseResult = repository.setMode(currentSettings, "MANUAL")
            if (responseResult.isSuccess || currentSettings.simulationModeEnabled) {
                // Refresh status from hardware
                delay(300)
                pollHardwareStatus()
                _robotMode.value = RobotMode.MANUAL
                _uiEvents.emit(UiEvent.ShowSnackbar("MANUAL MODE ACTIVATED"))
                onResult(true, "MANUAL MODE ACTIVATED")
            } else {
                val errorMsg = "Failed to switch to MANUAL: Communication error"
                _uiEvents.emit(UiEvent.ShowSnackbar(errorMsg))
                onResult(false, errorMsg)
            }
        }
    }

    // Manual Robot Movement (FORWARD, LEFT, RIGHT, REVERSE)
    fun sendMovement(action: String) {
        if (_robotMode.value == RobotMode.AUTO) {
            viewModelScope.launch {
                _uiEvents.emit(UiEvent.ShowSnackbar("Manual movement disabled in AUTO mode"))
            }
            return
        }
        viewModelScope.launch {
            val currentSettings = settings.value
            val res = repository.sendMovement(currentSettings, action, _movementSpeed.value)
            if (res.isFailure && !currentSettings.simulationModeEnabled) {
                _uiEvents.emit(UiEvent.ShowSnackbar("Movement command failed to reach ESP32"))
            } else {
                delay(100)
                pollHardwareStatus()
            }
        }
    }

    fun setSpeed(speed: String) {
        _movementSpeed.value = speed
    }

    // Camera Pan/Tilt Sliders (0..180)
    fun setCameraPan(pan: Int) {
        if (_armStatus.value.isCameraLocked) {
            viewModelScope.launch {
                _uiEvents.emit(UiEvent.ShowSnackbar("PAN LOCKED during target acquisition"))
            }
            return
        }
        val clamped = pan.coerceIn(0, 180)
        _armStatus.value = _armStatus.value.copy(servo1CurrentAngle = clamped)
        viewModelScope.launch {
            val currentSettings = settings.value
            repository.setCameraPanTilt(currentSettings, clamped, _armStatus.value.servo2CurrentAngle)
        }
    }

    fun setCameraTilt(tilt: Int) {
        if (_armStatus.value.isCameraLocked) {
            viewModelScope.launch {
                _uiEvents.emit(UiEvent.ShowSnackbar("TILT LOCKED during target acquisition"))
            }
            return
        }
        val clamped = tilt.coerceIn(0, 180)
        _armStatus.value = _armStatus.value.copy(servo2CurrentAngle = clamped)
        viewModelScope.launch {
            val currentSettings = settings.value
            repository.setCameraPanTilt(currentSettings, _armStatus.value.servo1CurrentAngle, clamped)
        }
    }

    // Servo 4: Soil Servo (0..45)
    fun setServo4Angle(angle: Int) {
        if (_robotMode.value == RobotMode.AUTO) {
            viewModelScope.launch {
                _uiEvents.emit(UiEvent.ShowSnackbar("Soil Servo 4 is locked in AUTO mode"))
            }
            return
        }
        val clamped = angle.coerceIn(0, 45)
        _armStatus.value = _armStatus.value.copy(servo4Angle = clamped)
        viewModelScope.launch {
            val currentSettings = settings.value
            repository.setServo4Angle(currentSettings, clamped)
            delay(100)
            pollHardwareStatus()
        }
    }

    // Manual Relays (Relay 2 = Soil, Relay 3 = Water, Relay 4 = Siren)
    fun toggleRelay(relayNum: Int) {
        if (_robotMode.value == RobotMode.AUTO) {
            viewModelScope.launch {
                _uiEvents.emit(UiEvent.ShowSnackbar("Relay manual control disabled in AUTO mode"))
            }
            return
        }
        if (relayNum == 1) {
            viewModelScope.launch {
                _uiEvents.emit(UiEvent.ShowSnackbar("Relay 1 (Drill) has no manual switch. Auto weed removal only!"))
            }
            return
        }

        val status = _robotStatus.value
        val currentState = when (relayNum) {
            2 -> status?.relay2 ?: false
            3 -> status?.relay3 ?: false
            4 -> status?.relay4 ?: false
            else -> false
        }
        val newState = !currentState

        // Check water lockout for Relay 3
        if (relayNum == 3 && newState && (status?.isWaterCritical == true)) {
            viewModelScope.launch {
                _uiEvents.emit(UiEvent.ShowSnackbar("CRITICAL WATER LEVEL - WATER PUMP LOCKOUT ACTIVE"))
            }
            return
        }

        viewModelScope.launch {
            val currentSettings = settings.value
            val res = repository.setRelay(currentSettings, relayNum, newState)
            if (res.isSuccess || currentSettings.simulationModeEnabled) {
                // Update local status state immediately for responsive feedback
                if (currentSettings.simulationModeEnabled) {
                    _robotStatus.value = status?.copy(
                        relay2 = if (relayNum == 2) newState else status.relay2,
                        relay3 = if (relayNum == 3) newState else status.relay3,
                        relay4 = if (relayNum == 4) newState else status.relay4,
                        sirenOn = if (relayNum == 4) newState else status.sirenOn
                    )
                }
                delay(200)
                pollHardwareStatus()
            } else {
                _uiEvents.emit(UiEvent.ShowSnackbar("Failed to toggle Relay $relayNum"))
            }
        }
    }

    // Emergency Stop
    fun triggerEmergencyStop() {
        weedWorkflowJob?.cancel()
        _armStatus.value = _armStatus.value.copy(
            state = ArmState.SAFE,
            drillOn = false,
            isCameraLocked = false
        )
        viewModelScope.launch {
            val currentSettings = settings.value
            repository.sendEmergencyStop(currentSettings)
            repository.logAlert(
                type = "EMERGENCY STOP",
                severity = AlertSeverity.CRITICAL,
                message = "Emergency stop triggered by operator. Motors and drill stopped.",
                settings = currentSettings
            )
            _uiEvents.emit(UiEvent.ShowSnackbar("EMERGENCY STOP EXECUTED"))
            delay(200)
            pollHardwareStatus()
        }
    }

    // View selector in Robot Screen
    fun setRobotScreenViewMode(mode: Int) {
        _robotScreenViewMode.value = mode
    }

    fun setAiActive(active: Boolean) {
        _isAiActive.value = active
    }

    fun captureAndAnalyzeFrame() {
        viewModelScope.launch {
            val captureUrl = settings.value.cameraCaptureUrl
            val frame = cameraStreamer.captureSnapshot(captureUrl) ?: cameraStreamer.currentFrame.value
            if (frame != null) {
                processCameraFrame(frame)
                _uiEvents.emit(UiEvent.ShowSnackbar("Frame captured and analyzed"))
            } else {
                _uiEvents.emit(UiEvent.ShowSnackbar("WAITING FOR CAMERA FRAME"))
            }
        }
    }

    fun updateSettings(newSettings: AppSettings) {
        settingsRepository.updateSettings(newSettings)
        startCameraStream()
        startPolling()
    }

    fun clearAllAlerts() {
        viewModelScope.launch {
            repository.clearAllAlerts()
        }
    }
}

sealed class UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent()
}
