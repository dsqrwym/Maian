package org.dsqrwym.shared.di.auth

import org.dsqrwym.shared.data.auth.SharedAuthRepository
import org.dsqrwym.shared.data.auth.SharedAuthApi
import org.koin.dsl.module

val sharedAuthModule = module {
    // 提供 AuthApiInterface（单例）
    single { SharedAuthApi(get()) }

    // 提供 AuthRepository（单例）
    single { SharedAuthRepository(get()) }
}