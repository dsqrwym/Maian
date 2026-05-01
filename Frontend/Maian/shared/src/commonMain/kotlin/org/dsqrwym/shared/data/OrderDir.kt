package org.dsqrwym.shared.data

import androidx.compose.runtime.Composable
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.order_dir_ascending
import maian.shared.generated.resources.order_dir_descending
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

enum class OrderDir(val value: String) {
    ASC("asc"),
    DESC("desc")
}

fun OrderDir.toStringResource(): StringResource =
    when (this) {
        OrderDir.ASC -> SharedRes.string.order_dir_ascending
        OrderDir.DESC -> SharedRes.string.order_dir_descending
    }

@Composable
fun OrderDir.displayName(): String = stringResource(toStringResource())
