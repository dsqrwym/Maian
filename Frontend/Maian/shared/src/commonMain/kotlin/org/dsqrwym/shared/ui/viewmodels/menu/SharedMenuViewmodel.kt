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
        if (_menuStates.value.isEmpty()) {
            _menuStates.value = items
        }
    }

    fun setBadge(route: Any, count: Int = 0, show: Boolean = true) {
        _menuStates.update { states ->
            states.map {
                if (isSameRoute(route, it.item.route))
                    it.copy(showBadge = show, badgeCount = count)
                else it
            }
        }
    }

    fun getBadgeCount(route: Any): Int {
        return _menuStates.value.find { isSameRoute(route, it.item.route) }?.badgeCount ?: 0
    }

    fun clearBadgeAll() {
        _menuStates.update { states ->
            states.map {
                it.copy(showBadge = false, badgeCount = 0)
            }
        }
    }
}