package com.ims.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ims.app.R
import com.ims.app.ui.theme.*

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String = "",
    icon: ImageVector,
    iconTint: Color = PrimaryBlue,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(iconTint.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun StudentAttendanceCard(
    name: String,
    rollNumber: String,
    currentStatus: String,
    remarks: String,
    onStatusChange: (String) -> Unit,
    onRemarksChange: (String) -> Unit,
    readOnly: Boolean = false,
    modifier: Modifier = Modifier
) {
    val statuses = listOf("PRESENT", "ABSENT")

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            var showRemarks by remember { mutableStateOf(remarks.isNotEmpty()) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(PrimaryBlue.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name.first().uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                // Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(id = R.string.common_roll_with_value, rollNumber),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // Remarks toggle (Teacher only or if student has remarks)
                if (!readOnly || remarks.isNotEmpty()) {
                    IconButton(onClick = { showRemarks = !showRemarks }) {
                        Icon(
                            imageVector = if (remarks.isNotEmpty()) Icons.Filled.Comment else Icons.Outlined.AddComment,
                            contentDescription = "Remarks",
                            tint = if (remarks.isNotEmpty()) PrimaryBlue else TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                // Status buttons
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    statuses.forEach { status ->
                        val isSelected = currentStatus == status
                        val bgColor by animateColorAsState(
                            targetValue = when {
                                isSelected && status == "PRESENT" -> SecondaryGreen
                                isSelected && status == "ABSENT" -> DangerRed
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            },
                            label = "statusColor"
                        )
                        val icon = when (status) {
                            "PRESENT" -> Icons.Filled.Check
                            "ABSENT" -> Icons.Filled.Close
                            else -> Icons.Filled.Schedule
                        }
                        val elevation by animateDpAsState(
                            targetValue = if (isSelected) 4.dp else 0.dp,
                            label = "elevation"
                        )
                        Surface(
                            modifier = Modifier
                                .size(36.dp)
                                .shadow(elevation, CircleShape)
                                .clip(CircleShape)
                                .clickable(enabled = !readOnly) { onStatusChange(status) },
                            color = bgColor,
                            shape = CircleShape
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = status,
                                    tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Expanded Remarks
            androidx.compose.animation.AnimatedVisibility(
                visible = showRemarks,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    if (!readOnly) {
                        OutlinedTextField(
                            value = remarks,
                            onValueChange = onRemarksChange,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            label = { Text("Add Remark") },
                            placeholder = { Text("e.g. Participated well, Came late") },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )
                    } else if (remarks.isNotEmpty()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = PrimaryBlue.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Outlined.Info,
                                    null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(
                                        "Teacher's Note",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = PrimaryBlue,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        remarks,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextMain
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IMSSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "",
    modifier: Modifier = Modifier,
    onSearch: () -> Unit = {}
) {
    val resolvedPlaceholder = if (placeholder.isBlank()) {
        stringResource(id = R.string.search_default_placeholder)
    } else {
        placeholder
    }

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        placeholder = {
            Text(
                text = resolvedPlaceholder,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = stringResource(id = R.string.search_title),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Filled.Clear, stringResource(id = R.string.common_clear))
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryBlue,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
fun StatusChip(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    actionText: String = "",
    onAction: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (actionText.isNotEmpty()) {
            TextButton(onClick = onAction) {
                Text(
                    text = actionText,
                    style = MaterialTheme.typography.labelMedium,
                    color = PrimaryBlue
                )
            }
        }
    }
}

@Composable
fun NewsCard(
    title: String,
    content: String,
    date: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = date,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ExamCard(
    examName: String,
    subjectName: String,
    date: String,
    status: String,
    totalMarks: Int,
    examType: String = "",
    evaluationMethod: String = "",
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    actionIcon: ImageVector? = null,
    onActionClick: (() -> Unit)? = null
) {
    val statusColor = when (status) {
        "DRAFT" -> TextMuted
        "PUBLISHED" -> PrimaryBlue
        "IN_PROGRESS" -> WarningAmber
        "COMPLETED" -> SecondaryGreen
        "RESULTS_PUBLISHED" -> SecondaryGreenDark
        else -> TextMuted
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Left accent bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(100.dp)
                    .background(statusColor, RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = examName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    StatusChip(text = status.replace("_", " "), color = statusColor)
                    if (actionIcon != null && onActionClick != null) {
                        Spacer(Modifier.width(8.dp))
                        IconButton(onClick = onActionClick, modifier = Modifier.size(24.dp)) {
                            Icon(actionIcon, null, tint = statusColor, modifier = Modifier.size(20.dp))
                        }
                    }
                }
                Text(
                    text = subjectName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.CalendarToday, "Date", modifier = Modifier.size(14.dp), tint = TextMuted)
                        Spacer(Modifier.width(4.dp))
                        Text(text = date, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    }
                    Text(
                        text = "Total: $totalMarks",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
                // Evaluation method info
                if (examType.isNotEmpty() || evaluationMethod.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Outlined.Assessment, "Eval", modifier = Modifier.size(14.dp), tint = TextMuted)
                        Text(
                            text = listOfNotNull(
                                examType.ifEmpty { null },
                                evaluationMethod.ifEmpty { null }
                            ).joinToString(" • "),
                            style = MaterialTheme.typography.labelSmall,
                            color = PrimaryBlue,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EventExamCard(
    examName: String,
    subjectName: String,
    date: String,
    startTime: String,
    endTime: String,
    location: String,
    status: String,
    modifier: Modifier = Modifier,
    onInfoClick: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    val statusColor = when (status) {
        "DRAFT" -> TextMuted
        "PUBLISHED" -> PrimaryBlue
        "IN_PROGRESS" -> WarningAmber
        "COMPLETED" -> SecondaryGreen
        "RESULTS_PUBLISHED" -> SecondaryGreenDark
        else -> TextMuted
    }

    val displayStatus = when (status) {
        "IN_PROGRESS" -> "Ongoing"
        "PUBLISHED" -> "Upcoming"
        "RESULTS_PUBLISHED" -> "Published"
        "COMPLETED" -> "Completed"
        else -> status.replace("_", " ")
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = examName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "By $subjectName",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted,
                        fontWeight = FontWeight.Medium
                    )
                }
                IconButton(
                    onClick = onInfoClick,
                    modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(
                        Icons.Filled.Info,
                        contentDescription = "Info",
                        tint = PrimaryBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.CalendarToday, "Date", modifier = Modifier.size(16.dp), tint = TextMuted)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "$date, $startTime - $endTime",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.LocationOn, "Location", modifier = Modifier.size(16.dp), tint = TextMuted)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = location,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Box(
                modifier = Modifier
                    .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = displayStatus,
                    style = MaterialTheme.typography.labelMedium,
                    color = statusColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String = "",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (subtitle.isNotEmpty()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}
