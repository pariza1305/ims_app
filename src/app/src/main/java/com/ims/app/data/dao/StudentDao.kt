package com.ims.app.data.dao

import androidx.room.*
import com.ims.app.data.entity.Student
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM students")
    fun getAllStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE id = :id")
    suspend fun getStudentById(id: Long): Student?

    @Query("SELECT * FROM students WHERE batchId = :batchId")
    fun getStudentsByBatch(batchId: Long): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE categoryId = :categoryId")
    fun getStudentsByCategory(categoryId: Long): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE name LIKE '%' || :query || '%' OR rollNumber LIKE '%' || :query || '%'")
    fun searchStudents(query: String): Flow<List<Student>>

    @Query("SELECT COUNT(*) FROM students")
    suspend fun getCount(): Int

    @Query("SELECT COUNT(*) FROM students WHERE batchId = :batchId")
    suspend fun getCountByBatch(batchId: Long): Int

    @Query("SELECT COUNT(*) FROM students WHERE categoryId IS NULL")
    suspend fun getUncategorizedCount(): Int

    @Query("""
        UPDATE students
        SET categoryId = CASE (id % 4)
            WHEN 1 THEN 1
            WHEN 2 THEN 2
            WHEN 3 THEN 3
            ELSE 4
        END
        WHERE categoryId IS NULL
    """)
    suspend fun assignDefaultCategoriesToUncategorized()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(student: Student): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(students: List<Student>)

    @Update
    suspend fun update(student: Student)

    @Delete
    suspend fun delete(student: Student)
}
