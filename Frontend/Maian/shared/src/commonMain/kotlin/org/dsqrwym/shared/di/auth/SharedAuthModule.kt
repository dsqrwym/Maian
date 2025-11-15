package org.dsqrwym.shared.di.auth

import org.dsqrwym.shared.data.auth.SharedAuthApi
import org.dsqrwym.shared.data.auth.SharedAuthRepository
import org.dsqrwym.shared.data.auth.session.AuthSessionViewModel
import org.dsqrwym.shared.ui.viewmodels.auth.SharedResetPasswordViewModel
import org.koin.core.component.KoinScopeComponent
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module

object SharedAuthScope : KoinScopeComponent {
    private const val SHARED_AUTH_SCOPE_ID = "shared_auth_scope"
    private var _scope: Scope? = null

    override
    val scope: Scope get() = _scope ?: createScope()

    fun createScope(): Scope {
        if (_scope == null || !_scope!!.isNotClosed()) {
            _scope = getKoin().getOrCreateScope(SHARED_AUTH_SCOPE_ID, named<SharedAuthScope>())
        }
        return _scope!!
    }

    fun closeScope() {
        _scope?.close()
        _scope = null
    }
}

val sharedAuthModule = module {
    // 提供 SharedAuthApi（单例）
    single { SharedAuthApi(get()) }

    // 提供 SharedAuthRepository（单例）
    single { SharedAuthRepository(get()) }
    // all callers observe the same auth state/effects across the app.
    // 作为单例注册，保证全局共享同一份会话状态与事件。
    single { AuthSessionViewModel(get(), get()) }

    // 提供共享的重置密码 ViewModel，供各版本使用
    scope<SharedAuthScope> {
        scoped {
            SharedResetPasswordViewModel(get(), get())
        }
    }

}