package com.ky.thisbookis;

import android.content.Context;
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
import java.util.ArrayList;

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
                    MyBook book = myBooksArray.get(getAdapterPosition());
                    String isbn = book.getIsbn();
                    isbn = isbn.substring(isbn.lastIndexOf(" ") + 1);
                    BaseApplication.moveToBookContentActivity(context, isbn, book.getTitle());
                    break;
            }
        }
    }
}
