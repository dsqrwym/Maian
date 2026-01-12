package org.dsqrwym.business.ui.media.model

import io.github.vinceglb.filekit.PlatformFile

data class UploadMediaItem(
    val localId: String,            // UI / Reorder 唯一 ID（永不变化）
    val file: PlatformFile,          // 本地文件
    val type: MediaType,
    val uploadState: UploadState,    // Idle / Uploading / Success / Failed
    val progress: Float = 0f,        // 0f..1f
    val serverId: String? = null     // 上传完成后回填
)

enum class UploadState {
    Idle,
    Uploading,
    Success,
    Failed
}