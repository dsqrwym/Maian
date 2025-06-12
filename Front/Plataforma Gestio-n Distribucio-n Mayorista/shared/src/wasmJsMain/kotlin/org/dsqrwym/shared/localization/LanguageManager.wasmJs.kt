package org.dsqrwym.shared.localization

import org.dsqrwym.shared.util.log.sharedlog

@JsFun("() => window.navigator.language")
external fun getBrowserLanguage() : String?

@JsFun("lang => console.log('Detected language:', lang)")
external fun logLanguage(lang: String)
actual fun getLocaleLanguage(): String {
    val lang = getBrowserLanguage() ?: "en"
//    logLanguage(lang)
    sharedlog(message = "Detected language: $lang")
    return lang
}