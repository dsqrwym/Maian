package org.dsqrwym.shared.data.orders

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.dsqrwym.shared.data.orders.dto.SharedFindOrderDto
import org.dsqrwym.shared.data.orders.dto.SharedOrderActionReasonDto
import org.dsqrwym.shared.data.orders.dto.SharedOrderDetail
import org.dsqrwym.shared.data.orders.dto.SharedOrderEstimatedDeliveryDateDto
import org.dsqrwym.shared.data.orders.dto.SharedOrderFilterMetadataDto
import org.dsqrwym.shared.data.orders.dto.SharedOrderSummary
import org.dsqrwym.shared.network.ApiConfig
import org.dsqrwym.shared.network.model.ApiResponse
import org.dsqrwym.shared.network.model.ApiResponseList

class SharedOrderApi(private val client: HttpClient) {
    suspend fun getRetailerOrders(query: SharedFindOrderDto): ApiResponse<ApiResponseList<SharedOrderSummary>> =
        client.get(ApiConfig.OrderPath.RETAILER) {
            applyOrderQuery(query)
        }.body()

    suspend fun getWholesalerOrders(query: SharedFindOrderDto): ApiResponse<ApiResponseList<SharedOrderSummary>> =
        client.get(ApiConfig.OrderPath.WHOLESALER) {
            applyOrderQuery(query)
        }.body()

    suspend fun getRetailerOrderFilterMetadata(): ApiResponse<SharedOrderFilterMetadataDto> =
        client.get(ApiConfig.OrderPath.RETAILER_FILTER_METADATA).body()

    suspend fun getWholesalerOrderFilterMetadata(): ApiResponse<SharedOrderFilterMetadataDto> =
        client.get(ApiConfig.OrderPath.WHOLESALER_FILTER_METADATA).body()

    suspend fun getRetailerOrderDetail(id: String, langCode: String): ApiResponse<SharedOrderDetail> =
        client.get(ApiConfig.OrderPath.retailerDetail(id)) {
            parameter("langCode", langCode)
        }.body()

    suspend fun getWholesalerOrderDetail(id: String, langCode: String): ApiResponse<SharedOrderDetail> =
        client.get(ApiConfig.OrderPath.wholesalerDetail(id)) {
            parameter("langCode", langCode)
        }.body()

    suspend fun cancelOrder(id: String, reason: String?): ApiResponse<Unit> =
        client.post(ApiConfig.OrderPath.cancel(id)) {
            contentType(ContentType.Application.Json)
            setBody(SharedOrderActionReasonDto(actionReason = reason))
        }.body()

    suspend fun rejectOrder(id: String, reason: String): ApiResponse<Unit> =
        client.post(ApiConfig.OrderPath.reject(id)) {
            contentType(ContentType.Application.Json)
            setBody(SharedOrderActionReasonDto(actionReason = reason))
        }.body()

    suspend fun acceptOrder(id: String): ApiResponse<Unit> =
        client.post(ApiConfig.OrderPath.accept(id)).body()

    suspend fun updateEstimatedDeliveryDate(id: String, estimatedDeliveryDate: String?): ApiResponse<Unit> =
        client.patch(ApiConfig.OrderPath.estimatedDeliveryDate(id)) {
            contentType(ContentType.Application.Json)
            setBody(SharedOrderEstimatedDeliveryDateDto(estimatedDeliveryDate = estimatedDeliveryDate))
        }.body()
}

private fun io.ktor.client.request.HttpRequestBuilder.applyOrderQuery(query: SharedFindOrderDto) {
    query.search?.trim()?.takeIf { it.isNotEmpty() }?.let { parameter("search", it) }
    query.status?.let { parameter("status", it.name) }
    query.startDate?.let { parameter("startDate", it) }
    query.endDate?.let { parameter("endDate", it) }
    parameter("sortBy", query.sortBy.name)
    parameter("orderBy", query.orderBy.value)
    query.minTotalPrice?.let { parameter("minTotalPrice", it) }
    query.maxTotalPrice?.let { parameter("maxTotalPrice", it) }
    query.minSubtotal?.let { parameter("minSubtotal", it) }
    query.maxSubtotal?.let { parameter("maxSubtotal", it) }
    query.minTotalIva?.let { parameter("minTotalIva", it) }
    query.maxTotalIva?.let { parameter("maxTotalIva", it) }
    query.minItemCount?.let { parameter("minItemCount", it) }
    query.maxItemCount?.let { parameter("maxItemCount", it) }
    parameter("page", query.page)
    parameter("limit", query.limit)
}
