package org.dsqrwym.shared.localization

import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.readResourceBytes

object LocalizationManager {
    private val localizedString = mutableMapOf<String, Map<String, String>>()
    private var currentLocale: String = "zh-CN"

    init {
        @Suppress("OPT_IN_USAGE")
        kotlinx.coroutines.GlobalScope.launch{
            loadLocale(currentLocale)
        }
    }

    suspend fun setLocale(locale: String) {
        if (currentLocale != locale) {
            currentLocale = locale
            loadLocale(locale)
        }
    }

    @OptIn(InternalResourceApi::class)
    private suspend fun loadLocale(locale: String) {
        if (localizedString.containsKey(locale)) return // 已经加载

        try {
            val resourceName = "files/$locale.json"
            val resource = readResourceBytes(resourceName)
            val jsonString = resource.decodeToString()
            val json = Json.parseToJsonElement(jsonString)
            localizedString[locale] = flattenJson(json)
            println(localizedString.toList().toString())
            println(LocalizationManager.getString("login.background.content_description"))
        } catch (e: Exception) {
            if (currentLocale != "zh-CN") {
                setLocale("zh-CN")
            }
            println("Error loading locale: $locale")
            println(e.message)

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