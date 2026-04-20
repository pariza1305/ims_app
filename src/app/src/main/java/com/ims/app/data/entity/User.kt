package com.ims.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole {
    ADMIN, TEACHER, STUDENT, PARENT
}

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val email: String,
    val role: UserRole,
    val avatarInitial: String = name.firstOrNull()?.uppercase() ?: "U",
    val phone: String = "",
    val department: String = ""
)
