package org.dsqrwym.shared.domain.profile

import org.dsqrwym.shared.data.location.dto.CityDto
import org.dsqrwym.shared.data.location.dto.CountryDto
import org.dsqrwym.shared.data.location.dto.ProvinceDto
import org.dsqrwym.shared.data.user.SpanishCompanyType

/**
 * 批发商卡片通用数据模型
 */
data class WholesalerCardData(
    // ── 基础字段（两种 DTO 共有，卡片必需） ──
    // id 是用来查询的id
    val id: String?,
    // userId 是用来显示的id
    val userId: String,
    val displayName: String?,
    val companyName: String,
    val companyType: SpanishCompanyType?,
    val description: String?,
    val logoFileId: String?,
    val deliveryAvailable: Boolean?,
    val pickupAvailable: Boolean?,
    val minimumOrderAmount: String?,
    val deliveryAreaDescription: String?,

    // 扩展字段
    val email: String? = null,
    val telephone: String? = null,
    val taxId: String? = null,
    val city: CityDto? = null,
    val province: ProvinceDto? = null,
    val country: CountryDto? = null,
)
