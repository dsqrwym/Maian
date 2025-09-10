package org.dsqrwym.shared.drawable

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import org.dsqrwym.shared.LocalIsDarkTheme
import org.jetbrains.compose.resources.DrawableResource
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.SharedRes
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.image_vertical_background
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.image_vertical_background_dark

/**
 * EN: Centralized accessors for shared image resources.
 * ZH: 共享图片资源的集中访问器。
 */
object SharedImages {
    @Composable
    fun background(): DrawableResource {
        return if (LocalIsDarkTheme.current) {
            SharedRes.drawable.image_vertical_background_dark
        } else {
            SharedRes.drawable.image_vertical_background
        }
    }
}


/**
 * EN: Centralized accessors for shared vector icons.
 * ZH: 共享矢量图标的集中访问器。
 */
object SharedIcons {
    val MaianLogo: ImageVector get() = org.dsqrwym.shared.drawable.MaianLogo
}