package com.android.emobilepos.firebase;

import android.content.Intent;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by guarionex on 11/23/16.
 */

public class FirebaseInstanceIdSrv extends com.google.firebase.iid.FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d("Firebase", "Refreshed token: " + refreshedToken);
        Intent intent = new Intent(this, RegistrationIntentService.class);
        startService(intent);
        // TODO: Implement this method to send any registration to your app's servers.
//        sendRegistrationToServer(refreshedToken);  
    }
}
