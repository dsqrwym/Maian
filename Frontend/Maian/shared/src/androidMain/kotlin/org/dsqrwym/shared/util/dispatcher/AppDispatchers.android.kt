package org.dsqrwym.shared.util.dispatcher

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

actual object AppDispatchers {
    actual val IO: CoroutineDispatcher = Dispatchers.IO
}
