package org.dsqrwym.enterprise.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.dsqrwym.business.navigation.BusinessNavSerializersModule

val EnterpriseSerializersModule = SerializersModule {
    polymorphic(NavKey::class) {
        subclass(RegisterScreen::class, RegisterScreen.serializer())
        subclass(Products::class, Products.serializer())
        subclass(ProductCreate::class, ProductCreate.serializer())
        subclass(ProductEdit::class, ProductEdit.serializer())
    }
    include(BusinessNavSerializersModule)
}

@Serializable
@SerialName("Register")
object RegisterScreen : NavKey

@Serializable
@SerialName("Products")
object Products : NavKey

@Serializable
@SerialName("Product-Create")
object ProductCreate : NavKey

@Serializable
@SerialName("Product-Edit")
data class ProductEdit(val id: String) : NavKey