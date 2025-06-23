package org.dsqrwym.shared.di

import org.dsqrwym.shared.ui.viewmodel.SharedSnackbarViewModel
import org.koin.dsl.module

val sharedModule = module {
    single {
        SharedSnackbarViewModel()
    }
}