package com.ims.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ims.app.data.dao.*
import com.ims.app.data.entity.*

@Database(
    entities = [
        User::class,
        Student::class,
        Course::class,
        Batch::class,
        Subject::class,
        AttendanceRecord::class,
        Exam::class,
        ExamResult::class,
        NewsArticle::class,
        Notification::class,
        StudentCategory::class,
        GraduationRecord::class
    ],
    version = 9,
    exportSchema = false
)
abstract class IMSDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun studentDao(): StudentDao
    abstract fun courseDao(): CourseDao
    abstract fun batchDao(): BatchDao
    abstract fun subjectDao(): SubjectDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun examDao(): ExamDao
    abstract fun newsDao(): NewsDao
    abstract fun notificationDao(): NotificationDao
    abstract fun academyDao(): AcademyDao

    companion object {
        @Volatile
        private var INSTANCE: IMSDatabase? = null

        fun getDatabase(context: Context): IMSDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    IMSDatabase::class.java,
                    "ims_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
