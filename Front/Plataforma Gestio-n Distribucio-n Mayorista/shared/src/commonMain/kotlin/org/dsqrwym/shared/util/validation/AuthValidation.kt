package org.dsqrwym.shared.util.validation

import org.jetbrains.compose.resources.StringResource
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.*


expect fun validateEmail(email: String): Boolean


// 验证函数
 fun validateUsernameOrEmail(input: String): StringResource? {
    if (input.isBlank()) {
        return SharedRes.string.login_validation_username_or_email_empty
    }

    val isEmail = validateEmail(input)

    if (isEmail && input.endsWith("@example.com", ignoreCase = true)) {
        return SharedRes.string.login_validation_email_domain_not_supported
    }

    // Username length validation
    if (!isEmail && (input.length !in 3..30)) {
        return SharedRes.string.login_validation_username_length_invalid
    }
    return null
}

fun validatePassword(password: String): StringResource? {
    if (password.isBlank()) {
        return SharedRes.string.login_validation_password_empty
    }
    if (password.length < 6) {
        return SharedRes.string.login_validation_password_too_short
    }
    if (!password.any { it.isUpperCase() }) {
        return SharedRes.string.login_validation_password_missing_uppercase
    }
    if (!password.any { it.isLowerCase() }) {
        return SharedRes.string.login_validation_password_missing_lowercase
    }
    if (!password.any { it.isDigit() }) {
        return SharedRes.string.login_validation_password_missing_digit
    }

    return null
}

fun validateRepeatPassword(password: String, repeatPassword: String): StringResource? {
    if (password != repeatPassword) {
        return SharedRes.string.forgot_repeat_password_mismatch
    }
    return validatePassword(repeatPassword)
}