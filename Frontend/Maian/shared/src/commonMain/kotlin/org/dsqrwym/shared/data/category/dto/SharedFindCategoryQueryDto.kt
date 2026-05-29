package org.dsqrwym.shared.data.category.dto

import kotlinx.serialization.Serializable
import org.dsqrwym.shared.data.OrderDir
import org.dsqrwym.shared.data.category.SharedCategoryProductFilterMode
import org.dsqrwym.shared.data.category.SharedCategorySelectField
import org.dsqrwym.shared.data.category.SharedCategorySortField
import org.dsqrwym.shared.data.category.SharedCategoryType
import org.dsqrwym.shared.paging.data.PaginationQuery

// isPrivate = false -> userID = not null
// userID = null or value
@Serializable
data class SharedFindCategoryDto(
    val search: String? = null,          // name/lang 模糊搜索关键字
    val langCode: String? = null,        // 语言代码 (e.g. "en", "es")
    val userId: String? = null,          // 按 user_id 过滤
    val parentId: String? = null,        // 按 parent_id 过滤
    val excludedIds: List<String>? = null, // 排除的 ID 列表
    val level: Int? = null,
    val maxLevel: Int? = null,           // 最大嵌套等级
    val withChildrenCount: Boolean? = null, // 放回计数
    val onlyWithOwnedChildren: Boolean? = null, // enterprise 只返回拥有用户子类别的
    val includePublic: Boolean? = null, // standard 包含公共分类
    val searchMatchMode: SharedCategoryProductFilterMode? = null,
    val productFilterMode: SharedCategoryProductFilterMode? = null,
    val type: SharedCategoryType? = null,      // PRIVATE / PUBLIC / ALL = NULL
    val fields: List<SharedCategorySelectField>? = null,
    val sortBy: SharedCategorySortField? = null,
    val sortOrder: OrderDir? = null,
    override val page: Int = 1,
    override val limit: Int = 20,
) : PaginationQuery

