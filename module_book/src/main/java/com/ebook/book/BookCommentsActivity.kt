package com.ebook.book

import android.widget.EditText
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.SPUtils
import com.ebook.api.entity.Comment
import com.ebook.book.adapter.BookCommentsAdapter
import com.ebook.book.databinding.ActivityBookCommentsBinding
import com.ebook.book.mvvm.factory.BookViewModelFactory
import com.ebook.book.mvvm.viewmodel.BookCommentsViewModel
import com.ebook.common.event.KeyCode
import com.ebook.common.util.SoftInputUtil.hideSoftInput
import com.ebook.common.view.DeleteDialog.Companion.newInstance
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.therouter.router.Route
import com.xrn1997.common.mvvm.view.BaseMvvmRefreshActivity
import com.xrn1997.common.util.ObservableListUtil.getListChangedCallback

@Route(path = KeyCode.Book.COMMENT_PATH)
class BookCommentsActivity :
    BaseMvvmRefreshActivity<ActivityBookCommentsBinding, BookCommentsViewModel>() {
    private lateinit var editText: EditText

    override fun getRefreshLayout(): RefreshLayout {
        return binding.refviewBookCommentsList
    }

    override fun onBindViewModel(): Class<BookCommentsViewModel> {
        return BookCommentsViewModel::class.java
    }

    override fun onBindViewModelFactory(): ViewModelProvider.Factory {
        return BookViewModelFactory
    }

    override fun initViewObservable() {
        mViewModel.getMVoidSingleLiveEvent().observe(this) {
            mViewModel.comments.set("")
            hideSoftInput(this@BookCommentsActivity, editText)
        }
    }

    override var toolBarTitle: String
        get() = "本章评论"
        set(toolBarTitle) {
            super.toolBarTitle = toolBarTitle
        }

    override fun initView() {
        editText = binding.textInput
        val mBookCommentsAdapter = BookCommentsAdapter(this, mViewModel.mList)
        mViewModel.mList.addOnListChangedCallback(getListChangedCallback(mBookCommentsAdapter))
        binding.viewBookComments.adapter = mBookCommentsAdapter
        mBookCommentsAdapter.setOnItemLongClickListener { comment: Comment, _: Int ->
            val username = SPUtils.getInstance().getString(KeyCode.Login.SP_USERNAME)
            if (comment.user.username == username) {
                val deleteDialog = newInstance()
                deleteDialog.setOnClickListener {
                    mViewModel.deleteComment(comment.id)
                }
                deleteDialog.show(supportFragmentManager, "deleteDialog")
            }
            true
        }
    }


    override fun initData() {
        val bundle = this.intent.extras
        if (bundle != null && !bundle.isEmpty) {
            val comment = Comment()
            comment.bookName = bundle.getString("bookName")
            comment.chapterName = bundle.getString("chapterName")
            comment.chapterUrl = bundle.getString("chapterUrl")
            mViewModel.comment = comment
            mViewModel.refreshData()
        }
    }

    override fun onBindVariableId(): Int {
        return BR.viewModel
    }

    override fun onBindLayout(): Int {
        return R.layout.activity_book_comments
    }
}
