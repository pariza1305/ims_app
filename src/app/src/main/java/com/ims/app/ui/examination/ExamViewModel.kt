package com.ims.app.ui.examination

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ims.app.data.entity.*
import com.ims.app.data.repository.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ExamState(
    val exams: List<Exam> = emptyList(),
    val filteredExams: List<Exam> = emptyList(),
    val selectedExam: Exam? = null,
    val batches: List<Batch> = emptyList(),
    val subjects: List<Subject> = emptyList(),
    val students: List<Student> = emptyList(),
    val results: List<ExamResult> = emptyList(),
    val studentResults: List<ExamResult> = emptyList(),
    val selectedBatchId: Long? = null,
    val selectedSubjectId: Long? = null,
    val filterStatus: ExamStatus? = null,
    val marksMap: Map<Long, Double> = emptyMap(),
    val gradesMap: Map<Long, String> = emptyMap(),
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val isLoading: Boolean = false,
    val searchQuery: String = ""
)

class ExamViewModel(
    private val examRepository: ExamRepository,
    private val batchRepository: BatchRepository,
    private val subjectRepository: SubjectRepository,
    private val studentRepository: StudentRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ExamState())
    val state: StateFlow<ExamState> = _state.asStateFlow()

    init {
        loadExams()
        loadBatches()
    }

    private fun loadExams() {
        viewModelScope.launch {
            examRepository.getAllExams().collect { exams ->
                _state.update { it.copy(exams = exams, filteredExams = applyFilters(exams, it.filterStatus, it.searchQuery)) }
            }
        }
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

    fun filterByStatus(status: ExamStatus?) {
        _state.update {
            it.copy(
                filterStatus = status,
                filteredExams = applyFilters(it.exams, status, it.searchQuery)
            )
        }
    }

    fun searchExams(query: String) {
        _state.update {
            it.copy(
                searchQuery = query,
                filteredExams = applyFilters(it.exams, it.filterStatus, query)
            )
        }
    }

    private fun applyFilters(exams: List<Exam>, status: ExamStatus?, query: String): List<Exam> {
        return exams.filter { exam ->
            (status == null || exam.status == status) &&
            (query.isEmpty() || exam.name.contains(query, ignoreCase = true))
        }
    }

    fun createExam(
        name: String,
        subjectId: Long,
        batchId: Long,
        groupName: String,
        groupedSubjectIds: String,
        date: String,
        startTime: String,
        endTime: String,
        location: String,
        totalMarks: Int,
        type: ExamType,
        customOptions: String,
        evaluationMethod: EvaluationMethod,
        status: ExamStatus
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            
            // If groupedSubjectIds is not empty, we create an exam for each subject
            val targetIds = if (groupedSubjectIds.isNotEmpty()) {
                groupedSubjectIds.split(",").mapNotNull { it.trim().toLongOrNull() }.toSet() + subjectId
            } else {
                setOf(subjectId)
            }

            targetIds.forEach { sid ->
                val exam = Exam(
                    name = name,
                    subjectId = sid,
                    batchId = batchId,
                    groupName = groupName,
                    groupedSubjectIds = groupedSubjectIds,
                    date = date,
                    startTime = startTime,
                    endTime = endTime,
                    location = location,
                    totalMarks = totalMarks,
                    type = type,
                    customOptions = customOptions,
                    evaluationMethod = evaluationMethod,
                    status = status
                )
                examRepository.insert(exam)
            }
            
            _state.update { it.copy(isSaving = false, isSaved = true) }
        }
    }

    fun updateExam(exam: Exam) {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            examRepository.update(exam)
            _state.update { it.copy(isSaving = false, isSaved = true) }
        }
    }

    fun deleteExam(exam: Exam) {
        viewModelScope.launch {
            examRepository.deleteResultsByExam(exam.id)
            examRepository.delete(exam)
        }
    }

    fun loadExamForRecording(examId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val exam = examRepository.getExamById(examId)
            _state.update { it.copy(selectedExam = exam) }

            exam?.let { e ->
                studentRepository.getStudentsByBatch(e.batchId).collect { students ->
                    _state.update { it.copy(students = students) }
                }
            }
        }
        viewModelScope.launch {
            examRepository.getResultsByExam(examId).collect { results ->
                val marksMap = results.associate { it.studentId to it.marksObtained }
                val gradesMap = results.associate { it.studentId to it.grade }
                _state.update { it.copy(results = results, marksMap = marksMap, gradesMap = gradesMap, isLoading = false) }
            }
        }
    }

    fun setMarks(studentId: Long, marks: Double) {
        val exam = _state.value.selectedExam ?: return
        val grade = calculateGrade(marks, exam.totalMarks, exam.evaluationMethod)
        _state.update { st ->
            st.copy(
                marksMap = st.marksMap.toMutableMap().apply { put(studentId, marks) },
                gradesMap = st.gradesMap.toMutableMap().apply { put(studentId, grade) },
                isSaved = false
            )
        }
    }

    fun setGradeOnly(studentId: Long, grade: String) {
        _state.update { st ->
            st.copy(
                gradesMap = st.gradesMap.toMutableMap().apply { put(studentId, grade) },
                marksMap = st.marksMap.toMutableMap().apply { put(studentId, 0.0) },
                isSaved = false
            )
        }
    }

    private fun calculateGrade(marks: Double, total: Int, evalMethod: com.ims.app.data.entity.EvaluationMethod): String {
        val percent = marks / total
        return when (evalMethod) {
            com.ims.app.data.entity.EvaluationMethod.GPA -> {
                when {
                    percent >= 0.9 -> "A+"
                    percent >= 0.8 -> "A"
                    percent >= 0.7 -> "B+"
                    percent >= 0.6 -> "B"
                    percent >= 0.5 -> "C"
                    percent >= 0.4 -> "D"
                    else -> "F"
                }
            }
            com.ims.app.data.entity.EvaluationMethod.CCE -> {
                when {
                    percent >= 0.91 -> "A1"
                    percent >= 0.81 -> "A2"
                    percent >= 0.71 -> "B1"
                    percent >= 0.61 -> "B2"
                    percent >= 0.51 -> "C1"
                    percent >= 0.41 -> "C2"
                    percent >= 0.33 -> "D"
                    else -> "E (Needs Improvement)"
                }
            }
            com.ims.app.data.entity.EvaluationMethod.CWA -> {
                String.format("%.1f%%", percent * 100)
            }
        }
    }

    fun saveResults() {
        val exam = _state.value.selectedExam ?: return
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            examRepository.deleteResultsByExam(exam.id)

            val results = _state.value.students.map { student ->
                val marks = _state.value.marksMap[student.id] ?: 0.0
                ExamResult(
                    examId = exam.id,
                    studentId = student.id,
                    marksObtained = marks,
                    grade = if (exam.type == com.ims.app.data.entity.ExamType.MARKS) calculateGrade(marks, exam.totalMarks, exam.evaluationMethod) else _state.value.gradesMap[student.id] ?: ""
                )
            }
            examRepository.insertResults(results)

            // Update exam status
            if (exam.status != ExamStatus.RESULTS_PUBLISHED) {
                examRepository.update(exam.copy(status = ExamStatus.COMPLETED))
            }

            _state.update { it.copy(isSaving = false, isSaved = true) }
        }
    }

    fun loadResultsByExam(examId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val exam = examRepository.getExamById(examId)
            _state.update { it.copy(selectedExam = exam) }

            exam?.let { e ->
                studentRepository.getStudentsByBatch(e.batchId).collect { students ->
                    _state.update { it.copy(students = students) }
                }
            }
        }
        viewModelScope.launch {
            examRepository.getResultsByExam(examId).collect { results ->
                _state.update { it.copy(results = results, isLoading = false) }
            }
        }
    }

    fun loadStudentResults(studentId: Long) {
        viewModelScope.launch {
            examRepository.getResultsByStudent(studentId).collect { results ->
                _state.update { it.copy(studentResults = results) }
            }
        }
    }

    fun publishResults(examId: Long) {
        viewModelScope.launch {
            val exam = examRepository.getExamById(examId) ?: return@launch
            examRepository.update(exam.copy(status = ExamStatus.RESULTS_PUBLISHED))
        }
    }

    fun updateExamStatus(examId: Long, status: ExamStatus) {
        viewModelScope.launch {
            val exam = examRepository.getExamById(examId) ?: return@launch
            examRepository.update(exam.copy(status = status))
        }
    }

    fun resetSaved() {
        _state.update { it.copy(isSaved = false) }
    }

    class Factory(
        private val examRepository: ExamRepository,
        private val batchRepository: BatchRepository,
        private val subjectRepository: SubjectRepository,
        private val studentRepository: StudentRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ExamViewModel(examRepository, batchRepository, subjectRepository, studentRepository) as T
        }
    }
}
