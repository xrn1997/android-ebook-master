package com.ebook.me

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ebook.api.entity.Comment
import com.ebook.common.event.KeyCode
import com.ebook.common.view.DeleteDialog.Companion.newInstance
import com.ebook.me.adapter.CommentListAdapter
import com.ebook.me.databinding.ActivityCommentBinding
import com.ebook.me.mvvm.factory.MeViewModelFactory
import com.ebook.me.mvvm.viewmodel.CommentViewModel
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.therouter.TheRouter.build
import com.therouter.router.Route
import com.xrn1997.common.mvvm.view.BaseMvvmRefreshActivity


@Route(path = KeyCode.Me.COMMENT_PATH, params = ["needLogin", "true"])
class MyCommentActivity : BaseMvvmRefreshActivity<ActivityCommentBinding, CommentViewModel>() {
    override fun initView() {
        val mCommentListAdapter = CommentListAdapter(this)
        mViewModel.mList.observe(this) {
            mCommentListAdapter.submitList(it)
        }
        binding.viewMyCommentList.layoutManager = LinearLayoutManager(this)
        binding.viewMyCommentList.adapter = mCommentListAdapter
        mCommentListAdapter.setOnItemClickListener { comment: Comment, _: Int ->
            val bundle = Bundle()
            bundle.putString("chapterUrl", comment.chapterUrl)
            bundle.putString("chapterName", comment.chapterName)
            bundle.putString("bookName", comment.bookName)
            build(KeyCode.Book.COMMENT_PATH)
                .with(bundle)
                .navigation(this@MyCommentActivity)
        }
        mCommentListAdapter.setOnItemLongClickListener { comment: Comment, _: Int ->
            val deleteDialog = newInstance()
            deleteDialog.setOnClickListener { mViewModel.deleteComment(comment.id) }
            deleteDialog.show(supportFragmentManager, "deleteDialog")
            true
        }
    }

    override fun enableToolbar(): Boolean {
        return true
    }
    override fun enableLoadMore(): Boolean {
        return false
    }

    override fun initData() {
        mRefreshLayout.autoRefresh()
    }
    override fun onBindViewModel(): Class<CommentViewModel> {
        return CommentViewModel::class.java
    }

    override fun onBindViewModelFactory(): ViewModelProvider.Factory {
        return MeViewModelFactory
    }

    override fun onBindViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        attachToParent: Boolean
    ): ActivityCommentBinding {
        return ActivityCommentBinding.inflate(inflater, parent, attachToParent)
    }

    override fun getRefreshLayout(): RefreshLayout {
        return binding.refreshCommentList
    }
}
