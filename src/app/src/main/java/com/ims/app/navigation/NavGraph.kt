package com.ims.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ims.app.R
import com.ims.app.ui.attendance.*
import com.ims.app.ui.dashboard.*
import com.ims.app.ui.examination.*
import com.ims.app.ui.theme.PrimaryBlue
import com.ims.app.ui.theme.TextMuted
import com.ims.app.data.entity.ExamStatus
import kotlinx.coroutines.launch

data class BottomNavItem(
    val route: String,
    val labelRes: Int,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun IMSNavGraph(
    dashboardViewModel: DashboardViewModel,
    attendanceViewModel: AttendanceViewModel,
    examViewModel: ExamViewModel,
    notificationViewModel: com.ims.app.ui.notification.NotificationViewModel,
    academyViewModel: com.ims.app.ui.academy.AcademyViewModel
) {
    val navController = rememberNavController()
    val dashboardState by dashboardViewModel.state.collectAsState()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val coroutineScope = rememberCoroutineScope()

    val bottomNavItems = listOf(
        BottomNavItem(Screen.DashboardHome.route, R.string.bottom_nav_home, Icons.Filled.Home, Icons.Outlined.Home),
        BottomNavItem(
            if (dashboardState.currentRole == com.ims.app.data.entity.UserRole.STUDENT) Screen.AttendanceMy.route else Screen.AttendanceMark.route,
            R.string.bottom_nav_attendance,
            Icons.Filled.FactCheck,
            Icons.Outlined.FactCheck
        ),
        BottomNavItem(Screen.ExamList.createRoute(), R.string.bottom_nav_exams, Icons.Filled.Quiz, Icons.Outlined.Quiz),
        BottomNavItem(Screen.DashboardProfile.route, R.string.bottom_nav_profile, Icons.Filled.Person, Icons.Outlined.Person)
    )

    val showBottomBar = bottomNavItems.any { item ->
        currentRoute != null && (currentRoute == item.route || currentRoute.split("?")[0] == item.route.split("?")[0])
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = androidx.compose.ui.unit.Dp(2f)
                ) {
                    bottomNavItems.forEach { item ->
                        val isSelected = currentRoute != null && (
                            currentRoute == item.route || 
                            currentRoute.split("?")[0] == item.route.split("?")[0]
                        )
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = stringResource(id = item.labelRes)
                                )
                            },
                            label = {
                                Text(
                                    stringResource(id = item.labelRes),
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != item.route) {
                                    if (item.route.startsWith(Screen.ExamList.route.split("?")[0])) {
                                        examViewModel.filterByStatus(null)
                                    }
                                    navController.navigate(item.route) {
                                        popUpTo(Screen.DashboardHome.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PrimaryBlue,
                                selectedTextColor = PrimaryBlue,
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted,
                                indicatorColor = PrimaryBlue.copy(alpha = 0.1f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    onLogin = { role ->
                        dashboardViewModel.switchRole(role)
                        navController.navigate(Screen.DashboardHome.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }
            // Dashboard
            composable(Screen.DashboardHome.route) {
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    onNavigateToAttendance = {
                        val route = if (dashboardState.currentRole == com.ims.app.data.entity.UserRole.STUDENT) Screen.AttendanceMy.route else Screen.AttendanceMark.route
                        navController.navigate(route)
                    },
                    onNavigateToExams = { status -> 
                        val resultsMode = status == com.ims.app.data.entity.ExamStatus.RESULTS_PUBLISHED
                        navController.navigate(Screen.ExamList.createRoute(status?.name, resultsMode = resultsMode)) 
                    },
                    onCreateExam = { navController.navigate(Screen.ExamCreate.route) },
                    onNavigateToSettings = { navController.navigate(Screen.DashboardSettings.route) },
                    onNavigateToProfile = { navController.navigate(Screen.DashboardProfile.route) },
                    onNavigateToSearch = { navController.navigate(Screen.DashboardSearch.route) },
                    onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },
                    onNavigateToCourseBatch = { 
                        if (dashboardState.currentRole == com.ims.app.data.entity.UserRole.STUDENT) {
                            val studentId = dashboardState.currentStudentId ?: 0L
                            coroutineScope.launch {
                                val batchId = academyViewModel.getStudentBatchId(studentId)
                                if (batchId != null) {
                                    navController.navigate(Screen.BatchDashboard.createRoute(batchId))
                                }
                            }
                        } else {
                            navController.navigate(Screen.ManageAcademy.route)
                        }
                    },
                    onNavigateToAdmissionForms = { navController.navigate(Screen.PlaceholderAdmissionForms.route) },
                    onNavigateToStudentCategories = { navController.navigate(Screen.PlaceholderStudentCategories.route) },
                    onNavigateToGraduationFacilities = { navController.navigate(Screen.PlaceholderGraduationFacilities.route) },
                    onNavigateToStudentsList = { navController.navigate(Screen.DashboardStudentsList.route) },
                    onNavigateToBatchesList = { navController.navigate(Screen.DashboardBatchesList.route) },
                    onNavigateToExamResults = { examId ->
                        navController.navigate(Screen.ExamViewResults.createRoute(examId))
                    }
                )
            }
            composable(Screen.DashboardStudentsList.route) {
                StudentsListScreen(
                    viewModel = dashboardViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.DashboardBatchesList.route) {
                BatchesListScreen(
                    viewModel = dashboardViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.DashboardSearch.route) {
                SearchScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToFeature = { route -> navController.navigate(route) }
                )
            }
            composable(Screen.DashboardSettings.route) {
                SettingsScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.DashboardProfile.route) {
                ProfileScreen(
                    viewModel = dashboardViewModel,
                    onBack = { navController.popBackStack() },
                    onSignOut = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.DashboardHome.route) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(Screen.ManageAcademy.route) {
                com.ims.app.ui.academy.ManageAcademyScreen(
                    viewModel = academyViewModel,
                    onBack = { navController.popBackStack() },
                    onCourseClick = { courseId -> navController.navigate(Screen.CourseBatches.createRoute(courseId)) }
                )
            }
            composable(
                Screen.CourseBatches.route,
                arguments = listOf(navArgument("courseId") { type = NavType.LongType })
            ) { backStackEntry ->
                val courseId = backStackEntry.arguments?.getLong("courseId") ?: 0L
                com.ims.app.ui.academy.CourseBatchesScreen(
                    courseId = courseId,
                    viewModel = academyViewModel,
                    onBack = { navController.popBackStack() },
                    onBatchClick = { batchId -> navController.navigate(Screen.BatchDashboard.createRoute(batchId)) }
                )
            }
            composable(
                Screen.BatchDashboard.route,
                arguments = listOf(navArgument("batchId") { type = NavType.LongType })
            ) { backStackEntry ->
                val batchId = backStackEntry.arguments?.getLong("batchId") ?: 0L
                com.ims.app.ui.academy.BatchDashboardScreen(
                    batchId = batchId,
                    viewModel = academyViewModel,
                    role = dashboardState.currentRole,
                    onBack = { navController.popBackStack() },
                    onTransferStudents = { navController.navigate(Screen.BatchTransfers.createRoute(batchId)) }
                )
            }
            composable(
                Screen.BatchTransfers.route,
                arguments = listOf(navArgument("batchId") { type = NavType.LongType })
            ) { backStackEntry ->
                val batchId = backStackEntry.arguments?.getLong("batchId") ?: 0L
                com.ims.app.ui.academy.BatchTransfersScreen(
                    batchId = batchId,
                    viewModel = academyViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.PlaceholderAdmissionForms.route) {
                FeaturePlaceholderScreen(
                    title = stringResource(id = R.string.placeholder_title_admission_forms),
                    description = stringResource(id = R.string.placeholder_desc_admission_forms),
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.PlaceholderSmsAlerts.route) {
                FeaturePlaceholderScreen(
                    title = stringResource(id = R.string.placeholder_title_sms_module),
                    description = stringResource(id = R.string.placeholder_desc_sms_module),
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.PlaceholderStudentCategories.route) {
                com.ims.app.ui.academy.StudentCategoriesScreen(
                    viewModel = academyViewModel,
                    role = dashboardState.currentRole,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.PlaceholderGraduationFacilities.route) {
                com.ims.app.ui.academy.GraduationFacilitiesScreen(
                    viewModel = academyViewModel,
                    role = dashboardState.currentRole,
                    studentId = dashboardState.currentStudentId,
                    onBack = { navController.popBackStack() }
                )
            }

            // Attendance
            composable(Screen.AttendanceMark.route) {
                MarkAttendanceScreen(
                    viewModel = attendanceViewModel,
                    currentUserId = dashboardState.currentUser?.id ?: 0L,
                    isReadOnly = false,
                    onOpenReports = { navController.navigate(Screen.AttendanceReport.route) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.AttendanceMy.route) {
                MyAttendanceScreen(
                    viewModel = attendanceViewModel,
                    studentId = dashboardState.currentStudentId ?: 0L,
                    onBack = { navController.popBackStack() },
                    onOpenReport = { navController.navigate(Screen.AttendanceReport.route) }
                )
            }
            composable(Screen.AttendanceReport.route) {
                AttendanceReportScreen(
                    viewModel = attendanceViewModel,
                    role = dashboardState.currentRole,
                    studentId = dashboardState.currentStudentId,
                    onBack = { navController.popBackStack() }
                )
            }

            // Examinations
            composable(
                route = Screen.ExamList.route,
                arguments = listOf(
                    navArgument("status") { nullable = true; type = NavType.StringType },
                    navArgument("resultsMode") { defaultValue = "false"; type = NavType.StringType }
                )
            ) { backStackEntry ->
                val statusStr = backStackEntry.arguments?.getString("status")
                val resultsMode = backStackEntry.arguments?.getString("resultsMode") == "true"
                LaunchedEffect(statusStr) {
                    val status = statusStr?.let { 
                        try { ExamStatus.valueOf(it) } catch (e: Exception) { null }
                    }
                    if (status != null) {
                        examViewModel.filterByStatus(status)
                    }
                }
                ExamListScreen(
                    viewModel = examViewModel,
                    role = dashboardState.currentRole,
                    resultsMode = resultsMode,
                    onBack = { navController.popBackStack() },
                    onCreateExam = { navController.navigate(Screen.ExamCreate.route) },
                    onExamClick = { examId ->
                        navController.navigate(Screen.ExamViewResults.createRoute(examId))
                    },
                    onRecordMarks = { examId ->
                        navController.navigate(Screen.ExamRecordMarks.createRoute(examId))
                    },
                    onViewResults = { examId ->
                        navController.navigate(Screen.ExamViewResults.createRoute(examId))
                    }
                )
            }
            composable(Screen.ExamCreate.route) {
                CreateExamScreen(
                    viewModel = examViewModel,
                    isReadOnly = dashboardState.currentRole == com.ims.app.data.entity.UserRole.STUDENT,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                Screen.ExamRecordMarks.route,
                arguments = listOf(navArgument("examId") { type = NavType.LongType })
            ) { backStackEntry ->
                val examId = backStackEntry.arguments?.getLong("examId") ?: 0L
                RecordMarksScreen(
                    viewModel = examViewModel,
                    examId = examId,
                    isReadOnly = dashboardState.currentRole == com.ims.app.data.entity.UserRole.STUDENT,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                Screen.ExamViewResults.route,
                arguments = listOf(navArgument("examId") { type = NavType.LongType })
            ) { backStackEntry ->
                val examId = backStackEntry.arguments?.getLong("examId") ?: 0L
                ViewResultsScreen(
                    viewModel = examViewModel,
                    examId = examId,
                    role = dashboardState.currentRole,
                    studentId = dashboardState.currentStudentId,
                    onBack = { navController.popBackStack() },
                    onSummaryReport = { selectedExamId ->
                        navController.navigate(Screen.ExamSummaryReport.createRoute(selectedExamId))
                    },
                    onStudentReportCard = { studentId ->
                        navController.navigate(Screen.ExamReportCard.createRoute(studentId))
                    }
                )
            }
            composable(
                Screen.ExamSummaryReport.route,
                arguments = listOf(navArgument("examId") { type = NavType.LongType })
            ) { backStackEntry ->
                val examId = backStackEntry.arguments?.getLong("examId") ?: 0L
                ExamSummaryReportScreen(
                    viewModel = examViewModel,
                    examId = examId,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                Screen.ExamReportCard.route,
                arguments = listOf(navArgument("studentId") { type = NavType.LongType })
            ) { backStackEntry ->
                val studentId = backStackEntry.arguments?.getLong("studentId") ?: 0L
                ReportCardScreen(
                    viewModel = examViewModel,
                    studentId = studentId,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Notifications.route) {
                com.ims.app.ui.notification.NotificationListScreen(
                    viewModel = notificationViewModel,
                    userId = dashboardState.currentUser?.id ?: 0L,
                    role = dashboardState.currentRole,
                    onBack = { navController.popBackStack() },
                    onCompose = { navController.navigate(Screen.SendNotification.route) }
                )
            }
            composable(Screen.SendNotification.route) {
                com.ims.app.ui.notification.SendNotificationScreen(
                    viewModel = notificationViewModel,
                    role = dashboardState.currentRole,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
