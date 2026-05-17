package org.dsqrwym.shared.data.orders

import org.dsqrwym.shared.data.orders.pdf.SharedOrderPdfActionResult
import org.dsqrwym.shared.network.model.SharedResponseResult

interface OrderPdfActionsRepository {
    suspend fun previewOrderPdf(id: String): SharedResponseResult<SharedOrderPdfActionResult>

    suspend fun downloadOrderPdf(id: String): SharedResponseResult<SharedOrderPdfActionResult>
}
