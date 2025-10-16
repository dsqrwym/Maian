package org.dsqrwym.shared.util.modifier

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput

fun Modifier.disableUserInput(disabled: Boolean): Modifier =
    if (disabled) {
        this.pointerInput(Unit) {
            awaitPointerEventScope {
                // 消耗所有事件，阻止它们传递给底层组件
                while (true) {
                    awaitPointerEvent(pass = PointerEventPass.Initial)
                    this.currentEvent.changes.forEach { it.consume() }
                }
            }
        }
    } else this
