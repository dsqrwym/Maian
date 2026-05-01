package org.dsqrwym.shared.util.formatter

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.jvm.JvmName

@JvmName("asStringNullable")
@Composable
fun StringResource?.asString(): String? {
    return this?.let { stringResource(it) }
}

@JvmName("asStringNotNull")
@Composable
fun StringResource.asString(): String {
    return stringResource(this)
}