package org.dsqrwym.standard.di.profile

import org.dsqrwym.standard.data.profile.RetailerProfileApi
import org.dsqrwym.standard.data.profile.RetailerProfileRepository
import org.dsqrwym.standard.ui.viewmodels.profile.RetailerProfileEditViewModel
import org.dsqrwym.standard.ui.viewmodels.profile.RetailerProfileViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val standardProfileModule = module {
    single { RetailerProfileApi(get(), get()) }
    single { RetailerProfileRepository(get()) }
    viewModel<RetailerProfileViewModel> { RetailerProfileViewModel(get(), get()) }
    viewModel<RetailerProfileEditViewModel> {
        RetailerProfileEditViewModel(get(), get(), get(), get(), get(), get())
    }
}
