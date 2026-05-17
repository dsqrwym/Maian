package org.dsqrwym.shared.ui.components.order.detail

import androidx.compose.runtime.Composable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.empty_field_placeholder
import org.dsqrwym.shared.data.orders.dto.SharedOrderDetail
import org.dsqrwym.shared.data.orders.dto.SharedOrderDetailItem
import org.dsqrwym.shared.data.orders.dto.SharedOrderPartnerSnapshot
import org.dsqrwym.shared.localization.LanguageManager
import org.dsqrwym.shared.util.formatter.notBlankOrNull
import org.jetbrains.compose.resources.stringResource

@Composable
fun String?.orderDetailPlaceholder(): String =
    notBlankOrNull() ?: stringResource(SharedRes.string.empty_field_placeholder)

fun SharedOrderPartnerSnapshot.orderDetailDisplayName(): String? =
    companyName.notBlankOrNull()
        ?: displayName.notBlankOrNull()
        ?: contactName.notBlankOrNull()
        ?: email.notBlankOrNull()

fun SharedOrderDetail.orderDetailDistinctItemTypes(): Int =
    items
        .map { it.variantProductId ?: it.productId ?: it.variantProductCode.ifBlank { it.productCode } }
        .distinct()
        .size

fun SharedOrderDetailItem.orderDetailLocalizedProductName(languageCode: String): String =
    productTranslationsSnapshot
        .orderDetailLocalizedTranslationValue(languageCode, "name", "product_name")
        ?: productName

fun SharedOrderDetailItem.orderDetailLocalizedProductTitle(languageCode: String): String? =
    productTranslationsSnapshot
        .orderDetailLocalizedTranslationValue(languageCode, "title", "product_title")
        ?: productTitle

fun JsonElement?.orderDetailCompactJsonValue(): String =
    this?.toString()
        ?.trim()
        ?.takeIf { it.isNotBlank() && it != "{}" && it != "[]" && it != "null" }
        ?.let { if (it.length > 240) it.take(237) + "..." else it }
        .orEmpty()

fun JsonElement?.orderDetailLocalizedTranslationValue(
    languageCode: String,
    vararg valueKeys: String,
): String? {
    val element = this ?: return null
    val normalizedLanguage = LanguageManager.normalizeLanguageCode(languageCode)
    val candidates = when (element) {
        is JsonArray -> element.mapNotNull { it as? JsonObject }
        is JsonObject -> buildList {
            element.entries.forEach { (key, value) ->
                when (value) {
                    is JsonObject -> add(value.withLanguageFallback(key))
                    is JsonPrimitive -> {
                        if (orderDetailLanguageMatches(normalizedLanguage, key)) {
                            add(buildJsonObjectFromPrimitive(languageCode = key, valueKey = valueKeys.firstOrNull(), value = value))
                        }
                    }
                    else -> Unit
                }
            }
            if (element.keys.any { it in orderDetailLanguageKeys }) add(element)
        }
        else -> emptyList()
    }

    return candidates
        .firstOrNull { candidate ->
            orderDetailLanguageKeys
                .mapNotNull { key -> candidate[key].jsonTextOrNull() }
                .any { orderDetailLanguageMatches(normalizedLanguage, it) }
        }
        ?.firstTextValue(valueKeys)
        ?.notBlankOrNull()
}

private val orderDetailLanguageKeys = setOf(
    "lang_code",
    "langCode",
    "language_code",
    "languageCode",
    "language",
    "locale",
)

private fun JsonObject.withLanguageFallback(languageCode: String): JsonObject =
    if (keys.any { it in orderDetailLanguageKeys }) {
        this
    } else {
        JsonObject(this + ("lang_code" to JsonPrimitive(languageCode)))
    }

private fun buildJsonObjectFromPrimitive(languageCode: String, valueKey: String?, value: JsonPrimitive): JsonObject =
    valueKey?.let { JsonObject(mapOf("lang_code" to JsonPrimitive(languageCode), it to value)) } ?: JsonObject(emptyMap())

private fun JsonObject.firstTextValue(keys: Array<out String>): String? =
    keys.firstNotNullOfOrNull { key -> this[key].jsonTextOrNull() }

private fun JsonElement?.jsonTextOrNull(): String? =
    (this as? JsonPrimitive)?.contentOrNull?.notBlankOrNull()

private fun orderDetailLanguageMatches(expectedNormalizedLanguage: String, candidateLanguage: String): Boolean {
    val normalizedCandidate = LanguageManager.normalizeLanguageCode(candidateLanguage)
    return normalizedCandidate == expectedNormalizedLanguage ||
            normalizedCandidate.substringBefore("-") == expectedNormalizedLanguage.substringBefore("-")
}
