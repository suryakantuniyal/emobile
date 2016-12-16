package com.android.emobilepos;


import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.widget.CursorAdapter;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.database.CustomersHandler;
import com.android.database.DBManager;
import com.android.database.OrderProductsHandler;
import com.android.database.OrdersHandler;
import com.android.database.ProductAddonsHandler;
import com.android.database.ProductsHandler;
import com.android.database.SalesTaxCodesHandler;
import com.android.emobilepos.models.Order;
import com.android.emobilepos.models.OrderProduct;
import com.android.emobilepos.ordering.OrderingMain_FA;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.NetworkUtils;
import com.android.support.OnHoldsManager;
import com.android.support.OrderProductUtils;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class OnHoldActivity extends BaseFragmentActivityActionBar {
    private Activity activity;
    private Cursor myCursor;
    private Global global;
    private Global.OrderType orderType = Global.OrderType.SALES_RECEIPT;
    boolean validPassword = true;
    private boolean isUpdateOnHold = false;
    private boolean hasBeenCreated = false;
    private MyPreferences myPref;
    private int selectedPos = 0;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.onhold_layout);
        activity = this;
        global = (Global) activity.getApplication();
        myPref = new MyPreferences(activity);
        ListView listView = (ListView) findViewById(R.id.onHoldListView);
        OrdersHandler ordersHandler = new OrdersHandler(activity);
        myCursor = ordersHandler.getOrderOnHold();
        ListViewCursorAdapter myAdapter = new ListViewCursorAdapter(activity, myCursor, CursorAdapter.NO_SELECTION);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
                                    long arg3) {
                selectedPos = pos;
                openPrintOnHold();
            }
        });
        listView.setAdapter(myAdapter);
        hasBeenCreated = true;

    }


    @Override
    public void onDestroy() {
        myCursor.close();
        super.onDestroy();
    }


    @Override
    public void onResume() {

        if (global.isApplicationSentToBackground(activity))
            global.loggedIn = false;
        global.stopActivityTransitionTimer();

        if (hasBeenCreated && !global.loggedIn) {
            if (global.getGlobalDlog() != null)
                global.getGlobalDlog().dismiss();
            global.promptForMandatoryLogin(activity);
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        boolean isScreenOn = powerManager.isScreenOn();
        if (!isScreenOn)
            global.loggedIn = false;
        global.startActivityTransitionTimer();
    }


    public class checkHoldStatus extends AsyncTask<Void, String, String> {
        boolean wasProcessed = false;
        private ProgressDialog myProgressDialog;

        @Override
        protected void onPreExecute() {
            myProgressDialog = new ProgressDialog(activity);
            myProgressDialog.setMessage("Loading...");
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.setCancelable(false);
            myProgressDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            myCursor.moveToPosition(selectedPos);
            myPref.setCustSelected(false);
            String ordID = myCursor.getString(myCursor.getColumnIndex("ord_id"));
            global.setSelectedComments(myCursor.getString(myCursor.getColumnIndex("ord_comment")));
            if (NetworkUtils.isConnectedToInternet(activity)) {
                try {
                    if (!isUpdateOnHold) {
                        if (!OnHoldsManager.isOnHoldAdminClaimRequired(ordID, activity)) {
                            wasProcessed = true;
                            OnHoldsManager.updateStatusOnHold(ordID, activity);
                        }
                    } else {
                        wasProcessed = true;
                        OnHoldsManager.updateStatusOnHold(ordID, activity);
                    }
                } catch (Exception e) {
                }

                return null;
            } else {
                OrdersHandler ordersHandler = new OrdersHandler(activity);
                if (ordersHandler.isOrderOffline(ordID))
                    wasProcessed = true;
                return null;
            }
        }

        @Override
        protected void onPostExecute(String unused) {
            myProgressDialog.dismiss();

            if (wasProcessed) {
                new executeOnHoldAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, false);
            } else {
                claimedTransactionPrompt();
            }
        }
    }


    private class executeOnHoldAsync extends AsyncTask<Boolean, Void, Intent> {
        private boolean proceed = false;
        private Intent intent;
        private OrderProductsHandler orderProdHandler;
        private boolean forPrinting = false;
        private ProgressDialog myProgressDialog;

        @Override
        protected void onPreExecute() {
            myProgressDialog = new ProgressDialog(activity);
            myProgressDialog.setMessage("Please wait...");
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.setCancelable(false);
            myProgressDialog.show();

        }

        @Override
        protected Intent doInBackground(Boolean... params) {

            myCursor.moveToPosition(selectedPos);
            Order order = OrdersHandler.getOrder(myCursor, activity);
            orderProdHandler = new OrderProductsHandler(activity);

            Global.lastOrdID = order.ord_id;// myCursor.getString(myCursor.getColumnIndex("ord_id"));
            Global.taxID = order.tax_id;//myCursor.getString(myCursor.getColumnIndex("tax_id"));

            orderType = Global.OrderType.getByCode(Integer.parseInt(order.ord_type));
            String ord_HoldName = order.ord_HoldName;//myCursor.getString(myCursor.getColumnIndex("ord_HoldName"));
            selectCustomer(order.cust_id);//myCursor.getString(myCursor.getColumnIndex("cust_id")));

            forPrinting = params[0];
            if (!forPrinting) {
                intent = new Intent(activity, OrderingMain_FA.class);
                // intent = new Intent(activity, SalesReceiptSplitActivity.class);
                String assignedTable = order.assignedTable;//myCursor.getString(myCursor.getColumnIndex("assignedTable"));
                intent.putExtra("selectedDinningTableNumber", assignedTable);
                intent.putExtra("onHoldOrderJson", order.toJson());

                intent.putExtra("openFromHold", true);
                if (assignedTable != null && !assignedTable.isEmpty()) {
                    intent.putExtra("RestaurantSaleType", Global.RestaurantSaleType.EAT_IN);
                } else {
                    intent.putExtra("RestaurantSaleType", Global.RestaurantSaleType.TO_GO);
                }
                switch (orderType) {
                    case SALES_RECEIPT:
                        intent.putExtra("option_number", Global.TransactionType.SALE_RECEIPT);
                        break;
                    case RETURN:
                        intent.putExtra("option_number", Global.TransactionType.RETURN);
                        break;
                    case ORDER:
                        intent.putExtra("option_number", Global.TransactionType.ORDERS);
                        break;
                    case INVOICE:
                        intent.putExtra("option_number", Global.TransactionType.INVOICE);
                        break;
                    case ESTIMATE:
                        intent.putExtra("option_number", Global.TransactionType.ESTIMATE);
                        break;
                }

                intent.putExtra("ord_HoldName", ord_HoldName);
                intent.putExtra("associateId", order.associateID);

                Global.isFromOnHold = true;

            }

            if (NetworkUtils.isConnectedToInternet(activity))
                proceed = true;

            return intent;
        }

        @Override
        protected void onPostExecute(Intent intent) {
            myProgressDialog.dismiss();
            if (proceed) {
                DBManager dbManager = new DBManager(activity);
                if (!forPrinting)
                    dbManager.synchDownloadOnHoldDetails(intent, myCursor.getString(myCursor.getColumnIndex("ord_id")), 0);
                else
                    dbManager.synchDownloadOnHoldDetails(intent, myCursor.getString(myCursor.getColumnIndex("ord_id")), 1);
            } else {
                Cursor c = orderProdHandler.getOrderProductsOnHold(myCursor.getString(myCursor.getColumnIndex("ord_id")));
                int size = c.getCount();
                if (size > 0) {
                    if (!forPrinting) {
                        addOrderProducts(OnHoldActivity.this, c);
                        startActivityForResult(intent, 0);
                        activity.finish();
                    } else {
                        DBManager dbManager = new DBManager(activity);
                        dbManager.synchDownloadOnHoldDetails(intent, myCursor.getString(myCursor.getColumnIndex("ord_id")), 1);
                    }
                } else
                    Toast.makeText(activity, "no available items", Toast.LENGTH_LONG).show();
            }
        }
    }


    public void printOnHoldTransaction() {
        new printAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class printAsync extends AsyncTask<Void, Void, Void> {
        private boolean printSuccessful = true;
        private ProgressDialog myProgressDialog;

        @Override
        protected void onPreExecute() {
            myProgressDialog = new ProgressDialog(activity);
            myProgressDialog.setMessage("Printing...");
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.setCancelable(false);
            myProgressDialog.show();

        }

        @Override
        protected Void doInBackground(Void... params) {

            if (Global.mainPrinterManager != null && Global.mainPrinterManager.getCurrentDevice() != null) {
                printSuccessful = Global.mainPrinterManager.getCurrentDevice().printTransaction(Global.lastOrdID, orderType, false, true);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            myProgressDialog.dismiss();

            if (!printSuccessful)
                showPrintDlg();

        }
    }

    private void showPrintDlg() {
        final Dialog dlog = new Dialog(this, R.style.Theme_TransparentTest);
        dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dlog.setCancelable(true);
        dlog.setCanceledOnTouchOutside(true);
        dlog.setContentView(R.layout.dlog_btn_left_right_layout);

        TextView viewTitle = (TextView) dlog.findViewById(R.id.dlogTitle);
        TextView viewMsg = (TextView) dlog.findViewById(R.id.dlogMessage);

        viewTitle.setText(R.string.dlog_title_error);
        viewMsg.setText(R.string.dlog_msg_failed_print);

        Button btnYes = (Button) dlog.findViewById(R.id.btnDlogLeft);
        Button btnNo = (Button) dlog.findViewById(R.id.btnDlogRight);
        dlog.findViewById(R.id.btnDlogCancel).setVisibility(View.GONE);
        btnYes.setText(R.string.button_yes);
        btnNo.setText(R.string.button_no);

        btnYes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dlog.dismiss();
                new printAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            }
        });
        btnNo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dlog.dismiss();
            }
        });
        dlog.show();
    }


    private void claimedTransactionPrompt() {

        final Dialog dlog = new Dialog(activity, R.style.Theme_TransparentTest);
        dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dlog.setCancelable(true);
        dlog.setCanceledOnTouchOutside(true);
        dlog.setContentView(R.layout.dlog_btn_left_right_layout);

        TextView viewTitle = (TextView) dlog.findViewById(R.id.dlogTitle);
        TextView viewMsg = (TextView) dlog.findViewById(R.id.dlogMessage);
        viewTitle.setText(R.string.dlog_title_claimed_hold);
        viewMsg.setText(R.string.dlog_msg_claimed_hold);
        Button btnOpen = (Button) dlog.findViewById(R.id.btnDlogLeft);
        Button btnCancel = (Button) dlog.findViewById(R.id.btnDlogRight);
        btnOpen.setText(R.string.button_open);
        btnCancel.setText(R.string.button_cancel);
        dlog.findViewById(R.id.btnDlogCancel).setVisibility(View.GONE);
        btnOpen.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dlog.dismiss();
                askForManagerPassDlg();

            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dlog.dismiss();
            }
        });
        dlog.show();

    }


    private void askForManagerPassDlg()        //type 0 = Change Password, type 1 = Configure Home Menu
    {

        final Dialog globalDlog = new Dialog(activity, R.style.Theme_TransparentTest);
        globalDlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        globalDlog.setCancelable(true);
        globalDlog.setCanceledOnTouchOutside(true);
        globalDlog.setContentView(R.layout.dlog_field_single_layout);

        final EditText viewField = (EditText) globalDlog.findViewById(R.id.dlogFieldSingle);
        viewField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        TextView viewTitle = (TextView) globalDlog.findViewById(R.id.dlogTitle);
        TextView viewMsg = (TextView) globalDlog.findViewById(R.id.dlogMessage);
        viewTitle.setText(R.string.dlog_title_confirm);
        if (!validPassword)
            viewMsg.setText(R.string.invalid_password);
        else
            viewMsg.setText(R.string.dlog_title_enter_manager_password);
        Button btnCancel = (Button) globalDlog.findViewById(R.id.btnCancelDlogSingle);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                globalDlog.dismiss();
            }
        });

        Button btnOk = (Button) globalDlog.findViewById(R.id.btnDlogSingle);
        btnOk.setText(R.string.button_ok);
        btnOk.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                globalDlog.dismiss();
                String value = viewField.getText().toString();
                if (value.equals(myPref.posManagerPass(true, null))) // validate manager password
                {
                    validPassword = true;
                    isUpdateOnHold = true;
                    new checkHoldStatus().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    validPassword = false;
                    askForManagerPassDlg();
                }
            }
        });
        globalDlog.show();

    }


    private void openPrintOnHold()        //type 0 = Change Password, type 1 = Configure Home Menu
    {

        final Dialog dlog = new Dialog(activity, R.style.Theme_TransparentTest);
        dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dlog.setCancelable(true);
        dlog.setCanceledOnTouchOutside(true);
        dlog.setContentView(R.layout.dlog_btn_left_right_layout);

        TextView viewTitle = (TextView) dlog.findViewById(R.id.dlogTitle);
        TextView viewMsg = (TextView) dlog.findViewById(R.id.dlogMessage);
        viewTitle.setText(R.string.dlog_title_choose_action);
        viewMsg.setVisibility(View.GONE);
        Button btnOpen = (Button) dlog.findViewById(R.id.btnDlogLeft);
        Button btnPrint = (Button) dlog.findViewById(R.id.btnDlogRight);
        Button btnCancel = (Button) dlog.findViewById(R.id.btnDlogCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlog.dismiss();
            }
        });
        btnOpen.setText(R.string.button_open);
        btnPrint.setText(R.string.button_print);

        btnOpen.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dlog.dismiss();
                new checkHoldStatus().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


            }
        });
        btnPrint.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dlog.dismiss();
                myCursor.moveToPosition(selectedPos);
                if (myCursor.getString(myCursor.getColumnIndex("ord_issync")).equals("0")) {
                    Global.lastOrdID = myCursor.getString(myCursor.getColumnIndex("ord_id"));
                    new printAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    new executeOnHoldAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, true);
                }
            }
        });
        dlog.show();

    }


    public static void addOrderProducts(Activity activity, Cursor c) {
        OrderProductsHandler orderProductsHandler = new OrderProductsHandler(activity);
        c.moveToFirst();
        List<OrderProduct> orderProducts = orderProductsHandler.getOrderProducts(c.getString(c.getColumnIndex("ord_id")));
        ProductAddonsHandler prodAddonHandler = new ProductAddonsHandler(activity);
        ProductsHandler prodHandler = new ProductsHandler(activity);
        String[] discountInfo;
        double total;
        double itemTotal = 0;
        Global global = (Global) activity.getApplication();
        global.orderProducts = new ArrayList<>();

        for (OrderProduct ord : orderProducts) {
            double discAmount = 0;
            total = (Double.parseDouble(ord.getOrdprod_qty())) * Double.parseDouble(ord.getFinalPrice());
            discountInfo = prodHandler.getDiscount(ord.getDiscount_id(), ord.getFinalPrice());
            if (discountInfo != null) {
                if (discountInfo[1] != null && discountInfo[1].equals("Fixed")) {
                    ord.setDiscount_is_fixed("1");
                }
                if (discountInfo[2] != null) {
                    discAmount = Double.parseDouble(discountInfo[4]);
                }
                if (discountInfo[3] != null) {
                    ord.setDiscount_is_taxable(discountInfo[3]);
                }
                if (discountInfo[4] != null) {
                    ord.setDisTotal(discountInfo[4]);
                    discAmount = Double.parseDouble(discountInfo[4]);
                    ord.setDiscount_value(discountInfo[4]);
                }
            }
            ord.setDisAmount(ord.getDiscount_value());
            if (itemTotal < 0)
                itemTotal = 0;
            ord.setItemTotal(Double.toString(total - discAmount));
            ord.setItemSubtotal(Double.toString(total));
            if (ord.isAddon()) {
                if (global.addonSelectionType == null)
                    global.addonSelectionType = new HashMap<>();
                int pos = global.orderProducts.size();
                if (pos > 0) {
                    String[] tempVal = prodAddonHandler.getAddonDetails(ord.getAddon_ordprod_id(), ord.getProd_id());
                    global.addonSelectionType.put(c.getString(c.getColumnIndex("prod_id")), new String[]{(ord.isAdded() ? "1" : "2"), tempVal[1], tempVal[0]});
                }
                global.orderProductAddons.add(ord);
                if (Global.addonSelectionMap == null)
                    Global.addonSelectionMap = new HashMap<>();
                if (Global.orderProductAddonsMap == null)
                    Global.orderProductAddonsMap = new HashMap<>();
                if (global.addonSelectionType.size() > 0) {
                    Global.addonSelectionMap.put(ord.getAddon_ordprod_id(), global.addonSelectionType);
                    Global.orderProductAddonsMap.put(ord.getAddon_ordprod_id(), global.orderProductAddons);
                    global.orderProductAddons = new ArrayList<>();
                    global.addonSelectionType = new HashMap<>();
                }
            } else {
                global.orderProducts.add(ord);
            }
        }
    }


    private void selectCustomer(String custID) {
        if (custID != null && !custID.isEmpty()) {
            CustomersHandler ch = new CustomersHandler(activity);
            HashMap<String, String> temp = ch.getCustomerInfo(custID);


            SalesTaxCodesHandler taxHandler = new SalesTaxCodesHandler(activity);
            SalesTaxCodesHandler.TaxableCode taxable = taxHandler.checkIfCustTaxable(temp.get("cust_taxable"));
            myPref.setCustTaxCode(taxable, temp.get("cust_salestaxcode"));

            myPref.setCustID(temp.get("cust_id"));    //getting cust_id as _id
            myPref.setCustName(temp.get("cust_name"));
            myPref.setCustIDKey(temp.get("custidkey"));
            myPref.setCustSelected(true);
            myPref.setCustPriceLevel(temp.get("pricelevel_id"));
            myPref.setCustEmail(temp.get("cust_email"));
        }
    }


    public class ListViewCursorAdapter extends CursorAdapter {
        private LayoutInflater inflater;


        ListViewCursorAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
            inflater = LayoutInflater.from(context);

            //imageLoaderTest = new ImageLoaderTest(activity);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {

            final ViewHolder myHolder = (ViewHolder) view.getTag();
            myHolder.holdID.setText(cursor.getString(myHolder.i_holdID));
            myHolder.holdName.setText(cursor.getString(myHolder.i_holdName));
            if (cursor.getString(myHolder.i_issync).equals("1"))//it is synched
            {
                myHolder.offlineFlag.setVisibility(View.GONE);
            } else {
                myHolder.offlineFlag.setVisibility(View.VISIBLE);
            }


        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {

            View retView;
            retView = inflater.inflate(R.layout.onhold_listview_adapter, parent, false);

            ViewHolder holder = new ViewHolder();
            holder.holdID = (TextView) retView.findViewById(R.id.onHoldID);
            holder.holdName = (TextView) retView.findViewById(R.id.onHoldName);
            holder.offlineFlag = (TextView) retView.findViewById(R.id.onHoldOfflineFlag);


            holder.i_holdID = cursor.getColumnIndex("_id");
            holder.i_holdName = cursor.getColumnIndex("ord_HoldName");
            holder.i_issync = cursor.getColumnIndex("ord_issync");


            retView.setTag(holder);
            return retView;
        }

        private class ViewHolder {

            TextView holdID, holdName, offlineFlag;

            int i_issync, i_holdID, i_holdName;
        }
    }
}
