package org.dsqrwym.business.ui.workspace

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.serialization.Serializable

@Serializable
enum class BusinessAuxiliarySurface {
    Editor,
    Preview,
}

@Stable
class BusinessAuxiliaryWorkspaceState(
    initialSurface: BusinessAuxiliarySurface? = null,
) {
    var currentSurface by mutableStateOf(initialSurface)
        private set

    val isOpen: Boolean
        get() = currentSurface != null

    fun open(surface: BusinessAuxiliarySurface = BusinessAuxiliarySurface.Editor) {
        currentSurface = surface
    }

    fun close() {
        currentSurface = null
    }

    fun toggle(surface: BusinessAuxiliarySurface = BusinessAuxiliarySurface.Editor) {
        currentSurface = if (currentSurface == surface) null else surface
    }
}

@Composable
fun rememberBusinessAuxiliaryWorkspaceState(
    initialSurface: BusinessAuxiliarySurface? = null,
): BusinessAuxiliaryWorkspaceState = remember {
    BusinessAuxiliaryWorkspaceState(initialSurface)
}
