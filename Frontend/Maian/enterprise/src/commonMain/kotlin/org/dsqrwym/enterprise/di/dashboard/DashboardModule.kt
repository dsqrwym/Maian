package org.dsqrwym.enterprise.di.dashboard

import org.dsqrwym.enterprise.data.dashboard.DashboardApi
import org.dsqrwym.enterprise.data.dashboard.DashboardRepository
import org.dsqrwym.enterprise.ui.viewmodels.dashboard.DashboardViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val dashboardModule = module {
    single { DashboardApi(get()) }
    single { DashboardRepository(get()) }
    viewModel<DashboardViewModel> { DashboardViewModel(get(), get()) }
}
