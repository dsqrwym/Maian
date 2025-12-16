package org.dsqrwym.admin

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import maian.admin.generated.resources.AdminRes
import maian.admin.generated.resources.category_management_description
import maian.business.generated.resources.BusinessRes
import maian.business.generated.resources.category_management
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.category
import org.dsqrwym.business.navigation.Categories
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
                AdminRes.string.category_management_description,
                Icons.Outlined.Category,
                SharedRes.string.category,
                setOf(UserRole.ADMIN, UserRole.SUPERADMIN),
            )
        )
    )
    val topBarActions: List<SharedMenuActions> = listOf(
        SharedMenuActions.ThemeChangeIconButton,
        SharedMenuActions.LanguageSwitcherIconButton,
    )

}