package org.dsqrwym.shared.navigation.menu

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.dashboard
import maian.shared.generated.resources.profile
import org.dsqrwym.shared.data.user.UserRole
import org.dsqrwym.shared.navigation.SharedDashboardScreen
import org.dsqrwym.shared.navigation.SharedProfileScreen
import org.jetbrains.compose.resources.StringResource

data class SharedMenuItemState(
    val item: SharedMenuItem<out Any>,
    val showBadge: Boolean = false,
    val badgeCount: Int = 0,
)

open class SharedMenuItem<R : @Serializable Any>(
    val route: @Serializable R,
    val label: StringResource,
    val description: StringResource? = null,
    val icon: ImageVector? = null,
    val iconContentDescription: StringResource? = null,
    val requiredRole: Set<UserRole>? = null,
    val isPrimary: Boolean = false
) {
    object Dashboard : SharedMenuItem<SharedDashboardScreen>(
        route = SharedDashboardScreen,
        label = SharedRes.string.dashboard,
        icon = Icons.Outlined.Home,
        iconContentDescription = SharedRes.string.dashboard,
        isPrimary = true
    )

    object Profile : SharedMenuItem<SharedProfileScreen>(
        route = SharedProfileScreen,
        label = SharedRes.string.profile,
        icon = Icons.Outlined.Person,
        iconContentDescription = SharedRes.string.profile,
        isPrimary = true
    )
}