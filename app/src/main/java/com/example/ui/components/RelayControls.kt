package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RobotMode
import com.example.data.model.RobotStatus
import com.example.ui.theme.CardBorderLight
import com.example.ui.theme.CardSurfaceWhite
import com.example.ui.theme.LocalRobotModeColors
import com.example.ui.theme.PureWhite
import com.example.ui.theme.StatusCritical
import com.example.ui.theme.StatusSafe
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted

@Composable
fun RelayControls(
    robotMode: RobotMode,
    robotStatus: RobotStatus?,
    onToggleRelay: (relayNum: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val modeColors = LocalRobotModeColors.current
    val isAuto = robotMode == RobotMode.AUTO
    val isWaterCritical = robotStatus?.isWaterCritical == true

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("relay_controls_card"),
        colors = CardDefaults.cardColors(containerColor = CardSurfaceWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderLight),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "RELAY & ACTUATOR CONTROL",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = modeColors.headingColor
            )

            Spacer(modifier = Modifier.height(14.dp))

            // RELAY 1: AUTO DRILL (Strictly NO manual switch, auto-workflow status only)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(modeColors.containerColor.copy(alpha = 0.5f))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = null,
                        tint = modeColors.primaryAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "RELAY 1: AUTO DRILL",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = modeColors.headingColor
                        )
                        Text(
                            text = "Auto-workflow only • Max 7s hardware cutoff",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                }

                val drillActive = robotStatus?.drillOn == true
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (drillActive) StatusCritical else CardBorderLight)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (drillActive) "DRILL ON" else "DRILL OFF",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = if (drillActive) PureWhite else TextDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // RELAY 2: SOIL RELAY (Manual only)
            RelaySwitchRow(
                title = "RELAY 2: SOIL",
                subtitle = "Manual actuator",
                icon = Icons.Default.Grass,
                state = robotStatus?.relay2 == true,
                enabled = !isAuto,
                onToggle = { onToggleRelay(2) },
                testTag = "relay2_soil_switch"
            )

            Spacer(modifier = Modifier.height(10.dp))

            // RELAY 3: WATER PUMP (Manual only; Locked if critical water)
            RelaySwitchRow(
                title = "RELAY 3: WATER PUMP",
                subtitle = if (isWaterCritical) "CRITICAL WATER LEVEL - PUMP LOCKOUT" else "Submersible irrigation pump",
                icon = Icons.Default.WaterDrop,
                state = robotStatus?.relay3 == true,
                enabled = !isAuto && !isWaterCritical,
                isLockout = isWaterCritical,
                onToggle = { onToggleRelay(3) },
                testTag = "relay3_water_switch"
            )

            Spacer(modifier = Modifier.height(10.dp))

            // RELAY 4: SIREN / ALARM
            RelaySwitchRow(
                title = "RELAY 4: SIREN / ALARM",
                subtitle = if (isAuto) "Auto alarm mode (Controlled by ESP32 safety)" else "Acoustic deterrent siren",
                icon = Icons.Default.NotificationsActive,
                state = robotStatus?.sirenOn == true || robotStatus?.relay4 == true,
                enabled = !isAuto,
                onToggle = { onToggleRelay(4) },
                testTag = "relay4_siren_switch"
            )
        }
    }
}

@Composable
fun RelaySwitchRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    state: Boolean,
    enabled: Boolean,
    isLockout: Boolean = false,
    onToggle: () -> Unit,
    testTag: String
) {
    val modeColors = LocalRobotModeColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isLockout) StatusCritical.copy(alpha = 0.08f) else CardSurfaceWhite)
            .border(1.dp, if (isLockout) StatusCritical.copy(alpha = 0.4f) else CardBorderLight, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isLockout) StatusCritical else modeColors.primaryAccent,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isLockout) StatusCritical else modeColors.headingColor
                )
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = if (isLockout) StatusCritical else TextMuted
                )
            }
        }

        Switch(
            checked = state,
            onCheckedChange = { onToggle() },
            enabled = enabled,
            modifier = Modifier.testTag(testTag),
            colors = SwitchDefaults.colors(
                checkedThumbColor = PureWhite,
                checkedTrackColor = modeColors.primaryAccent,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = CardBorderLight,
                disabledCheckedThumbColor = PureWhite,
                disabledCheckedTrackColor = modeColors.primaryAccent.copy(alpha = 0.4f)
            )
        )
    }
}
