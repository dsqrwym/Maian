rootProject.name = "PlataformaGestio-nDistribucio-nMayorista" // 根目录名称
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
    }
}

plugins {// 插件定义
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0" // 自动配置JDK toolchain
}

include(":shared", ":standard") // 包含的模块