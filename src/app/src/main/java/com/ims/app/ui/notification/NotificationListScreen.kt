package com.ims.app.ui.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ims.app.data.entity.Notification
import com.ims.app.data.entity.NotificationType
import com.ims.app.ui.theme.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationListScreen(
    viewModel: NotificationViewModel,
    userId: Long,
    role: com.ims.app.data.entity.UserRole,
    onBack: () -> Unit,
    onCompose: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(userId) {
        viewModel.loadNotifications(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (state.notifications.isNotEmpty()) {
                        TextButton(onClick = { viewModel.markAllAsRead(userId) }) {
                            Text("Mark all as read", color = PrimaryBlue)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            if (role == com.ims.app.data.entity.UserRole.ADMIN || role == com.ims.app.data.entity.UserRole.TEACHER) {
                FloatingActionButton(
                    onClick = onCompose,
                    containerColor = PrimaryBlue,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Compose Alert")
                }
            }
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
        } else if (state.notifications.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.NotificationsOff,
                        null,
                        modifier = Modifier.size(64.dp),
                        tint = TextMuted
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("No notifications yet", style = MaterialTheme.typography.titleMedium, color = TextMuted)
                    Text("We'll alert you when something happens", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                }
            }
        } else {
            val grouped = state.notifications.groupBy { 
                formatDateGroup(it.timestamp)
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                grouped.forEach { (group, notifications) ->
                    item {
                        Text(
                            text = group,
                            style = MaterialTheme.typography.labelLarge,
                            color = PrimaryBlue,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    items(notifications, key = { it.id }) { notification ->
                        NotificationItem(
                            notification = notification,
                            onClick = { 
                                if (!notification.isRead) viewModel.markAsRead(notification.id)
                            },
                            onDelete = { viewModel.deleteNotification(notification) }
                        )
                    }
                }
                
                item {
                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { viewModel.clearHistory(userId) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.DeleteSweep, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Clear History")
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItem(
    notification: Notification,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val backgroundColor = if (notification.isRead) MaterialTheme.colorScheme.surface else PrimaryBlue.copy(alpha = 0.05f)
    val icon = when (notification.type) {
        NotificationType.ATTENDANCE -> Icons.Default.FactCheck
        NotificationType.EXAM -> Icons.Default.Quiz
        NotificationType.NEWS -> Icons.Default.Newspaper
        NotificationType.SYSTEM -> Icons.Default.Info
    }
    val iconColor = when (notification.type) {
        NotificationType.ATTENDANCE -> SecondaryGreen
        NotificationType.EXAM -> WarningAmber
        NotificationType.NEWS -> PrimaryBlue
        NotificationType.SYSTEM -> TextMuted
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (notification.isRead) 0.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(24.dp))
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        notification.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (notification.isRead) FontWeight.SemiBold else FontWeight.Bold,
                        color = if (notification.isRead) MaterialTheme.colorScheme.onSurface else PrimaryBlue
                    )
                    if (!notification.isRead) {
                        Surface(
                            modifier = Modifier.size(8.dp),
                            shape = CircleShape,
                            color = PrimaryBlue
                        ) {}
                    }
                }
                
                Spacer(Modifier.height(4.dp))
                
                Text(
                    notification.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
                
                Spacer(Modifier.height(8.dp))
                
                Text(
                    formatRelativeTime(notification.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
            
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Close, "Delete", modifier = Modifier.size(18.dp), tint = TextMuted)
            }
        }
    }
}

fun formatDateGroup(timestamp: String): String {
    return try {
        val dt = LocalDateTime.parse(timestamp)
        val now = LocalDateTime.now()
        when {
            ChronoUnit.DAYS.between(dt, now) == 0L -> "Today"
            ChronoUnit.DAYS.between(dt, now) == 1L -> "Yesterday"
            else -> dt.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))
        }
    } catch (e: Exception) {
        "Recent"
    }
}

fun formatRelativeTime(timestamp: String): String {
    return try {
        val dt = LocalDateTime.parse(timestamp)
        val now = LocalDateTime.now()
        val minutes = ChronoUnit.MINUTES.between(dt, now)
        val hours = ChronoUnit.HOURS.between(dt, now)
        val days = ChronoUnit.DAYS.between(dt, now)

        when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "$minutes mins ago"
            hours < 24 -> "$hours hours ago"
            days == 1L -> "Yesterday"
            else -> dt.format(DateTimeFormatter.ofPattern("MMM dd, HH:mm"))
        }
    } catch (e: Exception) {
        timestamp
    }
}
