package org.dsqrwym.standard.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.LocationCity
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Numbers
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Pin
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.Streetview
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.account_info
import maian.shared.generated.resources.address_city
import maian.shared.generated.resources.address_country
import maian.shared.generated.resources.address_postal_code
import maian.shared.generated.resources.address_state_or_province
import maian.shared.generated.resources.address_street
import maian.shared.generated.resources.company_info
import maian.shared.generated.resources.company_name
import maian.shared.generated.resources.company_type
import maian.shared.generated.resources.display_name
import maian.shared.generated.resources.edit
import maian.shared.generated.resources.field_telephone_label
import maian.shared.generated.resources.field_username_label
import maian.shared.generated.resources.first_name
import maian.shared.generated.resources.last_name
import maian.shared.generated.resources.load_failed
import maian.shared.generated.resources.logout
import maian.shared.generated.resources.not_set
import maian.shared.generated.resources.profile_user_id_value
import maian.shared.generated.resources.reset_email_label
import maian.shared.generated.resources.store_address
import maian.shared.generated.resources.tax_id
import maian.standard.generated.resources.StandardRes
import maian.standard.generated.resources.contact_name
import maian.standard.generated.resources.retailer_profile
import org.dsqrwym.shared.data.auth.session.AuthSessionViewModel
import org.dsqrwym.shared.data.profile.RetailerProfileResponseDto
import org.dsqrwym.shared.data.user.toStringResource
import org.dsqrwym.shared.network.ApiConfig
import org.dsqrwym.shared.ui.components.dialog.SharedImageViewDialog
import org.dsqrwym.shared.ui.components.progressindicators.SharedLoadingDotsIndicator
import org.dsqrwym.shared.ui.components.scaffold.SharedTransparentScaffold
import org.dsqrwym.shared.ui.media.SharedAsyncImage
import org.dsqrwym.shared.util.lazygrid.SharedLazyGridLayout
import org.dsqrwym.shared.util.modifier.paddingWithoutTop
import org.dsqrwym.shared.util.modifier.placeholderWithShimmer
import org.dsqrwym.standard.ui.viewmodels.profile.RetailerProfileViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.currentKoinScope
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RetailerProfileScreen(
    onNavigateToEdit: () -> Unit,
    viewModel: RetailerProfileViewModel = koinViewModel(),
) {
    var showImageDialog by remember { mutableStateOf(false) }
    val profile = viewModel.profile
    val isLoading = viewModel.isLoading
    val authSessionViewModel: AuthSessionViewModel = currentKoinScope().get()

    LaunchedEffect(Unit) {
        viewModel.refreshProfile()
    }

    SharedTransparentScaffold(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(Icons.Outlined.Storefront, contentDescription = null)
                Text(
                    text = profile?.profile?.displayName
                        ?: profile?.profile?.companyName
                        ?: stringResource(StandardRes.string.retailer_profile),
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        showOverlayDialog = showImageDialog && profile?.logoFileId != null,
        overlayContent = {
            val logoFileId = profile?.logoFileId ?: return@SharedTransparentScaffold
            SharedImageViewDialog(
                model = profile.id?.let { ApiConfig.FilePath.userImage(it, logoFileId) } ?: "",
                onDismissRequest = { showImageDialog = false },
            )
        },
    ) { padding, scrollBehavior ->
        RetailerProfileState(
            uiState = viewModel.uiState,
            onRetry = viewModel::refreshProfile,
        ) {
            LazyVerticalStaggeredGrid(
                modifier = Modifier
                    .fillMaxSize()
                    .paddingWithoutTop(padding)
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                columns = StaggeredGridCells.Adaptive(minSize = 399.9.dp),
                contentPadding = PaddingValues(SharedLazyGridLayout.Padding),
                horizontalArrangement = SharedLazyGridLayout.arrangement,
                verticalItemSpacing = SharedLazyGridLayout.verticalItemSpacing,
            ) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    Spacer(Modifier.height(padding.calculateTopPadding()))
                }
                item {
                    RetailerHeaderCard(
                        modifier = Modifier.animateItem(),
                        profile = profile,
                        isLoading = isLoading,
                        onImageClick = { showImageDialog = true },
                        onEdit = onNavigateToEdit,
                        onLogout = { authSessionViewModel.logout() },
                    )
                }
                item {
                    AccountInfoCard(
                        modifier = Modifier.animateItem(),
                        profile = profile,
                        isLoading = isLoading,
                    )
                }
                if (profile.hasCompanyInfo()) {
                    item {
                        CompanyInfoCard(
                            modifier = Modifier.animateItem(),
                            profile = profile,
                            isLoading = isLoading,
                        )
                    }
                }
                if (profile.hasStoreDirection()) {
                    item {
                        StoreDirectionCard(
                            modifier = Modifier.animateItem(),
                            profile = profile,
                            isLoading = isLoading,
                        )
                    }
                }
                item(span = StaggeredGridItemSpan.FullLine) {
                    Spacer(Modifier.height(28.dp))
                }
            }
        }
    }
}

@Composable
private fun RetailerProfileState(
    uiState: org.dsqrwym.shared.ui.components.containers.UiState,
    onRetry: () -> Unit,
    content: @Composable () -> Unit,
) {
    when (uiState) {
        org.dsqrwym.shared.ui.components.containers.UiState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                SharedLoadingDotsIndicator()
            }
        }

        org.dsqrwym.shared.ui.components.containers.UiState.Error -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                ElevatedButton(onClick = onRetry) {
                    Text(stringResource(SharedRes.string.load_failed))
                }
            }
        }

        else -> content()
    }
}

@Composable
private fun RetailerHeaderCard(
    modifier: Modifier = Modifier,
    profile: RetailerProfileResponseDto?,
    isLoading: Boolean,
    onImageClick: () -> Unit,
    onEdit: () -> Unit,
    onLogout: () -> Unit,
) {
    val displayName = profile?.profile?.displayName?.takeIf { it.isNotBlank() }
        ?: profile?.profile?.companyName?.takeIf { it.isNotBlank() }
        ?: stringResource(SharedRes.string.not_set)

    OutlinedCard(modifier = modifier) {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            RetailerLogo(
                modifier = Modifier.placeholderWithShimmer(isLoading),
                userId = profile?.id,
                fileId = profile?.logoFileId,
                size = 88.dp,
                cornerRadius = 14.dp,
                onClick = onImageClick,
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
                        profile?.userId ?: stringResource(SharedRes.string.not_set),
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                FlowRow(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    profile?.profile?.companyType?.let {
                        RetailerChip(
                            modifier = Modifier.placeholderWithShimmer(isLoading),
                            text = stringResource(it.toStringResource()),
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FilledTonalButton(onClick = onEdit) {
                        Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(SharedRes.string.edit))
                    }

                    TextButton(
                        onClick = onLogout,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error,
                        ),
                    ) {
                        Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(SharedRes.string.logout))
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountInfoCard(
    modifier: Modifier = Modifier,
    profile: RetailerProfileResponseDto?,
    isLoading: Boolean,
) {
    OutlinedCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                stringResource(SharedRes.string.account_info),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(12.dp))
            profile?.email?.takeIf { it.isNotBlank() }?.let {
                InfoRow(Modifier.placeholderWithShimmer(isLoading), Icons.Outlined.Email, stringResource(SharedRes.string.reset_email_label), it)
            }
            profile?.username?.takeIf { it.isNotBlank() }?.let {
                InfoRow(Modifier.placeholderWithShimmer(isLoading), Icons.Rounded.Person, stringResource(SharedRes.string.field_username_label), it)
            }
            profile?.firstName?.takeIf { it.isNotBlank() }?.let {
                InfoRow(Modifier.placeholderWithShimmer(isLoading), Icons.Outlined.Badge, stringResource(SharedRes.string.first_name), it)
            }
            profile?.lastName?.takeIf { it.isNotBlank() }?.let {
                InfoRow(Modifier.placeholderWithShimmer(isLoading), Icons.Outlined.Badge, stringResource(SharedRes.string.last_name), it)
            }
            profile?.telephone?.takeIf { it.isNotBlank() }?.let {
                InfoRow(Modifier.placeholderWithShimmer(isLoading), Icons.Outlined.Phone, stringResource(SharedRes.string.field_telephone_label), it)
            }
            profile?.taxId?.takeIf { it.isNotBlank() }?.let {
                InfoRow(Modifier.placeholderWithShimmer(isLoading), Icons.Outlined.Numbers, stringResource(SharedRes.string.tax_id), it)
            }
        }
    }
}

@Composable
private fun CompanyInfoCard(
    modifier: Modifier = Modifier,
    profile: RetailerProfileResponseDto?,
    isLoading: Boolean,
) {
    val profileData = profile?.profile
    OutlinedCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                stringResource(SharedRes.string.company_info),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(12.dp))
            profileData?.companyName?.takeIf { it.isNotBlank() }?.let {
                InfoRow(Modifier.placeholderWithShimmer(isLoading), Icons.Outlined.Business, stringResource(SharedRes.string.company_name), it)
            }
            profileData?.displayName?.takeIf { it.isNotBlank() }?.let {
                InfoRow(Modifier.placeholderWithShimmer(isLoading), Icons.Outlined.Storefront, stringResource(SharedRes.string.display_name), it)
            }
            profileData?.companyType?.let {
                InfoRow(Modifier.placeholderWithShimmer(isLoading), Icons.Outlined.Business, stringResource(SharedRes.string.company_type), stringResource(it.toStringResource()))
            }
            profileData?.contactName?.takeIf { it.isNotBlank() }?.let {
                InfoRow(Modifier.placeholderWithShimmer(isLoading), Icons.Outlined.Badge, stringResource(StandardRes.string.contact_name), it)
            }
        }
    }
}

private fun RetailerProfileResponseDto?.hasCompanyInfo(): Boolean {
    val profile = this?.profile ?: return false
    return !profile.companyName.isNullOrBlank() ||
            !profile.displayName.isNullOrBlank() ||
            profile.companyType != null ||
            !profile.contactName.isNullOrBlank()
}

private fun RetailerProfileResponseDto?.hasStoreDirection(): Boolean {
    val direction = this?.storeDirections ?: return false
    return !direction.street.isNullOrBlank() ||
            !direction.zipCode.isNullOrBlank() ||
            direction.city != null ||
            direction.province != null ||
            direction.country != null
}

@Composable
private fun StoreDirectionCard(
    modifier: Modifier = Modifier,
    profile: RetailerProfileResponseDto?,
    isLoading: Boolean,
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
            direction?.street?.takeIf { it.isNotBlank() }?.let {
                InfoRow(Modifier.placeholderWithShimmer(isLoading), Icons.Outlined.Streetview, stringResource(SharedRes.string.address_street), it)
            }
            direction?.zipCode?.takeIf { it.isNotBlank() }?.let {
                InfoRow(Modifier.placeholderWithShimmer(isLoading), Icons.Outlined.Pin, stringResource(SharedRes.string.address_postal_code), it)
            }
            cityName?.let {
                InfoRow(Modifier.placeholderWithShimmer(isLoading), Icons.Outlined.LocationCity, stringResource(SharedRes.string.address_city), it)
            }
            provinceName?.let {
                InfoRow(Modifier.placeholderWithShimmer(isLoading), Icons.Outlined.Map, stringResource(SharedRes.string.address_state_or_province), it)
            }
            countryName?.let {
                InfoRow(Modifier.placeholderWithShimmer(isLoading), Icons.Outlined.Public, stringResource(SharedRes.string.address_country), it)
            }
        }
    }
}

@Composable
private fun RetailerLogo(
    modifier: Modifier = Modifier,
    userId: String?,
    fileId: String?,
    size: Dp,
    cornerRadius: Dp,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(cornerRadius)),
        contentAlignment = Alignment.Center,
    ) {
        userId?.let {
            SharedAsyncImage(
                model = fileId?.let { file -> ApiConfig.FilePath.userImage(it, file) },
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(onClick = onClick),
                contentDescription = stringResource(StandardRes.string.retailer_profile),
                contentScale = ContentScale.Crop,
                zoomable = false,
                enableContextMenu = false,
            )
        }
    }
}

@Composable
private fun RetailerChip(
    modifier: Modifier = Modifier,
    text: String,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraSmall,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun InfoRow(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
) {
    Row(
        modifier = modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(icon, contentDescription = icon.name, tint = MaterialTheme.colorScheme.primary)
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(text = value, style = MaterialTheme.typography.bodyMedium, overflow = TextOverflow.Ellipsis)
        }
    }
}
