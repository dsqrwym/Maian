package org.dsqrwym.standard.di.browse

import org.dsqrwym.standard.data.browse.RetailBrowseRepository
import org.dsqrwym.standard.data.cart.StandardCartApi
import org.dsqrwym.standard.data.cart.StandardCartRepository
import org.dsqrwym.standard.ui.viewmodels.cart.StandardCartViewModel
import org.dsqrwym.standard.ui.viewmodels.browse.*
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val standardBrowseModule = module {
    single { RetailBrowseRepository(get(), get(), get()) }
    single { StandardCartApi(get()) }
    single { StandardCartRepository(get()) }
    viewModel<ProductBrowseViewModel> { ProductBrowseViewModel(get(), get()) }
    viewModel<ProductDetailViewModel> { ProductDetailViewModel(get(), get(), get()) }
    viewModel<CategoryBrowseViewModel> { CategoryBrowseViewModel(get(), get()) }
    viewModel<WholesalersViewModel> { WholesalersViewModel(get(), get()) }
    viewModel<WholesalerHomeViewModel> { WholesalerHomeViewModel() }
    viewModel<WholesalerProfileViewModel> { WholesalerProfileViewModel(get(), get()) }
    viewModel<StandardCartViewModel> { StandardCartViewModel(get(), get(), get()) }
}
