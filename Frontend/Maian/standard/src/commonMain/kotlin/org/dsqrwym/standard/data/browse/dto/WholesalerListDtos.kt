package org.dsqrwym.standard.data.browse.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.dsqrwym.shared.data.location.dto.CityDto
import org.dsqrwym.shared.data.location.dto.ProvinceDto
import org.dsqrwym.shared.data.user.SpanishCompanyType

@Serializable
data class WholesalerListItemDto(
    val id: String,
    @SerialName("user_id")
    val userId: String? = null,
    @SerialName("profile_image_file_id")
    val profileImageFileId: Long? = null,
    @SerialName("display_name")
    val displayName: String? = null,
    @SerialName("company_name")
    val companyName: String,
    @SerialName("company_type")
    val companyType: SpanishCompanyType? = null,
    val description: String? = null,
    @SerialName("delivery_available")
    val deliveryAvailable: Boolean? = null,
    @SerialName("pickup_available")
    val pickupAvailable: Boolean? = null,
    @SerialName("minimum_order_amount")
    val minimumOrderAmount: String? = null,
    @SerialName("delivery_area_description")
    val deliveryAreaDescription: String? = null,
    val city: CityDto? = null,
    val province: ProvinceDto? = null,
)
