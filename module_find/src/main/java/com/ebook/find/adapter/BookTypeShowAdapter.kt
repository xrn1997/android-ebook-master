package com.ebook.find.adapter

import android.content.Context
import androidx.databinding.ObservableArrayList
import com.ebook.find.R
import com.ebook.find.databinding.AdpaterBookTypeItemBinding
import com.ebook.find.entity.BookType
import com.xrn1997.common.adapter.BaseBindAdapter

class BookTypeShowAdapter(context: Context, items: ObservableArrayList<BookType>) :
    BaseBindAdapter<BookType, AdpaterBookTypeItemBinding>(context, items) {
    override fun getLayoutItemId(viewType: Int): Int {
        return R.layout.adpater_book_type_item
    }

    override fun onBindItem(binding: AdpaterBookTypeItemBinding, item: BookType, position: Int) {
        binding.booktype = item
        binding.viewBooktype.setOnClickListener { mOnItemClickListener?.invoke(item, position) }
    }
}
