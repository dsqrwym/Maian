package org.dsqrwym.admin.ui.screens.categories

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.unit.dp
import maian.admin.generated.resources.*
import maian.business.generated.resources.BusinessRes
import maian.business.generated.resources.add_language_translation
import maian.business.generated.resources.all_languages_added
import maian.business.generated.resources.private_category
import maian.shared.generated.resources.*
import org.dsqrwym.admin.data.user.dto.WholeSalerUserResponse
import org.dsqrwym.admin.ui.viewmodels.categories.CategoriesCreateViewModel
import org.dsqrwym.business.ui.components.category.BusinessCategoryBasicInfoCard
import org.dsqrwym.business.ui.components.category.BusinessParentCategoryCard
import org.dsqrwym.business.ui.components.category.BusinessSelectedInfoCard
import org.dsqrwym.business.ui.components.category.BusinessTranslationCard
import org.dsqrwym.business.ui.components.row.BusinessTitleIconRow
import org.dsqrwym.shared.localization.LanguageManager
import org.dsqrwym.shared.ui.components.cards.FormCard
import org.dsqrwym.shared.ui.components.containers.UiState
import org.dsqrwym.shared.ui.components.input.selector.RemoteSearchableSelectorConfig
import org.dsqrwym.shared.ui.components.input.selector.SearchableSelectorRemote
import org.dsqrwym.shared.ui.components.scaffold.SharedTransparentScaffold
import org.dsqrwym.shared.ui.components.scaffold.SharedTransparentScaffoldFabButtonState
import org.dsqrwym.shared.ui.overlay.transparentDialogProperties
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

    val selectedParentCategory = viewModel.filterParentCategory
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
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                )
            }

            // 类别类型
            item {
                CategoryTypeCard(
                    isPlatformCategory,
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
                BusinessParentCategoryCard(
                    selectedParentCategory,
                    cardEnabled,
                    viewModel::updateFilterParentCategory,
                    viewModel::removeParentIdFilter,
                    viewModel::findParentCategories,
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
        properties = transparentDialogProperties(),
        onDismissRequest = onDismiss,
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

@Composable
private fun CategoryTypeCard(
    isPlatformCategory: Boolean,
    selectedWholesaler: WholeSalerUserResponse?,
    enabled: Boolean,
    onToggleCategoryType: (Boolean) -> Unit,
    onWholesalerChange: (WholeSalerUserResponse?) -> Unit,
    onRemoveWholesaler: () -> Unit,
    onSearchWholesalers: suspend (String?, Int, Int) -> List<WholeSalerUserResponse>,
    modifier: Modifier = Modifier
) {
    FormCard(
        modifier = modifier,
        title = stringResource(AdminRes.string.category_type),
        subtitle = stringResource(AdminRes.string.category_visibility),
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
                        if (isPlatformCategory) stringResource(AdminRes.string.platform_category) else stringResource(
                            BusinessRes.string.private_category
                        ),
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        if (isPlatformCategory) stringResource(AdminRes.string.visible_to_all) else stringResource(
                            AdminRes.string.visible_to_owner
                        ),
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
                    BusinessSelectedInfoCard(
                        visible = true,
                        description = stringResource(AdminRes.string.category_visible_to_all_tip),
                        icon = Icons.Outlined.Public,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        enabled = false
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        val usernameLabel = stringResource(SharedRes.string.field_username_label)
                        SearchableSelectorRemote(
                            config = RemoteSearchableSelectorConfig(
                                label = stringResource(AdminRes.string.select_wholesaler),
                                error = null,
                                leadingIcon = Icons.Outlined.PersonOutline,
                                selectedItem = selectedWholesaler,
                                onSelectedItemChange = onWholesalerChange,
                                pageSize = 100,
                                itemToString = {
                                    "ID: ${it.userId}, ${usernameLabel}: ${it.username}"
                                },
                                semanticsPropertyReceiver = {
                                    contentType = ContentType.Username
                                },
                                onSearch = onSearchWholesalers,
                            )
                        )

                        BusinessSelectedInfoCard(
                            visible = selectedWholesaler != null,
                            title = stringResource(AdminRes.string.wholesaler_selected),
                            description = "ID: ${selectedWholesaler?.userId}, ${usernameLabel}: ${selectedWholesaler?.username}",
                            onClear = onRemoveWholesaler,
                            enabled = isEnabled
                        )
                    }
                }
            }
        }
    }
}
