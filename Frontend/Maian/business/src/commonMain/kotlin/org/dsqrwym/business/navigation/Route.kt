package org.dsqrwym.business.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@SerialName("Categories")
object Categories : NavKey

@Serializable
@SerialName("Categories-Create")
object CategoryCreate : NavKey

@Serializable
@SerialName("Categories-Edit")
data class CategoryEdit(val id: String): NavKey
