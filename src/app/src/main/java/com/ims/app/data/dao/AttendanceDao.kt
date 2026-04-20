package com.ims.app.data.dao

import androidx.room.*
import com.ims.app.data.entity.AttendanceRecord
import com.ims.app.data.entity.AttendanceStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance_records WHERE date = :date AND subjectId = :subjectId")
    fun getAttendanceByDateAndSubject(date: String, subjectId: Long): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE studentId = :studentId AND date BETWEEN :startDate AND :endDate")
    fun getAttendanceByStudentAndDateRange(studentId: Long, startDate: String, endDate: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE date = :date")
    fun getAttendanceByDate(date: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE studentId IN (SELECT id FROM students WHERE batchId = :batchId) AND date = :date")
    fun getAttendanceByBatchAndDate(batchId: Long, date: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE studentId IN (SELECT id FROM students WHERE batchId = :batchId) AND subjectId = :subjectId AND date = :date")
    fun getAttendanceByBatchSubjectDate(batchId: Long, subjectId: Long, date: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE studentId IN (SELECT id FROM students WHERE batchId = :batchId) AND date BETWEEN :startDate AND :endDate")
    fun getAttendanceByBatchAndDateRange(batchId: Long, startDate: String, endDate: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE studentId IN (SELECT id FROM students WHERE batchId = :batchId) AND subjectId = :subjectId AND date BETWEEN :startDate AND :endDate")
    fun getAttendanceByBatchSubjectAndDateRange(batchId: Long, subjectId: Long, startDate: String, endDate: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE studentId = :studentId AND date = :date")
    fun getAttendanceByStudentAndDate(studentId: Long, date: String): Flow<List<AttendanceRecord>>

    @Query("SELECT COUNT(*) FROM attendance_records WHERE date = :date AND status = :status AND studentId IN (SELECT id FROM students WHERE batchId = :batchId)")
    suspend fun getCountByDateAndStatus(date: String, status: AttendanceStatus, batchId: Long): Int

    @Query("SELECT COUNT(*) FROM attendance_records WHERE date = :date AND studentId IN (SELECT id FROM students WHERE batchId = :batchId)")
    suspend fun getTotalCountByDate(date: String, batchId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: AttendanceRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<AttendanceRecord>)

    @Update
    suspend fun update(record: AttendanceRecord)

    @Delete
    suspend fun delete(record: AttendanceRecord)

    @Query("DELETE FROM attendance_records WHERE date = :date AND subjectId = :subjectId")
    suspend fun deleteByDateAndSubject(date: String, subjectId: Long)

    @Query("SELECT COUNT(*) FROM attendance_records")
    suspend fun getCount(): Int

    @Query("SELECT DISTINCT date FROM attendance_records ORDER BY date DESC LIMIT 30")
    suspend fun getRecentDates(): List<String>
}
