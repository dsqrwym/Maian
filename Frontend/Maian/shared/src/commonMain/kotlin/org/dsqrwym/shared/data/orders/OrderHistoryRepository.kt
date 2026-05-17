package org.dsqrwym.shared.data.orders

import kotlinx.coroutines.flow.SharedFlow
import org.dsqrwym.shared.data.orders.dto.SharedFindOrderDto
import org.dsqrwym.shared.data.orders.dto.SharedOrderFilterMetadataDto
import org.dsqrwym.shared.data.orders.dto.SharedOrderSummary
import org.dsqrwym.shared.network.model.ApiResponseList
import org.dsqrwym.shared.network.model.SharedResponseResult

interface OrderHistoryRepository : OrderPdfActionsRepository {
    val updateEvents: SharedFlow<Unit>

    suspend fun getOrders(query: SharedFindOrderDto): SharedResponseResult<ApiResponseList<SharedOrderSummary>>

    suspend fun getOrderFilterMetadata(): SharedResponseResult<SharedOrderFilterMetadataDto>
}
