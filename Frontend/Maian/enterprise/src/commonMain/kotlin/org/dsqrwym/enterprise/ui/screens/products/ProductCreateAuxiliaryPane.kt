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
import org.dsqrwym.business.ui.components.category.BusinessSelectedInfoCard
import org.dsqrwym.business.ui.components.richtext.BusinessRichTextEditor
import org.dsqrwym.business.ui.workspace.BusinessAuxiliarySurface
import org.dsqrwym.enterprise.MenuConfig
import org.dsqrwym.enterprise.ui.components.product.TranslationTabRow
import org.dsqrwym.enterprise.ui.screens.categories.AddLanguageDialog
import org.dsqrwym.enterprise.ui.viewmodels.products.ProductCreateViewModel
import org.dsqrwym.shared.ui.components.buttons.SharedCloseButton
import org.dsqrwym.shared.ui.components.cards.FormCard
import org.dsqrwym.shared.ui.components.containers.HazeContainer
import org.dsqrwym.shared.util.modifier.paddingTopForMenu
import org.dsqrwym.shared.util.platform.PlatformType
import org.dsqrwym.shared.util.platform.getPlatform
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductCreateAuxiliaryPane(
    viewModel: ProductCreateViewModel,
    surface: BusinessAuxiliarySurface,
    onClose: () -> Unit,
) {
    val isDesktop = remember { getPlatform().type == PlatformType.Desktop }
    val selectedLanguageIndex = viewModel.selectedTranslationIndex
        .coerceIn(0, viewModel.translationTabs.lastIndex)
    val translationTabs = viewModel.translationTabs
    val currentTranslation = viewModel.translationTabs[selectedLanguageIndex].first
    val currentDescription = viewModel.translationTabs[selectedLanguageIndex].second
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
                            Icon(Icons.Outlined.ShoppingBag, "产品")
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
                        BusinessSelectedInfoCard(
                            description = stringResource(BusinessRes.string.translations_count, translationTabs.size),
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
                            canAdd = viewModel.getAvailableLanguages().isNotEmpty()
                        )

                        BusinessRichTextEditor(
                            modifier = Modifier.fillMaxSize(),
                            fillMaxSize = true,
                            label = "产品详情",
                            placeholder = "请输入详细的产品介绍",
                            state = currentDescription,
                        )

                    }

                    BusinessAuxiliarySurface.Preview -> {
                        FormCard(title = "预览骨架") {
                            Text(
                                text = "这里预留给整产品预览工作区。后续可以接入 standard 模块的产品瀑布流或详情预览，而不是只预览富文本。",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )

                            Spacer(Modifier.height(16.dp))

                            Text("产品名: ${currentTranslation.name.ifBlank { "未填写" }}")
                            Text("标题: ${currentTranslation.title.orEmpty().ifBlank { "未填写" }}")
                            Text("分类: ${viewModel.filterCategory?.name ?: "未选择"}")
                            Text("税率: ${viewModel.productIva.ifBlank { "未填写" }}")
                            Text("状态: ${viewModel.productStatus.name}")
                            Text("媒体数量: ${viewModel.mediaPicker.mediaItems.size}")
                        }
                    }
                }
            }
        }
    }
}
