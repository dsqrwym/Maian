package org.dsqrwym.shared.ui.viewmodels.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import org.dsqrwym.shared.data.local.SharedUserPreferences
import org.dsqrwym.shared.navigation.SharedNavSerializersModule
import org.dsqrwym.shared.util.log.SharedLog

@OptIn(FlowPreview::class)
class SharedNavigationViewModel(
    initRoute: NavKey,
    private val stackKey: String? = null,
    extraSerializersModule: SerializersModule? = null
) : ViewModel() {
    private val navJson = Json {
        serializersModule = SerializersModule {
            include(SharedNavSerializersModule)
            extraSerializersModule?.let { include(it) }
        }
        ignoreUnknownKeys = true
    }
    private val _backStack = MutableStateFlow(loadSavedStack() ?: listOf(initRoute))
    val backStack = _backStack.asStateFlow()

    init {
        viewModelScope.launch {
            backStack
                .drop(1)
                .debounce(300)
                .collect {
                    persistNow()
                }
        }
    }

    private fun loadSavedStack(): List<NavKey>? {
        SharedLog.log(
            stackKey?.let {
                """
                    stackkey: $stackKey
                    json: ${SharedUserPreferences.getNavigationStack(it)}
                """
            }?.trimIndent() ?: ""
        )
        if (stackKey == null) return null
        return try {
            val json = SharedUserPreferences.getNavigationStack(stackKey)
            if (json != null) {
                navJson.decodeFromString<List<NavKey>>(json)
            } else null
        } catch (e: Exception) {
            SharedLog.log("Deserialization error: ${e.message}")
            null
        }
    }

    private fun persistNow() {
        val json = try {
            navJson.encodeToString(_backStack.value)
        } catch (e: Exception) {
            SharedLog.log("Serialization error: ${e.message}")
            null
        }

        if (stackKey == null || json == null) return

        try {
            SharedUserPreferences.saveNavigationStack(stackKey, json)
        } catch (_: Exception) {
        }
    }

    fun navigate(route: NavKey) {
        _backStack.value += route
    }

    fun pop() {
        if (_backStack.value.size > 1) {
            _backStack.value = _backStack.value.dropLast(1)
        }
    }

    fun replace(route: NavKey) {
        _backStack.value = _backStack.value.dropLast(1) + route
    }

    fun clearAndNavigate(route: NavKey) {
        _backStack.value = listOf(route)
    }

    fun current(): NavKey = _backStack.value.last()

    fun flushImmediately() {
        persistNow()
    }
}
