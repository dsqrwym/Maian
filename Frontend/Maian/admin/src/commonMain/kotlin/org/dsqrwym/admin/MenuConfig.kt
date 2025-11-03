package org.dsqrwym.admin

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.field_required
import org.dsqrwym.admin.navigation.Categories
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
                SharedRes.string.field_required,
                null,
                Icons.Outlined.Category,
                null,
                setOf(UserRole.ADMIN, UserRole.SUPERADMIN),
            )
        )
    )
    val topBarActions: List<SharedMenuActions> = listOf(
        SharedMenuActions.ThemeChangeIconButton,
        SharedMenuActions.LanguageSwitcherIconButton,
    )

}