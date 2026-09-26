package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RobotMode
import com.example.ui.components.AiDetectionOverlay
import com.example.ui.components.CameraPanTiltControls
import com.example.ui.components.CameraStreamView
import com.example.ui.components.MovementControls
import com.example.ui.components.RelayControls
import com.example.ui.components.SafetyBanner
import com.example.ui.components.Servo4SoilControl
import com.example.ui.theme.LocalRobotModeColors
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalWhite
import com.example.viewmodel.RobotViewModel

@Composable
fun RobotScreen(
    viewModel: RobotViewModel,
    onNavigateToCameraSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val modeColors = LocalRobotModeColors.current
    val robotMode by viewModel.robotMode.collectAsState()
    val robotStatus by viewModel.robotStatus.collectAsState()
    val cameraState by viewModel.cameraConnectionState.collectAsState()
    val armStatus by viewModel.armStatus.collectAsState()
    val speed by viewModel.movementSpeed.collectAsState()
    val viewMode by viewModel.robotScreenViewMode.collectAsState() // 0: NORMAL STREAM, 1: AI AUTO
    val detections by viewModel.detections.collectAsState()

    val frame by viewModel.cameraStreamer.currentFrame.collectAsState()
    val isStreaming by viewModel.cameraStreamer.isStreaming.collectAsState()
    val fps by viewModel.cameraStreamer.fps.collectAsState()
    val streamError by viewModel.cameraStreamer.streamError.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(RoyalWhite)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("robot_screen")
    ) {
        // Critical Safety Banner
        SafetyBanner(robotStatus = robotStatus)

        // View Mode Selector: [ NORMAL STREAM ] [ AI AUTO ] (Section 13)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.setRobotScreenViewMode(0) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("view_normal_stream_button")
                    .height(42.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (viewMode == 0) modeColors.primaryAccent else modeColors.containerColor,
                    contentColor = if (viewMode == 0) PureWhite else modeColors.primaryAccent
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "NORMAL STREAM",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Button(
                onClick = { viewModel.setRobotScreenViewMode(1) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("view_ai_auto_button")
                    .height(42.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (viewMode == 1) modeColors.primaryAccent else modeColors.containerColor,
                    contentColor = if (viewMode == 1) PureWhite else modeColors.primaryAccent
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "AI AUTO VIEW",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Camera Stream Container
        CameraStreamView(
            frame = frame,
            isStreaming = isStreaming,
            connectionState = cameraState,
            fps = fps,
            streamError = streamError,
            onDoubleTapRestart = { viewModel.restartCameraStream() },
            onRetryClick = { viewModel.startCameraStream() },
            onOpenSettingsClick = onNavigateToCameraSettings,
            overlayContent = {
                if (viewMode == 1) {
                    AiDetectionOverlay(
                        detections = detections,
                        frameWidth = frame?.width?.toFloat() ?: 640f,
                        frameHeight = frame?.height?.toFloat() ?: 480f
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Robot Movement Controls (Section 15 & 16)
        MovementControls(
            robotMode = robotMode,
            currentSpeed = speed,
            onMove = { action -> viewModel.sendMovement(action) },
            onSpeedChange = { s -> viewModel.setSpeed(s) }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Camera PAN / TILT Sliders (Section 17)
        CameraPanTiltControls(
            panAngle = armStatus.servo1CurrentAngle,
            tiltAngle = armStatus.servo2CurrentAngle,
            isLocked = armStatus.isCameraLocked,
            onPanChange = { pan -> viewModel.setCameraPan(pan) },
            onTiltChange = { tilt -> viewModel.setCameraTilt(tilt) }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Servo 4: Soil Servo (0 - 45) (Section 18)
        Servo4SoilControl(
            robotMode = robotMode,
            currentAngle = armStatus.servo4Angle,
            onAngleChange = { angle -> viewModel.setServo4Angle(angle) }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Relays: Relay 2 (Soil), Relay 3 (Water with lockout), Relay 4 (Siren) (Section 19 & 20)
        RelayControls(
            robotMode = robotMode,
            robotStatus = robotStatus,
            onToggleRelay = { relayNum -> viewModel.toggleRelay(relayNum) }
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}
