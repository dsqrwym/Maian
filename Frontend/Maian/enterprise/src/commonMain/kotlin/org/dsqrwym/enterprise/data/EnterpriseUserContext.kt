package org.dsqrwym.enterprise.data

import org.dsqrwym.shared.data.user.SharedUserPayload
import org.dsqrwym.shared.data.user.UserRole

fun SharedUserPayload.enterpriseOwnerUserId(): String =
    if (userRole.isWholesalerStaff()) {
        wholesalerId ?: userId
    } else {
        userId
    }

private fun UserRole.isWholesalerStaff(): Boolean =
    this == UserRole.SUPPORT ||
            this == UserRole.DELIVERY ||
            this == UserRole.WAREHOUSE
