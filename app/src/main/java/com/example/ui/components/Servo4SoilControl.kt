package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RobotMode
import com.example.ui.theme.CardBorderLight
import com.example.ui.theme.CardSurfaceWhite
import com.example.ui.theme.LocalRobotModeColors
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextMuted

@Composable
fun Servo4SoilControl(
    robotMode: RobotMode,
    currentAngle: Int,
    onAngleChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val modeColors = LocalRobotModeColors.current
    val isAuto = robotMode == RobotMode.AUTO

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("servo4_soil_card"),
        colors = CardDefaults.cardColors(containerColor = CardSurfaceWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderLight),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SOIL SERVO (SERVO 4)",
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
                                text = "SOIL SERVO LOCKED IN AUTO",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = modeColors.primaryAccent
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Allowed Range: 0° → 45°",
                    fontSize = 11.sp,
                    color = TextMuted
                )
                Text(
                    text = "CURRENT: $currentAngle°",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = modeColors.primaryAccent
                )
            }

            Slider(
                value = currentAngle.toFloat(),
                onValueChange = { if (!isAuto) onAngleChange(it.toInt().coerceIn(0, 45)) },
                valueRange = 0f..45f,
                steps = 44,
                enabled = !isAuto,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("servo4_slider"),
                colors = SliderDefaults.colors(
                    thumbColor = modeColors.primaryAccent,
                    activeTrackColor = modeColors.primaryAccent,
                    inactiveTrackColor = modeColors.containerColor,
                    disabledThumbColor = TextMuted,
                    disabledActiveTrackColor = CardBorderLight
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { if (!isAuto) onAngleChange(0) },
                    enabled = !isAuto,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("servo4_home_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = modeColors.containerColor,
                        contentColor = modeColors.primaryAccent
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = "HOME (0°)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { if (!isAuto) onAngleChange(22) },
                    enabled = !isAuto,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("servo4_center_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = modeColors.containerColor,
                        contentColor = modeColors.primaryAccent
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = "CENTER (22°)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
