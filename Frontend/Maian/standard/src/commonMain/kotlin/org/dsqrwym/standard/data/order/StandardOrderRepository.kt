package org.dsqrwym.standard.data.order

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
import org.dsqrwym.standard.data.order.dto.CreateOrderFromCartRequest

class StandardOrderRepository(
    private val orderApi: StandardOrderApi,
    private val sharedOrderApi: SharedOrderApi,
) : SharedObservableRepository(), OrderHistoryRepository {
    override suspend fun getOrders(query: SharedFindOrderDto): SharedResponseResult<ApiResponseList<SharedOrderSummary>> =
        withAuthOrError {
            safeApiCall {
                sharedOrderApi.getRetailerOrders(query)
            }
        }

    override suspend fun getOrderFilterMetadata(): SharedResponseResult<SharedOrderFilterMetadataDto> =
        withAuthOrError {
            safeApiCall {
                sharedOrderApi.getRetailerOrderFilterMetadata()
            }
        }

    suspend fun createOrderFromCart(wholesalerId: String): SharedResponseResult<Unit> =
        withAuthOrError {
            safeApiCall {
                orderApi.createOrderFromCart(
                    CreateOrderFromCartRequest(wholesalerId = wholesalerId.trim())
                )
            }.notifyUpdated()
        }

    suspend fun cancelOrder(id: String, reason: String?): SharedResponseResult<Unit> =
        withAuthOrError {
            safeApiCall {
                sharedOrderApi.cancelOrder(
                    id = id.trim(),
                    reason = reason?.trim()?.takeIf { it.isNotEmpty() },
                )
            }.notifyUpdated()
        }
}
