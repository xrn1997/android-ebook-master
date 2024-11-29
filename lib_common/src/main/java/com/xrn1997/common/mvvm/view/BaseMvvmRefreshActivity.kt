package com.xrn1997.common.mvvm.view

import androidx.viewbinding.ViewBinding
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.xrn1997.common.mvvm.viewmodel.BaseRefreshViewModel


/**
 * 基于MVVM ViewBinding的可刷新的Activity基类
 * @author xrn1997
 */
@Suppress("unused")
abstract class BaseMvvmRefreshActivity<V : ViewBinding, VM : BaseRefreshViewModel<*, *>> :
    BaseMvvmActivity<V, VM>() {
    protected lateinit var mRefreshLayout: RefreshLayout

    @JvmField
    protected var mOnItemClickListener: ((e: V, position: Int) -> Unit)? = null

    @JvmField
    protected var mOnItemLongClickListener: ((e: V, position: Int) -> Boolean)? = null

    override fun initContentView() {
        super.initContentView()
        initRefreshView()
    }

    override fun initBaseViewObservable() {
        super.initBaseViewObservable()
        initBaseViewRefreshObservable()
    }

    private fun initBaseViewRefreshObservable() {
        mViewModel.mUIChangeRefreshLiveData.mAutoRefreshLiveEvent
            .observe(this) { autoLoadData() }
        mViewModel.mUIChangeRefreshLiveData.mStopRefreshLiveEvent
            .observe(this) { success -> stopRefresh(success) }
        mViewModel.mUIChangeRefreshLiveData.mStopLoadMoreLiveEvent
            .observe(this) { success -> stopLoadMore(success) }
    }

    abstract fun getRefreshLayout(): RefreshLayout

    /**
     * 初始化刷新控件
     */
    open fun initRefreshView() {
        mRefreshLayout = getRefreshLayout()
        mRefreshLayout.setOnRefreshListener { onRefresh() }
        mRefreshLayout.setOnLoadMoreListener { onLoadMore() }
        mRefreshLayout.setEnableRefresh(enableRefresh())
        mRefreshLayout.setEnableLoadMore(enableLoadMore())
    }

    /**
     * 下拉刷新事件逻辑
     */
    open fun onRefresh() {
        mViewModel.refreshData()
    }

    /**
     * 上拉加载事件逻辑
     */
    open fun onLoadMore() {
        mViewModel.loadMore()
    }

    /**
     * 是否启用上拉加载
     * @return Boolean 默认false
     */
    open fun enableLoadMore(): Boolean {
        return false
    }

    /**
     * 是否启用下拉刷新
     * @return Boolean 默认true
     */
    open fun enableRefresh(): Boolean {
        return true
    }

    /**
     * 完成加载
     * @param success Boolean 数据是否成功刷新 （会影响到上次更新时间的改变）
     * @see RefreshLayout.finishRefresh
     */
    open fun stopRefresh(success: Boolean) {
        mRefreshLayout.finishRefresh(success)
    }

    /**
     * 完成加载
     * @param success Boolean 数据是否成功
     * @see RefreshLayout.finishLoadMore
     */
    open fun stopLoadMore(success: Boolean) {
        mRefreshLayout.finishLoadMore(success)
    }

    /**
     * 显示刷新动画并且触发刷新事件
     * @see RefreshLayout.autoRefresh
     */
    open fun autoLoadData() {
        mRefreshLayout.autoRefresh()
    }

    open fun setOnItemClickListener(onItemClickListener: (e: V, position: Int) -> Unit) {
        mOnItemClickListener = onItemClickListener
    }

    open fun setOnItemLongClickListener(onItemLongClickListener: (e: V, position: Int) -> Boolean) {
        mOnItemLongClickListener = onItemLongClickListener
    }
}