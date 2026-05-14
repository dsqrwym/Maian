package org.dsqrwym.enterprise.ui.viewmodels.profile

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.size
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import maian.business.generated.resources.BusinessRes
import maian.business.generated.resources.media_file_too_large_mb
import maian.shared.generated.resources.*
import org.dsqrwym.business.navigation.Categories
import org.dsqrwym.enterprise.data.profile.WholesalerProfileRepository
import org.dsqrwym.enterprise.data.profile.dto.UpdateWholesalerProfileDto
import org.dsqrwym.shared.data.auth.SharedAuthRepository
import org.dsqrwym.shared.data.file.SharedUploadEvent
import org.dsqrwym.shared.data.file.SharedUploadRepository
import org.dsqrwym.shared.data.local.SharedUserPayloadStorage
import org.dsqrwym.shared.data.profile.WholesalerProfileResponseDto
import org.dsqrwym.shared.data.user.SpanishCompanyType
import org.dsqrwym.shared.navigation.core.NavigationEvent
import org.dsqrwym.shared.navigation.core.SharedNavigable
import org.dsqrwym.shared.navigation.core.SharedNavigableDelegate
import org.dsqrwym.shared.network.model.SharedResponseResult
import org.dsqrwym.shared.ui.components.containers.UiState
import org.dsqrwym.shared.ui.viewmodels.MySnackbarViewModel
import org.dsqrwym.shared.ui.viewmodels.phone.SharedPhoneNumberViewModel
import org.dsqrwym.shared.util.patch.changedField
import org.dsqrwym.shared.util.patch.changedFieldNotNull
import org.dsqrwym.shared.util.timing.SharedUiTiming
import org.dsqrwym.shared.util.validation.isValidSpanishTaxId
import org.dsqrwym.shared.util.validation.validateUsername
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class WholesalerProfileEditViewModel(
    private val repository: WholesalerProfileRepository,
    private val uploadRepository: SharedUploadRepository,
    val phoneNumberViewModel: SharedPhoneNumberViewModel,
    val authRepository: SharedAuthRepository,
    private val mySnackbarViewModel: MySnackbarViewModel,
) : ViewModel(), SharedNavigable by SharedNavigableDelegate() {
    companion object {
        const val MAX_MINIMUM_ORDER_AMOUNT = 1_000_000.00
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
        // 是否正在加载
        !isLoading && saveUiState == UiState.Idle
                && uploadImage != UiState.Loading
                // 是否存在 username
                && isValidUserName()
                // 是否存在 taxId
                && isValidTaxId()
                // 校验 companyName
                && isValidCompanyName()
                // 校验 phoneNumber
                && isValidPhoneNumber()
                && hasPendingChanges()
    }

    fun isValidCompanyName(): Boolean {
        return companyName.isNotBlank() && companyNameError == null
    }

    fun isValidUserName(): Boolean {
        if (isCheckingUsername) return false
        return username.isNotBlank() && usernameError == null && !usernameExists
    }

    fun isValidTaxId(): Boolean {
        if (isCheckingTaxId) return false
        if (taxId.isBlank()) return true
        return taxIdError == null && !isTaxIdExists
    }

    fun isValidPhoneNumber(): Boolean {
        if (phoneNumberViewModel.phoneNumber.isBlank()) return true
        if (phoneNumberViewModel.isValidating) return false
        return phoneNumberViewModel.isValid
    }

    var firstName by mutableStateOf("")
        private set

    var lastName by mutableStateOf("")
        private set

    var username by mutableStateOf("")
        private set

    var taxId by mutableStateOf("")
        private set
    var taxIdError by mutableStateOf<StringResource?>(null)
        private set
    var isTaxIdExists by mutableStateOf(true)
        private set
    var taxIdCheckJob by mutableStateOf<Job?>(null)
        private set
    var isCheckingTaxId by mutableStateOf(false)
        private set

    var companyName by mutableStateOf("")
        private set
    var companyNameError by mutableStateOf<StringResource?>(null)
        private set

    var selectedCompanyType by mutableStateOf(SpanishCompanyType.SL)
        private set

    var displayName by mutableStateOf("")
        private set

    var description by mutableStateOf("")
        private set
    var deliveryAreaDescription by mutableStateOf("")
        private set

    var logoFileId by mutableStateOf<String?>(null)
        private set

    var minimumOrderAmount by mutableStateOf("0.00")
        private set

    var deliveryAvailable by mutableStateOf(false)
        private set
    var pickupAvailable by mutableStateOf(false)
        private set

    var pendingLogoFile by mutableStateOf<PlatformFile?>(null)
        private set

    var usernameError by mutableStateOf<StringResource?>(null)
        private set
    var usernameExists by mutableStateOf(true)
        private set
    var usernameCheckJob by mutableStateOf<Job?>(null)
        private set
    var isCheckingUsername by mutableStateOf(false)
        private set

    private var initialProfile: WholesalerProfileResponseDto? = null

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
                        selectedCompanyType = it.profile?.companyType ?: SpanishCompanyType.SL

                        displayName = it.profile?.displayName ?: ""
                        description = it.profile?.description ?: ""
                        deliveryAreaDescription = it.profile?.deliveryAreaDescription ?: ""

                        logoFileId = it.logoFileId

                        minimumOrderAmount = it.profile?.minimumOrderAmount ?: "0.00"

                        deliveryAvailable = it.profile?.deliveryAvailable ?: false
                        pickupAvailable = it.profile?.pickupAvailable ?: false

                        phoneNumberViewModel.updatePhoneNumber(it.telephone ?: "")
                    }
                }

                is SharedResponseResult.Error -> {
                    if (SharedResponseResult.shouldShowToUser(result.type)) {
                        result.message?.let {
                            mySnackbarViewModel.showError(it)
                        }
                    }
                    emitNavigation(NavigationEvent.ToRoute(Categories))
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
        this.username = value.take(30)

        usernameError = validateUsername(username)
        usernameExists = false

        usernameCheckJob?.cancel()
        isCheckingUsername = false

        if (usernameError != null) return

        usernameCheckJob = viewModelScope.launch {
            isCheckingUsername = true
            delay(SharedUiTiming.availabilityCheckDelay)
            usernameExists = true
            when (val result =
                authRepository.checkUserNameExist(username, userId = SharedUserPayloadStorage.get()?.userId)) {
                is SharedResponseResult.Success<Boolean> -> {
                    usernameExists = result.data == true
                    usernameExists.let {
                        if (it) {
                            usernameError = SharedRes.string.register_username_already_registered
                        }
                    }
                }

                else -> {}
            }
            isCheckingUsername = false
        }
    }

    fun updateCompanyName(value: String) {
        val trimmedValue = value.take(100)
        companyName = trimmedValue
        companyNameError = if (trimmedValue.isBlank()) SharedRes.string.field_cannot_be_empty
        else null
    }

    fun updateDisplayName(value: String) {
        displayName = value.take(60)
    }

    fun updateDescription(value: String) {
        description = value.take(300)
    }

    fun updateDeliveryAreaDescription(value: String) {
        deliveryAreaDescription = value.take(200)
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

        taxIdCheckJob = viewModelScope.launch {
            isCheckingTaxId = true
            delay(SharedUiTiming.availabilityCheckDelay)
            isTaxIdExists = true
            when (val result = authRepository.checkTaxIdExist(taxId)) {
                is SharedResponseResult.Success<Boolean> -> {
                    isTaxIdExists = result.data == true
                    isTaxIdExists.let {
                        if (it) {
                            taxIdError = SharedRes.string.tax_id_already_exists
                        }
                    }
                }

                else -> {}
            }
            isCheckingTaxId = false
        }
    }

    fun updateMinimumOrderAmount(value: String) {
        minimumOrderAmount = value
    }

    fun selectCompanyType(type: SpanishCompanyType) {
        selectedCompanyType = type
    }

    fun toggleDeliveryAvailable() {
        deliveryAvailable = !deliveryAvailable
    }

    fun togglePickupAvailable() {
        pickupAvailable = !pickupAvailable
    }


    fun selectLogoFile(file: PlatformFile) {
        if (file.size() > MAX_LOGO_SIZE) {
            viewModelScope.launch {
                mySnackbarViewModel.showInfo(
                    getString(
                        BusinessRes.string.media_file_too_large_mb,
                        file.name,
                        file.size() / 1024 / 1024
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

    fun saveProfile() {
        if (!(isValidUserName() && isValidTaxId() && isValidCompanyName() && isValidPhoneNumber())) return
        if (!saveButtonEnabled) return
        if (saveUiState != UiState.Idle) return
        saveUiState = UiState.Loading
        viewModelScope.launch {
            uploadPendingLogo()
            val updatedDto = UpdateWholesalerProfileDto(
                firstName = changedField(initialProfile?.firstName, firstName),
                lastName = changedField(initialProfile?.lastName, lastName),
                username = changedField(initialProfile?.username, username),
                taxId = changedField(initialProfile?.taxId, taxId),
                companyType = changedFieldNotNull(
                    initialProfile?.profile?.companyType ?: SpanishCompanyType.SL,
                    selectedCompanyType
                ),
                companyName = changedFieldNotNull(initialProfile?.profile?.companyName ?: "", companyName),
                displayName = changedField(initialProfile?.profile?.displayName, displayName),
                description = changedField(initialProfile?.profile?.description, description),
                deliveryAreaDescription = changedField(
                    initialProfile?.profile?.deliveryAreaDescription,
                    deliveryAreaDescription
                ),
                minimumOrderAmount = changedField(initialProfile?.profile?.minimumOrderAmount, minimumOrderAmount),
                deliveryAvailable = changedField(initialProfile?.profile?.deliveryAvailable, deliveryAvailable),
                pickupAvailable = changedField(initialProfile?.profile?.pickupAvailable, pickupAvailable),
                logoFileId = changedField(initialProfile?.logoFileId, logoFileId)
            )

            when (val result = repository.updateMyProfile(updatedDto)) {
                is SharedResponseResult.Success -> {
                    saveUiState = UiState.Success
                    val message = getString(SharedRes.string.update_success)
                    mySnackbarViewModel.showSuccess(
                        message = message
                    )
                    emitNavigation(NavigationEvent.Back)

                }

                is SharedResponseResult.Error -> {
                    saveUiState = UiState.Error
                    if (SharedResponseResult.shouldShowToUser(result.type)) {
                        result.message?.let { mySnackbarViewModel.showError(it) }
                    } else {
                        val message = getString(SharedRes.string.create_failed)
                        mySnackbarViewModel.showError(message)
                    }
                }
            }
            delay(SharedUiTiming.formStateResetDelay)
            saveUiState = UiState.Idle
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private suspend fun uploadPendingLogo() {
        val file = pendingLogoFile ?: return
        val localId = "logo-${Uuid.random()}"

        val result = uploadRepository
            .uploadFile(localId, file, viewModelScope)
            .first {
                it is SharedUploadEvent.Success ||
                        it is SharedUploadEvent.Error
            }

        logoFileId = when (result) {
            is SharedUploadEvent.Success -> {
                uploadImage = UiState.Success
                result.serverFileId
            }

            is SharedUploadEvent.Error -> {
                uploadImage = UiState.Error
                mySnackbarViewModel.showError(getString(SharedRes.string.update_failed))
                null
            }

            is SharedUploadEvent.Progress -> {
                uploadImage = UiState.Loading
                uploadImageProgress = result.value
                null
            }
        }
    }

    private fun normalizedOptional(value: String?): String? = value?.trim()?.takeIf { it.isNotBlank() }

    private fun normalizedAmount(value: String?): String = value?.trim()?.takeIf { it.isNotBlank() } ?: "0.00"

    private fun hasPendingChanges(): Boolean {
        val initial = initialProfile ?: return false
        val profile = initial.profile

        return normalizedOptional(initial.firstName) != normalizedOptional(firstName) ||
                normalizedOptional(initial.lastName) != normalizedOptional(lastName) ||
                normalizedOptional(initial.username) != normalizedOptional(username) ||
                normalizedOptional(initial.taxId) != normalizedOptional(taxId) ||
                normalizedOptional(initial.telephone) != normalizedOptional(phoneNumberViewModel.phoneNumber) ||
                (profile?.companyName ?: "") != companyName.trim() ||
                (profile?.companyType ?: SpanishCompanyType.SL) != selectedCompanyType ||
                normalizedOptional(profile?.displayName) != normalizedOptional(displayName) ||
                normalizedOptional(profile?.description) != normalizedOptional(description) ||
                normalizedOptional(profile?.deliveryAreaDescription) != normalizedOptional(deliveryAreaDescription) ||
                normalizedAmount(profile?.minimumOrderAmount) != normalizedAmount(minimumOrderAmount) ||
                (profile?.deliveryAvailable ?: false) != deliveryAvailable ||
                (profile?.pickupAvailable ?: false) != pickupAvailable ||
                initial.logoFileId != logoFileId ||
                pendingLogoFile != null
    }
}
