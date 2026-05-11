package org.dsqrwym.enterprise.ui.screens.categories

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import maian.business.generated.resources.BusinessRes
import maian.business.generated.resources.add_language_translation
import maian.business.generated.resources.all_languages_added
import maian.shared.generated.resources.*
import org.dsqrwym.business.ui.components.category.BusinessCategoryBasicInfoCard
import org.dsqrwym.business.ui.components.category.BusinessParentCategoryCard
import org.dsqrwym.business.ui.components.category.BusinessTranslationCard
import org.dsqrwym.business.ui.components.row.BusinessTitleIconRow
import org.dsqrwym.enterprise.ui.viewmodels.categories.CategoriesCreateViewModel
import org.dsqrwym.shared.localization.LanguageManager
import org.dsqrwym.shared.ui.components.containers.UiState
import org.dsqrwym.shared.ui.components.scaffold.SharedTransparentScaffold
import org.dsqrwym.shared.ui.components.scaffold.SharedTransparentScaffoldFabButtonState
import org.dsqrwym.shared.ui.overlay.transparentDialogProperties
import org.dsqrwym.shared.util.lazygrid.SharedLazyGridLayout
import org.dsqrwym.shared.util.modifier.paddingWithoutTop
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryCreateScreen(
    viewModel: CategoriesCreateViewModel = koinViewModel(),
    onNavigateBack: () -> Unit,
) {
    val categoryName = viewModel.categoryName
    val categoryNameError = viewModel.categoryNameError
    val isCheckingCategoryName = viewModel.isCheckingCategoryName

    val categoryIva = viewModel.categoryIva

    val selectedParentCategory = viewModel.filterCategory

    val translations = viewModel.translations
    val translationsIsValid by viewModel.translationIsValid

    val createStatus = viewModel.createButtonState
    val createEnabled by viewModel.createButtonEnabled
    val cardEnabled by derivedStateOf {
        createStatus != UiState.Loading
    }

    LaunchedEffect(Unit) {
        viewModel.navigateEvent.collect {
            onNavigateBack()
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
            BusinessTitleIconRow(
                stringResource(SharedRes.string.create),
                Icons.Outlined.Category,
                stringResource(SharedRes.string.category)
            )
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
            contentPadding = PaddingValues(SharedLazyGridLayout.Padding),
            horizontalArrangement = SharedLazyGridLayout.arrangement,
        ) {
            item(span = StaggeredGridItemSpan.FullLine) {
                Spacer(Modifier.height(padding.calculateTopPadding()))
            }
            // 基本信息卡片
            item {
                BusinessCategoryBasicInfoCard(
                    categoryName,
                    categoryNameError,
                    isCheckingCategoryName,
                    categoryIva,
                    cardEnabled,
                    viewModel::updateCategoryName,
                    viewModel::updateCategoryIva,
                    viewModel::formatIvaTwoDecimal,
                    modifier = Modifier.animateItem(),
                )
            }

            // 父类别选择
            item {
                BusinessParentCategoryCard(
                    selectedParentCategory,
                    cardEnabled,
                    viewModel::updateFilterCategory,
                    viewModel::removeFilterCategory,
                    viewModel::findCategories,
                    modifier = Modifier.animateItem(),
                )
            }

            // 多语言翻译
            item(span = StaggeredGridItemSpan.FullLine) {
                BusinessTranslationCard(
                    translations,
                    translationsIsValid,
                    cardEnabled,
                    viewModel.translations.size < LanguageManager.SupportedLanguages.entries.size - 1,
                    viewModel::showAddLanguageDialog,
                    viewModel::removeTranslation,
                    viewModel::upsertTranslation,
                    modifier = Modifier.animateItem(),
                )
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
        properties = transparentDialogProperties(),
        icon = { Icon(Icons.Outlined.Language, contentDescription = "Language") },
        title = { Text(stringResource(BusinessRes.string.add_language_translation)) },
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
                        stringResource(BusinessRes.string.all_languages_added),
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
