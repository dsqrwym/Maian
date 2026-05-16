package org.dsqrwym.shared.util.lazygrid

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.dsqrwym.shared.ui.components.buttons.SharedRetryButton
import org.dsqrwym.shared.ui.components.progressindicators.SharedLoadingDotsIndicator

object SharedLazyGridLayout {
    val arrangement = Arrangement.spacedBy(12.dp)
    val verticalItemSpacing = 12.dp
    val Padding = 16.dp

    fun LazyStaggeredGridScope.appendLoadingIndicator() {
        item(span = StaggeredGridItemSpan.SingleLane) {
            Box(
                modifier = Modifier.fillMaxSize().heightIn(min = 100.dp).animateItem(),
                contentAlignment = Alignment.Center
            ) {
                SharedLoadingDotsIndicator()
            }
        }
    }

    fun LazyStaggeredGridScope.appendErrorRetry(retry: () -> Unit) {
        item(span = StaggeredGridItemSpan.FullLine) {
            Box(
                modifier = Modifier.fillMaxSize().animateItem(),
                contentAlignment = Alignment.Center
            ) { SharedRetryButton(retry) }
        }
    }

    fun LazyGridScope.appendLoadingIndicator() {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Box(
                modifier = Modifier.fillMaxSize().heightIn(min = 100.dp).animateItem(),
                contentAlignment = Alignment.Center
            ) {
                SharedLoadingDotsIndicator()
            }
        }
    }

    fun LazyGridScope.appendErrorRetry(retry: () -> Unit) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Box(
                modifier = Modifier.fillMaxSize().animateItem(),
                contentAlignment = Alignment.Center
            ) { SharedRetryButton(retry) }
        }
    }
}
