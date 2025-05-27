package org.dsqrwym.shared.drawable

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.DrawableResource
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.Res
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.image_vertical_background
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.image_vertical_background_dark

@Composable
fun getImageMobileBackground(): DrawableResource {
    val isDarkTheme = isSystemInDarkTheme()
    if (isDarkTheme) {
        return Res.drawable.image_vertical_background_dark
    }
    return Res.drawable.image_vertical_background
}