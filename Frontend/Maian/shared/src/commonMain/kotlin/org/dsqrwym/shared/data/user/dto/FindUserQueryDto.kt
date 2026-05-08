package org.dsqrwym.shared.data.user.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.dsqrwym.shared.data.OrderDir
import org.dsqrwym.shared.paging.data.PaginationQuery
import org.dsqrwym.shared.data.user.UserRole
import org.dsqrwym.shared.data.user.UserStatus

enum class FindUserQueryOrderBy(val value: String) {
    ID("id"),
    USER_ID("user_id"),
    USERNAME("username"),

    EMAIL("email"),

    FIRST_NAME("first_name"),

    LAST_NAME("last_name"),

    TELEPHONE("telephone"),

    CIF("cif")
}

@Serializable
data class FindUserQueryDto(
    val search: String? = null,
    val role: UserRole? = null,
    val status: UserStatus? = null,
    val selectUserStatus: Boolean? = null,
    val selectUserRole: Boolean? = null,
    @SerialName("user_id")
    val userId: Boolean? = null,
    val username: Boolean? = null,
    val email: Boolean? = null,
    @SerialName("first_name")
    val firstName: Boolean? = null,
    @SerialName("last_name")
    val lastName: Boolean? = null,
    val telephone: Boolean? = null,
    val cif: Boolean? = null,
    val profile: Boolean? = null,
    val orderBy: FindUserQueryOrderBy? = null,
    val orderDir: OrderDir? = null,
    override val page: Int = 1,
    override val limit: Int = 50,
) : PaginationQuery