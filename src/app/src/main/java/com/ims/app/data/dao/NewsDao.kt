package com.ims.app.data.dao

import androidx.room.*
import com.ims.app.data.entity.NewsArticle
import kotlinx.coroutines.flow.Flow

@Dao
interface NewsDao {
    @Query("SELECT * FROM news_articles ORDER BY publishedAt DESC")
    fun getAllNews(): Flow<List<NewsArticle>>

    @Query("SELECT * FROM news_articles ORDER BY publishedAt DESC LIMIT :limit")
    fun getLatestNews(limit: Int): Flow<List<NewsArticle>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(article: NewsArticle): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(articles: List<NewsArticle>)

    @Delete
    suspend fun delete(article: NewsArticle)
}
