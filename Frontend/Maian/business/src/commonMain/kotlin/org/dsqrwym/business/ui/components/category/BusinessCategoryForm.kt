package org.dsqrwym.business.ui.components.category

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import maian.business.generated.resources.*
import maian.shared.generated.resources.*
import org.dsqrwym.shared.data.category.dto.ReducedCategoryResponse
import org.dsqrwym.shared.data.category.dto.SharedCategoryTranslation
import org.dsqrwym.shared.localization.LanguageManager
import org.dsqrwym.shared.ui.components.cards.FormCard
import org.dsqrwym.shared.ui.components.containers.UiState
import org.dsqrwym.shared.ui.components.input.outlinetextfields.MyOutlinedTextField
import org.dsqrwym.shared.ui.components.input.selector.RemoteSearchableSelectorConfig
import org.dsqrwym.shared.ui.components.input.selector.SearchableSelectorRemote
import org.dsqrwym.shared.ui.components.progressindicators.CheckingTrailingIcon
import org.dsqrwym.shared.util.formatter.asString
import org.dsqrwym.shared.util.modifier.placeholderWithShimmer
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource


@Composable
fun BusinessTranslationCard(
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
        title = stringResource(BusinessRes.string.other_languages),
        subtitle = "${stringResource(BusinessRes.string.add_multilingual_translation)}（${stringResource(SharedRes.string.field_optional)}）",
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
                Text(stringResource(BusinessRes.string.add_language_translation))
            }

            if (translations.isEmpty()) {
                Text(
                    stringResource(BusinessRes.string.no_other_language_translation),
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
                        BusinessLanguageTranslationItem(
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

            BusinessSelectedInfoCard(
                visible = translations.isNotEmpty(),
                description = stringResource(BusinessRes.string.translations_count, translations.size),
                icon = Icons.Outlined.Info,
                enabled = false,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun BusinessSelectedInfoCard(
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
private fun BusinessLanguageTranslationItem(
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
                placeholderText = stringResource(BusinessRes.string.input_language_translation, languageName),
                modifier = Modifier.fillMaxWidth().placeholderWithShimmer(isLoading),
                leadingIcon = Icons.Outlined.Translate,
                leadingIconContentDescription = stringResource(BusinessRes.string.translate),
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
fun BusinessCategoryBasicInfoCard(
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
        subtitle = stringResource(BusinessRes.string.category_name_tax_setting),
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
                labelText = "${stringResource(BusinessRes.string.category_name)} (${stringResource(SharedRes.string.field_required)})",
                placeholderText = stringResource(BusinessRes.string.please_input_category_name),
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
                placeholderText = stringResource(BusinessRes.string.empty_use_product_tax),
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
 fun BusinessParentCategoryCard(
    selectedParentCategory: ReducedCategoryResponse?, // 替换为实际类型
    enabled: Boolean,
    onParentCategoryChange: (ReducedCategoryResponse?) -> Unit,
    onRemoveParent: () -> Unit,
    onSearch: suspend (String?, Int, Int) -> List<ReducedCategoryResponse>,
    modifier: Modifier = Modifier
) {
    FormCard(
        modifier = modifier,
        title = stringResource(BusinessRes.string.parent_category),
        subtitle = "${stringResource(BusinessRes.string.select_parent_category_to_create)}（${stringResource(SharedRes.string.field_optional)}）",
        enabled = enabled
    ) { isEnabled ->
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SearchableSelectorRemote(
                config = RemoteSearchableSelectorConfig(
                    label = stringResource(BusinessRes.string.select_parent_category),
                    error = null,
                    leadingIcon = Icons.Outlined.Category,
                    selectedItem = selectedParentCategory,
                    onSelectedItemChange = onParentCategoryChange,
                    pageSize = 100,
                    itemToString = {
                        "${it.name}${it.translationString?.let { str -> " • $str" }.orEmpty()}"
                    },
                    onSearch = onSearch
                )
            )

            BusinessSelectedInfoCard(
                visible = selectedParentCategory != null,
                title = stringResource(BusinessRes.string.parent_category_selected),
                description = selectedParentCategory?.name ?: "",
                onClear = onRemoveParent,
                enabled = isEnabled
            )
        }
    }
}