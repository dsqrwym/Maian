package org.dsqrwym.shared.data.location.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CountryDto(
    @SerialName("iso_numeric") val isoNumeric: Int,
    val name: String,
    @SerialName("name_local") val nameLocal: String,
    @SerialName("iso_alpha2") val isoAlpha2: String? = null,
    @SerialName("iso_alpha3") val isoAlpha3: String? = null,
    @SerialName("currency_id") val currencyId: Int? = null
)

@Serializable
data class ProvinceDto(
    val id: Int,
    val name: String,
    @SerialName("name_local") val nameLocal: String,
    @SerialName("country_iso") val countryIso: Int? = null
)

@Serializable
data class CityDto(
    val id: Int,
    val name: String,
    @SerialName("name_local") val nameLocal: String,
    @SerialName("province_id") val provinceId: Int? = null
)

@Serializable
data class CurrencyDto(
    @SerialName("iso_alpha3") val isoAlpha3: String,
    val symbol: String? = null,
    @SerialName("decimal_digits") val decimalDigits: Int? = null
)
