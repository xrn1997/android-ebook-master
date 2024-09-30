package com.ebook.find.mvp.presenter.impl;


import android.util.Log;

import androidx.annotation.NonNull;

import com.ebook.api.service.TXTDownloadBookService;
import com.ebook.basebook.base.IView;
import com.ebook.basebook.base.impl.BasePresenterImpl;
import com.ebook.basebook.mvp.model.impl.WebBookModelImpl;
import com.ebook.basebook.utils.NetworkUtil;
import com.ebook.common.event.RxBusTag;
import com.ebook.db.ObjectBoxManager;
import com.ebook.db.entity.BookShelf;
import com.ebook.db.entity.SearchBook;
import com.ebook.db.entity.SearchHistory;
import com.ebook.db.entity.WebChapter;
import com.ebook.find.mvp.presenter.ISearchPresenter;
import com.ebook.find.mvp.view.ISearchView;
import com.ebook.find.mvvm.model.LibraryModel;
import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.xrn1997.common.event.SimpleObserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.objectbox.query.Query;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class SearchPresenterImpl extends BasePresenterImpl<ISearchView> implements ISearchPresenter {
    private static final String TAG = "SearchPresenterImpl";
    public static final String TAG_KEY = "tag";
    public static final String HAS_MORE_KEY = "hasMore";
    public static final String HAS_LOAD_KEY = "hasLoad";
    public static final String DUR_REQUEST_TIME = "durRequestTime";    //当前搜索引擎失败次数  成功一次会重新开始计数
    public static final String MAX_REQUEST_TIME = "maxRequestTime";   //最大连续请求失败次数

    public static final int BOOK = 2;
    private final List<Map<String, Object>> searchEngine;
    private final List<BookShelf> bookShelfList = new ArrayList<>();   //用来比对搜索的书籍是否已经添加进书架
    private Boolean hasSearch = false;   //判断是否搜索过
    private int page = 1;
    private long startThisSearchTime;
    private String durSearchKey;
    private Boolean isInput = false;

    public SearchPresenterImpl() {
        Observable.create((ObservableOnSubscribe<List<BookShelf>>) e -> {
                    List<BookShelf> temp;
                    try (Query<BookShelf> query = ObjectBoxManager.INSTANCE.getBookShelfBox().query().build()) {
                        temp = query.find();
                    }
                    e.onNext(temp);
                    e.onComplete();
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<>() {
                    @Override
                    public void onNext(List<BookShelf> value) {
                        bookShelfList.addAll(value);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: ", e);
                    }
                });

        //搜索引擎初始化
        searchEngine = new ArrayList<>();

        Map<String, Object> map = new HashMap<>();
        map.put(TAG_KEY, TXTDownloadBookService.URL);
        map.put(HAS_MORE_KEY, true);
        map.put(HAS_LOAD_KEY, false);
        map.put(DUR_REQUEST_TIME, 1);
        map.put(MAX_REQUEST_TIME, 3);
        searchEngine.add(map);
    }

    @Override
    public Boolean getHasSearch() {
        return hasSearch;
    }

    @Override
    public void setHasSearch(Boolean hasSearch) {
        this.hasSearch = hasSearch;
    }

    @Override
    public void insertSearchHistory() {
        final String content = mView.getEdtContent().getText().toString().trim();
        LibraryModel.insertSearchHistory(SearchPresenterImpl.BOOK, content)
                .subscribe(new SimpleObserver<>() {
                    @Override
                    public void onNext(SearchHistory value) {
                        mView.insertSearchHistorySuccess(value);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: ", e);
                    }
                });
    }

    @Override
    public void cleanSearchHistory() {
        final String content = mView.getEdtContent().getText().toString().trim();
        LibraryModel.cleanSearchHistory(SearchPresenterImpl.BOOK, content)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<>() {
                    @Override
                    public void onNext(Integer value) {
                        if (value > 0) {
                            mView.querySearchHistorySuccess(null);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: ", e);
                    }
                });
    }

    @Override
    public void querySearchHistory() {
        final String content = mView.getEdtContent().getText().toString().trim();
        LibraryModel.querySearchHistory(SearchPresenterImpl.BOOK, content)
                .subscribe(new SimpleObserver<>() {
                    @Override
                    public void onNext(List<SearchHistory> value) {
                        if (null != value)
                            mView.querySearchHistorySuccess(value);
                    }

                    @Override
                    public void onError(Throwable e) {

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
    }

    @Override
    public void toSearchBooks(String key, Boolean fromError) {
        if (key != null) {
            durSearchKey = key;
            this.startThisSearchTime = System.currentTimeMillis();
            for (int i = 0; i < searchEngine.size(); i++) {
                searchEngine.get(i).put(HAS_MORE_KEY, true);
                searchEngine.get(i).put(HAS_LOAD_KEY, false);
                searchEngine.get(i).put(DUR_REQUEST_TIME, 1);
            }
        }
        searchBook(durSearchKey, startThisSearchTime, fromError);
    }

    private void searchBook(final String content, final long searchTime, Boolean fromError) {
        if (searchTime == startThisSearchTime) {
            boolean canLoad = false;
            for (Map<String, Object> temp : searchEngine) {
                if ((Boolean) temp.get(HAS_MORE_KEY) && (int) temp.get(DUR_REQUEST_TIME) <= (int) temp.get(MAX_REQUEST_TIME)) {
                    canLoad = true;
                    break;
                }
            }
            if (canLoad) {
                int searchEngineIndex = -1;
                for (int i = 0; i < searchEngine.size(); i++) {
                    if (!(Boolean) searchEngine.get(i).get(HAS_LOAD_KEY) && (int) searchEngine.get(i).get(DUR_REQUEST_TIME) <= (int) searchEngine.get(i).get(MAX_REQUEST_TIME)) {
                        searchEngineIndex = i;
                        break;
                    }
                }
                if (searchEngineIndex == -1) {
                    this.page++;
                    for (Map<String, Object> item : searchEngine) {
                        item.put(HAS_LOAD_KEY, false);
                    }
                    if (!fromError) {
                        if (page - 1 == 1) {
                            mView.refreshFinish(false);
                        } else {
                            mView.loadMoreFinish(false);
                        }
                    } else {
                        searchBook(content, searchTime, false);
                    }
                } else {
                    final int finalSearchEngineIndex = searchEngineIndex;
                    WebBookModelImpl.getInstance().searchBook(content, page)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe(new SimpleObserver<>() {
                                @Override
                                public void onNext(List<SearchBook> value) {
                                    if (searchTime == startThisSearchTime) {
                                        searchEngine.get(finalSearchEngineIndex).put(HAS_LOAD_KEY, true);
                                        searchEngine.get(finalSearchEngineIndex).put(DUR_REQUEST_TIME, 1);
                                        if (value.isEmpty()) {
                                            searchEngine.get(finalSearchEngineIndex).put(HAS_MORE_KEY, false);
                                        } else {
                                            for (SearchBook temp : value) {
                                                for (BookShelf bookShelf : bookShelfList) {
                                                    if (Objects.equals(temp.noteUrl, bookShelf.noteUrl)) {
                                                        temp.setAdd(true);
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                        if (page == 1 && finalSearchEngineIndex == 0) {
                                            mView.refreshSearchBook(value);
                                        } else {
                                            if (!value.isEmpty() && !mView.checkIsExist(value.get(0)))
                                                mView.loadMoreSearchBook(value);
                                            else {
                                                searchEngine.get(finalSearchEngineIndex).put(HAS_MORE_KEY, false);
                                            }
                                        }
                                        searchBook(content, searchTime, false);
                                    }
                                }

                                @Override
                                public void onError(Throwable e) {
                                    Log.e(TAG, "onError: ", e);
                                    if (searchTime == startThisSearchTime) {
                                        searchEngine.get(finalSearchEngineIndex).put(HAS_LOAD_KEY, false);
                                        searchEngine.get(finalSearchEngineIndex).put(DUR_REQUEST_TIME, ((int) searchEngine.get(finalSearchEngineIndex).get(DUR_REQUEST_TIME)) + 1);
                                        mView.searchBookError(page == 1 && (finalSearchEngineIndex == 0 || mView.getSearchBookAdapter().getItemcount() == 0));
                                    }
                                }
                            });
                }
            } else {
                if (page == 1) {
                    mView.refreshFinish(true);
                } else {
                    mView.loadMoreFinish(true);
                }
                this.page++;
                for (Map<String, Object> item : searchEngine) {
                    item.put(HAS_LOAD_KEY, false);
                }
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void addBookToShelf(final SearchBook searchBook) {
        //  Log.e("添加到书架", searchBook.toString());
        final BookShelf bookShelfResult = new BookShelf();
        bookShelfResult.noteUrl = searchBook.noteUrl;
        bookShelfResult.finalDate = 0;
        bookShelfResult.durChapter = 0;
        bookShelfResult.durChapterPage = 0;
        bookShelfResult.setTag(searchBook.tag);
        WebBookModelImpl.getInstance().getBookInfo(bookShelfResult)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
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

    private void saveBookToShelf(final BookShelf bookShelf) {
        LibraryModel.saveBookToShelf(bookShelf)
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
        this.bookShelfList.add(bookShelf);
        List<SearchBook> searchBookList = mView.getSearchBookAdapter().getSearchBooks();
        for (int i = 0; i < searchBookList.size(); i++) {
            if (searchBookList.get(i).noteUrl.equals(bookShelf.noteUrl)) {
                searchBookList.get(i).setAdd(true);
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
        for (int i = 0; i < this.bookShelfList.size(); i++) {
            if (Objects.equals(this.bookShelfList.get(i).noteUrl, bookShelf.noteUrl)) {
                this.bookShelfList.remove(i);
                break;
            }
        }
        List<SearchBook> searchBookList = mView.getSearchBookAdapter().getSearchBooks();
        for (int i = 0; i < searchBookList.size(); i++) {
            if (Objects.equals(searchBookList.get(i).noteUrl, bookShelf.noteUrl)) {
                searchBookList.get(i).setAdd(false);
                mView.updateSearchItem(i);
                break;
            }
        }
    }

    @Override
    public Boolean getInput() {
        return isInput;
    }

    @Override
    public void setInput(Boolean input) {
        isInput = input;
    }
}
