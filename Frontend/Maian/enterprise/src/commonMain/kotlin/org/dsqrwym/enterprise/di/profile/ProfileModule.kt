package org.dsqrwym.enterprise.di.profile

import org.dsqrwym.enterprise.data.profile.WholesalerProfileApi
import org.dsqrwym.enterprise.data.profile.WholesalerProfileRepository
import org.dsqrwym.enterprise.ui.viewmodels.profile.WholesalerProfileEditViewModel
import org.dsqrwym.enterprise.ui.viewmodels.profile.WholesalerProfileViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val profileModule = module {
    single { WholesalerProfileApi(get()) }
    single { WholesalerProfileRepository(get()) }
    viewModel<WholesalerProfileViewModel> { WholesalerProfileViewModel(get(), get()) }
    viewModel<WholesalerProfileEditViewModel> {
        WholesalerProfileEditViewModel(get(), get(), get(), get(), get())
    }
}
