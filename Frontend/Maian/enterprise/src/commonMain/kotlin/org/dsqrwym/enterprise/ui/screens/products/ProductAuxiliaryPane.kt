package org.dsqrwym.enterprise.ui.screens.products

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import maian.business.generated.resources.BusinessRes
import maian.business.generated.resources.translations_count
import maian.enterprise.generated.resources.*
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.product_name
import org.dsqrwym.business.ui.components.category.BusinessSelectedInfoCard
import org.dsqrwym.business.ui.components.richtext.BusinessRichTextEditor
import org.dsqrwym.business.ui.workspace.BusinessAuxiliarySurface
import org.dsqrwym.enterprise.MenuConfig
import org.dsqrwym.enterprise.ui.components.product.TranslationTabRow
import org.dsqrwym.enterprise.ui.screens.categories.AddLanguageDialog
import org.dsqrwym.enterprise.ui.viewmodels.products.BaseProductFormViewModel
import org.dsqrwym.shared.data.products.displayName
import org.dsqrwym.shared.ui.components.buttons.SharedCloseButton
import org.dsqrwym.shared.ui.components.cards.FormCard
import org.dsqrwym.shared.ui.components.containers.HazeContainer
import org.dsqrwym.shared.util.modifier.paddingTopForMenu
import org.dsqrwym.shared.util.platform.PlatformType
import org.dsqrwym.shared.util.platform.getPlatform
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductAuxiliaryPane(
    viewModel: BaseProductFormViewModel,
    surface: BusinessAuxiliarySurface,
    onClose: () -> Unit,
) {
    val isDesktop = remember { getPlatform().type == PlatformType.Desktop }
    val selectedLanguageIndex = viewModel.selectedTranslationIndex
        .coerceIn(0, viewModel.translationTabs.lastIndex)
    val translationTabs = viewModel.translationTabs
    val currentTranslation = viewModel.translationTabs[selectedLanguageIndex].first
    val currentDescription = viewModel.translationTabs[selectedLanguageIndex].second
    val canAddTranslation = viewModel.canAddTranslation

    var showAddTranslation by remember { mutableStateOf(false) }

    HazeContainer(
        isOverlayVisible = showAddTranslation,
        overlayContent = {
            AddLanguageDialog(
                availableLanguages = viewModel.getAvailableLanguages(),
                onDismiss = { showAddTranslation = false },
                onAdd = { langCode, _ ->
                    viewModel.upsertTranslation(langCode, "", "")
                    showAddTranslation = false
                }
            )
        }
    ) {
        Scaffold(
            contentColor =  contentColorFor(MaterialTheme.colorScheme.background),
            containerColor = if (isDesktop) Color.Transparent else MaterialTheme.colorScheme.background,
            topBar = {
                CenterAlignedTopAppBar(
                    modifier = Modifier.paddingTopForMenu(true),
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent,
                    ),
                    navigationIcon = { SharedCloseButton(onClick = onClose) },
                    title = {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(currentTranslation.name)
                            Icon(Icons.Outlined.ShoppingBag, stringResource(SharedRes.string.product_name))
                        }
                    },
                    actions = {
                        MenuConfig.topBarActions.forEach {
                            it.content(TooltipAnchorPosition.Below)
                        }
                    }
                )
            }
        ) {
            Column(
                Modifier
                    .padding(it)
                    .padding(start = 12.dp, bottom = 12.dp, end = 12.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                when (surface) {
                    BusinessAuxiliarySurface.Editor -> {
                        if (translationTabs.isEmpty()) {
                            return@Column
                        }
                        BusinessSelectedInfoCard(
                            description = stringResource(BusinessRes.string.translations_count, translationTabs.size),
                            visible = translationTabs.size > 1,
                            icon = Icons.Outlined.Info,
                            enabled = false,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        TranslationTabRow(
                            translationTabs = translationTabs,
                            selectedLanguageIndex = selectedLanguageIndex,
                            onSelect = viewModel::changeLanguageIndex,
                            onRemove = viewModel::removeTranslation,
                            onAddClick = { showAddTranslation = true },
                            canAdd = canAddTranslation
                        )

                        BusinessRichTextEditor(
                            modifier = Modifier.fillMaxSize(),
                            fillMaxSize = true,
                            label = stringResource(EnterpriseRes.string.product_description),
                            placeholder = stringResource(EnterpriseRes.string.product_description_placeholder),
                            state = currentDescription,
                        )

                    }

                    BusinessAuxiliarySurface.Preview -> {
                        FormCard(title = stringResource(EnterpriseRes.string.product_preview_outline)) {
                            Text(
                                text = stringResource(EnterpriseRes.string.product_preview_placeholder),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )

                            Spacer(Modifier.height(16.dp))

                            Text(stringResource(EnterpriseRes.string.product_preview_name, currentTranslation.name.ifBlank { stringResource(EnterpriseRes.string.product_not_filled) }))
                            Text(stringResource(EnterpriseRes.string.product_preview_title, currentTranslation.title.orEmpty().ifBlank { stringResource(EnterpriseRes.string.product_not_filled) }))
                            Text(stringResource(EnterpriseRes.string.product_preview_category, viewModel.filterCategory?.name ?: stringResource(EnterpriseRes.string.product_not_selected)))
                            Text(stringResource(EnterpriseRes.string.product_preview_tax_rate, viewModel.productIva.ifBlank { stringResource(EnterpriseRes.string.product_not_filled) }))
                            Text(stringResource(EnterpriseRes.string.product_preview_status, viewModel.productStatus.displayName()))
                            Text(stringResource(EnterpriseRes.string.product_preview_media_count, viewModel.mediaPicker.mediaItems.size))
                        }
                    }
                }
            }
        }
    }
}



