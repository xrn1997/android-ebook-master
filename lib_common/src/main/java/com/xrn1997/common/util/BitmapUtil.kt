package com.xrn1997.common.util


import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.os.Environment
import android.view.View
import android.widget.ScrollView
import androidx.exifinterface.media.ExifInterface
import com.xrn1997.common.BaseApplication.Companion.context
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.ceil

/**
 * Bitmap存储与读取工具类
 * 使用该类请务必继承[com.xrn1997.common.BaseApplication]
 *@author xrn1997
 *@date 2021/3/16
 */
object BitmapUtil {
    /**
     * 将Bitmap以指定格式保存到指定路径(应用私有目录)
     * 注意,如果路径重复,就会覆盖
     * @param bitmap Bitmap
     * @param name String 文件名（需要后缀）
     * @param dir String 文件夹名称
     * @param compressFormat CompressFormat
     */
    @JvmStatic
    fun addBitmapToDir(
        bitmap: Bitmap,
        name: String,
        dir: String,
        compressFormat: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
    ) {
        val path = context.filesDir.path + File.separator + dir + File.separator
        FileUtil.checkDirPath(path)
        // 创建一个位于SD卡上的文件
        val file = File(path, name)
        val out: FileOutputStream
        try {
            // 打开指定文件输出流
            out = FileOutputStream(file)
            // 将位图输出到指定文件
            bitmap.compress(
                compressFormat, 100,
                out
            )
            out.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * 添加Bitmap到系统相册
     * @param bitmap Bitmap
     * @param displayName String 文件名（无须后缀）
     * @param compressFormat CompressFormat 图片压缩格式
     * @param mimeType String mine类型
     */
    @JvmStatic
    fun addBitmapToAlbum(
        bitmap: Bitmap,
        displayName: String,
        compressFormat: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
        mimeType: String = FileUtil.IMAGE_TYPE + "jpeg"
    ) {
        val uri =
            FileUtil.getImageFileUri(
                context,
                displayName,
                null,
                mimeType,
                Environment.DIRECTORY_DCIM
            )
        if (uri != null) {
            val outputStream = context.contentResolver.openOutputStream(uri)
            if (outputStream != null) {
                bitmap.compress(compressFormat, 100, outputStream)
                outputStream.close()
            }
        }
    }

    /**
     * 将ScrollView转为Bitmap，防止OOM异常，限制生成的Bitmap最大为720P。
     */
    @JvmStatic
    fun convertViewToBitmap(scrollView: ScrollView): Bitmap {
        var height = 0
        for (i in 0 until scrollView.childCount) {
            height += scrollView.getChildAt(i).height
            scrollView.getChildAt(i).setBackgroundColor(Color.WHITE) // 设置背景色为白色
        }
        val bitmap = Bitmap.createBitmap(scrollView.width, height, Bitmap.Config.RGB_565)
        Canvas(bitmap).apply {
            scrollView.draw(this)
        }
        return scaled720Bitmap(bitmap)
    }

    /**
     * 将View转为Bitmap，并限制最大尺寸为720P。
     */
    @JvmStatic
    fun convertViewToBitmap(view: View): Bitmap {
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        val bitmap =
            Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return scaled720Bitmap(bitmap)
    }


    /**
     * 合并标题与View生成Bitmap，适合分享用途。
     */
    @JvmStatic
    fun combineBitmapTitle(context: Context, titleStr: String, view: View): Bitmap {
        val bitmap =
            if (view is ScrollView) convertViewToBitmap(view) else convertViewToBitmap(view)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = DisplayUtil.sp2px(16f).toFloat()
        }

        val fm = textPaint.fontMetrics
        val textWidth = textPaint.measureText(titleStr)
        val textHeight = ceil((fm.descent - fm.ascent).toDouble()).toFloat()

        val titleHeight = DisplayUtil.dip2px(40f)
        var titleBitmap = Bitmap.createBitmap(bitmap.width, titleHeight, Bitmap.Config.RGB_565)

        Canvas(titleBitmap).apply {
            drawLine(0f, titleHeight / 2f, bitmap.width.toFloat(), titleHeight / 2f, Paint().apply {
                strokeWidth = titleHeight.toFloat()
            })
            drawText(
                titleStr,
                (bitmap.width - textWidth) / 2,
                titleHeight / 2 + textHeight / 3,
                textPaint
            )
        }

        titleBitmap = Bitmap.createScaledBitmap(
            titleBitmap,
            720,
            (720f / titleBitmap.width * titleBitmap.height).toInt(),
            true
        )

        val combinedBitmap = Bitmap.createBitmap(
            bitmap.width,
            bitmap.height + titleBitmap.height,
            Bitmap.Config.RGB_565
        )
        Canvas(combinedBitmap).apply {
            drawBitmap(titleBitmap, 0f, 0f, null)
            drawBitmap(bitmap, 0f, titleBitmap.height.toFloat(), null)
        }

        bitmap.recycle()
        titleBitmap.recycle()

        return combinedBitmap
    }

    /**
     * 将Bitmap转为字节数组。
     */
    @JvmStatic
    fun bmpToByteArray(
        bitmap: Bitmap,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
        recycle: Boolean = false
    ): ByteArray {
        ByteArrayOutputStream().use { baos ->
            bitmap.compress(format, 100, baos)
            if (recycle) bitmap.recycle()
            return baos.toByteArray()
        }
    }

    /**
     * 将字节数组转为Bitmap。
     */
    @JvmStatic
    fun bytesToBitmap(byteArray: ByteArray): Bitmap? =
        if (byteArray.isEmpty()) null else BitmapFactory.decodeByteArray(
            byteArray,
            0,
            byteArray.size
        )

    /**
     * 等比压缩Bitmap到720P。
     */
    @JvmStatic
    private fun scaled720Bitmap(bitmap: Bitmap): Bitmap {
        val (newWidth, newHeight) = if (bitmap.width > bitmap.height) {
            if (bitmap.height <= 720) return bitmap
            val width = (720f / bitmap.height * bitmap.width).toInt() to 720
            width
        } else {
            if (bitmap.width <= 720) return bitmap
            720 to (720f / bitmap.width * bitmap.height).toInt()
        }
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    /**
     * 压缩Bitmap到指定的尺寸，单位KB。
     */
    @JvmStatic
    fun compressImage(bitmap: Bitmap, targetSize: Int): Bitmap {
        var options = 100
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        while (baos.size() / 1024 > targetSize && options > 0) {
            baos.reset()
            options -= 10
            bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos)
        }
        val byteArray = baos.toByteArray()
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    /**
     * 获取图片的旋转角度。
     */
    @JvmStatic
    fun getBitmapDegree(path: String): Int {
        return try {
            val exif = ExifInterface(path)
            when (exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } catch (e: IOException) {
            e.printStackTrace()
            0
        }
    }

    /**
     * 旋转Bitmap到指定角度。
     */
    @JvmStatic
    fun rotateBitmapByDegree(bitmap: Bitmap, degree: Int): Bitmap {
        val matrix = Matrix().apply { postRotate(degree.toFloat()) }
        return try {
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true).also {
                if (it != bitmap) bitmap.recycle()
            }
        } catch (e: OutOfMemoryError) {
            bitmap
        }
    }

    /**
     * 获取Bitmap的大小（字节数）。
     */
    @JvmStatic
    fun getBitmapSize(bitmap: Bitmap): Int {
        return bitmap.allocationByteCount
    }
}
