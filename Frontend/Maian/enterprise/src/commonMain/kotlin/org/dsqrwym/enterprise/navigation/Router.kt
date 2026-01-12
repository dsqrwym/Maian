package org.dsqrwym.enterprise.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Register")
object RegisterScreen : NavKey

@Serializable
@SerialName("Products")
object Products : NavKey

@Serializable
@SerialName("Product-Create")
object ProductCreate : NavKey
