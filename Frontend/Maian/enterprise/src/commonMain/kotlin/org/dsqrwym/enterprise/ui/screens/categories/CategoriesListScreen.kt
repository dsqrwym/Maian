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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import maian.business.generated.resources.*
import maian.shared.generated.resources.*
import org.dsqrwym.business.ui.components.button.BusinessOutlinedDeleteButton
import org.dsqrwym.business.ui.components.category.BusinessCategorieLanguages
import org.dsqrwym.business.ui.components.category.BusinessCategoriePath
import org.dsqrwym.business.ui.components.category.BusinessConfirmDeleteCategories
import org.dsqrwym.enterprise.data.category.dto.CategoryResponse
import org.dsqrwym.enterprise.ui.viewmodels.categories.CategoriesListViewModel
import org.dsqrwym.shared.ui.components.buttons.SharedCloseButton
import org.dsqrwym.shared.ui.components.containers.UiState
import org.dsqrwym.shared.ui.components.icon.SharedCloseIcon
import org.dsqrwym.shared.ui.components.input.selector.RemoteSearchableSelectorConfig
import org.dsqrwym.shared.ui.components.input.selector.SearchableSelectorRemote
import org.dsqrwym.shared.ui.components.placeholder.SharedNotFoundPlaceholder
import org.dsqrwym.shared.ui.components.scaffold.SharedTransparentScaffold
import org.dsqrwym.shared.ui.components.scaffold.SharedTransparentScaffoldFabButtonState
import org.dsqrwym.shared.util.colum.SharedColumnLayout
import org.dsqrwym.shared.util.lazygrid.SharedLazyGridLayout
import org.dsqrwym.shared.util.lazygrid.SharedLazyGridLayout.appendErrorRetry
import org.dsqrwym.shared.util.lazygrid.SharedLazyGridLayout.appendLoadingIndicator
import org.dsqrwym.shared.util.modifier.paddingWithoutTop
import org.dsqrwym.shared.util.modifier.placeholderWithShimmer
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesListScreen(
    viewModel: CategoriesListViewModel = koinViewModel(),
    onNavigateToCreate: () -> Unit = {},
    onNavigateToEdit: (String) -> Unit = {}
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val lazyPagingItems = viewModel.pagedCategories.collectAsLazyPagingItems()

    val searchQuery = viewModel.searchQuery
    val showFilterDialog = viewModel.showFilterDialog
    val deleteCategory = viewModel.deleteCategory

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
        },
        fabButtonState = SharedTransparentScaffoldFabButtonState(
            buttonState = UiState.Idle,
            buttonEnabled = true,
            onButtonClick = onNavigateToCreate,
            buttonText = stringResource(SharedRes.string.create),
            buttonIcon = Icons.Outlined.Add,
            buttonIconDescription = stringResource(SharedRes.string.create)
        ),
    )
    { padding, scrollBehavior ->
        val density = LocalDensity.current
        var filterFlowRowHeight by remember { mutableStateOf(0.dp) }
        val isRefreshing = lazyPagingItems.loadState.refresh is LoadState.Loading
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
            if (lazyPagingItems.itemCount == 0 && lazyPagingItems.loadState.isIdle) {
                SharedNotFoundPlaceholder()
            } else {
                // 类别列表
                LazyVerticalStaggeredGrid(
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(scrollBehavior.nestedScrollConnection)
                        .padding(horizontal = SharedLazyGridLayout.Padding),
                    columns = StaggeredGridCells.Adaptive(minSize = 380.dp),
                    horizontalArrangement = SharedLazyGridLayout.arrangement,
                    verticalItemSpacing = SharedLazyGridLayout.verticalItemSpacing,
                ) {
                    item(span = StaggeredGridItemSpan.FullLine) {
                        Spacer(Modifier.height(padding.calculateTopPadding()))
                        Spacer(Modifier.fillMaxWidth().height(filterFlowRowHeight).heightIn(min = 28.dp))
                    }

                    // 加载状态
                    lazyPagingItems.apply {
                        when {
                            loadState.isIdle -> {
                                items(lazyPagingItems.itemCount) { index ->
                                    val category = lazyPagingItems[index]
                                    if (category != null) {
                                        CategoryListItem(
                                            isLoading = viewModel.isLoading,
                                            category = category,
                                            onEdit = { onNavigateToEdit(category.id.toString()) },
                                            onDelete = { viewModel.updateShowDeleteDialog(category) }
                                        )
                                    }
                                }
                            }

                            loadState.refresh is LoadState.Loading -> {
                                repeat(9) {
                                    item {
                                        CategoryListItem(
                                            isLoading = true,
                                            category = CategoryResponse(
                                                id = it.toLong(),
                                                name = "",
                                            ),
                                            onEdit = {},
                                            onDelete = {}
                                        )
                                    }
                                }
                            }

                            loadState.prepend is LoadState.Loading || loadState.append is LoadState.Loading -> {
                                appendLoadingIndicator()
                            }

                            loadState.append is LoadState.Error || loadState.prepend is LoadState.Error -> {
                                appendErrorRetry { retry() }
                            }
                        }
                    }

                    item {
                        Spacer(Modifier.height(28.dp))
                    }
                }
            }
            // 过滤标签
            FilterChipsRow(
                viewModel,
                Modifier.padding(start = 8.dp, end = 8.dp, top = padding.calculateTopPadding())
                    .onGloballyPositioned { coordinates ->
                        filterFlowRowHeight = with(density) { coordinates.size.height.toDp() }
                    }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryListItem(
    modifier: Modifier = Modifier,
    category: CategoryResponse,
    isLoading: Boolean = true,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    OutlinedCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(SharedColumnLayout.padding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = category.name,
                                style = MaterialTheme.typography.titleMedium
                            )
                            AssistChip(
                                onClick = {},
                                label = {
                                    Text(
                                        stringResource(BusinessRes.string.private_category),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (category.getParentName() != null) "${stringResource(BusinessRes.string.parent_category)}: ${category.getParentName()}" else stringResource(
                                    BusinessRes.string.base_category
                                ),
                                style = MaterialTheme.typography.bodySmall
                            )

                            Text(
                                text = "${stringResource(SharedRes.string.tax_rate)}->IVA: ${
                                    if (category.iva != null) category.iva.toString() + "%" else stringResource(
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

                IconButton(onClick = onEdit) {
                    Icon(Icons.Outlined.Edit, stringResource(SharedRes.string.edit))
                }

            }

            HorizontalDivider()

            Row(Modifier.fillMaxWidth().placeholderWithShimmer(isLoading)) {
                SelectionContainer(modifier = Modifier.weight(1f)) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        category.categoryTranslations?.let { BusinessCategorieLanguages(it) }
                        BusinessCategoriePath(category.getPath(), category.name)
                    }
                }
                BusinessOutlinedDeleteButton(
                    enabled = true,
                    onDelete = onDelete
                )
            }
        }
    }
}

@Composable
fun FilterDialog(
    viewModel: CategoriesListViewModel = koinViewModel()
) {
    AlertDialog(
        onDismissRequest = { viewModel.updateShowFilterDialog(false) },
        text = {
            SelectionContainer {
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
                                "${it.name}${it.translationString?.let { str -> " • $str" }.orEmpty()}"
                            },
                            onSearch = { query, page, limit ->
                                viewModel.findCategories(query, page, limit)
                            }
                        )
                    )
                }
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
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        viewModel.filterCategory?.let {
            ElevatedFilterChip(
                selected = true,
                onClick = { viewModel.removeFilterCategory() },
                label = { Text("${stringResource(BusinessRes.string.parent_category)}: ${it.name}") },
                trailingIcon = { SharedCloseIcon() }
            )
        }
    }
}