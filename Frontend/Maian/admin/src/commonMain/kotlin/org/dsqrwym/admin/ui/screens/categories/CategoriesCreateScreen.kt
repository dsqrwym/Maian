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
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.field_required
import org.dsqrwym.admin.data.categories.dto.ParentCategoryResponse
import org.dsqrwym.admin.data.user.dto.WholeSalerUserResponse
import org.dsqrwym.admin.ui.viewmodels.categories.CategoriesCreateViewModel
import org.dsqrwym.shared.data.category.dto.SharedCategoryTranslation
import org.dsqrwym.shared.localization.LanguageManager
import org.dsqrwym.shared.ui.components.buttons.MyExtendedFloatingActionButton
import org.dsqrwym.shared.ui.components.cards.FormCard
import org.dsqrwym.shared.ui.components.containers.HazeContainer
import org.dsqrwym.shared.ui.components.containers.UiState
import org.dsqrwym.shared.ui.components.input.outlinetextfields.MyOutlinedTextField
import org.dsqrwym.shared.ui.components.input.selector.RemoteSearchableSelectorConfig
import org.dsqrwym.shared.ui.components.input.selector.SearchableSelectorRemote
import org.dsqrwym.shared.util.formatter.asString
import org.dsqrwym.shared.util.modifier.paddingTopForMenu
import org.dsqrwym.shared.util.navigation.WindowWidthSizeClass
import org.dsqrwym.shared.util.navigation.calculateWindowSizeClass
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryCreateScreen(
    viewModel: CategoriesCreateViewModel = koinViewModel(),
    onNavigateBack: () -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val windowWidthSizeClass = calculateWindowSizeClass().widthSizeClass

    val categoryName = viewModel.categoryName
    val categoryNameError = viewModel.categoryNameError

    val categoryIva = viewModel.categoryIva

    val parentCategory by viewModel.parentCategories.collectAsState()
    val selectedParentCategory = viewModel.filterParentCategory

    val wholesalers by viewModel.wholesalers.collectAsState()
    val selectedWholesaler = viewModel.filterUser

    val translations = viewModel.translations
    val translationsIsValid by viewModel.translationIsValid

    val createStatus = viewModel.createButtonState
    val createEnabled by viewModel.createButtonEnabled
    val cardEnabled by derivedStateOf {
        createStatus != UiState.Loading
    }

    val isPlatformCategory = viewModel.isPlatformCategory

    LaunchedEffect(Unit) {
        viewModel.navigateEvent.collect {
            onNavigateBack()
        }
    }

    HazeContainer(
        viewModel.showAddLanguageDialog,
        {
            AddLanguageDialog(
                availableLanguages = viewModel.getAvailableLanguages(),
                onDismiss = { viewModel.showAddLanguageDialog(false) },
                onAdd = { langCode, _ ->
                    viewModel.upsertTranslation(langCode, "")
                    viewModel.showAddLanguageDialog(false)
                }
            )
        }
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    modifier = Modifier.paddingTopForMenu(),
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent,
                    ),
                    scrollBehavior = scrollBehavior,
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowBack, "返回")
                        }
                    },
                    title = {
                        Row {
                            Icon(Icons.Outlined.Category, "类别")
                            Text("创建类别")
                        }
                    },
                    actions = {
                        if (windowWidthSizeClass == WindowWidthSizeClass.Compact) return@CenterAlignedTopAppBar
                        MyExtendedFloatingActionButton(
                            buttonState = createStatus,
                            enabled = createEnabled,
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
                    buttonState = createStatus,
                    enabled = createEnabled,
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
                    CategoryBasicInfoCard(
                        categoryName,
                        categoryNameError,
                        categoryIva,
                        cardEnabled,
                        viewModel::updateCategoryName,
                        viewModel::updateCategoryIva,
                        viewModel::formatIvaTwoDecimal,
                    )
                }

                // 类别类型
                item {
                    CategoryTypeCard(
                        isPlatformCategory,
                        wholesalers,
                        selectedWholesaler,
                        cardEnabled,
                        viewModel::toggleCategoryType,
                        viewModel::updateFilterUser,
                        viewModel::removeUserIdFilter,
                        viewModel::findWholesalers,
                    )
                }

                // 父类别选择
                item {
                    ParentCategoryCard(
                        parentCategory,
                        selectedParentCategory,
                        cardEnabled,
                        viewModel::updateFilterParentCategory,
                        viewModel::removeParentIdFilter,
                        viewModel::findParentCategories,
                    )
                }

                // 多语言翻译
                item(span = StaggeredGridItemSpan.FullLine) {
                    TranslationCard(
                        translations,
                        translationsIsValid,
                        cardEnabled,
                        viewModel.getAvailableLanguages().isNotEmpty(),
                        viewModel::showAddLanguageDialog,
                        viewModel::removeTranslation,
                        viewModel::upsertTranslation,
                    )
                }
            }
        }
    }
}

@Composable
private fun LanguageTranslationItem(
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester,
    languageCode: String,
    languageName: String,
    translation: String,
    enabled: Boolean,
    onRemove: () -> Unit,
    onImeAction: () -> Unit,
    onEdit: (String) -> Unit
) {
    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
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

            MyOutlinedTextField(
                focusRequester = focusRequester,
                value = translation,
                onValueChange = onEdit,
                placeholderText = "输入 $languageName 翻译...",
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = Icons.Outlined.Translate,
                leadingIconContentDescription = "翻译",
                enabled = enabled,
                imeAction = ImeAction.Next,
                onImeAction = onImeAction,
                keyBordType = KeyboardType.Text,
                error = if (translation.isBlank()) stringResource(SharedRes.string.field_required) else null,
                labelText = languageName,
            )

            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }
        }
    }
}


@Composable
private fun AddLanguageDialog(
    availableLanguages: List<LanguageManager.SupportedLanguages>,
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit
) {
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
                        .fillMaxWidth(),
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

@Composable
private fun SelectedInfoCard(
    visible: Boolean,
    title: String? = null,
    description: String,
    enabled: Boolean = true,
    onClear: (() -> Unit)? = null,
    icon: ImageVector? = null,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(visible = visible) {
        Surface(
            color = containerColor,
            shape = MaterialTheme.shapes.small,
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 可选图标
                    if (icon != null) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = contentColor
                        )
                        Spacer(Modifier.width(8.dp))
                    }

                    Column {
                        title?.let {
                            Text(
                                it,
                                style = MaterialTheme.typography.labelSmall,
                                color = contentColor
                            )
                        }
                        Text(
                            description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = contentColor
                        )
                    }
                }

                if (enabled && onClear != null) {
                    IconButton(
                        onClick = onClear,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Clear,
                            contentDescription = "清除",
                            tint = contentColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryBasicInfoCard(
    categoryName: String,
    categoryNameError: StringResource?,
    categoryIva: String,
    enabled: Boolean,
    onNameChange: (String) -> Unit,
    onIvaChange: (String) -> Unit,
    onIvaBlur: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    FormCard(
        modifier = modifier,
        title = "基本信息",
        subtitle = "类别的名称和税率设置",
        uiState = if (categoryNameError != null) UiState.Error else UiState.Idle,
        enabled = enabled
    ) { isEnabled ->
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            MyOutlinedTextField(
                value = categoryName,
                onValueChange = onNameChange,
                leadingIcon = Icons.Outlined.Category,
                leadingIconContentDescription = "类别",
                labelText = "类别名称 (${stringResource(SharedRes.string.field_required)})",
                placeholderText = "请输入类别名称",
                enabled = isEnabled,
                error = categoryNameError.asString(),
                imeAction = ImeAction.Next,
                onImeAction = { focusManager.moveFocus(FocusDirection.Next) }
            )

            MyOutlinedTextField(
                modifier = Modifier.onFocusChanged {
                    if (!it.isFocused) onIvaBlur()
                },
                leadingIcon = Icons.Outlined.Percent,
                leadingIconContentDescription = "IVA",
                value = categoryIva,
                onValueChange = onIvaChange,
                labelText = "IVA (%)",
                placeholderText = "留空则使用产品的 IVA 设置",
                enabled = isEnabled,
                keyBordType = KeyboardType.Decimal,
                error = null,
                trailingIcon = {
                    if (categoryIva.isNotBlank() && isEnabled) {
                        IconButton(onClick = { onIvaChange("") }) {
                            Icon(Icons.Outlined.Clear, "清除")
                        }
                    }
                },
                imeAction = ImeAction.Next,
                onImeAction = { focusManager.moveFocus(FocusDirection.Next) }
            )
        }
    }
}

@Composable
private fun ParentCategoryCard(
    parentCategories: List<ParentCategoryResponse>, // 替换为实际类型
    selectedParentCategory: ParentCategoryResponse?, // 替换为实际类型
    enabled: Boolean,
    onParentCategoryChange: (ParentCategoryResponse?) -> Unit,
    onRemoveParent: () -> Unit,
    onSearch: suspend (String?, Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    FormCard(
        modifier = modifier,
        title = "父类别",
        subtitle = "选择父类别创建子类别（可选）",
        enabled = enabled
    ) { isEnabled ->
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SearchableSelectorRemote(
                config = RemoteSearchableSelectorConfig(
                    label = "选择父类别",
                    error = null,
                    leadingIcon = Icons.Outlined.Category,
                    items = parentCategories,
                    selectedItem = selectedParentCategory,
                    onSelectedItemChange = onParentCategoryChange,
                    pageSize = 100,
                    itemToString = {
                        "${it.name} ${
                            if (it.translation.isNotEmpty()) ", (" + it.translation.joinToString(", ") { translation ->
                                "${translation.langCode}: ${translation.name}"
                            } + ")" else ""
                        }"
                    },
                    itemId = { it.id.toString() },
                    onSearch = onSearch
                )
            )

            SelectedInfoCard(
                visible = selectedParentCategory != null,
                title = "已选择父类别",
                description = selectedParentCategory?.name ?: "",
                onClear = onRemoveParent,
                enabled = isEnabled
            )
        }
    }
}

@Composable
private fun CategoryTypeCard(
    isPlatformCategory: Boolean,
    wholesalers: List<WholeSalerUserResponse>, // 替换为实际类型
    selectedWholesaler: WholeSalerUserResponse?, // 替换为实际类型
    enabled: Boolean,
    onToggleCategoryType: (Boolean) -> Unit,
    onWholesalerChange: (WholeSalerUserResponse?) -> Unit,
    onRemoveWholesaler: () -> Unit,
    onSearchWholesalers: suspend (String?, Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    FormCard(
        modifier = modifier,
        title = "类别类型",
        subtitle = "设置类别的可见性范围",
        enabled = enabled
    ) { isEnabled ->
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if (isPlatformCategory) "平台类别" else "私有类别",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        if (isPlatformCategory) "对所有用户可见" else "仅指定批发商可见",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isPlatformCategory,
                    onCheckedChange = onToggleCategoryType,
                    enabled = isEnabled
                )
            }

            AnimatedContent(isPlatformCategory) { isPlatform ->
                if (isPlatform) {
                    SelectedInfoCard(
                        visible = true,
                        description = "此类别将对所有用户开放",
                        icon = Icons.Outlined.Public,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        enabled = false
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SearchableSelectorRemote(
                            config = RemoteSearchableSelectorConfig(
                                label = "选择批发商",
                                error = null,
                                leadingIcon = Icons.Outlined.PersonOutline,
                                items = wholesalers,
                                selectedItem = selectedWholesaler,
                                onSelectedItemChange = onWholesalerChange,
                                pageSize = 100,
                                itemToString = {
                                    "ID: ${it.userId}, 用户名: ${it.username}"
                                },
                                itemId = { it.userId },
                                semanticsPropertyReceiver = {
                                    contentType = ContentType.Username
                                },
                                onSearch = onSearchWholesalers,
                            )
                        )

                        SelectedInfoCard(
                            visible = selectedWholesaler != null,
                            title = "已选择批发商",
                            description = "ID: ${selectedWholesaler?.userId}, 用户名: ${selectedWholesaler?.username}",
                            onClear = onRemoveWholesaler,
                            enabled = isEnabled
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TranslationCard(
    translations: SnapshotStateList<SharedCategoryTranslation>,
    translationsIsValid: Boolean,
    enabled: Boolean,
    hasAvailableLanguages: Boolean,
    onShowAddDialog: (Boolean) -> Unit,
    onRemoveTranslation: (String) -> Unit,
    onEditTranslation: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    FormCard(
        modifier = modifier,
        title = "其他语言",
        subtitle = "添加多语言翻译（可选）",
        enabled = enabled
    ) { isEnabled ->
        val addLanguageEnabled by derivedStateOf {
            isEnabled && hasAvailableLanguages && translationsIsValid
        }
        val focusRequester = remember { FocusRequester() }
        Column(
            modifier = Modifier.heightIn(max = 500.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {
                    onShowAddDialog(true)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = addLanguageEnabled
            ) {
                Icon(Icons.Outlined.Add, "添加")
                Spacer(Modifier.width(8.dp))
                Text("添加语言翻译")
            }

            if (translations.isEmpty()) {
                Text(
                    "暂无其他语言翻译",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 260.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                ) {
                    items(translations, key = { it.langCode }) { (langCode, translation) ->
                        LanguageTranslationItem(
                            focusRequester = focusRequester,
                            languageCode = langCode,
                            languageName = LanguageManager.SupportedLanguages
                                .fromCode(langCode).displayName,
                            translation = translation,
                            enabled = isEnabled,
                            onRemove = { onRemoveTranslation(langCode) },
                            onEdit = { newText -> onEditTranslation(langCode, newText) },
                            onImeAction = { if (addLanguageEnabled) onShowAddDialog(true) }
                        )
                    }
                }
            }

            SelectedInfoCard(
                visible = translations.isNotEmpty(),
                description = "已添加 ${translations.size} 种语言翻译",
                icon = Icons.Outlined.Info,
                enabled = false,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}