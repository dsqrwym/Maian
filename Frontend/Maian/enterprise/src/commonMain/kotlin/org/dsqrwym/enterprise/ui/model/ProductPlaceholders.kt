package org.dsqrwym.enterprise.ui.model

import org.dsqrwym.enterprise.domain.product.Product
import org.dsqrwym.enterprise.domain.product.ProductImage
import org.dsqrwym.shared.data.products.SharedProductStatus
import org.dsqrwym.shared.domain.category.CategorySummary

object ProductPlaceholders {
    fun fake(index: Int = 0): Product =
        Product(
            id = "fake-$index",
            name = "Fake Product $index",
            title = "Fake Title $index",
            mainImage = ProductImage(Long.MIN_VALUE, ""),
            code = "CODE-$index",
            totalStock = 0,
            minOrderQty = 0,
            minPrice = "",
            minPriceIva = "",
            translations = emptyList(),
            status = SharedProductStatus.INACTIVE,
            iva = 0.0,
            mainCategory = CategorySummary(Long.MIN_VALUE.toString(), "Fake Category")
        )

    fun generateFakeProducts(count: Int = 10): List<Product> =
        List(count) { fake(it) }
}
