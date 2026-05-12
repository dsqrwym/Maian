package org.dsqrwym.shared.data.profile

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.dsqrwym.shared.data.location.dto.CityDto
import org.dsqrwym.shared.data.location.dto.CountryDto
import org.dsqrwym.shared.data.location.dto.ProvinceDto
import org.dsqrwym.shared.data.user.SpanishCompanyType

@Serializable
data class WholesalerProfileResponseDto(
    val id: String? = null,
    val email: String? = null,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("first_name") val firstName: String? = null,
    @SerialName("last_name") val lastName: String? = null,
    val username: String? = null,
    val telephone: String? = null,
    @SerialName("tax_id") val taxId: String? = null,
    val profile: WholesalerProfileDto? = null,
    @SerialName("profile_image_file_id") val logoFileId: String? = null,
    @SerialName("store_directions") val storeDirections: StoreDirectionDto? = null,
)

@Serializable
data class WholesalerProfileDto(
    @SerialName("company_name") val companyName: String,
    @SerialName("company_type") val companyType: SpanishCompanyType,
    @SerialName("display_name") val displayName: String? = null,
    val description: String? = null,
    @SerialName("delivery_area_description") val deliveryAreaDescription: String? = null,
    @SerialName("minimum_order_amount") val minimumOrderAmount: String? = null,
    @SerialName("delivery_available") val deliveryAvailable: Boolean? = null,
    @SerialName("pickup_available") val pickupAvailable: Boolean? = null,
)

@Serializable
data class StoreDirectionDto(
    val street: String? = null,
    @SerialName("zip_code") val zipCode: String? = null,
    val city: CityDto? = null,
    val province: ProvinceDto? = null,
    val country: CountryDto? = null,
)
