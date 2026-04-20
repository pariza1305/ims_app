package com.ims.app.ui.dashboard

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ims.app.R
import com.ims.app.data.entity.UserRole
import com.ims.app.ui.components.StatusChip
import com.ims.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: DashboardViewModel,
    onBack: () -> Unit,
    onSignOut: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val user = state.currentUser
    val student = state.currentStudent
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var editMode by remember { mutableStateOf(false) }
    var editName by remember(user) { mutableStateOf(user?.name ?: "") }
    var editEmail by remember(user) { mutableStateOf(user?.email ?: "") }
    var editPhone by remember(user) { mutableStateOf(user?.phone ?: "") }
    var editDepartment by remember(user) { mutableStateOf(user?.department ?: "") }

    val scrollState = rememberScrollState()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.profile_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, stringResource(id = R.string.common_back))
                    }
                },
                actions = {
                    if (!editMode) {
                        IconButton(onClick = { editMode = true }) {
                            Icon(Icons.Outlined.Edit, stringResource(id = R.string.profile_edit))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
        ) {
            // Header with Gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha=0.7f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.2f), CircleShape)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surface, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (user?.name?.firstOrNull()?.toString() ?: "U").uppercase(),
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        if (editMode) {
                            IconButton(
                                onClick = { /* Future: Image picker */ },
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(32.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                                    .padding(4.dp)
                            ) {
                                Icon(Icons.Filled.CameraAlt, "Change photo", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                    
                    if (!editMode) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = user?.name ?: "User",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(Modifier.height(4.dp))
                        StatusChip(
                            text = state.currentRole.name,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Info Card
                ProfileCard(title = "Personal Information") {
                    if (editMode) {
                        EditField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = "Full Name",
                            icon = Icons.Outlined.Person
                        )
                        EditField(
                            value = editEmail,
                            onValueChange = { editEmail = it },
                            label = "Email Address",
                            icon = Icons.Outlined.Email
                        )
                        EditField(
                            value = editPhone,
                            onValueChange = { editPhone = it },
                            label = "Phone Number",
                            icon = Icons.Outlined.Phone
                        )
                        EditField(
                            value = editDepartment,
                            onValueChange = { editDepartment = it },
                            label = "Department",
                            icon = Icons.Outlined.Business
                        )
                    } else {
                        InfoRow(icon = Icons.Outlined.Person, label = "Full Name", value = user?.name ?: "-")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant)
                        InfoRow(icon = Icons.Outlined.Email, label = "Email", value = user?.email ?: "-")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant)
                        InfoRow(icon = Icons.Outlined.Phone, label = "Phone", value = user?.phone?.ifEmpty { "Not Set" } ?: "Not Set")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant)
                        InfoRow(icon = Icons.Outlined.Business, label = "Department", value = user?.department?.ifEmpty { "General" } ?: "General")
                    }
                }

                // Role Specific Card
                if (!editMode) {
                    val cardTitle = when (state.currentRole) {
                        UserRole.STUDENT -> "Academic Profile"
                        UserRole.TEACHER -> "Professional Profile"
                        else -> "System Privileges"
                    }
                    
                    ProfileCard(title = cardTitle) {
                        when (state.currentRole) {
                            UserRole.STUDENT -> {
                                InfoRow(icon = Icons.Outlined.Badge, label = "Roll Number", value = student?.rollNumber ?: "TEMP-001")
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant)
                                InfoRow(icon = Icons.Outlined.Class, label = "Current Batch", value = "Standard 10")
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant)
                                InfoRow(icon = Icons.Outlined.CalendarToday, label = "Join Date", value = student?.admissionDate ?: "01 Jan 2024")
                            }
                            UserRole.TEACHER -> {
                                InfoRow(icon = Icons.Outlined.WorkOutline, label = "Designation", value = "Head of Department")
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant)
                                InfoRow(icon = Icons.Outlined.History, label = "Experience", value = "5+ Years")
                            }
                            UserRole.ADMIN -> {
                                InfoRow(icon = Icons.Outlined.Security, label = "Access Level", value = "Full System Access")
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant)
                                InfoRow(icon = Icons.Outlined.VerifiedUser, label = "Status", value = "Verified Administrator")
                            }
                            else -> {}
                        }
                    }
                }

                if (editMode) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { editMode = false },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlue)
                        ) {
                            Text("Cancel", color = PrimaryBlue, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                viewModel.updateProfile(editName, editEmail, editPhone, editDepartment)
                                editMode = false
                                scope.launch {
                                    snackbarHostState.showSnackbar("Profile updated successfully")
                                }
                            },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Text("Save Changes", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // Utility Actions
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(20.dp),
                        shadowElevation = 2.dp
                    ) {
                        Column {
                            ActionRow(icon = Icons.Outlined.Lock, label = "Security & Password", color = PrimaryBlue) {
                                // Action
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            ActionRow(icon = Icons.Outlined.Logout, label = "Sign Out", color = DangerRed) {
                                onSignOut()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 20.dp)
            )
            content()
        }
    }
}

@Composable
fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 0.5.sp)
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) },
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            containerColor = MaterialTheme.colorScheme.surface
        ),
        singleLine = true
    )
}

@Composable
fun ActionRow(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(16.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(Modifier.weight(1f))
            Icon(Icons.Filled.ChevronRight, null, tint = TextMuted.copy(alpha = 0.5f), modifier = Modifier.size(24.dp))
        }
    }
}
