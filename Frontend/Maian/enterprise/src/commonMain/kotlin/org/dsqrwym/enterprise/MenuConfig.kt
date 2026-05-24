package org.dsqrwym.enterprise

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import maian.enterprise.generated.resources.EnterpriseRes
import maian.enterprise.generated.resources.category_management_description
import maian.enterprise.generated.resources.dashboard_title
import maian.enterprise.generated.resources.employee_management
import maian.enterprise.generated.resources.employee_management_description
import maian.enterprise.generated.resources.product_management_description
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.category
import maian.shared.generated.resources.order_history
import maian.shared.generated.resources.orders
import maian.shared.generated.resources.products
import org.dsqrwym.business.navigation.Categories
import org.dsqrwym.enterprise.navigation.Employees
import org.dsqrwym.enterprise.navigation.OrderHistory
import org.dsqrwym.enterprise.navigation.Products
import org.dsqrwym.shared.data.user.UserRole
import org.dsqrwym.shared.navigation.SharedDashboardScreen
import org.dsqrwym.shared.navigation.menu.SharedMenuActions
import org.dsqrwym.shared.navigation.menu.SharedMenuItem
import org.dsqrwym.shared.navigation.menu.SharedMenuItemState

object MenuConfig {
    val menuList = listOf(
        SharedMenuItemState(
            SharedMenuItem(
                SharedDashboardScreen,
                EnterpriseRes.string.dashboard_title,
                icon = Icons.Outlined.Home,
                iconContentDescription = EnterpriseRes.string.dashboard_title,
                isPrimary = true,
            ),
        ),
        SharedMenuItemState(
            SharedMenuItem(
                OrderHistory,
                SharedRes.string.orders,
                SharedRes.string.order_history,
                Icons.AutoMirrored.Outlined.ReceiptLong,
                SharedRes.string.orders,
                setOf(UserRole.WHOLESALER, UserRole.WAREHOUSE, UserRole.DELIVERY, UserRole.SUPPORT),
                isPrimary = true,
            ),
        ),
        SharedMenuItemState(
            SharedMenuItem(
                Products,
                SharedRes.string.products,
                EnterpriseRes.string.product_management_description,
                Icons.Outlined.Inventory2,
                SharedRes.string.products,
                setOf(UserRole.WHOLESALER, UserRole.WAREHOUSE),
                isPrimary = true,
            ),
        ),
        SharedMenuItemState(SharedMenuItem.Profile),
        SharedMenuItemState(
            SharedMenuItem(
                Categories,
                SharedRes.string.category,
                EnterpriseRes.string.category_management_description,
                Icons.Outlined.Category,
                SharedRes.string.category,
                setOf(UserRole.WHOLESALER, UserRole.WAREHOUSE),
                isPrimary = false,
            ),
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
            ),
        ),
    )
    val topBarActions: List<SharedMenuActions> = listOf(
        SharedMenuActions.ThemeChangeIconButton,
        SharedMenuActions.LanguageSwitcherIconButton,
    )
}
