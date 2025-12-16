package org.dsqrwym.shared.navigation.menu

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import org.dsqrwym.shared.LocalWindowSizeClass
import org.dsqrwym.shared.navigation.menu.layouts.SharedBottomNavigationLayout
import org.dsqrwym.shared.navigation.menu.layouts.SharedNavigationRailLayout
import org.dsqrwym.shared.navigation.menu.layouts.SharedRailWithTopBarLayout
import org.dsqrwym.shared.ui.viewmodels.menu.SharedMenuViewModel
import org.dsqrwym.shared.util.navigation.WindowWidthSizeClass
import org.koin.compose.currentKoinScope

/**
 * 自适应导航组件
 *
 * 根据当前窗口尺寸自动选择合适的导航布局:
 * - Compact (< 600dp): Bottom Navigation + Drawer
 * - Medium (600-840dp): Navigation Rail
 * - Expanded (>= 840dp): Permanent Drawer
 *
 * @param menuConfig 菜单配置
 * @param currentRoute 当前路由
 * @param onNavigate 导航回调
 * @param modifier 修饰符
 * @param content 页面内容
 */
@Composable
fun SharedAdaptiveNavigation(
    menuConfig: SharedMenuConfiguration,
    currentRoute: Any,
    onNavigate: (Any) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val windowSizeClass = LocalWindowSizeClass.current
    val menuViewModel: SharedMenuViewModel = currentKoinScope().get()

    LaunchedEffect(Unit) {
        menuViewModel.initMenu(menuConfig)
    }

    val menuState by menuViewModel.menuStates.collectAsState()
    val newMenuConfig = menuConfig.copy(items = menuState)

    when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> {
            // < 600dp: 手机竖屏
            SharedBottomNavigationLayout(
                menuConfig = newMenuConfig,
                currentRoute = currentRoute,
                onNavigate = onNavigate,
                modifier = modifier,
                content = content
            )
        }

        WindowWidthSizeClass.Medium -> {
            // 600-840dp: 平板、手机横屏
            SharedNavigationRailLayout(
                menuConfig = newMenuConfig,
                currentRoute = currentRoute,
                onNavigate = onNavigate,
                content = content
            )
        }

        WindowWidthSizeClass.Expanded -> {
            // >= 840dp: 桌面、大屏平板
            SharedRailWithTopBarLayout(
                menuConfig = newMenuConfig,
                currentRoute = currentRoute,
                onNavigate = onNavigate,
                modifier = modifier,
                // appTitle = drawerTitle,
                content = content
            )
        }
    }
}