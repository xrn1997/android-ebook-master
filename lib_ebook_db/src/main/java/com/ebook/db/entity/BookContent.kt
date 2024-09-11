package com.ebook.db.entity

import android.os.Parcelable
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Transient
import kotlinx.parcelize.Parcelize

/**
 * 书本缓存内容
 */
@Parcelize
@Entity
data class BookContent(
    /**
     * 对应BookInfo noteUrl;
     */
    @JvmField
    var durChapterUrl: String? = null,

    /**
     * 当前章节  （包括番外）
     */
    @JvmField
    var durChapterIndex: Int = 0,

    /**
     * 当前章节内容
     */
    @JvmField
    var durChapterContent: String? = null,


    /**
     * 来源  某个网站/本地
     */
    @JvmField
    var tag: String? = null,

    @Transient
    var right: Boolean? = true,

    @JvmField
    @Transient
    var lineContent: List<String>? = ArrayList(),

    @JvmField
    @Transient
    var lineSize: Float = 0f,
    @Id var id: Long = 0
) : Parcelable {
    fun clone(): BookContent {
        return this.copy()
    }
}


