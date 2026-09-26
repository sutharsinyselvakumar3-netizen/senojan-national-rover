package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RobotMode
import com.example.data.model.TargetClass
import com.example.ui.components.AiDetectionOverlay
import com.example.ui.components.CameraStreamView
import com.example.ui.components.SafetyBanner
import com.example.ui.theme.CardBorderLight
import com.example.ui.theme.CardSurfaceWhite
import com.example.ui.theme.LocalRobotModeColors
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalWhite
import com.example.ui.theme.StatusCritical
import com.example.ui.theme.StatusSafe
import com.example.ui.theme.StatusUnknown
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.viewmodel.RobotViewModel

@Composable
fun AiVisionScreen(
    viewModel: RobotViewModel,
    onNavigateToCameraSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val modeColors = LocalRobotModeColors.current
    val robotMode by viewModel.robotMode.collectAsState()
    val robotStatus by viewModel.robotStatus.collectAsState()
    val cameraState by viewModel.cameraConnectionState.collectAsState()
    val armStatus by viewModel.armStatus.collectAsState()
    val detections by viewModel.detections.collectAsState()
    val isAiActive by viewModel.isAiActive.collectAsState()
    val aiStatusMessage by viewModel.aiStatusMessage.collectAsState()
    val weedConsecutiveCount by viewModel.weedConsecutiveCount.collectAsState()
    val weedConfirmed by viewModel.weedConfirmed.collectAsState()
    val settings by viewModel.settings.collectAsState()

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
            .testTag("ai_vision_screen")
    ) {
        // Critical Safety Banner
        SafetyBanner(robotStatus = robotStatus)

        // AI Control Panel (START AI, STOP AI, CAPTURE, ANALYZE) (Section 25)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("ai_controls_card"),
            colors = CardDefaults.cardColors(containerColor = CardSurfaceWhite),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderLight),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AI VISION ENGINE",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = modeColors.headingColor
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isAiActive) StatusSafe.copy(alpha = 0.15f) else CardBorderLight)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isAiActive) "AI ACTIVE" else "AI PAUSED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isAiActive) StatusSafe else TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.setAiActive(true) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("start_ai_button")
                            .height(38.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = modeColors.primaryAccent),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("START AI", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PureWhite)
                    }

                    Button(
                        onClick = { viewModel.setAiActive(false) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("stop_ai_button")
                            .height(38.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = modeColors.containerColor),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp), tint = modeColors.primaryAccent)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("STOP AI", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = modeColors.primaryAccent)
                    }

                    Button(
                        onClick = { viewModel.captureAndAnalyzeFrame() },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("capture_frame_button")
                            .height(38.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = modeColors.primaryAccent),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ANALYZE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PureWhite)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Live Camera View with AI Target Overlay
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
                AiDetectionOverlay(
                    detections = detections,
                    frameWidth = frame?.width?.toFloat() ?: 640f,
                    frameHeight = frame?.height?.toFloat() ?: 480f
                )
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Target Status & Consecutive Confirmation Card (Section 23)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("target_status_card"),
            colors = CardDefaults.cardColors(containerColor = CardSurfaceWhite),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderLight),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "TARGET STATUS & DISCRIMINATION",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = modeColors.headingColor
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(modeColors.containerColor.copy(alpha = 0.5f))
                        .padding(10.dp)
                ) {
                    Text(
                        text = aiStatusMessage,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = modeColors.primaryAccent
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Consecutive detection count badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Weed Confirmation Filter:",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                    Text(
                        text = "$weedConsecutiveCount / ${settings.consecutiveDetectionsRequired} Frames",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (weedConfirmed) StatusCritical else modeColors.primaryAccent
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Classes explanation pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ClassTag("WEED", "CONFIRM -> DRILL", StatusCritical)
                    ClassTag("ONION", "PROTECTED", StatusSafe)
                    ClassTag("UNKNOWN", "IGNORED", StatusUnknown)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Active Detections & Coordinate Calculation Card (Section 22)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardSurfaceWhite),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderLight),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "COORDINATE CONVERSION PIPELINE",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = modeColors.headingColor
                )
                Text(
                    text = "Bounding Box → Center X/Y → Camera Calibration → Ground Coordinates → Kinematics → Servos 1, 2, 3",
                    fontSize = 10.sp,
                    color = TextMuted,
                    lineHeight = 14.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                val primaryTarget = detections.firstOrNull()
                if (primaryTarget != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Pixel Box (X, Y, W, H):", fontSize = 11.sp, color = TextMuted)
                            Text(
                                "(${primaryTarget.x.toInt()}, ${primaryTarget.y.toInt()}, ${primaryTarget.width.toInt()}, ${primaryTarget.height.toInt()})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                        }
                        Column {
                            Text("Calculated Center (X, Y):", fontSize = 11.sp, color = TextMuted)
                            Text(
                                "(${primaryTarget.centerX.toInt()}, ${primaryTarget.centerY.toInt()})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = modeColors.primaryAccent
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Target Class:", fontSize = 11.sp, color = TextMuted)
                            Text(
                                primaryTarget.label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = if (primaryTarget.targetClass == TargetClass.WEED) StatusCritical else StatusSafe
                            )
                        }
                        Column {
                            Text("Confidence:", fontSize = 11.sp, color = TextMuted)
                            Text(
                                "${(primaryTarget.confidence * 100).toInt()}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (frame == null) "WAITING FOR CAMERA FRAME" else "NO TARGETS DETECTED IN CAMERA VIEW",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextMuted
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun ClassTag(title: String, subtitle: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Column {
            Text(title, fontSize = 9.sp, fontWeight = FontWeight.Black, color = color)
            Text(subtitle, fontSize = 8.sp, color = color)
        }
    }
}
