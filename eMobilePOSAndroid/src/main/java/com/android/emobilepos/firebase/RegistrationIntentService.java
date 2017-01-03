package com.android.emobilepos.firebase;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.android.dao.AssignEmployeeDAO;
import com.android.dao.FirebaseDAO;
import com.android.emobilepos.models.firebase.HubRegistrationPNS;
import com.android.emobilepos.models.realms.AssignEmployee;
import com.google.firebase.iid.FirebaseInstanceId;
import com.microsoft.windowsazure.messaging.NotificationHub;

/**
 * Created by guarionex on 11/25/16.
 */

public class RegistrationIntentService extends IntentService {

    private static final String TAG = "RegIntentService";

    private NotificationHub hub;

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String resultString;
        String regID;

        try {
            String FCM_token = FirebaseInstanceId.getInstance().getToken();
            Log.d(TAG, "FCM Registration Token: " + FCM_token);

            // Storing the registration id that indicates whether the generated token has been
            // sent to your server. If it is not stored, send the token to your server,
            // otherwise your server should have already received the token.
            if (((regID = sharedPreferences.getString("registrationID", null)) == null)) {

                NotificationHub hub = new NotificationHub(new NotificationSettings().getHubName(),
                        new NotificationSettings().getHubListenConnectionString(), this);
                Log.d(TAG, "Attempting a new registration with NH using FCM token : " + FCM_token);
//                regID = hub.register(FCM_token, "holds_sync").getRegistrationId();
                regID = hub.register(FCM_token).getRegistrationId();
                // If you want to use tags...
                // Refer to : https://azure.microsoft.com/en-us/documentation/articles/notification-hubs-routing-tag-expressions/
                // regID = hub.register(token, "tag1,tag2").getRegistrationId();
                resultString = "New NH Registration Successfully - RegId : " + regID;
                Log.d(TAG, resultString);
                FirebaseDAO.saveToken(regID, FCM_token);
//                sharedPreferences.edit().putString("registrationID", regID).apply();
//                sharedPreferences.edit().putString("FCMtoken", FCM_token).apply();
            }

            // Check if the token may have been compromised and needs refreshing.
            else if (!sharedPreferences.getString("FCMtoken", "").equalsIgnoreCase(FCM_token)) {

                NotificationHub hub = new NotificationHub(new NotificationSettings().getHubName(),
                        new NotificationSettings().getHubListenConnectionString(), this);
                Log.d(TAG, "NH Registration refreshing with token : " + FCM_token);
                regID = hub.register(FCM_token).getRegistrationId();

                // If you want to use tags...
                // Refer to : https://azure.microsoft.com/en-us/documentation/articles/notification-hubs-routing-tag-expressions/
                // regID = hub.register(token, "tag1,tag2").getRegistrationId();

                resultString = "New NH Registration Successfully - RegId : " + regID;
                Log.d(TAG, resultString);
                FirebaseDAO.saveToken(regID, FCM_token);
//                sharedPreferences.edit().putString("registrationID", regID).apply();
//                sharedPreferences.edit().putString("FCMtoken", FCM_token).apply();
            } else {
                resultString = "Previously Registered Successfully - RegId : " + regID;
            }
        } catch (Exception e) {
            Log.e(TAG, resultString = "Failed to complete registration", e);
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
        }
    }

    private class RegisterPNSTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... params) {
            AssignEmployee assignEmployee = AssignEmployeeDAO.getAssignEmployee();
            NotificationSettings settings = FirebaseDAO.getNotificationSettings();
            HubRegistrationPNS request = new HubRegistrationPNS();
            request.setEmployeeId(assignEmployee.getEmpId());
            request.setDeviceID(settings.getRegistrationToken());
//            request.setEmployeeId(assignEmployee.getA);
            return null;
        }
    }
}
