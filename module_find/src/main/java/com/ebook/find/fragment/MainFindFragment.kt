package com.ebook.find.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ebook.find.ChoiceBookActivity
import com.ebook.find.SearchActivity
import com.ebook.find.adapter.BookTypeShowAdapter
import com.ebook.find.adapter.LibraryBookListAdapter
import com.ebook.find.databinding.FragmentFindMainBinding
import com.ebook.find.entity.BookType
import com.ebook.find.mvvm.factory.FindViewModelFactory
import com.ebook.find.mvvm.viewmodel.LibraryViewModel
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.xrn1997.common.mvvm.view.BaseMvvmRefreshFragment
import com.xrn1997.common.util.getThemeColor

class MainFindFragment :
    BaseMvvmRefreshFragment<FragmentFindMainBinding, LibraryViewModel>() {
    override fun initView() {
        val mBookTypeShowAdapter = BookTypeShowAdapter(mActivity)
        mBookTypeShowAdapter.submitList(mViewModel.bookTypeList)
        val mLibraryKindBookAdapter = LibraryBookListAdapter(mActivity)
        mViewModel.mList.observe(this) { mLibraryKindBookAdapter.submitList(it) }
        val myRecyclerviewManager = MyRecyclerviewManager(mActivity)
        myRecyclerviewManager.setScrollEnabled(false)
        binding.lkbvKindbooklist.layoutManager = myRecyclerviewManager
        binding.lkbvKindbooklist.adapter = mLibraryKindBookAdapter
        binding.kindLl.adapter = mBookTypeShowAdapter
        mBookTypeShowAdapter.setOnItemClickListener { bookType: BookType, _: Int ->
            val bundle = Bundle()
            bundle.putString("url", bookType.url)
            bundle.putString("title", bookType.bookType)
            startActivity(ChoiceBookActivity::class.java, bundle)
        }
        binding.flSearch.setOnClickListener {
            //点击搜索
            startActivity(Intent(activity, SearchActivity::class.java))
        }
        context?.let {
            //todo fragment的toolbar还存在问题。
            mToolbarView?.setBackgroundColor(it.getThemeColor(com.google.android.material.R.attr.colorSurface))
            mToolbarView?.setToolbarTitleColor(it.getThemeColor(com.google.android.material.R.attr.colorOnSurface))
        }
    }

    override fun initData() {
        mRefreshLayout.autoRefresh()
    }

    override fun getRefreshLayout(): RefreshLayout {
        return binding.refviewLibrary
    }

    override fun onBindViewModel(): Class<LibraryViewModel> {
        return LibraryViewModel::class.java
    }

    override fun onBindViewModelFactory(): ViewModelProvider.Factory {
        return FindViewModelFactory
    }

    override fun enableLoadMore(): Boolean {
        return false
    }

    override fun onBindViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        attachToParent: Boolean
    ): FragmentFindMainBinding {
        return FragmentFindMainBinding.inflate(inflater, parent, attachToParent)
    }

    override var toolBarTitle: String
        get() = "书城"
        set(toolBarTitle) {
            super.toolBarTitle = toolBarTitle
        }

    override fun enableToolbar(): Boolean {
        return true
    }

    //自定义的manager，用于禁用滚动条
    class MyRecyclerviewManager(context: Context?) : LinearLayoutManager(context) {
        private var isScrollEnabled = true

        init {
            this.orientation = VERTICAL
        }

        fun setScrollEnabled(flag: Boolean) {
            this.isScrollEnabled = flag
        }

        override fun canScrollVertically(): Boolean {
            return isScrollEnabled && super.canScrollVertically()
        }
    }


    companion object {
        val TAG: String = MainFindFragment::class.java.simpleName

        fun newInstance(): MainFindFragment {
            return MainFindFragment()
        }
    }
}
