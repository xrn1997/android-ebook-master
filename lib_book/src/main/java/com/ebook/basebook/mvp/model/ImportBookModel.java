package com.ebook.basebook.mvp.model;

import com.ebook.db.entity.LocBookShelf;

import java.io.File;

import io.reactivex.rxjava3.core.Observable;


public interface ImportBookModel {

    Observable<LocBookShelf> importBook(File book);
}
