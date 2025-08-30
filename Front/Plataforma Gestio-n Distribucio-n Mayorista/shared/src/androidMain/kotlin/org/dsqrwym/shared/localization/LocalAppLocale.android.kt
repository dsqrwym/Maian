package org.dsqrwym.shared.localization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.util.*

actual object LocalAppLocale {
    private var default: Locale? = null

    // 读当前系统/应用默认 Locale
    actual val current: String
        @Composable get() = Locale.getDefault().toString()

    // 注入一个新的 Locale 值（或 null=还原默认）
    @Composable
    actual infix fun provides(value: String?): ProvidedValue<*> {
        val configuration = LocalConfiguration.current

        if (default == null) {
            default = Locale.getDefault()
        }

        val new = when (value) {
            null -> default!!
            else -> Locale(value)
        }
        Locale.setDefault(new)
        configuration.setLocale(new)
        // 把新的 config 通知资源系统（影响 stringResource 等）
        val resources = LocalContext.current.resources

        resources.updateConfiguration(configuration, resources.displayMetrics)
        // 把新的 configuration 以 CompositionLocal 的形式“提供”出去
        return LocalConfiguration.provides(configuration)
    }
}