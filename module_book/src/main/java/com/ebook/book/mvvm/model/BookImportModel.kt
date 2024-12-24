package com.ebook.book.mvvm.model

import android.app.Application
import com.ebook.book.util.BookImportUtil
import com.ebook.db.entity.LocBookShelf
import com.xrn1997.common.mvvm.model.BaseModel
import io.reactivex.rxjava3.core.Observable
import java.io.File

class BookImportModel(application: Application) : BaseModel(application) {
    fun importBook(file: File): Observable<LocBookShelf> {
        return BookImportUtil.importBook(file)
    }
}