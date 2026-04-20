package com.ims.app.ui.dashboard

import androidx.annotation.StringRes
import com.ims.app.R
import com.ims.app.navigation.Screen

data class DashboardFeature(
    @StringRes val titleRes: Int,
    @StringRes val keywordsRes: Int,
    val route: String
)

val dashboardFeatureCatalog = listOf(
    DashboardFeature(R.string.feature_dashboard_home, R.string.feature_keywords_dashboard_home, Screen.DashboardHome.route),
    DashboardFeature(R.string.feature_settings, R.string.feature_keywords_settings, Screen.DashboardSettings.route),
    DashboardFeature(R.string.feature_profile, R.string.feature_keywords_profile, Screen.DashboardProfile.route),
    DashboardFeature(R.string.feature_mark_attendance, R.string.feature_keywords_mark_attendance, Screen.AttendanceMark.route),
    DashboardFeature(R.string.feature_attendance_reports, R.string.feature_keywords_attendance_reports, Screen.AttendanceReport.route),
    DashboardFeature(R.string.feature_examinations, R.string.feature_keywords_examinations, Screen.ExamList.route),
    DashboardFeature(R.string.feature_create_exam, R.string.feature_keywords_create_exam, Screen.ExamCreate.route),
    DashboardFeature(R.string.feature_manage_courses, R.string.feature_keywords_manage_courses, Screen.ManageAcademy.route),
    DashboardFeature(R.string.feature_custom_admission_forms, R.string.feature_keywords_custom_admission_forms, Screen.PlaceholderAdmissionForms.route),
    DashboardFeature(R.string.feature_sms_alerts, R.string.feature_keywords_sms_alerts, Screen.PlaceholderSmsAlerts.route),
    DashboardFeature(R.string.feature_student_categories, R.string.feature_keywords_student_categories, Screen.PlaceholderStudentCategories.route),
    DashboardFeature(R.string.feature_graduation_facilities, R.string.feature_keywords_graduation_facilities, Screen.PlaceholderGraduationFacilities.route)
)
