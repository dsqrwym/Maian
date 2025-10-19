package org.dsqrwym.shared.util.navigation

fun isSameRoute(a: Any?, b: Any?): Boolean {
    if (a == null || b == null) return false

    val routeA = a.toString()
    val routeB = b.toString()

    val result = routeA.startsWith(routeB) || routeB.startsWith(routeA)

//    SharedLog.log(
//        tag = "isSameRoute", message = """
//        routeA = $routeA
//        routeB = $routeB
//        a = $a
//        b = $b
//        result = $result
//    """.trimIndent()
//    )

    return result
}