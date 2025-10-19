package org.dsqrwym.shared.navigation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// 起始页面
@Serializable
@SerialName("initial")
object SharedInitialScreen

@Serializable
@SerialName("login")
data class SharedLoginScreen(val email: String? = null)

@Serializable
@SerialName("reset-password")
data class SharedResetPasswordScreen(val email: String? = null)

@Serializable
@SerialName("privacy-policy")
object SharedPrivacyPolicy

@Serializable
@SerialName("user-agreement")
object SharedUserAgreement

// 主要
@Serializable
@SerialName("dashboard")
object SharedDashboardScreen
@Serializable
@SerialName("profile")
object SharedProfileScreen
