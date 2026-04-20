package com.ims.app.data.seed

import com.ims.app.data.IMSDatabase
import com.ims.app.data.entity.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

suspend fun seedDatabase(db: IMSDatabase) {
    val userDao = db.userDao()
    if (userDao.getCount() > 0) {
        ensureDefaultStudentCategories(db)
        ensureStudentCategoryAssignments(db)
        return
    }

    val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val today = LocalDate.now()

    // --- Users ---
    val users = mutableListOf(
        User(id = 1, name = "Dr. Admin", email = "admin@ims.edu", role = UserRole.ADMIN, department = "Administration"),
        User(id = 2, name = "Sarah Johnson", email = "sarah@ims.edu", role = UserRole.TEACHER, department = "Science"),
        User(id = 3, name = "Rahul Sharma", email = "rahul@ims.edu", role = UserRole.STUDENT, department = "Computer Science"),
        User(id = 4, name = "Priya Menon", email = "priya@ims.edu", role = UserRole.TEACHER, department = "Mathematics"),
        User(id = 5, name = "Mr. Gupta", email = "gupta@ims.edu", role = UserRole.PARENT, department = "")
    )

    // --- Courses ---
    val courses = listOf(
        Course(id = 1, name = "B.Tech Computer Science", code = "CS"),
        Course(id = 2, name = "B.Tech Mathematics", code = "MATH")
    )
    db.courseDao().insertAll(courses)

    // --- Batches ---
    val batches = listOf(
        Batch(id = 1, name = "CS-2024-A", courseId = 1, year = 2024),
        Batch(id = 2, name = "CS-2024-B", courseId = 1, year = 2024),
        Batch(id = 3, name = "MATH-2024", courseId = 2, year = 2024)
    )
    db.batchDao().insertAll(batches)

    // --- Subjects ---
    val subjects = listOf(
        Subject(id = 1, name = "Data Structures", batchId = 1, teacherId = 2),
        Subject(id = 2, name = "Algorithms", batchId = 1, teacherId = 2),
        Subject(id = 3, name = "Operating Systems", batchId = 2, teacherId = 2),
        Subject(id = 4, name = "Linear Algebra", batchId = 3, teacherId = 4),
        Subject(id = 5, name = "Calculus", batchId = 3, teacherId = 4),
        Subject(id = 6, name = "Database Systems", batchId = 2, teacherId = 2),
        Subject(id = 7, name = "Machine Learning", batchId = 1, teacherId = 2, isElective = true)
    )
    db.subjectDao().insertAll(subjects)

    // --- Students ---
    val studentNames = listOf(
        "Rahul Sharma", "Priya Patel", "Vikram Singh", "Anjali Gupta", "Rohan Das",
        "Sara Khan", "Aman Sharma", "Neha Mehta", "Arjun Reddy", "Kavita Joshi",
        "Raj Kumar", "Lakshmi Nair", "Suresh Babu", "Deepika Rao", "Karan Malhotra",
        "Ananya Sen", "Siddharth Iyer", "Ritu Verma", "Abhishek Pandey", "Sneha Kapoor"
    )
    
    val students = mutableListOf<Student>()
    studentNames.forEachIndexed { idx, name ->
        // Generate a User ID for each student
        // Rahul Sharma already has ID 3. Others will start from 100.
        val targetUserId = if (idx == 0) 3L else (100 + idx).toLong()
        
        if (idx > 0) {
            users.add(
                User(
                    id = targetUserId,
                    name = name,
                    email = name.lowercase().replace(" ", ".") + "@student.ims.edu",
                    role = UserRole.STUDENT,
                    department = "Computer Science"
                )
            )
        }

        students.add(
            Student(
                id = (idx + 1).toLong(),
                userId = targetUserId,
                batchId = when {
                    idx < 8 -> 1L
                    idx < 14 -> 2L
                    else -> 3L
                },
                rollNumber = "2024${(101 + idx)}",
                admissionDate = "2024-08-01",
                name = name,
                email = name.lowercase().replace(" ", ".") + "@student.ims.edu"
            )
        )
    }
    
    userDao.insertAll(users)
    db.studentDao().insertAll(students)

    // --- Attendance Records (last 14 days) ---
    val attendanceRecords = mutableListOf<AttendanceRecord>()
    var arId = 1L
    for (dayOffset in 0..13) {
        val date = today.minusDays(dayOffset.toLong()).format(fmt)
        for (student in students) {
            val subjectId = when (student.batchId) {
                1L -> if (dayOffset % 2 == 0) 1L else 2L
                2L -> if (dayOffset % 2 == 0) 3L else 6L
                else -> if (dayOffset % 2 == 0) 4L else 5L
            }
            val status = when {
                (arId % 10).toInt() == 0 -> AttendanceStatus.ABSENT
                else -> AttendanceStatus.PRESENT
            }
            attendanceRecords.add(
                AttendanceRecord(
                    id = arId++,
                    studentId = student.id,
                    subjectId = subjectId,
                    date = date,
                    status = status,
                    remarks = if (status == AttendanceStatus.ABSENT) "No reason provided" else ""
                )
            )
        }
    }
    db.attendanceDao().insertAll(attendanceRecords)

    // --- Exams ---
    val exams = listOf(
        Exam(id = 1, name = "Mid-Term: Data Structures", subjectId = 1, batchId = 1, date = today.minusDays(20).format(fmt), startTime = "09:00 AM", endTime = "12:00 PM", location = "Hall A", totalMarks = 100, type = ExamType.MARKS, evaluationMethod = EvaluationMethod.GPA, status = ExamStatus.RESULTS_PUBLISHED),
        Exam(id = 2, name = "Quiz 1: Algorithms", subjectId = 2, batchId = 1, date = today.minusDays(10).format(fmt), startTime = "11:30 AM", endTime = "12:30 PM", location = "Computer Lab 1", totalMarks = 50, type = ExamType.MARKS, evaluationMethod = EvaluationMethod.GPA, status = ExamStatus.COMPLETED),
        Exam(id = 3, name = "Final: Linear Algebra", subjectId = 4, batchId = 3, date = today.plusDays(7).format(fmt), startTime = "02:00 PM", endTime = "05:00 PM", location = "Auditorium", totalMarks = 100, type = ExamType.MARKS, evaluationMethod = EvaluationMethod.GPA, status = ExamStatus.PUBLISHED),
        Exam(id = 4, name = "Mid-Term: Operating Systems", subjectId = 3, batchId = 2, date = today.minusDays(5).format(fmt), startTime = "10:00 AM", endTime = "01:00 PM", location = "Room 102", totalMarks = 100, type = ExamType.MARKS, evaluationMethod = EvaluationMethod.GPA, status = ExamStatus.RESULTS_PUBLISHED),
        Exam(id = 5, name = "Quiz 2: Calculus", subjectId = 5, batchId = 3, date = today.plusDays(14).format(fmt), startTime = "09:00 AM", endTime = "10:00 AM", location = "Online (Exam Portal)", totalMarks = 30, type = ExamType.MARKS, evaluationMethod = EvaluationMethod.GPA, status = ExamStatus.DRAFT),
        // CCE grade-based assessment
        Exam(id = 6, name = "Assessment: Database Systems", subjectId = 6, batchId = 2, date = today.minusDays(3).format(fmt), startTime = "03:00 PM", endTime = "04:30 PM", location = "Hall B", totalMarks = 100, type = ExamType.GRADE, evaluationMethod = EvaluationMethod.CCE, status = ExamStatus.RESULTS_PUBLISHED),
        // CWA marks-based test
        Exam(id = 7, name = "Weekly Test: Calculus", subjectId = 5, batchId = 3, date = today.minusDays(1).format(fmt), startTime = "10:00 AM", endTime = "11:00 AM", location = "Room 205", totalMarks = 25, type = ExamType.MARKS, evaluationMethod = EvaluationMethod.CWA, status = ExamStatus.RESULTS_PUBLISHED)
    )
    db.examDao().insertAll(exams)

    // --- Exam Results (for completed/published exams) ---
    val results = mutableListOf<ExamResult>()
    var rId = 1L
    val completedExams = exams.filter { it.status == ExamStatus.RESULTS_PUBLISHED || it.status == ExamStatus.COMPLETED }
    for (exam in completedExams) {
        val batchStudents = students.filter { it.batchId == exam.batchId }
        for (student in batchStudents) {
            when (exam.evaluationMethod) {
                EvaluationMethod.GPA -> {
                    // GPA: generate marks and compute GPA grade
                    val marks = (45..exam.totalMarks).random().toDouble()
                    val grade = when {
                        marks / exam.totalMarks >= 0.9 -> "A+"
                        marks / exam.totalMarks >= 0.8 -> "A"
                        marks / exam.totalMarks >= 0.7 -> "B+"
                        marks / exam.totalMarks >= 0.6 -> "B"
                        marks / exam.totalMarks >= 0.5 -> "C"
                        marks / exam.totalMarks >= 0.4 -> "D"
                        else -> "F"
                    }
                    results.add(
                        ExamResult(
                            id = rId++,
                            examId = exam.id,
                            studentId = student.id,
                            marksObtained = marks,
                            grade = grade,
                            remarks = if (grade == "F") "Needs improvement" else ""
                        )
                    )
                }
                EvaluationMethod.CCE -> {
                    // CCE: grade-only exam (no numerical marks)
                    val cceGrades = listOf("A1", "A2", "B1", "B2", "C1", "C2", "D", "E (Needs Improvement)")
                    // Weighted random to make higher grades more common
                    val weights = listOf(15, 20, 20, 15, 12, 8, 6, 4)
                    val totalWeight = weights.sum()
                    val roll = (1..totalWeight).random()
                    var cumulative = 0
                    var selectedGrade = cceGrades.last()
                    for (i in cceGrades.indices) {
                        cumulative += weights[i]
                        if (roll <= cumulative) {
                            selectedGrade = cceGrades[i]
                            break
                        }
                    }
                    results.add(
                        ExamResult(
                            id = rId++,
                            examId = exam.id,
                            studentId = student.id,
                            marksObtained = 0.0, // Grade-only
                            grade = selectedGrade,
                            remarks = ""
                        )
                    )
                }
                EvaluationMethod.CWA -> {
                    // CWA: marks-based exam with percentage grade
                    val marks = (8..exam.totalMarks).random().toDouble()
                    val percent = marks / exam.totalMarks * 100
                    val grade = String.format("%.1f%%", percent)
                    results.add(
                        ExamResult(
                            id = rId++,
                            examId = exam.id,
                            studentId = student.id,
                            marksObtained = marks,
                            grade = grade,
                            remarks = ""
                        )
                    )
                }
            }
        }
    }
    db.examDao().insertResults(results)

    // --- News Articles ---
    val newsArticles = listOf(
        NewsArticle(id = 1, title = "Welcome to New Academic Year 2024-25", content = "We are excited to begin the new academic session. Orientation will be held on August 5th in the main auditorium.", publishedAt = today.minusDays(30).format(fmt), authorId = 1),
        NewsArticle(id = 2, title = "Mid-Term Exam Schedule Released", content = "The mid-term examination schedule for all batches has been published. Please check the Examinations module for details.", publishedAt = today.minusDays(15).format(fmt), authorId = 1),
        NewsArticle(id = 3, title = "Sports Day Announcement", content = "Annual sports day will be held on November 15th. All students are encouraged to participate in at least one event.", publishedAt = today.minusDays(5).format(fmt), authorId = 1),
        NewsArticle(id = 4, title = "Library Hours Extended", content = "Due to upcoming exams, library will remain open until 10 PM on weekdays. Weekend timings remain unchanged.", publishedAt = today.minusDays(2).format(fmt), authorId = 1),
        NewsArticle(id = 5, title = "Guest Lecture: AI in Education", content = "Dr. Smith from MIT will deliver a guest lecture on 'AI in Education' on December 1st at 3 PM in Lecture Hall A.", publishedAt = today.minusDays(1).format(fmt), authorId = 1)
    )
    db.newsDao().insertAll(newsArticles)

    // --- Student Categories ---
    ensureDefaultStudentCategories(db)
    ensureStudentCategoryAssignments(db)

    // --- Graduation Records ---
    val graduationRecords = listOf(
        GraduationRecord(id = 1, studentId = 1, graduationYear = 2024, status = GraduationStatus.CLEARED, certificateStatus = CertificateStatus.PROCESSING),
        GraduationRecord(id = 2, studentId = 2, graduationYear = 2024, status = GraduationStatus.PENDING_CLEARANCE, certificateStatus = CertificateStatus.NOT_REQUESTED),
        GraduationRecord(id = 3, studentId = 3, graduationYear = 2023, status = GraduationStatus.GRADUATED, certificateStatus = CertificateStatus.ISSUED)
    )
    db.academyDao().insertGraduationRecord(graduationRecords[0])
    db.academyDao().insertGraduationRecord(graduationRecords[1])
    db.academyDao().insertGraduationRecord(graduationRecords[2])
}

private suspend fun ensureDefaultStudentCategories(db: IMSDatabase) {
    if (db.academyDao().getCategoryCount() > 0) return

    val categories = listOf(
        StudentCategory(id = 1, name = "General", description = "Regular students"),
        StudentCategory(id = 2, name = "Scholarship", description = "Merit-based scholarship students"),
        StudentCategory(id = 3, name = "Residential", description = "Hostel residents"),
        StudentCategory(id = 4, name = "Exchange", description = "International exchange students")
    )
    db.academyDao().insertCategory(categories[0])
    db.academyDao().insertCategory(categories[1])
    db.academyDao().insertCategory(categories[2])
    db.academyDao().insertCategory(categories[3])
}

private suspend fun ensureStudentCategoryAssignments(db: IMSDatabase) {
    if (db.studentDao().getUncategorizedCount() == 0) return
    db.studentDao().assignDefaultCategoriesToUncategorized()
}
