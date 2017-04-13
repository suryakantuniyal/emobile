package com.android.emobilepos.firebase;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
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
        FirebaseInstanceId instance = FirebaseInstanceId.getInstance();
        String refreshedToken = instance.getToken();
        NotificationSettings settings = new NotificationSettings();
        settings.setRegistrationToken(refreshedToken);
        FirebaseDAO.saveFirebaseSettings(settings);

        Log.d("Firebase", "Refreshed token: " + refreshedToken);
        Intent intent = new Intent(this, RegistrationIntentService.class);

        startService(intent);

    }
}
