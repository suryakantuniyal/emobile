package com.android.emobilepos.models.firebase;

/**
 * Created by guarionex on 11/27/16.
 */

public class NotificationEvent {

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public enum NotificationEventAction {
        SYNC_HOLDS(0), SYNC_PRODUCTS(1);

        private int code;

        NotificationEventAction(int code) {

            this.code = code;
        }
    }

    private String merchantAccount;
    private NotificationEventAction notificationEventAction;
    private String targetEmployeeId;
    private String deviceId;

    public String getMerchantAccount() {
        return merchantAccount;
    }

    public void setMerchantAccount(String merchantAccount) {
        this.merchantAccount = merchantAccount;
    }

    public NotificationEventAction getNotificationEventAction() {
        return notificationEventAction;
    }

    public void setNotificationEventAction(NotificationEventAction notificationEventAction) {
        this.notificationEventAction = notificationEventAction;
    }

    public String getTargetEmployeeId() {
        return targetEmployeeId;
    }

    public void setTargetEmployeeId(String targetEmployeeId) {
        this.targetEmployeeId = targetEmployeeId;
    }


}
