package org.dsqrwym.business.ui.media

import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Rect
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.mimeType
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.size
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import maian.business.generated.resources.*
import org.dsqrwym.business.ui.media.model.MediaType.*
import org.dsqrwym.business.ui.media.model.MediaSource
import org.dsqrwym.business.ui.media.model.UploadedProductFile
import org.dsqrwym.business.ui.media.model.UploadMediaItem
import org.dsqrwym.business.ui.media.model.UploadState
import org.dsqrwym.shared.data.file.SharedUploadEvent
import org.dsqrwym.shared.data.file.SharedUploadRepository
import org.dsqrwym.shared.ui.components.containers.UiState
import org.dsqrwym.shared.ui.viewmodels.MySnackbarViewModel
import org.jetbrains.compose.resources.getString
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class MediaPickerViewModel(
    private val maxItemCount: Int = 10,
    val maxVideo: Int? = 1,
    val maxImage: Int? = 9,
    // 200MB = 200 * 1024 (KB) * 1024 (B)
    val maxItemSize: Long = 200 * 1024 * 1024L,
    private val uploadRepository: SharedUploadRepository? = null,
    private val snackbarViewModel: MySnackbarViewModel? = null,
    coroutineScope: CoroutineScope? = null
) : ViewModel() {
    val mediaPickerUiState by derivedStateOf {
        when{
            _mediaItems.isEmpty() -> UiState.Idle
            _mediaItems.any { it.uploadState == UploadState.Uploading } -> UiState.Loading
            _mediaItems.any { it.uploadState == UploadState.Failed } -> UiState.Error
            _mediaItems.all { it.uploadState == UploadState.Success } -> UiState.Success
            else -> UiState.Idle
        }
    }

    private val _mediaItems = mutableStateListOf<UploadMediaItem>()
    val mediaItems: List<UploadMediaItem> = _mediaItems

    private var _videoCount by mutableStateOf(0)
    val videoCount: Int get() = _videoCount
    private var _imageCount by mutableStateOf(0)
    val imageCount: Int get() = _imageCount

    val canAddMore: Boolean
        get() = _mediaItems.size < maxItemCount

    val remainingSlots: Int
        get() = maxItemCount - _mediaItems.size

    private val scope = coroutineScope ?: viewModelScope

    /* -----------------------------
     * Add / Remove / Reorder
     * ----------------------------- */

    fun addLocalFiles(files: List<PlatformFile>) {
        files.forEach { file ->
            if (_mediaItems.size >= maxItemCount) return

            if (file.size() > maxItemSize) {
                scope.launch {
                    snackbarViewModel?.showInfo(
                        getString(
                            BusinessRes.string.media_file_too_large_mb,
                            file.name,
                            file.size() / 1024 / 1024
                        )
                    )
                }
                return@forEach
            }

            val type = if (file.mimeType()?.primaryType == "video")
                VIDEO
            else
                IMAGE

            when (type) {
                VIDEO -> {
                    if (maxVideo != null && _videoCount >= maxVideo) return@forEach
                }

                IMAGE -> {
                    if (maxImage != null && _imageCount >= maxImage) return@forEach
                }

                DOCUMENT -> Unit
            }

            val item = UploadMediaItem(
                localId = generateLocalId(),
                source = MediaSource.Local(file),
                type = type,
                uploadState = UploadState.Idle
            )

            if (type == VIDEO) _videoCount++ else _imageCount++

            _mediaItems.add(item)
            startUpload(item)
        }
    }

    fun addUploadedProductFiles(files: List<UploadedProductFile>) {
        files.sortedBy { it.sort }.forEach { file ->
            if (_mediaItems.size >= maxItemCount) return
            if (_mediaItems.any { it.serverId == file.fileId }) return@forEach

            val type = if (file.mimeType?.substringBefore("/") == "video")
                VIDEO
            else
                IMAGE

            when (type) {
                VIDEO -> {
                    if (maxVideo != null && _videoCount >= maxVideo) return@forEach
                }

                IMAGE -> {
                    if (maxImage != null && _imageCount >= maxImage) return@forEach
                }

                DOCUMENT -> Unit
            }

            // 已属于产品的文件视为上传完成，只加入 UI 队列，不重复上传。
            _mediaItems.add(
                UploadMediaItem(
                    localId = "uploaded-${file.fileId}",
                    source = MediaSource.Remote(file.url, file.mimeType),
                    type = type,
                    uploadState = UploadState.Success,
                    progress = 1f,
                    serverId = file.fileId
                )
            )

            if (type == VIDEO) _videoCount++ else _imageCount++
        }
    }

    fun reorder(from: Int, to: Int) {
        if (from == to) return
        if (from !in _mediaItems.indices || to !in _mediaItems.indices) return
        _mediaItems.add(to, _mediaItems.removeAt(from))
    }

    fun removeById(localId: String) {
        uploadRepository?.cancel(localId, scope)
        val iterator = _mediaItems.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            if (item.localId == localId) {
                if (item.type == VIDEO) {
                    _videoCount--
                } else {
                    _imageCount--
                }
                iterator.remove()
                break
            }
        }
    }

    fun clear() {
        _mediaItems.clear()
        _videoCount = 0
        _imageCount = 0
    }

    /* -----------------------------
     * Drag-to-Delete Interaction
     * ----------------------------- */

    var draggingItemId by mutableStateOf<String?>(null)
        private set

    private var deleteZoneRect: Rect? = null

    var isHoveringDeleteZone by mutableStateOf(false)
        private set

    fun onDragStart(itemId: String) {
        draggingItemId = itemId
        isHoveringDeleteZone = false
    }

    fun onDragMove(itemBounds: Rect) {
        val zone = deleteZoneRect ?: return
        isHoveringDeleteZone = zone.overlaps(itemBounds)
    }

    fun onDragEnd() {
        if (isHoveringDeleteZone) {
            draggingItemId?.let { removeById(it) }
        }
        resetDragState()
    }

    fun updateDeleteZone(bounds: Rect) {
        deleteZoneRect = bounds
    }

    private fun resetDragState() {
        draggingItemId = null
        isHoveringDeleteZone = false
    }

    private fun startUpload(item: UploadMediaItem) {
        val source = item.source as? MediaSource.Local ?: return
        uploadRepository?.let { repository ->
            // 标记正在上传
            updateItem(item.localId) { it.copy(uploadState = UploadState.Uploading) }
            scope.launch {
                val flow = repository.uploadFile(
                    localId = item.localId,
                    file = source.file,
                    scope = scope
                )

                flow.collect { event ->
                    when (event) {
                        is SharedUploadEvent.Progress ->
                            onUploadProgress(item.localId, event.value)

                        is SharedUploadEvent.Success ->
                            onUploadSuccess(item.localId, event.serverFileId)

                        is SharedUploadEvent.Error ->
                            onUploadFailed(item.localId)
                    }
                }
            }
        }
    }

    fun retryUpload(localId: String) {
        _mediaItems.find { it.localId == localId }?.let {
            if (it.source is MediaSource.Local) startUpload(it)
        }
    }

    private fun onUploadProgress(localId: String, progress: Float) {
        updateItem(localId) { it.copy(progress = progress, uploadState = UploadState.Uploading) }
    }

    private fun onUploadSuccess(localId: String, serverId: String) {
        updateItem(localId) {
            it.copy(
                uploadState = UploadState.Success,
                serverId = serverId,
                progress = 1f
            )
        }
    }

    private fun onUploadFailed(localId: String) {
        updateItem(localId) { it.copy(uploadState = UploadState.Failed) }
    }

    private fun updateItem(
        localId: String,
        block: (UploadMediaItem) -> UploadMediaItem
    ) {
        val index = _mediaItems.indexOfFirst { it.localId == localId }
        if (index != -1) {
            val newItem = block(_mediaItems[index])
            _mediaItems[index] = newItem
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun generateLocalId(): String =
        Uuid.generateV7().toString()
}


