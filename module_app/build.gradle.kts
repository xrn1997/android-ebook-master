plugins {
    alias(libs.plugins.xrn1997.android.application)
    alias(libs.plugins.kapt)
    alias(libs.plugins.ksp)
    id("therouter")
}
android {
    namespace = "com.ebook"
    defaultConfig {
        applicationId = "com.ebook"
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
    kotlinOptions {
        jvmTarget = "17"
    }
}
val isModule = project.findProperty("isModule").toString().toBoolean()
dependencies {
    api(project(":lib_book"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    if (!isModule) {
        implementation(project(":module_main"))
        implementation(project(":module_find"))
        implementation(project(":module_me"))
        implementation(project(":module_book"))
        implementation(project(":module_login"))
    }


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
}
