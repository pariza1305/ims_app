package com.ims.app.data.dao

import androidx.room.*
import com.ims.app.data.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AcademyDao {
    // --- Categories ---
    @Query("SELECT * FROM student_categories")
    fun getAllCategories(): Flow<List<StudentCategory>>

    @Query("SELECT COUNT(*) FROM student_categories")
    suspend fun getCategoryCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: StudentCategory): Long

    @Delete
    suspend fun deleteCategory(category: StudentCategory)

    @Query("SELECT COUNT(*) FROM students WHERE categoryId = :categoryId")
    suspend fun getStudentCountByCategory(categoryId: Long): Int

    // --- Graduation ---
    @Query("SELECT * FROM graduation_records")
    fun getAllGraduationRecords(): Flow<List<GraduationRecord>>

    @Query("SELECT * FROM graduation_records WHERE studentId = :studentId")
    suspend fun getGraduationRecordByStudent(studentId: Long): GraduationRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGraduationRecord(record: GraduationRecord): Long

    @Update
    suspend fun updateGraduationRecord(record: GraduationRecord)
    
    @Query("SELECT * FROM students WHERE id IN (SELECT studentId FROM graduation_records WHERE status = 'GRADUATED' OR status = 'ALUMNI')")
    fun getAlumni(): Flow<List<Student>>
}
