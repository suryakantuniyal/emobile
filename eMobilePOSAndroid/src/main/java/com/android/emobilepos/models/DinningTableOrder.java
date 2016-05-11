package com.android.emobilepos.models;

import android.app.Activity;

import com.android.database.OrdersHandler;
import com.android.support.DateUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Guarionex on 5/10/2016.
 */
public class DinningTableOrder extends RealmObject {

    private DinningTable dinningTable;
    private Date orderStartDate;
    @PrimaryKey
    private String currentOrderId;
    private int numberOfGuest;

    public DinningTable getDinningTable() {
        return dinningTable;
    }

    public void setDinningTable(DinningTable dinningTable) {
        this.dinningTable = dinningTable;
    }

    public Date getOrderStartDate() {
        return orderStartDate;
    }

    public void setOrderStartDate(Date orderStartDate) {
        this.orderStartDate = orderStartDate;
    }

    public int getNumberOfGuest() {
        return numberOfGuest;
    }

    public void setNumberOfGuest(int numberOfGuest) {
        this.numberOfGuest = numberOfGuest;
    }

    public String getCurrentOrderId() {
        return currentOrderId;
    }

    public void setCurrentOrderId(String currentOrderId) {
        this.currentOrderId = currentOrderId;
    }

    public String getElapsedTime() {
        Map<TimeUnit, Long> map = DateUtils.computeDiff(getOrderStartDate(), new Date());
        long now = System.currentTimeMillis();
        String time = String.format("%02d:%02d", map.get(TimeUnit.HOURS), map.get(TimeUnit.MINUTES));
        return time;
    }

    public Order getOrder(Activity activity) {
        OrdersHandler ordersHandler = new OrdersHandler(activity);
        return ordersHandler.getOrder(getCurrentOrderId());
    }

}
