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
import com.androidnetworking.interfaces.StringRequestListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = RegisterActivity.class.getSimpleName();

    @BindView(R.id.text_username) EditText mEditUsername;
    @BindView(R.id.text_password) EditText mEditPassword;
    @BindView(R.id.spinner_role) Spinner mSpinnerRole;

    @OnClick(R.id.button_register) void onRegisterClick(){
        mEditUsername.setError(null);
        mEditPassword.setError(null);
        if (TextUtils.isEmpty(mEditUsername.getText())){
            mEditUsername.setError("Silahkan diisi");
        }else if(TextUtils.isEmpty(mEditPassword.getText())){
            mEditPassword.setError("Silahkan diisi");
        }else {
            String username = mEditUsername.getText().toString();
            String password = mEditPassword.getText().toString();
            String role = "2";
            switch ((String)mSpinnerRole.getSelectedItem()){
                case "Pasien" : role = "2"; break;
                case "Dokter" : role = "3"; break;
                case "Keluarga" : role = "4"; break;
            }

            doRegister(username, password, role);
        }
    }

    void doRegister(String username, String password, String role){
        final ProgressDialog dialog = new ProgressDialog(RegisterActivity.this);
        dialog.setMessage("Loading");
        dialog.setIndeterminate(true);
        dialog.show();

        AndroidNetworking.post(getString(R.string.api_register))
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
                            Toast.makeText(RegisterActivity.this, "Anda berhasil didaftarkan",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        }else {
                            Toast.makeText(RegisterActivity.this, "Anda gagal didaftarkan",
                                    Toast.LENGTH_SHORT).show();
                        }
                        dialog.dismiss();
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.i(TAG, "onResponse: error");
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
}
