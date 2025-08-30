package org.dsqrwym.shared.util.settings

import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SettingsListener
import java.util.prefs.PreferenceChangeEvent
import java.util.prefs.PreferenceChangeListener
import java.util.prefs.Preferences

private class EncryptedPreferencesSettings(
    private val delegate: Preferences
) : ObservableSettings {

    /**
     * A factory that can produce [Settings] instances.
     *
     * On the JVM platform, this class creates `Settings` objects backed by [Preferences].
     */
    class Factory(private val rootPreferences: Preferences = Preferences.userRoot()) : Settings.Factory {
        override fun create(name: String?): PreferencesSettings {
            val preferences = if (name != null) rootPreferences.node(name) else rootPreferences
            return PreferencesSettings(preferences)
        }
    }

    override val keys: Set<String> get() = delegate.keys().toSet()
    override val size: Int get() = delegate.keys().size

    override fun clear(): Unit = delegate.clear()

    override fun remove(key: String): Unit = delegate.remove(key)

    override fun hasKey(key: String): Boolean = key in delegate.keys()

    override fun putInt(key: String, value: Int): Unit = delegate.putInt(key, value)

    override fun getInt(key: String, defaultValue: Int): Int = delegate.getInt(key, defaultValue)

    override fun getIntOrNull(key: String): Int? =
        if (key in delegate.keys()) delegate.getInt(key, 0) else null

    override fun putLong(key: String, value: Long): Unit = delegate.putLong(key, value)

    override fun getLong(key: String, defaultValue: Long): Long = delegate.getLong(key, defaultValue)

    override fun getLongOrNull(key: String): Long? =
        if (key in delegate.keys()) delegate.getLong(key, 0L) else null

    override fun putString(key: String, value: String): Unit = delegate.put(key, value)

    override fun getString(key: String, defaultValue: String): String = delegate.get(key, defaultValue)

    override fun getStringOrNull(key: String): String? =
        if (key in delegate.keys()) delegate.get(key, "") else null

    override fun putFloat(key: String, value: Float): Unit = delegate.putFloat(key, value)

    override fun getFloat(key: String, defaultValue: Float): Float = delegate.getFloat(key, defaultValue)

    override fun getFloatOrNull(key: String): Float? =
        if (key in delegate.keys()) delegate.getFloat(key, 0f) else null

    override fun putDouble(key: String, value: Double): Unit = delegate.putDouble(key, value)

    override fun getDouble(key: String, defaultValue: Double): Double = delegate.getDouble(key, defaultValue)

    override fun getDoubleOrNull(key: String): Double? =
        if (key in delegate.keys()) delegate.getDouble(key, 0.0) else null

    override fun putBoolean(key: String, value: Boolean): Unit = delegate.putBoolean(key, value)

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean = delegate.getBoolean(key, defaultValue)

    override fun getBooleanOrNull(key: String): Boolean? =
        if (key in delegate.keys()) delegate.getBoolean(key, false) else null

    override fun addIntListener(
        key: String,
        defaultValue: Int,
        callback: (Int) -> Unit
    ): SettingsListener =
        addListener(key) { callback(getInt(key, defaultValue)) }

    override fun addLongListener(
        key: String,
        defaultValue: Long,
        callback: (Long) -> Unit
    ): SettingsListener =
        addListener(key) { callback(getLong(key, defaultValue)) }

    override fun addStringListener(
        key: String,
        defaultValue: String,
        callback: (String) -> Unit
    ): SettingsListener =
        addListener(key) { callback(getString(key, defaultValue)) }

    override fun addFloatListener(
        key: String,
        defaultValue: Float,
        callback: (Float) -> Unit
    ): SettingsListener =
        addListener(key) { callback(getFloat(key, defaultValue)) }

    override fun addDoubleListener(
        key: String,
        defaultValue: Double,
        callback: (Double) -> Unit
    ): SettingsListener =
        addListener(key) { callback(getDouble(key, defaultValue)) }

    override fun addBooleanListener(
        key: String,
        defaultValue: Boolean,
        callback: (Boolean) -> Unit
    ): SettingsListener =
        addListener(key) { callback(getBoolean(key, defaultValue)) }

    override fun addIntOrNullListener(
        key: String,
        callback: (Int?) -> Unit
    ): SettingsListener =
        addListener(key) { callback(getIntOrNull(key)) }

    override fun addLongOrNullListener(
        key: String,
        callback: (Long?) -> Unit
    ): SettingsListener =
        addListener(key) { callback(getLongOrNull(key)) }

    override fun addStringOrNullListener(
        key: String,
        callback: (String?) -> Unit
    ): SettingsListener =
        addListener(key) { callback(getStringOrNull(key)) }

    override fun addFloatOrNullListener(
        key: String,
        callback: (Float?) -> Unit
    ): SettingsListener =
        addListener(key) { callback(getFloatOrNull(key)) }

    override fun addDoubleOrNullListener(
        key: String,
        callback: (Double?) -> Unit
    ): SettingsListener =
        addListener(key) { callback(getDoubleOrNull(key)) }

    override fun addBooleanOrNullListener(
        key: String,
        callback: (Boolean?) -> Unit
    ): SettingsListener =
        addListener(key) { callback(getBooleanOrNull(key)) }

    private fun addListener(key: String, callback: () -> Unit): SettingsListener {
        var prev = delegate.get(key, null)

        val prefsListener =
            PreferenceChangeListener { event: PreferenceChangeEvent ->
                val updatedKey = event.key
                if (updatedKey != key) return@PreferenceChangeListener

                /*
                 We'll get called here on any update to the underlying Preferences delegate. We use a cache to determine
                 whether the value at this listener's key changed before calling the user-supplied callback.
                 */
                val current = event.newValue
                if (prev != current) {
                    callback()
                    prev = current
                }
            }
        delegate.addPreferenceChangeListener(prefsListener)
        return Listener(delegate, prefsListener)
    }

    /**
     * A handle to a listener instance returned by one of the addListener methods of [ObservableSettings], so it can be
     * deactivated as needed.
     *
     * On the JVM platform, this is a wrapper around [PreferenceChangeListener].
     */
    class Listener(
        private val preferences: Preferences,
        private val listener: PreferenceChangeListener
    ) : SettingsListener {
        override fun deactivate() {
            try {
                preferences.removePreferenceChangeListener(listener)
            } catch (e: IllegalArgumentException) {
                // Ignore error due to unregistered listener to match behavior of other platforms
            }
        }
    }
}


actual fun initSharedSettingsProvider() {
    SharedSettingsProvider.plain = PreferencesSettings(Preferences.userRoot().node("plain_settings"))

    SharedSettingsProvider.secure = PreferencesSettings(Preferences.userRoot().node("secure_settings"))
}
