package org.dsqrwym.shared.data.category

import androidx.compose.runtime.Composable
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.category_sort_iva
import maian.shared.generated.resources.category_sort_level
import maian.shared.generated.resources.category_sort_name
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

enum class SharedCategoryType {
    PRIVATE,
    PUBLIC
}

enum class SharedCategorySelectField() {
    IVA,
    USER_ID,
    LEVEL,
    RELATIONS,
    TRANSLATIONS
}

enum class SharedCategorySortField {
    NAME,
    LEVEL,
    IVA
}

val sharedEnterpriseCategorySortFields = listOf(
    SharedCategorySortField.NAME,
    SharedCategorySortField.LEVEL,
    SharedCategorySortField.IVA,
)

fun SharedCategorySortField.toStringResource(): StringResource =
    when (this) {
        SharedCategorySortField.NAME -> SharedRes.string.category_sort_name
        SharedCategorySortField.LEVEL -> SharedRes.string.category_sort_level
        SharedCategorySortField.IVA -> SharedRes.string.category_sort_iva
    }

@Composable
fun SharedCategorySortField.displayName(): String = stringResource(toStringResource())

enum class SharedCategoryProductFilterMode {
    SELF,
    DESCENDANT
}
