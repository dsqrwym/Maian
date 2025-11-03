package org.dsqrwym.shared.ui.components.dialog

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun ConfirmDeleteDialog(
    onDismissRequest: () -> Unit = {},
    onConfirm: () -> Unit = {},
    modifier: Modifier = Modifier,
    title: String = "你确认要删除吗？",
    text: String = "你确定要删除选中的元素吗？你将无法恢复数据。",
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        title = {
            Row (
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.DeleteForever,
                    contentDescription = "删除图标",
                    tint = MaterialTheme.colorScheme.error
                )
                Text(title)
            }
        },
        text = {
            Text(text)
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm()
                onDismissRequest()
            }) {
                Text("确认")
            }
        },
        dismissButton = {
            ElevatedButton(onClick = onDismissRequest) {
                Text("取消")
            }
        }
    )
}