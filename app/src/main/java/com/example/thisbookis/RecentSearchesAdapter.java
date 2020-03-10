package com.example.thisbookis;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.thisbookis.data.RecentSearch;
import com.example.thisbookis.data.SearchResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecentSearchesAdapter extends RecyclerView.Adapter<RecentSearchesAdapter.Viewholder> {

    static final String TAG = "RecentSearchesAdapter";

    Context mContext;
    private LinkedHashMap<String, RecentSearch> recentSearches;
    private ArrayList<RecentSearch> recentSearchesList;

    public RecentSearchesAdapter(Context mContext, LinkedHashMap<String, RecentSearch> recentSearches) {
        this.mContext = mContext;
        this.recentSearches = recentSearches;

        recentSearchesList = new ArrayList<>(recentSearches.values());
        Collections.sort(recentSearchesList, new Comparator<RecentSearch>() {
            @Override
            public int compare(RecentSearch current, RecentSearch after) {
                return -(current.getAddTime().compareTo(after.getAddTime()));
            }
        });
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.my_books_item, parent, false);
        return new Viewholder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {
        RecentSearch document = recentSearchesList.get(position);
        holder.setItem(document);
    }

    @Override
    public int getItemCount() {
        return recentSearches.size();
    }

    class Viewholder extends RecyclerView.ViewHolder implements View.OnClickListener{

        LinearLayout linearLayout;
        ImageView bookThumbnailImageView;
        TextView bookTitleTextView, bookAuthorsTextView;

        public Viewholder(@NonNull View itemView) {
            super(itemView);

            bookThumbnailImageView = itemView.findViewById(R.id.grid_item_thumbnail_iv);
            bookTitleTextView = itemView.findViewById(R.id.grid_item_title_tv);
            bookAuthorsTextView = itemView.findViewById(R.id.grid_item_authors_tv);
            linearLayout = itemView.findViewById(R.id.grid_item_ll);

            linearLayout.setOnClickListener(this);
        }

        private void setItem(RecentSearch document){
            Glide.with(mContext).load(document.getThumbnail()).into(bookThumbnailImageView);

            bookTitleTextView.setText(document.getTitle());
            String authors = document.getAuthors();
            bookAuthorsTextView.setText(BaseApplication.replaceString(authors));
        }

        private void getBookData(){
            RecentSearch r = recentSearchesList.get(getAdapterPosition());
            String isbn = r.getISBN();
            String title = r.getTitle();
            isbn = isbn.substring(isbn.lastIndexOf(" ") + 1);
            BaseApplication.moveToBookContentActivity(mContext, isbn, title);
        }

        @Override
        public void onClick(View view) {
            if(view.getId() == R.id.grid_item_ll){
                getBookData();
            }
        }
    }
}
