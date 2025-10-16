package org.dsqrwym.shared.data.location

import org.dsqrwym.shared.data.location.dto.CityDto
import org.dsqrwym.shared.data.location.dto.CountryDto
import org.dsqrwym.shared.data.location.dto.CurrencyDto
import org.dsqrwym.shared.data.location.dto.ProvinceDto
import org.dsqrwym.shared.network.SharedResponseResult
import org.dsqrwym.shared.network.safeApiCall

class SharedLocationRepository(private val api: SharedLocationsApi) {
    suspend fun getCountries(): SharedResponseResult<List<CountryDto>> {
        val result = safeApiCall { api.getCountries() }
        if (result is SharedResponseResult.Success) {
            return SharedResponseResult.Success(result.data ?: emptyList())
        }
        return result
    }

    suspend fun getProvincesByCountry(isoNumeric: Int): SharedResponseResult<List<ProvinceDto>> {
        val result = safeApiCall { api.getProvincesByCountry(isoNumeric) }
        if (result is SharedResponseResult.Success) {
            val provincesWithCountryIso = result.data?.map { it.copy(countryIso = isoNumeric) } ?: emptyList()
            return SharedResponseResult.Success(provincesWithCountryIso)
        }
        return result
    }

    suspend fun getCitiesByProvince(provinceId: Int): SharedResponseResult<List<CityDto>> {
        val result = safeApiCall { api.getCitiesByProvince(provinceId) }
        if (result is SharedResponseResult.Success) {
            val citiesWithProvinceId = result.data?.map { it.copy(provinceId = provinceId) } ?: emptyList()
            return SharedResponseResult.Success(citiesWithProvinceId)
        }
        return result
    }

    suspend fun getCurrencyByIsoNumeric(isoNumeric: Int): SharedResponseResult<CurrencyDto> =
        safeApiCall { api.getCurrencyByIsoNumeric(isoNumeric) }
}