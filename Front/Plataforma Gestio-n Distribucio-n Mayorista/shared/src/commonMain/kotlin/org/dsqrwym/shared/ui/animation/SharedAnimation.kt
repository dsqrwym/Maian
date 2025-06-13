package org.dsqrwym.shared.ui.animation

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.IntOffset

object SharedAuthAnimation {
    val DefaultEnterTransition: EnterTransition
        get() = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessVeryLow))

    val DefaultExitTransition: ExitTransition
        get() = slideOutHorizontally(
            targetOffsetX = { -it },
            animationSpec = tween()
        ) + fadeOut(animationSpec = tween())

    val WebEnterTransition: EnterTransition
        get() = slideIn(initialOffset = {
            IntOffset(it.width / 2, it.height / 2)
        })

    val WebExitTransition: ExitTransition
        get() = slideOut(targetOffset = {
            IntOffset(it.width, it.height)
        })


}
