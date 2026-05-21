package org.dsqrwym.standard.data.profile.dto

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.dsqrwym.shared.data.location.dto.DirectionPatchRequest
import org.dsqrwym.shared.data.user.SpanishCompanyType
import org.dsqrwym.shared.serialization.OptionalField
import org.dsqrwym.shared.serialization.OptionalFieldSerializer

@Serializable
data class UpdateRetailerProfileDto(
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
    val companyName: OptionalField<String?> = OptionalField.Undefined,

    @SerialName("display_name")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @Serializable(with = OptionalFieldSerializer::class)
    val displayName: OptionalField<String?> = OptionalField.Undefined,

    @SerialName("company_type")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @Serializable(with = OptionalFieldSerializer::class)
    val companyType: OptionalField<SpanishCompanyType?> = OptionalField.Undefined,

    @SerialName("contact_name")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @Serializable(with = OptionalFieldSerializer::class)
    val contactName: OptionalField<String?> = OptionalField.Undefined,

    @SerialName("profile_image_file_id")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @Serializable(with = OptionalFieldSerializer::class)
    val logoFileId: OptionalField<String?> = OptionalField.Undefined,

    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @Serializable(with = OptionalFieldSerializer::class)
    val address: OptionalField<DirectionPatchRequest> = OptionalField.Undefined,
)
