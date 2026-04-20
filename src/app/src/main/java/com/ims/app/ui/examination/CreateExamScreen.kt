package com.ims.app.ui.examination

import com.ims.app.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ims.app.data.entity.EvaluationMethod
import com.ims.app.data.entity.ExamStatus
import com.ims.app.data.entity.ExamType
import com.ims.app.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateExamScreen(
    viewModel: ExamViewModel,
    isReadOnly: Boolean,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var name by remember { mutableStateOf("") }
    var selectedBatchId by remember { mutableStateOf<Long?>(null) }
    var selectedSubjectId by remember { mutableStateOf<Long?>(null) }
    var date by remember { mutableStateOf(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))) }
    var startTime by remember { mutableStateOf("10:00 AM") }
    var endTime by remember { mutableStateOf("01:00 PM") }
    var location by remember { mutableStateOf("Main Hall") }
    var totalMarks by remember { mutableStateOf("100") }
    var selectedType by remember { mutableStateOf(ExamType.MARKS) }
    val context = androidx.compose.ui.platform.LocalContext.current
    var selectedEvaluationMethod by remember { mutableStateOf(com.ims.app.data.AppSettingsManager.getSavedGradingSystem(context)) }
    var selectedStatus by remember { mutableStateOf(ExamStatus.DRAFT) }
    var examGroupName by remember { mutableStateOf("") }
    var selectedGroupedSubjectIds by remember { mutableStateOf(setOf<Long>()) }
    var expandedBatch by remember { mutableStateOf(false) }
    var expandedSubject by remember { mutableStateOf(false) }
    var expandedType by remember { mutableStateOf(false) }
    var expandedStatus by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var nameError by remember { mutableStateOf(false) }
    var marksError by remember { mutableStateOf(false) }
    var customOptions by remember { mutableStateOf("Satisfactory, Unsatisfactory") }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            snackbarHostState.showSnackbar("Exam created successfully ✓")
            viewModel.resetSaved()
            onBack()
        }
    }

    val batchSubjects = if (selectedBatchId != null) {
        state.subjects.filter { it.batchId == selectedBatchId }
    } else state.subjects

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Create Exam", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // Exam Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; nameError = false },
                enabled = !isReadOnly,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.str_exam_name)) },
                placeholder = { Text(stringResource(R.string.str_exam_name)) },
                isError = nameError,
                supportingText = if (nameError) {{ Text(stringResource(R.string.str_name_is_required)) }} else null,
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Batch
            ExposedDropdownMenuBox(expanded = expandedBatch, onExpandedChange = { expandedBatch = it }) {
                OutlinedTextField(
                    value = state.batches.find { it.id == selectedBatchId }?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    enabled = !isReadOnly,
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    label = { Text(stringResource(R.string.batch)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedBatch) },
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(expanded = expandedBatch, onDismissRequest = { expandedBatch = false }) {
                    state.batches.forEach { batch ->
                        DropdownMenuItem(
                            text = { Text(batch.name) },
                            onClick = { selectedBatchId = batch.id; expandedBatch = false; selectedSubjectId = null }
                        )
                    }
                }
            }

            // Subject
            ExposedDropdownMenuBox(expanded = expandedSubject, onExpandedChange = { expandedSubject = it }) {
                OutlinedTextField(
                    value = batchSubjects.find { it.id == selectedSubjectId }?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    enabled = !isReadOnly,
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    label = { Text(stringResource(R.string.subject)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSubject) },
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(expanded = expandedSubject, onDismissRequest = { expandedSubject = false }) {
                    batchSubjects.forEach { subject ->
                        DropdownMenuItem(
                            text = { Text(subject.name) },
                            onClick = { selectedSubjectId = subject.id; expandedSubject = false }
                        )
                    }
                }
            }

            // Date
            OutlinedTextField(
                value = date,
                onValueChange = {},
                readOnly = true,
                enabled = !isReadOnly,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.str_exam_date)) },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Outlined.CalendarToday, "Pick Date")
                    }
                },
                shape = RoundedCornerShape(12.dp)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = startTime,
                    onValueChange = { startTime = it },
                    enabled = !isReadOnly,
                    modifier = Modifier.weight(1f),
                    label = { Text("Start Time") },
                    placeholder = { Text("10:00 AM") },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = endTime,
                    onValueChange = { endTime = it },
                    enabled = !isReadOnly,
                    modifier = Modifier.weight(1f),
                    label = { Text("End Time") },
                    placeholder = { Text("01:00 PM") },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                enabled = !isReadOnly,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Location") },
                placeholder = { Text("e.g. Hall A or Online") },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )


            // Total Marks (Conditional)
            if (selectedType == ExamType.MARKS) {
                OutlinedTextField(
                    value = totalMarks,
                    onValueChange = { totalMarks = it; marksError = false },
                    enabled = !isReadOnly,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.str_total_marks)) },
                    isError = marksError,
                    supportingText = if (marksError) {{ Text(stringResource(R.string.str_enter_a_valid_number)) }} else null,
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )
            }

            // Exam Type
            ExposedDropdownMenuBox(expanded = expandedType, onExpandedChange = { expandedType = it }) {
                OutlinedTextField(
                    value = selectedType.name,
                    onValueChange = {},
                    readOnly = true,
                    enabled = !isReadOnly,
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    label = { Text(stringResource(R.string.str_exam_type)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(expanded = expandedType, onDismissRequest = { expandedType = false }) {
                    ExamType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.name) },
                            onClick = { selectedType = type; expandedType = false }
                        )
                    }
                }
            }

            // Custom Options (Conditional)
            if (selectedType == ExamType.CUSTOM) {
                OutlinedTextField(
                    value = customOptions,
                    onValueChange = { customOptions = it },
                    enabled = !isReadOnly,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Custom Evaluation Options") },
                    placeholder = { Text("e.g. Pass, Fail") },
                    supportingText = { Text("Comma separated values for results dropdown") },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            Text("Grouping & Batch Creation", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = PrimaryBlue)

            OutlinedTextField(
                value = examGroupName,
                onValueChange = { examGroupName = it },
                enabled = !isReadOnly,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Batch Group Name (Optional)") },
                placeholder = { Text("e.g. Mid-Term 2024") },
                supportingText = { Text(stringResource(R.string.str_exam_group_name)) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            if (examGroupName.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = PrimaryBlue.copy(alpha = 0.05f))
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Select Subjects for this Group:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        batchSubjects.forEach { subject ->
                            Row(
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().clickable(enabled = !isReadOnly) {
                                    selectedGroupedSubjectIds = if (selectedGroupedSubjectIds.contains(subject.id)) {
                                        selectedGroupedSubjectIds - subject.id
                                    } else {
                                        selectedGroupedSubjectIds + subject.id
                                    }
                                }
                            ) {
                                Checkbox(
                                    checked = selectedGroupedSubjectIds.contains(subject.id),
                                    enabled = !isReadOnly,
                                    onCheckedChange = null
                                )
                                Text(subject.name, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }

            OutlinedTextField(
                value = selectedEvaluationMethod.name,
                onValueChange = {},
                readOnly = true,
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.str_evaluation_method) + " (Admin Controlled)") },
                shape = RoundedCornerShape(12.dp)
            )

            // Status
            ExposedDropdownMenuBox(expanded = expandedStatus, onExpandedChange = { expandedStatus = it }) {
                OutlinedTextField(
                    value = selectedStatus.name.replace("_", " "),
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    label = { Text(stringResource(R.string.str_status)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus) },
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(expanded = expandedStatus, onDismissRequest = { expandedStatus = false }) {
                    ExamStatus.entries.forEach { status ->
                        DropdownMenuItem(
                            text = { Text(status.name.replace("_", " ")) },
                            onClick = { selectedStatus = status; expandedStatus = false }
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Create button
            Button(
                onClick = {
                    nameError = name.isBlank()
                    marksError = selectedType == ExamType.MARKS && totalMarks.toIntOrNull() == null
                    if (!nameError && !marksError && selectedBatchId != null && selectedSubjectId != null && !isReadOnly) {
                        viewModel.createExam(
                            name = name,
                            subjectId = selectedSubjectId!!,
                            batchId = selectedBatchId!!,
                            groupName = examGroupName,
                            groupedSubjectIds = selectedGroupedSubjectIds.joinToString(","),
                            date = date,
                            startTime = startTime,
                            endTime = endTime,
                            location = location,
                            totalMarks = if (selectedType == ExamType.MARKS) totalMarks.toInt() else 0,
                            type = selectedType,
                            customOptions = if (selectedType == ExamType.CUSTOM) customOptions else "",
                            evaluationMethod = selectedEvaluationMethod,
                            status = selectedStatus
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                enabled = !state.isSaving && !isReadOnly
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = TextOnPrimary, strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                }
                Text(if (selectedGroupedSubjectIds.isNotEmpty()) "Create Batch (${selectedGroupedSubjectIds.size + 1})" else "Create Exam", fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        date = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate()
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    }
                    showDatePicker = false
                }) { Text(stringResource(R.string.str_ok)) }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.str_cancel)) } }
        ) { DatePicker(state = datePickerState) }
    }
}
