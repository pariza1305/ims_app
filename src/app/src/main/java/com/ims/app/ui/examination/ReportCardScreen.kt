package com.ims.app.ui.examination

import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ims.app.data.entity.ExamType
import com.ims.app.ui.components.*
import com.ims.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportCardScreen(
    viewModel: ExamViewModel,
    studentId: Long,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(studentId) {
        viewModel.loadStudentResults(studentId)
    }

    val student = state.students.find { it.id == studentId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Report Card", fontWeight = FontWeight.SemiBold)
                        student?.let {
                            Text(
                                it.name,
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
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp, top = 8.dp)
        ) {
            // Student Info Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(TextOnPrimary.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                student?.name?.first()?.uppercase() ?: "?",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextOnPrimary
                            )
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                student?.name ?: "Student",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextOnPrimary
                            )
                            Text(
                                "Roll: ${student?.rollNumber ?: "-"}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextOnPrimary.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // Aggregate stats
            if (state.studentResults.isNotEmpty()) {
                item {
                    val totalExams = state.studentResults.size
                    // Only average marks from MARKS-type exams (GRADE-type has 0.0)
                    val marksResults = state.studentResults.filter { r ->
                        val ex = state.exams.find { it.id == r.examId }
                        ex?.type == ExamType.MARKS
                    }
                    val avgMarks = if (marksResults.isNotEmpty()) marksResults.map { it.marksObtained }.average() else null

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatCard(
                            title = "Total Exams",
                            value = "$totalExams",
                            icon = Icons.Outlined.Quiz,
                            iconTint = PrimaryBlue,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = if (avgMarks != null) "Avg Score" else "Grades Given",
                            value = if (avgMarks != null) String.format("%.1f", avgMarks) else "${state.studentResults.count { it.grade.isNotEmpty() }}",
                            icon = Icons.Outlined.Analytics,
                            iconTint = SecondaryGreen,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Computed GPA / CWA aggregates
                    Spacer(Modifier.height(8.dp))

                    // Compute GPA from GPA-method exams
                    val gpaResults = state.studentResults.filter { r ->
                        val ex = state.exams.find { it.id == r.examId }
                        ex?.evaluationMethod == com.ims.app.data.entity.EvaluationMethod.GPA && ex.type == ExamType.MARKS
                    }
                    val computedGpa = if (gpaResults.isNotEmpty()) {
                        val gradePoints = gpaResults.map { r ->
                            val ex = state.exams.find { it.id == r.examId }
                            val pct = if (ex != null && ex.totalMarks > 0) r.marksObtained / ex.totalMarks else 0.0
                            when {
                                pct >= 0.9 -> 4.0
                                pct >= 0.8 -> 3.7
                                pct >= 0.7 -> 3.3
                                pct >= 0.6 -> 3.0
                                pct >= 0.5 -> 2.5
                                pct >= 0.4 -> 2.0
                                else -> 0.0
                            }
                        }
                        gradePoints.average()
                    } else null

                    // Compute CWA from CWA-method exams
                    val cwaResults = state.studentResults.filter { r ->
                        val ex = state.exams.find { it.id == r.examId }
                        ex?.evaluationMethod == com.ims.app.data.entity.EvaluationMethod.CWA && ex.type == ExamType.MARKS
                    }
                    val computedCwa = if (cwaResults.isNotEmpty()) {
                        val percentages = cwaResults.map { r ->
                            val ex = state.exams.find { it.id == r.examId }
                            if (ex != null && ex.totalMarks > 0) (r.marksObtained / ex.totalMarks) * 100 else 0.0
                        }
                        percentages.average()
                    } else null

                    if (computedGpa != null || computedCwa != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (computedGpa != null) {
                                StatCard(
                                    title = "GPA",
                                    value = String.format("%.2f", computedGpa),
                                    subtitle = "out of 4.0",
                                    icon = Icons.Outlined.Star,
                                    iconTint = WarningAmber,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (computedCwa != null) {
                                StatCard(
                                    title = "CWA",
                                    value = String.format("%.1f%%", computedCwa),
                                    subtitle = "weighted avg",
                                    icon = Icons.Outlined.Percent,
                                    iconTint = PrimaryBlue,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            item {
                SectionHeader(title = "All Exam Results")
            }

            items(state.studentResults) { result ->
                val examForResult = state.exams.find { it.id == result.examId }
                val resultColor = gradeColor(result.grade)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(80.dp)
                                .background(resultColor, RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    examForResult?.name ?: "Exam",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f)
                                )
                                StatusChip(text = result.grade, color = resultColor)
                            }
                            Spacer(Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Date: ${examForResult?.date ?: "N/A"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                                if (examForResult?.type == ExamType.MARKS) {
                                    Text(
                                        "${String.format("%.0f", result.marksObtained)} / ${examForResult.totalMarks}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = resultColor
                                    )
                                } else {
                                    Text(
                                        "Grade: ${result.grade}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = resultColor
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (state.studentResults.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Outlined.Description,
                        title = "No results",
                        subtitle = "This student has no exam results yet"
                    )
                }
            }
        }
    }
}
