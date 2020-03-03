package com.example.thisbookis;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.thisbookis.data.RecentSearch;
import com.example.thisbookis.data.SearchResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

public class RecentSearchesAdapter extends RecyclerView.Adapter<RecentSearchesAdapter.Viewholder> {

    Context mContext;
    LinkedHashMap<String, RecentSearch> recentSearches;
    ArrayList<RecentSearch> recentSearchesList;

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

    class Viewholder extends RecyclerView.ViewHolder{

        ImageView bookThumbnailImageView;
        TextView bookTitleTextView, bookAuthorsTextView;

        public Viewholder(@NonNull View itemView) {
            super(itemView);

            bookThumbnailImageView = itemView.findViewById(R.id.grid_item_thumbnail_iv);
            bookTitleTextView = itemView.findViewById(R.id.grid_item_title_tv);
            bookAuthorsTextView = itemView.findViewById(R.id.grid_item_authors_tv);
        }

        private void setItem(RecentSearch document){
            Glide.with(mContext).load(document.getThumbnail()).into(bookThumbnailImageView);

            bookTitleTextView.setText(document.getTitle());
            String authors = document.getAuthors();
            bookAuthorsTextView.setText(BaseApplication.replaceString(authors));
        }
    }
}
