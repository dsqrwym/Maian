package org.dsqrwym.shared.navigation.menu

import org.dsqrwym.shared.data.user.UserRole

data class SharedMenuConfiguration(
    val items: List<SharedMenuItemState>,
    val topBarActions: List<SharedMenuActions>? = null,
    val userRole: UserRole
) {
    fun getVisibleItems(): List<SharedMenuItemState> {
        val visible = items.filter {
            it.item.requiredRole == null || it.item.requiredRole.contains(userRole)
        }
        return visible
    }

    fun getPrimaryItems(): List<SharedMenuItemState> {
        val primary = items.filter { it.item.isPrimary }
        return primary
    }

    fun getSecondaryItems(): List<SharedMenuItemState> {
        val visible = getVisibleItems()
        val secondary = visible.filter { !it.item.isPrimary }
        return secondary
    }
}