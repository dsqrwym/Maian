package org.dsqrwym.shared.language


import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.derivedStateOf

/** Provides a map of generated language implementations. Do not edit manually. */
object SharedLanguageMap {
    private const val DEFAULT_LANGUAGE = "zh-CN"
    private val _currentLanguage = mutableStateOf(DEFAULT_LANGUAGE)

    private val map = mapOf(
        "zh-CN" to LangZhCn,
    )

    val currentLanguageState: State<String> get() = _currentLanguage

    val currentStrings: State<SharedLanguage> = derivedStateOf {
        map[_currentLanguage.value] ?: map[DEFAULT_LANGUAGE]!!
    }

    fun setCurrentLanguage(language: String) {
        _currentLanguage.value = if (map.containsKey(language)) language else DEFAULT_LANGUAGE
    }

    fun getCurrentLanguage(): String = _currentLanguage.value

}