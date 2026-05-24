package org.dsqrwym.enterprise.data.dashboard.dto

import kotlinx.serialization.Serializable
import org.dsqrwym.shared.data.products.dto.SharedProductTranslation

@Serializable
data class DashboardResponse(
    val summary: DashboardSummary,
    val orderStatus: DashboardOrderStatus,
    val revenueTrend: List<DashboardRevenueTrendItem> = emptyList(),
    val topSellingProducts: List<DashboardTopSellingProduct> = emptyList(),
)

@Serializable
data class DashboardSummary(
    val totalOrders: Int,
    val pendingOrders: Int,
    val acceptedOrders: Int,
    val totalRevenue: String,
    val averageOrderValue: String,
)

@Serializable
data class DashboardOrderStatus(
    val pending: Int,
    val accepted: Int,
    val rejected: Int,
    val cancelled: Int,
    val total: Int,
)

@Serializable
data class DashboardRevenueTrendItem(
    val date: String,
    val orderCount: Int,
    val acceptedCount: Int,
    val revenue: String,
)

@Serializable
data class DashboardTopSellingProduct(
    val productId: String? = null,
    val productName: String,
    val productTranslation: List<SharedProductTranslation> = emptyList(),
    val soldQuantity: Int,
    val revenue: String,
    val orderCount: Int,
)
