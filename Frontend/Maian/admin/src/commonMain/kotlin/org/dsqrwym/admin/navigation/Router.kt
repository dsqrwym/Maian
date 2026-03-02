package org.dsqrwym.admin.navigation

import kotlinx.serialization.modules.SerializersModule
import org.dsqrwym.business.navigation.BusinessNavSerializersModule

val AdminNavSerializersModule = SerializersModule {
    include(BusinessNavSerializersModule)
}