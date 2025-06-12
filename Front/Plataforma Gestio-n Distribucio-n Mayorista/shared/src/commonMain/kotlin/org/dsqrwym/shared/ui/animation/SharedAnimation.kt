package org.dsqrwym.shared.ui.animation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween

object SharedAuthAnimation {
    val DefaultEnterTransition: EnterTransition
        get() = fadeIn(animationSpec = tween(300))

    val DefaultExitTransition: ExitTransition
        get() = fadeOut(animationSpec = tween(300)) + slideOutHorizontally(
            targetOffsetX = { -it },
            animationSpec = tween(300)
        )
}
