package com.ebook.book.mvvm.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.ebook.api.dto.RespDTO;
import com.ebook.api.entity.Comment;
import com.ebook.api.entity.User;
import com.ebook.book.mvvm.model.BookCommentsModel;
import com.ebook.common.event.KeyCode;
import com.ebook.common.util.DateUtil;
import com.xrn1997.common.event.SingleLiveEvent;
import com.xrn1997.common.http.ExceptionHandler;
import com.xrn1997.common.mvvm.viewmodel.BaseRefreshViewModel;

import java.util.List;

import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;


public class BookCommentsViewModel extends BaseRefreshViewModel<Comment, BookCommentsModel> {
    private static final String TAG = BookCommentsViewModel.class.getSimpleName();
    public final ObservableField<String> comments = new ObservableField<>();
    public Comment comment = new Comment();
    private SingleLiveEvent<Void> mVoidSingleLiveEvent;

    public BookCommentsViewModel(@NonNull Application application, BookCommentsModel model) {
        super(application, model);
    }

    @Override
    public void refreshData() {
        mModel.getChapterComments(comment.getChapterUrl()).subscribe(new Observer<>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(RespDTO<List<Comment>> listRespDTO) {
                if (listRespDTO.code == ExceptionHandler.AppError.SUCCESS) {
                    List<Comment> comments = listRespDTO.data;
                    comments.sort((x, y) -> DateUtil.parseTime(y.getAddTime(), DateUtil.FormatType.yyyyMMddHHmm).compareTo(DateUtil.parseTime(x.getAddTime(), DateUtil.FormatType.yyyyMMddHHmm)));
                    mList.clear();
                    if (!comments.isEmpty()) {
                        mList.addAll(comments);
                    }
                    postStopRefreshEvent(true);
                } else {
                    Log.e(TAG, "error: " + listRespDTO.error);
                    postStopRefreshEvent(false);
                }

            }

            @Override
            public void onError(Throwable e) {
                postStopRefreshEvent(false);
            }

            @Override
            public void onComplete() {

            }
        });
    }

    @Override
    public boolean enableLoadMore() {
        return false;
    }

    @Override
    public void loadMore() {

    }

    /**
     * 添加评论
     */
    public void addComment() {
        if (!StringUtils.isEmpty(comments.get())) {
            User user = new User();
            user.setId(SPUtils.getInstance().getLong(KeyCode.Login.SP_USER_ID));
            comment.setUser(user);
            comment.setComment(comments.get());
            mModel.addComment(comment).subscribe(new Observer<>() {
                @Override
                public void onSubscribe(Disposable d) {

                }

                @Override
                public void onNext(RespDTO<Comment> commentRespDTO) {
                    if (commentRespDTO.code == ExceptionHandler.AppError.SUCCESS) {
                        getmVoidSingleLiveEvent().call();
                        refreshData();
                    } else {
                        Log.e(TAG, "error: " + commentRespDTO.error);
                    }
                }

                @Override
                public void onError(Throwable e) {

                }

                @Override
                public void onComplete() {

                }
            });
        } else {
            ToastUtils.showShort("不能为空哦！");
        }

    }

    public void deleteComment(Long id) {
        mModel.deleteComment(id).subscribe(new Observer<>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(RespDTO<Integer> integerRespDTO) {
                if (integerRespDTO.code == ExceptionHandler.AppError.SUCCESS) {
                    ToastUtils.showShort("删除成功！");
                    refreshData();
                } else {
                    Log.e(TAG, "error: " + integerRespDTO.error);
                }
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });

    }

    public SingleLiveEvent<Void> getmVoidSingleLiveEvent() {
        return mVoidSingleLiveEvent = createLiveData(mVoidSingleLiveEvent);
    }
}
