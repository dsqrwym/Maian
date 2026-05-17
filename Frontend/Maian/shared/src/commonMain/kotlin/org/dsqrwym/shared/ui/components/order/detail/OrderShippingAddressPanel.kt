package org.dsqrwym.shared.ui.components.order.detail

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.*
import org.dsqrwym.shared.data.orders.dto.SharedOrderShippingAddressSnapshot
import org.dsqrwym.shared.util.formatter.displayCityName
import org.dsqrwym.shared.util.formatter.displayCountryName
import org.dsqrwym.shared.util.formatter.displayProvinceName
import org.dsqrwym.shared.util.formatter.notBlankOrNull
import org.dsqrwym.shared.util.formatter.toSpanishAddressFormat
import org.jetbrains.compose.resources.stringResource

@Composable
fun OrderShippingAddressPanel(
    address: SharedOrderShippingAddressSnapshot?,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    OrderDetailPanelScroll(modifier) {
        OrderDetailCard(title = stringResource(SharedRes.string.shipping_address), icon = Icons.Outlined.LocationOn) {
            OrderDetailFieldGrid(
                fields = listOfNotNull(
                    address?.toSpanishAddressFormat()?.notBlankOrNull()?.let {
                        OrderDetailField(
                            stringResource(SharedRes.string.formatted_address),
                            it,
                            minWidth = 360.dp,
                            maxWidth = 760.dp,
                            weight = 2f,
                        )
                    },
                    orderDetailFieldOrNull(stringResource(SharedRes.string.address_street), address?.street),
                    orderDetailFieldOrNull(stringResource(SharedRes.string.address_postal_code), address?.zipCode),
                    orderDetailFieldOrNull(stringResource(SharedRes.string.address_city), address?.displayCityName()),
                    orderDetailFieldOrNull(stringResource(SharedRes.string.address_state_or_province), address?.displayProvinceName()),
                    orderDetailFieldOrNull(stringResource(SharedRes.string.address_country), address?.displayCountryName()),
                    orderDetailFieldOrNull(stringResource(SharedRes.string.city_name), address?.cityName),
                    orderDetailFieldOrNull(stringResource(SharedRes.string.city_name_local), address?.cityNameLocal),
                    orderDetailFieldOrNull(stringResource(SharedRes.string.province_name), address?.provinceName),
                    orderDetailFieldOrNull(stringResource(SharedRes.string.province_name_local), address?.provinceNameLocal),
                    orderDetailFieldOrNull(stringResource(SharedRes.string.country_name), address?.countryName),
                    orderDetailFieldOrNull(stringResource(SharedRes.string.country_name_local), address?.countryNameLocal),
                    orderDetailFieldOrNull(stringResource(SharedRes.string.country_alpha2), address?.countryAlpha2, minWidth = 96.dp, maxWidth = 140.dp, weight = 0f),
                    orderDetailFieldOrNull(stringResource(SharedRes.string.country_alpha3), address?.countryAlpha3, minWidth = 96.dp, maxWidth = 140.dp, weight = 0f),
                    orderDetailFieldOrNull(stringResource(SharedRes.string.country_iso), address?.countryIso?.toString(), minWidth = 96.dp, maxWidth = 140.dp, weight = 0f),
                ),
                isLoading = isLoading,
            )
        }
    }
}
