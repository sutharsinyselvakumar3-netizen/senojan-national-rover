package com.example.data.repository

import com.example.data.local.AlertDao
import com.example.data.local.AlertEntity
import com.example.data.model.AlertItem
import com.example.data.model.AlertSeverity
import com.example.data.model.AppSettings
import com.example.data.model.CommandResponse
import com.example.data.model.RobotStatus
import com.example.data.network.Esp32ApiClient
import com.example.service.NotificationHelper
import com.example.service.VibrationHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RobotRepository(
    private val apiClient: Esp32ApiClient,
    private val alertDao: AlertDao,
    private val notificationHelper: NotificationHelper,
    private val vibrationHelper: VibrationHelper
) {

    val allAlerts: Flow<List<AlertItem>> = alertDao.getAllAlerts().map { list ->
        list.map { it.toAlertItem() }
    }

    suspend fun fetchStatus(settings: AppSettings): Result<RobotStatus> {
        return apiClient.getStatus(settings.esp32BaseUrl)
    }

    suspend fun setMode(settings: AppSettings, mode: String): Result<CommandResponse> {
        return apiClient.setMode(settings.esp32BaseUrl, mode)
    }

    suspend fun sendMovement(settings: AppSettings, action: String, speed: String): Result<CommandResponse> {
        return apiClient.sendRobotMovement(settings.esp32BaseUrl, action, speed)
    }

    suspend fun setServo4Angle(settings: AppSettings, angle: Int): Result<CommandResponse> {
        return apiClient.setServo4Angle(settings.esp32BaseUrl, angle)
    }

    suspend fun setRelay(settings: AppSettings, relayNum: Int, state: Boolean): Result<CommandResponse> {
        return apiClient.setRelay(settings.esp32BaseUrl, relayNum, state)
    }

    suspend fun setCameraPanTilt(settings: AppSettings, pan: Int, tilt: Int): Result<CommandResponse> {
        return apiClient.sendCameraPanTilt(settings.esp32BaseUrl, pan, tilt)
    }

    suspend fun sendEmergencyStop(settings: AppSettings): Result<CommandResponse> {
        return apiClient.sendEmergencyStop(settings.esp32BaseUrl)
    }

    suspend fun logAlert(
        type: String,
        severity: AlertSeverity,
        message: String,
        settings: AppSettings
    ) {
        val alert = AlertItem(
            type = type,
            severity = severity,
            message = message
        )
        alertDao.insertAlert(AlertEntity.fromAlertItem(alert))

        // Trigger Android notifications if enabled
        if (settings.notificationsEnabled) {
            notificationHelper.showNotification(
                title = "AI COMPANION - $type",
                message = message
            )
        }

        // Trigger phone haptic vibration if enabled
        if (severity == AlertSeverity.CRITICAL && settings.vibrationHapticEnabled) {
            vibrationHelper.vibrateCritical()
        } else if (severity == AlertSeverity.WARNING && settings.vibrationHapticEnabled) {
            vibrationHelper.vibrateWarning()
        }
    }

    suspend fun markAlertResolved(id: Long) {
        alertDao.markResolved(id)
    }

    suspend fun clearAllAlerts() {
        alertDao.clearAll()
    }
}
