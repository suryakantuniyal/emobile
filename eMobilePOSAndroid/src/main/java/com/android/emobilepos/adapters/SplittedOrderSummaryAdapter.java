package com.android.emobilepos.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.android.emobilepos.R;
import com.android.emobilepos.models.OrderProduct;
import com.android.emobilepos.models.SplitedOrder;
import com.android.support.Global;
import com.android.support.NumberUtils;
import com.google.zxing.common.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import util.StringUtil;

public class SplittedOrderSummaryAdapter extends BaseAdapter implements Filterable {
    private LayoutInflater mInflater;
    private List<SplitedOrder> splitedOrders;
    private int selectedIndex = 0;

    public SplittedOrderSummaryAdapter(Activity activity, List<SplitedOrder> splitedOrders) {
        mInflater = LayoutInflater.from(activity);
        this.splitedOrders = splitedOrders;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.splitted_order_summary_listitem, null);
            holder = new ViewHolder();
            holder.tableNumber = (TextView) convertView.findViewById(R.id.splited_order_tablenumber_itemtextView);
            holder.seatNumber = (TextView) convertView.findViewById(R.id.splited_order_seatnumber_itemtextView);
            holder.ticketPrice = (TextView) convertView.findViewById(R.id.splited_order_price_itemtextView);
            holder.itemsList = (TextView) convertView.findViewById(R.id.splited_order_productsTextView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        SplitedOrder order = splitedOrders.get(position);
        holder.tableNumber.setText(order.getTableNumber());
        HashSet<String> seats = new HashSet<String>();
        HashSet<String> items = new HashSet<String>();

        for (OrderProduct product : order.getOrderProducts()) {
            seats.add(product.assignedSeat);
            items.add(product.ordprod_name);
        }

        holder.seatNumber.setText(org.springframework.util.StringUtils.arrayToDelimitedString(seats.toArray(), ", "));
        holder.ticketPrice.setText(Global.getCurrencyFormat(order.ord_total));
        holder.itemsList.setText(org.springframework.util.StringUtils.arrayToDelimitedString(items.toArray(), ", "));
        if (position == getSelectedIndex()) {
            convertView.setSelected(true);
        }
        return convertView;

    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(int selectedIndex) {
        this.selectedIndex = selectedIndex;
    }


    public class ViewHolder {
        TextView tableNumber;
        TextView seatNumber;
        TextView itemsCount;
        TextView ticketPrice;
        TextView itemsList;
    }


    @Override
    public Filter getFilter() {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getCount() {
        return splitedOrders.size();
    }

    @Override
    public Object getItem(int position) {
        return splitedOrders.get(position);
    }

}