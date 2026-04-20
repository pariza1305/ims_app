package com.ims.app.ui.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ims.app.data.entity.*
import com.ims.app.data.repository.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class NotificationState(
    val notifications: List<Notification> = emptyList(),
    val batches: List<Batch> = emptyList(),
    val students: List<Student> = emptyList(),
    val unreadCount: Int = 0,
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val sendStatus: String? = null
)

class NotificationViewModel(
    private val notificationRepository: NotificationRepository,
    private val attendanceRepository: AttendanceRepository,
    private val studentRepository: StudentRepository,
    private val batchRepository: BatchRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(NotificationState())
    val state: StateFlow<NotificationState> = _state.asStateFlow()

    fun loadNotifications(userId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            // Collect notifications
            notificationRepository.getNotificationsForUser(userId).collect { notifications ->
                _state.update { it.copy(notifications = notifications, isLoading = false) }
            }
        }
        
        viewModelScope.launch {
            notificationRepository.getUnreadCount(userId).collect { count ->
                _state.update { it.copy(unreadCount = count) }
            }
        }
    }

    fun loadBatches() {
        viewModelScope.launch {
            batchRepository.getAllBatches().collect { batches ->
                _state.update { it.copy(batches = batches) }
            }
        }
    }

    fun loadStudents() {
        viewModelScope.launch {
            studentRepository.getAllStudents().collect { students ->
                _state.update { it.copy(students = students) }
            }
        }
    }

    fun loadStudentsByBatch(batchId: Long) {
        viewModelScope.launch {
            studentRepository.getStudentsByBatch(batchId).collect { students ->
                _state.update { it.copy(students = students) }
            }
        }
    }

    fun searchStudents(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                _state.update { it.copy(students = emptyList()) }
                return@launch
            }
            studentRepository.searchStudents(query).collect { students ->
                _state.update { it.copy(students = students) }
            }
        }
    }

    fun sendGroupNotification(batchId: Long, title: String, message: String) {
        viewModelScope.launch {
            _state.update { it.copy(isSending = true, sendStatus = null) }
            try {
                studentRepository.getStudentsByBatch(batchId).first().forEach { student ->
                    // Try to find the correct userId if student.userId is 0 (seed data issue)
                    var targetUserId = student.userId
                    if (targetUserId == 0L) {
                        val matchingUser = userRepository.searchUsers(student.name).first().firstOrNull()
                        if (matchingUser != null) {
                            targetUserId = matchingUser.id
                        }
                    }
                    
                    if (targetUserId != 0L) {
                        val notification = Notification(
                            userId = targetUserId,
                            title = title,
                            message = message,
                            timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                        )
                        notificationRepository.insert(notification)
                    }
                }
                _state.update { it.copy(isSending = false, sendStatus = "Success") }
            } catch (e: Exception) {
                _state.update { it.copy(isSending = false, sendStatus = "Error: ${e.message}") }
            }
        }
    }

    fun sendSingleNotification(studentId: Long, title: String, message: String) {
        viewModelScope.launch {
            _state.update { it.copy(isSending = true, sendStatus = null) }
            try {
                val student = studentRepository.getStudentById(studentId)
                if (student != null) {
                    // Try to find the correct userId if student.userId is 0 (seed data issue)
                    var targetUserId = student.userId
                    if (targetUserId == 0L) {
                        val matchingUser = userRepository.searchUsers(student.name).first().firstOrNull()
                        if (matchingUser != null) {
                            targetUserId = matchingUser.id
                        }
                    }

                    if (targetUserId != 0L) {
                        val notification = Notification(
                            userId = targetUserId,
                            title = title,
                            message = message,
                            timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                        )
                        notificationRepository.insert(notification)
                    }
                }
                _state.update { it.copy(isSending = false, sendStatus = "Success") }
            } catch (e: Exception) {
                _state.update { it.copy(isSending = false, sendStatus = "Error: ${e.message}") }
            }
        }
    }

    fun clearStatus() {
        _state.update { it.copy(sendStatus = null) }
    }

    fun markAsRead(notificationId: Long) {
        viewModelScope.launch {
            notificationRepository.markAsRead(notificationId)
        }
    }

    fun markAllAsRead(userId: Long) {
        viewModelScope.launch {
            notificationRepository.markAllAsRead(userId)
        }
    }

    fun deleteNotification(notification: Notification) {
        viewModelScope.launch {
            notificationRepository.delete(notification)
        }
    }

    fun clearHistory(userId: Long) {
        viewModelScope.launch {
            notificationRepository.clearHistory(userId)
        }
    }

    class Factory(
        private val notificationRepository: NotificationRepository,
        private val attendanceRepository: AttendanceRepository,
        private val studentRepository: StudentRepository,
        private val batchRepository: BatchRepository,
        private val userRepository: UserRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NotificationViewModel(
                notificationRepository,
                attendanceRepository,
                studentRepository,
                batchRepository,
                userRepository
            ) as T
        }
    }
}
