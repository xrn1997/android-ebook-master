pluginManagement {
    includeBuild("build-logic")
    repositories {
        maven { url =uri("https://maven.aliyun.com/repository/public/") }
        maven { url = uri("https://www.jitpack.io") }
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        maven { url =uri("https://maven.aliyun.com/repository/public/") }
        maven { url = uri("https://www.jitpack.io") }
        google()
        mavenCentral()
    }
}


rootProject.name = "android-ebook"
include(":module_app")
include(":lib_book_common")
include(":module_main")
include(":module_book")
include(":module_find")
include(":module_me")
include(":lib_ebook_api")
include(":module_login")
include(":lib_ebook_db")
include(":lib_book")
