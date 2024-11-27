package com.ebook.find.adapter;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.blankj.utilcode.util.ActivityUtils.startActivity;

import android.content.Context;
import android.content.Intent;

import androidx.databinding.ObservableArrayList;

import com.ebook.basebook.mvp.presenter.impl.BookDetailPresenterImpl;
import com.ebook.basebook.mvp.view.impl.BookDetailActivity;
import com.ebook.db.entity.LibraryKindBookList;
import com.ebook.db.entity.SearchBook;
import com.ebook.find.R;
import com.ebook.find.databinding.ViewLibraryKindbookBinding;
import com.ebook.find.mvp.view.impl.ChoiceBookActivity;
import com.xrn1997.common.adapter.BaseBindAdapter;
import com.xrn1997.common.util.ObservableListUtil;

import kotlin.Unit;


public class LibraryBookListAdapter extends BaseBindAdapter<LibraryKindBookList, ViewLibraryKindbookBinding> {

    public LibraryBookListAdapter(Context context, ObservableArrayList<LibraryKindBookList> items) {
        super(context, items);
    }

    @Override
    protected int getLayoutItemId(int viewType) {
        return R.layout.view_library_kindbook;
    }

    @Override
    protected void onBindItem(ViewLibraryKindbookBinding binding, LibraryKindBookList item, int position) {
        binding.setLibraryKindBookList(item);
        ObservableArrayList<SearchBook> searchBooks = new ObservableArrayList<>();
        searchBooks.addAll(item.books);
        LibraryBookAdapter libraryBookAdapter = new LibraryBookAdapter(context, searchBooks);

        searchBooks.addOnListChangedCallback(ObservableListUtil.getListChangedCallback(libraryBookAdapter));
        if (item.kindUrl.isEmpty()) {
            binding.tvMore.setVisibility(GONE);
            binding.tvMore.setOnClickListener(null);
        } else {
            binding.tvMore.setVisibility(VISIBLE);
            binding.tvMore.setOnClickListener(v -> ChoiceBookActivity.startChoiceBookActivity(context, item.kindName, item.kindUrl));
        }
        libraryBookAdapter.setOnItemClickListener((searchBook, position1) -> {
            Intent intent = new Intent(context, BookDetailActivity.class);
            intent.putExtra("from", BookDetailPresenterImpl.FROM_SEARCH);
            intent.putExtra("data", searchBook);
            startActivity(intent);
            return Unit.INSTANCE;
        });
        binding.rvBooklist.setAdapter(libraryBookAdapter);
    }
}
