package com.ebook.book.mvvm.model;

import android.app.Application;
import android.util.Log;

import com.ebook.db.ObjectBoxManager;
import com.ebook.db.entity.BookShelf;
import com.ebook.db.entity.BookShelf_;
import com.xrn1997.common.mvvm.model.BaseModel;

import java.util.List;

import io.objectbox.query.QueryBuilder;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class BookListModel extends BaseModel {
    public BookListModel(Application application) {
        super(application);
    }

    public Observable<List<BookShelf>> getBookShelfList() {
        return Observable.create((ObservableOnSubscribe<List<BookShelf>>) e -> {
                    try (var query = ObjectBoxManager.INSTANCE.getBookShelfBox().query().order(BookShelf_.finalDate, QueryBuilder.DESCENDING).build()) {
                        var bookShelves = query.find();
                        var iterator = bookShelves.iterator();
                        while (iterator.hasNext()) {
                            var bookShelf = iterator.next();
                            Log.e("ttt", "getBookShelfList: " + bookShelf.getId());
                            var temp = bookShelf.bookInfo.getTarget();
                            if (temp == null) {
                                ObjectBoxManager.INSTANCE.getBookShelfBox().remove(bookShelf);
                                iterator.remove();
                            }
                        }
                        e.onNext(bookShelves);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
