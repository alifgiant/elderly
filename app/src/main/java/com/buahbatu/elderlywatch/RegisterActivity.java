package com.buahbatu.elderlywatch;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.OkHttpResponseAndStringRequestListener;
import com.androidnetworking.interfaces.StringRequestListener;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = RegisterActivity.class.getSimpleName();
    private final ProgressDialog dialog = new ProgressDialog(RegisterActivity.this);

    @BindView(R.id.text_username) EditText mEditUsername;
    @BindView(R.id.text_password) EditText mEditPassword;
    @BindView(R.id.spinner_role) Spinner mSpinnerRole;
    @BindView(R.id.text_full_name) EditText mEditFullName;
    @BindView(R.id.text_address) EditText mEditAddress;
    @BindView(R.id.text_day) EditText mEditBirthDay;
    @BindView(R.id.text_month) EditText mEditBirthMonth;
    @BindView(R.id.text_year) EditText mEditBirthYear;
    @BindView(R.id.text_phone_number) EditText mEditPhoneNumber;

    @OnClick(R.id.button_register) void onRegisterClick(){
        if (isNoErrorInput()){
            String username = mEditUsername.getText().toString();
            String password = mEditPassword.getText().toString();
            String role = "2";
            switch ((String)mSpinnerRole.getSelectedItem()){
                case "Keluarga" : role = "2"; break;
                case "Dokter" : role = "3"; break;
                case "Pasien" : role = "4"; break;
            }

            // show dialog
            dialog.setMessage("Loading");
            dialog.setIndeterminate(true);
            dialog.show();

            doRegister(username, password, role, new OnRegisterFinishedListener() {
                @Override
                public void onRegisterSuccess() {
                    String fullName = mEditFullName.getText().toString();
                    String address = mEditAddress.getText().toString();
                    String dob = mEditBirthDay.getText().toString() +
                            "/" + mEditBirthMonth.getText().toString() +
                            "/" + mEditBirthYear.getText().toString();
                    String phone = mEditPhoneNumber.getText().toString();
                    doSetupProfile(fullName, address, dob, phone);
                }

                @Override
                public void onRegisterFailed() {
                    Toast.makeText(RegisterActivity.this, "Anda gagal didaftarkan",
                            Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            });
        }
    }

    boolean isNoErrorInput(){
        mEditUsername.setError(null);
        mEditPassword.setError(null);
        mEditFullName.setError(null);
        mEditAddress.setError(null);
        mEditBirthDay.setError(null);
        mEditBirthMonth.setError(null);
        mEditBirthYear.setError(null);
        mEditPhoneNumber.setError(null);

        if (TextUtils.isEmpty(mEditUsername.getText())){
            mEditUsername.setError("Silahkan diisi");
            return false;
        }else if(TextUtils.isEmpty(mEditPassword.getText()) && mEditPassword.getText().length() >= 8 && mEditPassword.getText().length() <= 64){
            mEditPassword.setError("Silahkan diisi");
            return false;
        }else if(TextUtils.isEmpty(mEditFullName.getText())){
            mEditPassword.setError("Silahkan diisi");
            return false;
        }else if(TextUtils.isEmpty(mEditAddress.getText())){
            mEditPassword.setError("Silahkan diisi");
            return false;
        }else if(TextUtils.isEmpty(mEditBirthDay.getText()) && mEditBirthDay.getText().length() == 2){
            mEditPassword.setError("Silahkan diisi");
            return false;
        }else if(TextUtils.isEmpty(mEditBirthMonth.getText()) && mEditBirthMonth.getText().length() == 2){
            mEditPassword.setError("Silahkan diisi");
            return false;
        }else if(TextUtils.isEmpty(mEditBirthYear.getText()) && mEditBirthYear.getText().length() == 4){
            mEditPassword.setError("Silahkan diisi");
            return false;
        }else if(TextUtils.isEmpty(mEditPhoneNumber.getText())){
            mEditPassword.setError("Silahkan diisi");
            return false;
        }else {
            return true;
        }
    }

    void doRegister(String username, String password, String role, final OnRegisterFinishedListener listener){
        String address = AppSetting.getUrl(RegisterActivity.this)
                + AppSetting.getPort(RegisterActivity.this) + AppSetting.getSubPath(RegisterActivity.this);
        String url = String.format(Locale.US, getString(R.string.api_register), address);

        AndroidNetworking.post(url)
                .addBodyParameter("username", username)
                .addBodyParameter("password", password)
                .addBodyParameter("role", role)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        Log.i(TAG, "onResponse: " + response);
                        if (TextUtils.equals(response, "1")){
                            listener.onRegisterSuccess();
                        }else {
                            listener.onRegisterFailed();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        listener.onRegisterFailed();
                        Log.i(TAG, "onResponse: error");
                    }
                });
    }

    void doSetupProfile(String fullName, String address, String dob, String phoneNum){
        String ipAddress = AppSetting.getUrl(RegisterActivity.this)
                + AppSetting.getPort(RegisterActivity.this) + AppSetting.getSubPath(RegisterActivity.this);
        String url = String.format(Locale.US, getString(R.string.api_set_profile), ipAddress);

        AndroidNetworking.post(url)
                .setPriority(Priority.MEDIUM)
                .addHeaders("Cookie", AppSetting.getCookie(RegisterActivity.this))
                .addBodyParameter("full_name", fullName)
                .addBodyParameter("address", address)
                .addBodyParameter("birth_date", dob)
                .addBodyParameter("phone_number", phoneNum)
                .build()
                .getAsOkHttpResponseAndString(new OkHttpResponseAndStringRequestListener() {
                    @Override
                    public void onResponse(Response okHttpResponse, String response) {
                        Log.i(TAG, "onResponse: " + response);
                        String cookie = okHttpResponse.header("Set-Cookie", "");
                        AppSetting.setCookie(RegisterActivity.this, cookie);
                        if (TextUtils.equals(response, "1")){
                            Toast.makeText(RegisterActivity.this, "Anda berhasil didaftarkan",
                                    Toast.LENGTH_SHORT).show();
                            setResult(MainActivity.CODE_UPDATE_REQUEST_SUCCESS);
                            dialog.dismiss();
                            finish();
                        }else {
                            Toast.makeText(RegisterActivity.this, "Anda gagal didaftarkan",
                                    Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.i(TAG, "onError: " + anError.getErrorDetail());
                        Toast.makeText(RegisterActivity.this, "Anda gagal didaftarkan",
                                Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);

        // initialize networking lib
        AndroidNetworking.initialize(getApplicationContext());
    }

    interface OnRegisterFinishedListener{
        void onRegisterSuccess();
        void onRegisterFailed();
    }
}
