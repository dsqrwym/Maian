package org.dsqrwym.shared.util.formatter

/**
 * 一个简单的字符串格式化函数，模仿 sprintf，只支持顺序替换 %S 占位符。
 *
 * @param formatString 包含 %S 占位符的格式字符串。
 * @param args 用于替换占位符的参数列表。
 * @return 格式化后的字符串。
 * @throws IllegalArgumentException 如果参数数量不足以匹配所有的 %S 占位符。
 */
fun stringFormat(formatString: String, vararg args: Any?): String {
    var argIndex = 0 // 用于跟踪当前使用的参数索引

    // 使用正则表达式找到所有的 %S 占位符
    val regex = Regex("%S", RegexOption.IGNORE_CASE)
    val totalPlaceholders = regex.findAll(formatString).count()

    require(totalPlaceholders == args.size) {
        "占位符%S或%s和参数数量必须一致"
    }

    return regex.replace(formatString) { match ->
        // 每找到一个 %S，就尝试从 args 中取一个参数进行替换
        if (argIndex < args.size) {
            val replacement = args[argIndex].toString() // 将参数转换为字符串
            argIndex++ // 移动到下一个参数
            replacement
        } else {
            // 如果参数不够，则抛出异常
            throw IllegalArgumentException(
                "Format string requires $totalPlaceholders arguments but only ${args.size} were provided. " +
                        "Missing argument for placeholder #$${argIndex + 1} at position ${match.range.first} in '$formatString'."
            )
        }
    }
}