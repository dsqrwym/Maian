rootProject.name = "MaiAn" // 根目录名称
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS") // 类型安全项目访问器

pluginManagement {// 插件管理
    repositories {// 仓库
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
         maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {// 依赖解析管理
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
        maven { url = uri("https://jogamp.org/deployment/maven/") }
    }
}


plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(":shared", ":business", ":standard", ":enterprise", ":admin") // 包含的模块
