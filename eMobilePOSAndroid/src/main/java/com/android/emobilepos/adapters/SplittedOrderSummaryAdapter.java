package com.android.emobilepos.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.emobilepos.R;
import com.android.emobilepos.models.DinningTable;
import com.android.emobilepos.models.SplitedOrder;

import java.util.List;

public class SplittedOrderSummaryAdapter extends BaseAdapter implements Filterable {
    private LayoutInflater mInflater;
    private List<SplitedOrder> splitedOrders;


    public SplittedOrderSummaryAdapter(Activity activity, List<SplitedOrder> splitedOrderss) {
        mInflater = LayoutInflater.from(activity);
        this.splitedOrders = splitedOrders;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.main_menu_listviewadapter, null);
            holder = new ViewHolder();
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.splitedOrder = splitedOrders.get(position);
        return convertView;
    }


    public class ViewHolder {
        public SplitedOrder splitedOrder;
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