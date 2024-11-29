package com.xrn1997.common.mvvm.view

import android.os.Bundle
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.xrn1997.common.manager.ActivityManager
import com.xrn1997.common.mvvm.viewmodel.BaseViewModel


/**
 * 基于MVVM ViewBinding的Fragment基类
 * @author xrn1997
 */
abstract class BaseMvvmFragment<V : ViewBinding, VM : BaseViewModel<*>> : BaseFragment<V>() {

    /**
     * MVVM中的VM,负责处理视图的操作功能,与M进行数据交互.
     * 在onCreateView之前的生命周期中不得使用.
     */
    protected lateinit var mViewModel: VM

    override fun initContentView(root: ViewGroup) {
        super.initContentView(root)
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

    abstract fun onBindViewModel(): Class<VM>
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
            ActivityManager.finishActivity(mActivity)
        }
        mViewModel.mUIChangeLiveData.mOnBackPressedEvent.observe(this) {
            mActivity.onBackPressedDispatcher.onBackPressed()
        }
    }
}