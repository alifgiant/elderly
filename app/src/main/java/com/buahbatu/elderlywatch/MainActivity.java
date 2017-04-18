package com.buahbatu.elderlywatch;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.buahbatu.elderlywatch.adapter.ElderlyAdapter;
import com.buahbatu.elderlywatch.adapter.ProfileRecyclerAdapter;
import com.buahbatu.elderlywatch.loader.PatientLoader;
import com.buahbatu.elderlywatch.model.User;
import com.robinhood.spark.SparkView;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.buahbatu.elderlywatch.adapter.ProfileRecyclerAdapter.SELF;

public class MainActivity extends AppCompatActivity{
    private static final String TAG = MainActivity.class.getSimpleName();

    public static final String KEY_FULL_NAME = "full_name";
    public static final String KEY_ADDRESS = "address";
    public static final String KEY_PHONE_NUMBER = "phone_number";
    public static final String KEY_BIRTH_DATE = "birth_date";
    public static final int CODE_UPDATE_REQUEST = 100;
    public static final int CODE_UPDATE_REQUEST_SUCCESS = 200;

//    private SocketController controller;

    @BindView(R.id.profile_recycler) RecyclerView mRecyclerView;

    ProfileRecyclerAdapter recyclerAdapter;

    @Override
    protected void onResume() {
        super.onResume();
        recyclerAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onPause() {
        super.onPause();
        recyclerAdapter.disconnectAllConnector();
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
        if (!AppSetting.isUserLoggedIn(MainActivity.this)) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }else {
            setContentView(R.layout.activity_main);
            ButterKnife.bind(this);
            AndroidNetworking.initialize(getApplicationContext());

            // init recycler
            recyclerAdapter = new ProfileRecyclerAdapter(MainActivity.this, itemClickedListener);
            mRecyclerView.setAdapter(recyclerAdapter);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));

            // check whether doctor or not
            new PatientLoader(MainActivity.this).getPatientData(new PatientLoader.OnPatientDataReadyListener() {
                @Override
                public void onPatientDataArrived(List<String> userNames) {

                    // add current user profile
                    userNames.add(0, SELF);

                    // add patient data
                    for (String s : userNames){
                        recyclerAdapter.addUser(s);
                    }

//                    if (userNames.size() == 1)
//                        recyclerAdapter.patientViewEnabled();
                }

                @Override
                public void onPatientDataNone() {
                    // add current user profile
                    recyclerAdapter.addUser(SELF);
                }

                @Override
                public void onLoadFinished() {
                    // decide is patient or not
                }
            });
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

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == CODE_UPDATE_REQUEST && resultCode == CODE_UPDATE_REQUEST_SUCCESS){
//            // reload data
//            ProfileLoader profileLoader = new ProfileLoader(MainActivity.this, mFullNameText,
//                    mAddressText, mBirthDateText, mPhoneNumberText);
//            profileLoader.loadProfileData();
//        }
//    }


    ProfileRecyclerAdapter.OnItemListener itemClickedListener = new ProfileRecyclerAdapter.OnItemListener() {
        @Override
        public void itemClicked(int position, User user) {
            // set profile is only available for user[0]
            if (position == 0) {
                Intent moveIntent = new Intent(MainActivity.this, ProfileActivity.class);

                moveIntent.putExtra(KEY_FULL_NAME, user.FullName);
                moveIntent.putExtra(KEY_ADDRESS, user.Address);
                moveIntent.putExtra(KEY_PHONE_NUMBER, user.PhoneNumber);
                moveIntent.putExtra(KEY_BIRTH_DATE, user.BirthDate);
                startActivityForResult(moveIntent, CODE_UPDATE_REQUEST);
            }
        }
    };
}
