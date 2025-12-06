package org.dsqrwym.enterprise.ui.screens.categories

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.category
import maian.shared.generated.resources.update
import org.dsqrwym.enterprise.ui.viewmodels.categories.CategoriesEditViewModel
import org.dsqrwym.shared.localization.LanguageManager
import org.dsqrwym.shared.navigation.core.NavigationEvent
import org.dsqrwym.shared.ui.components.containers.UiState
import org.dsqrwym.shared.ui.components.scaffold.SharedTransparentScaffold
import org.dsqrwym.shared.ui.components.scaffold.SharedTransparentScaffoldFabButtonState
import org.dsqrwym.shared.util.modifier.paddingWithoutTop
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryEditScreen(
    categoryId: String,
    viewModel: CategoriesEditViewModel = koinViewModel(),
    onNavigate: (Any) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val isLoading = viewModel.isLoading
    var isInitialized by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isInitialized = true
    }
    // 初始化 VM
    LaunchedEffect(categoryId) {
        viewModel.initWithCategory(categoryId)
        viewModel.navigateEvent.collect {
            if (it is NavigationEvent.ToRoute<*>) {
                onNavigate(it.route)
            }
        }
    }

    val categoryName = viewModel.categoryName
    val categoryNameError = viewModel.categoryNameError
    val isCheckingCategoryName = viewModel.isCheckingCategoryName

    val categoryIva = viewModel.categoryIva

    val translations = viewModel.translations
    val translationsIsValid by viewModel.translationIsValid

    val updateStatus = viewModel.updateButtonState
    val updateEnabled by viewModel.updateButtonEnabled

    val cardEnabled by derivedStateOf {
        updateStatus != UiState.Loading
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(categoryName)
                Icon(Icons.Outlined.Category, stringResource(SharedRes.string.category))
            }
        },
        fabButtonState = SharedTransparentScaffoldFabButtonState(
            updateStatus,
            updateEnabled,
            { viewModel.submitUpdate() },
            stringResource(SharedRes.string.update),
            Icons.Outlined.Category,
            stringResource(SharedRes.string.update)
        )
    ) { padding, scrollBehavior ->
        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxSize()
                .paddingWithoutTop(padding)
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            columns = GridCells.Adaptive(minSize = 380.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
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
                    isLoading = isLoading
                )
            }

            item(span = { GridItemSpan(maxCurrentLineSpan) }) {
                TranslationCard(
                    translations,
                    translationsIsValid,
                    cardEnabled,
                    viewModel.translations.size < LanguageManager.SupportedLanguages.entries.size - 1, // 名字本身就是一种语言翻译
                    viewModel::showAddLanguageDialog,
                    viewModel::removeTranslation,
                    viewModel::upsertTranslation,
                    requestFocus = isInitialized,
                    modifier = Modifier.fillMaxWidth(),
                    isLoading = isLoading
                )
            }
        }
    }
}