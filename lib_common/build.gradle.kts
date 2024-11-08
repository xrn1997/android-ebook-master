plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kapt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.xrn1997.common"
    compileSdk = 35
    defaultConfig {
        minSdk = 26
        testOptions.targetSdk = 35
        consumerProguardFiles("consumer-rules.pro")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        dataBinding = true
        viewBinding = true
        compose = true
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
    api(libs.androidx.core.ktx)
    api(libs.androidx.appcompat)
    api(libs.material)
    api(libs.kotlin.reflect)
    //协程 https://github.com/Kotlin/kotlinx.coroutines
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.coroutines.android)

    // 输入型dialog https://github.com/afollestad/material-dialogs
    api(libs.afollestad.material.input)
    api(libs.afollestad.material.core)
    api(libs.afollestad.material.bottomsheets)
    //Activity和Fragment
    api(libs.androidx.fragment.ktx)
    api(libs.androidx.activity.ktx)
    //扁平化布局 https://github.com/androidx/constraintlayout
    // 教程 https://blog.csdn.net/guolin_blog/article/details/53122387
    api(libs.androidx.constraintlayout)
    //RecyclerView https://developer.android.google.cn/jetpack/androidx/releases/recyclerview?hl=zh_cn
    api(libs.androidx.recyclerview)
    //EventBus  https://github.com/greenrobot/EventBus
    api(libs.eventbus)
    //Rxjava https://github.com/ReactiveX/RxJava
    // 教程 https://www.jianshu.com/p/d9b504f5b3bd
    api(libs.rxkotlin)
    api(libs.rxandroid)
    api(libs.rxjava)
    //RxBinding https://github.com/JakeWharton/RxBinding
    api(libs.rxbinding)
    //RxLifecycle  https://github.com/trello/RxLifecycle
    api(libs.trello.rxlifecycle.kotlin)
    api(libs.trello.rxlifecycle.android)
    api(libs.trello.rxlifecycle.android.lifecycle)
    api(libs.trello.rxlifecycle.android.lifecycle.kotlin)
    api(libs.trello.rxlifecycle.components)
    api(libs.trello.rxlifecycle.components.preference)
    // ViewModel https://github.com/androidx/androidx/tree/androidx-main/lifecycle
    // 官网 https://developer.android.google.cn/jetpack/androidx/releases/lifecycle
    api(libs.androidx.lifecycle.viewmodel.ktx)
    api(libs.androidx.lifecycle.viewModelCompose)
    api(libs.androidx.lifecycle.livedata.ktx)
    api(libs.androidx.lifecycle.runtime.ktx)
    api(libs.androidx.lifecycle.viewmodel.savedstate)
    kapt(libs.androidx.lifecycle.common)
    //Android 工具类 https://github.com/Blankj/AndroidUtilCode
    api(libs.utilcodex)
    //Glide https://muyangmin.github.io/glide-docs-cn/
    api(libs.glide.core)
    ksp(libs.glide.compiler)
    //刷新加载控件 https://github.com/scwang90/SmartRefreshLayout/tree/main
    api(libs.refresh.layout.kernel)      //核心必须依赖
    api(libs.refresh.header.classics)    //经典刷新头
    api(libs.refresh.header.radar)       //雷达刷新头
    api(libs.refresh.header.falsify)    //虚拟刷新头
    api(libs.refresh.header.material)    //谷歌刷新头
    api(libs.refresh.header.two.level)   //二级刷新头
    api(libs.refresh.footer.ball)        //球脉冲加载
    api(libs.refresh.footer.classics)    //经典加载
    //Gson https://github.com/google/gson/
    api(libs.gson)
    //JSON工具
    api(libs.fastjson2)
    //Retrofit https://square.github.io/retrofit/
    api(libs.retrofit.adapter.rxjava3)
    api(libs.retrofit.core)
    api(libs.retrofit.converter.gson)
    api(libs.retrofit.converter.scalars)
    //第三方日志打印框架 https://github.com/ihsanbal/LoggingInterceptor/
    api(libs.ihsanbal.logging.intercepter) {
        exclude(group = "org.json", module = "json")
    }
    //Stetho https://github.com/facebook/stetho
    //教程 https://www.jianshu.com/p/6a407dd612ee
    api(libs.stetho)
    //监控okhttp的请求
    api(libs.stetho.okhttp3)
    //AutoFitTextView https://github.com/grantland/android-autofittextview
    api(libs.autofittextview)
    //删除动画（粒子效果） https://github.com/tyrantgit/ExplosionField
    //教程 http://t.csdn.cn/7atlJ
    api(libs.explosionfield)
    //PermissionX https://github.com/guolindev/PermissionX
    // 教程 https://blog.csdn.net/guolin_blog/category_10108528.html
    api(libs.permissionx)
    //Compose
    api(libs.androidx.activity.compose)
    api(libs.androidx.lifecycle.viewModelCompose)
    api(libs.androidx.compose.ui)
    api(libs.androidx.compose.ui.graphics)
    api(libs.androidx.compose.ui.tooling.preview)
    api(libs.androidx.compose.material3)
    api(libs.androidx.foundation)
    api(libs.runtime.livedata)
    api(platform(libs.androidx.compose.bom))
    //Navigation
    api(libs.androidx.navigation.compose)
    api(libs.androidx.navigation.fragment.ktx)
    api(libs.androidx.navigation.ui.ktx)
    api(libs.androidx.navigation.dynamic.features.fragment)
    //Palette
    api(libs.androidx.palette)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.testManifest)
    //测试依赖
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.compose.ui.test)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.navigation.testing)
    androidTestImplementation(platform(libs.androidx.compose.bom))
}