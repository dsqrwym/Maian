package org.dsqrwym.shared.di.location

import org.dsqrwym.shared.data.location.SharedLocationRepository
import org.dsqrwym.shared.data.location.SharedLocationsApi
import org.koin.dsl.module

val sharedLocationModule = module {
    single { SharedLocationsApi(get()) }
    single { SharedLocationRepository(get()) }
}
