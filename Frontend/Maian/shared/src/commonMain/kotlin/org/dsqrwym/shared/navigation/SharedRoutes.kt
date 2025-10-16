package org.dsqrwym.shared.navigation

import kotlinx.serialization.Serializable

// 起始页面
@Serializable
object InitialScreen
@Serializable
data class LoginScreen(val email: String? = null)
@Serializable
data class ForgotPasswordScreen(val email: String? = null)
@Serializable
object PrivacyPolicy

@Serializable
object UserAgreement