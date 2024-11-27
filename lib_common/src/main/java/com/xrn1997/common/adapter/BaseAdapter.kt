package com.xrn1997.common.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * RecyclerView Adapter基类
 * @author : xrn1997
 */
@Suppress("unused")
abstract class BaseAdapter<E, VH : RecyclerView.ViewHolder>(
    @JvmField
    protected val mContext: Context
) : RecyclerView.Adapter<VH>() {
    protected val list: MutableList<E> = ArrayList()

    /**
     * 获取数据列表
     */
    val mList get() = list

    @JvmField
    protected var mOnItemClickListener: ((e: E, position: Int) -> Unit)? = null

    @JvmField
    protected var mOnItemLongClickListener: ((e: E, position: Int) -> Boolean)? = null

    /**
     * 创建并且返回ViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val layout = onBindLayout()
        val view = LayoutInflater.from(mContext).inflate(layout, parent, false)
        return onCreateHolder(view)
    }

    /**
     * ViewHolder 绑定数据
     */
    override fun onBindViewHolder(holder: VH, position: Int) {
        val e = list[position]
        holder.itemView.setOnClickListener { mOnItemClickListener?.invoke(e, position) }
        holder.itemView.setOnLongClickListener {
            mOnItemLongClickListener?.invoke(e, position) ?: false
        }
        onBindData(holder, e, position)
    }

    /**
     * 返回数据数量
     */
    override fun getItemCount(): Int {
        return list.size
    }

    /**
     * 添加所有数据,不会清空原有数据
     */
    @SuppressLint("NotifyDataSetChanged")
    fun addAll(list: List<E>?) {
        if (!list.isNullOrEmpty()) {
            this.list.addAll(list)
            notifyDataSetChanged()
        }
    }

    /**
     * 更新数据,会清空原有数据
     */
    @SuppressLint("NotifyDataSetChanged")
    fun refresh(list: List<E>?) {
        this.list.clear()
        if (!list.isNullOrEmpty()) {
            this.list.addAll(list)
        }
        notifyDataSetChanged()
    }

    /**
     * 根据位置删除数据
     */
    fun remove(position: Int) {
        list.removeAt(position)
        notifyItemRemoved(position)
    }

    /**
     * 根据对象删除数据
     */
    fun remove(e: E) {
        val p = list.indexOf(e)
        list.remove(e)
        notifyItemRemoved(p)
    }

    /**
     * 根据对象添加数据
     */
    fun add(e: E, position: Int) {
        list.add(position, e)
        notifyItemInserted(position)
    }

    /**
     * 根据对象添加数据（加在最后）
     */
    fun addLast(e: E) {
        add(e, list.size)
    }

    /**
     * 根据对象添加数据（加在第一个）
     */
    fun addFirst(e: E) {
        add(e, 0)
    }

    /**
     * 删除所有数据
     */
    @SuppressLint("NotifyDataSetChanged")
    fun clear() {
        list.clear()
        notifyDataSetChanged()
    }

    /**
     * item监听
     */
    fun setOnItemClickListener(onItemClickListener: (e: E, position: Int) -> Unit) {
        mOnItemClickListener = onItemClickListener
    }

    /**
     * item长按监听
     */
    fun setOnItemLongClickListener(onItemLongClickListener: (e: E, position: Int) -> Boolean) {
        mOnItemLongClickListener = onItemLongClickListener
    }

    /**
     * 绑定item Layout
     */
    protected abstract fun onBindLayout(): Int

    /**
     * 直接用
     *
     * @param view itemView
     * @return VH
     */
    protected abstract fun onCreateHolder(view: View): VH

    /**
     * 绑定数据
     *
     * @param holder   viewHolder
     * @param e        item对象
     * @param position 索引
     */
    protected abstract fun onBindData(holder: VH, e: E, position: Int)
}
