package com.android.emobilepos.consignment;


import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.android.database.ConsignmentSignaturesDBHandler;
import com.android.database.ConsignmentTransactionHandler;
import com.android.database.CustomerInventoryHandler;
import com.android.database.OrderProductsHandler;
import com.android.database.OrdersHandler;
import com.android.database.ProductsHandler;
import com.android.emobilepos.DrawReceiptActivity;
import com.android.emobilepos.R;
import com.android.support.ConsignmentTransaction;
import com.android.support.GenerateNewID;
import com.android.support.GenerateNewID.IdType;
import com.android.support.Global;
import com.android.support.MyPreferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ConsignmentPickup_FR extends Fragment implements OnClickListener {
    private ListView myListview;
    private CustomAdapter_LV myAdapter;
    private ProgressDialog myProgressDialog;
    private CustomerInventoryHandler custInventoryHandler;
    private MyPreferences myPref;
    private Global global;
    private Activity activity;
    //private ConsignmentTransactionHandler cih;
    private List<ConsignmentTransaction> consTransactionList;

    private OrdersHandler ordersHandler;
    private OrderProductsHandler orderProductsHandler;
    private int orientation;
    private HashMap<String, String> signatureData = new HashMap<String, String>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.consign_fragment_layout, container, false);
        activity = getActivity();
        myListview = (ListView) view.findViewById(R.id.consignSummaryListView);
        myAdapter = new CustomAdapter_LV(activity);

        custInventoryHandler = new CustomerInventoryHandler(activity);
        myPref = new MyPreferences(activity);
        global = (Global) getActivity().getApplication();
        myListview.setAdapter(myAdapter);


        Button btnProcess = (Button) view.findViewById(R.id.saveConsignButton);
        btnProcess.setOnClickListener(this);

        return view;

    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.saveConsignButton:
                new processAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                break;
        }
    }


    public void notifyListViewChange() {
        myAdapter.notifyDataSetChanged();
    }


    private class processAsync extends AsyncTask<String, String, String> {
        ConsignmentTransaction consTransaction;


        @Override
        protected void onPreExecute() {
            consTransactionList = new ArrayList<ConsignmentTransaction>();

            myProgressDialog = new ProgressDialog(activity);
            myProgressDialog.setMessage("Processing...");
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.setCancelable(false);
            myProgressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {

            ordersHandler = new OrdersHandler(activity);
            orderProductsHandler = new OrderProductsHandler(activity);
            ProductsHandler prodHandler = new ProductsHandler(activity);

            String[] tempArray;
            int size = Global.consignMapKey.size();
            consTransaction = new ConsignmentTransaction();
            custInventoryHandler.insertUpdate(Global.custInventoryList);
            double newOnHandQty = 0;


            GenerateNewID generator = new GenerateNewID(activity);
            String consTransID = "";


//			if(cih.getDBSize()>0)
//				consTransID = cih.getLastConsTransID();
//			else if(!myPref.getLastConsTransID().isEmpty())
//				consTransID = myPref.getLastConsTransID();
            consTransID = generator.getNextID(IdType.CONSIGNMENT_ID);

            //consTransID = generator.generate(consTransID, 3);
            signatureData.put("ConsTrans_ID", consTransID);

            for (int i = 0; i < size; i++) {
                consTransaction.ConsTrans_ID = consTransID;
                consTransaction.ConsEmp_ID = myPref.getEmpID();
                consTransaction.ConsCust_ID = myPref.getCustID();
                consTransaction.ConsProd_ID = Global.consignMapKey.get(i);
                consTransaction.ConsPickup_ID = Global.consignment_order.ord_id;
                consTransaction.ConsOriginal_Qty = Global.consignSummaryMap.get(Global.consignMapKey.get(i)).get("original_qty");
                tempArray = Global.custInventoryMap.get(Global.consignMapKey.get(i));
                if (tempArray != null)
                    consTransaction.ConsInventory_Qty = tempArray[2];

                consTransaction.ConsPickup_Qty = Global.consignSummaryMap.get(Global.consignMapKey.get(i)).get("pickup");
                consTransaction.ConsNew_Qty = Global.custInventoryList.get(i).qty;


                newOnHandQty = Double.parseDouble(Global.consignment_products.get(i).getOnHand()) + Double.parseDouble(consTransaction.ConsPickup_Qty);

                prodHandler.updateProductOnHandQty(Global.consignMapKey.get(i), newOnHandQty);
                consTransactionList.add(consTransaction);
                consTransaction = new ConsignmentTransaction();
            }

            return null;
        }


        @Override
        protected void onPostExecute(String unused) {
            myProgressDialog.dismiss();


            global.encodedImage = "";
            orientation = getResources().getConfiguration().orientation;
            Intent intent = new Intent(getActivity(), DrawReceiptActivity.class);
            if (orientation == Configuration.ORIENTATION_PORTRAIT)
                intent.putExtra("inPortrait", true);
            else
                intent.putExtra("inPortrait", false);
            startActivityForResult(intent, Global.S_CONSIGNMENT_TRANSACTION);

        }
    }


    private void finishConsignment() {
        myProgressDialog.dismiss();

        activity.finish();
    }


    private void showPrintDlg(int title, int msg) {
        final Dialog dlog = new Dialog(activity, R.style.Theme_TransparentTest);
        dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dlog.setCancelable(false);
        dlog.setContentView(R.layout.dlog_btn_left_right_layout);

        TextView viewTitle = (TextView) dlog.findViewById(R.id.dlogTitle);
        TextView viewMsg = (TextView) dlog.findViewById(R.id.dlogMessage);
        viewTitle.setText(title);
        viewMsg.setText(msg);
        Button btnYes = (Button) dlog.findViewById(R.id.btnDlogLeft);
        Button btnNo = (Button) dlog.findViewById(R.id.btnDlogRight);
        dlog.findViewById(R.id.btnDlogCancel).setVisibility(View.GONE);
        btnYes.setText(R.string.button_yes);
        btnNo.setText(R.string.button_no);
        btnYes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                dlog.dismiss();
                new printAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
            }
        });
        btnNo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                dlog.dismiss();
                finishConsignment();
            }
        });
        dlog.show();
    }


    private class printAsync extends AsyncTask<String, String, String> {
        private boolean printSuccessful = true;

        @Override
        protected void onPreExecute() {
            myProgressDialog = new ProgressDialog(activity);
            myProgressDialog.setMessage("Printing...");
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.setCancelable(false);
            myProgressDialog.show();

        }

        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub

            if (Global.mainPrinterManager != null && Global.mainPrinterManager.currentDevice != null) {
                printSuccessful = Global.mainPrinterManager.currentDevice.printConsignmentPickup(consTransactionList, global.encodedImage);
            }

            global.encodedImage = new String();

            return null;
        }

        @Override
        protected void onPostExecute(String unused) {
            myProgressDialog.dismiss();
            if (printSuccessful)
                showPrintDlg(R.string.dlog_title_confirm, R.string.dlog_msg_want_to_print);
            else
                showPrintDlg(R.string.dlog_title_error, R.string.dlog_msg_failed_print);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Global.FROM_DRAW_RECEIPT_PORTRAIT) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }


        Global.consignment_order.ord_signature = "";


        signatureData.put("encoded_signature", global.encodedImage);
        ConsignmentSignaturesDBHandler signHandler = new ConsignmentSignaturesDBHandler(activity);
        signHandler.insert(signatureData);

        Global.consignment_order.processed = "1";
        ordersHandler.insert(Global.consignment_order);
        orderProductsHandler.insert(Global.consignment_products);
        ConsignmentTransactionHandler cih = new ConsignmentTransactionHandler(activity);
        cih.insert(consTransactionList);


        if (myPref.getPreferences(MyPreferences.pref_enable_printing)) {
            if (!myPref.getPreferences(MyPreferences.pref_automatic_printing))
                showPrintDlg(R.string.dlog_title_confirm, R.string.dlog_msg_want_to_print);
            else
                new printAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else
            finishConsignment();

        super.onActivityResult(requestCode, resultCode, data);
    }


    private class CustomAdapter_LV extends BaseAdapter {
        private LayoutInflater mInflater;

        private List<String> idList;


        public CustomAdapter_LV(Context context) {
            mInflater = LayoutInflater.from(context);

            idList = Global.consignMapKey;
        }


        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            ViewHolder holder;

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.consign_pickup_listview_adapter, null);

                holder = new ViewHolder();

                holder.prodName = (TextView) convertView.findViewById(R.id.consignProdName);
                holder.prodID = (TextView) convertView.findViewById(R.id.consignProdID);
                holder.originalQty = (TextView) convertView.findViewById(R.id.consignOriginalQty);
                holder.pickupQty = (TextView) convertView.findViewById(R.id.consignPickupQty);

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }


            holder.prodName.setText(getContentValues(position, 0));
            holder.prodID.setText(getContentValues(position, 1));
            holder.originalQty.setText(getContentValues(position, 2));
            holder.pickupQty.setText(getContentValues(position, 3));

            return convertView;
        }


        public class ViewHolder {
            TextView prodName, prodID, pickupQty, originalQty;

        }


        private String getContentValues(int position, int type) {
            String value = new String();
            String empStr = "";
            switch (type) {
                case 0://Name
                    value = Global.consignSummaryMap.get(Global.consignMapKey.get(position)).get("ordprod_name");
                    if (value == null)
                        value = empStr;
                    break;
                case 1://ID
                    value = Global.consignSummaryMap.get(Global.consignMapKey.get(position)).get("prod_id");
                    if (value == null)
                        value = empStr;
                    else
                        value = " (" + value + ")";
                    break;
                case 2:    //Rack
                    value = Global.consignSummaryMap.get(Global.consignMapKey.get(position)).get("original_qty");
                    if (value == null)
                        value = "0";
                    break;
                case 3:
                    value = Global.consignSummaryMap.get(Global.consignMapKey.get(position)).get("pickup");
                    if (value == null)
                        value = "0";
                    break;
            }
            return value;
        }


        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return idList.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return idList.get(position);
        }
    }
}
