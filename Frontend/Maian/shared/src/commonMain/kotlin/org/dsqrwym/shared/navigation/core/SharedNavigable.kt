package org.dsqrwym.shared.navigation.core

import kotlinx.coroutines.flow.SharedFlow

interface SharedNavigable {
    val navigateEvent: SharedFlow<NavigationEvent>
    suspend fun emitNavigation(event: NavigationEvent)
}