package com.example.thisbookis;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.CursorLoader;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.thisbookis.data.User;
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

import java.io.File;
import java.util.List;
import java.util.regex.Pattern;

public class SettingAcitivty extends BaseActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, TextWatcher {

    public final static String TAG = "SettingActivity";

    private final static int FROM_ALBUM = 100;

    Context mContext;

    ImageView profilImageView, profileImageUpdateButton, profileImageClearButton;
    EditText nicknameEditText;
    Button checkNicknameButton, modifyButton;
    Switch shareSwitch;
    TextView reportShareTextView;

    String newProfileImagePath;
    String newProfileImageStoreReference;
    String newProfileImageUrl;
    String newProfileNickname;
    boolean isChangedProfileImage = false;
    boolean isChangedNickname = false;
    boolean isValidNickname = false;
    boolean shouldShare;
    User userBeforeChange, userAfterChange;

    RequestOptions options;


    DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        mContext = this;



        userBeforeChange = BaseApplication.userData;
        userAfterChange = new User();
        userAfterChange.setUserId(userBeforeChange.getUserId());
        shouldShare = userBeforeChange.isShouldShareReport();

        Log.d(TAG, userBeforeChange.getNickname() + " " + userBeforeChange.getUserId());

        userRef = databaseRef.child(getString(R.string.firebase_user_data_key)).child(userBeforeChange.getUserId());

        options = RequestOptions.bitmapTransform(new CircleCrop()).error(R.drawable.ic_user_profile);
        initView();

    }

    private void initView(){
        profilImageView = findViewById(R.id.setting_profile_iv);
        profileImageUpdateButton = findViewById(R.id.setting_update_profile_image_btn);
        profileImageClearButton = findViewById(R.id.setting_profile_clear_btn);
        modifyButton = findViewById(R.id.setting_modify_request_btn);
        nicknameEditText = findViewById(R.id.setting_nick_name_et);
        checkNicknameButton = findViewById(R.id.setting_nick_name_check_btn);
        shareSwitch = findViewById(R.id.setting_sw);
        reportShareTextView = findViewById(R.id.setting_report_share_tv);

        Glide.with(mContext).load(userBeforeChange.getProfileURL()).apply(options).into(profilImageView);
        nicknameEditText.setText(userBeforeChange.getNickname());

        profilImageView.setOnClickListener(this);
        profileImageUpdateButton.setOnClickListener(this);
        profileImageClearButton.setOnClickListener(this);
        modifyButton.setOnClickListener(this);
        checkNicknameButton.setOnClickListener(this);
        shareSwitch.setOnCheckedChangeListener(this);
        nicknameEditText.addTextChangedListener(this);

        if(userBeforeChange.isShouldShareReport()){
            shareSwitch.setChecked(true);
        }else{
            shareSwitch.setChecked(false);
        }
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

    private void getImageFromAlbum(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, FROM_ALBUM);
    }

    private String getPath(Uri uri){
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader cursorLoader = new CursorLoader(mContext, uri, proj, null, null, null);

        Cursor cursor = cursorLoader.loadInBackground();
        int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        cursor.moveToFirst();

        return cursor.getString(index);
    }

    private void removeProfileImageInStorage(){
        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child(userBeforeChange.getProfilePath()); /* 유저 프로필 이미지 저장 경로 */

        storageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                if(newProfileImagePath != null && newProfileImagePath.equals("")) {
                    Log.d(TAG, "프로필 이미지 삭제 완료");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                BaseApplication.showErrorToast(mContext, "기존 이미지 삭제에 실패하였습니다. 다시 시도하여 주세요");
                progressOFF();
                e.printStackTrace();
            }
        });
    }

    private void uploadProfileImage(){

        if(!isChangedProfileImage){ /* 프로필 이미지가 바뀌지 않았다면 기존 path와 url로 설정 */
            userAfterChange.setProfilePath(userBeforeChange.getProfilePath());
            userAfterChange.setProfileURL(userBeforeChange.getProfileURL());
            updateProfile();
            return;
        }

        if(newProfileImagePath == null){ /* 프로필 이미지를 삭제하기만 한 경우 path와 url을 비워준 상태에서 update*/
            removeProfileImageInStorage();
            updateProfile();
            return;
        }

        if(userBeforeChange.getProfileURL() != null) {
            removeProfileImageInStorage();
        }

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        Uri file = Uri.fromFile(new File(newProfileImagePath));
        newProfileImageStoreReference = userBeforeChange.getUserId()+"/"+file.getPathSegments();
        final StorageReference profileRef = storageRef.child(newProfileImageStoreReference);
        UploadTask uploadTask = profileRef.putFile(file);

        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                Log.d(TAG, "이미지 업로드 성공");
                setProfileImageUrl(profileRef);
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

    private void setProfileImageUrl(StorageReference ref){
        ref.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                newProfileImageUrl = task.getResult().toString();
                Log.d(TAG, newProfileImageUrl + " " + newProfileImagePath);
                userAfterChange.setProfileURL(newProfileImageUrl);
                userAfterChange.setProfilePath(newProfileImageStoreReference);
                updateProfile();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                BaseApplication.showErrorToast(mContext, "프로필 이미지 등록에 실패하였습니다.\n다시 시도하여 주세요");
                Log.e(TAG, "프로필 이미지 url 받아오기 실패 : " + e.getMessage());
                progressOFF();
            }
        });
    }

    private void checkValidNickname(){
        newProfileNickname = nicknameEditText.getText().toString();

        isChangedNickname = true;

        String pattern = "^[ㄱ-ㅎ가-힣a-zA-Z0-9]*$";
        boolean result = Pattern.matches(pattern, newProfileNickname); /* 특수문자 포함 검사 */

        if(newProfileNickname == null || newProfileNickname.length() < 2){
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
                    if(newProfileNickname.equals(ds.getValue(User.class).getNickname())){
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

    private void setNickname(){
        if(isChangedNickname){
            if(isValidNickname){
                userAfterChange.setNickname(nicknameEditText.getText().toString());
            }else{
                Toast.makeText(mContext, "닉네임 중복검사를 해주세요", Toast.LENGTH_SHORT).show();
                progressOFF();
                return;
            }
        }else{
            userAfterChange.setNickname(userBeforeChange.getNickname());
        }

        uploadProfileImage();
    }

    private void updateProfile(){
        userAfterChange.setShouldShareReport(shouldShare);
        userAfterChange.setMyBooks(userBeforeChange.getMyBooks());
        userAfterChange.setReports(userBeforeChange.getReports());
        userRef.setValue(userAfterChange).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(mContext, "적용 되었습니다.", Toast.LENGTH_SHORT).show();
                progressOFF();
                BaseApplication.userData = userAfterChange;
                setResult(200);
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                BaseApplication.showDialog(mContext, "프로필 수정에 실패하였습니다.\n다시 시도해 주세요", null);
                Log.e(TAG, "Update Error : " + e.getMessage());
            }
        });
    }

    private void withdrawUserAccount(){
        if(userBeforeChange.getProfileURL() != null){
            removeProfileImageInStorage();
        }


    }



    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.setting_update_profile_image_btn:
                checkPermission();
                break;
            case R.id.setting_nick_name_check_btn:
                checkValidNickname();
                break;
            case R.id.setting_profile_clear_btn:
                Glide.with(mContext).load(R.drawable.ic_user_profile).apply(options).into(profilImageView);
                newProfileImagePath = null;
                isChangedProfileImage = true;
                break;
            case R.id.setting_modify_request_btn:
                progressON();
                setNickname();
                break;
            case R.id.setting_withdraw_btn:
                BaseApplication.showDialog(mContext, "회원 탈퇴 하시겠습니까?\n회원 탈퇴 후에는 작성하셨던 독후감 등 모든 정보는 삭제되어 복원할 수 없습니다."
                        , new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                withdrawUserAccount();
                            }
                        });
                break;

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data != null && requestCode == FROM_ALBUM){
            newProfileImagePath = getPath(data.getData());
            File file = new File(newProfileImagePath);
            isChangedProfileImage = true;
            Glide.with(mContext).load(file).apply(options).into(profilImageView);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        shouldShare = b;
        Log.d(TAG, "onCheckedChanged() : shouldShare :" + shouldShare);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        isValidNickname = false;
        isChangedNickname = true;
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }
}
