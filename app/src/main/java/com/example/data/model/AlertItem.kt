package com.example.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AlertSeverity {
    INFO,
    WARNING,
    CRITICAL
}

data class AlertItem(
    val id: Long = 0,
    val type: String,
    val severity: AlertSeverity,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isResolved: Boolean = false
) {
    val formattedTime: String
        get() = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
}
