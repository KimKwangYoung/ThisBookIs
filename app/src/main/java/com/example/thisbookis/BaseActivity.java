package com.example.thisbookis;

import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {

    /* Activity에서 활용할 SharedPreferences value들 */
    public static boolean isLoggedIn;
    public static int typeOfLogin;

    /* User id key */
    public static String userIDKey;

    public void setLoginData(){
        SharedPreferences sp = getSharedPreferences("loginData", MODE_PRIVATE);
        isLoggedIn = sp.getBoolean(BaseApplication.LOGIN_DATA_CHECK_KEY, false);
        typeOfLogin = sp.getInt(BaseApplication.LOGIN_DATA_TYPE_OF_LAST_LOGIN_KEY, 1000);
    }

    public void progressON(){
        BaseApplication.getInstance().progressON(this);
    }

    public void progressOFF(){
        BaseApplication.getInstance().progressOFF();
    }
}
