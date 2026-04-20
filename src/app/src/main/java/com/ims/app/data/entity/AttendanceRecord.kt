package com.ims.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AttendanceStatus {
    PRESENT, ABSENT
}

@Entity(tableName = "attendance_records")
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val subjectId: Long,
    val date: String,
    val status: AttendanceStatus,
    val remarks: String = ""
)
