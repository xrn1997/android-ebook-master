package com.ebook.me.mvvm.viewmodel

import android.app.Application
import android.util.Log
import com.ebook.api.dto.RespDTO
import com.ebook.api.entity.Comment
import com.ebook.common.util.DateUtil
import com.ebook.me.mvvm.model.CommentModel
import com.xrn1997.common.event.SimpleObserver
import com.xrn1997.common.http.ExceptionHandler
import com.xrn1997.common.mvvm.viewmodel.BaseRefreshViewModel
import com.xrn1997.common.util.ToastUtil.showShort
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable

class CommentViewModel(application: Application, model: CommentModel) :
    BaseRefreshViewModel<Comment, CommentModel>(application, model) {
    override fun refreshData() {
        mModel.getUserComments().subscribe(object : Observer<RespDTO<List<Comment>>> {
            override fun onSubscribe(d: Disposable) {
            }

            override fun onNext(listRespDTO: RespDTO<List<Comment>>) {
                if (listRespDTO.code == ExceptionHandler.AppError.SUCCESS) {
                    listRespDTO.data?.let { data ->
                        val sortedComments = data.sortedByDescending {
                            DateUtil.parseTime(it.addTime, DateUtil.FormatType.yyyyMMddHHmm)
                        }
                        mList.value = sortedComments
                    }
                } else {
                    Log.e(TAG, "error: ${listRespDTO.error}")
                }
                postStopRefreshEvent(true)
            }

            override fun onError(e: Throwable) {
                postStopRefreshEvent(false)
            }

            override fun onComplete() {
            }
        })
    }

    override fun loadMore() {
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

    companion object {
        private val TAG: String = CommentViewModel::class.java.simpleName
    }
}
