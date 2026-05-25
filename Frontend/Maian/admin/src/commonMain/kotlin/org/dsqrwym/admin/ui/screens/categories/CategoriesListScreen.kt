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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import maian.admin.generated.resources.AdminRes
import maian.admin.generated.resources.platform_category
import maian.admin.generated.resources.search_wholesaler_user
import maian.admin.generated.resources.select_wholesaler
import maian.business.generated.resources.*
import maian.shared.generated.resources.*
import org.dsqrwym.admin.permissions.canDeleteAdminCategory
import org.dsqrwym.admin.ui.viewmodels.categories.CategoriesListViewModel
import org.dsqrwym.business.ui.components.button.BusinessOutlinedDeleteButton
import org.dsqrwym.business.ui.components.category.BusinessCategoryLanguages
import org.dsqrwym.business.ui.components.category.BusinessCategoryPath
import org.dsqrwym.business.ui.components.category.BusinessConfirmDeleteCategories
import org.dsqrwym.shared.data.category.SharedCategoryType
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
import org.dsqrwym.shared.ui.components.input.SharedSingleLinePlaceholderText
import org.dsqrwym.shared.ui.components.input.selector.RemoteSearchableSelectorConfig
import org.dsqrwym.shared.ui.components.input.selector.SearchableSelectorRemote
import org.dsqrwym.shared.ui.components.placeholder.SharedNotFoundPlaceholder
import org.dsqrwym.shared.ui.components.row.SharedFilterChipsRow
import org.dsqrwym.shared.ui.components.scaffold.SharedTransparentScaffold
import org.dsqrwym.shared.ui.components.scaffold.SharedTransparentScaffoldFabButtonState
import org.dsqrwym.shared.ui.overlay.rememberResizeSafeDismissRequest
import org.dsqrwym.shared.ui.overlay.transparentDialogProperties
import org.dsqrwym.shared.ui.viewmodels.menu.SharedMenuViewModel
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
                                SharedCloseButton {
                                    viewModel.updateSearchQuery("")
                                }
                            }
                        }
                    )
                    IconButton(
                        onClick = { viewModel.updateShowFilterDialog(true) },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Outlined.FilterList, stringResource(SharedRes.string.filter))
                    }
                }
                // 过滤标签
                FilterChipsRow(viewModel)
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
                    modifier = Modifier.padding(top = padding.calculateTopPadding())
                        .align(Alignment.TopCenter),
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
                            viewModel.isLoading || lazyPagingItems.isRefreshing

                        items(
                            lazyPagingItems.itemCount,
                            key = lazyPagingItems.itemKey { "category-key${it.id}" }) { index ->
                            lazyPagingItems[index]?.let {
                                CategoryListItem(
                                    modifier = Modifier.animateItem(),
                                    isLoading = isCategoryLoading,
                                    categoriesListViewmodel = viewModel,
                                    category = it,
                                    userRole = userRole,
                                    onEdit = { onNavigateToEdit(it.id.toString()) },
                                    onDelete = { viewModel.updateShowDeleteDialog(it) }
                                )
                            }
                        }
                    }

                    if (lazyPagingItems.isAppendingOrPrepending) {
                        appendLoadingIndicator()
                    }

                    item(span = StaggeredGridItemSpan.FullLine) {
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
    categoriesListViewmodel: CategoriesListViewModel = koinViewModel(),
    category: CategoryNode,
    userRole: UserRole? = null,
    isLoading: Boolean = true,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val canDeleteCategory = userRole?.canDeleteAdminCategory() == true
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
                                        if (category.isPublic) SharedCategoryType.PUBLIC else SharedCategoryType.PRIVATE
                                    )
                                },
                                label = {
                                    Text(
                                        if (category.isPublic) stringResource(BusinessRes.string.platform_category) else stringResource(
                                            BusinessRes.string.private_category
                                        ),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            )
                        }

                        FlowRow(
                            verticalArrangement = Arrangement.Center,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                                    text = stringResource(
                                        BusinessRes.string.subcategories_count,
                                        childrenCount
                                    ),
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
                    Column(verticalArrangement = SharedColumnLayout.arrangement) {
                        BusinessCategoryLanguages(category.translations.map { it.toDto() })
                        BusinessCategoryPath(category.pathNames(), category.name)
                    }
                }
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                        TooltipAnchorPosition.Above
                    ),
                    tooltip = {
                        if (!canDeleteCategory) {
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
                        modifier = Modifier.align(Alignment.Bottom),
                        enabled = canDeleteCategory,
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
    val resizeSafeDismiss = rememberResizeSafeDismissRequest(
        onDismissRequest = { viewModel.updateShowFilterDialog(false) },
    )

    AlertDialog(
        properties = transparentDialogProperties(),
        onDismissRequest = resizeSafeDismiss,
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
                            placeholder = stringResource(SharedRes.string.search_parent_category_name),
                            selectedItem = viewModel.filterParentCategory,
                            onSelectedItemChange = {
                                viewModel.updateFilterParentCategory(it)
                            },
                            pageSize = 100,
                            itemToString = {
                                "${it.name}${
                                    it.translationDisplayText()?.let { str -> " • $str" }.orEmpty()
                                }"
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
                                placeholder = stringResource(AdminRes.string.search_wholesaler_user),
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
    SharedFilterChipsRow(
        modifier = modifier,
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
                trailingIcon = { SharedCloseIcon() }
            )
        }
        viewModel.filterUser?.let {
            ElevatedFilterChip(
                selected = true,
                onClick = { viewModel.removeUserIdFilter() },
                label = { Text("${stringResource(SharedRes.string.user)}: ${it.username}") },
                trailingIcon = { SharedCloseIcon() }
            )
        }
        viewModel.filterParentCategory?.let {
            ElevatedFilterChip(
                selected = true,
                onClick = { viewModel.removeParentIdFilter() },
                label = {
                    Text(
                        stringResource(
                            BusinessRes.string.parent_category_with_name,
                            it.name
                        )
                    )
                },
                trailingIcon = { SharedCloseIcon() }
            )
        }
    }
}
