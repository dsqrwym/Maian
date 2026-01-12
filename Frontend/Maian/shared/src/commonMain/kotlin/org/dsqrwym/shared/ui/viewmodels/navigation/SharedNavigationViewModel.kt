package org.dsqrwym.shared.ui.viewmodels.navigation
import androidx.lifecycle.ViewModel
import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SharedNavigationViewModel(initRoute: NavKey) : ViewModel() {
    private val _backStack = MutableStateFlow(listOf(initRoute))
    val backStack = _backStack.asStateFlow()

    fun navigate(route: NavKey) {
        _backStack.value += route
    }

    fun pop() {
        if (_backStack.value.size > 1) {
            _backStack.value = _backStack.value.dropLast(1)
        }
    }

    fun replace(route: NavKey) {
        _backStack.value = _backStack.value.dropLast(1) + route
    }

    fun current(): NavKey = _backStack.value.last()
}
