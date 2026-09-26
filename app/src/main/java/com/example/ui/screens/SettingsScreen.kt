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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppSettings
import com.example.ui.theme.CardBorderLight
import com.example.ui.theme.CardSurfaceWhite
import com.example.ui.theme.LocalRobotModeColors
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalWhite
import com.example.ui.theme.StatusCritical
import com.example.ui.theme.StatusSafe
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.viewmodel.RobotViewModel

@Composable
fun SettingsScreen(
    viewModel: RobotViewModel,
    modifier: Modifier = Modifier
) {
    val modeColors = LocalRobotModeColors.current
    val currentSettings by viewModel.settings.collectAsState()
    val alerts by viewModel.alerts.collectAsState()

    var esp32Ip by remember(currentSettings) { mutableStateOf(currentSettings.esp32Ip) }
    var esp32Port by remember(currentSettings) { mutableStateOf(currentSettings.esp32Port.toString()) }
    var cameraIp by remember(currentSettings) { mutableStateOf(currentSettings.cameraIp) }
    var cameraPort by remember(currentSettings) { mutableStateOf(currentSettings.cameraStreamPort.toString()) }
    var cameraStreamEndpoint by remember(currentSettings) { mutableStateOf(currentSettings.cameraStreamEndpoint) }
    var cameraCaptureEndpoint by remember(currentSettings) { mutableStateOf(currentSettings.cameraCaptureEndpoint) }
    var hotspotSsid by remember(currentSettings) { mutableStateOf(currentSettings.hotspotSsid) }
    var hotspotPassword by remember(currentSettings) { mutableStateOf(currentSettings.hotspotPassword) }

    var soilDryThreshold by remember(currentSettings) { mutableStateOf(currentSettings.soilDryThreshold.toString()) }
    var soilCriticalThreshold by remember(currentSettings) { mutableStateOf(currentSettings.soilCriticalThreshold.toString()) }
    var waterLowThreshold by remember(currentSettings) { mutableStateOf(currentSettings.waterLowThreshold.toString()) }
    var waterCriticalThreshold by remember(currentSettings) { mutableStateOf(currentSettings.waterCriticalThreshold.toString()) }

    var vibrationThreshold by remember(currentSettings) { mutableStateOf(currentSettings.vibrationThreshold.toString()) }
    var batteryLowThreshold by remember(currentSettings) { mutableStateOf(currentSettings.batteryLowThreshold.toString()) }
    var batteryCriticalThreshold by remember(currentSettings) { mutableStateOf(currentSettings.batteryCriticalThreshold.toString()) }

    var simulationMode by remember(currentSettings) { mutableStateOf(currentSettings.simulationModeEnabled) }
    var notificationsEnabled by remember(currentSettings) { mutableStateOf(currentSettings.notificationsEnabled) }
    var hapticEnabled by remember(currentSettings) { mutableStateOf(currentSettings.vibrationHapticEnabled) }

    var savedMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(RoyalWhite)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("settings_screen")
    ) {
        Text(
            text = "SYSTEM CONFIGURATION",
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = modeColors.headingColor
        )
        Text(
            text = "CURRENT ROBOT MODE: ${modeColors.modeName}",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = modeColors.primaryAccent
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (savedMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(StatusSafe.copy(alpha = 0.15f))
                    .border(1.dp, StatusSafe, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = savedMessage!!,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = StatusSafe
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // NETWORK CONFIGURATION CARD (Section 27)
        SettingsCard(title = "NETWORK CONFIGURATION") {
            OutlinedTextField(
                value = hotspotSsid,
                onValueChange = { hotspotSsid = it },
                label = { Text("Mobile Hotspot SSID") },
                modifier = Modifier.fillMaxWidth().testTag("hotspot_ssid_input"),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = hotspotPassword,
                onValueChange = { hotspotPassword = it },
                label = { Text("Hotspot Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth().testTag("hotspot_password_input"),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = esp32Ip,
                    onValueChange = { esp32Ip = it },
                    label = { Text("MAIN ESP32 IP") },
                    modifier = Modifier.weight(2f).testTag("esp32_ip_input"),
                    singleLine = true
                )
                OutlinedTextField(
                    value = esp32Port,
                    onValueChange = { esp32Port = it },
                    label = { Text("Port") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f).testTag("esp32_port_input"),
                    singleLine = true
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = cameraIp,
                    onValueChange = { cameraIp = it },
                    label = { Text("ESP32-CAM IP") },
                    modifier = Modifier.weight(2f).testTag("camera_ip_input"),
                    singleLine = true
                )
                OutlinedTextField(
                    value = cameraPort,
                    onValueChange = { cameraPort = it },
                    label = { Text("Port") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f).testTag("camera_port_input"),
                    singleLine = true
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = cameraStreamEndpoint,
                onValueChange = { cameraStreamEndpoint = it },
                label = { Text("Camera Stream Endpoint") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = cameraCaptureEndpoint,
                onValueChange = { cameraCaptureEndpoint = it },
                label = { Text("Camera Capture Endpoint") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // SENSOR & SAFETY THRESHOLDS (Sections 29, 30, 31, 33)
        SettingsCard(title = "SOIL & WATER THRESHOLDS") {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = soilDryThreshold,
                    onValueChange = { soilDryThreshold = it },
                    label = { Text("Soil Dry (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = soilCriticalThreshold,
                    onValueChange = { soilCriticalThreshold = it },
                    label = { Text("Soil Critical (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = waterLowThreshold,
                    onValueChange = { waterLowThreshold = it },
                    label = { Text("Water Low (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = waterCriticalThreshold,
                    onValueChange = { waterCriticalThreshold = it },
                    label = { Text("Water Lockout (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // BATTERY & VIBRATION SAFETY (Sections 31, 33)
        SettingsCard(title = "BATTERY & VIBRATION SAFETY") {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = batteryLowThreshold,
                    onValueChange = { batteryLowThreshold = it },
                    label = { Text("Battery Low (V)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = batteryCriticalThreshold,
                    onValueChange = { batteryCriticalThreshold = it },
                    label = { Text("Battery Cutoff (V)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = vibrationThreshold,
                onValueChange = { vibrationThreshold = it },
                label = { Text("Vibration Severity Threshold (G)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // NOTIFICATIONS & HAPTICS (Sections 46, 47)
        SettingsCard(title = "ALERTS & SYSTEM FEEDBACK") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Android System Notifications", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = PureWhite, checkedTrackColor = modeColors.primaryAccent)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Phone Haptic Feedback", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Switch(
                    checked = hapticEnabled,
                    onCheckedChange = { hapticEnabled = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = PureWhite, checkedTrackColor = modeColors.primaryAccent)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // DEVELOPER / LAB SIMULATION MODE (Section 50)
        SettingsCard(title = "DEVELOPER / LAB SIMULATION MODE") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Enable Simulation Fallback",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (simulationMode) StatusWarning else TextDark
                    )
                    Text(
                        text = "Used exclusively for testing UI workflows when physically detached from ESP32 hardware Wi-Fi.",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
                Switch(
                    checked = simulationMode,
                    onCheckedChange = { simulationMode = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = PureWhite, checkedTrackColor = StatusWarning)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // SAVE SETTINGS BUTTON
        Button(
            onClick = {
                val updated = currentSettings.copy(
                    esp32Ip = esp32Ip.trim(),
                    esp32Port = esp32Port.toIntOrNull() ?: 80,
                    cameraIp = cameraIp.trim(),
                    cameraStreamPort = cameraPort.toIntOrNull() ?: 81,
                    cameraStreamEndpoint = cameraStreamEndpoint.trim(),
                    cameraCaptureEndpoint = cameraCaptureEndpoint.trim(),
                    hotspotSsid = hotspotSsid.trim(),
                    hotspotPassword = hotspotPassword.trim(),
                    soilDryThreshold = soilDryThreshold.toIntOrNull() ?: 30,
                    soilCriticalThreshold = soilCriticalThreshold.toIntOrNull() ?: 15,
                    waterLowThreshold = waterLowThreshold.toIntOrNull() ?: 35,
                    waterCriticalThreshold = waterCriticalThreshold.toIntOrNull() ?: 15,
                    batteryLowThreshold = batteryLowThreshold.toDoubleOrNull() ?: 11.2,
                    batteryCriticalThreshold = batteryCriticalThreshold.toDoubleOrNull() ?: 10.5,
                    vibrationThreshold = vibrationThreshold.toFloatOrNull() ?: 1.5f,
                    notificationsEnabled = notificationsEnabled,
                    vibrationHapticEnabled = hapticEnabled,
                    simulationModeEnabled = simulationMode
                )
                viewModel.updateSettings(updated)
                savedMessage = "Settings successfully saved & hardware network connections updated!"
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("save_settings_button")
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = modeColors.primaryAccent),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("SAVE CONFIGURATION", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = PureWhite)
        }

        Spacer(modifier = Modifier.height(18.dp))

        // ALERT LOG & HISTORY CARD (Section 45)
        SettingsCard(title = "ALERT HISTORY LOG (${alerts.size} EVENTS)") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Room Database Persistent Log",
                    fontSize = 11.sp,
                    color = TextMuted
                )
                Button(
                    onClick = { viewModel.clearAllAlerts() },
                    modifier = Modifier.testTag("clear_alerts_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = CardBorderLight, contentColor = TextDark),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear All", fontSize = 10.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (alerts.isEmpty()) {
                Text(
                    text = "No alerts logged in database.",
                    fontSize = 11.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                alerts.take(10).forEach { alert ->
                    val color = when (alert.severity.name) {
                        "CRITICAL" -> StatusCritical
                        "WARNING" -> StatusWarning
                        else -> modeColors.primaryAccent
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(color.copy(alpha = 0.08f))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(color)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${alert.type} • ${alert.formattedTime}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = color
                            )
                            Text(
                                text = alert.message,
                                fontSize = 11.sp,
                                color = TextDark
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun SettingsCard(title: String, content: @Composable () -> Unit) {
    val modeColors = LocalRobotModeColors.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardSurfaceWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderLight),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = modeColors.headingColor
            )
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}
