package org.dsqrwym.shared.navigation.menu

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.dashboard
import maian.shared.generated.resources.profile
import org.dsqrwym.shared.data.user.UserRole
import org.dsqrwym.shared.navigation.SharedDashboardScreen
import org.dsqrwym.shared.navigation.SharedProfileScreen
import org.jetbrains.compose.resources.StringResource

data class SharedMenuItemState(
    val item: SharedMenuItem,
    val showBadge: Boolean = false,
    val badgeCount: Int = 0,
)

open class SharedMenuItem(
    val route: NavKey,
    val label: StringResource,
    val description: StringResource? = null,
    val icon: ImageVector? = null,
    val iconContentDescription: StringResource? = null,
    val requiredRole: Set<UserRole>? = null,
    val isPrimary: Boolean = false
) {
    object Dashboard : SharedMenuItem(
        route = SharedDashboardScreen,
        label = SharedRes.string.dashboard,
        icon = Icons.Outlined.Home,
        iconContentDescription = SharedRes.string.dashboard,
        isPrimary = true
    )

    object Profile : SharedMenuItem(
        route = SharedProfileScreen,
        label = SharedRes.string.profile,
        icon = Icons.Outlined.Person,
        iconContentDescription = SharedRes.string.profile,
        isPrimary = true
    )
}