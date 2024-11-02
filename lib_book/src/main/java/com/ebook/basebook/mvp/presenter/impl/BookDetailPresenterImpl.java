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
import com.ebook.common.BaseApplication;
import com.ebook.common.event.RxBusTag;
import com.ebook.db.ObjectBoxManager;
import com.ebook.db.entity.BookContent;
import com.ebook.db.entity.BookInfo;
import com.ebook.db.entity.BookShelf;
import com.ebook.db.entity.BookShelf_;
import com.ebook.db.entity.ChapterList;
import com.ebook.db.entity.SearchBook;
import com.ebook.db.entity.WebChapter;
import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.trello.rxlifecycle4.android.ActivityEvent;
import com.xrn1997.common.event.SimpleObserver;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import io.objectbox.Box;
import io.objectbox.query.Query;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;


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
                    try (Query<BookShelf> query = ObjectBoxManager.INSTANCE.getBookShelfBox().query().build()) {
                        var temp = query.find();
                        e.onNext(temp);
                    } catch (Exception ex) {
                        e.onError(ex);
                    }
                    e.onComplete();
                }).flatMap((Function<List<BookShelf>, ObservableSource<BookShelf>>) bookShelves -> {
                    synchronized (bookShelfList) {
                        bookShelfList.clear();
                        bookShelfList.addAll(bookShelves); // 确保线程安全
                    }
                    final BookShelf bookShelfResult = new BookShelf();
                    bookShelfResult.noteUrl = searchBook.noteUrl;
                    bookShelfResult.finalDate = System.currentTimeMillis();
                    bookShelfResult.durChapter = 0;
                    bookShelfResult.durChapterPage = 0;
                    bookShelfResult.setTag(searchBook.tag);
                    return WebBookModelImpl.getInstance().getBookInfo(bookShelfResult)
                            .onErrorResumeNext(Observable::error);
                }).map(bookShelf -> {
                    bookShelfList.stream()
                            .filter(shelf -> Objects.equals(shelf.noteUrl, bookShelf.noteUrl))
                            .findFirst()
                            .ifPresent(shelf -> {
                                inBookShelf = true;
                                bookShelf.durChapter = shelf.durChapter;
                                bookShelf.durChapterPage = shelf.durChapterPage;
                            });
                    return bookShelf;
                }).subscribeOn(Schedulers.io())
                .compose(((BaseActivity<?>) mView.getContext()).bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<>() {
                    @Override
                    public void onNext(@NotNull BookShelf value) {
                        WebBookModelImpl.getInstance().getChapterList(value)
                                .subscribe(new SimpleObserver<>() {
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
                        try (var query = ObjectBoxManager.INSTANCE.getBookShelfBox().query(BookShelf_.noteUrl.equal(mBookShelf.noteUrl)).build()) {
                            var temp = query.findFirst();
                            if (temp != null) {
                                mBookShelf.setId(temp.getId());
                            }
                            //网络数据获取成功  存入BookShelf表数据库
                            long id = ObjectBoxManager.INSTANCE.getBookShelfBox().put(mBookShelf);
                            Log.e(TAG, "addToBookShelf: " + id);
                            e.onNext(true);
                            e.onComplete();
                        } catch (Exception ex) {
                            e.onError(ex);
                        }
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
                        Box<BookShelf> bookShelfBox = ObjectBoxManager.INSTANCE.getBookShelfBox();
                        Box<BookInfo> bookInfoBox = ObjectBoxManager.INSTANCE.getBookInfoBox();
                        Box<ChapterList> chapterListBox = ObjectBoxManager.INSTANCE.getChapterListBox();
                        Box<BookContent> bookContentBox = ObjectBoxManager.INSTANCE.getBookContentBox();
                        try (var query = bookShelfBox.query(BookShelf_.noteUrl.equal(mBookShelf.noteUrl)).build()) {
                            var bookShelf = query.findFirst();
                            if (bookShelf != null) {
                                var bookInfo = bookShelf.bookInfo.getTarget();
                                if (bookInfo != null) {
                                    var chapterList = bookInfo.chapterList;
                                    if (chapterList != null) {
                                        for (var chapter : chapterList) {
                                            var bookContent = chapter.bookContent.getTarget();
                                            if (bookContent != null && bookContent.getId() != 0L) {
                                                bookContentBox.remove(bookContent);
                                            }
                                        }
                                        chapterListBox.remove(chapterList);
                                    }
                                    bookInfoBox.remove(bookInfo);
                                }
                                bookShelfBox.remove(bookShelf);
                            }
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
        bookShelfList.add(value);
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
}
