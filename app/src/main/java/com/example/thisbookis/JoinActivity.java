package com.example.thisbookis;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.CursorLoader;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.thisbookis.data.User;
import com.facebook.login.Login;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.nhn.android.naverlogin.OAuthLogin;

import java.io.File;
import java.util.List;
import java.util.regex.Pattern;

public class JoinActivity extends BaseActivity implements View.OnClickListener{

    public final static String TAG = "JoinActivity";

    Context mContext;

    EditText nicknameEditText;
    Button nicknameCheckButton, joinRequestButton;
    ImageView profileImageView, profileUploadButton, profileImageClearButton;

    String profileImagePath;
    String profileImageStoragePath;
    String userNickname;
    String imageDownloadUrl;
    boolean isValidNickname = false;

    private static final int FROM_ALBUM = 100;

    RequestOptions options;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        mContext = this;

        initView();

        options = RequestOptions.bitmapTransform(new CircleCrop())
                .error(R.drawable.ic_user_profile);

        setLoginData();
    }

    private void initView(){
        nicknameEditText = findViewById(R.id.join_nick_name_et);
        profileImageView = findViewById(R.id.join_profile_iv);
        profileUploadButton = findViewById(R.id.join_upload_profile_image_btn);
        profileImageClearButton = findViewById(R.id.join_profile_clear_btn);
        joinRequestButton = findViewById(R.id.join_request_btn);
        nicknameCheckButton = findViewById(R.id.join_nick_name_check_btn);

        profileImageClearButton.setOnClickListener(this);
        profileUploadButton.setOnClickListener(this);
        joinRequestButton.setOnClickListener(this);
        nicknameCheckButton.setOnClickListener(this);
    }


    private void checkPermission() {

        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                getImageFromAlbum();
                Log.d(TAG, "권한 허용 됨");

            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                Toast.makeText(mContext, "권한이 거부 되었습니다.", Toast.LENGTH_SHORT).show();
            }
        };

        TedPermission.with(mContext).setPermissionListener(permissionListener)
                .setDeniedMessage("권한이 거부되었습니다. \n프로필 사진을 변경하고 싶으시다면 설정 > 권한에서 허용해주세요")
                .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
                .setGotoSettingButton(true)
                .check();
    }

    /* 각 로그인 타입에 맞게 logout하여 token 삭제하기 ... 이미 연동은 되어있는 상태 */

    private void cancelJoin(){
        switch (typeOfLogin){
            case BaseApplication.LOGIN_WITH_KAKAO:
                UserManagement.getInstance().requestLogout(new LogoutResponseCallback() {
                    @Override
                    public void onCompleteLogout() {
                        removeSharedPreferencesValues();
                        redirectLoginActivity();
                    }
                });
                break;
            case BaseApplication.LOGIN_WITH_FACEBOOK:
                LoginManager.getInstance().logOut();
                removeSharedPreferencesValues();
                redirectLoginActivity();
                break;
            case BaseApplication.LOGIN_WITH_NAVER:
                OAuthLogin.getInstance().logout(mContext);
                if(OAuthLogin.getInstance().getAccessToken(mContext)==null){
                    Log.e(TAG, "네이버 로그아웃 성공");
                }
                removeSharedPreferencesValues();
                redirectLoginActivity();
                break;
        }
    }

    private void removeSharedPreferencesValues(){
        SharedPreferences.Editor editor = BaseApplication.loginData.edit();
        editor.remove(BaseApplication.LOGIN_DATA_CHECK_KEY);
        editor.remove(BaseApplication.LOGIN_DATA_TYPE_OF_LAST_LOGIN_KEY);
        editor.commit();
        setLoginData();
    }

    /* 앨범에서 이미지를 Uri로 가져옴 */
    private void getImageFromAlbum(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, FROM_ALBUM);
    }

    /* Glide를 사용하여 imageView에 업로드 */
    private void setImage(Uri imageUri){
        Glide.with(mContext).load(imageUri).apply(options).into(profileImageView);
    }

    /* 이미지 뷰에서 사진 삭제하고 이미지 경로 지우기 */
    private void clearProfileImage(){
        Glide.with(mContext).load(R.drawable.ic_account_circle_grey_700_36dp).apply(options).into(profileImageView);
        profileImagePath = null;
    }

    private String getPath(Uri uri){
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader cursorLoader = new CursorLoader(mContext, uri, proj, null, null, null);

        Cursor cursor = cursorLoader.loadInBackground();
        int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        cursor.moveToFirst();

        return cursor.getString(index);
    }



    private void uploadProfileImage(String imagePath){

        progressON();

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();

        Uri file = Uri.fromFile(new File(imagePath));
        profileImageStoragePath = userIDKey+"/"+file.getPathSegments();
        final StorageReference profileRef = storageRef.child(profileImageStoragePath);
        UploadTask uploadTask = profileRef.putFile(file);

        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                Log.d(TAG, "Upload profile image successfully!");
                getDownloadUrl(profileRef);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                BaseApplication.showDialog(mContext, "프로필 이미지 등록에 실패하였습니다.\n다시 시도하여 주세요", null);
                Log.e(TAG, "Upload profile image failure!! : " + e.getMessage());
                progressOFF();
            }
        });
    }

    private void getDownloadUrl(StorageReference ref){
        ref.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                imageDownloadUrl = task.getResult().toString();
                setUserData();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                BaseApplication.showDialog(mContext, "프로필 이미지 등록에 실패하였습니다.\n다시 시도하여 주세요", null);
                Log.e(TAG, "Upload profile image failure!! : " + e.getMessage());
                progressOFF();
            }
        });
    }

    /* 닉네임 유효성 체크 */
    private void checkValidNickname(){
        userNickname = nicknameEditText.getText().toString();

        String pattern = "^[ㄱ-ㅎ가-힣a-zA-Z0-9]*$";
        boolean result = Pattern.matches(pattern, userNickname); /* 특수문자 포함 검사 */

        if(userNickname == null || userNickname.length() < 2){
            BaseApplication.showDialog(mContext, "닉네임은 2글자 이상이여야 합니다.", null);
            return;
        }
        if(result == false /* 닉네임 특수문자 포함 검사 */){
            BaseApplication.showDialog(mContext, "닉네임에 특수문자를 제외한 2글자 이상입니다.", null);
            return;
        }

        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_user_data_key));

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    if(userNickname.equals(ds.getValue(User.class).getNickname())){
                        isValidNickname = false;
                        BaseApplication.showDialog(mContext, "이미 존재하는 닉네임 입니다.", null);
                        return;
                    }
                }

                isValidNickname = true;
                BaseApplication.showDialog(mContext, "사용 가능한 닉네임 입니다.", null);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setUserData() {
        progressON();

        final User user = new User();
        if(imageDownloadUrl != null){
            user.setProfileURL(imageDownloadUrl);
            user.setProfilePath(profileImageStoragePath);
        }
        user.setUserId(userIDKey);
        user.setNickname(userNickname);

        /* Firebase에 유저 정보 등록 */
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference().child("user").child(userIDKey);
        mRef.setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(mContext, "회원가입을 완료 하였습니다!", Toast.LENGTH_SHORT).show();
                BaseApplication.userData = user;
                Intent intent = new Intent(mContext, MainActivity.class);
                startActivity(intent);
                progressOFF();
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressOFF();
                BaseApplication.showDialog(mContext, "회원 가입에 실패하였습니다.\n"+e.getMessage(), null);
            }
        });
    }

    private void redirectLoginActivity(){
        Intent intent = new Intent(mContext, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        cancelJoin();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.join_upload_profile_image_btn:
                checkPermission();
                break;
            case R.id.join_profile_clear_btn:
                clearProfileImage();
                break;
            case R.id.join_nick_name_check_btn:
                checkValidNickname();
                break;
            case R.id.join_request_btn:
                if(isValidNickname == false){
                    BaseApplication.showDialog(mContext, "닉네임이 유효하지 않습니다.\n중복검사를 실행하여 주세요", null);
                    break;
                }else{
                    if(profileImagePath != null){
                        uploadProfileImage(profileImagePath);
                    }else{
                        setUserData();
                    }//End if
                    break;
                }//End if

        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == FROM_ALBUM && data != null){
            profileImagePath = getPath(data.getData());
            File file = new File(profileImagePath);
            setImage(Uri.fromFile(file));

        }
    }
}
