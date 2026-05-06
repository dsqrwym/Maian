package org.dsqrwym.shared.navigation.core

import androidx.navigation3.runtime.NavKey

sealed class NavigationEvent {
    data class ToRoute(val route: NavKey) : NavigationEvent()
    object Back : NavigationEvent()
}
