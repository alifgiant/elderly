package com.buahbatu.elderlywatch;

import android.content.Context;
import android.util.Log;
import org.java_websocket.WebSocket;
import org.json.JSONException;
import org.json.JSONObject;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import ua.naiksoftware.stomp.LifecycleEvent;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.client.StompClient;
import ua.naiksoftware.stomp.client.StompMessage;

public class SocketController {
    private static final String TAG = SocketController.class.getSimpleName();

    private StompClient mStompClient;

    public SocketController(Context context, String username, final OnMessageArriveListener messageArriveListener) {
        this.mStompClient = Stomp.over(WebSocket.class, context.getString(R.string.api_web_socket));

        mStompClient.lifecycle()
                .subscribe(new Action1<LifecycleEvent>() {
                    @Override
                    public void call(LifecycleEvent lifecycleEvent) {
                        switch (lifecycleEvent.getType()) {
                            case OPENED:
                                Log.i(TAG, "call: OPENED");
                                break;
                            case ERROR:
                                Log.e(TAG, "Stomp connection error", lifecycleEvent.getException());
                                break;
                            case CLOSED:
                                Log.i(TAG, "call: CLOSED");
                        }
                    }
                });

        // Receive greetings
        mStompClient.topic("/app/data/" + username)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<StompMessage>() {
                    @Override
                    public void call(StompMessage stompMessage) {
                        String payload = stompMessage.getPayload();
//                        Log.i(TAG, payload);
                        try {
                            JSONObject data = new JSONObject(payload);
                            double y = data.getDouble("x");
                            String status = data.getString("status");
                            messageArriveListener.onMessageArrive(y, status);
                        }catch (JSONException e){
                            Log.e(TAG, "data error: not json format");
                        }
                    }
                });
    }

    public void connect(){
        mStompClient.connect();
    }

    public void disconnect(){
        mStompClient.disconnect();
    }

    interface OnMessageArriveListener{
        void onMessageArrive(double y, String status);
    }
}
