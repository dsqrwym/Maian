package org.dsqrwym.shared.data.user

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import org.dsqrwym.shared.data.user.dto.FindUserQueryDto
import org.dsqrwym.shared.data.user.dto.FindWholesalerQueryDto
import org.dsqrwym.shared.network.ApiConfig
import org.dsqrwym.shared.network.model.ApiResponse
import org.dsqrwym.shared.network.model.ApiResponseList

class SharedUserApi(val client: HttpClient) {
    suspend inline fun <reified T> getUsers(query: FindUserQueryDto): ApiResponse<ApiResponseList<T>> {
        return client.get(ApiConfig.UserPath.USER) {
            query.search?.let { parameter("search", it) }
            query.role?.let { parameter("role", it) }
            query.status?.let { parameter("status", it) }
            query.userId?.let { parameter("user_id", it) }
            query.username?.let { parameter("username", it) }
            query.email?.let { parameter("email", it) }
            query.firstName?.let { parameter("first_name", it) }
            query.lastName?.let { parameter("last_name", it) }
            query.telephone?.let { parameter("telephone", it) }
            query.cif?.let { parameter("cif", it) }
            query.profile?.let { parameter("profile", it) }
            query.selectUserStatus?.let { parameter("selectUserStatus", it) }
            query.selectUserRole?.let { parameter("selectUserRole", it) }
            query.orderBy?.let { parameter("orderBy", it.value) }
            query.orderDir?.let { parameter("orderDir", it.value) }
            parameter("page", query.page)
            parameter("limit", query.limit)
        }.body()
    }

    suspend inline fun <reified T> getWholesalers(query: FindWholesalerQueryDto): ApiResponse<ApiResponseList<T>> {
        return client.get(ApiConfig.UserPath.WHOLESALERS) {
            query.search?.let { parameter("search", it) }
            query.deliveryAvailable?.let { parameter("delivery_available", it) }
            query.pickupAvailable?.let { parameter("pickup_available", it) }
            query.companyType?.let { parameter("company_type", it.value) }
            parameter("orderBy", query.orderBy.value)
            parameter("orderDir", query.orderDir.value)
            parameter("page", query.page)
            parameter("limit", query.limit)
        }.body()
    }
}
