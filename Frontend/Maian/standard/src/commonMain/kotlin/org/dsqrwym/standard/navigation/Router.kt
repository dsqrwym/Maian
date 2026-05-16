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
@SerialName("order_history")
object OrderHistoryScreen : NavKey

@Serializable
@SerialName("order_detail")
data class OrderDetailScreen(val orderId: String) : NavKey

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
@SerialName("wholesaler_profile")
data class WholesalerProfileRoute(
    val id: String,
) : NavKey

@Serializable
@SerialName("retailer_profile_edit")
object RetailerProfileEdit : NavKey

@Serializable
@SerialName("product_detail")
data class ProductDetailScreen(val productId: String) : NavKey
