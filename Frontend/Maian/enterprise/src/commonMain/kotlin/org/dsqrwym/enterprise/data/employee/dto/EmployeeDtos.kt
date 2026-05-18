package org.dsqrwym.enterprise.data.employee.dto

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.dsqrwym.enterprise.data.employee.EmployeeRole
import org.dsqrwym.enterprise.data.employee.EmployeeStatus
import org.dsqrwym.shared.serialization.OptionalField
import org.dsqrwym.shared.serialization.OptionalFieldSerializer

@Serializable
data class EmployeeResponse(
    val id: String,
    @SerialName("user_id")
    val userId: String? = null,
    @SerialName("first_name")
    val firstName: String? = null,
    @SerialName("last_name")
    val lastName: String? = null,
    val email: String,
    val username: String? = null,
    val telephone: String? = null,
    @SerialName("tax_id")
    val taxId: String? = null,
    val role: EmployeeRole,
    val status: EmployeeStatus,
)

@Serializable
data class EmployeeForUpdateResponse(
    val email: String? = null,
    @SerialName("first_name")
    val firstName: String? = null,
    @SerialName("last_name")
    val lastName: String? = null,
    val username: String? = null,
    val telephone: String? = null,
    @SerialName("tax_id")
    val taxId: String? = null,
    val role: EmployeeRole? = null,
    val status: EmployeeStatus? = null,
)

@Serializable
data class CreateEmployeeDto(
    val email: String,

    @SerialName("first_name")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val firstName: String? = null,

    @SerialName("last_name")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val lastName: String? = null,

    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val username: String? = null,

    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val telephone: String? = null,

    @SerialName("tax_id")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val taxId: String? = null,
)

@Serializable
data class UpdateEmployeeDto(
    @SerialName("first_name")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @Serializable(with = OptionalFieldSerializer::class)
    val firstName: OptionalField<String> = OptionalField.Undefined,

    @SerialName("last_name")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @Serializable(with = OptionalFieldSerializer::class)
    val lastName: OptionalField<String> = OptionalField.Undefined,

    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @Serializable(with = OptionalFieldSerializer::class)
    val username: OptionalField<String> = OptionalField.Undefined,

    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @Serializable(with = OptionalFieldSerializer::class)
    val telephone: OptionalField<String> = OptionalField.Undefined,

    @SerialName("tax_id")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @Serializable(with = OptionalFieldSerializer::class)
    val taxId: OptionalField<String> = OptionalField.Undefined,
)
