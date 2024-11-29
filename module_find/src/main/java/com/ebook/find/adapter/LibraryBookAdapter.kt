package com.ebook.find.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.ebook.db.entity.BookShelf
import com.ebook.db.entity.SearchBook
import com.ebook.find.databinding.AdapterLibraryKindbookBinding
import com.xrn1997.common.adapter.BaseBindAdapter

class LibraryBookAdapter(context: Context) :
    BaseBindAdapter<SearchBook, AdapterLibraryKindbookBinding>(
        context,
        SearchBookDifferCallback()
    ) {
    override fun onBindItem(
        binding: AdapterLibraryKindbookBinding,
        item: SearchBook,
        position: Int
    ) {
        Glide.with(context)
            .load(item.coverUrl)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .dontAnimate()
            .placeholder(com.ebook.basebook.R.drawable.img_cover_default)
            .error(com.ebook.basebook.R.drawable.img_cover_default)
            .fallback(com.ebook.basebook.R.drawable.img_cover_default)
            .fitCenter()
            .into(binding.ivCover)
        binding.tvName.text = item.name
        binding.tvAuthor.text = item.author
        val bookShelf = BookShelf()
        bookShelf.noteUrl = item.noteUrl
        binding.ibContent.setOnClickListener { mOnItemClickListener?.invoke(item, position) }
    }

    override fun onBindViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup,
        attachToParent: Boolean,
        viewType: Int
    ): AdapterLibraryKindbookBinding {
        return AdapterLibraryKindbookBinding.inflate(inflater, parent, attachToParent)
    }

    class SearchBookDifferCallback : DiffUtil.ItemCallback<SearchBook>() {
        override fun areItemsTheSame(oldItem: SearchBook, newItem: SearchBook): Boolean {
            return oldItem.noteUrl == newItem.noteUrl
        }

        override fun areContentsTheSame(oldItem: SearchBook, newItem: SearchBook): Boolean {
            return oldItem == newItem
        }

    }
}
