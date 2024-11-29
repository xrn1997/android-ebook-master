package com.xrn1997.common.mvvm.view

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.xrn1997.common.manager.ActivityManager
import com.xrn1997.common.mvvm.viewmodel.BaseViewModel

/**
 * 基于MVVM ViewBinding的Activity基类
 * @author xrn1997
 */
abstract class BaseMvvmActivity<V : ViewBinding, VM : BaseViewModel<*>> : BaseActivity<V>() {
    /**
     * MVVM中的VM,负责处理视图的操作功能,与M进行数据交互.
     */
    protected lateinit var mViewModel: VM

    override fun initContentView() {
        super.initContentView()
        initViewModel()
        initBaseViewObservable()
    }

    private fun initViewModel() {
        mViewModel = createViewModel()
        lifecycle.addObserver(mViewModel)
    }

    open fun createViewModel(): VM {
        return ViewModelProvider(this, onBindViewModelFactory())[onBindViewModel()]
    }

    /**
     * 绑定ViewModel,通常情况返回class即可
     */
    abstract fun onBindViewModel(): Class<VM>

    /**
     * 创建ViewModel实例的工厂
     */
    abstract fun onBindViewModelFactory(): ViewModelProvider.Factory

    protected open fun initBaseViewObservable() {
        mViewModel.mUIChangeLiveData.mShowLoadingViewEvent.observe(this) { show ->
            showLoadingView(show)
        }
        mViewModel.mUIChangeLiveData.mShowNoDataViewEvent.observe(this) { show ->
            showNoDataView(show)
        }
        mViewModel.mUIChangeLiveData.mShowNetWorkErrViewEvent.observe(this) { show ->
            showNetWorkErrView(show)
        }
        mViewModel.mUIChangeLiveData.mStartActivityEvent.observe(this) { params ->
            val clz = params[BaseViewModel.Companion.ParameterField.CLASS] as Class<*>?
            val bundle: Bundle? = params[BaseViewModel.Companion.ParameterField.BUNDLE] as Bundle?
            startActivity(clz, bundle)
        }
        mViewModel.mUIChangeLiveData.mFinishActivityEvent.observe(this) {
            ActivityManager.finishActivity(this@BaseMvvmActivity)
        }
        mViewModel.mUIChangeLiveData.mOnBackPressedEvent.observe(this) {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}