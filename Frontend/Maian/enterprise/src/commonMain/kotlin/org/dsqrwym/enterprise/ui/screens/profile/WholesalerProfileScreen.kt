package org.dsqrwym.enterprise.ui.screens.profile

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.profile
import org.dsqrwym.business.ui.components.row.BusinessTitleIconRow
import org.dsqrwym.enterprise.permissions.canEditEnterpriseProfile
import org.dsqrwym.enterprise.ui.viewmodels.profile.WholesalerProfileViewModel
import org.dsqrwym.shared.data.auth.session.AuthSessionViewModel
import org.dsqrwym.shared.data.user.UserRole
import org.dsqrwym.shared.domain.profile.toCardData
import org.dsqrwym.shared.network.ApiConfig
import org.dsqrwym.shared.ui.components.dialog.SharedImageViewDialog
import org.dsqrwym.shared.ui.components.scaffold.SharedTransparentScaffold
import org.dsqrwym.shared.ui.components.wholesaler.SharedWholesalerCard
import org.dsqrwym.shared.ui.components.wholesaler.SharedWholesalerProfileState
import org.dsqrwym.shared.ui.components.wholesaler.sharedWholesalerProfileDetails
import org.dsqrwym.shared.util.lazygrid.SharedLazyGridLayout
import org.dsqrwym.shared.util.modifier.paddingWithoutTop
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WholesalerProfileScreen(
    onNavigateToEdit: () -> Unit,
    userRole: UserRole? = null,
    viewModel: WholesalerProfileViewModel = koinViewModel(),
) {
    var showImageDialog by remember { mutableStateOf(false) }

    val profile = viewModel.profile
    val isLoading = viewModel.isLoading
    val authSessionViewModel = koinViewModel<AuthSessionViewModel>()
    val canEditProfile = userRole?.canEditEnterpriseProfile() == true

    LaunchedEffect(Unit) {
        viewModel.refreshProfile()
    }

    SharedTransparentScaffold(
        title = {
            BusinessTitleIconRow(
                profile?.profile?.companyName ?: "",
                Icons.Outlined.Store,
                stringResource(SharedRes.string.profile),
                isLoading
            )
        },
        showOverlayDialog = showImageDialog && profile?.logoFileId != null,
        overlayContent = {
            if (profile?.logoFileId == null) return@SharedTransparentScaffold
            SharedImageViewDialog(
                model = profile.id?.let {
                    profile.logoFileId?.let { fileId ->
                        ApiConfig.FilePath.userImage(
                            it,
                            fileId
                        )
                    }
                } ?: "",
                onDismissRequest = { showImageDialog = false }
            )
        },
    ) { padding, scrollBehavior ->
        SharedWholesalerProfileState(
            viewModel.uiState,
            onRetry = { viewModel.refreshProfile() }
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
                    SharedWholesalerCard(
                        modifier = Modifier.animateItem(),
                        data = profile?.toCardData(),
                        onImageClick = { showImageDialog = true },
                        onEdit = if (canEditProfile) onNavigateToEdit else null,
                        onLogout = { authSessionViewModel.logout() },
                    )
                }
                sharedWholesalerProfileDetails(
                    profile = profile,
                    isLoading = isLoading,
                )
            }
        }
    }
}

