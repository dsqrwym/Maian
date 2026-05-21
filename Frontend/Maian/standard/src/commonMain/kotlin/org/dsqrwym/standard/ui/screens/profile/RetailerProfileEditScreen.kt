package org.dsqrwym.standard.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Numbers
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.company_info
import maian.shared.generated.resources.company_logo
import maian.shared.generated.resources.company_name
import maian.shared.generated.resources.company_type
import maian.shared.generated.resources.contact_info
import maian.shared.generated.resources.delete_logo
import maian.shared.generated.resources.display_name
import maian.shared.generated.resources.display_name_placeholder
import maian.shared.generated.resources.edit_profile
import maian.shared.generated.resources.field_cannot_be_empty
import maian.shared.generated.resources.field_username_label
import maian.shared.generated.resources.field_username_placeholder
import maian.shared.generated.resources.first_name
import maian.shared.generated.resources.first_name_placeholder
import maian.shared.generated.resources.icon_content_description_person
import maian.shared.generated.resources.last_name
import maian.shared.generated.resources.last_name_placeholder
import maian.shared.generated.resources.save
import maian.shared.generated.resources.select_logo
import maian.shared.generated.resources.status_error_content_description
import maian.shared.generated.resources.store_address
import maian.shared.generated.resources.tax_id
import maian.shared.generated.resources.tax_id_placeholder
import maian.standard.generated.resources.StandardRes
import maian.standard.generated.resources.company_type_placeholder
import maian.standard.generated.resources.contact_name
import maian.standard.generated.resources.contact_name_placeholder
import maian.standard.generated.resources.upload_failed_retry
import maian.standard.generated.resources.upload_progress
import org.dsqrwym.shared.data.local.SharedUserPayloadStorage
import org.dsqrwym.shared.data.user.SpanishCompanyType
import org.dsqrwym.shared.data.user.toStringResource
import org.dsqrwym.shared.network.ApiConfig
import org.dsqrwym.shared.ui.components.cards.FormCard
import org.dsqrwym.shared.ui.components.containers.UiState
import org.dsqrwym.shared.ui.components.input.outlinedfields.MyOutlinedTextField
import org.dsqrwym.shared.ui.components.input.outlinedfields.OutlinedPhoneNumberField
import org.dsqrwym.shared.ui.components.input.selector.Selector
import org.dsqrwym.shared.ui.components.input.selector.SelectorConfig
import org.dsqrwym.shared.ui.components.location.SharedAddressInputSection
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
import org.dsqrwym.standard.ui.viewmodels.profile.RetailerProfileEditViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RetailerProfileEditScreen(
    onNavigateBack: () -> Unit,
    viewModel: RetailerProfileEditViewModel = koinViewModel(),
) {
    val focusManager = LocalFocusManager.current
    val phoneNumberViewModel = viewModel.phoneNumberViewModel
    val isLoading = viewModel.isLoading

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    LaunchedEffect(viewModel) {
        viewModel.navigateEvent.collect {
            onNavigateBack()
        }
    }

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
        overlayContent = {},
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
                    uiState = UiState.Idle,
                ) { enabled ->
                    MyOutlinedTextField(
                        modifier = Modifier.placeholderWithShimmer(isLoading),
                        enabled = enabled,
                        value = viewModel.companyName,
                        placeholderText = stringResource(SharedRes.string.company_name),
                        onValueChange = viewModel::updateCompanyName,
                        labelText = stringResource(SharedRes.string.company_name),
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
                        onItemSelected = viewModel::selectCompanyType,
                        config = SelectorConfig(
                            modifier = Modifier.placeholderWithShimmer(isLoading),
                            label = stringResource(SharedRes.string.company_type),
                            placeholder = stringResource(StandardRes.string.company_type_placeholder),
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
                        value = viewModel.contactName,
                        onValueChange = viewModel::updateContactName,
                        labelText = stringResource(StandardRes.string.contact_name),
                        placeholderText = stringResource(StandardRes.string.contact_name_placeholder),
                        leadingIcon = Icons.Outlined.Badge,
                        imeAction = ImeAction.Next,
                        onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
                    )
                }
            }

            item {
                FormCard(
                    modifier = Modifier.animateItem(),
                    title = stringResource(SharedRes.string.contact_info),
                    uiState = if (
                        viewModel.usernameError == null &&
                        phoneNumberViewModel.errorMessage == null &&
                        viewModel.taxIdError == null
                    ) UiState.Idle else UiState.Error,
                    verticalArrangement = SharedColumnLayout.arrangement,
                ) { enabled ->
                    MyOutlinedTextField(
                        modifier = Modifier.placeholderWithShimmer(isLoading),
                        enabled = enabled,
                        value = viewModel.firstName,
                        placeholderText = stringResource(SharedRes.string.first_name_placeholder),
                        onValueChange = viewModel::updateFirstName,
                        labelText = stringResource(SharedRes.string.first_name),
                        leadingIcon = Icons.Rounded.Person,
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
                        leadingIcon = Icons.Rounded.Person,
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
                        labelText = "${stringResource(SharedRes.string.field_username_label)} (${stringResource(SharedRes.string.field_cannot_be_empty)})",
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
                        },
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
                        imeAction = ImeAction.Done,
                        onImeAction = {
                            focusManager.clearFocus()
                            viewModel.saveProfile()
                        },
                    )
                }
            }

            item {
                FormCard(
                    modifier = Modifier.animateItem(),
                    title = stringResource(SharedRes.string.store_address),
                    uiState = if (viewModel.isAddressValidForSave()) UiState.Idle else UiState.Error,
                    verticalArrangement = SharedColumnLayout.arrangement,
                ) { enabled ->
                    SharedAddressInputSection(
                        addressFormState = viewModel.addressFormState,
                        enabled = enabled,
                        focusManager = focusManager,
                        fieldModifier = Modifier.placeholderWithShimmer(isLoading),
                        onDone = {
                            focusManager.clearFocus()
                            viewModel.saveProfile()
                        },
                    )
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
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = SharedColumnLayout.arrangement,
    ) {
        Box(
            modifier = Modifier
                .size(220.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceVariant),
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

                else -> {
                    Icon(
                        imageVector = Icons.Outlined.AddAPhoto,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                    )
                }
            }

            when (loadImageUiState) {
                UiState.Loading -> {
                    Column(
                        modifier = Modifier
                            .matchParentSize()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator(progress = { progress }, modifier = Modifier.size(32.dp))
                        Text(
                            text = stringResource(
                                StandardRes.string.upload_progress,
                                (progress * 100).toInt().coerceIn(0, 100),
                            ),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }

                UiState.Error -> {
                    TextButton(
                        onClick = retry,
                        modifier = Modifier
                            .matchParentSize()
                            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.82f)),
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = stringResource(SharedRes.string.status_error_content_description),
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp),
                            )
                            Text(
                                text = stringResource(StandardRes.string.upload_failed_retry),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                            )
                        }
                    }
                }

                else -> {}
            }
        }

        FlowRow(
            horizontalArrangement = SharedRowLayout.arrangement,
            verticalArrangement = SharedColumnLayout.arrangement,
        ) {
            FilledTonalButton(onClick = onPick, enabled = enabled) {
                Icon(Icons.Outlined.AddAPhoto, contentDescription = stringResource(SharedRes.string.select_logo))
                Text(stringResource(SharedRes.string.select_logo))
            }

            if (pendingLogoFile != null || logoFileId != null) {
                OutlinedButton(onClick = onRemove, enabled = enabled) {
                    Icon(Icons.Outlined.Delete, contentDescription = stringResource(SharedRes.string.delete_logo))
                    Text(stringResource(SharedRes.string.delete_logo))
                }
            }
        }
    }
}
