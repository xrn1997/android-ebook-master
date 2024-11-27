package com.ebook.find.mvvm.viewmodel

import android.app.Application
import android.util.Log
import androidx.databinding.ObservableArrayList
import com.ebook.basebook.cache.ACache
import com.ebook.basebook.mvp.model.impl.WebBookModelImpl
import com.ebook.db.entity.Library
import com.ebook.db.entity.LibraryKindBookList
import com.ebook.find.entity.BookType
import com.ebook.find.mvvm.model.LibraryModel
import com.ebook.find.mvvm.model.LibraryModel.Companion.getLibraryData
import com.xrn1997.common.event.SimpleObserver
import com.xrn1997.common.mvvm.viewmodel.BaseRefreshViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

class LibraryViewModel(application: Application, model: LibraryModel) :
    BaseRefreshViewModel<Library, LibraryModel>(application, model) {
    private val bookTypes = ObservableArrayList<BookType>()
    val libraryKindBookLists: ObservableArrayList<LibraryKindBookList> = ObservableArrayList()
    private val mCache: ACache = ACache.get(application.applicationContext)
    private var isFirst = true

    override fun refreshData() {
        //   Log.d(TAG, "refreshData: start");
        if (isFirst) {
            isFirst = false
            getLibraryData(mCache)
                .subscribe(object : SimpleObserver<Library>() {
                    override fun onNext(value: Library) {
                        libraryKindBookLists.clear()
                        value.kindBooks?.let {
                            libraryKindBookLists.addAll(it)
                        }
                        //       Log.d(TAG, "refreshData onNext: " + value.toString());
                        getLibraryNewData()
                    }

                    override fun onError(e: Throwable) {
                        getLibraryNewData()
                    }
                })
        } else {
            getLibraryNewData()
        }
    }

    override fun enableLoadMore(): Boolean {
        return false
    }

    override fun loadMore() {
        postStopLoadMoreEvent(false)
    }

    private fun getLibraryNewData() {
        //   Log.d(TAG, "getLibraryNewData: start");
        WebBookModelImpl.getInstance().getLibraryData(mCache)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SimpleObserver<Library>() {
                override fun onNext(value: Library) {
                    //     Log.d(TAG, "refreshData onNext: " + value.getKindBooks().get(0).getKindName());
                    libraryKindBookLists.clear()
                    value.kindBooks?.let {
                        libraryKindBookLists.addAll(it)
                    }
                    postStopRefreshEvent(true)
                    //   Log.d(TAG, "refreshData onNext: finish");
                }

                override fun onError(e: Throwable) {
                    postStopRefreshEvent(false)
                    Log.e(TAG, "onError: ", e)
                }
            })
    }

    val bookTypeList: ObservableArrayList<BookType>
        get() {
            bookTypes.addAll(mModel.bookTypeList)
            return bookTypes
        }

    companion object {
        val TAG: String = LibraryViewModel::class.java.simpleName
        const val LIBRARY_CACHE_KEY: String = "cache_library"
    }
}
