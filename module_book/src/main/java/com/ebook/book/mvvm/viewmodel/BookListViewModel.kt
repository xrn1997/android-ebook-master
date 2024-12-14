package com.ebook.book.mvvm.viewmodel

import android.app.Application
import com.ebook.book.mvvm.model.BookListModel
import com.ebook.db.entity.BookShelf
import com.xrn1997.common.event.SimpleObserver
import com.xrn1997.common.mvvm.viewmodel.BaseRefreshViewModel

class BookListViewModel(
    application: Application,
    model: BookListModel
) : BaseRefreshViewModel<BookShelf, BookListModel>(application, model) {
    override fun refreshData() {
        mModel.getBookShelfList()
            .doOnSubscribe(this)
            .subscribe(object : SimpleObserver<List<BookShelf>>() {

            override fun onNext(value: List<BookShelf>) {
                if (value.isNotEmpty()) {
                    mList.value = value
                }
                postStopRefreshEvent(true)
            }

            override fun onError(e: Throwable) {
                postStopRefreshEvent(false)
            }

        })
    }

    override fun loadMore() {
    }


}
