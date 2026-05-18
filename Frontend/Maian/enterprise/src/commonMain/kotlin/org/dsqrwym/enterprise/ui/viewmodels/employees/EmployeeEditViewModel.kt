package org.dsqrwym.enterprise.ui.viewmodels.employees

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.update_failed
import maian.shared.generated.resources.update_success
import org.dsqrwym.enterprise.data.employee.EmployeeRepository
import org.dsqrwym.enterprise.data.employee.EmployeeRole
import org.dsqrwym.enterprise.data.employee.EmployeeStatus
import org.dsqrwym.enterprise.data.employee.dto.EmployeeForUpdateResponse
import org.dsqrwym.enterprise.data.employee.dto.UpdateEmployeeDto
import org.dsqrwym.enterprise.data.profile.WholesalerProfileRepository
import org.dsqrwym.enterprise.navigation.Employees
import org.dsqrwym.shared.data.auth.SharedAuthRepository
import org.dsqrwym.shared.navigation.core.NavigationEvent
import org.dsqrwym.shared.navigation.core.SharedNavigable
import org.dsqrwym.shared.navigation.core.SharedNavigableDelegate
import org.dsqrwym.shared.network.model.SharedResponseResult
import org.dsqrwym.shared.serialization.OptionalField
import org.dsqrwym.shared.ui.components.containers.UiState
import org.dsqrwym.shared.ui.viewmodels.MySnackbarViewModel
import org.dsqrwym.shared.ui.viewmodels.phone.SharedPhoneNumberViewModel
import org.dsqrwym.shared.util.timing.SharedUiTiming
import org.jetbrains.compose.resources.getString

class EmployeeEditViewModel(
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
    var isLoading by mutableStateOf(false)
        private set
    var updateButtonState by mutableStateOf(UiState.Idle)
        private set
    var role by mutableStateOf<EmployeeRole?>(null)
        private set
    var status by mutableStateOf<EmployeeStatus?>(null)
        private set

    private var employeeId: String? = null
    private var initialEmployee: EmployeeForUpdateResponse? = null
    private var initialUsername = ""
    private var initialTelephone = ""

    val updateButtonEnabled = derivedStateOf {
        !isLoading &&
                updateButtonState == UiState.Idle &&
                isValidUsername() &&
                isValidTaxId() &&
                isValidPhoneNumber() &&
                hasWritablePendingChanges()
    }

    fun initWithEmployee(
        id: String,
        routeEmail: String?,
        routeRole: EmployeeRole?,
        routeStatus: EmployeeStatus?,
    ) {
        if (employeeId == id && initialEmployee != null) return
        reset()
        employeeId = id
        email = routeEmail.orEmpty()
        role = routeRole
        status = routeStatus
        usernameAvailabilitySkipUserId = id.takeIf { it.isUuidLike() }

        viewModelScope.launch {
            isLoading = true
            when (val result = repository.getEmployeeForUpdate(id)) {
                is SharedResponseResult.Success -> {
                    val data = result.data ?: EmployeeForUpdateResponse()
                    initialEmployee = data
                    email = data.email ?: routeEmail.orEmpty()
                    firstName = data.firstName.orEmpty()
                    lastName = data.lastName.orEmpty()
                    username = employeeUsernameForForm(data.username)
                    initialUsername = username
                    usernameAvailabilityExemptValue = username
                    phoneNumberViewModel.resetPhoneNumberViewModel()
                    data.telephone?.takeIf { it.isNotBlank() }?.let {
                        phoneNumberViewModel.updatePhoneNumber(it)
                        initialTelephone = it
                    }
                    taxId = data.taxId.orEmpty()
                    role = data.role ?: routeRole
                    status = data.status ?: routeStatus
                    usernameError = null
                    usernameExists = false
                    taxIdError = null
                }

                is SharedResponseResult.Error -> {
                    if (SharedResponseResult.shouldShowToUser(result.type)) {
                        result.message?.let { mySnackbarViewModel.showError(it) }
                    }
                    emitNavigation(NavigationEvent.ToRoute(Employees))
                }
            }
            isLoading = false
        }
    }

    fun updateEmployee() {
        val id = employeeId ?: return
        if (!updateButtonEnabled.value) return
        viewModelScope.launch {
            updateButtonState = UiState.Loading
            when (val result = repository.updateEmployee(id, buildUpdateDto())) {
                is SharedResponseResult.Success -> {
                    updateButtonState = UiState.Success
                    mySnackbarViewModel.showSuccess(getString(SharedRes.string.update_success))
                    emitNavigation(NavigationEvent.ToRoute(Employees))
                }

                is SharedResponseResult.Error -> {
                    updateButtonState = UiState.Error
                    if (SharedResponseResult.shouldShowToUser(result.type)) {
                        result.message?.let { mySnackbarViewModel.showError(it) }
                    } else {
                        mySnackbarViewModel.showError(getString(SharedRes.string.update_failed))
                    }
                }
            }
            delay(SharedUiTiming.formStateResetDelay)
            updateButtonState = UiState.Idle
        }
    }

    private fun buildUpdateDto(): UpdateEmployeeDto {
        val initial = initialEmployee
        return UpdateEmployeeDto(
            firstName = changedNonBlank(initial?.firstName, firstName),
            lastName = changedNonBlank(initial?.lastName, lastName),
            username = changedNonBlank(initialUsername, username),
            telephone = changedNonBlank(initialTelephone, optionalTelephone().orEmpty()),
            taxId = changedNonBlank(initial?.taxId, taxId.uppercase()),
        )
    }

    private fun hasWritablePendingChanges(): Boolean {
        val dto = buildUpdateDto()
        return dto.firstName is OptionalField.Value ||
                dto.lastName is OptionalField.Value ||
                dto.username is OptionalField.Value ||
                dto.telephone is OptionalField.Value ||
                dto.taxId is OptionalField.Value
    }

    private fun changedNonBlank(old: String?, new: String): OptionalField<String> {
        val normalizedNew = new.trim().takeIf { it.isNotBlank() } ?: return OptionalField.Undefined
        val normalizedOld = old?.trim()?.takeIf { it.isNotBlank() }
        return if (normalizedOld != normalizedNew) {
            OptionalField.Value(normalizedNew)
        } else {
            OptionalField.Undefined
        }
    }

    private fun reset() {
        isLoading = false
        updateButtonState = UiState.Idle
        email = ""
        firstName = ""
        lastName = ""
        username = ""
        usernameError = null
        usernameExists = false
        taxId = ""
        taxIdError = null
        role = null
        status = null
        initialEmployee = null
        initialUsername = ""
        initialTelephone = ""
        phoneNumberViewModel.resetPhoneNumberViewModel()
    }
}

private fun String.isUuidLike(): Boolean =
    Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$").matches(this)
