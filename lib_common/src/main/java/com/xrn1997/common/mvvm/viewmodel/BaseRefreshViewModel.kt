package com.xrn1997.common.mvvm.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.xrn1997.common.event.SingleLiveEvent
import com.xrn1997.common.mvvm.model.BaseModel

/**
 * 可刷新的ViewModel基类
 * @author xrn1997
 */
@Suppress("unused")
abstract class BaseRefreshViewModel<T, M : BaseModel>(
    application: Application,
    model: M
) : BaseViewModel<M>(application, model) {
    @JvmField
    val mList = MutableLiveData<List<T>>()

    protected var defaultUIChangeRefreshLiveData: UIChangeRefreshLiveData? = null
    val mUIChangeRefreshLiveData: UIChangeRefreshLiveData
        get() {
            if (defaultUIChangeRefreshLiveData == null) {
                defaultUIChangeRefreshLiveData = UIChangeRefreshLiveData()
            }
            return defaultUIChangeRefreshLiveData as UIChangeRefreshLiveData
        }

    inner class UIChangeRefreshLiveData {
        private var stopRefreshLiveEvent: SingleLiveEvent<Boolean>? = null
        private var autoRefreshLiveEvent: SingleLiveEvent<Unit>? = null
        private var stopLoadMoreLiveEvent: SingleLiveEvent<Boolean>? = null
        val mStopRefreshLiveEvent
            get() = createLiveData(stopRefreshLiveEvent).also { stopRefreshLiveEvent = it }
        val mAutoRefreshLiveEvent
            get() = createLiveData(autoRefreshLiveEvent).also { autoRefreshLiveEvent = it }
        val mStopLoadMoreLiveEvent
            get() = createLiveData(stopLoadMoreLiveEvent).also { stopLoadMoreLiveEvent = it }
    }

    /**
     *
     * 停止刷新
     * @param boolean Boolean 数据是否成功刷新 （会影响到上次更新时间的改变）
     */
    fun postStopRefreshEvent(boolean: Boolean) {
        mUIChangeRefreshLiveData.mStopRefreshLiveEvent.postValue(boolean)
    }

    /**
     * 自动刷新
     */
    fun postAutoRefreshEvent() {
        mUIChangeRefreshLiveData.mAutoRefreshLiveEvent.call()
    }

    /**
     * 停止加载
     * @param boolean Boolean 数据是否成功
     */
    fun postStopLoadMoreEvent(boolean: Boolean) {
        mUIChangeRefreshLiveData.mStopLoadMoreLiveEvent.postValue(boolean)
    }

    /**
     * 刷新数据
     */
    abstract fun refreshData()

    /**
     * 加载更多
     */
    open fun loadMore() {}
}