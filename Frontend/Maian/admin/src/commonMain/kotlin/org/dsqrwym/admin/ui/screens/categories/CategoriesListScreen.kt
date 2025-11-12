package org.dsqrwym.admin.ui.screens.categories

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.eygraber.compose.placeholder.PlaceholderHighlight
import com.eygraber.compose.placeholder.material3.placeholder
import com.eygraber.compose.placeholder.material3.shimmer
import org.dsqrwym.admin.data.categories.dto.CategoryResponse
import org.dsqrwym.admin.ui.viewmodels.categories.CategoriesListViewmodel
import org.dsqrwym.shared.data.category.SharedCategoryType
import org.dsqrwym.shared.data.category.dto.SharedCategoryTranslation
import org.dsqrwym.shared.ui.components.containers.HazeContainer
import org.dsqrwym.shared.ui.components.dialog.ConfirmDeleteDialog
import org.dsqrwym.shared.ui.components.input.selector.RemoteSearchableSelectorConfig
import org.dsqrwym.shared.ui.components.input.selector.SearchableSelectorRemote
import org.dsqrwym.shared.ui.components.progressindicators.MyCircularProgressIndicator
import org.dsqrwym.shared.util.modifier.paddingTopForMenu
import org.dsqrwym.shared.util.navigation.WindowWidthSizeClass
import org.dsqrwym.shared.util.navigation.calculateWindowSizeClass
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesListScreen(
    viewModel: CategoriesListViewmodel = koinViewModel(),
    onNavigateToCreate: () -> Unit = {},
    onNavigateToEdit: (Long) -> Unit = {}
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val windowWidthSizeClass = calculateWindowSizeClass().widthSizeClass

    val lazyPagingItems = viewModel.pagedCategories.collectAsLazyPagingItems()

    val searchQuery = viewModel.searchQuery
    val showFilterDialog = viewModel.showFilterDialog
    val deleteCategory = viewModel.deleteCategory

    HazeContainer(
        isOverlayVisible = showFilterDialog || deleteCategory != null,
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
        }
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                CategoriesListTopBar(
                    searchQuery = searchQuery,
                    onQueryChange = { viewModel.updateSearchQuery(it) },
                    onSearch = { viewModel.updateSearchQuery(it) },
                    onFilterClick = { viewModel.updateShowFilterDialog(true) },
                    onCreateClick = onNavigateToCreate,
                    scrollBehavior = scrollBehavior,
                    windowWidth = windowWidthSizeClass
                )
            },
            floatingActionButton = {
                if (windowWidthSizeClass != WindowWidthSizeClass.Compact) return@Scaffold
                ExtendedFloatingActionButton(
                    modifier = Modifier,
                    icon = {
                        Icon(Icons.Outlined.Add, "创建类别")
                    },
                    text = { Text("创建类别") }, onClick = onNavigateToCreate
                )
            }
        ) { padding ->
            val density = LocalDensity.current
            var filterFlowRowHeight by remember { mutableStateOf(0.dp) }
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
            ) {
                if (lazyPagingItems.itemCount == 0 && lazyPagingItems.loadState.refresh !is LoadState.Loading) {
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
                                "没有找到类别",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                } else {
                    // 类别列表
                    LazyVerticalGrid(
                        modifier = Modifier
                            .fillMaxSize()
                            .nestedScroll(scrollBehavior.nestedScrollConnection)
                            .padding(horizontal = 16.dp),
                        columns = GridCells.Adaptive(minSize = 380.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
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
                                                onEdit = { onNavigateToEdit(category.id) },
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
                                    item(span = { GridItemSpan(maxLineSpan) }) {
                                        MyCircularProgressIndicator()
                                    }
                                }


                                loadState.append is LoadState.Error || loadState.prepend is LoadState.Error -> {
                                    item(span = { GridItemSpan(maxLineSpan) }) {
                                        Text(
                                            "加载失败，点击重试",
                                            modifier = Modifier.clickable { retry() }
                                        )
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
                    Modifier.padding(horizontal = 16.dp).onGloballyPositioned { coordinates ->
                        filterFlowRowHeight = with(density) { coordinates.size.height.toDp() }
                    }
                )
            }
        }
    }
}

@Composable
fun CategoryListItem(
    modifier: Modifier = Modifier,
    categoriesListViewmodel: CategoriesListViewmodel = koinViewModel(),
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
                    .placeholder(visible = isLoading, highlight = PlaceholderHighlight.shimmer()),
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
                            onClick = {
                                categoriesListViewmodel.updateFilterCategoryType(
                                    if (category.isPublic()) SharedCategoryType.PUBLIC else SharedCategoryType.PRIVATE
                                )
                            },
                            label = {
                                Text(
                                    if (category.isPublic()) "平台" else "私有",
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
                            text = if (category.getParentName() != null) "父类别: ${category.getParentName()}" else "基础类别",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Text(
                            text = "IVA: ${if (category.iva != null) category.iva.toString() + "%" else "未设置"}",
                            style = MaterialTheme.typography.bodySmall
                        )

                        val childrenCount = category.getChildrenCount()
                        if (childrenCount > 0) {
                            Text(
                                text = "$childrenCount 个子类别",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                IconButton(onClick = onEdit) {
                    Icon(Icons.Outlined.Edit, "编辑")
                }

            }

            HorizontalDivider()

            Row(Modifier.fillMaxWidth().placeholder(visible = isLoading, highlight = PlaceholderHighlight.shimmer())) {
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
                    )
                ) {
                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("删除类别")
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
            "路径:",
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
            "其他语言:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        languages.forEach { (langCode, name) ->
            Text(
                "$langCode: $name",
                style = MaterialTheme.typography.bodySmall
            )
        }
        languages.ifEmpty {
            Text(
                "无",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun ConfirmDeleteCategories(
    category: CategoryResponse,
    onDismiss: () -> Unit = {},
    onConfirm: () -> Unit = {},
) {
    val content =
        "确定要删除类别 \"${category.name}\" 吗？${if (category.getChildrenCount() > 0) "\n\n注意：这将同时删除 ${category.getChildrenCount()} 个子类别且" else "这将"} 无法恢复。"
    ConfirmDeleteDialog(
        title = "删除类别",
        text = content,
        onDismissRequest = onDismiss,
        onConfirm = onConfirm,
    )
}


@Composable
fun FilterDialog(
    viewModel: CategoriesListViewmodel = koinViewModel()
) {
    val categoryType = viewModel.filterCategoryType
    val parentCategory by viewModel.parentCategories.collectAsState()
    val wholesalers by viewModel.wholesalers.collectAsState()
    AlertDialog(
        onDismissRequest = { viewModel.updateShowFilterDialog(false) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // 标题
                Text("过滤类别:")
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
                        Text("全部")
                    }

                    // 选项2：平台类别
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = categoryType == SharedCategoryType.PUBLIC,
                            onClick = { viewModel.updateFilterCategoryType(SharedCategoryType.PUBLIC) }
                        )
                        Text("平台类别")
                    }

                    // 选项3：私有类别
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = categoryType == SharedCategoryType.PRIVATE,
                            onClick = { viewModel.updateFilterCategoryType(SharedCategoryType.PRIVATE) }
                        )
                        Text("私有类别")
                    }
                }

                SearchableSelectorRemote(
                    config = RemoteSearchableSelectorConfig(
                        label = "选择父类别",
                        error = null,
                        leadingIcon = Icons.Outlined.Category,
                        items = parentCategory,
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
                        itemId = { it.id.toString() },
                        onSearch = { query, page, limit ->
                            viewModel.findParentCategories(query, page, limit)
                        }
                    )
                )

                AnimatedVisibility(visible = categoryType == SharedCategoryType.PRIVATE) {
                    SearchableSelectorRemote(
                        config = RemoteSearchableSelectorConfig(
                            label = "选择批发商",
                            error = null,
                            leadingIcon = Icons.Outlined.PersonOutline,
                            items = wholesalers,
                            selectedItem = viewModel.filterUser,
                            onSelectedItemChange = {
                                viewModel.updateFilterUser(it)
                            },
                            pageSize = 100,
                            itemToString = {
                                "ID: ${it.userId}, 用户名: ${it.username}"
                            },
                            itemId = { it.userId },
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
        },
        confirmButton = {
            TextButton(onClick = { viewModel.updateShowFilterDialog(false) }) {
                Text("关闭")
            }
        }
    )
}

@Composable
private fun FilterChipsRow(
    viewModel: CategoriesListViewmodel,
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
                label = { Text(it.name) },
                trailingIcon = { Icon(Icons.Outlined.Close, null) }
            )
        }
        viewModel.filterUser?.let {
            ElevatedFilterChip(
                selected = true,
                onClick = { viewModel.removeUserIdFilter() },
                label = { Text("用户: ${it.username}") },
                trailingIcon = { Icon(Icons.Outlined.Close, null) }
            )
        }
        viewModel.filterParentCategory?.let {
            ElevatedFilterChip(
                selected = true,
                onClick = { viewModel.removeParentIdFilter() },
                label = { Text("父类: ${it.name}") },
                trailingIcon = { Icon(Icons.Outlined.Close, null) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesListTopBar(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onFilterClick: () -> Unit,
    onCreateClick: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    windowWidth: WindowWidthSizeClass
) {
    TopAppBar(
        modifier = Modifier.paddingTopForMenu(),
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent,
        ),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                SearchBarDefaults.InputField(
                    modifier = Modifier.weight(0.8f),
                    query = searchQuery,
                    onQueryChange = onQueryChange,
                    onSearch = onSearch,
                    expanded = false,
                    onExpandedChange = {},
                    placeholder = { Text("搜索类别...") },
                    leadingIcon = { Icon(Icons.Outlined.Search, null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onQueryChange("") }) {
                                Icon(Icons.Outlined.Clear, "清除")
                            }
                        }
                    }
                )
                IconButton(onClick = onFilterClick, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Outlined.FilterList, "过滤")
                }
            }
        },
        actions = {
            if (windowWidth != WindowWidthSizeClass.Compact) {
                ExtendedFloatingActionButton(
                    modifier = Modifier.padding(end = 16.dp),
                    icon = { Icon(Icons.Outlined.Add, null) },
                    text = { Text("创建类别") },
                    onClick = onCreateClick
                )
            }
        }
    )
}