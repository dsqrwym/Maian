package org.dsqrwym.shared.data.orders

import org.dsqrwym.shared.data.orders.dto.SharedOrderPartnerSnapshot
import org.dsqrwym.shared.data.orders.dto.SharedOrderSummary

enum class SharedOrderPartnerMode {
    WHOLESALER,
    RETAILER,
}

fun SharedOrderSummary.partnerFor(mode: SharedOrderPartnerMode): SharedOrderPartnerSnapshot? =
    when (mode) {
        SharedOrderPartnerMode.WHOLESALER -> wholesalerSnapshot
        SharedOrderPartnerMode.RETAILER -> retailerSnapshot
    }

fun SharedOrderPartnerSnapshot?.displayNameOrFallback(): String {
    if (this == null) return "-"
    return listOf(companyName, displayName, contactName, email, taxId)
        .firstOrNull { !it.isNullOrBlank() }
        ?: "-"
}
