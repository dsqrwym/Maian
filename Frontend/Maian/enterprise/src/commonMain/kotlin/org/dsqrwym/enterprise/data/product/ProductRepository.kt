package org.dsqrwym.enterprise.data.product

import org.dsqrwym.enterprise.data.product.dto.*
import org.dsqrwym.enterprise.data.product.mapper.toDomain
import org.dsqrwym.enterprise.data.enterpriseOwnerUserId
import org.dsqrwym.enterprise.domain.product.Product
import org.dsqrwym.shared.data.OrderDir
import org.dsqrwym.shared.data.SharedObservableRepository
import org.dsqrwym.shared.data.products.SharedProductApi
import org.dsqrwym.shared.data.products.SharedProductListSelectField
import org.dsqrwym.shared.data.products.SharedProductSortField
import org.dsqrwym.shared.data.products.SharedProductStatus
import org.dsqrwym.shared.data.products.dto.SharedFindProductDto
import org.dsqrwym.shared.data.products.dto.SharedProductTranslation
import org.dsqrwym.shared.network.model.ApiResponseList
import org.dsqrwym.shared.network.model.SharedResponseResult
import org.dsqrwym.shared.network.safeApiCall
import org.dsqrwym.shared.network.withAuthOrError
import org.dsqrwym.shared.serialization.OptionalField
import org.dsqrwym.shared.serialization.map

class ProductRepository(private val sharedProductApi: SharedProductApi, private val productApi: ProductApi) :
    SharedObservableRepository() {
    suspend fun getProducts(
        search: String? = null,
        langCode: String? = null,
        categoryId: String? = null,
        sortBy: SharedProductSortField? = SharedProductSortField.NAME,
        sortOrder: OrderDir = OrderDir.ASC,
        status: SharedProductStatus? = null,
        page: Int = 1,
        limit: Int = 50
    ): SharedResponseResult<ApiResponseList<Product>> {
        return withAuthOrError { user ->
            val fields: List<SharedProductListSelectField> = listOf(
                SharedProductListSelectField.IVA,
                SharedProductListSelectField.STATUS,
                SharedProductListSelectField.CATEGORY,
            )
            val query = SharedFindProductDto(
                wholesalerId = user.enterpriseOwnerUserId(),
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
            when (val result = safeApiCall { sharedProductApi.getProducts<ProductResponse>(query) }) {
                is SharedResponseResult.Success -> SharedResponseResult.Success(
                    result.data?.let {
                        ApiResponseList(
                            items = it.items.map { product -> product.toDomain() },
                            pagination = it.pagination
                        )
                    }
                )

                is SharedResponseResult.Error -> result
            }
        }
    }

    suspend fun createProduct(
        name: String,
        title: String? = null,
        description: String? = null,
        iva: String,
        productCode: String,
        primaryCategoryId: String,
        subCategoryIds: List<String>? = null,
        variants: List<ProductVariantDto>,
        translations: List<SharedProductTranslation>? = null,
        files: List<ProductFileDto>? = null,
    ): SharedResponseResult<Unit> {
        return withAuthOrError { user ->
            safeApiCall {
                productApi.createProduct(
                    ProductCreateDto(
                        userId = user.enterpriseOwnerUserId(),
                        name = name.trim(),
                        title = title?.trim(),
                        description = description?.trim(),
                        iva = iva,
                        productCode = productCode.trim(),
                        primaryCategoryId = primaryCategoryId,
                        subCategoryIds = subCategoryIds?.map { it.trim() }?.distinct()?.ifEmpty { null },
                        variants = variants.map {
                            it.copy(
                                productCode = it.productCode.map { code -> code.trim() },
                                id = OptionalField.Undefined
                            )
                        },
                        translations = translations?.map {
                            it.copy(
                                name = it.name.trim(),
                                title = it.title?.trim(),
                                description = it.description?.trim()
                            )
                        }?.ifEmpty { null },
                        files = files
                    )
                )
            }.notifyUpdated()
        }
    }

    suspend fun updateProduct(id: String, dto: ProductUpdateDto): SharedResponseResult<Unit> {
        return safeApiCall {
            productApi.updateProduct(
                id, dto.copy(
                    name = dto.name.map { it.trim() },
                    title = dto.title?.map { it.trim() },
                    description = dto.description.map { it.trim() },
                    iva = dto.iva,
                    productCode = dto.productCode.map { it.trim() },
                    primaryCategoryId = dto.primaryCategoryId,
                    subCategoryIds = dto.subCategoryIds.map { ids -> ids.map { it.trim() }.distinct() },
                    variantsToDelete = dto.variantsToDelete,
                    createVariants = dto.createVariants.map { variants ->
                        variants.map {
                            it.copy(
                                productCode = it.productCode.map { code -> code.trim() },
                                id = OptionalField.Undefined
                            )
                        }
                    },
                    updateVariants = dto.updateVariants.map { variants ->
                        variants.map {
                            it.copy(productCode = it.productCode.map { code -> code.trim() })
                        }
                    },
                    translationsToDelete = dto.translationsToDelete,
                    translations = dto.translations.map { translations ->
                        translations.map {
                            it.copy(
                                name = it.name.trim(),
                                title = it.title?.trim(),
                                description = it.description?.trim(),
                            )
                        }
                    }
                )
            )
        }.notifyUpdated()
    }

    suspend fun getProductForUpdate(id: String): SharedResponseResult<ProductResponseForUpdate> {
        return safeApiCall { productApi.getProductForUpdate(id) }
    }

    suspend fun deleteProduct(id: String): SharedResponseResult<Unit> {
        return safeApiCall { productApi.deleteProduct(id) }.notifyUpdated()
    }
}
