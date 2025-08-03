package org.dsqrwym.shared.drawable

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import org.dsqrwym.shared.drawable.sharedicons.Cancel
import org.dsqrwym.shared.drawable.sharedicons.Pending
import org.dsqrwym.shared.theme.DarkAppColorScheme
import org.jetbrains.compose.resources.DrawableResource
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.Res
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.image_vertical_background
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.image_vertical_background_dark

object SharedImages {
    @Composable
    fun background(): DrawableResource {
        return if (MaterialTheme.colorScheme == DarkAppColorScheme) {
            Res.drawable.image_vertical_background_dark
        } else {
            Res.drawable.image_vertical_background
        }
    }
}


object SharedIcons {
    val MaianLogo: ImageVector get() = org.dsqrwym.shared.drawable.MaianLogo
    val InProgress: ImageVector get() = Pending
    val CircleError: ImageVector get() = Cancel
}