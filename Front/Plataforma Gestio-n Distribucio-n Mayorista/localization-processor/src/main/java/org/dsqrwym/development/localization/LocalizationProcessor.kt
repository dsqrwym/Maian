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

class LocalizationProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>
) : SymbolProcessor {
    private var processed = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (processed) {
            return emptyList()
        }

        // 从 KSP 选项获取资源路径。这需要在 build.gradle 中配置。
        val localizationJsonPath = options["localization.json.path"]
            ?: throw IllegalArgumentException("localization.json.path must be set")
        logger.info("Localization JSON path: $localizationJsonPath")

        val resourcesDir = File(localizationJsonPath)
        if (!resourcesDir.exists() || !resourcesDir.isDirectory) {
            throw IllegalArgumentException("Invalid localization JSON path: $localizationJsonPath")
        }

        val baseLocaleFile = File(resourcesDir, options["baseLocaleFile"] ?: "zh-CN.json")

        if (!baseLocaleFile.exists()) {
            val foundJsonFiles = resourcesDir.listFiles { _, name -> name.endsWith(".json") }
                ?.joinToString { it.name } ?: "none"
            logger.warn("Available JSON files in $localizationJsonPath: $foundJsonFiles")
            throw IllegalArgumentException("Base locale file not found: $baseLocaleFile")
        }

        val outputPackageName =
            options["localization.output.package"] ?: "org.dsqrwym.shared.localization"
        val outputFileName = options["localization.output.file"] ?: "LocalizationManager"
        val managePackageName = options["localization.manage.package"]
            ?: "org.dsqrwym.shared.localization.LocalizationManager"

        processKeys(
            baseLocaleFile.readText(),
            codeGenerator,
            logger,
            outputPackageName,
            outputFileName,
            managePackageName
        )

        processed = true
        return emptyList()
    }
}

private val KOTLIN_KEYWORDS = setOf(
    "object", "class", "fun", "val", "var", "when", "if", "else",
    "for", "while", "do", "interface", "package"
)

private fun processKeys(
    jsonContent: String,
    codeGenerator: CodeGenerator,
    logger: KSPLogger,
    outputPackageName: String,
    outputFileName: String,
    managePackageName: String
) {
    try {
        val json = Json { ignoreUnknownKeys = true }
        val parsedJson = json.parseToJsonElement(jsonContent)
        val flatMap = flattenJson(logger, parsedJson)
        val keys = flatMap.keys.toList().sorted()

        if (keys.isEmpty()) {
            logger.warn("No keys found in localization JSON")
            return
        }

        generateLocalizationObject(
            keys,
            codeGenerator,
            logger,
            outputPackageName,
            outputFileName,
            managePackageName
        )
    } catch (e: Exception) {
        logger.error("Error parsing localization JSON: ${e.message}")
    }
}

private fun flattenJson(
    logger: KSPLogger,
    element: JsonElement,
    parentKey: String = ""
): Map<String, String> {
    val map = mutableMapOf<String, String>()
    when (element) {
        is JsonObject -> {
            element.forEach { (key, value) ->
                val fullKey = if (parentKey.isEmpty()) key else "$parentKey.$key"
                when (value) {
                    is JsonPrimitive -> map[fullKey] = value.content
                    is JsonObject -> map.putAll(flattenJson(logger, value, fullKey))
                    else -> logger.warn("Unsupported JSON type: $value")
                }
            }
        }

        else -> logger.warn("Root element must be a JSON object")
    }
    return map
}

private fun generateLocalizationObject(
    keys: List<String>,
    codeGenerator: CodeGenerator,
    logger: KSPLogger,
    outputPackageName: String,
    outputFileName: String,
    managePackageName: String
) {
    // 构建嵌套结构
    val rootNode = Node("")
    keys.forEach { key ->
        var currentNode = rootNode
        key.split('.').forEach { part ->
            currentNode = currentNode.children[part] ?: run {
                val newNode = Node(part, parent = currentNode)
                currentNode.children[part] = newNode
                newNode
            }
        }
    }

    // 生成代码

    val dependencies = Dependencies(aggregating = false)
    val file = codeGenerator.createNewFile(dependencies, outputPackageName, outputFileName)

    try {
        file.bufferedWriter().use {
            it.appendLine("package $outputPackageName")
            it.appendLine()
            it.appendLine("import $managePackageName")
            it.appendLine()
            it.appendLine("/** Generated localization accessor object. Do not edit manually. */")
            it.appendLine("object $outputFileName {")

            generateNestedObjects(it, rootNode)

            it.appendLine("}")
            it.appendLine()
        }
        logger.info("Generated $outputPackageName.$outputFileName successfully with ${keys.size} keys.")
    } catch (e: Exception) {
        logger.error("Error generating $outputPackageName.$outputFileName: ${e.message}")

    } finally {
        try {
            file.close()
        } catch (e: Exception) {
            logger.error("Error closing file: ${e.message}")
        }
    }
}

private class Node(val name: String, val parent: Node? = null) {
    val children = mutableMapOf<String, Node>()

}

private fun generateNestedObjects(writer: Appendable, node: Node, indentLevel: Int = 1) {
    node.children.values.forEach { child ->
        val indent = "    ".repeat(indentLevel)
        val propertyName = keyToPropertyName(child.name)

        val escapedName = if (propertyName in KOTLIN_KEYWORDS) "`$propertyName`"
        else propertyName
        writer.appendLine("${indent}object $escapedName {")

        if (child.children.isEmpty()) {
            writer.appendLine("$indent    const val KEY = \"${getFullKeyPath(child)}\"")
            writer.appendLine("$indent    fun get() = LocalizationManager.getString(KEY)")
        } else {
            generateNestedObjects(writer, child, indentLevel + 1)
        }

        writer.appendLine("${indent}}")
        writer.appendLine()
    }
}

private fun getFullKeyPath(node: Node): String {
    val path = mutableListOf<String>()
    var current: Node? = node
    while (current != null && current.name.isNotEmpty()) {
        path.add(current.name)
        current = current.parent
    }
    return path.reversed().joinToString(".")
}

// 将 JSON key (例如 "user_greeting.welcome") 转换为合法的 Kotlin 属性名 (例如 "user_greeting_welcome")
private fun keyToPropertyName(key: String): String {
    return key.replace(Regex("[^A-Za-z0-9_]"), "_") // 将所有非字母数字下划线替换为下划线
        .replace(Regex("_{2,}"), "_")      // 合并多个下划线
        .trim('_')                         // 去除首尾下划线
        .let {
            // 如果以数字开头，在前面加下划线 (虽然Kotlin属性可以以下划线开头)
            if (it.isNotEmpty() && it.first().isDigit()) "_$it" else it
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