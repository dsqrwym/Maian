package org.dsqrwym.shared.navigation.menu

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.dsqrwym.shared.navigation.menu.layouts.SharedBottomNavigationLayout
import org.dsqrwym.shared.navigation.menu.layouts.SharedNavigationRailLayout
import org.dsqrwym.shared.navigation.menu.layouts.SharedRailWithTopBarLayout
import org.dsqrwym.shared.util.navigation.WindowWidthSizeClass
import org.dsqrwym.shared.util.navigation.calculateWindowSizeClass

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
 * @param topBarTitle 顶部栏标题(仅 Compact 模式使用)
 * @param drawerTitle 抽屉标题(仅 Expanded 模式使用)
 * @param content 页面内容
 */
@Composable
fun SharedAdaptiveNavigation(
    menuConfig: SharedMenuConfiguration,
    currentRoute: Any,
    onNavigate: (Any) -> Unit,
    modifier: Modifier = Modifier,
    drawerTitle: String = "导航菜单",
    content: @Composable () -> Unit
) {
    val windowSizeClass = calculateWindowSizeClass()

    when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> {
            // < 600dp: 手机竖屏
            SharedBottomNavigationLayout(
                menuConfig = menuConfig,
                currentRoute = currentRoute,
                onNavigate = onNavigate,
                modifier = modifier,
                content = content
            )
        }
        WindowWidthSizeClass.Medium -> {
            // 600-840dp: 平板、手机横屏
            SharedNavigationRailLayout(
                menuConfig = menuConfig,
                currentRoute = currentRoute,
                onNavigate = onNavigate,
                content = content
            )
        }
        WindowWidthSizeClass.Expanded -> {
            // >= 840dp: 桌面、大屏平板
            SharedRailWithTopBarLayout(
                menuConfig = menuConfig,
                currentRoute = currentRoute,
                onNavigate = onNavigate,
                modifier = modifier,
               // appTitle = drawerTitle,
                content = content
            )
        }
    }
}