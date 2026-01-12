package org.dsqrwym.shared.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// 起始页面
@Serializable
@SerialName("initial")
object SharedInitialScreen : NavKey

@Serializable
@SerialName("login")
data class SharedLoginScreen(val email: String? = null) : NavKey

@Serializable
@SerialName("reset-password")
data class SharedResetPasswordScreen(val email: String? = null) : NavKey

@Serializable
@SerialName("privacy-policy")
object SharedPrivacyPolicy : NavKey

@Serializable
@SerialName("user-agreement")
object SharedUserAgreement : NavKey

// 主要
@Serializable
@SerialName("dashboard")
object SharedDashboardScreen : NavKey

@Serializable
@SerialName("profile")
object SharedProfileScreen : NavKey
