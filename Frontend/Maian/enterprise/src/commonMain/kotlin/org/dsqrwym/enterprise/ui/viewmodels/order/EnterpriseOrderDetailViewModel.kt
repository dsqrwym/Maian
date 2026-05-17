package org.dsqrwym.enterprise.ui.viewmodels.order

import org.dsqrwym.enterprise.data.order.EnterpriseOrderRepository
import org.dsqrwym.shared.ui.viewmodels.MySnackbarViewModel
import org.dsqrwym.shared.ui.viewmodels.orders.OrderDetailViewModel

class EnterpriseOrderDetailViewModel(
    repository: EnterpriseOrderRepository,
    mySnackbarHostState: MySnackbarViewModel,
) : OrderDetailViewModel(repository, mySnackbarHostState)
