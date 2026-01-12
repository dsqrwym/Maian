package org.dsqrwym.shared.ui.media

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpOffset
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.copy
import maian.shared.generated.resources.save
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SharedMediaContextMenu(
    showMenu: Boolean,
    onDismiss: () -> Unit,
    // Determines if we use a BottomSheet (true) or Dropdown (false)
    useBottomSheet: Boolean,
    // Position for the Dropdown
    clickOffset: Offset,
    onCopy: () -> Unit,
    onSave: () -> Unit,
    enableCopy: Boolean = true,
    enableSave: Boolean = true
) {
    if (!showMenu) return

    val density = LocalDensity.current
    val copyText = stringResource(SharedRes.string.copy)
    val saveText = stringResource(SharedRes.string.save)

    if (useBottomSheet) {
        // Mobile / Touch Layout
        ModalBottomSheet(onDismissRequest = onDismiss) {
            if (enableCopy) {
                ListItem(
                    headlineContent = { Text(copyText) },
                    leadingContent = {
                        Icon(Icons.Outlined.ContentCopy, contentDescription = copyText)
                    },
                    modifier = Modifier.combinedClickable(onClick = {
                        onCopy()
                        onDismiss()
                    })
                )
            }
            if (enableSave) {
                ListItem(
                    headlineContent = { Text(saveText) },
                    leadingContent = { Icon(Icons.Outlined.Download, contentDescription = saveText) },
                    modifier = Modifier.combinedClickable(onClick = {
                        onSave()
                        onDismiss()
                    })
                )
            }
        }
    } else {
        // Desktop / Mouse Layout
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = onDismiss,
            offset = with(density) { DpOffset(clickOffset.x.toDp(), clickOffset.y.toDp()) }
        ) {
            if (enableCopy) {
                DropdownMenuItem(
                    text = { Text(copyText) },
                    leadingIcon = { Icon(Icons.Outlined.ContentCopy, contentDescription = copyText) },
                    onClick = {
                        onCopy()
                        onDismiss()
                    }
                )
            }
            if (enableSave) {
                DropdownMenuItem(
                    text = { Text(saveText) },
                    leadingIcon = { Icon(Icons.Outlined.Download, contentDescription = saveText) },
                    onClick = {
                        onSave()
                        onDismiss()
                    }
                )
            }
        }
    }
}