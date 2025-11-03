package org.dsqrwym.admin.data.categories.model

import kotlinx.serialization.Serializable

@Serializable
data class CategoryData(
    val id: Long,
    val userId: String?,
    val name: String,
    val iva: Double?,
    val parent: CategoryData?,
    val children: List<CategoryData>?,
    val lang: Map<String, String>?,
    val createdAt: String,
) {
    fun isPublic(): Boolean = userId == null
    fun getChildrenCount(): Int = children?.size ?: 0
    fun getParentName(): String? = parent?.name
    fun getPath(separator: Char = '>'): List<String> {
        // 使用递归向上查找 parent
        return buildList {
            var current: CategoryData? = this@CategoryData
            while (current != null) {
                add(current.name)
                current = current.parent
            }
        }.reversed().joinToString("@$separator@").split("@")
    }
}

fun getFakeCategories(): List<CategoryData> {
    val now = "2025-10-26T10:00:00Z"

    // 第三级（叶子节点）
    val tropical = CategoryData(
        id = 306,
        userId = "user-123",
        name = "Tropical Fruits",
        iva = 7.0,
        parent = null, // 临时置空，稍后填充
        children = null,
        lang = mapOf("en" to "Tropical Fruits", "zh" to "热带水果", "es" to "Frutas tropicales"),
        createdAt = now
    )

    val citrus = CategoryData(
        id = 302,
        userId = "user-123",
        name = "Citrus Fruits",
        iva = 7.0,
        parent = null,
        children = null,
        lang = mapOf("en" to "Citrus Fruits", "zh" to "柑橘类水果", "es" to "Frutas cítricas"),
        createdAt = now
    )

    // 第二级
    val fruits = CategoryData(
        id = 203,
        userId = "user-123",
        name = "Fruits",
        iva = 7.0,
        parent = null, // 稍后由顶层设定
        children = listOf(
            tropical.copy(parent = null), // 临时
            citrus.copy(parent = null)
        ),
        lang = mapOf("en" to "Fruits", "zh" to "水果", "es" to "Frutas"),
        createdAt = now
    )

    val vegetables = CategoryData(
        id = 20223,
        userId = "user-123",
        name = "Vegetables",
        iva = 6.0,
        parent = null,
        children = listOf(),
        lang = mapOf("en" to "Vegetables", "zh" to "蔬菜", "es" to "Verduras"),
        createdAt = now
    )

    // 顶层
    val food = CategoryData(
        id = 101,
        userId = "user-123",
        name = "Food",
        iva = 5.0,
        parent = null,
        children = listOf(fruits, vegetables),
        lang = mapOf("en" to "Food", "zh" to "食品", "es" to "Alimentos"),
        createdAt = now
    )

    // 回填 parent 引用
    val fruitsWithParent = fruits.copy(
        id = 12,
        parent = food,
        children = listOf(
            tropical.copy(parent = fruits),
            citrus.copy(parent = fruits)
        )
    )
    val vegetablesWithParent = vegetables.copy(parent = food, id = 122)
    val foodWithChildren = food.copy(id = 932, children = listOf(fruitsWithParent, vegetablesWithParent))

    return listOf(foodWithChildren, fruitsWithParent, vegetablesWithParent, tropical, citrus, fruits)
}