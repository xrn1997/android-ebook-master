package com.ebook.find.mvp.presenter.impl;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.ebook.basebook.base.IView;
import com.ebook.basebook.base.activity.BaseActivity;
import com.ebook.basebook.base.impl.BasePresenterImpl;
import com.ebook.basebook.mvp.model.impl.WebBookModelImpl;
import com.ebook.basebook.observer.SimpleObserver;
import com.ebook.basebook.utils.NetworkUtil;
import com.ebook.common.event.RxBusTag;
import com.ebook.db.ObjectBoxManager;
import com.ebook.db.entity.BookShelf;
import com.ebook.db.entity.SearchBook;
import com.ebook.db.entity.WebChapter;
import com.ebook.find.mvp.presenter.IChoiceBookPresenter;
import com.ebook.find.mvp.view.IChoiceBookView;
import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.trello.rxlifecycle3.android.ActivityEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.objectbox.query.Query;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class ChoiceBookPresenterImpl extends BasePresenterImpl<IChoiceBookView> implements IChoiceBookPresenter {
    private static final String TAG = "ChoiceBookPresenterImpl";
    private final String url;
    private final String title;

    private int page = 1;
    private long startThisSearchTime;
    private final List<BookShelf> bookShelves = new ArrayList<>();   //用来比对搜索的书籍是否已经添加进书架

    public ChoiceBookPresenterImpl(final Intent intent) {
        url = intent.getStringExtra("url");
        title = intent.getStringExtra("title");
        Observable.create((ObservableOnSubscribe<List<BookShelf>>) e -> {
                    List<BookShelf> temp;
                    try (Query<BookShelf> query = ObjectBoxManager.INSTANCE.getBookShelfBox().query().build()) {
                        temp = query.find();
                    }
                    e.onNext(temp);
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<>() {
                    @Override
                    public void onNext(List<BookShelf> value) {
                        bookShelves.addAll(value);
                        initPage();
                        toSearchBooks(null);
                        mView.startRefreshAnim();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: ", e);
                    }
                });
    }

    @Override
    public int getPage() {
        return page;
    }

    @Override
    public void initPage() {
        this.page = 1;
        this.startThisSearchTime = System.currentTimeMillis();
    }

    @Override
    public void toSearchBooks(String key) {
        final long tempTime = startThisSearchTime;
        searchBook(tempTime);
    }

    private void searchBook(final long searchTime) {
        WebBookModelImpl.getInstance().getKindBook(mView.getContext(), url, page)
                .subscribeOn(Schedulers.io())
                .compose(((BaseActivity<?>) mView.getContext()).bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<>() {
                    @Override
                    public void onNext(List<SearchBook> value) {
                        if (searchTime == startThisSearchTime && value != null) {
                            for (SearchBook temp : value) {
                                for (BookShelf bookShelf : bookShelves) {
                                    if (Objects.equals(temp.noteUrl, bookShelf.noteUrl)) {
                                        temp.setAdd(true);
                                        break;
                                    }
                                }
                            }
                            if (page == 1) {
                                mView.refreshSearchBook(value);
                                mView.refreshFinish(value.isEmpty());
                            } else {
                                mView.loadMoreSearchBook(value);
                                mView.loadMoreFinish(value.isEmpty());
                            }
                            page++;
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: ", e);
                        mView.searchBookError();
                    }
                });
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void addBookToShelf(final SearchBook searchBook) {
        final BookShelf bookShelfResult = new BookShelf();
        bookShelfResult.noteUrl = searchBook.noteUrl;
        bookShelfResult.finalDate = 0;
        bookShelfResult.durChapter = 0;
        bookShelfResult.durChapterPage = 0;
        bookShelfResult.setTag(searchBook.tag);
        WebBookModelImpl.getInstance().getBookInfo(bookShelfResult)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(((BaseActivity<?>) mView.getContext()).bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(new SimpleObserver<>() {
                    @Override
                    public void onNext(BookShelf value) {
                        WebBookModelImpl.getInstance().getChapterList(value).subscribe(new SimpleObserver<>() {
                            @Override
                            public void onNext(WebChapter<BookShelf> bookShelfWebChapter) {
                                saveBookToShelf(bookShelfWebChapter.data);
                            }

                            @Override
                            public void onError(Throwable e) {
                                mView.addBookShelfFailed(NetworkUtil.ERROR_CODE_OUTTIME);
                            }
                        });
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.addBookShelfFailed(NetworkUtil.ERROR_CODE_OUTTIME);
                    }
                });
    }

    @Override
    public String getTitle() {
        return title;
    }

    private void saveBookToShelf(final BookShelf bookShelf) {
        Observable.create((ObservableOnSubscribe<BookShelf>) e -> {
                    //todo insertOrReplaceInTx insertOrReplace
                    ObjectBoxManager.INSTANCE.getChapterListBox().put(bookShelf.getBookInfo().getTarget().chapterlist);
                    ObjectBoxManager.INSTANCE.getBookInfoBox().put(bookShelf.getBookInfo().getTarget());
                    //网络数据获取成功  存入BookShelf表数据库
                    ObjectBoxManager.INSTANCE.getBookShelfBox().put(bookShelf);
                    e.onNext(bookShelf);
                    e.onComplete();
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(((BaseActivity<?>) mView.getContext()).bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(new SimpleObserver<>() {
                    @Override
                    public void onNext(BookShelf value) {
                        //成功   //发送RxBus
                        RxBus.get().post(RxBusTag.HAD_ADD_BOOK, value);
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.addBookShelfFailed(NetworkUtil.ERROR_CODE_OUTTIME);
                    }
                });
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////

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
    public void hadAddBook(BookShelf bookShelf) {
        bookShelves.add(bookShelf);
        List<SearchBook> books = mView.getSearchBookAdapter().getSearchBooks();
        for (int i = 0; i < books.size(); i++) {
            if (Objects.equals(books.get(i).noteUrl, bookShelf.noteUrl)) {
                books.get(i).setAdd(true);
                mView.updateSearchItem(i);
                break;
            }
        }
    }

    @Subscribe(thread = EventThread.MAIN_THREAD,
            tags = {
                    @Tag(RxBusTag.HAD_REMOVE_BOOK)
            }
    )
    public void hadRemoveBook(BookShelf bookShelf) {
        for (int i = 0; i < bookShelves.size(); i++) {
            if (Objects.equals(bookShelves.get(i).noteUrl, bookShelf.noteUrl)) {
                bookShelves.remove(i);
                break;
            }
        }
        List<SearchBook> books = mView.getSearchBookAdapter().getSearchBooks();
        for (int i = 0; i < books.size(); i++) {
            if (Objects.equals(books.get(i).noteUrl, bookShelf.noteUrl)) {
                books.get(i).setAdd(false);
                mView.updateSearchItem(i);
                break;
            }
        }
    }
}