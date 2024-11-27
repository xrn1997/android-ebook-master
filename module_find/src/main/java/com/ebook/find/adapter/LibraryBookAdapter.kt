package com.ebook.find.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableArrayList
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.ebook.db.entity.BookShelf
import com.ebook.db.entity.SearchBook
import com.ebook.find.R
import com.ebook.find.databinding.AdapterLibraryKindbookBinding
import com.xrn1997.common.adapter.BaseBindAdapter

class LibraryBookAdapter(context: Context, items: ObservableArrayList<SearchBook>) :
    BaseBindAdapter<SearchBook, AdapterLibraryKindbookBinding>(context, items) {
    override fun getLayoutItemId(viewType: Int): Int {
        return R.layout.adapter_library_kindbook
    }

    override fun onBindItem(
        binding: AdapterLibraryKindbookBinding,
        item: SearchBook,
        position: Int
    ) {
        binding.searchbook = item
        val bookShelf = BookShelf()
        bookShelf.noteUrl = item.noteUrl
        binding.ibContent.setOnClickListener { mOnItemClickListener?.invoke(item, position) }
    }

    companion object {
        @JvmStatic
        @BindingAdapter(value = ["imageUrl", "placeHolder"], requireAll = false)
        fun loadImage(imageView: ImageView, url: String?, holderDrawable: Drawable?) {
            // Log.d("glide_cover", "loadImage url: "+url);
            Glide.with(imageView.context)
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .dontAnimate()
                .placeholder(holderDrawable)
                .error(holderDrawable)
                .fallback(holderDrawable)
                .fitCenter()
                .into(imageView)
        }
    }
}
