package com.buahbatu.elderlywatch;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.ANRequest;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.OkHttpResponseAndStringRequestListener;

import okhttp3.Response;

public class AppSetting {
    public static void setCookie(Context context, String cookieResponse){
        String[] splitCookieInfo = cookieResponse.split(";");
        String token = splitCookieInfo[0];
        SharedPreferences preferences =
                context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("cookie", token);
        editor.apply();
    }

    public static String getCookie(Context context){
        SharedPreferences preferences =
                context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE);
        return preferences.getString("cookie", "");
    }

    public static boolean isUserLoggedIn(Context context){
        return !TextUtils.isEmpty(getCookie(context));
    }

    public static void saveUsername(Context context, String username){
        SharedPreferences preferences =
                context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("username", username);
        editor.apply();
    }

    public static String getUsername(Context context){
        SharedPreferences preferences =
                context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE);
        return preferences.getString("username", "");
    }
}
