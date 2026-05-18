package org.dsqrwym.enterprise.ui.screens.employees

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import maian.enterprise.generated.resources.EnterpriseRes
import maian.enterprise.generated.resources.employee_edit_title
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.update
import org.dsqrwym.business.ui.components.row.BusinessTitleIconRow
import org.dsqrwym.enterprise.data.employee.EmployeeRole
import org.dsqrwym.enterprise.data.employee.EmployeeStatus
import org.dsqrwym.enterprise.ui.components.employees.EmployeeAccountCard
import org.dsqrwym.enterprise.ui.components.employees.EmployeeContactCard
import org.dsqrwym.enterprise.ui.viewmodels.employees.EmployeeEditViewModel
import org.dsqrwym.shared.ui.components.containers.UiState
import org.dsqrwym.shared.ui.components.scaffold.SharedTransparentScaffold
import org.dsqrwym.shared.ui.components.scaffold.SharedTransparentScaffoldFabButtonState
import org.dsqrwym.shared.util.lazygrid.SharedLazyGridLayout
import org.dsqrwym.shared.util.modifier.paddingWithoutTop
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeEditScreen(
    id: String,
    email: String?,
    role: EmployeeRole?,
    status: EmployeeStatus?,
    onNavigateBack: () -> Unit = {},
    viewModel: EmployeeEditViewModel = koinViewModel(),
) {
    LaunchedEffect(id) {
        viewModel.initWithEmployee(id, email, role, status)
        viewModel.navigateEvent.collect {
            onNavigateBack()
        }
    }

    SharedTransparentScaffold(
        onNavigateBack = onNavigateBack,
        showOverlayDialog = false,
        overlayContent = {},
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                BusinessTitleIconRow(
                    stringResource(EnterpriseRes.string.employee_edit_title),
                    Icons.Outlined.Groups,
                    viewModel.email.ifBlank { stringResource(EnterpriseRes.string.employee_edit_title) },
                    viewModel.isLoading,
                )
            }
        },
        fabButtonState = SharedTransparentScaffoldFabButtonState(
            buttonState = viewModel.updateButtonState,
            buttonEnabled = viewModel.updateButtonEnabled.value,
            onButtonClick = viewModel::updateEmployee,
            buttonText = stringResource(SharedRes.string.update),
            buttonIcon = Icons.Outlined.Save,
            buttonIconDescription = stringResource(SharedRes.string.update),
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
                EmployeeAccountCard(
                    modifier = Modifier.animateItem(),
                    email = viewModel.email,
                    emailError = null,
                    isCheckingEmail = false,
                    emailReadOnly = true,
                    username = viewModel.username,
                    usernameError = viewModel.usernameError,
                    isCheckingUsername = viewModel.isCheckingUsername,
                    role = viewModel.role,
                    status = viewModel.status,
                    enabled = viewModel.updateButtonState == UiState.Idle,
                    isLoading = viewModel.isLoading,
                    onEmailChange = {},
                    onUsernameChange = viewModel::updateUsername,
                    onRoleChange = null,
                )
            }

            item {
                EmployeeContactCard(
                    modifier = Modifier.animateItem(),
                    firstName = viewModel.firstName,
                    lastName = viewModel.lastName,
                    taxId = viewModel.taxId,
                    taxIdError = viewModel.taxIdError,
                    phoneNumberViewModel = viewModel.phoneNumberViewModel,
                    enabled = viewModel.updateButtonState == UiState.Idle,
                    isLoading = viewModel.isLoading,
                    onFirstNameChange = viewModel::updateFirstName,
                    onLastNameChange = viewModel::updateLastName,
                    onTaxIdChange = viewModel::updateTaxId,
                )
            }
        }
    }
}
