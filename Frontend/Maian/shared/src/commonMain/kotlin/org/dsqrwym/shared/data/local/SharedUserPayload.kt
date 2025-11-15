package org.dsqrwym.shared.data.local

import kotlinx.serialization.json.Json
import org.dsqrwym.shared.data.user.SharedUserPayload
import org.dsqrwym.shared.util.secret.xorBytes
import org.dsqrwym.shared.util.settings.SharedSettingsProvider
import kotlin.io.encoding.Base64

object SharedUserPayloadStorage {
    private val storage = SharedSettingsProvider.plain
    private const val USER_PAYLOAD_KEY = "user_payload"
    private val json = Json {
        ignoreUnknownKeys = true
    }

    fun save(payload: SharedUserPayload) {
        val jsonString = json.encodeToString(payload)
        val encoded = Base64.encode(xorBytes(jsonString.encodeToByteArray()))
        storage.putString(USER_PAYLOAD_KEY, encoded)
    }

    fun get(): SharedUserPayload? {
        val jsonString = storage.getStringOrNull(USER_PAYLOAD_KEY) ?: return null
        val decoded = xorBytes(Base64.decode(jsonString))
        return json.decodeFromString(decoded.decodeToString())
    }

    fun clear() {
        storage.remove(USER_PAYLOAD_KEY)
    }
}