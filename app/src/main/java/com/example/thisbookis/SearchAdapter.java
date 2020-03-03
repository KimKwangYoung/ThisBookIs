package com.example.thisbookis;

import android.app.Activity;
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
import com.example.thisbookis.data.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    static final String TAG = "SearchAdapter";
    Context mContext;
    List<SearchResult.Document> results;

    public SearchAdapter(Context mContext, List<SearchResult.Document> results) {
        this.mContext = mContext;
        this.results = results;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.search_result_item, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SearchResult.Document document = results.get(position);
        holder.setItem(document);
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        LinearLayout itemLinearLayout;
        ImageView thumnailImageView;
        TextView titleTextView, authorTextView, publisherTextView, priceTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            itemLinearLayout = itemView.findViewById(R.id.search_result_item_ll);
            thumnailImageView = itemView.findViewById(R.id.search_result_thumnail_iv);
            titleTextView = itemView.findViewById(R.id.search_result_title_tv);
            authorTextView = itemView.findViewById(R.id.search_result_author_tv);
            publisherTextView = itemView.findViewById(R.id.search_result_publisher_tv);
            priceTextView = itemView.findViewById(R.id.search_result_price_tv);

            itemLinearLayout.setOnClickListener(this);
        }

        public void setItem(SearchResult.Document data){
            Glide.with(mContext).load(data.getThumbnail()).error(R.drawable.ic_book)
                    .override(300, 400)
                    .into(thumnailImageView);
            titleTextView.setText(data.getTitle());
            String authors = Arrays.deepToString(data.getAuthors());
            if(!authors.equals("")) {
                authorTextView.setText(BaseApplication.replaceString(authors));
            }else{
                authorTextView.setText("-");
            }


            if(data.getPublisher() != null && !data.getPublisher().equals("")) {
                publisherTextView.setText(data.getPublisher());
            }else{
                publisherTextView.setText(" - ");
            }
            String price = String.format(Locale.getDefault(),"%d원", data.getPrice());
            priceTextView.setText(price);

        }

        private void addRecentSearch(){
            LinkedHashMap<String, RecentSearch> recentSearches;
            User user = BaseApplication.userData;
            SearchResult.Document document = results.get(getAdapterPosition());
            if(user.getRecentSearches() == null){
                recentSearches = new LinkedHashMap<>();
            }else{
                recentSearches = user.getRecentSearches();
            }

            Log.d(TAG, recentSearches.size() + "개");

            if(recentSearches.size() >= 10){
                ArrayList<RecentSearch> list = new ArrayList<>(recentSearches.values());
                Collections.sort(list, new Comparator<RecentSearch>() {
                    @Override
                    public int compare(RecentSearch current, RecentSearch after) {
                        return -(current.getAddTime().compareTo(after.getAddTime()));
                    }
                });

                list.remove(list.size()-1);
                recentSearches.clear();
                for(RecentSearch r : list){
                    recentSearches.put(r.getISBN(), r);
                }
                Log.d(TAG, "실행 확인");
            }

            Date now = new Date();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            String isbn = document.getIsbn();
            String title = document.getTitle();
            String authors = Arrays.deepToString(document.getAuthors());
            String thumbnail = document.getThumbnail();
            String addTime = simpleDateFormat.format(now);

            RecentSearch recentSearch = new RecentSearch(isbn, title, authors, addTime, thumbnail);

            recentSearches.put(document.getIsbn(), recentSearch);

            DatabaseReference recentSearchRef = FirebaseDatabase.getInstance().getReference()
                    .child(mContext.getString(R.string.firebase_user_data_key)).child(user.getUserId())
                    .child("recentSearches");

            recentSearchRef.setValue(recentSearches).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    BaseApplication.userData.setRecentSearches(recentSearches);
                    Log.d(TAG, "addRecentSearch() : 최근 검색 추가 완료");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "addRecentSearch() : 최근 검색 추가 실패 " + e.getMessage());
                }
            });

        }


        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.search_result_item_ll:
                    addRecentSearch();
                    Intent intent = new Intent(mContext, BookContentsActivity.class);
                    intent.putExtra("book", results.get(getAdapterPosition()));
                    mContext.startActivity(intent);
                    ((Activity)mContext).overridePendingTransition(R.anim.anim_slide_in_bottom, R.anim.anim_slide_stay);
            }
        }
    }
}
