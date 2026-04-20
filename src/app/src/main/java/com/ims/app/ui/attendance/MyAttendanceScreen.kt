package com.ims.app.ui.attendance

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ims.app.ui.components.EmptyState
import com.ims.app.ui.theme.PrimaryBlue
import com.ims.app.ui.theme.SecondaryGreen
import com.ims.app.ui.theme.DangerRed
import com.ims.app.ui.theme.TextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAttendanceScreen(
    viewModel: AttendanceViewModel,
    studentId: Long,
    onBack: () -> Unit,
    onOpenReport: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(studentId, state.filterAcadYear, state.filterSemester) {
        viewModel.loadStudentAttendanceSummary(studentId)
    }

    val acadYears = listOf("2024-25", "2025-26")
    val semesters = listOf("Fall", "Spring")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Attendance", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onOpenReport) {
                        Icon(Icons.Outlined.Assignment, contentDescription = "Detailed Report")
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
        ) {
            // Attendance Filter Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Attendance Filter",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue.copy(alpha = 0.8f),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    FilterRow(
                        label = "Acad Year :",
                        selectedOption = state.filterAcadYear,
                        options = acadYears,
                        onOptionSelected = { viewModel.setStudentFilters(it, state.filterSemester, studentId) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    FilterRow(
                        label = "Semester :",
                        selectedOption = state.filterSemester,
                        options = semesters,
                        onOptionSelected = { viewModel.setStudentFilters(state.filterAcadYear, it, studentId) }
                    )
                }
            }

            // Semester Badge
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 16.dp)
                    .background(PrimaryBlue.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.CalendarToday,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "${state.filterSemester} ${state.filterAcadYear}",
                        color = PrimaryBlue,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }

            // Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Course Name", modifier = Modifier.weight(2f), fontWeight = FontWeight.Bold, color = TextMuted, fontSize = 13.sp)
                Text("Total", modifier = Modifier.weight(0.7f), fontWeight = FontWeight.Bold, color = TextMuted, fontSize = 13.sp, textAlign = TextAlign.Center)
                Text("Pres...", modifier = Modifier.weight(0.7f), fontWeight = FontWeight.Bold, color = TextMuted, fontSize = 13.sp, textAlign = TextAlign.Center)
                Text("Abs...", modifier = Modifier.weight(0.7f), fontWeight = FontWeight.Bold, color = TextMuted, fontSize = 13.sp, textAlign = TextAlign.Center)
            }

            Divider(color = MaterialTheme.colorScheme.outlineVariant)

            // Attendance List
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            } else if (state.studentSummary.isEmpty()) {
                EmptyState(
                    icon = Icons.Outlined.EventBusy,
                    title = "No classes found",
                    subtitle = "No attendance records for the selected period"
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(state.studentSummary) { item ->
                        AttendanceSummaryRow(item)
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterRow(
    label: String,
    selectedOption: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyLarge
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.weight(1.5f)
        ) {
            OutlinedTextField(
                value = selectedOption,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AttendanceSummaryRow(item: StudentAttendanceSummary) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            item.subjectName,
            modifier = Modifier.weight(2f),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            maxLines = 2
        )
        Text(
            "${item.totalClasses}",
            modifier = Modifier.weight(0.7f),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp
        )
        Text(
            "${item.presentCount}",
            modifier = Modifier.weight(0.7f),
            textAlign = TextAlign.Center,
            color = SecondaryGreen,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
        Text(
            "${item.absentCount}",
            modifier = Modifier.weight(0.7f),
            textAlign = TextAlign.Center,
            color = DangerRed,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
    }
}
