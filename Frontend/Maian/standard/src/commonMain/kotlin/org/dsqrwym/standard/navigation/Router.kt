package org.dsqrwym.standard.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("register")
object RegisterScreen : NavKey

@Serializable
@SerialName("products")
object ProductsScreen : NavKey

@Serializable
@SerialName("categories")
object CategoriesScreen : NavKey

@Serializable
@SerialName("distributors")
object DistributorsScreen : NavKey

@Serializable
@SerialName("category_browse")
data class CategoryBrowseRoute(
    val id: String,
    val name: String,
    val level: Int,
    val parentId: String? = null,
    val distributorId: String? = null,
    val pathNames: List<String> = emptyList(),
    val railFallbackCategories: List<CategoryBrowseRouteCategory> = emptyList(),
) : NavKey

@Serializable
data class CategoryBrowseRouteCategory(
    val id: String,
    val name: String,
    val level: Int,
    val parentId: String? = null,
)

@Serializable
@SerialName("distributor_home")
data class DistributorHomeScreen(
    val id: String,
    val userId: String? = null,
    val username: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val telephone: String? = null,
    val cif: String? = null,
    val companyName: String? = null,
) : NavKey

@Serializable
@SerialName("product_detail_placeholder")
data class ProductDetailPlaceholderScreen(val productId: String) : NavKey
