package com.example.thisbookis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.thisbookis.data.Book;
import com.example.thisbookis.data.MyBook;
import com.example.thisbookis.data.Report;
import com.example.thisbookis.data.SearchResult;
import com.example.thisbookis.data.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;

public class BookContentsActivity extends AppCompatActivity implements View.OnClickListener{

    public static final String TAG = "BookContentsActivity";

    SearchResult.Document apiBook;
    TextView titleTextView, authorsTextView, publisherTextView, priceTextView, contentsTextView
            , readUsersCntTextView, reportsCntTextView;
    ImageView thumbnailImageView;

    RecyclerView reportsRecyclerView;


    Context mContext;

    Animation fab_open, fab_close;
    Boolean isFabOpen = false;
    FloatingActionButton fabButton, addBookFabButton, writeReportFabButton;
    ProgressBar progressBar;

    Book bookData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_contents);

        mContext = this;

        Intent intent = getIntent();
        apiBook = (SearchResult.Document)intent.getSerializableExtra("book");

        fab_open = AnimationUtils.loadAnimation(mContext, R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(mContext, R.anim.fab_close);

        searchBook();

    }

    private void initView() {
        titleTextView = findViewById(R.id.book_contents_title_tv);
        authorsTextView = findViewById(R.id.book_contents_authors_tv);
        publisherTextView = findViewById(R.id.book_contents_publisher_tv);
        priceTextView = findViewById(R.id.book_contents_price_tv);
        thumbnailImageView = findViewById(R.id.book_contents_thumbnail_iv);
        contentsTextView = findViewById(R.id.book_contents_contents_tv);
        fabButton = findViewById(R.id.book_contents_fab);
        addBookFabButton = findViewById(R.id.book_contents_fab_add_book);
        writeReportFabButton = findViewById(R.id.book_contents_fab_write_report);
        progressBar = findViewById(R.id.book_contents_pb);
        readUsersCntTextView = findViewById(R.id.book_contents_read_users_cnt_tv);
        reportsCntTextView = findViewById(R.id.book_contents_reports_cnt_tv);
        reportsRecyclerView = findViewById(R.id.book_contents_reports_rv);

        // 책 소개 얻어오기
        getBookDescription();

        if(bookData == null){
            readUsersCntTextView.setText("0");
            reportsCntTextView.setText("0");
        }else if(bookData.getReadUsers() == null && bookData.getReportsOfBook() != null){
            readUsersCntTextView.setText("0");
            reportsCntTextView.setText(Integer.toString(bookData.getReportsOfBook().size()));
        }else if(bookData.getReadUsers() != null && bookData.getReportsOfBook() == null){
            readUsersCntTextView.setText(Integer.toString(bookData.getReadUsers().size()));
            reportsCntTextView.setText("0");
        }else{
            readUsersCntTextView.setText(Integer.toString(bookData.getReadUsers().size()));
            reportsCntTextView.setText(Integer.toString(bookData.getReportsOfBook().size()));
        }


        if(bookData != null) {
            initReportLayout();
        }

        Glide.with(mContext).asBitmap()
                .override(300, 400)
                .error(R.drawable.ic_book)
                .thumbnail(0.1f)
                .load(apiBook.getThumbnail()).into(thumbnailImageView);


        titleTextView.setText(apiBook.getTitle());
        priceTextView.setText(apiBook.getPrice() + "원");
//        if(apiBook.getContents().equals("")){
//            contentsTextView.setText("책 소개가 없습니다.");
//        }else {
//            contentsTextView.setText(apiBook.getContents());
//        }


        String authors;
        if(apiBook.getAuthors() != null && apiBook.getAuthors().length != 0) {
            authors = replaceString(Arrays.deepToString(apiBook.getAuthors()));
        }else{
            authors = "-";
        }
        authorsTextView.setText(authors);

        String publisher;
        if(apiBook.getPublisher() != null && !apiBook.getPublisher().equals("")) {
            publisher = apiBook.getPublisher();
        }else{
            publisher = "-";
        }

        publisherTextView.setText(publisher);

        fabButton.setOnClickListener(this);
        writeReportFabButton.setOnClickListener(this);
        addBookFabButton.setOnClickListener(this);

    }

    private void initReportLayout() {

        ArrayList<Report> reports = new ArrayList<>();
        ArrayList<User> users = new ArrayList<>();

        if(bookData.getReportsOfBook() == null){
            reportsRecyclerView.setVisibility(View.GONE);
            return;
        }

        int reportIndex = 0;
        for(Report r : bookData.getReportsOfBook().values()){
            if(reportIndex > 2){ /* 최대 3개까지만 받아오도록 */
                break;
            }
            reports.add(r);
            reportIndex += 1;
        }//End for

        String userRefKey = getString(R.string.firebase_user_data_key);
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child(userRefKey);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    for(Report r : reports){
                        if(ds.getKey().equals(r.getWriter())){
                            users.add(ds.getValue(User.class));
                        }
                    }
                }//End for

                connectReportsAdapter(reports, users);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void connectReportsAdapter(ArrayList<Report> reports, ArrayList<User> users) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, RecyclerView.VERTICAL, false);
        reportsRecyclerView.setLayoutManager(layoutManager);
        ReportOfBookContentsAdapater adapater = new ReportOfBookContentsAdapater(mContext, reports, users);
        reportsRecyclerView.setAdapter(adapater);
    }

    public String replaceString(String s){
        s = s.replace("[","");
        s = s.replace("]","");
        return s;

    }

    private void moveCreateReportActivity(){
        if (BaseApplication.userData.getReports() != null){
            for(Report r : BaseApplication.userData.getReports().values()){
                if(r.getBookISBN().equals(apiBook.getIsbn())){
                    BaseApplication.showInfoToast(mContext, "이미 독후감을 작성하셨습니다.");
                    return;
                }
            }
        }
        Intent intent = new Intent(mContext, WriteReportActivity.class);
        intent.putExtra("book", apiBook);
        startActivity(intent);
    }

    /**
     * DB에 책 정보가 저장 되어 있는지 검색하고
     * 없다면 책 추가하기
     */
    private void searchBook(){

        DatabaseReference bookRef = FirebaseDatabase.getInstance().getReference().child("book").child(apiBook.getIsbn());
        bookRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                bookData = dataSnapshot.getValue(Book.class);
                initView();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * 책 읽은 유저 리스트에 추가
     */
    private void addUserToReadUsers(Book book){
        LinkedHashMap<String, String> readUsers;

        if(book.getReadUsers() != null) {
            readUsers = book.getReadUsers();
        }else{
            readUsers = new LinkedHashMap<>();
        }

        readUsers.put(BaseApplication.userData.getUserId(), BaseApplication.userData.getNickname());

        DatabaseReference bookRef = FirebaseDatabase.getInstance().getReference().child("book").child(book.getIsbn());
        bookRef.child("readUsers").setValue(readUsers).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                addBookToMyBooks();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(mContext, "읽은 책으로 추가 실패", Toast.LENGTH_SHORT).show();
                Log.e("addUserToReadUsers", e.getMessage());
            }
        });
    }

    private void addBookToFirebase() {
        Book book = new Book();
        book.setIsbn(apiBook.getIsbn());
        book.setThumbnail(apiBook.getThumbnail());
        book.setTitle(apiBook.getTitle());
        LinkedHashMap<String, String> readUsers = new LinkedHashMap<>();
        readUsers.put(BaseApplication.userData.getUserId(), BaseApplication.userData.getNickname());
        book.setReadUsers(readUsers);

        DatabaseReference bookRef = FirebaseDatabase.getInstance().getReference().child("book");
        bookRef.child(apiBook.getIsbn()).setValue(book).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                addBookToMyBooks();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressBar.setVisibility(View.GONE);
                BaseApplication.showErrorToast(mContext, "책 추가 실패");
                Log.e("BookContentsActivity", e.getMessage());
            }
        });

    }

    private void addBookToMyBooks() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.firebase_user_data_key))
                .child(BaseApplication.userData.getUserId());

        MyBook myBook = new MyBook();
        myBook.setTitle(apiBook.getTitle());
        myBook.setAuthors(Arrays.deepToString(apiBook.getAuthors()));
        myBook.setIsbn(apiBook.getIsbn());
        myBook.setThumbnail(apiBook.getThumbnail());

        // 등록 시간 넣어주기
        Date today = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH-mm");
        String registrationTime = sdf.format(today);
        myBook.setRegistrationTime(registrationTime);

        LinkedHashMap<String, MyBook> myBooks = BaseApplication.userData.getMyBooks();
        myBooks.put(apiBook.getIsbn(), myBook);
        BaseApplication.userData.setMyBooks(myBooks);

        userRef.child("myBooks").setValue(myBooks).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                progressBar.setVisibility(View.GONE);
                BaseApplication.showCompleteToast(mContext, "읽은 책으로 추가 되었습니다.");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressBar.setVisibility(View.GONE);
                BaseApplication.showErrorToast(mContext, "내 서재에 등록 실패");
                Log.e("addBookToMyBooks() :: " , e.getMessage());
            }
        });

    }

    private void getBookDescription(){
        new DescriptionTask().execute(apiBook.getUrl());
    }

    public class DescriptionTask extends AsyncTask<String, Void, String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            Log.d(TAG, "DescriptionTask : onPreExecute()");

        }

        @Override
        protected String doInBackground(String... strings) {
            String desc = "";
            try {
                Document doc = Jsoup.connect(strings[0]).get();
                Element element = doc.select("p[class=desc]").first();
//                Element element = doc.select("p [class=desc]").first();
                desc = element.text();
                Log.d(TAG, element.text());
            }catch (Exception e){
                Log.e(TAG, "DescriptionTask : doInBackground() - "  + e.getMessage());
            }
            return desc;
        }

        @Override
        protected void onPostExecute(String s) {

            if (s != null && !s.equals("")){
                contentsTextView.setText(s);
            }else{
                contentsTextView.setText("책 소개가 없습니다.");
            }

        }
    }
    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.anim_slide_stay, R.anim.anim_slide_down);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.book_contents_fab:
                anim();
                break;

            case R.id.book_contents_fab_write_report:
                moveCreateReportActivity();
                break;

            case R.id.book_contents_fab_add_book:
                if(BaseApplication.userData.getMyBooks().containsKey(apiBook.getIsbn())){
                    Toast.makeText(mContext, "이미 책이 서재에 존재합니다.", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.INVISIBLE);
                    break;
                }/* 읽은 책에 존재한다면 실행 하지않음. */
                progressBar.setVisibility(View.VISIBLE);
                if(bookData == null){
                    addBookToFirebase();
                }else{
                    addUserToReadUsers(bookData);
                }
                break;
        }
    }

    private void anim() {
        if(isFabOpen){
            fabButton.setImageResource(R.drawable.ic_add_white_24dp);
            addBookFabButton.startAnimation(fab_close);
            writeReportFabButton.startAnimation(fab_close);
            addBookFabButton.setClickable(false);
            writeReportFabButton.setClickable(false);
            isFabOpen = false;
        }else{
            fabButton.setImageResource(R.drawable.ic_clear_white_24dp);
            addBookFabButton.startAnimation(fab_open);
            writeReportFabButton.startAnimation(fab_open);
            addBookFabButton.setClickable(true);
            writeReportFabButton.setClickable(true);
            isFabOpen = true;
        }
    }
}
