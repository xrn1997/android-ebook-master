package com.xrn1997.common.mvvm.view


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.viewbinding.ViewBinding
import com.blankj.utilcode.util.NetworkUtils
import com.trello.rxlifecycle4.components.support.RxAppCompatActivity
import com.xrn1997.common.R
import com.xrn1997.common.databinding.ActivityRootBinding
import com.xrn1997.common.databinding.StubLoadingBinding
import com.xrn1997.common.databinding.StubNetErrorBinding
import com.xrn1997.common.databinding.StubNoDataBinding
import com.xrn1997.common.event.BaseActivityEvent
import com.xrn1997.common.manager.ActivityManager
import com.xrn1997.common.mvvm.IBaseView
import com.xrn1997.common.view.LoadingView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


/**
 * 基于ViewBinding的Activity基类
 * @author xrn1997
 */
@Suppress("unused")
abstract class BaseActivity<V : ViewBinding> : RxAppCompatActivity(), IBaseView {
    private lateinit var mContentView: ViewGroup
    private lateinit var mViewStubContent: RelativeLayout
    private var mToolBarTitle: TextView? = null

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
     * 该binding仅用于取代findViewById
     */
    protected val binding get() = _binding

    /**
     * 默认toolBarTitle，并且设置完成后，通过setTitle是无法修改的。
     * @see setTitle
     */
    open var toolBarTitle: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val mBinding = ActivityRootBinding.inflate(layoutInflater)
        val rootView: View = mBinding.root
        super.setContentView(rootView)
        mContentView = findViewById(android.R.id.content)
        EventBus.getDefault().register(this)
        initCommonView(mBinding)
        initData()
        ActivityManager.addActivity(this)
    }

    private fun initCommonView(binding: ActivityRootBinding) {
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
        initContentView()
        initView()
    }

    /**
     * 是否让界面自动适配系统栏，即不延伸到系统栏，默认true
     * 注意，如果根布局延伸到了系统栏，请手动使用[ViewCompat.setOnApplyWindowInsetsListener]
     * 控制一些特定布局的偏移，防止可操作区域进入系统栏，产生交互冲突。
     */
    open fun enableFitsSystemWindows(): Boolean {
        return true
    }

    /**
     * 绑定toolbar layout
     * @return Int
     */
    open fun onBindToolbarLayout(): Int {
        return R.layout.view_toolbar
    }

    open fun initContentView() {
        _binding = onBindViewBinding(LayoutInflater.from(this), mViewStubContent, false)
        mViewStubContent.id = android.R.id.content
        mContentView.id = View.NO_ID
        mViewStubContent.removeAllViews()
        mViewStubContent.addView(_binding.root)
    }

    /**
     * 初始化toolbar，可重写，如果enableToolbar()返回false，则该
     * 项不起作用。
     * @see BaseActivity.enableToolbar
     */
    open fun initToolbar(view: View) {
        mToolBarTitle = view.findViewById(R.id.toolbar_title)
        mToolbar = view.findViewById(R.id.toolbar_root)
        if (mToolbar != null) {
            setSupportActionBar(mToolbar)
            supportActionBar?.setDisplayShowTitleEnabled(false)
            mToolbar?.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        }
    }

    /**
     * 是否启用toolbar，默认true
     * @return Boolean
     */
    open fun enableToolbar(): Boolean {
        return true
    }

    override fun onTitleChanged(title: CharSequence?, color: Int) {
        super.onTitleChanged(title, color)
        //android:label 默认title
        if (!title.isNullOrEmpty()) {
            mToolBarTitle?.text = title
        }
        // 自定义title
        if (toolBarTitle.isNotEmpty()) {
            mToolBarTitle?.text = toolBarTitle
        }
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
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        ActivityManager.removeActivity(this)
    }

    override fun getContext(): Context {
        return this
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
        val intent = Intent(this, clz)
        if (bundle != null) {
            intent.putExtras(bundle)
        }
        startActivity(intent)
    }

    /**
     * 如有必要，可以用EventBus传值调用BaseActivity中的方法
     * @param event BaseActivityEvent<T>?
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    open fun <T> onEvent(event: BaseActivityEvent<T>?) {
        Log.d(TAG, "onEvent: $event")
    }

    companion object {
        private val TAG = BaseActivity::class.java.simpleName
    }
}