package org.dsqrwym.shared.data.products.dto

import kotlinx.serialization.Serializable
import org.dsqrwym.shared.data.OrderDir
import org.dsqrwym.shared.paging.data.PaginationQuery
import org.dsqrwym.shared.data.products.SharedProductListSelectField
import org.dsqrwym.shared.data.products.SharedProductSortField
import org.dsqrwym.shared.data.products.SharedProductStatus

@Serializable
data class SharedFindProductDto(
    val search: String? = null,                  // name/title/product_code 模糊搜索关键字
    val langCode: String? = null,                // 指定返回语言字段 (e.g., "en", "es")
    val categoryId: String? = null, // 分类过滤（主分类或关联分类）
    val wholesalerId: String? = null,            // 批发商 ID 过滤 (UUID)
    val sortBy: SharedProductSortField? = SharedProductSortField.NAME, // 排序字段: name/product_code/available_stock/price_iva/price/best_selling
    val sortOrder: OrderDir = OrderDir.ASC,              // asc / desc
    val status: SharedProductStatus? = null,     // 产品状态过滤
    val fields: List<SharedProductListSelectField>? = null, // 选择返回字段
    override val page: Int = 1,
    override val limit: Int = 50,
) : PaginationQuery
