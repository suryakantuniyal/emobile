package com.android.emobilepos.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.android.emobilepos.adapters.OrderProductListAdapter;

/**
 * Created by Guarionex on 2/18/2016.
 */
public class OrderSeatProduct  {
    public boolean isDeleted;
    public OrderProductListAdapter.RowType rowType;
    public String seatNumber;
    public int seatGroupId;
    public OrderProduct orderProduct;

    public OrderSeatProduct(String seatNumber, int groupId) {
        this.seatNumber = seatNumber;
        this.seatGroupId = groupId;
        this.rowType = OrderProductListAdapter.RowType.TYPE_HEADER;
    }

    public OrderSeatProduct(OrderProduct orderProduct) {
        this.orderProduct = orderProduct;
        this.rowType = OrderProductListAdapter.RowType.TYPE_ITEM;
    }

}
