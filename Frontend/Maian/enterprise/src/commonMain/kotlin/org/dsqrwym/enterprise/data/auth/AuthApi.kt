package org.dsqrwym.enterprise.data.auth

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.dsqrwym.enterprise.data.auth.dto.CompleteRegisterRequest
import org.dsqrwym.enterprise.data.auth.dto.StartRegisterRequest
import org.dsqrwym.enterprise.network.EnterpriseApi.AuthPath.REGISTRATION_WHOLESALER
import org.dsqrwym.enterprise.network.EnterpriseApi.AuthPath.REGISTRATION_WHOLESALER_COMPLETE
import org.dsqrwym.shared.network.ApiResponse

class AuthApi(private val client: HttpClient) {
    suspend fun startRegister(dto: StartRegisterRequest): ApiResponse<Unit> {
        return client.post(REGISTRATION_WHOLESALER) {
            contentType(ContentType.Application.Json)
            setBody(dto)
        }.body()
    }

    suspend fun completeResister(dto: CompleteRegisterRequest): ApiResponse<Unit> {
        return client.post(REGISTRATION_WHOLESALER_COMPLETE) {
            contentType(ContentType.Application.Json)
            setBody(dto)
        }.body()
    }
}