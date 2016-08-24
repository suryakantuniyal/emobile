package com.android.emobilepos.models.storedAndForward;

import com.android.emobilepos.models.Payment;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;

/**
 * Created by guarionex on 8/23/16.
 */
public class StoreAndForward extends RealmObject {
    private long id;
    private Date creationDate;
    @Ignore
    private StoreAndForwatdStatus storeAndForwatdStatus;
    @Ignore
    private
    PaymentType paymentType;
    private int paymentTypeValue;
    private int status;
    private String paymentXml;
    private boolean retry;
    private Payment payment;

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public boolean isRetry() {
        return retry;
    }

    public void setRetry(boolean retry) {
        this.retry = retry;
    }

    public PaymentType getPaymentType() {
        paymentType = PaymentType.getInstance(paymentTypeValue);
        return paymentType;
    }

    public void setPaymentType(PaymentType paymentType) {
        paymentTypeValue = paymentType.getCode();
        this.paymentType = paymentType;
    }

    public enum StoreAndForwatdStatus {
        PENDING(0), COMPLETED(1), PARTIAL(2);
        int code;

        StoreAndForwatdStatus(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static StoreAndForwatdStatus getInstance(int code) {
            switch (code) {
                case 0:
                    return PENDING;
                case 1:
                    return COMPLETED;
                case 2:
                    return PARTIAL;
                default:
                    return PENDING;
            }
        }
    }

    public enum PaymentType {
        BOLORO(0), CREDIT_CARD(1);
        int code;

        PaymentType(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static PaymentType getInstance(int code) {
            switch (code) {
                case 0:
                    return BOLORO;
                case 1:
                    return CREDIT_CARD;
                default:
                    return CREDIT_CARD;
            }
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public StoreAndForwatdStatus getStoreAndForwatdStatus() {
        storeAndForwatdStatus = StoreAndForwatdStatus.getInstance(status);
        return storeAndForwatdStatus;
    }

    public void setStoreAndForwatdStatus(StoreAndForwatdStatus storeAndForwatdStatus) {
        status = storeAndForwatdStatus.getCode();
        this.storeAndForwatdStatus = storeAndForwatdStatus;
    }

    public String getPaymentXml() {
        return paymentXml;
    }

    public void setPaymentXml(String paymentXml) {
        this.paymentXml = paymentXml;
    }

}
