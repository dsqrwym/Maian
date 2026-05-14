package org.dsqrwym.shared.ui.components.wholesaler

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Euro
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import maian.shared.generated.resources.*
import org.dsqrwym.shared.data.user.toStringResource
import org.dsqrwym.shared.domain.profile.WholesalerCardData
import org.dsqrwym.shared.network.ApiConfig
import org.dsqrwym.shared.ui.media.SharedAsyncImage
import org.dsqrwym.shared.util.modifier.placeholderWithShimmer
import org.jetbrains.compose.resources.stringResource


/**
 * 批发商卡片组件
 * 两种模式：Profile（大卡片）或 ListItem（列表紧凑卡片）
 */
@Composable
fun SharedWholesalerCard(
    data: WholesalerCardData?,
    variant: WholesalerCardVariant = WholesalerCardVariant.Profile,
    isLoading: Boolean = false,
    onEdit: (() -> Unit)? = null,
    onLogout: (() -> Unit)? = null,
    onImageClick: (() -> Unit)? = null,
    onCardClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    when (variant) {
        WholesalerCardVariant.Profile -> ProfileVariant(
            isLoading = isLoading,
            data = data,
            onEdit = onEdit,
            onLogout = onLogout,
            onImageClick = onImageClick,
            modifier = modifier,
        )

        WholesalerCardVariant.ListItem -> ListItemVariant(
            isLoading = isLoading,
            data = data,
            onCardClick = onCardClick,
            onImageClick = onImageClick,
            modifier = modifier,
        )
    }
}

enum class WholesalerCardVariant { Profile, ListItem }

@Composable
private fun ProfileVariant(
    data: WholesalerCardData?,
    isLoading: Boolean = false,
    onImageClick: (() -> Unit)?,
    onEdit: (() -> Unit)?,
    onLogout: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val displayName = data?.displayName?.takeIf { it.isNotBlank() }
        ?: data?.companyName.takeIf { it?.isNotBlank() == true }
        ?: stringResource(SharedRes.string.not_set)

    OutlinedCard(
        modifier = modifier,
    ) {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            WholesalerLogo(
                modifier = Modifier.placeholderWithShimmer(isLoading),
                userId = data?.id,
                fileId = data?.logoFileId,
                size = 88.dp,
                onClick = onImageClick,
                cornerRadius = 14.dp,
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minWidth = 160.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    modifier = Modifier.placeholderWithShimmer(isLoading),
                    text = displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                )

                Text(
                    modifier = Modifier.placeholderWithShimmer(isLoading),
                    text = stringResource(
                        SharedRes.string.profile_user_id_value,
                        data?.userId ?: stringResource(SharedRes.string.not_set)
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                // 标签 chips
                FlowRow(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    data?.companyType?.let {
                        WholesalerChip(
                            modifier = Modifier.placeholderWithShimmer(isLoading),
                            text = stringResource(it.toStringResource())
                        )
                    }
                    if (data?.deliveryAvailable == true) {
                        WholesalerChip(
                            modifier = Modifier.placeholderWithShimmer(isLoading),
                            text = stringResource(SharedRes.string.delivery_available),
                            icon = Icons.Outlined.LocalShipping,
                        )
                    }
                    if (data?.pickupAvailable == true) {
                        WholesalerChip(
                            modifier = Modifier.placeholderWithShimmer(isLoading),
                            text = stringResource(SharedRes.string.pickup_available),
                            icon = Icons.Outlined.Store,
                        )
                    }
                }

                data?.description?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp).placeholderWithShimmer(isLoading),
                    )
                }

                // 操作按钮行：编辑在左，登出在右
                if (onEdit != null || onLogout != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        onEdit?.let {
                            FilledTonalButton(onClick = it) {
                                Icon(
                                    Icons.Outlined.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(stringResource(SharedRes.string.edit))
                            }
                        }

                        onLogout?.let {
                            TextButton(
                                onClick = it,
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error,
                                ),
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Outlined.Logout,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(stringResource(SharedRes.string.logout))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ListItemVariant(
    isLoading: Boolean = false,
    data: WholesalerCardData?,
    onCardClick: (() -> Unit)?,
    onImageClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val displayName = data?.displayName?.takeIf { it.isNotBlank() }
        ?: data?.companyName ?: ""

    OutlinedCard(
        modifier = modifier,
        onClick = { onCardClick?.invoke() },
    ) {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            WholesalerLogo(
                modifier = Modifier.placeholderWithShimmer(isLoading),
                userId = data?.id,
                fileId = data?.logoFileId,
                size = 56.dp,
                onClick = onImageClick,
                cornerRadius = 10.dp,
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minWidth = 140.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    modifier = Modifier.placeholderWithShimmer(isLoading),
                    text = displayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                )

                // chips 行
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    data?.companyType?.let {
                        WholesalerChip(
                            modifier = Modifier.placeholderWithShimmer(isLoading),
                            text = stringResource(it.toStringResource()),
                            small = true,
                        )
                    }
                    // 城市/省份
                    val location = listOfNotNull(data?.city?.name, data?.province?.name)
                        .firstOrNull()
                    location?.let {
                        WholesalerChip(
                            modifier = Modifier.placeholderWithShimmer(isLoading),
                            text = it, small = true
                        )
                    }
                }

                // 配送 / 自提 / 最低金额
                FlowRow(
                    modifier = Modifier.padding(top = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    if (data?.deliveryAvailable == true) {
                        IconText(
                            modifier = Modifier.placeholderWithShimmer(isLoading),
                            icon = Icons.Outlined.LocalShipping,
                            text = stringResource(SharedRes.string.delivery_available),
                        )
                    }
                    if (data?.pickupAvailable == true) {
                        IconText(
                            modifier = Modifier.placeholderWithShimmer(isLoading),
                            icon = Icons.Outlined.Store,
                            text = stringResource(SharedRes.string.pickup_available),
                        )
                    }
                    data?.minimumOrderAmount?.let {
                        IconText(
                            modifier = Modifier.placeholderWithShimmer(isLoading),
                            icon = Icons.Outlined.Euro,
                            text = stringResource(SharedRes.string.minimum_order_amount_value, it),
                        )
                    }
                }

                data?.description?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        modifier = Modifier.placeholderWithShimmer(isLoading),
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun WholesalerLogo(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)?,
    userId: String?,
    fileId: String?,
    size: Dp,
    cornerRadius: Dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(
                0.5.dp,
                MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(cornerRadius),
            ),
        contentAlignment = Alignment.Center,
    ) {
        userId?.let { userId ->
            SharedAsyncImage(
                model = fileId?.let { ApiConfig.FilePath.userImage(userId, it) },
                modifier = Modifier.fillMaxSize()
                    .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
                contentDescription = stringResource(SharedRes.string.wholesalers),
                contentScale = ContentScale.Crop,
                zoomable = false,
                enableContextMenu = false
            )
        }
    }

}

@Composable
private fun WholesalerChip(
    modifier: Modifier = Modifier,
    text: String?,
    icon: ImageVector? = null,
    small: Boolean = false,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraSmall,
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = if (small) 8.dp else 10.dp,
                vertical = if (small) 2.dp else 3.dp,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            icon?.let {
                Icon(it, contentDescription = null, modifier = Modifier.size(if (small) 11.dp else 12.dp))
            }
            Text(
                text = text ?: "",
                style = if (small) MaterialTheme.typography.labelSmall
                else MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun IconText(modifier: Modifier = Modifier, icon: ImageVector, text: String) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            icon,
            contentDescription = icon.name,
            modifier = Modifier.size(13.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
