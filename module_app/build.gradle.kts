plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kotlinKapt)
    alias(libs.plugins.kotlinKsp)
    id("therouter")
}
val isLoginModule: String by project
val isModule: String by project
android {

    defaultConfig {
        applicationId = "com.ebook"
        minSdk = 26
        targetSdk = 35
        compileSdk = 35
        versionCode = 4
        versionName = "1.1.6"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    buildFeatures {
        dataBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    namespace = "com.ebook"
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))
    api(project(":lib_book"))
    implementation(libs.core.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    if (!isModule.toBoolean()) {
        implementation(project(":module_main"))
        implementation(project(":module_find"))
        implementation(project(":module_me"))
        implementation(project(":module_book"))
        implementation(project(":module_login"))
    } else if (!isLoginModule.toBoolean()) {
        implementation(project(":module_login"))
    }

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
