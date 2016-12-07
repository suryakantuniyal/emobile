package com.android.emobilepos.firebase;

import android.content.Intent;
import android.util.Log;

import com.android.dao.FirebaseDAO;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

/**
 * Created by guarionex on 11/23/16.
 */

public class FirebaseInstanceIdSrv extends com.google.firebase.iid.FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d("Firebase", "Refreshed token: " + refreshedToken);
        FirebaseMessaging.getInstance().subscribeToTopic("allDevices");
        Intent intent = new Intent(this, RegistrationIntentService.class);
        NotificationSettings settings = new NotificationSettings();
        settings.setRegistrationToken(refreshedToken);
        FirebaseDAO.saveFirebaseSettings(settings);
        startService(intent);
        // TODO: Implement this method to send any registration to your app's servers.
//        sendRegistrationToServer(refreshedToken);  
    }
}
