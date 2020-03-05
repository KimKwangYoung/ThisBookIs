package com.example.thisbookis.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.thisbookis.BaseApplication;
import com.example.thisbookis.R;
import com.example.thisbookis.data.Book;

import java.util.ArrayList;

public class TopRankBookFragment extends Fragment {

    int index;
    Context mContext;
    ArrayList<Book> topRankBooks;
    ImageView thumbnailImageView;
    TextView titleTextView;
    TextView authorsTextView;
    TextView readUserCountTextView;

    public final static String TAG = "TopRankBookFragment";

    public TopRankBookFragment() {
    }

    public TopRankBookFragment(Context mContext, ArrayList<Book> topRankBooks) {
        this.mContext = mContext;
        this.topRankBooks = topRankBooks;
    }

    @Override
    public void setArguments(@Nullable Bundle args) {
        index = args.getInt("index");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.top_rank_page, container, false);
        thumbnailImageView = layout.findViewById(R.id.top_rank_book_page_thumbnail_iv);
        titleTextView = layout.findViewById(R.id.top_rank_book_page_book_title_tv);
        authorsTextView = layout.findViewById(R.id.top_rank_book_page_book_authors_tv);
        readUserCountTextView = layout.findViewById(R.id.top_rank_book_page_book_read_user_cnt_tv);

        switch (index){
            case 0:
                setItem(topRankBooks.get(0));
                Log.d(TAG, topRankBooks.get(0).getTitle());
                break;
            case 1:
                setItem(topRankBooks.get(1));
                Log.d(TAG, topRankBooks.get(1).getTitle());
                break;
            case 2:
                setItem(topRankBooks.get(2));
                Log.d(TAG, topRankBooks.get(2).getTitle());
                break;

        }
        return layout;
    }

    private void setItem(Book book){
        Glide.with(mContext).load(book.getThumbnail()).into(thumbnailImageView);
        titleTextView.setText(book.getTitle());
        authorsTextView.setText(BaseApplication.replaceString(book.getAuthors()));
        String s = book.getReadUserCount() + "명이 읽었어요!";
        readUserCountTextView.setText(s);
    }
}
