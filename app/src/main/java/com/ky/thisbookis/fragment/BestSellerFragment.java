package com.ky.thisbookis.fragment;

import android.content.Context;
import android.os.Bundle;
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
import com.ky.thisbookis.BaseApplication;
import com.ky.thisbookis.R;
import com.ky.thisbookis.data.BestSeller;

import java.util.ArrayList;

public class BestSellerFragment extends Fragment {

    private Context mContext;
    private ArrayList<BestSeller.Item> bestSellers;

    public final static String TAG = "BestSellerFragment";

    private int index;

    private ImageView thumbnailImageView;
    private TextView bookTitleTextView, authorTextView, publisherTextView;

    BestSellerFragment(Context mContext, ArrayList<BestSeller.Item> bestSellers) {
        this.mContext = mContext;
        this.bestSellers = bestSellers;
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
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.best_seller_page, container, false);
        thumbnailImageView = layout.findViewById(R.id.best_seller_thumbnail_iv);
        bookTitleTextView = layout.findViewById(R.id.best_seller_book_title_tv);
        authorTextView = layout.findViewById(R.id.best_seller_book_author_tv);
        publisherTextView = layout.findViewById(R.id.best_seller_book_publisher_tv);
        BestSeller.Item bookItem = bestSellers.get(index);
        switch (index){
            case 0:
                setPage(bestSellers.get(0));
                break;
            case 1:
                setPage(bestSellers.get(1));
                break;
            case 2:
                setPage(bestSellers.get(2));
                break;
        }

        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BaseApplication.moveToBookContentActivity(mContext, bookItem.getIsbn(), bookItem.getTitle());
            }
        });
        return layout;
    }

    private void setPage(BestSeller.Item item){
        Glide.with(mContext).load(item.getThumbnail()).into(thumbnailImageView);
        bookTitleTextView.setText(item.getTitle());
        authorTextView.setText(item.getAuthor());
        publisherTextView.setText(item.getPublisher());


    }
}
