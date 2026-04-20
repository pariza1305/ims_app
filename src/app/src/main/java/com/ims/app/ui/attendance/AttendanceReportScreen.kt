package com.ims.app.ui.attendance

import com.ims.app.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ims.app.data.entity.UserRole
import com.ims.app.data.entity.AttendanceStatus
import com.ims.app.ui.components.*
import com.ims.app.ui.theme.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceReportScreen(
    viewModel: AttendanceViewModel,
    role: UserRole,
    studentId: Long?,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var expandedBatch by remember { mutableStateOf(false) }
    var selectedBatchForReport by remember { mutableStateOf<Long?>(null) }
    var selectedDateForReport by remember { mutableStateOf(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))) }
    var showDatePicker by remember { mutableStateOf(false) }
    var expandedSubject by remember { mutableStateOf(false) }
    var selectedSubjectForReport by remember { mutableStateOf<Long?>(null) }
    val isStudent = role == UserRole.STUDENT && studentId != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Attendance Reports", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Tab selector
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = PrimaryBlue
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Daily", fontWeight = if (selectedTab == 0) FontWeight.SemiBold else FontWeight.Normal) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Monthly", fontWeight = if (selectedTab == 1) FontWeight.SemiBold else FontWeight.Normal) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Subject-wise", fontWeight = if (selectedTab == 2) FontWeight.SemiBold else FontWeight.Normal) }
                )
            }

            Spacer(Modifier.height(16.dp))

            // Batch selector
            if (!isStudent) {
                ExposedDropdownMenuBox(
                    expanded = expandedBatch,
                    onExpandedChange = { expandedBatch = it }
                ) {
                    OutlinedTextField(
                        value = state.batches.find { it.id == selectedBatchForReport }?.name ?: "Select Batch",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedBatch) },
                        shape = RoundedCornerShape(12.dp),
                        label = { Text(stringResource(R.string.batch)) }
                    )
                    ExposedDropdownMenu(expanded = expandedBatch, onDismissRequest = { expandedBatch = false }) {
                        state.batches.forEach { batch ->
                            DropdownMenuItem(
                                text = { Text(batch.name) },
                                onClick = {
                                    selectedBatchForReport = batch.id
                                    expandedBatch = false
                                    when (selectedTab) {
                                        0 -> viewModel.loadDailyReport(selectedDateForReport, batch.id)
                                        1 -> viewModel.loadMonthlyReport(batch.id, state.reportMonth, state.reportYear)
                                    2 -> {
                                        selectedSubjectForReport?.let { subjectId ->
                                            viewModel.loadSubjectWiseReport(batch.id, subjectId, state.reportMonth, state.reportYear)
                                        }
                                    }
                                    }
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            } else {
                Text(
                    text = "Student Mode: Showing your own records only",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
                Spacer(Modifier.height(12.dp))
            }

            when (selectedTab) {
                0 -> DailyReportContent(
                    state = state,
                    selectedDate = selectedDateForReport,
                    onDateChange = { date ->
                        selectedDateForReport = date
                        if (isStudent) {
                            studentId?.let { viewModel.loadStudentDailyReport(it, date) }
                        } else {
                            selectedBatchForReport?.let { viewModel.loadDailyReport(date, it) }
                        }
                    },
                    onShowDatePicker = { showDatePicker = true }
                )
                1 -> MonthlyReportContent(
                    state = state,
                    selectedBatchId = selectedBatchForReport,
                    onMonthChange = { month, year ->
                        if (isStudent) {
                            studentId?.let { viewModel.loadStudentMonthlyReport(it, month, year) }
                        } else {
                            selectedBatchForReport?.let { viewModel.loadMonthlyReport(it, month, year) }
                        }
                    }
                )
                2 -> SubjectWiseReportContent(
                    state = state,
                    selectedBatchId = selectedBatchForReport,
                    selectedSubjectId = selectedSubjectForReport,
                    expandedSubject = expandedSubject,
                    onExpandedSubject = { expandedSubject = it },
                    onSubjectSelected = { subjectId, month, year ->
                        selectedSubjectForReport = subjectId
                        selectedBatchForReport?.let { batchId ->
                            viewModel.loadSubjectWiseReport(batchId, subjectId, month, year)
                        }
                    },
                    onMonthChange = { month, year ->
                        val batchId = selectedBatchForReport
                        val subjectId = selectedSubjectForReport
                        if (batchId != null && subjectId != null) {
                            viewModel.loadSubjectWiseReport(batchId, subjectId, month, year)
                        }
                    }
                )
            }
        }
    }

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
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                        selectedDateForReport = date
                        if (isStudent) {
                            studentId?.let { viewModel.loadStudentDailyReport(it, date) }
                        } else {
                            selectedBatchForReport?.let { viewModel.loadDailyReport(date, it) }
                        }
                    }
                    showDatePicker = false
                }) { Text(stringResource(R.string.str_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.str_cancel)) }
            }
        ) { DatePicker(state = datePickerState) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubjectWiseReportContent(
    state: AttendanceState,
    selectedBatchId: Long?,
    selectedSubjectId: Long?,
    expandedSubject: Boolean,
    onExpandedSubject: (Boolean) -> Unit,
    onSubjectSelected: (Long, Int, Int) -> Unit,
    onMonthChange: (Int, Int) -> Unit
) {
    var currentMonth by remember { mutableIntStateOf(state.reportMonth) }
    var currentYear by remember { mutableIntStateOf(state.reportYear) }
    val subjects = state.subjects.filter { selectedBatchId == null || it.batchId == selectedBatchId }

    Column {
        ExposedDropdownMenuBox(
            expanded = expandedSubject,
            onExpandedChange = onExpandedSubject
        ) {
            OutlinedTextField(
                value = subjects.find { it.id == selectedSubjectId }?.name ?: "Select Subject",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                label = { Text(stringResource(R.string.subject)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSubject) },
                shape = RoundedCornerShape(12.dp)
            )
            ExposedDropdownMenu(expanded = expandedSubject, onDismissRequest = { onExpandedSubject(false) }) {
                subjects.forEach { subject ->
                    DropdownMenuItem(
                        text = { Text(subject.name) },
                        onClick = {
                            onSubjectSelected(subject.id, currentMonth, currentYear)
                            onExpandedSubject(false)
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                if (currentMonth == 1) { currentMonth = 12; currentYear-- } else currentMonth--
                onMonthChange(currentMonth, currentYear)
            }) { Icon(Icons.Filled.ChevronLeft, "Previous") }
            Text("${java.time.Month.of(currentMonth)} $currentYear", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            IconButton(onClick = {
                if (currentMonth == 12) { currentMonth = 1; currentYear++ } else currentMonth++
                onMonthChange(currentMonth, currentYear)
            }) { Icon(Icons.Filled.ChevronRight, "Next") }
        }

        Spacer(Modifier.height(12.dp))
        if (selectedBatchId == null) {
            Text(
                text = "Select a batch first to view subject-wise reports.",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
            Spacer(Modifier.height(8.dp))
        } else if (selectedSubjectId == null) {
            Text(
                text = "Select a subject to load the report.",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
            Spacer(Modifier.height(8.dp))
        }
        MonthlyReportContent(state = state, selectedBatchId = selectedBatchId, onMonthChange = onMonthChange)
    }
}

@Composable
private fun DailyReportContent(
    state: AttendanceState,
    selectedDate: String,
    onDateChange: (String) -> Unit,
    onShowDatePicker: () -> Unit
) {
    Column {
        OutlinedTextField(
            value = selectedDate,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.str_report_date)) },
            trailingIcon = {
                IconButton(onClick = onShowDatePicker) {
                    Icon(Icons.Outlined.CalendarToday, "Calendar")
                }
            },
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.height(16.dp))

        if (state.reportRecords.isNotEmpty()) {
            // Summary stats
            val present = state.reportRecords.count { it.status == AttendanceStatus.PRESENT }
            val absent = state.reportRecords.count { it.status == AttendanceStatus.ABSENT }
            val total = state.reportRecords.size
            val percent = if (total > 0) (present * 100 / total) else 0

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = "Present",
                    value = "$present",
                    icon = Icons.Outlined.CheckCircle,
                    iconTint = SecondaryGreen,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Absent",
                    value = "$absent",
                    icon = Icons.Outlined.Cancel,
                    iconTint = DangerRed,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "Attendance Rate: $percent%",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (percent >= 75) SecondaryGreen else DangerRed
            )
            Spacer(Modifier.height(16.dp))
        }

        // Student-wise list
        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(state.reportStudents) { student ->
                val record = state.reportRecords.find { it.studentId == student.id }
                val status = record?.status
                val statusColor = when (status) {
                    AttendanceStatus.PRESENT -> SecondaryGreen
                    AttendanceStatus.ABSENT -> DangerRed
                    else -> TextMuted
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(10.dp),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(PrimaryBlue.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                student.name.first().uppercase(),
                                style = MaterialTheme.typography.titleSmall,
                                color = PrimaryBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(student.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text("Roll: ${student.rollNumber}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        }
                        StatusChip(
                            text = status?.name ?: "N/A",
                            color = statusColor
                        )
                    }
                    if (record?.remarks?.isNotEmpty() == true) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp)
                                .padding(bottom = 12.dp),
                            color = PrimaryBlue.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Outlined.Info,
                                    null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    record.remarks,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMain
                                )
                            }
                        }
                    }
                }
            }

            if (state.reportStudents.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Outlined.Assessment,
                        title = "No data",
                        subtitle = "Select a batch to view attendance report"
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthlyReportContent(
    state: AttendanceState,
    selectedBatchId: Long?,
    onMonthChange: (Int, Int) -> Unit
) {
    var currentMonth by remember { mutableIntStateOf(state.reportMonth) }
    var currentYear by remember { mutableIntStateOf(state.reportYear) }

    Column {
        // Month navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                if (currentMonth == 1) { currentMonth = 12; currentYear-- } else currentMonth--
                onMonthChange(currentMonth, currentYear)
            }) {
                Icon(Icons.Filled.ChevronLeft, "Previous")
            }
            Text(
                "${java.time.Month.of(currentMonth).getDisplayName(TextStyle.FULL, Locale.getDefault())} $currentYear",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            IconButton(onClick = {
                if (currentMonth == 12) { currentMonth = 1; currentYear++ } else currentMonth++
                onMonthChange(currentMonth, currentYear)
            }) {
                Icon(Icons.Filled.ChevronRight, "Next")
            }
        }

        Spacer(Modifier.height(12.dp))

        // Monthly summary per student
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.reportStudents) { student ->
                val studentRecords = state.reportRecords.filter { it.studentId == student.id }
                val present = studentRecords.count { it.status == AttendanceStatus.PRESENT }
                val total = studentRecords.size
                val percent = if (total > 0) (present * 100 / total) else 0

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(student.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text("Roll: ${student.rollNumber}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            }
                            Text(
                                "$percent%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    percent >= 90 -> SecondaryGreen
                                    percent >= 75 -> WarningAmber
                                    else -> DangerRed
                                }
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { percent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = when {
                                percent >= 90 -> SecondaryGreen
                                percent >= 75 -> WarningAmber
                                else -> DangerRed
                            },
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "$present present out of $total classes",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }
            }

            if (state.reportStudents.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Outlined.Assessment,
                        title = "No data",
                        subtitle = "Select a batch to view monthly report"
                    )
                }
            }
        }
    }
}
