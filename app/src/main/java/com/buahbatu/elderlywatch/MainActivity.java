package com.buahbatu.elderlywatch;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.OkHttpResponseAndJSONObjectRequestListener;
import com.androidnetworking.interfaces.StringRequestListener;
import com.buahbatu.elderlywatch.adapter.ElderlyAdapter;
import com.robinhood.spark.SparkView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements SocketController.OnMessageArriveListener{
    private static final String TAG = MainActivity.class.getSimpleName();

    public static final String KEY_FULL_NAME = "full_name";
    public static final String KEY_ADDRESS = "address";
    public static final String KEY_PHONE_NUMBER = "phone_number";
    public static final String KEY_BIRTH_DATE = "birth_date";
    public static final int CODE_UPDATE_REQUEST = 100;
    public static final int CODE_UPDATE_REQUEST_SUCCESS = 200;

    private SocketController controller;

    @BindView(R.id.text_status) TextView mStatusText;
    @BindView(R.id.text_full_name) TextView mFullNameText;
    @BindView(R.id.text_address) TextView mAddressText;
    @BindView(R.id.text_phone_number) TextView mPhoneNumberText;
    @BindView(R.id.text_birth_date) TextView mBirthDateText;

    @BindView(R.id.spark_graph) SparkView mDataView;
    private ElderlyAdapter elderlyAdapter;

    private boolean ringtoneIsIdle = true;
    void soundOnDrop(){
        if (ringtoneIsIdle){
//            ringtoneIsIdle = false;
//            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
//            r.play();
//
//            MediaPlayer mp = MediaPlayer.create(this, notification);
//            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                @Override
//                public void onCompletion(MediaPlayer mp) {
//                    ringtoneIsIdle = true;
//                }
//            });
//            mp.start();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // create socket
        if (controller == null) {
            controller = new SocketController(MainActivity.this,
                    AppSetting.getUsername(MainActivity.this), this);
        }
        controller.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        controller.disconnect();
        controller = null;
    }

    @OnClick(R.id.profile_card) void onProfileClick(){
        Intent moveIntent = new Intent(MainActivity.this, ProfileActivity.class);
        moveIntent.putExtra(KEY_FULL_NAME, mFullNameText.getText().toString());
        moveIntent.putExtra(KEY_ADDRESS, mAddressText.getText().toString());
        moveIntent.putExtra(KEY_PHONE_NUMBER, mPhoneNumberText.getText().toString());
        moveIntent.putExtra(KEY_BIRTH_DATE, mBirthDateText.getText().toString());
        startActivityForResult(moveIntent, CODE_UPDATE_REQUEST);
    }

    void loadProfileData(){
        final ProgressDialog dialog = new ProgressDialog(MainActivity.this);
        dialog.setMessage("Loading user data");
        dialog.setIndeterminate(true);
        dialog.show();
        String url = String.format(Locale.US, getString(R.string.api_get_profile), AppSetting.getUrl(MainActivity.this));
        AndroidNetworking.post(url)
                .setPriority(Priority.MEDIUM)
                .addHeaders("Cookie", AppSetting.getCookie(MainActivity.this))
                .build()
                .getAsOkHttpResponseAndJSONObject(new OkHttpResponseAndJSONObjectRequestListener() {
                    @Override
                    public void onResponse(Response okHttpResponse, JSONObject response) {
                        String cookie = okHttpResponse.header("Set-Cookie", "");
                        AppSetting.setCookie(MainActivity.this, cookie);

                        String fullName = "";
                        String address = "";
                        String birthDate = "";
                        String phoneNum = "";

                        try {
                            fullName = response.getString("full_name");
                            address = response.getString("address");
                            birthDate = response.getString("birth_date");
                            phoneNum = response.getString("phone_number");
                        }catch (JSONException e){
                            Log.e(TAG, "onResponse: JSON field missing");
                        }

                        mFullNameText.setText(fullName);
                        mAddressText.setText(address);
                        mBirthDateText.setText(birthDate);
                        mPhoneNumberText.setText(phoneNum);

                        dialog.dismiss();
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.i(TAG, "onResponse: error");
                        dialog.dismiss();
                    }
                });
    }

    void onLogOutClick(){
        final ProgressDialog dialog = new ProgressDialog(MainActivity.this);
        dialog.setMessage("Loading");
        dialog.setIndeterminate(true);
        dialog.show();
        String url = String.format(Locale.US, getString(R.string.api_logout), AppSetting.getUrl(MainActivity.this));
        AndroidNetworking.post(url)
                .setPriority(Priority.MEDIUM)
                .addHeaders("Cookie", AppSetting.getCookie(MainActivity.this))
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        Log.i(TAG, "onResponse: " + response);
                        AppSetting.setCookie(MainActivity.this, "");
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        finish();
                        dialog.dismiss();
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.i(TAG, "onResponse: error");
                        dialog.dismiss();
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!AppSetting.isUserLoggedIn(MainActivity.this)) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }else {
            ButterKnife.bind(this);
            AndroidNetworking.initialize(getApplicationContext());

            // load profile data
            loadProfileData();
            elderlyAdapter = new ElderlyAdapter();
            mDataView.setAdapter(elderlyAdapter);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.button_logout:
                onLogOutClick();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CODE_UPDATE_REQUEST && resultCode == CODE_UPDATE_REQUEST_SUCCESS)
            loadProfileData(); // reload data
    }

    @Override
    public void onMessageArrive(double y, String status) {
        Log.i(TAG, "onMessageArrive: " + y + " " + status);
        elderlyAdapter.addDataToChart((float)y);
        elderlyAdapter.notifyDataSetChanged();
        mStatusText.setText(status.toUpperCase());
        switch (status){
            case "idle": mStatusText.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.colorIdle));
                break;
            case "sit": mStatusText.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.colorSit));
                break;
            case "walk": mStatusText.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.colorWalk));
                break;
            case "drop": mStatusText.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.colorDrop));
                soundOnDrop();
                break;
        }
    }
}
