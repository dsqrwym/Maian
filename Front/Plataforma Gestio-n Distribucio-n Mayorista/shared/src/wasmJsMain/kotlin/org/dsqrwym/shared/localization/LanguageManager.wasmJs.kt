package org.dsqrwym.shared.localization

@JsFun("() => window.navigator.language")
external fun getBrowserLanguage() : String?

@JsFun("lang => console.log('Detected language:', lang)")
external fun logLanguage(lang: String)
actual fun getLocaleLanguage(): String {
    val lang = getBrowserLanguage() ?: "en"
    logLanguage(lang)
    return lang
}