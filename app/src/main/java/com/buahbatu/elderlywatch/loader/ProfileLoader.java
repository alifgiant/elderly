package com.buahbatu.elderlywatch.loader;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.OkHttpResponseAndJSONObjectRequestListener;
import com.buahbatu.elderlywatch.AppSetting;
import com.buahbatu.elderlywatch.LoginActivity;
import com.buahbatu.elderlywatch.R;
import com.buahbatu.elderlywatch.model.User;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import okhttp3.Response;

public class ProfileLoader {
    private static final String TAG = ProfileLoader.class.getSimpleName();
    private Context context;
    private OnProfileReadyListener listener;

    private boolean isWorking = false;
    private List<String> usernameList;

    public ProfileLoader(Context context, OnProfileReadyListener listener) {
        this.context = context;
        this.listener = listener;
        this.usernameList = new ArrayList<>();
    }

    public void addLoadQueue(String username){
        usernameList.add(username);
        if (!isWorking) {
            isWorking = true;
            loadProfileData();
        }
    }

    private void loadProfileData(){
        final String username = usernameList.remove(0);

        final ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage("Loading user data");
        dialog.setIndeterminate(true);
        dialog.show();

        Log.i(TAG, "loadProfileData: " + username);
        Log.i(TAG, "ProfileResponse before: " + AppSetting.getCookie(context));

        String ipAddress = AppSetting.getUrl(context)
                + AppSetting.getPort(context) + AppSetting.getSubPath(context);
        String url = String.format(Locale.US, context.getString(R.string.api_get_profile), ipAddress);

        AndroidNetworking.post(url)
                .setPriority(Priority.MEDIUM)
                .addHeaders("Cookie", AppSetting.getCookie(context))
                .addBodyParameter("username", username)
                .build()
                .getAsOkHttpResponseAndJSONObject(new OkHttpResponseAndJSONObjectRequestListener() {
                    @Override
                    public void onResponse(Response okHttpResponse, JSONObject response) {
                        String cookie = okHttpResponse.header("Set-Cookie", AppSetting.getCookie(context));
                        // handle error
//                        cookie = cookie.equals("") ? AppSetting.getCookie(context) : cookie;
                        AppSetting.setCookie(context, cookie);

                        Log.i(TAG, "ProfileResponse after: " + cookie);
                        Log.i(TAG, "ProfileResponse: " + response);

                        String fullName;
                        String address;
                        String birthDate;
                        String phoneNum;
                        String role = "4"; // default is patient

                        try {
                            fullName = response.getString("full_name").toUpperCase();
                            address = response.getString("address").toUpperCase();
                            birthDate = response.getString("birth_date");
                            String[] splits = birthDate.split("-");
//                            Log.i(TAG, "onResponse: " + splits.length);
                            birthDate = splits[2] + "/" + splits[1] + "/" + splits[0];
                            phoneNum = response.getString("phone_number");
                            if (response.has("role")){
                                role = response.getString("role");
                            }
                            listener.onProfileDataReady(new User(username, fullName, address, birthDate, phoneNum, role));
                        }catch (JSONException e){
                            Log.e(TAG, "onResponse: JSON field missing");
                        }
                        dialog.dismiss();

                        if (usernameList.size() > 0){
                            loadProfileData();
                        }else {
                            isWorking = false;
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.i(TAG, "onResponse: error");
                        dialog.dismiss();
                    }
                });
    }

    public interface OnProfileReadyListener{
        void onProfileDataReady(User user);
    }
}
