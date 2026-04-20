package com.ims.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val batchId: Long,
    val rollNumber: String,
    val admissionDate: String,
    val categoryId: Long? = null,
    val name: String = "",
    val email: String = ""
)
