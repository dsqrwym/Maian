package org.dsqrwym.shared.ui.components.wholesaler

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.LocationCity
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Numbers
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Pin
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.Streetview
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.account_info
import maian.shared.generated.resources.address_city
import maian.shared.generated.resources.address_country
import maian.shared.generated.resources.address_postal_code
import maian.shared.generated.resources.address_state_or_province
import maian.shared.generated.resources.address_street
import maian.shared.generated.resources.amount_euro_value
import maian.shared.generated.resources.business_settings
import maian.shared.generated.resources.cancel
import maian.shared.generated.resources.company_name
import maian.shared.generated.resources.confirm
import maian.shared.generated.resources.delivery_area
import maian.shared.generated.resources.delivery_available
import maian.shared.generated.resources.field_telephone_label
import maian.shared.generated.resources.field_username_label
import maian.shared.generated.resources.first_name
import maian.shared.generated.resources.last_name
import maian.shared.generated.resources.minimum_order_amount
import maian.shared.generated.resources.not_set
import maian.shared.generated.resources.pickup_available
import maian.shared.generated.resources.reset_email_label
import maian.shared.generated.resources.store_address
import maian.shared.generated.resources.tax_id
import org.dsqrwym.shared.data.profile.WholesalerProfileResponseDto
import org.dsqrwym.shared.ui.components.buttons.SharedRetryButton
import org.dsqrwym.shared.ui.components.containers.UiState
import org.dsqrwym.shared.ui.components.progressindicators.SharedLoadingDotsIndicator
import org.dsqrwym.shared.ui.components.row.SharedInfoRow
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
                SharedInfoRow(
                    modifier = Modifier.placeholderWithShimmer(isLoading),
                    Icons.Outlined.Email,
                    stringResource(SharedRes.string.reset_email_label),
                    it
                )
            }
            profile?.username?.let {
                SharedInfoRow(
                    modifier = Modifier.placeholderWithShimmer(isLoading),
                    Icons.Rounded.Person,
                    stringResource(SharedRes.string.field_username_label),
                    it
                )
            }
            profile?.firstName?.let {
                SharedInfoRow(
                    modifier = Modifier.placeholderWithShimmer(isLoading),
                    Icons.Outlined.Badge,
                    stringResource(SharedRes.string.first_name),
                    it
                )
            }
            profile?.lastName?.let {
                SharedInfoRow(
                    modifier = Modifier.placeholderWithShimmer(isLoading),
                    Icons.Outlined.Badge,
                    stringResource(SharedRes.string.last_name),
                    it
                )
            }
            profile?.telephone?.let {
                SharedInfoRow(
                    modifier = Modifier.placeholderWithShimmer(isLoading),
                    Icons.Outlined.Phone,
                    stringResource(SharedRes.string.field_telephone_label),
                    it
                )
            }
            profile?.taxId?.let {
                SharedInfoRow(
                    modifier = Modifier.placeholderWithShimmer(isLoading),
                    Icons.Outlined.Numbers, stringResource(SharedRes.string.tax_id), it
                )
            }
            profile?.profile?.companyName?.let {
                SharedInfoRow(
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
                SharedInfoRow(
                    modifier = Modifier.placeholderWithShimmer(isLoading),
                    Icons.Outlined.Streetview,
                    stringResource(SharedRes.string.address_street),
                    it
                )
            }
            direction?.zipCode?.let {
                SharedInfoRow(
                    modifier = Modifier.placeholderWithShimmer(isLoading),
                    Icons.Outlined.Pin,
                    stringResource(SharedRes.string.address_postal_code),
                    it
                )
            }
            cityName?.let {
                SharedInfoRow(
                    Modifier.placeholderWithShimmer(isLoading),
                    Icons.Outlined.LocationCity,
                    stringResource(SharedRes.string.address_city),
                    it
                )
            }
            provinceName?.let {
                SharedInfoRow(
                    Modifier.placeholderWithShimmer(isLoading),
                    Icons.Outlined.Map,
                    stringResource(SharedRes.string.address_state_or_province),
                    it
                )
            }
            countryName?.let {
                SharedInfoRow(
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
            SharedInfoRow(
                Modifier.placeholderWithShimmer(isLoading),

                Icons.Outlined.ShoppingCart,
                stringResource(SharedRes.string.minimum_order_amount),
                if (moq > 0.00) stringResource(SharedRes.string.amount_euro_value, moq.toString())
                else stringResource(SharedRes.string.not_set)
            )

            SharedInfoRow(
                Modifier.placeholderWithShimmer(isLoading),
                Icons.Outlined.LocalShipping,
                stringResource(SharedRes.string.delivery_available),
                stringResource(if (profileData?.deliveryAvailable == true) SharedRes.string.confirm else SharedRes.string.cancel)
            )

            SharedInfoRow(
                Modifier.placeholderWithShimmer(isLoading),
                Icons.Outlined.Storefront,
                stringResource(SharedRes.string.pickup_available),
                stringResource(if (profileData?.pickupAvailable == true) SharedRes.string.confirm else SharedRes.string.cancel)
            )

            profileData?.deliveryAreaDescription?.takeIf { it.isNotBlank() }?.let {
                SharedInfoRow(
                    Modifier.placeholderWithShimmer(isLoading),
                    Icons.Outlined.Place, stringResource(SharedRes.string.delivery_area), it
                )
            }
        }
    }
}
