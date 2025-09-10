package org.dsqrwym.shared.util.navigation

import androidx.compose.ui.focus.FocusManager
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

/**
 * navigateWithKeyboardDismiss
 *
 * EN: Navigate to a route after clearing keyboard focus to avoid unwanted input.
 * ZH: 在导航前清除键盘焦点，防止输入框残留焦点导致的异常输入。
 */
fun <T : Any> NavHostController.navigateWithKeyboardDismiss(
    route: T,
    navOptions: NavOptions? = null,
    navigatorExtras: Navigator.Extras? = null,
    focusManager: FocusManager,
    force: Boolean = false
) {
    focusManager.clearFocus(force)
    navigate(route, navOptions, navigatorExtras)
}

/**
 * popBackStackWithKeyboardDismiss
 *
 * EN: Pop back stack after dismissing the keyboard/focus.
 * ZH: 先收起键盘/清理焦点再返回上一个页面。
 */
fun NavHostController.popBackStackWithKeyboardDismiss(
    focusManager: FocusManager,
    force: Boolean = false
): Boolean {
    focusManager.clearFocus(force)
    return popBackStack()
}

/**
 * onLeaveScreen
 *
 * EN: Observe navigation and invoke a callback when currentRoute is left; optional delay for transitions.
 * ZH: 监听导航变化，在离开指定路由时触发回调；可设置延时以配合转场动画。
 */
@Suppress("unused")
suspend fun NavHostController.onLeaveScreen(
    currentRoute: String?,
    delayMillis: Long = 270,
    onLeave: suspend () -> Unit
) {
    currentBackStackEntryFlow.collectLatest { backStackEntry ->
        if (backStackEntry.destination.route != currentRoute) {
            delay(delayMillis)
            onLeave()
        }
    }
}