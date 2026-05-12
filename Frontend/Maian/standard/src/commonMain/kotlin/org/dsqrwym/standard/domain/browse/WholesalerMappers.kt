package org.dsqrwym.standard.domain.browse

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
