import com.android.build.api.dsl.ApplicationExtension
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinxSerialization)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "enterpriseComposeApp"
            isStatic = true
        }
    }

    jvm("desktop")

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            commonWebpackConfig {
                outputFileName = "enterpriseComposeApp.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        val androidMain by getting
        androidMain.dependencies {
            implementation(project(":shared"))
            implementation(project(":business"))
        }

        val commonMain by getting
        commonMain.dependencies {
            // 拖拽
            implementation(libs.reorderable)
            implementation(project(":shared"))
            implementation(project(":business"))
        }

        val desktopMain by getting
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(project(":shared"))
            implementation(project(":business"))
        }

        webMain.dependencies {
            implementation(project(":shared"))
            implementation(project(":business"))
        }

    }
}

extensions.configure<ApplicationExtension>("android") {
    namespace = "org.dsqrwym.enterprise"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "org.dsqrwym.maian.enterprise"
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
            isMinifyEnabled = true   // 同时触发 Shrinking + Optimization + Obfuscation
            isShrinkResources = true   // 移除未用资源
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    //debugImplementation(compose.uiTooling)
    debugImplementation(libs.ui.tooling)
}

compose.resources {
    publicResClass = true
    nameOfResClass = "EnterpriseRes"
    generateResClass = always
}


compose.desktop {
    application {
        mainClass = "org.dsqrwym.enterprise.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "MaiAn.Enterprise"
            packageVersion = "1.0.0"
            vendor = "DSQRWYM"
            copyright = "© 2025 DSQRWYM. All rights reserved."
        }

        jvmArgs("--add-opens", "java.desktop/sun.awt=ALL-UNNAMED")
        jvmArgs("--add-opens", "java.desktop/java.awt.peer=ALL-UNNAMED") // recommended but not necessary

        if (System.getProperty("os.name").contains("Mac")) {
            jvmArgs("--add-opens", "java.desktop/sun.lwawt=ALL-UNNAMED")
            jvmArgs("--add-opens", "java.desktop/sun.lwawt.macosx=ALL-UNNAMED")
        }

        buildTypes.release.proguard {
            obfuscate.set(true)
            optimize.set(false)
            configurationFiles.from(
                rootProject.file("desktop-proguard-rules.pro"),
                project.file("proguard-rules.pro")
            )
        }
    }
}

// Exclude kotlinx-coroutines-test from desktop runtime to prevent
// ExceptionCollectorAsService ServiceLoader error in release builds.
configurations.configureEach {
    if (name.contains("desktopRuntime") || name.contains("desktopCompile")) {
        exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-test")
    }
}
