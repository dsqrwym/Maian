package org.dsqrwym.standard.ui.screens.browse.wholesaler

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.wholesalers
import org.dsqrwym.shared.data.profile.WholesalerProfileResponseDto
import org.dsqrwym.shared.domain.profile.toCardData
import org.dsqrwym.shared.network.ApiConfig
import org.dsqrwym.shared.ui.components.dialog.SharedImageViewDialog
import org.dsqrwym.shared.ui.components.scaffold.SharedTransparentScaffold
import org.dsqrwym.shared.ui.components.wholesaler.SharedWholesalerCard
import org.dsqrwym.shared.ui.components.wholesaler.SharedWholesalerProfileState
import org.dsqrwym.shared.ui.components.wholesaler.sharedWholesalerProfileDetails
import org.dsqrwym.shared.util.lazygrid.SharedLazyGridLayout
import org.dsqrwym.shared.util.modifier.paddingWithoutTop
import org.dsqrwym.standard.ui.viewmodels.browse.WholesalerProfileViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WholesalerProfileScreen(
    wholesalerId: String,
    onNavigateBack: () -> Unit,
    onProfileLoaded: (WholesalerProfileResponseDto) -> Unit = {},
    viewModel: WholesalerProfileViewModel = koinViewModel(),
) {
    var showImageDialog by remember { mutableStateOf(false) }
    val profile = viewModel.profile
    val cardData = profile?.toCardData()

    LaunchedEffect(wholesalerId) {
        viewModel.loadProfile(wholesalerId)
    }
    LaunchedEffect(profile?.id) {
        profile?.let(onProfileLoaded)
    }

    SharedTransparentScaffold(
        onNavigateBack = onNavigateBack,
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
                onDismissRequest = { showImageDialog = false },
            )
        },
        title = {
            Text(
                text = profile?.profile?.displayName
                    ?: profile?.profile?.companyName
                    ?: stringResource(SharedRes.string.wholesalers),
                maxLines = 1,
            )
        },
    ) { padding, scrollBehavior ->
        SharedWholesalerProfileState(
            uiState = viewModel.uiState,
            onRetry = { viewModel.loadProfile(wholesalerId) },
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
                        data = cardData,
                        onImageClick = cardData?.logoFileId?.let { { showImageDialog = true } },
                    )
                }
                sharedWholesalerProfileDetails(
                    profile = profile,
                    isLoading = viewModel.isLoading,
                )
            }
        }
    }
}
