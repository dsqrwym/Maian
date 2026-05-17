package org.dsqrwym.enterprise.di.orders

import org.dsqrwym.enterprise.data.order.EnterpriseOrderRepository
import org.dsqrwym.enterprise.ui.viewmodels.order.EnterpriseOrderDetailViewModel
import org.dsqrwym.enterprise.ui.viewmodels.order.EnterpriseOrderHistoryViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val ordersModule = module {
    single { EnterpriseOrderRepository(get()) }
    viewModel<EnterpriseOrderHistoryViewModel> { EnterpriseOrderHistoryViewModel(get(), get()) }
    viewModel<EnterpriseOrderDetailViewModel> { EnterpriseOrderDetailViewModel(get(), get()) }
}
