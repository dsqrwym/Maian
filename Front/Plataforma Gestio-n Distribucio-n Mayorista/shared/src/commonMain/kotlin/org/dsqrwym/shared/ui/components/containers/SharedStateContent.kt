package org.dsqrwym.shared.ui.components.containers

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.dsqrwym.shared.drawable.SharedIcons
import org.dsqrwym.shared.theme.AppExtraColors
import org.dsqrwym.shared.ui.animations.SharedAuthAnimation
import org.dsqrwym.shared.ui.components.progressindicators.SharedProgressIndicator

enum class SharedUiState {
    Idle, Loading, Success, Error
}

@Composable
fun SharedStateContent(
    state: SharedUiState = SharedUiState.Idle,
    size: Dp = 26.dp,
    progressStrokeWith: Dp = 3.dp,
    successIconTint: Color = AppExtraColors.current.correct,
    errorIconTint: Color = MaterialTheme.colorScheme.error,
    modifier: Modifier = Modifier,
    idleContent: @Composable () -> Unit,
) {
    AnimatedContent(
        targetState = state,
        transitionSpec = {
            SharedAuthAnimation.DefaultEnterTransition togetherWith SharedAuthAnimation.DefaultExitTransition
        },
        label = "State Content Transition",
        modifier = modifier,
    ) { target ->
        when (target) {
            SharedUiState.Idle -> idleContent()
            SharedUiState.Loading -> SharedProgressIndicator(size, progressStrokeWith)
            SharedUiState.Success -> {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = "完成",
                    tint = successIconTint,
                    modifier = Modifier.size(size)
                )
            }

            SharedUiState.Error -> {
                Icon(
                    imageVector = SharedIcons.CircleError,
                    contentDescription = null,
                    tint = errorIconTint,
                    modifier = Modifier.size(size)
                )
            }
        }

    }
}