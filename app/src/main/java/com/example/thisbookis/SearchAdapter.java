package com.example.thisbookis;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.example.thisbookis.data.SearchResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

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
            if(authors != null && !authors.equals("")) {
                authorTextView.setText(BaseApplication.replaceString(authors));
            }else{
                authorTextView.setText("-");
            }


            if(data.getPublisher() != null && !data.getPublisher().equals("")) {
                publisherTextView.setText(data.getPublisher());
            }else{
                publisherTextView.setText(" - ");
            }

            priceTextView.setText(Integer.toString(data.getPrice()));

        }



        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.search_result_item_ll:
                    Intent intent = new Intent(mContext, BookContentsActivity.class);
                    intent.putExtra("book", results.get(getAdapterPosition()));
                    mContext.startActivity(intent);
                    ((Activity)mContext).overridePendingTransition(R.anim.anim_slide_in_bottom, R.anim.anim_slide_stay);
            }
        }
    }
}
