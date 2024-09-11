package com.ebook.find.mvvm.model;

import android.app.Application;

import com.ebook.basebook.cache.ACache;
import com.ebook.basebook.constant.Url;
import com.ebook.basebook.mvp.model.impl.WebBookModelImpl;
import com.ebook.db.ObjectBoxManager;
import com.ebook.db.entity.BookInfo;
import com.ebook.db.entity.BookShelf;
import com.ebook.db.entity.ChapterList;
import com.ebook.db.entity.Library;
import com.ebook.db.entity.SearchHistory;
import com.ebook.db.entity.SearchHistory_;
import com.ebook.find.entity.BookType;
import com.xrn1997.common.mvvm.model.BaseModel;

import java.util.ArrayList;
import java.util.List;

import io.objectbox.Box;
import io.objectbox.query.QueryBuilder;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;


public class LibraryModel extends BaseModel {
    public final static String LIBRARY_CACHE_KEY = "cache_library";

    public LibraryModel(Application application) {
        super(application);
    }

    //获得书库信息
    public static Observable<Library> getLibraryData(ACache mCache) {
        return Observable.create((ObservableOnSubscribe<String>) e -> {
                    String cache = mCache.getAsString(LIBRARY_CACHE_KEY);
                    e.onNext(cache);
                    e.onComplete();
                }).flatMap((Function<String, ObservableSource<Library>>) s -> WebBookModelImpl.getInstance().analyzeLibraryData(s))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    //获取书架书籍列表信息
    public static Observable<List<BookShelf>> getBookShelfList() {
        return Observable.create((ObservableOnSubscribe<List<BookShelf>>) e -> {
                    List<BookShelf> temp = ObjectBoxManager.INSTANCE.getStore().boxFor(BookShelf.class).query().build().find();
                    e.onNext(temp);
                    e.onComplete();
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    //将书籍信息存入书架书籍列表
    public static Observable<BookShelf> saveBookToShelf(BookShelf bookShelf) {
        return Observable.create((ObservableOnSubscribe<BookShelf>) e -> {
                    //todo insertOrReplaceInTx
                    ObjectBoxManager.INSTANCE.getStore().boxFor(ChapterList.class).put(bookShelf.bookInfo.getTarget().chapterlist);
                    ObjectBoxManager.INSTANCE.getStore().boxFor(BookInfo.class).put(bookShelf.bookInfo.getTarget());
                    //网络数据获取成功  存入BookShelf表数据库
                    ObjectBoxManager.INSTANCE.getStore().boxFor(BookShelf.class).put(bookShelf);
                    e.onNext(bookShelf);
                    e.onComplete();
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    //保存查询记录
    public static Observable<SearchHistory> insertSearchHistory(int type, String content) {
        return Observable.create((ObservableOnSubscribe<SearchHistory>) e -> {
                    Box<SearchHistory> boxStore = ObjectBoxManager.INSTANCE.getStore().boxFor(SearchHistory.class);
                    List<SearchHistory> searchHistories = boxStore
                            .query(SearchHistory_.type.equal(type).and(SearchHistory_.content.equal(content)))
                            .build().find(0, 1);
                    SearchHistory searchHistory;
                    if (!searchHistories.isEmpty()) {
                        searchHistory = searchHistories.get(0);
                        searchHistory.date = System.currentTimeMillis();
                        //todo update
                        boxStore.put(searchHistory);
                    } else {
                        searchHistory = new SearchHistory(type, content, System.currentTimeMillis());
                        boxStore.put(searchHistory);
                    }
                    e.onNext(searchHistory);
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    //删除查询记录
    public static Observable<Integer> cleanSearchHistory(int type, String content) {
        return Observable.create((ObservableOnSubscribe<Integer>) e -> {
                    Box<SearchHistory> boxStore = ObjectBoxManager.INSTANCE.getStore().boxFor(SearchHistory.class);
                    List<SearchHistory> histories = boxStore
                            .query(SearchHistory_.type.equal(type))
                            .contains(SearchHistory_.content, content, QueryBuilder.StringOrder.CASE_INSENSITIVE)  // 等同于 SQL 中的 "content LIKE ?"
                            .build().find();
                    boxStore.remove(histories);
                    e.onNext(histories.size());
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    //获得查询记录
    public static Observable<List<SearchHistory>> querySearchHistory(int type, String content) {
        return Observable.create((ObservableOnSubscribe<List<SearchHistory>>) e -> {
                    List<SearchHistory> histories = ObjectBoxManager.INSTANCE.getStore().boxFor(SearchHistory.class)
                            .query(SearchHistory_.type.equal(type))
                            .contains(SearchHistory_.content, content, QueryBuilder.StringOrder.CASE_INSENSITIVE)
                            .order(SearchHistory_.date, QueryBuilder.DESCENDING)
                            .build().find(0, 20);
                    e.onNext(histories);
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    //获取书籍类型信息，此处用本地数据。
    public List<BookType> getBookTypeList() {
        List<BookType> bookTypeList = new ArrayList<>();
        bookTypeList.add(new BookType("玄幻小说", Url.xh));
        bookTypeList.add(new BookType("修真小说", Url.xz));
        bookTypeList.add(new BookType("都市小说", Url.ds));
        bookTypeList.add(new BookType("历史小说", Url.ls));
        bookTypeList.add(new BookType("网游小说", Url.wy));
        bookTypeList.add(new BookType("科幻小说", Url.kh));
        bookTypeList.add(new BookType("其他小说", Url.qt));
        return bookTypeList;
    }

}
