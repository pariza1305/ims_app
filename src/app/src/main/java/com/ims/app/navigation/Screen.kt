package com.ims.app.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object DashboardHome : Screen("dashboard_home")
    object DashboardSearch : Screen("dashboard_search")
    object DashboardSettings : Screen("dashboard_settings")
    object DashboardProfile : Screen("dashboard_profile")
    object DashboardStudentsList : Screen("dashboard_students_list")
    object DashboardBatchesList : Screen("dashboard_batches_list")
    object ManageAcademy : Screen("manage_academy")
    object CourseBatches : Screen("course_batches/{courseId}") {
        fun createRoute(courseId: Long) = "course_batches/$courseId"
    }
    object BatchDashboard : Screen("batch_dashboard/{batchId}") {
        fun createRoute(batchId: Long) = "batch_dashboard/$batchId"
    }
    object BatchTransfers : Screen("batch_transfers/{batchId}") {
        fun createRoute(batchId: Long) = "batch_transfers/$batchId"
    }
    object PlaceholderAdmissionForms : Screen("placeholder_admission_forms")
    object PlaceholderSmsAlerts : Screen("placeholder_sms_alerts")
    object PlaceholderStudentCategories : Screen("placeholder_student_categories")
    object PlaceholderGraduationFacilities : Screen("placeholder_graduation_facilities")
    object AttendanceMark : Screen("attendance_mark")
    object AttendanceMy : Screen("attendance_my")
    object AttendanceReport : Screen("attendance_report")
    object ExamList : Screen("exam_list?status={status}&resultsMode={resultsMode}") {
        fun createRoute(status: String? = null, resultsMode: Boolean = false) = 
            "exam_list?status=${status ?: ""}&resultsMode=$resultsMode"
    }
    object ExamCreate : Screen("exam_create")
    object ExamRecordMarks : Screen("exam_record_marks/{examId}") {
        fun createRoute(examId: Long) = "exam_record_marks/$examId"
    }
    object ExamViewResults : Screen("exam_view_results/{examId}") {
        fun createRoute(examId: Long) = "exam_view_results/$examId"
    }
    object ExamReportCard : Screen("exam_report_card/{studentId}") {
        fun createRoute(studentId: Long) = "exam_report_card/$studentId"
    }
    object ExamSummaryReport : Screen("exam_summary_report/{examId}") {
        fun createRoute(examId: Long) = "exam_summary_report/$examId"
    }
    object Notifications : Screen("notifications")
    object SendNotification : Screen("send_notification")
}
