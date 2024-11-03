plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kotlinKapt)
    alias(libs.plugins.kotlinKsp)
}
android {

    defaultConfig {
        minSdk = 26
        testOptions.targetSdk = 35
        compileSdk = 35
    }

    buildTypes {
        getByName("debug") {
            buildConfigField("boolean", "IS_DEBUG", "true")
        }
        getByName("release") {
            buildConfigField("boolean", "IS_DEBUG", "false")
        }
    }
    buildFeatures {
        dataBinding = true
        viewBinding = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    namespace = "com.ebook.common"
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    api(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))
    api(project(":lib_common"))
    api(project(":lib_ebook_api"))
    api(project(":lib_ebook_db"))
    api(libs.androidx.legacy.support.v4)
    api(libs.appcompat)
    api(libs.constraintlayout)
    api(libs.material)

    implementation(libs.core.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    ksp(libs.apt)
    api(libs.router)
    //rxjava
    api(libs.rxjava)
    api(libs.rxandroid)

    api(libs.eventbus)

    api(libs.rxbinding) {
        exclude(group = "com.android.support")
    }
    //glide图片加载
    api(libs.glide) {
        exclude(group = "com.android.support")
    }
    ksp(libs.compiler)

    api(libs.androidx.lifecycle.extensions)
    kapt(libs.androidx.lifecycle.common.java8)

    api(libs.dagger)
    ksp(libs.dagger.compiler)

    api(libs.stetho)

    api(libs.multi.image.selector)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    // 安卓工具类 https://github.com/Blankj/AndroidUtilCode
    api(libs.utilcodex)

    //JSOUP
    api(libs.jsoup)
    //高斯模糊类
    api(libs.glide.transformations)
    //AutoFitTextView
    api(libs.autofittextview)
    //删除粒子效果
    api(libs.explosionfield)
    //View简易动画
    api(libs.library) { artifact { type = "aar" } }
    api(libs.androidanimations.library) { artifact { type = "aar" } }
    //CircleImageView
    api(libs.circleimageview)
    //SwitchButton
    api(libs.switchbutton.library)
    api(libs.lib)
    //RxBus
    api(libs.rxbus) {
        exclude(group = "com.jakewharton.timber", module = "timber")
    }
    api(libs.converter.scalars)
    api(libs.juniversalchardet)

//    debugApi(libs.leakcanary.android)
}
