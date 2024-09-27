package com.ebook.book.service;

import static com.xrn1997.common.BaseApplication.context;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.ebook.basebook.mvp.model.impl.WebBookModelImpl;
import com.ebook.basebook.observer.SimpleObserver;
import com.ebook.book.R;
import com.ebook.book.fragment.MainBookFragment;
import com.ebook.common.event.RxBusTag;
import com.ebook.db.ObjectBoxManager;
import com.ebook.db.entity.BookContent;
import com.ebook.db.entity.BookContent_;
import com.ebook.db.entity.BookShelf;
import com.ebook.db.entity.BookShelf_;
import com.ebook.db.entity.ChapterList;
import com.ebook.db.entity.ChapterList_;
import com.ebook.db.entity.DownloadChapter;
import com.ebook.db.entity.DownloadChapterList;
import com.ebook.db.entity.DownloadChapter_;
import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;

import java.util.List;
import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

@SuppressWarnings("unused")
public class DownloadService extends Service {
    public static final String TAG = "DownloadService";
    public static final int reTryTimes = 1;
    private NotificationManager notifyManager;
    private Boolean isStartDownload = false;
    private Boolean isInit = false;
    private Boolean isDownloading = false;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RxBus.get().unregister(this);
        isInit = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isInit) {
            isInit = true;
            notifyManager = getSystemService(NotificationManager.class);
            NotificationChannel notificationChannel =
                    new NotificationChannel("40", "App Service", NotificationManager.IMPORTANCE_HIGH);
            notifyManager.createNotificationChannel(notificationChannel);
            RxBus.get().register(this);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void addNewTask(final List<DownloadChapter> newData) {
        isStartDownload = true;
        Observable.create((ObservableOnSubscribe<Boolean>) e -> {
                    for (var chapter : newData) {
                        try (var query = ObjectBoxManager.INSTANCE.getDownloadChapterBox().query(DownloadChapter_.durChapterUrl.equal(chapter.durChapterUrl)).build()) {
                            var tmp = query.findFirst();
                            if (tmp != null) {
                                chapter.setId(tmp.getId());
                            }
                        }
                    }
                    ObjectBoxManager.INSTANCE.getDownloadChapterBox().put(newData);
                    e.onNext(true);
                    e.onComplete();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new SimpleObserver<>() {
                    @Override
                    public void onNext(Boolean value) {
                        if (!isDownloading) {
                            toDownload();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: ", e);
                    }
                });
    }

    private void toDownload() {
        isDownloading = true;
        if (isStartDownload) {
            Observable.create((ObservableOnSubscribe<DownloadChapter>) e -> {
                        List<BookShelf> bookShelfList;
                        try (var query = ObjectBoxManager.INSTANCE.getBookShelfBox().query().orderDesc(BookShelf_.finalDate).build()) {
                            bookShelfList = query.find();
                        }
                        if (!bookShelfList.isEmpty()) {
                            for (BookShelf bookItem : bookShelfList) {
                                if (!Objects.equals(bookItem.getTag(), BookShelf.LOCAL_TAG)) {
                                    List<DownloadChapter> downloadChapterList;
                                    try (var query = ObjectBoxManager.INSTANCE.getDownloadChapterBox().query(DownloadChapter_.noteUrl.equal(bookItem.noteUrl)).order(DownloadChapter_.durChapterIndex).build()) {
                                        downloadChapterList = query.find(0, 1);
                                    }
                                    if (!downloadChapterList.isEmpty()) {
                                        e.onNext(downloadChapterList.get(0));
                                        e.onComplete();
                                        return;
                                    }
                                }
                            }
                        }
                        ObjectBoxManager.INSTANCE.getDownloadChapterBox().removeAll();
                        e.onNext(new DownloadChapter());
                        e.onComplete();
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SimpleObserver<>() {
                        @Override
                        public void onNext(DownloadChapter value) {
                            if (!value.noteUrl.isEmpty()) {
                                downloading(context,value, 0);
                            } else {
                                Observable.create(e -> {
                                            ObjectBoxManager.INSTANCE.getDownloadChapterBox().removeAll();
                                            e.onNext(new Object());
                                            e.onComplete();
                                        })
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new SimpleObserver<>() {
                                            @Override
                                            public void onNext(Object value) {
                                                isDownloading = false;
                                                finishDownload();
                                            }

                                            @Override
                                            public void onError(Throwable e) {
                                                Log.e(TAG, "onError: ", e);
                                                isDownloading = false;
                                            }
                                        });
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.e(TAG, "onError: ", e);
                            isDownloading = false;
                        }
                    });
        } else {
            isPause();
        }
    }

    private void downloading(Context context, final DownloadChapter data, final int durTime) {
        if (durTime < reTryTimes && isStartDownload) {
            isProgress(data);
            Observable.create((ObservableOnSubscribe<BookContent>) e -> {
                        try (var query = ObjectBoxManager.INSTANCE.getBookContentBox().query(BookContent_.durChapterUrl.equal(data.durChapterUrl)).build()) {
                            var result = query.findFirst();
                            e.onNext(result != null ? result : new BookContent());
                            e.onComplete();
                        } catch (Exception ex) {
                            e.onError(ex);
                        }
                    }).flatMap((Function<BookContent, ObservableSource<BookContent>>) bookContent -> {
                        if (bookContent.durChapterUrl.isEmpty()) {
                            //todo 存在问题
                            //章节内容不存在
                            return WebBookModelImpl.getInstance().getBookContent(context,data.durChapterUrl, data.durChapterIndex).map(bookContent1 -> {
                                ObjectBoxManager.INSTANCE.getDownloadChapterBox().remove(data);
                                Log.e(TAG, "downloading: " + bookContent1.getRight());
                                if (bookContent1.getRight()) {
                                    var cl = new ChapterList(data.noteUrl, data.durChapterIndex, data.durChapterUrl, data.durChapterName, data.tag, true);
                                    try (var query = ObjectBoxManager.INSTANCE.getChapterListBox().query(ChapterList_.durChapterUrl.equal(bookContent1.durChapterUrl)).build()) {
                                        var tmp = query.findFirst();
                                        if (tmp != null) {
                                            cl.setId(tmp.getId());
                                            var bc = tmp.getBookContent().getTarget();
                                            if (bc != null) {
                                                bookContent1.setId(bc.getId());
                                            }
                                        }
                                    }
                                    cl.bookContent.setTarget(bookContent1);
                                    ObjectBoxManager.INSTANCE.getChapterListBox().put(cl);
                                }
                                return bookContent1;
                            });
                        } else {
                            //存在章节内容
                            return Observable.create((ObservableOnSubscribe<BookContent>) e -> {
                                ObjectBoxManager.INSTANCE.getDownloadChapterBox().remove(data);
                                e.onNext(bookContent);
                                e.onComplete();
                            });
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(new SimpleObserver<>() {
                        @Override
                        public void onNext(BookContent value) {
                            if (isStartDownload) {
                                new Handler().postDelayed(() -> {
                                    if (isStartDownload) {
                                        toDownload();
                                    } else {
                                        isPause();
                                    }
                                }, 800);
                            } else {
                                isPause();
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.e(TAG, "onError: ", e);
                            int time = durTime + 1;
                            downloading(context,data, time);
                        }
                    });
        } else {
            if (isStartDownload) {
                Observable.create((ObservableOnSubscribe<Boolean>) e -> {
                            ObjectBoxManager.INSTANCE.getDownloadChapterBox().remove(data);
                            e.onNext(true);
                            e.onComplete();
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(new SimpleObserver<>() {
                            @Override
                            public void onNext(Boolean value) {
                                if (isStartDownload) {
                                    new Handler().postDelayed(() -> {
                                        if (isStartDownload) {
                                            toDownload();
                                        } else {
                                            isPause();
                                        }
                                    }, 800);
                                } else {
                                    isPause();
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e(TAG, "onError: ", e);
                                if (!isStartDownload)
                                    isPause();
                            }
                        });
            } else
                isPause();
        }
    }

    public void startDownload() {
        isStartDownload = true;
        toDownload();
    }

    public void pauseDownload() {
        isStartDownload = false;
        notifyManager.cancelAll();
    }

    public void cancelDownload() {
        Observable.create(e -> {
                    ObjectBoxManager.INSTANCE.getDownloadChapterBox().removeAll();
                    e.onNext(new Object());
                    e.onComplete();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Object value) {
                        isStartDownload = false;
                        notifyManager.cancelAll();
                        stopService(new Intent(getApplication(), DownloadService.class));
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void isPause() {
        isDownloading = false;
        Observable.create((ObservableOnSubscribe<DownloadChapter>) e -> {
                    List<BookShelf> bookShelfList;
                    try (var query = ObjectBoxManager.INSTANCE.getBookShelfBox().query().orderDesc(BookShelf_.finalDate).build()) {
                        bookShelfList = query.find();
                    }
                    if (!bookShelfList.isEmpty()) {
                        for (BookShelf bookItem : bookShelfList) {
                            if (!Objects.equals(bookItem.getTag(), BookShelf.LOCAL_TAG)) {
                                List<DownloadChapter> downloadChapterList;
                                try (var query = ObjectBoxManager.INSTANCE.getDownloadChapterBox().query(DownloadChapter_.noteUrl.equal(bookItem.noteUrl)).order(DownloadChapter_.durChapterIndex).build()) {
                                    downloadChapterList = query.find(0, 1);
                                }
                                if (!downloadChapterList.isEmpty()) {
                                    e.onNext(downloadChapterList.get(0));
                                    e.onComplete();
                                    return;
                                }
                            }
                        }
                    }
                    ObjectBoxManager.INSTANCE.getDownloadChapterBox().removeAll();
                    e.onNext(new DownloadChapter());
                    e.onComplete();
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<>() {
                    @Override
                    public void onNext(DownloadChapter value) {
                        if (!value.noteUrl.isEmpty()) {
                            RxBus.get().post(RxBusTag.PAUSE_DOWNLOAD_LISTENER, new Object());
                        } else {
                            RxBus.get().post(RxBusTag.FINISH_DOWNLOAD_LISTENER, new Object());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: ", e);
                    }
                });
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private void isProgress(DownloadChapter downloadChapter) {
        RxBus.get().post(RxBusTag.PROGRESS_DOWNLOAD_LISTENER, downloadChapter);
        Intent mainIntent = new Intent(this, MainBookFragment.class);
        PendingIntent mainPendingIntent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            mainPendingIntent = PendingIntent.getActivity(this, 0, mainIntent, PendingIntent.FLAG_IMMUTABLE);
        } else {
            mainPendingIntent = PendingIntent.getActivity(this, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        //创建 Notification.Builder 对象
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "40")
                .setSmallIcon(R.mipmap.ic_launcher)
                //点击通知后自动清除
                .setAutoCancel(true)
                .setContentTitle("正在下载：" + downloadChapter.bookName)
                .setContentText(downloadChapter.durChapterName)
                .setContentIntent(mainPendingIntent);
        //发送通知
        int notifyId = 19931118;
        notifyManager.notify(notifyId, builder.build());
    }

    private void finishDownload() {
        RxBus.get().post(RxBusTag.FINISH_DOWNLOAD_LISTENER, new Object());
        notifyManager.cancelAll();
        new Handler(Looper.getMainLooper()).post(() -> {
            Toast.makeText(getApplicationContext(), "全部离线章节下载完成", Toast.LENGTH_SHORT).show();
            stopService(new Intent(getApplication(), DownloadService.class));
        });
    }

    @Subscribe(thread = EventThread.MAIN_THREAD,
            tags = {
                    @Tag(RxBusTag.PAUSE_DOWNLOAD)
            }
    )
    public void pauseTask(Object o) {
        pauseDownload();
    }

    @Subscribe(thread = EventThread.MAIN_THREAD,
            tags = {
                    @Tag(RxBusTag.START_DOWNLOAD)
            }
    )
    public void startTask(Object o) {
        startDownload();
    }

    @Subscribe(thread = EventThread.MAIN_THREAD,
            tags = {
                    @Tag(RxBusTag.CANCEL_DOWNLOAD)
            }
    )
    public void cancelTask(Object o) {
        cancelDownload();
    }

    @Subscribe(thread = EventThread.MAIN_THREAD,
            tags = {
                    @Tag(RxBusTag.ADD_DOWNLOAD_TASK)
            }
    )
    public void addTask(DownloadChapterList newData) {
        addNewTask(newData.data);
    }
}