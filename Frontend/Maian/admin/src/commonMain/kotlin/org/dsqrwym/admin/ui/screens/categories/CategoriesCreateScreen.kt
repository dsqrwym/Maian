package org.dsqrwym.admin.ui.screens.categories

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.field_required
import org.dsqrwym.admin.data.categories.model.getFakeCategories
import org.dsqrwym.admin.ui.viewmodels.categories.CategoriesCreateEditViewModel
import org.dsqrwym.shared.localization.LanguageManager
import org.dsqrwym.shared.ui.components.buttons.MyExtendedFloatingActionButton
import org.dsqrwym.shared.ui.components.cards.FormCard
import org.dsqrwym.shared.ui.components.containers.HazeContainer
import org.dsqrwym.shared.ui.components.containers.UiState
import org.dsqrwym.shared.ui.components.input.outlinetextfields.MyOutlinedTextField
import org.dsqrwym.shared.ui.components.input.selector.SearchableSelector
import org.dsqrwym.shared.ui.components.input.selector.SearchableSelectorDefaults
import org.dsqrwym.shared.util.formatter.asString
import org.dsqrwym.shared.util.modifier.paddingTopForMenu
import org.dsqrwym.shared.util.navigation.WindowWidthSizeClass
import org.dsqrwym.shared.util.navigation.calculateWindowSizeClass
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

// 模拟批发商数据
data class WholesalerDto(
    val id: String,
    val name: String,
    val email: String = "example@gmail.com"
)

private val mockWholesalers = listOf(
    WholesalerDto("ws-001", "Madrid Wholesale Co."),
    WholesalerDto("ws-002", "Barcelona Trading Ltd."),
    WholesalerDto("ws-003", "Valencia Distribution"),
    WholesalerDto("ws-004", "Sevilla Imports"),
    WholesalerDto("ws-005", "Bilbao Suppliers")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryCreateScreen(
    viewModel: CategoriesCreateEditViewModel = koinViewModel(),
    onNavigateBack: () -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val windowWidthSizeClass = calculateWindowSizeClass().widthSizeClass
    val focusManager = LocalFocusManager.current

    val categoryName = viewModel.categoryName
    val translations = viewModel.translations

    HazeContainer(
        viewModel.showAddLanguageDialog,
        {
            AddLanguageDialog(
                existingLanguages = viewModel.translations.keys.toList(),
                onDismiss = { viewModel.showAddLanguageDialog(false) },
                onAdd = { langCode, _ ->
                    viewModel.addTranslation(langCode)
                    viewModel.showAddLanguageDialog(false)
                }
            )
        }
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    modifier = Modifier.paddingTopForMenu(),
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    ),
                    scrollBehavior = scrollBehavior,
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowBack, "返回")
                        }
                    },
                    title = {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.Category, "类别")
                        }
                    },
                    actions = {
                        if (windowWidthSizeClass == WindowWidthSizeClass.Compact) return@TopAppBar
                        MyExtendedFloatingActionButton(
                            buttonState = viewModel.createButtonState,
                            modifier = Modifier.padding(end = 16.dp),
                            icon = {
                                Icon(Icons.Outlined.Add, "创建类别")
                            },
                            text = { Text("创建类别") }, onClick = { viewModel.createCategory() }
                        )
                    }
                )
            },
            floatingActionButton = {
                if (windowWidthSizeClass != WindowWidthSizeClass.Compact) return@Scaffold
                MyExtendedFloatingActionButton(
                    buttonState = viewModel.createButtonState,
                    modifier = Modifier.padding(end = 16.dp),
                    icon = {
                        Icon(Icons.Outlined.Add, "创建类别")
                    },
                    text = { Text("创建类别") }, onClick = { viewModel.createCategory() }
                )
            }
        ) { padding ->
            LazyVerticalStaggeredGrid(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                columns = StaggeredGridCells.Adaptive(minSize = 360.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 基本信息卡片
                item {
                    FormCard(
                        title = "基本信息",
                        subtitle = "类别的名称和税率设置",
                        uiState = if (viewModel.nameError) UiState.Error else UiState.Idle,
                        enabled = viewModel.createButtonState != UiState.Loading
                    ) { enabled ->
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            MyOutlinedTextField(
                                value = categoryName,
                                onValueChange = {
                                    viewModel.updateCategoryName(it)
                                },
                                leadingIcon = Icons.Outlined.Category,
                                leadingIconContentDescription = "类别",
                                labelText = "类别名称 (${stringResource(SharedRes.string.field_required)})",
                                placeholderText = "请输入类别名称",
                                enabled = enabled,
                                error = viewModel.categoryNameError.asString(),
                                imeAction = ImeAction.Next,
                                onImeAction = {
                                    focusManager.moveFocus(FocusDirection.Next)
                                }
                            )

                            MyOutlinedTextField(
                                modifier = Modifier.onFocusChanged {
                                    if (!it.isFocused) {
                                        viewModel.formatIvaTwoDecimal()
                                    }
                                },
                                leadingIcon = Icons.Outlined.Percent,
                                leadingIconContentDescription = "IVA",
                                value = viewModel.categoryIva,
                                onValueChange = { viewModel.updateCategoryIva(it) },
                                labelText = "IVA (%)",
                                placeholderText = "留空则使用产品的 IVA 设置",
                                enabled = enabled,
                                keyBordType = KeyboardType.Decimal,
                                error = null,
                                trailingIcon = {
                                    if (viewModel.categoryIva.isNotBlank() && enabled) {
                                        IconButton(onClick = { viewModel.updateCategoryIva("") }) {
                                            Icon(Icons.Outlined.Clear, "清除")
                                        }
                                    }
                                },
                                imeAction = ImeAction.Next,
                                onImeAction = {
                                    focusManager.moveFocus(FocusDirection.Next)
                                }
                            )
                        }
                    }
                }

                // 父类别选择
                item {
                    FormCard(
                        title = "父类别",
                        subtitle = "选择父类别创建子类别（可选）",
                        uiState = UiState.Idle,
                        enabled = viewModel.createButtonState != UiState.Loading
                    ) { enabled ->
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            SearchableSelector(
                                items = getFakeCategories(),
                                itemToString = { it.name },
                                itemId = { it.id.toString() },
                                config = SearchableSelectorDefaults(
                                    label = "选择父类别",
                                    placeholder = "搜索类别...",
                                    enabled = enabled,
                                    selectedItemId = viewModel.selectedParentId?.toString(),
                                    onSelectedItemIdChange = {
                                        viewModel.selectParentCategory(it?.toLongOrNull())
                                    },
                                    leadingIcon = Icons.Outlined.Category,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            )

                            AnimatedVisibility(visible = viewModel.selectedParentId != null) {
                                val parentCategory = getFakeCategories()
                                    .find { it.id == viewModel.selectedParentId }
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                "已选择父类别",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                            Text(
                                                parentCategory?.name ?: "",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        }
                                        if (enabled) {
                                            IconButton(
                                                onClick = { viewModel.clearParentCategory() },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Clear,
                                                    "清除",
                                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 类别类型
                item {
                    FormCard(
                        title = "类别类型",
                        subtitle = "设置类别的可见性范围",
                        uiState = if (viewModel.wholesalerError) UiState.Error else UiState.Idle,
                        enabled = viewModel.createButtonState != UiState.Loading
                    ) { enabled ->
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        if (viewModel.isPlatformCategory) "平台类别" else "私有类别",
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        if (viewModel.isPlatformCategory) "对所有用户可见" else "仅指定批发商可见",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = viewModel.isPlatformCategory,
                                    onCheckedChange = { viewModel.toggleCategoryType(it) },
                                    enabled = enabled
                                )
                            }

                            // 批发商选择器（仅在私有类别时显示）
                            AnimatedContent(viewModel.isPlatformCategory) { isPlatform ->
                                if (isPlatform) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = MaterialTheme.shapes.small
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.Public,
                                                null,
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                "此类别将对所有用户开放",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                } else {
                                    SearchableSelector(
                                        items = mockWholesalers,
                                        itemToString = { it.name },
                                        itemId = { it.id },
                                        config = SearchableSelectorDefaults(
                                            label = "选择批发商 *",
                                            placeholder = "搜索批发商...",
                                            enabled = enabled,
                                            selectedItemId = viewModel.selectedWholesalerId,
                                            onSelectedItemIdChange = { viewModel.selectWholesaler(it) },
                                            leadingIcon = Icons.Default.Business,
                                            error = if (viewModel.wholesalerError) "请选择批发商" else null,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // 多语言翻译
                item {
                    FormCard(
                        title = "其他语言",
                        subtitle = "添加多语言翻译（可选）",
                        uiState = UiState.Idle,
                        enabled = viewModel.createButtonState != UiState.Loading
                    ) { enabled ->
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // 添加语言按钮
                            OutlinedButton(
                                onClick = { viewModel.showAddLanguageDialog(true) },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = enabled && viewModel.getAvailableLanguages().isNotEmpty()
                            ) {
                                Icon(Icons.Outlined.Add, "添加")
                                Spacer(Modifier.width(8.dp))
                                Text("添加语言翻译")
                            }

                            // 已添加的翻译列表
                            if (translations.isEmpty()) {
                                Text(
                                    "暂无其他语言翻译",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            } else {
                                LazyVerticalGrid(
                                    columns = GridCells.Adaptive(minSize = 180.dp),
                                ) {
                                    items(items = translations.toList(), key = { it.first }) { (langCode, translation) ->
                                        LanguageTranslationItem(
                                            languageCode = langCode,
                                            languageName = LanguageManager.SupportedLanguages.fromCode(langCode)
                                                .displayName,
                                            translation = translation,
                                            enabled = enabled,
                                            onRemove = { viewModel.removeTranslation(langCode) },
                                            onEdit = { newText ->
                                                viewModel.updateTranslation(langCode, newText)
                                            }
                                        )
                                    }
                                }
                            }

                            // 提示信息
                            if (translations.isNotEmpty()) {
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Outlined.Info,
                                            null,
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            "已添加 ${viewModel.translations.size} 种语言翻译",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LanguageTranslationItem(
    languageCode: String,
    languageName: String,
    translation: String,
    enabled: Boolean,
    onRemove: () -> Unit,
    onEdit: (String) -> Unit
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Language,
                        null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "$languageName ($languageCode)",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                if (enabled) {
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Delete,
                            "删除",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            OutlinedTextField(
                value = translation,
                onValueChange = onEdit,
                placeholder = { Text("输入 $languageName 翻译...") },
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Text
                )
            )
        }
    }
}


@Composable
private fun AddLanguageDialog(
    existingLanguages: List<String>,
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit
) {
    val availableLanguages = LanguageManager.SupportedLanguages.entries
        .filter { it.code !in existingLanguages }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Outlined.Language, contentDescription = "Language") },
        title = { Text("添加语言翻译") },
        text = {
            if (availableLanguages.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "已添加所有支持的语言",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp), // 限制对话框高度
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(availableLanguages) { language ->
                        Surface(
                            onClick = { onAdd(language.code, language.displayName) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.small,
                            tonalElevation = 1.dp
                        ) {
                            ListItem(
                                headlineContent = { Text(language.displayName) },
                                supportingContent = { Text(language.code) },
                                leadingContent = {
                                    Icon(
                                        Icons.Outlined.Language,
                                        contentDescription = null
                                    )
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(if (availableLanguages.isEmpty()) "关闭" else "取消")
            }
        }
    )
}