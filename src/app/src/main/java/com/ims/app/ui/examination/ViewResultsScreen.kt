package com.ims.app.ui.examination

import com.ims.app.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ims.app.data.entity.ExamType
import com.ims.app.data.entity.UserRole
import com.ims.app.ui.components.*
import com.ims.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewResultsScreen(
    viewModel: ExamViewModel,
    examId: Long,
    role: UserRole,
    studentId: Long?,
    onBack: () -> Unit,
    onSummaryReport: (Long) -> Unit,
    onStudentReportCard: (Long) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(examId) {
        viewModel.loadResultsByExam(examId)
    }

    val exam = state.selectedExam
    val visibleResults = if (role == UserRole.STUDENT && studentId != null) {
        state.results.filter { it.studentId == studentId }
    } else {
        state.results
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Exam Results", fontWeight = FontWeight.SemiBold)
                        exam?.let {
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
                actions = {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    IconButton(onClick = {
                        val csv = buildString {
                            if (exam?.type == ExamType.MARKS) {
                                appendLine("Student Name,Roll,Marks,Grade")
                            } else {
                                appendLine("Student Name,Roll,Grade")
                            }
                            state.students.forEach { s ->
                                val r = state.results.find { it.studentId == s.id }
                                if (exam?.type == ExamType.MARKS) {
                                    appendLine("${s.name},${s.rollNumber},${r?.marksObtained ?: 0.0},${r?.grade ?: ""}")
                                } else {
                                    appendLine("${s.name},${s.rollNumber},${r?.grade ?: ""}")
                                }
                            }
                        }
                        val resolver = context.contentResolver
                        val contentValues = android.content.ContentValues().apply {
                            put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, "Export_${exam?.name}.csv")
                            put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                            put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOWNLOADS)
                        }
                        val uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                        uri?.let {
                            resolver.openOutputStream(it)?.use { os ->
                                os.write(csv.toByteArray())
                            }
                            android.widget.Toast.makeText(context, "Exported to Downloads!", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(Icons.Outlined.FileDownload, "Export to CSV")
                    }
                    if (role != UserRole.STUDENT && exam?.status != com.ims.app.data.entity.ExamStatus.RESULTS_PUBLISHED && state.results.isNotEmpty()) {
                        TextButton(onClick = {
                            viewModel.publishResults(examId)
                        }) {
                            Text("Publish", color = PrimaryBlue, fontWeight = FontWeight.SemiBold)
                        }
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
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp, top = 8.dp)
        ) {
            // Summary stats + charts
            if (visibleResults.isNotEmpty() && exam != null) {

                // ── Summary Stats ──
                item {
                    if (exam.type == ExamType.MARKS) {
                        // MARKS exam: show numerical stats
                        val avg = visibleResults.map { it.marksObtained }.average()
                        val highest = visibleResults.maxOf { it.marksObtained }
                        val lowest = visibleResults.minOf { it.marksObtained }
                        val passCount = visibleResults.count { it.marksObtained >= (exam.totalMarks * 0.4) }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatCard(
                                title = "Average",
                                value = String.format("%.1f", avg),
                                icon = Icons.Outlined.Analytics,
                                iconTint = PrimaryBlue,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Highest",
                                value = String.format("%.0f", highest),
                                icon = Icons.Outlined.TrendingUp,
                                iconTint = SecondaryGreen,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatCard(
                                title = "Lowest",
                                value = String.format("%.0f", lowest),
                                icon = Icons.Outlined.TrendingDown,
                                iconTint = DangerRed,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Pass Rate",
                                value = "${passCount * 100 / visibleResults.size}%",
                                icon = Icons.Outlined.CheckCircle,
                                iconTint = SecondaryGreen,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else {
                        // GRADE/CUSTOM exam: show grade frequency stats
                        val gradeFreq = visibleResults.groupingBy { it.grade }.eachCount()
                        val mostFrequent = gradeFreq.maxByOrNull { it.value }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatCard(
                                title = "Total Results",
                                value = "${visibleResults.size}",
                                icon = Icons.Outlined.People,
                                iconTint = PrimaryBlue,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Unique Grades",
                                value = "${gradeFreq.size}",
                                icon = Icons.Outlined.Category,
                                iconTint = SecondaryGreen,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatCard(
                                title = "Most Frequent",
                                value = mostFrequent?.key ?: "N/A",
                                icon = Icons.Outlined.Star,
                                iconTint = WarningAmber,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Eval Method",
                                value = exam.evaluationMethod.name,
                                icon = Icons.Outlined.Rule,
                                iconTint = PrimaryBlue,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // ── Graphical View ──
                item {
                    SectionHeader(title = "Graphical View")
                    Spacer(Modifier.height(8.dp))

                    if (exam.type == ExamType.MARKS) {
                        // MARKS exam: Show marks-range histogram with dynamic buckets
                        val numBuckets = 5
                        val bucketSize = exam.totalMarks.toDouble() / numBuckets
                        val bucketLabels = (0 until numBuckets).map { i ->
                            val low = (i * bucketSize).toInt()
                            val high = if (i == numBuckets - 1) exam.totalMarks else ((i + 1) * bucketSize).toInt()
                            "$low-$high"
                        }
                        val bucketCounts = IntArray(numBuckets)
                        visibleResults.forEach { result ->
                            val bucketIdx = ((result.marksObtained / exam.totalMarks) * numBuckets).toInt()
                                .coerceIn(0, numBuckets - 1)
                            bucketCounts[bucketIdx]++
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    "Score Distribution",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    bucketLabels.forEachIndexed { idx, label ->
                                        val count = bucketCounts[idx]
                                        val percentage = if (visibleResults.isNotEmpty()) count * 100f / visibleResults.size else 0f
                                        val barHeight = (24 + (percentage * 1.2f)).dp
                                        val barColor = when (idx) {
                                            0 -> DangerRed
                                            1 -> WarningAmber
                                            2 -> WarningAmber
                                            3 -> PrimaryBlue
                                            else -> SecondaryGreen
                                        }
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                "${percentage.toInt()}%",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextMuted
                                            )
                                            Card(
                                                modifier = Modifier
                                                    .width(40.dp)
                                                    .height(barHeight),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (count > 0) barColor else MaterialTheme.colorScheme.surfaceVariant
                                                ),
                                                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
                                            ) {}
                                            Text(
                                                "$label\n($count)",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // GRADE/CUSTOM exam: Show grade distribution chart
                        val gradeLabels = visibleResults.map { it.grade }.distinct().sorted()
                        val validGrades = if (gradeLabels.isEmpty()) listOf("N/A") else gradeLabels
                        val countsMap = visibleResults.groupingBy { it.grade }.eachCount()

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    "Grade Distribution",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    validGrades.forEach { grade ->
                                        val count = countsMap[grade] ?: 0
                                        val percentage = if (visibleResults.isNotEmpty()) count * 100f / visibleResults.size else 0f
                                        val barHeight = (24 + (percentage * 1.2f)).dp
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                "${percentage.toInt()}%",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextMuted
                                            )
                                            Card(
                                                modifier = Modifier
                                                    .width(28.dp)
                                                    .height(barHeight),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (count > 0) gradeColor(grade) else MaterialTheme.colorScheme.surfaceVariant
                                                ),
                                                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
                                            ) {}
                                            Text(
                                                "$grade\n($count)",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { onSummaryReport(examId) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.str_generate_summary_report_view))
                    }

                    // Automated report card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Automated Report (Simulated)", fontWeight = FontWeight.SemiBold)
                            Text("Exam: ${exam.name}", style = MaterialTheme.typography.bodySmall)
                            Text("Type: ${exam.type} | Evaluation: ${exam.evaluationMethod}", style = MaterialTheme.typography.bodySmall)
                            if (exam.type == ExamType.MARKS) {
                                val avg = visibleResults.map { it.marksObtained }.average()
                                val highest = visibleResults.maxOf { it.marksObtained }
                                val lowest = visibleResults.minOf { it.marksObtained }
                                val passCount = visibleResults.count { it.marksObtained >= (exam.totalMarks * 0.4) }
                                Text("Average: ${String.format("%.1f", avg)} | Highest: ${String.format("%.0f", highest)} | Lowest: ${String.format("%.0f", lowest)}", style = MaterialTheme.typography.bodySmall)
                                Text("Pass Percentage: ${passCount * 100 / visibleResults.size}%", style = MaterialTheme.typography.bodySmall)
                            } else {
                                val gradeFreq = visibleResults.groupingBy { it.grade }.eachCount()
                                    .entries.sortedByDescending { it.value }
                                Text("Grade Distribution: ${gradeFreq.joinToString { "${it.key}: ${it.value}" }}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                item {
                    Spacer(Modifier.height(8.dp))
                    SectionHeader(title = "Student Results")
                }
            }

            // Results list sorted by marks descending (for MARKS) or by grade (for GRADE/CUSTOM)
            val sortedResults = if (exam?.type == ExamType.MARKS) {
                visibleResults.sortedByDescending { it.marksObtained }
            } else {
                visibleResults.sortedBy { it.grade }
            }
            items(sortedResults) { result ->
                val student = state.students.find { it.id == result.studentId }
                val resultColor = gradeColor(result.grade)
                val isPass = if (exam?.type == ExamType.MARKS) {
                    exam.let { result.marksObtained >= (it.totalMarks * 0.4) }
                } else {
                    // For grade-based, don't show pass/fail
                    true
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { student?.let { onStudentReportCard(it.id) } },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(72.dp)
                                .background(resultColor, RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                        )
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
                                    student?.name?.first()?.uppercase() ?: "?",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlue
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    student?.name ?: "Student",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    "Roll: ${student?.rollNumber ?: "-"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                if (exam?.type == ExamType.MARKS) {
                                    Text(
                                        "${String.format("%.0f", result.marksObtained)}/${exam.totalMarks}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = resultColor
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    StatusChip(text = result.grade, color = resultColor)
                                    if (exam?.type == ExamType.MARKS && !isPass) {
                                        Spacer(Modifier.width(4.dp))
                                        StatusChip(text = "FAIL", color = DangerRed)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (visibleResults.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Outlined.Assessment,
                        title = "No results yet",
                        subtitle = "Record marks first to see results"
                    )
                }
            }
        }
    }
}
