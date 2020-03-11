package com.ky.thisbookis.fragment;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.ky.thisbookis.AppInfoActivity;
import com.ky.thisbookis.BaseActivity;
import com.ky.thisbookis.BaseApplication;
import com.ky.thisbookis.DraftListActivity;
import com.ky.thisbookis.LoginActivity;
import com.ky.thisbookis.MyBooksActivity;
import com.ky.thisbookis.MyReportsActivity;
import com.ky.thisbookis.NoticeActivity;
import com.ky.thisbookis.SettingAcitivty;
import com.ky.thisbookis.R;
import com.ky.thisbookis.data.Draft;
import com.ky.thisbookis.data.User;
import com.facebook.login.LoginManager;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.nhn.android.naverlogin.OAuthLogin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class HomeFragment extends Fragment implements View.OnClickListener {

    public final static String TAG = "HomeFragment";

    private final int MODIFY_PROFILE_REQUEST_CODE = 100;
    private Fragment fragment;

    private User userData;
    private ViewGroup rootView;
    private ArrayList<Draft> drafts;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_my_page, container, false);

        Log.d(TAG, "onCreateView");

        fragment = this;

        initData();
        initView(rootView);


        return rootView;
    }

    private void initView(ViewGroup rootView){

        Button logoutButton;
        ImageView profileImageView;
        TextView profileNickNameTextView, myBooksCountTextView, myReportsCountTextView
                , draftTitleTextView, draftContentTextView, draftMoreViewButton;
        LinearLayout settingButton, myBooksButton, appInfoButton, myReportsButton, draftHideLinearLayout, draftLinearLayout
                ,noticeButton;

        profileImageView = rootView.findViewById(R.id.home_profile_image_iv);
        profileNickNameTextView = rootView.findViewById(R.id.home_nickname_tv);
        logoutButton = rootView.findViewById(R.id.my_page_logout_btn);
        settingButton = rootView.findViewById(R.id.home_setting_modify_btn);
        myBooksButton = rootView.findViewById(R.id.home_my_books_btn);
        myBooksCountTextView = rootView.findViewById(R.id.home_my_books_cnt_tv);
        myReportsCountTextView = rootView.findViewById(R.id.home_my_reports_cnt_tv);
        myReportsButton = rootView.findViewById(R.id.home_my_reports_btn);
        draftTitleTextView = rootView.findViewById(R.id.home_draft_title_tv);
        draftContentTextView = rootView.findViewById(R.id.home_draft_contents_tv);
        draftHideLinearLayout = rootView.findViewById(R.id.home_draft_hide_ll);
        draftLinearLayout = rootView.findViewById(R.id.home_draft_ll);
        draftMoreViewButton = rootView.findViewById(R.id.home_draft_more_view_btn);
        appInfoButton = rootView.findViewById(R.id.home_app_info_ll);
        noticeButton = rootView.findViewById(R.id.home_app_notice_ll);

        logoutButton.setOnClickListener(this);
        settingButton.setOnClickListener(this);
        myBooksButton.setOnClickListener(this);
        myReportsButton.setOnClickListener(this);
        draftMoreViewButton.setOnClickListener(this);
        appInfoButton.setOnClickListener(this);
        noticeButton.setOnClickListener(this);

        RequestOptions options = BaseApplication.profileImageOptions;
        Glide.with(fragment).load(userData.getProfileURL()).apply(options).into(profileImageView);

        profileNickNameTextView.setText(userData.getNickname());
        String bookCnt;
        String reportCnt;

        if(userData.getMyBooks() != null) {
            bookCnt = Integer.toString(userData.getMyBooks().size());
        }else{
            bookCnt = "0";
        }

        if (userData.getReports() != null){
            reportCnt = Integer.toString(userData.getReports().size());
        }else{
            reportCnt = "0";
        }

        myBooksCountTextView.setText(bookCnt);
        myReportsCountTextView.setText(reportCnt);

        if(drafts != null && drafts.size() > 0){
            Draft draft = drafts.get(0);
            draftTitleTextView.setText(draft.getTitle());
            draftContentTextView.setText(draft.getContent());
            draftHideLinearLayout.setVisibility(View.GONE);
            draftLinearLayout.setVisibility(View.VISIBLE);
        }else{
            draftLinearLayout.setVisibility(View.GONE);
            draftHideLinearLayout.setVisibility(View.VISIBLE);
        }

    }

    private void initData(){
        userData = BaseApplication.userData;
        if(userData.getTemporaryStorages() != null) {
            drafts = new ArrayList<>(userData.getTemporaryStorages().values());
            Log.d(TAG, "initData 실행");
            Collections.sort(drafts, new Comparator<Draft>() {
                @Override
                public int compare(Draft current, Draft after) {
                    return -(current.getSaveTime().compareTo(after.getSaveTime()));
                }
            });
        }else{
            drafts = null;
        }

    }

    /* ############################## 로그아웃 코드 ##############################*/
    //TODO: 중복되는 코드들 어떻게 활용할 건지 고민해보기
    private void logout(){
        switch (BaseActivity.typeOfLogin){
            case BaseApplication.LOGIN_WITH_KAKAO:
                UserManagement.getInstance().requestLogout(new LogoutResponseCallback() {
                    @Override
                    public void onCompleteLogout() {
                        removeSharedPreferences();
                        redirectLoginActivity();
                    }
                });
                break;
            case BaseApplication.LOGIN_WITH_FACEBOOK:
                LoginManager.getInstance().logOut();
                removeSharedPreferences();
                redirectLoginActivity();
                break;
            case BaseApplication.LOGIN_WITH_NAVER:
                OAuthLogin.getInstance().logout(getActivity());
                if(OAuthLogin.getInstance().getAccessToken(getActivity())==null){
                    removeSharedPreferences();
                    redirectLoginActivity();
                    Log.e(TAG, "네이버 로그아웃 성공");
                }
                break;
        }
    }

    private void removeSharedPreferences(){
        SharedPreferences.Editor editor = BaseApplication.loginData.edit();
        editor.putBoolean(BaseApplication.LOGIN_DATA_CHECK_KEY, false);
        editor.apply();
    }

    private void redirectLoginActivity(){
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
        if(getActivity() != null){
            getActivity().finish();
        }
    }

    /**
     * ############################## 로그아웃 코드  ##############################
     * */

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.my_page_logout_btn:
                logout();
                break;
            case R.id.home_setting_modify_btn:
                Intent settingIntent = new Intent(getActivity(), SettingAcitivty.class);
                startActivityForResult(settingIntent, MODIFY_PROFILE_REQUEST_CODE);
                break;
            case R.id.home_my_books_btn:
                Log.d(TAG, BaseApplication.userData.getMyBooks()+" ");
                Intent booksIntent = new Intent(getActivity(), MyBooksActivity.class);
                startActivity(booksIntent);
                break;
            case R.id.home_my_reports_btn:
                Intent reportIntent = new Intent(getActivity(), MyReportsActivity.class);
                startActivity(reportIntent);
                break;
            case R.id.home_draft_more_view_btn:
                Intent intent = new Intent(getActivity(), DraftListActivity.class);
                startActivity(intent);
                break;
            case R.id.home_app_info_ll:
                Intent appInfoIntent = new Intent(getActivity(), AppInfoActivity.class);
                startActivity(appInfoIntent);
                break;
            case R.id.home_app_notice_ll:
                Intent noticeIntent = new Intent(getActivity(), NoticeActivity.class);
                startActivity(noticeIntent);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == MODIFY_PROFILE_REQUEST_CODE && resultCode == 200){
            Log.d(TAG, "onActivityResult");
            initData();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        initData();
        initView(rootView);
    }
}
