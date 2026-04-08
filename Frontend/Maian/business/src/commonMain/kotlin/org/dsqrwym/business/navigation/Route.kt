package org.dsqrwym.business.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

val BusinessNavSerializersModule = SerializersModule {
    polymorphic(NavKey::class){
        subclass(Categories::class, Categories.serializer())
        subclass(CategoryCreate::class, CategoryCreate.serializer())
        subclass(CategoryEdit::class, CategoryEdit.serializer())
        subclass(ProductWorkspaceMainPane::class, ProductWorkspaceMainPane.serializer())
        subclass(ProductWorkspaceAuxPane::class, ProductWorkspaceAuxPane.serializer())
    }
}

@Serializable
@SerialName("Categories")
object Categories : NavKey

@Serializable
@SerialName("Categories-Create")
object CategoryCreate : NavKey

@Serializable
@SerialName("Categories-Edit")
data class CategoryEdit(val id: String): NavKey
