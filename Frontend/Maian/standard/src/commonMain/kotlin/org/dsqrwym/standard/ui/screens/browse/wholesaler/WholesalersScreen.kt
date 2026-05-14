package org.dsqrwym.standard.ui.screens.browse.wholesaler

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import maian.shared.generated.resources.*
import maian.standard.generated.resources.StandardRes
import maian.standard.generated.resources.no_wholesalers
import maian.standard.generated.resources.search_wholesalers
import org.dsqrwym.shared.data.OrderDir
import org.dsqrwym.shared.data.user.SpanishCompanyType
import org.dsqrwym.shared.data.user.dto.WholesalerSortField
import org.dsqrwym.shared.data.user.toStringResource
import org.dsqrwym.shared.network.ApiConfig
import org.dsqrwym.shared.ui.components.buttons.SharedCloseButton
import org.dsqrwym.shared.ui.components.dialog.SharedImageViewDialog
import org.dsqrwym.shared.ui.components.icon.SharedCloseIcon
import org.dsqrwym.shared.ui.components.input.selector.Selector
import org.dsqrwym.shared.ui.components.input.selector.SelectorConfig
import org.dsqrwym.shared.ui.components.row.SharedFilterChipsRow
import org.dsqrwym.shared.ui.components.scaffold.SharedTransparentScaffold
import org.dsqrwym.shared.ui.components.wholesaler.*
import org.dsqrwym.shared.ui.overlay.transparentDialogProperties
import org.dsqrwym.standard.domain.browse.RetailWholesaler
import org.dsqrwym.standard.domain.browse.toCardData
import org.dsqrwym.standard.ui.viewmodels.browse.WholesalersViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private val wholesalerSortFields = listOf(
    WholesalerSortField.DISPLAY_NAME,
    WholesalerSortField.COMPANY_NAME,
    WholesalerSortField.CITY,
    WholesalerSortField.PROVINCE,
    WholesalerSortField.MINIMUM_ORDER_AMOUNT,
)

private data class CompanyTypeFilterOption(
    val companyType: SpanishCompanyType?,
    val label: String,
)

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun WholesalersScreen(
    onNavigateBack: (() -> Unit)? = null,
    onWholesalerClick: (RetailWholesaler) -> Unit,
    viewModel: WholesalersViewModel = koinViewModel(),
) {
    val paginatedWholesalers = viewModel.pagedWholesalers.collectAsLazyPagingItems()
    var previewWholesaler by remember { mutableStateOf<RetailWholesaler?>(null) }

    SharedTransparentScaffold(
        onNavigateBack = onNavigateBack,
        showOverlayDialog = viewModel.showFilterDialog || viewModel.showSortDialog || previewWholesaler?.logoFileId != null,
        overlayContent = {
            previewWholesaler?.logoFileId?.let { logoFileId ->
                SharedImageViewDialog(
                    model = previewWholesaler?.id.takeIf { it != null }
                        ?.let { ApiConfig.FilePath.userImage(it, logoFileId) } ?: "",
                    imageName = previewWholesaler?.displayName ?: previewWholesaler?.companyName,
                    onDismissRequest = { previewWholesaler = null },
                )
            }
            if (viewModel.showFilterDialog) {
                WholesalerFilterDialog(viewModel)
            }
            if (viewModel.showSortDialog) {
                SharedWholesalerSortDialog(
                    selectedSortBy = viewModel.sortBy,
                    sortDir = viewModel.sortDir,
                    fields = wholesalerSortFields,
                    onToggleSort = viewModel::toggleSort,
                    onDismissRequest = { viewModel.updateShowSortDialog(false) },
                )
            }
        },
        title = {
            Column {
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
                        placeholder = { Text(stringResource(StandardRes.string.search_wholesalers)) },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Search,
                                stringResource(StandardRes.string.search_wholesalers),
                            )
                        },
                        trailingIcon = {
                            if (viewModel.searchText.isNotEmpty()) {
                                SharedCloseButton(onClick = viewModel::clearSearch)
                            }
                        },
                    )
                    IconButton(onClick = { viewModel.updateShowFilterDialog(true) }) {
                        Icon(Icons.Outlined.FilterList, stringResource(SharedRes.string.filter))
                    }
                    IconButton(onClick = { viewModel.updateShowSortDialog(true) }) {
                        Icon(Icons.Outlined.SwapVert, stringResource(SharedRes.string.sort))
                    }
                }
                WholesalerFilterChipsRow(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
    ) { padding, scrollBehavior ->
        SharedWholesalerWaterfall(
            paginatedItems = paginatedWholesalers,
            scrollBehavior = scrollBehavior,
            padding = padding,
            emptyText = stringResource(StandardRes.string.no_wholesalers),
            key = paginatedWholesalers.itemKey { "wholesaler-${it.id}" },
        ) { wholesaler ->
            WholesalerListItem(
                wholesaler = wholesaler,
                onClick = { onWholesalerClick(wholesaler) },
                onImageClick = { previewWholesaler = wholesaler },
            )
        }
    }
}

@Composable
private fun WholesalerListItem(
    wholesaler: RetailWholesaler,
    onClick: () -> Unit,
    onImageClick: () -> Unit,
) {
    SharedWholesalerCard(
        data = wholesaler.toCardData(),
        variant = WholesalerCardVariant.ListItem,
        onCardClick = onClick,
        onImageClick = wholesaler.logoFileId?.let { { onImageClick() } },
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun WholesalerFilterDialog(viewModel: WholesalersViewModel) {
    AlertDialog(
        properties = transparentDialogProperties(),
        onDismissRequest = { viewModel.updateShowFilterDialog(false) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = stringResource(SharedRes.string.filter),
                    style = MaterialTheme.typography.titleMedium,
                )
                AvailabilityFilter(
                    title = stringResource(SharedRes.string.delivery_available),
                    selected = viewModel.deliveryAvailable,
                    onSelected = viewModel::updateDeliveryAvailable,
                )
                AvailabilityFilter(
                    title = stringResource(SharedRes.string.pickup_available),
                    selected = viewModel.pickupAvailable,
                    onSelected = viewModel::updatePickupAvailable,
                )
                CompanyTypeFilter(viewModel)
            }
        },
        confirmButton = {
            TextButton(onClick = { viewModel.updateShowFilterDialog(false) }) {
                Text(stringResource(SharedRes.string.close))
            }
        },
    )
}

@Composable
private fun CompanyTypeFilter(viewModel: WholesalersViewModel) {
    val options = listOf(
        CompanyTypeFilterOption(null, stringResource(SharedRes.string.all)),
    ) + SpanishCompanyType.entries.map {
        CompanyTypeFilterOption(it, stringResource(it.toStringResource()))
    }

    Selector(
        items = options,
        selectedItem = options.firstOrNull { it.companyType == viewModel.companyType },
        itemToString = { it.label },
        onItemSelected = { viewModel.updateCompanyType(it?.companyType) },
        config = SelectorConfig(
            label = stringResource(SharedRes.string.company_type),
            placeholder = stringResource(SharedRes.string.all),
        ),
    )
}

@Composable
private fun AvailabilityFilter(
    title: String,
    selected: Boolean?,
    onSelected: (Boolean?) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ElevatedFilterChip(
                selected = selected == null,
                onClick = { onSelected(null) },
                label = { Text(stringResource(SharedRes.string.all)) },
            )
            ElevatedFilterChip(
                selected = selected == true,
                onClick = { onSelected(true) },
                label = { Text(title) },
            )
        }
    }
}

@Composable
private fun WholesalerFilterChipsRow(
    viewModel: WholesalersViewModel,
    modifier: Modifier = Modifier,
) {
    SharedFilterChipsRow(modifier = modifier) {
        viewModel.deliveryAvailable?.let {
            ElevatedFilterChip(
                selected = true,
                onClick = { viewModel.updateDeliveryAvailable(null) },
                label = { Text(stringResource(SharedRes.string.delivery_available)) },
                trailingIcon = { SharedCloseIcon() },
            )
        }
        viewModel.pickupAvailable?.let {
            ElevatedFilterChip(
                selected = true,
                onClick = { viewModel.updatePickupAvailable(null) },
                label = { Text(stringResource(SharedRes.string.pickup_available)) },
                trailingIcon = { SharedCloseIcon() },
            )
        }
        viewModel.companyType?.let { companyType ->
            ElevatedFilterChip(
                selected = true,
                onClick = { viewModel.updateCompanyType(null) },
                label = { Text(stringResource(companyType.toStringResource())) },
                trailingIcon = { SharedCloseIcon() },
            )
        }
        SharedWholesalerSortChip(
            sortBy = viewModel.sortBy,
            sortDir = viewModel.sortDir,
            onToggleDirection = {
                viewModel.updateSortDir(if (viewModel.sortDir == OrderDir.ASC) OrderDir.DESC else OrderDir.ASC)
            },
        )
    }
}
