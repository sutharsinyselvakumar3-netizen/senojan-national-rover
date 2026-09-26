package com.example.ui.components

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DeviceConnectionState
import com.example.data.model.RobotMode
import com.example.ui.theme.LocalRobotModeColors
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalWhite
import com.example.ui.theme.StatusCritical
import com.example.ui.theme.StatusSafe
import com.example.ui.theme.StatusUnknown
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.TextMuted

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TopStatusBar(
    robotMode: RobotMode,
    esp32State: DeviceConnectionState,
    cameraState: DeviceConnectionState,
    aiReady: Boolean,
    onModeClick: () -> Unit,
    onEmergencyStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    val modeColors = LocalRobotModeColors.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("top_status_bar"),
        color = RoyalWhite,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "AI COMPANION",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = modeColors.headingColor,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "See the Weed. Protect the Onion. Work Smart.",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = modeColors.primaryAccent
                    )
                }

                // Emergency Stop Button
                ElevatedButton(
                    onClick = onEmergencyStop,
                    modifier = Modifier
                        .testTag("emergency_stop_button")
                        .height(38.dp),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = StatusCritical,
                        contentColor = PureWhite
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Emergency Stop",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "E-STOP",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Badges row: Mode selector button and device connection pills
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Global Mode pill button (tap to open switch dialog)
                Box(
                    modifier = Modifier
                        .testTag("global_mode_pill")
                        .clip(RoundedCornerShape(16.dp))
                        .background(modeColors.containerColor)
                        .border(1.5.dp, modeColors.primaryAccent, RoundedCornerShape(16.dp))
                        .clickable { onModeClick() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(modeColors.primaryAccent)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "MODE: ${modeColors.modeName}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = modeColors.primaryAccent
                        )
                    }
                }

                // ESP32 Status Pill
                ConnectionPill(
                    label = "ESP32",
                    state = esp32State
                )

                // Camera Status Pill
                ConnectionPill(
                    label = "CAM",
                    state = cameraState
                )

                // AI Status Pill
                StatusPill(
                    label = "AI: ${if (aiReady) "READY" else "INIT"}",
                    color = if (aiReady) StatusSafe else StatusWarning
                )
            }
        }
    }
}

@Composable
fun ConnectionPill(label: String, state: DeviceConnectionState) {
    val (text, color) = when (state) {
        DeviceConnectionState.ONLINE -> "ONLINE" to StatusSafe
        DeviceConnectionState.OFFLINE -> "OFFLINE" to StatusCritical
        DeviceConnectionState.CONNECTING -> "CONNECTING" to StatusWarning
        DeviceConnectionState.STALE -> "DATA STALE" to StatusWarning
        DeviceConnectionState.ERROR -> "ERROR" to StatusCritical
    }

    StatusPill(label = "$label: $text", color = color)
}

@Composable
fun StatusPill(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 5.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
