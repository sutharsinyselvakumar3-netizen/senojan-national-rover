package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RobotStatus
import com.example.ui.theme.StatusCritical
import com.example.ui.theme.StatusWarning

@Composable
fun SafetyBanner(
    robotStatus: RobotStatus?,
    modifier: Modifier = Modifier
) {
    if (robotStatus == null) return

    val messages = mutableListOf<Pair<String, Boolean>>() // text to isCritical

    if (robotStatus.isBatteryCritical) {
        messages.add("CRITICAL BATTERY: Voltage (${robotStatus.batteryVoltageDisplay}) below safety threshold! Motors & Drill locked." to true)
    }
    if (robotStatus.isMpuFault) {
        messages.add("MPU SAFETY FAULT: Robot chassis tilt/instability detected! Operations halted." to true)
    }
    if (robotStatus.isVibrationCriticalState) {
        messages.add("VIBRATION SAFETY FAULT: Sustained severe mechanical vibration detected! Auto stop enforced." to true)
    }
    if (robotStatus.isWaterCritical) {
        messages.add("CRITICAL WATER LEVEL: Water pump locked out to prevent pump burnout." to true)
    } else if (robotStatus.waterState == "LOW") {
        messages.add("LOW WATER LEVEL: Please refill water reservoir soon." to false)
    }

    if (messages.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("safety_banner_container")
    ) {
        for ((msg, isCrit) in messages) {
            val bannerColor = if (isCrit) StatusCritical else StatusWarning

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(bannerColor.copy(alpha = 0.12f))
                    .border(1.dp, bannerColor.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = bannerColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = msg,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = bannerColor,
                    lineHeight = 15.sp
                )
            }
        }
    }
}
