package com.ims.app.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.ims.app.BuildConfig
import com.ims.app.R
import com.ims.app.i18n.AppLanguageManager
import com.ims.app.ui.theme.*
import com.ims.app.data.AppSettingsManager
import com.ims.app.data.entity.EvaluationMethod

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedLanguageTag by remember { mutableStateOf(AppLanguageManager.getSavedLanguageTag(context)) }
    var selectedCountry by remember { mutableStateOf("India") }
    var selectedCurrency by remember { mutableStateOf("INR") }
    var selectedTimezone by remember { mutableStateOf("IST (UTC+5:30)") }
    var selectedGrading by remember { mutableStateOf(AppSettingsManager.getSavedGradingSystem(context).name) }
    var autoUniqueId by remember { mutableStateOf(AppSettingsManager.getAutoUniqueId(context)) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showCountryDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showTimezoneDialog by remember { mutableStateOf(false) }
    var showGradingDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.settings_title), fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, stringResource(id = R.string.common_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    stringResource(id = R.string.settings_section_general),
                    style = MaterialTheme.typography.labelMedium,
                    color = PrimaryBlue,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Outlined.Language,
                    title = stringResource(id = R.string.settings_language),
                    subtitle = languageLabelForTag(selectedLanguageTag),
                    onClick = { showLanguageDialog = true }
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Outlined.Schedule,
                    title = stringResource(id = R.string.settings_country),
                    subtitle = selectedCountry,
                    onClick = { showCountryDialog = true }
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Outlined.Payments,
                    title = stringResource(id = R.string.settings_currency),
                    subtitle = selectedCurrency,
                    onClick = { showCurrencyDialog = true }
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Outlined.Schedule,
                    title = stringResource(id = R.string.settings_timezone),
                    subtitle = selectedTimezone,
                    onClick = { showTimezoneDialog = true }
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Outlined.Grade,
                    title = stringResource(id = R.string.settings_grading_system),
                    subtitle = selectedGrading,
                    onClick = { showGradingDialog = true }
                )
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Fingerprint, stringResource(id = R.string.settings_auto_id), tint = PrimaryBlue, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(id = R.string.settings_auto_unique_id), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                            Text(if (autoUniqueId) stringResource(id = R.string.settings_enabled) else stringResource(id = R.string.settings_disabled), style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        }
                        Switch(checked = autoUniqueId, onCheckedChange = { 
                            autoUniqueId = it
                            AppSettingsManager.setAutoUniqueId(context, it)
                        })
                    }
                }
            }
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    stringResource(id = R.string.settings_section_system),
                    style = MaterialTheme.typography.labelMedium,
                    color = PrimaryBlue,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Outlined.Info,
                    title = stringResource(id = R.string.settings_app_version),
                    subtitle = "1.0.0",
                    onClick = {}
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Outlined.Code,
                    title = stringResource(id = R.string.settings_app_identifier),
                    subtitle = BuildConfig.APP_IDENTIFIER,
                    onClick = {}
                )
            }
        }
    }

    // Language dialog
    if (showLanguageDialog) {
        val languageOptions = listOf(
            LanguageOption(AppLanguageManager.TAG_ENGLISH, context.getString(R.string.language_english)),
            LanguageOption(AppLanguageManager.TAG_HINDI, context.getString(R.string.language_hindi)),
            LanguageOption(AppLanguageManager.TAG_PUNJABI, context.getString(R.string.language_punjabi))
        )
        LanguageSelectionDialog(
            title = stringResource(id = R.string.settings_select_language),
            options = languageOptions,
            selectedTag = selectedLanguageTag,
            onSelect = {
                selectedLanguageTag = it.tag
                AppLanguageManager.setLanguage(context, it.tag)
                showLanguageDialog = false
            },
            onDismiss = { showLanguageDialog = false }
        )
    }
    if (showTimezoneDialog) {
        SelectionDialog(
            title = stringResource(id = R.string.settings_select_timezone),
            options = listOf("IST (UTC+5:30)", "EST (UTC-5)", "PST (UTC-8)", "GMT (UTC+0)", "JST (UTC+9)"),
            selected = selectedTimezone,
            onSelect = { selectedTimezone = it; showTimezoneDialog = false },
            onDismiss = { showTimezoneDialog = false }
        )
    }
    if (showCountryDialog) {
        SelectionDialog(
            title = stringResource(id = R.string.settings_select_country),
            options = listOf("India", "USA", "UK", "Germany", "Japan"),
            selected = selectedCountry,
            onSelect = { selectedCountry = it; showCountryDialog = false },
            onDismiss = { showCountryDialog = false }
        )
    }
    if (showCurrencyDialog) {
        SelectionDialog(
            title = stringResource(id = R.string.settings_select_currency),
            options = listOf("INR", "USD", "GBP", "EUR", "JPY"),
            selected = selectedCurrency,
            onSelect = { selectedCurrency = it; showCurrencyDialog = false },
            onDismiss = { showCurrencyDialog = false }
        )
    }
    if (showGradingDialog) {
        SelectionDialog(
            title = stringResource(id = R.string.settings_select_grading_system),
            options = EvaluationMethod.entries.map { it.name },
            selected = selectedGrading,
            onSelect = { 
                selectedGrading = it
                AppSettingsManager.setSavedGradingSystem(context, EvaluationMethod.valueOf(it))
                showGradingDialog = false 
            },
            onDismiss = { showGradingDialog = false }
        )
    }
}

private data class LanguageOption(
    val tag: String,
    val label: String
)

@Composable
private fun languageLabelForTag(tag: String): String {
    return when (tag) {
        AppLanguageManager.TAG_HINDI -> stringResource(id = R.string.language_hindi)
        AppLanguageManager.TAG_PUNJABI -> stringResource(id = R.string.language_punjabi)
        else -> stringResource(id = R.string.language_english)
    }
}

@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, title, tint = PrimaryBlue, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextMuted)
            }
            Icon(Icons.Filled.ChevronRight, "Navigate", tint = TextMuted)
        }
    }
}

@Composable
private fun SelectionDialog(
    title: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.SemiBold) },
        text = {
            Column {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelect(option) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = option == selected,
                            onClick = { onSelect(option) },
                            colors = RadioButtonDefaults.colors(selectedColor = PrimaryBlue)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(option, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(id = R.string.common_cancel)) }
        }
    )
}

@Composable
private fun LanguageSelectionDialog(
    title: String,
    options: List<LanguageOption>,
    selectedTag: String,
    onSelect: (LanguageOption) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.SemiBold) },
        text = {
            Column {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelect(option) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = option.tag == selectedTag,
                            onClick = { onSelect(option) },
                            colors = RadioButtonDefaults.colors(selectedColor = PrimaryBlue)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(option.label, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(id = R.string.common_cancel)) }
        }
    )
}
