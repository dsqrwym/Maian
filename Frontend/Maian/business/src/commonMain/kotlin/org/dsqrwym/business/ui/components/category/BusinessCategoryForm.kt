package org.dsqrwym.business.ui.components.category

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Percent
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import maian.business.generated.resources.BusinessRes
import maian.business.generated.resources.add_language_translation
import maian.business.generated.resources.add_multilingual_translation
import maian.business.generated.resources.category_name
import maian.business.generated.resources.category_name_tax_setting
import maian.business.generated.resources.empty_use_product_tax
import maian.business.generated.resources.input_language_translation
import maian.business.generated.resources.no_other_language_translation
import maian.business.generated.resources.other_languages
import maian.business.generated.resources.parent_category
import maian.business.generated.resources.parent_category_selected
import maian.business.generated.resources.please_input_category_name
import maian.business.generated.resources.select_parent_category
import maian.business.generated.resources.select_parent_category_to_create
import maian.business.generated.resources.translate
import maian.business.generated.resources.translations_count
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.add
import maian.shared.generated.resources.basic_info
import maian.shared.generated.resources.category
import maian.shared.generated.resources.field_optional
import maian.shared.generated.resources.field_required
import maian.shared.generated.resources.search_parent_category_name
import maian.shared.generated.resources.tax_rate
import org.dsqrwym.business.ui.components.button.BusinessDeleteIconButton
import org.dsqrwym.shared.data.category.dto.SharedCategoryTranslation
import org.dsqrwym.shared.domain.category.CategorySummary
import org.dsqrwym.shared.localization.LanguageManager
import org.dsqrwym.shared.ui.components.buttons.SharedCloseButton
import org.dsqrwym.shared.ui.components.cards.FormCard
import org.dsqrwym.shared.ui.components.containers.UiState
import org.dsqrwym.shared.ui.components.input.outlinedfields.MyOutlinedTextField
import org.dsqrwym.shared.ui.components.input.selector.RemoteSearchableSelectorConfig
import org.dsqrwym.shared.ui.components.input.selector.SearchableSelectorRemote
import org.dsqrwym.shared.ui.components.progressindicators.CheckingTrailingIcon
import org.dsqrwym.shared.util.colum.SharedColumnLayout
import org.dsqrwym.shared.util.formatter.asString
import org.dsqrwym.shared.util.lazygrid.SharedLazyGridLayout
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
        val focusManager = LocalFocusManager.current
        var previousList by remember { mutableStateOf<List<String>>(emptyList()) }
        var newlyAddedLangCode by remember { mutableStateOf<String?>(null) }


        LaunchedEffect(translations.size) {
            val current = translations.map { it.langCode }
            val added = current - previousList.toSet()
            if (added.isNotEmpty()) {
                newlyAddedLangCode = added.first()
            }
            previousList = current
        }

        Column(
            modifier = Modifier.heightIn(max = 500.dp),
            verticalArrangement = SharedColumnLayout.arrangement
        ) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = SharedLazyGridLayout.arrangement,
                itemVerticalAlignment = Alignment.CenterVertically,
            ) {
                BusinessSelectedInfoCard(
                    modifier = Modifier.weight(1f, false).widthIn(max = 336.dp),
                    visible = translations.isNotEmpty(),
                    description = stringResource(BusinessRes.string.translations_count, translations.size),
                    icon = Icons.Outlined.Info,
                    enabled = false,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedButton(
                    onClick = {
                        onShowAddDialog(true)
                    },
                    modifier = Modifier.placeholderWithShimmer(isLoading),
                    enabled = addLanguageEnabled
                ) {
                    Icon(Icons.Outlined.Add, stringResource(SharedRes.string.add))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(BusinessRes.string.add_language_translation))
                }
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
                    verticalArrangement = SharedLazyGridLayout.arrangement,
                    horizontalArrangement = SharedLazyGridLayout.arrangement,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                ) {
                    items(translations, key = { it.langCode }) { (langCode, translation) ->
                        BusinessLanguageTranslationItem(
                            requestFocus = (langCode == newlyAddedLangCode),
                            onFocusConsumed = {
                                if (newlyAddedLangCode == langCode) {
                                    newlyAddedLangCode = null
                                }
                            },
                            languageCode = langCode,
                            languageName = LanguageManager.SupportedLanguages
                                .fromCode(langCode).displayName,
                            translation = translation,
                            enabled = isEnabled,
                            onRemove = { onRemoveTranslation(langCode) },
                            onEdit = { newText -> onEditTranslation(langCode, newText) },
                            onImeAction = {
                                if (addLanguageEnabled) onShowAddDialog(true) else focusManager.moveFocus(
                                    FocusDirection.Next
                                )
                            },
                            isLoading = isLoading
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BusinessSelectedInfoCard(
    visible: Boolean = true,
    title: String? = null,
    description: String,
    enabled: Boolean = true,
    onClear: (() -> Unit)? = null,
    icon: ImageVector? = null,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(visible = visible, modifier = modifier) {
        Surface(
            color = containerColor,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
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
                    SharedCloseButton(
                        onClick = onClear,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BusinessLanguageTranslationItem(
    modifier: Modifier = Modifier,
    requestFocus: Boolean,
    onFocusConsumed: () -> Unit,
    languageCode: String,
    languageName: String,
    translation: String,
    enabled: Boolean,
    onRemove: () -> Unit,
    onImeAction: () -> Unit,
    onEdit: (String) -> Unit,
    isLoading: Boolean = false,
) {
    val focusRequester = remember { FocusRequester() }
    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(SharedColumnLayout.padding),
            verticalArrangement = SharedColumnLayout.arrangement
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
                        Icons.Outlined.Language,
                        "$languageName ($languageCode)",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "$languageName ($languageCode)",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                if (enabled) {
                    BusinessDeleteIconButton { onRemove() }
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

            LaunchedEffect(requestFocus) {
                if (requestFocus) {
                    focusRequester.requestFocus()
                    onFocusConsumed()
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
    categoryIva: String?,
    enabled: Boolean,
    onNameChange: (String) -> Unit,
    onIvaChange: (String?) -> Unit,
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
        Column(verticalArrangement = SharedColumnLayout.arrangement) {
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
                value = categoryIva ?: "",
                onValueChange = onIvaChange,
                labelText = "${stringResource(SharedRes.string.tax_rate)}->IVA(%)",
                placeholderText = stringResource(BusinessRes.string.empty_use_product_tax),
                enabled = isEnabled,
                keyBordType = KeyboardType.Decimal,
                error = null,
                trailingIcon = {
                    if (categoryIva?.isNotBlank() == true && isEnabled) {
                        SharedCloseButton {
                            onIvaChange(null)
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
    selectedParentCategory: CategorySummary?,
    enabled: Boolean,
    onParentCategoryChange: (CategorySummary?) -> Unit,
    onRemoveParent: () -> Unit,
    onSearch: suspend (String?, Int, Int) -> List<CategorySummary>,
    modifier: Modifier = Modifier
) {
    val currentLanguage = LanguageManager.getCurrent().code
    FormCard(
        modifier = modifier,
        title = stringResource(BusinessRes.string.parent_category),
        subtitle = "${stringResource(BusinessRes.string.select_parent_category_to_create)}（${stringResource(SharedRes.string.field_optional)}）",
        enabled = enabled
    ) { isEnabled ->
        Column(verticalArrangement = SharedColumnLayout.arrangement) {
            SearchableSelectorRemote(
                config = RemoteSearchableSelectorConfig(
                    label = stringResource(BusinessRes.string.select_parent_category),
                    error = null,
                    leadingIcon = Icons.Outlined.Category,
                    placeholder = stringResource(SharedRes.string.search_parent_category_name),
                    selectedItem = selectedParentCategory,
                    onSelectedItemChange = onParentCategoryChange,
                    pageSize = 100,
                    itemToString = {
                        it.translationDisplayText(currentLanguage)
                    },
                    onSearch = onSearch
                )
            )

            BusinessSelectedInfoCard(
                visible = selectedParentCategory != null,
                title = stringResource(BusinessRes.string.parent_category_selected),
                description = selectedParentCategory?.localizedName(LanguageManager.getCurrent().code) ?: "",
                onClear = onRemoveParent,
                enabled = isEnabled
            )
        }
    }
}
