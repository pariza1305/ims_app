package com.ims.app.data.repository

import com.ims.app.data.dao.*
import com.ims.app.data.entity.*
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {
    fun getAllUsers(): Flow<List<User>> = userDao.getAllUsers()
    fun getUsersByRole(role: UserRole): Flow<List<User>> = userDao.getUsersByRole(role)
    fun searchUsers(query: String): Flow<List<User>> = userDao.searchUsers(query)
    suspend fun getUserById(id: Long): User? = userDao.getUserById(id)
    suspend fun insert(user: User): Long = userDao.insert(user)
    suspend fun insertAll(users: List<User>) = userDao.insertAll(users)
    suspend fun update(user: User) = userDao.update(user)
    suspend fun delete(user: User) = userDao.delete(user)
    suspend fun getCount(): Int = userDao.getCount()
}

class StudentRepository(private val studentDao: StudentDao) {
    fun getAllStudents(): Flow<List<Student>> = studentDao.getAllStudents()
    fun getStudentsByBatch(batchId: Long): Flow<List<Student>> = studentDao.getStudentsByBatch(batchId)
    fun getStudentsByCategory(categoryId: Long): Flow<List<Student>> = studentDao.getStudentsByCategory(categoryId)
    fun searchStudents(query: String): Flow<List<Student>> = studentDao.searchStudents(query)
    suspend fun getStudentById(id: Long): Student? = studentDao.getStudentById(id)
    suspend fun getCount(): Int = studentDao.getCount()
    suspend fun getCountByBatch(batchId: Long): Int = studentDao.getCountByBatch(batchId)
    suspend fun insert(student: Student): Long = studentDao.insert(student)
    suspend fun insertAll(students: List<Student>) = studentDao.insertAll(students)
    suspend fun update(student: Student) = studentDao.update(student)
    suspend fun delete(student: Student) = studentDao.delete(student)
}

class CourseRepository(private val courseDao: CourseDao) {
    fun getAllCourses(): Flow<List<Course>> = courseDao.getAllCourses()
    suspend fun getCourseById(id: Long): Course? = courseDao.getCourseById(id)
    suspend fun insert(course: Course): Long = courseDao.insert(course)
    suspend fun insertAll(courses: List<Course>) = courseDao.insertAll(courses)
    suspend fun update(course: Course) = courseDao.update(course)
    suspend fun delete(course: Course) = courseDao.delete(course)
}

class BatchRepository(private val batchDao: BatchDao) {
    fun getAllBatches(): Flow<List<Batch>> = batchDao.getAllBatches()
    fun getBatchesByCourse(courseId: Long): Flow<List<Batch>> = batchDao.getBatchesByCourse(courseId)
    suspend fun getBatchById(id: Long): Batch? = batchDao.getBatchById(id)
    suspend fun insert(batch: Batch): Long = batchDao.insert(batch)
    suspend fun insertAll(batches: List<Batch>) = batchDao.insertAll(batches)
    suspend fun getCount(): Int = batchDao.getCount()
}

class SubjectRepository(private val subjectDao: SubjectDao) {
    fun getAllSubjects(): Flow<List<Subject>> = subjectDao.getAllSubjects()
    fun getSubjectsByBatch(batchId: Long): Flow<List<Subject>> = subjectDao.getSubjectsByBatch(batchId)
    suspend fun getSubjectById(id: Long): Subject? = subjectDao.getSubjectById(id)
    suspend fun insert(subject: Subject): Long = subjectDao.insert(subject)
    suspend fun insertAll(subjects: List<Subject>) = subjectDao.insertAll(subjects)
    suspend fun update(subject: Subject) = subjectDao.update(subject)
    suspend fun getCount(): Int = subjectDao.getCount()
}

class AttendanceRepository(private val attendanceDao: AttendanceDao) {
    fun getAttendanceByDateAndSubject(date: String, subjectId: Long): Flow<List<AttendanceRecord>> =
        attendanceDao.getAttendanceByDateAndSubject(date, subjectId)
    fun getAttendanceByDate(date: String): Flow<List<AttendanceRecord>> =
        attendanceDao.getAttendanceByDate(date)
    fun getAttendanceByBatchAndDate(batchId: Long, date: String): Flow<List<AttendanceRecord>> =
        attendanceDao.getAttendanceByBatchAndDate(batchId, date)
    fun getAttendanceByBatchSubjectDate(batchId: Long, subjectId: Long, date: String): Flow<List<AttendanceRecord>> =
        attendanceDao.getAttendanceByBatchSubjectDate(batchId, subjectId, date)
    fun getAttendanceByBatchAndDateRange(batchId: Long, startDate: String, endDate: String): Flow<List<AttendanceRecord>> =
        attendanceDao.getAttendanceByBatchAndDateRange(batchId, startDate, endDate)
    fun getAttendanceByBatchSubjectAndDateRange(batchId: Long, subjectId: Long, startDate: String, endDate: String): Flow<List<AttendanceRecord>> =
        attendanceDao.getAttendanceByBatchSubjectAndDateRange(batchId, subjectId, startDate, endDate)
    fun getAttendanceByStudentAndDateRange(studentId: Long, startDate: String, endDate: String): Flow<List<AttendanceRecord>> =
        attendanceDao.getAttendanceByStudentAndDateRange(studentId, startDate, endDate)
    fun getAttendanceByStudentAndDate(studentId: Long, date: String): Flow<List<AttendanceRecord>> =
        attendanceDao.getAttendanceByStudentAndDate(studentId, date)
    suspend fun getCountByDateAndStatus(date: String, status: AttendanceStatus, batchId: Long): Int =
        attendanceDao.getCountByDateAndStatus(date, status, batchId)
    suspend fun getTotalCountByDate(date: String, batchId: Long): Int =
        attendanceDao.getTotalCountByDate(date, batchId)
    suspend fun insert(record: AttendanceRecord): Long = attendanceDao.insert(record)
    suspend fun insertAll(records: List<AttendanceRecord>) = attendanceDao.insertAll(records)
    suspend fun update(record: AttendanceRecord) = attendanceDao.update(record)
    suspend fun delete(record: AttendanceRecord) = attendanceDao.delete(record)
    suspend fun deleteByDateAndSubject(date: String, subjectId: Long) =
        attendanceDao.deleteByDateAndSubject(date, subjectId)
    suspend fun getCount(): Int = attendanceDao.getCount()
    suspend fun getRecentDates(): List<String> = attendanceDao.getRecentDates()
}

class ExamRepository(private val examDao: ExamDao) {
    fun getAllExams(): Flow<List<Exam>> = examDao.getAllExams()
    fun getExamsByBatch(batchId: Long): Flow<List<Exam>> = examDao.getExamsByBatch(batchId)
    fun getExamsBySubject(subjectId: Long): Flow<List<Exam>> = examDao.getExamsBySubject(subjectId)
    fun getExamsByStatus(status: ExamStatus): Flow<List<Exam>> = examDao.getExamsByStatus(status)
    fun searchExams(query: String): Flow<List<Exam>> = examDao.searchExams(query)
    fun getResultsByExam(examId: Long): Flow<List<ExamResult>> = examDao.getResultsByExam(examId)
    fun getResultsByStudent(studentId: Long): Flow<List<ExamResult>> = examDao.getResultsByStudent(studentId)
    suspend fun getExamById(id: Long): Exam? = examDao.getExamById(id)
    suspend fun getPendingExamCount(): Int = examDao.getPendingExamCount()
    suspend fun insert(exam: Exam): Long = examDao.insert(exam)
    suspend fun insertAll(exams: List<Exam>) = examDao.insertAll(exams)
    suspend fun update(exam: Exam) = examDao.update(exam)
    suspend fun delete(exam: Exam) = examDao.delete(exam)
    suspend fun getCount(): Int = examDao.getCount()
    suspend fun getResult(examId: Long, studentId: Long): ExamResult? = examDao.getResult(examId, studentId)
    suspend fun insertResult(result: ExamResult): Long = examDao.insertResult(result)
    suspend fun insertResults(results: List<ExamResult>) = examDao.insertResults(results)
    suspend fun updateResult(result: ExamResult) = examDao.updateResult(result)
    suspend fun deleteResultsByExam(examId: Long) = examDao.deleteResultsByExam(examId)
}

class NewsRepository(private val newsDao: NewsDao) {
    fun getAllNews(): Flow<List<NewsArticle>> = newsDao.getAllNews()
    fun getLatestNews(limit: Int = 5): Flow<List<NewsArticle>> = newsDao.getLatestNews(limit)
    suspend fun insert(article: NewsArticle): Long = newsDao.insert(article)
    suspend fun insertAll(articles: List<NewsArticle>) = newsDao.insertAll(articles)
    suspend fun delete(article: NewsArticle) = newsDao.delete(article)
}

class NotificationRepository(private val notificationDao: NotificationDao) {
    fun getNotificationsForUser(userId: Long): Flow<List<Notification>> =
        notificationDao.getNotificationsForUser(userId)

    fun getUnreadCount(userId: Long): Flow<Int> =
        notificationDao.getUnreadCount(userId)

    suspend fun markAsRead(notificationId: Long) =
        notificationDao.markAsRead(notificationId)

    suspend fun markAllAsRead(userId: Long) =
        notificationDao.markAllAsRead(userId)

    suspend fun insert(notification: Notification): Long =
        notificationDao.insert(notification)

    suspend fun delete(notification: Notification) =
        notificationDao.delete(notification)

    suspend fun clearHistory(userId: Long) =
        notificationDao.clearHistory(userId)
}

class StudentCategoryRepository(private val academyDao: AcademyDao) {
    fun getAllCategories(): Flow<List<StudentCategory>> = academyDao.getAllCategories()
    suspend fun insertCategory(category: StudentCategory) = academyDao.insertCategory(category)
    suspend fun deleteCategory(category: StudentCategory) = academyDao.deleteCategory(category)
    suspend fun getStudentCountByCategory(categoryId: Long): Int = academyDao.getStudentCountByCategory(categoryId)
}

class GraduationRepository(private val academyDao: AcademyDao) {
    fun getAllGraduationRecords(): Flow<List<GraduationRecord>> = academyDao.getAllGraduationRecords()
    suspend fun getGraduationRecordByStudent(studentId: Long): GraduationRecord? = academyDao.getGraduationRecordByStudent(studentId)
    suspend fun insertGraduationRecord(record: GraduationRecord) = academyDao.insertGraduationRecord(record)
    suspend fun updateGraduationRecord(record: GraduationRecord) = academyDao.updateGraduationRecord(record)
    fun getAlumni(): Flow<List<Student>> = academyDao.getAlumni()
}
