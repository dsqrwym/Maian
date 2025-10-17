package org.dsqrwym.shared.navigation.core

import kotlinx.serialization.Serializable

sealed class NavigationEvent {
    data class ToRoute<T : @Serializable Any>(val route: T) : NavigationEvent()
}
