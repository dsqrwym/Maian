package org.dsqrwym.shared.data.file

sealed interface SharedUploadEvent {
    data class Progress(val value: Float) : SharedUploadEvent
    data class Success(val serverFileId: String) : SharedUploadEvent
    data class Error(val message: String?) : SharedUploadEvent
}