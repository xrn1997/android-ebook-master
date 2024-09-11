package com.ebook.db.entity

import android.os.Parcelable
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import kotlinx.parcelize.Parcelize


/**
 * 书本信息
 */
@Parcelize
@Entity
data class BookInfo(
    /**
     * 小说名
     */
    var name: String? = null,
    var tag: String? = null,
    /**
     * 如果是来源网站   则小说根地址 /如果是本地  则是小说本地MD5
     */
    var noteUrl: String? = null,
    /**
     * 章节目录地址
     */
    var chapterUrl: String? = null,
    /**
     * 章节列表
     */
    @JvmField
    @Transient
    var chapterlist: List<ChapterList>? = ArrayList(),
    /**
     * 章节最后更新时间
     */
    @JvmField
    var finalRefreshData: Long = 0,
    /**
     * 小说封面
     */
    var coverUrl: String? = null,
    /**
     * 作者
     */
    var author: String? = null,
    /**
     * 简介
     */
    var introduce: String? = null,
    /**
     * 来源
     */
    var origin: String? = null,
    /**
     * 状态，连载or完结
     */
    var status: String? = null,
    @Id var id: Long = 0
) : Parcelable {

    fun clone(): BookInfo {
        return this.copy()
    }
}