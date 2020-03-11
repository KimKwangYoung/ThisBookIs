package com.ky.thisbookis;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialog;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.ky.thisbookis.data.SearchResult;
import com.ky.thisbookis.data.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kakao.auth.KakaoSDK;
import com.pd.chocobar.ChocoBar;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BaseApplication extends Application {

    public final static String TAG = "BaseApplication";

    public static BaseApplication baseApplication;

    /* 마지막 로그인 방법 상수 */
    public final static int LOGIN_CHECK_DEFAULT = 1003;
    public final static int LOGIN_WITH_KAKAO = 1001;
    public final static int LOGIN_WITH_FACEBOOK = 1002;
    public final static int LOGIN_WITH_NAVER = 1003;

    /* 마지막 로그인 방법과 로그인 정보를 저장 하기 위한 SharedPreferences */
    public static SharedPreferences loginData;

    /* loginData 키 값으로 사용될 String 상수 */
    public final static String LOGIN_DATA_CHECK_KEY = "isLoggedIn";
    public final static String LOGIN_DATA_TYPE_OF_LAST_LOGIN_KEY = "typeOfLogin";

    public static User userData = null;
    private static String naverSecretKey;

    public static RequestOptions profileImageOptions = RequestOptions.bitmapTransform(new CircleCrop()).skipMemoryCache(true)
            .error(R.drawable.ic_user_profile);

    AppCompatDialog progressDialog;

    public static BaseApplication getInstance(){
        if(baseApplication == null){
            throw new IllegalStateException("this application does not inherit com.kakao.BaseApplication");
        }
        return baseApplication;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        baseApplication = this;
        KakaoSDK.init(new KakaoSDKAdapter());
        if(loginData == null){
            getPreferences();
        }

    }

    public SharedPreferences getPreferences(){
       return loginData = getSharedPreferences("loginData", MODE_PRIVATE);
    }

    public static void showDialog(final Context context, String message, @Nullable DialogInterface.OnClickListener clickListener){
        AlertDialog.Builder joinDialog_builder;
        joinDialog_builder = new AlertDialog.Builder(context);
        if(clickListener == null) {
            joinDialog_builder.setMessage(message).setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
        }else{
            joinDialog_builder.setMessage(message).setPositiveButton("확인", clickListener)
                    .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
        } //End if

        final AlertDialog joinDialog = joinDialog_builder.create();
        joinDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                joinDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(context, R.color.mainColor));
                joinDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(context, R.color.mainColor));
            }
        });
        joinDialog.show();
    }

    /* 로그인 여부 */
    public static boolean getLoginCheckValue(){
        return loginData.getBoolean(LOGIN_DATA_CHECK_KEY, false);
    }

    /* 최근 로그인 방법 */
    public static int getTypeOfLastLoginValue(){
        return loginData.getInt(LOGIN_DATA_TYPE_OF_LAST_LOGIN_KEY, 1000);
    }

    public static void setSharedPreferences(boolean isLoggedin, int typeOfLogin){
        SharedPreferences.Editor editor = loginData.edit();
        editor.putBoolean(LOGIN_DATA_CHECK_KEY, isLoggedin);
        editor.putInt(LOGIN_DATA_TYPE_OF_LAST_LOGIN_KEY, typeOfLogin);
        editor.commit();
    }

    /**
     *  Progress Dialog 관련 코드
     *  BaseActivity와 BaseFragment를 만들어 밑에 함수들을 구현해 Activity들과 Fragment에서 사용할 수 있도록 함
     **/
    public void progressON(Activity activity){
        if(activity != null && activity.isFinishing()){
            return;
        }

        if (progressDialog != null && progressDialog.isShowing()){
            return;
        }else{

            progressDialog = new AppCompatDialog(activity);
            progressDialog.setCancelable(false);
            progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            progressDialog.setContentView(R.layout.loading_dialog);
            progressDialog.show();

        }

        ImageView dialogImageView = progressDialog.findViewById(R.id.loading_iv);
        final AnimationDrawable loadingAnimation = (AnimationDrawable) dialogImageView.getBackground();
        dialogImageView.post(new Runnable() {
            @Override
            public void run() {
                loadingAnimation.start();
            }
        });

    }

    public void progressOFF(){
        if(progressDialog != null && progressDialog.isShowing()){
            progressDialog.dismiss();
        }
    }

    public static void getUserData(final Context mContext){
        DatabaseReference mRef;
        mRef = FirebaseDatabase.getInstance().getReference().child(mContext.getString(R.string.firebase_user_data_key));
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String userIDKey = BaseActivity.userIDKey;
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    if(ds.getKey().equals(userIDKey)){/* 유저 정보가 있다면 메인 액티비티로 이동 */
                        userData = ds.getValue(User.class);
                        Log.d(TAG, "getUserData() : 로그인 유저 정보 가져오기 성공");
                        Intent intent = new Intent(mContext, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        mContext.startActivity(intent);
                        Activity activity = (Activity) mContext;
                        activity.finish();
                        return;
                    }
                }//End for

                /* 유저 정보가 없다면 회원가입 권유 dialog 띄워주기 */
                if(!((Activity)mContext).isFinishing()) {
                    showDialog(mContext, "회원가입이 되어 있지 않습니다.\n회원가입 하시겠어요?", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(mContext, JoinActivity.class);
                            mContext.startActivity(intent);
                            Activity activity = (Activity) mContext;
                            activity.finish();
                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static void showCompleteToast(Context context, String message){
        ChocoBar.builder().setBackgroundColor(context.getResources().getColor(R.color.mainColor, null))
                .setTextSize(13)
                .setMaxLines(1)
                .setText(message)
                .setActivity((Activity)context)
                .build().show();
    }

    public static void showErrorToast(Context context, String message){
        ChocoBar.builder().setBackgroundColor(Color.parseColor("#FF0000"))
                .setTextSize(13)
                .setMaxLines(1)
                .setText(message)
                .setActivity((Activity)context)
                .build().show();
    }

    public static void showInfoToast(Context context, String message){
        ChocoBar.builder().setBackgroundColor(Color.parseColor("#55000000"))
                .setTextSize(13)
                .setMaxLines(1)
                .setText(message)
                .setActivity((Activity)context)
                .build().show();
    }

    public static String replaceString(String s){
        s = s.replace("[","");
        s = s.replace("]","");
        return s;

    }

    public static void getNaverSecretKeyFromDatabase(Context context){
        DatabaseReference keyRef = FirebaseDatabase.getInstance().getReference().child("key").child("naver");
        keyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                naverSecretKey = dataSnapshot.getValue(String.class);
                Log.d(TAG, "Naver Secret Key 받아오기 완료");
                NaverLogin.getInstance(context).startLogin();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public String naverSecretKey(){
        return naverSecretKey;
    }

    public static void moveToBookContentActivity(Context context, String isbn, String title){
        String authorizationKey = context.getString(R.string.kakao_api_key);
        Call<SearchResult> call = KakaoApiClient.getInstance().searchService.getBookList(authorizationKey, isbn, 50, 1);
        Callback<SearchResult> callback = new Callback<SearchResult>() {
            @Override
            public void onResponse(Call<SearchResult> call, Response<SearchResult> response) {
                if(response.isSuccessful()){
                    SearchResult searchResult = response.body();
                    List<SearchResult.Document> documentList = searchResult.getDocuments();
                    SearchResult.Document bookDocument = null;
                    if(documentList.size() == 1){
                        bookDocument = documentList.get(0);
                    }else{
                        for(SearchResult.Document d : documentList){
                            if(d.getTitle().equals(title)){
                                bookDocument = d;
                                break;
                            }
                        }
                    }//End if

                    if(bookDocument == null){
                        showInfoToast(context, "책 정보를 찾지 못했습니다. 검색창에서 검색하여 주세요");
                        return;
                    }
                    Intent intent = new Intent(context, BookContentsActivity.class);
                    intent.putExtra("book", bookDocument);
                    context.startActivity(intent);

                }else{
                    BaseApplication.showErrorToast(context, "책 정보를 읽어오지 못했습니다. 다시 시도하여 주세요");
                    Log.e(TAG, "Retrofit Error :: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<SearchResult> call, Throwable t) {
                Log.e(TAG, "getBookData() : isCanceled : " + call.isCanceled() + " Error Message : " + t.getMessage());
            }
        };

        call.enqueue(callback);
    }

}
