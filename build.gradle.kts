buildscript {
    dependencies {
        //https://docs.objectbox.io/kotlin-support
        classpath(libs.objectbox.gradle.plugin)
    }
}
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.theRouter) apply false
    alias(libs.plugins.kotlinKsp) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.kotlinKapt) apply false
}