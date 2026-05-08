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
@SerialName("wholesalers")
object WholesalersScreen : NavKey

@Serializable
@SerialName("category_browse")
data class CategoryBrowseRoute(
    val id: String,
    val name: String,
    val level: Int,
    val parentId: String? = null,
    val wholesalerId: String? = null,
    val pathNames: List<String> = emptyList(),
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
data class WholesalerHomeScreen(
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

@Serializable
@SerialName("product_detail")
data class ProductDetailScreen(val productId: String) : NavKey
