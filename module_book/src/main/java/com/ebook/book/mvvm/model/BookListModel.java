package com.ebook.book.mvvm.model;

import android.app.Application;

import com.ebook.db.ObjectBoxManager;
import com.ebook.db.entity.BookInfo;
import com.ebook.db.entity.BookInfo_;
import com.ebook.db.entity.BookShelf;
import com.ebook.db.entity.BookShelf_;
import com.xrn1997.common.mvvm.model.BaseModel;

import java.util.List;

import io.objectbox.query.Query;
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
                    List<BookShelf> bookShelves;
                    try (Query<BookShelf> query = ObjectBoxManager.INSTANCE.getBookShelfBox().query().order(BookShelf_.finalDate, QueryBuilder.DESCENDING).build()) {
                        bookShelves = query.find();
                        for (int i = 0; i < bookShelves.size(); i++) {
                            List<BookInfo> temp = ObjectBoxManager.INSTANCE.getBookInfoBox().query(BookInfo_.noteUrl.equal(bookShelves.get(i).noteUrl)).build().find(0, 1);
                            if (temp.isEmpty()) {
                                ObjectBoxManager.INSTANCE.getBookShelfBox().remove(bookShelves.get(i));
                                bookShelves.remove(i);
                                i--;
                            }
                        }
                        e.onNext(bookShelves);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
