package org.dsqrwym.enterprise

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Groups
import maian.business.generated.resources.BusinessRes
import maian.business.generated.resources.category_management
import maian.enterprise.generated.resources.EnterpriseRes
import maian.enterprise.generated.resources.category_management_description
import maian.enterprise.generated.resources.employee_management
import maian.enterprise.generated.resources.employee_management_description
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.category
import maian.shared.generated.resources.order_history
import maian.shared.generated.resources.orders
import org.dsqrwym.enterprise.navigation.Employees
import org.dsqrwym.business.navigation.Categories
import org.dsqrwym.enterprise.navigation.OrderHistory
import org.dsqrwym.shared.data.user.UserRole
import org.dsqrwym.shared.navigation.menu.SharedMenuActions
import org.dsqrwym.shared.navigation.menu.SharedMenuItem
import org.dsqrwym.shared.navigation.menu.SharedMenuItemState

object MenuConfig {
    val menuList = listOf(
        SharedMenuItemState(SharedMenuItem.Dashboard),
        SharedMenuItemState(SharedMenuItem.Profile),
        // 二级
        SharedMenuItemState(
            SharedMenuItem(
                Categories,
                BusinessRes.string.category_management,
                EnterpriseRes.string.category_management_description,
                Icons.Outlined.Category,
                SharedRes.string.category,
                setOf(UserRole.WHOLESALER, UserRole.WAREHOUSE),
            )
        ),
        SharedMenuItemState(
            SharedMenuItem(
                OrderHistory,
                SharedRes.string.orders,
                SharedRes.string.order_history,
                Icons.AutoMirrored.Outlined.ReceiptLong,
                SharedRes.string.orders,
                setOf(UserRole.WHOLESALER, UserRole.WAREHOUSE, UserRole.DELIVERY, UserRole.SUPPORT),
                isPrimary = false,
            )
        ),
        SharedMenuItemState(
            SharedMenuItem(
                Employees,
                EnterpriseRes.string.employee_management,
                EnterpriseRes.string.employee_management_description,
                Icons.Outlined.Groups,
                EnterpriseRes.string.employee_management,
                setOf(UserRole.WHOLESALER),
                isPrimary = false,
            )
        )
    )
    val topBarActions: List<SharedMenuActions> = listOf(
        SharedMenuActions.ThemeChangeIconButton,
        SharedMenuActions.LanguageSwitcherIconButton,
    )

}
