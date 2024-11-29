package com.ebook.find.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.blankj.utilcode.util.ActivityUtils
import com.ebook.basebook.mvp.presenter.impl.BookDetailPresenterImpl
import com.ebook.basebook.mvp.view.impl.BookDetailActivity
import com.ebook.common.callback.LibraryKindBookListDifferCallback
import com.ebook.db.entity.LibraryKindBookList
import com.ebook.db.entity.SearchBook
import com.ebook.find.databinding.ViewLibraryKindbookBinding
import com.ebook.find.mvp.view.impl.ChoiceBookActivity
import com.xrn1997.common.adapter.BaseBindAdapter

class LibraryBookListAdapter(context: Context) :
    BaseBindAdapter<LibraryKindBookList, ViewLibraryKindbookBinding>(
        context, LibraryKindBookListDifferCallback()
    ) {
    override fun onBindItem(
        binding: ViewLibraryKindbookBinding,
        item: LibraryKindBookList,
        position: Int
    ) {
        binding.tvKindname.text = item.kindName
        val libraryBookAdapter = LibraryBookAdapter(context)
        libraryBookAdapter.submitList(item.books)
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

    override fun onBindViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup,
        attachToParent: Boolean,
        viewType: Int
    ): ViewLibraryKindbookBinding {
        return ViewLibraryKindbookBinding.inflate(inflater, parent, attachToParent)
    }

}
