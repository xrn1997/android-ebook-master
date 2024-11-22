package com.xrn1997.common.util

/**
 * 常用正则表达式判断工具类
 * @author xrn1997
 * @date 2022/3/2 19:14
 */
@Suppress("unused")
object RegexUtil {
    /**
     * 手机号的正则表达式
     */
    const val PHONE_PATTERN =
        "^((13[0-9])|(14[579])|(15[0-35-9])|(16[2567])|(17[0-35-8])|(18[0-9])|(19[0-35-9]))\\d{8}\$"

    /**
     * 图片文件后缀名正则表达式
     */
    const val PICTURE_PATTERN = ".+(\\.jpeg|\\.jpg|\\.gif|\\.bmp|\\.png).*"

    /**
     * 视频文件后缀名正则表达式
     */
    const val VIDEO_PATTERN =
        ".+(\\.avi|\\.wmv|\\.mpeg|\\.mp4|\\.mov|\\.mkv|\\.flv|\\.f4v|\\.m4v|\\.rm|\\.rmvb|\\.3gp|\\.dat|\\.ts|\\.mts|\\.vob).*"

    /**
     * URL格式正则表达式
     */
    const val URL_PATTERN =
        "(https?|ftp|file)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]"

    /**
     * QQ号
     */
    const val QQ_PATTEN = "^[1-9](\\d){4,9}$"

    /**
     * 邮箱
     */
    const val EMAIL_PATTEN = "[a-zA-Z0-9_.]{1,}@(([a-zA-z0-9]-*){1,}\\.){1,3}[a-zA-z\\-]{1,}"

    /**
     * 检查是否符合正则表达式
     * @date 2022/2/24 20:50
     * @param str 待检测的字符串
     * @param regex  正则表达式
     * @return boolean
     */
    @JvmStatic
    fun checkRegex(regex: String, str: String): Boolean {
        val pattern = Regex(regex)
        return pattern.matches(str)
    }

    /**
     * 检查是否符合中国大陆手机号格式的正则表达式
     * @param phone String
     * @return Boolean
     */
    @JvmStatic
    fun checkPhone(phone: String): Boolean {
        return checkRegex(PHONE_PATTERN, phone)
    }

    /**
     * 检查是否符合视频文件名格式的正则表达式
     * @param video String
     * @return Boolean
     */
    @JvmStatic
    fun checkVideo(video: String): Boolean {
        return checkRegex(VIDEO_PATTERN, video.lowercase())
    }

    /**
     * 检查是否符合图片文件名格式的正则表达式
     * @param picture String
     * @return Boolean
     */
    @JvmStatic
    fun checkPicture(picture: String): Boolean {
        return checkRegex(PICTURE_PATTERN, picture.lowercase())
    }

    /**
     * 检查是否符合URL网址格式的正则表达式
     * @param url String
     * @return Boolean
     */
    @JvmStatic
    fun checkUrl(url: String): Boolean {
        return checkRegex(URL_PATTERN, url)
    }

    /**
     * 检查是否符合QQ号格式
     */
    fun checkQQ(qq: String): Boolean {
        return checkRegex(QQ_PATTEN, qq)
    }

    /**
     * 检测是否符合邮箱格式
     */
    fun checkEmail(email: String): Boolean {
        return checkRegex(EMAIL_PATTEN, email)
    }
}