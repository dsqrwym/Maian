package org.dsqrwym.shared.data.category.dto

import kotlinx.serialization.Serializable
import org.dsqrwym.shared.data.category.SharedCategorySelectField
import org.dsqrwym.shared.data.category.SharedCategoryType
import org.dsqrwym.shared.data.pagination.PaginationQuery

// isPrivate = false -> userID = not null
// userID = null or value
@Serializable
data class SharedFindCategoryDto(
    val search: String? = null,          // name/lang 模糊搜索关键字
    val langCode: String? = null,        // 语言代码 (e.g. "en", "es")
    val userId: String? = null,          // 按 user_id 过滤
    val parentId: String? = null,        // 按 parent_id 过滤
    val maxLevel: Int? = null,           // 最大嵌套等级
    val type: SharedCategoryType? = null,      // PRIVATE / PUBLIC / ALL = NULL
    val fields: List<SharedCategorySelectField>? = null,
    override val page: Int = 1,
    override val limit: Int = 50,
) : PaginationQuery

