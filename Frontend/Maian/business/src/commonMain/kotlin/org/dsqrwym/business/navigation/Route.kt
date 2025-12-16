package org.dsqrwym.business.navigation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@SerialName("Categories")
object Categories

@Serializable
@SerialName("Categories-Create")
object CategoryCreate

@Serializable
@SerialName("Categories-Edit")
data class CategoryEdit(val id: String)
