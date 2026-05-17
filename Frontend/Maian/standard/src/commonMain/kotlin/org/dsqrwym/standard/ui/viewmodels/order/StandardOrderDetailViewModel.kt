package org.dsqrwym.standard.ui.viewmodels.order

import org.dsqrwym.shared.ui.viewmodels.MySnackbarViewModel
import org.dsqrwym.shared.ui.viewmodels.orders.OrderDetailViewModel
import org.dsqrwym.standard.data.order.StandardOrderRepository

class StandardOrderDetailViewModel(
    repository: StandardOrderRepository,
    mySnackbarHostState: MySnackbarViewModel,
) : OrderDetailViewModel(repository, mySnackbarHostState)
