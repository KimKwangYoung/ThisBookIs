package com.example.thisbookis;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;

import com.example.thisbookis.data.Report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;

public class MyReportsActivity extends AppCompatActivity implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {

    Context mContext;

    RecyclerView reportsRecyclerView;
    ImageView backButton, sortButton;

    LinkedHashMap<String, Report> reportMap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_reports);

        mContext = this;

        initData();
        initView();
    }

    private void initView() {
        reportsRecyclerView = findViewById(R.id.my_reports_rv);

        backButton = findViewById(R.id.my_reports_back_btn);
        sortButton = findViewById(R.id.my_reports_sort_btn);

        backButton.setOnClickListener(this);
        sortButton.setOnClickListener(this);

        sortByWriteTime(); // 기본 정렬 - 등록 순
    }

    private void initData() {
       reportMap = BaseApplication.userData.getReports();
    }

    private void setRecyclerView(ArrayList<Report> myReports){
        LinearLayoutManager manager = new LinearLayoutManager(mContext, RecyclerView.VERTICAL, false);
        reportsRecyclerView.setLayoutManager(manager);
        MyReportsAdapter searchAdapter = new MyReportsAdapter(mContext, myReports);
        reportsRecyclerView.setAdapter(searchAdapter);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.my_reports_back_btn:
                onBackPressed();
                break;
            case R.id.my_reports_sort_btn:
                PopupMenu popup = new PopupMenu(mContext, view);
                getMenuInflater().inflate(R.menu.filter_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(this);
                popup.show();
                break;
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.sort_by_add_time:
                sortByWriteTime();
                break;
            case R.id.sort_by_book_title:
                sortByBookTitle();
                break;
        }
        return false;
    }

    private void sortByWriteTime(){
        ArrayList<Report> myReportsArray = new ArrayList<>(reportMap.values());

        Collections.sort(myReportsArray, new Comparator<Report>() {
            @Override
            public int compare(Report report1, Report report2) {
                return -(report1.getWriteTime().compareTo(report2.getWriteTime()));
            }
        });

        setRecyclerView(myReportsArray);

    }

    private void sortByBookTitle(){
        ArrayList<Report> myReportsArray = new ArrayList<>(reportMap.values());

        Collections.sort(myReportsArray, new Comparator<Report>() {
            @Override
            public int compare(Report report1, Report report2) {
                return report1.getBookTitle().compareTo(report2.getBookTitle());
            }
        });

        setRecyclerView(myReportsArray);

    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
        initView();
    }
}
