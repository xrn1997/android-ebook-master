package com.ebook.book.mvvm.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.ebook.book.mvvm.model.BookCommentsModel
import com.ebook.book.mvvm.model.BookDetailModel
import com.ebook.book.mvvm.model.BookImportModel
import com.ebook.book.mvvm.model.BookListModel
import com.ebook.book.mvvm.model.BookReadModel
import com.ebook.book.mvvm.viewmodel.BookCommentsViewModel
import com.ebook.book.mvvm.viewmodel.BookDetailViewModel
import com.ebook.book.mvvm.viewmodel.BookImportViewModel
import com.ebook.book.mvvm.viewmodel.BookListViewModel
import com.ebook.book.mvvm.viewmodel.BookReadViewModel

object BookViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        // 通过extras获取application
        val mApplication = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!
        return when (modelClass) {
            BookListViewModel::class.java -> {
                BookListViewModel(mApplication, BookListModel(mApplication))
            }

            BookCommentsViewModel::class.java -> {
                BookCommentsViewModel(mApplication, BookCommentsModel(mApplication))
            }
            BookDetailViewModel::class.java -> {
                BookDetailViewModel(mApplication, BookDetailModel(mApplication))
            }

            BookImportViewModel::class.java -> {
                BookImportViewModel(mApplication, BookImportModel(mApplication))
            }

            BookReadViewModel::class.java -> {
                BookReadViewModel(mApplication, BookReadModel(mApplication))
            }

            else -> throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName())
        } as T
    }
}
