package org.dsqrwym.standard.di.browse

import org.dsqrwym.standard.data.browse.RetailBrowseRepository
import org.dsqrwym.standard.ui.viewmodels.browse.BrowseScopeStateHolder
import org.dsqrwym.standard.ui.viewmodels.browse.CategoryBrowseViewModel
import org.dsqrwym.standard.ui.viewmodels.browse.WholesalerHomeViewModel
import org.dsqrwym.standard.ui.viewmodels.browse.WholesalersViewModel
import org.dsqrwym.standard.ui.viewmodels.browse.ProductBrowseViewModel
import org.dsqrwym.standard.ui.viewmodels.browse.ProductDetailViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val standardBrowseModule = module {
    single { RetailBrowseRepository(get(), get(), get()) }
    single { BrowseScopeStateHolder() }
    viewModel<ProductBrowseViewModel> { ProductBrowseViewModel(get(), get()) }
    viewModel<ProductDetailViewModel> { ProductDetailViewModel(get(), get()) }
    viewModel<CategoryBrowseViewModel> { CategoryBrowseViewModel(get(), get()) }
    viewModel<WholesalersViewModel> { WholesalersViewModel(get()) }
    viewModel<WholesalerHomeViewModel> { WholesalerHomeViewModel() }
}
