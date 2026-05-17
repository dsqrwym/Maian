package org.dsqrwym.standard.data.order

import io.ktor.http.HttpStatusCode
import org.dsqrwym.shared.data.orders.OrderDetailRepository
import org.dsqrwym.shared.data.SharedObservableRepository
import org.dsqrwym.shared.data.orders.OrderHistoryRepository
import org.dsqrwym.shared.data.orders.SharedOrderApi
import org.dsqrwym.shared.data.orders.dto.SharedFindOrderDto
import org.dsqrwym.shared.data.orders.dto.SharedOrderDetail
import org.dsqrwym.shared.data.orders.dto.SharedOrderFilterMetadataDto
import org.dsqrwym.shared.data.orders.dto.SharedOrderSummary
import org.dsqrwym.shared.localization.LanguageManager
import org.dsqrwym.shared.network.model.ApiResponseList
import org.dsqrwym.shared.network.model.SharedResponseResult
import org.dsqrwym.shared.network.safeApiCall
import org.dsqrwym.shared.network.withAuthOrError
import org.dsqrwym.standard.data.order.dto.CreateOrderFromCartRequest

class StandardOrderRepository(
    private val orderApi: StandardOrderApi,
    private val sharedOrderApi: SharedOrderApi,
) : SharedObservableRepository(), OrderHistoryRepository, OrderDetailRepository {
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

    override suspend fun getOrderDetail(id: String): SharedResponseResult<SharedOrderDetail> =
        withAuthOrError {
            safeApiCall {
                sharedOrderApi.getRetailerOrderDetail(
                    id = id.trim(),
                    langCode = LanguageManager.getCurrentLanguage(),
                )
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

    override suspend fun cancelOrder(id: String, reason: String?): SharedResponseResult<Unit> =
        withAuthOrError {
            safeApiCall {
                sharedOrderApi.cancelOrder(
                    id = id.trim(),
                    reason = reason?.trim()?.takeIf { it.isNotEmpty() },
                )
            }.notifyUpdated()
        }

    override suspend fun acceptOrder(id: String): SharedResponseResult<Unit> = unsupportedOrderAction()

    override suspend fun rejectOrder(id: String, reason: String): SharedResponseResult<Unit> = unsupportedOrderAction()

    override suspend fun updateEstimatedDeliveryDate(
        id: String,
        estimatedDeliveryDate: String?,
    ): SharedResponseResult<Unit> = unsupportedOrderAction()
}

private fun unsupportedOrderAction(): SharedResponseResult<Unit> =
    SharedResponseResult.Error(HttpStatusCode.Forbidden)
