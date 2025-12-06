import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinxSerialization)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    jvm("desktop")

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName = "enterpriseComposeApp"
        browser {
            val rootDirPath = project.rootDir.path
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                outputFileName = "enterpriseComposeApp.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(rootDirPath)
                        add(projectDirPath)
                        outputPath?.let { add(it.absolutePath) }
                    }
                }
            }
        }
        binaries.executable()
    }

    sourceSets {
        val androidMain by getting
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            //implementation(project(":shared"))
        }

        val commonMain by getting
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            implementation(libs.material.icons.core)
            implementation(libs.material.icons.extended)

            // 跨平台储存，防止在commonMain写很多代码
            implementation(libs.russhwolf.multiplatform.settings)
            // 官方导航
            implementation(libs.kmp.navigation.compose)

            implementation(libs.androidx.paging.common)
            implementation(libs.androidx.paging.compose)

            implementation(libs.datatable.material3)
            implementation(libs.table.core)
            // Koin
            implementation(libs.koin.core) // 或最新版本
            implementation(libs.koin.compose.viewmodel)

            // kotlin 高性能持久化不可变集合库， table依赖需要
            implementation(libs.kotlinx.collections.immutable)

            implementation(libs.coil.compose)
            implementation(libs.zoomable)

            implementation(project(":shared"))
        }

        val desktopMain by getting
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            //implementation(project(":shared"))
        }

        val wasmJsMain by getting
        wasmJsMain.dependencies {
            //implementation(project(":shared"))
        }

    }
}

android {
    namespace = "org.dsqrwym.enterprise"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "org.dsqrwym.pgdm.enterprise"
        minSdk = libs.versions.android.minSdk.get().toInt()
        //noinspection OldTargetApi
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            //isShrinkResources = true   // 移除未用资源
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

compose.resources {
    publicResClass = true
    nameOfResClass = "EnterpriseRes"
    generateResClass = auto
}


compose.desktop {
    application {
        mainClass = "org.dsqrwym.enterprise.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "org.dsqrwym.pgdm.enterprise"
            packageVersion = "1.0.0"
        }
    }
}