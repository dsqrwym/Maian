package org.dsqrwym.shared.data

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.dsqrwym.shared.network.model.SharedResponseResult

open class SharedObservableRepository {
    private val _updateEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val updateEvents = _updateEvents.asSharedFlow()
    suspend fun notifyUpdated() {
        _updateEvents.emit(Unit)
    }

    suspend fun <T>SharedResponseResult<T>.notifyUpdated(): SharedResponseResult<T> {
        if (this is SharedResponseResult.Success) {
            // 显式调用成员函数, 不然wasmjs出现递归错误
            (this@SharedObservableRepository).notifyUpdated()
        }
        return this
    }
}