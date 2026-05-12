package org.dsqrwym.standard.data.browse

/**
 * 零售浏览数据仓库
 * 负责处理所有浏览相关的数据获取，包括产品、分类、批发商等数据
 * 提供统一的数据访问接口，隐藏具体的API调用细节
 */

import org.dsqrwym.shared.data.OrderDir
import org.dsqrwym.shared.data.category.*
import org.dsqrwym.shared.data.category.dto.SharedFindCategoryDto
import org.dsqrwym.shared.data.products.SharedProductApi
import org.dsqrwym.shared.data.products.SharedProductSortField
import org.dsqrwym.shared.data.products.SharedProductStatus
import org.dsqrwym.shared.data.products.dto.SharedFindProductDto
import org.dsqrwym.shared.data.user.SpanishCompanyType
import org.dsqrwym.shared.data.user.SharedUserApi
import org.dsqrwym.shared.data.user.dto.FindWholesalerQueryDto
import org.dsqrwym.shared.data.user.dto.WholesalerSortField
import org.dsqrwym.shared.network.model.ApiResponseList
import org.dsqrwym.shared.network.model.SharedResponseResult
import org.dsqrwym.shared.network.safeApiCall
import org.dsqrwym.standard.data.browse.dto.*
import org.dsqrwym.standard.domain.browse.*

/**
 * 零售浏览数据仓库
 * 封装所有浏览相关的数据访问逻辑
 * 
 * @param productApi 产品API接口
 * @param categoryApi 分类API接口
 * @param userApi 用户API接口
 */
class RetailBrowseRepository(
    private val productApi: SharedProductApi,
    private val categoryApi: SharedCategoryApi,
    private val userApi: SharedUserApi,
) {
    /**
     * 获取产品列表
     * 支持多种过滤条件和排序方式
     * 
     * 业务规则：
     * - 没有批发商：查询所有 active 状态的产品
     * - 有批发商：查询该批发商的 active 状态产品
     * - 有分类ID：按分类过滤产品
     * 
     * @param search 搜索关键词
     * @param langCode 语言代码
     * @param categoryId 分类ID，用于过滤产品
     * @param wholesalerId 批发商ID，用于过滤产品
     * @param sortBy 排序字段，默认按名称排序
     * @param sortOrder 排序方向，默认升序
     * @param page 页码，默认第1页
     * @param limit 每页数量，默认30条
     * @return 产品列表的响应结果
     */
    suspend fun getProducts(
        search: String? = null,
        langCode: String? = null,
        categoryId: String? = null,
        wholesalerId: String? = null,
        sortBy: SharedProductSortField? = SharedProductSortField.NAME,
        sortOrder: OrderDir = OrderDir.ASC,
        page: Int = 1,
        limit: Int = 30,
    ): SharedResponseResult<ApiResponseList<RetailProduct>> {
        // 我在仓库这一层把空搜索吃掉，后面 paging 就不用一直猜空字符串算不算过滤
        val query = SharedFindProductDto(
            search = search?.trim()?.takeIf { it.isNotEmpty() },
            langCode = langCode,
            categoryId = categoryId,
            wholesalerId = wholesalerId,
            sortBy = sortBy,
            sortOrder = sortOrder,
            status = SharedProductStatus.ACTIVE,
            page = page,
            limit = limit,
        )
        return when (val result = safeApiCall { productApi.getProducts<RetailProductResponse>(query) }) {
            is SharedResponseResult.Success -> {
                // 我这里马上转成 standard 自己的 domain，UI 那边就别碰后端字段了
                SharedResponseResult.Success(
                    result.data?.let {
                        ApiResponseList(
                            items = it.items.map { product -> product.toDomain() },
                            pagination = it.pagination,
                        )
                    }
                )
            }
            is SharedResponseResult.Error -> result
        }
    }

    /**
     * 获取产品详情
     * 
     * @param id 产品ID
     * @param langCode 语言代码
     * @return 产品详情的响应结果
     */
    suspend fun getProductDetail(
        id: String,
        langCode: String? = null,
    ): SharedResponseResult<RetailProductDetail> {
        return when (val result = safeApiCall { productApi.getProduct<RetailProductDetailResponse>(id, langCode) }) {
            is SharedResponseResult.Success -> SharedResponseResult.Success(result.data?.toDomain())
            is SharedResponseResult.Error -> result
        }
    }

    /**
     * 获取分类列表
     * 支持多种过滤条件和层级查询
     * 
     * @param langCode 语言代码
     * @param search 搜索关键词
     * @param parentId 父分类ID，null表示查询顶级分类
     * @param level 分类层级，1-3级
     * @param type 分类类型（公共/私有）
     * @param userId 用户ID，用于查询私有分类
     * @param includePublic 是否包含公共分类
     * @param page 页码，默认第1页
     * @param limit 每页数量，默认100条
     * @return 分类列表的响应结果
     */
    suspend fun getCategories(
        langCode: String? = null,
        search: String? = null,
        parentId: String? = null,
        level: Int? = null,
        type: SharedCategoryType? = null,
        userId: String? = null,
        includePublic: Boolean? = null,
        page: Int = 1,
        limit: Int = 100,
    ): SharedResponseResult<ApiResponseList<RetailCategory>> {
        // 我用一个通用分类查询兜住产品页、分类页两种入口，差别都塞在 query 里
        val query = SharedFindCategoryDto(
            search = search?.trim()?.takeIf { it.isNotEmpty() },
            langCode = langCode,
            userId = userId,
            includePublic = includePublic,
            parentId = parentId,
            level = level,
            // 分类页使用 DESCENDANT，保证父类会因子孙类有产品而保留
            productFilterMode = SharedCategoryProductFilterMode.DESCENDANT,
            // DESCENDANT，保证父类会因为子孙类匹配而保留
            searchMatchMode = SharedCategoryProductFilterMode.DESCENDANT,
            type = type,
            fields = listOf(
                SharedCategorySelectField.LEVEL,        // 分类层级
                SharedCategorySelectField.USER_ID,      // 用户ID
                SharedCategorySelectField.TRANSLATIONS, // 翻译数据
            ),
            page = page,
            limit = limit,
        )

        return when (val result = safeApiCall { categoryApi.getCategories<RetailCategoryResponse>(query) }) {
            is SharedResponseResult.Success -> {
                // 成功响应：转换为分类领域模型
                SharedResponseResult.Success(
                    result.data?.let {
                        ApiResponseList(
                            items = it.items.map { category -> category.toDomain() },
                            pagination = it.pagination,
                        )
                    }
                )
            }
            is SharedResponseResult.Error -> result
        }
    }

    /**
     * 获取产品浏览页的分类过滤器
     * 用于产品页顶部的分类快速筛选
     * 
     * 业务规则：
     * - 没有批发商：显示所有公共分类
     * - 有批发商：显示该批发商的私有分类 + 公共分类
     * - 只显示直接挂了产品的分类
     * 
     * @param langCode 语言代码
     * @param wholesalerId 批发商ID，null表示全局浏览
     * @param page 页码，默认第1页
     * @param limit 每页数量，默认20条
     * @return 分类列表的响应结果
     */
    suspend fun getProductBrowseCategories(
        langCode: String? = null,
        wholesalerId: String? = null,
        page: Int = 1,
        limit: Int = 20,
    ): SharedResponseResult<ApiResponseList<RetailCategory>> {
        // 我这里拿的是产品页顶部那排分类，只要直接挂了商品的分类就够了
        val query = SharedFindCategoryDto(
            langCode = langCode,
            // 如果没有批发商，显示公共分类
            type = if (wholesalerId == null) SharedCategoryType.PUBLIC else null,
            userId = wholesalerId,
            // 保证有批发商ID时，同时包含公共分类和该批发商的私有分类
            includePublic = wholesalerId != null,
            fields = listOf(SharedCategorySelectField.TRANSLATIONS), // 只需要翻译字段
            productFilterMode = SharedCategoryProductFilterMode.SELF, // 只显示直接挂了产品的分类
            sortBy = SharedCategorySortField.LEVEL,
            sortOrder = OrderDir.ASC,
            page = page,
            limit = limit,
        )
        return when (val result = safeApiCall { categoryApi.getCategories<RetailCategoryResponse>(query) }) {
            is SharedResponseResult.Success -> {
                SharedResponseResult.Success(
                    result.data?.let {
                        ApiResponseList(
                            items = it.items.map { category -> category.toDomain() },
                            pagination = it.pagination,
                        )
                    }
                )
            }
            is SharedResponseResult.Error -> result
        }
    }

    /**
     * 获取特定范围的分类
     * 用于分类浏览页面的分类查询
     * 
     * 业务规则：
     * - 不传type：后端根据其他参数智能返回分类
     *   - 没有批发商：返回所有公共分类 + 私有分类
     *   - 有批发商：返回公共分类 + 该批发商的私有分类
     * - 使用DESCENDANT模式：父类会因子孙类有产品而保留
     * 
     * @param langCode 语言代码
     * @param search 搜索关键词
     * @param wholesalerId 批发商ID
     * @param parentId 父分类ID
     * @param level 分类层级
     * @param page 页码
     * @param limit 每页数量
     * @return 分类列表的响应结果
     */
    suspend fun getScopedCategories(
        langCode: String?,
        search: String? = null,
        wholesalerId: String?,
        parentId: String?,
        level: Int?,
        page: Int = 1,
        limit: Int = 30,
    ): SharedResponseResult<ApiResponseList<RetailCategory>> {
        // 我让分类页一直走这个入口，店铺模式和全局模式的差别就在 wholesalerId/includePublic
        return getCategories(
            langCode = langCode,
            search = search,
            parentId = parentId,
            level = level,
            // 不传type：让后端根据其他参数智能返回
            // - 没有批发商：返回所有 public + private
            // - 有批发商：配合 userId + includePublic 返回 public + 该批发商 private
            type = null,
            // 进入批发商页面时，userId 表示当前批发商
            userId = wholesalerId,
            // 进入批发商页面时，要求后端返回 public + 该批发商 private
            includePublic = wholesalerId != null,
            page = page,
            limit = limit,
        )
    }

    /**
     * 获取批发商列表
     * 用于批发商选择和浏览
     * 
     * @param search 搜索关键词
     * @param page 页码，默认第1页
     * @param limit 每页数量，默认50条
     * @return 批发商列表的响应结果
     */
    suspend fun getWholesalers(
        search: String? = null,
        deliveryAvailable: Boolean? = null,
        pickupAvailable: Boolean? = null,
        companyType: SpanishCompanyType? = null,
        orderBy: WholesalerSortField = WholesalerSortField.DISPLAY_NAME,
        orderDir: OrderDir = OrderDir.ASC,
        page: Int = 1,
        limit: Int = 20,
    ): SharedResponseResult<ApiResponseList<RetailWholesaler>> {
        // 我把批发商列表也按 paging 的写法包起来，筛选排序都在这个 query 里收口
        val query = FindWholesalerQueryDto(
            search = search?.trim()?.takeIf { it.isNotEmpty() },
            deliveryAvailable = deliveryAvailable,
            pickupAvailable = pickupAvailable,
            companyType = companyType,
            orderBy = orderBy,
            orderDir = orderDir,
            page = page,
            limit = limit,
        )
        return when (val result = safeApiCall { userApi.getWholesalers<WholesalerListItemDto>(query) }) {
            is SharedResponseResult.Success -> {
                // 成功响应：转换为批发商领域模型
                SharedResponseResult.Success(
                    result.data?.let {
                        ApiResponseList(
                            items = it.items.map { distributor -> distributor.toDomain() },
                            pagination = it.pagination,
                        )
                    }
                )
            }
            is SharedResponseResult.Error -> result
        }
    }
}
