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

import com.android.dao.DinningTableDAO;
import com.android.dao.DinningTableOrderDAO;
import com.android.emobilepos.R;
import com.android.emobilepos.models.orders.Order;
import com.android.emobilepos.models.realms.Clerk;
import com.android.emobilepos.models.realms.DinningTable;
import com.android.emobilepos.models.realms.DinningTableOrder;
import com.android.support.Global;

import java.util.HashMap;
import java.util.List;

import io.realm.RealmList;

public class DinningTablesAdapter extends BaseAdapter implements Filterable {
    private final HashMap<String, List<Clerk>> tableAssignedClerks;
    private LayoutInflater mInflater;
    private List<DinningTable> dinningTables;
    private Activity activity;
    private RealmList<DinningTable> selectedDinningTables;

    public DinningTablesAdapter(Activity activity, List<DinningTable> dinningTables) {
        this.activity = activity;
        mInflater = LayoutInflater.from(activity);
        this.dinningTables = dinningTables;
        tableAssignedClerks = DinningTableDAO.getTableAssignedClerks();
    }

    public void setSelectedDinningTables(RealmList<DinningTable> selectedDinningTables) {
        this.selectedDinningTables = selectedDinningTables;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.dinning_table_map_item, null);
            holder = new ViewHolder();
            holder.time = (TextView) convertView.findViewById(R.id.timetextView21);
            holder.guests = (TextView) convertView.findViewById(R.id.gueststextView16);
            holder.amount = (TextView) convertView.findViewById(R.id.amounttextView23);
            holder.image = (ImageView) convertView.findViewById(R.id.dinningtableimageView3);
            holder.tableNumber = (TextView) convertView.findViewById(R.id.tableNumbertextView);
            holder.isSelectedCheckBox = (ImageView) convertView.findViewById(R.id.selectedCheckboximageView);
            holder.clerkName = (TextView) convertView.findViewById(R.id.clerkNametextView23);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        DinningTableOrder tableOrder = DinningTableOrderDAO.getByNumber(dinningTables.get(position).getNumber());
        holder.image.setImageResource(R.drawable.table_round_lg);
        holder.tableNumber.setText(dinningTables.get(position).getNumber());
        holder.dinningTable = dinningTables.get(position);
        List<Clerk> clerks = tableAssignedClerks.get(dinningTables.get(position).getId());

        if (selectedDinningTables != null) {
            boolean selected = selectedDinningTables.contains(dinningTables.get(position));
            if (selected) {
                holder.isSelectedCheckBox.setVisibility(View.VISIBLE);
            } else {
                holder.isSelectedCheckBox.setVisibility(View.GONE);
            }
        } else {
            holder.isSelectedCheckBox.setVisibility(View.GONE);
        }
        if (tableOrder != null) {
            holder.time.setBackgroundResource(R.color.seat7);
            holder.guests.setBackgroundResource(R.color.seat7);
            holder.amount.setBackgroundResource(R.color.seat7);
            holder.time.setVisibility(View.VISIBLE);
            holder.guests.setVisibility(View.VISIBLE);
            holder.amount.setVisibility(View.VISIBLE);
            holder.clerkName.setText(clerks != null && !clerks.isEmpty() ? clerks.get(0).getEmpName() : "");
            holder.time.setText(tableOrder.getElapsedTime());
            holder.guests.setText(String.format("%d/%d", tableOrder.getNumberOfGuest(), dinningTables.get(position).getSeats()));
            Order order = tableOrder.getOrder(activity);
            holder.amount.setText(Global.getCurrencyFormat(order.ord_subtotal));
        } else {
            holder.time.setBackgroundResource(R.color.seat12);
            holder.guests.setBackgroundResource(R.color.seat12);
            holder.amount.setBackgroundResource(R.color.seat12);
            holder.time.setVisibility(View.GONE);
            holder.clerkName.setText(clerks != null && !clerks.isEmpty() ? clerks.get(0).getEmpName() : "");
            holder.guests.setText(String.format("%d/%d", 0, dinningTables.get(position).getSeats()));
            holder.amount.setVisibility(View.GONE);
        }
        return convertView;
    }


    public class ViewHolder {
        TextView tableNumber, guests, time, amount, clerkName;
        ImageView image;
        ImageView isSelectedCheckBox;
        public DinningTable dinningTable;
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
        return dinningTables.size();
    }

    @Override
    public Object getItem(int position) {
        return dinningTables.get(position);
    }

}