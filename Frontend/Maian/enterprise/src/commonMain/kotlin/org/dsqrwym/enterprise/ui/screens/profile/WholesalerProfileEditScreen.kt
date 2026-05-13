package org.dsqrwym.enterprise.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.blur.blurEffect
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.rememberHazeState
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import maian.enterprise.generated.resources.EnterpriseRes
import maian.enterprise.generated.resources.field_company_type_placeholder
import maian.enterprise.generated.resources.upload_failed_retry
import maian.enterprise.generated.resources.upload_progress
import maian.shared.generated.resources.*
import org.dsqrwym.enterprise.ui.viewmodels.profile.WholesalerProfileEditViewModel
import org.dsqrwym.shared.data.local.SharedUserPayloadStorage
import org.dsqrwym.shared.data.user.SpanishCompanyType
import org.dsqrwym.shared.data.user.toStringResource
import org.dsqrwym.shared.network.ApiConfig
import org.dsqrwym.shared.theme.MyHazeStyles
import org.dsqrwym.shared.ui.components.buttons.SharedCloseButton
import org.dsqrwym.shared.ui.components.cards.FormCard
import org.dsqrwym.shared.ui.components.containers.UiState
import org.dsqrwym.shared.ui.components.input.outlinedfields.MyOutlinedDoubleField
import org.dsqrwym.shared.ui.components.input.outlinedfields.MyOutlinedTextField
import org.dsqrwym.shared.ui.components.input.outlinedfields.OutlinedPhoneNumberField
import org.dsqrwym.shared.ui.components.input.selector.Selector
import org.dsqrwym.shared.ui.components.input.selector.SelectorConfig
import org.dsqrwym.shared.ui.components.progressindicators.CheckingTrailingIcon
import org.dsqrwym.shared.ui.components.scaffold.SharedTransparentScaffold
import org.dsqrwym.shared.ui.components.scaffold.SharedTransparentScaffoldFabButtonState
import org.dsqrwym.shared.ui.media.SharedAsyncImage
import org.dsqrwym.shared.util.colum.SharedColumnLayout
import org.dsqrwym.shared.util.formatter.asString
import org.dsqrwym.shared.util.lazygrid.SharedLazyGridLayout
import org.dsqrwym.shared.util.modifier.paddingWithoutTop
import org.dsqrwym.shared.util.modifier.placeholderWithShimmer
import org.dsqrwym.shared.util.row.SharedRowLayout
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WholesalerProfileEditScreen(
    onNavigateBack: () -> Unit,
    viewModel: WholesalerProfileEditViewModel = koinViewModel(),
) {
    LaunchedEffect(Unit) {
        viewModel.loadProfile()
        viewModel.navigateEvent.collect { _ ->
            onNavigateBack()
        }
    }

    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val companyName = viewModel.companyName
    val phoneNumberViewModel = viewModel.phoneNumberViewModel
    val minimumOrderAmount = viewModel.minimumOrderAmount
    val isLoading = viewModel.isLoading

    val imagePicker = rememberFilePickerLauncher(
        type = FileKitType.Image,
        mode = FileKitMode.Single,
    ) { file: PlatformFile? ->
        file?.let { viewModel.selectLogoFile(it) }
    }

    SharedTransparentScaffold(
        title = {
            Text(
                text = stringResource(SharedRes.string.edit_profile),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        onNavigateBack = onNavigateBack,
        showOverlayDialog = false,
        overlayContent = {

        },
        fabButtonState = SharedTransparentScaffoldFabButtonState(
            buttonState = viewModel.saveUiState,
            buttonEnabled = viewModel.saveButtonEnabled,
            onButtonClick = {
                focusManager.clearFocus()
                viewModel.saveProfile()
            },
            buttonText = stringResource(SharedRes.string.save),
            buttonIcon = Icons.Outlined.Save,
            buttonIconDescription = stringResource(SharedRes.string.save),
        ),
    ) { padding, scrollBehavior ->

        LazyVerticalStaggeredGrid(
            modifier = Modifier
                .fillMaxSize()
                .paddingWithoutTop(padding)
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            columns = StaggeredGridCells.Adaptive(minSize = 399.9.dp),
            contentPadding = PaddingValues(SharedLazyGridLayout.Padding),
            horizontalArrangement = SharedLazyGridLayout.arrangement,
        ) {
            item(span = StaggeredGridItemSpan.FullLine) {
                Spacer(Modifier.height(padding.calculateTopPadding()))
            }

            item {
                FormCard(
                    modifier = Modifier.animateItem(),
                    title = stringResource(SharedRes.string.company_logo),
                    uiState = viewModel.uploadImage,
                ) { enabled ->
                    LogoPickerContent(
                        modifier = Modifier.placeholderWithShimmer(isLoading),
                        logoFileId = viewModel.logoFileId,
                        loadImageUiState = viewModel.uploadImage,
                        progress = viewModel.uploadImageProgress,
                        pendingLogoFile = viewModel.pendingLogoFile,
                        enabled = enabled,
                        onPick = imagePicker::launch,
                        onRemove = viewModel::removeLogo,
                        retry = viewModel::saveProfile,
                    )
                }
            }

            item {
                FormCard(
                    modifier = Modifier.animateItem(),
                    title = stringResource(SharedRes.string.company_info),
                    verticalArrangement = SharedColumnLayout.arrangement,
                    uiState = if (viewModel.companyNameError == null) UiState.Loading else UiState.Error,
                ) { enabled ->
                    MyOutlinedTextField(
                        modifier = Modifier.placeholderWithShimmer(isLoading),
                        enabled = enabled,
                        value = companyName,
                        placeholderText = stringResource(EnterpriseRes.string.field_company_type_placeholder),
                        onValueChange = viewModel::updateCompanyName,
                        labelText = stringResource(SharedRes.string.company_name),
                        error = viewModel.companyNameError.asString(),
                        leadingIcon = Icons.Outlined.Business,
                        imeAction = ImeAction.Next,
                        onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
                    )

                    val companyTypeLabels =
                        SpanishCompanyType.entries.associateWith { stringResource(it.toStringResource()) }
                    Selector(
                        items = SpanishCompanyType.entries,
                        itemToString = { companyTypeLabels[it].orEmpty() },
                        selectedItem = viewModel.selectedCompanyType,
                        onItemSelected = { it?.let { type -> viewModel.selectCompanyType(type) } },
                        config = SelectorConfig(
                            modifier = Modifier.placeholderWithShimmer(isLoading),
                            label = stringResource(SharedRes.string.company_type),
                            leadingIcon = Icons.Outlined.Category,
                        ),
                    )

                    MyOutlinedTextField(
                        modifier = Modifier.placeholderWithShimmer(isLoading),
                        enabled = enabled,
                        value = viewModel.displayName,
                        onValueChange = viewModel::updateDisplayName,
                        labelText = stringResource(SharedRes.string.display_name),
                        placeholderText = stringResource(SharedRes.string.display_name_placeholder),
                        leadingIcon = Icons.Outlined.Storefront,
                        imeAction = ImeAction.Next,
                        onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
                    )

                    MyOutlinedTextField(
                        modifier = Modifier.placeholderWithShimmer(isLoading),
                        enabled = enabled,
                        value = viewModel.description,
                        placeholderText = stringResource(SharedRes.string.description_placeholder),
                        onValueChange = viewModel::updateDescription,
                        labelText = stringResource(SharedRes.string.description),
                        leadingIcon = Icons.Outlined.Description,
                        singleLine = false,
                        imeAction = ImeAction.Next,
                        onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
                    )
                }
            }

            item {
                FormCard(
                    modifier = Modifier.animateItem(),
                    title = stringResource(SharedRes.string.contact_info),
                    uiState = if (viewModel.usernameError == null && phoneNumberViewModel.errorMessage == null && viewModel.taxIdError == null) UiState.Loading else UiState.Error,
                    verticalArrangement = SharedColumnLayout.arrangement,
                ) { enabled ->
                    MyOutlinedTextField(
                        modifier = Modifier.placeholderWithShimmer(isLoading),
                        enabled = enabled,
                        value = viewModel.firstName,
                        placeholderText = stringResource(SharedRes.string.first_name_placeholder),
                        onValueChange = viewModel::updateFirstName,
                        labelText = stringResource(SharedRes.string.first_name),
                        leadingIcon = Icons.Outlined.Person,
                        semanticsPropertyReceiver = {
                            contentType = ContentType.PersonFirstName
                        },
                        imeAction = ImeAction.Next,
                        onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
                    )

                    MyOutlinedTextField(
                        modifier = Modifier.placeholderWithShimmer(isLoading),
                        enabled = enabled,
                        value = viewModel.lastName,
                        placeholderText = stringResource(SharedRes.string.last_name_placeholder),
                        onValueChange = viewModel::updateLastName,
                        labelText = stringResource(SharedRes.string.last_name),
                        leadingIcon = Icons.Outlined.Person,
                        semanticsPropertyReceiver = {
                            contentType = ContentType.PersonLastName
                        },
                        imeAction = ImeAction.Next,
                        onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
                    )

                    MyOutlinedTextField(
                        modifier = Modifier.placeholderWithShimmer(isLoading),
                        enabled = enabled,
                        value = viewModel.username,
                        onValueChange = viewModel::updateUsername,
                        labelText = stringResource(SharedRes.string.field_username_label),
                        placeholderText = stringResource(SharedRes.string.field_username_placeholder),
                        leadingIcon = Icons.Rounded.Person,
                        leadingIconContentDescription = stringResource(SharedRes.string.icon_content_description_person),
                        trailingIcon = {
                            CheckingTrailingIcon(viewModel.isCheckingUsername)
                        },
                        error = viewModel.usernameError?.asString(),
                        imeAction = ImeAction.Next,
                        onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
                        semanticsPropertyReceiver = {
                            contentType = ContentType.NewUsername
                        }
                    )

                    OutlinedPhoneNumberField(
                        modifier = Modifier.placeholderWithShimmer(isLoading),
                        phoneNumberViewModel = phoneNumberViewModel,
                        enabled = enabled,
                        imeAction = ImeAction.Next,
                        onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
                    )

                    MyOutlinedTextField(
                        modifier = Modifier.placeholderWithShimmer(isLoading),
                        enabled = enabled,
                        value = viewModel.taxId,
                        onValueChange = viewModel::updateTaxId,
                        labelText = stringResource(SharedRes.string.tax_id),
                        placeholderText = stringResource(SharedRes.string.tax_id_placeholder),
                        leadingIcon = Icons.Outlined.Numbers,
                        leadingIconContentDescription = stringResource(SharedRes.string.tax_id),
                        trailingIcon = {
                            CheckingTrailingIcon(viewModel.isCheckingTaxId)
                        },
                        error = viewModel.taxIdError?.asString(),
                        imeAction = ImeAction.Next,
                        onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
                    )
                }
            }

            item {
                FormCard(
                    modifier = Modifier.animateItem(),
                    title = stringResource(SharedRes.string.business_settings),
                    uiState = UiState.Loading,
                    verticalArrangement = SharedColumnLayout.arrangement,
                ) { enabled ->
                    MyOutlinedDoubleField(
                        modifier = Modifier.placeholderWithShimmer(isLoading),
                        enabled = enabled,
                        value = minimumOrderAmount,
                        onValueChange = { it?.let { viewModel.updateMinimumOrderAmount(it) } },
                        labelText = stringResource(SharedRes.string.minimum_order_amount),
                        placeholderText = stringResource(SharedRes.string.minimum_order_amount_placeholder),
                        trailingIcon = {
                            if (minimumOrderAmount.toDoubleOrNull() != 0.0) {
                                SharedCloseButton {
                                    viewModel.updateMinimumOrderAmount("0.0")
                                }
                            }
                        },
                        leadingIcon = Icons.Outlined.ShoppingCart,
                        min = 0.0,
                        max = WholesalerProfileEditViewModel.MAX_MINIMUM_ORDER_AMOUNT,
                        imeAction = ImeAction.Next,
                        onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
                    )

                    MyOutlinedTextField(
                        modifier = Modifier.placeholderWithShimmer(isLoading),
                        enabled = enabled,
                        value = viewModel.deliveryAreaDescription,
                        placeholderText = stringResource(SharedRes.string.delivery_area_placeholder),
                        onValueChange = viewModel::updateDeliveryAreaDescription,
                        labelText = stringResource(SharedRes.string.delivery_area),
                        leadingIcon = Icons.Outlined.Place,
                        singleLine = false,
                        imeAction = ImeAction.Next,
                        onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
                    )

                    FlowRow(
                        horizontalArrangement = SharedRowLayout.arrangement,
                        verticalArrangement = SharedColumnLayout.arrangement
                    ) {
                        Row(
                            modifier = Modifier,
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = SharedRowLayout.arrangement,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.LocalShipping,
                                contentDescription = Icons.Outlined.LocalShipping.name,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Text(stringResource(SharedRes.string.delivery_available))
                            Switch(
                                modifier = Modifier.placeholderWithShimmer(isLoading),
                                checked = viewModel.deliveryAvailable,
                                onCheckedChange = { viewModel.toggleDeliveryAvailable() },
                            )
                        }
                        Spacer(Modifier.width(16.dp))
                        Row(
                            modifier = Modifier,
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = SharedRowLayout.arrangement,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Storefront,
                                contentDescription = Icons.Outlined.Storefront.name,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Text(stringResource(SharedRes.string.pickup_available))
                            Switch(
                                modifier = Modifier.placeholderWithShimmer(isLoading),
                                checked = viewModel.pickupAvailable,
                                onCheckedChange = { viewModel.togglePickupAvailable() },
                            )
                        }
                    }
                }
            }

            item(span = StaggeredGridItemSpan.FullLine) {
                Spacer(Modifier.height(28.dp))
            }
        }
    }
}

@Composable
private fun LogoPickerContent(
    modifier: Modifier = Modifier,
    logoFileId: String? = null,
    loadImageUiState: UiState = UiState.Idle,
    pendingLogoFile: PlatformFile? = null,
    progress: Float = 0f,
    enabled: Boolean = true,
    onPick: () -> Unit,
    onRemove: () -> Unit,
    retry: () -> Unit,
) {
    val hazeState = rememberHazeState()
    val hazeStyle = MyHazeStyles.glass()
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = SharedColumnLayout.arrangement
    ) {
        Box(
            modifier = Modifier
                .size(220.dp)
                .clip(MaterialTheme.shapes.medium),
            contentAlignment = Alignment.Center,
        ) {
            when {
                pendingLogoFile != null -> {
                    SharedAsyncImage(
                        model = pendingLogoFile,
                        modifier = Modifier.fillMaxSize(),
                        contentDescription = stringResource(SharedRes.string.company_logo),
                        contentScale = ContentScale.Crop,
                        zoomable = false,
                    )
                }

                logoFileId != null -> {
                    SharedAsyncImage(
                        model = ApiConfig.FilePath.userImage(SharedUserPayloadStorage.get()?.userId ?: "", logoFileId),
                        modifier = Modifier.fillMaxSize(),
                        contentDescription = stringResource(SharedRes.string.company_logo),
                        contentScale = ContentScale.Crop,
                        zoomable = false,
                    )
                }
                // No logo
                else -> {
                    Icon(
                        imageVector = Icons.Outlined.AddAPhoto,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                    )
                }
            }
            if (loadImageUiState != UiState.Success && loadImageUiState != UiState.Idle) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .pointerInput(Unit) {} // 阻止点击穿透
                        .hazeEffect(hazeState) {
                            blurEffect {
                                style =  hazeStyle
                                progressive = dev.chrisbanes.haze.blur.HazeProgressive.RadialGradient(
                                    radiusIntensity = 0.6f
                                )
                            }
                        }
                ) {
                    when (loadImageUiState) {
                        UiState.Loading -> {
                            Column(
                                modifier = Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier.size(32.dp),
                                )
                                Text(
                                    text = stringResource(
                                        EnterpriseRes.string.upload_progress,
                                        (progress * 100).toInt().coerceIn(0, 100)
                                    ),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }

                        UiState.Error -> {
                            TextButton(retry, Modifier.align(Alignment.Center)) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Info,
                                        contentDescription = stringResource(SharedRes.string.status_error_content_description),
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        text = stringResource(EnterpriseRes.string.upload_failed_retry),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Action buttons
        Row(horizontalArrangement = SharedRowLayout.arrangement) {
            FilledTonalButton(onClick = onPick, enabled = enabled) {
                Icon(
                    Icons.Outlined.AddAPhoto,
                    contentDescription = stringResource(SharedRes.string.select_logo)
                )
                Text(stringResource(SharedRes.string.select_logo))
            }

            if (pendingLogoFile != null || logoFileId != null) {
                OutlinedButton(onClick = onRemove, enabled = enabled) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = stringResource(SharedRes.string.delete_logo)
                    )
                    Text(stringResource(SharedRes.string.delete_logo))
                }
            }
        }
    }
}
