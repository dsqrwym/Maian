package org.dsqrwym.shared.navigation.menu.layouts

import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import org.dsqrwym.shared.drawable.SharedIcons
import org.dsqrwym.shared.navigation.menu.SharedMenuConfiguration
import org.dsqrwym.shared.theme.MyHazeStyles
import org.dsqrwym.shared.ui.components.containers.MyBadgedBox
import org.dsqrwym.shared.ui.components.graphics.AnimatedImgVector
import org.dsqrwym.shared.ui.components.menu.SharedMenuIcon
import org.dsqrwym.shared.ui.components.menu.SharedMenuTooltipBox
import org.dsqrwym.shared.util.formatter.asString
import org.dsqrwym.shared.util.navigation.isSameRoute

/**
 * Rail + Top Bar 布局 (用于桌面、大屏平板 >= 840dp)
 *
 * 符合 Material Design 3 XR 规范:
 * - 左侧 Navigation Rail 显示所有菜单项(主要+次要)
 * - 顶部悬浮标签栏显示主要菜单项
 * - 适合大屏幕的双重导航
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedRailWithTopBarLayout(
    menuConfig: SharedMenuConfiguration,
    currentRoute: Any,
    onNavigate: (Any) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val primaryItems = menuConfig.getPrimaryItems()
    val secondaryItems = menuConfig.getSecondaryItems()
    val topNavigationHaze = rememberHazeState()
    val topNavigationHazeStyle = MyHazeStyles.glass()

    Row(modifier = modifier.fillMaxSize()) {
        // 左侧 Navigation Rail
        Box {
            NavigationRail(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 88.dp)
                    .shadow(elevation = 12.dp, shape = RectangleShape),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Spacer(Modifier.height(12.dp))
                    AnimatedImgVector(
                        durationMillis = 800,
                        imageVector = SharedIcons.MaianLogo, modifier = Modifier.size(58.dp)
                    )
                    Spacer(Modifier.height(12.dp))

                    HorizontalDivider()

                    Spacer(Modifier.height(12.dp))
                    // 次要菜单项
                    SharedNavigationRailItem(
                        secondaryItems,
                        currentRoute,
                        onNavigate
                    )

                    if (secondaryItems.isNotEmpty()) {
                        Spacer(Modifier.weight(1f))
                    }
                    menuConfig.topBarActions?.forEach { action ->
                        NavigationRailItem(
                            selected = false,
                            icon = {
                                action.content(TooltipAnchorPosition.Right)
                            },
                            onClick = { /* no op */ }
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(start = 80.dp)
                    .width(16.dp)
                    .align(Alignment.CenterEnd)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.background,
                                Color.Transparent
                            )
                        )
                    )
            )
        }


        // 右侧内容区域 + 顶部标签栏
        Box(modifier = Modifier.fillMaxSize()) {
            // 内容区域
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(topNavigationHaze)
            ) {
                content()
            }


            // 顶部悬浮标签栏 (只显示主要菜单)
            OutlinedCard(
                modifier = Modifier.padding(top = 16.dp)
                    .fillMaxWidth(0.86f)
                    .align(Alignment.TopCenter),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.Transparent
                ),
                shape = CircleShape,
                // elevation = CardDefaults.outlinedCardElevation(6.dp),
            ) {
                val selectedTabIndex by derivedStateOf {
                    primaryItems.indexOfFirst {
                        isSameRoute(currentRoute, it.item.route)
                    }.coerceAtLeast(-1)
                }
                PrimaryTabRow(
                    modifier = Modifier.hazeEffect(topNavigationHaze, topNavigationHazeStyle) {
                        alpha = 0.76f
                        progressive = HazeProgressive.verticalGradient(
                            startIntensity = 0.9f,
                            endIntensity = 0.18f
                        )
                    },
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.Transparent,
                    indicator = {
                        if (selectedTabIndex < 0) return@PrimaryTabRow
                        TabRowDefaults.PrimaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(selectedTabIndex, matchContentSize = true),
                            width = Dp.Unspecified,
                            height = 3.8.dp
                        )
                    },
                ) {
                    primaryItems.forEach { state ->
                        SharedMenuTooltipBox(
                            state,
                            TooltipAnchorPosition.Below
                        ) {
                            LeadingIconTab(
                                selected = isSameRoute(currentRoute, state.item.route),
                                onClick = { onNavigate(state.item.route) },
                                text = {
                                    Text(
                                        modifier = Modifier
                                            .focusable()
                                            .basicMarquee(
                                                Int.MAX_VALUE,
                                                MarqueeAnimationMode.Immediately
                                            ),
                                        text = state.item.label.asString() ?: "tab item label",
                                        style = MaterialTheme.typography.labelLarge,
                                        maxLines = 1
                                    )
                                },
                                icon = {
                                    MyBadgedBox(state.showBadge, state.badgeCount) {
                                        SharedMenuIcon(
                                            imageVector = state.item.icon ?: Icons.Outlined.Apps,
                                            contentDescription = state.item.iconContentDescription.asString() ?: "tab item icon"
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}