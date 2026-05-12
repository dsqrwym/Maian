package org.dsqrwym.shared.di.profile

import org.dsqrwym.shared.data.profile.SharedWholesalerProfileApi
import org.dsqrwym.shared.data.profile.SharedWholesalerProfileRepository
import org.koin.dsl.module

val sharedProfileModule = module {
    single { SharedWholesalerProfileApi(get()) }
    single { SharedWholesalerProfileRepository(get()) }
}