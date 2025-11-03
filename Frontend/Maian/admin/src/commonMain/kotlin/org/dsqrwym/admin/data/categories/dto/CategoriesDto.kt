package org.dsqrwym.admin.data.categories.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CategoryDto(
    val id: Long,
    @SerialName("user_id") val userId: String? = null,
    val name: String,
    val iva: Double? = null,
    @SerialName("parent_id") val parentId: Long? = null,
    val lang: Map<String, String>? = null,
)