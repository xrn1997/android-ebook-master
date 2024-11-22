package com.xrn1997.common.util

import android.content.res.Resources
import android.util.TypedValue

/**
 * 单位转换工具类，提供 dp、px、sp 之间的转换
 */
object DisplayUtil {

    /**
     * 将 px 转换为 dp，保证尺寸大小不变
     *
     * @param pxValue 要转换的 px 值
     * @param scale density 缩放因子
     * @return dp 值
     */
    @JvmStatic
    fun px2dip(pxValue: Float, scale: Float): Int {
        return (pxValue / scale + 0.5f).toInt()
    }

    /**
     * 将 px 转换为 dp，使用系统的 density
     *
     * @param pxValue 要转换的 px 值
     * @return dp 值
     */
    @JvmStatic
    fun px2dip(pxValue: Float): Int {
        return (pxValue / Resources.getSystem().displayMetrics.density + 0.5f).toInt()
    }

    /**
     * 将 dp 转换为 px，保证尺寸大小不变
     *
     * @param dipValue 要转换的 dp 值
     * @param scale density 缩放因子
     * @return px 值
     */
    @JvmStatic
    fun dip2px(dipValue: Float, scale: Float): Int {
        return (dipValue * scale + 0.5f).toInt()
    }

    /**
     * 将 dp 转换为 px，使用系统的 density
     *
     * @param dipValue 要转换的 dp 值
     * @return px 值
     */
    @JvmStatic
    fun dip2px(dipValue: Float): Int {
        return (dipValue * Resources.getSystem().displayMetrics.density + 0.5f).toInt()
    }

    /**
     * 将 px 转换为 sp，保证文字大小不变
     * 使用 TypedValue.applyDimension 进行单位转换
     *
     * @param pxValue 要转换的 px 值
     * @return sp 值
     */
    @JvmStatic
    fun px2sp(pxValue: Float): Int {
        // 使用 TypedValue.applyDimension 进行 px 到 sp 的转换
        val spValue = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, // 单位类型：SP
            pxValue,                    // px 值
            Resources.getSystem().displayMetrics
        )
        return spValue.toInt()
    }

    /**
     * 将 sp 转换为 px，保证文字大小不变
     * 使用 TypedValue.applyDimension 进行单位转换
     *
     * @param spValue 要转换的 sp 值
     * @return px 值
     */
    @JvmStatic
    fun sp2px(spValue: Float): Int {
        // 使用 TypedValue.applyDimension 进行 sp 到 px 的转换
        val pxValue = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, // 单位类型：SP
            spValue,                    // sp 值
            Resources.getSystem().displayMetrics
        )
        return pxValue.toInt()
    }
}
