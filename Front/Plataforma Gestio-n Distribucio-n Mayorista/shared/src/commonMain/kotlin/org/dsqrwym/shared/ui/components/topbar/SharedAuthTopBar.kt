package org.dsqrwym.shared.ui.components.topbar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.SharedRes
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.login_button_back_button_content_description

@Composable
fun SharedAuthTopBar(
    title: String? = null,
    onBackButtonClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        IconButton(onClick = onBackButtonClick) {
            Icon(
                Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                stringResource(SharedRes.string.login_button_back_button_content_description),
                modifier = Modifier.fillMaxSize().scale(1.3f),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }

        if (title != null) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 36.sp,
                fontWeight = FontWeight.W800,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}