package org.dsqrwym.enterprise.di.auth

import org.dsqrwym.enterprise.data.auth.AuthApi
import org.dsqrwym.enterprise.data.auth.AuthRepository
import org.dsqrwym.enterprise.ui.viewmodels.auth.LoginViewModel
import org.dsqrwym.enterprise.ui.viewmodels.auth.RegisterViewModel
import org.dsqrwym.shared.di.auth.SharedAuthScope
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val enterpriseAuthModule = module {
    // Bind login repository for Standard variant
    single<AuthApi> { AuthApi(get()) }
    single<AuthRepository> { AuthRepository(get(), get(), get()) }

    scope<SharedAuthScope> {
        scoped {
            RegisterViewModel(get(), get(), get(), get(), get())
        }
        viewModel<LoginViewModel> {
            LoginViewModel(get(), get(), get())
        }
    }
}

