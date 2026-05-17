package org.dsqrwym.enterprise.data.order

import io.ktor.http.HttpStatusCode
import org.dsqrwym.shared.data.SharedObservableRepository
import org.dsqrwym.shared.data.orders.OrderDetailRepository
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

class EnterpriseOrderRepository(
    private val orderApi: SharedOrderApi,
) : SharedObservableRepository(), OrderHistoryRepository, OrderDetailRepository {
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

    override suspend fun getOrderDetail(id: String): SharedResponseResult<SharedOrderDetail> =
        withAuthOrError {
            safeApiCall {
                orderApi.getWholesalerOrderDetail(
                    id = id.trim(),
                    langCode = LanguageManager.getCurrentLanguage(),
                )
            }
        }

    override suspend fun acceptOrder(id: String): SharedResponseResult<Unit> =
        withAuthOrError {
            safeApiCall {
                orderApi.acceptOrder(id.trim())
            }.notifyUpdated()
        }

    override suspend fun rejectOrder(id: String, reason: String): SharedResponseResult<Unit> =
        withAuthOrError {
            safeApiCall {
                orderApi.rejectOrder(id.trim(), reason.trim())
            }.notifyUpdated()
        }

    override suspend fun cancelOrder(id: String, reason: String?): SharedResponseResult<Unit> = unsupportedOrderAction()

    override suspend fun updateEstimatedDeliveryDate(id: String, estimatedDeliveryDate: String?): SharedResponseResult<Unit> =
        withAuthOrError {
            safeApiCall {
                orderApi.updateEstimatedDeliveryDate(
                    id = id.trim(),
                    estimatedDeliveryDate = estimatedDeliveryDate,
                )
            }.notifyUpdated()
        }
}

private fun unsupportedOrderAction(): SharedResponseResult<Unit> =
    SharedResponseResult.Error(HttpStatusCode.Forbidden)
