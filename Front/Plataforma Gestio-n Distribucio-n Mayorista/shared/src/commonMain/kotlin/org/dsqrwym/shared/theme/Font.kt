package org.dsqrwym.shared.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.MiSansVF
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.Res


@Composable
fun MiSansNormalTypography() : Typography {
    val miSans = FontFamily(
        org.jetbrains.compose.resources.Font(resource = Res.font.MiSansVF)
    )

    return Typography(
        displaySmall = MaterialTheme.typography.displaySmall.copy(fontFamily = miSans),
        displayMedium = MaterialTheme.typography.displayMedium.copy(fontFamily = miSans),
        displayLarge = MaterialTheme.typography.displayLarge.copy(fontFamily = miSans),
        headlineSmall = MaterialTheme.typography.headlineSmall.copy(fontFamily = miSans),
        headlineMedium = MaterialTheme.typography.headlineMedium.copy(fontFamily = miSans),
        headlineLarge = MaterialTheme.typography.headlineLarge.copy(fontFamily = miSans),
        titleSmall = MaterialTheme.typography.titleSmall.copy(fontFamily = miSans),
        titleMedium = MaterialTheme.typography.titleMedium.copy(fontFamily = miSans),
        titleLarge = MaterialTheme.typography.titleLarge.copy(fontFamily = miSans),
        bodySmall = MaterialTheme.typography.bodySmall.copy(fontFamily = miSans),
        bodyMedium = MaterialTheme.typography.bodyMedium.copy(fontFamily = miSans),
        bodyLarge = MaterialTheme.typography.bodyLarge.copy(fontFamily = miSans),
        labelSmall = MaterialTheme.typography.labelSmall.copy(fontFamily = miSans),
        labelMedium = MaterialTheme.typography.labelMedium.copy(fontFamily = miSans),
        labelLarge = MaterialTheme.typography.labelLarge.copy(fontFamily = miSans)
    )
}