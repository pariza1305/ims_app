package com.ims.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val batchId: Long,
    val teacherId: Long = 0,
    val isElective: Boolean = false
)
