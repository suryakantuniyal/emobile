package com.android.emobilepos.models;

import com.android.emobilepos.adapters.OrderProductListAdapter;
import com.android.emobilepos.models.orders.OrderProduct;

/**
 * Created by Guarionex on 2/18/2016.
 */
public class OrderSeatProduct {
    public boolean isDeleted;
    public OrderProductListAdapter.RowType rowType;
    public String seatNumber;
    public OrderProduct orderProduct;
    private int seatGroupId;

    public OrderSeatProduct(String seatNumber, int groupId) {
        this.seatNumber = seatNumber;
        this.seatGroupId = groupId;
        this.rowType = OrderProductListAdapter.RowType.TYPE_HEADER;
    }

    public OrderSeatProduct(OrderProduct orderProduct) {
        this.orderProduct = orderProduct;
        this.orderProduct.setSeatGroupId(this.seatGroupId);
        this.rowType = OrderProductListAdapter.RowType.TYPE_ITEM;
    }

    public int getSeatGroupId() {
        return seatGroupId;
    }

    public void setSeatGroupId(int seatGroupId) {
        this.seatGroupId = seatGroupId;
        if (orderProduct != null) {
            orderProduct.setSeatGroupId(seatGroupId);
        }
    }
}
