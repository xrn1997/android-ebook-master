package com.ebook.db.entity

import android.os.Parcelable
import io.objectbox.annotation.Backlink
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToOne
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize


/**
 * 章节列表
 */
@Parcelize
@Entity
data class ChapterList(
    /**
     * 对应BookInfo noteUrl;
     */
    var noteUrl: String? = null,
    /**
     * 当前章节数
     */
    @JvmField
    var durChapterIndex: Int = 0,
    /**
     * 当前章节对应的文章地址
     */
    var durChapterUrl: String? = null,
    /**
     * 当前章节名称
     */
    var durChapterName: String? = null,
    var tag: String? = null,
    var hasCache: Boolean = false,
    @Id var id: Long = 0
) : Parcelable {
    constructor(
        noteUrl: String?,
        durChapterIndex: Int,
        durChapterUrl: String?,
        durChapterName: String?,
        tag: String?,
        hasCache: Boolean
    ) : this() {
        this.noteUrl = noteUrl
        this.durChapterIndex = durChapterIndex
        this.durChapterUrl = durChapterUrl
        this.durChapterName = durChapterName
        this.tag = tag
        this.hasCache = hasCache
    }

    @IgnoredOnParcel
    lateinit var bookInfo: ToOne<BookInfo>

    @IgnoredOnParcel
    lateinit var bookContent: ToOne<BookContent>

    fun clone(): ChapterList {
        return this.copy()
    }
}
