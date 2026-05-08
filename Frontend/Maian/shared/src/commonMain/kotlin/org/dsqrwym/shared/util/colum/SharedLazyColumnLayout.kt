package org.dsqrwym.shared.util.colum

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.dsqrwym.shared.ui.components.buttons.SharedRetryButton
import org.dsqrwym.shared.ui.components.progressindicators.SharedLoadingDotsIndicator

object SharedLazyColumnLayout {
    fun LazyListScope.appendLoadingIndicator() {
        item {
            Box(
                modifier = Modifier.fillMaxSize().heightIn(min = 100.dp).animateItem(),
                contentAlignment = Alignment.Center
            ) {
                SharedLoadingDotsIndicator()
            }
        }
    }

    fun LazyListScope.appendErrorRetry(retry: () -> Unit) {
        item {
            Box(
                modifier = Modifier.fillMaxSize().animateItem(),
                contentAlignment = Alignment.Center
            ) { SharedRetryButton(retry) }
        }
    }
}