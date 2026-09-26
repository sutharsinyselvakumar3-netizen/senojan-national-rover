package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.AlertItem
import com.example.data.model.AlertSeverity

@Entity(tableName = "alerts")
data class AlertEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val severity: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isResolved: Boolean = false
) {
    fun toAlertItem(): AlertItem {
        val parsedSeverity = try {
            AlertSeverity.valueOf(severity)
        } catch (_: Exception) {
            AlertSeverity.INFO
        }
        return AlertItem(
            id = id,
            type = type,
            severity = parsedSeverity,
            message = message,
            timestamp = timestamp,
            isResolved = isResolved
        )
    }

    companion object {
        fun fromAlertItem(item: AlertItem): AlertEntity {
            return AlertEntity(
                id = item.id,
                type = item.type,
                severity = item.severity.name,
                message = item.message,
                timestamp = item.timestamp,
                isResolved = item.isResolved
            )
        }
    }
}
