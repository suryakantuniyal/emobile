package com.android.emobilepos.models;

import android.app.Activity;
import android.content.Context;

import com.android.dao.AssignEmployeeDAO;
import com.android.emobilepos.models.realms.AssignEmployee;
import com.android.emobilepos.models.realms.OrderAttributes;
import com.android.support.Customer;
import com.android.support.DateUtils;
import com.android.support.MyPreferences;
import com.google.gson.Gson;

import java.util.Date;
import java.util.List;

import util.json.JsonUtils;

public class Order implements Cloneable {
    public String ord_id = "";
    public String qbord_id = "";
    public String emp_id = "";
    public String cust_id = "";
    public String clerk_id = "";
    public String c_email = "";
    public String ord_signature = "";
    public String ord_po = "";
    public String total_lines = "";
    public String total_lines_pay = "0";
    public String ord_total = "";
    public String ord_comment = "";
    public String ord_delivery = "";
    public String ord_timecreated = "";
    public String ord_timesync = "";
    public String qb_synctime = "";
    public String emailed = "";
    public String processed = "";
    public String ord_type = "";
    public String ord_type_name = "";
    public String ord_claimnumber = "";
    public String ord_rganumber = "";
    public String ord_returns_pu = "";
    public String ord_inventory = "";
    public String ord_issync = "";
    public String tax_id = "";
    public String ord_shipvia = "";
    public String ord_shipto = "";
    public String ord_terms = "";
    public String ord_custmsg = "";
    public String ord_class = "";
    public String ord_subtotal = "";
    public String ord_lineItemDiscount = "";
    public String ord_globalDiscount = "";
    public String ord_taxamount = "";
    public String ord_discount = "";
    public String ord_discount_id = "";
    public String ord_latitude = "";
    public String ord_longitude = "";
    public String tipAmount = "";
    public String custidkey = "";
    public String isOnHold = "0"; // 0 - not on hold, 1 - on hold
    public String ord_HoldName = "";
    public String is_stored_fwd = "0";
    public String VAT = "0";
    public String isVoid = "";
    public String gran_total = "";
    public String cust_name = "";
    public String sync_id = "";
    public Customer customer;

    //private Global global;

    public String assignedTable;
    public String associateID;
    public int numberOfSeats;
    public String ord_timeStarted;
    public List<OrderAttributes> orderAttributes;
    private List<DataTaxes> listOrderTaxes;
    public Order() {
        ord_issync = "0";
        isVoid = "0";
        processed = "0"; //need to be 1 when order has been processed or 9 if voided
        ord_timecreated = DateUtils.getDateAsString(new Date(), DateUtils.DATE_yyyy_MM_ddTHH_mm_ss);
    }

    public Order(Context activity) {
        MyPreferences myPref = new MyPreferences(activity);
        ord_issync = "0";
        isVoid = "0";
        processed = "0"; //need to be 1 when order has been processed or 9 if voided
        ord_timecreated = DateUtils.getDateAsString(new Date(), DateUtils.DATE_yyyy_MM_ddTHH_mm_ss);
        AssignEmployee assignEmployee = AssignEmployeeDAO.getAssignEmployee();
        emp_id = String.valueOf(assignEmployee != null ? assignEmployee.getEmpId() : "");
        custidkey = myPref.getCustIDKey();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }


    public String toJson() {
        Gson gson = JsonUtils.getInstance();
        String json = gson.toJson(this, Order.class);
        return json;
    }

    public List<DataTaxes> getListOrderTaxes() {
        return listOrderTaxes;
    }

    public void setListOrderTaxes(List<DataTaxes> listOrderTaxes) {
        this.listOrderTaxes = listOrderTaxes;
    }
}
