package org.dsqrwym.standard.ui.viewmodels.browse

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.dsqrwym.standard.domain.browse.RetailDistributor

data class BrowseScopeState(
    val wholesalerId: String? = null,
    val wholesalerName: String? = null,
    val wholesaler: RetailDistributor? = null,
)

class BrowseScopeStateHolder {
    var state by mutableStateOf(BrowseScopeState())
        private set

    fun selectWholesaler(wholesaler: RetailDistributor) {
        state = BrowseScopeState(
            wholesalerId = wholesaler.id,
            wholesalerName = wholesaler.displayName,
            wholesaler = wholesaler,
        )
    }

    fun clearWholesaler() {
        state = BrowseScopeState()
    }
}
