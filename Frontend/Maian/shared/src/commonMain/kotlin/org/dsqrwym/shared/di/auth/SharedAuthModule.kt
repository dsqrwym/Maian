package org.dsqrwym.shared.di.auth

import org.dsqrwym.shared.data.auth.SharedAuthApi
import org.dsqrwym.shared.data.auth.SharedAuthRepository
import org.dsqrwym.shared.data.auth.session.AuthSessionViewModel
import org.koin.dsl.module

val sharedAuthModule = module {
    // 提供 AuthApiInterface（单例）
    single { SharedAuthApi(get()) }

    // 提供 AuthRepository（单例）
    single { SharedAuthRepository(get()) }
    // all callers observe the same auth state/effects across the app.
    // 作为单例注册，保证全局共享同一份会话状态与事件。
    single { AuthSessionViewModel() }
}