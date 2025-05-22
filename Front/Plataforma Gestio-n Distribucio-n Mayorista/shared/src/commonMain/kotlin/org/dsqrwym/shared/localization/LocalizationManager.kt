package org.dsqrwym.shared.localization

import kotlinx.coroutines.CompletableDeferred
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.readResourceBytes

object LocalizationManager {
    private val localizedString = mutableMapOf<String, Map<String, String>>()
    private const val DEFAULT_LOCALE = "zh-CN"
    private var currentLocale: String = DEFAULT_LOCALE
    private val initializationDeferred = CompletableDeferred<Unit>()

    suspend fun initialize(locale: String = DEFAULT_LOCALE) {
        if (initializationDeferred.isCompleted) return

        currentLocale = locale
        val loaded = loadLocale(locale)

        if (!loaded && currentLocale != DEFAULT_LOCALE) {
            currentLocale = DEFAULT_LOCALE
            loadLocale(currentLocale)
        }

        initializationDeferred.complete(Unit)
    }

    suspend fun setLocale(locale: String) {
        if (currentLocale != locale) {
            currentLocale = locale
            if (loadLocale(locale)) currentLocale = locale
        }
    }

    @OptIn(InternalResourceApi::class)
    private suspend fun loadLocale(locale: String): Boolean {
        if (localizedString.containsKey(locale)) return true// 已经加载

        return try {
            val resourceName = "files/$locale.json"
            val resource = readResourceBytes(resourceName)
            val jsonString = resource.decodeToString()
            val json = Json.parseToJsonElement(jsonString)
            localizedString[locale] = flattenJson(json)
            true
        } catch (e: Exception) {
            if (currentLocale != DEFAULT_LOCALE) {
                setLocale(DEFAULT_LOCALE)
            }
            println("Error loading locale: $locale")
            println(e.message)
            false
        }
    }

    private fun flattenJson(element: JsonElement): Map<String, String> {
        val result = mutableMapOf<String, String>()
        when (element) {
            is JsonObject -> flattenObject("", element, result)
            else -> error("Root must be a JSON object")
        }
        return result
    }

    private fun flattenObject(
        prefix: String,
        jsonObject: JsonObject,
        result: MutableMap<String, String>
    ) {
        jsonObject.forEach { (key, value) ->
            val fullKey = if (prefix.isEmpty()) key else "$prefix.$key"
            when (value) {
                is JsonPrimitive -> result[fullKey] = value.content
                is JsonObject -> flattenObject(fullKey, value, result)
                else -> Unit // 忽略其他类型
            }
        }
    }

    fun getString(key: String): String {
        if (!initializationDeferred.isCompleted) {
            println("Warning: getString('$key') called before init.")
            return key
        }
        return localizedString[currentLocale]?.get(key) ?: key // 如果找不到对应的 key，返回 key 本身作为默认值
    }

    fun getString(key: String, vararg formatArgs: Any): String {
        val rawString = getString(key)
        var result = rawString
        formatArgs.forEachIndexed { index, arg ->
            result = result.replace("%${index + 1}\$s", arg.toString())
        }
        return result
    }

    fun getCurrentLocale(): String = currentLocale
    fun getSupportedLocales(): List<String> = localizedString.keys.toList()
}