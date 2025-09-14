package org.dsqrwym.standard.di

import org.dsqrwym.standard.ui.viewmodels.auth.SharedAuthViewModel
import org.koin.dsl.module

val standardModule = module {
    single {
        SharedAuthViewModel(get(), get(), get())
    }
}