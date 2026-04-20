package com.ims.app.ui.academy

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ims.app.data.entity.Batch
import com.ims.app.ui.theme.PrimaryBlue
import com.ims.app.ui.theme.TextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchTransfersScreen(
    batchId: Long,
    viewModel: AcademyViewModel,
    onBack: () -> Unit
) {
    val students by viewModel.getStudentsForBatch(batchId).collectAsState(initial = emptyList())
    val allBatches by viewModel.getAllBatches().collectAsState(initial = emptyList())
    val destinationBatches = allBatches.filter { it.id != batchId }

    var selectedStudentIds by remember { mutableStateOf(setOf<Long>()) }
    var selectedDestinationBatch by remember { mutableStateOf<Batch?>(null) }
    var expandedDestination by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Transfer Students", fontWeight = FontWeight.SemiBold) },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = expandedDestination,
                onExpandedChange = { expandedDestination = it }
            ) {
                OutlinedTextField(
                    value = selectedDestinationBatch?.name ?: "Select Destination Batch",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDestination) },
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = expandedDestination,
                    onDismissRequest = { expandedDestination = false }
                ) {
                    destinationBatches.forEach { batch ->
                        DropdownMenuItem(
                            text = { Text(batch.name) },
                            onClick = {
                                selectedDestinationBatch = batch
                                expandedDestination = false
                            }
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = selectedStudentIds.size == students.size && students.isNotEmpty(),
                    onCheckedChange = { checked ->
                        selectedStudentIds = if (checked) students.map { it.id }.toSet() else emptySet()
                    }
                )
                Text("Select All", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.weight(1f))
                Text("${selectedStudentIds.size} Selected", style = MaterialTheme.typography.bodySmall, color = PrimaryBlue)
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(students) { student ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedStudentIds.contains(student.id),
                            onCheckedChange = { checked ->
                                selectedStudentIds = if (checked) {
                                    selectedStudentIds + student.id
                                } else {
                                    selectedStudentIds - student.id
                                }
                            }
                        )
                        Column {
                            Text(student.name, fontWeight = FontWeight.Medium)
                            Text(student.rollNumber, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        }
                    }
                }
            }

            Button(
                onClick = {
                    if (selectedDestinationBatch != null && selectedStudentIds.isNotEmpty()) {
                        viewModel.transferStudents(selectedStudentIds, selectedDestinationBatch!!.id)
                        selectedStudentIds = emptySet()
                        // Give some visual feedback that they disappeared (which they will, reactively)
                    }
                },
                enabled = selectedDestinationBatch != null && selectedStudentIds.isNotEmpty(),
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Icon(Icons.Filled.SwapHoriz, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Transfer ${selectedStudentIds.size} Students")
            }
        }
    }
}
