package com.ims.app.ui.academy

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ims.app.data.entity.*
import com.ims.app.ui.theme.PrimaryBlue
import com.ims.app.ui.theme.SecondaryGreen
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GraduationFacilitiesScreen(
    viewModel: AcademyViewModel,
    role: UserRole,
    studentId: Long?,
    onBack: () -> Unit
) {
    val graduationRecords by viewModel.getAllGraduationRecords().collectAsState(initial = emptyList())
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (role == UserRole.STUDENT) "My Graduation Status" else "Graduation Hub", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        if (role == UserRole.STUDENT) {
            val myRecord = graduationRecords.find { it.studentId == studentId }
            Box(Modifier.fillMaxSize().padding(padding)) {
                if (myRecord != null) {
                    PersonalGraduationView(myRecord)
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.School, null, modifier = Modifier.size(64.dp), tint = Color.Gray.copy(alpha = 0.3f))
                        Spacer(Modifier.height(16.dp))
                        Text("You are not yet on the graduation track.", color = Color.Gray)
                    }
                }
            }
        } else {
            Column(Modifier.fillMaxSize().padding(padding)) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = PrimaryBlue
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Clearance Tracking") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Graduation Registry") }
                    )
                }

                if (selectedTab == 0) {
                    ClearanceTab(viewModel, graduationRecords, role)
                } else {
                    AlumniTab(viewModel)
                }
            }
        }
    }
}

@Composable
fun PersonalGraduationView(record: GraduationRecord) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = PrimaryBlue.copy(alpha = 0.05f))
        ) {
            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.School, null, Modifier.size(48.dp), tint = PrimaryBlue)
                Spacer(Modifier.height(16.dp))
                Text(
                    "Class of ${record.graduationYear}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                ChipStatus(record.status)
            }
        }

        Text("Clearance Checklist", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        
        val steps = listOf(
            "Account Clearance" to (record.status != GraduationStatus.IN_PROGRESS),
            "Library Clearance" to (record.status != GraduationStatus.IN_PROGRESS),
            "Faculty Clearance" to (record.status != GraduationStatus.IN_PROGRESS),
            "Final Approval" to (record.status == GraduationStatus.GRADUATED)
        )

        steps.forEach { (label, isDone) ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (isDone) Icons.Default.FactCheck else Icons.Default.AssignmentTurnedIn,
                    null,
                    tint = if (isDone) SecondaryGreen else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(16.dp))
                Text(
                    label,
                    color = if (isDone) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.weight(1f))
                if (isDone) Icon(Icons.Default.CheckCircle, null, tint = SecondaryGreen, modifier = Modifier.size(20.dp))
            }
        }

        Spacer(Modifier.weight(1f))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Certificate Status", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        record.certificateStatus.name.replace("_", " "),
                        fontWeight = FontWeight.Bold,
                        color = when (record.certificateStatus) {
                            CertificateStatus.ISSUED -> SecondaryGreen
                            CertificateStatus.DELIVERED -> SecondaryGreen
                            CertificateStatus.PROCESSING -> PrimaryBlue
                            CertificateStatus.NOT_REQUESTED -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
                Icon(Icons.Default.AssignmentTurnedIn, null, tint = PrimaryBlue)
            }
        }
    }
}

@Composable
fun ClearanceTab(viewModel: AcademyViewModel, records: List<GraduationRecord>, role: UserRole) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PrimaryBlue.copy(alpha = 0.05f))
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Graduation Track 2024", fontWeight = FontWeight.Bold, color = PrimaryBlue)
                    Text("Manage clearance and certificate status for candidates.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        
        if (records.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("No students on graduation track yet", color = Color.Gray)
                }
            }
        }

        items(records) { record ->
            GraduationRecordItem(record, viewModel, role)
        }
    }
}

@Composable
fun GraduationRecordItem(record: GraduationRecord, viewModel: AcademyViewModel, role: UserRole) {
    // In a real app, we'd fetch the student details. 
    // For this UI, we'll just show the ID and status for now or assume student info is available.
    // Keeping it simple for the tracking UI.
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = PrimaryBlue.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.School, null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Student ID: ${record.studentId}", fontWeight = FontWeight.Bold)
                    Text("Class of ${record.graduationYear}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
                
                ChipStatus(record.status)
            }
            
            HorizontalDivider(Modifier.padding(vertical = 12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Clearance", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(if (record.status == GraduationStatus.CLEARED || record.status == GraduationStatus.GRADUATED) "CLEARED" else "PENDING", 
                        fontWeight = FontWeight.SemiBold, 
                        color = if (record.status == GraduationStatus.CLEARED || record.status == GraduationStatus.GRADUATED) SecondaryGreen else Color.Red)
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text("Certificate", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(record.certificateStatus.name.replace("_", " "), fontWeight = FontWeight.SemiBold, color = PrimaryBlue)
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { 
                        viewModel.updateGraduationStatus(record.copy(status = GraduationStatus.CLEARED)) 
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    enabled = record.status == GraduationStatus.IN_PROGRESS || record.status == GraduationStatus.PENDING_CLEARANCE
                ) {
                    Icon(Icons.Default.FactCheck, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Clear", style = MaterialTheme.typography.labelSmall)
                }
                
                Button(
                    onClick = { 
                        viewModel.updateGraduationStatus(record.copy(status = GraduationStatus.GRADUATED, certificateStatus = CertificateStatus.ISSUED)) 
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryGreen),
                    enabled = record.status == GraduationStatus.CLEARED && role == UserRole.ADMIN
                ) {
                    Icon(Icons.Default.AssignmentTurnedIn, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Graduate", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
fun ChipStatus(status: GraduationStatus) {
    Surface(
        color = when(status) {
            GraduationStatus.GRADUATED, GraduationStatus.ALUMNI -> SecondaryGreen.copy(alpha = 0.1f)
            GraduationStatus.CLEARED -> PrimaryBlue.copy(alpha = 0.1f)
            else -> Color.Gray.copy(alpha = 0.1f)
        },
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = status.name.replace("_", " "),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = when(status) {
                GraduationStatus.GRADUATED, GraduationStatus.ALUMNI -> SecondaryGreen
                GraduationStatus.CLEARED -> PrimaryBlue
                else -> Color.Gray
            },
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AlumniTab(viewModel: AcademyViewModel) {
    val alumni by viewModel.getAlumni().collectAsState(initial = emptyList())
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (alumni.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("No alumni records found", color = Color.Gray)
                }
            }
        }
        
        items(alumni) { student ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.School, null, tint = SecondaryGreen)
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(student.name, fontWeight = FontWeight.Bold)
                        Text("Class of 2024 • ${student.rollNumber}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }
        }
    }
}
