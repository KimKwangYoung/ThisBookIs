package com.ky.thisbookis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.ky.thisbookis.fragment.HomeFragment;
import com.ky.thisbookis.fragment.RecommendFragment;
import com.ky.thisbookis.fragment.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;


public class MainActivity extends AppCompatActivity {

    Fragment fragmentMyPage, fragmentSearch, fragmentRanking;
    FragmentManager fragmentManager = getSupportFragmentManager();

    long pressedTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragmentMyPage = new HomeFragment();

        fragmentManager.beginTransaction().replace(R.id.main_fragment_container, fragmentMyPage).commit();

        BottomNavigationView bottomNavigationView = findViewById(R.id.main_bottom_navigation_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.home_tab:
                        moveMyPage();
                        return true;
                    case R.id.search_tab:
                        moveSearch();
                        return true;
                    case R.id.recommend_tab:
                        moveRanking();
                        return true;
                }
                return false;
            }
        });
    }

    private void moveSearch(){
        if(fragmentSearch == null){
            fragmentSearch = new SearchFragment();
            fragmentManager.beginTransaction().add(R.id.main_fragment_container, fragmentSearch).commit();
        }

        if(fragmentMyPage != null) fragmentManager.beginTransaction().hide(fragmentMyPage).commit();
        if(fragmentSearch != null) fragmentManager.beginTransaction().show(fragmentSearch).commit();
        if(fragmentRanking != null) fragmentManager.beginTransaction().hide(fragmentRanking).commit();

    }

    private void moveMyPage(){
        if(fragmentMyPage == null){
            fragmentMyPage = new HomeFragment();
            fragmentManager.beginTransaction().add(R.id.main_fragment_container, fragmentMyPage).commit();
        }

        if(fragmentMyPage != null) fragmentManager.beginTransaction().show(fragmentMyPage).commit();
        if(fragmentSearch != null) fragmentManager.beginTransaction().hide(fragmentSearch).commit();
        if(fragmentRanking != null) fragmentManager.beginTransaction().hide(fragmentRanking).commit();

    }

    private void moveRanking(){
        if(fragmentRanking == null){
            fragmentRanking = new RecommendFragment();
            fragmentManager.beginTransaction().add(R.id.main_fragment_container, fragmentRanking).commit();
        }

        if(fragmentMyPage != null) fragmentManager.beginTransaction().hide(fragmentMyPage).commit();
        if(fragmentSearch != null) fragmentManager.beginTransaction().hide(fragmentSearch).commit();
        if(fragmentRanking != null) fragmentManager.beginTransaction().show(fragmentRanking).commit();

    }

    @Override
    public void onBackPressed() {

        if(pressedTime == 0){
            Toast.makeText(getApplicationContext(), "한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
            pressedTime = System.currentTimeMillis();
        }else{
            int seconds = (int) (System.currentTimeMillis() - pressedTime);

            if(seconds > 2000){
                Toast.makeText(getApplicationContext(), "한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
                pressedTime = 0;
            }else{
                super.onBackPressed();
            }
        }


    }
}
