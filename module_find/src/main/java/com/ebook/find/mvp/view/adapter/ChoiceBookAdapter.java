package com.ebook.find.mvp.view.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ebook.basebook.view.refreshview.RefreshRecyclerViewAdapter;
import com.ebook.db.entity.BookShelf;
import com.ebook.db.entity.SearchBook;
import com.ebook.find.R;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ChoiceBookAdapter extends RefreshRecyclerViewAdapter {
    private final List<SearchBook> searchBooks;
    private final Context context;
    private OnItemClickListener itemClickListener;

    public ChoiceBookAdapter(Context context) {
        super(true);
        searchBooks = new ArrayList<>();
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateRefreshRecyclerViewHolder(ViewGroup parent, int viewType) {
        return new Viewholder(LayoutInflater.from(context)
                .inflate(R.layout.adapter_searchbook_item, parent, false));
    }

    @Override
    public void onBindViewholder(final RecyclerView.ViewHolder holder, final int position) {
        BookShelf bookShelf = new BookShelf();
        // Log.d("解析结果", searchBooks.get(position).getCoverUrl());
        bookShelf.noteUrl = searchBooks.get(position).noteUrl;
        Glide.with(((Viewholder) holder).ivCover.getContext())
                .load(searchBooks.get(position).coverUrl)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .fitCenter()
                .dontAnimate()
                .placeholder(com.ebook.basebook.R.drawable.img_cover_default)
                .into(((Viewholder) holder).ivCover);
        ((Viewholder) holder).tvName.setText(searchBooks.get(position).name);
        ((Viewholder) holder).tvAuthor.setText(searchBooks.get(position).author);
        String state = searchBooks.get(position).state;
        if (state.isEmpty()) {
            ((Viewholder) holder).tvState.setVisibility(View.GONE);
        } else {
            ((Viewholder) holder).tvState.setVisibility(View.VISIBLE);
            ((Viewholder) holder).tvState.setText(state);
        }
        long words = searchBooks.get(position).words;
        if (words <= 0) {
            ((Viewholder) holder).tvWords.setVisibility(View.GONE);
        } else {
            String wordsS = words + "字";
            if (words > 10000) {
                DecimalFormat df = new DecimalFormat("#.#");
                wordsS = df.format(words * 1.0f / 10000f) + "万字";
            }
            ((Viewholder) holder).tvWords.setVisibility(View.VISIBLE);
            ((Viewholder) holder).tvWords.setText(wordsS);
        }
        String kind = searchBooks.get(position).kind;
        if (kind.isEmpty()) {
            ((Viewholder) holder).tvKind.setVisibility(View.GONE);
        } else {
            ((Viewholder) holder).tvKind.setVisibility(View.VISIBLE);
            ((Viewholder) holder).tvKind.setText(kind);
        }
        if (!searchBooks.get(position).lastChapter.isEmpty())
            ((Viewholder) holder).tvLatest.setText(searchBooks.get(position).lastChapter);
        else if (!searchBooks.get(position).desc.isEmpty()) {
            ((Viewholder) holder).tvLatest.setText(searchBooks.get(position).desc);
        } else
            ((Viewholder) holder).tvLatest.setText("");
        if (!searchBooks.get(position).origin.isEmpty()) {
            ((Viewholder) holder).tvOrigin.setVisibility(View.VISIBLE);
            ((Viewholder) holder).tvOrigin.setText("来源:" + searchBooks.get(position).origin);
        } else {
            ((Viewholder) holder).tvOrigin.setVisibility(View.GONE);
        }
        if (searchBooks.get(position).getAdd()) {
            ((Viewholder) holder).tvAddShelf.setText("已添加");
            ((Viewholder) holder).tvAddShelf.setEnabled(false);
        } else {
            ((Viewholder) holder).tvAddShelf.setText("+添加");
            ((Viewholder) holder).tvAddShelf.setEnabled(true);
        }

        ((Viewholder) holder).flContent.setOnClickListener(v -> {
            if (itemClickListener != null)
                itemClickListener.clickItem(((Viewholder) holder).ivCover, position, searchBooks.get(position));
        });
        ((Viewholder) holder).tvAddShelf.setOnClickListener(v -> {
            if (itemClickListener != null)
                itemClickListener.clickAddShelf(((Viewholder) holder).tvAddShelf, position, searchBooks.get(position));
        });
    }

    @Override
    public int getItemViewtype(int position) {
        return 0;
    }

    @Override
    public int getItemcount() {
        return searchBooks.size();
    }

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void addAll(List<SearchBook> newData) {
        if (newData != null && !newData.isEmpty()) {
            int position = getItemcount();
            if (!newData.isEmpty()) {
                searchBooks.addAll(newData);
            }
            notifyItemInserted(position);
            notifyItemRangeChanged(position, newData.size());
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void replaceAll(List<SearchBook> newData) {
        searchBooks.clear();
        if (newData != null && !newData.isEmpty()) {
            searchBooks.addAll(newData);
        }
        notifyDataSetChanged();
    }

    public List<SearchBook> getSearchBooks() {
        return searchBooks;
    }

    public interface OnItemClickListener {
        void clickAddShelf(View clickView, int position, SearchBook searchBook);

        void clickItem(View animView, int position, SearchBook searchBook);
    }

    static class Viewholder extends RecyclerView.ViewHolder {
        final FrameLayout flContent;
        final ImageView ivCover;
        final TextView tvName;
        final TextView tvAuthor;
        final TextView tvState;
        final TextView tvWords;
        final TextView tvKind;
        final TextView tvLatest;
        final TextView tvAddShelf;
        final TextView tvOrigin;

        public Viewholder(View itemView) {
            super(itemView);
            flContent = itemView.findViewById(R.id.fl_content);
            ivCover = itemView.findViewById(R.id.iv_cover);
            tvName = itemView.findViewById(R.id.tv_name);
            tvAuthor = itemView.findViewById(R.id.tv_author);
            tvState = itemView.findViewById(R.id.tv_state);
            tvWords = itemView.findViewById(R.id.tv_words);
            tvLatest = itemView.findViewById(R.id.tv_lastest);
            tvAddShelf = itemView.findViewById(R.id.tv_addshelf);
            tvKind = itemView.findViewById(R.id.tv_kind);
            tvOrigin = itemView.findViewById(R.id.tv_origin);
        }
    }
}