package org.dsqrwym.shared.ui.viewmodels.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.dsqrwym.shared.data.local.SharedUserPreferences

internal object SharedNavigationPersistence {
    private const val CURRENT_VERSION = 1
    private val routeSerializer = PolymorphicSerializer(NavKey::class)
    private val routeListSerializer = ListSerializer(routeSerializer)

    fun save(
        key: String,
        state: SharedNavigationState,
        json: Json,
    ) {
        runCatching {
            val persisted = state.snapshot().toPersisted(json)
            json.encodeToString(PersistedNavigationState.serializer(), persisted)
        }.onSuccess { encoded ->
            SharedUserPreferences.saveNavigationStack(key, encoded)
        }.onFailure {
            SharedUserPreferences.clearNavigationStack(key)
        }
    }

    fun restore(
        key: String,
        state: SharedNavigationState,
        json: Json,
    ): Boolean {
        val encoded = SharedUserPreferences.getNavigationStack(key) ?: return false
        return runCatching {
            val persisted = json.decodeFromString(PersistedNavigationState.serializer(), encoded)
            if (persisted.version != CURRENT_VERSION) return@runCatching false
            state.restore(persisted.toSnapshot(json))
        }.getOrElse {
            SharedUserPreferences.clearNavigationStack(key)
            false
        }
    }

    private fun SharedNavigationStateSnapshot.toPersisted(json: Json): PersistedNavigationState {
        return PersistedNavigationState(
            version = CURRENT_VERSION,
            selectedTopLevelRoute = selectedTopLevelRoute?.let { json.encodeRoute(it) },
            singleBackStack = singleBackStack?.let { json.encodeRoutes(it) },
            backStacks = backStacks.map { (rootRoute, routes) ->
                PersistedNavigationBackStack(
                    rootRoute = json.encodeRoute(rootRoute),
                    routes = json.encodeRoutes(routes),
                )
            },
        )
    }

    private fun PersistedNavigationState.toSnapshot(json: Json): SharedNavigationStateSnapshot {
        return SharedNavigationStateSnapshot(
            selectedTopLevelRoute = selectedTopLevelRoute?.let { json.decodeRoute(it) },
            singleBackStack = singleBackStack?.let { json.decodeRoutes(it) },
            backStacks = backStacks.associate { stack ->
                json.decodeRoute(stack.rootRoute) to json.decodeRoutes(stack.routes)
            },
        )
    }

    private fun Json.encodeRoute(route: NavKey): String {
        return encodeToString(routeSerializer, route)
    }

    private fun Json.decodeRoute(encoded: String): NavKey {
        return decodeFromString(routeSerializer, encoded)
    }

    private fun Json.encodeRoutes(routes: List<NavKey>): String {
        return encodeToString(routeListSerializer, routes)
    }

    private fun Json.decodeRoutes(encoded: String): List<NavKey> {
        return decodeFromString(routeListSerializer, encoded)
    }
}

@Serializable
private data class PersistedNavigationState(
    val version: Int = 1,
    val selectedTopLevelRoute: String? = null,
    val singleBackStack: String? = null,
    val backStacks: List<PersistedNavigationBackStack> = emptyList(),
)

@Serializable
private data class PersistedNavigationBackStack(
    val rootRoute: String,
    val routes: String,
)
