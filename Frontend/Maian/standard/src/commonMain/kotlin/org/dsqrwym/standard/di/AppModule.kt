package org.dsqrwym.standard.di

import org.dsqrwym.standard.data.auth.AuthApi
import org.dsqrwym.standard.data.auth.AuthRepository
import org.dsqrwym.standard.ui.viewmodels.auth.LoginViewModel
import org.dsqrwym.standard.ui.viewmodels.auth.RegisterViewModel
import org.dsqrwym.standard.ui.viewmodels.auth.SharedAuthViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val standardModule = module {
    // Bind login repository for Standard variant
    single<AuthApi> { AuthApi(get()) }
    single<AuthRepository> { AuthRepository(get(), get()) }
    single<RegisterViewModel> {
        RegisterViewModel(get(), get(), get(), get())
    }
    viewModel<LoginViewModel> {
        LoginViewModel(get(), get(), get())
    }

    // Optional: existing VM
    single {
        SharedAuthViewModel(get(), get(), get(), get())
    }
}