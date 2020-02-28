package com.example.thisbookis;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.nhn.android.naverlogin.OAuthLogin;
import com.nhn.android.naverlogin.OAuthLoginHandler;

import org.json.JSONObject;

import java.lang.ref.WeakReference;

public class NaverLogin {

    //TODO : 로그인 하고 토큰 유지 방법 알아보기
    public final static String TAG = "NaverLogin";

    public static OAuthLogin mOAuthLoginModule = OAuthLogin.getInstance();
    private static NaverLogin naverLogin = new NaverLogin();
    private static Context mContext;
    private NaverLogin() {

    }

    public static NaverLogin getInstance(Context context){
        mContext = context;
        mOAuthLoginModule.init(mContext, mContext.getString(R.string.naver_app_key)
                , BaseApplication.getInstance().naverSecretKey()
                , "ThisBookIs");
        return naverLogin;
    }

    public void startLogin(){
        mOAuthLoginModule = OAuthLogin.getInstance();
        mOAuthLoginModule.startOauthLoginActivity((Activity)mContext, mOAuthLoginHandler);
    }

    public OAuthLoginHandler mOAuthLoginHandler = new OAuthLoginHandler() {
        @Override
        public void run(boolean success) {
            if(success){
                String accessToken = mOAuthLoginModule.getAccessToken(mContext);
                NaverProfileTask task = new NaverProfileTask();
                task.execute(accessToken);
            }else{
                Log.e(TAG, "Naver login failure! ::" + mOAuthLoginModule.getLastErrorCode(mContext)
                        + " / " + mOAuthLoginModule.getLastErrorDesc(mContext));
            }
        }
    };

    class NaverProfileTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String token = strings[0];
            String apiURL = mContext.getString(R.string.naver_api_url);
            return mOAuthLoginModule.requestApi(mContext, token, apiURL);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONObject jsonObject = new JSONObject(s);
                BaseActivity.userIDKey = (String)jsonObject.getJSONObject("response").get("id");
                Log.e(TAG, "NaverProfileTask : 유저 ID 얻어오기 성공");
                BaseActivity.isLoggedIn = true;
                BaseActivity.typeOfLogin = BaseApplication.LOGIN_WITH_NAVER;
                BaseApplication.setSharedPreferences(true, BaseApplication.LOGIN_WITH_NAVER);
                BaseApplication.getUserData(mContext);
            }catch (Exception e){
                Log.e(TAG, "Fail to get user id" + e.getMessage());
            }
        }
    }
}
