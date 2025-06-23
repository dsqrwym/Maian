import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// ---------------------------
// 插件配置区
// ---------------------------
plugins {
    alias(libs.plugins.kotlinMultiplatform)     // Kotlin多平台支持
    alias(libs.plugins.androidLibrary)          // 用来表明自己是Android库模块插件
    alias(libs.plugins.composeMultiplatform)    // Compose跨平台UI框架
    alias(libs.plugins.composeCompiler)         // Compose编译器集成
    //alias(libs.plugins.composeHotReload)        // 热重载， 但是应该没用
    id("com.google.devtools.ksp") version "2.1.21-2.0.1"
    alias(libs.plugins.kotlinxSerialization)
}

// ---------------------------
// Kotlin多平台配置
// ---------------------------
kotlin {
    // Android目标配置
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)     // 强制使用Java 11字节码， 官方推荐
        }
    }

    // iOS多目标配置（X64模拟器/ARM64真机）
    listOf(
        iosX64(),               // Intel模拟器
        iosArm64(),             // 真机设备
        iosSimulatorArm64()     // M系列芯片模拟器
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "sharedComposeApp"       // 生成的框架名称
            isStatic = true                     // 生成静态库
        }
    }

    // Desktop目标配置（JVM）
    jvm("desktop")      // // 隐式继承项目的Java版本配置

    // ---------------------------
    // WebAssembly (Wasm) 配置
    // ---------------------------
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName = "sharedComposeModule"    // 输出的ES模块名称
        browser {
            commonWebpackConfig {
                outputFileName = "sharedComposeApp.js"  // 生成的JS入口文件， 必要的
            }
        }
        binaries.library()  // 生成库模式（非可执行文件）
    }

    // ---------------------------
    // 依赖管理
    // ---------------------------
    sourceSets {
        // 桌面平台专属配置
        val desktopMain by getting

        // Android主源码集
        androidMain.dependencies {
            implementation(compose.preview)     // Compose预览支持
            implementation(libs.androidx.activity.compose)  // AndroidX兼容
            implementation(libs.jmail) // 邮箱验证密数据
            implementation(libs.androidx.security.crypto) //安卓安全加密
        }

        // 公共主源码集（跨平台共享）
        val commonMain by getting {
            dependencies {
                // Compose基础库
                implementation(compose.runtime)         // 运行时核心
                implementation(compose.foundation)      // 基础布局组件
                implementation(compose.material3)       // Material3设计
                implementation(compose.ui)              // UI组件工具集

                // 资源管理
                implementation(compose.components.resources)        // 跨平台资源支持
                implementation(compose.components.uiToolingPreview) // 预览工具

                // Android生命周期组件（跨平台）
                implementation(libs.androidx.lifecycle.viewmodel)       // ViewModel
                implementation(libs.androidx.lifecycle.runtimeCompose)  // 生命周期与Compose集成

                // 根据KMP官网教程 添加处理日期的跨平台库
                implementation(libs.kotlinx.datetime)

                // Material图标扩展
                implementation(libs.material.icons.core)

                // Haze 核心库 利用各个平台API实现毛玻璃效果
                implementation(libs.haze)
                // JSON处理
                implementation(libs.kotlinx.serialization.json)
                // 跨平台储存，防止在commonMain写很多代码
                implementation(libs.russhwolf.multiplatform.settings)
                // 跨平台Web View 封装
                api(libs.compose.webview.multiplatform)
                // KOIN 注入
                implementation(libs.koin.core) // 或最新版本
                implementation(libs.koin.compose.viewmodel)
            }
            kotlin.srcDir("src/commonGenerated/kotlin")
            resources.srcDir("src/commonMain/composeResources")
        }

        // 公共测试源码集
        commonTest.dependencies {
            implementation(libs.kotlin.test)    // 单元测试框架
        }

        // 桌面平台依赖
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)        // 桌面原生集成
            implementation(libs.kotlinx.coroutinesSwing)         // 协程Swing支持
            implementation(libs.jmail) // 邮箱验证
        }

        // 根据KMP官网教程 在网页端添加处理日期的跨平台库
        wasmJsMain.dependencies {
            // JS-Joda时区支持
            implementation(npm("@js-joda/timezone", "2.3.0")) //项中包含对必要 npm 包的引用
        }
    }
}

// ---------------------------
// Android专属配置
// ---------------------------
android {
    namespace = "org.dsqrwym.shared"    // 包名唯一标识
    compileSdk = libs.versions.android.compileSdk.get().toInt() // 编译SDK版本

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()     // 最低支持版本
        testOptions.targetSdk = libs.versions.android.targetSdk.get().toInt()   // 测试目标版本
    }

    // 资源打包配置
    packaging {
        resources {
            // 排除冲突的元数据文件
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    // 构建类型配置
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false // 关闭代码混淆
        }
    }

    // Java版本兼容性
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

// ---------------------------
// 开发调试依赖
// ---------------------------
dependencies {
    debugImplementation(compose.uiTooling) // Compose UI调试工具
    implementation(kotlin("stdlib-jdk8"))
    // implementation(project(":localization-processor"))
    // ksp(project(":localization-processor"))
    kspCommonMainMetadata(project(":localization-processor"))
}

ksp {
    arg(
        "localization.json.path",
        project.file("src/commonMain/kotlin/org/dsqrwym/shared/localization").absolutePath
    )
    arg("localization.base.locale", "zh-CN")
    arg("localization.output.package", "org.dsqrwym.shared.localization")
    arg("localization.output.file", "SharedLanguage")
}


val copyGeneratedLanguageByKspToCommon by tasks.registering(Copy::class) {
    val sourceDir = layout.buildDirectory.dir("generated/ksp/metadata/commonMain/kotlin/org/dsqrwym/shared/localization")
    val destinationDir = layout.projectDirectory.dir("src/commonGenerated/kotlin/org/dsqrwym/shared/language")
    /*
    from(layout.buildDirectory.dir("generated/ksp/metadata/commonMain/kotlin/org/dsqrwym/shared/localization")) {
        include("*.kt")
        val firstLineReplaced = mutableListOf(false)
        // Kotlin 不允许在 lambda 内修改外部 var 所以用这个mutable对象
        // 使用 filter 修改内容
        filter { line ->
            // 这里用一个标志控制只替换第一行
            if (!firstLineReplaced[0]) {
                firstLineReplaced[0] = true
                "package org.dsqrwym.shared.language"
            } else {
                line
            }
        }
    }
    */

    from(sourceDir) {
        include("**/*.kt")
        filteringCharset = "UTF-8"

        // 针对每个文件执行
        eachFile {
            val originalLines = file.readLines()
            val modifiedLines = originalLines.toMutableList()

            // 替换第一行为目标 package
            if (modifiedLines.isNotEmpty()) {
                modifiedLines[0] = "package org.dsqrwym.shared.language"
            }

            // 重写文件内容
            file.writeText(modifiedLines.joinToString("\n"))
        }
    }

    into(destinationDir)

    doFirst {
        println("Copying and modifying SharedLanguage.kt to $destinationDir")
    }
}

tasks.named("compileKotlinMetadata") {
    finalizedBy(copyGeneratedLanguageByKspToCommon)
}