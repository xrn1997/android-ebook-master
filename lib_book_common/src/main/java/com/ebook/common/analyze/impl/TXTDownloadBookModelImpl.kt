package com.ebook.common.analyze.impl

import android.content.Context
import android.util.Log
import com.ebook.api.RetrofitBuilder
import com.ebook.api.cache.ACache
import com.ebook.api.config.Url
import com.ebook.api.service.TXTDownloadBookService
import com.ebook.common.analyze.StationBookModel
import com.ebook.common.manager.ErrorAnalyzeContentManager
import com.ebook.db.entity.BookContent
import com.ebook.db.entity.BookInfo
import com.ebook.db.entity.BookShelf
import com.ebook.db.entity.ChapterList
import com.ebook.db.entity.Library
import com.ebook.db.entity.LibraryKindBookList
import com.ebook.db.entity.LibraryNewBook
import com.ebook.db.entity.SearchBook
import com.ebook.db.entity.WebChapter
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.schedulers.Schedulers
import org.jsoup.Jsoup
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import kotlin.math.max

/**
 * @author xrn1997
 */
object TXTDownloadBookModelImpl : StationBookModel {
    private const val TAG = "shuangliusc.com"
    private val mBookService = RetrofitBuilder.getRetrofitObject(TXTDownloadBookService.URL)
        .create(TXTDownloadBookService::class.java)

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    override fun getKindBook(url: String, page: Int): Observable<List<SearchBook>> {
        val type = when (url) {
            Url.xh -> 1
            Url.xz -> 2
            Url.ds -> 3
            Url.ls -> 4
            Url.wy -> 5
            Url.kh -> 6
            Url.qt -> 8
            else -> -1
        }
        if (type == -1) {
            Log.e(TAG, "getKindBook: 网址错误")
            return Observable.just(emptyList())
        }
        return mBookService.getKindBooks("/list/" + type + "_" + page + ".html")
            .flatMap { s: String -> analyzeKindBook(s) }
    }

    private fun analyzeKindBook(s: String): Observable<List<SearchBook>> {
        return Observable.create { e: ObservableEmitter<List<SearchBook>> ->
            val doc = Jsoup.parse(s)
            //解析分类书籍
            val kindBookEs = doc.getElementsByAttributeValue(
                "class",
                "txt-list txt-list-row5"
            )[0].getElementsByTag("li")
            val books: MutableList<SearchBook> =
                ArrayList()
            for (i in kindBookEs.indices) {
                val item = SearchBook()
                item.tag = TXTDownloadBookService.URL
                item.author = kindBookEs[i].getElementsByClass("s4").text()
                item.lastChapter = kindBookEs[i].getElementsByTag("a")[1].text()
                item.origin = TAG
                item.name = kindBookEs[i].getElementsByTag("a")[0].text()
                item.noteUrl = kindBookEs[i].getElementsByTag("a")[0].attr("href")
                val temp =
                    item.noteUrl.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val c = if (temp[temp.size - 1].length == 4) {
                    temp[temp.size - 1][0]
                } else {
                    '0'
                }
                item.coverUrl =
                    TXTDownloadBookService.COVER_URL + "/" + c + "/" + temp[temp.size - 1] + "/" + temp[temp.size - 1] + "s.jpg"
                books.add(item)
            }
            e.onNext(books)
            e.onComplete()
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    override fun getLibraryData(aCache: ACache): Observable<Library> {
        return mBookService.getLibraryData("").flatMap { s: String ->
            if (s.isNotEmpty()) {
                aCache.put("cache_library", s)
            }
            analyzeLibraryData(s)
        }
    }

    override fun analyzeLibraryData(data: String): Observable<Library> {
        return Observable.create { e: ObservableEmitter<Library> ->
            val result = Library()
            val doc = Jsoup.parse(data)
            //解析最新书籍
            val newBookEs = doc
                .getElementsByAttributeValue("class", "txt-list txt-list-row3")[1]
                .getElementsByClass("s2")
            val libraryNewBooks: MutableList<LibraryNewBook> =
                ArrayList()
            for (i in newBookEs.indices) {
                val itemE = newBookEs[i].getElementsByTag("a")[0]
                val item = LibraryNewBook(
                    itemE.text(),
                    itemE.attr("href"),
                    TXTDownloadBookService.URL,
                    TAG
                )
                libraryNewBooks.add(item)
            }
            result.libraryNewBooks = libraryNewBooks
            //////////////////////////////////////////////////////////////////////
            //解析分类推荐
            val kindBooks: MutableList<LibraryKindBookList> =
                ArrayList()
            val kindContentEs = doc.getElementsByClass("tp-box")
            val kindEs = doc.getElementsByClass("nav")
            for (i in kindContentEs.indices) {
                val kindItem = LibraryKindBookList()
                kindItem.kindName = kindContentEs[i].getElementsByTag("h2")[0].text()
                kindItem.kindUrl =
                    TXTDownloadBookService.URL + kindEs[0].getElementsByTag("a")[i + 2].attr("href")
                val books: MutableList<SearchBook> =
                    ArrayList()
                val firstBookE = kindContentEs[i].getElementsByClass("top")[0]
                val firstBook = SearchBook()
                firstBook.tag = TXTDownloadBookService.URL
                firstBook.origin = TAG
                firstBook.name = firstBookE.getElementsByTag("a")[1].text()
                firstBook.noteUrl = firstBookE.getElementsByTag("a")[0].attr("href")
                //                Log.e(TAG, "analyzeLibraryData: "+ZeroBookService.URL + firstBookE.getElementsByTag("img").get(0).attr("src") );
                firstBook.coverUrl =
                    TXTDownloadBookService.URL + firstBookE.getElementsByTag("img")[0].attr("src")
                firstBook.kind = kindItem.kindName
                books.add(firstBook)
                val otherBookEs = kindContentEs[i].getElementsByTag("li")
                for (j in otherBookEs.indices) {
                    val item = SearchBook()
                    item.tag = TXTDownloadBookService.URL
                    item.origin = TAG
                    item.kind = kindItem.kindName
                    item.noteUrl = otherBookEs[j].getElementsByTag("a")[0].attr("href")
                    val temp = item.noteUrl.split("/".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                    //小说URL比较特殊，需要拼凑一下。
                    val bookNum = temp[temp.size - 1].trim()
                    val index = max((bookNum.length - 3).toDouble(), 0.0).toInt()
                    val c = if (index == 0) "0" else bookNum.substring(0, index)
                    item.coverUrl =
                        TXTDownloadBookService.COVER_URL + "/" + c + "/" + temp[temp.size - 1] + "/" + temp[temp.size - 1] + "s.jpg"
                    //                    Log.e(TAG, "analyzeLibraryData: " + item.getCoverUrl());
                    item.name = otherBookEs[j].getElementsByTag("a")[0].text()
                    books.add(item)
                }
                kindItem.books = books
                kindBooks.add(kindItem)
            }
            //////////////
            result.kindBooks = kindBooks
            e.onNext(result)
            e.onComplete()
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    override fun searchBook(content: String, page: Int): Observable<List<SearchBook>> {
        try {
            val str = URLEncoder.encode(content, "UTF-8")
            return mBookService
                .searchBook(str)
                .flatMap { s: String ->
                    analyzeSearchBook(
                        s
                    )
                }
        } catch (e: UnsupportedEncodingException) {
            throw RuntimeException(e)
        }
    }


    private fun analyzeSearchBook(s: String): Observable<List<SearchBook>> {
        return Observable.create { e: ObservableEmitter<List<SearchBook>> ->
            try {
                val doc = Jsoup.parse(s)
                val booksE = doc.getElementsByAttributeValue(
                    "class",
                    "txt-list txt-list-row5"
                )[0].getElementsByTag("li")
                //第一个为列表表头，所以如果有书booksE的size必定大于2
                if (booksE.size >= 2) {
                    val books: MutableList<SearchBook> =
                        ArrayList()
                    for (i in 1 until booksE.size) {
                        val item = SearchBook()
                        item.tag = TXTDownloadBookService.URL
                        item.author = booksE[i].getElementsByClass("s4")[0].text()
                        item.lastChapter =
                            booksE[i].getElementsByClass("s3")[0].getElementsByTag("a")[0].text()
                        item.origin = TAG
                        item.name =
                            booksE[i].getElementsByClass("s2")[0].getElementsByTag("a")[0].text()
                        item.noteUrl =
                            booksE[i].getElementsByClass("s2")[0].getElementsByTag("a")[0].attr("href")
                        val temp = item.noteUrl.split("/".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray()
                        //小说URL比较特殊，需要拼凑一下。
                        val bookNum = temp[temp.size - 1].trim()
                        val index = max((bookNum.length - 3).toDouble(), 0.0).toInt()
                        val c = if (index == 0) "0" else bookNum.substring(0, index)
                        item.coverUrl =
                            TXTDownloadBookService.COVER_URL + "/" + c + "/" + temp[temp.size - 1] + "/" + temp[temp.size - 1] + "s.jpg"
                        books.add(item)
                    }
                    e.onNext(books)
                } else {
                    e.onNext(ArrayList())
                }
            } catch (ex: Exception) {
                Log.e(TAG, "analyzeSearchBook: ", ex)
                e.onNext(ArrayList())
            }
            e.onComplete()
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    override fun getBookInfo(bookShelf: BookShelf): Observable<BookShelf> {
        return mBookService
            .getBookInfo(bookShelf.noteUrl.replace(TXTDownloadBookService.URL, ""))
            .flatMap { s: String ->
                analyzeBookInfo(
                    s,
                    bookShelf
                )
            }
    }

    private fun analyzeBookInfo(s: String, bookShelf: BookShelf): Observable<BookShelf> {
        return Observable.create { e: ObservableEmitter<BookShelf> ->
            bookShelf.tag = TXTDownloadBookService.URL
            bookShelf.bookInfo.target = analyzeBookInfo(s, bookShelf.noteUrl)
            e.onNext(bookShelf)
            e.onComplete()
        }
    }

    private fun analyzeBookInfo(s: String, novelUrl: String): BookInfo {
        val bookInfo = BookInfo()
        bookInfo.noteUrl = novelUrl
        bookInfo.tag = TXTDownloadBookService.URL
        val doc = Jsoup.parse(s)
        bookInfo.name = doc.getElementsByClass("info")[0].getElementsByTag("h1")[0].text()
        bookInfo.author = doc.getElementsByClass("info")[0].getElementsByTag("p")[0].text()
            .replace("作&nbsp;&nbsp;&nbsp;&nbsp;者：", "")
        bookInfo.introduce = "\u3000\u3000" + doc.getElementsByAttributeValue(
            "class",
            "desc xs-hidden"
        )[0].text()
        if (bookInfo.introduce == "\u3000\u3000") {
            bookInfo.introduce = "暂无简介"
        }
        val temp = novelUrl.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val c = if (temp[temp.size - 1].length == 4) {
            temp[temp.size - 1][0]
        } else {
            '0'
        }
        bookInfo.coverUrl =
            TXTDownloadBookService.COVER_URL + "/" + c + "/" + temp[temp.size - 1] + "/" + temp[temp.size - 1] + "s.jpg"
        bookInfo.chapterUrl = novelUrl
        bookInfo.origin = TAG

        return bookInfo
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    override fun getChapterList(bookShelf: BookShelf): Observable<WebChapter<BookShelf>> {
        return mBookService
            .getChapterList(
                bookShelf.bookInfo.target.chapterUrl.replace(
                    TXTDownloadBookService.URL,
                    ""
                )
            )
            .flatMap { s: String ->
                analyzeChapterList(
                    s,
                    bookShelf
                )
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    private fun analyzeChapterList(
        s: String,
        bookShelf: BookShelf
    ): Observable<WebChapter<BookShelf>> {
        return Observable.create { e: ObservableEmitter<WebChapter<BookShelf>> ->
            bookShelf.tag = TXTDownloadBookService.URL
            val temp = analyzeChapterList(s, bookShelf.noteUrl)
            val bookInfo = bookShelf.bookInfo.target
            for (chapterList in temp.data) {
                chapterList.bookInfo.target = bookInfo
                bookInfo.chapterList.add(chapterList)
            }
            e.onNext(WebChapter(bookShelf, temp.next))
            e.onComplete()
        }
    }

    private fun analyzeChapterList(s: String, novelUrl: String): WebChapter<List<ChapterList>> {
        val doc = Jsoup.parse(s)
        val chapterList = doc.getElementById("section-list")!!.getElementsByTag("li")
        val chapters: MutableList<ChapterList> = ArrayList()
        for (i in chapterList.indices) {
            val temp = ChapterList()
            temp.durChapterUrl =
                TXTDownloadBookService.URL + chapterList[i].getElementsByTag("a").attr("href") //id
            temp.durChapterIndex = i
            temp.durChapterName = chapterList[i].getElementsByTag("a").text()
            temp.noteUrl = novelUrl
            temp.tag = TXTDownloadBookService.URL

            chapters.add(temp)
        }
        return WebChapter(chapters, false)
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    override fun getBookContent(
        context: Context,
        durChapterUrl: String,
        durChapterIndex: Int
    ): Observable<BookContent> {
        return mBookService
            .getBookContent(durChapterUrl.replace(TXTDownloadBookService.URL, ""))
            .flatMap { s: String ->
                analyzeBookContent(
                    context,
                    s,
                    durChapterUrl,
                    durChapterIndex
                )
            }
    }


    private fun analyzeBookContent(
        context: Context,
        s: String,
        durChapterUrl: String,
        durChapterIndex: Int
    ): Observable<BookContent> {
        return Observable.create { e: ObservableEmitter<BookContent> ->
            val bookContent = BookContent()
            bookContent.durChapterIndex = durChapterIndex
            bookContent.durChapterUrl = durChapterUrl
            bookContent.tag = TXTDownloadBookService.URL
            try {
                val doc = Jsoup.parse(s)
                val contentEs = doc.getElementById("content")!!
                    .textNodes()
                val content = StringBuilder()
                for (i in contentEs.indices) {
                    var temp = contentEs[i].text().trim { it <= ' ' }
                    temp = temp.replace(" ".toRegex(), "").replace(" ".toRegex(), "")
                        .replace("\\s*".toRegex(), "")
                    if (temp.isNotEmpty()) {
                        content.append("\u3000\u3000").append(temp)
                        if (i < contentEs.size - 1) {
                            content.append("\r\n")
                        }
                    }
                }
                bookContent.durChapterContent = content.toString()
                bookContent.right = true
            } catch (ex: Exception) {
                Log.e(TAG, "analyzeBookContent: ", ex)

                ErrorAnalyzeContentManager.getInstance().writeNewErrorUrl(context, durChapterUrl)
                bookContent.durChapterContent =
                    durChapterUrl.substring(0, durChapterUrl.indexOf('/', 8)) + "站点暂时不支持解析"
                bookContent.right = false
            }
            e.onNext(bookContent)
            e.onComplete()
        }
    }
}
