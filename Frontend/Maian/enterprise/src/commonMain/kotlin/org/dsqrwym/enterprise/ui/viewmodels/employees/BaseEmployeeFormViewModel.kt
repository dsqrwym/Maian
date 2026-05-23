package org.dsqrwym.enterprise.ui.viewmodels.employees

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.field_cannot_be_empty
import maian.shared.generated.resources.invalid_tax_id
import maian.shared.generated.resources.register_email_already_registered
import maian.shared.generated.resources.register_username_already_registered
import maian.shared.generated.resources.reset_email_format_error
import maian.shared.generated.resources.validation_email_domain_not_supported
import org.dsqrwym.enterprise.data.profile.WholesalerProfileRepository
import org.dsqrwym.shared.data.auth.SharedAuthRepository
import org.dsqrwym.shared.network.model.SharedResponseResult
import org.dsqrwym.shared.ui.viewmodels.MySnackbarViewModel
import org.dsqrwym.shared.ui.viewmodels.phone.SharedPhoneNumberViewModel
import org.dsqrwym.shared.util.timing.SharedUiTiming
import org.dsqrwym.shared.util.validation.isValidSpanishTaxId
import org.dsqrwym.shared.util.validation.validateEmail
import org.dsqrwym.shared.util.validation.validateUsername
import org.jetbrains.compose.resources.StringResource

abstract class BaseEmployeeFormViewModel(
    protected val authRepository: SharedAuthRepository,
    protected val wholesalerProfileRepository: WholesalerProfileRepository,
    val phoneNumberViewModel: SharedPhoneNumberViewModel,
    protected val mySnackbarViewModel: MySnackbarViewModel,
) : ViewModel() {
    var email by mutableStateOf("")
        protected set
    var emailError by mutableStateOf<StringResource?>(null)
        protected set
    var emailExists by mutableStateOf(false)
        protected set
    var isCheckingEmail by mutableStateOf(false)
        protected set
    private var emailCheckJob: Job? = null

    var firstName by mutableStateOf("")
        protected set
    var lastName by mutableStateOf("")
        protected set
    var username by mutableStateOf("")
        protected set
    var usernameError by mutableStateOf<StringResource?>(null)
        protected set
    var usernameExists by mutableStateOf(false)
        protected set
    var isCheckingUsername by mutableStateOf(false)
        protected set
    private var usernameCheckJob: Job? = null

    var taxId by mutableStateOf("")
        protected set
    var taxIdError by mutableStateOf<StringResource?>(null)
        protected set

    private var wholesalerPublicId: String? = null
    protected var usernameAvailabilityExemptValue: String? = null
    protected var usernameAvailabilitySkipUserId: String? = null

    init {
        phoneNumberViewModel.isOptional = true
    }

    open fun updateEmail(value: String) {
        email = value.take(100).trim()
        emailExists = false
        emailError = validateEmployeeEmail(email)
        emailCheckJob?.cancel()
        isCheckingEmail = false
        if (emailError != null) return

        emailCheckJob = viewModelScope.launch {
            isCheckingEmail = true
            delay(SharedUiTiming.availabilityCheckDelay)
            when (val result = authRepository.checkEmailExists(email)) {
                is SharedResponseResult.Success -> {
                    emailExists = result.data == true
                    emailError = if (emailExists) SharedRes.string.register_email_already_registered else null
                }

                is SharedResponseResult.Error -> {
                    if (SharedResponseResult.shouldShowToUser(result.type)) {
                        result.message?.let { mySnackbarViewModel.showError(it) }
                    }
                    emailExists = true
                }
            }
            isCheckingEmail = false
        }
    }

    fun updateFirstName(value: String) {
        firstName = value.take(50)
    }

    fun updateLastName(value: String) {
        lastName = value.take(60)
    }

    fun updateUsername(value: String) {
        username = value.take(30).trim()
        usernameExists = false
        usernameError = validateUsername(username)
        usernameCheckJob?.cancel()
        isCheckingUsername = false

        if (usernameError != null) return
        if (username == usernameAvailabilityExemptValue) return

        usernameCheckJob = viewModelScope.launch {
            isCheckingUsername = true
            delay(SharedUiTiming.availabilityCheckDelay)
            when (
                val result = authRepository.checkUserNameExist(
                    username = username,
                    wholesalerId = getWholesalerPublicId(),
                    userId = usernameAvailabilitySkipUserId,
                )
            ) {
                is SharedResponseResult.Success -> {
                    usernameExists = result.data == true
                    usernameError = if (usernameExists) {
                        SharedRes.string.register_username_already_registered
                    } else {
                        null
                    }
                }

                is SharedResponseResult.Error -> {
                    if (SharedResponseResult.shouldShowToUser(result.type)) {
                        result.message?.let { mySnackbarViewModel.showError(it) }
                    }
                    usernameExists = true
                }
            }
            isCheckingUsername = false
        }
    }

    fun updateTaxId(value: String) {
        val filtered = value.uppercase().filter { it.isDigit() || it in "ABCDEFGHIJKLMNOPQRSTUVWXYZ" }
        taxId = filtered.take(9)
        taxIdError = if (taxId.isNotBlank() && !isValidSpanishTaxId(taxId)) {
            SharedRes.string.invalid_tax_id
        } else {
            null
        }
    }

    protected fun isValidEmail(): Boolean =
        email.isNotBlank() && emailError == null && !emailExists && !isCheckingEmail

    protected fun isValidUsername(): Boolean =
        username.isNotBlank() && usernameError == null && !usernameExists && !isCheckingUsername

    protected fun isValidTaxId(): Boolean =
        taxId.isBlank() || taxIdError == null

    protected fun isValidPhoneNumber(): Boolean {
        if (phoneNumberViewModel.phoneNumber.isBlank()) return true
        if (phoneNumberViewModel.isValidating) return false
        return phoneNumberViewModel.isValid
    }

    protected fun optional(value: String): String? =
        value.trim().takeIf { it.isNotBlank() }

    protected fun optionalTelephone(): String? =
        optional(phoneNumberViewModel.formattedPhoneNumber).orElseOptional(phoneNumberViewModel.phoneNumber)

    private fun validateEmployeeEmail(value: String): StringResource? =
        when {
            value.isBlank() -> SharedRes.string.field_cannot_be_empty
            value.endsWith("@example.com", ignoreCase = true) -> SharedRes.string.validation_email_domain_not_supported
            !validateEmail(value) -> SharedRes.string.reset_email_format_error
            else -> null
        }

    private suspend fun getWholesalerPublicId(): String? {
        wholesalerPublicId?.let { return it }
        return when (val result = wholesalerProfileRepository.getMyProfile()) {
            is SharedResponseResult.Success -> result.data?.userId?.also { wholesalerPublicId = it }
            is SharedResponseResult.Error -> null
        }
    }
}

private fun String?.orElseOptional(fallback: String): String? =
    this ?: fallback.trim().takeIf { it.isNotBlank() }
