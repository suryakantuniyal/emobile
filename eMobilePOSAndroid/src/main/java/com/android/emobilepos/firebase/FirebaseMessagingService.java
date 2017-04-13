package com.android.emobilepos.firebase;

import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    public FirebaseMessagingService() {
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO(developer): Handle FCM messages here.
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
        Log.d("FMS", "From: " + remoteMessage.getFrom());
        Log.d("FMS", "Notification Message Body: " + remoteMessage.getNotification().getBody());

    }

    @Override
    public void onMessageSent(String s) {
        super.onMessageSent(s);
    }
}
