plugins {
    alias(libs.plugins.xrn1997.android.library)
    alias(libs.plugins.kapt)
    alias(libs.plugins.ksp)
}
android {
    namespace = "com.ebook.common"
    buildTypes {
        debug {
            buildConfigField("boolean", "IS_DEBUG", "true")
        }
        release {
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
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    api(project(":lib_common"))
    api(project(":lib_ebook_api"))
    api(project(":lib_ebook_db"))
    api(libs.androidx.legacy.support.v4)
    api(libs.androidx.appcompat)
    api(libs.androidx.constraintlayout)
    api(libs.material)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    ksp(libs.router.apt)
    api(libs.router)
    //rxjava
    api(libs.rxjava)
    api(libs.rxandroid)

    api(libs.eventbus)

    api(libs.rxbinding) {
        exclude(group = "com.android.support")
    }
    //glide图片加载
    api(libs.glide.core) {
        exclude(group = "com.android.support")
    }
    ksp(libs.glide.compiler)

    api(libs.androidx.lifecycle.extensions)
    ksp(libs.androidx.lifecycle.common.java8)

    api(libs.dagger)
    ksp(libs.dagger.compiler)

    api(libs.stetho)

    api(libs.multi.image.selector)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    // 安卓工具类 https://github.com/Blankj/AndroidUtilCode
    api(libs.blankj.utilcodex)

    //JSOUP
    api(libs.jsoup)
    //高斯模糊类
    api(libs.glide.transformations)
    //AutoFitTextView
    api(libs.autofittextview)
    //删除粒子效果
    api(libs.explosionfield)
    //View简易动画
    api(libs.daimajia.easing.library) { artifact { type = "aar" } }
    api(libs.daimajia.androidanimations.library) { artifact { type = "aar" } }
    //CircleImageView
    api(libs.hdodenhof.circleimageview)
    //SwitchButton
    api(libs.switchbutton.library)
    api(libs.victor.lib)
    //RxBus
    api(libs.rxbus) {
        exclude(group = "com.jakewharton.timber", module = "timber")
    }
    api(libs.retrofit.converter.scalars)
    api(libs.juniversalchardet)

//    debugApi(libs.leakcanary.android)
}
