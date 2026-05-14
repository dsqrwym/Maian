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
@SerialName("cart")
object CartScreen : NavKey

@Serializable
enum class StandardTopLevelRoute {
    PRODUCTS,
    CATEGORIES,
    WHOLESALERS,
    CART,
}

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
enum class WholesalerProfileScopeAction {
    NONE,
    CLEAR_ON_BACK,
}

@Serializable
@SerialName("wholesaler_profile")
data class WholesalerProfileRoute(
    val id: String,
    val returnTopLevelRoute: StandardTopLevelRoute? = null,
    val onBackScopeAction: WholesalerProfileScopeAction = WholesalerProfileScopeAction.NONE,
) : NavKey

@Serializable
@SerialName("product_detail")
data class ProductDetailScreen(val productId: String) : NavKey

fun NavKey.toStandardTopLevelRoute(): StandardTopLevelRoute? =
    when (this) {
        ProductsScreen -> StandardTopLevelRoute.PRODUCTS
        CategoriesScreen -> StandardTopLevelRoute.CATEGORIES
        WholesalersScreen -> StandardTopLevelRoute.WHOLESALERS
        CartScreen -> StandardTopLevelRoute.CART
        else -> null
    }

fun StandardTopLevelRoute.toNavKey(): NavKey =
    when (this) {
        StandardTopLevelRoute.PRODUCTS -> ProductsScreen
        StandardTopLevelRoute.CATEGORIES -> CategoriesScreen
        StandardTopLevelRoute.WHOLESALERS -> WholesalersScreen
        StandardTopLevelRoute.CART -> CartScreen
    }
