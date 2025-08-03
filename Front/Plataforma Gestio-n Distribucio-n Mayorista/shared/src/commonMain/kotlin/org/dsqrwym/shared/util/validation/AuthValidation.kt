package org.dsqrwym.shared.util.validation

import org.dsqrwym.shared.language.SharedLanguageMap


expect fun validateEmail(email: String): Boolean


// 验证函数
fun validateUsernameOrEmail(input: String): String? {
    if (input.isBlank()) {
        return SharedLanguageMap.currentStrings.value.login_validation_username_or_email_empty //"用户名或邮箱不能为空"
    }

    val isEmail = validateEmail(input)

    if (isEmail && input.endsWith("@example.com", ignoreCase = true)) {
        return SharedLanguageMap.currentStrings.value.login_validation_email_domain_not_supported // "不支持 example.com 邮箱"
    }

    // Username length validation
    if (!isEmail && (input.length < 3 || input.length > 30)) {
        return SharedLanguageMap.currentStrings.value.login_validation_username_length_invalid // "用户名长度应在3到30个字符之间"
    }
    return null
}

fun validatePassword(password: String): String? {
    if (password.isBlank()) {
        return SharedLanguageMap.currentStrings.value.login_validation_password_empty // "密码不能为空"
    }
    if (password.length < 6) {
        return SharedLanguageMap.currentStrings.value.login_validation_password_too_short // "密码长度至少为6个字符"
    }
    if (!password.any { it.isUpperCase() }) {
        return SharedLanguageMap.currentStrings.value.login_validation_password_missing_uppercase // "密码必须包含至少一个大写字母"
    }
    if (!password.any { it.isLowerCase() }) {
        return SharedLanguageMap.currentStrings.value.login_validation_password_missing_lowercase // "密码必须包含至少一个小写字母"
    }
    if (!password.any { it.isDigit() }) {
        return SharedLanguageMap.currentStrings.value.login_validation_password_missing_digit // "密码必须包含至少一个数字"
    }

    return null
}

fun validateRepeatPassword(password: String, repeatPassword: String): String? {
    if (password != repeatPassword) {
        return "输入的密码与新密码不同"
    }
    return validatePassword(repeatPassword)
}