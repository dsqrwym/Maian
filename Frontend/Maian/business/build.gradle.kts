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
        //iosX64(),               // Intel模拟器 有些库不支持所以可以不要了
        iosArm64(),             // 真机设备
        iosSimulatorArm64()     // M系列芯片模拟器
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "businessComposeApp"       // 生成的框架名称
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
        outputModuleName = "businessComposeModule"    // 输出的ES模块名称
        browser {
            commonWebpackConfig {
                outputFileName = "businessComposeApp.js"  // 生成的JS入口文件， 必要的
            }
        }
        binaries.library()  // 生成库模式（非可执行文件）
        generateTypeScriptDefinitions()
        outputModuleName = "business"
    }
    // ---------------------------
    // 依赖管理
    // ---------------------------
    sourceSets {
        // 桌面平台专属配置
        val desktopMain by getting

        iosMain.dependencies {
            implementation(project(":shared"))
        }

        // Android主源码集
        androidMain.dependencies {
            implementation(project(":shared"))
        }

        // 公共主源码集（跨平台共享）
        val commonMain by getting

        commonMain.dependencies {
            api(libs.table.core)
            // kotlin 高性能持久化不可变集合库， table依赖需要
            api(libs.kotlinx.collections.immutable)

            implementation(project(":shared"))
        }

        // 公共测试源码集
        commonTest.dependencies {
            implementation(project(":shared"))
        }


        // 桌面平台依赖
        desktopMain.dependencies {
            implementation(project(":shared"))
        }

        // 根据KMP官网教程 在网页端添加处理日期的跨平台库
        wasmJsMain.dependencies {
            implementation(project(":shared"))
        }
    }
}

// ---------------------------
// Android专属配置
// ---------------------------
android {
    namespace = "org.dsqrwym.business"    // 包名唯一标识
    compileSdk = libs.versions.android.compileSdk.get().toInt() // 编译SDK版本

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()     // 最低支持版本
        testOptions.targetSdk = libs.versions.android.targetSdk.get().toInt()   // 测试目标版本
        proguardFile("consumer-rules.pro")
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
            isMinifyEnabled = true   // 同时触发 Shrinking + Optimization + Obfuscation
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
    implementation(libs.ktor.client.auth)
    debugImplementation(compose.uiTooling) // Compose UI调试工具
}

// ---------------------------
// 资源生成设置
// ---------------------------
compose.resources {
    publicResClass = true
    nameOfResClass = "BusinessRes"
    generateResClass = always
}