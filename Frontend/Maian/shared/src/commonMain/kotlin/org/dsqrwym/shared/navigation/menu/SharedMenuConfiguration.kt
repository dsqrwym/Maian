package org.dsqrwym.shared.navigation.menu

import org.dsqrwym.shared.data.user.UserRole

data class SharedMenuConfiguration(
    val items: List<SharedMenuItemState>,
    val topBarActions: List<SharedMenuActions>? = null,
    val userRole: UserRole
) {
    fun getVisibleItems(): List<SharedMenuItemState> {
        return items.filter {
            it.item.requiredRole == null || it.item.requiredRole.contains(userRole)
        }
    }

    fun getPrimaryItems(): List<SharedMenuItemState> {
        return items.filter { it.item.isPrimary }
    }

    fun getSecondaryItems(): List<SharedMenuItemState> {
        return getVisibleItems().filter { !it.item.isPrimary }
    }
}