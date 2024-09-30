package com.ebook.find.mvp.view;

import android.widget.EditText;

import com.ebook.basebook.base.IView;
import com.ebook.db.entity.SearchBook;
import com.ebook.db.entity.SearchHistory;
import com.ebook.find.mvp.view.adapter.SearchBookAdapter;

import java.util.List;

public interface ISearchView extends IView {

    /**
     * 成功 新增查询记录
     *
     */
    void insertSearchHistorySuccess(SearchHistory searchHistory);

    /**
     * 成功搜索 搜索记录
     *
     */
    void querySearchHistorySuccess(List<SearchHistory> searchHistories);

    /**
     * 首次查询成功 更新UI
     *
     */
    void refreshSearchBook(List<SearchBook> books);

    /**
     * 加载更多书籍成功 更新UI
     *
     */
    void loadMoreSearchBook(List<SearchBook> books);

    /**
     * 刷新成功
     *
     */
    void refreshFinish(Boolean isAll);

    /**
     * 加载成功
     *
     */
    void loadMoreFinish(Boolean isAll);

    /**
     * 搜索失败
     *
     */
    void searchBookError(Boolean isRefresh);

    /**
     * 获取搜索内容EditText
     *
     */
    EditText getEdtContent();

    /**
     * 添加书籍失败
     *
     */
    void addBookShelfFailed(int code);

    SearchBookAdapter getSearchBookAdapter();

    void updateSearchItem(int index);

    /**
     * 判断书籍是否已经在书架上
     *
     */
    Boolean checkIsExist(SearchBook searchBook);
}
