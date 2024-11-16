package com.xrn1997.common.util

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import androidx.core.graphics.ColorUtils
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsCompat.Type.ime
import androidx.core.view.WindowInsetsControllerCompat
import androidx.palette.graphics.Palette
import com.blankj.utilcode.util.BarUtils.getStatusBarHeight
import com.blankj.utilcode.util.ScreenUtils.getScreenWidth

/**
 * 内容填充，自行控制视图边界行为。
 */
fun Activity.fullWindow() {
    if (window == null) return
    WindowCompat.setDecorFitsSystemWindows(window, false)
}
/**
 * 状态栏自适应浅、深色模式
 */
fun Activity.setAndroidNativeLightStatusBar() {
    if (window == null) return
    val controller = WindowCompat.getInsetsController(window, window.decorView)
    controller.isAppearanceLightStatusBars = !isDarkMode()
}

/**
 * 设置状态栏颜色
 * @param isLight true为白色，false为黑色
 */
fun Activity.setStatusBar(isLight: Boolean) {
    if (window == null) return
    val controller = WindowCompat.getInsetsController(window, window.decorView)
    controller.isAppearanceLightStatusBars = isLight
}

/**
 * 状态栏与图片颜色相反
 */
fun Activity.setStatusBarWithBitmap(bitmap: Bitmap) {
    if (window == null) return
    val controller = WindowCompat.getInsetsController(window, window.decorView)
    val luminance = bitmap.detectColor()
    controller.isAppearanceLightStatusBars = if (luminance < 0.5) true else false
}

/**
 * 解析Bitmap颜色（亮、暗）.
 * @return 亮度值，一般亮度值低于0.5，认为是暗，反之为亮。
 */
fun Bitmap.detectColor(): Double {
    val colorCount = 5
    val left = 0
    val top = 0
    val right = getScreenWidth()
    val bottom = getStatusBarHeight()
    var luminance = 0.0
    Palette
        .from(this)
        .maximumColorCount(colorCount)
        .setRegion(left, top, right, bottom)
        .generate {
            it?.let { palette ->
                var mostPopularSwatch: Palette.Swatch? = null
                for (swatch in palette.swatches) {
                    if (mostPopularSwatch == null
                        || swatch.population > mostPopularSwatch.population
                    ) {
                        mostPopularSwatch = swatch
                    }
                }
                mostPopularSwatch?.let { swatch ->
                    luminance = ColorUtils.calculateLuminance(swatch.rgb)
                }
            }
        }
    return luminance
}

/**
 * 获取当前是否为深色模式
 * 深色模式的值为:0x21
 * 浅色模式的值为:0x11
 * @return true 为是深色模式   false为不是深色模式
 */
fun Context.isDarkMode(): Boolean {
    return resources.configuration.uiMode == 0x21
}


/**
 * 隐藏ime
 */
fun Activity.hideIme() {
    if (window == null) return
    val controller = WindowCompat.getInsetsController(window, window.decorView)
    controller.hide(ime())
}

/**
 * 显示ime
 */
fun Activity.showIme() {
    if (window == null) return
    val controller = WindowCompat.getInsetsController(window, window.decorView)
    controller.show(ime())
}

/**
 * 隐藏状态栏
 */
fun Activity.hideStatusBar() {
    if (window == null) return
    val controller = WindowCompat.getInsetsController(window, window.decorView)
    //设置可以通过手势唤出系统栏
    controller.systemBarsBehavior =
        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

    ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { view, windowInsets ->
        val statusBar = WindowInsetsCompat.Type.statusBars()
        if (windowInsets.isVisible(statusBar)) {
            controller.hide(statusBar)
        }
        ViewCompat.onApplyWindowInsets(view, windowInsets)
    }
}

/**
 * 显示状态栏
 */
fun Activity.showStatusBar() {
    if (window == null) return
    val controller = WindowCompat.getInsetsController(window, window.decorView)
    ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { view, windowInsets ->
        val statusBar = WindowInsetsCompat.Type.statusBars()
        if (!windowInsets.isVisible(statusBar)) {
            controller.show(statusBar)
        }
        ViewCompat.onApplyWindowInsets(view, windowInsets)
    }
}