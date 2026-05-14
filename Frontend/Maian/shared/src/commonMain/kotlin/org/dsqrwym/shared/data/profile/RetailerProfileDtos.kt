package org.dsqrwym.shared.data.profile

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.dsqrwym.shared.data.user.SpanishCompanyType

@Serializable
data class RetailerProfileResponseDto(
    val id: String? = null,
    val email: String? = null,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("first_name") val firstName: String? = null,
    @SerialName("last_name") val lastName: String? = null,
    val username: String? = null,
    val telephone: String? = null,
    @SerialName("tax_id") val taxId: String? = null,
    val profile: RetailerProfileDto? = null,
    @SerialName("profile_image_file_id") val logoFileId: String? = null,
    @SerialName("store_directions") val storeDirections: StoreDirectionDto? = null,
)

@Serializable
data class RetailerProfileDto(
    @SerialName("company_name") val companyName: String? = null,
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("company_type") val companyType: SpanishCompanyType? = null,
    @SerialName("contact_name") val contactName: String? = null,
)
