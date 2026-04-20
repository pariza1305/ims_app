package com.ims.app.ui.examination

import com.ims.app.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ims.app.data.entity.UserRole
import com.ims.app.data.entity.ExamStatus
import com.ims.app.ui.components.*
import com.ims.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamListScreen(
    viewModel: ExamViewModel,
    role: UserRole,
    resultsMode: Boolean = false,
    onBack: () -> Unit,
    onCreateExam: () -> Unit,
    onExamClick: (Long) -> Unit,
    onRecordMarks: (Long) -> Unit,
    onViewResults: (Long) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val canEdit = role != UserRole.STUDENT
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (resultsMode) "Published Results" 
                        else if (role == UserRole.STUDENT) "Recent & Upcoming Exams" 
                        else "Examinations", 
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            if (canEdit && !resultsMode) {
                ExtendedFloatingActionButton(
                    onClick = onCreateExam,
                    containerColor = PrimaryBlue,
                    contentColor = TextOnPrimary,
                    icon = { Icon(Icons.Filled.Add, "Create") },
                    text = { Text(stringResource(R.string.str_new_exam)) }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            if (role != UserRole.STUDENT && !resultsMode) {
                // Search
                IMSSearchBar(
                    query = state.searchQuery,
                    onQueryChange = { viewModel.searchExams(it) },
                    placeholder = "Search exams..."
                )

                Spacer(Modifier.height(12.dp))

                // Filter chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = state.filterStatus == null,
                        onClick = { viewModel.filterByStatus(null) },
                        label = { Text(stringResource(R.string.str_all)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryBlue.copy(alpha = 0.1f),
                            selectedLabelColor = PrimaryBlue
                        )
                    )
                    ExamStatus.entries.forEach { status ->
                        FilterChip(
                            selected = state.filterStatus == status,
                            onClick = { viewModel.filterByStatus(status) },
                            label = { Text(status.name.replace("_", " ")) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryBlue.copy(alpha = 0.1f),
                                selectedLabelColor = PrimaryBlue
                            )
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    "${state.filteredExams.size} exams",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMuted
                )
                Spacer(Modifier.height(8.dp))
            } else if (resultsMode) {
                Spacer(Modifier.height(16.dp))
                Text(
                    "Displaying all finalized results",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PrimaryBlue,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(8.dp))
            } else {
                Spacer(Modifier.height(16.dp))
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(state.filteredExams, key = { it.id }) { exam ->
                    val subjectName = state.subjects.find { it.id == exam.subjectId }?.name ?: "Subject"
                    
                    if (role == UserRole.STUDENT) {
                        EventExamCard(
                            examName = exam.name,
                            subjectName = subjectName,
                            date = exam.date,
                            startTime = exam.startTime,
                            endTime = exam.endTime,
                            location = exam.location,
                            status = exam.status.name,
                            onInfoClick = { onViewResults(exam.id) },
                            onClick = { onViewResults(exam.id) }
                        )
                    } else {
                        ExamCard(
                            examName = exam.name,
                            subjectName = subjectName,
                            date = exam.date,
                            status = exam.status.name,
                            totalMarks = exam.totalMarks,
                            examType = exam.type.name,
                            evaluationMethod = exam.evaluationMethod.name,
                            actionIcon = if (exam.status == ExamStatus.DRAFT) Icons.Outlined.Publish else null,
                            onActionClick = {
                                if (exam.status == ExamStatus.DRAFT) {
                                    viewModel.updateExamStatus(exam.id, ExamStatus.PUBLISHED)
                                }
                            },
                            onClick = {
                                if (exam.status == ExamStatus.DRAFT) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("This exam is a Draft. Please Publish it to record marks.")
                                    }
                                } else {
                                    if (!canEdit) onViewResults(exam.id)
                                    else when (exam.status) {
                                        ExamStatus.RESULTS_PUBLISHED, ExamStatus.COMPLETED -> onViewResults(exam.id)
                                        else -> onRecordMarks(exam.id)
                                    }
                                }
                            }
                        )
                    }
                }

                if (state.filteredExams.isEmpty()) {
                    item {
                        EmptyState(
                            icon = Icons.Outlined.Quiz,
                            title = "No exams found",
                            subtitle = "Create a new exam or change filters"
                        )
                    }
                }
            }
        }
    }
}
