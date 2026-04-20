package com.ims.app.ui.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ims.app.data.entity.*
import com.ims.app.data.repository.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

data class StudentAttendanceSummary(
    val subjectId: Long,
    val subjectName: String,
    val totalClasses: Int,
    val presentCount: Int,
    val absentCount: Int
)

data class AttendanceState(
    val batches: List<Batch> = emptyList(),
    val subjects: List<Subject> = emptyList(),
    val students: List<Student> = emptyList(),
    val selectedBatchId: Long? = null,
    val selectedSubjectId: Long? = null,
    val selectedDate: String = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
    val attendanceMap: Map<Long, AttendanceStatus> = emptyMap(),
    val remarksMap: Map<Long, String> = emptyMap(),
    val existingRecords: List<AttendanceRecord> = emptyList(),
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val isLoading: Boolean = false,
    // Report state
    val reportRecords: List<AttendanceRecord> = emptyList(),
    val reportStudents: List<Student> = emptyList(),
    val reportMonth: Int = LocalDate.now().monthValue,
    val reportYear: Int = LocalDate.now().year,
    val recentDates: List<String> = emptyList(),
    val reportSubjectId: Long? = null,
    // Student summary state
    val studentSummary: List<StudentAttendanceSummary> = emptyList(),
    val filterAcadYear: String = "2025-26",
    val filterSemester: String = "Spring"
)

class AttendanceViewModel(
    private val batchRepository: BatchRepository,
    private val subjectRepository: SubjectRepository,
    private val studentRepository: StudentRepository,
    private val attendanceRepository: AttendanceRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AttendanceState())
    val state: StateFlow<AttendanceState> = _state.asStateFlow()

    init {
        loadBatches()
        loadRecentDates()
    }

    private fun loadBatches() {
        viewModelScope.launch {
            batchRepository.getAllBatches().collect { batches ->
                _state.update { it.copy(batches = batches) }
            }
        }
        viewModelScope.launch {
            subjectRepository.getAllSubjects().collect { subjects ->
                _state.update { it.copy(subjects = subjects) }
            }
        }
    }

    private fun loadRecentDates() {
        viewModelScope.launch {
            val dates = attendanceRepository.getRecentDates()
            _state.update { it.copy(recentDates = dates) }
        }
    }

    fun selectBatch(batchId: Long) {
        _state.update { it.copy(selectedBatchId = batchId, selectedSubjectId = null, students = emptyList(), attendanceMap = emptyMap()) }
        viewModelScope.launch {
            subjectRepository.getSubjectsByBatch(batchId).collect { subjects ->
                _state.update { it.copy(subjects = subjects) }
            }
        }
    }

    fun selectSubject(subjectId: Long) {
        _state.update { it.copy(selectedSubjectId = subjectId) }
        loadStudentsAndAttendance()
    }

    fun selectDate(date: String) {
        _state.update { it.copy(selectedDate = date, isSaved = false) }
        loadStudentsAndAttendance()
    }

    private fun loadStudentsAndAttendance() {
        val batchId = _state.value.selectedBatchId ?: return
        val subjectId = _state.value.selectedSubjectId ?: return
        val date = _state.value.selectedDate

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            studentRepository.getStudentsByBatch(batchId).collect { students ->
                _state.update { it.copy(students = students) }
            }
        }
        viewModelScope.launch {
            attendanceRepository.getAttendanceByBatchSubjectDate(batchId, subjectId, date).collect { records ->
                val map = records.associate { it.studentId to it.status }
                val remarks = records.associate { it.studentId to it.remarks }
                _state.update { it.copy(
                    existingRecords = records,
                    attendanceMap = map,
                    remarksMap = remarks,
                    isLoading = false
                ) }
            }
        }
    }

    fun markAttendance(studentId: Long, status: AttendanceStatus) {
        _state.update { it.copy(
            attendanceMap = it.attendanceMap.toMutableMap().apply { put(studentId, status) },
            isSaved = false
        ) }
    }

    fun setRemarks(studentId: Long, remarks: String) {
        _state.update { it.copy(
            remarksMap = it.remarksMap.toMutableMap().apply { put(studentId, remarks) }
        ) }
    }

    fun saveAttendance(currentUserId: Long) {
        val batchId = _state.value.selectedBatchId ?: return
        val subjectId = _state.value.selectedSubjectId ?: return
        val date = _state.value.selectedDate
        val subjectName = _state.value.subjects.find { it.id == subjectId }?.name ?: "Subject"

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            // Delete existing and re-insert
            attendanceRepository.deleteByDateAndSubject(date, subjectId)

            val records = _state.value.students.map { student ->
                val status = _state.value.attendanceMap[student.id] ?: AttendanceStatus.PRESENT
                
                val remarks = _state.value.remarksMap[student.id] ?: ""
                // Trigger notification for absent students
                if (status == AttendanceStatus.ABSENT) {
                    val message = "You have been marked ABSENT for $subjectName on $date." +
                            if (remarks.isNotEmpty()) "\n\nTeacher's Note: $remarks" else ""
                    
                    notificationRepository.insert(
                        Notification(
                            userId = student.userId,
                            title = "Absence Recorded",
                            message = message,
                            timestamp = java.time.LocalDateTime.now().toString(),
                            type = NotificationType.ATTENDANCE
                        )
                    )
                }

                AttendanceRecord(
                    studentId = student.id,
                    subjectId = subjectId,
                    date = date,
                    status = status,
                    remarks = _state.value.remarksMap[student.id] ?: ""
                )
            }
            attendanceRepository.insertAll(records)

            // Notify Teacher (Confirmation)
            notificationRepository.insert(
                Notification(
                    userId = currentUserId,
                    title = "Attendance Submitted",
                    message = "Attendance for $subjectName (Batch ID: $batchId) successfully saved for $date.",
                    timestamp = java.time.LocalDateTime.now().toString(),
                    type = NotificationType.ATTENDANCE
                )
            )

            _state.update { it.copy(isSaving = false, isSaved = true) }
        }
    }

    // Reports
    fun loadDailyReport(date: String, batchId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            studentRepository.getStudentsByBatch(batchId).collect { students ->
                _state.update { it.copy(reportStudents = students) }
            }
        }
        viewModelScope.launch {
            attendanceRepository.getAttendanceByBatchAndDate(batchId, date).collect { records ->
                _state.update { it.copy(reportRecords = records, isLoading = false) }
            }
        }
    }

    fun loadMonthlyReport(batchId: Long, month: Int, year: Int) {
        val ym = YearMonth.of(year, month)
        val startDate = ym.atDay(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val endDate = ym.atEndOfMonth().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        _state.update { it.copy(reportMonth = month, reportYear = year) }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            studentRepository.getStudentsByBatch(batchId).collect { students ->
                _state.update { it.copy(reportStudents = students) }
            }
        }
        viewModelScope.launch {
            attendanceRepository.getAttendanceByBatchAndDateRange(batchId, startDate, endDate).collect { records ->
                _state.update { it.copy(reportRecords = records, isLoading = false) }
            }
        }
    }

    fun loadSubjectWiseReport(batchId: Long, subjectId: Long, month: Int, year: Int) {
        val ym = YearMonth.of(year, month)
        val startDate = ym.atDay(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val endDate = ym.atEndOfMonth().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        _state.update { it.copy(reportMonth = month, reportYear = year, reportSubjectId = subjectId) }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            studentRepository.getStudentsByBatch(batchId).collect { students ->
                _state.update { it.copy(reportStudents = students) }
            }
        }
        viewModelScope.launch {
            attendanceRepository.getAttendanceByBatchSubjectAndDateRange(batchId, subjectId, startDate, endDate).collect { records ->
                _state.update { it.copy(reportRecords = records, isLoading = false) }
            }
        }
    }

    fun loadStudentDailyReport(studentId: Long, date: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val student = studentRepository.getStudentById(studentId)
            _state.update { it.copy(reportStudents = listOfNotNull(student)) }
        }
        viewModelScope.launch {
            attendanceRepository.getAttendanceByStudentAndDate(studentId, date).collect { records ->
                _state.update { it.copy(reportRecords = records, isLoading = false) }
            }
        }
    }

    fun loadStudentMonthlyReport(studentId: Long, month: Int, year: Int) {
        val ym = YearMonth.of(year, month)
        val startDate = ym.atDay(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val endDate = ym.atEndOfMonth().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        _state.update { it.copy(reportMonth = month, reportYear = year) }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val student = studentRepository.getStudentById(studentId)
            _state.update { it.copy(reportStudents = listOfNotNull(student)) }
        }
        viewModelScope.launch {
            attendanceRepository.getAttendanceByStudentAndDateRange(studentId, startDate, endDate).collect { records ->
                _state.update { it.copy(reportRecords = records, isLoading = false) }
            }
        }
    }

    fun setStudentFilters(year: String, semester: String, studentId: Long) {
        _state.update { it.copy(filterAcadYear = year, filterSemester = semester) }
        loadStudentAttendanceSummary(studentId)
    }

    fun loadStudentAttendanceSummary(studentId: Long) {
        val acadYear = _state.value.filterAcadYear // e.g. "2025-26"
        val semester = _state.value.filterSemester // e.g. "Spring"

        // Deriving date range from filters
        val startYear = acadYear.split("-")[0].toInt() // 2025
        val (startDate, endDate) = if (semester == "Fall") {
            // Fall: July to December of transition year
            "$startYear-07-01" to "$startYear-12-31"
        } else {
            // Spring: January to June of the second year
            val endYear = startYear + 1
            "$endYear-01-01" to "$endYear-06-30"
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            // Collect both attendance and subjects for name mapping
            combine(
                attendanceRepository.getAttendanceByStudentAndDateRange(studentId, startDate, endDate),
                subjectRepository.getAllSubjects()
            ) { records, subjects ->
                val grouped = records.groupBy { it.subjectId }
                subjects.filter { s -> grouped.containsKey(s.id) }.map { subject ->
                    val subjectRecords = grouped[subject.id] ?: emptyList()
                    StudentAttendanceSummary(
                        subjectId = subject.id,
                        subjectName = subject.name,
                        totalClasses = subjectRecords.size,
                        presentCount = subjectRecords.count { it.status == AttendanceStatus.PRESENT },
                        absentCount = subjectRecords.count { it.status == AttendanceStatus.ABSENT }
                    )
                }
            }.collect { summary ->
                _state.update { it.copy(studentSummary = summary, isLoading = false) }
            }
        }
    }

    class Factory(
        private val batchRepository: BatchRepository,
        private val subjectRepository: SubjectRepository,
        private val studentRepository: StudentRepository,
        private val attendanceRepository: AttendanceRepository,
        private val notificationRepository: NotificationRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AttendanceViewModel(
                batchRepository,
                subjectRepository,
                studentRepository,
                attendanceRepository,
                notificationRepository
            ) as T
        }
    }
}
