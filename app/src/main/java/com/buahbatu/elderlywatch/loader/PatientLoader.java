package com.buahbatu.elderlywatch.loader;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.OkHttpResponseAndJSONObjectRequestListener;
import com.buahbatu.elderlywatch.AppSetting;
import com.buahbatu.elderlywatch.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.Response;

/**
 * Created by maakbar on 4/12/17.
 */

public class PatientLoader {
    private static final String TAG = PatientLoader.class.getSimpleName();

    private Context context;

    public PatientLoader(Context context) {
        this.context = context;
    }

    public void getPatientData(final OnPatientDataReadyListener dataReadyListener){
        final ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage("Checking whether doctor or not");
        dialog.setIndeterminate(true);
        dialog.show();

        String ipAddress = AppSetting.getUrl(context) + AppSetting.getPort(context);
        String url = String.format(Locale.US, context.getString(R.string.api_get_patients), ipAddress);

        AndroidNetworking.post(url)
                .setPriority(Priority.MEDIUM)
                .addHeaders("Cookie", AppSetting.getCookie(context))
                .build()
                .getAsOkHttpResponseAndJSONObject(new OkHttpResponseAndJSONObjectRequestListener() {
                    @Override
                    public void onResponse(Response okHttpResponse, JSONObject response) {
                        String cookie = okHttpResponse.header("Set-Cookie", AppSetting.getCookie(context));
                        AppSetting.setCookie(context, cookie);

                        List<String> patients = new ArrayList<>();
                        boolean isSuccess = false;
                        try {
                            JSONArray patientsJSON = response.getJSONArray("patients");
                            for (int i = 0; i < patientsJSON.length(); i++) {
                                patients.add(patientsJSON.getString(i));
                            }
                            dataReadyListener.onPatientDataArrived(patients);
                            isSuccess = true;
                        }catch (JSONException e){
                            Log.e(TAG, "onResponse: JSON field missing");
                        }
                        dialog.dismiss();
                        if (!isSuccess)
                            dataReadyListener.onPatientDataNone();
                        dataReadyListener.onLoadFinished();
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.i(TAG, "onResponse: error");
                        dialog.dismiss();
                        dataReadyListener.onPatientDataNone();
                        dataReadyListener.onLoadFinished();
                    }
                });
    }

    public interface OnPatientDataReadyListener{
        void onPatientDataArrived(List<String> userNames);
        void onPatientDataNone();
        void onLoadFinished();
    }
}
