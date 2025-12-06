package org.dsqrwym.enterprise.data.product

import org.dsqrwym.enterprise.data.product.dto.ProductResponse
import org.dsqrwym.shared.data.OrderDir
import org.dsqrwym.shared.data.products.SharedProductApi
import org.dsqrwym.shared.data.products.SharedProductListSelectField
import org.dsqrwym.shared.data.products.SharedProductSortField
import org.dsqrwym.shared.data.products.SharedProductStatus
import org.dsqrwym.shared.data.products.dto.SharedFindProductDto
import org.dsqrwym.shared.network.ApiResponseList
import org.dsqrwym.shared.network.SharedResponseResult
import org.dsqrwym.shared.network.safeApiCall
import org.dsqrwym.shared.network.withAuthOrError

class ProductRepository(private val sharedProductApi: SharedProductApi) {
    suspend fun getProducts(
        search: String? = null,
        langCode: String? = null,
        categoryId: String? = null,
        sortBy: SharedProductSortField? = SharedProductSortField.NAME,
        sortOrder: OrderDir? = OrderDir.ASC,
        status: SharedProductStatus? = null,
        page: Int = 1,
        limit: Int = 50
    ): SharedResponseResult<ApiResponseList<ProductResponse>> {
        return withAuthOrError { user ->
            val fields: List<SharedProductListSelectField> = listOf(
                SharedProductListSelectField.IVA,
                SharedProductListSelectField.STATUS,
                SharedProductListSelectField.CATEGORY,
            )
            val query = SharedFindProductDto(
                wholesalerId = user.userId,
                search = search?.trim(),
                langCode = langCode,
                categoryId = categoryId,
                sortBy = sortBy,
                sortOrder = sortOrder,
                status = status,
                fields = fields,
                page = page,
                limit = limit
            )
            safeApiCall { sharedProductApi.getProducts(query) }
        }
    }
}