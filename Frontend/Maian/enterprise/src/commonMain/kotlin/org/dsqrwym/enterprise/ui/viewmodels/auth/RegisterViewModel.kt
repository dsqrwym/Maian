package org.dsqrwym.enterprise.ui.viewmodels.auth

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import io.ktor.http.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import maian.enterprise.generated.resources.EnterpriseRes
import maian.enterprise.generated.resources.register_failed
import maian.enterprise.generated.resources.register_success
import maian.shared.generated.resources.*
import org.dsqrwym.enterprise.data.auth.AuthRepository
import org.dsqrwym.enterprise.data.auth.dto.CompleteRegisterRequest
import org.dsqrwym.enterprise.data.auth.dto.SpanishCompanyType
import org.dsqrwym.enterprise.data.local.UserPreference
import org.dsqrwym.shared.data.auth.SharedAuthRepository
import org.dsqrwym.shared.data.location.SharedLocationRepository
import org.dsqrwym.shared.data.location.dto.CityDto
import org.dsqrwym.shared.data.location.dto.CountryDto
import org.dsqrwym.shared.data.location.dto.DirectionRequest
import org.dsqrwym.shared.data.location.dto.ProvinceDto
import org.dsqrwym.shared.navigation.SharedLoginScreen
import org.dsqrwym.shared.navigation.core.NavigationEvent
import org.dsqrwym.shared.navigation.core.SharedNavigable
import org.dsqrwym.shared.navigation.core.SharedNavigableDelegate
import org.dsqrwym.shared.network.ApiConfig
import org.dsqrwym.shared.network.SharedResponseResult
import org.dsqrwym.shared.ui.components.containers.UiState
import org.dsqrwym.shared.ui.viewmodels.MySnackbarViewModel
import org.dsqrwym.shared.ui.viewmodels.auth.VerifyOtpCodeViewModelBase
import org.dsqrwym.shared.ui.viewmodels.phone.SharedPhoneNumberViewModel
import org.dsqrwym.shared.util.validation.validateEmail
import org.dsqrwym.shared.util.validation.validatePassword
import org.dsqrwym.shared.util.validation.validateRepeatPassword
import org.dsqrwym.shared.util.validation.validateUsername
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import kotlin.time.ExperimentalTime

class RegisterViewModel(
    val phoneNumberViewModel: SharedPhoneNumberViewModel,
    override val sharedAuthRepository: SharedAuthRepository,
    private val authRepository: AuthRepository,
    private val locationRepository: SharedLocationRepository,
    override val mySnackbarViewModel: MySnackbarViewModel
) : VerifyOtpCodeViewModelBase(sharedAuthRepository, mySnackbarViewModel),
    SharedNavigable by SharedNavigableDelegate() {
    var maxStep by mutableStateOf(3)
    var currentStep by mutableStateOf(1)
    var emailNotExists by mutableStateOf<Boolean?>(null)
    val hasCurrentStepError by derivedStateOf {
        when (currentStep) {
            1 -> !validateRegisterStep1()
            2 -> !validateRegisterStep2()
            3 -> !validateRegisterStep3()
            else -> false
        }
    }
    var nextButtonUiState by mutableStateOf(UiState.Idle)
    val nextButtonEnabled by derivedStateOf {
        val noError = !hasCurrentStepError
        when (currentStep) {
            1 -> noError && email.isNotBlank()
            2 -> noError && codeIsComplete
            3 -> {
                if (!noError) return@derivedStateOf false
                if (isCheckingUsername) return@derivedStateOf false
                if (password.isBlank()) return@derivedStateOf false
                if (repeatPassword.isBlank()) return@derivedStateOf false
                if (companyName.isBlank()) return@derivedStateOf false
                if (selectedCompanyType == null) return@derivedStateOf false
                if (!phoneNumberViewModel.isValid) return@derivedStateOf false
                if (street.isBlank()) return@derivedStateOf false
                if (zipCode.isBlank()) return@derivedStateOf false
                if (selectedCountryIso == null) return@derivedStateOf false
                if (selectedProvinceId == null) return@derivedStateOf false
                if (selectedCityId == null) return@derivedStateOf false
                return@derivedStateOf true
            }

            else -> false
        }
    }
    var password by mutableStateOf("")
    var passwordError by mutableStateOf<StringResource?>(null)

    var repeatPassword by mutableStateOf("")
    var repeatPasswordError by mutableStateOf<StringResource?>(null)
    var username by mutableStateOf("")
    var usernameError by mutableStateOf<StringResource?>(null)
    var usernameExists by mutableStateOf<Boolean?>(null)
    var usernameCheckJob by mutableStateOf<Job?>(null)
    var isCheckingUsername by mutableStateOf(false)

    // Company information (required)
    var companyName by mutableStateOf("")
    var companyNameError by mutableStateOf<StringResource?>(null)
    var selectedCompanyType: SpanishCompanyType? by mutableStateOf(null)
    var companyTypeError by mutableStateOf<StringResource?>(null)

    // Address state (required)
    var street by mutableStateOf("")
    var streetError by mutableStateOf<StringResource?>(null)
    var zipCode by mutableStateOf("")
    var zipCodeError by mutableStateOf<StringResource?>(null)

    var countries by mutableStateOf<List<CountryDto>>(emptyList())
    var provinces by mutableStateOf<List<ProvinceDto>>(emptyList())
    var cities by mutableStateOf<List<CityDto>>(emptyList())

    var selectedCountryIso: Int? by mutableStateOf(null)
    var selectedCountryError by mutableStateOf<StringResource?>(null)
    var selectedProvinceId: Int? by mutableStateOf(null)
    var selectedProvinceError by mutableStateOf<StringResource?>(null)
    var selectedCityId: Int? by mutableStateOf(null)
    var selectedCityError by mutableStateOf<StringResource?>(null)

    var isLoadingCountries by mutableStateOf(false)
    var isLoadingProvinces by mutableStateOf(false)
    var isLoadingCities by mutableStateOf(false)

    fun updateCompanyName(value: String) {
        companyName = value
        companyNameError = if (value.isBlank()) SharedRes.string.field_cannot_be_empty
        else null
    }

    fun selectCompanyType(type: SpanishCompanyType?) {
        selectedCompanyType = type
        companyTypeError = if (type == null) SharedRes.string.field_required
        else null
    }

    fun updateStreet(value: String) {
        street = value
        streetError = if (value.isBlank()) SharedRes.string.field_cannot_be_empty
        else null
    }

    fun updateZipCode(value: String) {
        zipCode = value
        zipCodeError = if (value.isBlank()) SharedRes.string.field_cannot_be_empty
        else null
    }

    fun selectCountry(isoNumeric: Int?) {
        if (selectedCountryIso != isoNumeric) {
            selectedCountryIso = isoNumeric
            // Reset dependent selections
            selectedProvinceId = null
            selectedCityId = null
            provinces = emptyList()
            cities = emptyList()
            // Load provinces
            if (isoNumeric != null) {
                selectedCountryError = null
                viewModelScope.launch {
                    loadProvinces(isoNumeric)
                }
                selectedProvinceId = null
            } else {
                selectedCountryError = SharedRes.string.field_required
            }
        }
    }

    fun selectProvince(id: Int?) {
        if (selectedProvinceId != id) {
            selectedProvinceId = id
            selectedCityId = null
            cities = emptyList()
            if (id != null) {
                selectedProvinceError = null
                viewModelScope.launch { loadCities(id) }
                selectedCityId = null
            } else {
                selectedProvinceError = SharedRes.string.field_required
            }
        }
    }

    fun selectCity(id: Int?) {
        if (selectedCityId != id) {
            selectedCityId = id
            selectedCityError = if (id != null) null else SharedRes.string.field_required
        }
    }

    fun ensureCountriesLoaded() {
        if (countries.isEmpty() && !isLoadingCountries) {
            viewModelScope.launch { loadCountries() }
        }
    }

    private suspend fun loadCountries() {
        isLoadingCountries = true
        countries = when (val result = locationRepository.getCountries()) {
            is SharedResponseResult.Success -> {
                result.data ?: emptyList()
            }

            is SharedResponseResult.Error -> {
                emptyList()
            }
        }
        isLoadingCountries = false
    }

    private suspend fun loadProvinces(iso: Int) {
        isLoadingProvinces = true
        provinces = when (val result = locationRepository.getProvincesByCountry(iso)) {
            is SharedResponseResult.Success -> {
                result.data ?: emptyList()
            }

            is SharedResponseResult.Error -> emptyList()
        }
        isLoadingProvinces = false
    }

    private suspend fun loadCities(provinceId: Int) {
        isLoadingCities = true
        cities = when (val result = locationRepository.getCitiesByProvince(provinceId)) {
            is SharedResponseResult.Success -> {
                result.data ?: emptyList()
            }

            is SharedResponseResult.Error -> emptyList()
        }
        isLoadingCities = false
    }

    fun updateEmail(email: String) {
        updateEmail(email, {
            emailNotExists = !it
            emailError = if (!it) null else SharedRes.string.register_email_already_registered
        }, {
            emailNotExists = true
            emailError = SharedRes.string.register_email_already_registered
        })
    }

    fun updatePassword(password: String) {
        this.password = password
        passwordError = validatePassword(password)
        if (this.repeatPassword.isNotBlank()) {
            repeatPasswordError = validateRepeatPassword(password, repeatPassword)
        }
    }

    fun updateRepeatPassword(password: String) {
        this.repeatPassword = password
        repeatPasswordError = validateRepeatPassword(this@RegisterViewModel.password, password)
    }

    fun updateUsername(username: String) {
        this.username = username
        if (username.isNotBlank()) {
            usernameError = validateUsername(username)
        } else {
            usernameError = null
            usernameExists = null
        }
        if (usernameError != null) return

        usernameCheckJob?.cancel()
        isCheckingUsername = false

        usernameCheckJob = viewModelScope.launch {
            isCheckingUsername = true
            delay(500)
            usernameExists = true
            when (val result = sharedAuthRepository.checkUserNameExist(username)) {
                is SharedResponseResult.Success<Boolean> -> {
                    usernameExists = result.data == true
                    usernameExists?.let {
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

    override fun sendCode() {
        viewModelScope.launch { sendVerificationCode() }
    }

    fun validateRegisterStep1(): Boolean {
        return emailError == null && validateEmail(email) && emailNotExists == true
    }

    fun validateRegisterStep2(): Boolean {
        return codeError == null && codeIsComplete
    }

    fun validateRegisterStep3(): Boolean {
        if (streetError != null) return false
        if (zipCodeError != null) return false
        if (selectedCityError != null) return false
        if (selectedProvinceError != null) return false
        if (selectedCountryError != null) return false

        if (passwordError != null) return false
        if (repeatPasswordError != null) return false

        if (phoneNumberViewModel.errorMessage != null) return false

        if (usernameError != null) return false

        if (username.isNotBlank()) {
            return usernameExists == false
        }
        return true
    }

    private suspend fun sendVerificationCode() {
        when (val result = authRepository.startRegister(email)) {
            is SharedResponseResult.Success<*> -> {
                nextButtonUiState = UiState.Success
                mySnackbarViewModel.showSuccess(getString(SharedRes.string.otp_code_sent))
                if (!codeSend) {
                    currentStep++
                }
            }

            is SharedResponseResult.Error -> {
                nextButtonUiState = UiState.Error
                if (result.type == HttpStatusCode.TooManyRequests) {
                    mySnackbarViewModel.showInfo(getString(SharedRes.string.request_too_frequent))
                } else {
                    mySnackbarViewModel.showError(getString(SharedRes.string.register_email_already_registered))
                    emailError = SharedRes.string.register_email_already_registered
                }
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun verifyCode() {
        verifyCode(verifyUrl = ApiConfig.AuthPath.REGISTRATION_VERIFY_EMAIL, whenSuccess = {
            currentStep++
        })
    }

    fun nextButtonClicked() {
        if (currentStep <= maxStep && nextButtonEnabled) {
            viewModelScope.launch {
                nextButtonUiState = UiState.Loading
                when (currentStep) {
                    1 -> sendVerificationCode()
                    2 -> verifyCode()
                    3 -> register()
                }
                nextButtonUiState = UiState.Idle
            }
        }
    }

    private suspend fun register() {
        verifyCodeResult?.let { response ->
            selectedCompanyType?.let { type ->
                if (selectedCityId == null || selectedProvinceId == null || selectedCountryIso == null) return
                val req = CompleteRegisterRequest(
                    email = email,
                    password = password,
                    username = username.ifBlank { null },
                    address = DirectionRequest(
                        street = street,
                        zipCode = zipCode,
                        city = selectedCityId!!,
                        province = selectedProvinceId!!,
                        country = selectedCountryIso!!
                    ),
                    verificationId = response.verificationId,
                    token = response.token,
                    companyType = type,
                    companyName = companyName,
                    telephone = phoneNumberViewModel.formattedPhoneNumber

                )
                when (val result = authRepository.completeResister(req)) {
                    is SharedResponseResult.Success -> {
                        mySnackbarViewModel.showSuccess(getString(EnterpriseRes.string.register_success))
                        currentStep++
                        UserPreference.setUserSelectRole(loginType = LoginType.WHOLESALER)
                        emitNavigation(NavigationEvent.ToRoute(SharedLoginScreen(email)))
                    }

                    is SharedResponseResult.Error -> {
                        if (result.type == HttpStatusCode.TooManyRequests) {
                            mySnackbarViewModel.showInfo(getString(SharedRes.string.request_too_frequent))
                        } else if (result.type == HttpStatusCode.Unauthorized) {
                            mySnackbarViewModel.showError(getString(SharedRes.string.token_invalid_or_expired))
                            resetRegister()
                        } else {
                            mySnackbarViewModel.showError(getString(EnterpriseRes.string.register_failed))
                            resetRegister()
                        }
                    }
                }
            }
        }
    }

    fun resetRegister() {
        currentStep = 1
        nextButtonUiState = UiState.Idle

        resetEmail()
        resetOtpState()

        password = ""
        passwordError = null

        repeatPassword = ""
        repeatPasswordError = null

        username = ""
        usernameError = null
        usernameExists = null
        usernameCheckJob?.cancel()
        isCheckingUsername = false

        street = ""
        streetError = null

        zipCode = ""
        zipCodeError = null

        selectedCountryIso = null
        selectedCountryError = null

        selectedProvinceId = null
        selectedProvinceError = null

        selectedCityId = null
        selectedCityError = null

        phoneNumberViewModel.resetPhoneNumberViewModel()
    }
}