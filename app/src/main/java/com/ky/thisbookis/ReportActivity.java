package com.ky.thisbookis;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.ky.thisbookis.data.Comment;
import com.ky.thisbookis.data.Report;
import com.ky.thisbookis.data.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class ReportActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher {

    public final static String TAG = "ReportActivity";

    Context mContext;

    User me;
    User writer;
    Report reportData;

    String reportKey;
    String bookIsbn;
    String bookInfo;
    boolean shouldShare = false;
    boolean isLike;

    LinkedHashMap<String, Comment> commentMap;
    ArrayList<Comment> commentsList;
    Map<String, Boolean> likes;

    LinearLayout hideCommnetLinearLayout;
    TextView reportTitleTextView, reportContentsTextView, bookInfoTextView
            ,likeCntTextView, commentCntTextView, writerNicknameTextView, reportWriteTimeTextView
            ,commentButton;
    EditText commentEditText;
    ImageView writerProfileImageView;

    RecyclerView commentRecyclerView;
    ImageView likeButton;

    DatabaseReference reportRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        mContext = this;

        Intent intent = getIntent();
        reportData = (Report) intent.getSerializableExtra("report");


        if(reportData != null){
           shouldShare = reportData.isShouldShare();
           reportKey = reportData.getReportKey();
           String authors = BaseApplication.replaceString(reportData.getBookAuthors());
           bookInfo = reportData.getBookTitle() + "/" + authors;
           bookIsbn = reportData.getBookISBN();
        }else{
            BaseApplication.showDialog(mContext, "예기치 못한 오류가 발생하였습니다. \n다시 시도하여 주세요", null);
            Log.e(TAG, "onCreate() : 독후감 객체 넘어오지 않음");
            finish();
            return;
        }

        if(shouldShare){
            getReportData();
        }else{
            getWriterData();
        }

    }

    private void initData(){
        if(reportData.getLike() == null){
            likes = new HashMap<>();
        }else{
            likes = reportData.getLike();
        }

        if(reportData.getComments() == null){
            commentMap = new LinkedHashMap<>();
        }else{
            commentMap = reportData.getComments();
        }

        me = BaseApplication.userData;

        reportRef = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.firebase_book_data_key)).child(reportData.getBookISBN())
                .child("reportsOfBook").child(reportData.getReportKey());

        initView();

    }

    private void initView(){

        reportTitleTextView = findViewById(R.id.report_title_tv);
        reportContentsTextView = findViewById(R.id.report_contents_tv);
        bookInfoTextView = findViewById(R.id.report_book_info_tv);
        likeCntTextView = findViewById(R.id.report_like_cnt_tv);
        commentCntTextView = findViewById(R.id.report_comment_cnt_tv);
        commentRecyclerView = findViewById(R.id.report_comment_rv);
        likeButton = findViewById(R.id.report_like_btn);
        commentEditText = findViewById(R.id.report_comment_et);
        hideCommnetLinearLayout = findViewById(R.id.report_comment_hide_ll);
        writerProfileImageView = findViewById(R.id.report_writer_profile_image_iv);
        writerNicknameTextView = findViewById(R.id.report_writer_nickname_tv);
        reportWriteTimeTextView = findViewById(R.id.report_write_time_tv);
        commentButton = findViewById(R.id.report_comment_input_btn);


        reportTitleTextView.setText(reportData.getTitle());
        reportContentsTextView.setText(reportData.getContents());
        bookInfoTextView.setText(bookInfo);
        commentEditText.addTextChangedListener(this);

        String likeCnt, commentCnt;

        if (reportData.getComments() != null){
            commentCnt = Integer.toString(reportData.getComments().size());
        }else{
            commentCnt = "0";
        }

        likeCnt = Integer.toString(reportData.getLikeCount());
        likeCntTextView.setText(likeCnt);

        commentCntTextView.setText(commentCnt);

        if(!shouldShare){
            hideCommnetLinearLayout.setVisibility(View.VISIBLE);
            commentEditText.setVisibility(View.GONE);
        }

        /* 글쓴이 프로필 부분 */
        RequestOptions options = BaseApplication.profileImageOptions;
        Glide.with(mContext).load(writer.getProfileURL()).apply(options).into(writerProfileImageView);
        writerNicknameTextView.setText(writer.getNickname());
        reportWriteTimeTextView.setText(reportData.getWriteTime());

        if(likes.containsKey(me.getUserId())){
            likeButton.setImageDrawable(getDrawable(R.drawable.ic_like_true));
        }

        likeButton.setOnClickListener(this);
        commentButton.setOnClickListener(this);

        getCommentUserList();

    }

    private void getReportData(){
        DatabaseReference reportRef = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.firebase_book_data_key)).child(bookIsbn)
                .child("reportsOfBook").child(reportData.getReportKey());

        reportRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                reportData = dataSnapshot.getValue(Report.class);
                Log.d(TAG, "getReportData() : 독후감 정보 얻어오기 성공");
                getWriterData();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "getReportData() : 독후감 정보 얻어오기 실패 " + databaseError.getMessage());
            }
        });
    }

    private void getWriterData(){
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.firebase_user_data_key)).child(reportData.getWriter());

        try {
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    writer = dataSnapshot.getValue(User.class);
                    Log.d(TAG, "getWriterData() : 글쓴이 정보 얻어오기 성공");
                    initData();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "getWriteData() :  글쓴이 정보 얻어오기 실패 " + databaseError.getMessage());
                }
            });
        }catch (Exception e){
            Log.e(TAG, "getWriteData() :  글쓴이 정보 얻어오기 실패 ");
            String anonymousUserNickname = "글쓴이 미상";
            writer.setNickname(anonymousUserNickname);
            initView();
        }
    }

    private void like(){

        if(!shouldShare){
            BaseApplication.showInfoToast(mContext, "독후감이 비공개인 상태에서는 좋아요를 누를 수 없습니다.");
            return;
        }

        User me = BaseApplication.userData;

        reportRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                Report r = mutableData.getValue(Report.class);
                if(r == null){
                    return Transaction.success(mutableData);
                }

                Log.d(TAG, "doTransaction");
                if (likes.containsKey(me.getUserId())){
                    r.setLikeCount(r.getLikeCount()-1);
                    likes.remove(me.getUserId());
                    r.setLike(likes);
                } else {
                    r.setLikeCount(r.getLikeCount()+1);
                    likes.put(me.getUserId(), true);
                    r.setLike(likes);
                }

                reportData = r;
                mutableData.setValue(r);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                if(likes.containsKey(me.getUserId())){
                    likeButton.setImageResource(R.drawable.ic_like_true);
                }else{
                    likeButton.setImageResource(R.drawable.ic_like_false);
                }
                String likeCnt = Integer.toString(reportData.getLikeCount());
                likeCntTextView.setText(likeCnt);

                Log.d(TAG, "like() : " + databaseError);
            }
        });


    }

    private void comment() {
        String commentContent = commentEditText.getText().toString();

        if (commentContent.equals("")) {
            BaseApplication.showInfoToast(mContext, "댓글 내용이 없습니다.");
            return;
        }

        Comment comment = new Comment();
        comment.setContent(commentContent);
        comment.setWriter(me.getUserId());

        Date todayTime = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd H:mm:ss");
        comment.setTimeAddComments(sdf.format(todayTime));


        DatabaseReference commentRef = reportRef.child("comments");
        String commentKey = commentRef.push().getKey();

        commentRef.child(commentKey).setValue(comment).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                commentMap.put(commentKey, comment);
                Log.d(TAG, "댓글 적용 완료");
                commentEditText.setText(null);
                commentButton.setVisibility(View.GONE);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(commentEditText.getWindowToken(), 0);
                commentCntTextView.setText(String.format(Locale.getDefault(),"%d", commentMap.size()));
                getCommentUserList();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "댓글 적용 실패" + e.getMessage());
            }
        });
    }

    private void getCommentUserList(){
        commentsList = new ArrayList<>(commentMap.values());
        Collections.sort(commentsList, new Comparator<Comment>() {
            @Override
            public int compare(Comment current, Comment after) {
                return -(current.getTimeAddComments().compareTo(after.getTimeAddComments()));
            }
        });
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_user_data_key));

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, User> users = new HashMap<>();
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    for(Comment c : commentsList){
                        if(ds.getKey().equals(c.getWriter())){
                            if(!users.containsKey(ds.getKey())){
                                users.put(c.getWriter(), ds.getValue(User.class));
                            }
                            Log.d(TAG,"checkUser : " + ds.getValue(User.class).getNickname());
                        }
                    }
                }//End for
                setCommentRecyclerView(users);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setCommentRecyclerView(Map<String, User> users) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, RecyclerView.VERTICAL, false);
        commentRecyclerView.setLayoutManager(layoutManager);
        CommentAdapter adapter = new CommentAdapter(mContext, commentsList, users);
        commentRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.report_like_btn:
                like();
                break;
            case R.id.report_comment_input_btn:
                comment();
                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        commentButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }
}
