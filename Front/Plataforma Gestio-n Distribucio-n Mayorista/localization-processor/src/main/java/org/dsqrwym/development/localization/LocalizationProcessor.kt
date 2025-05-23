package org.dsqrwym.development.localization

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.io.File
import java.util.Locale

class LocalizationProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>
) : SymbolProcessor {
    private var processed = false

    companion object {
        const val OPTION_LOCALIZATION_JSON_PATH = "localization.json.path"
        const val OPTION_BASE_LOCALE = "localization.base.locale"
        const val OPTION_OUTPUT_PACKAGE = "localization.output.package"
        const val OPTION_OUTPUT_FILE = "localization.output.file"

        private val KOTLIN_KEYWORDS = setOf(
            "as", "as?", "break", "class", "continue", "do", "else", "false", "for", "fun",
            "if", "in", "interface", "is", "!is", "null", "object", "package", "return", "super",
            "this", "throw", "true", "try", "typealias", "typeof", "val", "var", "when", "while",
        )
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (processed) {
            return emptyList()
        }
        // 从 KSP 选项获取资源路径。这需要在 build.gradle 中配置。
        val jsonPath = options[OPTION_LOCALIZATION_JSON_PATH]
            ?: run { logger.error("KSP 选项 '$OPTION_LOCALIZATION_JSON_PATH' 必须设置。"); return emptyList() }
        val outputPackageName = options[OPTION_OUTPUT_PACKAGE]
            ?: run { logger.error("KSP 选项 '$OPTION_OUTPUT_PACKAGE' 必须设置。"); return emptyList() }
        val baseLocale = options[OPTION_BASE_LOCALE]
            ?: run { logger.error("KSP 选项 '$OPTION_BASE_LOCALE' 必须设置 (例如 'en' 对应 en.json)。"); return emptyList() }
        val outputFileName = options[OPTION_OUTPUT_FILE]
            ?: "Language"

        logger.info("开始本地化 KSP 处理...")
        logger.info(" -> JSON 路径: $jsonPath")
        logger.info(" -> 输出包名: $outputPackageName")
        logger.info(" -> 用于生成接口键的基础区域设置: $baseLocale")

        val resourcesDir = File(jsonPath)
        if (!resourcesDir.exists() || !resourcesDir.isDirectory) {
            logger.error("无效的 JSON 路径: '$jsonPath' 不存在或不是一个目录。")
            return emptyList()
        }

        // 获取所有 .json 文件，忽略大小写
        val jsonFiles =
            resourcesDir.listFiles { _, name -> name.endsWith(".json", ignoreCase = true) }
                ?.toList()
        if (jsonFiles.isNullOrEmpty()) {
            logger.warn("在 '$jsonPath' 中没有找到 JSON 文件。跳过本地化代码生成。")
            return emptyList()
        }

        val baseLocaleFileName = "$baseLocale.json"
        val baseLocaleFile =
            jsonFiles.find { it.name.equals(baseLocaleFileName, ignoreCase = true) }
                ?: run {
                    logger.error("基础区域设置文件 '$baseLocaleFileName' 未在 '$jsonPath' 中找到。找到的文件: ${jsonFiles.joinToString { it.name }}。")
                    return emptyList()
                }

        val baseJsonContent = baseLocaleFile.readText()
        // 配置 Json 解析器，使其更宽容，例如忽略未知键或允许注释（如果您的 JSON 源包含它们）
        val jsonParser = Json { ignoreUnknownKeys = true; isLenient = true }
        val baseFlatMap = try {
            flattenJson(logger, jsonParser.parseToJsonElement(baseJsonContent))
        } catch (e: Exception) {
            logger.error("解析基础区域设置文件 '$baseLocaleFileName' 失败: ${e.message}")
            return emptyList()
        }

        // 生成属性名 (例如 login_title) 到 JSON 键 (例如 login.title) 的映射列表
        val interfaceProperties = baseFlatMap.keys.map { jsonKey ->
            keyToPropertyName(jsonKey) to jsonKey
        }.distinctBy { it.first }.sortedBy { it.first } // 按属性名排序并去重

        if (interfaceProperties.isEmpty()) {
            logger.warn("在基础区域设置文件 '$baseLocaleFileName' 中没有找到任何键。跳过代码生成。")
            return emptyList()
        }

        // 1. 生成 Language.kt 接口
        generateLanguageInterface(interfaceProperties, outputPackageName, outputFileName)

        val generatedLangObjects = mutableListOf<Pair<String, String>>() // 保存 (区域设置名称, 生成的类名)

        // 2. 为每个 JSON 文件生成 Lang<Locale>.kt 对象
        jsonFiles.forEach { jsonFile ->
            val localeNameFromFile = jsonFile.nameWithoutExtension // 例如: "en", "zh-CN"
            val currentJsonContent = jsonFile.readText()
            val currentFlatMap = try {
                flattenJson(logger, jsonParser.parseToJsonElement(currentJsonContent))
            } catch (e: Exception) {
                logger.error("解析区域设置文件 '${jsonFile.name}' 失败: ${e.message}。跳过此文件。")
                return@forEach // continue to next file
            }


            val langClassName = generateLangObject(
                localeNameFromFile,
                interfaceProperties, // 使用从基础区域设置获取的属性列表
                currentFlatMap,      // 当前区域设置的实际值
                outputPackageName,
                outputFileName
            )
            generatedLangObjects.add(localeNameFromFile to langClassName)
        }

        // 3. 生成包含语言映射的辅助类 (GeneratedLocalizationMap.kt)
        if (generatedLangObjects.isNotEmpty()) {
            generateLocalizationMapProvider(generatedLangObjects, outputPackageName, outputFileName)
        }

        processed = true
        logger.info("本地化 KSP 处理成功完成。")
        return emptyList()
    }

    private fun generateLanguageInterface(
        properties: List<Pair<String, String>>, // (属性名, JSON键)
        packageName: String,
        fileName: String
    ) {
        // Dependencies(false) 表示生成的文件不直接依赖于特定的源文件，
        // 而是依赖于 KSP 处理器本身或其读取的任意文件。
        // 这意味着如果 JSON 文件更改，KSP 可能需要手动触发（例如 clean build）或通过其他机制来重新生成。
        // 更高级的 KSP 用法可能会尝试将 JSON 文件注册为 KSP 的输入源，但这比较复杂。
        val dependencies = Dependencies(false)
        val file = codeGenerator.createNewFile(dependencies, packageName, fileName)

        file.bufferedWriter().use { writer ->
            writer.appendLine("package $packageName")
            writer.appendLine()
            writer.appendLine("/** Generated base language interface. Do not edit manually. */")
            writer.appendLine("interface $fileName {")
            properties.forEach { (propertyName, _) ->
                writer.appendLine("    val $propertyName: String")
            }
            writer.appendLine("}")
        }
        logger.info("已生成接口 $packageName.$fileName")
    }

    private fun generateLangObject(
        locale: String, // 例如 "en", "zh-CN"
        interfaceProperties: List<Pair<String, String>>, // (属性名, JSON键) - 来自基础区域设置
        currentLocaleValues: Map<String, String>, // 当前区域设置的扁平化 JSON (JSON键 -> 值)
        packageName: String,
        fileName: String
    ): String {
        val langClassNameSuffix = sanitizeLocaleForClassName(locale)
        val className = "Lang$langClassNameSuffix"
        val dependencies = Dependencies(false)
        val file = codeGenerator.createNewFile(dependencies, packageName, className)

        file.bufferedWriter().use { writer ->
            writer.appendLine("package $packageName")
            writer.appendLine()
            writer.appendLine("/** Generated language object for locale '$locale'. Do not edit manually. */")
            // 使用 internal 可见性，以便这些对象主要在模块内部通过 LocalizationManager 使用
            writer.appendLine("internal object $className : $fileName {")
            interfaceProperties.forEach { (propertyName, jsonKey) ->
                val value = currentLocaleValues[jsonKey]
                if (value == null) {
                    logger.warn("语言 '$locale': 属性 '$propertyName' (对应 JSON 键 '$jsonKey') 的值缺失。将使用空字符串。")
                    writer.appendLine("    override val $propertyName: String = \"\" // FIXME: 键 '$jsonKey' 在语言 '$locale' 中缺失")
                } else {
                    writer.appendLine("    override val $propertyName: String = \"${value.escapeKotlinStringLiteral()}\"")
                }
            }
            writer.appendLine("}")
        }
        logger.info("已为语言 '$locale' 生成对象 $packageName.$className")
        return className
    }

    private fun generateLocalizationMapProvider(
        generatedLangs: List<Pair<String, String>>, // (区域设置名称, 生成的类名)
        packageName: String,
        interfaceFileName: String
    ) {
        val fileName = "${interfaceFileName}Map"
        val dependencies = Dependencies(false)
        val file = codeGenerator.createNewFile(dependencies, packageName, fileName)

        file.bufferedWriter().use { writer ->
            writer.appendLine("package $packageName")
            writer.appendLine()
            // 假设 Language.kt 在同一个包中，否则需要导入
            // writer.appendLine("import $packageName.Language")
            writer.appendLine()
            writer.appendLine("import androidx.compose.runtime.State")
            writer.appendLine("import androidx.compose.runtime.mutableStateOf")
            writer.appendLine("import androidx.compose.runtime.derivedStateOf")
            writer.appendLine()
            writer.appendLine("/** Provides a map of generated language implementations. Do not edit manually. */")
            writer.appendLine("internal object $fileName {") // internal 限制其使用范围
            // 添加默认语言常量
            writer.appendLine("    private const val DEFAULT_LANGUAGE = \"zh-CN\"")
            // 添加状态管理
            writer.appendLine("    private val _currentLanguage = mutableStateOf(DEFAULT_LANGUAGE)")
            writer.appendLine()
            // 生成语言映射
            writer.appendLine("    private val map = mapOf(")
            generatedLangs.forEach { (locale, className) ->
                writer.appendLine("        \"$locale\" to $className,")
            }
            writer.appendLine("    )")
            writer.appendLine()
            writer.appendLine("    val currentLanguageState: State<String> get() = _currentLanguage")
            writer.appendLine()
//            writer.appendLine("    fun getMap(): Map<String, $interfaceFileName> {") // 返回 Language 类型
//            writer.appendLine("        return mapOf(")
//            generatedLangs.forEach { (locale, className) ->
//                writer.appendLine("            \"$locale\" to $className,")
//            }
            // 添加当前语言字符串的获取方法
            writer.appendLine("    val currentStrings: State<${interfaceFileName}> = derivedStateOf {")
            writer.appendLine("        map[_currentLanguage.value] ?: map[DEFAULT_LANGUAGE]!!")
            writer.appendLine("    }")
            writer.appendLine()
            // 添加方法
            writer.appendLine("    fun setCurrentLanguage(language: String) {")
            writer.appendLine("        _currentLanguage.value = if (map.containsKey(language)) language else DEFAULT_LANGUAGE")
            writer.appendLine("    }")
            writer.appendLine()
            writer.appendLine("    fun getCurrentLanguage(): String = _currentLanguage.value")
            writer.appendLine()
            //writer.appendLine("    fun getMap(): $interfaceFileName = map[_currentLanguage.value] ?: map[DEFAULT_LANGUAGE]!!")
            writer.appendLine("}")
        }
        logger.info("已生成 $packageName.$fileName，包含 ${generatedLangs.size} 种语言的映射。")
    }

    private fun flattenJson(
        logger: KSPLogger,
        element: JsonElement,
        prefix: String = "",
        // 使用一个可变的 Map 在递归中累积结果，以提高效率
        result: MutableMap<String, String> = mutableMapOf()
    ): Map<String, String> {
        when (element) {
            is JsonObject -> {
                element.entries.forEach { (key, value) ->
                    // 对 JSON 键中的点进行处理，如果需要，但通常点是作为分隔符的
                    val newPrefix = if (prefix.isEmpty()) key else "$prefix.$key"
                    when (value) {
                        is JsonPrimitive -> {
                            // 确保我们只处理字符串或将其转换为字符串
                            if (value.isString) {
                                result[newPrefix] = value.content
                            } else {
                                result[newPrefix] = value.content // 或 value.toString()，取决于您的需求
                                logger.info("键 '$newPrefix' 的值不是字符串而是 JsoPrimitive: '${value.content}'。已使用其字符串表示。")
                            }
                        }

                        is JsonObject -> flattenJson(logger, value, newPrefix, result) // 递归调用
                        // is kotlinx.serialization.json.JsonArray -> logger.warn("JSON 数组目前不支持扁平化处理，键 '$newPrefix' 的值将被忽略。")
                        else -> logger.warn("在键 '$newPrefix' 处遇到不支持的 JSON 元素类型: ${value::class.simpleName}。")
                    }
                }
            }

            else -> {
                if (prefix.isEmpty()) { // 仅当根元素不是对象时报错
                    logger.error("JSON 的根元素必须是一个对象。实际类型: ${element::class.simpleName}")
                }
                // 如果不是根元素，则说明遇到了非对象/非基本类型的值，这在上面的 when(value) 中处理
            }
        }
        return result
    }

    private fun keyToPropertyName(jsonKey: String): String {
        // 1. 将点（或其他分隔符，如果您的键使用的话）替换为下划线
        val baseName = jsonKey.replace('.', '_')
            // 2. 移除非字母数字和下划线以外的所有字符
            .replace(Regex("[^A-Za-z0-9_]"), "")
            // 3. 将多个连续的下划线替换为单个下划线
            .replace(Regex("_{2,}"), "_")
            // 4. 移除可能因替换而产生的前导或尾随下划线
            .trim('_')

        return when {
            // 5. 如果处理后名称为空，则提供一个占位符
            baseName.isEmpty() -> {
                logger.warn("为 JSON 键 '$jsonKey' 生成了空的属性名。将使用占位符 '_invalidKey_'。")
                "_invalidKey_"
            }
            // 6. 检查是否为 Kotlin 关键字，如果是，则用反引号括起来
            KOTLIN_KEYWORDS.contains(baseName) -> "`$baseName`"
            // 7. 如果属性名以数字开头，则在其前面添加下划线
            baseName.first().isDigit() -> "_$baseName"
            // 8. 否则，使用处理后的名称
            else -> baseName
        }
    }

    private fun sanitizeLocaleForClassName(locale: String): String {
        // 例如: "zh-CN" -> "ZhCn", "en_US" -> "EnUs", "en" -> "En"
        return locale.split('-', '_').joinToString("") { part ->
            part.lowercase(Locale.getDefault())
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }.ifEmpty { "Default" } // 如果区域设置字符串为空，则使用 "Default"
    }

    private fun String.escapeKotlinStringLiteral(): String {
        return this
            .replace("\\", "\\\\")  // 1. 处理反斜杠 (必须最先处理)
            .replace("\"", "\\\"")  // 2. 处理双引号
            .replace("\n", "\\n")   // 3. 处理换行符
            .replace("\r", "\\r")   // 4. 处理回车符
            .replace("\t", "\\t")   // 5. 处理制表符
            .replace("\b", "\\b")   // 6. 处理退格符
            // 7. 安全地处理美元符号，以避免与 Kotlin 的字符串模板冲突
            //    将其替换为 ${'$'} 可以在字符串模板中明确表示一个字面美元符号
            .replace("\$", "\${\'\$\'}")
    }
}

class LocalizationProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return LocalizationProcessor(
            environment.codeGenerator,
            environment.logger,
            environment.options
        )
    }
}