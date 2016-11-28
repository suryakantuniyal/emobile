package com.android.emobilepos.models.firebase;

/**
 * Created by guarionex on 11/27/16.
 */

public class NotificationEvent {

    private String merchantAccount;
    private NotificationEventAction notificationEventAction;
    private String employeeId;
    private String deviceId;

    public enum NotificationEventAction {
        SYNC_HOLDS(0), SYNC_PRODUCTS(1);

        private int code;

        NotificationEventAction(int code) {

            this.code = code;
        }
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }


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

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }


}
