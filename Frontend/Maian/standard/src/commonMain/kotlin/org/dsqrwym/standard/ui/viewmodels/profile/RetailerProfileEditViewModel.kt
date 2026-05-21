package org.dsqrwym.standard.ui.viewmodels.profile

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.size
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import maian.shared.generated.resources.*
import maian.standard.generated.resources.StandardRes
import maian.standard.generated.resources.media_file_too_large_mb
import org.dsqrwym.shared.data.auth.SharedAuthRepository
import org.dsqrwym.shared.data.file.SharedUploadEvent
import org.dsqrwym.shared.data.file.SharedUploadRepository
import org.dsqrwym.shared.data.location.SharedLocationRepository
import org.dsqrwym.shared.data.local.SharedUserPayloadStorage
import org.dsqrwym.shared.data.profile.RetailerProfileResponseDto
import org.dsqrwym.shared.data.user.SpanishCompanyType
import org.dsqrwym.shared.navigation.core.NavigationEvent
import org.dsqrwym.shared.navigation.core.SharedNavigable
import org.dsqrwym.shared.navigation.core.SharedNavigableDelegate
import org.dsqrwym.shared.network.model.SharedResponseResult
import org.dsqrwym.shared.ui.components.containers.UiState
import org.dsqrwym.shared.ui.viewmodels.MySnackbarViewModel
import org.dsqrwym.shared.ui.viewmodels.address.SharedAddressFormState
import org.dsqrwym.shared.ui.viewmodels.phone.SharedPhoneNumberViewModel
import org.dsqrwym.shared.util.patch.changedField
import org.dsqrwym.shared.util.timing.SharedUiTiming
import org.dsqrwym.shared.util.validation.isValidSpanishTaxId
import org.dsqrwym.shared.util.validation.validateUsername
import org.dsqrwym.standard.data.profile.RetailerProfileRepository
import org.dsqrwym.standard.data.profile.dto.UpdateRetailerProfileDto
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class RetailerProfileEditViewModel(
    private val repository: RetailerProfileRepository,
    private val uploadRepository: SharedUploadRepository,
    val phoneNumberViewModel: SharedPhoneNumberViewModel,
    private val authRepository: SharedAuthRepository,
    locationRepository: SharedLocationRepository,
    private val mySnackbarViewModel: MySnackbarViewModel,
) : ViewModel(), SharedNavigable by SharedNavigableDelegate() {
    companion object {
        const val MAX_LOGO_SIZE: Long = 200 * 1024 * 1024L
    }

    var isLoading by mutableStateOf(false)
        private set
    var saveUiState by mutableStateOf(UiState.Idle)
        private set
    var uploadImage by mutableStateOf(UiState.Idle)
        private set
    var uploadImageProgress by mutableFloatStateOf(0F)
        private set

    val saveButtonEnabled by derivedStateOf {
        !isLoading &&
                saveUiState == UiState.Idle &&
                uploadImage != UiState.Loading &&
                isValidUserName() &&
                isValidTaxId() &&
                isValidPhoneNumber() &&
                addressFormState.isValidForSave(initialProfile?.storeDirections) &&
                hasPendingChanges()
    }

    var firstName by mutableStateOf("")
        private set
    var lastName by mutableStateOf("")
        private set
    var username by mutableStateOf("")
        private set
    var usernameError by mutableStateOf<StringResource?>(null)
        private set
    var usernameExists by mutableStateOf(false)
        private set
    var usernameCheckJob by mutableStateOf<Job?>(null)
        private set
    var isCheckingUsername by mutableStateOf(false)
        private set

    var taxId by mutableStateOf("")
        private set
    var taxIdError by mutableStateOf<StringResource?>(null)
        private set
    var isTaxIdExists by mutableStateOf(false)
        private set
    var taxIdCheckJob by mutableStateOf<Job?>(null)
        private set
    var isCheckingTaxId by mutableStateOf(false)
        private set

    var companyName by mutableStateOf("")
        private set
    var selectedCompanyType by mutableStateOf<SpanishCompanyType?>(null)
        private set
    var displayName by mutableStateOf("")
        private set
    var contactName by mutableStateOf("")
        private set
    var logoFileId by mutableStateOf<String?>(null)
        private set
    var pendingLogoFile by mutableStateOf<PlatformFile?>(null)
        private set

    val addressFormState = SharedAddressFormState(locationRepository, viewModelScope)

    private var initialProfile: RetailerProfileResponseDto? = null

    init {
        phoneNumberViewModel.isOptional = true
    }

    fun loadProfile() {
        viewModelScope.launch {
            isLoading = true
            when (val result = repository.getMyProfile()) {
                is SharedResponseResult.Success -> {
                    result.data?.let {
                        initialProfile = it
                        firstName = it.firstName ?: ""
                        lastName = it.lastName ?: ""
                        username = it.username ?: ""
                        usernameError = null
                        usernameExists = false
                        taxId = it.taxId ?: ""
                        taxIdError = null
                        isTaxIdExists = false
                        companyName = it.profile?.companyName ?: ""
                        displayName = it.profile?.displayName ?: ""
                        selectedCompanyType = it.profile?.companyType
                        contactName = it.profile?.contactName ?: ""
                        logoFileId = it.logoFileId
                        phoneNumberViewModel.updatePhoneNumber(it.telephone ?: "")
                        addressFormState.populate(it.storeDirections)
                    }
                }

                is SharedResponseResult.Error -> {
                    if (SharedResponseResult.shouldShowToUser(result.type)) {
                        result.message?.let { mySnackbarViewModel.showError(it) }
                    }
                    emitNavigation(NavigationEvent.Back)
                }
            }
            isLoading = false
        }
    }

    fun updateFirstName(value: String) {
        firstName = value.take(50)
    }

    fun updateLastName(value: String) {
        lastName = value.take(60)
    }

    fun updateUsername(value: String) {
        username = value.take(30)
        usernameError = validateUsername(username)
        usernameExists = false

        usernameCheckJob?.cancel()
        isCheckingUsername = false
        if (usernameError != null) return

        usernameCheckJob = viewModelScope.launch {
            isCheckingUsername = true
            delay(SharedUiTiming.availabilityCheckDelay)
            usernameExists = true
            when (
                val result = authRepository.checkUserNameExist(
                    username,
                    userId = SharedUserPayloadStorage.get()?.userId,
                )
            ) {
                is SharedResponseResult.Success<Boolean> -> {
                    usernameExists = result.data == true
                    if (usernameExists) {
                        usernameError = SharedRes.string.register_username_already_registered
                    }
                }

                else -> {}
            }
            isCheckingUsername = false
        }
    }

    fun updateTaxId(value: String) {
        val filtered = value.uppercase().filter { it.isDigit() || it in "ABCDEFGHIJKLMNOPQRSTUVWXYZ" }
        taxId = filtered.take(9)

        if (taxId.isNotBlank() && !isValidSpanishTaxId(taxId)) {
            taxIdError = SharedRes.string.invalid_tax_id
            return
        }
        taxIdError = null

        taxIdCheckJob?.cancel()
        isCheckingTaxId = false
        if (taxId.isBlank()) {
            isTaxIdExists = false
            return
        }

        taxIdCheckJob = viewModelScope.launch {
            isCheckingTaxId = true
            delay(SharedUiTiming.availabilityCheckDelay)
            isTaxIdExists = true
            when (val result = authRepository.checkTaxIdExist(taxId)) {
                is SharedResponseResult.Success<Boolean> -> {
                    isTaxIdExists = result.data == true
                    if (isTaxIdExists) {
                        taxIdError = SharedRes.string.tax_id_already_exists
                    }
                }

                else -> {}
            }
            isCheckingTaxId = false
        }
    }

    fun updateCompanyName(value: String) {
        companyName = value.take(100)
    }

    fun updateDisplayName(value: String) {
        displayName = value.take(60)
    }

    fun updateContactName(value: String) {
        contactName = value.take(80)
    }

    fun selectCompanyType(type: SpanishCompanyType?) {
        selectedCompanyType = type
    }

    fun selectLogoFile(file: PlatformFile) {
        if (file.size() > MAX_LOGO_SIZE) {
            viewModelScope.launch {
                mySnackbarViewModel.showInfo(
                    getString(
                        StandardRes.string.media_file_too_large_mb,
                        file.name,
                        file.size() / 1024 / 1024,
                    )
                )
            }
            return
        }
        pendingLogoFile = file
    }

    fun removeLogo() {
        pendingLogoFile = null
        logoFileId = null
    }

    fun isValidUserName(): Boolean {
        if (isCheckingUsername) return false
        return username.isNotBlank() && usernameError == null && !usernameExists
    }

    fun isValidTaxId(): Boolean {
        if (taxId.isBlank()) return true
        if (isCheckingTaxId) return false
        return taxIdError == null && !isTaxIdExists
    }

    fun isValidPhoneNumber(): Boolean {
        if (phoneNumberViewModel.phoneNumber.isBlank()) return true
        if (phoneNumberViewModel.isValidating) return false
        return phoneNumberViewModel.isValid
    }

    fun isAddressValidForSave(): Boolean {
        return addressFormState.isValidForSave(initialProfile?.storeDirections)
    }

    fun saveProfile() {
        if (!addressFormState.validateForSave(initialProfile?.storeDirections)) return
        if (!saveButtonEnabled) return
        if (saveUiState != UiState.Idle) return
        saveUiState = UiState.Loading
        viewModelScope.launch {
            if (!uploadPendingLogo()) {
                saveUiState = UiState.Error
                delay(SharedUiTiming.formStateResetDelay)
                saveUiState = UiState.Idle
                return@launch
            }

            val updatedDto = UpdateRetailerProfileDto(
                firstName = changedField(normalizedOptional(initialProfile?.firstName), normalizedOptional(firstName)),
                lastName = changedField(normalizedOptional(initialProfile?.lastName), normalizedOptional(lastName)),
                username = changedField(normalizedOptional(initialProfile?.username), normalizedOptional(username)),
                telephone = changedField(
                    normalizedOptional(initialProfile?.telephone),
                    normalizedOptional(phoneNumberViewModel.phoneNumber)
                ),
                taxId = changedField(normalizedOptional(initialProfile?.taxId), normalizedOptional(taxId)),
                companyName = changedField(
                    normalizedOptional(initialProfile?.profile?.companyName),
                    normalizedOptional(companyName)
                ),
                displayName = changedField(
                    normalizedOptional(initialProfile?.profile?.displayName),
                    normalizedOptional(displayName)
                ),
                companyType = changedField(initialProfile?.profile?.companyType, selectedCompanyType),
                contactName = changedField(
                    normalizedOptional(initialProfile?.profile?.contactName),
                    normalizedOptional(contactName)
                ),
                logoFileId = changedField(initialProfile?.logoFileId, logoFileId),
                address = addressFormState.changedDirectionField(initialProfile?.storeDirections),
            )

            when (val result = repository.updateMyProfile(updatedDto)) {
                is SharedResponseResult.Success -> {
                    saveUiState = UiState.Success
                    mySnackbarViewModel.showSuccess(getString(SharedRes.string.update_success))
                    emitNavigation(NavigationEvent.Back)
                }

                is SharedResponseResult.Error -> {
                    saveUiState = UiState.Error
                    if (SharedResponseResult.shouldShowToUser(result.type)) {
                        result.message?.let { mySnackbarViewModel.showError(it) }
                    } else {
                        mySnackbarViewModel.showError(getString(SharedRes.string.update_failed))
                    }
                }
            }
            delay(SharedUiTiming.formStateResetDelay)
            saveUiState = UiState.Idle
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private suspend fun uploadPendingLogo(): Boolean {
        val file = pendingLogoFile ?: return true
        val localId = "retailer-logo-${Uuid.random()}"

        val result = uploadRepository
            .uploadFile(localId, file, viewModelScope)
            .first { event ->
                when (event) {
                    is SharedUploadEvent.Progress -> {
                        uploadImage = UiState.Loading
                        uploadImageProgress = event.value
                        false
                    }

                    is SharedUploadEvent.Success,
                    is SharedUploadEvent.Error -> true
                }
            }

        return when (result) {
            is SharedUploadEvent.Success -> {
                uploadImage = UiState.Success
                logoFileId = result.serverFileId
                pendingLogoFile = null
                true
            }

            is SharedUploadEvent.Error -> {
                uploadImage = UiState.Error
                mySnackbarViewModel.showError(result.message ?: getString(SharedRes.string.update_failed))
                false
            }

            is SharedUploadEvent.Progress -> true
        }
    }

    private fun normalizedOptional(value: String?): String? = value?.trim()?.takeIf { it.isNotBlank() }

    private fun hasPendingChanges(): Boolean {
        val initial = initialProfile ?: return false
        val profile = initial.profile

        return normalizedOptional(initial.firstName) != normalizedOptional(firstName) ||
                normalizedOptional(initial.lastName) != normalizedOptional(lastName) ||
                normalizedOptional(initial.username) != normalizedOptional(username) ||
                normalizedOptional(initial.telephone) != normalizedOptional(phoneNumberViewModel.phoneNumber) ||
                normalizedOptional(initial.taxId) != normalizedOptional(taxId) ||
                normalizedOptional(profile?.companyName) != normalizedOptional(companyName) ||
                normalizedOptional(profile?.displayName) != normalizedOptional(displayName) ||
                profile?.companyType != selectedCompanyType ||
                normalizedOptional(profile?.contactName) != normalizedOptional(contactName) ||
                initial.logoFileId != logoFileId ||
                addressFormState.hasChanged(initial.storeDirections) ||
                pendingLogoFile != null
    }
}
