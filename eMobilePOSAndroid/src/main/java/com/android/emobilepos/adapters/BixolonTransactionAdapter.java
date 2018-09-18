package com.android.emobilepos.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.database.OrdersHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.bixolon.BixolonTransactionsActivity;
import com.android.emobilepos.models.orders.Order;
import com.android.emobilepos.models.realms.BixolonTransaction;
import com.android.support.Global;

import drivers.EMSBixolonRD;
import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

/**
 * Created by guarionex on 6/2/17.
 */

public class BixolonTransactionAdapter extends RealmRecyclerViewAdapter<BixolonTransaction, BixolonTransactionAdapter.ViewHolder> {

    private final OrdersHandler ordersDB;
    private EMSBixolonRD bixolon;
    private Context context;

    public BixolonTransactionAdapter(@NonNull Context context, @Nullable OrderedRealmCollection<BixolonTransaction> data, boolean autoUpdate) {
        super(data, autoUpdate);
        ordersDB = new OrdersHandler(context);
        this.context = context;
        if (Global.mainPrinterManager != null && Global.mainPrinterManager.getCurrentDevice() != null
                && Global.mainPrinterManager.getCurrentDevice() instanceof EMSBixolonRD) {
            bixolon = (EMSBixolonRD) Global.mainPrinterManager.getCurrentDevice();
        }
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
        final BixolonTransaction transaction = getItem(position);
        Order order = ordersDB.getOrder(transaction.getOrderId());
        holder.amount.setText(order.ord_total);
        holder.clientName.setText(order.cust_name);
        holder.title.setText(String.format("%s - %s", order.ord_id, transaction.getBixolonTransactionId()));
        holder.voidText.setText(context.getString(R.string.failed));
        holder.retryBtn.setVisibility(View.VISIBLE);
        holder.retryBtn.setTag(order);
        holder.retryBtn.setEnabled(bixolon != null);
        if (bixolon != null) {
            holder.retryBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Order order = (Order) v.getTag();
                    new RetryBixolonTransactionTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, order);
                }
            });
        }
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, clientName, amount, voidText;
        ImageView syncIcon;
        Button retryBtn;

        ViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.transLVtitle);
            clientName = (TextView) view.findViewById(R.id.transLVid);
            amount = (TextView) view.findViewById(R.id.transLVamount);
            voidText = (TextView) view.findViewById(R.id.transVoidText);
            syncIcon = (ImageView) view.findViewById(R.id.transIcon);
            retryBtn = (Button) view.findViewById(R.id.bixolonTransactionRetrybutton);
        }
    }

    private class RetryBixolonTransactionTask extends AsyncTask<Order, Void, Boolean> {
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(context);
            dialog.setTitle(R.string.processing);
            dialog.setIndeterminate(true);
            dialog.setMessage(context.getString(R.string.sync_sending_orders));
            dialog.show();
        }

        @Override
        protected Boolean doInBackground(Order... params) {
            Order order = params[0];
            return bixolon.printTransaction(order.ord_id, Global.OrderType.getByCode(Integer.parseInt(order.ord_type)), false, false, null);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            dialog.dismiss();
            if (!result) {
                Global.showPrompt(context, R.string.sync_fail, context.getString(R.string.bixolon_command_fail));
            }

        }
    }
}
