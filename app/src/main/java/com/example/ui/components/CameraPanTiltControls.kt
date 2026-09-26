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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import com.example.ui.theme.CardBorderLight
import com.example.ui.theme.CardSurfaceWhite
import com.example.ui.theme.LocalRobotModeColors
import com.example.ui.theme.TextMuted

@Composable
fun CameraPanTiltControls(
    panAngle: Int,
    tiltAngle: Int,
    isLocked: Boolean,
    onPanChange: (Int) -> Unit,
    onTiltChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val modeColors = LocalRobotModeColors.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("camera_pan_tilt_card"),
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
                    text = "CAMERA GIMBAL (PAN / TILT)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = modeColors.headingColor
                )

                if (isLocked) {
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
                                text = "LOCKED DURING AUTO TARGETING",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = modeColors.primaryAccent
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // PAN SLIDER (0 - 180)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isLocked) "CAMERA PAN (PAN LOCKED)" else "CAMERA PAN",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = modeColors.headingColor
                )
                Text(
                    text = "$panAngle°",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = modeColors.primaryAccent
                )
            }

            Slider(
                value = panAngle.toFloat(),
                onValueChange = { onPanChange(it.toInt()) },
                valueRange = 0f..180f,
                enabled = !isLocked,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("camera_pan_slider"),
                colors = SliderDefaults.colors(
                    thumbColor = modeColors.primaryAccent,
                    activeTrackColor = modeColors.primaryAccent,
                    inactiveTrackColor = modeColors.containerColor,
                    disabledThumbColor = TextMuted,
                    disabledActiveTrackColor = CardBorderLight
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // TILT SLIDER (0 - 180)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isLocked) "CAMERA TILT (TILT LOCKED)" else "CAMERA TILT",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = modeColors.headingColor
                )
                Text(
                    text = "$tiltAngle°",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = modeColors.primaryAccent
                )
            }

            Slider(
                value = tiltAngle.toFloat(),
                onValueChange = { onTiltChange(it.toInt()) },
                valueRange = 0f..180f,
                enabled = !isLocked,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("camera_tilt_slider"),
                colors = SliderDefaults.colors(
                    thumbColor = modeColors.primaryAccent,
                    activeTrackColor = modeColors.primaryAccent,
                    inactiveTrackColor = modeColors.containerColor,
                    disabledThumbColor = TextMuted,
                    disabledActiveTrackColor = CardBorderLight
                )
            )
        }
    }
}
