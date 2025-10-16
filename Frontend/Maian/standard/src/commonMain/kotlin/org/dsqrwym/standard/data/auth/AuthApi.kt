package org.dsqrwym.standard.data.auth

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.dsqrwym.shared.network.ApiConfig
import org.dsqrwym.shared.network.ApiResponse
import org.dsqrwym.standard.data.auth.dto.CompleteRegisterRequest
import org.dsqrwym.standard.data.auth.dto.StartRegisterRequest

class AuthApi(private val client: HttpClient) {
    suspend fun startRegister(dto: StartRegisterRequest): ApiResponse<Unit> {
        return client.post(ApiConfig.AuthPath.REGISTRATION_RETAILER) {
            contentType(ContentType.Application.Json)
            setBody(dto)
        }.body()
    }

    suspend fun completeRegister(dto: CompleteRegisterRequest): ApiResponse<Unit> {
        return client.post(ApiConfig.AuthPath.REGISTRATION_RETAILER_COMPLETE) {
            contentType(ContentType.Application.Json)
            setBody(dto)
        }.body()
    }

}