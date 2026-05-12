package org.dsqrwym.shared.domain.profile

import org.dsqrwym.shared.data.profile.WholesalerProfileResponseDto

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
    email = email,
    telephone = telephone,
    taxId = taxId,
    city = storeDirections?.city,
    province = storeDirections?.province,
    country = storeDirections?.country,
)
