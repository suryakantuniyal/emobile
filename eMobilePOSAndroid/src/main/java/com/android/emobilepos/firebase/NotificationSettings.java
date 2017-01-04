package com.android.emobilepos.firebase;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

/**
 * Created by guarionex on 11/25/16.
 */

public class NotificationSettings extends RealmObject {
    @PrimaryKey
    private String SenderId = "555089729868";
    private String HubName = "eMobilePOSNotification";
    private String HubListenConnectionString = "Endpoint=sb://emobileposnotification.servicebus.windows.net/;SharedAccessKeyName=DefaultListenSharedAccessSignature;SharedAccessKey=UvYALhl8TJM71bt9OjK/WMmvwtexf1nsRztV6mMsW1c=";
    private String registrationToken;
    private String topicId;
    private String hubRegistrationId;
    private String authorizationKey = "key=AAAAgT3tGUw:APA91bHti3tuO7EJvsqWiFF-YJil6fhDff67AorKTJzJ6ihWud7g-1roBfDuP21zAYTdgTdvlkEQQdp8mFPU9AT1LS_mIGg7y63SyZTaBFZZ8HnD0xea7vdg7Yr3VrGt0zK_WP6_ajGuSCJ71oI_lvQu67T8Yrs7qg";
    private String registrationStatus = HUBRegistrationStatus.UNKNOWN.name();
    @Ignore
    private HUBRegistrationStatus registrationStatusEnum = HUBRegistrationStatus.UNKNOWN;

    public String getSenderId() {
        return SenderId;
    }

    public void setSenderId(String senderId) {
        SenderId = senderId;
    }

    public String getHubName() {
        return HubName;
    }

    public void setHubName(String hubName) {
        HubName = hubName;
    }

    public String getHubListenConnectionString() {
        return HubListenConnectionString;
    }

    public void setHubListenConnectionString(String hubListenConnectionString) {
        HubListenConnectionString = hubListenConnectionString;
    }

    public String getRegistrationToken() {
        return registrationToken;
    }

    public void setRegistrationToken(String registrationToken) {
        this.registrationToken = registrationToken;
    }

    public String getTopicId() {
        return topicId;
    }

    public void setTopicId(String topicId) {
        this.topicId = topicId;
    }

    public String getAuthorizationKey() {
        return authorizationKey;
    }

    public void setAuthorizationKey(String authorizationKey) {
        this.authorizationKey = authorizationKey;
    }

    public String getHubRegistrationId() {
        return hubRegistrationId;
    }

    public void setHubRegistrationId(String hubRegistrationId) {
        this.hubRegistrationId = hubRegistrationId;
    }

    public HUBRegistrationStatus getRegistrationStatusEnum() {
        registrationStatusEnum = HUBRegistrationStatus.valueOf(registrationStatus);
        return registrationStatusEnum;
    }

    public void setRegistrationStatusEnum(HUBRegistrationStatus registrationStatusEnum) {
        registrationStatus = registrationStatusEnum.name();
        this.registrationStatusEnum = registrationStatusEnum;
    }

    public enum HUBRegistrationStatus {SUCCEED, UNKNOWN}

    public NotificationSettings getUnmanagedObject() {
        return Realm.getDefaultInstance().copyFromRealm(this);
    }
}
