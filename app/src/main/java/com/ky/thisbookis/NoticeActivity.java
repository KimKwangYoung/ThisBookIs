package com.ky.thisbookis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;

import com.ky.thisbookis.data.Notice;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class NoticeActivity extends AppCompatActivity {

    private Context mContext;
    private RecyclerView noticeRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice);

        mContext = this;
        initData();

    }

    private void initData(){
        DatabaseReference noticeRef = FirebaseDatabase.getInstance().getReference()
                .child("notice");
        Query noticeQuery = noticeRef.orderByChild("time");

        noticeQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Notice> notices = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    notices.add(ds.getValue(Notice.class));
                }

                if(!notices.isEmpty()){
                    Collections.reverse(notices);
                    initView(notices);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initView(ArrayList<Notice> notices) {
        noticeRecyclerView = findViewById(R.id.notice_rv);

        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, RecyclerView.VERTICAL, false);
        noticeRecyclerView.setLayoutManager(layoutManager);
        NoticeAdapter adapter = new NoticeAdapter(mContext, notices);
        noticeRecyclerView.setAdapter(adapter);
    }
}
