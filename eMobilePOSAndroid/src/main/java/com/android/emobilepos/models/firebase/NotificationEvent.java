package com.android.emobilepos.models.firebase;

/**
 * Created by guarionex on 11/27/16.
 */

public class NotificationEvent {
    private String to;
    private Notification notification;

    public NotificationEvent() {
        this.setNotification(new Notification());
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public Notification getNotification() {
        return notification;
    }

    public void setNotification(Notification notification) {
        this.notification = notification;
    }

    public enum NotificationEventAction {
        SYNC_HOLDS(3), SYNC_MESAS_CONFIG(6), NONE(99);

        private int code;

        public static NotificationEventAction getNotificationEventByCode(int code) {
            switch (code) {
                case 3:
                    return SYNC_HOLDS;
                case 6:
                    return SYNC_MESAS_CONFIG;
                default:
                    return NONE;
            }
        }

        NotificationEventAction(int code) {
            this.code = code;
        }
    }

    public class Notification {
        private String merchantAccount;
        private NotificationEventAction notificationEventAction;
        private String employeeId;
        private String deviceId;

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

}
