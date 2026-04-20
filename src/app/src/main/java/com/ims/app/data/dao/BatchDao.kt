package com.ims.app.data.dao

import androidx.room.*
import com.ims.app.data.entity.Batch
import kotlinx.coroutines.flow.Flow

@Dao
interface BatchDao {
    @Query("SELECT * FROM batches")
    fun getAllBatches(): Flow<List<Batch>>

    @Query("SELECT * FROM batches WHERE id = :id")
    suspend fun getBatchById(id: Long): Batch?

    @Query("SELECT * FROM batches WHERE courseId = :courseId")
    fun getBatchesByCourse(courseId: Long): Flow<List<Batch>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(batch: Batch): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(batches: List<Batch>)

    @Query("SELECT COUNT(*) FROM batches")
    suspend fun getCount(): Int
}
