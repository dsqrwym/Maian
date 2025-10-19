package org.dsqrwym.shared.navigation.menu

import org.dsqrwym.shared.data.user.UserRole

data class SharedMenuConfiguration(
    val items: List<SharedMenuItem<out Any>>,
    val topBarActions: List<SharedMenuActions>? = null,
    val userRole: UserRole
) {
    fun getVisibleItems(): List<SharedMenuItem<out Any>> {
        return items.filter {
            it.requiredRole == null || it.requiredRole.contains(userRole)
        }
    }

    fun getPrimaryItems(): List<SharedMenuItem<out Any>> {
        return items.filter { it.isPrimary }
    }

    fun getSecondaryItems(): List<SharedMenuItem<out Any>> {
        return getVisibleItems().filter { !it.isPrimary }
    }
}