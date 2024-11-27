package com.ebook.me.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableArrayList
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.ebook.api.config.API
import com.ebook.api.entity.Comment
import com.ebook.common.view.profilePhoto.CircleImageView
import com.ebook.me.R
import com.ebook.me.databinding.AdapterCommentListItemBinding
import com.xrn1997.common.adapter.BaseBindAdapter


@Suppress("unused")
class CommentListAdapter(context: Context, items: ObservableArrayList<Comment>) :
    BaseBindAdapter<Comment, AdapterCommentListItemBinding>(context, items) {
    override fun getLayoutItemId(viewType: Int): Int {
        return R.layout.adapter_comment_list_item
    }

    override fun onBindItem(binding: AdapterCommentListItemBinding, item: Comment, position: Int) {
        binding.comment = item
        binding.layoutCommentItem.setOnClickListener {
            mOnItemClickListener?.invoke(item, position)
        }
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
