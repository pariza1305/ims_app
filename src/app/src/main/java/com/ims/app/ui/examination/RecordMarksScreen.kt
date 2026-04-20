package com.ims.app.ui.examination

import com.ims.app.R
import androidx.compose.ui.res.stringResource
import com.ims.app.data.entity.ExamType
import com.ims.app.data.entity.ExamStatus
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ims.app.ui.components.*
import com.ims.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordMarksScreen(
    viewModel: ExamViewModel,
    examId: Long,
    isReadOnly: Boolean,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(examId) {
        viewModel.loadExamForRecording(examId)
    }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            snackbarHostState.showSnackbar("Marks saved successfully ✓")
            viewModel.resetSaved()
        }
    }

    val exam = state.selectedExam

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Record Marks", fontWeight = FontWeight.SemiBold)
                        exam?.let {
                            Text(
                                "${it.name} • ${it.evaluationMethod.name} • Total: ${it.totalMarks}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            if (state.students.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val filled = state.marksMap.size
                        val total = state.students.size
                        Text(
                            "Filled: $filled / $total students",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        if (exam?.status == ExamStatus.DRAFT) {
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                            ) {
                                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Warning, null, tint = MaterialTheme.colorScheme.error)
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            "The exam is in Draft mode. Registration of marks is disabled until published.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                        TextButton(
                                            onClick = { viewModel.updateExamStatus(exam.id, ExamStatus.PUBLISHED) },
                                            modifier = Modifier.align(Alignment.End)
                                        ) {
                                            Text("Publish Now", color = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = {
                                if (exam?.status != ExamStatus.DRAFT) {
                                    viewModel.saveResults()
                                    // If it was just published, move to in-progress
                                    if (exam?.status == ExamStatus.PUBLISHED) {
                                        viewModel.updateExamStatus(exam.id, ExamStatus.IN_PROGRESS)
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            enabled = !state.isSaving && !isReadOnly && exam?.status != ExamStatus.DRAFT
                        ) {
                            if (state.isSaving) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = TextOnPrimary, strokeWidth = 2.dp)
                                Spacer(Modifier.width(8.dp))
                            }
                            Icon(Icons.Filled.Save, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Save All Marks", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp, top = 8.dp)
        ) {
            items(state.students, key = { it.id }) { student ->
                val currentMarks = state.marksMap[student.id]
                val currentGrade = state.gradesMap[student.id] ?: ""
                var marksText by remember(currentMarks) {
                    mutableStateOf(currentMarks?.let { String.format("%.0f", it) } ?: "")
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(PrimaryBlue.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                student.name.first().uppercase(),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(student.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text("Roll: ${student.rollNumber}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        }
                        Spacer(Modifier.width(8.dp))
                        if (exam?.type == ExamType.MARKS) {
                            OutlinedTextField(
                                value = marksText,
                                onValueChange = { input ->
                                    marksText = input
                                    if (!isReadOnly) {
                                        input.toDoubleOrNull()?.let { marks ->
                                            if (marks in 0.0..(exam.totalMarks.toDouble())) {
                                                viewModel.setMarks(student.id, marks)
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.width(80.dp),
                                enabled = !isReadOnly,
                                placeholder = { Text(stringResource(R.string.str_0)) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(8.dp),
                                textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Spacer(Modifier.width(8.dp))
                            if (currentGrade.isNotEmpty()) {
                                StatusChip(text = currentGrade, color = gradeColor(currentGrade))
                            }
                        } else {
                            // GRADE/CUSTOM type: show dropdown based on evaluation method or custom options
                            val gradeOptions = when {
                                exam?.type == ExamType.CUSTOM -> {
                                    exam.customOptions.split(",").filter { it.isNotBlank() }.map { it.trim() }
                                }
                                exam?.evaluationMethod == com.ims.app.data.entity.EvaluationMethod.GPA ->
                                    listOf("A+", "A", "B+", "B", "C", "D", "F")
                                exam?.evaluationMethod == com.ims.app.data.entity.EvaluationMethod.CCE ->
                                    listOf("A1", "A2", "B1", "B2", "C1", "C2", "D", "E (Needs Improvement)")
                                exam?.evaluationMethod == com.ims.app.data.entity.EvaluationMethod.CWA ->
                                    listOf("Outstanding", "Excellent", "Very Good", "Good", "Average", "Below Average")
                                else -> emptyList()
                            }
                            var expanded by remember { mutableStateOf(false) }

                            ExposedDropdownMenuBox(
                                expanded = expanded && !isReadOnly,
                                onExpandedChange = { if (!isReadOnly) expanded = it }
                            ) {
                                OutlinedTextField(
                                    value = currentGrade,
                                    onValueChange = {},
                                    readOnly = true,
                                    modifier = Modifier
                                        .width(130.dp)
                                        .menuAnchor(),
                                    enabled = !isReadOnly,
                                    placeholder = { Text(if (exam?.type == ExamType.CUSTOM) "Select" else "Grade", style = MaterialTheme.typography.labelSmall) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    textStyle = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                    }
                                )
                                ExposedDropdownMenu(
                                    expanded = expanded && !isReadOnly,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    gradeOptions.forEach { grade ->
                                        DropdownMenuItem(
                                            text = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(8.dp)
                                                            .background(gradeColor(grade), CircleShape)
                                                    )
                                                    Spacer(Modifier.width(8.dp))
                                                    Text(grade)
                                                }
                                            },
                                            onClick = {
                                                viewModel.setGradeOnly(student.id, grade)
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (state.students.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Outlined.EditNote,
                        title = "No students",
                        subtitle = "No students enrolled in this exam's batch"
                    )
                }
            }
        }
    }
}
