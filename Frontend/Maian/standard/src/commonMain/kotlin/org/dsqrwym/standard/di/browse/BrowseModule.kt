package org.dsqrwym.standard.di.browse

import org.dsqrwym.standard.data.browse.RetailBrowseRepository
import org.dsqrwym.standard.ui.viewmodels.browse.*
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val standardBrowseModule = module {
    single { RetailBrowseRepository(get(), get(), get()) }
    single { BrowseScopeStateHolder() }
    viewModel<ProductBrowseViewModel> { ProductBrowseViewModel(get(), get()) }
    viewModel<ProductDetailViewModel> { ProductDetailViewModel(get(), get()) }
    viewModel<CategoryBrowseViewModel> { CategoryBrowseViewModel(get(), get()) }
    viewModel<WholesalersViewModel> { WholesalersViewModel(get(), get()) }
    viewModel<WholesalerHomeViewModel> { WholesalerHomeViewModel() }
    viewModel<WholesalerProfileViewModel> { WholesalerProfileViewModel(get(), get()) }
}
