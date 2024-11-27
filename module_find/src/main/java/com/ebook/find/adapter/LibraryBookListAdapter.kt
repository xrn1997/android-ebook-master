package com.ebook.find.adapter

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.databinding.ObservableArrayList
import com.blankj.utilcode.util.ActivityUtils
import com.ebook.basebook.mvp.presenter.impl.BookDetailPresenterImpl
import com.ebook.basebook.mvp.view.impl.BookDetailActivity
import com.ebook.db.entity.LibraryKindBookList
import com.ebook.db.entity.SearchBook
import com.ebook.find.R
import com.ebook.find.databinding.ViewLibraryKindbookBinding
import com.ebook.find.mvp.view.impl.ChoiceBookActivity
import com.xrn1997.common.adapter.BaseBindAdapter
import com.xrn1997.common.util.ObservableListUtil.getListChangedCallback

class LibraryBookListAdapter(context: Context, items: ObservableArrayList<LibraryKindBookList>) :
    BaseBindAdapter<LibraryKindBookList, ViewLibraryKindbookBinding>(context, items) {
    override fun getLayoutItemId(viewType: Int): Int {
        return R.layout.view_library_kindbook
    }

    override fun onBindItem(
        binding: ViewLibraryKindbookBinding,
        item: LibraryKindBookList,
        position: Int
    ) {
        binding.libraryKindBookList = item
        val searchBooks = ObservableArrayList<SearchBook>()
        searchBooks.addAll(item.books)
        val libraryBookAdapter = LibraryBookAdapter(context, searchBooks)

        searchBooks.addOnListChangedCallback(getListChangedCallback(libraryBookAdapter))
        if (item.kindUrl.isEmpty()) {
            binding.tvMore.visibility = View.GONE
            binding.tvMore.setOnClickListener(null)
        } else {
            binding.tvMore.visibility = View.VISIBLE
            binding.tvMore.setOnClickListener {
                ChoiceBookActivity.startChoiceBookActivity(context, item.kindName, item.kindUrl)
            }
        }
        libraryBookAdapter.setOnItemClickListener { searchBook: SearchBook?, _: Int? ->
            val intent = Intent(context, BookDetailActivity::class.java)
            intent.putExtra("from", BookDetailPresenterImpl.FROM_SEARCH)
            intent.putExtra("data", searchBook)
            ActivityUtils.startActivity(intent)
        }
        binding.rvBooklist.adapter = libraryBookAdapter
    }
}
