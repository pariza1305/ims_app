package com.ims.app.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ims.app.R
import com.ims.app.ui.components.*
import com.ims.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onNavigateToFeature: (String) -> Unit
) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    val filteredFeatures = remember(query, context.resources.configuration.locales[0]) {
        if (query.isBlank()) {
            dashboardFeatureCatalog
        } else {
            dashboardFeatureCatalog.filter {
                context.getString(it.titleRes).contains(query, ignoreCase = true) ||
                    context.getString(it.keywordsRes).contains(query, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.search_title), fontWeight = FontWeight.SemiBold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            IMSSearchBar(
                query = query,
                onQueryChange = { query = it },
                placeholder = stringResource(id = R.string.search_feature_placeholder)
            )

            Spacer(Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filteredFeatures) { feature ->
                    ListItem(
                        headlineContent = { Text(stringResource(id = feature.titleRes), fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text(stringResource(id = R.string.search_tap_to_navigate)) },
                        leadingContent = { Icon(Icons.Outlined.ArrowOutward, stringResource(id = R.string.common_navigate), tint = PrimaryBlue) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToFeature(feature.route) },
                        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
                    )
                }
                if (filteredFeatures.isEmpty()) {
                    item {
                        EmptyState(
                            icon = Icons.Outlined.SearchOff,
                            title = stringResource(id = R.string.search_no_features_found),
                            subtitle = stringResource(id = R.string.search_try_different_keyword)
                        )
                    }
                }
            }
        }
    }
}
