package com.ims.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class GraduationStatus {
    IN_PROGRESS,
    PENDING_CLEARANCE,
    CLEARED,
    GRADUATED,
    ALUMNI
}

enum class CertificateStatus {
    NOT_REQUESTED,
    PROCESSING,
    ISSUED,
    DELIVERED
}

@Entity(tableName = "graduation_records")
data class GraduationRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val graduationYear: Int,
    val status: GraduationStatus = GraduationStatus.IN_PROGRESS,
    val certificateStatus: CertificateStatus = CertificateStatus.NOT_REQUESTED,
    val clearanceRemarks: String = ""
)
