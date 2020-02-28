package com.example.thisbookis;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.kakao.auth.ApiResponseCallback;
import com.kakao.auth.AuthService;
import com.kakao.auth.network.response.AccessTokenInfoResponse;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.nhn.android.naverlogin.OAuthLogin;


public class LoadingActivity extends BaseActivity {

    public final static String TAG = "LoadingActivity";

    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        mContext = this;

        getSharedPreferencesValues();

        Log.d(TAG, "isLoggedIn : " + isLoggedIn + " typeOfLogin : " + typeOfLogin);
        if (isLoggedIn) {
            switch (typeOfLogin){
                case BaseApplication.LOGIN_WITH_KAKAO:
                    checkKakaoAccessTokenAndGetUserId();
                    break;
                case BaseApplication.LOGIN_WITH_NAVER:
                    BaseApplication.getNaverSecretKeyFromDatabase(mContext);
                    break;
                case BaseApplication.LOGIN_WITH_FACEBOOK:
                    checkFaceBookAccessTokenAndGetUserId();
                    break;
            }
        }else{
            startLoginActivity();
        }//End if
    }

    private void getSharedPreferencesValues(){
        isLoggedIn = BaseApplication.getLoginCheckValue();
        typeOfLogin = BaseApplication.getTypeOfLastLoginValue();
    }

    /** 연동된 소셜에서 Accesstoken 사용하여 user id 받아오기 **/

    private void checkKakaoAccessTokenAndGetUserId(){
        AuthService.getInstance().requestAccessTokenInfo(new ApiResponseCallback<AccessTokenInfoResponse>() {
            @Override
            public void onSessionClosed(ErrorResult errorResult) {
                Log.e(TAG, "onSessionClosed");
            }

            @Override
            public void onSuccess(AccessTokenInfoResponse result) {
                userIDKey = Long.toString(result.getUserId());
                BaseApplication.getUserData(mContext);
            }
        });
    }

    private void checkFaceBookAccessTokenAndGetUserId(){
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
        if(isLoggedIn){
            userIDKey = accessToken.getUserId();
            BaseApplication.getUserData(mContext);
        }
    }

    /** ********************* 소셜 별로 user id 받아오기 함수 끝 ********************* **/


    /* Firebase에서 user data 조회 후 가져오기 없을 시 소셜 로그아웃 처리 */

    private void startLoginActivity(){
        Intent intent = new Intent(LoadingActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    /* 소셜 로그인은 되어있으나 가입하지않아 정보가 없을때는 로그아웃 */
    private void socialLogout(){
        switch (typeOfLogin){
            case BaseApplication.LOGIN_WITH_KAKAO:
                UserManagement.getInstance().requestLogout(new LogoutResponseCallback() {
                    @Override
                    public void onCompleteLogout() {
                        startLoginActivity();
                    }
                });
            case BaseApplication.LOGIN_WITH_FACEBOOK:
                LoginManager.getInstance().logOut();
                startLoginActivity();
            case BaseApplication.LOGIN_WITH_NAVER:
                OAuthLogin.getInstance().logout(mContext);
                startLoginActivity();
        }
    }



    @Override
    public void onBackPressed() {
    }
}
