package com.ebook.me.mvvm.model

import android.app.Application
import com.blankj.utilcode.util.SPUtils
import com.ebook.api.RetrofitManager
import com.ebook.api.dto.RespDTO
import com.ebook.api.entity.Comment
import com.ebook.api.service.CommentService
import com.ebook.common.event.KeyCode
import com.xrn1997.common.http.RxJavaAdapter.exceptionTransformer
import com.xrn1997.common.http.RxJavaAdapter.schedulersTransformer
import com.xrn1997.common.mvvm.model.BaseModel
import io.reactivex.rxjava3.core.Observable

class CommentModel(application: Application) : BaseModel(application) {
    private val commentService: CommentService = RetrofitManager.getInstance().commentService


    /**
     * 删除评论
     */
    fun deleteComment(id: Long): Observable<RespDTO<Int>> {
        return commentService.deleteComment(RetrofitManager.getInstance().TOKEN, id)
            .compose(schedulersTransformer())
            .compose(exceptionTransformer())
    }

    /**
     * 获得用户评论
     */
    fun getUserComments(): Observable<RespDTO<List<Comment>>> {
        val username = SPUtils.getInstance().getString(KeyCode.Login.SP_USERNAME)
        return commentService.getUserComments(RetrofitManager.getInstance().TOKEN, username)
            .compose(schedulersTransformer())
            .compose(exceptionTransformer())
    }
}
