package org.dsqrwym.enterprise.ui.screens.products

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.richeditor.model.RichTextState
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import maian.business.generated.resources.BusinessRes
import maian.business.generated.resources.add_language_translation
import maian.business.generated.resources.parent_category_selected
import maian.business.generated.resources.translations_count
import maian.shared.generated.resources.*
import org.dsqrwym.business.drawable.sharedicons.Barcode
import org.dsqrwym.business.ui.components.button.BusinessDeleteIconButton
import org.dsqrwym.business.ui.components.category.BusinessSelectedInfoCard
import org.dsqrwym.business.ui.components.richtext.BusinessRichTextEditor
import org.dsqrwym.business.ui.media.MediaPickerViewModel
import org.dsqrwym.business.ui.media.model.MediaType
import org.dsqrwym.business.ui.media.model.UploadMediaItem
import org.dsqrwym.business.ui.media.model.UploadState
import org.dsqrwym.enterprise.ui.screens.categories.AddLanguageDialog
import org.dsqrwym.enterprise.ui.viewmodels.products.ProductCreateViewModel
import org.dsqrwym.shared.data.category.dto.ReducedCategoryResponse
import org.dsqrwym.shared.data.products.SharedProductStatus
import org.dsqrwym.shared.data.products.dto.SharedProductTranslation
import org.dsqrwym.shared.drawable.SharedIcons
import org.dsqrwym.shared.drawable.sharedicons.InProgress
import org.dsqrwym.shared.localization.LanguageManager
import org.dsqrwym.shared.theme.MyHazeStyles
import org.dsqrwym.shared.ui.components.buttons.SharedCloseButton
import org.dsqrwym.shared.ui.components.buttons.SharedScannerButton
import org.dsqrwym.shared.ui.components.cards.FormCard
import org.dsqrwym.shared.ui.components.containers.SharedOverlayContentBox
import org.dsqrwym.shared.ui.components.containers.UiState
import org.dsqrwym.shared.ui.components.input.outlinetextfields.MyOutlinedTextField
import org.dsqrwym.shared.ui.components.input.selector.RemoteSearchableSelectorConfig
import org.dsqrwym.shared.ui.components.input.selector.SearchableSelector
import org.dsqrwym.shared.ui.components.input.selector.SearchableSelectorDefaults
import org.dsqrwym.shared.ui.components.input.selector.SearchableSelectorRemote
import org.dsqrwym.shared.ui.components.scaffold.SharedTransparentScaffold
import org.dsqrwym.shared.ui.components.scaffold.SharedTransparentScaffoldFabButtonState
import org.dsqrwym.shared.ui.media.SharedAsyncImage
import org.dsqrwym.shared.ui.media.SharedVideoPlayer
import org.dsqrwym.shared.util.colum.SharedColumnLayout
import org.dsqrwym.shared.util.formatter.asString
import org.dsqrwym.shared.util.lazygrid.SharedLazyGridLayout
import org.dsqrwym.shared.util.modifier.paddingWithoutTop
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyGridState
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductCreateScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: ProductCreateViewModel = koinViewModel()
) {

    val mediaPicker = viewModel.mediaPicker
    val translationTabs = viewModel.translationTabs
    val currentLanguageIndex = viewModel.selectedLanguageIndex

    val productCode = viewModel.productCode
    val productIva = viewModel.productIva
    val productStatus = viewModel.productStatus

    val selectedCategory = viewModel.filterCategory

    SharedTransparentScaffold(
        onNavigateBack = onNavigateBack,
        showOverlayDialog = viewModel.showAddLanguageDialog,
        overlayContent = {
            AddLanguageDialog(
                availableLanguages = viewModel.getAvailableLanguages(),
                onDismiss = { viewModel.showAddLanguageDialog(false) },
                onAdd = { langCode, _ ->
                    viewModel.upsertTranslation(langCode, "", "")
                    viewModel.showAddLanguageDialog(false)
                }
            )
        },
        title = {
            Row {
                Icon(Icons.Outlined.ShoppingBag, "产品")
                Text(stringResource(SharedRes.string.create))
            }
        },
        fabButtonState = SharedTransparentScaffoldFabButtonState(
            UiState.Idle,
            true,
            { },
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
            columns = StaggeredGridCells.Adaptive(minSize = 399.dp),
            contentPadding = PaddingValues(SharedLazyGridLayout.Padding),
            horizontalArrangement = SharedLazyGridLayout.arrangement,
        ) {
            item(span = StaggeredGridItemSpan.FullLine) {
                Spacer(Modifier.height(padding.calculateTopPadding()))
            }
            item {
                FormCard(title = "媒体文件") {
                    ProductMediaUploader(mediaPicker)
                }
            }
            item {
                FormCard(title = "产品信息（多语言）") {
                    ProductTranslationTabs(
                        translationTabs,
                        null,
                        currentLanguageIndex,
                        viewModel::changeLanguageIndex,
                        viewModel::upsertTranslation,
                        viewModel::removeTranslation,
                        viewModel::showAddLanguageDialog,
                        viewModel::getAvailableLanguages
                    )
                }
            }
            item {
                FormCard(title = "产品属性") {
                    ProductMetaFields(
                        selectedCategory = selectedCategory,
                        onSelectedCategoryChange = viewModel::updateFilterCategory,
                        onSearchCategory = viewModel::findCategories,
                        onRemoveCategory = viewModel::removeFilterCategory,
                        productCode = productCode,
                        onProductCodeChange = viewModel::updateProductCode,
                        productIva = productIva,
                        onIvaChange = viewModel::updateProductIva,
                        onIvaBlur = viewModel::formatIvaTwoDecimal,
                        productStatus = productStatus,
                        onProductStatusChange = viewModel::updateProductStatus,
                    )
                }
            }
        }
    }
}

@Composable
fun ProductMetaFields(
    selectedCategory: ReducedCategoryResponse?,
    onSelectedCategoryChange: (ReducedCategoryResponse?) -> Unit,
    onSearchCategory: suspend (String?, Int, Int) -> List<ReducedCategoryResponse>,
    onRemoveCategory: () -> Unit,
    productCode: String = "",
    onProductCodeChange: (String) -> Unit = {},
    productIva: String = "",
    onIvaChange: (String) -> Unit = {},
    onIvaBlur: () -> Unit = {},
    productStatus: SharedProductStatus = SharedProductStatus.ACTIVE,
    onProductStatusChange: (SharedProductStatus) -> Unit = {},
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = SharedColumnLayout.arrangement,
    ) {
        SearchableSelectorRemote(
            config = RemoteSearchableSelectorConfig(
                label = "选择产品主类别",
                error = null,
                leadingIcon = Icons.Outlined.Category,
                selectedItem = selectedCategory,
                onSelectedItemChange = onSelectedCategoryChange,
                pageSize = 100,
                itemToString = {
                    "${it.name}${it.translationString?.let { str -> " • $str" }.orEmpty()}"
                },
                onSearch = onSearchCategory,
            )
        )

        BusinessSelectedInfoCard(
            visible = selectedCategory != null,
            title = stringResource(BusinessRes.string.parent_category_selected),
            description = selectedCategory?.name ?: "",
            onClear = onRemoveCategory,
            enabled = true
        )


        MyOutlinedTextField(
            value = productCode,
            onValueChange = onProductCodeChange,
            leadingIcon = SharedIcons.Barcode,
            leadingIconContentDescription = "产品编码",
            trailingIcon = {
                if (productCode.isBlank()) {
                    SharedScannerButton(onProductCodeChange)
                } else SharedCloseButton { onProductCodeChange("") }
            },
            labelText = "产品编码 (${stringResource(SharedRes.string.field_required)})",
            placeholderText = "请输入产品编码",
            error = null,
            keyBordType = KeyboardType.Uri,
            imeAction = ImeAction.Next,
            onImeAction = { focusManager.moveFocus(FocusDirection.Next) }
        )

        MyOutlinedTextField(
            value = productIva,
            onValueChange = onIvaChange,
            modifier = Modifier.onFocusChanged {
                if (!it.isFocused) onIvaBlur()
            },
            leadingIcon = Icons.Outlined.Percent,
            leadingIconContentDescription = stringResource(SharedRes.string.tax_rate),
            labelText = "${stringResource(SharedRes.string.tax_rate)}->IVA(%)",
            placeholderText = "请输入产品税率",
            keyBordType = KeyboardType.Decimal,
            error = null,
            trailingIcon = {
                if (productIva.isNotBlank()) {
                    SharedCloseButton {
                        onIvaChange("")
                    }
                }
            },
            imeAction = ImeAction.Next,
            onImeAction = { focusManager.moveFocus(FocusDirection.Next) }
        )

        SearchableSelector(
            items = SharedProductStatus.entries,
            itemToString = { it.name },
            itemId = { it.name },
            config = SearchableSelectorDefaults(
                label = "状态",
                placeholder = "请设置产品状态",
                readOnly = true,
                selectedItemId = productStatus.name,
                leadingIcon = SharedIcons.InProgress,
                onSelectedItemIdChange = {
                    it?.let { onProductStatusChange(SharedProductStatus.valueOf(it)) }
                }
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductTranslationTabs(
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
    getAvailableLanguages: () -> List<LanguageManager.SupportedLanguages>,
) {
    val focusManager = LocalFocusManager.current
    val selectedLanguageIndex =
        currentLanguageIndex.coerceIn(minimumValue = 0, maximumValue = translationTabs.lastIndex)
    val currentTranslation = translationTabs[selectedLanguageIndex].first
    val currentDescription = translationTabs[selectedLanguageIndex].second

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = SharedColumnLayout.arrangement,
    ) {
        Text(
            text = "这是一个测试时",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        BusinessSelectedInfoCard(
            visible = true,
            description = stringResource(BusinessRes.string.translations_count, translationTabs.size),
            icon = Icons.Outlined.Info,
            enabled = false,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = SharedLazyGridLayout.arrangement,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PrimaryScrollableTabRow(
                selectedTabIndex = selectedLanguageIndex,
                modifier = Modifier.weight(1f),
            ) {
                translationTabs.forEachIndexed { index, lang ->
                    val language = LanguageManager.SupportedLanguages.fromCode(lang.first.langCode)
                    val content = "${language.displayName} (${language.code})"
                    LeadingIconTab(
                        selected = selectedLanguageIndex == index,
                        onClick = {
                            changeLanguageIndex(index)
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
                                if (index > 0) {
                                    BusinessDeleteIconButton { removeTranslation(lang.first.langCode) }
                                }
                            }
                        }
                    )
                }
            }
            OutlinedButton(
                onClick = {
                    showAddLanguageDialog(true)
                },
                enabled = getAvailableLanguages().isNotEmpty()
            ) {
                Icon(Icons.Outlined.Add, stringResource(SharedRes.string.add))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(BusinessRes.string.add_language_translation))
            }
        }

        MyOutlinedTextField(
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
            labelText = "产品名称",
            placeholderText = "请输入产品名称",
            imeAction = ImeAction.Next,
            error = currentProductNameError.asString(),
            onImeAction = { focusManager.moveFocus(FocusDirection.Next) },
        )

        MyOutlinedTextField(
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
            labelText = "产品标题",
            placeholderText = "请输入产品名称，用于进行简短的介绍",
            imeAction = ImeAction.Next,
            error = null,
            onImeAction = { focusManager.moveFocus(FocusDirection.Next) },
        )

        BusinessRichTextEditor(
            label = "产品详情",
            placeholder = "请输入详细的产品介绍",
            state = currentDescription
        )
    }
}

@OptIn(ExperimentalTime::class)
@Composable
fun ProductMediaUploader(
    mediaPicker: MediaPickerViewModel,
    modifier: Modifier = Modifier
) {
    val mediaList = mediaPicker.mediaItems
    val hasVideo by derivedStateOf { mediaPicker.videoCount > 0 }
    val canAddMore = mediaPicker.canAddMore
    val fileKitMode by derivedStateOf {
        FileKitMode.Multiple(mediaPicker.remainingSlots)
    }
    val filekitType by derivedStateOf {
        if (mediaPicker.imageCount >= 9) {
            return@derivedStateOf FileKitType.Video
        }
        if (hasVideo) {
            return@derivedStateOf FileKitType.Image
        }
        FileKitType.ImageAndVideo
    }
    val maxSize by derivedStateOf { mediaPicker.maxItemSize.div(1024 * 1024) }

    val launcher = rememberFilePickerLauncher(
        type = filekitType,
        mode = fileKitMode,
    ) { files ->
        files?.let(mediaPicker::addLocalFiles)
    }

    // 2. Reorderable State
    val gridState = rememberLazyGridState()
    val hapticFeedback = LocalHapticFeedback.current
    val reorderableState = rememberReorderableLazyGridState(
        lazyGridState = gridState,
        scrollThresholdPadding = PaddingValues(SharedLazyGridLayout.Padding)
    ) { from, to ->
        // 处理数据重排
        mediaPicker.reorder(from.index - 1, to.index - 1)
        hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
    }
    val isDragging = reorderableState.isAnyItemDragging

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = SharedLazyGridLayout.arrangement,
    ) {
        Text(
            text = "长按拖拽排序，首张图片为主图。单个文件不超过 ${maxSize}MB",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Adaptive(minSize = 80.dp), // 自适应网格
            verticalArrangement = SharedLazyGridLayout.arrangement,
            horizontalArrangement = SharedLazyGridLayout.arrangement,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp, max = 500.dp) // 给一个高度限制，允许内部滚动
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                val deleteButtonWeight by animateFloatAsState(targetValue = if (isDragging) 1f else 0f)
                AnimatedVisibility(visible = isDragging || canAddMore) {
                    Row(
                        modifier = Modifier.padding(top = 10.dp).height(80.dp),
                        horizontalArrangement = SharedLazyGridLayout.arrangement
                    ) {
                        if (canAddMore) {
                            MediaAddGridItem(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                onClick = { launcher.launch() }
                            )
                        }

                        // 只有当权重足够大（说明正在显示或正在进行动画）时才渲染组件
                        if (deleteButtonWeight > 0f) {
                            MediaRemoveGridItem(
                                modifier = Modifier
                                    .alpha(deleteButtonWeight.coerceIn(0f, 1f))
                                    .weight(deleteButtonWeight)
                                    .fillMaxHeight()
                                    .onGloballyPositioned {
                                        mediaPicker.updateDeleteZone(it.boundsInWindow())
                                    },
                                isHovering = mediaPicker.isHoveringDeleteZone
                            )
                        }
                    }
                }
            }

            // 3. 渲染媒体列表
            itemsIndexed(mediaList, key = { _, item -> item.localId }) { index, item ->
                ReorderableItem(
                    state = reorderableState,
                    key = item.localId
                ) { isDragging ->
                    MediaGridItem(
                        item = item,
                        index = index,
                        isDragging = isDragging,
                        modifier = Modifier
                            .onGloballyPositioned { layoutCoordinates ->
                                // 实时上报位置给 Manager
                                if (isDragging) {
                                    mediaPicker.onDragMove(layoutCoordinates.boundsInWindow())
                                }
                            }
                            .draggableHandle(
                                onDragStarted = {
                                    mediaPicker.onDragStart(item.localId)
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                                },
                                onDragStopped = {
                                    mediaPicker.onDragEnd()
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureEnd)
                                }),
                        retry = {
                            mediaPicker.retryUpload(item.localId)
                        }
                    ) {}
                }
            }
            item(span = { GridItemSpan(maxLineSpan) }) {

            }
        }
    }
}

@Composable
fun MediaGridItem(
    index: Int,
    isDragging: Boolean,
    item: UploadMediaItem,
    modifier: Modifier = Modifier,
    retry: () -> Unit,
    onClick: () -> Unit
) {
    val hazeState = rememberHazeState()
    val hazeStyle = MyHazeStyles.glass()
    SharedOverlayContentBox(
        modifier = modifier
            .aspectRatio(1f)
            .shadow(
                elevation = if (isDragging) 16.dp else 0.dp,
                shape = RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = {
                if (!isDragging) {
                    onClick()
                }
            }),
        topEndOverlay = {
            Text(
                text = " ${index + 1} ",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.alpha(if (isDragging) 0f else 1f)
            )
        }
    ) {
        // ===== 媒体内容 =====
        when (
            item.type) {
            MediaType.IMAGE -> {
                SharedAsyncImage(
                    model = item.file,
                    contentDescription = "image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().hazeSource(hazeState),
                    zoomable = false,
                    enableContextMenu = false
                )
            }

            MediaType.VIDEO -> {
                SharedVideoPlayer(
                    item.file,
                    Modifier.hazeSource(hazeState),
                    showProgressBar = false,
                    enableContextMenu = false
                )
            }

            MediaType.DOCUMENT -> Unit
        }

        // ===== 上传状态遮罩 =====
        if (item.uploadState != UploadState.Success && item.uploadState != UploadState.Idle) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .pointerInput(Unit) {} // 阻止点击穿透
                    .hazeEffect(hazeState, hazeStyle) {
                        progressive = HazeProgressive.RadialGradient(
                            radiusIntensity = 0.6f
                        )
                    }
            ) {
                when (item.uploadState) {
                    UploadState.Uploading -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                progress = { item.progress },
                                modifier = Modifier.size(32.dp),
                            )
                            Text(
                                text = "上传中：${(item.progress * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }

                    UploadState.Failed -> {
                        TextButton(retry, Modifier.align(Alignment.Center)) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = stringResource(SharedRes.string.status_error_content_description),
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "上传失败\n点击重试",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                )
                            }
                        }
                    }

                    else -> {}
                }
            }
        }
    }
}

@Composable
fun MediaAddGridItem(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = "Add Media",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "上传",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}


@Composable
fun MediaRemoveGridItem(
    modifier: Modifier = Modifier,
    isHovering: Boolean = false
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isHovering) MaterialTheme.colorScheme.error.copy(alpha = 0.1f) else Color.Transparent
    )
    val borderColor by animateColorAsState(
        targetValue = if (isHovering) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant
    )
    val scale by animateFloatAsState(
        targetValue = if (isHovering) 1.038f else 1f,
    )
    Box(
        modifier = Modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .background(backgroundColor)
            .then(modifier),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = "Remove Media",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (isHovering) "松手删除" else "拖动到此删除",
                style = MaterialTheme.typography.labelSmall,
                color = if (isHovering) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.primary
            )
        }
    }
}
