package com.xrn1997.common.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

/**
 * 使用ViewBinding的ListAdapter
 * @author xrn1997
 */
@Suppress("unused")
abstract class BaseBindAdapter<T, V : ViewBinding>(
    @JvmField
    protected var context: Context,
    diffCallback: DiffUtil.ItemCallback<T>
) : ListAdapter<T, BaseBindAdapter.BaseBindingViewHolder<V>>(diffCallback) {

    @JvmField
    protected var mOnItemClickListener: ((e: T, position: Int) -> Unit)? = null

    @JvmField
    protected var mOnItemLongClickListener: ((e: T, position: Int) -> Boolean)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseBindingViewHolder<V> {
        val binding: V = onBindViewBinding((LayoutInflater.from(context)), parent, false, viewType)
        return BaseBindingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BaseBindingViewHolder<V>, position: Int) {
        onBindItem(holder.binding, getItem(position), position)
    }

    class BaseBindingViewHolder<V : ViewBinding>(val binding: V) :
        RecyclerView.ViewHolder(binding.root)

    /**
     * item监听
     */
    fun setOnItemClickListener(onItemClickListener: (e: T, position: Int) -> Unit) {
        mOnItemClickListener = onItemClickListener
    }

    /**
     * item长按监听
     */
    fun setOnItemLongClickListener(onItemLongClickListener: (e: T, position: Int) -> Boolean) {
        mOnItemLongClickListener = onItemLongClickListener
    }

    /**
     * 这个方法返回需要绑定的ViewBinding
     * @param inflater LayoutInflater
     * @param parent ViewGroup? 整合到哪
     * @param attachToParent Boolean  这里返回的是false
     * @param viewType view类别
     * @return V
     */
    protected abstract fun onBindViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup,
        attachToParent: Boolean,
        viewType: Int
    ): V

    /**
     * 绑定数据
     *
     * @param binding   viewBinding
     * @param item        item对象
     * @param position 索引
     */
    protected abstract fun onBindItem(binding: V, item: T, position: Int)
}