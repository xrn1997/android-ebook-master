package com.ebook.db

import android.content.Context
import com.ebook.db.entity.BookContent
import com.ebook.db.entity.BookInfo
import com.ebook.db.entity.BookShelf
import com.ebook.db.entity.ChapterList
import com.ebook.db.entity.MyObjectBox
import com.ebook.db.entity.SearchHistory
import io.objectbox.Box
import io.objectbox.BoxStore

/**
 * db manager
 * @author xrn1997
 * @date 2021/6/14
 */
object ObjectBoxManager {
    lateinit var store: BoxStore
        private set

    /**
     * Application处初始化
     */
    @JvmStatic
    fun init(context: Context) {
        store = MyObjectBox.builder()
            .androidContext(context.applicationContext)
            .build()
    }

    fun getBookShelfBox(): Box<BookShelf> {
        return store.boxFor(BookShelf::class.java)
    }

    fun getBookContentBox(): Box<BookContent> {
        return store.boxFor(BookContent::class.java)
    }

    fun getBookInfoBox(): Box<BookInfo> {
        return store.boxFor(BookInfo::class.java)
    }

    fun getChapterListBox(): Box<ChapterList> {
        return store.boxFor(ChapterList::class.java)
    }

    fun getSearchHistoryBox(): Box<SearchHistory> {
        return store.boxFor(SearchHistory::class.java)
    }

}