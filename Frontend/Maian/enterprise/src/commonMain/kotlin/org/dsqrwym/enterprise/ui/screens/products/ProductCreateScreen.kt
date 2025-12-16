package org.dsqrwym.enterprise.ui.screens.products

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.mimeType
import io.github.vinceglb.filekit.name
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.create
import org.dsqrwym.shared.ui.components.cards.FormCard
import org.dsqrwym.shared.ui.components.containers.UiState
import org.dsqrwym.shared.ui.components.scaffold.SharedTransparentScaffold
import org.dsqrwym.shared.ui.components.scaffold.SharedTransparentScaffoldFabButtonState
import org.jetbrains.compose.resources.stringResource
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyGridState
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductCreateScreen(
    onNavigateBack: () -> Unit = {}
) {
    SharedTransparentScaffold(
        onNavigateBack = onNavigateBack,
        showOverlayDialog = false,
        overlayContent = {},
        title = {
            Row {
                Icon(Icons.Outlined.Article, "产品")
                Text("创建")
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
        val mediaList = remember { mutableStateListOf<MediaItem>() }
        Column {
            FormCard(title = "基础信息") {
                FlowRow {
                    ProductMediaUploader(
                        mediaList = mediaList,
                        onMediaListChange = {
                            mediaList.clear()
                            mediaList.addAll(it)
                        }
                    )
                }
            }
        }
    }
}


data class MediaItem(
    val id: String, // 用于 Reorderable 的唯一 Key
    val file: PlatformFile,
    val isVideo: Boolean
)

@OptIn(ExperimentalTime::class)
@Composable
fun ProductMediaUploader(
    mediaList: MutableList<MediaItem>,
    onMediaListChange: (List<MediaItem>) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    val launcher = rememberFilePickerLauncher(
        type = FileKitType.ImageAndVideo,
        mode = FileKitMode.Multiple(maxItems = 10),
        title = "选择图片或者照片",
    ) { files ->
        files?.let { pickedFiles ->
            val newItems = pickedFiles.map { file ->
                val isVideo = file.mimeType()?.primaryType?.contains("video")
                MediaItem(
                    id = "${file.name}_${Clock.System.now().toEpochMilliseconds()}_${(0..9999).random()}", // 确保Key唯一
                    file = file,
                    isVideo = isVideo ?: false
                )
            }
            mediaList.addAll(newItems)
            onMediaListChange(mediaList)
        }
    }

    // 2. Reorderable State
    val gridState = rememberLazyGridState()
    val reorderableState = rememberReorderableLazyGridState(
        lazyGridState = gridState,
        scrollThresholdPadding = PaddingValues(16.dp)
    ) { from, to ->
        // 处理数据重排
        val fromIndex = from.index
        val toIndex = to.index

        // 只有在媒体列表范围内才移动 (排除最后的 Add 按钮)
        if (toIndex < mediaList.size && fromIndex < mediaList.size) {
            val item = mediaList.removeAt(fromIndex)
            mediaList.add(toIndex, item)
            onMediaListChange(mediaList)
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "媒体文件 (长按拖拽排序，首张为主图)",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )

        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Adaptive(minSize = 100.dp), // 自适应网格
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp, max = 500.dp) // 给一个高度限制，允许内部滚动
        ) {
            // 3. 渲染媒体列表
            itemsIndexed(mediaList, key = { _, item -> item.id }) { index, item ->
                ReorderableItem(
                    state = reorderableState,
                    key = item.id
                ) { isDragging ->
                    val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp)

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .shadow(elevation, RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .clip(RoundedCornerShape(8.dp))
                            // 绑定拖拽事件 (长按触发)
                            .draggableHandle(
                                onDragStarted = { /* 可选：震动反馈 */ },
                                interactionSource = remember { MutableInteractionSource() }
                            )
                    ) {
                        // 图片/视频预览
                        if (!item.isVideo) {
                            AsyncImage(
                                model = item.file, // Coil3 通常支持 PlatformFile，如果不支持需转 path 或 bytes
                                contentDescription = "Product Media",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // 视频遮罩
                        if (item.isVideo) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayCircle,
                                    contentDescription = "Video",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        // 主图标签 (Index 0)
                        if (index == 0) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(bottomEnd = 8.dp),
                                modifier = Modifier.align(Alignment.TopStart)
                            ) {
                                Text(
                                    text = "主图",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // 序号标签 (方便对应 FileKit 排序)
                        if (index > 0) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.8f),
                                shape = RoundedCornerShape(bottomStart = 8.dp),
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // 删除按钮
                        IconButton(
                            onClick = {
                                mediaList.removeAt(index)
                                onMediaListChange(mediaList)
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(2.dp)
                                .size(24.dp)
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // 4. 添加按钮 (始终作为最后一项)
            item {
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { launcher.launch() },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Media",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = "上传",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}