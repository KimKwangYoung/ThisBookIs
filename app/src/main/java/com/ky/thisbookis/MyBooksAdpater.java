package com.ky.thisbookis;

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
import com.ky.thisbookis.data.MyBook;
import com.ky.thisbookis.data.SearchResult;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyBooksAdpater extends RecyclerView.Adapter<MyBooksAdpater.Viewholder> {

    Context context;
    private ArrayList<MyBook> myBooksArray;

    MyBooksAdpater(Context context, ArrayList<MyBook> myBooksArray) {
        this.context = context;
        this.myBooksArray = myBooksArray;
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
        MyBook myBook = myBooksArray.get(position);
        holder.setItem(myBook);
    }

    @Override
    public int getItemCount() {
        return myBooksArray.size();
    }

    public class Viewholder extends RecyclerView.ViewHolder implements View.OnClickListener{

        LinearLayout linearLayout;
        ImageView thumbnailImageView;
        TextView titleTextView, authorsTextView;

        private Viewholder(@NonNull View itemView) {
            super(itemView);

            linearLayout = itemView.findViewById(R.id.grid_item_ll);
            thumbnailImageView = itemView.findViewById(R.id.grid_item_thumbnail_iv);
            titleTextView = itemView.findViewById(R.id.grid_item_title_tv);
            authorsTextView = itemView.findViewById(R.id.grid_item_authors_tv);

            linearLayout.setOnClickListener(this);
        }

        public void setItem(MyBook myBook){
            Glide.with(context).load(myBook.getThumbnail()).error(R.drawable.ic_camera_alt_black_18dp).into(thumbnailImageView);

            titleTextView.setText(myBook.getTitle());
            String authors = BaseApplication.replaceString(myBook.getAuthors());
            authorsTextView.setText(authors);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.grid_item_ll:
                    searchBookDocument();
                    break;
            }

        }

        private void searchBookDocument() {
            String isbn = myBooksArray.get(getAdapterPosition()).getIsbn();
            String title = myBooksArray.get(getAdapterPosition()).getTitle();
            String keyword = isbn.substring(isbn.lastIndexOf(" ") + 1);
            String authorizationKey = context.getString(R.string.kakao_api_key);
            Call<SearchResult> call = KakaoApiClient.getInstance().searchService.getBookList(authorizationKey, keyword, 50, 1);
            Callback<SearchResult> callback = new Callback<SearchResult>() {
                @Override
                public void onResponse(Call<SearchResult> call, Response<SearchResult> response) {
                    if(response.isSuccessful()) {
                        SearchResult searchResult = response.body();
                        List<SearchResult.Document> documentList = searchResult.getDocuments();
                        SearchResult.Document bookDocument = null;
                        for(SearchResult.Document d : documentList){
                            if(d.getTitle().equals(title)){
                                bookDocument = d;
                                break;
                            }
                        }

                        Intent intent = new Intent(context, BookContentsActivity.class);
                        intent.putExtra("book", bookDocument);
                        context.startActivity(intent);

                    }else{
                        BaseApplication.showErrorToast(context, "책 정보를 읽어오지 못했습니다. 다시 시도하여 주세요");
                        Log.e("MyBooksAdapter", "Retrofit Error :: " + response.errorBody());
                    }

                }

                @Override
                public void onFailure(Call<SearchResult> call, Throwable t) {

                }
            };

            call.enqueue(callback);
        }
    }
}
