package org.dsqrwym.enterprise.ui.screens.categories

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import maian.enterprise.generated.resources.*
import maian.shared.generated.resources.*
import org.dsqrwym.enterprise.data.category.dto.CategoryResponse
import org.dsqrwym.enterprise.ui.viewmodels.categories.CategoriesListViewModel
import org.dsqrwym.shared.data.category.dto.SharedCategoryTranslation
import org.dsqrwym.shared.ui.components.buttons.SharedRetryButton
import org.dsqrwym.shared.ui.components.containers.UiState
import org.dsqrwym.shared.ui.components.dialog.ConfirmDeleteDialog
import org.dsqrwym.shared.ui.components.input.selector.RemoteSearchableSelectorConfig
import org.dsqrwym.shared.ui.components.input.selector.SearchableSelectorRemote
import org.dsqrwym.shared.ui.components.progressindicators.MyCircularProgressIndicator
import org.dsqrwym.shared.ui.components.scaffold.SharedTransparentScaffold
import org.dsqrwym.shared.ui.components.scaffold.SharedTransparentScaffoldFabButtonState
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
                ConfirmDeleteCategories(
                    category = category,
                    onDismiss = {
                        viewModel.updateShowDeleteDialog(null)
                    },
                    onConfirm = {
                        viewModel.deleteCategory(category)
                    }
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
                    onSearch = viewModel::updateSearchQuery,
                    expanded = false,
                    onExpandedChange = {},
                    placeholder = { Text(stringResource(EnterpriseRes.string.search_category)) },
                    leadingIcon = { Icon(Icons.Outlined.Search, null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(Icons.Outlined.Clear, stringResource(SharedRes.string.clear))
                            }
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
            Box {
                if (lazyPagingItems.itemCount == 0 && lazyPagingItems.loadState.isIdle) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Outlined.Category,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                stringResource(SharedRes.string.not_found),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                } else {
                    // 类别列表
                    LazyVerticalStaggeredGrid(
                        modifier = Modifier
                            .fillMaxSize()
                            .nestedScroll(scrollBehavior.nestedScrollConnection)
                            .padding(horizontal = 16.dp),
                        columns = StaggeredGridCells.Adaptive(minSize = 380.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalItemSpacing = 12.dp,
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
                                    item(span = StaggeredGridItemSpan.FullLine) {
                                        MyCircularProgressIndicator()
                                    }
                                }


                                loadState.append is LoadState.Error || loadState.prepend is LoadState.Error -> {
                                    item(span = StaggeredGridItemSpan.FullLine) {
                                       SharedRetryButton(retry = { retry() })
                                    }
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
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .placeholderWithShimmer(isLoading),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
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
                                    stringResource(EnterpriseRes.string.private_category),
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
                            text = if (category.getParentName() != null) "${stringResource(EnterpriseRes.string.parent_category)}: ${category.getParentName()}" else stringResource(
                                EnterpriseRes.string.base_category
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
                                text = stringResource(EnterpriseRes.string.subcategories_count, childrenCount),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                IconButton(onClick = onEdit) {
                    Icon(Icons.Outlined.Edit, stringResource(SharedRes.string.edit))
                }

            }

            HorizontalDivider()

            Row(Modifier.fillMaxWidth().placeholderWithShimmer(isLoading)) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    category.categoryTranslations?.let { CategorieLanguages(it) }
                    CategoriePath(category.getPath(), category.name)
                }
                OutlinedButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    enabled = true
                ) {
                    Icon(
                        Icons.Default.Delete,
                        stringResource(SharedRes.string.delete),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(SharedRes.string.delete))
                }
            }
        }
    }
}

@Composable
fun CategoriePath(path: List<String>, categoryName: String) {
    if (path.size < 2) return
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "${stringResource(SharedRes.string.path)}: ",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        path.forEach {
            Text(
                it,
                color = if (it != categoryName) Color.Unspecified else MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun CategorieLanguages(languages: List<SharedCategoryTranslation>) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "${stringResource(EnterpriseRes.string.other_languages)}:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        if (languages.isEmpty()) {
            Text(" -- ", style = MaterialTheme.typography.bodySmall)
        } else {
            languages.forEachIndexed { index, (langCode, name) ->
                Text(
                    "$langCode: $name",
                    style = MaterialTheme.typography.bodySmall
                )

                if (index != languages.lastIndex) {
                    Text(
                        "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ConfirmDeleteCategories(
    category: CategoryResponse,
    onDismiss: () -> Unit = {},
    onConfirm: () -> Unit = {},
) {
    val childrenCount = category.childrenCount
    val content = """${stringResource(EnterpriseRes.string.confirm_delete_category, category.name)}
        ${
        if (childrenCount > 0) stringResource(
            EnterpriseRes.string.delete_warning_with_children,
            childrenCount
        ) else ""
    }
    """.trimIndent()
    ConfirmDeleteDialog(
        title = stringResource(SharedRes.string.delete),
        text = content,
        onDismissRequest = onDismiss,
        onConfirm = onConfirm,
    )
}


@Composable
fun FilterDialog(
    viewModel: CategoriesListViewModel = koinViewModel()
) {
    AlertDialog(
        onDismissRequest = { viewModel.updateShowFilterDialog(false) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // 标题
                Text(stringResource(SharedRes.string.filter))

                SearchableSelectorRemote(
                    config = RemoteSearchableSelectorConfig(
                        label = stringResource(EnterpriseRes.string.select_parent_category),
                        error = null,
                        leadingIcon = Icons.Outlined.Category,
                        selectedItem = viewModel.filterParentCategory,
                        onSelectedItemChange = {
                            viewModel.updateFilterParentCategory(it)
                        },
                        pageSize = 100,
                        itemToString = {
                            "${it.name} ${
                                if (it.translation.isNotEmpty()) ", (" + it.translation.joinToString(", ") { translation ->
                                    "${translation.langCode}: ${translation.name}"
                                } + ")" else ""
                            }"
                        },
                        onSearch = { query, page, limit ->
                            viewModel.findParentCategories(query, page, limit)
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
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        viewModel.filterParentCategory?.let {
            ElevatedFilterChip(
                selected = true,
                onClick = { viewModel.removeParentIdFilter() },
                label = { Text("${stringResource(EnterpriseRes.string.parent_category)}: ${it.name}") },
                trailingIcon = { Icon(Icons.Outlined.Close, null) }
            )
        }
    }
}