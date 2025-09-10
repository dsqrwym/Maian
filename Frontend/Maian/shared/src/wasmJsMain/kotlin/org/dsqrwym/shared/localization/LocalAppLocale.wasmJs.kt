package org.dsqrwym.shared.localization
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.intl.Locale

external object window {
    var __customLocale: String?
}

actual object LocalAppLocale {
    private val LocalAppLocale = staticCompositionLocalOf { Locale(getLocaleLanguage()) }
    actual val current: String
        @Composable get() = LocalAppLocale.current.toString()

    @Composable
    actual infix fun provides(value: String?): ProvidedValue<*> {
        window.__customLocale = value?.replace('_', '-')
        return LocalAppLocale.provides(Locale.current)
    }
}