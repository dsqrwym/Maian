package org.dsqrwym.enterprise.data.order

import org.dsqrwym.shared.data.SharedObservableRepository
import org.dsqrwym.shared.data.orders.OrderHistoryRepository
import org.dsqrwym.shared.data.orders.SharedOrderApi
import org.dsqrwym.shared.data.orders.dto.SharedFindOrderDto
import org.dsqrwym.shared.data.orders.dto.SharedOrderFilterMetadataDto
import org.dsqrwym.shared.data.orders.dto.SharedOrderSummary
import org.dsqrwym.shared.network.model.ApiResponseList
import org.dsqrwym.shared.network.model.SharedResponseResult
import org.dsqrwym.shared.network.safeApiCall
import org.dsqrwym.shared.network.withAuthOrError

class EnterpriseOrderRepository(
    private val orderApi: SharedOrderApi,
) : SharedObservableRepository(), OrderHistoryRepository {
    override suspend fun getOrders(query: SharedFindOrderDto): SharedResponseResult<ApiResponseList<SharedOrderSummary>> =
        withAuthOrError {
            safeApiCall {
                orderApi.getWholesalerOrders(query)
            }
        }

    override suspend fun getOrderFilterMetadata(): SharedResponseResult<SharedOrderFilterMetadataDto> =
        withAuthOrError {
            safeApiCall {
                orderApi.getWholesalerOrderFilterMetadata()
            }
        }

    suspend fun acceptOrder(id: String): SharedResponseResult<Unit> =
        withAuthOrError {
            safeApiCall {
                orderApi.acceptOrder(id.trim())
            }.notifyUpdated()
        }

    suspend fun rejectOrder(id: String, reason: String): SharedResponseResult<Unit> =
        withAuthOrError {
            safeApiCall {
                orderApi.rejectOrder(id.trim(), reason.trim())
            }.notifyUpdated()
        }

    suspend fun updateEstimatedDeliveryDate(id: String, estimatedDeliveryDate: String?): SharedResponseResult<Unit> =
        withAuthOrError {
            safeApiCall {
                orderApi.updateEstimatedDeliveryDate(
                    id = id.trim(),
                    estimatedDeliveryDate = estimatedDeliveryDate,
                )
            }.notifyUpdated()
        }
}
