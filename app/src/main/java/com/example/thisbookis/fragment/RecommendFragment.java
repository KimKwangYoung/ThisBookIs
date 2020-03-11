package com.example.thisbookis.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.example.thisbookis.BaseApplication;
import com.example.thisbookis.InterParkApiClient;
import com.example.thisbookis.R;
import com.example.thisbookis.ReportActivity;
import com.example.thisbookis.data.BestSeller;
import com.example.thisbookis.data.Book;
import com.example.thisbookis.data.Report;
import com.example.thisbookis.data.User;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecommendFragment extends Fragment {

    public static final String TAG = "RecommendFragment";

    private final int TOP_RANK_BOOK_INDEX = 0;
    private final int BEST_SELLER_INDEX = 1;

    Context mContext;

    private User me;

    private ArrayList<Book> topRankBooks;
    private ArrayList<BestSeller.Item> topThreeBestSellers;
    private ViewGroup rootView;

    private String bookRefKey;

    private int releatedReportIndex;

    private static final int TOP_RANK_BOOK_MAX_PAGE = 3;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_recommend, container, false);


        initData();
        return rootView;
    }

    private void initData(){
        releatedReportIndex = 0;
        bookRefKey = getString(R.string.firebase_book_data_key);
        mContext = getActivity();
        me = BaseApplication.userData;
        getTopRankBook();
        initReleatedReportCard();
        getBestSeller();
        getAllReport();
    }

    private void initTopRankBookLayout(){
        ViewPager topRankBookViewPager;
        topRankBookViewPager = rootView.findViewById(R.id.recommend_top_rank_book_vp);
        topRankBookViewPager.setAdapter(new ViewPagerAdapter(getChildFragmentManager()
                , FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, TOP_RANK_BOOK_INDEX));
        topRankBookViewPager.setCurrentItem(0);

        TabLayout tabLayout = rootView.findViewById(R.id.recommend_top_rank_book_tl);
        tabLayout.setupWithViewPager(topRankBookViewPager, true);
    }

    private void getTopRankBook() {
        topRankBooks = new ArrayList<>();
        LinearLayout topRankBookLinearLayout = rootView.findViewById(R.id.recommend_top_rank_book_ll);

        DatabaseReference reportRef = FirebaseDatabase.getInstance().getReference()
                .child(bookRefKey);

        Query topRankBookQuery = reportRef.orderByChild("readUserCount").limitToLast(3);
        topRankBookQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    topRankBooks.add(ds.getValue(Book.class));
                }
                if(topRankBooks.isEmpty() || topRankBooks.size() < 3){
                    Log.d(TAG, "getTopRankBook() : topRankBooks 비어있음");
                    topRankBookLinearLayout.setVisibility(View.GONE);
                    return;
                }

                Collections.reverse(topRankBooks);
                for(Book b : topRankBooks){
                    Log.d(TAG, b.getTitle());
                }

                initTopRankBookLayout();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * ###################### ReleatedReport #########################
     * 내가 쓴 최근 독후감을 얻어와 해당 책에 대한 좋아요가 제일 많은 다른 독후감 구해오기
     *
     */
    private void initReleatedReportCard(){

        ArrayList<Report> myReports = new ArrayList<>(me.getReports().values());
        if(myReports.isEmpty()){
            Log.d(TAG, "initReleatedReportCard() : 유저의 독후감이 존재하지 않음");
            return;
        }

        Collections.sort(myReports, new Comparator<Report>() {
            @Override
            public int compare(Report current, Report next) {
                return -(current.getWriteTime().compareTo(next.getWriteTime()));
            }
        });
        Report r = myReports.get(releatedReportIndex);
        releatedReportIndex += 1;

        DatabaseReference bookRef = FirebaseDatabase.getInstance().getReference()
                .child(bookRefKey)
                .child(r.getBookISBN()).child("reportsOfBook");

        Query reportQuery = bookRef.orderByChild("likeCount").limitToLast(2);

        reportQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Report releatedReport = new Report();
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    if(!r.getReportKey().equals(ds.getKey())){
                        releatedReport = ds.getValue(Report.class);
                        Log.d(TAG, "initReleatedReportCard() : 관련 독후감 얻어오기 성공");
                        break;
                    }
                }
                if(releatedReport.getReportKey() != null) {
                    getReleatedReportWriter(releatedReport, 0);
                    return;
                }else{
                    Log.d(TAG, "실행 확인 : " + r.getBookTitle() + " / " + releatedReportIndex);
                    initReleatedReportCard();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    /**
     * 독후감을 구해온 후 작성자 user로 받아오기
     * @param report : 해당 책에서 좋아요가 가장 많은 독후감
     */

    private void getReleatedReportWriter(Report report, int reportDivisionIndex) {
        if(report.getReportKey() == null){ // 관련된 독후감이 없을 경우 보여주지 않기 or 독후감이 자신이 쓴 경우 밖에 없을 경우 보여주지 않기
            Log.d(TAG, "getReleatedReportWriter() : 관련 독후감 null");
            return;
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.firebase_user_data_key))
                .child(report.getWriter());

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               User writer = dataSnapshot.getValue(User.class);
               if(writer != null) {
                   Log.d(TAG,"getReleatedReportWriter() : 관련 독후감 작성자 얻어오기 성공");
                   if(reportDivisionIndex == 0){
                       setReleatedReportLayout(writer, report);
                   }else if(reportDivisionIndex == 1){
                       initTopRankReportLayout(writer, report);
                   }
               }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * initReleatedReportCard(), getReleatedReportWriter()에서 구해온
     * 독후감과 유저를 전달해 해당 뷰에 내용 넣어주기
     * @param writer
     * @param report
     */

    private void setReleatedReportLayout(User writer, Report report){
        LinearLayout releatedReportLinearLayout = rootView.findViewById(R.id.recommend_releated_report_ll);
        ImageView writerProfileImageView = rootView.findViewById(R.id.recommend_releated_report_profile_iv);
        TextView wrtierNicknameTextView = rootView.findViewById(R.id.recommend_releated_report_nickname_tv);
        TextView reportLikeCountTextView = rootView.findViewById(R.id.recommend_releated_report_like_cnt_tv);
        TextView BookTitleTextView = rootView.findViewById(R.id.recommend_releated_report_book_title_tv);
        TextView reportTitleTextView = rootView.findViewById(R.id.recommend_releated_report_title_tv);
        TextView reportContentTextView = rootView.findViewById(R.id.recommend_releated_report_content_tv);

        Glide.with(mContext).load(writer.getProfileURL()).apply(BaseApplication.profileImageOptions)
                .into(writerProfileImageView);

        wrtierNicknameTextView.setText(writer.getNickname());
        String likeCountString = report.getLikeCount() + "명이 좋아합니다.";
        reportLikeCountTextView.setText(likeCountString);
        String bookTitleString = "'" + report.getBookTitle() + "'";
        BookTitleTextView.setText(bookTitleString);
        reportTitleTextView.setText(report.getTitle());
        reportContentTextView.setText(report.getContents());

        releatedReportLinearLayout.setVisibility(View.VISIBLE);
        reportContentTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveReportActivity(report);
            }
        });



        Log.d(TAG, "setReleatedReport() : 관련 독후감 카드 설정 완료!");

    }

    /**
     * ###################### ReleatedReport #########################
     */


    /**
     * ###################### BestSeller #########################
     * 인터파크 도서 api를 활용하여 '국내도서' 베스트 셀러 가져오기
     */
    private void getBestSeller(){
        LinearLayout bestsellerLinearLayout = rootView.findViewById(R.id.recommend_best_seller_ll);

        Call<BestSeller> call = InterParkApiClient.getInstance().bestSellerService.getBestSeller(
                getString(R.string.inter_park_api_key), "100", "json");

        Callback<BestSeller> callback = new Callback<BestSeller>() {
            @Override
            public void onResponse(Call<BestSeller> call, Response<BestSeller> response) {
                if(response.isSuccessful()){
                    ArrayList<BestSeller.Item> bestSellers = new ArrayList<>();
                    bestSellers = response.body().getItems();
                    if(!bestSellers.isEmpty()){
                        Log.d(TAG, "베스트 셀러 얻어오기 성공");
                    }
                    bestsellerLinearLayout.setVisibility(View.VISIBLE);
                    setBestSellerLayout(bestSellers);

                }else{
                    Log.e(TAG, "베스트 셀러 얻어오기 실패 " + response.errorBody().toString() );
                }
            }

            @Override
            public void onFailure(Call<BestSeller> call, Throwable t) {
                Log.e(TAG, "베스트 셀러 얻어오기 실패!! " + t.getMessage() + "//" + call.request().url().toString() );
            }
        };

        call.enqueue(callback);
    }

    /**
     * 베스트 셀러 list를 top3로 제한하여 viewpager로 보여주기
     * @param bestSellers
     */
    private void setBestSellerLayout(ArrayList<BestSeller.Item> bestSellers){
        topThreeBestSellers = new ArrayList<>();
        for(int i = 0; i < 3; i++){
            topThreeBestSellers.add(bestSellers.get(i));
        }

        ViewPager bestSellerViewPager = rootView.findViewById(R.id.recommend_best_seller_vp);
        bestSellerViewPager.setAdapter(new ViewPagerAdapter(getChildFragmentManager(),
                FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,
                BEST_SELLER_INDEX));
        bestSellerViewPager.setCurrentItem(0);


        TabLayout tabLayout = rootView.findViewById(R.id.recommend_best_seller_tl);
        tabLayout.setupWithViewPager(bestSellerViewPager, true);
    }

    /**
     * ###################### BestSeller #########################
     */

    /**
     * ###################### TopRankReport #########################
     * 작성된지 한달 이내 독후감 중 가장 좋아요를 많이 받은 독후감 가져오기
     */

    private void getAllReport(){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child(bookRefKey);
        Map<String, Report> reports = new HashMap<>();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar cal = new GregorianCalendar(Locale.KOREA);
        cal.add(Calendar.MONTH, -1);
        String monthAgo = sdf.format(cal.getTime());


        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    Book book = ds.getValue(Book.class);
                    if(ds.getValue(Book.class).getReportsOfBook() != null){
                        reports.putAll(book.getReportsOfBook());
                    }
                }

                if(reports.isEmpty()){
                    Log.d(TAG, "getAllReport() : 독후감 목록 비어있음");
                    return;
                }

                Iterator<Report> reportIterator = reports.values().iterator();
                while (reportIterator.hasNext()){
                    Report cursor = reportIterator.next();
                    String writeTime = cursor.getWriteTime();
                    writeTime = writeTime.substring(0, 10);
                    if(monthAgo.compareTo(writeTime) > 0){
                        reportIterator.remove();
                    }
                }

                ArrayList<Report> reportsList = new ArrayList<>(reports.values());
                Collections.sort(reportsList, new Comparator<Report>() {
                    @Override
                    public int compare(Report current, Report next) {
                        Integer currentLikeCount = current.getLikeCount();
                        Integer nextLikeCount = next.getLikeCount();
                        return -(currentLikeCount.compareTo(nextLikeCount));
                    }
                });

                for(Report r : reportsList){
                    Log.d(TAG, r.getTitle() + " / " + r.getLikeCount());
                }
                Report report = reportsList.get(0);
                getReleatedReportWriter(report, 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    private void initTopRankReportLayout(User user, Report report){
        LinearLayout topRankReportLinearLayout = rootView.findViewById(R.id.recommend_top_rank_report_ll);
        ImageView topRankReportWriterProfileImageView = rootView.findViewById(R.id.recommend_top_rank_report_profile_iv);
        TextView topRankReportWriterNicknameTextView = rootView.findViewById(R.id.recommend_top_rank_report_nickname_tv);
        TextView topRankReportLikeCountTextView = rootView.findViewById(R.id.recommend_top_rank_report_like_cnt_tv);
        TextView topRankReportTitleTextView = rootView.findViewById(R.id.recommend_top_rank_report_title_tv);
        TextView topRankReportContentTextView = rootView.findViewById(R.id.recommend_top_rank_report_content_tv);

        Glide.with(mContext).load(user.getProfileURL()).apply(BaseApplication.profileImageOptions)
                .into(topRankReportWriterProfileImageView);

        topRankReportWriterNicknameTextView.setText(user.getNickname());
        String likeCount = report.getLikeCount() + "명이 좋아합니다.";
        topRankReportLikeCountTextView.setText(likeCount);
        topRankReportTitleTextView.setText(report.getTitle());
        topRankReportContentTextView.setText(report.getContents());

        topRankReportLinearLayout.setVisibility(View.VISIBLE);

        topRankReportContentTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveReportActivity(report);
            }
        });
    }

    /**
     * ###################### TopRankReport #########################
     */


    private void moveReportActivity(Report report){
        Intent intent = new Intent(getActivity(), ReportActivity.class);
        intent.putExtra("report", report);
        startActivity(intent);
    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {
        private Fragment currentFragment;
        private int divisionIndex;
        private ViewPagerAdapter(@NonNull FragmentManager fm, int behavior, int divisionIndex) {
            super(fm, behavior);
            this.divisionIndex = divisionIndex;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            final int FIRST_INDEX = 0;
            Bundle bundle = new Bundle();
            if(position < 0 || TOP_RANK_BOOK_MAX_PAGE<=position){
                return null;
            }
            if(divisionIndex == TOP_RANK_BOOK_INDEX) {
                currentFragment = new TopRankBookFragment(mContext, topRankBooks);
            }else{
                currentFragment = new BestSellerFragment(mContext, topThreeBestSellers);
            }

            switch (position){
                case 0:
                    bundle.putInt("index", FIRST_INDEX);
                    currentFragment.setArguments(bundle);
                    break;
                case 1:
                    bundle.putInt("index", FIRST_INDEX + 1);
                    currentFragment.setArguments(bundle);
                    break;
                case 2:
                    bundle.putInt("index", FIRST_INDEX + 2);
                    currentFragment.setArguments(bundle);
                    break;

            }
            return currentFragment;
        }

        @Override
        public int getCount() {
            return TOP_RANK_BOOK_MAX_PAGE;
        }
    }

}
