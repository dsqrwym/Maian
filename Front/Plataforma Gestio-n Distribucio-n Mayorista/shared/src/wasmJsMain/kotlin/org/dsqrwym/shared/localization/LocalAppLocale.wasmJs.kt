package org.dsqrwym.shared.localization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.intl.Locale

external object Window {
    var CustomLocale: String?
}

actual object LocalAppLocale {
    private val LocalAppLocale = staticCompositionLocalOf { Locale.current }

    // 读当前系统/应用默认 Locale
    actual val current: String
        @Composable get() = LocalAppLocale.current.toString()

    @Composable
    actual infix fun provides(value: String?): ProvidedValue<*> {
        // 浏览器偏好语言是只读的，这里把自定义语言先塞到 window 全局变量里
        Window.CustomLocale = value?.replace('_', '-') // 转换成 BCP-47 风格
        return LocalAppLocale.provides(Locale.current) // 让 CompositionLocal 更新
    }
}