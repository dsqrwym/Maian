package org.dsqrwym.enterprise.ui.screens.products

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.create
import maian.shared.generated.resources.status_error_content_description
import org.dsqrwym.business.ui.media.MediaPickerViewModel
import org.dsqrwym.business.ui.media.model.MediaType
import org.dsqrwym.business.ui.media.model.UploadMediaItem
import org.dsqrwym.business.ui.media.model.UploadState
import org.dsqrwym.enterprise.ui.viewmodels.products.ProductCreateViewModel
import org.dsqrwym.shared.theme.MyHazeStyles
import org.dsqrwym.shared.ui.components.cards.FormCard
import org.dsqrwym.shared.ui.components.containers.SharedOverlayContentBox
import org.dsqrwym.shared.ui.components.containers.UiState
import org.dsqrwym.shared.ui.components.scaffold.SharedTransparentScaffold
import org.dsqrwym.shared.ui.components.scaffold.SharedTransparentScaffoldFabButtonState
import org.dsqrwym.shared.ui.media.SharedAsyncImage
import org.dsqrwym.shared.ui.media.SharedVideoPlayer
import org.dsqrwym.shared.util.form.SharedFormLayout
import org.dsqrwym.shared.util.lazygrid.SharedLazyGridLayout
import org.dsqrwym.shared.util.modifier.paddingWithoutTop
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
    SharedTransparentScaffold(
        onNavigateBack = onNavigateBack,
        showOverlayDialog = false,
        overlayContent = {},
        title = {
            Row {
                Icon(Icons.AutoMirrored.Outlined.Article, "产品")
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
        val mediaPicker = viewModel.mediaPicker

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .paddingWithoutTop(SharedFormLayout.Padding)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        ) {
            /*FormCard(title = "基础信息") {
                FlowRow {
                    ProductMediaUploader(
                        mediaPicker
                    )
                }
            }*/

            FormCard(title = "媒体文件") {
                FlowRow {
                    ProductMediaUploader(
                        mediaPicker
                    )
                }
            }
        }
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
