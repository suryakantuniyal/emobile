package com.android.emobilepos.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.database.ProductAddonsHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.models.OrderProduct;
import com.android.emobilepos.models.OrderSeatProduct;
import com.android.emobilepos.ordering.OrderingMain_FA;
import com.android.emobilepos.ordering.PickerAddon_FA;
import com.android.support.Global;
import com.android.support.MyPreferences;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Guarionex on 1/20/2016.
 */
public class OrderProductListAdapter extends BaseAdapter {


    private OrderingMain_FA orderingMainFa;

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
    public List<OrderSeatProduct> orderSeatProductList;
    public List<OrderSeatProduct> orderSeatProductFullList;
    private MyPreferences myPref;
    public int selectedPosition;
    Activity activity;

    public OrderProductListAdapter(Activity activity, List<OrderProduct> orderProducts,
                                   OrderingMain_FA orderingMainFa) {
        this.orderingMainFa = orderingMainFa;
        mInflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        myPref = new MyPreferences(activity);
        this.activity = activity;
        this.orderProducts = orderProducts;
        initSeats(orderingMainFa.getSelectedSeatsAmount());
        if (orderSeatProductList != null && !orderSeatProductList.isEmpty()) {
            orderingMainFa.setSelectedSeatNumber(orderSeatProductList.get(0).seatNumber);
        }
    }


    public int getSeatsAmount() {
        int count = 0;
        for (OrderSeatProduct seatProduct : orderSeatProductList) {
            if (seatProduct.rowType == RowType.TYPE_HEADER) {
                count++;
            }
        }
        return count;
    }

    public int getNextGroupId() {
        int next = 0;
        for (OrderSeatProduct seatProduct : orderSeatProductFullList) {
            if (seatProduct.rowType == RowType.TYPE_HEADER) {
                next = seatProduct.getSeatGroupId() + 1;
            }
        }
        return next;
    }

    public String getFirstSeat() {
        for (OrderSeatProduct seatProduct : orderSeatProductList) {
            if (seatProduct.rowType == RowType.TYPE_HEADER) {
                return seatProduct.seatNumber;
            }
        }
        return null;
    }

    private void initSeats(int seatsAmount) {
        orderSeatProductFullList = new ArrayList<OrderSeatProduct>();
        orderSeatProductList = new ArrayList<OrderSeatProduct>();
        if (seatsAmount > 0) {
            for (int i = 0; i < seatsAmount; i++) {
                OrderSeatProduct seatProduct = new OrderSeatProduct(String.valueOf(i + 1), getNextGroupId());
                seatProduct.setSeatGroupId(i + 1);
                orderSeatProductList.add(seatProduct);
            }
        }
        orderSeatProductFullList.addAll(orderSeatProductList);
    }

    public List<OrderProduct> getOrderProducts(String seatNumber) {
        List<OrderProduct> l = new ArrayList<OrderProduct>();
        for (OrderProduct product : orderProducts) {
            if (product.assignedSeat.equalsIgnoreCase(seatNumber)) {
                l.add(product);
            }
        }
        return l;
    }

    public OrderSeatProduct getSeat(String seatNumber) {
        for (OrderSeatProduct seatProduct : orderSeatProductList) {
            if (seatProduct.rowType == RowType.TYPE_HEADER) {
                if (seatProduct.seatNumber.equalsIgnoreCase(seatNumber)) {
                    return seatProduct;
                }
            }
        }
        return null;
    }

    public void addSeat() {
        int seatNumber = 0;
        if (!orderSeatProductFullList.isEmpty()) {
            seatNumber = Integer.parseInt(orderSeatProductFullList.get(orderSeatProductFullList.size() - 1).seatNumber);
        }
        OrderSeatProduct product = new OrderSeatProduct(String.valueOf(seatNumber + 1), getNextGroupId());
        product.setSeatGroupId(getNextGroupId());
        orderSeatProductList.add(product);
        orderSeatProductFullList.add(product);
        notifyDataSetChanged();
    }

    public void addSeat(OrderProduct orderProduct) {
        if (getSeat(orderProduct.assignedSeat) == null) {
            OrderSeatProduct product = new OrderSeatProduct(orderProduct.assignedSeat, orderProduct.seatGroupId);
            product.setSeatGroupId(orderProduct.seatGroupId == 0 ? getNextGroupId() : orderProduct.seatGroupId);
            orderSeatProductList.add(product);
            orderSeatProductFullList.add(product);
        }
        notifyDataSetChanged();
    }

    public void removeSeat(String seatNumber) {
        for (OrderSeatProduct seatProduct : orderSeatProductFullList) {
            if (seatProduct.rowType == RowType.TYPE_HEADER && seatProduct.seatNumber.equalsIgnoreCase(seatNumber)) {
                seatProduct.isDeleted = true;
            }
        }
        notifyDataSetChanged();
    }

    public void moveSeatItems(List<OrderProduct> orderProducts, String targetSeat) {
        for (OrderProduct product : orderProducts) {
            product.assignedSeat = targetSeat;
        }
        notifyDataSetChanged();
    }

    public void joinSeatsGroupId(int sourceGroupId, int targetGroupId) {
        for (OrderSeatProduct seatProduct : orderSeatProductList) {
//            if (seatProduct.rowType == RowType.TYPE_HEADER) {
            if (seatProduct.getSeatGroupId() == sourceGroupId) {
                seatProduct.setSeatGroupId(targetGroupId);
            }
//            }
        }
        notifyDataSetChanged();
    }

    private List<OrderSeatProduct> getValidOrderSeatProductList() {
        ArrayList<OrderSeatProduct> l = new ArrayList<OrderSeatProduct>();
        if (orderSeatProductFullList.size() > 0) {
            for (OrderSeatProduct seatProduct : orderSeatProductFullList) {
                if (seatProduct.rowType == RowType.TYPE_HEADER && !seatProduct.isDeleted) {
                    OrderSeatProduct osp = new OrderSeatProduct(seatProduct.seatNumber, getNextGroupId());
                    osp.setSeatGroupId(seatProduct.getSeatGroupId());
                    l.add(osp);
                    for (OrderProduct product : orderProducts) {
                        if (product != null && product.assignedSeat != null &&
                                product.assignedSeat.equalsIgnoreCase(seatProduct.seatNumber)) {
                            OrderSeatProduct sp = new OrderSeatProduct(product);
                            sp.setSeatGroupId(osp.getSeatGroupId());
                            l.add(sp);
                        }
                    }
                    orderSeatProductFullList.set(orderSeatProductFullList.indexOf(seatProduct), osp);
                }
            }
        } else {
            for (OrderProduct product : orderProducts) {
                if (product != null) {
                    l.add(new OrderSeatProduct(product));
                }
            }
        }
        return l;
    }

    private void refreshList() {
        orderSeatProductList = getValidOrderSeatProductList();
    }

    @Override
    public void notifyDataSetChanged() {
        refreshList();

        super.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return orderSeatProductList.size();
    }

    @Override
    public Object getItem(int position) {
        return orderSeatProductList.get(position);
    }

    @Override
    public int getItemViewType(int position) {
        return orderSeatProductList.get(position).rowType.code;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        RowType type = orderSeatProductList.get(position).rowType;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.product_receipt_adapter, null);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        switch (type) {
            case TYPE_HEADER:
                ImageButton menuButton = (ImageButton) convertView.findViewById(R.id.headerMenubutton);
                menuButton.setOnClickListener((OrderingMain_FA) activity);
                menuButton.setTag(orderSeatProductList.get(position));
                convertView.findViewById(R.id.seatHeaderSection).setVisibility(View.VISIBLE);
                convertView.findViewById(R.id.itemSection).setVisibility(View.GONE);
                ((TextView) convertView.findViewById(R.id.seatNumbertextView)).setText(String.format("Seat %s", orderSeatProductList.get(position).seatNumber));
                int colorId = activity.getResources().getIdentifier("seat" + orderSeatProductList.get(position).getSeatGroupId(), "color", activity.getPackageName());
                convertView.findViewById(R.id.seatHeaderSection).setBackgroundResource(colorId);
                if (orderingMainFa.getSelectedSeatNumber().equalsIgnoreCase(orderSeatProductList.get(position).seatNumber)) {
                    selectedPosition = position;
                    convertView.setActivated(true);
                    convertView.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.blue_row_selector));
                } else {
                    convertView.setActivated(false);
                    convertView.setBackgroundDrawable(null);
                    convertView.setBackgroundResource(colorId);
                }
                convertView.setVisibility(orderSeatProductList.get(position).isDeleted ? View.GONE : View.VISIBLE);
                break;
            case TYPE_ITEM:
                convertView = mInflater.inflate(R.layout.product_receipt_adapter, null);
                convertView.findViewById(R.id.seatHeaderSection).setVisibility(View.GONE);
                convertView.findViewById(R.id.itemSection).setVisibility(View.VISIBLE);
                holder.itemQty = (TextView) convertView.findViewById(R.id.itemQty);
                holder.itemName = (TextView) convertView.findViewById(R.id.itemName);
                holder.itemAmount = (TextView) convertView.findViewById(R.id.itemAmount);
                holder.distQty = (TextView) convertView.findViewById(R.id.distQty);
                holder.distAmount = (TextView) convertView.findViewById(R.id.distAmount);
                holder.granTotal = (TextView) convertView.findViewById(R.id.granTotal);

                holder.addonButton = (Button) convertView.findViewById(R.id.addonButton);
                if (holder.addonButton != null)
                    holder.addonButton.setFocusable(false);
                if (orderSeatProductList.get(position).rowType == RowType.TYPE_ITEM) {
                    setHolderValues(holder, position);
                }
                break;
        }
        convertView.setTag(holder);

        return convertView;
    }


    public void setHolderValues(ViewHolder holder, final int pos) {
        Global global = (Global) activity.getApplication();
        final OrderProduct product = orderSeatProductList.get(pos).orderProduct;
        final int orderProductIdx = orderSeatProductList.get(pos).rowType == OrderProductListAdapter.RowType.TYPE_ITEM ? global.orderProducts.indexOf(orderSeatProductList.get(pos).orderProduct) : 0;
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
                        intent.putExtra("item_position", orderProductIdx);


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


