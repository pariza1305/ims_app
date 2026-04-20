package com.ims.app.data.dao

import androidx.room.*
import com.ims.app.data.entity.Exam
import com.ims.app.data.entity.ExamResult
import com.ims.app.data.entity.ExamStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ExamDao {
    @Query("SELECT * FROM exams ORDER BY date DESC")
    fun getAllExams(): Flow<List<Exam>>

    @Query("SELECT * FROM exams WHERE id = :id")
    suspend fun getExamById(id: Long): Exam?

    @Query("SELECT * FROM exams WHERE batchId = :batchId ORDER BY date DESC")
    fun getExamsByBatch(batchId: Long): Flow<List<Exam>>

    @Query("SELECT * FROM exams WHERE subjectId = :subjectId ORDER BY date DESC")
    fun getExamsBySubject(subjectId: Long): Flow<List<Exam>>

    @Query("SELECT * FROM exams WHERE status = :status ORDER BY date DESC")
    fun getExamsByStatus(status: ExamStatus): Flow<List<Exam>>

    @Query("SELECT * FROM exams WHERE name LIKE '%' || :query || '%'")
    fun searchExams(query: String): Flow<List<Exam>>

    @Query("SELECT COUNT(*) FROM exams WHERE status IN ('DRAFT', 'PUBLISHED', 'IN_PROGRESS')")
    suspend fun getPendingExamCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(exam: Exam): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(exams: List<Exam>)

    @Update
    suspend fun update(exam: Exam)

    @Delete
    suspend fun delete(exam: Exam)

    @Query("SELECT COUNT(*) FROM exams")
    suspend fun getCount(): Int

    // ExamResult queries
    @Query("SELECT * FROM exam_results WHERE examId = :examId")
    fun getResultsByExam(examId: Long): Flow<List<ExamResult>>

    @Query("SELECT * FROM exam_results WHERE studentId = :studentId")
    fun getResultsByStudent(studentId: Long): Flow<List<ExamResult>>

    @Query("SELECT * FROM exam_results WHERE examId = :examId AND studentId = :studentId")
    suspend fun getResult(examId: Long, studentId: Long): ExamResult?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: ExamResult): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResults(results: List<ExamResult>)

    @Update
    suspend fun updateResult(result: ExamResult)

    @Query("DELETE FROM exam_results WHERE examId = :examId")
    suspend fun deleteResultsByExam(examId: Long)
}
