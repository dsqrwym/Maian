package org.dsqrwym.standard.domain.browse

import org.dsqrwym.shared.data.profile.WholesalerProfileResponseDto
import org.dsqrwym.shared.domain.profile.WholesalerCardData
import org.dsqrwym.standard.data.browse.dto.WholesalerListItemDto

fun WholesalerListItemDto.toDomain(): RetailWholesaler =
    RetailWholesaler(
        id = id,
        userId = userId,
        displayName = displayName,
        companyName = companyName,
        companyType = companyType,
        description = description,
        logoFileId = profileImageFileId?.toString(),
        deliveryAvailable = deliveryAvailable,
        pickupAvailable = pickupAvailable,
        minimumOrderAmount = minimumOrderAmount,
        deliveryAreaDescription = deliveryAreaDescription,
        city = city,
        province = province,
    )

fun WholesalerProfileResponseDto.toRetailWholesaler(fallbackId: String): RetailWholesaler =
    RetailWholesaler(
        id = id ?: fallbackId,
        userId = userId,
        displayName = profile?.displayName,
        companyName = profile?.companyName.orEmpty(),
        companyType = profile?.companyType,
        description = profile?.description,
        logoFileId = logoFileId,
        deliveryAvailable = profile?.deliveryAvailable,
        pickupAvailable = profile?.pickupAvailable,
        minimumOrderAmount = profile?.minimumOrderAmount,
        deliveryAreaDescription = profile?.deliveryAreaDescription,
        city = storeDirections?.city,
        province = storeDirections?.province,
    )

fun RetailWholesaler.toCardData(): WholesalerCardData =
    WholesalerCardData(
        id = id,
        userId = userId.orEmpty(),
        displayName = displayName,
        companyName = companyName,
        companyType = companyType,
        description = description,
        logoFileId = logoFileId,
        deliveryAvailable = deliveryAvailable,
        pickupAvailable = pickupAvailable,
        minimumOrderAmount = minimumOrderAmount,
        deliveryAreaDescription = deliveryAreaDescription,
        city = city,
        province = province,
    )
