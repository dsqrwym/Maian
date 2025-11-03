package org.dsqrwym.admin.ui.screens.categories

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import org.dsqrwym.admin.data.categories.model.CategoryData
import org.dsqrwym.admin.ui.viewmodels.categories.CategoriesListViewmodel
import org.dsqrwym.admin.ui.viewmodels.categories.CategoriesType
import org.dsqrwym.shared.ui.components.containers.HazeContainer
import org.dsqrwym.shared.ui.components.dialog.ConfirmDeleteDialog
import org.dsqrwym.shared.util.modifier.paddingTopForMenu
import org.dsqrwym.shared.util.navigation.WindowWidthSizeClass
import org.dsqrwym.shared.util.navigation.calculateWindowSizeClass
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesListScreen(
    categoriesListViewmodel: CategoriesListViewmodel = koinViewModel(),
    onNavigateToCreate: () -> Unit = {},
    onNavigateToEdit: (Long) -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    val searchBarState = rememberSearchBarState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val windowWidthSizeClass = calculateWindowSizeClass().widthSizeClass

    val categories = categoriesListViewmodel.filteredCategories
    val searchQuery = categoriesListViewmodel.searchQuery
    val showFilterDialog = categoriesListViewmodel.showFilterDialog
    val deleteCategory = categoriesListViewmodel.deleteCategory

    HazeContainer(
        isOverlayVisible = showFilterDialog || deleteCategory != null,
        overlayContent = {
            deleteCategory?.let { category ->
                ConfirmDeleteCategories(
                    category = category,
                    onDismiss = {
                        categoriesListViewmodel.updateShowDeleteDialog(null)
                    },
                    onConfirm = {
                        categoriesListViewmodel.deleteCategory(category)
                    }
                )
            }
            if (showFilterDialog) {
                FilterDialog(categoriesListViewmodel)
            }
        }
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    modifier = Modifier.paddingTopForMenu(),
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                    ),
                    scrollBehavior = scrollBehavior,
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            SearchBar(
                                state = searchBarState,
                                inputField = {
                                    SearchBarDefaults.InputField(
                                        query = searchQuery,
                                        onQueryChange = { categoriesListViewmodel.updateSearchQuery(it) },
                                        onSearch = {
                                            categoriesListViewmodel.updateSearchQuery(it)
                                            focusManager.clearFocus()
                                        },
                                        expanded = false,
                                        onExpandedChange = {},
                                        placeholder = { Text("搜索类别...") },
                                        leadingIcon = { Icon(Icons.Outlined.Search, null) },
                                        trailingIcon = {
                                            if (searchQuery.isNotEmpty()) {
                                                IconButton(onClick = { categoriesListViewmodel.updateSearchQuery("") }) {
                                                    Icon(Icons.Outlined.Clear, "清除")
                                                }
                                            }
                                        }
                                    )
                                },
                            )
                            IconButton(
                                onClick = { categoriesListViewmodel.updateShowFilterDialog(true) },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(Icons.Outlined.FilterList, "过滤")
                            }
                        }
                    },
                    actions = {
                        if (windowWidthSizeClass == WindowWidthSizeClass.Compact) return@TopAppBar
                        ExtendedFloatingActionButton(
                            modifier = Modifier.padding(end = 16.dp),
                            icon = {
                                Icon(Icons.Outlined.Add, "创建类别")
                            },
                            text = { Text("创建类别") }, onClick = onNavigateToCreate
                        )
                    }
                )
            },
            floatingActionButton = {
                if (windowWidthSizeClass != WindowWidthSizeClass.Compact) return@Scaffold
                ExtendedFloatingActionButton(
                    icon = {
                        Icon(Icons.Outlined.Add, "创建类别")
                    },
                    text = { Text("创建类别") }, onClick = onNavigateToCreate
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // 过滤标签
                FlowRow(Modifier.fillMaxWidth(0.8f)) {
                    categoriesListViewmodel.filterCategoryType?.let { type ->
                        FilterChip(
                            selected = true,
                            onClick = { categoriesListViewmodel.removeCategoryTypeFilter(type) },
                            label = {
                                Text(type.name)
                            },
                            trailingIcon = {
                                Icon(Icons.Outlined.Close, "移除过滤") },
                            )
                    }
                }
                // 类别列表
                if (categories.isEmpty()) {
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
                    LazyVerticalGrid(
                        modifier = Modifier
                            .fillMaxSize()
                            .nestedScroll(scrollBehavior.nestedScrollConnection)
                            .padding(PaddingValues(16.dp)),
                        columns = GridCells.Adaptive(minSize = 380.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(items = categories, key = { it.id }) { category ->
                            CategoryListItem(
                                categoriesListViewmodel = categoriesListViewmodel,
                                category = category,
                                onEdit = { onNavigateToEdit(category.id) },
                                onDelete = { categoriesListViewmodel.updateShowDeleteDialog(category) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryListItem(
    categoriesListViewmodel: CategoriesListViewmodel = koinViewModel(),
    category: CategoryData,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                                    if (category.isPublic()) CategoriesType.PUBLIC else CategoriesType.PRIVATE
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
                        if (category.getParentName() != null) {
                            Text(
                                text = "父类别: ${category.getParentName()}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        if (category.iva != null) {
                            Text(
                                text = "IVA: ${category.iva}%",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

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

            Row(Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    category.lang?.let { CategorieLanguages(it) }
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
fun CategorieLanguages(languages: Map<String, String>) {
    if (languages.isEmpty()) return
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "其他语言:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        languages.forEach { (lang, name) ->
            Text(
                "$lang: $name",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun ConfirmDeleteCategories(
    category: CategoryData,
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
fun FilterOption(
    selected: Boolean,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(Modifier.width(8.dp))
        Text(label, modifier = Modifier.weight(1f))
    }
}

@Composable
fun FilterDialog(
    categoriesListViewmodel: CategoriesListViewmodel = koinViewModel()
) {
    AlertDialog(
        onDismissRequest = { categoriesListViewmodel.updateShowFilterDialog(false) },
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
                            selected = categoriesListViewmodel.filterCategoryType == null,
                            onClick = { categoriesListViewmodel.updateFilterCategoryType(null) }
                        )
                        Text("全部")
                    }

                    // 选项2：平台类别
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = categoriesListViewmodel.filterCategoryType == CategoriesType.PUBLIC,
                            onClick = { categoriesListViewmodel.updateFilterCategoryType(CategoriesType.PUBLIC) }
                        )
                        Text("平台类别")
                    }

                    // 选项3：私有类别
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = categoriesListViewmodel.filterCategoryType == CategoriesType.PRIVATE,
                            onClick = { categoriesListViewmodel.updateFilterCategoryType(CategoriesType.PRIVATE) }
                        )
                        Text("私有类别")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { categoriesListViewmodel.updateShowFilterDialog(false) }) {
                Text("关闭")
            }
        }
    )
}