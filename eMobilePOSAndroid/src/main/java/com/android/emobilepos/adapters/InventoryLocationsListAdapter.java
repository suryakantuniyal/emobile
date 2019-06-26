package com.android.emobilepos.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.emobilepos.R;

import java.util.Collections;
import java.util.List;

import com.android.emobilepos.models.InventoryItem;

public class InventoryLocationsListAdapter extends RecyclerView.Adapter<InventoryLocationsListAdapter.View_Holder> {

    List<InventoryItem> list = Collections.emptyList();
    Context context;

    public InventoryLocationsListAdapter(List<InventoryItem> list, Context context) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public View_Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.multi_inventory_locations_item, parent, false);
        View_Holder holder = new View_Holder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull View_Holder holder, int position) {
        InventoryItem item = list.get(position);

        holder.name.setText(item.getName());
        holder.qty.setText(String.valueOf(item.getQty()));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void insert(int position, InventoryItem data) {
        list.add(position, data);
        notifyItemInserted(position);
    }

    public void remove(InventoryItem data) {
        int position = list.indexOf(data);
        list.remove(position);
        notifyItemRemoved(position);
    }

    public class View_Holder extends RecyclerView.ViewHolder {

        LinearLayout rv;
        TextView name;
        TextView qty;

        View_Holder(View itemView) {
            super(itemView);
            rv = itemView.findViewById(R.id.LinearLayoutRecyclerItem);
            name = itemView.findViewById(R.id.inventoryLocationName);
            qty = itemView.findViewById(R.id.inventoryQtyinLocation);
        }
    }
}
