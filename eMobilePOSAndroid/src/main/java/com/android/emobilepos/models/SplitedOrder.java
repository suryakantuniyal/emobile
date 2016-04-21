package com.android.emobilepos.models;

import android.app.Activity;
import android.os.Parcel;
import android.os.Parcelable;

import com.android.support.Customer;

import java.util.List;

/**
 * Created by guari_000 on 2/4/2016.
 */
public class SplitedOrder extends Order implements Cloneable, Comparable<SplitedOrder> {
    private List<OrderProduct> orderProducts;
    private String tableNumber;
    public long splittedOrderId = System.currentTimeMillis();


    public SplitedOrder(Activity activity, Order order) {
        super(activity);
        init(order);
        splittedOrderId = System.currentTimeMillis();
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void syncOrderProductIds() {
        for (OrderProduct product : orderProducts) {
            product.ord_id = ord_id;
        }
    }

    private void init(Order order) {
        assignedTable = order.assignedTable;
        associateID = order.associateID;
        numberOfSeats = order.numberOfSeats;
        ord_id = order.ord_id;
        qbord_id = order.qbord_id;
        emp_id = order.emp_id;
        cust_id = order.cust_id;
        clerk_id = order.clerk_id;
        c_email = order.c_email;
        ord_signature = order.ord_signature;
        ord_po = order.ord_po;
        total_lines = order.total_lines;
        total_lines_pay = order.total_lines_pay;
        ord_total = order.ord_total;
        ord_comment = order.ord_comment;
        ord_delivery = order.ord_delivery;
        ord_timecreated = order.ord_timecreated;
        ord_timesync = order.ord_timesync;
        qb_synctime = order.qb_synctime;
        emailed = order.emailed;
        processed = order.processed;
        ord_type = order.ord_type;
        ord_type_name = order.ord_type_name;
        ord_claimnumber = order.ord_claimnumber;
        ord_rganumber = order.ord_rganumber;
        ord_returns_pu = order.ord_returns_pu;
        ord_inventory = order.ord_inventory;
        ord_issync = order.ord_issync;
        tax_id = order.tax_id;
        ord_shipvia = order.ord_shipvia;
        ord_shipto = order.ord_shipto;
        ord_terms = order.ord_terms;
        ord_custmsg = order.ord_custmsg;
        ord_class = order.ord_class;
        ord_subtotal = order.ord_subtotal;
        ord_lineItemDiscount = order.ord_lineItemDiscount;
        ord_globalDiscount = order.ord_globalDiscount;
        ord_taxamount = order.ord_taxamount;
        ord_discount = order.ord_discount;
        ord_discount_id = order.ord_discount_id;
        ord_latitude = order.ord_latitude;
        ord_longitude = order.ord_longitude;
        tipAmount = order.tipAmount;
        custidkey = order.custidkey;
        isOnHold = order.isOnHold; // 0 - not on hold, 1 - on hold
        ord_HoldName = order.ord_HoldName;
        is_stored_fwd = order.is_stored_fwd;
        VAT = order.VAT;
        isVoid = order.isVoid;
        gran_total = order.gran_total;
        cust_name = order.cust_name;
        sync_id = order.sync_id;
        customer = order.customer;
    }

    public List<OrderProduct> getOrderProducts() {
        return orderProducts;
    }

    public void setOrderProducts(List<OrderProduct> orderProducts) {
        this.orderProducts = orderProducts;
    }

    public String getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(String tableNumber) {
        this.tableNumber = tableNumber;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof SplitedOrder && this.splittedOrderId == (((SplitedOrder) o).splittedOrderId);
    }

    @Override
    public int compareTo(SplitedOrder another) {
        return this.ord_id.compareTo(another.ord_id);
    }
}
