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
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    jvm("desktop")

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName = "standardComposeApp"
        browser {
            commonWebpackConfig {
                outputFileName = "standardComposeApp.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        val androidMain by getting
        androidMain.dependencies {
            // implementation(project(":shared"))
        }

        val commonMain by getting
        commonMain.dependencies {
            implementation(project(":shared"))
        }

        val desktopMain by getting
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            //implementation(project(":shared"))
        }

        webMain.dependencies {
            //implementation(project(":shared"))
        }
    }
}

android {
    namespace = "org.dsqrwym.standard"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "org.dsqrwym.pgdm.standard"
        minSdk = libs.versions.android.minSdk.get().toInt()
        //noinspection OldTargetApi
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
        proguardFiles("proguard-rules.pro")
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            //isMinifyEnabled = false
            isMinifyEnabled = true   // 同时触发 Shrinking + Optimization + Obfuscation
            isShrinkResources = true   // 移除未用资源
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    debugImplementation(libs.ui.tooling)
}

compose.resources {
    publicResClass = true
    nameOfResClass = "StandardRes"
    generateResClass = always
}

compose.desktop {
    application {
        mainClass = "org.dsqrwym.standard.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "org.dsqrwym.pgdm.standard"
            packageVersion = "1.0.0"

            windows {
                iconFile.set(project.file("src/desktopMain/resources/icons/windows/StoreLogo.ico"))
            }
            linux {
                iconFile.set(project.file("src/desktopMain/resources/icons/linux/StoreLogo.png"))
            }
            macOS {
                iconFile.set(project.file("src/desktopMain/resources/icons/macos/StoreLogo.png"))
            }
        }

        jvmArgs("--add-opens", "java.desktop/sun.awt=ALL-UNNAMED")
        jvmArgs("--add-opens", "java.desktop/java.awt.peer=ALL-UNNAMED") // recommended but not necessary

        if (System.getProperty("os.name").contains("Mac")) {
            jvmArgs("--add-opens", "java.desktop/sun.lwawt=ALL-UNNAMED")
            jvmArgs("--add-opens", "java.desktop/sun.lwawt.macosx=ALL-UNNAMED")
        }
    }
}