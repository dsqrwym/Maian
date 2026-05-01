package org.dsqrwym.business.ui.media.model

import io.github.vinceglb.filekit.PlatformFile

sealed interface MediaSource {
    data class Local(val file: PlatformFile) : MediaSource
    data class Remote(val url: String, val mimeType: String? = null) : MediaSource
}

data class UploadMediaItem(
    val localId: String,            // UI / Reorder 唯一 ID（永不变更）
    val source: MediaSource,         // 本地文件或已上传的远程文件
    val type: MediaType,
    val uploadState: UploadState,    // Idle / Uploading / Success / Failed
    val progress: Float = 0f,        // 0f..1f
    val serverId: String? = null     // 上传完成后回填
)

data class UploadedProductFile(
    val fileId: String,
    val sort: Int,
    val url: String,
    val mimeType: String? = null
)

enum class UploadState {
    Idle,
    Uploading,
    Success,
    Failed
}
