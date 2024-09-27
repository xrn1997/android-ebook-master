package com.ebook.basebook.mvp.presenter.impl;

import static com.xrn1997.common.BaseApplication.context;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import com.ebook.basebook.base.activity.BaseActivity;
import com.ebook.basebook.base.impl.BasePresenterImpl;
import com.ebook.basebook.base.manager.BitIntentDataManager;
import com.ebook.basebook.mvp.model.impl.ImportBookModelImpl;
import com.ebook.basebook.mvp.model.impl.WebBookModelImpl;
import com.ebook.basebook.mvp.presenter.IBookReadPresenter;
import com.ebook.basebook.mvp.view.IBookReadView;
import com.ebook.basebook.observer.SimpleObserver;
import com.ebook.basebook.view.BookContentView;
import com.ebook.common.event.RxBusTag;
import com.ebook.common.util.ToastUtil;
import com.ebook.db.ObjectBoxManager;
import com.ebook.db.entity.BookContent;
import com.ebook.db.entity.BookContent_;
import com.ebook.db.entity.BookShelf;
import com.ebook.db.entity.BookShelf_;
import com.ebook.db.entity.LocBookShelf;
import com.ebook.db.entity.ReadBookContent;
import com.ebook.db.event.DBCode;
import com.hwangjr.rxbus.RxBus;
import com.trello.rxlifecycle3.android.ActivityEvent;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class ReadBookPresenterImpl extends BasePresenterImpl<IBookReadView> implements IBookReadPresenter {

    public final static int OPEN_FROM_OTHER = 0;
    public final static int OPEN_FROM_APP = 1;
    private final static String TAG = "ReadBookPresenterImpl";
    private Boolean isAdd = false; //判断是否已经添加进书架
    private int open_from;
    private BookShelf bookShelf;

    private int pageLineCount = 5;   //假设5行一页

    public ReadBookPresenterImpl() {

    }

    @Override
    public void initData(Activity activity) {
        Intent intent = activity.getIntent();
        open_from = intent.getIntExtra("from", OPEN_FROM_OTHER);
        if (open_from == OPEN_FROM_APP) {
            String key = intent.getStringExtra("data_key");
            bookShelf = (BookShelf) BitIntentDataManager.getInstance().getData(key);
            Log.e(TAG, "initData: " + bookShelf.bookInfo.getTarget().chapterlist.size());
            if (!Objects.equals(bookShelf.getTag(), BookShelf.LOCAL_TAG)) {
                mView.showDownloadMenu();
            }
            BitIntentDataManager.getInstance().cleanData(key);
            checkInShelf();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                    !Environment.isExternalStorageManager()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                        .setMessage("在Android11及以上的版本中，本程序还需要您同意允许访问所有文件权限，不然无法打开和扫描本地文件")
                        .setPositiveButton("确定", (dialog, which) -> mView.getRequestPermission().launch(new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)))
                        .setNegativeButton("取消", ((dialog, which) -> activity.onBackPressed()));
                AlertDialog dialog = builder.create();
                //点击dialog之外的空白处，dialog不能消失
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
            } else {
                openBookFromOther(activity);
            }
        }
    }

    @Override
    public void openBookFromOther(Activity activity) {
        //APP外部打开
        Uri uri = activity.getIntent().getData();
        mView.showLoadBook();
        getRealFilePath(activity, uri)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new SimpleObserver<>() {
                    @Override
                    public void onNext(@NotNull String value) {
                        ImportBookModelImpl.getInstance().importBook(new File(value))
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeOn(Schedulers.io())
                                .subscribe(new SimpleObserver<>() {
                                    @Override
                                    public void onNext(@NotNull LocBookShelf value) {
                                        if (value.getNew())
                                            RxBus.get().post(RxBusTag.HAD_ADD_BOOK, value);
                                        bookShelf = value.bookShelf;
                                        mView.dimissLoadBook();
                                        checkInShelf();
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        Log.e(TAG, "onError: ", e);
                                        mView.dimissLoadBook();
                                        mView.loadLocationBookError();
                                        ToastUtil.showToast("文本打开失败！");
                                    }
                                });
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: ", e);
                        mView.dimissLoadBook();
                        mView.loadLocationBookError();
                        ToastUtil.showToast("文本打开失败！");
                    }
                });
    }

    @Override
    public void detachView() {
    }

    @Override
    public int getOpen_from() {
        return open_from;
    }

    @Override
    public BookShelf getBookShelf() {
        return bookShelf;
    }

    @Override
    public void initContent() {
        mView.initContentSuccess(bookShelf.durChapter, bookShelf.getBookInfo().getTarget().chapterlist.size(), bookShelf.durChapterPage);
    }

    @Override
    public void loadContent(final BookContentView bookContentView, final long bookTag, final int chapterIndex, int pageIndex) {
        if (null != bookShelf && !bookShelf.getBookInfo().getTarget().chapterlist.isEmpty()) {
            var chapterList = bookShelf.getBookInfo().getTarget().chapterlist.get(chapterIndex);
            var bookContent = chapterList.getBookContent().getTarget();
            if (bookContent != null && !bookContent.durChapterContent.isEmpty()) {
                if (bookContent.lineSize == mView.getPaint().getTextSize() && !bookContent.lineContent.isEmpty()) {
                    //已有数据
                    int tempCount = (int) Math.ceil(bookContent.lineContent.size() * 1.0 / pageLineCount) - 1;

                    if (pageIndex == DBCode.BookContentView.DUR_PAGE_INDEX_BEGIN) {
                        pageIndex = 0;
                    } else if (pageIndex == DBCode.BookContentView.DUR_PAGE_INDEX_END) {
                        pageIndex = tempCount;
                    } else {
                        if (pageIndex >= tempCount) {
                            pageIndex = tempCount;
                        }
                    }

                    int start = pageIndex * pageLineCount;
                    int end = pageIndex == tempCount ? bookContent.lineContent.size() : start + pageLineCount;
                    if (bookContentView != null && bookTag == bookContentView.getqTag()) {
                        bookContentView.updateData(bookTag, chapterList.getDurChapterName()
                                , bookContent.lineContent.subList(start, end)
                                , chapterIndex
                                , bookShelf.getBookInfo().getTarget().chapterlist.size()
                                , pageIndex
                                , tempCount + 1);
                    }
                } else {
                    //有元数据  重新分行
                    bookContent.lineSize = mView.getPaint().getTextSize();
                    final int finalPageIndex = pageIndex;
                    SeparateParagraphtoLines(bookContent.durChapterContent)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .compose(((BaseActivity<?>) mView.getContext()).bindUntilEvent(ActivityEvent.DESTROY))
                            .subscribe(new SimpleObserver<>() {
                                @Override
                                public void onNext(List<String> value) {
                                    bookContent.lineContent.clear();
                                    bookContent.lineContent.addAll(value);
                                    loadContent(bookContentView, bookTag, chapterIndex, finalPageIndex);
                                }

                                @Override
                                public void onError(Throwable e) {
                                    if (bookContentView != null && bookTag == bookContentView.getqTag())
                                        bookContentView.loadError();
                                }
                            });
                }
            } else {
                final int finalPageIndex1 = pageIndex;
                Observable.create((ObservableOnSubscribe<ReadBookContent>) e -> {
                            try (var query = ObjectBoxManager.INSTANCE.getBookContentBox()
                                    .query(BookContent_.durChapterUrl.equal(chapterList.getDurChapterUrl()))
                                    .build()) {
                                var tempList = query.find();
                                e.onNext(new ReadBookContent(tempList, finalPageIndex1));
                                e.onComplete();
                            } catch (Exception ex) {
                                e.onError(ex);
                            }
                        }).observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .compose(((BaseActivity<?>) mView.getContext()).bindUntilEvent(ActivityEvent.DESTROY))
                        .subscribe(new SimpleObserver<>() {
                            @Override
                            public void onNext(ReadBookContent tempList) {
                                if (!tempList.bookContentList.isEmpty() && !tempList.bookContentList.get(0).durChapterContent.isEmpty()) {
                                    chapterList.bookContent.setTarget(tempList.bookContentList.get(0));
                                    loadContent(bookContentView, bookTag, chapterIndex, tempList.pageIndex);
                                } else {
                                    final int finalPageIndex1 = tempList.pageIndex;
                                    WebBookModelImpl.getInstance().getBookContent(context, chapterList.getDurChapterUrl(), chapterIndex).map(bookContent -> {
                                                if (bookContent.getRight()) {
                                                    chapterList.setHasCache(true);
                                                    chapterList.getBookContent().setTarget(bookContent);
                                                    ObjectBoxManager.INSTANCE.getChapterListBox().put(chapterList);
                                                }
                                                return bookContent;
                                            })
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribeOn(Schedulers.io())
                                            .compose(((BaseActivity<?>) mView.getContext()).bindUntilEvent(ActivityEvent.DESTROY))
                                            .subscribe(new SimpleObserver<>() {
                                                @Override
                                                public void onNext(BookContent value) {
                                                    if (!value.durChapterUrl.isEmpty()) {
                                                        chapterList.bookContent.setTarget(value);
                                                        if (bookTag == bookContentView.getqTag())
                                                            loadContent(bookContentView, bookTag, chapterIndex, finalPageIndex1);
                                                    } else {
                                                        if (bookContentView != null && bookTag == bookContentView.getqTag())
                                                            bookContentView.loadError();
                                                    }
                                                }

                                                @Override
                                                public void onError(Throwable e) {
                                                    Log.e(TAG, "onError: ", e);
                                                    if (bookContentView != null && bookTag == bookContentView.getqTag())
                                                        bookContentView.loadError();
                                                }
                                            });
                                }
                            }

                            @Override
                            public void onError(Throwable e) {

                            }
                        });
            }
        } else {
            if (bookContentView != null && bookTag == bookContentView.getqTag())
                bookContentView.loadError();
        }
    }

    @Override
    public void updateProgress(int chapterIndex, int pageIndex) {
        bookShelf.durChapter = chapterIndex;
        bookShelf.durChapterPage = pageIndex;
    }

    @Override
    public void saveProgress() {
        if (bookShelf != null) {
            Observable.create((ObservableOnSubscribe<BookShelf>) e -> {
                        bookShelf.finalDate = System.currentTimeMillis();
                        try (var query = ObjectBoxManager.INSTANCE.getBookShelfBox().query(BookShelf_.noteUrl.equal(bookShelf.noteUrl)).build()) {
                            var temp = query.findFirst();
                            if (temp != null) {
                                bookShelf.setId(temp.getId());
                            }
                        }
                        ObjectBoxManager.INSTANCE.getBookShelfBox().put(bookShelf);
                        e.onNext(bookShelf);
                        e.onComplete();
                    }).subscribeOn(Schedulers.io())
                    .subscribe(new SimpleObserver<>() {
                        @Override
                        public void onNext(BookShelf value) {
                            RxBus.get().post(RxBusTag.UPDATE_BOOK_PROGRESS, bookShelf);
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.e(TAG, "onError: ", e);
                        }
                    });
        }
    }

    @Override
    public String getChapterTitle(int chapterIndex) {
        if (bookShelf.getBookInfo().getTarget().chapterlist.isEmpty()) {
            return "无章节";
        } else
            return bookShelf.getBookInfo().getTarget().chapterlist.get(chapterIndex).getDurChapterName();
    }

    public Observable<List<String>> SeparateParagraphtoLines(final String paragraphstr) {
        return Observable.create(e -> {
            TextPaint mPaint = (TextPaint) mView.getPaint();
            mPaint.setSubpixelText(true);
            Layout tempLayout = new StaticLayout(paragraphstr, mPaint, mView.getContentWidth(), Layout.Alignment.ALIGN_NORMAL, 0, 0, false);
            List<String> linesdata = new ArrayList<>();
            for (int i = 0; i < tempLayout.getLineCount(); i++) {
                linesdata.add(paragraphstr.substring(tempLayout.getLineStart(i), tempLayout.getLineEnd(i)));
            }
            e.onNext(linesdata);
            e.onComplete();
        });
    }

    @Override
    public void setPageLineCount(int pageLineCount) {
        this.pageLineCount = pageLineCount;
    }

    private void checkInShelf() {
        Observable.create((ObservableOnSubscribe<Boolean>) e -> {
                    try (var query = ObjectBoxManager.INSTANCE.getBookShelfBox()
                            .query(BookShelf_.noteUrl.equal(bookShelf.noteUrl)).build()) {
                        var temp = query.find();
                        isAdd = !temp.isEmpty();
                        e.onNext(isAdd);
                        e.onComplete();
                    } catch (Exception ex) {
                        e.onError(ex);
                    }
                }).subscribeOn(Schedulers.io())
                .compose(((BaseActivity<?>) mView.getContext()).bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<>() {
                    @Override
                    public void onNext(Boolean value) {
                        mView.initPop();
                        mView.setHpbReadProgressMax(bookShelf.getBookInfo().getTarget().chapterlist.size());
                        mView.startLoadingBook();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: ", e);
                    }
                });
    }

    @Override
    public void addToShelf(final OnAddListner addListner) {
        if (bookShelf != null) {
            Observable.create((ObservableOnSubscribe<Boolean>) e -> {
                        try (var query = ObjectBoxManager.INSTANCE.getBookShelfBox().query(BookShelf_.noteUrl.equal(bookShelf.noteUrl)).build()) {
                            var temp = query.findFirst();
                            if (temp != null) {
                                bookShelf.setId(temp.getId());
                            }
                        }
                        //网络数据获取成功  存入BookShelf表数据库
                        ObjectBoxManager.INSTANCE.getBookShelfBox().put(bookShelf);
                        RxBus.get().post(RxBusTag.HAD_ADD_BOOK, bookShelf);
                        isAdd = true;
                        e.onNext(true);
                        e.onComplete();
                    }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SimpleObserver<Object>() {
                        @Override
                        public void onNext(Object value) {
                            if (addListner != null)
                                addListner.addSuccess();
                        }

                        @Override
                        public void onError(Throwable e) {

                        }
                    });
        }
    }

    public Boolean getAdd() {
        return isAdd;
    }

    public Observable<String> getRealFilePath(final Context context, final Uri uri) {
        return Observable.create(e -> {
            String data = "";
            if (null != uri) {
                final String scheme = uri.getScheme();
                if (scheme == null)
                    data = uri.getPath();
                else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
                    data = uri.getPath();
                } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
                    Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
                    if (null != cursor) {
                        if (cursor.moveToFirst()) {
                            int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                            if (index > -1) {
                                data = cursor.getString(index);
                            }
                        }
                        cursor.close();
                    }

                    if ((data == null || data.isEmpty()) && uri.getPath() != null && uri.getPath().contains("/storage/emulated/")) {
                        data = uri.getPath().substring(uri.getPath().indexOf("/storage/emulated/"));
                    }
                }
            }
            e.onNext(data == null ? "" : data);
            e.onComplete();
        });
    }

    public interface OnAddListner {
        void addSuccess();
    }
}
