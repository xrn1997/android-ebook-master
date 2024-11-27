package com.ebook.book.mvvm.model

import android.app.Application
import com.ebook.api.RetrofitManager
import com.ebook.api.dto.RespDTO
import com.ebook.api.entity.Comment
import com.ebook.api.service.CommentService
import com.xrn1997.common.http.RxJavaAdapter.exceptionTransformer
import com.xrn1997.common.http.RxJavaAdapter.schedulersTransformer
import com.xrn1997.common.mvvm.model.BaseModel
import io.reactivex.rxjava3.core.Observable

class BookCommentsModel(application: Application) : BaseModel(application) {
    private val commentService: CommentService = RetrofitManager.getInstance().commentService

    /**
     * 添加评论
     */
    fun addComment(comment: Comment?): Observable<RespDTO<Comment>> {
        return commentService.addComment(RetrofitManager.getInstance().TOKEN, comment)
            .compose(schedulersTransformer())
            .compose(exceptionTransformer())
    }

    /**
     * 获得章节评论
     */
    fun getChapterComments(chapterUrl: String?): Observable<RespDTO<List<Comment>>> {
        return commentService.getChapterComments(RetrofitManager.getInstance().TOKEN, chapterUrl)
            .compose(schedulersTransformer())
            .compose(exceptionTransformer())
    }

    /**
     * 删除评论
     */
    fun deleteComment(id: Long?): Observable<RespDTO<Int>> {
        return commentService.deleteComment(RetrofitManager.getInstance().TOKEN, id)
            .compose(schedulersTransformer())
            .compose(exceptionTransformer())
    }
}
