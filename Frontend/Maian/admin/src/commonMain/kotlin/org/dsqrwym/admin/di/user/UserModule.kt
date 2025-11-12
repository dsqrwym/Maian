package org.dsqrwym.admin.di.user

import org.dsqrwym.admin.data.user.UserRepository
import org.koin.dsl.module

val userModule = module {
    single { UserRepository(get()) }
}