package com.ebook.book.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableArrayList
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.ebook.api.config.API
import com.ebook.api.entity.Comment
import com.ebook.book.R
import com.ebook.book.databinding.AdpaterBookCommentsItemBinding
import com.ebook.common.view.profilePhoto.CircleImageView
import com.xrn1997.common.adapter.BaseBindAdapter

class BookCommentsAdapter(context: Context, items: ObservableArrayList<Comment>) :
    BaseBindAdapter<Comment, AdpaterBookCommentsItemBinding>(context, items) {
    override fun getLayoutItemId(viewType: Int): Int {
        return R.layout.adpater_book_comments_item
    }

    override fun onBindItem(binding: AdpaterBookCommentsItemBinding, item: Comment, position: Int) {
        binding.comment = item
        binding.layoutCommentItem.setOnLongClickListener {
            mOnItemLongClickListener?.invoke(item, position) ?: false
        }
    }

    companion object {
        @JvmStatic
        @BindingAdapter(value = ["imageUrl", "placeHolder"], requireAll = false)
        fun loadImage(imageView: CircleImageView, url: String, holderDrawable: Drawable?) {
            // Log.d("glide_cover", "loadImage url: "+url);
            Glide.with(imageView.context)
                .load(API.URL_HOST_USER + "user/image/" + url)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .fitCenter()
                .dontAnimate()
                .placeholder(holderDrawable)
                .into(imageView)
        }
    }
}
