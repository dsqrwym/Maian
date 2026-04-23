package org.dsqrwym.shared.data.location

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import org.dsqrwym.shared.data.location.dto.*
import org.dsqrwym.shared.network.ApiConfig
import org.dsqrwym.shared.network.model.ApiResponse
import org.dsqrwym.shared.network.HttpClientProvider
import org.dsqrwym.shared.util.log.SharedLog
import org.dsqrwym.shared.util.platform.PlatformType
import org.dsqrwym.shared.util.platform.getPlatform


class SharedLocationsApi(private val client: HttpClient) {
    suspend fun getCountries(): ApiResponse<List<CountryDto>> {
        return client.get(ApiConfig.LocationsPath.COUNTRIES).body()
    }

    suspend fun getProvincesByCountry(isoNumeric: Int): ApiResponse<List<ProvinceDto>> {
        return client.get(ApiConfig.LocationsPath.provincesByCountry(isoNumeric)).body()
    }

    suspend fun getCitiesByProvince(provinceId: Int): ApiResponse<List<CityDto>> {
        return client.get(ApiConfig.LocationsPath.citiesByProvince(provinceId)).body()
    }

    suspend fun getCurrencyByIsoNumeric(isoNumeric: Int): ApiResponse<CurrencyDto> {
        return client.get(ApiConfig.LocationsPath.currencyByCountry(isoNumeric)).body()
    }

    /**
     * 获取当前设备的真实国家代码（如 CN / US / JP）
     * - 优先通过 https://ipapi.co/json/
     * - 失败时返回系统默认 Locale.country
     * - 自动缓存结果，避免重复请求
     */
    suspend fun getRealRegionCode(): IpApiResponse {
        val platform = getPlatform()
        val client =
            if (platform.type is PlatformType.Web) HttpClientProvider.publicClient else HttpClientProvider.client

        val endpoints = if (getPlatform().type is PlatformType.Web) listOf("https://ipinfo.io/json") else listOf(
            "https://ipapi.co/json/",
            "https://ipinfo.io/json",
        )

        for (url in endpoints) {
            return try {
                client?.get(url)?.body() ?: IpApiResponse(null)
            } catch (e: Exception) {
                SharedLog.log(
                    message = """
                    URL $url Failed : $e 
                    ${client?.get(url)?.bodyAsText()}
                """.trimIndent(), tag = "Detected"
                )
                continue
            }
        }
        return client?.get("https://ipinfo.io/json")?.body() ?: IpApiResponse(null)
    }
}