package org.dsqrwym.admin.domain.category

import org.dsqrwym.admin.data.categories.dto.CategoryResponse
import org.dsqrwym.shared.domain.category.CategoryNode

fun CategoryResponse.toDomain(): CategoryNode =
    CategoryNode(
        id = id,
        name = name,
        iva = iva,
        ownerUserId = userId,
        parent = parent?.toDomain(),
        children = children?.map { it.toDomain() },
        childrenCount = childrenCount,
        translations = categoryTranslations.orEmpty()
    )
