package com.example.thisbookis;

import androidx.annotation.NonNull;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.thisbookis.data.Book;
import com.example.thisbookis.data.Draft;
import com.example.thisbookis.data.Report;
import com.example.thisbookis.data.SearchResult;
import com.example.thisbookis.data.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class WriteReportActivity extends BaseActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    public final static String TAG = "WriteReportActivity";

    Context mContext;

    EditText titleEditText, contentsEditText;
    Switch shouldShareSwitch;
    TextView bookTitleTextView, saveDraftButton;
    ImageView backButton;
    Button saveButton;

    SearchResult.Document apiBook;

    boolean isShared;
    boolean shouldShare;
    boolean isModifying;
    boolean isDraft;
    User user;
    Report report;
    String reportTitle;
    String reportContents;

    Draft draft;
    Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_report);

        mContext = this;
        mHandler = new Handler();

        initData();
        initView();

    }

    private void initView() {
        titleEditText = findViewById(R.id.write_report_title_et);
        contentsEditText = findViewById(R.id.write_report_contents_et);
        shouldShareSwitch = findViewById(R.id.write_report_shoul_share_sw);
        bookTitleTextView = findViewById(R.id.write_report_book_title_tv);
        backButton = findViewById(R.id.write_report_back_btn);
        saveButton = findViewById(R.id.write_report_save_btn);
        saveDraftButton = findViewById(R.id.write_report_save_draft_btn);

        if(isModifying){
            bookTitleTextView.setText(report.getBookTitle());
            titleEditText.setText(report.getTitle());
            contentsEditText.setText(report.getContents());
            shouldShareSwitch.setChecked(report.isShouldShare());
        }else if(isDraft){
            bookTitleTextView.setText(apiBook.getTitle());
            titleEditText.setText(draft.getTitle());
            contentsEditText.setText(draft.getContent());
            shouldShare = draft.getShouldShare();
            shouldShareSwitch.setChecked(shouldShare);
        }else{
            bookTitleTextView.setText(apiBook.getTitle());
            shouldShareSwitch.setChecked(shouldShare);
        }

        shouldShareSwitch.setOnCheckedChangeListener(this);
        backButton.setOnClickListener(this);
        saveButton.setOnClickListener(this);
        saveDraftButton.setOnClickListener(this);

    }

    private void initData(){
        user = BaseApplication.userData;
        Intent intent = getIntent();
        isModifying = intent.getBooleanExtra("modify", false);
        isDraft = intent.getBooleanExtra("isDraft", false);
        // 기존 독후감을 수정 중이라면 report 객체를 받아와 작업, 새로 독후감을 쓰는거라면 책 검색결과 객체를 가져와 작업
        if(isModifying){
            report = (Report) intent.getSerializableExtra("report");
            isShared = report.isShouldShare();
            shouldShare = isShared;
        }else if(isDraft){
            apiBook  = (SearchResult.Document) intent.getSerializableExtra("book");
            draft = (Draft) intent.getSerializableExtra("draft");
        }else{
            apiBook = (SearchResult.Document) intent.getSerializableExtra("book");
            shouldShare = user.isShouldShareReport();
        }


    }

    private void searchBook(){
        DatabaseReference bookRef = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_book_data_key));

        bookRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    if(apiBook.getIsbn().equals(ds.getKey())){
                        Book book = ds.getValue(Book.class);
                        addReport(book);
                        return;
                    }
                }//End for

                putReportData();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    /**
     * 책이 없을 시 추가
     */
    private void putReportData() {

        Book book = new Book();
        book.setTitle(apiBook.getTitle());
        book.setThumbnail(apiBook.getThumbnail());
        book.setIsbn(apiBook.getIsbn());

        DatabaseReference bookRef = FirebaseDatabase
                .getInstance()
                .getReference()
                .child(getString(R.string.firebase_book_data_key))
                .child(book.getIsbn());

        bookRef.setValue(book).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "putReportData() : 책 추가 완료");
                addReport(book);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "putReportData() : 책 추가 실패 " + e.getMessage());
            }
        });


    }

    private void addReport(Book book) {

        DatabaseReference userReportsRef = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.firebase_user_data_key))
                .child(BaseApplication.userData.getUserId())
                .child("reports");

        String reportKey = userReportsRef.push().getKey();

        Report report = new Report();

        report.setBookISBN(apiBook.getIsbn());
        report.setWriter(BaseApplication.userData.getUserId());
        report.setTitle(reportTitle);
        report.setContents(reportContents);
        report.setShouldShare(shouldShare);
        report.setReportKey(reportKey);
        report.setBookAuthors(Arrays.deepToString(apiBook.getAuthors()));
        report.setBookThumbnail(apiBook.getThumbnail());
        report.setBookTitle(apiBook.getTitle());

        // 현재 시간을 작성시간으로 넣어주기
        Date today = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String writingTime = simpleDateFormat.format(today);

        report.setWriteTime(writingTime);

        User userData = BaseApplication.userData;

        LinkedHashMap<String, Report> myReports;
        if(userData.getReports() != null){
            myReports = userData.getReports();
        }else{
            myReports = new LinkedHashMap<>();
        }

        myReports.put(reportKey, report);
        BaseApplication.userData.setReports(myReports);


        userReportsRef.setValue(myReports).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                if(shouldShare){
                    shareReport(book, report);
                    return;
                }
                if(isDraft){
                    removeDraft();
                }
                progressOFF();
                BaseApplication.showCompleteToast(mContext, "독후감 작성 완료!");
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                }, 500);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressOFF();
                BaseApplication.showErrorToast(mContext, "독후감 작성 실패");
                Log.e(TAG, "addReport() : "+e.getMessage());
            }
        });


    }

    private void shareReport(Book book, Report report) {
        LinkedHashMap<String, Report> reportsOfBook = book.getReportsOfBook();
        if(reportsOfBook == null){
            reportsOfBook = new LinkedHashMap<>();
        }
        reportsOfBook.put(report.getReportKey(), report);

        DatabaseReference bookReportRef = FirebaseDatabase
                .getInstance()
                .getReference()
                .child(getString(R.string.firebase_book_data_key))
                .child(book.getIsbn())
                .child("reportsOfBook");

        bookReportRef.setValue(reportsOfBook).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                progressOFF();
                BaseApplication.showCompleteToast(mContext, "독후감 작성 완료!");
                finish();
                return;
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressOFF();
                BaseApplication.showErrorToast(mContext, "독후감 공유 실패..");
                Log.e(TAG, "shareReport() : " + e.getMessage());
            }
        });
    }

    private void startSaveProgress(){

        if (reportTitle.trim().length() < 5){
            Toast.makeText(mContext, "독후감 제목은 5글자 이상이어야 합니다.", Toast.LENGTH_SHORT).show();
            progressOFF();
            return;
        }

        if (reportContents.trim().length() < 30){
            Toast.makeText(mContext, "독후감 내용은 30자 이상이어야합니다.", Toast.LENGTH_SHORT).show();
            progressOFF();
            return;
        }

        if (isModifying){
            modifyStart();
        }else{
            searchBook();
        }

    }

    private void modifyStart(){

        report.setTitle(reportTitle);
        report.setContents(reportContents);
        report.setShouldShare(shouldShare);

        DatabaseReference reportRef = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.firebase_user_data_key)).child(report.getWriter())
                .child("reports").child(report.getReportKey());

        reportRef.setValue(report).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "modifyStart() : 유저 영역 독후감 수정 완료");
                if(isShared){
                    if(shouldShare){
                        modifyReportOfBook();
                    }else{
                        removeReportOfBook();
                    }
                }else{
                    if (shouldShare){
                        addReportOfBook();
                    }else{
                        updateComplete();
                        return;
                    }
                }
            }
        });
    }

    private void addReportOfBook() {
        DatabaseReference bookRef = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.firebase_book_data_key))
                .child(report.getBookISBN())
                .child("reportsOfBook")
                .child(report.getReportKey());

        bookRef.setValue(report).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "addReportOfBook() : 책 영역 독후감 추가 완료");
                updateComplete();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "addReportOfBook() : 책 영역 독후감 추가 실패 " + e.getMessage());
            }
        });
    }

    /**
     * 기존 공개되있던 독후감에서 내용 수정만 한 경우 수정된 점만 변경해주기
     */
    private void modifyReportOfBook() {
        DatabaseReference reportRef = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.firebase_book_data_key))
                .child(report.getBookISBN())
                .child("reportsOfBook")
                .child(report.getReportKey());

        Map<String, Object> reportUpdates = new HashMap<>();
        reportUpdates.put("title", report.getTitle());
        reportUpdates.put("contents", report.getContents());

        reportRef.updateChildren(reportUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                updateComplete();
                Log.d(TAG, "modifyReportOfBook() : 책 부분 독후감 수정 완료");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "modifyReportOfBook() : 책 부분 독후감 수정 실패");
            }
        });
    }

    /**
     * 공개에서 비공개로 전환 시 책 영역에 독후감 삭제하기
     */
    private void removeReportOfBook() {
        DatabaseReference reportRef = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.firebase_book_data_key)).child(report.getBookISBN())
                .child("reportsOfBook").child(report.getReportKey());

        reportRef.setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "removeReportOfBook() : 책 부분 독후감 삭제 완료");
                updateComplete();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "removeReportOfBook() : 책 부분 독후감 삭제 실패 " + e.getMessage());
            }
        });
    }

    private void updateComplete(){
        LinkedHashMap<String, Report> myReports = BaseApplication.userData.getReports();
        myReports.put(report.getReportKey(), report);
        BaseApplication.userData.setReports(myReports);
        progressOFF();
        BaseApplication.showCompleteToast(mContext, "수정되었습니다.");
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 500);


    }

    private void saveDraft(){
        String title;
        String content;
        String saveTime;
        boolean shouldShare;

        title = titleEditText.getText().toString();
        content = contentsEditText.getText().toString();

        Date today = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        saveTime = sdf.format(today);

        shouldShare = this.shouldShare;

        Draft draft = new Draft(title, content, shouldShare, saveTime, apiBook.getIsbn(), apiBook.getTitle());

        DatabaseReference draftRef = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_user_data_key))
                .child(user.getUserId()).child("temporaryStorages");

        String draftKey = draftRef.push().getKey();
        draft.setDraftKey(draftKey);

        draftRef = draftRef.child(draftKey);

        draftRef.setValue(draft).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "saveDraft() : 임시저장 성공");
                BaseApplication.showCompleteToast(mContext, "임시저장 되었습니다.");
                LinkedHashMap<String, Draft> temporaryStorages;
                if(user.getTemporaryStorages() == null){
                    temporaryStorages = new LinkedHashMap<>();
                }else{
                    temporaryStorages = user.getTemporaryStorages();
                }
                temporaryStorages.put(draftKey, draft);
                BaseApplication.userData.setTemporaryStorages(temporaryStorages);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                }, 500);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "saveDraft() : 임시저장 실패 " + e.getMessage());
            }
        });
    }

    private void removeDraft(){
        if(draft == null){
            return;
        }

        LinkedHashMap<String, Draft> drafts = user.getTemporaryStorages();
        drafts.remove(draft.getDraftKey());

        DatabaseReference draftRef = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_user_data_key))
                .child(user.getUserId()).child("temporaryStorages");

        draftRef.setValue(drafts).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                BaseApplication.userData.setTemporaryStorages(drafts);
                Log.d(TAG, "removeDraft() : 임시저장본 삭제 완료");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "removeDraft() : 임시저장본 삭제 실패 " + e.getMessage());
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.write_report_back_btn:
                onBackPressed();
                break;
            case R.id.write_report_save_btn:
                reportTitle = titleEditText.getText().toString();
                reportContents = contentsEditText.getText().toString();
                progressON();
                if(isModifying){
                    BaseApplication.showDialog(mContext
                            , "수정하시겠습니까?\n독후감이 공개상태에서 비공개상태로 변경될 경우 기존 좋아요와 댓글은 모두 삭제됩니다."
                            , new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    modifyStart();
                                }
                            });
                }else{
                    startSaveProgress();
                }
                break;
            case R.id.write_report_save_draft_btn:
                BaseApplication.showDialog(mContext, "임시 저장하시겠습니까?", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        saveDraft();
                    }
                });
        }
    }

    @Override
    public void onBackPressed() {
        BaseApplication.showDialog(mContext, "이전으로 돌아가시면 작성한 내용을 잃게 됩니다.\n돌아가시겠습니까?", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                WriteReportActivity.super.onBackPressed();
            }
        });
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        shouldShare = b;
    }

    @Override
    public void finish() {
        super.finish();
    }
}
