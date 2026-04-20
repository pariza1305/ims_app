package com.ims.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ExamType {
    MARKS, GRADE, CUSTOM
}

enum class EvaluationMethod {
    GPA, CCE, CWA
}

enum class ExamStatus {
    DRAFT, PUBLISHED, IN_PROGRESS, COMPLETED, RESULTS_PUBLISHED
}

@Entity(tableName = "exams")
data class Exam(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val subjectId: Long,
    val batchId: Long,
    val groupName: String = "",
    val groupedSubjectIds: String = "",
    val date: String,
    val startTime: String = "10:00 AM",
    val endTime: String = "01:00 PM",
    val location: String = "Examination Hall",
    val totalMarks: Int,
    val type: ExamType = ExamType.MARKS,
    val customOptions: String = "",
    val evaluationMethod: EvaluationMethod = EvaluationMethod.GPA,
    val status: ExamStatus = ExamStatus.DRAFT
)
