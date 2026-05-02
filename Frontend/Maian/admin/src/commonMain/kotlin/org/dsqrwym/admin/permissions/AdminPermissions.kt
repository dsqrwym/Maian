package org.dsqrwym.admin.permissions

import org.dsqrwym.shared.data.user.UserRole

fun UserRole.canDeleteAdminCategory(): Boolean =
    this == UserRole.SUPERADMIN
