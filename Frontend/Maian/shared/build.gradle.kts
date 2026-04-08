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
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }

    // Android目标配置
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)     // 强制使用Java 11字节码， 官方推荐
        }
    }

    // iOS多目标配置（X64模拟器/ARM64真机）
    listOf(
        //iosX64(),               // Intel模拟器 有些库不支持所以可以不要了
        iosArm64(),             // 真机设备
        iosSimulatorArm64()     // M系列芯片模拟器
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "sharedComposeApp"       // 生成的框架名称
            isStatic = true                     // 生成静态库
        }
    }

    // Desktop目标配置（JVM）
    jvm("desktop")

    // ---------------------------
    // WebAssembly (Wasm) 配置
    // ---------------------------
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        /*binaries.library()  // 生成库模式（非可执行文件）
        generateTypeScriptDefinitions()*/
    }
    // ---------------------------
    // 依赖管理
    // ---------------------------
    sourceSets {
        // 桌面平台专属配置
        val desktopMain by getting

        nativeMain.dependencies {
            api(libs.ktor.client.darwin) // Ktor 引擎

            implementation(libs.kscan) // 扫描条码
        }

        // Android主源码集
        androidMain.dependencies {
            api(libs.androidx.activity.compose)  // AndroidX兼容
            api(libs.jmail) // 邮箱验证密数据
            api(libs.androidx.security.crypto) //安卓安全加密
            api(libs.ktor.client.okhttp) // Ktor 引擎

            implementation(libs.journeyapps.zxing.android.embedded) // 扫描条码
        }

        // 公共主源码集（跨平台共享）
        val commonMain by getting {
            resources.srcDir("src/commonMain/composeResources")
        }

        commonMain.dependencies {
            // Compose基础库
            api("org.jetbrains.compose.runtime:runtime:1.10.2") // 运行时核心
            //api(compose.runtime)         // 运行时核心
            //api("org.jetbrains.compose.runtime:runtime:1.11.0-alpha01")         // 运行时核心
            api("org.jetbrains.compose.foundation:foundation:1.10.2")      // 基础布局组件
            //api(compose.foundation)      // 基础布局组件
            //api("org.jetbrains.compose.foundation:foundation:1.11.0-alpha01")      // 基础布局组件
            api("org.jetbrains.compose.material3:material3:1.9.0")       // Material3设计
            //api(compose.material3)       // Material3设计
            //api("org.jetbrains.compose.material3:material3:1.9.0") // Material3设计.
            api("org.jetbrains.compose.ui:ui:1.10.2")              // UI组件工具集
            //api(compose.ui)              // UI组件工具集
            //api("org.jetbrains.compose.ui:ui:1.11.0-alpha01")              // UI组件工具集

            // 资源管理
            api("org.jetbrains.compose.components:components-resources:1.10.2")        // 跨平台资源支持
            //api(compose.components.resources)        // 跨平台资源支持
            //api("org.jetbrains.compose.components:components-resources:1.11.0-alpha01")        // 跨平台资源支持
            api("org.jetbrains.compose.ui:ui-tooling-preview:1.10.2") // 预览工具
            //api(compose.components.uiToolingPreview) // 预览工具
            //api("org.jetbrains.compose.ui:ui-tooling-preview:1.11.0-alpha01") // 预览工具

            // Android生命周期组件（跨平台）
            api(libs.androidx.lifecycle.viewmodel)       // ViewModel
            api(libs.androidx.lifecycle.runtimeCompose)  // 生命周期与Compose集成

            // 根据KMP官网教程 添加处理日期的跨平台库
            api(libs.kotlinx.datetime)
            // 官方导航
            api(libs.kmp.navigation.compose)
            // Navigation3
            api(libs.androidx.navigation3.runtime)
            //api(libs.navigation3.ui)
            api(libs.jetbrains.navigation3.ui)
            api(libs.jetbrains.material3.adaptiveNavigation3)
            api(libs.jetbrains.lifecycle.viewmodelNavigation3)
            // placeholder
            api(libs.compose.placeholder.material3)

            //implementation(libs.androidx.paging.runtime) 分页
            api(libs.androidx.paging.common)
            api(libs.androidx.paging.compose)
            // Material图标扩展
            api(libs.material.icons.core)
            api(libs.material.icons.extended)

            // 电话号码解析
            api(libs.libphonenumber)

            // Haze 核心库 利用各个平台API实现毛玻璃效果
            api(libs.haze)
            // JSON处理
            api(libs.kotlinx.serialization.json)
            // 跨平台储存，防止在commonMain写很多代码
            api(libs.russhwolf.multiplatform.settings)
            // 跨平台Web View 封装
            api(libs.compose.webview.multiplatform)
            // KOIN 注入
            api(libs.koin.core)
            api(libs.koin.compose.viewmodel)
            // Ktor-client 核心
            api(libs.ktor.client.core)
            api(libs.ktor.client.content.negotiation)
            api(libs.ktor.client.logging)
            api(libs.ktor.client.auth)
            api(libs.ktor.serialization.kotlinx.json)

            // 平台原生通知
            api(libs.alert.kmp)

            // sonner toaster
            api(libs.sonner)

            // coil image
            api(libs.coil.compose)
            api(libs.coil.network.ktor3)

            api(libs.composemediaplayer)

            // 图片放大操作
            api(libs.zoomable)

            // File kit 文件管理
            api(libs.filekit.core)
            api(libs.filekit.dialogs.compose)
            api(libs.filekit.coil)

            // Lottie 动画
            api(libs.compottie.lite)

            // 精准计算
            implementation(libs.bignum)

            // 回国
            api(libs.compose.multiplatform.media.player)

        }

        // 公共测试源码集
        commonTest.dependencies {
            api(libs.kotlin.test)    // 单元测试框架
        }

        // 桌面平台依赖
        desktopMain.dependencies {
            api(compose.desktop.currentOs)        // 桌面原生集成
            api(libs.kotlinx.coroutinesSwing)         // 协程Swing支持
            api(libs.jmail) // 邮箱验证
            api(libs.ktor.client.cio) // Ktor 引擎

            implementation(libs.zxing.core) // 解析条码二维码
            implementation(libs.zxing.javase) // BufferedImageLuminanceSource
            implementation(libs.webcam.capture) // 使用摄像头
        }

        // 根据KMP官网教程 在网页端添加处理日期的跨平台库
        webMain.dependencies {
            // JS-Joda时区支持
            api(npm("@js-joda/timezone", "2.3.0")) //项中包含对必要 npm 包的引用
            api(libs.ktor.client.js) // Ktor 引擎
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
        defaultConfig {
            consumerProguardFiles("consumer-rules.pro")
        }
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

// ---------------------------
// 开发调试依赖
// ---------------------------
dependencies {
    implementation(libs.ktor.client.auth)
    //debugImplementation(compose.uiTooling) // Compose UI调试工具
    debugImplementation(libs.ui.tooling) // Compose UI调试工具
}

// ---------------------------
// 资源生成设置
// ---------------------------
compose.resources {
    publicResClass = true
    nameOfResClass = "SharedRes"
    generateResClass = always
}