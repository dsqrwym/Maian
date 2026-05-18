package org.dsqrwym.enterprise.ui.viewmodels.employees

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import maian.enterprise.generated.resources.EnterpriseRes
import maian.enterprise.generated.resources.employee_create_success_pending_verification
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.create_failed
import org.dsqrwym.enterprise.data.employee.EmployeeRepository
import org.dsqrwym.enterprise.data.employee.EmployeeRole
import org.dsqrwym.enterprise.data.employee.dto.CreateEmployeeDto
import org.dsqrwym.enterprise.data.profile.WholesalerProfileRepository
import org.dsqrwym.enterprise.navigation.Employees
import org.dsqrwym.shared.data.auth.SharedAuthRepository
import org.dsqrwym.shared.navigation.core.NavigationEvent
import org.dsqrwym.shared.navigation.core.SharedNavigable
import org.dsqrwym.shared.navigation.core.SharedNavigableDelegate
import org.dsqrwym.shared.network.model.SharedResponseResult
import org.dsqrwym.shared.ui.components.containers.UiState
import org.dsqrwym.shared.ui.viewmodels.MySnackbarViewModel
import org.dsqrwym.shared.ui.viewmodels.phone.SharedPhoneNumberViewModel
import org.dsqrwym.shared.util.timing.SharedUiTiming
import org.jetbrains.compose.resources.getString

class EmployeeCreateViewModel(
    private val repository: EmployeeRepository,
    authRepository: SharedAuthRepository,
    wholesalerProfileRepository: WholesalerProfileRepository,
    phoneNumberViewModel: SharedPhoneNumberViewModel,
    mySnackbarViewModel: MySnackbarViewModel,
) : BaseEmployeeFormViewModel(
    authRepository,
    wholesalerProfileRepository,
    phoneNumberViewModel,
    mySnackbarViewModel,
), SharedNavigable by SharedNavigableDelegate() {
    var selectedRole by mutableStateOf(EmployeeRole.WAREHOUSE)
        private set
    var createButtonState by mutableStateOf(UiState.Idle)
        private set

    val createButtonEnabled = derivedStateOf {
        createButtonState == UiState.Idle &&
                isValidEmail() &&
                isValidUsername() &&
                isValidTaxId() &&
                isValidPhoneNumber()
    }

    fun updateRole(role: EmployeeRole) {
        selectedRole = role
    }

    fun createEmployee() {
        if (!createButtonEnabled.value) return
        viewModelScope.launch {
            createButtonState = UiState.Loading
            val dto = CreateEmployeeDto(
                email = email,
                firstName = optional(firstName),
                lastName = optional(lastName),
                username = optional(username),
                telephone = optionalTelephone(),
                taxId = optional(taxId),
            )

            when (val result = repository.createEmployee(selectedRole, dto)) {
                is SharedResponseResult.Success -> {
                    createButtonState = UiState.Success
                    mySnackbarViewModel.showSuccess(
                        getString(EnterpriseRes.string.employee_create_success_pending_verification)
                    )
                    emitNavigation(NavigationEvent.ToRoute(Employees))
                }

                is SharedResponseResult.Error -> {
                    createButtonState = UiState.Error
                    if (SharedResponseResult.shouldShowToUser(result.type)) {
                        result.message?.let { mySnackbarViewModel.showError(it) }
                    } else {
                        mySnackbarViewModel.showError(getString(SharedRes.string.create_failed))
                    }
                }
            }
            delay(SharedUiTiming.formStateResetDelay)
            createButtonState = UiState.Idle
        }
    }
}
