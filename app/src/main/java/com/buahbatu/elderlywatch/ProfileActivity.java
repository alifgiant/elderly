package com.buahbatu.elderlywatch;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.OkHttpResponseAndJSONObjectRequestListener;
import com.androidnetworking.interfaces.OkHttpResponseAndStringRequestListener;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Response;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = ProfileActivity.class.getSimpleName();

    @BindView(R.id.text_full_name) EditText mEditFullName;
    @BindView(R.id.text_address) EditText mEditAddress;
    @BindView(R.id.text_phone_number) EditText mEditPhoneNum;
    @BindView(R.id.text_birth_date) EditText mEditBirthDate;

    @OnClick(R.id.button_submit) void onUpdateSubmit(){
        mEditFullName.setError(null);
        mEditAddress.setError(null);
        mEditPhoneNum.setError(null);
        mEditBirthDate.setError(null);

        if (TextUtils.isEmpty(mEditFullName.getText()))
            mEditFullName.setError("Silahkan diisi");
        else if (TextUtils.isEmpty(mEditAddress.getText()))
            mEditAddress.setError("Silahkan diisi");
        else if (TextUtils.isEmpty(mEditPhoneNum.getText()))
            mEditPhoneNum.setError("Silahkan diisi");
        else if (TextUtils.isEmpty(mEditBirthDate.getText()))
            mEditBirthDate.setError("Silahkan diisi");
        else {
            updateData(mEditFullName.getText().toString(), mEditAddress.getText().toString(),
                    mEditPhoneNum.getText().toString(), mEditBirthDate.getText().toString());
        }

    }

    @OnClick(R.id.button_cancel) void onCancelClick(){
        finish();
    }

    void updateData(String fullName, String address, String phoneNum, String birthDate){
        final ProgressDialog dialog = new ProgressDialog(ProfileActivity.this);
        dialog.setMessage("Saving user data");
        dialog.setIndeterminate(true);
        dialog.show();
        AndroidNetworking.post(getString(R.string.api_set_profile))
                .setPriority(Priority.MEDIUM)
                .addHeaders("Cookie", AppSetting.getCookie(ProfileActivity.this))
                .addBodyParameter("full_name", fullName)
                .addBodyParameter("address", address)
                .addBodyParameter("phone_number", phoneNum)
                .addBodyParameter("birth_date", birthDate)
                .build()
                .getAsOkHttpResponseAndString(new OkHttpResponseAndStringRequestListener() {
                    @Override
                    public void onResponse(Response okHttpResponse, String response) {
                        Log.i(TAG, "onResponse: " + response);
                        String cookie = okHttpResponse.header("Set-Cookie", "");
                        AppSetting.setCookie(ProfileActivity.this, cookie);
                        if (TextUtils.equals(response, "1")){
                            Toast.makeText(ProfileActivity.this, "Menyimpan data sukses",
                                    Toast.LENGTH_SHORT).show();
                            setResult(MainActivity.CODE_UPDATE_REQUEST_SUCCESS);
                            finish();
                        }else {
                            Toast.makeText(ProfileActivity.this, "Menyimpan data gagal",
                                    Toast.LENGTH_SHORT).show();
                        }
                        dialog.dismiss();
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.i(TAG, "onError: " + anError.getErrorDetail());
                        Toast.makeText(ProfileActivity.this, "Menyimpan data gagal 2",
                                Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
    }

    void setupViews(){
        mEditFullName.setText(getIntent().getStringExtra(MainActivity.KEY_FULL_NAME));
        mEditAddress.setText(getIntent().getStringExtra(MainActivity.KEY_ADDRESS));
        mEditPhoneNum.setText(getIntent().getStringExtra(MainActivity.KEY_PHONE_NUMBER));
        mEditBirthDate.setText(getIntent().getStringExtra(MainActivity.KEY_BIRTH_DATE));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ButterKnife.bind(this);
        AndroidNetworking.initialize(ProfileActivity.this);

        setupViews();
    }
}
