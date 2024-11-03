plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
}

android {

    defaultConfig {
        minSdk = 26
        testOptions.targetSdk = 35
        compileSdk = 35
    }
    sourceSets {
        getByName("main") {
            jniLibs.srcDir("src/main/jniLibs")
        }
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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    namespace = "com.ebook.api"
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    api(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))
    api(libs.appcompat)
    api(libs.annotations)

    //network
    api(libs.converter.scalars)
    api(libs.adapter.rxjava3)
    api(libs.converter.gson)
    api(libs.logging.interceptor)

    //json解析
    api(libs.gson)
    api(libs.fastjson2)
    //rx管理View的生命周期
    api(libs.rxlifecycle) {
        exclude(group = "com.android.support")
    }
    api(libs.rxlifecycle.components) {
        exclude(group = "com.android.support")
    }
    api(libs.rxlifecycle.android) {
        exclude(group = "com.android.support")
    }
    implementation(libs.core.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    api(libs.converter.scalars)
}
