package org.dsqrwym.shared.util.dispatcher

import kotlinx.coroutines.CoroutineDispatcher

expect object AppDispatchers {
    val IO: CoroutineDispatcher
}
