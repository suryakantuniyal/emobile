package com.innobins.innotrack.FCM;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import com.innobins.innotrack.utils.URLContstant;

/**
 * Created by silence12 on 19/6/17.
 */

public class  SendRegistrationTokentoServer extends IntentService {

    SharedPreferences mSharedPreferences;

    public SendRegistrationTokentoServer() {
        super("SendRegistrationTokentoServer");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mSharedPreferences = getSharedPreferences(URLContstant.PREFERENCE_NAME, MODE_PRIVATE);
        String registrationToken = mSharedPreferences.getString(URLContstant.FCM_TOKEN, "MOOVO");
        Log.d("length-token", String.valueOf(registrationToken));
        sendRegistrationToServer(registrationToken);
    }

    public void sendRegistrationToServer(String mToken) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("userName", mSharedPreferences.getString(URLContstant.KEY_USERNAME, ""));
            jsonObject.put("phoneNumber", mSharedPreferences.getString(URLContstant.KEY_USER_PHONE, ""));
            jsonObject.put("fcmToken", mToken);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String username = mSharedPreferences.getString(URLContstant.KEY_USERNAME, "");
        String phone = mSharedPreferences.getString(URLContstant.KEY_USER_PHONE, "");
        String tokenUrl = URLContstant.BASE_URL + "/api/customer/addtoken/";
//        APIServices.getInstance().PostCall(getApplicationContext(), "/api/customer/addtoken/", jsonObject, new ResponseCallback() {
//            @Override
//            public void OnResponse(JSONObject Response) {
//
//                if (Response != null) {
//                    try {
//                        Log.d("Registration FCM Token ", Response.getJSONObject("response").getString("message"));
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        });
    }
}
