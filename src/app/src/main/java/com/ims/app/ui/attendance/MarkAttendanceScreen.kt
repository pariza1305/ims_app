package com.ims.app.ui.attendance

import com.ims.app.R
import androidx.compose.ui.res.stringResource
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ims.app.data.entity.AttendanceStatus
import com.ims.app.ui.components.*
import com.ims.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkAttendanceScreen(
    viewModel: AttendanceViewModel,
    currentUserId: Long,
    isReadOnly: Boolean,
    onOpenReports: () -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var expandedBatch by remember { mutableStateOf(false) }
    var expandedSubject by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            snackbarHostState.showSnackbar("Attendance saved successfully ✓")
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Mark Attendance", fontWeight = FontWeight.SemiBold)
                        if (state.selectedBatchId != null) {
                            val batch = state.batches.find { it.id == state.selectedBatchId }
                            Text(
                                "${batch?.name ?: ""} • ${state.selectedDate}",
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
                actions = {
                    TextButton(onClick = onOpenReports) {
                        Text("Reports", fontWeight = FontWeight.SemiBold, color = PrimaryBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
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
                        // Stats row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            val present = state.attendanceMap.values.count { it == AttendanceStatus.PRESENT }
                            val absent = state.attendanceMap.values.count { it == AttendanceStatus.ABSENT }
                            val unmarked = state.students.size - state.attendanceMap.size
                            StatusChip("Present: $present", SecondaryGreen)
                            StatusChip("Absent: $absent", DangerRed)
                            if (unmarked > 0) StatusChip("Unmarked: $unmarked", TextMuted)
                        }
                        Button(
                            onClick = { viewModel.saveAttendance(currentUserId) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !state.isSaving && !isReadOnly,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            if (state.isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = TextOnPrimary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(Modifier.width(8.dp))
                            }
                            Text(
                                if (isReadOnly) "Read-only (Student Mode)" else if (state.isSaving) "Saving..." else "Save Attendance",
                                fontWeight = FontWeight.SemiBold
                            )
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
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // Filters
            item {
                Spacer(Modifier.height(8.dp))
                Text("Select Batch & Subject", style = MaterialTheme.typography.labelMedium, color = PrimaryBlue)
                Spacer(Modifier.height(8.dp))
            }

            // Batch selector
            item {
                ExposedDropdownMenuBox(
                    expanded = expandedBatch,
                    onExpandedChange = { expandedBatch = it }
                ) {
                    OutlinedTextField(
                        value = state.batches.find { it.id == state.selectedBatchId }?.name ?: "Select Batch",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedBatch) },
                        shape = RoundedCornerShape(12.dp),
                        label = { Text(stringResource(R.string.batch)) }
                    )
                    ExposedDropdownMenu(
                        expanded = expandedBatch,
                        onDismissRequest = { expandedBatch = false }
                    ) {
                        state.batches.forEach { batch ->
                            DropdownMenuItem(
                                text = { Text(batch.name) },
                                onClick = {
                                    viewModel.selectBatch(batch.id)
                                    expandedBatch = false
                                }
                            )
                        }
                    }
                }
            }

            // Subject selector
            if (state.selectedBatchId != null) {
                item {
                    ExposedDropdownMenuBox(
                        expanded = expandedSubject,
                        onExpandedChange = { expandedSubject = it }
                    ) {
                        OutlinedTextField(
                            value = state.subjects.find { it.id == state.selectedSubjectId }?.name ?: "Select Subject",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSubject) },
                            shape = RoundedCornerShape(12.dp),
                            label = { Text(stringResource(R.string.subject)) }
                        )
                        ExposedDropdownMenu(
                            expanded = expandedSubject,
                            onDismissRequest = { expandedSubject = false }
                        ) {
                            state.subjects.forEach { subject ->
                                DropdownMenuItem(
                                    text = { Text(subject.name) },
                                    onClick = {
                                        viewModel.selectSubject(subject.id)
                                        expandedSubject = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Date
            if (state.selectedSubjectId != null) {
                item {
                    OutlinedTextField(
                        value = state.selectedDate,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.date)) },
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Outlined.CalendarToday, "Pick Date")
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Mark All shortcuts
            if (state.students.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AssistChip(
                            onClick = {
                                state.students.forEach { viewModel.markAttendance(it.id, AttendanceStatus.PRESENT) }
                            },
                            enabled = !isReadOnly,
                            label = { Text(stringResource(R.string.str_all_present)) },
                            leadingIcon = { Icon(Icons.Filled.CheckCircle, null, Modifier.size(16.dp)) },
                            colors = AssistChipDefaults.assistChipColors(
                                labelColor = SecondaryGreen,
                                leadingIconContentColor = SecondaryGreen
                            )
                        )
                        AssistChip(
                            onClick = {
                                state.students.forEach { viewModel.markAttendance(it.id, AttendanceStatus.ABSENT) }
                            },
                            enabled = !isReadOnly,
                            label = { Text(stringResource(R.string.str_all_absent)) },
                            leadingIcon = { Icon(Icons.Filled.Cancel, null, Modifier.size(16.dp)) },
                            colors = AssistChipDefaults.assistChipColors(
                                labelColor = DangerRed,
                                leadingIconContentColor = DangerRed
                            )
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "${state.students.size} students",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextMuted
                    )
                }
            }

            // Student list
            items(state.students, key = { it.id }) { student ->
                StudentAttendanceCard(
                    name = student.name,
                    rollNumber = student.rollNumber,
                    currentStatus = state.attendanceMap[student.id]?.name ?: "",
                    remarks = state.remarksMap[student.id] ?: "",
                    onStatusChange = { statusStr ->
                        viewModel.markAttendance(student.id, AttendanceStatus.valueOf(statusStr))
                    },
                    onRemarksChange = { viewModel.setRemarks(student.id, it) },
                    readOnly = isReadOnly
                )
            }

            if (state.students.isEmpty() && state.selectedSubjectId != null) {
                item {
                    EmptyState(
                        icon = Icons.Outlined.Groups,
                        title = "No students found",
                        subtitle = "No students are enrolled in this batch"
                    )
                }
            }
        }
    }

    // Simple date picker dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate()
                            .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                        viewModel.selectDate(date)
                    }
                    showDatePicker = false
                }) { Text(stringResource(R.string.str_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.str_cancel)) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
