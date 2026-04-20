package com.ims.app.ui.examination

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ims.app.data.entity.ExamType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamSummaryReportScreen(
    viewModel: ExamViewModel,
    examId: Long,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(examId) {
        viewModel.loadResultsByExam(examId)
    }

    val exam = state.selectedExam
    val results = state.results

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Exam Summary Report", fontWeight = FontWeight.SemiBold) },
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
            contentPadding = PaddingValues(bottom = 20.dp, top = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Exam: ${exam?.name ?: "N/A"}", fontWeight = FontWeight.SemiBold)
                        Text("Evaluation Method: ${exam?.evaluationMethod ?: "N/A"}")
                        Text("Exam Type: ${exam?.type ?: "N/A"}")
                        Text("Exam Group: ${exam?.groupName?.ifBlank { "N/A" } ?: "N/A"}")
                        Text("Grouped Subjects (IDs): ${exam?.groupedSubjectIds?.ifBlank { "N/A" } ?: "N/A"}")
                    }
                }
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Statistical Summary", fontWeight = FontWeight.SemiBold)
                        if (exam?.type == ExamType.MARKS && results.isNotEmpty()) {
                            // MARKS exam: show numerical statistics
                            val average = results.map { it.marksObtained }.average()
                            val highest = results.maxOf { it.marksObtained }
                            val lowest = results.minOf { it.marksObtained }
                            val passCount = results.count { it.marksObtained >= exam.totalMarks * 0.4 }
                            val passPercentage = passCount * 100 / results.size
                            Text("Average Marks: ${"%.1f".format(average)}")
                            Text("Highest Score: ${"%.0f".format(highest)}")
                            Text("Lowest Score: ${"%.0f".format(lowest)}")
                            Text("Pass Percentage: $passPercentage%")
                        } else if (results.isNotEmpty()) {
                            // GRADE/CUSTOM exam: show grade frequency breakdown
                            Text("Total Results: ${results.size}")
                            val gradeFreq = results.groupingBy { it.grade }.eachCount()
                                .entries.sortedByDescending { it.value }
                            gradeFreq.forEach { (grade, count) ->
                                val pct = count * 100 / results.size
                                Text("  $grade: $count students ($pct%)")
                            }
                        } else {
                            Text("No results available")
                        }
                    }
                }
            }
            items(results) { result ->
                val student = state.students.find { it.id == result.studentId }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("${student?.name ?: "Student"} (${student?.rollNumber ?: "-"})", fontWeight = FontWeight.Medium)
                        if (exam?.type == ExamType.MARKS) {
                            Text("Score: ${"%.0f".format(result.marksObtained)}")
                        }
                        Text("Grade: ${result.grade}")
                    }
                }
            }
        }
    }
}
