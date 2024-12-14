package com.ebook.find

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ebook.basebook.mvp.presenter.impl.BookDetailPresenterImpl
import com.ebook.basebook.mvp.view.impl.BookDetailActivity
import com.ebook.basebook.utils.NetworkUtil
import com.ebook.common.event.RxBusTag
import com.ebook.db.entity.BookShelf
import com.ebook.db.entity.SearchBook
import com.ebook.find.adapter.SearchBookAdapter
import com.ebook.find.databinding.ActivityBookchoiceBinding
import com.ebook.find.mvvm.factory.FindViewModelFactory
import com.ebook.find.mvvm.viewmodel.ChoiceBookViewModel
import com.hwangjr.rxbus.RxBus
import com.hwangjr.rxbus.annotation.Subscribe
import com.hwangjr.rxbus.annotation.Tag
import com.hwangjr.rxbus.thread.EventThread
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.xrn1997.common.mvvm.view.BaseMvvmRefreshActivity

class ChoiceBookActivity :
    BaseMvvmRefreshActivity<ActivityBookchoiceBinding, ChoiceBookViewModel>() {
    private lateinit var tvTitle: TextView
    private lateinit var rfRvSearchBooks: RecyclerView
    private lateinit var searchBookAdapter: SearchBookAdapter

    override fun getRefreshLayout(): RefreshLayout {
        return binding.rfRvSmartRefreshLayout
    }

    override fun onBindViewModel(): Class<ChoiceBookViewModel> {
        return ChoiceBookViewModel::class.java
    }

    override fun onBindViewModelFactory(): ViewModelProvider.Factory {
        return FindViewModelFactory
    }

    override fun initView() {
        tvTitle = binding.tvTitle
        searchBookAdapter = SearchBookAdapter(this)
        rfRvSearchBooks = binding.rfRvSearchBooks
        rfRvSearchBooks.layoutManager = LinearLayoutManager(this)
        rfRvSearchBooks.adapter = searchBookAdapter
        binding.ivReturn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        searchBookAdapter.setItemClickListener(object : SearchBookAdapter.OnItemClickListener {
            override fun clickAddShelf(clickView: View, position: Int, searchBook: SearchBook) {
                mViewModel.addBookToShelf(searchBook)
            }

            override fun clickItem(animView: View, position: Int, searchBook: SearchBook) {
                val intent = Intent(this@ChoiceBookActivity, BookDetailActivity::class.java)
                intent.putExtra("from", BookDetailPresenterImpl.FROM_SEARCH)
                intent.putExtra("data", searchBook)
                startActivity(intent)
            }
        })
    }


    override fun initData() {
        val bundle = this.intent.extras
        if (bundle != null) {
            tvTitle.text = bundle.getString("title")
            mViewModel.url = bundle.getString("url")
        }
        mViewModel.addBookShelfFailedEvent.observe(this) { code ->
            Toast.makeText(this, NetworkUtil.getErrorTip(code), Toast.LENGTH_SHORT).show()
        }
        mViewModel.mList.observe(this) {
            searchBookAdapter.submitList(it)
        }
    }

    override fun enableToolbar(): Boolean {
        return false
    }

    override fun onBindViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        attachToParent: Boolean
    ): ActivityBookchoiceBinding {
        return ActivityBookchoiceBinding.inflate(inflater, parent, attachToParent)
    }

    override fun enableLoadMore(): Boolean {
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RxBus.get().register(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        RxBus.get().unregister(this)
    }

    // 处理添加书籍事件
    @Subscribe(thread = EventThread.MAIN_THREAD, tags = [Tag(RxBusTag.HAD_ADD_BOOK)])
    fun hadAddBook(bookShelf: BookShelf) {
        mViewModel.bookShelves.add(bookShelf)
        handleBookUpdate(bookShelf, true)  // true 表示添加书籍
    }

    // 处理移除书籍事件
    @Subscribe(thread = EventThread.MAIN_THREAD, tags = [Tag(RxBusTag.HAD_REMOVE_BOOK)])
    fun hadRemoveBook(bookShelf: BookShelf) {
        mViewModel.bookShelves.remove(bookShelf)
        handleBookUpdate(bookShelf, false)  // false 表示移除书籍
    }

    // 公共处理逻辑
    private fun handleBookUpdate(bookShelf: BookShelf, isAdd: Boolean) {
        val currentList = mViewModel.mList.value?.toMutableList() ?: mutableListOf()

        val index = currentList.indexOfFirst { it.noteUrl == bookShelf.noteUrl }
        if (index != -1) {
            // 根据 isAdd 判断是添加还是移除书籍
            val updatedBook = currentList[index].copy(add = isAdd)
            currentList[index] = updatedBook
        }
        searchBookAdapter.submitList(currentList)
    }
}