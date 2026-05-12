package org.dsqrwym.enterprise.ui.screens.categories

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
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
import maian.business.generated.resources.*
import maian.shared.generated.resources.*
import org.dsqrwym.business.ui.components.button.BusinessOutlinedDeleteButton
import org.dsqrwym.business.ui.components.category.BusinessCategoryLanguages
import org.dsqrwym.business.ui.components.category.BusinessCategoryPath
import org.dsqrwym.business.ui.components.category.BusinessConfirmDeleteCategories
import org.dsqrwym.business.ui.components.tooltip.PermissionTooltip
import org.dsqrwym.enterprise.permissions.canManageEnterpriseCategories
import org.dsqrwym.enterprise.ui.viewmodels.categories.CategoriesListViewModel
import org.dsqrwym.shared.data.category.mapper.toDto
import org.dsqrwym.shared.data.user.UserRole
import org.dsqrwym.shared.domain.category.CategoryNode
import org.dsqrwym.shared.paging.hasLoadError
import org.dsqrwym.shared.paging.isAppendingOrPrepending
import org.dsqrwym.shared.paging.isEmptyResult
import org.dsqrwym.shared.paging.isRefreshing
import org.dsqrwym.shared.ui.components.buttons.SharedCloseButton
import org.dsqrwym.shared.ui.components.containers.UiState
import org.dsqrwym.shared.ui.components.icon.SharedCloseIcon
import org.dsqrwym.shared.ui.components.input.selector.RemoteSearchableSelectorConfig
import org.dsqrwym.shared.ui.components.input.selector.SearchableSelectorRemote
import org.dsqrwym.shared.ui.components.placeholder.SharedNotFoundPlaceholder
import org.dsqrwym.shared.ui.components.row.SharedFilterChipsRow
import org.dsqrwym.shared.ui.components.scaffold.SharedTransparentScaffold
import org.dsqrwym.shared.ui.components.scaffold.SharedTransparentScaffoldFabButtonState
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
    val deleteCategory = viewModel.deleteCategory
    val canManageCategories = userRole?.canManageEnterpriseCategories() == true
    val noPermissionText = stringResource(SharedRes.string.error_no_permission)

    SharedTransparentScaffold(
        topBarScrollBehavior = scrollBehavior,
        showOverlayDialog = showFilterDialog || deleteCategory != null,
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
                        placeholder = { Text(stringResource(BusinessRes.string.search_category)) },
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

                        items(lazyPagingItems.itemCount, key = { lazyPagingItems.itemKey { it.id } }) { index ->
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

                    item {
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
fun FilterDialog(
    viewModel: CategoriesListViewModel = koinViewModel()
) {
    AlertDialog(
        properties = transparentDialogProperties(),
        onDismissRequest = { viewModel.updateShowFilterDialog(false) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // 标题
                Text(stringResource(SharedRes.string.filter))
                SearchableSelectorRemote(
                    config = RemoteSearchableSelectorConfig(
                        label = stringResource(BusinessRes.string.select_parent_category),
                        error = null,
                        leadingIcon = Icons.Outlined.Category,
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
    }
}
