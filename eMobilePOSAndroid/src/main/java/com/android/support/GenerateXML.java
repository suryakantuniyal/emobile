package com.android.support;

import android.app.Activity;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Xml;

import com.android.database.AddressHandler;
import com.android.database.ConsignmentTransactionHandler;
import com.android.database.CustomerInventoryHandler;
import com.android.database.CustomersHandler;
import com.android.database.InvoicePaymentsHandler;
import com.android.database.MemoTextHandler;
import com.android.database.OrderProductsAttr_DB;
import com.android.database.OrderProductsHandler;
import com.android.database.OrdersHandler;
import com.android.database.PaymentsHandler;
import com.android.database.ShiftExpensesDBHandler;
import com.android.database.ShiftPeriodsDBHandler;
import com.android.database.TemplateHandler;
import com.android.database.TimeClockHandler;
import com.android.database.TransferInventory_DB;
import com.android.database.TransferLocations_DB;
import com.android.database.VoidTransactionsHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.models.Order;
import com.android.emobilepos.models.OrderProduct;
import com.android.emobilepos.shifts.ClockInOut_FA;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import util.StringUtil;

public class GenerateXML {

    private static final String UTF_8 = "utf-8";
    private MyPreferences info;
    private StringBuilder ending = new StringBuilder();
    private Activity thisActivity;
    private static String empstr = "";
    private MyPreferences myPref;

    public GenerateXML(Activity activity) {
        info = new MyPreferences(activity);
        thisActivity = activity;
        myPref = new MyPreferences(activity);
        if (thisActivity instanceof ClockInOut_FA) {
            try {
                ending.append("&EmpID=")
                        .append(URLEncoder.encode(((ClockInOut_FA) (thisActivity)).getClerkID(), UTF_8));
                ending.append("&ActivationKey=").append(URLEncoder.encode(info.getActivKey(), UTF_8));
                ending.append("&DeviceID=").append(URLEncoder.encode(info.getDeviceID(), UTF_8));
                ending.append("&BundleVersion=").append(URLEncoder.encode(info.getBundleVersion(), UTF_8));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else {
            try {
                ending.append("&EmpID=").append(URLEncoder.encode(info.getEmpID(), UTF_8));
                ending.append("&ActivationKey=").append(URLEncoder.encode(info.getActivKey(), UTF_8));
                ending.append("&DeviceID=").append(URLEncoder.encode(info.getDeviceID(), UTF_8));
                ending.append("&BundleVersion=").append(URLEncoder.encode(info.getBundleVersion(), UTF_8));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        }
    }

    public String getAuth() {
        StringBuilder sb = new StringBuilder();

        try {
            sb.append("getAuth.aspx?ac=").append(URLEncoder.encode(info.getAcctNumber(), UTF_8));
            sb.append("&p=").append(URLEncoder.encode(info.getAcctPassword(), UTF_8));
            sb.append(ending.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return (sb.toString());
    }

    public String getAccountLogo() {
        StringBuilder sb = new StringBuilder();
        sb.append("https://sync.enablermobile.com/deviceASXMLTrans/getLogo.aspx?RegID=");
        try {
            sb.append(URLEncoder.encode(info.getAcctNumber(), UTF_8));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    public String getOnHold(int type, String ordID) {
        StringBuilder sb = new StringBuilder();
        switch (type) {
            case Global.S_ORDERS_ON_HOLD_DETAILS:
                sb.append("getXMLOrdersOnHoldDetail.ashx");
                break;
            case Global.S_CHECK_STATUS_ON_HOLD:
                sb.append("getXMLCheckStatusOnHold.ashx");
                break;
            case Global.S_UPDATE_STATUS_ON_HOLD:
                sb.append("getXMLUpdateStatusOnHold.ashx");
                break;
            case Global.S_CHECKOUT_ON_HOLD:
                sb.append("getXMLCheckOutOnHold.ashx");
                break;
        }

        try {
            sb.append("?RegID=").append(URLEncoder.encode(info.getAcctNumber(), UTF_8)).append("&ordID=")
                    .append(URLEncoder.encode(ordID, UTF_8));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    public String getTimeClock() {
        StringBuilder sb = new StringBuilder();
        sb.append("getXMLTimeClock.ashx?RegID=");
        try {
            sb.append(URLEncoder.encode(info.getAcctNumber(), UTF_8));
            sb.append("&empid=").append(URLEncoder.encode(((ClockInOut_FA) (thisActivity)).getClerkID(), UTF_8));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    public String getDeviceID() {
        StringBuilder sb = new StringBuilder();

        try {
            sb.append("getDeviceId.aspx?ac=").append(URLEncoder.encode(info.getAcctNumber(), UTF_8));
            sb.append(ending.toString());

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return (sb.toString());
    }

    public String getFirstAvailLic() {
        StringBuilder sb = new StringBuilder();

        try {
            sb.append("getFirstAvailLic.aspx?ac=").append(URLEncoder.encode(info.getAcctNumber(), UTF_8));
            sb.append(ending.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return (sb.toString());
    }

    public String getServerTime() {
        StringBuilder sb = new StringBuilder();

        try {
            sb.append("getServerTime.ashx?ac=").append(URLEncoder.encode(info.getAcctNumber(), UTF_8));
            sb.append(ending.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    public String getDinnerTables() {
        StringBuilder sb = new StringBuilder();
        try {
            sb.append(thisActivity.getString(R.string.sync_enablermobile_getxmldinnertables)).append("?regid=")
                    .append(URLEncoder.encode(info.getAcctNumber(), UTF_8));
            sb.append(ending.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    public String getSalesAssociate() {
        StringBuilder sb = new StringBuilder();
        try {
            sb.append(thisActivity.getString(R.string.sync_enablermobile_getxmlsalesassociate)).append("?regid=")
                    .append(URLEncoder.encode(info.getAcctNumber(), UTF_8));
            sb.append(ending.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    public String getMixMatch() {
        StringBuilder sb = new StringBuilder();
        try {
            sb.append(thisActivity.getString(R.string.sync_enablermobile_getxmlmixmatch)).append("?regid=")
                    .append(URLEncoder.encode(info.getAcctNumber(), UTF_8));
            sb.append(ending.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    public String updateSyncTime(String time) {
        StringBuilder sb = new StringBuilder();

        try {
            sb.append("updateSyncTime.ashx?RegID=").append(URLEncoder.encode(info.getAcctNumber(), UTF_8));
            sb.append("&EmpID=").append(URLEncoder.encode(info.getEmpID(), UTF_8)).append("&syncTime=")
                    .append(URLEncoder.encode(time, UTF_8));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    public String getEmployees() {
        StringBuilder sb = new StringBuilder();

        try {
            sb.append("RequestEmployees.aspx?ac=").append(URLEncoder.encode(info.getAcctNumber(), UTF_8));
            sb.append(ending.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return (sb.toString());
    }

    public String assignEmployees() {
        StringBuilder sb = new StringBuilder();

        try {
            sb.append("AssignEmployees.aspx?RegID=").append(URLEncoder.encode(info.getAcctNumber(), UTF_8));
            sb.append("&MSemployeeID=").append(URLEncoder.encode(info.getEmpID(), UTF_8));
            sb.append(ending.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return (sb.toString());
    }

    public String disableEmployee() {
        StringBuilder sb = new StringBuilder();

        try {
            sb.append("DisableEmployee.aspx?ac=").append(URLEncoder.encode(info.getAcctNumber(), UTF_8));
            sb.append("&key=").append(URLEncoder.encode(info.getActivKey(), UTF_8));
            sb.append(ending.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return (sb.toString());
    }

    public String downloadPayments() {
        StringBuilder sb = new StringBuilder();

        try {
            sb.append("DownloadPayments.aspx?ac=").append(URLEncoder.encode(info.getAcctNumber(), UTF_8));
            sb.append(ending.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return (sb.toString());
    }


    public String downloadAll(String key) {
        String value = info.getXMLAction(key);
        StringBuilder sb = new StringBuilder();

        try {
            sb.append(value).append("?RegID=").append(URLEncoder.encode(info.getAcctNumber(), UTF_8));
            sb.append("&MSemployeeID=").append(URLEncoder.encode(info.getEmpID(), UTF_8));
            sb.append("&MSZoneID=").append(URLEncoder.encode(info.getZoneID(), UTF_8));
            sb.append(ending.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return (sb.toString());
    }

    private void buildAccountInformation(XmlSerializer serializer)
            throws IllegalArgumentException, IllegalStateException, IOException {
        serializer.startTag(empstr, "AccountInformation");
        serializer.startTag(empstr, "DeviceID");
        serializer.text(info.getDeviceID());
        serializer.endTag(empstr, "DeviceID");
        serializer.startTag(empstr, "EmployeeID");
        serializer.text(info.getEmpID());
        serializer.endTag(empstr, "EmployeeID");
        serializer.startTag(empstr, "ActivationKey");
        serializer.text(info.getActivKey());
        serializer.endTag(empstr, "ActivationKey");
        serializer.startTag(empstr, "Account");
        serializer.text(info.getAcctNumber());
        serializer.endTag(empstr, "Account");
        serializer.startTag(empstr, "BundleVersion");
        serializer.text(info.getBundleVersion());
        serializer.endTag(empstr, "BundleVersion");
        serializer.startTag(empstr, "Password");
        serializer.text(info.getAcctPassword());
        serializer.endTag(empstr, "Password");
        serializer.endTag(empstr, "AccountInformation");
    }

    public String synchNewCustomer() {
        XmlSerializer serializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();

        try {
            serializer.setOutput(writer);
            serializer.startDocument("UTF-8", true);
            serializer.startTag(empstr, "ASXML");

            buildAccountInformation(serializer);

            serializer.startTag(empstr, "NewCustomers");
            buildNewCustomer(serializer);
            serializer.endTag(empstr, "NewCustomers");
            serializer.endDocument();
            return writer.toString();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void buildNewCustomer(XmlSerializer serializer) {
        CustomersHandler custHandler = new CustomersHandler(thisActivity);
        Cursor cursor = custHandler.getUnsynchCustomers();
        cursor.moveToFirst();
        int size = cursor.getCount();
        for (int i = 0; i < size; i++) {
            try {

                serializer.startTag(empstr, "new_customer");

                serializer.startTag(empstr, "cust_id");
                serializer.text(cursor.getString(cursor.getColumnIndex("cust_id")));
                serializer.endTag(empstr, "cust_id");

                serializer.startTag(empstr, "cust_idRef");
                serializer.text(cursor.getString(cursor.getColumnIndex("cust_id")));
                serializer.endTag(empstr, "cust_idRef");

                serializer.startTag(empstr, "cust_name");
                serializer.text(cursor.getString(cursor.getColumnIndex("cust_name")));
                serializer.endTag(empstr, "cust_name");

                serializer.startTag(empstr, "cust_firstName");
                serializer.text(cursor.getString(cursor.getColumnIndex("cust_firstName")));
                serializer.endTag(empstr, "cust_firstName");

                serializer.startTag(empstr, "cust_lastName");
                serializer.text(cursor.getString(cursor.getColumnIndex("cust_lastName")));
                serializer.endTag(empstr, "cust_lastName");

                serializer.startTag(empstr, "CompanyName");
                serializer.text(cursor.getString(cursor.getColumnIndex("CompanyName")));
                serializer.endTag(empstr, "CompanyName");

                serializer.startTag(empstr, "cust_contact");
                serializer.text(cursor.getString(cursor.getColumnIndex("cust_contact")));
                serializer.endTag(empstr, "cust_contact");

                serializer.startTag(empstr, "cust_phone");
                serializer.text(cursor.getString(cursor.getColumnIndex("cust_phone")));
                serializer.endTag(empstr, "cust_phone");

                serializer.startTag(empstr, "cust_email");
                serializer.text(cursor.getString(cursor.getColumnIndex("cust_email")));
                serializer.endTag(empstr, "cust_email");

                serializer.startTag(empstr, "cust_birthDate");
                serializer.text(cursor.getString(cursor.getColumnIndex("cust_dob")));
                serializer.endTag(empstr, "cust_birthDate");

                serializer.startTag(empstr, "cust_addresses");
                buildCustomerAddress(serializer, cursor.getString(cursor.getColumnIndex("cust_id")));
                serializer.endTag(empstr, "cust_addresses");

                serializer.endTag(empstr, "new_customer");

                cursor.moveToNext();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
        cursor.close();

    }

    public void buildCustomerAddress(XmlSerializer serializer, String custID) {
        AddressHandler addrHandler = new AddressHandler(thisActivity);
        Cursor cursor = addrHandler.getCursorAddress(custID);
        cursor.moveToFirst();
        int size = cursor.getCount();

        for (int i = 0; i < size; i++) {
            try {
                serializer.startTag(empstr, "cust_address");

                serializer.startTag(empstr, "addr_id");
                serializer.text(cursor.getString(cursor.getColumnIndex("addr_id")));
                serializer.endTag(empstr, "addr_id");

                serializer.startTag(empstr, "addr_b_type");
                serializer.text(cursor.getString(cursor.getColumnIndex("addr_b_type")));
                serializer.endTag(empstr, "addr_b_type");

                serializer.startTag(empstr, "addr_b_str1");
                serializer.text(cursor.getString(cursor.getColumnIndex("addr_b_str1")));
                serializer.endTag(empstr, "addr_b_str1");

                serializer.startTag(empstr, "addr_b_str2");
                serializer.text(cursor.getString(cursor.getColumnIndex("addr_b_str2")));
                serializer.endTag(empstr, "addr_b_str2");

                serializer.startTag(empstr, "addr_b_str3");
                serializer.text(cursor.getString(cursor.getColumnIndex("addr_b_str3")));
                serializer.endTag(empstr, "addr_b_str3");

                serializer.startTag(empstr, "addr_b_city");
                serializer.text(cursor.getString(cursor.getColumnIndex("addr_b_city")));
                serializer.endTag(empstr, "addr_b_city");

                serializer.startTag(empstr, "addr_b_state");
                serializer.text(cursor.getString(cursor.getColumnIndex("addr_b_state")));
                serializer.endTag(empstr, "addr_b_state");

                serializer.startTag(empstr, "addr_b_country");
                serializer.text(cursor.getString(cursor.getColumnIndex("addr_b_country")));
                serializer.endTag(empstr, "addr_b_country");

                serializer.startTag(empstr, "addr_b_zipcode");
                serializer.text(cursor.getString(cursor.getColumnIndex("addr_b_zipcode")));
                serializer.endTag(empstr, "addr_b_zipcode");

                serializer.startTag(empstr, "addr_s_type");
                serializer.text(cursor.getString(cursor.getColumnIndex("addr_s_type")));
                serializer.endTag(empstr, "addr_s_type");

                serializer.startTag(empstr, "addr_s_str1");
                serializer.text(cursor.getString(cursor.getColumnIndex("addr_s_str1")));
                serializer.endTag(empstr, "addr_s_str1");

                serializer.startTag(empstr, "addr_s_str2");
                serializer.text(cursor.getString(cursor.getColumnIndex("addr_s_str2")));
                serializer.endTag(empstr, "addr_s_str2");

                serializer.startTag(empstr, "addr_s_str3");
                serializer.text(cursor.getString(cursor.getColumnIndex("addr_s_str3")));
                serializer.endTag(empstr, "addr_s_str3");

                serializer.startTag(empstr, "addr_s_city");
                serializer.text(cursor.getString(cursor.getColumnIndex("addr_s_city")));
                serializer.endTag(empstr, "addr_s_city");

                serializer.startTag(empstr, "addr_s_state");
                serializer.text(cursor.getString(cursor.getColumnIndex("addr_s_state")));
                serializer.endTag(empstr, "addr_s_state");

                serializer.startTag(empstr, "addr_s_country");
                serializer.text(cursor.getString(cursor.getColumnIndex("addr_s_country")));
                serializer.endTag(empstr, "addr_s_country");

                serializer.startTag(empstr, "addr_s_zipcode");
                serializer.text(cursor.getString(cursor.getColumnIndex("addr_s_zipcode")));
                serializer.endTag(empstr, "addr_s_zipcode");

                serializer.endTag(empstr, "cust_address");

                cursor.moveToNext();

            } catch (Exception e) {

                throw new RuntimeException(e);
            }
        }

        cursor.close();
    }

    public String synchOrders(boolean isOnHold) {

        XmlSerializer serializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();
        try {
            serializer.setOutput(writer);
            serializer.startDocument("UTF-8", true);
            serializer.startTag(empstr, "ASXML");

            buildAccountInformation(serializer);

            serializer.startTag(empstr, "Orders");
            buildOrder(serializer, isOnHold);
            serializer.endTag(empstr, "Orders");
            serializer.endDocument();

            return writer.toString();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

    public void buildOrder(XmlSerializer serializer, boolean isOnHold) {
        OrdersHandler ordersHandler = new OrdersHandler(thisActivity);
        CustomersHandler custHandler = new CustomersHandler(thisActivity);
        HashMap<String, String> custInfo = new HashMap<String, String>();
        List<Order> orders;
        if (!isOnHold) {
            orders = ordersHandler.getUnsyncOrders();
        } else
            orders = ordersHandler.getUnsyncOrdersOnHold();

        for (Order order : orders) {
            try {

                serializer.startTag(empstr, "Order");

                if (isOnHold) {
                    serializer.startTag(empstr, "holdName");
                    serializer.text(order.ord_HoldName);//cursor.getString(cursor.getColumnIndex("ord_HoldName")));
                    serializer.endTag(empstr, "holdName");
                }

                String assignedTable = order.assignedTable;//cursor.getString(cursor.getColumnIndex("assignedTable"));
                serializer.startTag(empstr, "assignedTable");
                serializer.text(StringUtil.nullStringToEmpty(assignedTable));
                serializer.endTag(empstr, "assignedTable");

                String associateID = order.associateID;//cursor.getString(cursor.getColumnIndex("associateID"));
                serializer.startTag(empstr, "associateID");
                serializer.text(StringUtil.nullStringToEmpty(associateID));
                serializer.endTag(empstr, "associateID");

                String numberOfSeats = String.valueOf(order.numberOfSeats);//cursor.getString(cursor.getColumnIndex("numberOfSeats"));
                serializer.startTag(empstr, "numberOfSeats");
                serializer.text(numberOfSeats);
                serializer.endTag(empstr, "numberOfSeats");

                serializer.startTag(empstr, "ord_timeStarted");
                serializer.text(order.ord_timeStarted);
                serializer.endTag(empstr, "ord_timeStarted");

                serializer.startTag(empstr, "ord_id");
                serializer.text(order.ord_id);//cursor.getString(cursor.getColumnIndex("ord_id")));
                serializer.endTag(empstr, "ord_id");

                serializer.startTag(empstr, "qbord_id");
                serializer.text(order.qbord_id);//cursor.getString(cursor.getColumnIndex("qbord_id")));
                serializer.endTag(empstr, "qbord_id");

                serializer.startTag(empstr, "emp_id");
                serializer.text(order.emp_id);//cursor.getString(cursor.getColumnIndex("emp_id")));
                serializer.endTag(empstr, "emp_id");

                serializer.startTag(empstr, "cust_id");
                serializer.text(order.cust_id);//cursor.getString(cursor.getColumnIndex("cust_id")));
                serializer.endTag(empstr, "cust_id");

                serializer.startTag(empstr, "clerk_id");
                serializer.text(order.clerk_id);//cursor.getString(cursor.getColumnIndex("clerk_id")));
                serializer.endTag(empstr, "clerk_id");

                serializer.startTag(empstr, "cust_email");
                serializer.text(order.c_email);//cursor.getString(cursor.getColumnIndex("c_email")));
                serializer.endTag(empstr, "cust_email");

                serializer.startTag(empstr, "ord_signature");
                serializer.text(order.ord_signature);// cursor.getString(cursor.getColumnIndex("ord_signature")));
                serializer.endTag(empstr, "ord_signature");

                serializer.startTag(empstr, "ord_po");
                serializer.text(order.ord_po);//cursor.getString(cursor.getColumnIndex("ord_po")));
                serializer.endTag(empstr, "ord_po");

                serializer.startTag(empstr, "total_lines");
                serializer.text(order.total_lines);//cursor.getString(cursor.getColumnIndex("total_lines")));
                serializer.endTag(empstr, "total_lines");

                serializer.startTag(empstr, "total_lines_pay");
                serializer.text(order.total_lines_pay);//cursor.getString(cursor.getColumnIndex("total_lines_pay")));
                serializer.endTag(empstr, "total_lines_pay");

                serializer.startTag(empstr, "ord_total");
                serializer.text(order.ord_total);//cursor.getString(cursor.getColumnIndex("ord_total")));
                serializer.endTag(empstr, "ord_total");

                serializer.startTag(empstr, "ord_comment");
                serializer.text(order.ord_comment);//cursor.getString(cursor.getColumnIndex("ord_comment")));
                serializer.endTag(empstr, "ord_comment");

                serializer.startTag(empstr, "ord_delivery");
                serializer.text(order.ord_delivery);//cursor.getString(cursor.getColumnIndex("ord_delivery")));
                serializer.endTag(empstr, "ord_delivery");

                serializer.startTag(empstr, "ord_timecreated");
                serializer.text(order.ord_timecreated);//cursor.getString(cursor.getColumnIndex("ord_timecreated")));
                serializer.endTag(empstr, "ord_timecreated");

                serializer.startTag(empstr, "ord_timesync");
                serializer.text(order.ord_timesync);//cursor.getString(cursor.getColumnIndex("ord_timesync")));
                serializer.endTag(empstr, "ord_timesync");

                serializer.startTag(empstr, "qb_synctime");
                serializer.text(order.qb_synctime);//cursor.getString(cursor.getColumnIndex("qb_synctime")));
                serializer.endTag(empstr, "qb_synctime");

                serializer.startTag(empstr, "emailed");
                serializer.text(order.emailed);//cursor.getString(cursor.getColumnIndex("emailed")));
                serializer.endTag(empstr, "emailed");

                serializer.startTag(empstr, "processed");
                serializer.text(order.processed);//cursor.getString(cursor.getColumnIndex("processed")));
                serializer.endTag(empstr, "processed");

                serializer.startTag(empstr, "ord_type");
                serializer.text(order.ord_type);//cursor.getString(cursor.getColumnIndex("ord_type")));
                serializer.endTag(empstr, "ord_type");

                serializer.startTag(empstr, "ord_claimnumber");
                serializer.text(order.ord_claimnumber);//cursor.getString(cursor.getColumnIndex("ord_claimnumber")));
                serializer.endTag(empstr, "ord_claimnumber");

                serializer.startTag(empstr, "ord_rganumber");
                serializer.text(order.ord_rganumber);//cursor.getString(cursor.getColumnIndex("ord_rganumber")));
                serializer.endTag(empstr, "ord_rganumber");

                serializer.startTag(empstr, "ord_returns_pu");
                serializer.text(order.ord_returns_pu);//cursor.getString(cursor.getColumnIndex("ord_returns_pu")));
                serializer.endTag(empstr, "ord_returns_pu");

                serializer.startTag(empstr, "ord_inventory");
                serializer.text(order.ord_inventory);//cursor.getString(cursor.getColumnIndex("ord_inventory")));
                serializer.endTag(empstr, "ord_inventory");

                serializer.startTag(empstr, "ord_issync");
                serializer.text(order.ord_issync);//cursor.getString(cursor.getColumnIndex("ord_issync")));
                serializer.endTag(empstr, "ord_issync");

                serializer.startTag(empstr, "tax_id");
                serializer.text(order.tax_id);//cursor.getString(cursor.getColumnIndex("tax_id")));
                serializer.endTag(empstr, "tax_id");

                serializer.startTag(empstr, "ord_shipvia");
                serializer.text(order.ord_shipvia);//cursor.getString(cursor.getColumnIndex("ord_shipvia")));
                serializer.endTag(empstr, "ord_shipvia");

                serializer.startTag(empstr, "ord_shipto");
                serializer.text(order.ord_shipto);//cursor.getString(cursor.getColumnIndex("ord_shipto")));
                serializer.endTag(empstr, "ord_shipto");

                serializer.startTag(empstr, "ord_terms");
                serializer.text(order.ord_terms);//cursor.getString(cursor.getColumnIndex("ord_terms")));
                serializer.endTag(empstr, "ord_terms");

                serializer.startTag(empstr, "ord_custmsg");
                serializer.text(order.ord_custmsg);//cursor.getString(cursor.getColumnIndex("ord_custmsg")));
                serializer.endTag(empstr, "ord_custmsg");

                serializer.startTag(empstr, "ord_class");
                serializer.text(order.ord_class);//cursor.getString(cursor.getColumnIndex("ord_class")));
                serializer.endTag(empstr, "ord_class");

                serializer.startTag(empstr, "ord_subtotal");
                serializer.text(order.ord_subtotal);//cursor.getString(cursor.getColumnIndex("ord_subtotal")));
                serializer.endTag(empstr, "ord_subtotal");

                serializer.startTag(empstr, "ord_taxamount");
                serializer.text(order.ord_taxamount);//cursor.getString(cursor.getColumnIndex("ord_taxamount")));
                serializer.endTag(empstr, "ord_taxamount");

                serializer.startTag(empstr, "ord_discount");
                serializer.text(order.ord_discount);//cursor.getString(cursor.getColumnIndex("ord_discount")));
                serializer.endTag(empstr, "ord_discount");

                serializer.startTag(empstr, "ord_discount_id");
                serializer.text(order.ord_discount_id);//cursor.getString(cursor.getColumnIndex("ord_discount_id")));
                serializer.endTag(empstr, "ord_discount_id");

                serializer.startTag(empstr, "ord_latitude");
                serializer.text(order.ord_latitude);//cursor.getString(cursor.getColumnIndex("ord_latitude")));
                serializer.endTag(empstr, "ord_latitude");

                serializer.startTag(empstr, "ord_longitude");
                serializer.text(order.ord_longitude);//cursor.getString(cursor.getColumnIndex("ord_longitude")));
                serializer.endTag(empstr, "ord_longitude");

                serializer.startTag(empstr, "tipAmount");
                serializer.text(order.tipAmount);//cursor.getString(cursor.getColumnIndex("tipAmount")));
                serializer.endTag(empstr, "tipAmount");

                serializer.startTag(empstr, "VAT");
                serializer.text(
                        Boolean.toString(order.VAT.equals("1")));//cursor.getString(cursor.getColumnIndex("VAT")).equals("1")));
                serializer.endTag(empstr, "VAT");

                custInfo = custHandler.getXMLCustAddr(order.cust_id);//cursor.getString(cursor.getColumnIndex("cust_id")));
                serializer.startTag(empstr, "cust_fname");
                serializer.text(getCustAddr(custInfo, "cust_fname"));
                serializer.endTag(empstr, "cust_fname");

                serializer.startTag(empstr, "cust_lname");
                serializer.text(getCustAddr(custInfo, "cust_lname"));
                serializer.endTag(empstr, "cust_lname");

                serializer.startTag(empstr, "Billing");

                serializer.attribute(empstr, "type", "Business");
                serializer.startTag(empstr, "addr_b_str1");
                serializer.text(getCustAddr(custInfo, "addr_b_str1"));
                serializer.endTag(empstr, "addr_b_str1");
                serializer.startTag(empstr, "addr_b_str2");
                serializer.text(getCustAddr(custInfo, "addr_b_str2"));
                serializer.endTag(empstr, "addr_b_str2");
                serializer.startTag(empstr, "addr_b_str3");
                serializer.text(getCustAddr(custInfo, "addr_b_str3"));
                serializer.endTag(empstr, "addr_b_str3");
                serializer.startTag(empstr, "addr_b_city");
                serializer.text(getCustAddr(custInfo, "addr_b_city"));
                serializer.endTag(empstr, "addr_b_city");
                serializer.startTag(empstr, "addr_b_state");
                serializer.text(getCustAddr(custInfo, "addr_b_state"));
                serializer.endTag(empstr, "addr_b_state");
                serializer.startTag(empstr, "addr_b_country");
                serializer.text(getCustAddr(custInfo, "addr_b_country"));
                serializer.endTag(empstr, "addr_b_country");
                serializer.startTag(empstr, "addr_b_zipcode");
                serializer.text(getCustAddr(custInfo, "addr_b_zipcode"));
                serializer.endTag(empstr, "addr_b_zipcode");

                serializer.endTag(empstr, "Billing");

                serializer.startTag(empstr, "Shipping");

                serializer.attribute(empstr, "type", "Business");
                serializer.startTag(empstr, "addr_s_str1");
                serializer.text(getCustAddr(custInfo, "addr_s_str1"));
                serializer.endTag(empstr, "addr_s_str1");
                serializer.startTag(empstr, "addr_s_str2");
                serializer.text(getCustAddr(custInfo, "addr_s_str2"));
                serializer.endTag(empstr, "addr_s_str2");
                serializer.startTag(empstr, "addr_s_str3");
                serializer.text(getCustAddr(custInfo, "addr_s_str3"));
                serializer.endTag(empstr, "addr_s_str3");
                serializer.startTag(empstr, "addr_s_city");
                serializer.text(getCustAddr(custInfo, "addr_s_city"));
                serializer.endTag(empstr, "addr_s_city");
                serializer.startTag(empstr, "addr_s_state");
                serializer.text(getCustAddr(custInfo, "addr_s_state"));
                serializer.endTag(empstr, "addr_s_state");
                serializer.startTag(empstr, "addr_s_country");
                serializer.text(getCustAddr(custInfo, "addr_s_country"));
                serializer.endTag(empstr, "addr_s_country");
                serializer.startTag(empstr, "addr_s_zipcode");
                serializer.text(getCustAddr(custInfo, "addr_s_zipcode"));
                serializer.endTag(empstr, "addr_s_zipcode");

                serializer.endTag(empstr, "Shipping");

                serializer.startTag(empstr, "OrderProducts");
                if (myPref.getPreferences(MyPreferences.pref_restaurant_mode)) {
                    if (order.isOnHold.equalsIgnoreCase("0"))//cursor.getString(cursor.getColumnIndex("isOnHold")).equals("0")) // not
                        // on
                        // hold
                        buildOrderProducts(serializer, order.ord_id, true, false);
                    else
                        buildOrderProducts(serializer, order.ord_id, true, true);
                } else {
                    buildOrderProducts(serializer, order.ord_id, false, false);
                }
                serializer.endTag(empstr, "OrderProducts");
                serializer.endTag(empstr, "Order");

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }

    public void buildOrder(XmlSerializer serializer, boolean isOnHold, Order order)
            throws IllegalArgumentException, IllegalStateException, IOException {
        order.ord_timesync = DateUtils.getDateAsString(new Date());
        serializer.startTag(empstr, "Order");

        if (isOnHold) {
            serializer.startTag(empstr, "holdName");
            serializer.text(order.ord_HoldName);
            serializer.endTag(empstr, "holdName");
        }


        serializer.startTag(empstr, "associateID");
        serializer.text(StringUtil.nullStringToEmpty(order.associateID));
        serializer.endTag(empstr, "associateID");
        serializer.startTag(empstr, "assignedTable");
        serializer.text(StringUtil.nullStringToEmpty(order.assignedTable));
        serializer.endTag(empstr, "assignedTable");


        serializer.startTag(empstr, "ord_id");
        serializer.text(order.ord_id);
        serializer.endTag(empstr, "ord_id");

        serializer.startTag(empstr, "qbord_id");
        serializer.text(order.qbord_id);
        serializer.endTag(empstr, "qbord_id");

        serializer.startTag(empstr, "emp_id");
        serializer.text(order.emp_id);
        serializer.endTag(empstr, "emp_id");

        serializer.startTag(empstr, "cust_id");
        serializer.text(order.cust_id);
        serializer.endTag(empstr, "cust_id");

        serializer.startTag(empstr, "clerk_id");
        serializer.text(order.clerk_id);
        serializer.endTag(empstr, "clerk_id");

        serializer.startTag(empstr, "cust_email");
        serializer.text(order.c_email);
        serializer.endTag(empstr, "cust_email");

        serializer.startTag(empstr, "ord_signature");
        serializer.text(order.ord_signature);
        serializer.endTag(empstr, "ord_signature");

        serializer.startTag(empstr, "ord_po");
        serializer.text(order.ord_po);
        serializer.endTag(empstr, "ord_po");

        serializer.startTag(empstr, "total_lines");
        serializer.text(order.total_lines);
        serializer.endTag(empstr, "total_lines");

        serializer.startTag(empstr, "total_lines_pay");
        serializer.text(order.total_lines_pay);
        serializer.endTag(empstr, "total_lines_pay");

        serializer.startTag(empstr, "ord_total");
        serializer.text(order.ord_total);
        serializer.endTag(empstr, "ord_total");

        serializer.startTag(empstr, "ord_comment");
        serializer.text(order.ord_comment);
        serializer.endTag(empstr, "ord_comment");

        serializer.startTag(empstr, "ord_delivery");
        serializer.text(order.ord_delivery);
        serializer.endTag(empstr, "ord_delivery");

        serializer.startTag(empstr, "ord_timecreated");
        serializer.text(order.ord_timecreated);
        serializer.endTag(empstr, "ord_timecreated");

        serializer.startTag(empstr, "ord_timesync");
        serializer.text(order.ord_timesync);
        serializer.endTag(empstr, "ord_timesync");

        serializer.startTag(empstr, "qb_synctime");
        serializer.text(order.qb_synctime);
        serializer.endTag(empstr, "qb_synctime");

        serializer.startTag(empstr, "emailed");
        serializer.text(order.emailed);
        serializer.endTag(empstr, "emailed");

        serializer.startTag(empstr, "processed");
        serializer.text(order.processed);
        serializer.endTag(empstr, "processed");

        serializer.startTag(empstr, "ord_type");
        serializer.text(order.ord_type);
        serializer.endTag(empstr, "ord_type");

        serializer.startTag(empstr, "ord_claimnumber");
        serializer.text(order.ord_claimnumber);
        serializer.endTag(empstr, "ord_claimnumber");

        serializer.startTag(empstr, "ord_rganumber");
        serializer.text(order.ord_rganumber);
        serializer.endTag(empstr, "ord_rganumber");

        serializer.startTag(empstr, "ord_returns_pu");
        serializer.text(order.ord_returns_pu);
        serializer.endTag(empstr, "ord_returns_pu");

        serializer.startTag(empstr, "ord_inventory");
        serializer.text(order.ord_inventory);
        serializer.endTag(empstr, "ord_inventory");

        serializer.startTag(empstr, "ord_issync");
        serializer.text(order.ord_issync);
        serializer.endTag(empstr, "ord_issync");

        serializer.startTag(empstr, "tax_id");
        serializer.text(order.tax_id);
        serializer.endTag(empstr, "tax_id");

        serializer.startTag(empstr, "ord_shipvia");
        serializer.text(order.ord_shipvia);
        serializer.endTag(empstr, "ord_shipvia");

        serializer.startTag(empstr, "ord_shipto");
        serializer.text(order.ord_shipto);
        serializer.endTag(empstr, "ord_shipto");

        serializer.startTag(empstr, "ord_terms");
        serializer.text(order.ord_terms);
        serializer.endTag(empstr, "ord_terms");

        serializer.startTag(empstr, "ord_custmsg");
        serializer.text(order.ord_custmsg);
        serializer.endTag(empstr, "ord_custmsg");

        serializer.startTag(empstr, "ord_class");
        serializer.text(order.ord_class);
        serializer.endTag(empstr, "ord_class");

        serializer.startTag(empstr, "ord_subtotal");
        serializer.text(order.ord_subtotal);
        serializer.endTag(empstr, "ord_subtotal");

        serializer.startTag(empstr, "ord_taxamount");
        serializer.text(order.ord_taxamount);
        serializer.endTag(empstr, "ord_taxamount");

        serializer.startTag(empstr, "ord_discount");
        serializer.text(order.ord_discount);
        serializer.endTag(empstr, "ord_discount");

        serializer.startTag(empstr, "ord_discount_id");
        serializer.text(order.ord_discount_id);
        serializer.endTag(empstr, "ord_discount_id");

        serializer.startTag(empstr, "ord_latitude");
        serializer.text(order.ord_latitude);
        serializer.endTag(empstr, "ord_latitude");

        serializer.startTag(empstr, "ord_longitude");
        serializer.text(order.ord_longitude);
        serializer.endTag(empstr, "ord_longitude");

        serializer.startTag(empstr, "tipAmount");
        serializer.text(order.tipAmount);
        serializer.endTag(empstr, "tipAmount");

        serializer.startTag(empstr, "VAT");
        serializer.text(Boolean.toString(order.VAT.equals("1")));
        serializer.endTag(empstr, "VAT");

        serializer.startTag(empstr, "OrderProducts");
        buildOrderProducts(serializer, order.ord_id, myPref.getPreferences(MyPreferences.pref_restaurant_mode),
                isOnHold);
        serializer.endTag(empstr, "OrderProducts");
        serializer.endTag(empstr, "Order");

    }

    private String getCustAddr(HashMap<String, String> map, String key) {
        String val = map.get(key);
        if (val == null)
            val = empstr;
        return val;
    }

    public void buildOrderProducts(XmlSerializer serializer, String limiter, boolean isRestMode, boolean isOnHold) {
        OrderProductsHandler handler = new OrderProductsHandler(thisActivity);
        Cursor cursor = handler.getCursorData(limiter);
        cursor.moveToFirst();
        int size = cursor.getCount();

        for (int i = 0; i < size; i++) {
            try {
                OrderProduct product = OrderProductsHandler.getOrderProduct(cursor);
                if (!isRestMode || (isRestMode && ((cursor.getString(cursor.getColumnIndex("addon")).equals("0"))
                        || (cursor.getString(cursor.getColumnIndex("addon")).equals("1") && isOnHold)))) {
                    serializer.startTag(empstr, "OrderProduct");

                    String assignedSeat = product.getAssignedSeat();//cursor.getString(cursor.getColumnIndex("assignedSeat"));
                    serializer.startTag(empstr, "assignedSeat");
                    serializer.text(StringUtil.nullStringToEmpty(assignedSeat));
                    serializer.endTag(empstr, "assignedSeat");

                    String parentAddonOrderProductId = product.getParentAddonOrderProductId();
                    serializer.startTag(empstr, "parentAddonOrderProductId");
                    serializer.text(StringUtil.nullStringToEmpty(parentAddonOrderProductId));
                    serializer.endTag(empstr, "parentAddonOrderProductId");

                    String seatGroupId = String.valueOf(product.getSeatGroupId());// cursor.getString(cursor.getColumnIndex("seatGroupId"));
                    serializer.startTag(empstr, "seatGroupId");
                    serializer.text(StringUtil.nullStringToEmpty(seatGroupId));
                    serializer.endTag(empstr, "seatGroupId");


                    serializer.startTag(empstr, "isAddon");
                    serializer.text(product.getAddon());//cursor.getString(cursor.getColumnIndex("addon")));
                    serializer.endTag(empstr, "isAddon");

                    serializer.startTag(empstr, "isAdded");
                    serializer.text(product.getIsAdded());//cursor.getString(cursor.getColumnIndex("isAdded")));
                    serializer.endTag(empstr, "isAdded");

                    serializer.startTag(empstr, "isPrinted");

                    if (!isOnHold)
                        serializer.text(product.getIsPrinted());//cursor.getString(cursor.getColumnIndex("isPrinted")));
                    else
                        serializer.text("1");

                    serializer.endTag(empstr, "isPrinted");

                    String itemVoid = product.getItem_void();//cursor.getString(cursor.getColumnIndex("item_void"));
                    if (!TextUtils.isEmpty(itemVoid)) {
                        serializer.startTag(empstr, "item_void");
                        serializer.text(product.getItem_void());//cursor.getString(cursor.getColumnIndex("item_void")));
                        serializer.endTag(empstr, "item_void");
                    }
                    serializer.startTag(empstr, "ordprod_id");
                    serializer.text(product.getOrdprod_id());//cursor.getString(cursor.getColumnIndex("ordprod_id")));
                    serializer.endTag(empstr, "ordprod_id");

                    serializer.startTag(empstr, "ord_id");
                    serializer.text(product.getOrd_id());//cursor.getString(cursor.getColumnIndex("ord_id")));
                    serializer.endTag(empstr, "ord_id");

                    serializer.startTag(empstr, "prod_id");
                    serializer.text(product.getProd_id());//cursor.getString(cursor.getColumnIndex("prod_id")));
                    serializer.endTag(empstr, "prod_id");

                    serializer.startTag(empstr, "ordprod_qty");
                    serializer.text(product.getOrdprod_qty());//cursor.getString(cursor.getColumnIndex("ordprod_qty")));
                    serializer.endTag(empstr, "ordprod_qty");
//                    BigDecimal price = new BigDecimal(cursor.getString(cursor.getColumnIndex("overwrite_price")))
//                            .multiply(new BigDecimal(cursor.getString(cursor.getColumnIndex("ordprod_qty"))));
                    serializer.startTag(empstr, "overwrite_price");
                    serializer.text(product.getFinalPrice());//cursor.getString(cursor.getColumnIndex("overwrite_price")));
                    serializer.endTag(empstr, "overwrite_price");

                    serializer.startTag(empstr, "reason_id");
                    serializer.text(product.getReason_id());//cursor.getString(cursor.getColumnIndex("reason_id")));
                    serializer.endTag(empstr, "reason_id");

                    serializer.startTag(empstr, "ordprod_name");
                    serializer.text(product.getOrdprod_name());//cursor.getString(cursor.getColumnIndex("ordprod_name")));
                    serializer.endTag(empstr, "ordprod_name");

                    serializer.startTag(empstr, "ordprod_desc");
                    serializer.text(product.getOrdprod_desc());//cursor.getString(cursor.getColumnIndex("ordprod_desc")));
                    serializer.endTag(empstr, "ordprod_desc");

                    serializer.startTag(empstr, "pricelevel_id");
                    serializer.text(product.getPricelevel_id());//cursor.getString(cursor.getColumnIndex("pricelevel_id")));
                    serializer.endTag(empstr, "pricelevel_id");

                    serializer.startTag(empstr, "prod_seq");
                    serializer.text(product.getProd_seq());//cursor.getString(cursor.getColumnIndex("prod_seq")));
                    serializer.endTag(empstr, "prod_seq");

                    serializer.startTag(empstr, "uom_name");
                    serializer.text(product.getUom_name());//cursor.getString(cursor.getColumnIndex("uom_name")));
                    serializer.endTag(empstr, "uom_name");

                    // <uom_id>Packet</uom_id>
                    serializer.startTag(empstr, "uom_id");
                    serializer.text(product.getUom_id());//cursor.getString(cursor.getColumnIndex("uom_id")));
                    serializer.endTag(empstr, "uom_id");

                    serializer.startTag(empstr, "uom_conversion");
                    serializer.text(product.getUom_conversion());//cursor.getString(cursor.getColumnIndex("uom_conversion")));
                    serializer.endTag(empstr, "uom_conversion");

                    serializer.startTag(empstr, "prod_taxId");
                    serializer.text(product.getProd_taxId());//cursor.getString(cursor.getColumnIndex("prod_taxId")));
                    serializer.endTag(empstr, "prod_taxId");

                    serializer.startTag(empstr, "totalLineValue");
                    serializer.text(cursor.getString(cursor.getColumnIndex("totalLineValue")));
                    serializer.endTag(empstr, "totalLineValue");
                    String prod_taxValue = Global.getRoundBigDecimal(new BigDecimal(cursor.getDouble(cursor.getColumnIndex("prod_taxValue"))));

                    serializer.startTag(empstr, "prod_taxValue");
                    serializer.text(prod_taxValue);
                    serializer.endTag(empstr, "prod_taxValue");

                    serializer.startTag(empstr, "prod_discountId");
                    serializer.text(product.getDiscount_id());//cursor.getString(cursor.getColumnIndex("discount_id")));
                    serializer.endTag(empstr, "prod_discountId");

                    serializer.startTag(empstr, "prod_discountValue");
                    serializer.text(product.getDiscount_value());//cursor.getString(cursor.getColumnIndex("discount_value")));
                    serializer.endTag(empstr, "prod_discountValue");

                    // <Attributes/>
                    serializer.startTag(empstr, "Attributes");
                    buildOrdProdAttr(serializer, cursor.getString(cursor.getColumnIndex("ordprod_id")));
                    serializer.endTag(empstr, "Attributes");

                    serializer.endTag(empstr, "OrderProduct");
                }
                cursor.moveToNext();

            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
        cursor.close();

    }

    private void buildOrdProdAttr(XmlSerializer serializer, String value) {
        OrderProductsAttr_DB handler = new OrderProductsAttr_DB(thisActivity);
        Cursor c = handler.getOrdProdAttr(value);
        c.moveToFirst();
        int size = c.getCount();

        for (int i = 0; i < size; i++) {
            try {
                serializer.startTag(empstr, "Attribute");

                serializer.startTag(empstr, "attribute_id");
                serializer.text(c.getString(c.getColumnIndex("attribute_id")));
                serializer.endTag(empstr, "attribute_id");

                serializer.startTag(empstr, "name");
                serializer.text(c.getString(c.getColumnIndex("name")));
                serializer.endTag(empstr, "name");

                serializer.startTag(empstr, "value");
                serializer.text(c.getString(c.getColumnIndex("value")));
                serializer.endTag(empstr, "value");

                serializer.endTag(empstr, "Attribute");
                c.moveToNext();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
        c.close();
    }

    public String synchPayments() {
        XmlSerializer serializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();
        try {
            serializer.setOutput(writer);
            serializer.startDocument("UTF-8", true);
            serializer.startTag(empstr, "ASXML");

            buildAccountInformation(serializer);
            serializer.startTag(empstr, "Payments");
            buildPayments(serializer);
            serializer.endTag(empstr, "Payments");
            serializer.endDocument();

            return writer.toString();

        } catch (Exception e) {
            throw new RuntimeException(e);

        }
    }

    public void buildPayments(XmlSerializer serializer) {
        PaymentsHandler handler = new PaymentsHandler(thisActivity);
        Cursor cursor = handler.getUnsyncPayments();
        cursor.moveToFirst();
        int size = cursor.getCount();
        String payID;
        for (int i = 0; i < size; i++) {
            try {
                serializer.startTag(empstr, "Payment");

                payID = cursor.getString(cursor.getColumnIndex("pay_id"));

                serializer.startTag(empstr, "pay_id");
                serializer.text(payID);
                serializer.endTag(empstr, "pay_id");

                serializer.startTag(empstr, "group_pay_id");
                serializer.text(cursor.getString(cursor.getColumnIndex("group_pay_id")));
                serializer.endTag(empstr, "group_pay_id");

                serializer.startTag(empstr, "cust_id");
                serializer.text(cursor.getString(cursor.getColumnIndex("cust_id")));
                serializer.endTag(empstr, "cust_id");

                serializer.startTag(empstr, "pay_latitude");
                serializer.text(cursor.getString(cursor.getColumnIndex("pay_latitude")));
                serializer.endTag(empstr, "pay_latitude");

                serializer.startTag(empstr, "pay_longitude");
                serializer.text(cursor.getString(cursor.getColumnIndex("pay_longitude")));
                serializer.endTag(empstr, "pay_longitude");

                serializer.startTag(empstr, "emp_id");
                serializer.text(cursor.getString(cursor.getColumnIndex("emp_id")));
                serializer.endTag(empstr, "emp_id");

                serializer.startTag(empstr, "paymethod_id");
                serializer.text(cursor.getString(cursor.getColumnIndex("paymethod_id")));
                serializer.endTag(empstr, "paymethod_id");

                serializer.startTag(empstr, "pay_check");
                serializer.text(cursor.getString(cursor.getColumnIndex("pay_check")));
                serializer.endTag(empstr, "pay_check");

                serializer.startTag(empstr, "pay_receipt");
                serializer.text(cursor.getString(cursor.getColumnIndex("pay_receipt")));
                serializer.endTag(empstr, "pay_receipt");

                serializer.startTag(empstr, "pay_amount");
                serializer.text(cursor.getString(cursor.getColumnIndex("pay_amount")));
                serializer.endTag(empstr, "pay_amount");

                serializer.startTag(empstr, "tipAmount");
                serializer.text(cursor.getString(cursor.getColumnIndex("pay_tip")));
                serializer.endTag(empstr, "tipAmount");

                serializer.startTag(empstr, "pay_comment");
                serializer.text(cursor.getString(cursor.getColumnIndex("pay_comment")));
                serializer.endTag(empstr, "pay_comment");

                serializer.startTag(empstr, "pay_timecreated");
                serializer.text(cursor.getString(cursor.getColumnIndex("pay_timecreated")));
                serializer.endTag(empstr, "pay_timecreated");

                serializer.startTag(empstr, "pay_timesync");
                serializer.text(cursor.getString(cursor.getColumnIndex("pay_timesync")));
                serializer.endTag(empstr, "pay_timesync");

                serializer.startTag(empstr, "account_id");
                serializer.text(cursor.getString(cursor.getColumnIndex("account_id")));
                serializer.endTag(empstr, "account_id");

                serializer.startTag(empstr, "pay_issync");
                serializer.text(cursor.getString(cursor.getColumnIndex("pay_issync")));
                serializer.endTag(empstr, "pay_issync");

                serializer.startTag(empstr, "pay_name");
                serializer.text(cursor.getString(cursor.getColumnIndex("pay_name")));
                serializer.endTag(empstr, "pay_name");

                serializer.startTag(empstr, "processed");
                serializer.text(cursor.getString(cursor.getColumnIndex("processed")));
                serializer.endTag(empstr, "processed");

                serializer.startTag(empstr, "pay_poscode");
                serializer.text(cursor.getString(cursor.getColumnIndex("pay_poscode")));
                serializer.endTag(empstr, "pay_poscode");

                serializer.startTag(empstr, "pay_seccode");
                serializer.text(cursor.getString(cursor.getColumnIndex("pay_seccode")));
                serializer.endTag(empstr, "pay_seccode");

                serializer.startTag(empstr, "pay_resultcode");
                serializer.text(cursor.getString(cursor.getColumnIndex("pay_resultcode")));
                serializer.endTag(empstr, "pay_resultcode");

                serializer.startTag(empstr, "pay_resultmessage");
                serializer.text(cursor.getString(cursor.getColumnIndex("pay_resultmessage")));
                serializer.endTag(empstr, "pay_resultmessage");

                serializer.startTag(empstr, "pay_result");
                serializer.text(cursor.getString(cursor.getColumnIndex("pay_result")));
                serializer.endTag(empstr, "pay_result");

                serializer.startTag(empstr, "pay_date");
                serializer.text(cursor.getString(cursor.getColumnIndex("pay_date")));
                serializer.endTag(empstr, "pay_date");

                serializer.startTag(empstr, "recordnumber");
                serializer.text(cursor.getString(cursor.getColumnIndex("recordnumber")));
                serializer.endTag(empstr, "recordnumber");

                serializer.startTag(empstr, "authcode");
                serializer.text(cursor.getString(cursor.getColumnIndex("authcode")));
                serializer.endTag(empstr, "authcode");

                serializer.startTag(empstr, "pay_transid");
                serializer.text(cursor.getString(cursor.getColumnIndex("pay_transid")));
                serializer.endTag(empstr, "pay_transid");

                serializer.startTag(empstr, "status");
                serializer.text(cursor.getString(cursor.getColumnIndex("status")));
                serializer.endTag(empstr, "status");

                serializer.startTag(empstr, "job_id");
                serializer.text(cursor.getString(cursor.getColumnIndex("job_id")));
                serializer.endTag(empstr, "job_id");

                serializer.startTag(empstr, "inv_id");
                serializer.text(cursor.getString(cursor.getColumnIndex("inv_id")));
                serializer.endTag(empstr, "inv_id");

                serializer.startTag(empstr, "clerk_id");
                serializer.text(cursor.getString(cursor.getColumnIndex("clerk_id")));
                serializer.endTag(empstr, "clerk_id");

                serializer.startTag(empstr, "pay_ccfournum");
                serializer.text(cursor.getString(cursor.getColumnIndex("ccnum_last4")));
                serializer.endTag(empstr, "pay_ccfournum");

                serializer.startTag(empstr, "pay_cardtype");
                serializer.text(cursor.getString(cursor.getColumnIndex("card_type")));
                serializer.endTag(empstr, "pay_cardtype");

                serializer.startTag(empstr, "trans_type");
                serializer.text(cursor.getString(cursor.getColumnIndex("pay_type")));
                serializer.endTag(empstr, "trans_type");

                serializer.startTag(empstr, "refNumber");
                serializer.text(cursor.getString(cursor.getColumnIndex("ref_num")));
                serializer.endTag(empstr, "refNumber");

                serializer.startTag(empstr, "email");
                serializer.text(cursor.getString(cursor.getColumnIndex("pay_email")));
                serializer.endTag(empstr, "email");

                serializer.startTag(empstr, "phone");
                serializer.text(cursor.getString(cursor.getColumnIndex("pay_phone")));
                serializer.endTag(empstr, "phone");

                if (!cursor.getString(cursor.getColumnIndex("original_pay_id")).isEmpty()) {
                    serializer.startTag(empstr, "VoidBlock");
                    serializer.startTag(empstr, "original_pay_id");
                    serializer.text(cursor.getString(cursor.getColumnIndex("original_pay_id")));
                    serializer.endTag(empstr, "original_pay_id");
                    serializer.endTag(empstr, "VoidBlock");
                }

                if (Global.isIvuLoto) {
                    serializer.startTag(empstr, "ivuLotto");

                    serializer.startTag(empstr, "ivuLottoDrawDate");
                    serializer.text(cursor.getString(cursor.getColumnIndex("IvuLottoDrawDate")));
                    serializer.endTag(empstr, "ivuLottoDrawDate");
                    serializer.startTag(empstr, "ivuLottoNumber");
                    serializer.text(cursor.getString(cursor.getColumnIndex("IvuLottoNumber")));
                    serializer.endTag(empstr, "ivuLottoNumber");
                    serializer.startTag(empstr, "Tax1");
                    serializer.text(cursor.getString(cursor.getColumnIndex("Tax1_amount")));
                    serializer.endTag(empstr, "Tax1");
                    serializer.startTag(empstr, "Tax2");
                    serializer.text(cursor.getString(cursor.getColumnIndex("Tax2_amount")));
                    serializer.endTag(empstr, "Tax2");

                    serializer.endTag(empstr, "ivuLotto");
                }

                buildInvoicePayment(serializer, payID);

                serializer.endTag(empstr, "Payment");

                cursor.moveToNext();

            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
        cursor.close();
    }

    private void buildInvoicePayment(XmlSerializer serializer, String payID) {
        InvoicePaymentsHandler invPayHandler = new InvoicePaymentsHandler(thisActivity);
        List<String[]> list = invPayHandler.getInvoicesPaymentsList(payID);
        int size = list.size();
        if (size > 0) {
            try {
                serializer.startTag(empstr, "Invoices");

                for (int i = 0; i < size; i++) {
                    serializer.startTag(empstr, "InvoicePayment");

                    serializer.startTag(empstr, "inv_id");
                    serializer.text(list.get(i)[0]);
                    serializer.endTag(empstr, "inv_id");

                    serializer.startTag(empstr, "applied_amount");
                    serializer.text(list.get(i)[1]);
                    serializer.endTag(empstr, "applied_amount");

                    serializer.endTag(empstr, "InvoicePayment");

                }

                serializer.endTag(empstr, "Invoices");

            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public String syncVoidTransactions() {
        XmlSerializer serializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();
        try {
            serializer.setOutput(writer);
            serializer.startDocument("UTF-8", true);
            serializer.startTag(empstr, "ASXML");

            buildAccountInformation(serializer);

            serializer.startTag(empstr, "VoidTransactions");
            buildVoidTransactions(serializer);
            serializer.endTag(empstr, "VoidTransactions");

            serializer.endDocument();

            return writer.toString();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void buildVoidTransactions(XmlSerializer serializer) {
        VoidTransactionsHandler handler = new VoidTransactionsHandler(thisActivity);
        Cursor cursor = handler.getUnsyncVoids();
        cursor.moveToFirst();
        int size = cursor.getCount();
        for (int i = 0; i < size; i++) {
            try {

                serializer.startTag(empstr, "void");

                serializer.startTag(empstr, "ord_id");
                serializer.text(cursor.getString(cursor.getColumnIndex("ord_id")));
                serializer.endTag(empstr, "ord_id");

                serializer.startTag(empstr, "ord_type");
                serializer.text(cursor.getString(cursor.getColumnIndex("ord_type")));
                serializer.endTag(empstr, "ord_type");

                serializer.endTag(empstr, "void");
                cursor.moveToNext();

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        cursor.close();
    }

    public String synchTemplates() {

        XmlSerializer serializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();
        try {
            serializer.setOutput(writer);
            serializer.startDocument("UTF-8", true);
            serializer.startTag(empstr, "ASXML");

            buildAccountInformation(serializer);

            serializer.startTag(empstr, "CustomerTemplates");
            buildTemplate(serializer);
            serializer.endTag(empstr, "CustomerTemplates");
            serializer.endDocument();

            return writer.toString();
            // return "";

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void buildTemplate(XmlSerializer serializer) {

        TemplateHandler handler = new TemplateHandler(thisActivity);
        Cursor cursor = handler.getUnsyncTemplates();
        cursor.moveToFirst();
        int size = cursor.getCount();
        String str;
        for (int i = 0; i < size; i++) {
            try {
                serializer.startTag(empstr, "Template");

                serializer.startTag(empstr, "cust_id");
                serializer.text(cursor.getString(cursor.getColumnIndex("cust_id")));
                serializer.endTag(empstr, "cust_id");

                serializer.startTag(empstr, "product_id");
                serializer.text(cursor.getString(cursor.getColumnIndex("product_id")));
                serializer.endTag(empstr, "product_id");

                serializer.startTag(empstr, "quantity");
                serializer.text(cursor.getString(cursor.getColumnIndex("quantity")));
                serializer.endTag(empstr, "quantity");

                serializer.startTag(empstr, "price_level_id");
                serializer.text(cursor.getString(cursor.getColumnIndex("price_level_id")));
                serializer.endTag(empstr, "price_level_id");

                serializer.startTag(empstr, "price_level");
                serializer.text(cursor.getString(cursor.getColumnIndex("price_level")));
                serializer.endTag(empstr, "price_level");

                serializer.startTag(empstr, "name");
                serializer.text(cursor.getString(cursor.getColumnIndex("name")));
                serializer.endTag(empstr, "name");

                serializer.startTag(empstr, "price");
                str = cursor.getString(cursor.getColumnIndex("price"));
                if (str != null)
                    serializer.text(str);
                else
                    serializer.text("");
                serializer.endTag(empstr, "price");

                serializer.startTag(empstr, "overwrite_price");
                serializer.text(cursor.getString(cursor.getColumnIndex("overwrite_price")));
                serializer.endTag(empstr, "overwrite_price");

                serializer.endTag(empstr, "Template");

                cursor.moveToNext();

            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
        cursor.close();
    }

    public String synchCustomerInventory() {
        XmlSerializer serializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();
        try {
            serializer.setOutput(writer);
            serializer.startDocument("UTF-8", true);
            serializer.startTag(empstr, "ASXML");

            buildAccountInformation(serializer);

            serializer.startTag(empstr, "CustomerInventory");
            buildCustomerInventory(serializer);
            serializer.endTag(empstr, "CustomerInventory");
            serializer.endDocument();

            return writer.toString();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void buildCustomerInventory(XmlSerializer serializer) {
        CustomerInventoryHandler handler = new CustomerInventoryHandler(thisActivity);
        Cursor cursor = handler.getUnsychedItems();
        cursor.moveToFirst();
        int size = cursor.getCount();
        for (int i = 0; i < size; i++) {
            try {
                serializer.startTag(empstr, "Inventory");

                serializer.startTag(empstr, "cust_id");
                serializer.text(cursor.getString(cursor.getColumnIndex("cust_id")));
                serializer.endTag(empstr, "cust_id");

                serializer.startTag(empstr, "prod_id");
                serializer.text(cursor.getString(cursor.getColumnIndex("prod_id")));
                serializer.endTag(empstr, "prod_id");

                serializer.startTag(empstr, "qty");
                serializer.text(cursor.getString(cursor.getColumnIndex("qty")));
                serializer.endTag(empstr, "qty");

                serializer.startTag(empstr, "price");
                serializer.text(cursor.getString(cursor.getColumnIndex("price")));
                serializer.endTag(empstr, "price");

                serializer.startTag(empstr, "cust_update");
                serializer.text(cursor.getString(cursor.getColumnIndex("cust_update")));
                serializer.endTag(empstr, "cust_update");

                serializer.endTag(empstr, "Inventory");

                cursor.moveToNext();

            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
        cursor.close();
    }

    public String synchConsignmentTransaction() {
        XmlSerializer serializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();
        try {
            serializer.setOutput(writer);
            serializer.startDocument("UTF-8", true);
            serializer.startTag(empstr, "ASXML");

            buildAccountInformation(serializer);

            serializer.startTag(empstr, "ConsignmentTransactions");
            buildConsignmentTransaction(serializer);
            serializer.endTag(empstr, "ConsignmentTransactions");
            serializer.endDocument();

            return writer.toString();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void buildConsignmentTransaction(XmlSerializer serializer) {

        ConsignmentTransactionHandler handler = new ConsignmentTransactionHandler(thisActivity);
        Cursor cursor = handler.getUnsychedItems();
        cursor.moveToFirst();
        int size = cursor.getCount();
        for (int i = 0; i < size; i++) {
            try {
                serializer.startTag(empstr, "Transaction");

                serializer.startTag(empstr, "ConsTrans_ID");
                serializer.text(cursor.getString(cursor.getColumnIndex("ConsTrans_ID")));
                serializer.endTag(empstr, "ConsTrans_ID");

                serializer.startTag(empstr, "ConsEmp_ID");
                serializer.text(cursor.getString(cursor.getColumnIndex("ConsEmp_ID")));
                serializer.endTag(empstr, "ConsEmp_ID");

                serializer.startTag(empstr, "ConsCust_ID");
                serializer.text(cursor.getString(cursor.getColumnIndex("ConsCust_ID")));
                serializer.endTag(empstr, "ConsCust_ID");

                serializer.startTag(empstr, "ConsInvoice_ID");
                serializer.text(cursor.getString(cursor.getColumnIndex("ConsInvoice_ID")));
                serializer.endTag(empstr, "ConsInvoice_ID");

                serializer.startTag(empstr, "ConsReturn_ID");
                serializer.text(cursor.getString(cursor.getColumnIndex("ConsReturn_ID")));
                serializer.endTag(empstr, "ConsReturn_ID");

                serializer.startTag(empstr, "ConsDispatch_ID");
                serializer.text(cursor.getString(cursor.getColumnIndex("ConsDispatch_ID")));
                serializer.endTag(empstr, "ConsDispatch_ID");

                serializer.startTag(empstr, "ConsPickup_ID");
                serializer.text(cursor.getString(cursor.getColumnIndex("ConsPickup_ID")));
                serializer.endTag(empstr, "ConsPickup_ID");

                serializer.startTag(empstr, "ConsProd_ID");
                serializer.text(cursor.getString(cursor.getColumnIndex("ConsProd_ID")));
                serializer.endTag(empstr, "ConsProd_ID");

                serializer.startTag(empstr, "ConsInventory_Qty");
                serializer.text(cursor.getString(cursor.getColumnIndex("ConsOriginal_Qty")));
                serializer.endTag(empstr, "ConsInventory_Qty");

                serializer.startTag(empstr, "ConsStock_Qty");
                serializer.text(cursor.getString(cursor.getColumnIndex("ConsStock_Qty")));
                serializer.endTag(empstr, "ConsStock_Qty");

                serializer.startTag(empstr, "ConsInvoice_Qty");
                serializer.text(cursor.getString(cursor.getColumnIndex("ConsInvoice_Qty")));
                serializer.endTag(empstr, "ConsInvoice_Qty");

                serializer.startTag(empstr, "ConsReturn_Qty");
                serializer.text(cursor.getString(cursor.getColumnIndex("ConsReturn_Qty")));
                serializer.endTag(empstr, "ConsReturn_Qty");

                serializer.startTag(empstr, "ConsDispatch_Qty");
                serializer.text(cursor.getString(cursor.getColumnIndex("ConsDispatch_Qty")));
                serializer.endTag(empstr, "ConsDispatch_Qty");

                serializer.startTag(empstr, "ConsPickup_Qty");
                serializer.text(cursor.getString(cursor.getColumnIndex("ConsPickup_Qty")));
                serializer.endTag(empstr, "ConsPickup_Qty");

                serializer.startTag(empstr, "ConsNew_Qty");
                serializer.text(cursor.getString(cursor.getColumnIndex("ConsNew_Qty")));
                serializer.endTag(empstr, "ConsNew_Qty");

                serializer.startTag(empstr, "Cons_timecreated");
                serializer.text(cursor.getString(cursor.getColumnIndex("Cons_timecreated")));
                serializer.endTag(empstr, "Cons_timecreated");

                serializer.endTag(empstr, "Transaction");

                cursor.moveToNext();

            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
        cursor.close();
    }

    public String synchTimeClock() {
        XmlSerializer serializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();
        try {
            serializer.setOutput(writer);
            serializer.startDocument("UTF-8", true);
            serializer.startTag(empstr, "ASXML");

            buildAccountInformation(serializer);

            serializer.startTag(empstr, "newTimeClockRq");
            buildTimeClock(serializer);
            serializer.endTag(empstr, "newTimeClockRq");
            serializer.endDocument();

            return writer.toString();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void buildTimeClock(XmlSerializer serializer) {

        TimeClockHandler tcHandler = new TimeClockHandler(thisActivity);
        Cursor cursor = tcHandler.getAllUnsync();
        cursor.moveToFirst();
        int size = cursor.getCount();
        for (int i = 0; i < size; i++) {
            try {
                serializer.startTag(empstr, "TimeClock");

                serializer.startTag(empstr, "timeclockid");
                serializer.text(cursor.getString(cursor.getColumnIndex("timeclockid")));
                serializer.endTag(empstr, "timeclockid");

                serializer.startTag(empstr, "emp_id");
                serializer.text(cursor.getString(cursor.getColumnIndex("emp_id")));
                serializer.endTag(empstr, "emp_id");

                serializer.startTag(empstr, "status");
                serializer.text(cursor.getString(cursor.getColumnIndex("status")));
                serializer.endTag(empstr, "status");

                serializer.startTag(empstr, "issync");
                serializer.text(cursor.getString(cursor.getColumnIndex("issync")));
                serializer.endTag(empstr, "issync");

                serializer.startTag(empstr, "punchtime");
                serializer.text(cursor.getString(cursor.getColumnIndex("punchtime")));
                serializer.endTag(empstr, "punchtime");

                serializer.endTag(empstr, "TimeClock");

                cursor.moveToNext();

            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
        cursor.close();
    }

    public String synchInventoryTransfer() {
        XmlSerializer serializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();

        try {
            serializer.setOutput(writer);
            serializer.startDocument("UTF-8", true);
            serializer.startTag(empstr, "ASXML");

            buildAccountInformation(serializer);

            serializer.startTag(empstr, "UpdateInventory");
            buildUpdateInventory(serializer);
            serializer.endTag(empstr, "UpdateInventory");
            serializer.endDocument();
            return writer.toString();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void buildUpdateInventory(XmlSerializer serializer) {
        TransferLocations_DB handler = new TransferLocations_DB(thisActivity);
        Cursor c = handler.getUnsyncTransfers();
        c.moveToFirst();
        int size = c.getCount();
        for (int i = 0; i < size; i++) {
            try {
                serializer.startTag(empstr, "Location");

                serializer.startTag(empstr, TransferLocations_DB.trans_id);
                serializer.text(c.getString(c.getColumnIndex(TransferLocations_DB.trans_id)));
                serializer.endTag(empstr, TransferLocations_DB.trans_id);

                serializer.startTag(empstr, TransferLocations_DB.loc_key_from);
                serializer.text(c.getString(c.getColumnIndex(TransferLocations_DB.loc_key_from)));
                serializer.endTag(empstr, TransferLocations_DB.loc_key_from);

                serializer.startTag(empstr, TransferLocations_DB.loc_key_to);
                serializer.text(c.getString(c.getColumnIndex(TransferLocations_DB.loc_key_to)));
                serializer.endTag(empstr, TransferLocations_DB.loc_key_to);

                serializer.startTag(empstr, TransferLocations_DB.emp_id);
                serializer.text(c.getString(c.getColumnIndex(TransferLocations_DB.emp_id)));
                serializer.endTag(empstr, TransferLocations_DB.emp_id);

                serializer.startTag(empstr, TransferLocations_DB.trans_timecreated);
                serializer.text(c.getString(c.getColumnIndex(TransferLocations_DB.trans_timecreated)));
                serializer.endTag(empstr, TransferLocations_DB.trans_timecreated);

                serializer.startTag(empstr, "InventoryTransactions");
                buildInventoryTransactions(serializer, c.getString(c.getColumnIndex(TransferLocations_DB.trans_id)));
                serializer.endTag(empstr, "InventoryTransactions");

                serializer.endTag(empstr, "Location");

                c.moveToNext();

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        c.close();
    }

    private void buildInventoryTransactions(XmlSerializer serializer, String _trans_id) {
        TransferInventory_DB handler = new TransferInventory_DB(thisActivity);
        Cursor c = handler.getInventoryTransactions(_trans_id);
        c.moveToFirst();
        int size = c.getCount();
        for (int i = 0; i < size; i++) {
            try {
                serializer.startTag(empstr, "Transaction");

                serializer.startTag(empstr, TransferInventory_DB.prod_id);
                serializer.text(c.getString(c.getColumnIndex(TransferInventory_DB.prod_id)));
                serializer.endTag(empstr, TransferInventory_DB.prod_id);

                serializer.startTag(empstr, TransferInventory_DB.prod_qty);
                serializer.text(c.getString(c.getColumnIndex(TransferInventory_DB.prod_qty)));
                serializer.endTag(empstr, TransferInventory_DB.prod_qty);

                serializer.endTag(empstr, "Transaction");

                c.moveToNext();

            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
        c.close();
    }

    public static XmlSerializer getXmlSerializer() {
        XmlSerializer serializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();
        try {
            serializer.setOutput(writer);
            serializer.startDocument("UTF-8", true);
            serializer.startTag(empstr, "ASXML");
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return serializer;
    }

    public String synchShift() {
        XmlSerializer serializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();

        try {
            serializer.setOutput(writer);
            serializer.startDocument("UTF-8", true);
            serializer.startTag(empstr, "ASXML");

            buildAccountInformation(serializer);

            serializer.startTag(empstr, "ShiftPeriods");
            buildShiftPeriods(serializer);
            serializer.endTag(empstr, "ShiftPeriods");
            serializer.endDocument();

            String myString = writer.toString();
            return writer.toString();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void buildShiftPeriods(XmlSerializer serializer) {
        Long lExpenseDateCreated;
        Date dExpenseDateCreated;
        String sExpenseDateCreated;
        String shiftID;
        ShiftExpensesDBHandler shiftExpensesDBHandler;
        shiftExpensesDBHandler = new ShiftExpensesDBHandler(thisActivity);
        Cursor expensesByShift;
        ShiftPeriodsDBHandler handler = new ShiftPeriodsDBHandler(thisActivity);
        Cursor c = handler.getUnsyncShifts();
        c.moveToFirst();
        int size = c.getCount();
        for (int i = 0; i < size; i++) {
            try {
                shiftID = c.getString(c.getColumnIndex("shift_id"));

                serializer.startTag(empstr, "shift");

                serializer.startTag(empstr, "shift_id");
                serializer.text(shiftID);
                serializer.endTag(empstr, "shift_id");

                serializer.startTag(empstr, "assignee_id");
                serializer.text(c.getString(c.getColumnIndex("assignee_id")));
                serializer.endTag(empstr, "assignee_id");

                serializer.startTag(empstr, "assignee_name");
                serializer.text(c.getString(c.getColumnIndex("assignee_name")));
                serializer.endTag(empstr, "assignee_name");

                serializer.startTag(empstr, "creationDate");
                serializer.text(c.getString(c.getColumnIndex("creationDate")));
                serializer.endTag(empstr, "creationDate");


                serializer.startTag(empstr, "startTime");
                serializer.text(c.getString(c.getColumnIndex("startTime")));
                serializer.endTag(empstr, "startTime");


                if (!c.getString(c.getColumnIndex("endTime")).isEmpty()) {
                    serializer.startTag(empstr, "endTime");
                    serializer.text(c.getString(c.getColumnIndex("endTime")));
                    serializer.endTag(empstr, "endTime");
                }


                serializer.startTag(empstr, "beginning_petty_cash");
                serializer.text(c.getString(c.getColumnIndex("beginning_petty_cash")));
                serializer.endTag(empstr, "beginning_petty_cash");

                serializer.startTag(empstr, "total_expenses");
                serializer.text("0");
                serializer.endTag(empstr, "total_expenses");

                serializer.startTag(empstr, "ending_petty_cash");
                serializer.text(c.getString(c.getColumnIndex("ending_petty_cash")));
                serializer.endTag(empstr, "ending_petty_cash");

                serializer.startTag(empstr, "ending_cash");
                serializer.text(c.getString(c.getColumnIndex("total_ending_cash")));
                serializer.endTag(empstr, "ending_cash");

                serializer.startTag(empstr, "entered_close_amount");
                serializer.text(c.getString(c.getColumnIndex("entered_close_amount")));
                serializer.endTag(empstr, "entered_close_amount");

                serializer.startTag(empstr, "total_transactions_cash");
                serializer.text(c.getString(c.getColumnIndex("total_transaction_cash")));
                serializer.endTag(empstr, "total_transactions_cash");


                //get cursor with expenses for this shift
                expensesByShift = shiftExpensesDBHandler.getShiftExpenses(shiftID);

                serializer.startTag(empstr, "Expenses");

                while (!expensesByShift.isAfterLast()) {
                    serializer.startTag(empstr, "Expense");

                    serializer.startTag(empstr, "expense_id");
                    serializer.text(expensesByShift.getString(expensesByShift.getColumnIndex("_id")));
                    serializer.endTag(empstr, "expense_id");

                    serializer.startTag(empstr, "shift_id");
                    serializer.text(expensesByShift.getString(expensesByShift.getColumnIndex("shiftPeriodID")));
                    serializer.endTag(empstr, "shift_id");

                    serializer.startTag(empstr, "creationDate");
                    //the expenseID is the number of milliseconds from 1970
                    //use it to find when the expense was created
                    lExpenseDateCreated = Long.valueOf(expensesByShift.getString(expensesByShift.getColumnIndex("_id")));
                    dExpenseDateCreated = new Date(lExpenseDateCreated);
                    sExpenseDateCreated = dExpenseDateCreated.toLocaleString();
                    serializer.text(sExpenseDateCreated);
                    serializer.endTag(empstr, "creationDate");


                    serializer.startTag(empstr, "cash_amount");
                    serializer.text(expensesByShift.getString(expensesByShift.getColumnIndex("cashAmount")));
                    serializer.endTag(empstr, "cash_amount");


                    serializer.startTag(empstr, "product_id");
                    serializer.text(expensesByShift.getString(expensesByShift.getColumnIndex("productID")));
                    serializer.endTag(empstr, "product_id");


                    serializer.endTag(empstr, "Expense");

                    expensesByShift.moveToNext();
                }


                serializer.endTag(empstr, "Expenses");

                serializer.endTag(empstr, "shift");

                c.moveToNext();

            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
        c.close();
    }

    public String synchWalletReceipts() {
        XmlSerializer serializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();

        try {
            serializer.setOutput(writer);
            serializer.startDocument("UTF-8", true);

            serializer.startTag(empstr, "ASXML");

            buildAccountInformation(serializer);

            serializer.startTag(empstr, "Orders");
            builderWalletOrder(serializer);
            serializer.endTag(empstr, "Orders");
            serializer.endDocument();

            return writer.toString();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void builderWalletOrder(XmlSerializer serializer) {

        OrdersHandler handler = new OrdersHandler(thisActivity);
        MemoTextHandler memoHandler = new MemoTextHandler(thisActivity);
        CustomersHandler custHandler = new CustomersHandler(thisActivity);
        HashMap<String, String> custInfo;
        HashMap<String, String> orderInfo = memoHandler.getOrderInfo();
        Cursor c = handler.getTupyxOrders();
        c.moveToFirst();
        int size = c.getCount();
        for (int i = 0; i < size; i++) {
            try {
                serializer.startTag(empstr, "Order");

                serializer.startTag(empstr, "OrderInfo");

                serializer.startTag(empstr, "MERCHANT_NAME");
                serializer.text(StringUtil.nullStringToEmpty(orderInfo.get("MERCHANT_NAME")));
                serializer.endTag(empstr, "MERCHANT_NAME");

                serializer.startTag(empstr, "MERCHANT_EMAIL");
                serializer.text(StringUtil.nullStringToEmpty(orderInfo.get("MERCHANT_EMAIL")));
                serializer.endTag(empstr, "MERCHANT_EMAIL");

                serializer.startTag(empstr, "header1");
                serializer.text(StringUtil.nullStringToEmpty(orderInfo.get("header1")));
                serializer.endTag(empstr, "header1");

                serializer.startTag(empstr, "header2");
                serializer.text(StringUtil.nullStringToEmpty(orderInfo.get("header2")));
                serializer.endTag(empstr, "header2");

                serializer.startTag(empstr, "header3");
                serializer.text(StringUtil.nullStringToEmpty(orderInfo.get("header3")));
                serializer.endTag(empstr, "header3");

                serializer.startTag(empstr, "footer1");
                serializer.text(StringUtil.nullStringToEmpty(orderInfo.get("footer1")));
                serializer.endTag(empstr, "footer1");

                serializer.startTag(empstr, "footer2");
                serializer.text(StringUtil.nullStringToEmpty(orderInfo.get("footer2")));
                serializer.endTag(empstr, "footer2");

                serializer.startTag(empstr, "footer3");
                serializer.text(StringUtil.nullStringToEmpty(orderInfo.get("footer3")));
                serializer.endTag(empstr, "footer3");

                serializer.startTag(empstr, "EMPLOYEE_NAME");
                serializer.text(orderInfo.get("EMPLOYEE_NAME"));
                serializer.endTag(empstr, "EMPLOYEE_NAME");

                serializer.startTag(empstr, "CLERK_NAME");
                serializer.text(orderInfo.get("CLERK_NAME"));
                serializer.endTag(empstr, "CLERK_NAME");

                serializer.endTag(empstr, "OrderInfo");

                serializer.startTag(empstr, "tupyx_user_id");
                serializer.text(c.getString(c.getColumnIndex("tupyx_user_id")));
                serializer.endTag(empstr, "tupyx_user_id");

                serializer.startTag(empstr, "ord_id");
                serializer.text(c.getString(c.getColumnIndex("ord_id")));
                serializer.endTag(empstr, "ord_id");

                serializer.startTag(empstr, "qbord_id");
                serializer.text(c.getString(c.getColumnIndex("qbord_id")));
                serializer.endTag(empstr, "qbord_id");

                serializer.startTag(empstr, "emp_id");
                serializer.text(c.getString(c.getColumnIndex("emp_id")));
                serializer.endTag(empstr, "emp_id");

                serializer.startTag(empstr, "cust_id");
                serializer.text(StringUtil.nullStringToEmpty(c.getString(c.getColumnIndex("cust_id"))));
                serializer.endTag(empstr, "cust_id");

                serializer.startTag(empstr, "clerk_id");
                serializer.text(c.getString(c.getColumnIndex("clerk_id")));
                serializer.endTag(empstr, "clerk_id");

                serializer.startTag(empstr, "cust_name");
                serializer.text(StringUtil.nullStringToEmpty(c.getString(c.getColumnIndex("cust_name"))));
                serializer.endTag(empstr, "cust_name");

                serializer.startTag(empstr, "cust_email");
                serializer.text(c.getString(c.getColumnIndex("c_email")));
                serializer.endTag(empstr, "cust_email");

                serializer.startTag(empstr, "ord_signature");
                serializer.text(c.getString(c.getColumnIndex("ord_signature")));
                serializer.endTag(empstr, "ord_signature");

                serializer.startTag(empstr, "ord_po");
                serializer.text(c.getString(c.getColumnIndex("ord_po")));
                serializer.endTag(empstr, "ord_po");

                serializer.startTag(empstr, "total_lines");
                serializer.text(c.getString(c.getColumnIndex("total_lines")));
                serializer.endTag(empstr, "total_lines");

                serializer.startTag(empstr, "total_lines_pay");
                serializer.text(c.getString(c.getColumnIndex("total_lines_pay")));
                serializer.endTag(empstr, "total_lines_pay");

                serializer.startTag(empstr, "ord_total");
                serializer.text(c.getString(c.getColumnIndex("ord_total")));
                serializer.endTag(empstr, "ord_total");

                serializer.startTag(empstr, "ord_comment");
                serializer.text(c.getString(c.getColumnIndex("ord_comment")));
                serializer.endTag(empstr, "ord_comment");

                serializer.startTag(empstr, "ord_delivery");
                serializer.text(c.getString(c.getColumnIndex("ord_delivery")));
                serializer.endTag(empstr, "ord_delivery");

                serializer.startTag(empstr, "ord_timecreated");
                serializer.text(c.getString(c.getColumnIndex("ord_timecreated")));
                serializer.endTag(empstr, "ord_timecreated");

                serializer.startTag(empstr, "ord_timesync");
                serializer.text(c.getString(c.getColumnIndex("ord_timesync")));
                serializer.endTag(empstr, "ord_timesync");

                serializer.startTag(empstr, "qb_synctime");
                serializer.text(c.getString(c.getColumnIndex("qb_synctime")));
                serializer.endTag(empstr, "qb_synctime");

                serializer.startTag(empstr, "emailed");
                serializer.text(c.getString(c.getColumnIndex("emailed")));
                serializer.endTag(empstr, "emailed");

                serializer.startTag(empstr, "processed");
                serializer.text(c.getString(c.getColumnIndex("processed")));
                serializer.endTag(empstr, "processed");

                serializer.startTag(empstr, "ord_type");
                serializer.text(c.getString(c.getColumnIndex("ord_type")));
                serializer.endTag(empstr, "ord_type");

                serializer.startTag(empstr, "ord_claimnumber");
                serializer.text(c.getString(c.getColumnIndex("ord_claimnumber")));
                serializer.endTag(empstr, "ord_claimnumber");

                serializer.startTag(empstr, "ord_rganumber");
                serializer.text(c.getString(c.getColumnIndex("ord_rganumber")));
                serializer.endTag(empstr, "ord_rganumber");

                serializer.startTag(empstr, "ord_returns_pu");
                serializer.text(c.getString(c.getColumnIndex("ord_returns_pu")));
                serializer.endTag(empstr, "ord_returns_pu");

                serializer.startTag(empstr, "ord_inventory");
                serializer.text(c.getString(c.getColumnIndex("ord_inventory")));
                serializer.endTag(empstr, "ord_inventory");

                serializer.startTag(empstr, "ord_issync");
                serializer.text(c.getString(c.getColumnIndex("ord_issync")));
                serializer.endTag(empstr, "ord_issync");

                serializer.startTag(empstr, "tax_id");
                serializer.text(c.getString(c.getColumnIndex("tax_id")));
                serializer.endTag(empstr, "tax_id");

                serializer.startTag(empstr, "ord_shipvia");
                serializer.text(c.getString(c.getColumnIndex("ord_shipvia")));
                serializer.endTag(empstr, "ord_shipvia");

                serializer.startTag(empstr, "ord_shipto");
                serializer.text(c.getString(c.getColumnIndex("ord_shipto")));
                serializer.endTag(empstr, "ord_shipto");

                serializer.startTag(empstr, "ord_terms");
                serializer.text(c.getString(c.getColumnIndex("ord_terms")));
                serializer.endTag(empstr, "ord_terms");

                serializer.startTag(empstr, "ord_custmsg");
                serializer.text(c.getString(c.getColumnIndex("ord_custmsg")));
                serializer.endTag(empstr, "ord_custmsg");

                serializer.startTag(empstr, "ord_class");
                serializer.text(c.getString(c.getColumnIndex("ord_class")));
                serializer.endTag(empstr, "ord_class");

                serializer.startTag(empstr, "ord_subtotal");
                serializer.text(c.getString(c.getColumnIndex("ord_subtotal")));
                serializer.endTag(empstr, "ord_subtotal");

                serializer.startTag(empstr, "ord_taxamount");
                serializer.text(c.getString(c.getColumnIndex("ord_taxamount")));
                serializer.endTag(empstr, "ord_taxamount");

                serializer.startTag(empstr, "ord_discount");
                serializer.text(c.getString(c.getColumnIndex("ord_discount")));
                serializer.endTag(empstr, "ord_discount");

                serializer.startTag(empstr, "ord_discount_id");
                serializer.text(c.getString(c.getColumnIndex("ord_discount_id")));
                serializer.endTag(empstr, "ord_discount_id");

                serializer.startTag(empstr, "ord_discount_value");
                serializer.text(c.getString(c.getColumnIndex("ord_discount")));
                serializer.endTag(empstr, "ord_discount_value");

                serializer.startTag(empstr, "ord_discount_type");
                serializer.text(c.getString(c.getColumnIndex("ord_discount_id")));
                serializer.endTag(empstr, "ord_discount_type");

                serializer.startTag(empstr, "ord_latitude");
                serializer.text(c.getString(c.getColumnIndex("ord_latitude")));
                serializer.endTag(empstr, "ord_latitude");

                serializer.startTag(empstr, "ord_longitude");
                serializer.text(c.getString(c.getColumnIndex("ord_longitude")));
                serializer.endTag(empstr, "ord_longitude");

                serializer.startTag(empstr, "tipAmount");
                serializer.text(c.getString(c.getColumnIndex("pay_tip")));
                serializer.endTag(empstr, "tipAmount");

                serializer.startTag(empstr, "ShippingRate");
                serializer.text(empstr);
                serializer.endTag(empstr, "ShippingRate");

                if (c.getString(c.getColumnIndex("cust_id")) != null) {
                    custInfo = custHandler.getXMLCustAddr(c.getString(c.getColumnIndex("cust_id")));
                    serializer.startTag(empstr, "cust_fname");
                    serializer.text(getCustAddr(custInfo, "cust_fname"));
                    serializer.endTag(empstr, "cust_fname");

                    serializer.startTag(empstr, "cust_lname");
                    serializer.text(getCustAddr(custInfo, "cust_lname"));
                    serializer.endTag(empstr, "cust_lname");

                    serializer.startTag(empstr, "Billing");

                    serializer.attribute(empstr, "type", "Business");
                    serializer.startTag(empstr, "addr_b_str1");
                    serializer.text(getCustAddr(custInfo, "addr_b_str1"));
                    serializer.endTag(empstr, "addr_b_str1");
                    serializer.startTag(empstr, "addr_b_str2");
                    serializer.text(getCustAddr(custInfo, "addr_b_str2"));
                    serializer.endTag(empstr, "addr_b_str2");
                    serializer.startTag(empstr, "addr_b_str3");
                    serializer.text(getCustAddr(custInfo, "addr_b_str3"));
                    serializer.endTag(empstr, "addr_b_str3");
                    serializer.startTag(empstr, "addr_b_city");
                    serializer.text(getCustAddr(custInfo, "addr_b_city"));
                    serializer.endTag(empstr, "addr_b_city");
                    serializer.startTag(empstr, "addr_b_state");
                    serializer.text(getCustAddr(custInfo, "addr_b_state"));
                    serializer.endTag(empstr, "addr_b_state");
                    serializer.startTag(empstr, "addr_b_country");
                    serializer.text(getCustAddr(custInfo, "addr_b_country"));
                    serializer.endTag(empstr, "addr_b_country");
                    serializer.startTag(empstr, "addr_b_zipcode");
                    serializer.text(getCustAddr(custInfo, "addr_b_zipcode"));
                    serializer.endTag(empstr, "addr_b_zipcode");

                    serializer.endTag(empstr, "Billing");

                    serializer.startTag(empstr, "Shipping");

                    serializer.attribute(empstr, "type", "Business");
                    serializer.startTag(empstr, "addr_s_str1");
                    serializer.text(getCustAddr(custInfo, "addr_s_str1"));
                    serializer.endTag(empstr, "addr_s_str1");
                    serializer.startTag(empstr, "addr_s_str2");
                    serializer.text(getCustAddr(custInfo, "addr_s_str2"));
                    serializer.endTag(empstr, "addr_s_str2");
                    serializer.startTag(empstr, "addr_s_str3");
                    serializer.text(getCustAddr(custInfo, "addr_s_str3"));
                    serializer.endTag(empstr, "addr_s_str3");
                    serializer.startTag(empstr, "addr_s_city");
                    serializer.text(getCustAddr(custInfo, "addr_s_city"));
                    serializer.endTag(empstr, "addr_s_city");
                    serializer.startTag(empstr, "addr_s_state");
                    serializer.text(getCustAddr(custInfo, "addr_s_state"));
                    serializer.endTag(empstr, "addr_s_state");
                    serializer.startTag(empstr, "addr_s_country");
                    serializer.text(getCustAddr(custInfo, "addr_s_country"));
                    serializer.endTag(empstr, "addr_s_country");
                    serializer.startTag(empstr, "addr_s_zipcode");
                    serializer.text(getCustAddr(custInfo, "addr_s_zipcode"));
                    serializer.endTag(empstr, "addr_s_zipcode");

                    serializer.endTag(empstr, "Shipping");
                }

                serializer.startTag(empstr, "OrderProduct");
                walletOrderProducts(serializer, c.getString(c.getColumnIndex("ord_id")));
                serializer.endTag(empstr, "OrderProduct");

                serializer.startTag(empstr, "Payments");
                serializer.startTag(empstr, "Payment");

                serializer.startTag(empstr, "paymentMethodName");
                serializer.text(c.getString(c.getColumnIndex("card_type")));
                serializer.endTag(empstr, "paymentMethodName");

                serializer.startTag(empstr, "amount");
                serializer.text(c.getString(c.getColumnIndex("pay_amount")));
                serializer.endTag(empstr, "amount");

                serializer.startTag(empstr, "merchantAccount");
                serializer.text(empstr);
                serializer.endTag(empstr, "merchantAccount");

                serializer.startTag(empstr, "RetrievalReferenceNumber");
                serializer.text(empstr);
                serializer.endTag(empstr, "RetrievalReferenceNumber");

                serializer.startTag(empstr, "BatchNumber");
                serializer.text(empstr);
                serializer.endTag(empstr, "BatchNumber");

                serializer.startTag(empstr, "TerminalNumber");
                serializer.text(empstr);
                serializer.endTag(empstr, "TerminalNumber");

                serializer.startTag(empstr, "pay_id");
                serializer.text(c.getString(c.getColumnIndex("pay_id")));
                serializer.endTag(empstr, "pay_id");

                serializer.startTag(empstr, "group_pay_id");
                serializer.text(c.getString(c.getColumnIndex("group_pay_id")));
                serializer.endTag(empstr, "group_pay_id");

                serializer.startTag(empstr, "trans_type");
                serializer.text(c.getString(c.getColumnIndex("pay_type")));
                serializer.endTag(empstr, "trans_type");

                serializer.startTag(empstr, "cust_id");
                serializer.text(StringUtil.nullStringToEmpty(c.getString(c.getColumnIndex("cust_id"))));
                serializer.endTag(empstr, "cust_id");

                serializer.startTag(empstr, "cust_id_key");
                serializer.text(StringUtil.nullStringToEmpty(c.getString(c.getColumnIndex("cust_id_ref"))));
                serializer.endTag(empstr, "cust_id_key");

                serializer.startTag(empstr, "pay_latitude");
                serializer.text(c.getString(c.getColumnIndex("pay_latitude")));
                serializer.endTag(empstr, "pay_latitude");

                serializer.startTag(empstr, "pay_longitude");
                serializer.text(c.getString(c.getColumnIndex("pay_longitude")));
                serializer.endTag(empstr, "pay_longitude");

                serializer.startTag(empstr, "emp_id");
                serializer.text(c.getString(c.getColumnIndex("emp_id")));
                serializer.endTag(empstr, "emp_id");

                serializer.startTag(empstr, "paymethod_id");
                serializer.text(c.getString(c.getColumnIndex("paymethod_id")));
                serializer.endTag(empstr, "paymethod_id");

                serializer.startTag(empstr, "pay_ccfournum");
                serializer.text(c.getString(c.getColumnIndex("ccnum_last4")));
                serializer.endTag(empstr, "pay_ccfournum");

                serializer.startTag(empstr, "pay_check");
                serializer.text(c.getString(c.getColumnIndex("pay_check")));
                serializer.endTag(empstr, "pay_check");

                serializer.startTag(empstr, "pay_receipt");
                serializer.text(c.getString(c.getColumnIndex("pay_receipt")));
                serializer.endTag(empstr, "pay_receipt");

                serializer.startTag(empstr, "pay_amount");
                serializer.text(c.getString(c.getColumnIndex("pay_amount")));
                serializer.endTag(empstr, "pay_amount");

                serializer.startTag(empstr, "tipAmount");
                serializer.text(c.getString(c.getColumnIndex("pay_tip")));
                serializer.endTag(empstr, "tipAmount");

                serializer.startTag(empstr, "pay_comment");
                serializer.text(c.getString(c.getColumnIndex("pay_comment")));
                serializer.endTag(empstr, "pay_comment");

                serializer.startTag(empstr, "pay_timecreated");
                serializer.text(c.getString(c.getColumnIndex("pay_timecreated")));
                serializer.endTag(empstr, "pay_timecreated");

                serializer.startTag(empstr, "pay_timesync");
                serializer.text(c.getString(c.getColumnIndex("pay_timesync")));
                serializer.endTag(empstr, "pay_timesync");

                serializer.startTag(empstr, "account_id");
                serializer.text(c.getString(c.getColumnIndex("account_id")));
                serializer.endTag(empstr, "account_id");

                serializer.startTag(empstr, "pay_issync");
                serializer.text(c.getString(c.getColumnIndex("pay_issync")));
                serializer.endTag(empstr, "pay_issync");

                serializer.startTag(empstr, "pay_name");
                serializer.text(c.getString(c.getColumnIndex("pay_name")));
                serializer.endTag(empstr, "pay_name");

                serializer.startTag(empstr, "processed");
                serializer.text(c.getString(c.getColumnIndex("processed")));
                serializer.endTag(empstr, "processed");

                serializer.startTag(empstr, "pay_poscode");
                serializer.text(c.getString(c.getColumnIndex("pay_poscode")));
                serializer.endTag(empstr, "pay_poscode");

                serializer.startTag(empstr, "pay_seccode");
                serializer.text(c.getString(c.getColumnIndex("pay_seccode")));
                serializer.endTag(empstr, "pay_seccode");

                serializer.startTag(empstr, "pay_resultcode");
                serializer.text(c.getString(c.getColumnIndex("pay_resultcode")));
                serializer.endTag(empstr, "pay_resultcode");

                serializer.startTag(empstr, "pay_resultmessage");
                serializer.text(c.getString(c.getColumnIndex("pay_resultmessage")));
                serializer.endTag(empstr, "pay_resultmessage");

                serializer.startTag(empstr, "pay_result");
                serializer.text(c.getString(c.getColumnIndex("pay_result")));
                serializer.endTag(empstr, "pay_result");

                serializer.startTag(empstr, "pay_date");
                serializer.text(c.getString(c.getColumnIndex("pay_date")));
                serializer.endTag(empstr, "pay_date");

                serializer.startTag(empstr, "recordnumber");
                serializer.text(c.getString(c.getColumnIndex("recordnumber")));
                serializer.endTag(empstr, "recordnumber");

                serializer.startTag(empstr, "authcode");
                serializer.text(c.getString(c.getColumnIndex("authcode")));
                serializer.endTag(empstr, "authcode");

                serializer.startTag(empstr, "pay_transid");
                serializer.text(c.getString(c.getColumnIndex("pay_transid")));
                serializer.endTag(empstr, "pay_transid");

                serializer.startTag(empstr, "status");
                serializer.text(c.getString(c.getColumnIndex("status")));
                serializer.endTag(empstr, "status");

                serializer.startTag(empstr, "job_id");
                serializer.text(c.getString(c.getColumnIndex("job_id")));
                serializer.endTag(empstr, "job_id");

                serializer.startTag(empstr, "inv_id");
                serializer.text(c.getString(c.getColumnIndex("inv_id")));
                serializer.endTag(empstr, "inv_id");

                serializer.startTag(empstr, "clerk_id");
                serializer.text(c.getString(c.getColumnIndex("clerk_id")));
                serializer.endTag(empstr, "clerk_id");

                serializer.startTag(empstr, "refNumber");
                serializer.text(c.getString(c.getColumnIndex("ref_num")));
                serializer.endTag(empstr, "refNumber");

                serializer.startTag(empstr, "pay_cardtype");
                serializer.text(c.getString(c.getColumnIndex("card_type")));
                serializer.endTag(empstr, "pay_cardtype");

                serializer.startTag(empstr, "cashReturned");
                serializer.text("0");
                serializer.endTag(empstr, "cashReturned");

                if (Global.isIvuLoto) {
                    serializer.startTag(empstr, "ivuLotto");

                    serializer.startTag(empstr, "ivuLottoDrawDate");
                    serializer.text(c.getString(c.getColumnIndex("IvuLottoDrawDate")));
                    serializer.endTag(empstr, "ivuLottoDrawDate");
                    serializer.startTag(empstr, "ivuLottoNumber");
                    serializer.text(c.getString(c.getColumnIndex("IvuLottoNumber")));
                    serializer.endTag(empstr, "ivuLottoNumber");
                    serializer.startTag(empstr, "Tax1");
                    serializer.text(c.getString(c.getColumnIndex("Tax1_amount")));
                    serializer.endTag(empstr, "Tax1");
                    serializer.startTag(empstr, "Tax2");
                    serializer.text(c.getString(c.getColumnIndex("Tax2_amount")));
                    serializer.endTag(empstr, "Tax2");

                    serializer.endTag(empstr, "ivuLotto");
                }

                // buildInvoicePayment(serializer,payID);

                serializer.endTag(empstr, "Payment");
                serializer.endTag(empstr, "Payments");

                serializer.endTag(empstr, "Order");

                c.moveToNext();

            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
        c.close();
        // myDB.close();
    }

    private void walletOrderProducts(XmlSerializer serializer, String ordID)
            throws IllegalArgumentException, IllegalStateException, IOException {
        OrderProductsHandler handler = new OrderProductsHandler(thisActivity);
        Cursor c = handler.getWalletOrdProd(ordID);
        c.moveToFirst();
        int size = c.getCount();
        boolean isRestMode = info.getPreferences(MyPreferences.pref_restaurant_mode);

        for (int i = 0; i < size; i++) {
            if (!isRestMode || (isRestMode && ((c.getString(c.getColumnIndex("addon")).equals("0"))))) {
                serializer.startTag(empstr, "OrderProduct");

                serializer.startTag(empstr, "isAddon");
                serializer.text(c.getString(c.getColumnIndex("addon")));
                serializer.endTag(empstr, "isAddon");

                serializer.startTag(empstr, "isAdded");
                serializer.text(c.getString(c.getColumnIndex("isAdded")));
                serializer.endTag(empstr, "isAdded");

                serializer.startTag(empstr, "isPrinted");

                serializer.text(c.getString(c.getColumnIndex("isPrinted")));

                serializer.endTag(empstr, "isPrinted");

                serializer.startTag(empstr, "item_void");
                serializer.text(c.getString(c.getColumnIndex("item_void")));
                serializer.endTag(empstr, "item_void");

                serializer.startTag(empstr, "ordprod_id");
                serializer.text(c.getString(c.getColumnIndex("ordprod_id")));
                serializer.endTag(empstr, "ordprod_id");

                serializer.startTag(empstr, "ord_id");
                serializer.text(c.getString(c.getColumnIndex("ord_id")));
                serializer.endTag(empstr, "ord_id");

                serializer.startTag(empstr, "prod_id");
                serializer.text(c.getString(c.getColumnIndex("prod_id")));
                serializer.endTag(empstr, "prod_id");

                serializer.startTag(empstr, "ordprod_qty");
                serializer.text(c.getString(c.getColumnIndex("ordprod_qty")));
                serializer.endTag(empstr, "ordprod_qty");

                serializer.startTag(empstr, "overwrite_price");
                serializer.text(c.getString(c.getColumnIndex("overwrite_price")));
                serializer.endTag(empstr, "overwrite_price");

                serializer.startTag(empstr, "ordprod_pointsUsedToPay");
                serializer.text("0");
                serializer.endTag(empstr, "ordprod_pointsUsedToPay");

                serializer.startTag(empstr, "reason_id");
                serializer.text(c.getString(c.getColumnIndex("reason_id")));
                serializer.endTag(empstr, "reason_id");

                serializer.startTag(empstr, "ordprod_name");
                serializer.text(c.getString(c.getColumnIndex("ordprod_name")));
                serializer.endTag(empstr, "ordprod_name");

                serializer.startTag(empstr, "ordprod_desc");
                serializer.text(c.getString(c.getColumnIndex("ordprod_desc")));
                serializer.endTag(empstr, "ordprod_desc");

                serializer.startTag(empstr, "pricelevel_id");
                serializer.text(c.getString(c.getColumnIndex("pricelevel_id")));
                serializer.endTag(empstr, "pricelevel_id");

                serializer.startTag(empstr, "prod_seq");
                serializer.text(c.getString(c.getColumnIndex("prod_seq")));
                serializer.endTag(empstr, "prod_seq");

                serializer.startTag(empstr, "uom_name");
                serializer.text(c.getString(c.getColumnIndex("uom_name")));
                serializer.endTag(empstr, "uom_name");

                // <uom_id>Packet</uom_id>
                serializer.startTag(empstr, "uom_id");
                serializer.text(c.getString(c.getColumnIndex("uom_id")));
                serializer.endTag(empstr, "uom_id");

                serializer.startTag(empstr, "uom_conversion");
                serializer.text(c.getString(c.getColumnIndex("uom_conversion")));
                serializer.endTag(empstr, "uom_conversion");

                serializer.startTag(empstr, "prod_taxId");
                serializer.text(c.getString(c.getColumnIndex("prod_taxId")));
                serializer.endTag(empstr, "prod_taxId");
                String prod_taxValue = Global.getRoundBigDecimal(new BigDecimal(c.getDouble(c.getColumnIndex("prod_taxValue"))));

                serializer.startTag(empstr, "prod_taxValue");
                serializer.text(prod_taxValue);
                serializer.endTag(empstr, "prod_taxValue");

                serializer.startTag(empstr, "prod_discountId");
                serializer.text(c.getString(c.getColumnIndex("discount_id")));
                serializer.endTag(empstr, "prod_discountId");

                serializer.startTag(empstr, "prod_discountValue");
                serializer.text(c.getString(c.getColumnIndex("discount_value")));
                serializer.endTag(empstr, "prod_discountValue");

                serializer.startTag(empstr, "ProductImageURL");
                serializer.text(StringUtil.nullStringToEmpty(c.getString(c.getColumnIndex("prod_img_name"))));
                serializer.endTag(empstr, "ProductImageURL");

                serializer.startTag(empstr, "product_total");
                serializer.text(c.getString(c.getColumnIndex("itemTotal")));
                serializer.endTag(empstr, "product_total");

                serializer.endTag(empstr, "OrderProduct");
            }
            c.moveToNext();
        }
        c.close();
    }


}
