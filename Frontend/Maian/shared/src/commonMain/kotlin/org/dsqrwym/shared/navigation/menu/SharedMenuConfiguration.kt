package org.dsqrwym.shared.navigation.menu

import org.dsqrwym.shared.data.user.UserRole
import org.dsqrwym.shared.util.log.SharedLog

data class SharedMenuConfiguration(
    val items: List<SharedMenuItemState>,
    val topBarActions: List<SharedMenuActions>? = null,
    val userRole: UserRole
) {
    fun getVisibleItems(): List<SharedMenuItemState> {
        val visible = items.filter {
            it.item.requiredRole == null || it.item.requiredRole.contains(userRole)
        }

        SharedLog.log(
            message = buildString {
                appendLine("getVisibleItems() called")
                appendLine("userRole = $userRole")
                appendLine("all items = ${items.size}")
                appendLine("visible items = ${visible.size}")
                items.forEach {
                    appendLine(" - item: ${it.item.label} | requiredRole=${it.item.requiredRole} | isPrimary=${it.item.isPrimary}")
                }
            }
        )

        return visible
    }

    fun getPrimaryItems(): List<SharedMenuItemState> {
        val primary = items.filter { it.item.isPrimary }

        SharedLog.log(
            message = buildString {
                appendLine("getPrimaryItems() called")
                appendLine("primary items = ${primary.size}")
                primary.forEach {
                    appendLine(" - item: ${it.item.label}")
                }
            }
        )

        return primary
    }

    fun getSecondaryItems(): List<SharedMenuItemState> {
        val visible = getVisibleItems()
        val secondary = visible.filter { !it.item.isPrimary }

        SharedLog.log(
            message = buildString {
                appendLine("getSecondaryItems() called")
                appendLine("visible items = ${visible.size}")
                appendLine("secondary items = ${secondary.size}")
                visible.forEach {
                    appendLine(" - visible: ${it.item.label} | isPrimary=${it.item.isPrimary}")
                }
            }
        )

        return secondary
    }
}