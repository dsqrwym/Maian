package org.dsqrwym.shared.util.clipboard

/**
 * A cross‑platform representation of things that may be copied to the system clipboard.
 *
 * Platforms may support only a subset (e.g., Text). Unsupported types should return false.
 */
sealed class SharedClipboardData {
    data class Text(val value: String) : SharedClipboardData()
    data class Image(val bytes: ByteArray, val mime: String) : SharedClipboardData() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Image) return false

            if (!bytes.contentEquals(other.bytes)) return false
            if (mime != other.mime) return false

            return true
        }

        override fun hashCode(): Int {
            var result = bytes.contentHashCode()
            result = 31 * result + mime.hashCode()
            return result
        }
    }

    data class Files(val files: List<String>) : SharedClipboardData()
}
