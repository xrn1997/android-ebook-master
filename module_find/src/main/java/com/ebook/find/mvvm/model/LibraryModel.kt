package com.ebook.find.mvvm.model

import android.app.Application
import com.ebook.basebook.cache.ACache
import com.ebook.basebook.constant.Url
import com.ebook.basebook.mvp.model.impl.WebBookModelImpl
import com.ebook.db.ObjectBoxManager.bookShelfBox
import com.ebook.db.ObjectBoxManager.searchHistoryBox
import com.ebook.db.entity.BookShelf
import com.ebook.db.entity.BookShelf_
import com.ebook.db.entity.Library
import com.ebook.db.entity.SearchHistory
import com.ebook.db.entity.SearchHistory_
import com.ebook.find.entity.BookType
import com.xrn1997.common.mvvm.model.BaseModel
import io.objectbox.query.QueryBuilder
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.schedulers.Schedulers

@Suppress("unused")
class LibraryModel(application: Application) : BaseModel(application) {
    //获取书籍类型信息，此处用本地数据。
    fun getBookTypeList(): List<BookType> {
        val bookTypeList: MutableList<BookType> = ArrayList()
            bookTypeList.add(BookType("玄幻小说", Url.xh))
            bookTypeList.add(BookType("修真小说", Url.xz))
            bookTypeList.add(BookType("都市小说", Url.ds))
            bookTypeList.add(BookType("历史小说", Url.ls))
            bookTypeList.add(BookType("网游小说", Url.wy))
            bookTypeList.add(BookType("科幻小说", Url.kh))
            bookTypeList.add(BookType("其他小说", Url.qt))
            return bookTypeList
        }

    companion object {
        private const val LIBRARY_CACHE_KEY: String = "cache_library"

        //获得书库信息
        @JvmStatic
        fun getLibraryData(mCache: ACache): Observable<Library> {
            return Observable.create { e: ObservableEmitter<String> ->
                val cache = mCache.getAsString(LIBRARY_CACHE_KEY)
                e.onNext(cache)
                e.onComplete()
            }.flatMap { s: String ->
                WebBookModelImpl.getInstance().analyzeLibraryData(s)
            }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        }

        //获取书架书籍列表信息
        @JvmStatic
        fun getBookShelfList(): Observable<List<BookShelf>> {
            return Observable.create { e: ObservableEmitter<List<BookShelf>> ->
                try {
                    bookShelfBox.query().build().use { query ->
                        val temp = query.find()
                        e.onNext(temp)
                        e.onComplete()
                    }
                } catch (ex: Exception) {
                    e.onError(ex)
                }
            }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        }

        //将书籍信息存入书架书籍列表
        @JvmStatic
        fun saveBookToShelf(bookShelf: BookShelf): Observable<BookShelf> {
            return Observable.create { e: ObservableEmitter<BookShelf> ->
                bookShelfBox.query(BookShelf_.noteUrl.equal(bookShelf.noteUrl))
                    .build().use { query ->
                        val temp = query.findFirst()
                        if (temp != null) {
                            bookShelf.id = temp.id
                        } else {
                            bookShelf.id = 0L
                        }
                    }
                //网络数据获取成功  存入BookShelf表数据库
                bookShelfBox.put(bookShelf)
                e.onNext(bookShelf)
                e.onComplete()
            }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        }

        //保存查询记录
        @JvmStatic
        fun insertSearchHistory(type: Int, content: String): Observable<SearchHistory> {
            return Observable.create { e: ObservableEmitter<SearchHistory> ->
                val boxStore = searchHistoryBox
                boxStore
                    .query(
                        SearchHistory_.type.equal(type).and(SearchHistory_.content.equal(content))
                    )
                    .build().use { query ->
                        val searchHistories = query.find(0, 1)
                        val searchHistory: SearchHistory
                        if (searchHistories.isNotEmpty()) {
                            searchHistory = searchHistories[0]
                            searchHistory.date = System.currentTimeMillis()
                            boxStore.put(searchHistory)
                        } else {
                            searchHistory =
                                SearchHistory(type, content, System.currentTimeMillis())
                            boxStore.put(searchHistory)
                        }
                        e.onNext(searchHistory)
                    }
            }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        }

        //删除查询记录
        @JvmStatic
        fun cleanSearchHistory(type: Int, content: String): Observable<Int> {
            return Observable.create { e: ObservableEmitter<Int> ->
                val boxStore = searchHistoryBox
                boxStore
                    .query(SearchHistory_.type.equal(type))
                    .contains(
                        SearchHistory_.content,
                        content,
                        QueryBuilder.StringOrder.CASE_INSENSITIVE
                    ) // 等同于 SQL 中的 "content LIKE ?"
                    .build().use { query ->
                        val histories = query.find()
                        boxStore.remove(histories)
                        e.onNext(histories.size)
                    }
            }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        }

        //获得查询记录
        @JvmStatic
        fun querySearchHistory(type: Int, content: String): Observable<List<SearchHistory>> {
            return Observable.create { e: ObservableEmitter<List<SearchHistory>> ->
                searchHistoryBox
                    .query(SearchHistory_.type.equal(type))
                    .contains(
                        SearchHistory_.content,
                        content,
                        QueryBuilder.StringOrder.CASE_INSENSITIVE
                    )
                    .order(SearchHistory_.date, QueryBuilder.DESCENDING)
                    .build().use { query ->
                        val histories = query.find(0, 20)
                        e.onNext(histories)
                    }
            }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        }
    }
}
