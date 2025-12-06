package org.dsqrwym.enterprise.di.products

import org.dsqrwym.enterprise.data.product.ProductRepository
import org.dsqrwym.enterprise.ui.viewmodels.products.ProductsListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val productsModule = module {
    single { ProductRepository(get()) }
    viewModel<ProductsListViewModel> { ProductsListViewModel(get(), get()) }
}