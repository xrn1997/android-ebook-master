package com.ebook.book.mvvm.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.StringUtils
import com.ebook.api.dto.RespDTO
import com.ebook.api.entity.Comment
import com.ebook.api.entity.User
import com.ebook.book.mvvm.model.BookCommentsModel
import com.ebook.common.event.KeyCode
import com.ebook.common.util.DateUtil
import com.xrn1997.common.event.SimpleObserver
import com.xrn1997.common.event.SingleLiveEvent
import com.xrn1997.common.http.ExceptionHandler
import com.xrn1997.common.mvvm.viewmodel.BaseRefreshViewModel
import com.xrn1997.common.util.ToastUtil.showShort
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable

class BookCommentsViewModel(application: Application, model: BookCommentsModel) :
    BaseRefreshViewModel<Comment, BookCommentsModel>(application, model) {
    @JvmField
    val comments: MutableLiveData<String> = MutableLiveData()

    @JvmField
    var comment: Comment = Comment()
    private var mVoidSingleLiveEvent: SingleLiveEvent<Void>? = null

    override fun refreshData() {
        mModel.getChapterComments(comment.chapterUrl)
            .subscribe(object : Observer<RespDTO<List<Comment>>> {
                override fun onSubscribe(d: Disposable) {
                }

                override fun onNext(listRespDTO: RespDTO<List<Comment>>) {
                    if (listRespDTO.code == ExceptionHandler.AppError.SUCCESS) {
                        listRespDTO.data?.let { data ->
                            val sortedComments = data.sortedByDescending {
                                DateUtil.parseTime(it.addTime, DateUtil.FormatType.yyyyMMddHHmm)
                            }
                            mList.clear()
                            mList.addAll(sortedComments)
                        }
                        postStopRefreshEvent(true)
                    } else {
                        Log.e(TAG, "error: " + listRespDTO.error)
                        postStopRefreshEvent(false)
                    }
                }

                override fun onError(e: Throwable) {
                    postStopRefreshEvent(false)
                }

                override fun onComplete() {
                }
            })
    }

    override fun enableLoadMore(): Boolean {
        return false
    }

    override fun loadMore() {
    }

    /**
     * 添加评论
     */
    fun addComment() {
        if (!StringUtils.isEmpty(comments.get())) {
            val user = User()
            user.id = SPUtils.getInstance().getLong(KeyCode.Login.SP_USER_ID)
            comment.user = user
            comment.comment = comments.get()
            mModel.addComment(comment).subscribe(object : Observer<RespDTO<Comment>> {
                override fun onSubscribe(d: Disposable) {
                }

                override fun onNext(commentRespDTO: RespDTO<Comment>) {
                    if (commentRespDTO.code == ExceptionHandler.AppError.SUCCESS) {
                        getMVoidSingleLiveEvent().call()
                        refreshData()
                    } else {
                        Log.e(TAG, "error: " + commentRespDTO.error)
                    }
                }

                override fun onError(e: Throwable) {
                }

                override fun onComplete() {
                }
            })
        } else {
            showShort(getApplication<Application>().applicationContext, "不能为空哦！")
        }
    }

    fun deleteComment(id: Long) {
        mModel.deleteComment(id).subscribe(object : SimpleObserver<RespDTO<Int>>() {
            override fun onNext(integerRespDTO: RespDTO<Int>) {
                if (integerRespDTO.code == ExceptionHandler.AppError.SUCCESS) {
                    showShort(getApplication<Application>().applicationContext, "删除成功！")
                    refreshData()
                } else {
                    Log.e(TAG, "error: " + integerRespDTO.error)
                }
            }

            override fun onError(e: Throwable) {
            }
        })
    }

    fun getMVoidSingleLiveEvent(): SingleLiveEvent<Void> {
        return createLiveData(mVoidSingleLiveEvent).also { mVoidSingleLiveEvent = it }
    }

    companion object {
        private val TAG: String = BookCommentsViewModel::class.java.simpleName
    }
}
