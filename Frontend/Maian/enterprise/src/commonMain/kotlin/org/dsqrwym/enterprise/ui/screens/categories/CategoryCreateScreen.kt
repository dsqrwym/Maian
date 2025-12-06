package org.dsqrwym.enterprise.ui.screens.categories

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
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import maian.enterprise.generated.resources.*
import maian.shared.generated.resources.*
import org.dsqrwym.enterprise.ui.viewmodels.categories.CategoriesCreateViewModel
import org.dsqrwym.shared.data.category.dto.ReducedCategoryResponse
import org.dsqrwym.shared.data.category.dto.SharedCategoryTranslation
import org.dsqrwym.shared.localization.LanguageManager
import org.dsqrwym.shared.navigation.core.NavigationEvent
import org.dsqrwym.shared.ui.components.cards.FormCard
import org.dsqrwym.shared.ui.components.containers.UiState
import org.dsqrwym.shared.ui.components.input.outlinetextfields.MyOutlinedTextField
import org.dsqrwym.shared.ui.components.input.selector.RemoteSearchableSelectorConfig
import org.dsqrwym.shared.ui.components.input.selector.SearchableSelectorRemote
import org.dsqrwym.shared.ui.components.progressindicators.CheckingTrailingIcon
import org.dsqrwym.shared.ui.components.scaffold.SharedTransparentScaffold
import org.dsqrwym.shared.ui.components.scaffold.SharedTransparentScaffoldFabButtonState
import org.dsqrwym.shared.util.formatter.asString
import org.dsqrwym.shared.util.modifier.paddingWithoutTop
import org.dsqrwym.shared.util.modifier.placeholderWithShimmer
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryCreateScreen(
    viewModel: CategoriesCreateViewModel = koinViewModel(),
    onNavigate: (Any) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val categoryName = viewModel.categoryName
    val categoryNameError = viewModel.categoryNameError
    val isCheckingCategoryName = viewModel.isCheckingCategoryName

    val categoryIva = viewModel.categoryIva

    val selectedParentCategory = viewModel.filterParentCategory

    val translations = viewModel.translations
    val translationsIsValid by viewModel.translationIsValid

    val createStatus = viewModel.createButtonState
    val createEnabled by viewModel.createButtonEnabled
    val cardEnabled by derivedStateOf {
        createStatus != UiState.Loading
    }

    LaunchedEffect(Unit) {
        viewModel.navigateEvent.collect {
            if (it is NavigationEvent.ToRoute<*>) {
                onNavigate(it.route)
            }
        }
    }

    SharedTransparentScaffold(
        onNavigateBack = onNavigateBack,
        showOverlayDialog = viewModel.showAddLanguageDialog,
        overlayContent = {
            AddLanguageDialog(
                availableLanguages = viewModel.getAvailableLanguages(),
                onDismiss = { viewModel.showAddLanguageDialog(false) },
                onAdd = { langCode, _ ->
                    viewModel.upsertTranslation(langCode, "")
                    viewModel.showAddLanguageDialog(false)
                }
            )
        },
        title = {
            Row {
                Icon(Icons.Outlined.Category, stringResource(SharedRes.string.category))
                Text(stringResource(SharedRes.string.create))
            }
        },
        fabButtonState = SharedTransparentScaffoldFabButtonState(
            createStatus,
            createEnabled,
            { viewModel.createCategory() },
            stringResource(SharedRes.string.create),
            Icons.Outlined.Add,
            stringResource(SharedRes.string.create)
        )
    ) { padding, scrollBehavior ->
        LazyVerticalStaggeredGrid(
            modifier = Modifier
                .fillMaxSize()
                .paddingWithoutTop(padding)
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            columns = StaggeredGridCells.Adaptive(minSize = 360.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(span = StaggeredGridItemSpan.FullLine) {
                Spacer(Modifier.height(padding.calculateTopPadding()))
            }
            // 基本信息卡片
            item {
                CategoryBasicInfoCard(
                    categoryName,
                    categoryNameError,
                    isCheckingCategoryName,
                    categoryIva,
                    cardEnabled,
                    viewModel::updateCategoryName,
                    viewModel::updateCategoryIva,
                    viewModel::formatIvaTwoDecimal,
                )
            }

            // 父类别选择
            item {
                ParentCategoryCard(
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
                    viewModel.translations.size < LanguageManager.SupportedLanguages.entries.size - 1,
                    viewModel::showAddLanguageDialog,
                    viewModel::removeTranslation,
                    viewModel::upsertTranslation,
                )
            }
        }
    }
}


@Composable
private fun LanguageTranslationItem(
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester,
    requestFocus: Boolean = true,
    languageCode: String,
    languageName: String,
    translation: String,
    enabled: Boolean,
    onRemove: () -> Unit,
    onImeAction: () -> Unit,
    onEdit: (String) -> Unit,
    isLoading: Boolean = false,
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
                Row(
                    modifier = Modifier.placeholderWithShimmer(isLoading),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                        modifier = Modifier.size(32.dp).placeholderWithShimmer(isLoading)
                    ) {
                        Icon(
                            Icons.Outlined.Delete,
                            stringResource(SharedRes.string.delete),
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
                placeholderText = stringResource(EnterpriseRes.string.input_language_translation, languageName),
                modifier = Modifier.fillMaxWidth().placeholderWithShimmer(isLoading),
                leadingIcon = Icons.Outlined.Translate,
                leadingIconContentDescription = stringResource(EnterpriseRes.string.translate),
                enabled = enabled,
                imeAction = ImeAction.Next,
                onImeAction = onImeAction,
                keyBordType = KeyboardType.Text,
                error = if (translation.isBlank()) stringResource(SharedRes.string.field_required) else null,
                labelText = languageName,
            )

            LaunchedEffect(Unit) {
                if (requestFocus) {
                    focusRequester.requestFocus()
                }
            }
        }
    }
}


@Composable
internal fun AddLanguageDialog(
    availableLanguages: List<LanguageManager.SupportedLanguages>,
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Outlined.Language, contentDescription = "Language") },
        title = { Text(stringResource(EnterpriseRes.string.add_language_translation)) },
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
                        stringResource(EnterpriseRes.string.all_languages_added),
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
                Text(
                    if (availableLanguages.isEmpty()) stringResource(SharedRes.string.close) else stringResource(
                        SharedRes.string.cancel
                    )
                )
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
                            contentDescription = stringResource(SharedRes.string.clear),
                            tint = contentColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryBasicInfoCard(
    categoryName: String,
    categoryNameError: StringResource?,
    isChecking: Boolean,
    categoryIva: String,
    enabled: Boolean,
    onNameChange: (String) -> Unit,
    onIvaChange: (String) -> Unit,
    onIvaBlur: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
) {
    val focusManager = LocalFocusManager.current

    FormCard(
        modifier = modifier,
        title = stringResource(SharedRes.string.basic_info),
        subtitle = stringResource(EnterpriseRes.string.category_name_tax_setting),
        uiState = if (categoryNameError != null) UiState.Error else UiState.Idle,
        enabled = enabled
    ) { isEnabled ->
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            MyOutlinedTextField(
                modifier = Modifier.placeholderWithShimmer(isLoading),
                value = categoryName,
                onValueChange = onNameChange,
                leadingIcon = Icons.Outlined.Category,
                leadingIconContentDescription = stringResource(SharedRes.string.category),
                trailingIcon = { CheckingTrailingIcon(isChecking) },
                labelText = "${stringResource(EnterpriseRes.string.category_name)} (${stringResource(SharedRes.string.field_required)})",
                placeholderText = stringResource(EnterpriseRes.string.please_input_category_name),
                enabled = isEnabled,
                error = categoryNameError.asString(),
                imeAction = ImeAction.Next,
                onImeAction = { focusManager.moveFocus(FocusDirection.Next) }
            )

            MyOutlinedTextField(
                modifier = Modifier.onFocusChanged {
                    if (!it.isFocused) onIvaBlur()
                }.placeholderWithShimmer(isLoading),
                leadingIcon = Icons.Outlined.Percent,
                leadingIconContentDescription = stringResource(SharedRes.string.tax_rate),
                value = categoryIva,
                onValueChange = onIvaChange,
                labelText = "${stringResource(SharedRes.string.tax_rate)}->IVA(%)",
                placeholderText = stringResource(EnterpriseRes.string.empty_use_product_tax),
                enabled = isEnabled,
                keyBordType = KeyboardType.Decimal,
                error = null,
                trailingIcon = {
                    if (categoryIva.isNotBlank() && isEnabled) {
                        IconButton(onClick = { onIvaChange("") }) {
                            Icon(Icons.Outlined.Clear, stringResource(SharedRes.string.clear))
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
    selectedParentCategory: ReducedCategoryResponse?, // 替换为实际类型
    enabled: Boolean,
    onParentCategoryChange: (ReducedCategoryResponse?) -> Unit,
    onRemoveParent: () -> Unit,
    onSearch: suspend (String?, Int, Int) -> List<ReducedCategoryResponse>,
    modifier: Modifier = Modifier
) {
    FormCard(
        modifier = modifier,
        title = stringResource(EnterpriseRes.string.parent_category),
        subtitle = "${stringResource(EnterpriseRes.string.select_parent_category_to_create)}（${stringResource(SharedRes.string.field_optional)}）",
        enabled = enabled
    ) { isEnabled ->
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SearchableSelectorRemote(
                config = RemoteSearchableSelectorConfig(
                    label = stringResource(EnterpriseRes.string.select_parent_category),
                    error = null,
                    leadingIcon = Icons.Outlined.Category,
                    selectedItem = selectedParentCategory,
                    onSelectedItemChange = onParentCategoryChange,
                    pageSize = 100,
                    itemToString = {
                        "${it.name} • ${it.translationString}"
                    },
                    onSearch = onSearch
                )
            )

            SelectedInfoCard(
                visible = selectedParentCategory != null,
                title = stringResource(EnterpriseRes.string.parent_category_selected),
                description = selectedParentCategory?.name ?: "",
                onClear = onRemoveParent,
                enabled = isEnabled
            )
        }
    }
}

@Composable
internal fun TranslationCard(
    translations: SnapshotStateList<SharedCategoryTranslation>,
    translationsIsValid: Boolean,
    enabled: Boolean,
    hasAvailableLanguages: Boolean,
    onShowAddDialog: (Boolean) -> Unit,
    onRemoveTranslation: (String) -> Unit,
    onEditTranslation: (String, String) -> Unit,
    modifier: Modifier = Modifier,
    requestFocus: Boolean = true,
    isLoading: Boolean = false,
) {
    FormCard(
        modifier = modifier,
        title = stringResource(EnterpriseRes.string.other_languages),
        subtitle = "${stringResource(EnterpriseRes.string.add_multilingual_translation)}（${stringResource(SharedRes.string.field_optional)}）",
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
                modifier = Modifier.align(Alignment.End).placeholderWithShimmer(isLoading),
                enabled = addLanguageEnabled
            ) {
                Icon(Icons.Outlined.Add, stringResource(SharedRes.string.add))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(EnterpriseRes.string.add_language_translation))
            }

            if (translations.isEmpty()) {
                Text(
                    stringResource(EnterpriseRes.string.no_other_language_translation),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 280.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                ) {
                    items(translations, key = { it.langCode }) { (langCode, translation) ->
                        LanguageTranslationItem(
                            requestFocus = requestFocus,
                            focusRequester = focusRequester,
                            languageCode = langCode,
                            languageName = LanguageManager.SupportedLanguages
                                .fromCode(langCode).displayName,
                            translation = translation,
                            enabled = isEnabled,
                            onRemove = { onRemoveTranslation(langCode) },
                            onEdit = { newText -> onEditTranslation(langCode, newText) },
                            onImeAction = { if (addLanguageEnabled) onShowAddDialog(true) },
                            isLoading = isLoading
                        )
                    }
                }
            }

            SelectedInfoCard(
                visible = translations.isNotEmpty(),
                description = stringResource(EnterpriseRes.string.translations_count, translations.size),
                icon = Icons.Outlined.Info,
                enabled = false,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}