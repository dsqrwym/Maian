package org.dsqrwym.shared.ui.components.wholesaler

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import maian.shared.generated.resources.*
import org.dsqrwym.shared.data.profile.WholesalerProfileResponseDto
import org.dsqrwym.shared.ui.components.buttons.SharedRetryButton
import org.dsqrwym.shared.ui.components.containers.UiState
import org.dsqrwym.shared.ui.components.progressindicators.SharedLoadingDotsIndicator
import org.dsqrwym.shared.util.modifier.placeholderWithShimmer
import org.jetbrains.compose.resources.stringResource

fun LazyStaggeredGridScope.sharedWholesalerProfileDetails(
    profile: WholesalerProfileResponseDto?,
    isLoading: Boolean,
) {
    item {
        AccountInfoCard(Modifier.animateItem(), profile, isLoading)
    }
    item {
        StoreDirectionCard(Modifier.animateItem(), profile, isLoading)
    }
    item {
        BusinessSettingsCard(Modifier.animateItem(), profile, isLoading)
    }
}

@Composable
fun SharedWholesalerProfileState(
    uiState: UiState,
    onRetry: () -> Unit,
    content: @Composable () -> Unit,
) {
    when (uiState) {
        UiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            SharedLoadingDotsIndicator()
        }

        UiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            SharedRetryButton(onRetry)
        }

        else -> content()
    }
}
@Composable
private fun AccountInfoCard(
    modifier: Modifier = Modifier,
    profile: WholesalerProfileResponseDto?,
    isLoading: Boolean = false
) {
    OutlinedCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                stringResource(SharedRes.string.account_info),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(12.dp))

            profile?.email?.let {
                InfoRow(
                    modifier = Modifier.placeholderWithShimmer(isLoading),
                    Icons.Outlined.Email,
                    stringResource(SharedRes.string.reset_email_label),
                    it
                )
            }
            profile?.username?.let {
                InfoRow(
                    modifier = Modifier.placeholderWithShimmer(isLoading),
                    Icons.Rounded.Person,
                    stringResource(SharedRes.string.field_username_label),
                    it
                )
            }
            profile?.firstName?.let {
                InfoRow(
                    modifier = Modifier.placeholderWithShimmer(isLoading),
                    Icons.Outlined.Badge,
                    stringResource(SharedRes.string.first_name),
                    it
                )
            }
            profile?.lastName?.let {
                InfoRow(
                    modifier = Modifier.placeholderWithShimmer(isLoading),
                    Icons.Outlined.Badge,
                    stringResource(SharedRes.string.last_name),
                    it
                )
            }
            profile?.telephone?.let {
                InfoRow(
                    modifier = Modifier.placeholderWithShimmer(isLoading),
                    Icons.Outlined.Phone,
                    stringResource(SharedRes.string.field_telephone_label),
                    it
                )
            }
            profile?.taxId?.let {
                InfoRow(
                    modifier = Modifier.placeholderWithShimmer(isLoading),
                    Icons.Outlined.Numbers, stringResource(SharedRes.string.tax_id), it
                )
            }
            profile?.profile?.companyName?.let {
                InfoRow(
                    modifier = Modifier.placeholderWithShimmer(isLoading),
                    Icons.Outlined.Business,
                    stringResource(SharedRes.string.company_name),
                    it
                )
            }
        }
    }
}

@Composable
private fun StoreDirectionCard(
    modifier: Modifier = Modifier,
    profile: WholesalerProfileResponseDto?,
    isLoading: Boolean = false
) {
    val direction = profile?.storeDirections
    val cityName = direction?.city?.nameLocal ?: direction?.city?.name
    val provinceName = direction?.province?.nameLocal ?: direction?.province?.name
    val countryName = direction?.country?.nameLocal ?: direction?.country?.name

    OutlinedCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                stringResource(SharedRes.string.store_address),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(12.dp))

            direction?.street?.let {
                InfoRow(
                    modifier = Modifier.placeholderWithShimmer(isLoading),
                    Icons.Outlined.Streetview,
                    stringResource(SharedRes.string.address_street),
                    it
                )
            }
            direction?.zipCode?.let {
                InfoRow(
                    modifier = Modifier.placeholderWithShimmer(isLoading),
                    Icons.Outlined.Pin,
                    stringResource(SharedRes.string.address_postal_code),
                    it
                )
            }
            cityName?.let {
                InfoRow(
                    Modifier.placeholderWithShimmer(isLoading),
                    Icons.Outlined.LocationCity,
                    stringResource(SharedRes.string.address_city),
                    it
                )
            }
            provinceName?.let {
                InfoRow(
                    Modifier.placeholderWithShimmer(isLoading),
                    Icons.Outlined.Map,
                    stringResource(SharedRes.string.address_state_or_province),
                    it
                )
            }
            countryName?.let {
                InfoRow(
                    Modifier.placeholderWithShimmer(isLoading),
                    Icons.Outlined.Public,
                    stringResource(SharedRes.string.address_country),
                    it
                )
            }
        }
    }
}

@Composable
private fun BusinessSettingsCard(
    modifier: Modifier = Modifier,
    profile: WholesalerProfileResponseDto?,
    isLoading: Boolean = false
) {
    val profileData = profile?.profile

    OutlinedCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                stringResource(SharedRes.string.business_settings),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(12.dp))

            val moq = profileData?.minimumOrderAmount?.toDoubleOrNull() ?: 0.00
            InfoRow(
                Modifier.placeholderWithShimmer(isLoading),

                Icons.Outlined.ShoppingCart,
                stringResource(SharedRes.string.minimum_order_amount),
                if (moq > 0.00) stringResource(SharedRes.string.amount_euro_value, moq.toString())
                else stringResource(SharedRes.string.not_set)
            )

            InfoRow(
                Modifier.placeholderWithShimmer(isLoading),
                Icons.Outlined.LocalShipping,
                stringResource(SharedRes.string.delivery_available),
                stringResource(if (profileData?.deliveryAvailable == true) SharedRes.string.confirm else SharedRes.string.cancel)
            )

            InfoRow(
                Modifier.placeholderWithShimmer(isLoading),
                Icons.Outlined.Storefront,
                stringResource(SharedRes.string.pickup_available),
                stringResource(if (profileData?.pickupAvailable == true) SharedRes.string.confirm else SharedRes.string.cancel)
            )

            profileData?.deliveryAreaDescription?.takeIf { it.isNotBlank() }?.let {
                InfoRow(
                    Modifier.placeholderWithShimmer(isLoading),
                    Icons.Outlined.Place, stringResource(SharedRes.string.delivery_area), it
                )
            }
        }
    }
}

@Composable
private fun InfoRow(modifier: Modifier = Modifier, icon: ImageVector, label: String, value: String) {
    Row(
        modifier = modifier
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = icon.name,
            tint = MaterialTheme.colorScheme.primary,
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
