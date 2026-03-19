package org.dsqrwym.shared.localization

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("() => Intl.DateTimeFormat().resolvedOptions().timeZone")
external fun jsGetTimeZone(): String

actual fun getSystemTimeZone(): String = jsGetTimeZone()