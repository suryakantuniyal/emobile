package com.android.emobilepos.mainmenu;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.android.database.ConsignmentTransactionHandler;
import com.android.database.CustomersHandler;
import com.android.database.DBManager;
import com.android.database.OrdersHandler;
import com.android.database.PaymentsHandler;
import com.android.database.TemplateHandler;
import com.android.database.TransferLocations_DB;
import com.android.database.VoidTransactionsHandler;
import com.android.emobilepos.R;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.NetworkUtils;
import com.android.support.SynchMethods;

public class SyncTab_FR extends Fragment implements View.OnClickListener {
    public static Handler syncTabHandler;
    ProgressDialog dialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.synchronization_layout, container, false);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setViewData(view);
        Button syncSendButton = (Button) view.findViewById(R.id.syncSendButton);
        Button syncReceiveButton = (Button) view.findViewById(R.id.syncReceiveButton);
        syncSendButton.setOnClickListener(this);
        syncReceiveButton.setOnClickListener(this);
        setHandler();
    }

    private void setHandler() {
        syncTabHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {

                switch (msg.what) {
//                    case 1:
//                        dialog = new ProgressDialog(getActivity());
//                        dialog.setIndeterminate(true);
//                        dialog.setMessage(getString(R.string.sync_inprogress));
//                        dialog.show();
//                        break;
                    case 0:
                        MainMenu_FA mainMenuFa = (MainMenu_FA) getActivity();
                        mainMenuFa.getSynchTextView().setVisibility(View.GONE);
                        Global.dismissDialog(getActivity(), dialog);
                    default:
                        setViewData(getView());
                }
                return false;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        setViewData(getView());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    private void setViewData(View view) {
        if (view != null) {
            MyPreferences preferences = new MyPreferences(getActivity());
            TextView syncPaymentsQty = (TextView) view.findViewById(R.id.syncPaymentsQty);
            TextView sync_salesQty = (TextView) view.findViewById(R.id.sync_salesQty);
            TextView syncSignaturesQty = (TextView) view.findViewById(R.id.syncSignaturesQty);
            TextView syncConsignmentsQty = (TextView) view.findViewById(R.id.syncConsignmentsQty);
            TextView syncTemplatesQty = (TextView) view.findViewById(R.id.syncTemplatesQty);
            TextView syncCustomersQty = (TextView) view.findViewById(R.id.syncCustomersQty);
            TextView syncVoidsQty = (TextView) view.findViewById(R.id.syncVoidsQty);
            TextView syncTransfersQty = (TextView) view.findViewById(R.id.syncTransfersQty);
            TextView synchFeedText = (TextView) view.findViewById(R.id.synchFeedText);
            TextView synchSendDate = (TextView) view.findViewById(R.id.synchSendDate);
            TextView synchReceiveDate = (TextView) view.findViewById(R.id.synchReceiveDate);

            PaymentsHandler paymentHandler = new PaymentsHandler(getActivity());
            int unsyncPayments = (int) paymentHandler.getNumUnsyncPayments();
            syncPaymentsQty.setText(String.valueOf(unsyncPayments));

            OrdersHandler ordersHandler = new OrdersHandler(getActivity());
            int unsycOrders = (int) ordersHandler.getNumUnsyncOrders();
            sync_salesQty.setText(String.valueOf(unsycOrders));

            VoidTransactionsHandler voidHandler = new VoidTransactionsHandler();
            int unsyncVoids = (int) voidHandler.getNumUnsyncVoids();
            syncVoidsQty.setText(String.valueOf(unsyncVoids));

            CustomersHandler custHandler = new CustomersHandler(getActivity());
            int unsyncCust = (int) custHandler.getNumUnsyncCustomers();
            syncCustomersQty.setText(String.valueOf(unsyncCust));

            TemplateHandler templateHandler = new TemplateHandler(getActivity());
            int unsyncTemplates = (int) templateHandler.getNumUnsyncTemplates();
            syncTemplatesQty.setText(String.valueOf(unsyncTemplates));

            ConsignmentTransactionHandler consignmentHandler = new ConsignmentTransactionHandler(getActivity());
            int unsyncConsignment = (int) consignmentHandler.getNumUnsyncItems();
            syncConsignmentsQty.setText(String.valueOf(unsyncConsignment));

            TransferLocations_DB transferDB = new TransferLocations_DB(getActivity());
            int unsyncTransfer = (int) transferDB.getNumUnsyncTransfers();
            syncTransfersQty.setText(String.valueOf(unsyncTransfer));

            syncSignaturesQty.setText("0");
            synchFeedText.setText(getWifiConnectivityName());
            synchSendDate.setText(preferences.getLastSendSync());
            synchReceiveDate.setText(preferences.getLastReceiveSync());
        }
    }

    private String getWifiConnectivityName() {
        String wifiName = getString(R.string.sync_no_connectivity);
        StringBuilder sb = new StringBuilder();

        ConnectivityManager connManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo myWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo myMobile = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (myWifi != null && myWifi.isConnected()) {
            WifiManager wifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            sb.append(getString(R.string.sync_connected_to)).append(": ").append(wifiInfo.getSSID());
            wifiName = sb.toString();
        } else if (myMobile != null && myMobile.isConnected()) {
            wifiName = sb.append(getString(R.string.sync_connected_to)).append(": Carrier's Network").toString();
        }

        return wifiName;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.syncSendButton:
                dialog = new ProgressDialog(getActivity());
                dialog.setIndeterminate(true);
                dialog.setMessage(getString(R.string.sync_inprogress));
                dialog.show();
                DBManager dbManager = new DBManager(getActivity(), Global.FROM_SYNCH_ACTIVITY);
                SynchMethods sm = new SynchMethods(dbManager);
                if (NetworkUtils.isConnectedToInternet(getActivity())) {
                    sm.synchSend(Global.FROM_SYNCH_ACTIVITY, true, getActivity());
                }
                break;
            case R.id.syncReceiveButton:
                dbManager = new DBManager(getActivity(), Global.FROM_SYNCH_ACTIVITY);
                if (dbManager.unsynchItemsLeft()) {
                    Global.showPrompt(getActivity(), R.string.dlog_title_error, getActivity().getString(R.string.send_unsync_items_first));
                } else {
                    new SyncReceiveTask().execute(dbManager);
//                    sm = new SynchMethods(dbManager);
//                    sm.syncReceive();
                }
                break;
        }
    }

    public class SyncReceiveTask extends AsyncTask<DBManager, Void, Boolean> {
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(getActivity());
            dialog.setTitle(R.string.sync_title);
            dialog.setIndeterminate(true);
            dialog.setMessage(getString(R.string.sync_inprogress));
            dialog.show();
        }

        @Override
        protected Boolean doInBackground(DBManager... params) {
            DBManager dbManager = params[0];
            SynchMethods sm = new SynchMethods(dbManager);
            return sm.syncReceive();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            dialog.dismiss();
            if (!result) {
                Global.showPrompt(getActivity(), R.string.sync_title, getString(R.string.sync_fail));
            }
            SyncTab_FR.syncTabHandler.sendEmptyMessage(0);
        }
    }
}
