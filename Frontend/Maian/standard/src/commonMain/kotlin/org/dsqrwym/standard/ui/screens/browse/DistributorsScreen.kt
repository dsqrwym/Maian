package org.dsqrwym.standard.ui.screens.browse

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.load_failed
import maian.standard.generated.resources.StandardRes
import maian.standard.generated.resources.current_wholesaler
import maian.standard.generated.resources.no_distributors
import maian.standard.generated.resources.search_distributors
import org.dsqrwym.shared.ui.components.buttons.SharedCloseButton
import org.dsqrwym.shared.ui.components.scaffold.SharedTransparentScaffold
import org.dsqrwym.standard.domain.browse.RetailDistributor
import org.dsqrwym.standard.ui.viewmodels.browse.DistributorsViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun DistributorsScreen(
    onNavigateBack: (() -> Unit)? = null,
    selectedWholesalerId: String? = null,
    selectedWholesaler: RetailDistributor? = null,
    onDistributorClick: (RetailDistributor) -> Unit,
    viewModel: DistributorsViewModel = koinViewModel(),
) {
    LaunchedEffect(Unit) {
        viewModel.ensureLoaded()
    }

    SharedTransparentScaffold(
        onNavigateBack = onNavigateBack,
        showOverlayDialog = false,
        overlayContent = {},
        title = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SearchBarDefaults.InputField(
                    modifier = Modifier.weight(1f),
                    query = viewModel.searchText,
                    onQueryChange = viewModel::updateSearchText,
                    onSearch = { viewModel.submitSearch() },
                    expanded = false,
                    onExpandedChange = {},
                    placeholder = { Text(stringResource(StandardRes.string.search_distributors)) },
                    leadingIcon = { Icon(Icons.Outlined.Search, stringResource(StandardRes.string.search_distributors)) },
                    trailingIcon = {
                        if (viewModel.searchText.isNotEmpty()) {
                            SharedCloseButton(onClick = viewModel::clearSearch)
                        }
                    },
                )
            }
        },
    ) { padding, _ ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            val displayDistributors = remember(viewModel.distributors, selectedWholesalerId, selectedWholesaler) {
                buildList {
                    selectedWholesaler?.let(::add)
                    addAll(viewModel.distributors.filterNot { it.id == selectedWholesalerId })
                }
            }

            when {
                viewModel.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }

                viewModel.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(viewModel.error ?: stringResource(SharedRes.string.load_failed))
                }

                displayDistributors.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(StandardRes.string.no_distributors))
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 16.dp),
                    ) {
                        items(displayDistributors, key = { it.id }) { distributor ->
                            val selected = distributor.id == selectedWholesalerId
                            DistributorCard(
                                distributor = distributor,
                                selected = selected,
                                onClick = { onDistributorClick(distributor) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DistributorCard(
    distributor: RetailDistributor,
    selected: Boolean = false,
    onClick: () -> Unit,
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = if (selected) {
            CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            )
        } else {
            CardDefaults.elevatedCardColors()
        },
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.Storefront,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Column(Modifier.weight(1f)) {
                Text(
                    text = distributor.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                distributor.secondaryText.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (selected) {
                AssistChip(
                    onClick = onClick,
                    label = { Text(stringResource(StandardRes.string.current_wholesaler), maxLines = 1) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = stringResource(StandardRes.string.current_wholesaler),
                        )
                    },
                )
            }
        }
    }
}
