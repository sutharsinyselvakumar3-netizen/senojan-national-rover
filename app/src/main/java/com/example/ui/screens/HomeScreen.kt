package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DeviceConnectionState
import com.example.data.model.RobotMode
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    viewModel: RobotViewModel,
    onNavigateToCameraSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val modeColors = LocalRobotModeColors.current
    val robotMode by viewModel.robotMode.collectAsState()
    val robotStatus by viewModel.robotStatus.collectAsState()
    val esp32State by viewModel.esp32ConnectionState.collectAsState()
    val cameraState by viewModel.cameraConnectionState.collectAsState()
    val aiReady by viewModel.aiReady.collectAsState()
    val armStatus by viewModel.armStatus.collectAsState()
    val frame by viewModel.cameraStreamer.currentFrame.collectAsState()
    val isStreaming by viewModel.cameraStreamer.isStreaming.collectAsState()
    val fps by viewModel.cameraStreamer.fps.collectAsState()
    val streamError by viewModel.cameraStreamer.streamError.collectAsState()
    val alerts by viewModel.alerts.collectAsState()

    val latestAlert = alerts.firstOrNull()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(RoyalWhite)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("home_screen")
    ) {
        // Critical Safety Banner
        SafetyBanner(robotStatus = robotStatus)

        // Live Camera Preview Card
        Text(
            text = "LIVE CAMERA FEED",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = modeColors.headingColor
        )
        Spacer(modifier = Modifier.height(6.dp))

        CameraStreamView(
            frame = frame,
            isStreaming = isStreaming,
            connectionState = cameraState,
            fps = fps,
            streamError = streamError,
            onDoubleTapRestart = { viewModel.restartCameraStream() },
            onRetryClick = { viewModel.startCameraStream() },
            onOpenSettingsClick = onNavigateToCameraSettings
        )

        Spacer(modifier = Modifier.height(18.dp))

        // System Telemetry Grid Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ROBOT HARDWARE TELEMETRY",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = modeColors.headingColor
            )

            val safetyText = robotStatus?.safetyStatusText ?: "UNKNOWN"
            val safetyColor = when (safetyText) {
                "SAFE" -> StatusSafe
                "WARNING" -> StatusWarning
                "CRITICAL" -> StatusCritical
                else -> StatusUnknown
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(safetyColor.copy(alpha = 0.15f))
                    .border(1.dp, safetyColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "SAFETY: $safetyText",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = safetyColor
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Telemetry Grid
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val itemWidth = Modifier.weight(1f)

            // BATTERY CARD
            TelemetryCard(
                title = "BATTERY",
                value = robotStatus?.batteryPercentDisplay ?: "UNKNOWN",
                subtext = robotStatus?.batteryVoltageDisplay ?: "UNKNOWN",
                icon = Icons.Default.BatteryChargingFull,
                iconColor = if (robotStatus?.isBatteryCritical == true) StatusCritical else modeColors.primaryAccent,
                modifier = itemWidth
            )

            // SOIL MOISTURE CARD
            TelemetryCard(
                title = "SOIL MOISTURE",
                value = robotStatus?.soilMoistureDisplay ?: "UNKNOWN",
                subtext = if (robotStatus?.soilSensorValid == true) "Sensor: OK" else "Sensor: UNKNOWN",
                icon = Icons.Default.Grass,
                iconColor = modeColors.primaryAccent,
                modifier = itemWidth
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val itemWidth = Modifier.weight(1f)

            // WATER LEVEL CARD
            val waterState = robotStatus?.waterState ?: "UNKNOWN"
            TelemetryCard(
                title = "WATER LEVEL",
                value = robotStatus?.waterLevelDisplay ?: "UNKNOWN",
                subtext = "State: $waterState",
                icon = Icons.Default.WaterDrop,
                iconColor = if (robotStatus?.isWaterCritical == true) StatusCritical else modeColors.primaryAccent,
                modifier = itemWidth
            )

            // MPU6050 INERTIAL CARD
            val mpuStable = robotStatus?.mpuStable
            val mpuText = if (mpuStable == true) "Stable" else if (mpuStable == false) "UNSTABLE" else "UNKNOWN"
            TelemetryCard(
                title = "MPU6050 IMU",
                value = mpuText,
                subtext = "P: ${robotStatus?.pitchDisplay ?: "U"} • R: ${robotStatus?.rollDisplay ?: "U"}",
                icon = Icons.Default.CompassCalibration,
                iconColor = if (mpuStable == false) StatusCritical else modeColors.primaryAccent,
                modifier = itemWidth
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val itemWidth = Modifier.weight(1f)

            // VIBRATION CARD
            val vibCritical = robotStatus?.vibrationCritical == true
            val vibDetected = robotStatus?.vibrationDetected == true
            val vibText = when {
                vibCritical -> "CRITICAL"
                vibDetected -> "DETECTED"
                robotStatus?.vibrationSensorValid == true -> "NORMAL"
                else -> "UNKNOWN"
            }
            TelemetryCard(
                title = "VIBRATION",
                value = vibText,
                subtext = if (vibCritical) "Auto Stop Enforced" else "Chassis Monitoring",
                icon = Icons.Default.Vibration,
                iconColor = if (vibCritical) StatusCritical else if (vibDetected) StatusWarning else modeColors.primaryAccent,
                modifier = itemWidth
            )

            // ARM & DRILL CARD
            val armState = robotStatus?.armState ?: armStatus.state.name
            val drillText = if (robotStatus?.drillOn == true || armStatus.drillOn) "ON" else "OFF"
            TelemetryCard(
                title = "ARM & DRILL",
                value = armState,
                subtext = "Drill: $drillText • Pan: ${robotStatus?.panAngle ?: 90}°",
                icon = Icons.Default.PrecisionManufacturing,
                iconColor = modeColors.primaryAccent,
                modifier = itemWidth
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // RELAYS STATUS OVERVIEW CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardSurfaceWhite),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderLight),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "ACTUATOR & RELAY STATES",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = modeColors.headingColor
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    RelayIndicatorPill("R1 Drill", robotStatus?.drillOn == true || armStatus.drillOn)
                    RelayIndicatorPill("R2 Soil", robotStatus?.relay2 == true)
                    RelayIndicatorPill("R3 Water", robotStatus?.relay3 == true)
                    RelayIndicatorPill("R4 Siren", robotStatus?.sirenOn == true || robotStatus?.relay4 == true)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // LATEST ALERT TICKER
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardSurfaceWhite),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderLight),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = if (latestAlert?.severity?.name == "CRITICAL") StatusCritical else modeColors.primaryAccent,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "LATEST ALERT",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )
                    Text(
                        text = latestAlert?.message ?: "System operational. No active hardware faults.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextDark
                    )
                }
                if (latestAlert != null) {
                    Text(
                        text = latestAlert.formattedTime,
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun TelemetryCard(
    title: String,
    value: String,
    subtext: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    val modeColors = LocalRobotModeColors.current

    Card(
        modifier = modifier.testTag("telemetry_card_${title.lowercase()}"),
        colors = CardDefaults.cardColors(containerColor = CardSurfaceWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderLight),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = modeColors.headingColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtext,
                fontSize = 10.sp,
                color = TextMuted
            )
        }
    }
}

@Composable
fun RelayIndicatorPill(name: String, active: Boolean) {
    val modeColors = LocalRobotModeColors.current

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (active) modeColors.primaryAccent else CardBorderLight)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = "$name: ${if (active) "ON" else "OFF"}",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (active) PureWhite else TextDark
        )
    }
}
