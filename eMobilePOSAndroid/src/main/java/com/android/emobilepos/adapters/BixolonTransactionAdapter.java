package com.android.emobilepos.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.database.OrdersHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.models.orders.Order;
import com.android.emobilepos.models.realms.BixolonTransaction;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

/**
 * Created by guarionex on 6/2/17.
 */

public class BixolonTransactionAdapter extends RealmRecyclerViewAdapter<BixolonTransaction, BixolonTransactionAdapter.ViewHolder> {

    private final OrdersHandler ordersDB;
    private Context context;

    public BixolonTransactionAdapter(@NonNull Context context, @Nullable OrderedRealmCollection<BixolonTransaction> data, boolean autoUpdate) {
        super(context, data, autoUpdate);
        ordersDB = new OrdersHandler(context);
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.trans_lvadapter, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public int getItemCount() {
        return getData().size();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        BixolonTransaction transaction = getItem(position);
        Order order = ordersDB.getOrder(transaction.getOrderId());
        holder.amount.setText(order.ord_total);
        holder.clientName.setText(order.cust_name);
        holder.title.setText(String.format("%s - %s", order.ord_id, transaction.getBixolonTransactionId()));
        holder.voidText.setText(context.getString(R.string.failed));
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, clientName, amount, voidText;
        ImageView syncIcon;

        ViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.transLVtitle);
            clientName = (TextView) view.findViewById(R.id.transLVid);
            amount = (TextView) view.findViewById(R.id.transLVamount);
            voidText = (TextView) view.findViewById(R.id.transVoidText);
            syncIcon = (ImageView) view.findViewById(R.id.transIcon);
        }
    }
}
