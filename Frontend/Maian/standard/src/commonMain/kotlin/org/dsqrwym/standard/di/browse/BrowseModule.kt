package org.dsqrwym.standard.di.browse

import org.dsqrwym.standard.data.browse.RetailBrowseRepository
import org.dsqrwym.standard.ui.viewmodels.browse.CategoryBrowseViewModel
import org.dsqrwym.standard.ui.viewmodels.browse.DistributorHomeViewModel
import org.dsqrwym.standard.ui.viewmodels.browse.DistributorsViewModel
import org.dsqrwym.standard.ui.viewmodels.browse.ProductBrowseViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val standardBrowseModule = module {
    single { RetailBrowseRepository(get(), get(), get()) }
    viewModel<ProductBrowseViewModel> { ProductBrowseViewModel(get(), get()) }
    viewModel<CategoryBrowseViewModel> { CategoryBrowseViewModel(get(), get()) }
    viewModel<DistributorsViewModel> { DistributorsViewModel(get()) }
    viewModel<DistributorHomeViewModel> { DistributorHomeViewModel() }
}
