package com.android.emobilepos;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v4.widget.CursorAdapter;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.dao.ClerkDAO;
import com.android.database.CustomersHandler;
import com.android.database.DBManager;
import com.android.database.OrderProductsHandler;
import com.android.database.OrdersHandler;
import com.android.database.SalesTaxCodesHandler;
import com.android.emobilepos.firebase.PollingNotificationService;
import com.android.emobilepos.mainmenu.MainMenu_FA;
import com.android.emobilepos.models.firebase.NotificationEvent;
import com.android.emobilepos.models.orders.Order;
import com.android.emobilepos.ordering.OrderingMain_FA;
import com.android.support.DateUtils;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.NetworkUtils;
import com.android.support.OnHoldsManager;
import com.android.support.SynchMethods;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;
import com.crashlytics.android.Crashlytics;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class OnHoldActivity extends BaseFragmentActivityActionBar {
    boolean validPassword = false;
    private Activity activity;
    private Cursor myCursor;
    private Global global;
    private Global.OrderType orderType = Global.OrderType.SALES_RECEIPT;
    private boolean isUpdateOnHold = false;
    private boolean hasBeenCreated = false;
    private MyPreferences myPref;
    private int selectedPos = 0;
    private HoldsCursorAdapter myAdapter;
    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Handler handler = new Handler();
            String eventAction = intent.getStringExtra(MainMenu_FA.NOTIFICATION_MESSAGE);
            NotificationEvent.NotificationEventAction action = NotificationEvent.NotificationEventAction.getNotificationEventByCode(Integer.parseInt(eventAction));
            switch (action) {
                case SYNC_HOLDS:
                    if (myCursor != null && !myCursor.isClosed()) {
                        myCursor.close();
                    }
                    OrdersHandler ordersHandler = new OrdersHandler(activity);
                    myCursor = ordersHandler.getOrdersOnHoldCursor();
                    myAdapter.swapCursor(myCursor);
                    myAdapter.notifyDataSetChanged();
                    break;
                case SYNC_MESAS_CONFIG:
                    break;
            }
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                }
            };
            handler.post(runnable);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.onhold_layout);
        activity = this;
        global = (Global) activity.getApplication();
        myPref = new MyPreferences(activity);
        ListView listView = findViewById(R.id.onHoldListView);
        OrdersHandler ordersHandler = new OrdersHandler(activity);
        myCursor = ordersHandler.getOrdersOnHoldCursor();
        long unsyncOrdersOnHold = ordersHandler.getNumUnsyncOrdersOnHold();
        if (unsyncOrdersOnHold > 0) {
            new SyncOnHolds().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//            DBManager dbManager = new DBManager(this);
//            SynchMethods sm = new SynchMethods(dbManager);
//            sm.synchSendOnHold(false, false, this, null);
        }
        myAdapter = new HoldsCursorAdapter(activity, myCursor, CursorAdapter.NO_SELECTION);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos,
                                    long count) {
                selectedPos = pos;
                openPrintOnHold();
            }
        });
        listView.setAdapter(myAdapter);
        hasBeenCreated = true;
        if (myPref.isPollingHoldsEnable() && !PollingNotificationService.isServiceRunning(this)) {
            PollingNotificationService.start(this, PollingNotificationService.PollingServicesFlag.ONHOLDS.getCode() | PollingNotificationService.PollingServicesFlag.DINING_TABLES.getCode());
        }
        if(myPref.isUse_syncplus_services()){
            new RefreshHolds().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.refreshHolds: {
                if (NetworkUtils.isConnectedToInternet(this)) {
                    new RefreshHolds().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    Global.showPrompt(this, R.string.dlog_title_error, getString(R.string.dlog_msg_no_internet_access));
                }
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        myCursor.close();
        super.onDestroy();
        unregisterReceiver(messageReceiver);
    }

    @Override
    public void onResume() {
        if (global.isApplicationSentToBackground())
            Global.loggedIn = false;
        global.stopActivityTransitionTimer();
        if (hasBeenCreated && !Global.loggedIn) {
            if (global.getGlobalDlog() != null)
                global.getGlobalDlog().dismiss();
            global.promptForMandatoryLogin(activity);
        }
        registerReceiver(messageReceiver, new IntentFilter(MainMenu_FA.NOTIFICATION_RECEIVED));
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
//        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
//        boolean isScreenOn = powerManager.isScreenOn();
//        if (!isScreenOn && myPref.isExpireUserSession())
//            Global.loggedIn = false;
        global.startActivityTransitionTimer();
    }

    public void printOnHoldTransaction() {
        new printAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void showPrintDlg() {
        final Dialog dlog = new Dialog(this, R.style.Theme_TransparentTest);
        dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dlog.setCancelable(true);
        dlog.setCanceledOnTouchOutside(true);
        dlog.setContentView(R.layout.dlog_btn_left_right_layout);

        TextView viewTitle = dlog.findViewById(R.id.dlogTitle);
        TextView viewMsg = dlog.findViewById(R.id.dlogMessage);

        viewTitle.setText(R.string.dlog_title_error);
        viewMsg.setText(R.string.dlog_msg_failed_print);

        Button btnYes = dlog.findViewById(R.id.btnDlogLeft);
        Button btnNo = dlog.findViewById(R.id.btnDlogRight);
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

    private void claimedTransactionPrompt(boolean isInternetConnected) {

        final Dialog dlog = new Dialog(activity, R.style.Theme_TransparentTest);
        dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dlog.setCancelable(true);
        dlog.setCanceledOnTouchOutside(true);
        dlog.setContentView(R.layout.dlog_btn_left_right_layout);

        TextView viewTitle = dlog.findViewById(R.id.dlogTitle);
        TextView viewMsg = dlog.findViewById(R.id.dlogMessage);
        viewTitle.setText(isInternetConnected ? R.string.dlog_title_claimed_hold : R.string.dlog_msg_no_internet_access);
        viewMsg.setText(R.string.dlog_msg_claimed_hold);
        Button btnOpen = dlog.findViewById(R.id.btnDlogLeft);
        Button btnCancel = dlog.findViewById(R.id.btnDlogRight);
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

        final EditText viewField = globalDlog.findViewById(R.id.dlogFieldSingle);
        viewField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        TextView viewTitle = globalDlog.findViewById(R.id.dlogTitle);
        TextView viewMsg = globalDlog.findViewById(R.id.dlogMessage);
        viewTitle.setText(R.string.dlog_title_confirm);
        if (!validPassword)
            viewMsg.setText(R.string.invalid_password);
        else
            viewMsg.setText(R.string.dlog_title_enter_manager_password);
        Button btnCancel = globalDlog.findViewById(R.id.btnCancelDlogSingle);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                globalDlog.dismiss();
            }
        });

        Button btnOk = globalDlog.findViewById(R.id.btnDlogSingle);
        btnOk.setText(R.string.button_ok);
        btnOk.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                globalDlog.dismiss();
                String value = viewField.getText().toString();
                if (myPref.loginManager(value)) // validate manager password
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
        myCursor.moveToPosition(selectedPos);
        TextView viewTitle = dlog.findViewById(R.id.dlogTitle);
        TextView viewMsg = dlog.findViewById(R.id.dlogMessage);
        viewTitle.setText(R.string.dlog_title_choose_action);
        viewMsg.setVisibility(View.GONE);
        Button btnOpen = dlog.findViewById(R.id.btnDlogLeft);
        Button btnPrint = dlog.findViewById(R.id.btnDlogRight);
        Button btnCancel = dlog.findViewById(R.id.btnDlogCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlog.dismiss();
            }
        });
        btnOpen.setText(R.string.button_open);
        btnPrint.setText(R.string.button_print);
        OrdersHandler ordersHandler = new OrdersHandler(this);
        final Order order = ordersHandler.getOrder(myCursor.getString(myCursor.getColumnIndex("ord_id")));
        btnOpen.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dlog.dismiss();
                if (myPref.isUseClerks() && !TextUtils.isEmpty(order.assignedTable)) {
//                    Clerk associate = ClerkDAO.getByEmpId(Integer.parseInt(myPref.getClerkID()), true);
                    boolean hasTable = ClerkDAO.hasAssignedDinningTable(Integer.parseInt(myPref.getClerkID()), order.assignedTable);
//                    long count = associate == null ? 0 : associate.getAssignedDinningTables()
//                            .where()
//                            .equalTo("number", myCursor.getString(myCursor.getColumnIndex("assignedTable"))).count();
                    if (hasTable) {
                        validPassword = true;
                        new checkHoldStatus().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } else {
                        final Dialog popDlog = new Dialog(OnHoldActivity.this, R.style.TransparentDialogFullScreen);
                        popDlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        popDlog.setCancelable(true);
                        popDlog.setCanceledOnTouchOutside(false);
                        popDlog.setContentView(R.layout.dlog_field_single_layout);
                        final EditText viewField = popDlog.findViewById(R.id.dlogFieldSingle);
                        viewField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        TextView viewTitle = popDlog.findViewById(R.id.dlogTitle);
                        final TextView viewMsg = popDlog.findViewById(R.id.dlogMessage);
                        viewTitle.setText(R.string.dlog_title_enter_manager_password);
                        if (!validPassword)
                            viewMsg.setText(R.string.invalid_password);
                        else
                            viewMsg.setText(R.string.enter_password);
                        Button btnOk = popDlog.findViewById(R.id.btnDlogSingle);
                        Button btnCancel = popDlog.findViewById(R.id.btnCancelDlogSingle);

                        btnOk.setText(R.string.button_ok);
                        btnOk.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                String enteredPass = viewField.getText().toString().trim();
                                enteredPass = TextUtils.isEmpty(enteredPass) ? "0" : enteredPass;
                                if (myPref.loginManager(enteredPass)) // validate manager password
                                {
                                    popDlog.dismiss();
                                    validPassword = true;
                                    isUpdateOnHold = true;
                                    new checkHoldStatus().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                } else {
                                    validPassword = false;
                                    viewMsg.setText(R.string.invalid_password);
                                }
                            }
                        });
                        btnCancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                popDlog.dismiss();
                            }
                        });
                        popDlog.show();
                    }
                } else {
                    new checkHoldStatus().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
        });
        btnPrint.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dlog.dismiss();
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

    private class RefreshHolds extends AsyncTask<Void, Void, Void> {
        private ProgressDialog myProgressDialog;

        @Override
        protected void onPreExecute() {
            myProgressDialog = new ProgressDialog(activity);
            myProgressDialog.setMessage("Loading...");
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.setCancelable(true);
            myProgressDialog.setCanceledOnTouchOutside(true);
            myProgressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (myPref.isPollingHoldsEnable() && !PollingNotificationService.isServiceRunning(OnHoldActivity.this)) {
                    PollingNotificationService.start(OnHoldActivity.this, PollingNotificationService.PollingServicesFlag.ONHOLDS.getCode() | PollingNotificationService.PollingServicesFlag.DINING_TABLES.getCode());
                }
                SynchMethods.synchOrdersOnHoldList(OnHoldActivity.this);
                Intent intent = new Intent(MainMenu_FA.NOTIFICATION_RECEIVED);
                intent.putExtra(MainMenu_FA.NOTIFICATION_MESSAGE, String.valueOf(NotificationEvent.NotificationEventAction.SYNC_HOLDS.getCode()));
                sendBroadcast(intent);
                Log.d("NotificationHandler", "sendBroadcast");
            } catch (SAXException e) {
                e.printStackTrace();
                Crashlytics.logException(e);
            } catch (IOException e) {
                e.printStackTrace();
                Crashlytics.logException(e);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                Crashlytics.logException(e);
            } catch (KeyManagementException e) {
                e.printStackTrace();
                Crashlytics.logException(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (myProgressDialog != null && myProgressDialog.isShowing()) {
                myProgressDialog.dismiss();
            }
        }
    }

    public class checkHoldStatus extends AsyncTask<Void, String, Boolean> {
        boolean wasProcessed = false;
        private ProgressDialog myProgressDialog;

        @Override
        protected void onPreExecute() {
            myProgressDialog = new ProgressDialog(activity);
            myProgressDialog.setMessage(getString(R.string.loading));
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.setCancelable(false);
            myProgressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
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
                    e.printStackTrace();
                    Crashlytics.logException(e);
                }
                return true;
            } else {
                OrdersHandler ordersHandler = new OrdersHandler(activity);
                if (ordersHandler.isOrderOffline(ordID) || validPassword)
                    wasProcessed = true;
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean isConnectedInternet) {
            myProgressDialog.dismiss();

            if (wasProcessed) {
                new executeOnHoldAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, false);
            } else {
                claimedTransactionPrompt(isConnectedInternet);
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
            Global.lastOrdID = order.ord_id;
            Global.taxID = order.tax_id;
            orderType = Global.OrderType.getByCode(Integer.parseInt(order.ord_type));
            String ord_HoldName = order.ord_HoldName;
            selectCustomer(order.cust_id);
            forPrinting = params[0];
            if (!forPrinting) {
                intent = new Intent(activity, OrderingMain_FA.class);
                String assignedTable = order.assignedTable;
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
                    dbManager.synchDownloadOnHoldDetails(intent, myCursor.getString(myCursor.getColumnIndex("ord_id")), 0, activity);
                else
                    dbManager.synchDownloadOnHoldDetails(intent, myCursor.getString(myCursor.getColumnIndex("ord_id")), 1, activity);
            } else {
                Cursor c = orderProdHandler.getOrderProductsOnHold(myCursor.getString(myCursor.getColumnIndex("ord_id")));
                int size = c.getCount();
                if (size > 0) {
                    if (!forPrinting) {
//                        addOrderProducts(OnHoldActivity.this, c);
                        startActivityForResult(intent, 0);
                        activity.finish();
                    } else {
                        DBManager dbManager = new DBManager(activity);
                        dbManager.synchDownloadOnHoldDetails(intent, myCursor.getString(myCursor.getColumnIndex("ord_id")), 1, activity);
                    }
                } else
                    Toast.makeText(activity, "no available items", Toast.LENGTH_LONG).show();
            }
        }
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

    private class HoldsCursorAdapter extends CursorAdapter {
        private LayoutInflater inflater;

        HoldsCursorAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);

            inflater = LayoutInflater.from(context);
        }

        boolean isRestaurantHold(int position) {
            getCursor().moveToPosition(position);
            return !TextUtils.isEmpty(getCursor().getString(getCursor().getColumnIndex("assignedTable")));
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            final ViewHolder myHolder = (ViewHolder) view.getTag();
            myHolder.holdID.setText(cursor.getString(myHolder.i_holdID));
            myHolder.holdName.setText(cursor.getString(myHolder.i_holdName));
            if (cursor.getString(myHolder.i_issync).equals("1")) {
                myHolder.offlineFlag.setVisibility(View.GONE);
            } else {
                myHolder.offlineFlag.setVisibility(View.VISIBLE);
            }
            String table = cursor.getString(myHolder.i_assignedTable);
            String seats = cursor.getString(myHolder.i_numberOfSeats);
            String createdTime = cursor.getString(myHolder.i_timeCreated);
            String total = Global.getCurrencyFormat(cursor.getString(myHolder.i_orderTotal));
            Map<TimeUnit, Long> map = DateUtils.computeDiff(DateUtils.getDateStringAsDate(createdTime, DateUtils.DATE_yyyy_MM_ddTHH_mm_ss), new Date());
            String timeOnSite = String.format(Locale.getDefault(), "%02d:%02d", map.get(TimeUnit.HOURS), map.get(TimeUnit.MINUTES));
            if (TextUtils.isEmpty(table)) {
                myHolder.tableTextView.setVisibility(View.INVISIBLE);
                myHolder.guestsNumber.setVisibility(View.INVISIBLE);
                myHolder.timeOnSite.setVisibility(View.INVISIBLE);
                myHolder.orderTotal.setVisibility(View.INVISIBLE);
            } else {
                myHolder.tableTextView.setVisibility(View.VISIBLE);
                myHolder.tableTextView.setVisibility(View.VISIBLE);
                myHolder.guestsNumber.setVisibility(View.VISIBLE);
                myHolder.timeOnSite.setVisibility(View.VISIBLE);
                myHolder.orderTotal.setVisibility(View.VISIBLE);
                myHolder.tableTextView.setText(table);
                myHolder.guestsNumber.setText(seats);
                myHolder.timeOnSite.setText(timeOnSite);
                myHolder.orderTotal.setText(total);
            }
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View retView;
            retView = inflater.inflate(R.layout.onhold_listview_adapter, parent, false);
            ViewHolder holder = new ViewHolder();
            holder.holdID = retView.findViewById(R.id.onHoldID);
            holder.tableTextView = retView.findViewById(R.id.restaurantTabletextView);
            holder.holdName = retView.findViewById(R.id.onHoldName);
            holder.guestsNumber = retView.findViewById(R.id.guestNumbertextView);
            holder.orderTotal = retView.findViewById(R.id.orderTotaltextView);
            holder.timeOnSite = retView.findViewById(R.id.timeOnSitetextView);
            holder.offlineFlag = retView.findViewById(R.id.onHoldOfflineFlag);
            holder.i_holdID = cursor.getColumnIndex("_id");
            holder.i_holdName = cursor.getColumnIndex("ord_HoldName");
            holder.i_issync = cursor.getColumnIndex("ord_issync");
            holder.i_assignedTable = cursor.getColumnIndex("assignedTable");
            holder.i_timeCreated = cursor.getColumnIndex("ord_timecreated");
            holder.i_numberOfSeats = cursor.getColumnIndex("numberOfSeats");
            holder.i_orderTotal = cursor.getColumnIndex("ord_total");

            retView.setTag(holder);
            return retView;
        }

        private class ViewHolder {
            TextView holdID, holdName, offlineFlag, guestsNumber, orderTotal, timeOnSite;
            TextView tableTextView;
            int i_issync, i_holdID, i_holdName, i_assignedTable, i_orderTotal, i_numberOfSeats, i_timeCreated;
        }
    }

    private class SyncOnHolds extends AsyncTask<Void, Void, Boolean> {
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(OnHoldActivity.this);
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            dialog.setMessage(getString(R.string.sync_sending_orders));
            dialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            DBManager dbManager = new DBManager(OnHoldActivity.this);
            SynchMethods sm = new SynchMethods(dbManager);
            return sm.synchSendOnHold(false, false, OnHoldActivity.this, null);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            Global.dismissDialog(OnHoldActivity.this, dialog);
        }
    }
}
