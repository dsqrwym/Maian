package org.dsqrwym.standard.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("register")
object RegisterScreen : NavKey

@Serializable
@SerialName("supplier")
object SuppliersScreen : NavKey

@Serializable
@SerialName("basket")
object BasketScreen : NavKey

@Serializable
@SerialName("chat")
object ChatScreen : NavKey