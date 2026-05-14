package org.dsqrwym.standard.domain.cart.mapper

import org.dsqrwym.standard.data.cart.dto.CartGroupDto
import org.dsqrwym.standard.data.cart.dto.CartItemDto
import org.dsqrwym.standard.data.cart.dto.CartItemImageDto
import org.dsqrwym.standard.data.cart.dto.CartResponseDto
import org.dsqrwym.standard.data.cart.dto.CartSummaryDto
import org.dsqrwym.standard.data.cart.dto.CartWholesalerDto
import org.dsqrwym.standard.domain.cart.Cart
import org.dsqrwym.standard.domain.cart.CartGroup
import org.dsqrwym.standard.domain.cart.CartGroupStatus
import org.dsqrwym.standard.domain.cart.CartItem
import org.dsqrwym.standard.domain.cart.CartItemImage
import org.dsqrwym.standard.domain.cart.CartItemStatus
import org.dsqrwym.standard.domain.cart.CartSummary
import org.dsqrwym.standard.domain.cart.CartWholesaler

fun CartResponseDto.toDomain(): Cart =
    Cart(
        groups = groups.map { it.toDomain() },
        summary = summary.toDomain(),
    )

private fun CartGroupDto.toDomain(): CartGroup =
    CartGroup(
        wholesaler = wholesaler.toDomain(),
        itemCount = itemCount,
        totalQuantity = totalQuantity,
        subtotal = subtotal,
        ivaTotal = ivaTotal,
        total = total,
        status = status.toCartGroupStatus(),
        items = items.map { it.toDomain() },
    )

private fun CartWholesalerDto.toDomain(): CartWholesaler =
    CartWholesaler(
        id = id,
        companyName = companyName,
        displayName = displayName,
        profileImageFileId = profileImageFileId,
        minimumOrderAmount = minimumOrderAmount,
    )

private fun CartItemDto.toDomain(): CartItem =
    CartItem(
        cartDetailId = cartDetailId,
        productId = productId,
        variantId = variantId,
        productName = productName,
        productTitle = productTitle,
        productCode = productCode,
        variantCode = variantCode,
        mainImage = mainImage?.toDomain(),
        quantity = quantity,
        saleUnitQty = saleUnitQty,
        minOrderQty = minOrderQty,
        maxOrderQuantity = maxOrderQuantity,
        price = price,
        priceIva = priceIva,
        iva = iva,
        lineSubtotal = lineSubtotal,
        lineIva = lineIva,
        lineTotal = lineTotal,
        status = status.toCartItemStatus(),
    )

private fun CartItemImageDto.toDomain(): CartItemImage =
    CartItemImage(
        id = id,
        mimeType = mimeType,
    )

private fun CartSummaryDto.toDomain(): CartSummary =
    CartSummary(
        wholesalerCount = wholesalerCount,
        itemCount = itemCount,
        totalQuantity = totalQuantity,
        subtotal = subtotal,
        ivaTotal = ivaTotal,
        total = total,
    )

private fun String.toCartItemStatus(): CartItemStatus =
    CartItemStatus.entries.firstOrNull { it.name == uppercase() } ?: CartItemStatus.UNKNOWN

private fun String.toCartGroupStatus(): CartGroupStatus =
    CartGroupStatus.entries.firstOrNull { it.name == uppercase() } ?: CartGroupStatus.UNKNOWN
