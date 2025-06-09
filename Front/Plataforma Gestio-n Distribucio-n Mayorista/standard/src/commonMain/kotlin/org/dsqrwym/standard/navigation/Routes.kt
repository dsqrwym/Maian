package org.dsqrwym.standard.navigation

import kotlinx.serialization.Serializable

// 起始页面
@Serializable
data class InitialScreen(
    val denied : Boolean = false
)

@Serializable
object LoginScreen