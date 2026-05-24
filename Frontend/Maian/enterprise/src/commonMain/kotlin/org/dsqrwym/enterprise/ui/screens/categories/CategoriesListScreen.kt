package org.dsqrwym.enterprise.ui.screens.categories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import maian.business.generated.resources.BusinessRes
import maian.business.generated.resources.base_category
import maian.business.generated.resources.parent_category_with_name
import maian.business.generated.resources.select_parent_category
import maian.business.generated.resources.subcategories_count
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.close
import maian.shared.generated.resources.create
import maian.shared.generated.resources.edit
import maian.shared.generated.resources.error_no_permission
import maian.shared.generated.resources.filter
import maian.shared.generated.resources.not_set
import maian.shared.generated.resources.search_category_name
import maian.shared.generated.resources.search_parent_category_name
import maian.shared.generated.resources.sort
import maian.shared.generated.resources.tax_rate
import org.dsqrwym.business.ui.components.button.BusinessOutlinedDeleteButton
import org.dsqrwym.business.ui.components.category.BusinessCategoryLanguages
import org.dsqrwym.business.ui.components.category.BusinessCategoryPath
import org.dsqrwym.business.ui.components.category.BusinessConfirmDeleteCategories
import org.dsqrwym.business.ui.components.tooltip.PermissionTooltip
import org.dsqrwym.enterprise.permissions.canManageEnterpriseCategories
import org.dsqrwym.enterprise.ui.viewmodels.categories.CategoriesListViewModel
import org.dsqrwym.shared.data.OrderDir
import org.dsqrwym.shared.data.category.displayName
import org.dsqrwym.shared.data.category.mapper.toDto
import org.dsqrwym.shared.data.category.sharedEnterpriseCategorySortFields
import org.dsqrwym.shared.data.user.UserRole
import org.dsqrwym.shared.domain.category.CategoryNode
import org.dsqrwym.shared.paging.hasLoadError
import org.dsqrwym.shared.paging.isAppendingOrPrepending
import org.dsqrwym.shared.paging.isEmptyResult
import org.dsqrwym.shared.paging.isRefreshing
import org.dsqrwym.shared.ui.components.buttons.SharedCloseButton
import org.dsqrwym.shared.ui.components.containers.UiState
import org.dsqrwym.shared.ui.components.icon.SharedCloseIcon
import org.dsqrwym.shared.ui.components.input.SharedSingleLinePlaceholderText
import org.dsqrwym.shared.ui.components.input.selector.RemoteSearchableSelectorConfig
import org.dsqrwym.shared.ui.components.input.selector.SearchableSelectorRemote
import org.dsqrwym.shared.ui.components.placeholder.SharedNotFoundPlaceholder
import org.dsqrwym.shared.ui.components.product.SharedProductSortDirectionLabel
import org.dsqrwym.shared.ui.components.row.SharedFilterChipsRow
import org.dsqrwym.shared.ui.components.scaffold.SharedTransparentScaffold
import org.dsqrwym.shared.ui.components.scaffold.SharedTransparentScaffoldFabButtonState
import org.dsqrwym.shared.ui.overlay.rememberResizeSafeDismissRequest
import org.dsqrwym.shared.ui.overlay.transparentDialogProperties
import org.dsqrwym.shared.util.colum.SharedColumnLayout
import org.dsqrwym.shared.util.lazygrid.SharedLazyGridLayout
import org.dsqrwym.shared.util.lazygrid.SharedLazyGridLayout.appendErrorRetry
import org.dsqrwym.shared.util.lazygrid.SharedLazyGridLayout.appendLoadingIndicator
import org.dsqrwym.shared.util.modifier.paddingWithoutTop
import org.dsqrwym.shared.util.modifier.placeholderWithShimmer
import org.dsqrwym.shared.util.row.SharedRowLayout
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesListScreen(
    viewModel: CategoriesListViewModel = koinViewModel(),
    userRole: UserRole? = null,
    onNavigateToCreate: () -> Unit = {},
    onNavigateToEdit: (String) -> Unit = {}
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val lazyPagingItems = viewModel.pagedCategories.collectAsLazyPagingItems()

    val searchQuery = viewModel.searchQuery
    val showFilterDialog = viewModel.showFilterDialog
    val showSortDialog = viewModel.showSortDialog
    val deleteCategory = viewModel.deleteCategory
    val canManageCategories = userRole?.canManageEnterpriseCategories() == true
    val noPermissionText = stringResource(SharedRes.string.error_no_permission)

    SharedTransparentScaffold(
        topBarScrollBehavior = scrollBehavior,
        showOverlayDialog = showFilterDialog || showSortDialog || deleteCategory != null,
        overlayContent = {
            deleteCategory?.let { category ->
                BusinessConfirmDeleteCategories(
                    category.name,
                    category.childrenCount,
                    { viewModel.updateShowDeleteDialog(null) },
                    { viewModel.deleteCategory(category) }
                )
            }
            if (showFilterDialog) {
                FilterDialog(viewModel)
            }
            if (showSortDialog) {
                CategorySortDialog(viewModel)
            }
        },
        title = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    SearchBarDefaults.InputField(
                        modifier = Modifier.weight(0.8f),
                        query = searchQuery,
                        onQueryChange = viewModel::updateSearchQuery,
                        onSearch = { viewModel.refresh() },
                        expanded = false,
                        onExpandedChange = {},
                        placeholder = {
                            SharedSingleLinePlaceholderText(stringResource(SharedRes.string.search_category_name))
                        },
                        leadingIcon = { Icon(Icons.Outlined.Search, null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                SharedCloseButton { viewModel.updateSearchQuery("") }
                            }
                        }
                    )
                    IconButton(onClick = { viewModel.updateShowFilterDialog(true) }, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Outlined.FilterList, stringResource(SharedRes.string.filter))
                    }
                    IconButton(onClick = { viewModel.updateShowSortDialog(true) }, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Outlined.SwapVert, stringResource(SharedRes.string.sort))
                    }
                }
                // 过滤标签
                FilterChipsRow(viewModel)
            }
        },
        fabButtonState = SharedTransparentScaffoldFabButtonState(
            buttonState = UiState.Idle,
            buttonEnabled = canManageCategories,
            onButtonClick = onNavigateToCreate,
            buttonText = stringResource(SharedRes.string.create),
            buttonIcon = Icons.Outlined.Add,
            buttonIconDescription = stringResource(SharedRes.string.create),
            disabledTooltipText = noPermissionText,
        ),
    )
    { padding, scrollBehavior ->
        val isRefreshing = lazyPagingItems.isRefreshing
        val pullRefreshState = rememberPullToRefreshState()
        PullToRefreshBox(
            modifier = Modifier
                .fillMaxSize()
                .paddingWithoutTop(padding),
            isRefreshing = isRefreshing,
            state = pullRefreshState,
            onRefresh = {
                lazyPagingItems.refresh()
            },
            indicator = {
                PullToRefreshDefaults.Indicator(
                    modifier = Modifier.padding(top = padding.calculateTopPadding()).align(Alignment.TopCenter),
                    isRefreshing = isRefreshing,
                    state = pullRefreshState,
                )
            }
        ) {
            if (lazyPagingItems.isEmptyResult) {
                SharedNotFoundPlaceholder()
            } else {
                // 类别列表
                LazyVerticalStaggeredGrid(
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(scrollBehavior.nestedScrollConnection)
                        .padding(horizontal = SharedLazyGridLayout.Padding),
                    columns = StaggeredGridCells.Adaptive(minSize = 330.dp),
                    horizontalArrangement = SharedLazyGridLayout.arrangement,
                    verticalItemSpacing = SharedLazyGridLayout.verticalItemSpacing,
                ) {
                    item(span = StaggeredGridItemSpan.FullLine) {
                        Spacer(Modifier.height(padding.calculateTopPadding()))
                    }

                    if (lazyPagingItems.hasLoadError) {
                        appendErrorRetry { lazyPagingItems.retry() }
                    } else {
                        val isCategoryLoading =
                            lazyPagingItems.isRefreshing || viewModel.isLoading

                        items(lazyPagingItems.itemCount, key = lazyPagingItems.itemKey { it.id } ) { index ->
                            lazyPagingItems[index]?.let {
                                CategoryListItem(
                                    modifier = Modifier.animateItem(),
                                    isLoading = isCategoryLoading,
                                    category = it,
                                    userRole = userRole,
                                    noPermissionText = noPermissionText,
                                    onEdit = { onNavigateToEdit(it.id.toString()) },
                                    onDelete = { viewModel.updateShowDeleteDialog(it) }
                                )
                            }
                        }
                    }

                    if (lazyPagingItems.isAppendingOrPrepending) {
                        appendLoadingIndicator()
                    }

                    item (span = StaggeredGridItemSpan.FullLine){
                        Spacer(Modifier.height(28.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryListItem(
    modifier: Modifier = Modifier,
    category: CategoryNode,
    isLoading: Boolean = true,
    userRole: UserRole? = null,
    noPermissionText: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val canManageCategory = userRole?.canManageEnterpriseCategories() == true
    OutlinedCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(SharedColumnLayout.padding),
            verticalArrangement = SharedColumnLayout.arrangement
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .placeholderWithShimmer(isLoading),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SelectionContainer(modifier = Modifier.weight(1f)) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = SharedRowLayout.arrangement
                        ) {
                            Text(
                                text = category.name,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        FlowRow(
                            verticalArrangement = Arrangement.Center,
                            horizontalArrangement = SharedRowLayout.arrangement
                        ) {
                            Text(
                                text = category.parentName?.let {
                                    stringResource(BusinessRes.string.parent_category_with_name, it)
                                } ?: stringResource(
                                    BusinessRes.string.base_category
                                ),
                                style = MaterialTheme.typography.bodySmall
                            )

                            Text(
                                text = "${stringResource(SharedRes.string.tax_rate)}->IVA: ${
                                    if (category.iva != null) category.iva + "%" else stringResource(
                                        SharedRes.string.not_set
                                    )
                                }",
                                style = MaterialTheme.typography.bodySmall
                            )

                            val childrenCount = category.childrenCount
                            if (childrenCount > 0) {
                                Text(
                                    text = stringResource(BusinessRes.string.subcategories_count, childrenCount),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                PermissionTooltip(canManageCategory, noPermissionText) {
                    IconButton(onClick = onEdit, enabled = canManageCategory) {
                        Icon(Icons.Outlined.Edit, stringResource(SharedRes.string.edit))
                    }
                }

            }

            HorizontalDivider()

            Row(Modifier.fillMaxWidth().placeholderWithShimmer(isLoading)) {
                SelectionContainer(modifier = Modifier.weight(1f)) {
                    Column(verticalArrangement = SharedColumnLayout.arrangement) {
                        BusinessCategoryLanguages(category.translations.map { it.toDto() })
                        BusinessCategoryPath(category.pathNames(), category.name)
                    }
                }
                Box(modifier = Modifier.align(Alignment.Bottom)) {
                    PermissionTooltip(canManageCategory, noPermissionText) {
                        BusinessOutlinedDeleteButton(
                            enabled = canManageCategory,
                            onDelete = onDelete
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategorySortDialog(
    viewModel: CategoriesListViewModel
) {
    val resizeSafeDismiss = rememberResizeSafeDismissRequest(
        onDismissRequest = { viewModel.updateShowSortDialog(false) },
    )

    AlertDialog(
        properties = transparentDialogProperties(),
        onDismissRequest = resizeSafeDismiss,
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.SwapVert, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text(stringResource(SharedRes.string.sort), style = MaterialTheme.typography.titleMedium)
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    sharedEnterpriseCategorySortFields.forEach { field ->
                        val selected = viewModel.sortBy == field
                        val label = field.displayName()
                        ElevatedFilterChip(
                            selected = selected,
                            onClick = { viewModel.toggleSort(field) },
                            label = {
                                if (selected) {
                                    SharedProductSortDirectionLabel(label = label, sortDir = viewModel.sortDir)
                                } else {
                                    Text(label)
                                }
                            },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { viewModel.updateShowSortDialog(false) }) {
                Text(stringResource(SharedRes.string.close))
            }
        }
    )
}

@Composable
fun FilterDialog(
    viewModel: CategoriesListViewModel = koinViewModel()
) {
    val resizeSafeDismiss = rememberResizeSafeDismissRequest(
        onDismissRequest = { viewModel.updateShowFilterDialog(false) },
    )

    AlertDialog(
        properties = transparentDialogProperties(),
        onDismissRequest = resizeSafeDismiss,
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // 标题
                Text(stringResource(SharedRes.string.filter))
                SearchableSelectorRemote(
                    config = RemoteSearchableSelectorConfig(
                        label = stringResource(BusinessRes.string.select_parent_category),
                        error = null,
                        leadingIcon = Icons.Outlined.Category,
                        placeholder = stringResource(SharedRes.string.search_parent_category_name),
                        selectedItem = viewModel.filterCategory,
                        onSelectedItemChange = {
                            viewModel.updateFilterCategory(it)
                        },
                        pageSize = 100,
                        itemToString = {
                            "${it.name}${it.translationDisplayText()?.let { str -> " • $str" }.orEmpty()}"
                        },
                        onSearch = { query, page, limit ->
                            viewModel.findCategories(query, page, limit)
                        }
                    )
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { viewModel.updateShowFilterDialog(false) }) {
                Text(stringResource(SharedRes.string.close))
            }
        }
    )
}

@Composable
private fun FilterChipsRow(
    viewModel: CategoriesListViewModel,
    modifier: Modifier = Modifier
) {
    SharedFilterChipsRow(modifier = modifier) {
        viewModel.filterCategory?.let {
            ElevatedFilterChip(
                selected = true,
                onClick = { viewModel.removeFilterCategory() },
                label = { Text(stringResource(BusinessRes.string.parent_category_with_name, it.name)) },
                trailingIcon = { SharedCloseIcon() }
            )
        }
        viewModel.sortBy?.let { sortBy ->
            ElevatedFilterChip(
                selected = true,
                onClick = {
                    viewModel.updateSortDir(
                        if (viewModel.sortDir == OrderDir.ASC) OrderDir.DESC else OrderDir.ASC
                    )
                },
                label = {
                    SharedProductSortDirectionLabel(
                        label = "${stringResource(SharedRes.string.sort)}: ${sortBy.displayName()}",
                        sortDir = viewModel.sortDir
                    )
                },
            )
        }
    }
}
