package org.dsqrwym.enterprise.data.profile.dto

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.dsqrwym.shared.data.user.SpanishCompanyType
import org.dsqrwym.shared.serialization.OptionalField
import org.dsqrwym.shared.serialization.OptionalFieldSerializer

@Serializable
data class UpdateWholesalerProfileDto(
    @SerialName("first_name")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @Serializable(with = OptionalFieldSerializer::class)
    val firstName: OptionalField<String?> = OptionalField.Undefined,

    @SerialName("last_name")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @Serializable(with = OptionalFieldSerializer::class)
    val lastName: OptionalField<String?> = OptionalField.Undefined,

    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @Serializable(with = OptionalFieldSerializer::class)
    val username: OptionalField<String?> = OptionalField.Undefined,

    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @Serializable(with = OptionalFieldSerializer::class)
    val telephone: OptionalField<String?> = OptionalField.Undefined,

    @SerialName("tax_id")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @Serializable(with = OptionalFieldSerializer::class)
    val taxId: OptionalField<String?> = OptionalField.Undefined,

    @SerialName("company_name")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @Serializable(with = OptionalFieldSerializer::class)
    val companyName: OptionalField<String> = OptionalField.Undefined,

    @SerialName("company_type")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @Serializable(with = OptionalFieldSerializer::class)
    val companyType: OptionalField<SpanishCompanyType> = OptionalField.Undefined,

    @SerialName("display_name")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @Serializable(with = OptionalFieldSerializer::class)
    val displayName: OptionalField<String?> = OptionalField.Undefined,

    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @Serializable(with = OptionalFieldSerializer::class)
    val description: OptionalField<String?> = OptionalField.Undefined,

    @SerialName("delivery_area_description")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @Serializable(with = OptionalFieldSerializer::class)
    val deliveryAreaDescription: OptionalField<String?> = OptionalField.Undefined,

    @SerialName("profile_image_file_id")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @Serializable(with = OptionalFieldSerializer::class)
    val logoFileId: OptionalField<String?> = OptionalField.Undefined,

    @SerialName("minimum_order_amount")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @Serializable(with = OptionalFieldSerializer::class)
    val minimumOrderAmount: OptionalField<String?> = OptionalField.Undefined,

    @SerialName("delivery_available")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @Serializable(with = OptionalFieldSerializer::class)
    val deliveryAvailable: OptionalField<Boolean?> = OptionalField.Undefined,

    @SerialName("pickup_available")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @Serializable(with = OptionalFieldSerializer::class)
    val pickupAvailable: OptionalField<Boolean?> = OptionalField.Undefined,
)
