package org.dsqrwym.shared.util.lazygrid

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.ui.unit.dp
import org.dsqrwym.shared.ui.components.buttons.SharedRetryButton
import org.dsqrwym.shared.ui.components.progressindicators.SharedCircularProgressIndicator

object SharedLazyGridLayout {
    val horizontalArrangement = Arrangement.spacedBy(12.dp)
    val verticalItemSpacing = 12.dp
    val horizontalPadding = 16.dp

    fun LazyStaggeredGridScope.appendLoadingIndicator() {
        item(span = StaggeredGridItemSpan.FullLine) {
            SharedCircularProgressIndicator()
        }
    }

    fun LazyStaggeredGridScope.appendErrorRetry(retry: () -> Unit) {
        item(span = StaggeredGridItemSpan.FullLine) {
            SharedRetryButton(retry = retry)
        }
    }
}