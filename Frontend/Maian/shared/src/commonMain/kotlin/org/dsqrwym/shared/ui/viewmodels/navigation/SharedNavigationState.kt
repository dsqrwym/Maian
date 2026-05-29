package org.dsqrwym.shared.ui.viewmodels.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import org.dsqrwym.shared.navigation.SharedNavSerializersModule

@Stable
class SharedNavigationState internal constructor(
    private val startRoute: NavKey,
    private val activeRootRoute: () -> NavKey,
    private val currentTopLevelRouteValue: () -> NavKey,
    private val topLevelRoutes: Set<NavKey>,
    private val onSwitchTopLevelRoute: ((NavKey) -> Unit)?,
    private val backStacks: Map<NavKey, NavBackStack<NavKey>>,
    private val singleBackStack: NavBackStack<NavKey>?,
    private val onNavigationChanged: ((SharedNavigationState) -> Unit)? = null,
) {
    val backStack: NavBackStack<NavKey>
        get() = singleBackStack ?: backStacks.getValue(currentTopLevelRoute)

    val currentRoute: NavKey
        get() = backStack.last()

    val currentTopLevelRoute: NavKey
        get() = currentTopLevelRouteValue()

    fun current(): NavKey = currentRoute

    fun navigate(route: NavKey) {
        if (isTopLevelRoute(route)) {
            navigateToTopLevel(route)
            return
        }

        val stack = backStack
        if (stack.lastOrNull() == route) return
        stack.add(route)
        notifyNavigationChanged()
    }

    fun navigateToTopLevel(route: NavKey) {
        if (!isTopLevelRoute(route)) {
            navigate(route)
            return
        }

        if (singleBackStack != null) {
            val stack = backStack
            if (stack.firstOrNull() == route) {
                if (trimToRoot(stack)) {
                    notifyNavigationChanged()
                }
                return
            }
            stack.clear()
            stack.add(route)
            notifyNavigationChanged()
            return
        }

        if (currentTopLevelRoute == route) {
            if (trimToRoot(backStack)) {
                notifyNavigationChanged()
            }
            return
        }
        onSwitchTopLevelRoute?.invoke(route)
        notifyNavigationChanged()
    }

    fun pop(): Boolean {
        val stack = backStack
        if (stack.size > 1) {
            stack.removeAt(stack.lastIndex)
            notifyNavigationChanged()
            return true
        }

        if (singleBackStack == null && currentTopLevelRoute != startRoute) {
            onSwitchTopLevelRoute?.invoke(startRoute)
            notifyNavigationChanged()
            return true
        }

        return false
    }

    fun popTo(route: NavKey, inclusive: Boolean = false): Boolean {
        val stack = backStack
        val routeIndex = stack.indexOfLast { it == route }
        if (routeIndex == -1) return false

        val targetSize = if (inclusive) routeIndex else routeIndex + 1
        if (targetSize <= 0) return false

        val originalSize = stack.size
        while (stack.size > targetSize) {
            stack.removeAt(stack.lastIndex)
        }
        if (stack.size != originalSize) {
            notifyNavigationChanged()
        }

        return true
    }

    fun replace(route: NavKey) {
        if (isTopLevelRoute(route)) {
            resetTopLevelStack(route)
            return
        }

        val stack = backStack
        if (stack.isEmpty()) {
            stack.add(route)
            notifyNavigationChanged()
            return
        }

        if (stack.last() == route) return

        stack.removeAt(stack.lastIndex)
        stack.add(route)
        notifyNavigationChanged()
    }

    fun clearAndNavigate(route: NavKey) {
        if (isTopLevelRoute(route)) {
            resetTopLevelStack(route)
            return
        }

        val stack = backStack
        val rootRoute = activeRootRoute()
        val newStack = buildList {
            add(rootRoute)
            if (rootRoute != route) {
                add(route)
            }
        }
        if (stack.toList() == newStack) return

        stack.clear()
        stack.addAll(newStack)
        notifyNavigationChanged()
    }

    internal fun snapshot(): SharedNavigationStateSnapshot {
        return SharedNavigationStateSnapshot(
            selectedTopLevelRoute = if (singleBackStack == null) currentTopLevelRoute else null,
            singleBackStack = singleBackStack?.toList(),
            backStacks = backStacks.mapValues { (_, stack) -> stack.toList() },
        )
    }

    internal fun restore(snapshot: SharedNavigationStateSnapshot): Boolean {
        if (singleBackStack != null) {
            val routes = snapshot.singleBackStack ?: return false
            replaceStack(singleBackStack, normalizeRestoredStack(startRoute, routes))
            return true
        }

        var restored = false
        snapshot.backStacks.forEach { (rootRoute, routes) ->
            val stack = backStacks[rootRoute] ?: return@forEach
            replaceStack(stack, normalizeRestoredStack(rootRoute, routes))
            restored = true
        }

        val selectedRoute = snapshot.selectedTopLevelRoute
        if (selectedRoute != null && isTopLevelRoute(selectedRoute)) {
            onSwitchTopLevelRoute?.invoke(selectedRoute)
            restored = true
        }

        return restored
    }

    private fun isTopLevelRoute(route: NavKey): Boolean = topLevelRoutes.contains(route)

    private fun resetTopLevelStack(route: NavKey) {
        if (!isTopLevelRoute(route)) return

        val previousTopLevelRoute = currentTopLevelRoute
        val targetStack = singleBackStack ?: backStacks.getValue(route)
        val stackChanged = targetStack.size != 1 || targetStack.firstOrNull() != route
        if (stackChanged) {
            targetStack.clear()
            targetStack.add(route)
        }

        val routeChanged = previousTopLevelRoute != route
        if (routeChanged) {
            onSwitchTopLevelRoute?.invoke(route)
        }

        if (stackChanged || routeChanged) {
            notifyNavigationChanged()
        }
    }

    private fun trimToRoot(stack: NavBackStack<NavKey>): Boolean {
        val originalSize = stack.size
        while (stack.size > 1) {
            stack.removeAt(stack.lastIndex)
        }
        return stack.size != originalSize
    }

    private fun normalizeRestoredStack(rootRoute: NavKey, routes: List<NavKey>): List<NavKey> {
        return if (routes.firstOrNull() == rootRoute) {
            routes.ifEmpty { listOf(rootRoute) }
        } else {
            listOf(rootRoute) + routes
        }
    }

    private fun replaceStack(stack: NavBackStack<NavKey>, routes: List<NavKey>) {
        if (stack.toList() == routes) return
        stack.clear()
        stack.addAll(routes)
    }

    private fun notifyNavigationChanged() {
        onNavigationChanged?.invoke(this)
    }
}

internal data class SharedNavigationStateSnapshot(
    val selectedTopLevelRoute: NavKey?,
    val singleBackStack: List<NavKey>?,
    val backStacks: Map<NavKey, List<NavKey>>,
)

@Composable
fun rememberSharedNavigationState(
    initRoute: NavKey,
    extraSerializersModule: SerializersModule? = null,
    persistenceKey: String? = null,
): SharedNavigationState {
    val serializersModule = rememberNavigationSerializersModule(extraSerializersModule)
    val savedStateConfiguration = rememberNavigationSavedStateConfiguration(serializersModule)
    val persistenceJson = rememberNavigationPersistenceJson(serializersModule)
    val onNavigationChanged = remember(persistenceKey, persistenceJson) {
        persistenceKey?.let { key ->
            { state: SharedNavigationState ->
                SharedNavigationPersistence.save(key, state, persistenceJson)
            }
        }
    }
    val backStack = rememberNavBackStack(savedStateConfiguration, initRoute)

    val navigationState = remember(initRoute, backStack, onNavigationChanged) {
        SharedNavigationState(
            startRoute = initRoute,
            activeRootRoute = { backStack.first() },
            currentTopLevelRouteValue = { backStack.first() },
            topLevelRoutes = setOf(initRoute),
            onSwitchTopLevelRoute = null,
            backStacks = emptyMap(),
            singleBackStack = backStack,
            onNavigationChanged = onNavigationChanged,
        )
    }

    LaunchedEffect(persistenceKey, navigationState, persistenceJson) {
        persistenceKey?.let { key ->
            SharedNavigationPersistence.restore(key, navigationState, persistenceJson)
        }
    }

    return navigationState
}

@Composable
fun rememberSharedNavigationState(
    startRoute: NavKey,
    topLevelRoutes: List<NavKey>,
    extraSerializersModule: SerializersModule? = null,
    persistenceKey: String? = null,
): SharedNavigationState {
    if (topLevelRoutes.isEmpty()) {
        return rememberSharedNavigationState(
            initRoute = startRoute,
            extraSerializersModule = extraSerializersModule,
            persistenceKey = persistenceKey,
        )
    }

    val orderedTopLevelRoutes = remember(startRoute, topLevelRoutes) {
        buildList {
            add(startRoute)
            topLevelRoutes
                .filterNot { it == startRoute }
                .forEach(::add)
        }
    }
    val topLevelRouteSet = remember(orderedTopLevelRoutes) {
        orderedTopLevelRoutes.toSet()
    }
    val serializersModule = rememberNavigationSerializersModule(extraSerializersModule)
    val savedStateConfiguration = rememberNavigationSavedStateConfiguration(serializersModule)
    val persistenceJson = rememberNavigationPersistenceJson(serializersModule)
    val onNavigationChanged = remember(persistenceKey, persistenceJson) {
        persistenceKey?.let { key ->
            { state: SharedNavigationState ->
                SharedNavigationPersistence.save(key, state, persistenceJson)
            }
        }
    }
    val selectedTopLevelRouteStack = rememberNavBackStack(savedStateConfiguration, startRoute)
    val backStacks = orderedTopLevelRoutes.associateWith { route ->
        rememberNavBackStack(savedStateConfiguration, route)
    }

    val navigationState = remember(
        startRoute,
        topLevelRouteSet,
        selectedTopLevelRouteStack,
        backStacks,
        onNavigationChanged,
    ) {
        SharedNavigationState(
            startRoute = startRoute,
            activeRootRoute = { backStacks.getValue(selectedTopLevelRouteStack.last()).first() },
            currentTopLevelRouteValue = { selectedTopLevelRouteStack.last() },
            topLevelRoutes = topLevelRouteSet,
            onSwitchTopLevelRoute = { route ->
                selectedTopLevelRouteStack.clear()
                selectedTopLevelRouteStack.add(route)
            },
            backStacks = backStacks,
            singleBackStack = null,
            onNavigationChanged = onNavigationChanged,
        )
    }

    LaunchedEffect(persistenceKey, navigationState, persistenceJson) {
        persistenceKey?.let { key ->
            SharedNavigationPersistence.restore(key, navigationState, persistenceJson)
        }
    }

    return navigationState
}

@Composable
private fun rememberNavigationSerializersModule(
    extraSerializersModule: SerializersModule?,
): SerializersModule {
    return remember(extraSerializersModule) {
        SerializersModule {
            include(SharedNavSerializersModule)
            extraSerializersModule?.let { include(it) }
        }
    }
}

@Composable
private fun rememberNavigationSavedStateConfiguration(
    serializersModule: SerializersModule,
): SavedStateConfiguration {
    return remember(serializersModule) {
        SavedStateConfiguration {
            this.serializersModule = serializersModule
        }
    }
}

@Composable
private fun rememberNavigationPersistenceJson(
    serializersModule: SerializersModule,
): Json {
    return remember(serializersModule) {
        Json {
            this.serializersModule = serializersModule
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
    }
}
