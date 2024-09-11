package com.ebook.db.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SearchBook(
    @JvmField
    var noteUrl: String? = null,
    @JvmField
    var coverUrl: String? = null,
    @JvmField
    var name: String? = null,
    @JvmField
    var author: String? = null,
    @JvmField
    var words: Long = 0,
    @JvmField
    var state: String? = null,
    @JvmField
    var lastChapter: String? = null,
    var add: Boolean = false,
    @JvmField
    var tag: String? = null,
    @JvmField
    var kind: String? = null,
    @JvmField
    var origin: String? = null,
    @JvmField
    var desc: String? = null
) : Parcelable