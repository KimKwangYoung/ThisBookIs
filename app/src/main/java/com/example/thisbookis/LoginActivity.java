package com.example.thisbookis;

import androidx.annotation.Nullable;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;

import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.LoginButton;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeV2ResponseCallback;
import com.kakao.usermgmt.response.MeV2Response;
import com.kakao.util.exception.KakaoException;

import com.nhn.android.naverlogin.OAuthLogin;
import com.nhn.android.naverlogin.ui.view.OAuthLoginButton;

import java.util.Arrays;

public class LoginActivity extends BaseActivity implements View.OnClickListener {

    public final String TAG = "LoginActivity";

    Context mContext;

    /* 카카오 로그인 부분 변수 */
    TextView kakaoCustomButton;
    LoginButton kakaoLoginButton;
    SessionCallback mSessionCallback;

    /* 페이스북 로그인 부분 변수 */
    private CallbackManager callbackManager;
    com.facebook.login.widget.LoginButton facebookLoginButton;
    TextView facebookCustomButton;

    /* 네이버 로그인 부분 변수 */
    static OAuthLogin mOAuthLoginModule;
    OAuthLoginButton naverLoginButton;
    TextView naverCustomButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mContext = this;

        initView();
        initData();

    }

    private void initView(){

        kakaoCustomButton = findViewById(R.id.login_kakao_custom_btn);
        kakaoLoginButton = findViewById(R.id.login_kakao_login_btn);
        facebookCustomButton = findViewById(R.id.login_facebook_custom_btn);
        facebookLoginButton = findViewById(R.id.login_facebook_login_btn);
        naverCustomButton = findViewById(R.id.login_naver_custom_btn);
        naverLoginButton = findViewById(R.id.login_naver_login_btn);
    }

    private void initData(){
        kakaoCustomButton.setOnClickListener(this);
        facebookCustomButton.setOnClickListener(this);
        naverCustomButton.setOnClickListener(this);

        /* 카카오톡 로그인 관련 설정 */
        mSessionCallback = new SessionCallback();
        Session.getCurrentSession().addCallback(mSessionCallback);

        /* 페이스북 로그인 관련 설정 */
        callbackManager = CallbackManager.Factory.create();
        facebookLoginButton.setReadPermissions(Arrays.asList("public_profile, email"));
        facebookLoginButton.registerCallback(callbackManager, setFacebookCallback());

        /* 네이버 로그인 관련 설정 */

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.login_kakao_custom_btn:
                kakaoLoginButton.performClick();
                break;
            case R.id.login_facebook_custom_btn:
                facebookLoginButton.performClick();
                break;
            case R.id.login_naver_custom_btn:
                BaseApplication.getNaverSecretKeyFromDatabase(mContext);
                break;
        }
    }


    /* 로그인 여부와 로그인 방법 SharedPreferences에 저장 */
    public void saveValueToSharedPreferences(boolean isLoggedIn, int typeOfLogin){
        Log.e(TAG, "SharedPreferences Data : " + isLoggedIn + " / " + typeOfLogin);
        SharedPreferences.Editor editor = BaseApplication.loginData.edit();
        editor.putBoolean(BaseApplication.LOGIN_DATA_CHECK_KEY, isLoggedIn);
        editor.putInt(BaseApplication.LOGIN_DATA_TYPE_OF_LAST_LOGIN_KEY, typeOfLogin);
        editor.commit();

        setLoginData();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }
        callbackManager.onActivityResult(requestCode, resultCode, data);

    }

    /* 카카오 로그인 결과 관련 콜백 정의 */
    private class SessionCallback implements ISessionCallback{

        @Override
        public void onSessionOpened() {
            requestMe();
        }

        @Override
        public void onSessionOpenFailed(KakaoException exception) {
            Log.e(TAG, "Login Failed::" + exception.getErrorType() + " " + exception.getMessage());
        }

        public void requestMe(){
            UserManagement.getInstance().me(new MeV2ResponseCallback() {
                @Override
                public void onSessionClosed(ErrorResult errorResult) {
                    Log.e(TAG, "onSessionClosed");
                }

                @Override
                public void onSuccess(MeV2Response result) {
                    saveValueToSharedPreferences(true, BaseApplication.LOGIN_WITH_KAKAO);
                    userIDKey = Long.toString(result.getId());
                    BaseApplication.getUserData(mContext);
                    Log.e(TAG, "재실행 체크");
                    return;
                }

                @Override
                public void onFailure(ErrorResult errorResult) {
                    Log.e(TAG, "onFailure::" + errorResult.getErrorMessage());
                }
            });
        }
    }


    /* 페이스북 로그인 시도 응답에 대한 처리 */
    public FacebookCallback setFacebookCallback(){

        FacebookCallback<LoginResult> facebookCallback = new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                saveValueToSharedPreferences(true, BaseApplication.LOGIN_WITH_FACEBOOK);
                userIDKey = loginResult.getAccessToken().getUserId();
                BaseApplication.getUserData(mContext);
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        };

        return facebookCallback;
    }




}
