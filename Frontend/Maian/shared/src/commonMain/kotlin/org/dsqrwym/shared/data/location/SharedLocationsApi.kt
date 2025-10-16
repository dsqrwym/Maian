package org.dsqrwym.shared.data.location

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import org.dsqrwym.shared.data.location.dto.CityDto
import org.dsqrwym.shared.data.location.dto.CountryDto
import org.dsqrwym.shared.data.location.dto.CurrencyDto
import org.dsqrwym.shared.data.location.dto.ProvinceDto
import org.dsqrwym.shared.network.ApiConfig
import org.dsqrwym.shared.network.ApiResponse

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
}