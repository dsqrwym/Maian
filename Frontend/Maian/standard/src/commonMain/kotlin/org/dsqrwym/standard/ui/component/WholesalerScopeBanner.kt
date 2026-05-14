package org.dsqrwym.standard.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import maian.shared.generated.resources.*
import maian.standard.generated.resources.StandardRes
import maian.standard.generated.resources.cart_exit_wholesaler_scope
import org.dsqrwym.shared.domain.profile.WholesalerCardData
import org.jetbrains.compose.resources.stringResource

@Composable
fun WholesalerStoreBanner(
    data: WholesalerCardData?,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (data == null) return

    val displayName = data.displayName?.takeIf { it.isNotBlank() } ?: data.companyName
    val location = data.city?.nameLocal ?: data.city?.name
    ?: data.province?.nameLocal ?: data.province?.name

    Surface(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.primaryContainer,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
            )

            FlowRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.alignByBaseline(),
                )

                Box(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .width(1.dp)
                        .height(13.dp)
                        .align(Alignment.CenterVertically)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    location?.let {
                        StripMetaItem(icon = Icons.Outlined.LocationOn, text = it)
                    }
                    if (data.deliveryAvailable == true) {
                        StripMetaItem(
                            icon = Icons.Outlined.LocalShipping,
                            text = stringResource(SharedRes.string.delivery),
                        )
                    }
                    if (data.pickupAvailable == true) {
                        StripMetaItem(
                            icon = Icons.Outlined.Store,
                            text = stringResource(SharedRes.string.pickup),
                        )
                    }
                    data.minimumOrderAmount?.let {
                        StripMetaItem(
                            icon = Icons.Outlined.Euro,
                            text = stringResource(SharedRes.string.min_order_fmt, it),
                        )
                    }
                }
            }

            ExitWholesalerModeButton(
                Modifier,
                onExit,
                border = BorderStroke(
                    0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            )
        }
    }
}

@Composable
fun ExitWholesalerModeButton(
    modifier: Modifier = Modifier,
    onExit: () -> Unit,
    border: BorderStroke? = null,
    colors: ButtonColors = ButtonDefaults.outlinedButtonColors()
) {
    OutlinedButton(
        modifier = modifier,
        onClick = onExit,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 5.dp),
        border = border,
        colors = colors
    ) {
        Icon(
            Icons.Outlined.Close,
            contentDescription = stringResource(StandardRes.string.cart_exit_wholesaler_scope),
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(3.dp))
        Text(
            text = stringResource(SharedRes.string.exit),
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
private fun StripMetaItem(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(13.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            maxLines = 1,
        )
    }
}
