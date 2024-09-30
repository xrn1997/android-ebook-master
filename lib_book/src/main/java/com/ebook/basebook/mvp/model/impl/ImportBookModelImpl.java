package com.ebook.basebook.mvp.model.impl;


import com.ebook.basebook.base.impl.MBaseModelImpl;
import com.ebook.basebook.mvp.model.ImportBookModel;
import com.ebook.db.ObjectBoxManager;
import com.ebook.db.entity.BookContent;
import com.ebook.db.entity.BookInfo;
import com.ebook.db.entity.BookShelf;
import com.ebook.db.entity.BookShelf_;
import com.ebook.db.entity.ChapterList;
import com.ebook.db.entity.ChapterList_;
import com.ebook.db.entity.LocBookShelf;

import org.mozilla.universalchardet.UniversalDetector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.objectbox.Box;
import io.objectbox.query.Query;
import io.reactivex.rxjava3.core.Observable;


public class ImportBookModelImpl extends MBaseModelImpl implements ImportBookModel {
    private volatile static ImportBookModelImpl importBookModel;

    public static ImportBookModelImpl getInstance() {
        if (importBookModel == null) {
            synchronized (ImportBookModelImpl.class) {
                if (importBookModel == null) {
                    importBookModel = new ImportBookModelImpl();
                }
            }
        }
        return importBookModel;
    }

    @Override
    public Observable<LocBookShelf> importBook(final File book) {
        return Observable.create(e -> {
            MessageDigest md = MessageDigest.getInstance("MD5");
            FileInputStream in = new FileInputStream(book);
            byte[] buffer = new byte[2048];
            int len;
            while ((len = in.read(buffer, 0, 2048)) != -1) {
                md.update(buffer, 0, len);
            }
            in.close();

            String md5 = new BigInteger(1, md.digest()).toString(16);
            Box<BookShelf> bookShelfBox = ObjectBoxManager.INSTANCE.getBookShelfBox();
            BookShelf bookShelf;
            List<BookShelf> temp;
            try (Query<BookShelf> query = bookShelfBox.query(BookShelf_.noteUrl.equal(md5)).build()) {
                temp = query.find();
            }
            boolean isNew = true;
            if (!temp.isEmpty()) {
                isNew = false;
                bookShelf = temp.get(0);
            } else {
                bookShelf = new BookShelf();
                bookShelf.finalDate = System.currentTimeMillis();
                bookShelf.durChapter = 0;
                bookShelf.durChapterPage = 0;
                bookShelf.setTag(BookShelf.LOCAL_TAG);
                bookShelf.noteUrl = md5;

                BookInfo bookInfo = new BookInfo();
                bookInfo.setAuthor("佚名");
                bookInfo.setName(book.getName().replace(".txt", "").replace(".TXT", ""));
                bookInfo.finalRefreshData = System.currentTimeMillis();
                bookInfo.setCoverUrl("");
                bookInfo.setNoteUrl(md5);
                bookInfo.setTag(BookShelf.LOCAL_TAG);
                bookShelf.bookInfo.setTarget(bookInfo);
                //保存章节
                saveChapter(book, md5);
                try (Query<ChapterList> query = ObjectBoxManager.INSTANCE
                        .getChapterListBox()
                        .query(ChapterList_.noteUrl.equal(bookShelf.noteUrl))
                        .order(ChapterList_.durChapterIndex)
                        .build()) {
                    for (ChapterList chapterList : query.find()) {
                        bookShelf.getBookInfo().getTarget().chapterlist.add(chapterList);
                    }
                }
                bookShelfBox.put(bookShelf);
            }
            e.onNext(new LocBookShelf(isNew, bookShelf));
            e.onComplete();
        });
    }

    @SuppressWarnings("unused")
    private Boolean isAdded(BookShelf temp, List<BookShelf> shelf) {
        if (shelf == null || shelf.isEmpty()) {
            return false;
        } else {
            int a = 0;
            for (int i = 0; i < shelf.size(); i++) {
                if (Objects.equals(temp.noteUrl, shelf.get(i).noteUrl)) {
                    break;
                } else {
                    a++;
                }
            }
            return a != shelf.size();
        }
    }

    private void saveChapter(File book, String md5) throws IOException {
        String regex = "第.{1,7}章.*";

        String encoding;

        FileInputStream fis = new FileInputStream(book);
        byte[] buf = new byte[4096];
        UniversalDetector detector = new UniversalDetector(null);
        int nRead;
        while ((nRead = fis.read(buf)) > 0 && !detector.isDone()) {
            detector.handleData(buf, 0, nRead);
        }
        detector.dataEnd();
        encoding = detector.getDetectedCharset();
        if (encoding == null || encoding.isEmpty())
            encoding = "utf-8";
        fis.close();

        int chapterPageIndex = 0;
        String title = null;
        StringBuilder contentBuilder = new StringBuilder();
        fis = new FileInputStream(book);
        InputStreamReader inputReader = new InputStreamReader(fis, encoding);
        BufferedReader buffReader = new BufferedReader(inputReader);
        String line;
        while ((line = buffReader.readLine()) != null) {
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(line);
            if (m.find()) {
                String temp = line.trim().substring(0, line.trim().indexOf("第"));
                if (!temp.trim().isEmpty()) {
                    contentBuilder.append(temp);
                }
                if (!contentBuilder.toString().isEmpty()) {
                    if (!contentBuilder.toString().replaceAll(" ", "").replaceAll("\\s*", "").trim().isEmpty()) {
                        saveDurChapterContent(md5, chapterPageIndex, title, contentBuilder.toString());
                        chapterPageIndex++;
                    }
                    contentBuilder.delete(0, contentBuilder.length());
                }
                title = line.trim().substring(line.trim().indexOf("第"));
            } else {
                String temp = line.trim().replaceAll(" ", "").replaceAll(" ", "").replaceAll("\\s*", "");
                if (temp.isEmpty()) {
                    if (contentBuilder.length() > 0) {
                        contentBuilder.append("\r\n\u3000\u3000");
                    } else {
                        contentBuilder.append("\r\u3000\u3000");
                    }
                } else {
                    contentBuilder.append(temp);
                    if (title == null) {
                        title = line.trim();
                    }
                }
            }
        }
        if (contentBuilder.length() > 0) {
            saveDurChapterContent(md5, chapterPageIndex, title, contentBuilder.toString());
            contentBuilder.delete(0, contentBuilder.length());
        }
        buffReader.close();
        inputReader.close();
        fis.close();
    }

    private void saveDurChapterContent(String md5, int chapterPageIndex, String name, String content) {
        ChapterList chapterList = new ChapterList();
        chapterList.setNoteUrl(md5);
        chapterList.durChapterIndex = chapterPageIndex;
        chapterList.setTag(BookShelf.LOCAL_TAG);
        chapterList.setDurChapterUrl(md5 + "_" + chapterPageIndex);
        chapterList.setDurChapterName(name);

        BookContent bookContent = new BookContent();
        bookContent.durChapterUrl = chapterList.getDurChapterUrl();
        bookContent.tag = BookShelf.LOCAL_TAG;
        bookContent.durChapterIndex = chapterList.durChapterIndex;
        bookContent.durChapterContent = content;
        chapterList.bookContent.setTarget(bookContent);
        ObjectBoxManager.INSTANCE.getChapterListBox().put(chapterList);
    }
}
