package org.dsqrwym.shared.ui.viewmodels.menu


import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.dsqrwym.shared.navigation.menu.SharedMenuItemState
import org.dsqrwym.shared.util.navigation.isSameRoute

class SharedMenuViewModel : ViewModel() {

    private val _menuStates = MutableStateFlow<List<SharedMenuItemState>>(emptyList())
    val menuStates = _menuStates.asStateFlow()

    fun initMenu(items: List<SharedMenuItemState>) {
        _menuStates.value = items
    }

    fun setBadge(route: Any, show: Boolean, count: Int = 0) {
        _menuStates.update { states ->
            states.map {
                if (isSameRoute(route, it))
                    it.copy(showBadge = show, badgeCount = count)
                else it
            }
        }
    }
}