package org.dsqrwym.shared.navigation.menu

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable
import org.dsqrwym.shared.data.user.UserRole
import org.dsqrwym.shared.navigation.SharedDashboardScreen
import org.dsqrwym.shared.navigation.SharedProfileScreen

data class SharedMenuItemState(
    val item: SharedMenuItem<out Any>,
    val showBadge: Boolean = false,
    val badgeCount: Int = 0,
)

open class SharedMenuItem<R : @Serializable Any>(
    val route: @Serializable R,
    val label: String,
    val description: String? = null,
    val icon: ImageVector? = null,
    val iconContentDescription: String? = null,
    val requiredRole: Set<UserRole>? = null,
    val isPrimary: Boolean = false
) {
    object Dashboard : SharedMenuItem<SharedDashboardScreen>(
        route = SharedDashboardScreen,
        label = "Dashboard",
        icon = Icons.Outlined.Home,
        iconContentDescription = "Dashboard",
        isPrimary = true
    )

    object Profile : SharedMenuItem<SharedProfileScreen>(
        route = SharedProfileScreen,
        label = "Profile",
        icon = Icons.Outlined.Person,
        iconContentDescription = "Profile",
        isPrimary = true
    )
}