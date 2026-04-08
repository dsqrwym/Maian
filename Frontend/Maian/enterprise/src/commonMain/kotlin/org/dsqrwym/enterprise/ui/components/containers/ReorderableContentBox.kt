package org.dsqrwym.enterprise.ui.components.containers

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import org.dsqrwym.shared.ui.components.containers.SharedOverlayContentBox

@Composable
fun ReorderableContentBox(
    index: Int,
    modifier: Modifier,
    isDragging: Boolean,
    onClick: (() -> Unit)? = null,
    content: @Composable (BoxScope.() -> Unit)
) {
    SharedOverlayContentBox(
        modifier = modifier
            .shadow(
                elevation = if (isDragging) 16.dp else 0.dp,
                shape = RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp))
            .then(onClick?.let {
                Modifier.clickable(onClick = {
                    if (!isDragging) {
                        it()
                    }
                })
            } ?: Modifier),
        topEndOverlay = {
            Text(
                text = " ${index + 1} ",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.alpha(if (isDragging) 0f else 1f)
            )
        }
    ) {
        content()
    }
}
