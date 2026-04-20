package com.ims.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class NotificationType {
    ATTENDANCE, EXAM, SYSTEM, NEWS
}

@Entity(tableName = "notifications")
data class Notification(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val title: String,
    val message: String,
    val timestamp: String,
    val isRead: Boolean = false,
    val type: NotificationType = NotificationType.SYSTEM
)
