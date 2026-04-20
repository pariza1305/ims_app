package com.ims.app.ui.academy

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.outlined.LibraryBooks
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ims.app.data.entity.Batch
import com.ims.app.data.entity.UserRole
import com.ims.app.ui.theme.PrimaryBlue
import com.ims.app.ui.theme.TextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchDashboardScreen(
    batchId: Long,
    viewModel: AcademyViewModel,
    role: UserRole,
    onBack: () -> Unit,
    onTransferStudents: () -> Unit
) {
    var batch by remember { mutableStateOf<Batch?>(null) }
    LaunchedEffect(batchId) {
        batch = viewModel.getBatchById(batchId)
    }

    val subjects by viewModel.getSubjectsForBatch(batchId).collectAsState(initial = emptyList())
    val students by viewModel.getStudentsForBatch(batchId).collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(batch?.name ?: "Batch Details", fontWeight = FontWeight.SemiBold) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = PrimaryBlue.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Batch Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                        Spacer(Modifier.height(8.dp))
                        Text("Total Students: ${students.size}", style = MaterialTheme.typography.bodyMedium)
                        Text("Total Subjects: ${subjects.size}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            if (role == UserRole.ADMIN) {
                item {
                    Button(
                        onClick = onTransferStudents,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Filled.PeopleAlt, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Transfer Students")
                    }
                }
            }

            item {
                Text(
                    "Subjects",
                    style = MaterialTheme.typography.labelMedium,
                    color = PrimaryBlue,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(subjects) { subject ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.LibraryBooks, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(subject.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Elective", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            Spacer(Modifier.width(4.dp))
                            Switch(
                                checked = subject.isElective,
                                onCheckedChange = if (role == UserRole.ADMIN) { { viewModel.toggleElectiveStatus(subject) } } else null,
                                enabled = role == UserRole.ADMIN
                            )
                        }
                    }
                }
            }
        }
    }
}
