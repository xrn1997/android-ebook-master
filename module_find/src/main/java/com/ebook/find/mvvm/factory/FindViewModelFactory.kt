package com.ebook.find.mvvm.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.ebook.find.mvvm.model.LibraryModel
import com.ebook.find.mvvm.model.SearchModel
import com.ebook.find.mvvm.viewmodel.ChoiceBookViewModel
import com.ebook.find.mvvm.viewmodel.LibraryViewModel
import com.ebook.find.mvvm.viewmodel.SearchViewModel


object FindViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        // 通过extras获取application
        val mApplication = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!
        return when (modelClass) {
            LibraryViewModel::class.java -> {
                LibraryViewModel(mApplication, LibraryModel(mApplication))
            }
            ChoiceBookViewModel::class.java -> {
                ChoiceBookViewModel(mApplication, SearchModel(mApplication))
            }

            SearchViewModel::class.java -> {
                SearchViewModel(mApplication, SearchModel(mApplication))
            }

            else -> throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName())
        } as T
    }
}
