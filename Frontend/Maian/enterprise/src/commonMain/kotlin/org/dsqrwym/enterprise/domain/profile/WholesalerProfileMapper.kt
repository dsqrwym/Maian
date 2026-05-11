package org.dsqrwym.enterprise.domain.profile

import org.dsqrwym.enterprise.data.profile.dto.WholesalerProfileResponseDto
import org.dsqrwym.shared.domain.profile.WholesalerCardData

fun WholesalerProfileResponseDto.toCardData() = WholesalerCardData(
    id = id,
    userId = userId ?: "",
    displayName = profile?.displayName,
    companyName = profile?.companyName ?: "",
    companyType = profile?.companyType,
    description = profile?.description,
    logoFileId = logoFileId,
    deliveryAvailable = profile?.deliveryAvailable,
    pickupAvailable = profile?.pickupAvailable,
    minimumOrderAmount = profile?.minimumOrderAmount,
    deliveryAreaDescription = profile?.deliveryAreaDescription,
    // 扩展字段
    email = email,
    telephone = telephone,
    taxId = taxId,
    city = storeDirections?.city,
    province = storeDirections?.province,
    country = storeDirections?.country,
)