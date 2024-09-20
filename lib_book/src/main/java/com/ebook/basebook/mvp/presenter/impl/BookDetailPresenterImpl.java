package com.ebook.basebook.mvp.presenter.impl;

import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.ebook.basebook.base.IView;
import com.ebook.basebook.base.activity.BaseActivity;
import com.ebook.basebook.base.impl.BasePresenterImpl;
import com.ebook.basebook.base.manager.BitIntentDataManager;
import com.ebook.basebook.mvp.model.impl.WebBookModelImpl;
import com.ebook.basebook.mvp.presenter.IBookDetailPresenter;
import com.ebook.basebook.mvp.view.IBookDetailView;
import com.ebook.basebook.observer.SimpleObserver;
import com.ebook.common.BaseApplication;
import com.ebook.common.event.RxBusTag;
import com.ebook.db.ObjectBoxManager;
import com.ebook.db.entity.BookInfo;
import com.ebook.db.entity.BookInfo_;
import com.ebook.db.entity.BookShelf;
import com.ebook.db.entity.BookShelf_;
import com.ebook.db.entity.SearchBook;
import com.ebook.db.entity.WebChapter;
import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.trello.rxlifecycle3.android.ActivityEvent;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import io.objectbox.Box;
import io.objectbox.query.Query;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class BookDetailPresenterImpl extends BasePresenterImpl<IBookDetailView> implements IBookDetailPresenter {
    public final static int FROM_BOOKSHELF = 1;
    public final static int FROM_SEARCH = 2;

    private final static String TAG = "BookDetailPresenterImpl";
    private final int openFrom;
    private final List<BookShelf> bookShelfList = Collections.synchronizedList(new ArrayList<>());   //用来比对搜索的书籍是否已经添加进书架
    private SearchBook searchBook;
    private BookShelf mBookShelf;
    private Boolean inBookShelf;

    public BookDetailPresenterImpl(Intent intent) {
        openFrom = intent.getIntExtra("from", FROM_BOOKSHELF);
        if (openFrom == FROM_BOOKSHELF) {
            String key = intent.getStringExtra("data_key");
            mBookShelf = (BookShelf) BitIntentDataManager.getInstance().getData(key);
            BitIntentDataManager.getInstance().cleanData(key);
            inBookShelf = true;
        } else {
            searchBook = intent.getParcelableExtra("data");
            inBookShelf = searchBook.getAdd();
        }
    }

    public Boolean getInBookShelf() {
        return inBookShelf;
    }

    public void setInBookShelf(Boolean inBookShelf) {
        this.inBookShelf = inBookShelf;
    }

    public int getOpenFrom() {
        return openFrom;
    }

    public SearchBook getSearchBook() {
        return searchBook;
    }

    public BookShelf getBookShelf() {
        return mBookShelf;
    }

    @Override
    public void getBookShelfInfo() {
        Observable.create((ObservableOnSubscribe<List<BookShelf>>) e -> {
                    List<BookShelf> temp;
                    try (Query<BookShelf> query = ObjectBoxManager.INSTANCE.getBookShelfBox().query().build()) {
                        temp = query.find();
                        e.onNext(temp);
                    }
                    e.onComplete();
                }).flatMap((Function<List<BookShelf>, ObservableSource<BookShelf>>) bookShelf -> {
                    bookShelfList.addAll(bookShelf);

                    final BookShelf bookShelfResult = new BookShelf();
                    bookShelfResult.noteUrl=searchBook.noteUrl;
                    bookShelfResult.finalDate = System.currentTimeMillis();
                    bookShelfResult.durChapter = 0;
                    bookShelfResult.durChapterPage = 0;
                    bookShelfResult.setTag(searchBook.tag);
                    return WebBookModelImpl.getInstance().getBookInfo(bookShelfResult);
                }).map(bookShelf -> {
                    for (int i = 0; i < bookShelfList.size(); i++) {
                        if (Objects.equals(bookShelfList.get(i).noteUrl, bookShelf.noteUrl)) {
                            inBookShelf = true;
                            bookShelf.durChapter = bookShelfList.get(i).durChapter;
                            bookShelf.durChapterPage = bookShelfList.get(i).durChapterPage;
                            break;
                        }
                    }
                    return bookShelf;
                }).subscribeOn(Schedulers.io())
                .compose(((BaseActivity<?>) mView.getContext()).bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<>() {
                    @Override
                    public void onNext(@NotNull BookShelf value) {
                        WebBookModelImpl.getInstance().getChapterList(value).subscribe(new SimpleObserver<>() {
                            @Override
                            public void onNext(WebChapter<BookShelf> bookShelfWebChapter) {
                                mBookShelf = bookShelfWebChapter.data;
                                mView.updateView();
                            }

                            @Override
                            public void onError(Throwable e) {
                                mBookShelf = null;
                                Log.e("错误信息", "getChapterList onError: ", e);
                                mView.getBookShelfError();
                            }
                        });
                    }

                    @Override
                    public void onError(@NotNull Throwable e) {
                        mBookShelf = null;
                        Log.e("错误信息", "subscribe onError: ", e);
                        mView.getBookShelfError();
                    }
                });
    }

    @Override
    public void addToBookShelf() {
        if (mBookShelf != null) {
            Observable.create((ObservableOnSubscribe<Boolean>) e -> {
                        //todo insertOrReplaceInTx insertOrReplace
                        ObjectBoxManager.INSTANCE.getChapterListBox().put(mBookShelf.getBookInfo().getTarget().chapterlist);
                        ObjectBoxManager.INSTANCE.getBookInfoBox().put(mBookShelf.getBookInfo().getTarget());
                        //网络数据获取成功  存入BookShelf表数据库
                        ObjectBoxManager.INSTANCE.getBookShelfBox().put(mBookShelf);
                        e.onNext(true);
                        e.onComplete();
                    }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .compose(((BaseActivity<?>) mView.getContext()).bindUntilEvent(ActivityEvent.DESTROY))
                    .subscribe(new SimpleObserver<>() {
                        @Override
                        public void onNext(@NotNull Boolean value) {
                            if (value) {
                                RxBus.get().post(RxBusTag.HAD_ADD_BOOK, mBookShelf);
                            } else {
                                Toast.makeText(BaseApplication.context, "放入书架失败!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(@NotNull Throwable e) {
                            Log.e(TAG, "onError: ", e);
                            Toast.makeText(BaseApplication.context, "放入书架失败!", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    public void removeFromBookShelf() {
        if (mBookShelf != null) {
            Observable.create((ObservableOnSubscribe<Boolean>) e -> {
                        Box<BookShelf> bookShelfBox=ObjectBoxManager.INSTANCE.getBookShelfBox();
                        List<BookShelf> bookShelves=bookShelfBox.query(BookShelf_.noteUrl.equal(mBookShelf.noteUrl)).build().find();
                        bookShelfBox.remove(bookShelves);
                        Box<BookInfo> bookInfoBox=ObjectBoxManager.INSTANCE.getBookInfoBox();
                        List<BookInfo> bookInfos=bookInfoBox.query(BookInfo_.noteUrl.equal(mBookShelf.getBookInfo().getTarget().getNoteUrl())).build().find();
                        bookInfoBox.remove(bookInfos);

                        var chapterList = mBookShelf.getBookInfo().getTarget().chapterlist;
                        if (!chapterList.isEmpty()) {
                            for (int i = 0; i < chapterList.size(); i++) {
                                ObjectBoxManager.INSTANCE.getBookContentBox().remove(chapterList.get(i).bookContent.getTarget());
                            }
                            ObjectBoxManager.INSTANCE.getChapterListBox().remove(chapterList);
                        }
                        e.onNext(true);
                        e.onComplete();
                    }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .compose(((BaseActivity<?>) mView.getContext()).bindUntilEvent(ActivityEvent.DESTROY))
                    .subscribe(new SimpleObserver<>() {
                        @Override
                        public void onNext(Boolean value) {
                            if (value) {
                                RxBus.get().post(RxBusTag.HAD_REMOVE_BOOK, mBookShelf);
                            } else {
                                Toast.makeText(BaseApplication.context, "移出书架失败!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.e(TAG, "onError: ", e);
                            Toast.makeText(BaseApplication.context, "移出书架失败!", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    public void attachView(@NonNull IView iView) {
        super.attachView(iView);
        RxBus.get().register(this);
    }

    @Override
    public void detachView() {
        RxBus.get().unregister(this);
    }

    @Subscribe(thread = EventThread.MAIN_THREAD,
            tags = {
                    @Tag(RxBusTag.HAD_ADD_BOOK)
            }
    )
    public void hadAddBook(BookShelf value) {
        if ((null != mBookShelf && value.noteUrl.equals(mBookShelf.noteUrl)) || (null != searchBook && value.noteUrl.equals(searchBook.noteUrl))) {
            inBookShelf = true;
            if (null != searchBook) {
                searchBook.setAdd(true);
            }
            mView.updateView();
        }
    }

    @Subscribe(thread = EventThread.MAIN_THREAD,
            tags = {
                    @Tag(RxBusTag.HAD_REMOVE_BOOK)
            }
    )
    public void hadRemoveBook(BookShelf value) {
        for (int i = 0; i < bookShelfList.size(); i++) {
            if (bookShelfList.get(i).noteUrl.equals(value.noteUrl)) {
                bookShelfList.remove(i);
                break;
            }
        }
        if ((null != mBookShelf && value.noteUrl.equals(mBookShelf.noteUrl)) || (null != searchBook && value.noteUrl.equals(searchBook.noteUrl))) {
            inBookShelf = false;
            if (null != searchBook) {
                searchBook.setAdd(false);
            }
            mView.updateView();
        }
    }

    @Subscribe(thread = EventThread.MAIN_THREAD,
            tags = {
                    @Tag(RxBusTag.HAD_ADD_BOOK),
            }
    )
    public void hadBook(BookShelf value) {
        bookShelfList.add(value);
        if ((null != mBookShelf && value.noteUrl.equals(mBookShelf.noteUrl)) || (null != searchBook && value.noteUrl.equals(searchBook.noteUrl))) {
            inBookShelf = true;
            if (null != searchBook) {
                searchBook.setAdd(true);
            }
            mView.updateView();
        }
    }
}
