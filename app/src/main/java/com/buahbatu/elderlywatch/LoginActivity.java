package com.buahbatu.elderlywatch;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.OkHttpResponseAndStringRequestListener;
import com.androidnetworking.interfaces.StringRequestListener;

import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = LoginActivity.class.getSimpleName();

    @BindView(R.id.text_username) EditText mEditUsername;
    @BindView(R.id.text_password) EditText mEditPassword;

    @OnClick(R.id.button_login) void onLoginClick(){
        mEditUsername.setError(null);
        mEditPassword.setError(null);
        if (TextUtils.isEmpty(mEditUsername.getText())){
            mEditUsername.setError("Silahkan diisi");
        }else if(TextUtils.isEmpty(mEditPassword.getText())){
            mEditPassword.setError("Silahkan diisi");
        }else {
            String username = mEditUsername.getText().toString();
            String password = mEditPassword.getText().toString();

            doLogin(username, password);
        }
    }
    @OnClick(R.id.button_register) void onRegisterClick(){
        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
    }

    void doLogin(final String username, String password){
        final ProgressDialog dialog = new ProgressDialog(LoginActivity.this);
        dialog.setMessage("Loading");
        dialog.setIndeterminate(true);
        dialog.show();

        String url = String.format(Locale.US, getString(R.string.api_login), AppSetting.getUrl(LoginActivity.this));
        AndroidNetworking.post(url)
                .addBodyParameter("username", username)
                .addBodyParameter("password", password)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsOkHttpResponseAndString(new OkHttpResponseAndStringRequestListener() {
                    @Override
                    public void onResponse(Response okHttpResponse, String response) {
                        Log.i(TAG, "onResponse: " + response);
                        if (TextUtils.equals(response, "1")){
                            String cookie = okHttpResponse.header("Set-Cookie", "");
                            AppSetting.setCookie(LoginActivity.this, cookie);
                            AppSetting.saveUsername(LoginActivity.this, username);
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        }else {
                            Toast.makeText(LoginActivity.this, "Username atau Password salah",
                                    Toast.LENGTH_SHORT).show();
                        }
                        dialog.dismiss();
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.i(TAG, "onResponse: error");
                        Toast.makeText(LoginActivity.this, "Login gagal cek koneksi interne anda",
                                Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.login_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.button_setting:
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                LinearLayout layout = new LinearLayout(LoginActivity.this);
                final EditText urlText = new EditText(LoginActivity.this);
                layout.addView(urlText);
                builder.setView(layout).setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (!TextUtils.isEmpty(urlText.getText())){
                            AppSetting.saveUrl(LoginActivity.this, urlText.getText().toString());
                            Toast.makeText(LoginActivity.this, "Url disimpan", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
