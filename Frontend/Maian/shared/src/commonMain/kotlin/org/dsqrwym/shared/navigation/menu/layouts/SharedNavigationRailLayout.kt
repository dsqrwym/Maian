package org.dsqrwym.shared.navigation.menu.layouts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuOpen
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.*
import androidx.compose.material3.TooltipDefaults.rememberTooltipPositionProvider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import org.dsqrwym.shared.navigation.menu.SharedMenuConfiguration
import org.dsqrwym.shared.navigation.menu.SharedMenuIcon
import org.dsqrwym.shared.navigation.menu.SharedMenuItem
import org.dsqrwym.shared.theme.MyHazeStyles
import org.dsqrwym.shared.util.navigation.isSameRoute

/**
 * Navigation Rail 布局 (用于平板、手机横屏 600-840dp)
 *
 * 特点:
 * - 左侧垂直导航栏
 * - 显示所有可见菜单项
 * - 节省水平空间
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedNavigationRailLayout(
    menuConfig: SharedMenuConfiguration,
    currentRoute: Any,
    onNavigate: (Any) -> Unit,
    content: @Composable () -> Unit
) {
    var isRailExpanded by remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val currentItem = menuConfig.items.find { isSameRoute(it.route, currentRoute) }

    val topBarHazeState = rememberHazeState()
    val topBarHazeStyle = MyHazeStyles.topBar()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                ),
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(onClick = { isRailExpanded = !isRailExpanded }) {
                        Icon(
                            imageVector = if (isRailExpanded) Icons.AutoMirrored.Outlined.MenuOpen else Icons.Outlined.Menu,
                            contentDescription = "Menu"
                        )
                    }
                },
                title = {
                    Text(currentItem?.label ?: "")
                },
                actions = {
                    menuConfig.topBarActions?.forEach {
                        it.content(TooltipAnchorPosition.Below)
                    }
                },
                modifier = Modifier.fillMaxWidth().hazeEffect(state = topBarHazeState) {
                    progressive = HazeProgressive.verticalGradient(
                        startIntensity = 10f,
                        endIntensity = 0f,
                        preferPerformance = false // 设为 true 可提升性能但降低质量
                    )
                    style = topBarHazeStyle
                    alpha = 0.95f
                }
            )
        },
        containerColor = Color.Transparent,
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
    ) { paddingValues ->
        Row(modifier = Modifier.fillMaxSize()) {
            AnimatedVisibility(
                modifier = Modifier.fillMaxHeight().widthIn(max = 88.dp),
                visible = isRailExpanded,
                enter = androidx.compose.animation.expandHorizontally(),
                exit = androidx.compose.animation.shrinkHorizontally()
            ) {
                NavigationRail {
                    Spacer(Modifier.height(paddingValues.calculateTopPadding()))

                    SharedNavigationRailItem(
                        items = menuConfig.getPrimaryItems(),
                        currentRoute = currentRoute,
                        onNavigate = {
                            onNavigate(it)
                            isRailExpanded = false
                        }
                    )

                    val secondary = menuConfig.getSecondaryItems()
                    if (secondary.isNotEmpty()) {
                        HorizontalDivider()
                        SharedNavigationRailItem(
                            items = secondary,
                            currentRoute = currentRoute,
                            onNavigate = {
                                onNavigate(it)
                                isRailExpanded = false
                            }
                        )
                    }
                }
            }

            // 主内容区
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .hazeSource(state = topBarHazeState),
                contentAlignment = Alignment.Center
            ) {
                content()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedNavigationRailItem(
    items: List<SharedMenuItem<out Any>>,
    currentRoute: Any,
    onNavigate: (Any) -> Unit,
) {
    items.forEach { item ->
        NavigationRailItem(
            icon = {
                TooltipBox(
                    positionProvider = rememberTooltipPositionProvider(TooltipAnchorPosition.Right),
                    tooltip = {
                        PlainTooltip {
                            Text(item.label)
                        }
                    },
                    state = TooltipState()
                ) {
                    SharedMenuIcon(
                        imageVector = item.icon ?: Icons.Outlined.Apps,
                        contentDescription = item.iconContentDescription
                    )
                }
            },
            label = { Text(item.label) },
            selected = isSameRoute(currentRoute, item.route),
            onClick = { onNavigate(item.route) }
        )
    }
}