package org.dsqrwym.enterprise.navigation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("register")
object RegisterScreen

@Serializable
@SerialName("Categories")
object Categories

@Serializable
@SerialName("Categories-Create")
object CategoryCreate

@Serializable
@SerialName("Categories-Edit")
data class CategoryEdit(val id: String)