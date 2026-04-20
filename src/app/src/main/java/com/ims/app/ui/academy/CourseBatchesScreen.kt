package com.ims.app.ui.academy

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Class
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ims.app.data.entity.Batch
import com.ims.app.data.entity.Course
import com.ims.app.ui.theme.PrimaryBlue
import com.ims.app.ui.theme.TextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseBatchesScreen(
    courseId: Long,
    viewModel: AcademyViewModel,
    onBack: () -> Unit,
    onBatchClick: (Long) -> Unit
) {
    var course by remember { mutableStateOf<Course?>(null) }
    LaunchedEffect(courseId) {
        course = viewModel.getCourseById(courseId)
    }
    
    val batches by viewModel.getBatchesForCourse(courseId).collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(course?.name ?: "Course Batches", fontWeight = FontWeight.SemiBold) },
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "Select a Batch",
                    style = MaterialTheme.typography.labelMedium,
                    color = PrimaryBlue,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(batches) { batch ->
                BatchCard(batch = batch, onClick = { onBatchClick(batch.id) })
            }
        }
    }
}

@Composable
fun BatchCard(batch: Batch, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.Class, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(batch.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                Text("Year: ${batch.year}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = "Manage Batch", tint = TextMuted)
        }
    }
}
