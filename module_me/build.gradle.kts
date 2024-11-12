plugins {
    alias(libs.plugins.xrn1997.android.component)
    alias(libs.plugins.kapt)
    alias(libs.plugins.ksp)
}
android {
    namespace = "com.ebook.me"
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        debug {
            isMinifyEnabled = false
            proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
            )
        }
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
        viewBinding = true
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}
dependencies {
    api(project(":lib_book_common"))
    //todo 相关模块均为lib时才能导入
    implementation(project(":module_login"))
    ksp(libs.dagger.compiler)
    ksp(libs.router.apt)
    implementation(libs.router)
    ksp(libs.glide.compiler)
    ksp(libs.androidx.lifecycle.common)
    //测试依赖
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
}