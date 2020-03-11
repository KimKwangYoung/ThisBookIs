package com.ky.thisbookis;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.ky.thisbookis.data.Notice;

public class NoticeContentActivity extends AppCompatActivity {

    private static final String TAG = "NoticeContentActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_content);

        Intent intent = getIntent();
        Notice notice = (Notice) intent.getSerializableExtra("notice");

        if(notice == null){
            Log.d(TAG, "공지사항 얻어오기 실패");
            return;
        }

        TextView titleTextView = findViewById(R.id.notice_content_title_tv);
        TextView timeTextView = findViewById(R.id.notice_content_time_tv);
        TextView contentTextView = findViewById(R.id.notice_content_content_tv);

        titleTextView.setText(notice.getTitle());
        timeTextView.setText(notice.getTime());
        contentTextView.setText(notice.getContent());

    }
}
