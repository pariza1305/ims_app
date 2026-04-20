package com.ims.app.ui.academy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ims.app.data.entity.*
import com.ims.app.data.repository.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class AcademyViewModel(
    private val courseRepository: CourseRepository,
    private val batchRepository: BatchRepository,
    private val subjectRepository: SubjectRepository,
    private val studentRepository: StudentRepository,
    private val categoryRepository: StudentCategoryRepository,
    private val graduationRepository: GraduationRepository
) : ViewModel() {

    fun getAllCourses(): Flow<List<Course>> = courseRepository.getAllCourses()
    
    fun getBatchesForCourse(courseId: Long): Flow<List<Batch>> = batchRepository.getBatchesByCourse(courseId)
    
    fun getSubjectsForBatch(batchId: Long): Flow<List<Subject>> = subjectRepository.getSubjectsByBatch(batchId)
    
    fun getStudentsForBatch(batchId: Long): Flow<List<Student>> = studentRepository.getStudentsByBatch(batchId)

    fun getStudentsByCategory(categoryId: Long): Flow<List<Student>> = studentRepository.getStudentsByCategory(categoryId)

    suspend fun getStudentBatchId(studentId: Long): Long? = studentRepository.getStudentById(studentId)?.batchId

    suspend fun getCourseById(id: Long) = courseRepository.getCourseById(id)
    suspend fun getBatchById(id: Long) = batchRepository.getBatchById(id)
    fun getAllBatches(): Flow<List<Batch>> = batchRepository.getAllBatches()

    fun toggleElectiveStatus(subject: Subject) {
        viewModelScope.launch {
            subjectRepository.update(subject.copy(isElective = !subject.isElective))
        }
    }

    fun transferStudents(studentIds: Set<Long>, destinationBatchId: Long) {
        viewModelScope.launch {
            studentIds.forEach { id ->
                val student = studentRepository.getStudentById(id)
                if (student != null) {
                    studentRepository.update(student.copy(batchId = destinationBatchId))
                }
            }
        }
    }

    // --- Student Categories ---
    fun getAllCategories() = categoryRepository.getAllCategories()
    
    fun addCategory(name: String, description: String) {
        viewModelScope.launch {
            categoryRepository.insertCategory(StudentCategory(name = name, description = description))
        }
    }

    fun deleteCategory(category: StudentCategory) {
        viewModelScope.launch {
            categoryRepository.deleteCategory(category)
        }
    }

    suspend fun getStudentCountByCategory(categoryId: Long) = categoryRepository.getStudentCountByCategory(categoryId)

    fun assignCategoryToStudent(studentId: Long, categoryId: Long?) {
        viewModelScope.launch {
            val student = studentRepository.getStudentById(studentId)
            if (student != null) {
                studentRepository.update(student.copy(categoryId = categoryId))
            }
        }
    }

    // --- Graduation ---
    fun getAllGraduationRecords() = graduationRepository.getAllGraduationRecords()
    
    fun updateGraduationStatus(record: GraduationRecord) {
        viewModelScope.launch {
            graduationRepository.updateGraduationRecord(record)
        }
    }

    fun getAlumni() = graduationRepository.getAlumni()

    suspend fun getGraduationRecordForStudent(studentId: Long) = graduationRepository.getGraduationRecordByStudent(studentId)

    fun registerGraduation(studentId: Long, year: Int) {
        viewModelScope.launch {
            val existing = graduationRepository.getGraduationRecordByStudent(studentId)
            if (existing == null) {
                graduationRepository.insertGraduationRecord(GraduationRecord(studentId = studentId, graduationYear = year))
            }
        }
    }

    class Factory(
        private val courseRepository: CourseRepository,
        private val batchRepository: BatchRepository,
        private val subjectRepository: SubjectRepository,
        private val studentRepository: StudentRepository,
        private val categoryRepository: StudentCategoryRepository,
        private val graduationRepository: GraduationRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AcademyViewModel(
                courseRepository, batchRepository, subjectRepository, 
                studentRepository, categoryRepository, graduationRepository
            ) as T
        }
    }
}
