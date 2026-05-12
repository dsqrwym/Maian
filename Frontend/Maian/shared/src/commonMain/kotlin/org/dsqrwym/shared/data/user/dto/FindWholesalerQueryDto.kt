package org.dsqrwym.shared.data.user.dto

import kotlinx.serialization.Serializable
import org.dsqrwym.shared.data.OrderDir
import org.dsqrwym.shared.data.user.SpanishCompanyType
import org.dsqrwym.shared.paging.data.PaginationQuery

enum class WholesalerSortField(val value: String) {
    DISPLAY_NAME("display_name"),
    COMPANY_NAME("company_name"),
    CITY("city"),
    PROVINCE("province"),
    MINIMUM_ORDER_AMOUNT("minimum_order_amount"),
}

@Serializable
data class FindWholesalerQueryDto(
    val search: String? = null,
    val deliveryAvailable: Boolean? = null,
    val pickupAvailable: Boolean? = null,
    val companyType: SpanishCompanyType? = null,
    val orderBy: WholesalerSortField = WholesalerSortField.DISPLAY_NAME,
    val orderDir: OrderDir = OrderDir.ASC,
    override val page: Int = 1,
    override val limit: Int = 50,
) : PaginationQuery
