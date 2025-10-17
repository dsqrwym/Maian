package org.dsqrwym.shared.navigation.core

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class SharedNavigableDelegate : SharedNavigable {
    private val _navigateEvent = MutableSharedFlow<NavigationEvent>(extraBufferCapacity = 1)
    override val navigateEvent = _navigateEvent.asSharedFlow()

    override suspend fun emitNavigation(event: NavigationEvent) {
        _navigateEvent.emit(event)
    }
}