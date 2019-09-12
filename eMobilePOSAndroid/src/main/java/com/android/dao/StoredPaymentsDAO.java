package com.android.dao;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import com.android.database.DBManager;
import com.android.database.PaymentsHandler;
import com.android.emobilepos.models.PaymentDetails;
import com.android.emobilepos.models.realms.AssignEmployee;
import com.android.emobilepos.models.realms.Payment;
import com.android.emobilepos.models.realms.StoreAndForward;
import com.android.support.Global;
import com.android.support.MyPreferences;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

public class StoredPaymentsDAO {

    public static StoreAndForward getStoreAndForward(String pay_uuid) {
        Realm realm = Realm.getDefaultInstance();
        StoreAndForward first;
        try {
            first = realm.where(StoreAndForward.class).equalTo("payment.pay_uuid", pay_uuid).findFirst();
            if (first != null) {
                first = realm.copyFromRealm(first);
            }
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
        return first;
    }

//    public static void updateSignaturePayment(final String pay_uuid, final String encodedImage) {
//        Realm realm = Realm.getDefaultInstance();
//        Payment payment;
//        try {
//            realm.beginTransaction();
//            payment = realm.where(Payment.class).equalTo("pay_uuid", pay_uuid).findFirst();
//            payment.setPay_signature(encodedImage);
//        } finally {
//            realm.commitTransaction();
//        }
////        return storeAndForward.getPayment().getPay_amount();
//    }


    public static int getRetryTransCount(String _job_id) {
        Realm realm = Realm.getDefaultInstance();
        try {

            int size = realm.where(StoreAndForward.class).equalTo("payment.job_id", _job_id)
                    .equalTo("payment.is_retry", "1").findAll().size();
            realm.close();
            return size;
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    public static long getCountPendingStoredPayments(String job_id) {
        Realm realm = Realm.getDefaultInstance();
        try {

            int size = realm.where(StoreAndForward.class)
                    .equalTo("payment.job_id", job_id).findAll().size();
            realm.close();
            return size;
        } finally {
            if (realm != null) {
                realm.close();
            }
        }

    }

    public static void deletePaymentFromJob(String _job_id) {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            StoreAndForward first = realm.where(StoreAndForward.class)
                    .equalTo("payment.job_id", _job_id).findFirst();
            if (first != null && first.isValid()) {
                first.deleteFromRealm();
            }
        } finally {
            realm.commitTransaction();
            if (realm != null) {
                realm.close();
            }
        }
    }


    public static PaymentDetails getPrintingForPaymentDetails(String payID, int type) {
        StringBuilder sb = new StringBuilder();
        Realm realm = Realm.getDefaultInstance();
        Payment payment = null;
        try {
            StoreAndForward first = realm.where(StoreAndForward.class).equalTo("payment.pay_id", payID).findFirst();
            if (first != null) {
                payment = realm.copyFromRealm(first.getPayment());
            }
        } finally {
            if (realm != null) {
                realm.close();
            }
        }

        switch (type) {
            case 1:
                String custId = payment == null ? "" : payment.getCust_id();
                sb.append("SELECT " + "IFNULL(c.cust_name,'Unknown') as 'cust_name' " + "FROM Customers c where c.cust_id = '").append(custId).append("'");
                break;
        }
        Cursor cursor = null;
        try {
            cursor = DBManager.getDatabase().rawQuery(sb.toString(), null);
            PaymentDetails paymentDetails = new PaymentDetails();
            boolean haveCustomer = cursor.moveToFirst();
            if (payment != null) {
                if (TextUtils.isEmpty(payment.getPaymethod_id()) || payment.getPaymethod_id().equalsIgnoreCase("Genius")) {
                    paymentDetails.setPaymethod_name(payment.getCard_type());
                } else {
                    paymentDetails.setPaymethod_name(payment.getPaymentMethod().getPaymethod_name());
                }
                paymentDetails.setPay_date(Global.formatToDisplayDate(payment.getPay_date(), 0));
                paymentDetails.setCustomerId(payment.getCust_id());
                paymentDetails.setPay_timecreated(Global.formatToDisplayDate(payment.getPay_timecreated(), 2));
                paymentDetails.setCust_name(haveCustomer ? cursor.getString(cursor.getColumnIndex("cust_name")) : "Unknown");
                paymentDetails.setOrd_total(payment.getPay_amount());
                paymentDetails.setPay_amount(payment.getPay_amount());
                paymentDetails.setChange(new BigDecimal(payment.getAmountTender()).subtract(new BigDecimal(payment.getPay_amount())).toString());
                paymentDetails.setPay_signature(payment.getPay_signature());
                paymentDetails.setPay_transid(payment.getPay_transid());
                paymentDetails.setCcnum_last4(payment.getCcnum_last4());
                paymentDetails.setPay_check(payment.getPay_check());
                paymentDetails.setIs_refund(payment.getIs_refund());
                paymentDetails.setIvuLottoDrawDate(payment.getIvuLottoDrawDate());
                paymentDetails.setIvuLottoNumber(payment.getIvuLottoNumber());
                paymentDetails.setIvuLottoQR(payment.getIvuLottoQR());
                paymentDetails.setPay_dueamount(payment.getPay_dueamount());
                paymentDetails.setInv_id(payment.getInv_id());
                paymentDetails.setJob_id(payment.getJob_id());
                paymentDetails.setTax1_amount(payment.getTax1_amount());
                paymentDetails.setTax2_amount(payment.getTax2_amount());
                paymentDetails.setTax3_amount(payment.getTax3_amount());
                paymentDetails.setTax1_name(payment.getTax1_name());
                paymentDetails.setTax2_name(payment.getTax2_name());
                paymentDetails.setTax3_name(payment.getTax3_name());
                paymentDetails.setEmvContainer(payment.getEmvContainer());

            }

            cursor.close();
            return paymentDetails;
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

    }

    public static List<PaymentDetails> getPaymentForPrintingTransactions(String jobID) {
        Realm realm = Realm.getDefaultInstance();
        List<StoreAndForward> storeAndForwards;
        try {
            storeAndForwards = realm.where(StoreAndForward.class).equalTo("payment.job_id", jobID).findAll();
            if (storeAndForwards != null) {
                storeAndForwards = realm.copyFromRealm(storeAndForwards);
            }
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
        List<PaymentDetails> list = new ArrayList<>();

        PaymentDetails details = new PaymentDetails();
        for (StoreAndForward sf : storeAndForwards) {
            details.setPayType("0");
            details.setPay_amount(sf.getPayment().getPay_amount());
            details.setPaymethod_name(sf.getPayment().getPaymentMethod().getPaymethod_name());
            details.setPay_tip(sf.getPayment().getPay_tip());
            details.setPay_signature(sf.getPayment().getPay_signature());
            details.setPay_transid(sf.getPayment().getPay_transid());
            details.setCcnum_last4(sf.getPayment().getCcnum_last4());
            details.setIvuLottoDrawDate(sf.getPayment().getIvuLottoDrawDate());
            details.setIvuLottoNumber(sf.getPayment().getIvuLottoNumber());
            details.setIvuLottoQR(sf.getPayment().getIvuLottoQR());
            details.setPay_dueamount(sf.getPayment().getPay_dueamount());
            list.add(details);
            details = new PaymentDetails();
        }
        return list;
    }

    public static void updateStatusDeleted(StoreAndForward storeAndForward) {
        Realm realm = Realm.getDefaultInstance();
        try {
            long id = storeAndForward.getId();
            realm.beginTransaction();
            RealmResults<StoreAndForward> all = realm.where(StoreAndForward.class).equalTo("id", id).findAll();
            if (all.isValid()) {
                all.deleteAllFromRealm();
            }
        } finally {
            realm.commitTransaction();
            if (realm != null) {
                realm.close();
            }
        }
    }

    public static void purgeDeletedStoredPayment() {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            realm.where(StoreAndForward.class).equalTo("status"
                    , StoreAndForward.StoreAndForwatdStatus.DELETED.getCode())
                    .findAll().deleteAllFromRealm();
        } finally {
            realm.commitTransaction();
            if (realm != null) {
                realm.close();
            }
        }
    }

    public static void updateStoredPaymentForRetry(StoreAndForward storeAndForward) {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            storeAndForward.setRetry(true);
        } finally {
            realm.commitTransaction();
            if (realm != null) {
                realm.close();
            }
        }
    }


    public static void insert(Activity activity, Payment payment, StoreAndForward.PaymentType paymentType) {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            StoreAndForward storeAndForward = new StoreAndForward();
            storeAndForward.setPaymentType(paymentType);
            storeAndForward.setPaymentXml(payment.getPayment_xml());
            storeAndForward.setPayment(payment);
            storeAndForward.setStoreAndForwatdStatus(StoreAndForward.StoreAndForwatdStatus.PENDING);
            storeAndForward.setRetry(false);
            storeAndForward.setCreationDate(new Date());
            storeAndForward.setId(System.currentTimeMillis());
            realm.insertOrUpdate(storeAndForward);
            realm.commitTransaction();
            // realm.close();
        } catch (Exception e) {
            realm.cancelTransaction();
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
        PaymentsHandler.setLastPaymentInserted(payment);
        new MyPreferences(activity).setLastPayID(payment.getPay_id());
    }

    public static void updateStoreForwardPaymentToRetry(StoreAndForward storeAndForward) {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            storeAndForward.setRetry(true);
        } finally {
            realm.commitTransaction();
            if (realm != null) {
                realm.close();
            }
        }
    }

    public static String getLastPaymentId(Context activity, int deviceId, int year) {
        MyPreferences myPref = new MyPreferences(activity);
        String lastPayID = myPref.getLastPayID();
        boolean getIdFromRealm = false;
        if (TextUtils.isEmpty(lastPayID) || lastPayID.length() <= 4) {
            getIdFromRealm = true;
        } else {
            String[] tokens = myPref.getLastPayID().split("-");
            if (!tokens[2].equalsIgnoreCase(String.valueOf(year))) {
                getIdFromRealm = true;
            }
        }

        if (getIdFromRealm) {
            AssignEmployee assignEmployee = AssignEmployeeDAO.getAssignEmployee();
            Realm realm = Realm.getDefaultInstance();
            try {


                StoreAndForward storeAndForward = realm.where(StoreAndForward.class)
                        .beginsWith("payment.pay_id", deviceId + "-")
                        .endsWith("payment.pay_id", "-" + year).findFirst();
                if (storeAndForward != null && storeAndForward.getPayment() != null) {
                    lastPayID = storeAndForward.getPayment().getPay_id();
                }
                if (TextUtils.isEmpty(lastPayID)) {
                    lastPayID = assignEmployee.getEmpId() + "-" + "00001" + "-" + year;
                }
                myPref.setLastPayID(lastPayID);
                realm.close();
            } finally {
                if (realm != null) {
                    realm.close();
                }
            }
        }
        return lastPayID;
    }

    public static List<StoreAndForward> getAll() {
        Realm realm = Realm.getDefaultInstance();
        try {
            List<StoreAndForward> all = realm.where(StoreAndForward.class).findAll();
            if (all != null) {
                all = realm.copyFromRealm(all);
            }
            realm.close();
            return all;
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }
}
