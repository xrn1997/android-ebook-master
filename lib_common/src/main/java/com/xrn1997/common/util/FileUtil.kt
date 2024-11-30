package com.xrn1997.common.util

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

/**
 * 文件工具类
 * @author xrn1997
 * @date 2022/3/5 10:47
 */
@Suppress("unused")
object FileUtil {
    private val TAG = this::class.java.simpleName

    /**
     * 需要附加类型,如[IMAGE_TYPE]+"jpeg"
     */
    const val IMAGE_TYPE = "image/"

    /**
     * 需要附加类型,如[VIDEO_TYPE]+"mp4"
     */
    const val VIDEO_TYPE = "video/"

    /**
     * 需要附加类型,如[AUDIO_TYPE]+"mpeg"
     */
    const val AUDIO_TYPE = "audio/"

    /**
     * 根据文件路径获得文件数据
     * @param path String 文件路径
     * @return ByteArray?
     */
    @JvmStatic
    fun getFileByte(path: String): ByteArray? {
        val f = File(path)
        if (!f.exists()) {
            return null
        }
        val bos = ByteArrayOutputStream(
            f.length().toInt()
        )
        val bufferedInputStream: BufferedInputStream?
        try {
            bufferedInputStream = BufferedInputStream(FileInputStream(f))
            val bufSize = 1024
            val buffer = ByteArray(bufSize)
            var len: Int
            while (-1 != bufferedInputStream.read(buffer, 0, bufSize).also { len = it }) {
                bos.write(buffer, 0, len)
            }
            bufferedInputStream.close()
            return bos.toByteArray()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                bos.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return null
    }

    /**
     * 根据Uri返回文件绝对路径
     * 兼容了file:///开头的 和 content://开头的情况
     * 对于content的情况，仅支持外部共享目录
     * @param context Context
     * @param uri Uri
     * @return String?
     */
    @JvmStatic
    fun getRealPathFromUri(context: Context, uri: Uri): String? {
        var filePath: String? = null
        val scheme = uri.scheme

        when {
            // 处理无 scheme 的情况
            scheme.isNullOrEmpty() -> filePath = uri.path

            // 处理 File 类型的 Uri
            ContentResolver.SCHEME_FILE.equals(scheme, ignoreCase = true) -> filePath = uri.path

            // 处理 Content 类型的 Uri
            ContentResolver.SCHEME_CONTENT.equals(scheme, ignoreCase = true) -> {
                // 通过 ContentResolver 获取数据
                val projection = arrayOf(MediaStore.Images.Media.DATA)
                context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val columnIndex = cursor.getColumnIndexOrThrow(projection[0])
                        filePath = cursor.getString(columnIndex)
                    }
                }
                // 如果第一次查询没有结果，尝试查询非媒体类型的 Uri
                if (filePath.isNullOrEmpty()) {
                    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val columnIndex = cursor.getColumnIndexOrThrow("_data")
                            filePath = cursor.getString(columnIndex)
                        }
                    }
                }
            }
        }

        return filePath
    }

    /**
     * 检查文件夹是否存在，注意权限
     * @param dirPath String
     * @return String
     */
    @JvmStatic
    fun checkDirPath(dirPath: String): String {
        val dir = File(dirPath)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dirPath
    }

    /**
     * 删除单个文件，注意权限
     * @param filePath 被删除文件的文件名
     * @return 文件删除成功返回true,否则返回false
     */
    @JvmStatic
    fun deleteFile(filePath: String): Boolean {
        val file = File(filePath)
        return if (file.isFile && file.exists()) {
            file.delete()
        } else false
    }

    /**
     * 删除文件夹以及目录下的文件，注意权限
     * @param filePath 被删除目录的文件路径
     * @return 目录删除成功返回true,否则返回false
     */
    @JvmStatic
    fun deleteDirectory(filePath: String): Boolean {

        var flag: Boolean
        //如果filePath不以文件分隔符结尾,自动添加文件分隔符

        val dirFile = File(
            if (!filePath.endsWith(File.separator)) {
                filePath + File.separator
            } else filePath
        )
        if (!dirFile.exists() || !dirFile.isDirectory) {
            return false
        }
        flag = true
        val files = dirFile.listFiles() ?: return true
        //遍历删除文件夹下的所有文件(包括子目录)
        for (i in files.indices) {
            if (files[i].isFile) {
                //删除子文件
                flag = deleteFile(files[i].absolutePath)
                if (!flag) break
            } else {
                //删除子目录
                flag = deleteDirectory(files[i].absolutePath)
                if (!flag) break
            }
        }
        //删除当前空目录
        return if (!flag) false else dirFile.delete()
    }

    /**
     * 根据时间戳生成文件名
     * @param type String 文件后缀名(不带点)
     * @return String 文件名
     */
    @JvmStatic
    fun getFileName(type: String): String {
        return SimpleDateFormat.getDateTimeInstance().format(Date()) + "." + type
    }

    /**
     * 设置ContentValues
     * @param mimeType String 文件类型[IMAGE_TYPE] [VIDEO_TYPE] [AUDIO_TYPE]
     * @param directory 子文件夹
     * @param systemDirectory 如[Environment.DIRECTORY_DCIM]
     * 如 Environment.MUSIC+"/xxx"
     * @param displayName String 文件名
     * @return ContentValues
     */
    @JvmStatic
    fun getFileContentValues(
        mimeType: String,
        directory: String? = "",
        systemDirectory: String,
        displayName: String,
    ): ContentValues {
        val values = ContentValues()
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
        values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
        val path =
            if (directory != "") systemDirectory + File.separator + directory else systemDirectory
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, path)
        } else {
            values.put(
                MediaStore.MediaColumns.DATA,
                "${Environment.getExternalStorageDirectory().path}/${path}/$displayName"
            )
        }
        return values
    }

    /**
     * 遍历文件夹下的所有图片
     * @param context Context
     * @param uri Uri
     * @return List<Uri>?
     */
    @JvmStatic
    fun queryImageFromUri(
        context: Context,
        uri: Uri
    ): List<Uri>? {
        val cursor = context.contentResolver.query(
            uri,
            null,
            null,
            null,
            "${MediaStore.MediaColumns.DATE_ADDED} desc"
        )
        if (cursor != null) {
            val uriList: MutableList<Uri> = ArrayList(cursor.columnCount)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
                val tempUri =
                    ContentUris.withAppendedId(uri, id)
                uriList.add(tempUri)
                Log.d(TAG, "image uri is $tempUri")
            }
            cursor.close()
        }
        return null
    }

    /**
     * 获得图片文件的Uri，共享Picture目录下
     * @param context Context
     * @param displayName String
     * @param directory String?
     * @param mimeType String
     * @param systemDirectory String
     * @return Uri?
     */
    @JvmStatic
    fun getImageFileUri(
        context: Context,
        displayName: String,
        directory: String? = "",
        mimeType: String = IMAGE_TYPE + "jpeg",
        systemDirectory: String = Environment.DIRECTORY_PICTURES,
    ): Uri? {
        return context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            getFileContentValues(mimeType, directory, systemDirectory, displayName)
        )
    }

    /**
     * 获得视频文件的Uri，共享Movie目录下
     * @param context Context
     * @param displayName String
     * @param directory String?
     * @param mimeType String
     * @param systemDirectory String
     * @return Uri?
     */
    @JvmStatic
    fun getVideoFileUri(
        context: Context,
        displayName: String,
        directory: String? = "",
        mimeType: String = VIDEO_TYPE + "mp4",
        systemDirectory: String = Environment.DIRECTORY_MOVIES,
    ): Uri? {
        return context.contentResolver.insert(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            getFileContentValues(mimeType, directory, systemDirectory, displayName)
        )
    }

    /**
     * 获得音频文件的Uri，共享Music目录下
     * @param context Context
     * @param displayName String
     * @param directory String?
     * @param mimeType String
     * @param systemDirectory String
     * @return Uri?
     */
    @JvmStatic
    fun getAudioFileUri(
        context: Context,
        displayName: String,
        directory: String? = "",
        mimeType: String = AUDIO_TYPE + "mp3",
        systemDirectory: String = Environment.DIRECTORY_MUSIC,
    ): Uri? {
        return context.contentResolver.insert(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            getFileContentValues(mimeType, directory, systemDirectory, displayName)
        )
    }

    /**
     * 私有目录创建文件
     */
    fun getPrivateFile(
        context: Context,
        fileName: String,
        useExternalStorage: Boolean = true
    ): File {
        // 根据 useExternalStorage 参数决定使用内部存储还是外部存储
        val fileDir = if (useExternalStorage) {
            File(context.getExternalFilesDir(null), "Shared")
        } else {
            File(context.filesDir, "Shared")
        }

        // 检查并创建 Shared 目录
        if (!fileDir.exists()) {
            fileDir.mkdirs()
        }

        // 在 Shared 目录下创建文件
        val file = File(fileDir, fileName)
        if (!file.exists()) {
            file.createNewFile()
        }
        // 通过 FileProvider 获取 content:// URI
        return file
    }

    /**
     * 获取Uri
     * @param file 必须是[getPrivateFile]的返回值，之所以拆开是因为直接从Uri获取Path不方便。
     */
    fun getUri(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider.file",  // 与 AndroidManifest 中的 authorities 匹配
            file
        )
    }


}

class FileUtilProvider : FileProvider()