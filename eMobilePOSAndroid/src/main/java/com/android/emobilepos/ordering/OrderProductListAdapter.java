package com.android.emobilepos.ordering;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.android.emobilepos.models.OrderProduct;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Guarionex on 1/20/2016.
 */
public class OrderProductListAdapter extends BaseAdapter {
    public enum RowType {
        TYPE_ITEM(0), TYPE_HEADER(1);
        int code;

        RowType(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }
    private LayoutInflater mInflater;
    List<OrderProduct> orderProducts;
    int seatsAmount;
    List<OrderSeatProduct> list;

    public OrderProductListAdapter(Context context, List<OrderProduct> orderProducts, int seatsAmount) {
        mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.orderProducts = orderProducts;
        this.seatsAmount = seatsAmount;
        list = new ArrayList<OrderSeatProduct>();
        for (OrderProduct product : orderProducts) {
            list.add(new OrderSeatProduct(product));
        }
        for (int i = 0; i < seatsAmount; i++) {
            list.add(new OrderSeatProduct(i));
        }
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public int getItemViewType(int position) {
        return list.get(position).rowType.code;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }

    public class OrderSeatProduct {
        RowType rowType;
        int seatNumber;
        OrderProduct orderProduct;

        public OrderSeatProduct(int seatNumber) {
            this.seatNumber = seatNumber;
            this.rowType = RowType.TYPE_HEADER;
        }

        public OrderSeatProduct(OrderProduct orderProduct) {
            this.orderProduct = orderProduct;
            this.rowType = RowType.TYPE_ITEM;
        }
    }
}


