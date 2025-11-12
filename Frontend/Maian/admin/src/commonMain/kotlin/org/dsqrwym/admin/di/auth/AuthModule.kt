package org.dsqrwym.admin.di.auth

import org.dsqrwym.admin.data.auth.AuthRepository
import org.dsqrwym.admin.ui.viewmodels.auth.LoginViewModel
import org.dsqrwym.shared.di.auth.SharedAuthScope
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val adminAuthModule = module {
    single<AuthRepository> { AuthRepository(get()) }

    scope<SharedAuthScope> {
        viewModel<LoginViewModel> {
            LoginViewModel(get(), get(), get())
        }
    }
}

