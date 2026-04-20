package com.ims.app.ui.notification

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ims.app.data.entity.UserRole
import com.ims.app.ui.theme.PrimaryBlue
import com.ims.app.ui.theme.SecondaryGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendNotificationScreen(
    viewModel: NotificationViewModel,
    role: UserRole,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    
    // Group Alert State
    var selectedBatchId by remember { mutableLongStateOf(0L) }
    var groupTitle by remember { mutableStateOf("") }
    var groupMessage by remember { mutableStateOf("") }
    
    // Single Alert State
    var searchQuery by remember { mutableStateOf("") }
    var selectedStudentId by remember { mutableLongStateOf(0L) }
    var selectedStudentName by remember { mutableStateOf("") }
    var singleTitle by remember { mutableStateOf("") }
    var singleMessage by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loadBatches()
    }

    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotEmpty() && searchQuery != selectedStudentName) {
            viewModel.searchStudents(searchQuery)
        }
    }

    LaunchedEffect(state.sendStatus) {
        state.sendStatus?.let { status ->
            snackbarHostState.showSnackbar(status)
            if (status == "Success") {
                if (selectedTab == 0) {
                    groupTitle = ""
                    groupMessage = ""
                    selectedBatchId = 0L
                } else {
                    singleTitle = ""
                    singleMessage = ""
                    searchQuery = ""
                    selectedStudentId = 0L
                    selectedStudentName = ""
                }
            }
            viewModel.clearStatus()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Compose Alert", fontWeight = FontWeight.SemiBold) },
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
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = PrimaryBlue,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = PrimaryBlue
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Group, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Group Alert")
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Single Alert")
                        }
                    }
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (selectedTab == 0) {
                    // Group Alert UI
                    item {
                        Text("Send message to an entire batch", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    item {
                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = state.batches.find { it.id == selectedBatchId }?.name ?: "Select Batch",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Target Batch") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                state.batches.forEach { batch ->
                                    DropdownMenuItem(
                                        text = { Text(batch.name) },
                                        onClick = {
                                            selectedBatchId = batch.id
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = groupTitle,
                            onValueChange = { groupTitle = it },
                            label = { Text("Title") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = groupMessage,
                            onValueChange = { groupMessage = it },
                            label = { Text("Message") },
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    item {
                        Button(
                            onClick = {
                                if (selectedBatchId != 0L && groupTitle.isNotBlank() && groupMessage.isNotBlank()) {
                                    viewModel.sendGroupNotification(selectedBatchId, groupTitle, groupMessage)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            enabled = !state.isSending
                        ) {
                            if (state.isSending) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                            } else {
                                Text("Send Group Alert")
                            }
                        }
                    }
                } else {
                    // Single Alert UI
                    item {
                        Text("Send a message to a specific student", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    item {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("Search Student Name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = { if (selectedStudentId != 0L) Icon(Icons.Default.Check, null, tint = SecondaryGreen) }
                        )
                    }

                    if (searchQuery.isNotEmpty() && searchQuery != selectedStudentName) {
                        items(state.students) { student ->
                            Card(
                                onClick = {
                                    selectedStudentId = student.id
                                    selectedStudentName = student.name
                                    searchQuery = student.name
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Person, null, tint = PrimaryBlue)
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text(student.name, fontWeight = FontWeight.Bold)
                                        Text("Roll: ${student.rollNumber}", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = singleTitle,
                            onValueChange = { singleTitle = it },
                            label = { Text("Title") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = singleMessage,
                            onValueChange = { singleMessage = it },
                            label = { Text("Message") },
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    item {
                        Button(
                            onClick = {
                                if (selectedStudentId != 0L && singleTitle.isNotBlank() && singleMessage.isNotBlank()) {
                                    viewModel.sendSingleNotification(selectedStudentId, singleTitle, singleMessage)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            enabled = !state.isSending
                        ) {
                            if (state.isSending) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                            } else {
                                Text("Send Single Alert")
                            }
                        }
                    }
                }
            }
        }
    }
}
