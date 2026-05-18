package org.dsqrwym.standard.di.auth

import org.dsqrwym.shared.di.auth.SharedAuthScope
import org.dsqrwym.standard.data.auth.AuthApi
import org.dsqrwym.standard.data.auth.AuthRepository
import org.dsqrwym.standard.ui.viewmodels.auth.LoginViewModel
import org.dsqrwym.standard.ui.viewmodels.auth.RegisterViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val standardAuthModule = module {
    // Bind login repository for Standard variant
    single<AuthApi> { AuthApi(get()) }
    single<AuthRepository> { AuthRepository(get(), get(), get()) }

    scope<SharedAuthScope> {
        scoped {
            RegisterViewModel(get(), get(), get(), get())
        }
        viewModel<LoginViewModel> {
            LoginViewModel(get(), get(), get())
        }
    }
}

