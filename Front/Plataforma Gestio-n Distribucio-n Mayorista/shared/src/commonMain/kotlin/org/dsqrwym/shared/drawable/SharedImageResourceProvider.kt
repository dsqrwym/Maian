package org.dsqrwym.shared.drawable

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import org.dsqrwym.shared.theme.DarkAppColorScheme
import org.jetbrains.compose.resources.DrawableResource
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.Res
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.image_vertical_background
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.image_vertical_background_dark

@Composable
fun getImageMobileBackground(): DrawableResource {
    if (MaterialTheme.colorScheme == DarkAppColorScheme) {
        return Res.drawable.image_vertical_background_dark
    }
    return Res.drawable.image_vertical_background
}