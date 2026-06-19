package com.example.data.model

import java.util.UUID

enum class NotificationSeverity {
    INFO,
    WARNING,
    ERROR
}

/**
 * Clean data model representing a user notification (toast or banner)
 * with support for various severity levels.
 */
data class UserNotification(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val severity: NotificationSeverity,
    val timestamp: Long = System.currentTimeMillis()
)
