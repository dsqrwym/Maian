package org.dsqrwym.shared.ui.viewmodels.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.modules.SerializersModule
import org.dsqrwym.shared.navigation.SharedNavSerializersModule

/**
 * 共享导航状态 —— 支持单栈、多栈模式，处理顶层路由（如底部导航栏）和普通页面跳转。
 * 使用 @Stable 让 Compose 编译器信任其稳定性，减少重组。
 */
@Stable
class SharedNavigationState internal constructor(
    private val startRoute: NavKey, // 起始路由
    private val activeRootRoute: () -> NavKey, // 当前活动根路由
    private val currentTopLevelRouteValue: () -> NavKey,  // 当前选中的顶层路由
    private val topLevelRoutes: Set<NavKey>, // 所有顶层路由
    private val onSwitchTopLevelRoute: ((NavKey) -> Unit)?,  // 切换顶层路由时的回调
    private val backStacks: Map<NavKey, NavBackStack<NavKey>>,  // 每个顶层路由对应的独立后退栈
    private val singleBackStack: NavBackStack<NavKey>?, //  单栈模式使用的唯一后退栈
) {
    val backStack: NavBackStack<NavKey>
        get() = singleBackStack ?: backStacks.getValue(currentTopLevelRoute)

    val currentRoute: NavKey
        get() = backStack.last()

    val currentTopLevelRoute: NavKey
        get() = currentTopLevelRouteValue()

    fun current(): NavKey = currentRoute

    /** 导航到指定路由：如果是顶层路由走 navigateToTopLevel 逻辑，否则压入当前栈 */
    fun navigate(route: NavKey) {
        if (isTopLevelRoute(route)) {
            navigateToTopLevel(route)
            return
        }

        val stack = backStack
        if (stack.lastOrNull() == route) return // 已在栈顶则忽略
        stack.add(route)
    }

    /** 处理导航到顶层路由（切换标签页或重置标签页状态） */
    fun navigateToTopLevel(route: NavKey) {
        if (!isTopLevelRoute(route)) {
            navigate(route)
            return
        }
        // ----- 单栈模式 -----
        if (singleBackStack != null) {
            val stack = backStack
            // 若栈底已经是目标路由，只需清空到根（回到该标签页首页）
            if (stack.firstOrNull() == route) {
                trimToRoot(stack)
                return
            }
            // 否则清空整个栈，将目标路由设为新的根
            stack.clear()
            stack.add(route)
            return
        }
        // ----- 多栈模式 -----
        // 如果已经在目标顶层路由上，只清空其内部栈到根
        if (currentTopLevelRoute == route) {
            trimToRoot(backStack)
            return
        }
        // 否则通知外部切换顶层路由（外部应当更新 selectedTopLevelRouteStack）
        onSwitchTopLevelRoute?.invoke(route)
    }

    fun pop(): Boolean {
        val stack = backStack
        if (stack.size > 1) {
            stack.removeAt(stack.lastIndex)
            return true
        }
        // 栈只剩一页，且是多栈模式、当前顶层不是起始路由 → 切换到起始路由（类似回到首页）
        if (singleBackStack == null && currentTopLevelRoute != startRoute) {
            onSwitchTopLevelRoute?.invoke(startRoute)
            return true
        }
        // 无法再返回
        return false
    }

    /** 回退到指定路由（可选是否包含该路由自身） */
    fun popTo(route: NavKey, inclusive: Boolean = false): Boolean {
        val stack = backStack
        // 从后往前找最后一个匹配的位置
        val routeIndex = stack.indexOfLast { it == route }
        if (routeIndex == -1) return false
        // 计算要保留的栈大小：inclusive=false 保留到 route 下一层，inclusive=true 保留到 route 之前
        val targetSize = if (inclusive) routeIndex else routeIndex + 1
        if (targetSize <= 0) return false
        // 弹出栈顶直到达到目标大小
        while (stack.size > targetSize) {
            stack.removeAt(stack.lastIndex)
        }

        return true
    }

    /** 替换当前栈顶路由（不改变栈深度）用于登录失效、权限不够 */
    fun replace(route: NavKey) {
        // 替换目标是顶层路由 → 重置整个顶层栈
        if (isTopLevelRoute(route)) {
            resetTopLevelStack(route)
            return
        }

        val stack = backStack
        if (stack.isEmpty()) {
            stack.add(route)
            return
        }

        if (stack.last() == route) return

        stack.removeAt(stack.lastIndex)
        stack.add(route)
    }

    /** 清空当前后退栈，然后加入活动根路由，最后导航到目标路由（若目标不是根） */
    fun clearAndNavigate(route: NavKey) {
        if (isTopLevelRoute(route)) {
            resetTopLevelStack(route)
            return
        }

        val stack = backStack
        stack.clear()
        stack.add(activeRootRoute())
        if (stack.last() != route) {
            stack.add(route)
        }
    }

    private fun isTopLevelRoute(route: NavKey): Boolean = topLevelRoutes.contains(route)

    private fun resetTopLevelStack(route: NavKey) {
        if (!isTopLevelRoute(route)) return

        val targetStack = singleBackStack ?: backStacks.getValue(route)
        targetStack.clear()
        targetStack.add(route)
        onSwitchTopLevelRoute?.invoke(route)
    }

    /** 将栈修剪到只剩根元素（栈底） */
    private fun trimToRoot(stack: NavBackStack<NavKey>) {
        while (stack.size > 1) {
            stack.removeAt(stack.lastIndex)
        }
    }
}

/**
 * 创建单栈模式的 SharedNavigationState（适用于没有底部导航栏或不需要多标签页保留栈的场景比如Auth）
 * @param initRoute 起始路由
 * @param extraSerializersModule 可选的额外序列化模块，用于保存状态
 */
@Composable
fun rememberSharedNavigationState(
    initRoute: NavKey,
    extraSerializersModule: SerializersModule? = null,
): SharedNavigationState {
    val serializersModule = rememberNavigationSerializersModule(extraSerializersModule)
    val savedStateConfiguration = rememberNavigationSavedStateConfiguration(serializersModule)
    val backStack = rememberNavBackStack(savedStateConfiguration, initRoute)

    return remember(initRoute, backStack) {
        SharedNavigationState(
            startRoute = initRoute,
            activeRootRoute = { backStack.first() },
            currentTopLevelRouteValue = { backStack.first() },
            topLevelRoutes = setOf(initRoute),
            onSwitchTopLevelRoute = null,
            backStacks = emptyMap(),
            singleBackStack = backStack,
        )
    }
}

/**
 * 创建多栈模式的 SharedNavigationState（每个顶层路由拥有独立后退栈，适合底部导航栏）
 * @param startRoute 起始顶层路由（会作为第一个标签页）
 * @param topLevelRoutes 所有顶层路由列表（不包含 startRoute 时会自动补入并去重）
 * @param extraSerializersModule 可选的额外序列化模块
 */
@Composable
fun rememberSharedNavigationState(
    startRoute: NavKey,
    topLevelRoutes: List<NavKey>,
    extraSerializersModule: SerializersModule? = null,
): SharedNavigationState {
    // 如果没有提供任何顶层路由，降级为单栈模式
    if (topLevelRoutes.isEmpty()) {
        return rememberSharedNavigationState(
            initRoute = startRoute,
            extraSerializersModule = extraSerializersModule,
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
    val selectedTopLevelRouteStack = rememberNavBackStack(savedStateConfiguration, startRoute)
    val backStacks = orderedTopLevelRoutes.associateWith { route ->
        rememberNavBackStack(savedStateConfiguration, route)
    }

    return remember(
        startRoute,
        topLevelRouteSet,
        selectedTopLevelRouteStack,
        backStacks,
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
        )
    }
}

/**
 * 组合共享序列化模块与额外模块，并使用 remember 缓存结果
 */
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

/**
 * 根据序列化模块创建 SavedStateConfiguration，并 remember
 */
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
