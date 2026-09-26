package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RobotMode
import com.example.ui.theme.AutoDarkBlue
import com.example.ui.theme.CardSurfaceWhite
import com.example.ui.theme.ManualDarkGreen
import com.example.ui.theme.PureWhite
import com.example.ui.theme.StatusCritical
import com.example.ui.theme.TextDark

@Composable
fun ModeSwitchDialog(
    currentMode: RobotMode,
    isProcessing: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onConfirmSwitch: () -> Unit
) {
    val isSwitchingToAuto = currentMode == RobotMode.MANUAL

    AlertDialog(
        onDismissRequest = { if (!isProcessing) onDismiss() },
        containerColor = CardSurfaceWhite,
        shape = RoundedCornerShape(16.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (errorMessage != null) Icons.Default.Warning else Icons.Default.Info,
                    contentDescription = null,
                    tint = if (errorMessage != null) StatusCritical else if (isSwitchingToAuto) AutoDarkBlue else ManualDarkGreen,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (errorMessage != null) "COMMAND REJECTED"
                           else if (isSwitchingToAuto) "SWITCH TO AUTO MODE?"
                           else "SWITCH TO MANUAL MODE?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = TextDark
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = StatusCritical
                    )
                } else if (isSwitchingToAuto) {
                    Text(
                        text = "Automatic operation will control:\n" +
                                "• Robot movement\n" +
                                "• Weed detection\n" +
                                "• Arm movement\n" +
                                "• Drill operation (Relay 1)\n\n" +
                                "Manual movement and manual soil/water controls will be locked.\n\n" +
                                "Continue?",
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = TextDark
                    )
                } else {
                    Text(
                        text = "Automatic weed detection and automatic arm/drill operation will stop.\n\n" +
                                "Manual controls will be re-enabled.\n\n" +
                                "Continue?",
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = TextDark
                    )
                }

                if (isProcessing) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Communicating with MAIN ESP32 hardware...",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSwitchingToAuto) AutoDarkBlue else ManualDarkGreen
                    )
                }
            }
        },
        confirmButton = {
            if (errorMessage != null) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("dismiss_error_dialog_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = TextDark)
                ) {
                    Text("OK", color = PureWhite)
                }
            } else {
                Button(
                    onClick = onConfirmSwitch,
                    enabled = !isProcessing,
                    modifier = Modifier.testTag("confirm_mode_switch_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSwitchingToAuto) AutoDarkBlue else ManualDarkGreen
                    )
                ) {
                    Text(
                        text = if (isSwitchingToAuto) "CONFIRM AUTO" else "CONFIRM MANUAL",
                        fontWeight = FontWeight.Bold,
                        color = PureWhite
                    )
                }
            }
        },
        dismissButton = {
            if (errorMessage == null) {
                OutlinedButton(
                    onClick = onDismiss,
                    enabled = !isProcessing,
                    modifier = Modifier.testTag("cancel_mode_switch_button")
                ) {
                    Text("CANCEL")
                }
            }
        }
    )
}
