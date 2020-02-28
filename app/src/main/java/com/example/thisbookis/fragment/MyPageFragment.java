package com.example.thisbookis.fragment;


import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
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
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.thisbookis.BaseActivity;
import com.example.thisbookis.BaseApplication;
import com.example.thisbookis.LoginActivity;
import com.example.thisbookis.MyBooksActivity;
import com.example.thisbookis.MyReportsActivity;
import com.example.thisbookis.SettingAcitivty;
import com.example.thisbookis.R;
import com.example.thisbookis.data.User;
import com.facebook.login.LoginManager;
import com.kakao.auth.authorization.AuthorizationResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.nhn.android.naverlogin.OAuthLogin;


public class MyPageFragment extends Fragment implements View.OnClickListener {

    public final static String TAG = "MyPageFragment";

    private final int MODIFY_PROFILE_REQUEST_CODE = 100;
    Fragment fragment;

    ImageView profileImageView;
    TextView profileNickNameTextView, myBooksCountTextView, myReportsCountTextView;
    LinearLayout settingButton, myBooksButton, myReportsButton;

    Button logoutButton;

    User userData;
    ViewGroup rootView;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_my_page, container, false);

        Log.d(TAG, "onCreateView");

        userData = new User();

        fragment = this;

        initData();
        initView(rootView);


        return rootView;
    }

    private void initView(ViewGroup rootView){
        profileImageView = rootView.findViewById(R.id.my_page_profile_image_iv);
        profileNickNameTextView = rootView.findViewById(R.id.my_page_nickname_tv);
        logoutButton = rootView.findViewById(R.id.my_page_logout_btn);
        settingButton = rootView.findViewById(R.id.my_page_setting_modify_btn);
        myBooksButton = rootView.findViewById(R.id.my_page_my_books_btn);
        myBooksCountTextView = rootView.findViewById(R.id.my_page_my_books_cnt_tv);
        myReportsCountTextView = rootView.findViewById(R.id.my_page_my_reports_cnt_tv);
        myReportsButton = rootView.findViewById(R.id.my_page_my_reports_btn);


        logoutButton.setOnClickListener(this);
        settingButton.setOnClickListener(this);
        myBooksButton.setOnClickListener(this);
        myReportsButton.setOnClickListener(this);

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

    }

    private void initData(){
        userData = BaseApplication.userData;
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
        editor.commit();
    }

    private void redirectLoginActivity(){
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    /* ############################## 로그아웃 코드  ##############################*/

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.my_page_logout_btn:
                logout();
                break;
            case R.id.my_page_setting_modify_btn:
                Intent settingIntent = new Intent(getActivity(), SettingAcitivty.class);
                startActivityForResult(settingIntent, MODIFY_PROFILE_REQUEST_CODE);
                break;
            case R.id.my_page_my_books_btn:
                Log.d(TAG, BaseApplication.userData.getMyBooks()+" ");
                Intent booksIntent = new Intent(getActivity(), MyBooksActivity.class);
                startActivity(booksIntent);
                break;
            case R.id.my_page_my_reports_btn:
                Intent reportIntent = new Intent(getActivity(), MyReportsActivity.class);
                startActivity(reportIntent);
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
        initView(rootView);
    }
}
