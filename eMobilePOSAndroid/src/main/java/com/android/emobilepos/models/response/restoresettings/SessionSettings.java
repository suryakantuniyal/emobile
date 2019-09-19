package com.android.emobilepos.models.response.restoresettings;

import com.google.gson.annotations.SerializedName;

public class SessionSettings {
    @SerializedName("ExpireUserLoginSession")
    private boolean ExpireUserLoginSession= false;
    @SerializedName("UserSessionExpirationTime")
    private String UserSessionExpirationTime= "";

    public boolean isExpireUserLoginSession() {
        return ExpireUserLoginSession;
    }

    public void setExpireUserLoginSession(boolean expireUserLoginSession) {
        ExpireUserLoginSession = expireUserLoginSession;
    }

    public String getUserSessionExpirationTime() {
        return UserSessionExpirationTime;
    }

    public void setUserSessionExpirationTime(String userSessionExpirationTime) {
        UserSessionExpirationTime = userSessionExpirationTime;
    }
}
