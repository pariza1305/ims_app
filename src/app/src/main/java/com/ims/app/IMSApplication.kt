package com.ims.app

import android.app.Application
import com.ims.app.data.IMSDatabase
import com.ims.app.data.repository.*
import com.ims.app.data.seed.seedDatabase
import com.ims.app.i18n.AppLanguageManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class IMSApplication : Application() {

    lateinit var database: IMSDatabase
    lateinit var userRepository: UserRepository
    lateinit var studentRepository: StudentRepository
    lateinit var courseRepository: CourseRepository
    lateinit var batchRepository: BatchRepository
    lateinit var subjectRepository: SubjectRepository
    lateinit var attendanceRepository: AttendanceRepository
    lateinit var examRepository: ExamRepository
    lateinit var newsRepository: NewsRepository
    lateinit var notificationRepository: NotificationRepository
    lateinit var studentCategoryRepository: StudentCategoryRepository
    lateinit var graduationRepository: GraduationRepository

    override fun onCreate() {
        super.onCreate()

        // Restore previously selected app language before UI composition.
        AppLanguageManager.applySavedLanguage(this)

        // Initialize database
        database = IMSDatabase.getDatabase(this)

        // Initialize repositories
        userRepository = UserRepository(database.userDao())
        studentRepository = StudentRepository(database.studentDao())
        courseRepository = CourseRepository(database.courseDao())
        batchRepository = BatchRepository(database.batchDao())
        subjectRepository = SubjectRepository(database.subjectDao())
        attendanceRepository = AttendanceRepository(database.attendanceDao())
        examRepository = ExamRepository(database.examDao())
        newsRepository = NewsRepository(database.newsDao())
        notificationRepository = NotificationRepository(database.notificationDao())
        studentCategoryRepository = StudentCategoryRepository(database.academyDao())
        graduationRepository = GraduationRepository(database.academyDao())

        // Seed data
        CoroutineScope(Dispatchers.IO).launch {
            seedDatabase(database)
        }
    }
}
