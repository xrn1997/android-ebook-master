package com.ebook.db.entity

import android.os.Parcelable
import com.ebook.db.event.DBCode
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToOne
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize


/**
 * 书架item
 */
@Parcelize
@Entity
data class BookShelf(
    /**
     * 对应BookInfo noteUrl;
     */
    @JvmField
    var noteUrl: String? = null,
    /**
     * 当前章节 （包括番外）
     */
    @JvmField
    var durChapter: Int = 0,
    @JvmField
    var durChapterPage: Int = DBCode.BookContentView.DURPAGEINDEXBEGIN,
    /**
     * 最后阅读时间
     */
    @JvmField
    var finalDate: Long = 0,
    var tag: String? = null,
    @Id var id: Long = 0
) : Parcelable {
    companion object {
        /**
         * 更新时间间隔 至少
         */
        @Transient
        const val REFRESH_TIME: Long = (5 * 60 * 1000).toLong()

        @Transient
        const val LOCAL_TAG: String = "loc_book"
    }

    @IgnoredOnParcel
    lateinit var bookInfo: ToOne<BookInfo>

    fun clone(): BookShelf {
        return this.copy()
    }
}