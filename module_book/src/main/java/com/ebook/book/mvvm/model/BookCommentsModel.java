package com.ebook.book.mvvm.model;

import android.app.Application;

import com.ebook.api.RetrofitManager;
import com.ebook.api.dto.RespDTO;
import com.ebook.api.entity.Comment;
import com.ebook.api.service.CommentService;
import com.xrn1997.common.http.RxJavaAdapter;
import com.xrn1997.common.mvvm.model.BaseModel;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;


public class BookCommentsModel extends BaseModel {
    private final CommentService commentService;

    public BookCommentsModel(Application application) {
        super(application);
        commentService = RetrofitManager.getInstance().getCommentService();
    }

    /**
     * 添加评论
     */
    public Observable<RespDTO<Comment>> addComment(Comment comment) {
        return commentService.addComment(RetrofitManager.getInstance().TOKEN, comment)
                .compose(RxJavaAdapter.INSTANCE.schedulersTransformer())
                .compose(RxJavaAdapter.INSTANCE.exceptionTransformer());
    }

    /**
     * 获得章节评论
     */
    public Observable<RespDTO<List<Comment>>> getChapterComments(String chapterUrl) {
        return commentService.getChapterComments(RetrofitManager.getInstance().TOKEN, chapterUrl)
                .compose(RxJavaAdapter.INSTANCE.schedulersTransformer())
                .compose(RxJavaAdapter.INSTANCE.exceptionTransformer());
    }

    /**
     * 删除评论
     */
    public Observable<RespDTO<Integer>> deleteComment(Long id) {
        return commentService.deleteComment(RetrofitManager.getInstance().TOKEN, id)
                .compose(RxJavaAdapter.INSTANCE.schedulersTransformer())
                .compose(RxJavaAdapter.INSTANCE.exceptionTransformer());
    }
}
