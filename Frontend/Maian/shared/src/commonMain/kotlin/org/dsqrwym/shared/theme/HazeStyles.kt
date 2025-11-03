package org.dsqrwym.shared.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint

/**
 * Haze 模糊样式
 * 为亮色和暗色主题提供明显的模糊效果
 */
object MyHazeStyles {

    /**
     * 标准模糊样式 - 适用于大多数场景
     * 在亮色和暗色模式下都有明显的半透明模糊效果
     */
    @Composable
    fun standard(): HazeStyle {
        val isLight = MaterialTheme.colorScheme.surface == SurfaceLight

        return HazeStyle(
            // 模糊半径 - 较大的值提供更明显的模糊
            blurRadius = 28.dp,

            // 色调 - 使用主题的 Surface 颜色，增加不透明度以确保可见性
            tints = listOf(
                HazeTint(
                    color = if (isLight) {
                        // 亮色模式：使用 Surface 颜色 + 85% 不透明度
                        SurfaceLight.copy(alpha = 0.85f)
                    } else {
                        // 暗色模式：使用 Surface 颜色 + 90% 不透明度
                        SurfaceDark.copy(alpha = 0.90f)
                    }
                )
            ),

            // 噪点因子 - 添加质感，使模糊更自然
            noiseFactor = 0.18f
        )
    }

    /**
     * 强模糊样式 - 用于需要更强视觉分离的场景
     * 提供更厚重的模糊效果
     */
    @Composable
    fun thick(): HazeStyle {
        val isLight = MaterialTheme.colorScheme.surface == SurfaceLight

        return HazeStyle(
            blurRadius = 35.dp,
            tints = listOf(
                HazeTint(
                    color = if (isLight) {
                        // 亮色模式：使用 SurfaceContainer 增强层次感
                        SurfaceContainerLight.copy(alpha = 0.92f)
                    } else {
                        // 暗色模式：使用 SurfaceContainer
                        SurfaceContainerDark.copy(alpha = 0.95f)
                    }
                )
            ),
            noiseFactor = 0.20f
        )
    }

    /**
     * 轻薄模糊样式 - 用于需要更多透明度的场景
     * 保持内容可见性的同时提供模糊效果
     */
    @Composable
    fun thin(): HazeStyle {
        val isLight = MaterialTheme.colorScheme.surface == SurfaceLight

        return HazeStyle(
            blurRadius = 24.dp,
            tints = listOf(
                HazeTint(
                    color = if (isLight) {
                        SurfaceLight.copy(alpha = 0.70f)
                    } else {
                        SurfaceDark.copy(alpha = 0.70f)
                    }
                )
            ),
            noiseFactor = 0.05f
        )
    }

    /**
     * 玻璃态样式 - 模拟玻璃材质效果
     * 使用 SurfaceVariant 创建独特的玻璃质感
     */
    @Composable
    fun glass(): HazeStyle {
        val isLight = MaterialTheme.colorScheme.surface == SurfaceLight

        return HazeStyle(
            blurRadius = 30.dp,
            tints = listOf(
                HazeTint(
                    color = if (isLight) {
                        // 亮色模式：使用 SurfaceVariant 创建蓝色玻璃感
                        SurfaceVariantLight.copy(alpha = 0.80f)
                    } else {
                        // 暗色模式：使用 SurfaceVariant 创建深蓝玻璃感
                        SurfaceVariantDark.copy(alpha = 0.85f)
                    }
                ),
                // 叠加轻微的 Primary 色调增加品牌感
                HazeTint(
                    color = if (isLight) {
                        PrimaryLight.copy(alpha = 0.05f)
                    } else {
                        PrimaryDark.copy(alpha = 0.08f)
                    }
                )
            ),
            noiseFactor = 0.16f
        )
    }

    /**
     * TopBar 专用样式 - 针对顶部应用栏优化
     * 确保文本可读性的同时提供明显的模糊分层
     */
    @Composable
    fun topBar(): HazeStyle {
        val isLight = MaterialTheme.colorScheme.surface == SurfaceLight

        return HazeStyle(
            blurRadius = 26.dp,
            tints = listOf(
                HazeTint(
                    color = if (isLight) {
                        // 亮色模式：使用 SurfaceContainerLow 保持清爽
                        SurfaceContainerLowLight.copy(alpha = 0.88f)
                    } else {
                        // 暗色模式：使用 SurfaceContainerHigh 提供深度
                        SurfaceContainerHighDark.copy(alpha = 0.92f)
                    }
                ),
                // 添加轻微的表面色调强化分层
                HazeTint(
                    color = if (isLight) {
                        SurfaceLight.copy(alpha = 0.15f)
                    } else {
                        SurfaceDark.copy(alpha = 0.20f)
                    }
                )
            ),
            noiseFactor = 0.17f
        )
    }

    /**
     * 高对比度样式 - 用于需要最大可见性的场景
     * 提供最明显的模糊效果，几乎不透明
     */
    @Composable
    fun highContrast(): HazeStyle {
        val isLight = MaterialTheme.colorScheme.surface == SurfaceLight

        return HazeStyle(
            blurRadius = 32.dp,
            tints = listOf(
                HazeTint(
                    color = if (isLight) {
                        SurfaceContainerHighestLight.copy(alpha = 0.95f)
                    } else {
                        SurfaceContainerHighestDark.copy(alpha = 0.97f)
                    }
                )
            ),
            noiseFactor = 0.22f
        )
    }
}