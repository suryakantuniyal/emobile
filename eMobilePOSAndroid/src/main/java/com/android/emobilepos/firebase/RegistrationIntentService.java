package com.android.emobilepos.firebase;

import android.app.IntentService;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.android.dao.AssignEmployeeDAO;
import com.android.dao.FirebaseDAO;
import com.android.emobilepos.BuildConfig;
import com.android.emobilepos.R;
import com.android.emobilepos.models.firebase.HubRegistrationPNS;
import com.android.emobilepos.models.realms.AssignEmployee;
import com.android.support.MyPreferences;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.microsoft.windowsazure.messaging.NotificationHub;

import oauthclient.HttpClient;
import oauthclient.OAuthClient;
import oauthclient.OAuthManager;
import util.json.JsonUtils;

/**
 * Created by guarionex on 11/25/16.
 */

public class RegistrationIntentService extends IntentService {
    private static final String TAG = "RegIntentService";

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String resultString;
        String regID = null;
        NotificationSettings notificationSettings = FirebaseDAO.getNotificationSettings();
        try {
            String FCM_token = FirebaseInstanceId.getInstance().getToken();
            Log.d(TAG, "FCM Registration Token: " + FCM_token);

            // Storing the registration id that indicates whether the generated token has been
            // sent to your server. If it is not stored, send the token to your server,
            // otherwise your server should have already received the token.
            if (notificationSettings == null || TextUtils.isEmpty(notificationSettings.getHubRegistrationId())) {
                NotificationHub hub = new NotificationHub(new NotificationSettings().getHubName(),
                        new NotificationSettings().getHubListenConnectionString(), this);
                Log.d(TAG, "Attempting a new registration with NH using FCM token : " + FCM_token);
//                regID = hub.register(FCM_token, "holds_sync").getRegistrationId();
//                regID = hub.register(FCM_token).getRegistrationId();
                // If you want to use tags...
                // Refer to : https://azure.microsoft.com/en-us/documentation/articles/notification-hubs-routing-tag-expressions/
                // regID = hub.register(token, "tag1,tag2").getRegistrationId();
                resultString = "New NH Registration Successfully - RegId : " + regID;
                Log.d(TAG, resultString);
                notificationSettings = new NotificationSettings();
                notificationSettings.setRegistrationToken(FCM_token);
                notificationSettings.setHubRegistrationId(regID);
                FirebaseDAO.saveFirebaseSettings(notificationSettings);
//                sharedPreferences.edit().putString("registrationID", regID).apply();
//                sharedPreferences.edit().putString("FCMtoken", FCM_token).apply();
                registerPNS();
            }
            // Check if the token may have been compromised and needs refreshing.
            else if (!notificationSettings.getRegistrationToken().equalsIgnoreCase(FCM_token)
                    || notificationSettings.getRegistrationStatusEnum() == NotificationSettings.HUBRegistrationStatus.UNKNOWN) {
                NotificationHub hub = new NotificationHub(new NotificationSettings().getHubName(),
                        new NotificationSettings().getHubListenConnectionString(), this);
                Log.d(TAG, "NH Registration refreshing with token : " + FCM_token);
//                regID = hub.register(FCM_token).getRegistrationId();

                // If you want to use tags...
                // Refer to : https://azure.microsoft.com/en-us/documentation/articles/notification-hubs-routing-tag-expressions/
                // regID = hub.register(token, "tag1,tag2").getRegistrationId();

                resultString = "New NH Registration Successfully - RegId : " + regID;
                Log.d(TAG, resultString);
                FirebaseDAO.saveToken(regID, FCM_token);
                registerPNS();
//                sharedPreferences.edit().putString("registrationID", regID).apply();
//                sharedPreferences.edit().putString("FCMtoken", FCM_token).apply();
            }
        } catch (Exception e) {
            Log.e(TAG, resultString = "Failed to complete registration", e);
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
        }
    }

    private void registerPNS() {
        MyPreferences preferences = new MyPreferences(RegistrationIntentService.this);
        if (!TextUtils.isEmpty(preferences.getAcctNumber()) && !TextUtils.isEmpty(preferences.getAcctPassword())) {
            OAuthManager.getInstance(RegistrationIntentService.this, preferences.getAcctNumber(), preferences.getAcctPassword());
            HttpClient httpClient = new HttpClient();
            String url = getString(R.string.sync_register_pns);
            OAuthClient authClient = OAuthManager.getOAuthClient(RegistrationIntentService.this);
            AssignEmployee assignEmployee = AssignEmployeeDAO.getAssignEmployee(false);
            NotificationSettings settings = FirebaseDAO.getNotificationSettings();
            HubRegistrationPNS request = new HubRegistrationPNS();
            request.setEmployeeId(assignEmployee.getEmpId());
            request.setDeviceID(settings.getRegistrationToken());
            request.setActivationKey(preferences.getActivKey());
            request.setOs(BuildConfig.OS);
            request.setBundleVersion(preferences.getBundleVersion());
            request.setPns(settings.getRegistrationToken());
            Gson gson = JsonUtils.getInstance();
            try {
                String response = httpClient.post(url, gson.toJson(request), authClient);
                FirebaseDAO.saveHUBRegistrationStatus(NotificationSettings.HUBRegistrationStatus.SUCCEED);
            } catch (Exception e) {
                e.printStackTrace();
                FirebaseDAO.saveHUBRegistrationStatus(NotificationSettings.HUBRegistrationStatus.UNKNOWN);
            }
        }
    }
}