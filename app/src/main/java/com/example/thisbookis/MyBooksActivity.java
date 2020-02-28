package com.example.thisbookis;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;

import com.example.thisbookis.data.MyBook;
import com.example.thisbookis.data.Report;
import com.example.thisbookis.data.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;

public class MyBooksActivity extends AppCompatActivity implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {

    Context mContext;

    RecyclerView recyclerView;
    ImageView backButton, sortButton;

    LinkedHashMap<String, MyBook> myBooks;
    User userData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_books);

        mContext = this;

        initData();
        initView();
    }

    private void initData() {
        userData = BaseApplication.userData;
        myBooks = userData.getMyBooks();
    }

    private void initView() {
        recyclerView = findViewById(R.id.my_books_rv);

        sortButton = findViewById(R.id.my_books_sort_btn);
        backButton = findViewById(R.id.my_books_back_btn);

        sortButton.setOnClickListener(this);
        backButton.setOnClickListener(this);

        sortByRestrationTime(); // 기본 정렬 방식 - 등록 순
    }

    private void setRecyclerView(ArrayList<MyBook> myBooksArray){
        GridLayoutManager layoutManager = new GridLayoutManager(mContext, 3);
        recyclerView.setLayoutManager(layoutManager);

        MyBooksAdpater adpater = new MyBooksAdpater(mContext, myBooksArray);
        recyclerView.setAdapter(adpater);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.my_books_sort_btn:
                PopupMenu popup = new PopupMenu(mContext, view);
                getMenuInflater().inflate(R.menu.filter_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(this);
                popup.show();
                break;
            case R.id.my_books_back_btn:
                onBackPressed();
                break;
        }
    }


    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.sort_by_add_time:
                sortByRestrationTime();
                break;
            case R.id.sort_by_book_title:
                sortByBookTitle();
                break;
        }
        return false;
    }

    private void sortByRestrationTime(){
        ArrayList<MyBook> myBooksArray = new ArrayList<>(myBooks.values());

        Collections.sort(myBooksArray, new Comparator<MyBook>() {
            @Override
            public int compare(MyBook myBook1, MyBook myBook2) {
                return -(myBook1.getRegistrationTime().compareTo(myBook2.getRegistrationTime()));
            }
        });

        setRecyclerView(myBooksArray);

    }

    private void sortByBookTitle(){
        ArrayList<MyBook> myBooksArray = new ArrayList<>(myBooks.values());

        Collections.sort(myBooksArray, new Comparator<MyBook>() {
            @Override
            public int compare(MyBook myBook1, MyBook myBook2) {
                return myBook1.getTitle().compareTo(myBook2.getTitle());
            }
        });

        setRecyclerView(myBooksArray);

    }

}
