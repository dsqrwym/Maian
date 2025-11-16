package org.dsqrwym.enterprise

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import maian.enterprise.generated.resources.EnterpriseRes
import maian.enterprise.generated.resources.category_management
import maian.enterprise.generated.resources.category_management_description
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.category
import org.dsqrwym.enterprise.navigation.Categories
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
                EnterpriseRes.string.category_management,
                EnterpriseRes.string.category_management_description,
                Icons.Outlined.Category,
                SharedRes.string.category,
                setOf(UserRole.WHOLESALER, UserRole.WAREHOUSE),
            )
        )
    )
    val topBarActions: List<SharedMenuActions> = listOf(
        SharedMenuActions.ThemeChangeIconButton,
        SharedMenuActions.LanguageSwitcherIconButton,
    )

}