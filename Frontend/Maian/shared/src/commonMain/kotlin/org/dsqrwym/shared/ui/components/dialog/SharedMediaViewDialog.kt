package org.dsqrwym.shared.ui.components.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.dsqrwym.shared.ui.components.icon.SharedCloseIcon
import org.dsqrwym.shared.ui.media.SharedAsyncImage


@Composable
fun SharedImageViewDialog(
    model: String,
    imageName: String? = null,
    onDismissRequest: () -> Unit
) {
    SharedMediaViewDialog(onDismissRequest = onDismissRequest) {
        SharedAsyncImage(
            modifier = Modifier.wrapContentSize(),
            model = model,
            contentDescription = imageName ?: "图片",
            imageName = imageName
        )
    }
}


@Composable
fun SharedMediaViewDialog(
    onDismissRequest: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .clickable(
                    onClick = onDismissRequest,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ),
            contentAlignment = Alignment.Center
        ) {
            // 媒体内容区域
            content()

            // 关闭按钮
            FilledTonalIconButton(
                colors = IconButtonDefaults.filledTonalIconButtonColors()
                    .copy(containerColor = IconButtonDefaults.filledTonalIconButtonColors().containerColor.copy(alpha = 0.5f)),
                onClick = onDismissRequest,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                SharedCloseIcon(
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}