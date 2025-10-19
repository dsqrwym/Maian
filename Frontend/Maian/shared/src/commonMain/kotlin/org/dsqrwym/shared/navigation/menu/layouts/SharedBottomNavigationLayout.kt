package org.dsqrwym.shared.navigation.menu.layouts

import androidx.compose.animation.core.LinearEasing
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuOpen
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.launch
import org.dsqrwym.shared.navigation.menu.SharedMenuConfiguration
import org.dsqrwym.shared.navigation.menu.SharedMenuIcon
import org.dsqrwym.shared.theme.MyHazeStyles
import org.dsqrwym.shared.util.navigation.isSameRoute

/**
 * 底部导航布局 (用于手机竖屏 < 600dp)
 *
 * Material Design 3 规范:
 * - 底部导航栏显示3-5个主要目标
 * - 侧边抽屉显示次要功能和额外选项
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedBottomNavigationLayout(
    menuConfig: SharedMenuConfiguration,
    currentRoute: Any,
    onNavigate: (Any) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val currentItem = menuConfig.items.find { isSameRoute(it.route, currentRoute) }

    val topBarHazeState = rememberHazeState()
    val topBarHazeStyle = MyHazeStyles.topBar()

    val drawerHazeState = rememberHazeState()
    val drawerHazeStyle = MyHazeStyles.standard()

    val scope = rememberCoroutineScope()

    // 主要菜单项(底部导航栏,最多5个)
    val primaryItems = menuConfig.getPrimaryItems().take(5)
    // 次要菜单项
    val secondaryItems = menuConfig.getSecondaryItems()
    // 超出5个的主要菜单项也放入抽屉
    val overflowPrimaryItems = menuConfig.getPrimaryItems().drop(5)
    val drawerItems = overflowPrimaryItems + secondaryItems

    ModalNavigationDrawer(
        modifier = modifier,
        drawerState = drawerState,
        drawerContent = {
            if (drawerItems.isNotEmpty()) {
                ModalDrawerSheet(
                    modifier = Modifier.fillMaxWidth(0.58f).hazeEffect(state = drawerHazeState) {
                        // 使用标准样式，无渐进式模糊
                        style = drawerHazeStyle
                        // 抽屉固定高透明度，营造玻璃质感
                        alpha = 0.95f
                        progressive = HazeProgressive.horizontalGradient(
                            easing = LinearEasing,
                            startIntensity = 2f,
                            endIntensity = 0.05f
                        )
                    },
                    drawerContainerColor = Color.Transparent,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(vertical = 16.dp)
                    ) {
                        Text(
                            text = "更多功能",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(horizontal = 28.dp, vertical = 16.dp)
                        )

                        HorizontalDivider()
                        Spacer(Modifier.height(8.dp))

                        drawerItems.forEach { item ->
                            NavigationDrawerItem(
                                icon = {
                                    SharedMenuIcon(
                                        imageVector = item.icon ?: Icons.Outlined.Apps,
                                        contentDescription = item.iconContentDescription
                                    )
                                },
                                label = { Text(item.label) },
                                selected = isSameRoute(currentRoute, item.route),
                                onClick = {
                                    onNavigate(item.route)
                                    scope.launch { drawerState.close() }
                                },
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        },
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(currentItem?.label ?: "") },
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent
                    ),
                    navigationIcon = {
                        if (drawerItems.isNotEmpty()) {
                            IconButton(onClick = {
                                scope.launch { drawerState.open() }
                            }) {
                                SharedMenuIcon(
                                    imageVector = if (drawerState.isOpen) Icons.AutoMirrored.Outlined.MenuOpen else Icons.Outlined.Menu,
                                    contentDescription = if (drawerState.isOpen) "关闭菜单" else "打开菜单"
                                )
                            }
                        }
                    },
                    actions = {
                        menuConfig.topBarActions?.take(3)?.forEach { action ->
                            action.content(TooltipAnchorPosition.Below)
                        }
                    },
                    modifier = Modifier.hazeEffect(state = topBarHazeState) {
                        progressive = HazeProgressive.verticalGradient(
                            startIntensity = 2f,
                            endIntensity = 0.05f,
                            preferPerformance = false // 设为 true 可提升性能但降低质量
                        )
                        style = topBarHazeStyle
                        alpha = when {
                            scrollBehavior.state.collapsedFraction > 0f -> {
                                // 滚动时：根据折叠程度增加到 1.0
                                0.38f + (scrollBehavior.state.collapsedFraction * 0.6f)
                            }

                            else -> {
                                // 顶部时：保持 40% 透明度，让内容若隐若现
                                0.38f
                            }
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar(
                    tonalElevation = 3.dp
                ) {
                    primaryItems.forEach { item ->
                        NavigationBarItem(
                            icon = {
                                SharedMenuIcon(
                                    imageVector = item.icon ?: Icons.Outlined.Apps,
                                    contentDescription = item.iconContentDescription
                                )
                            },
                            label = { Text(item.label) },
                            selected = isSameRoute(currentRoute, item.route),
                            onClick = { onNavigate(item.route) }
                        )
                    }
                }
            },
            containerColor = Color.Transparent,
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
                .hazeSource(state = drawerHazeState)
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .hazeSource(state = topBarHazeState)
            ) {
                content()
            }
        }
    }
}