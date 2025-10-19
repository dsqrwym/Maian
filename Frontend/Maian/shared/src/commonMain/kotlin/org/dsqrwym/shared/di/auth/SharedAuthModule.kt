package org.dsqrwym.shared.di.auth

import org.dsqrwym.shared.data.auth.SharedAuthApi
import org.dsqrwym.shared.data.auth.SharedAuthRepository
import org.dsqrwym.shared.data.auth.session.AuthSessionViewModel
import org.dsqrwym.shared.ui.viewmodels.auth.SharedResetPasswordViewModel
import org.koin.dsl.module

val sharedAuthModule = module {
    // 提供 SharedAuthApi（单例）
    single { SharedAuthApi(get()) }

    // 提供 SharedAuthRepository（单例）
    single { SharedAuthRepository(get()) }
    // all callers observe the same auth state/effects across the app.
    // 作为单例注册，保证全局共享同一份会话状态与事件。
    single { AuthSessionViewModel(get(), get()) }
    // 提供共享的重置密码 ViewModel（单例），供各版本使用
    single { SharedResetPasswordViewModel(get(), get()) }
    // 提供共享的注册/创建账户 ViewModel（单例）
}