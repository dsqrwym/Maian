package org.dsqrwym.admin.ui.screens.categories

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import maian.admin.generated.resources.AdminRes
import maian.admin.generated.resources.platform_category
import maian.admin.generated.resources.select_wholesaler
import maian.business.generated.resources.*
import maian.shared.generated.resources.*
import org.dsqrwym.admin.data.categories.dto.CategoryResponse
import org.dsqrwym.admin.ui.viewmodels.categories.CategoriesListViewModel
import org.dsqrwym.business.ui.components.button.BusinessOutlinedDeleteButton
import org.dsqrwym.business.ui.components.category.BusinessCategorieLanguages
import org.dsqrwym.business.ui.components.category.BusinessCategoriePath
import org.dsqrwym.business.ui.components.category.BusinessConfirmDeleteCategories
import org.dsqrwym.shared.data.category.SharedCategoryType
import org.dsqrwym.shared.data.user.UserRole
import org.dsqrwym.shared.ui.components.buttons.SharedRetryButton
import org.dsqrwym.shared.ui.components.containers.UiState
import org.dsqrwym.shared.ui.components.input.selector.RemoteSearchableSelectorConfig
import org.dsqrwym.shared.ui.components.input.selector.SearchableSelectorRemote
import org.dsqrwym.shared.ui.components.progressindicators.SharedCircularProgressIndicator
import org.dsqrwym.shared.ui.components.scaffold.SharedTransparentScaffold
import org.dsqrwym.shared.ui.components.scaffold.SharedTransparentScaffoldFabButtonState
import org.dsqrwym.shared.ui.viewmodels.menu.SharedMenuViewModel
import org.dsqrwym.shared.util.modifier.paddingWithoutTop
import org.dsqrwym.shared.util.modifier.placeholderWithShimmer
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesListScreen(
    viewModel: CategoriesListViewModel = koinViewModel(),
    menuViewModel: SharedMenuViewModel = koinViewModel(),
    onNavigateToCreate: () -> Unit = {},
    onNavigateToEdit: (String) -> Unit = {}
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val userRole = menuViewModel.menuConfiguration?.userRole

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
                                                categoriesListViewmodel = viewModel,
                                                category = category,
                                                userRole = userRole,
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
                                                categoriesListViewmodel = viewModel,
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
                                        SharedCircularProgressIndicator()
                                    }
                                }


                                loadState.append is LoadState.Error || loadState.prepend is LoadState.Error -> {
                                    item(span = StaggeredGridItemSpan.FullLine) {
                                        SharedRetryButton { retry() }
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
    categoriesListViewmodel: CategoriesListViewModel = koinViewModel(),
    category: CategoryResponse,
    userRole: UserRole? = null,
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
                                onClick = {
                                    categoriesListViewmodel.updateFilterCategoryType(
                                        if (category.isPublic()) SharedCategoryType.PUBLIC else SharedCategoryType.PRIVATE
                                    )
                                },
                                label = {
                                    Text(
                                        if (category.isPublic()) stringResource(BusinessRes.string.platform_category) else stringResource(
                                            BusinessRes.string.private_category
                                        ),
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
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
                    tooltip = {
                        if (userRole != UserRole.SUPERADMIN) {
                            PlainTooltip {
                                SelectionContainer {
                                    Text(stringResource(SharedRes.string.error_no_permission))
                                }
                            }
                        }
                    },
                    state = rememberTooltipState()
                ) {
                    BusinessOutlinedDeleteButton(
                        enabled = userRole == UserRole.SUPERADMIN,
                        onDelete = onDelete
                    )
                }
            }
        }
    }
}

@Composable
fun FilterDialog(
    viewModel: CategoriesListViewModel = koinViewModel()
) {
    val categoryType = viewModel.filterCategoryType
    AlertDialog(
        onDismissRequest = { viewModel.updateShowFilterDialog(false) },
        text = {
            SelectionContainer {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // 标题
                    Text(stringResource(SharedRes.string.filter))
                    // 三个单选按钮并排的行
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 选项1：全部
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = categoryType == null,
                                onClick = { viewModel.updateFilterCategoryType(null) }
                            )
                            Text(stringResource(SharedRes.string.all))
                        }

                        // 选项2：平台类别
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = categoryType == SharedCategoryType.PUBLIC,
                                onClick = { viewModel.updateFilterCategoryType(SharedCategoryType.PUBLIC) }
                            )
                            Text(stringResource(BusinessRes.string.platform_category))
                        }

                        // 选项3：私有类别
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = categoryType == SharedCategoryType.PRIVATE,
                                onClick = { viewModel.updateFilterCategoryType(SharedCategoryType.PRIVATE) }
                            )
                            Text(stringResource(BusinessRes.string.private_category))
                        }
                    }

                    SearchableSelectorRemote(
                        config = RemoteSearchableSelectorConfig(
                            label = stringResource(BusinessRes.string.select_parent_category),
                            error = null,
                            leadingIcon = Icons.Outlined.Category,
                            selectedItem = viewModel.filterParentCategory,
                            onSelectedItemChange = {
                                viewModel.updateFilterParentCategory(it)
                            },
                            pageSize = 100,
                            itemToString = {
                                "${it.name}${it.translationString?.let { str -> " • $str" }.orEmpty()}"
                            },
                            onSearch = { query, page, limit ->
                                viewModel.findParentCategories(query, page, limit)
                            }
                        )
                    )

                    AnimatedVisibility(visible = categoryType == SharedCategoryType.PRIVATE) {
                        val usernameLabel = stringResource(SharedRes.string.field_username_label)
                        SearchableSelectorRemote(
                            config = RemoteSearchableSelectorConfig(
                                label = stringResource(AdminRes.string.select_wholesaler),
                                error = null,
                                leadingIcon = Icons.Outlined.PersonOutline,
                                selectedItem = viewModel.filterUser,
                                onSelectedItemChange = {
                                    viewModel.updateFilterUser(it)
                                },
                                pageSize = 100,
                                itemToString = {
                                    "ID: ${it.userId}, ${usernameLabel}: ${it.username}"
                                },
                                semanticsPropertyReceiver = {
                                    contentType = ContentType.Username
                                },
                                onSearch = { query, page, limit ->
                                    viewModel.findWholesalers(query, page, limit)
                                },
                            )
                        )
                    }
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
        viewModel.filterCategoryType?.let {
            ElevatedFilterChip(
                selected = true,
                onClick = { viewModel.removeCategoryTypeFilter() },
                label = {
                    Text(
                        if (it == SharedCategoryType.PUBLIC) stringResource(AdminRes.string.platform_category) else stringResource(
                            BusinessRes.string.private_category
                        )
                    )
                },
                trailingIcon = { Icon(Icons.Outlined.Close, null) }
            )
        }
        viewModel.filterUser?.let {
            ElevatedFilterChip(
                selected = true,
                onClick = { viewModel.removeUserIdFilter() },
                label = { Text("${stringResource(SharedRes.string.user)}: ${it.username}") },
                trailingIcon = { Icon(Icons.Outlined.Close, null) }
            )
        }
        viewModel.filterParentCategory?.let {
            ElevatedFilterChip(
                selected = true,
                onClick = { viewModel.removeParentIdFilter() },
                label = { Text("${stringResource(BusinessRes.string.parent_category)}: ${it.name}") },
                trailingIcon = { Icon(Icons.Outlined.Close, null) }
            )
        }
    }
}