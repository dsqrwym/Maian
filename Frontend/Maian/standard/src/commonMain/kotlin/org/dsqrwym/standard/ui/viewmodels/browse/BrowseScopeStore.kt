package org.dsqrwym.standard.ui.viewmodels.browse

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.dsqrwym.standard.domain.browse.RetailWholesaler

data class BrowseScopeState(
    val wholesalerId: String? = null,
    val wholesalerName: String? = null,
    val wholesaler: RetailWholesaler? = null,
)

object BrowseScopeStore {
    var state by mutableStateOf(BrowseScopeState())
        private set

    fun selectWholesaler(wholesaler: RetailWholesaler) {
        state = BrowseScopeState(
            wholesalerId = wholesaler.id,
            wholesalerName = wholesaler.displayName?.takeIf { it.isNotBlank() }
                ?: wholesaler.companyName.takeIf { it.isNotBlank() },
            wholesaler = wholesaler,
        )
    }

    fun clearWholesaler() {
        state = BrowseScopeState()
    }
}
