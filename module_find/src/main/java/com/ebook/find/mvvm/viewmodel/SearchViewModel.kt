package com.ebook.find.mvvm.viewmodel

import android.app.Application
import android.util.Log
import com.ebook.basebook.mvp.model.impl.WebBookModelImpl
import com.ebook.basebook.utils.NetworkUtil
import com.ebook.common.event.RxBusTag
import com.ebook.db.ObjectBoxManager.bookShelfBox
import com.ebook.db.entity.BookShelf
import com.ebook.db.entity.SearchBook
import com.ebook.db.entity.SearchHistory
import com.ebook.db.entity.WebChapter
import com.ebook.find.mvvm.model.LibraryModel
import com.ebook.find.mvvm.model.SearchModel
import com.hwangjr.rxbus.RxBus
import com.xrn1997.common.event.SimpleObserver
import com.xrn1997.common.event.SingleLiveEvent
import com.xrn1997.common.mvvm.viewmodel.BaseRefreshViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.schedulers.Schedulers

class SearchViewModel(
    application: Application,
    model: SearchModel
) : BaseRefreshViewModel<SearchBook, SearchModel>(application, model) {
    val bookShelves: MutableList<BookShelf> = ArrayList() //用来比对搜索的书籍是否已经添加进书架
    private var hasSearch = false //判断是否搜索过
    var page: Int = 1
        private set
    private var durSearchKey: String = ""
    var isInput = false
    val successEvent: SingleLiveEvent<List<SearchHistory>> = SingleLiveEvent()
    val addBookShelfFailedEvent: SingleLiveEvent<Int> = SingleLiveEvent()

    init {
        Observable.create { e: ObservableEmitter<List<BookShelf>> ->
            var temp: List<BookShelf>
            bookShelfBox.query().build().use { query ->
                temp = query.find()
            }
            e.onNext(temp)
            e.onComplete()
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe(this)
            .subscribe(object : SimpleObserver<List<BookShelf>>() {
                override fun onNext(value: List<BookShelf>) {
                    bookShelves.addAll(value)
                }

                override fun onError(e: Throwable) {
                    Log.e(TAG, "onError: ", e)
                }
            })
    }


    override fun loadMore() {
        searchBook(durSearchKey)
    }

    fun getHasSearch(): Boolean {
        return hasSearch
    }

    fun setHasSearch(hasSearch: Boolean) {
        this.hasSearch = hasSearch
    }

    fun insertSearchHistory(content: String) {

        mModel.insertSearchHistory(BOOK, content)
            .doOnSubscribe(this)
            .subscribe(object : SimpleObserver<SearchHistory>() {
                override fun onNext(value: SearchHistory) {
                    querySearchHistory(value.content)
                }

                override fun onError(e: Throwable) {
                    Log.e(TAG, "onError: ", e)
                }
            })
    }

    fun cleanSearchHistory(content: String) {
        mModel.cleanSearchHistory(BOOK, content)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe(this)
            .subscribe(object : SimpleObserver<Int>() {
                override fun onNext(value: Int) {
                    if (value > 0) {
                        successEvent.setValue(listOf())
                    }
                }

                override fun onError(e: Throwable) {
                    Log.e(TAG, "onError: ", e)
                }
            })
    }

    fun querySearchHistory(content: String) {
        mModel.querySearchHistory(BOOK, content)
            .doOnSubscribe(this)
            .subscribe(object : SimpleObserver<List<SearchHistory>>() {
                override fun onNext(value: List<SearchHistory>) {
                    successEvent.setValue(value)
                }

                override fun onError(e: Throwable) {
                }
            })
    }

    fun initPage() {
        this.page = 1
    }

    fun toSearchBooks(content: String) {
        if (content.isEmpty()) {
            return
        }
        postShowLoadingViewEvent(true)
        durSearchKey = content
        searchBook(durSearchKey)
    }

    private fun searchBook(content: String) {
        WebBookModelImpl.getInstance().searchBook(content, page)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .doOnSubscribe(this)
            .subscribe(object : SimpleObserver<List<SearchBook>>() {
                override fun onNext(value: List<SearchBook>) {
                    for (temp in value) {
                        for ((noteUrl) in bookShelves) {
                            if (temp.noteUrl == noteUrl) {
                                temp.add = true
                                break
                            }
                        }
                    }
                    if (page == 1) {
                        mList.value = value
                    } else if (value.isNotEmpty()) {
                        //todo 可能有性能问题
                        val list = mList.value ?: emptyList()
                        mList.value = list + value
                    }
                    postShowLoadingViewEvent(false)
                    postStopLoadMoreEvent(true)
                }

                override fun onError(e: Throwable) {
                    postShowLoadingViewEvent(false)
                    postStopLoadMoreEvent(false)
                }
            })
        }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    fun addBookToShelf(searchBook: SearchBook) {
        postShowLoadingViewEvent(true)
        //  Log.e("添加到书架", searchBook.toString());
        val bookShelfResult = BookShelf()
        bookShelfResult.noteUrl = searchBook.noteUrl
        bookShelfResult.finalDate = 0
        bookShelfResult.durChapter = 0
        bookShelfResult.durChapterPage = 0
        bookShelfResult.tag = searchBook.tag
        WebBookModelImpl.getInstance().getBookInfo(bookShelfResult)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe(this)
            .flatMap { fetchedBookShelf ->
                // 获取章节列表
                WebBookModelImpl.getInstance().getChapterList(fetchedBookShelf)
            }
            .subscribe(object : SimpleObserver<WebChapter<BookShelf>>() {
                override fun onNext(bookShelfWebChapter: WebChapter<BookShelf>) {
                    saveBookToShelf(bookShelfWebChapter.data)
                    postShowLoadingViewEvent(false)
                }

                override fun onError(e: Throwable) {
                    addBookShelfFailedEvent.setValue(NetworkUtil.ERROR_CODE_OUTTIME)
                    postShowLoadingViewEvent(false)
                }
            })
    }

    private fun saveBookToShelf(bookShelf: BookShelf) {
        LibraryModel.saveBookToShelf(bookShelf)
            .doOnSubscribe(this)
            .subscribe(object : SimpleObserver<BookShelf>() {
                override fun onNext(value: BookShelf) {
                    //成功   //发送RxBus
                    RxBus.get().post(RxBusTag.HAD_ADD_BOOK, value)
                }

                override fun onError(e: Throwable) {
                    addBookShelfFailedEvent.setValue(NetworkUtil.ERROR_CODE_OUTTIME)
                }
            })
    }

    override fun refreshData() {}

    companion object {
        const val TAG: String = "SearchPresenterImpl"
        const val BOOK: Int = 2
    }
}