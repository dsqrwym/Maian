package org.dsqrwym.shared.data.file.dto

import io.github.vinceglb.filekit.PlatformFile
import org.dsqrwym.shared.data.file.SharedUploadState

data class SharedUploadItem(
    val localId: String,            // UI / Reorder 唯一 ID（永不变化）
    val file: PlatformFile,          // 本地文件
    val mimeType: String,
    val uploadState: SharedUploadState,    // Idle / Uploading / Success / Failed
    val progress: Float = 0f,        // 0f..1f
    val serverId: String? = null     // 上传完成后回填
)