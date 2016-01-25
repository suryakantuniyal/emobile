package com.android.emobilepos.ordering;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.android.database.ProductAddonsHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.models.OrderProduct;
import com.android.support.Global;
import com.android.support.MyPreferences;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Guarionex on 1/20/2016.
 */
public class OrderProductListAdapter extends BaseAdapter {
    public enum RowType {
        TYPE_HEADER(0), TYPE_ITEM(1);
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
    private MyPreferences myPref;
    Activity activity;

    public OrderProductListAdapter(Activity activity, List<OrderProduct> orderProducts, int seatsAmount) {
        mInflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        myPref = new MyPreferences(activity);
        this.activity = activity;
        this.orderProducts = orderProducts;
        this.seatsAmount = seatsAmount;
        refreshList();
    }

    public List<OrderSeatProduct> getOrderSeatProductList() {
        ArrayList<OrderSeatProduct> l = new ArrayList<OrderSeatProduct>();
        for (int i = 0; i < seatsAmount; i++) {
            l.add(new OrderSeatProduct(i));
            for (OrderProduct product : orderProducts) {
                if (product != null & product.assignedSeat != null &&
                        product.assignedSeat.equalsIgnoreCase(String.valueOf(i+1))) {
                    l.add(new OrderSeatProduct(product));
                }
            }
        }
        return l;
    }

    private void refreshList() {
        list = getOrderSeatProductList();
    }

    @Override
    public void notifyDataSetChanged() {
        refreshList();
        super.notifyDataSetChanged();
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
        ViewHolder holder;
        RowType type = list.get(position).rowType;
        if (convertView == null || (type == RowType.TYPE_ITEM && convertView.getTag() == null)) {
            holder = new ViewHolder();
            switch (type) {
                case TYPE_HEADER:
                    convertView = mInflater.inflate(R.layout.seat_receipt_adapter, null);
                    break;
                case TYPE_ITEM:
                    convertView = mInflater.inflate(R.layout.product_receipt_adapter, null);
                    holder.itemQty = (TextView) convertView.findViewById(R.id.itemQty);
                    holder.itemName = (TextView) convertView.findViewById(R.id.itemName);
                    holder.itemAmount = (TextView) convertView.findViewById(R.id.itemAmount);
                    holder.distQty = (TextView) convertView.findViewById(R.id.distQty);
                    holder.distAmount = (TextView) convertView.findViewById(R.id.distAmount);
                    holder.granTotal = (TextView) convertView.findViewById(R.id.granTotal);

                    holder.addonButton = (Button) convertView.findViewById(R.id.addonButton);
                    if (holder.addonButton != null)
                        holder.addonButton.setFocusable(false);
                    if (list.get(position).rowType == RowType.TYPE_ITEM) {
                        setHolderValues(holder, position);
                    }
                    convertView.setTag(holder);
                    break;
            }
        } else {
            holder = (ViewHolder) convertView.getTag();
            if (list.get(position).rowType == RowType.TYPE_ITEM) {
                if (holder == null) {
                    holder.itemQty = (TextView) convertView.findViewById(R.id.itemQty);
                    holder.itemName = (TextView) convertView.findViewById(R.id.itemName);
                    holder.itemAmount = (TextView) convertView.findViewById(R.id.itemAmount);
                    holder.distQty = (TextView) convertView.findViewById(R.id.distQty);
                    holder.distAmount = (TextView) convertView.findViewById(R.id.distAmount);
                    holder.granTotal = (TextView) convertView.findViewById(R.id.granTotal);

                    holder.addonButton = (Button) convertView.findViewById(R.id.addonButton);
                    if (holder.addonButton != null)
                        holder.addonButton.setFocusable(false);
                    if (list.get(position).rowType == RowType.TYPE_ITEM) {
                        setHolderValues(holder, position);
                    }
                    convertView.setTag(holder);
                }
                setHolderValues(holder, position);
            }
        }

        return convertView;
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


    public void setHolderValues(ViewHolder holder, final int pos) {
        final OrderProduct product = list.get(pos).orderProduct;
        final String tempId = product.ordprod_id;

        if (!myPref.getPreferences(MyPreferences.pref_restaurant_mode) || (myPref.getPreferences(MyPreferences.pref_restaurant_mode) && (Global.addonSelectionMap == null || (Global.addonSelectionMap != null && !Global.addonSelectionMap.containsKey(tempId))))) {
            if (holder.addonButton != null)
                holder.addonButton.setVisibility(View.INVISIBLE);
        } else {
            if (holder.addonButton != null) {
                holder.addonButton.setVisibility(View.VISIBLE);
                holder.addonButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(activity, PickerAddon_FA.class);
                        String prodID = product.prod_id;
//                        global.addonSelectionType = Global.addonSelectionMap.get(tempId);

                        intent.putExtra("addon_map_key", tempId);
                        intent.putExtra("isEditAddon", true);
                        intent.putExtra("prod_id", prodID);
                        intent.putExtra("item_position", pos);


                        ProductAddonsHandler prodAddonsHandler = new ProductAddonsHandler(activity);
                        Global.productParentAddons = prodAddonsHandler.getParentAddons(prodID);

                        activity.startActivityForResult(intent, 0);
                    }
                });
            }
        }

        holder.itemQty.setText(product.ordprod_qty);
        holder.itemName.setText(product.ordprod_name);

        String temp = Global.formatNumToLocale(Double.parseDouble(product.overwrite_price));
        holder.itemAmount.setText(Global.getCurrencyFormat(temp));


        holder.distQty.setText(product.disAmount);
        temp = Global.formatNumToLocale(Double.parseDouble(product.disTotal));
        holder.distAmount.setText(Global.getCurrencyFormat(temp));

        temp = Global.formatNumToLocale(Double.parseDouble(product.itemTotal));
        holder.granTotal.setText(Global.getCurrencyFormat(temp));

    }

    public class ViewHolder {
        TextView itemQty;
        TextView itemName;
        TextView itemAmount;
        TextView distQty;
        TextView distAmount;
        TextView granTotal;
        Button addonButton;
    }
}


