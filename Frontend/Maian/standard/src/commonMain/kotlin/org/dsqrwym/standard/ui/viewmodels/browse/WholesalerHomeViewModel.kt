package org.dsqrwym.standard.ui.viewmodels.browse

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class WholesalerHomeViewModel : ViewModel() {
    var selectedTab by mutableStateOf(0)
        private set

    fun selectTab(index: Int) {
        selectedTab = index
    }
}
