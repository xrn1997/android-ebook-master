package com.ebook.book.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableArrayList
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.ebook.book.R
import com.ebook.book.databinding.AdapterBookListItemBinding
import com.ebook.db.entity.BookShelf
import com.xrn1997.common.adapter.BaseBindAdapter

class BookListAdapter(context: Context, items: ObservableArrayList<BookShelf>) :
    BaseBindAdapter<BookShelf, AdapterBookListItemBinding>(context, items) {
    override fun getLayoutItemId(viewType: Int): Int {
        return R.layout.adapter_book_list_item
    }

    override fun onBindItem(binding: AdapterBookListItemBinding, item: BookShelf, position: Int) {
        binding.bookshelf = item
        binding.viewBookDetail.setOnClickListener {
            mOnItemClickListener?.invoke(item, position)
        }
        binding.viewBookDetail.setOnLongClickListener {
            mOnItemLongClickListener?.invoke(item, position) ?: false
        }
    }

    companion object {
        @JvmStatic
        @BindingAdapter(value = ["imageUrl", "placeHolder"], requireAll = false)
        fun loadImage(imageView: ImageView, url: String?, holderDrawable: Drawable?) {
            // Log.d("glide_cover", "loadImage url: "+url);
            Glide.with(imageView.context)
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .fitCenter()
                .dontAnimate()
                .placeholder(holderDrawable)
                .into(imageView)
        }
    }
}
