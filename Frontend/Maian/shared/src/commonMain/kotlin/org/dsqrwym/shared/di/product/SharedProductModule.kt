package org.dsqrwym.shared.di.product

import org.dsqrwym.shared.data.products.SharedProductApi
import org.koin.dsl.module

val sharedProductModule = module {
    single { SharedProductApi(get()) }
}