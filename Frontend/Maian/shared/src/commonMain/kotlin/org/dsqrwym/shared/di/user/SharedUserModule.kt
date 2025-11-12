package org.dsqrwym.shared.di.user

import org.dsqrwym.shared.data.user.SharedUserApi
import org.koin.dsl.module

val sharedUserModule = module {
    single { SharedUserApi(get()) }
}