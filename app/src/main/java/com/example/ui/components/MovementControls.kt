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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.ui.theme.CardBorderLight
import com.example.ui.theme.CardSurfaceWhite
import com.example.ui.theme.LocalRobotModeColors
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted

@Composable
fun MovementControls(
    robotMode: RobotMode,
    currentSpeed: String,
    onMove: (action: String) -> Unit,
    onSpeedChange: (speed: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val modeColors = LocalRobotModeColors.current
    val isAuto = robotMode == RobotMode.AUTO

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("movement_controls_card"),
        colors = CardDefaults.cardColors(containerColor = CardSurfaceWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderLight),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ROBOT MOTION",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = modeColors.headingColor
                )

                if (isAuto) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(modeColors.containerColor)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = modeColors.primaryAccent
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "AUTO CONTROL ACTIVE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = modeColors.primaryAccent
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Speed Selector: SLOW, MEDIUM, FAST
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("SLOW", "MEDIUM", "FAST").forEach { speed ->
                    val isSelected = currentSpeed == speed
                    Button(
                        onClick = { if (!isAuto) onSpeedChange(speed) },
                        enabled = !isAuto,
                        modifier = Modifier
                            .testTag("speed_button_$speed")
                            .height(34.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) modeColors.primaryAccent else modeColors.containerColor,
                            contentColor = if (isSelected) PureWhite else modeColors.primaryAccent
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = speed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cross D-Pad: Exactly 4 buttons: FORWARD, LEFT, RIGHT, REVERSE (No stop button)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // FORWARD Button
                DirectionalButton(
                    label = "FORWARD",
                    icon = Icons.Default.KeyboardArrowUp,
                    enabled = !isAuto,
                    onClick = { onMove("FORWARD") },
                    testTag = "move_forward_button"
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(40.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // LEFT Button
                    DirectionalButton(
                        label = "LEFT",
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        enabled = !isAuto,
                        onClick = { onMove("LEFT") },
                        testTag = "move_left_button"
                    )

                    // RIGHT Button
                    DirectionalButton(
                        label = "RIGHT",
                        icon = Icons.AutoMirrored.Filled.ArrowForward,
                        enabled = !isAuto,
                        onClick = { onMove("RIGHT") },
                        testTag = "move_right_button"
                    )
                }

                // REVERSE Button
                DirectionalButton(
                    label = "REVERSE",
                    icon = Icons.Default.KeyboardArrowDown,
                    enabled = !isAuto,
                    onClick = { onMove("REVERSE") },
                    testTag = "move_reverse_button"
                )
            }
        }
    }
}

@Composable
fun DirectionalButton(
    label: String,
    icon: ImageVector,
    enabled: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val modeColors = LocalRobotModeColors.current

    ElevatedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .testTag(testTag)
            .size(width = 96.dp, height = 52.dp),
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = modeColors.containerColor,
            contentColor = modeColors.primaryAccent,
            disabledContainerColor = CardBorderLight,
            disabledContentColor = TextMuted
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
