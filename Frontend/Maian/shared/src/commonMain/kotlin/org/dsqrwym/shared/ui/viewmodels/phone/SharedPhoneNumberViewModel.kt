package org.dsqrwym.shared.ui.viewmodels.phone

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.michaelrocks.libphonenumber.kotlin.NumberParseException
import io.michaelrocks.libphonenumber.kotlin.PhoneNumberUtil
import io.michaelrocks.libphonenumber.kotlin.Phonenumber.PhoneNumber
import io.michaelrocks.libphonenumber.kotlin.metadata.defaultMetadataLoader
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import maian.shared.generated.resources.*
import org.dsqrwym.shared.data.location.SharedLocationRepository
import org.dsqrwym.shared.util.platform.getPlatformDeviceInfo
import org.jetbrains.compose.resources.StringResource

/**
 * 电话号码验证状态
 */
sealed class PhoneValidationState {
    object Idle : PhoneValidationState()
    object Validating : PhoneValidationState()
    data class Valid(val formattedNumber: String) : PhoneValidationState()
    object Invalid : PhoneValidationState()
}

/**
 * 电话号码 ViewModel
 * 负责电话号码的验证、格式化和错误处理
 */
class SharedPhoneNumberViewModel(private val locationRepository: SharedLocationRepository) : ViewModel() {

    val phoneNumberUtil: PhoneNumberUtil by lazy {
        PhoneNumberUtil.createInstance(defaultMetadataLoader())
    }

    // 电话号码原始输入
    var phoneNumber by mutableStateOf("")
        private set

    // 格式化后的电话号码（国际格式）
    var formattedPhoneNumber by mutableStateOf("")
        private set

    // 当前尝试解析的格式（用于错误提示）
    var attemptedFormat by mutableStateOf<String?>(null)
        private set

    // 验证状态
    var validationState by mutableStateOf<PhoneValidationState>(PhoneValidationState.Idle)
        private set

    // 错误信息
    var errorMessage by mutableStateOf<StringResource?>(null)
        private set

    // 是否正在验证
    val isValidating: Boolean
        get() = validationState is PhoneValidationState.Validating

    // 是否有效
    val isValid: Boolean
        get() = validationState is PhoneValidationState.Valid

    // 检测到的国家/地区代码
    var detectedRegion by mutableStateOf<String?>(null)
        private set

    // 防抖 Job
    private var validationJob: Job? = null

    /**
     * 检测设备所在地区
     */
    suspend fun getDetectRegion(): String {
        detectedRegion?.let {
            return it
        }
        val region = locationRepository.getRealRegionCode() ?: getPlatformDeviceInfo().countryCode
        detectedRegion = region
        return region
    }

    /**
     * 更新电话号码
     * @param value 新的电话号码值
     */
    fun updatePhoneNumber(value: String) {
        phoneNumber = value

        // 取消之前的验证任务
        validationJob?.cancel()

        if (value.isBlank()) {
            errorMessage = SharedRes.string.phone_error_empty
            phoneNumber = value
            formattedPhoneNumber = value
            validationState = PhoneValidationState.Invalid
            return
        }

        validationJob = viewModelScope.launch {
            validationState = PhoneValidationState.Validating
            delay(500)
            validatePhoneNumber(value, getDetectRegion())
        }
    }

    /**
     * 验证电话号码
     */
    private fun validatePhoneNumber(number: String, region: String) {
        try {
            // 解析电话号码
            val parsedNumber: PhoneNumber = phoneNumberUtil.parse(number, region)

            attemptedFormat = try {
                phoneNumberUtil.format(
                    parsedNumber,
                    PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL
                )
            } catch (_: Exception) {
                null
            }

            // 验证号码是否有效
            val isValidNumber = phoneNumberUtil.isValidNumber(parsedNumber)

            if (!isValidNumber) {
                // 检查具体的无效原因
                val validationResult = phoneNumberUtil.isPossibleNumberWithReason(parsedNumber)
                errorMessage = when (validationResult) {
                    PhoneNumberUtil.ValidationResult.INVALID_COUNTRY_CODE ->
                        SharedRes.string.phone_error_invalid_country_code

                    PhoneNumberUtil.ValidationResult.TOO_SHORT ->
                        SharedRes.string.phone_error_too_short

                    PhoneNumberUtil.ValidationResult.TOO_LONG ->
                        SharedRes.string.phone_error_too_long

                    PhoneNumberUtil.ValidationResult.INVALID_LENGTH ->
                        SharedRes.string.phone_error_invalid_length

                    else ->
                        SharedRes.string.phone_error_invalid
                }
                validationState = PhoneValidationState.Invalid
                return
            }

            // 格式化号码
            formattedPhoneNumber = phoneNumberUtil.format(
                parsedNumber,
                PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL
            )

            phoneNumber = formattedPhoneNumber

            // 验证成功
            errorMessage = null
            validationState = PhoneValidationState.Valid(formattedPhoneNumber)
        } catch (e: NumberParseException) {
            errorMessage = when (e.errorType) {
                NumberParseException.ErrorType.INVALID_COUNTRY_CODE ->
                    SharedRes.string.phone_error_invalid_country_code

                NumberParseException.ErrorType.NOT_A_NUMBER ->
                    SharedRes.string.phone_error_not_a_number

                NumberParseException.ErrorType.TOO_SHORT_NSN ->
                    SharedRes.string.phone_error_too_short

                NumberParseException.ErrorType.TOO_SHORT_AFTER_IDD ->
                    SharedRes.string.phone_error_too_short_after_idd

                NumberParseException.ErrorType.TOO_LONG ->
                    SharedRes.string.phone_error_too_long
            }
            validationState = PhoneValidationState.Invalid

        } catch (_: Exception) {
            validationState = PhoneValidationState.Invalid
        }
    }

    fun resetPhoneNumberViewModel() {
        validationJob = null
        attemptedFormat = null
        validationState = PhoneValidationState.Idle
        errorMessage = null
        phoneNumber = ""
        formattedPhoneNumber = ""
    }
}