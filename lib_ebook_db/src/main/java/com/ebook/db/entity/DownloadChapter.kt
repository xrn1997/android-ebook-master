package com.ebook.db.entity

import android.os.Parcelable
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity
data class DownloadChapter(
    @Id var id: Long = 0,
    @JvmField
    var noteUrl: String? = null,
    /**
     * 当前章节数
     */
    @JvmField
    var durChapterIndex: Int = 0,
    /**
     * 当前章节对应的文章地址
     */
    @JvmField
    var durChapterUrl: String? = null,
    /**
     * 当前章节名称
     */
    @JvmField
    var durChapterName: String? = null,

    @JvmField
    var tag: String? = null,

    @JvmField
    var bookName: String? = null,
    /**
     * 小说封面
     */
    @JvmField
    var coverUrl: String? = null,

    ) : Parcelable
