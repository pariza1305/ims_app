package com.ims.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ims.app.data.entity.*
import com.ims.app.data.repository.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class DashboardState(
    val currentUser: User? = null,
    val currentRole: UserRole = UserRole.ADMIN,
    val currentStudentId: Long? = null,
    val currentStudent: Student? = null,
    val students: List<Student> = emptyList(),
    val totalStudents: Int = 0,
    val todayAttendancePercent: Int = 0,
    val pendingExams: Int = 0,
    val latestNews: List<NewsArticle> = emptyList(),
    val recentExams: List<Exam> = emptyList(),
    val batches: List<Batch> = emptyList(),
    val searchQuery: String = "",
    val searchResults: SearchResults = SearchResults(),
    val unreadNotificationCount: Int = 0,
    val isLoading: Boolean = true
)

data class SearchResults(
    val students: List<Student> = emptyList(),
    val exams: List<Exam> = emptyList(),
    val users: List<User> = emptyList(),
    val attendanceRecords: List<AttendanceRecord> = emptyList()
)
class DashboardViewModel(
    private val userRepository: UserRepository,
    private val studentRepository: StudentRepository,
    private val batchRepository: BatchRepository,
    private val attendanceRepository: AttendanceRepository,
    private val examRepository: ExamRepository,
    private val newsRepository: NewsRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val pendingExams = examRepository.getPendingExamCount()

            val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val totalToday = try {
                attendanceRepository.getTotalCountByDate(today, 1)
            } catch (_: Exception) { 0 }
            val presentToday = try {
                attendanceRepository.getCountByDateAndStatus(today, AttendanceStatus.PRESENT, 1)
            } catch (_: Exception) { 0 }
            val attendancePercent = if (totalToday > 0) (presentToday * 100 / totalToday) else 0

            _state.update {
                it.copy(
                    todayAttendancePercent = attendancePercent,
                    pendingExams = pendingExams,
                    isLoading = false
                )
            }
        }

        viewModelScope.launch {
            studentRepository.getAllStudents().collect { students ->
                _state.update { it.copy(students = students, totalStudents = students.size) }
            }
        }

        viewModelScope.launch {
            newsRepository.getLatestNews(5).collect { news ->
                _state.update { it.copy(latestNews = news) }
            }
        }

        viewModelScope.launch {
            examRepository.getAllExams().collect { exams ->
                _state.update { it.copy(recentExams = exams.take(3)) }
            }
        }

        viewModelScope.launch {
            batchRepository.getAllBatches().collect { batches ->
                _state.update { it.copy(batches = batches) }
            }
        }
    }

    fun switchRole(role: UserRole) {
        viewModelScope.launch {
            val userId = when (role) {
                UserRole.ADMIN -> 1L
                UserRole.TEACHER -> 2L
                UserRole.STUDENT -> 3L
                UserRole.PARENT -> 5L
            }
            val user = userRepository.getUserById(userId)
            val studentId = if (role == UserRole.STUDENT) 1L else null
            val student = if (studentId != null) studentRepository.getStudentById(studentId) else null
            _state.update { it.copy(currentRole = role, currentUser = user, currentStudentId = studentId, currentStudent = student) }
            
            // Observe unread notifications for the new user
            viewModelScope.launch {
                notificationRepository.getUnreadCount(userId).collect { count ->
                    _state.update { it.copy(unreadNotificationCount = count) }
                }
            }
        }
    }

    fun updateProfile(name: String, email: String, phone: String, department: String) {
        viewModelScope.launch {
            val user = _state.value.currentUser ?: return@launch
            val updatedUser = user.copy(
                name = name,
                email = email,
                phone = phone,
                department = department
            )
            userRepository.update(updatedUser)
            _state.update { it.copy(currentUser = updatedUser) }
        }
    }

    fun search(query: String) {
        _state.update { it.copy(searchQuery = query) }
        if (query.length < 2) {
            _state.update { it.copy(searchResults = SearchResults()) }
            return
        }
        viewModelScope.launch {
            studentRepository.searchStudents(query).collect { students ->
                _state.update { it.copy(searchResults = it.searchResults.copy(students = students)) }
            }
        }
        viewModelScope.launch {
            examRepository.searchExams(query).collect { exams ->
                _state.update { it.copy(searchResults = it.searchResults.copy(exams = exams)) }
            }
        }
        viewModelScope.launch {
            userRepository.searchUsers(query).collect { users ->
                _state.update { it.copy(searchResults = it.searchResults.copy(users = users)) }
            }
        }
        viewModelScope.launch {
            attendanceRepository.getAttendanceByDate(query).collect { records ->
                _state.update { it.copy(searchResults = it.searchResults.copy(attendanceRecords = records.take(20))) }
            }
        }
    }

    fun refresh() {
        loadDashboardData()
    }

    class Factory(
        private val userRepository: UserRepository,
        private val studentRepository: StudentRepository,
        private val batchRepository: BatchRepository,
        private val attendanceRepository: AttendanceRepository,
        private val examRepository: ExamRepository,
        private val newsRepository: NewsRepository,
        private val notificationRepository: NotificationRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DashboardViewModel(
                userRepository, studentRepository, batchRepository,
                attendanceRepository, examRepository, newsRepository,
                notificationRepository
            ) as T
        }
    }
}
