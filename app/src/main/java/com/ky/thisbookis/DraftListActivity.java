package com.ky.thisbookis;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;

import com.ky.thisbookis.data.Draft;
import com.ky.thisbookis.data.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;

public class DraftListActivity extends AppCompatActivity {

    Context mContext;

    RecyclerView draftRecyclerView;


    ArrayList<Draft> draftsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draft_list);

        mContext = this;

        initData();
        initView();
    }

    private void initData(){
        User user = BaseApplication.userData;
        LinkedHashMap<String, Draft> myDrafts;
        if(user.getTemporaryStorages() != null){
            myDrafts = user.getTemporaryStorages();

            draftsList = new ArrayList<>(myDrafts.values());
            Collections.sort(draftsList, new Comparator<Draft>() {
                @Override
                public int compare(Draft current, Draft after) {
                    return -(current.getSaveTime().compareTo(after.getSaveTime()));
                }
            });
        }else{
            return;
        }

    }

    private void initView() {
        draftRecyclerView = findViewById(R.id.draft_list_rv);

        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, RecyclerView.VERTICAL, false);
        draftRecyclerView.setLayoutManager(layoutManager);
        DraftListAdapter draftListAdapter = new DraftListAdapter(mContext, draftsList);
        draftRecyclerView.setAdapter(draftListAdapter);

    }

    @Override
    protected void onResume() {
        super.onResume();
        draftsList.clear();
        initData();
        initView();
    }
}
