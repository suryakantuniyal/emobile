package com.android.emobilepos.firebase;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by guarionex on 11/25/16.
 */

public class NotificationSettings extends RealmObject {
    @PrimaryKey
    private static String SenderId = "555089729868";
    private static String HubName = "eMobilePOSNotification";
    private static String HubListenConnectionString = "Endpoint=sb://emobileposnotification.servicebus.windows.net/;SharedAccessKeyName=DefaultListenSharedAccessSignature;SharedAccessKey=UvYALhl8TJM71bt9OjK/WMmvwtexf1nsRztV6mMsW1c=";
    private String registrationToken;

    public static String getSenderId() {
        return SenderId;
    }

    public static void setSenderId(String senderId) {
        SenderId = senderId;
    }

    public static String getHubName() {
        return HubName;
    }

    public static void setHubName(String hubName) {
        HubName = hubName;
    }

    public static String getHubListenConnectionString() {
        return HubListenConnectionString;
    }

    public static void setHubListenConnectionString(String hubListenConnectionString) {
        HubListenConnectionString = hubListenConnectionString;
    }

    public String getRegistrationToken() {
        return registrationToken;
    }

    public void setRegistrationToken(String registrationToken) {
        this.registrationToken = registrationToken;
    }
}
