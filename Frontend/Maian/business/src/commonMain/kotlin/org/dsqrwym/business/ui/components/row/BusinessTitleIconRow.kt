package org.dsqrwym.business.ui.components.row

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import org.dsqrwym.shared.util.modifier.placeholderWithShimmer
import org.dsqrwym.shared.util.row.SharedRowLayout

@Composable
fun BusinessTitleIconRow(
    titleText: String,
    icon: ImageVector,
    iconContentDescription: String? = null,
    isLoading: Boolean = false
) {
    Row(
        modifier = Modifier.placeholderWithShimmer(isLoading),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = SharedRowLayout.arrangement
    ) {
        Text(titleText)
        Icon(icon, iconContentDescription)
    }
}