package com.xrn1997.common.mvvm.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.viewbinding.ViewBinding
import com.blankj.utilcode.util.NetworkUtils
import com.trello.rxlifecycle4.components.support.RxAppCompatActivity
import com.trello.rxlifecycle4.components.support.RxFragment
import com.xrn1997.common.R
import com.xrn1997.common.databinding.FragmentRootBinding
import com.xrn1997.common.databinding.StubLoadingBinding
import com.xrn1997.common.databinding.StubNetErrorBinding
import com.xrn1997.common.databinding.StubNoDataBinding
import com.xrn1997.common.event.BaseFragmentEvent
import com.xrn1997.common.mvvm.IBaseView
import com.xrn1997.common.view.LoadingView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


/**
 * 基于ViewBinding的Fragment基类
 * @author xrn1997
 */
@Suppress("unused")
abstract class BaseFragment<V : ViewBinding> : RxFragment(), IBaseView {
    protected lateinit var mActivity: RxAppCompatActivity
    private lateinit var mViewStubContent: RelativeLayout
    protected val mNetErrorView by lazy {
        val view = mViewStubError.inflate()
        val netErrorView = StubNetErrorBinding.bind(view).root
        netErrorView.setRefreshBtnClickListener {
            NetworkUtils.isAvailableAsync {
                if (it) {
                    netErrorView.show(false)
                    initData()
                }
            }
        }
        netErrorView
    }
    protected val mNoDataView by lazy {
        val view = mViewStubNoData.inflate()
        StubNoDataBinding.bind(view).root
    }
    protected val mLoadingView: LoadingView by lazy {
        val view: View = mViewStubLoading.inflate()
        StubLoadingBinding.bind(view).root
    }
    protected var mToolbar: Toolbar? = null

    private lateinit var mViewStubToolbar: ViewStub
    private lateinit var mViewStubLoading: ViewStub
    private lateinit var mViewStubNoData: ViewStub
    private lateinit var mViewStubError: ViewStub

    private lateinit var _binding: V

    /**
     * 此属性仅在onCreateView及之后的生命周期有效.
     * 请注意不要随便覆写.
     */
    protected open val binding get() = _binding

    /**
     * 默认toolBarTitle
     */
    open var toolBarTitle: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivity = activity as RxAppCompatActivity
        EventBus.getDefault().register(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val mBinding = FragmentRootBinding.inflate(inflater, container, false)
        initCommonView(mBinding)
        return mBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData()
    }

    open fun initCommonView(binding: FragmentRootBinding) {
        mViewStubToolbar = binding.vsToolbar
        mViewStubContent = binding.rlContent
        mViewStubLoading = binding.vsLoading
        mViewStubError = binding.vsError
        mViewStubNoData = binding.vsNoData
        binding.parentLayout.fitsSystemWindows = enableFitsSystemWindows()
        if (enableToolbar()) {
            mViewStubToolbar.layoutResource = onBindToolbarLayout()
            val view = mViewStubToolbar.inflate()
            initToolbar(view)
        }
        initContentView(mViewStubContent)
        initView()
    }

    /**
     * 给根布局设置fitsSystemWindows,默认false
     */
    open fun enableFitsSystemWindows(): Boolean {
        return false
    }

    /**
     * 绑定toolbar layout
     * @return Int
     */
    open fun onBindToolbarLayout(): Int {
        return R.layout.view_toolbar
    }

    open fun initContentView(root: ViewGroup) {
        _binding = onBindViewBinding(LayoutInflater.from(mActivity), root, true)
    }

    /**
     * 初始化toolbar,可重写,如果enableToolbar()返回false,则该
     * 项不起作用.
     * @see BaseActivity.enableToolbar
     */
    open fun initToolbar(view: View) {
        val mToolBarTitle: TextView? = view.findViewById(R.id.toolbar_title)
        mToolbar = view.findViewById(R.id.toolbar_root)
        if (mToolbar != null) {
            mActivity.setSupportActionBar(mToolbar)
            mActivity.supportActionBar?.setDisplayShowTitleEnabled(false)
            mToolbar?.setNavigationOnClickListener { mActivity.onBackPressedDispatcher.onBackPressed() }
        }
        if (mToolBarTitle != null) {
            mToolBarTitle.text = toolBarTitle
        }
    }

    /**
     * 是否启用toolbar,默认false
     * @return Boolean
     */
    open fun enableToolbar(): Boolean {
        return false
    }

    abstract override fun initView()

    abstract override fun initData()

    /**
     * 这个方法返回需要绑定的ViewBinding
     * @param inflater LayoutInflater
     * @param parent ViewGroup? 整合到哪
     * @param attachToParent Boolean  是否整合到Parent上
     * @return V
     */
    abstract fun onBindViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        attachToParent: Boolean
    ): V


    override fun finishActivity() {
        mActivity.finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun showLoadingView(show: Boolean) {
        mLoadingView.show(show)
    }


    override fun showNoDataView(show: Boolean, resId: Int?) {
        resId?.let {
            mNoDataView.setNoDataView(it)
        }
        mNoDataView.show(show)
    }

    override fun showNetWorkErrView(show: Boolean, resId: Int?) {
        resId?.let {
            mNetErrorView.setNetErrorView(it)
        }
        mNetErrorView.show(show)
    }

    protected fun startActivity(clz: Class<*>?, bundle: Bundle?) {
        val intent = Intent(mActivity, clz)
        if (bundle != null) {
            intent.putExtras(bundle)
        }
        startActivity(intent)
    }

    /**
     * 如有必要,可以用EventBus传值调用BaseActivity中的方法
     * @param event BaseFragmentEvent<T>?
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    open fun <T> onEvent(event: BaseFragmentEvent<T>?) {
        Log.d(TAG, "onEvent: $event")
    }

    companion object {
        private val TAG = BaseActivity::class.java.simpleName
    }
}