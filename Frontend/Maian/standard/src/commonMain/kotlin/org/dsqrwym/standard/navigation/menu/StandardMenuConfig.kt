package org.dsqrwym.standard.navigation.menu

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.ShopTwo
import androidx.compose.material.icons.outlined.ShoppingCart
import org.dsqrwym.shared.data.user.UserRole
import org.dsqrwym.shared.navigation.menu.SharedMenuActions
import org.dsqrwym.shared.navigation.menu.SharedMenuItem
import org.dsqrwym.shared.navigation.menu.SharedMenuItemState
import org.dsqrwym.standard.navigation.BasketScreen
import org.dsqrwym.standard.navigation.ChatScreen
import org.dsqrwym.standard.navigation.SuppliersScreen

object StandardMenuConfig {
    val menuList = listOf(
        SharedMenuItemState(SharedMenuItem.Dashboard),
        SharedMenuItemState(
            SharedMenuItem(
                route = SuppliersScreen,
                label = "Suppliers",
                icon = Icons.Outlined.ShopTwo,
                iconContentDescription = "Suppliers",
                isPrimary = true
            )
        ),
        SharedMenuItemState(
            SharedMenuItem(
                route = ChatScreen,
                label = "Chat",
                icon = Icons.AutoMirrored.Outlined.Chat,
                iconContentDescription = "Chat",
                isPrimary = true
            )
        ),
        SharedMenuItemState(
            SharedMenuItem(
                route = BasketScreen,
                label = "Basket",
                icon = Icons.Outlined.ShoppingCart,
                isPrimary = true
            )
        ),
        SharedMenuItemState(SharedMenuItem.Profile),
        SharedMenuItemState(
            SharedMenuItem(
                route = SuppliersScreen,
                label = "Suppliers",
                icon = Icons.Outlined.ShopTwo,
                iconContentDescription = "Suppliers",
            )
        ),
        SharedMenuItemState(
            SharedMenuItem(
                route = ChatScreen,
                label = "Chat",
                icon = Icons.AutoMirrored.Outlined.Chat,
                iconContentDescription = "Chat",
            )
        ),
        SharedMenuItemState(
            SharedMenuItem(
                route = BasketScreen,
                label = "Basket",
                icon = Icons.Outlined.ShoppingCart,
            )
        ),
    )
    val topBarActions: List<SharedMenuActions> = listOf(
        SharedMenuActions.ThemeChangeIconButton,
        SharedMenuActions.LanguageSwitcherIconButton,
    )
    val userRole = UserRole.RETAILER
}
