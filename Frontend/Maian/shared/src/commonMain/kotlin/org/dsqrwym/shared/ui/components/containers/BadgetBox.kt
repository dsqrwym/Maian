package org.dsqrwym.shared.ui.components.containers

import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable


@Composable
fun MyBadgedBox(
    showBadge: Boolean,
    badgeNumber: Int,
    badgeContent: @Composable () -> Unit
) {
    BadgedBox(
        badge = {
            if (showBadge) {
                Badge {
                    if (badgeNumber > 0) {
                        Text(badgeNumber.toString())
                    } else {
                        Text("!")
                    }
                }
            }
        }
    ) {
        badgeContent()
    }
}
