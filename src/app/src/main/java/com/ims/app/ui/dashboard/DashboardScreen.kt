package com.ims.app.ui.dashboard

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ims.app.R
import com.ims.app.data.entity.UserRole
import com.ims.app.data.entity.ExamStatus
import com.ims.app.ui.components.*
import com.ims.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToAttendance: () -> Unit,
    onNavigateToExams: (ExamStatus?) -> Unit,
    onCreateExam: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToCourseBatch: () -> Unit,
    onNavigateToAdmissionForms: () -> Unit,
    onNavigateToStudentCategories: () -> Unit,
    onNavigateToGraduationFacilities: () -> Unit,
    onNavigateToStudentsList: () -> Unit,
    onNavigateToBatchesList: () -> Unit,
    onNavigateToExamResults: (Long) -> Unit,
    onNavigateToNotifications: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val roleOptions = listOf(UserRole.ADMIN, UserRole.TEACHER, UserRole.STUDENT)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = stringResource(id = R.string.dashboard_demo_role_switch),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(16.dp)
                )
                roleOptions.forEach { role ->
                    val isSelected = role == state.currentRole
                    NavigationDrawerItem(
                        label = { Text(localizedRoleLabel(role)) },
                        selected = isSelected,
                        onClick = {
                            viewModel.switchRole(role)
                            scope.launch { drawerState.close() }
                        },
                        icon = {
                            val icon = when (role) {
                                UserRole.ADMIN -> Icons.Filled.AdminPanelSettings
                                UserRole.TEACHER -> Icons.Filled.School
                                UserRole.STUDENT -> Icons.Filled.Person
                                UserRole.PARENT -> Icons.Filled.FamilyRestroom
                            }
                            Icon(icon, localizedRoleLabel(role))
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                    )
                }
            }
        }
    ) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                actions = {
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(Icons.Outlined.Search, stringResource(id = R.string.search_title))
                    }
                    IconButton(onClick = onNavigateToNotifications) {
                        BadgedBox(
                            badge = {
                                if (state.unreadNotificationCount > 0) {
                                    Badge(
                                        containerColor = DangerRed,
                                        contentColor = Color.White
                                    ) {
                                        Text("${state.unreadNotificationCount}")
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Outlined.Notifications, "Notifications")
                        }
                    }
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(Icons.Outlined.Menu, stringResource(id = R.string.dashboard_role_switch_drawer))
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Outlined.Settings, stringResource(id = R.string.settings_title))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->

        val quickActions = listOfNotNull(
            QuickActionItem(Icons.Filled.EditNote, stringResource(id = if (state.currentRole == UserRole.STUDENT) R.string.dashboard_my_attendance else R.string.dashboard_mark_attendance), SecondaryGreen, onNavigateToAttendance),
            if (state.currentRole != UserRole.STUDENT) QuickActionItem(Icons.Filled.NoteAdd, stringResource(id = R.string.dashboard_create_exam), PrimaryBlue, onCreateExam) else null,
            QuickActionItem(Icons.Filled.Assessment, stringResource(id = R.string.dashboard_view_results), WarningAmber, { onNavigateToExams(ExamStatus.RESULTS_PUBLISHED) }),
            QuickActionItem(Icons.Filled.Folder, stringResource(id = if (state.currentRole == UserRole.STUDENT) R.string.dashboard_my_academy else R.string.dashboard_courses_batches), PrimaryBlue, onNavigateToCourseBatch),
            if (state.currentRole == UserRole.ADMIN) QuickActionItem(Icons.Filled.PostAdd, stringResource(id = R.string.dashboard_admission_forms), SecondaryGreen, onNavigateToAdmissionForms) else null,
            if (state.currentRole != UserRole.STUDENT) QuickActionItem(Icons.Filled.Groups, stringResource(id = R.string.dashboard_student_categories), PrimaryBlue, onNavigateToStudentCategories) else null,
            QuickActionItem(Icons.Filled.School, if (state.currentRole == UserRole.STUDENT) "Graduation Status" else stringResource(id = R.string.dashboard_graduation_facilities), SecondaryGreen, onNavigateToGraduationFacilities)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Greeting
            item {
                Column {
                    Text(
                        text = stringResource(id = R.string.dashboard_good_morning),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(
                                id = R.string.dashboard_hi_with_name,
                                state.currentUser?.name ?: localizedRoleLabel(state.currentRole)
                            ),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                        Spacer(Modifier.weight(1f))
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(PrimaryBlue.copy(alpha = 0.1f), CircleShape)
                                .clip(CircleShape)
                                .clickable(onClick = onNavigateToProfile),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (state.currentUser?.name?.first()?.uppercase() ?: state.currentRole.name.first().uppercase()).toString(),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    StatusChip(
                        text = stringResource(id = R.string.dashboard_role_with_value, localizedRoleLabel(state.currentRole)),
                        color = PrimaryBlue
                    )
                }
            }

            // Quick Stats
            item {
                SectionHeader(title = stringResource(id = R.string.dashboard_quick_stats))
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = stringResource(id = R.string.dashboard_total_students),
                        value = "${state.totalStudents}",
                        subtitle = stringResource(id = R.string.dashboard_across_all_batches),
                        icon = Icons.Outlined.Groups,
                        iconTint = PrimaryBlue,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToStudentsList
                    )
                    StatCard(
                        title = stringResource(id = R.string.dashboard_attendance),
                        value = "${state.todayAttendancePercent}%",
                        subtitle = stringResource(id = R.string.dashboard_todays_average),
                        icon = Icons.Outlined.CheckCircle,
                        iconTint = SecondaryGreen,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToAttendance
                    )
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = stringResource(id = R.string.dashboard_pending_exams),
                        value = "${state.pendingExams}",
                        subtitle = stringResource(id = R.string.dashboard_upcoming),
                        icon = Icons.Outlined.Assignment,
                        iconTint = WarningAmber,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToExams(null) }
                    )
                    StatCard(
                        title = stringResource(id = R.string.dashboard_batches),
                        value = "${state.batches.size}",
                        subtitle = stringResource(id = R.string.dashboard_active_batches),
                        icon = Icons.Outlined.Class,
                        iconTint = PrimaryBlue,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToBatchesList
                    )
                }
            }

            // Quick Actions
            item {
                SectionHeader(title = stringResource(id = R.string.dashboard_quick_actions))
            }

            items(quickActions.chunked(3)) { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowItems.forEach { item ->
                        QuickActionButton(
                            icon = item.icon,
                            label = item.label,
                            color = item.color,
                            onClick = item.onClick,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Fill remaining space with invisible spacers to maintain consistent 1/3 item widths
                    repeat(3 - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            // Recent Exams
            if (state.recentExams.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = stringResource(id = R.string.dashboard_recent_exams),
                        actionText = stringResource(id = R.string.dashboard_view_all),
                        onAction = { onNavigateToExams(null) }
                    )
                }
                items(state.recentExams) { exam ->
                    ExamCard(
                        examName = exam.name,
                        subjectName = stringResource(id = R.string.dashboard_subject_id_with_value, exam.subjectId),
                        date = exam.date,
                        status = exam.status.name,
                        totalMarks = exam.totalMarks,
                        onClick = { onNavigateToExamResults(exam.id) }
                    )
                }
            }

            // Latest News
            if (state.latestNews.isNotEmpty()) {
                item {
                    SectionHeader(title = stringResource(id = R.string.dashboard_latest_news))
                }
                items(state.latestNews) { article ->
                    NewsCard(
                        title = article.title,
                        content = article.content,
                        date = article.publishedAt
                    )
                }
            }
        }
    }
    }
}

@Composable
private fun localizedRoleLabel(role: UserRole): String {
    return when (role) {
        UserRole.ADMIN -> stringResource(id = R.string.role_admin)
        UserRole.TEACHER -> stringResource(id = R.string.role_teacher)
        UserRole.STUDENT -> stringResource(id = R.string.role_student)
        UserRole.PARENT -> stringResource(id = R.string.role_parent)
    }
}

@Composable
private fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, label, tint = color, modifier = Modifier.size(24.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = color,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

private data class QuickActionItem(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String,
    val color: androidx.compose.ui.graphics.Color,
    val onClick: () -> Unit
)
