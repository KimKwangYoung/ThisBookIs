package com.example.thisbookis.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.thisbookis.BaseApplication;
import com.example.thisbookis.R;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;

public class FeedFragment extends Fragment {

    public static final String TAG = "FeedFragment";

    Context mContext;

    private User me, releatedReportUser;

    private ArrayList<Book> topRankBooks;
    private Report releatedReport;
    private ViewGroup rootView;

    private TopRankBookFragment currentFragment;

    private static final int TOP_RANK_BOOK_MAX_PAGE = 3;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_feed, container, false);


        initData();
        return rootView;
    }

    private void initData(){
        mContext = getActivity();
        me = BaseApplication.userData;
        topRankBooks = new ArrayList<>();
        getTopRankBook();
    }

    private void initView(ViewGroup rootView){

    }

    private void initTopRankBookLayout(){
        ViewPager topRankBookViewPager;
        topRankBookViewPager = rootView.findViewById(R.id.feed_top_rank_book_vp);
        topRankBookViewPager.setAdapter(new TopRankBookAdapter(getChildFragmentManager()));
        topRankBookViewPager.setCurrentItem(0);

        TabLayout tabLayout = rootView.findViewById(R.id.feed_top_rank_book_tl);
        tabLayout.setupWithViewPager(topRankBookViewPager, true);

    }

    private void getReleatedBook(){

    }

    private void getTopRankBook() {
        DatabaseReference reportRef = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.firebase_book_data_key));

        Query topRankBookQuery = reportRef.orderByChild("readUserCount").limitToLast(3);
        topRankBookQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    topRankBooks.add(ds.getValue(Book.class));
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

    private class TopRankBookAdapter extends FragmentPagerAdapter {

        public TopRankBookAdapter(@NonNull FragmentManager fm, int behavior) {
            super(fm, behavior);
        }

        public TopRankBookAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            final int FIRST_INDEX = 0;
            Bundle bundle = new Bundle();
            if(position < 0 || TOP_RANK_BOOK_MAX_PAGE<=position){
                return null;
            }

            switch (position){
                case 0:
                    bundle.putInt("index", FIRST_INDEX);
                    currentFragment = new TopRankBookFragment(mContext, topRankBooks);
                    currentFragment.setArguments(bundle);
                    Log.d(TAG, bundle.getInt("index") + "");
                    break;
                case 1:
                    bundle.putInt("index", FIRST_INDEX + 1);
                    currentFragment = new TopRankBookFragment(mContext, topRankBooks);
                    currentFragment.setArguments(bundle);
                    Log.d(TAG, bundle.getInt("index") + "");
                    break;
                case 2:
                    bundle.putInt("index", FIRST_INDEX + 2);
                    currentFragment= new TopRankBookFragment(mContext, topRankBooks);
                    currentFragment.setArguments(bundle);
                    Log.d(TAG, bundle.getInt("index") + "");
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
