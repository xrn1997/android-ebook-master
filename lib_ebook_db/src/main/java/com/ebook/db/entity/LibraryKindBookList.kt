package com.ebook.db.entity

/**
 * 书城 书籍分类推荐列表
 */
class LibraryKindBookList {
    @JvmField
    var kindName: String = String()

    @JvmField
    var kindUrl: String = String()

    @JvmField
    var books: List<SearchBook>? = null
}
