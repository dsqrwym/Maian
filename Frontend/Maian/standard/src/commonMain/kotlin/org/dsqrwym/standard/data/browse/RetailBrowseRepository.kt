package org.dsqrwym.standard.data.browse

import org.dsqrwym.shared.data.OrderDir
import org.dsqrwym.shared.data.category.*
import org.dsqrwym.shared.data.category.dto.SharedFindCategoryDto
import org.dsqrwym.shared.data.products.SharedProductApi
import org.dsqrwym.shared.data.products.SharedProductSortField
import org.dsqrwym.shared.data.products.SharedProductStatus
import org.dsqrwym.shared.data.products.dto.SharedFindProductDto
import org.dsqrwym.shared.data.user.SharedUserApi
import org.dsqrwym.shared.data.user.UserRole
import org.dsqrwym.shared.data.user.dto.FindUserQueryDto
import org.dsqrwym.shared.network.model.ApiResponseList
import org.dsqrwym.shared.network.model.SharedResponseResult
import org.dsqrwym.shared.network.safeApiCall
import org.dsqrwym.standard.data.browse.dto.RetailCategoryResponse
import org.dsqrwym.standard.data.browse.dto.RetailDistributorResponse
import org.dsqrwym.standard.data.browse.dto.RetailProductResponse
import org.dsqrwym.standard.data.browse.dto.toDomain
import org.dsqrwym.standard.domain.browse.RetailCategory
import org.dsqrwym.standard.domain.browse.RetailDistributor
import org.dsqrwym.standard.domain.browse.RetailProduct

class RetailBrowseRepository(
    private val productApi: SharedProductApi,
    private val categoryApi: SharedCategoryApi,
    private val userApi: SharedUserApi,
) {
    /**
     * 没有批发商：查所有 active 产品
     * 有批发商：查该批发商 active 产品
     * 有 categoryId：按 category 过滤产品
     */
    suspend fun getProducts(
        search: String? = null,
        langCode: String? = null,
        categoryId: String? = null,
        distributorId: String? = null,
        sortBy: SharedProductSortField? = SharedProductSortField.NAME,
        sortOrder: OrderDir = OrderDir.ASC,
        page: Int = 1,
        limit: Int = 30,
    ): SharedResponseResult<ApiResponseList<RetailProduct>> {
        val query = SharedFindProductDto(
            search = search?.trim()?.takeIf { it.isNotEmpty() },
            langCode = langCode,
            categoryId = categoryId,
            wholesalerId = distributorId,
            sortBy = sortBy,
            sortOrder = sortOrder,
            status = SharedProductStatus.ACTIVE,
            page = page,
            limit = limit,
        )
        return when (val result = safeApiCall { productApi.getProducts<RetailProductResponse>(query) }) {
            is SharedResponseResult.Success -> SharedResponseResult.Success(
                result.data?.let {
                    ApiResponseList(
                        items = it.items.map { product -> product.toDomain() },
                        pagination = it.pagination,
                    )
                }
            )

            is SharedResponseResult.Error -> result
        }
    }

    suspend fun getCategories(
        langCode: String? = null,
        search: String? = null,
        parentId: String? = null,
        level: Int? = null,
        type: SharedCategoryType? = null,
        userId: String? = null,
        includePublic: Boolean? = null,
        productFilterMode: SharedCategoryProductFilterMode? = null,
        page: Int = 1,
        limit: Int = 100,
    ): SharedResponseResult<ApiResponseList<RetailCategory>> {
        val query = SharedFindCategoryDto(
            search = search?.trim()?.takeIf { it.isNotEmpty() },
            langCode = langCode,
            userId = userId,
            includePublic = includePublic,
            parentId = parentId,
            level = level,
            productFilterMode = productFilterMode,
            type = type,
            fields = listOf(
                SharedCategorySelectField.LEVEL,
                SharedCategorySelectField.USER_ID,
                SharedCategorySelectField.TRANSLATIONS,
            ),
            page = page,
            limit = limit,
        )

        return when (val result = safeApiCall { categoryApi.getCategories<RetailCategoryResponse>(query) }) {
            is SharedResponseResult.Success -> SharedResponseResult.Success(
                result.data?.let {
                    ApiResponseList(
                        items = it.items.map { category -> category.toDomain() },
                        pagination = it.pagination,
                    )
                }
            )

            is SharedResponseResult.Error -> result
        }
    }

    /**
     * 公共产品页顶部分类过滤：
     * 只显示自己直接挂了产品的公共分类，用于产品页快速筛选
     */
    suspend fun getProductBrowseCategories(
        langCode: String? = null,
        page: Int = 1,
        limit: Int = 20,
    ): SharedResponseResult<ApiResponseList<RetailCategory>> {
        val query = SharedFindCategoryDto(
            langCode = langCode,
            type = SharedCategoryType.PUBLIC,
            fields = listOf(SharedCategorySelectField.TRANSLATIONS),
            productFilterMode = SharedCategoryProductFilterMode.SELF,
            sortBy = SharedCategorySortField.LEVEL,
            sortOrder = OrderDir.ASC,
            page = page,
            limit = limit,
        )
        return when (val result = safeApiCall { categoryApi.getCategories<RetailCategoryResponse>(query) }) {
            is SharedResponseResult.Success -> SharedResponseResult.Success(
                result.data?.let {
                    ApiResponseList(
                        items = it.items.map { category -> category.toDomain() },
                        pagination = it.pagination,
                    )
                }
            )

            is SharedResponseResult.Error -> result
        }
    }

    suspend fun getScopedCategories(
        langCode: String?,
        search: String? = null,
        wholesalerId: String?,
        parentId: String?,
        level: Int?,
        productFilterMode: SharedCategoryProductFilterMode? = SharedCategoryProductFilterMode.DESCENDANT,
        page: Int = 1,
        limit: Int = 30,
    ): SharedResponseResult<ApiResponseList<RetailCategory>> {
        return getCategories(
            langCode = langCode,
            search = search,
            parentId = parentId,
            level = level,
            // 不传 type：
            // - 没进入批发商：后端返回所有 public + private
            // - 进入批发商：配合 userId + includePublic 返回 public + 该批发商 private
            type = null,
            // 进入批发商页面时，userId 表示当前批发商
            userId = wholesalerId,
            // 进入批发商页面时，要求后端返回 public + 该批发商 private
            includePublic = wholesalerId != null,
            // 分类页使用 DESCENDANT，保证父类会因子孙类有产品而保留
            productFilterMode = productFilterMode,
            page = page,
            limit = limit,
        )
    }

    suspend fun getDistributors(
        search: String? = null,
        page: Int = 1,
        limit: Int = 50,
    ): SharedResponseResult<ApiResponseList<RetailDistributor>> {
        val query = FindUserQueryDto(
            search = search?.trim()?.takeIf { it.isNotEmpty() },
            role = UserRole.WHOLESALER,
            userId = true,
            username = true,
            email = true,
            firstName = true,
            lastName = true,
            telephone = true,
            cif = true,
            profile = true,
            page = page,
            limit = limit,
        )
        return when (val result = safeApiCall { userApi.getUsers<RetailDistributorResponse>(query) }) {
            is SharedResponseResult.Success -> SharedResponseResult.Success(
                result.data?.let {
                    ApiResponseList(
                        items = it.items.map { distributor -> distributor.toDomain() },
                        pagination = it.pagination,
                    )
                }
            )

            is SharedResponseResult.Error -> result
        }
    }
}
