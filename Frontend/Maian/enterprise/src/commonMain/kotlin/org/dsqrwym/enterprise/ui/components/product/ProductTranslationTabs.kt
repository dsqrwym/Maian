package org.dsqrwym.enterprise.ui.components.product

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Expand
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.richeditor.model.RichTextState
import maian.business.generated.resources.BusinessRes
import maian.business.generated.resources.add_language_translation
import maian.business.generated.resources.translations_count
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.add
import maian.shared.generated.resources.field_optional
import maian.shared.generated.resources.field_required
import org.dsqrwym.business.ui.components.button.BusinessDeleteIconButton
import org.dsqrwym.business.ui.components.category.BusinessSelectedInfoCard
import org.dsqrwym.business.ui.components.richtext.BusinessRichTextEditor
import org.dsqrwym.business.ui.components.richtext.RichTextStyleButton
import org.dsqrwym.shared.data.products.dto.SharedProductTranslation
import org.dsqrwym.shared.localization.LanguageManager
import org.dsqrwym.shared.ui.components.input.outlinedfields.MyOutlinedTextField
import org.dsqrwym.shared.util.colum.SharedColumnLayout
import org.dsqrwym.shared.util.formatter.asString
import org.dsqrwym.shared.util.lazygrid.SharedLazyGridLayout
import org.dsqrwym.shared.util.modifier.placeholderWithShimmer
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductTranslationTabs(
    isLoading: Boolean = false,
    translationTabs: SnapshotStateList<Pair<SharedProductTranslation, RichTextState>>,
    currentProductNameError: StringResource?,
    currentLanguageIndex: Int,
    changeLanguageIndex: (Int) -> Unit,
    upsertTranslation: (
        String,
        String,
        String?,
        String?
    ) -> Unit,
    removeTranslation: (String) -> Unit,
    showAddLanguageDialog: (Boolean) -> Unit,
    canAddTranslation: Boolean,
    isAuxiliaryOpen: Boolean,
    onToggleRichTextEditor: () -> Unit = {},
) {
    val focusManager = LocalFocusManager.current
    if (translationTabs.isEmpty()) {
        return
    }
    val selectedLanguageIndex =
        currentLanguageIndex.coerceIn(
            minimumValue = 0,
            maximumValue = translationTabs.lastIndex
        )
    val currentTranslation = translationTabs[selectedLanguageIndex].first
    val currentDescription = translationTabs[selectedLanguageIndex].second

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = SharedColumnLayout.arrangement,
    ) {
        BusinessSelectedInfoCard(
            modifier = Modifier.placeholderWithShimmer(isLoading),
            visible = translationTabs.size > 1,
            description = stringResource(BusinessRes.string.translations_count, translationTabs.lastIndex),
            icon = Icons.Outlined.Info,
            enabled = false,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )

        TranslationTabRow(
            isLoading = isLoading,
            translationTabs = translationTabs,
            selectedLanguageIndex = selectedLanguageIndex,
            onSelect = changeLanguageIndex,
            onRemove = removeTranslation,
            onAddClick = { showAddLanguageDialog(true) },
            canAdd = canAddTranslation
        )

        MyOutlinedTextField(
            modifier = Modifier.placeholderWithShimmer(isLoading),
            value = currentTranslation.name,
            onValueChange = {
                upsertTranslation(
                    currentTranslation.langCode,
                    it,
                    currentTranslation.title,
                    currentTranslation.description
                )
            },
            leadingIcon = Icons.AutoMirrored.Outlined.Label,
            leadingIconContentDescription = "",
            labelText = "产品名称 (${stringResource(SharedRes.string.field_required)})",
            placeholderText = "请输入产品名称",
            imeAction = ImeAction.Next,
            error = currentProductNameError.asString(),
            onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
        )

        MyOutlinedTextField(
            modifier = Modifier.placeholderWithShimmer(isLoading),
            value = currentTranslation.title ?: "",
            onValueChange = {
                upsertTranslation(
                    currentTranslation.langCode,
                    currentTranslation.name,
                    it,
                    currentTranslation.description
                )
            },
            leadingIcon = Icons.AutoMirrored.Outlined.Article,
            leadingIconContentDescription = "",
            labelText = "产品标题 (${stringResource(SharedRes.string.field_optional)})",
            placeholderText = "请输入产品名称，用于进行简短的介绍",
            imeAction = ImeAction.Next,
            onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
        )

        BusinessRichTextEditor(
            isLoading = isLoading,
            label = "产品详情 (${stringResource(SharedRes.string.field_optional)})",
            placeholder = "请输入详细的产品介绍",
            state = currentDescription,
            toolbarItems = {
                item {
                    RichTextStyleButton(
                        enabled = !isLoading,
                        onClick = onToggleRichTextEditor,
                        icon = Icons.Outlined.Expand,
                        isSelected = isAuxiliaryOpen,
                    )
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslationTabRow(
    isLoading: Boolean = false,
    translationTabs: SnapshotStateList<Pair<SharedProductTranslation, RichTextState>>,
    selectedLanguageIndex: Int,
    onSelect: (Int) -> Unit,
    onRemove: (String) -> Unit,
    onAddClick: () -> Unit,
    canAdd: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = SharedLazyGridLayout.arrangement,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PrimaryScrollableTabRow(
            selectedTabIndex = selectedLanguageIndex,
            modifier = Modifier.weight(1f).placeholderWithShimmer(isLoading),
            containerColor = Color.Transparent,
        ) {
            translationTabs.forEachIndexed { index, lang ->
                val isMainLanguage = index == 0
                val language = LanguageManager.SupportedLanguages.fromCode(lang.first.langCode)
                val content = if (isMainLanguage) "主语言" else "${language.displayName} (${language.code})"
                LeadingIconTab(
                    selected = selectedLanguageIndex == index,
                    onClick = {
                        onSelect(index)
                    },
                    icon = {
                        Icon(Icons.Outlined.Language, content, modifier = Modifier.size(20.dp))
                    },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(1.dp)
                        ) {
                            Text(content, style = MaterialTheme.typography.labelLarge)
                            if (!isMainLanguage) {
                                BusinessDeleteIconButton { onRemove(lang.first.langCode) }
                            }
                        }
                    }
                )
            }
        }
        OutlinedButton(
            modifier = Modifier.placeholderWithShimmer(isLoading),
            onClick = onAddClick,
            enabled = canAdd,
        ) {
            Icon(Icons.Outlined.Add, stringResource(SharedRes.string.add))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(BusinessRes.string.add_language_translation))
        }
    }
}