plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false

    alias(libs.plugins.composeHotReload) apply false
    kotlin("jvm") version "2.2.0" apply false
}

buildscript {
    dependencies {
        classpath(kotlin("gradle-plugin", version = "2.2.0"))
    }
}