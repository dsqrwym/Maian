package org.dsqrwym.shared.di.category

import org.dsqrwym.shared.data.category.SharedCategoryApi
import org.koin.dsl.module

val sharedCategoryModule = module {
    single { SharedCategoryApi(get()) }
}