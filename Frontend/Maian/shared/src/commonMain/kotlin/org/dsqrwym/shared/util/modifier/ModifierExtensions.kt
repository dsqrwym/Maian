package org.dsqrwym.shared.util.modifier

//import com.eygraber.compose.placeholder.material3
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.eygraber.compose.placeholder.PlaceholderHighlight
import com.eygraber.compose.placeholder.material3.placeholder
import com.eygraber.compose.placeholder.material3.shimmer
import org.dsqrwym.shared.util.navigation.WindowWidthSizeClass
import org.dsqrwym.shared.util.navigation.calculateWindowSizeClass

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

@Composable
fun Modifier.paddingTopForMenu(): Modifier {
    if (calculateWindowSizeClass().widthSizeClass == WindowWidthSizeClass.Compact) return this

    return this.padding(
        top = if (calculateWindowSizeClass().widthSizeClass == WindowWidthSizeClass.Expanded) 76.dp else 0.dp
    )
}

@Composable
fun Modifier.placeholderWithShimmer(visible: Boolean): Modifier {
    return this.placeholder(visible, highlight = PlaceholderHighlight.shimmer())
}

@Composable
fun Modifier.paddingWithoutTop(padding: PaddingValues): Modifier{
    return this.padding(
        bottom = padding.calculateBottomPadding(),
        start = padding.calculateStartPadding(LayoutDirection.Ltr),
        end = padding.calculateEndPadding(LayoutDirection.Ltr)
    )
}