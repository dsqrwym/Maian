package org.dsqrwym.enterprise.permissions

import org.dsqrwym.shared.data.user.UserRole

fun UserRole.canManageEnterpriseProducts(): Boolean =
    this == UserRole.WHOLESALER || this == UserRole.WAREHOUSE

fun UserRole.canManageEnterpriseCategories(): Boolean =
    this == UserRole.WHOLESALER || this == UserRole.WAREHOUSE
