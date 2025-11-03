package org.dsqrwym.shared.ui.components.cards

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import org.dsqrwym.shared.ui.components.containers.UiState

/**
 * FormCard
 *
 * EN: Card component for general form operations (create/edit).
 * Shows animated status icons (Error/Loading/Success) in the header.
 * Optionally displays step indicator when multiple steps are present.
 * Automatically disables interaction when not in active state.
 *
 * ZH: 用于通用表单操作（创建/编辑）的卡片组件。
 * 在标题栏显示动画状态图标（错误/加载中/成功）。
 * 当有多个步骤时可选显示步骤指示器。
 * 非活动状态时自动禁用交互。
 *
 * @param title 卡片标题 / Card title
 * @param subtitle 卡片副标题（可选）/ Card subtitle (optional)
 * @param step 当前步骤编号（仅多步骤时使用）/ Current step number (for multi-step only)
 * @param totalSteps 总步骤数，默认1表示单步骤 / Total steps, default 1 for single step
 * @param uiState 表单状态 / Form state
 * @param enabled 是否启用交互 / Whether interaction is enabled
 * @param showStepIndicator 是否强制显示步骤指示器 / Whether to force show step indicator
 * @param content 表单内容 / Form content
 */
@Composable
fun FormCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
    step: Int = 1,
    totalSteps: Int = 1,
    uiState: UiState = UiState.Idle,
    enabled: Boolean = true,
    showStepIndicator: Boolean = false,
    content: @Composable ColumnScope.(enabled: Boolean) -> Unit
) {
    val elevation by animateDpAsState(
        targetValue = if (enabled && uiState != UiState.Success) 4.dp else 0.dp
    )

    val shouldShowStep = totalSteps > 1 || showStepIndicator

    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.outlinedCardElevation(elevation),
        border = CardDefaults.outlinedCardBorder(enabled),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (enabled) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with title and status icon
            if (title != null || subtitle != null || uiState != UiState.Idle || shouldShowStep) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Title section
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        if (title != null) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (enabled) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                }
                            )
                        }
                        if (subtitle != null) {
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    // Status and step indicator
                    Row(
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 16.dp)
                    ) {
                        // Step indicator (only show when multiple steps)
                        if (shouldShowStep) {
                            Text(
                                text = "$step/$totalSteps",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }

                        // Status icon
                        StatusIcon(uiState = uiState)
                    }
                }

                // Divider after header
                if (uiState != UiState.Idle || title != null || subtitle != null) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }

            // Form content
            Box {
                Column(modifier = Modifier.fillMaxWidth()) {
                    content(enabled)
                }

                // Overlay to disable interaction when not enabled
                if (!enabled) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .pointerInput(Unit) {
                                awaitPointerEventScope {
                                    while (true) {
                                        awaitPointerEvent()
                                    }
                                }
                            }
                    )
                }
            }
        }
    }
}