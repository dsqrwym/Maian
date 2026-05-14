package org.dsqrwym.standard.ui.component.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import org.dsqrwym.shared.drawable.SharedIcons
import org.dsqrwym.shared.ui.media.SharedAsyncImage
import org.dsqrwym.shared.util.modifier.placeholderWithShimmer

@Composable
internal fun CartImageBox(
    imageUrl: String?,
    contentDescription: String,
    modifier: Modifier,
    isLoading: Boolean,
    onClick: (() -> Unit)?,
) {
    Box(
        modifier = modifier
            .placeholderWithShimmer(isLoading)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(
                0.5.dp,
                MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(10.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        SharedAsyncImage(
            model = imageUrl ?: SharedIcons.MaianLogo,
            modifier = Modifier
                .fillMaxSize()
                .then(if (imageUrl != null && onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            zoomable = false,
            enableContextMenu = false,
        )
    }
}
