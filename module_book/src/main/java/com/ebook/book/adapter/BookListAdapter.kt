package com.ebook.book.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.ebook.book.databinding.AdapterBookListItemBinding
import com.ebook.db.entity.BookShelf
import com.xrn1997.common.adapter.BaseBindAdapter

class BookListAdapter(context: Context) :
    BaseBindAdapter<BookShelf, AdapterBookListItemBinding>(context) {

    override fun onBindItem(binding: AdapterBookListItemBinding, item: BookShelf, position: Int) {
        binding.bookshelf = item
        binding.viewBookDetail.setOnClickListener {
            mOnItemClickListener?.invoke(item, position)
        }
        binding.viewBookDetail.setOnLongClickListener {
            mOnItemLongClickListener?.invoke(item, position) ?: false
        }
    }
    override fun onBindViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup,
        attachToParent: Boolean,
        viewType: Int
    ): AdapterBookListItemBinding {
        return AdapterBookListItemBinding.inflate(inflater, parent, attachToParent)
    }
}
