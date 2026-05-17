package org.dsqrwym.shared.data.orders

import kotlinx.coroutines.flow.SharedFlow
import org.dsqrwym.shared.data.orders.dto.SharedOrderDetail
import org.dsqrwym.shared.network.model.SharedResponseResult

interface OrderDetailRepository {
    val updateEvents: SharedFlow<Unit>

    suspend fun getOrderDetail(id: String): SharedResponseResult<SharedOrderDetail>

    suspend fun acceptOrder(id: String): SharedResponseResult<Unit>

    suspend fun rejectOrder(id: String, reason: String): SharedResponseResult<Unit>

    suspend fun cancelOrder(id: String, reason: String?): SharedResponseResult<Unit>

    suspend fun updateEstimatedDeliveryDate(
        id: String,
        estimatedDeliveryDate: String?,
    ): SharedResponseResult<Unit>
}
