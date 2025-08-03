package org.dsqrwym.standard.di

import org.dsqrwym.standard.ui.viewmodels.auth.AuthViewModel
import org.koin.dsl.module

val standardModule = module {
    single {
        AuthViewModel()
    }
}