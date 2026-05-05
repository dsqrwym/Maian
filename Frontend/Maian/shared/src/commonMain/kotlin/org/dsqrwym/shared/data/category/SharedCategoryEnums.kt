package org.dsqrwym.shared.data.category

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
    LEVEL
}

enum class SharedCategoryProductFilterMode {
    SELF,
    DESCENDANT
}
