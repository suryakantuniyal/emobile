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

import com.android.dao.ClerkDAO;
import com.android.dao.ShiftDAO;
import com.android.database.CustomersHandler;
import com.android.database.DBManager;
import com.android.database.OrderProductsHandler;
import com.android.database.OrdersHandler;
import com.android.database.SalesTaxCodesHandler;
import com.android.emobilepos.mainmenu.MainMenu_FA;
import com.android.emobilepos.models.orders.Order;
import com.android.emobilepos.models.firebase.NotificationEvent;
import com.android.emobilepos.models.realms.Clerk;
import com.android.emobilepos.models.realms.Shift;
import com.android.emobilepos.ordering.OrderingMain_FA;
import com.android.support.DateUtils;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.NetworkUtils;
import com.android.support.OnHoldsManager;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class OnHoldActivity extends BaseFragmentActivityActionBar {
    private Activity activity;
    private Cursor myCursor;
    private Global global;
    private Global.OrderType orderType = Global.OrderType.SALES_RECEIPT;
    private boolean isUpdateOnHold = false;
    private boolean hasBeenCreated = false;
    private MyPreferences myPref;
    private int selectedPos = 0;
    boolean validPassword = true;
    private HoldsCursorAdapter myAdapter;

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

    }

    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Handler handler = new Handler();
            String eventAction = intent.getStringExtra(MainMenu_FA.NOTIFICATION_MESSAGE);
            NotificationEvent.NotificationEventAction action = NotificationEvent.NotificationEventAction.getNotificationEventByCode(Integer.parseInt(eventAction));
            switch (action) {
                case SYNC_HOLDS:
                    OrdersHandler ordersHandler = new OrdersHandler(activity);
                    myCursor = ordersHandler.getOrderOnHold();
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

    private void askWaiterSignin() {
        if (myPref.isUseClerks()) {
            Shift openShift = ShiftDAO.getOpenShift(Integer.parseInt(myPref.getClerkID()));
            Clerk associate = ClerkDAO.getByEmpId(openShift.getAssigneeId(),true);
            long count = associate == null ? 0 : associate.getAssignedDinningTables().where().equalTo("number", myCursor.getString(myCursor.getColumnIndex("assignedTable"))).count();
            if (associate != null && count > 0) {
                validPassword = true;
                new checkHoldStatus().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                final Dialog popDlog = new Dialog(this, R.style.TransparentDialogFullScreen);
                popDlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                popDlog.setCancelable(true);
                popDlog.setCanceledOnTouchOutside(false);
                popDlog.setContentView(R.layout.dlog_field_single_layout);
                final EditText viewField = (EditText) popDlog.findViewById(R.id.dlogFieldSingle);
                viewField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                TextView viewTitle = (TextView) popDlog.findViewById(R.id.dlogTitle);
                TextView viewMsg = (TextView) popDlog.findViewById(R.id.dlogMessage);
                viewTitle.setText(R.string.dlog_title_enter_manager_password);
                if (!validPassword)
                    viewMsg.setText(R.string.invalid_password);
                else
                    viewMsg.setText(R.string.enter_password);
                Button btnOk = (Button) popDlog.findViewById(R.id.btnDlogSingle);
                Button btnCancel = (Button) popDlog.findViewById(R.id.btnCancelDlogSingle);

                btnOk.setText(R.string.button_ok);
                btnOk.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        popDlog.dismiss();
                        myCursor.moveToPosition(selectedPos);
                        String enteredPass = viewField.getText().toString().trim();
                        enteredPass = TextUtils.isEmpty(enteredPass) ? "0" : enteredPass;
                        if (enteredPass.equals(myPref.getPosManagerPass())) // validate manager password
                        {
                            validPassword = true;
                            isUpdateOnHold = true;
                            new checkHoldStatus().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        } else {
                            validPassword = false;
                            askWaiterSignin();
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
            final Dialog popDlog = new Dialog(this, R.style.TransparentDialogFullScreen);
            popDlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            popDlog.setCancelable(true);
            popDlog.setCanceledOnTouchOutside(false);
            popDlog.setContentView(R.layout.dlog_field_single_layout);
            final EditText viewField = (EditText) popDlog.findViewById(R.id.dlogFieldSingle);
            viewField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            TextView viewTitle = (TextView) popDlog.findViewById(R.id.dlogTitle);
            TextView viewMsg = (TextView) popDlog.findViewById(R.id.dlogMessage);
            viewTitle.setText(R.string.dlog_title_waiter_manager_signin);
            if (!validPassword)
                viewMsg.setText(R.string.invalid_password);
            else
                viewMsg.setText(R.string.enter_password);
            Button btnOk = (Button) popDlog.findViewById(R.id.btnDlogSingle);
            Button btnCancel = (Button) popDlog.findViewById(R.id.btnCancelDlogSingle);

            btnOk.setText(R.string.button_ok);
            btnOk.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    popDlog.dismiss();
                    myCursor.moveToPosition(selectedPos);
                    String enteredPass = viewField.getText().toString().trim();
                    enteredPass = TextUtils.isEmpty(enteredPass) ? "0" : enteredPass;
                    boolean isDigits = org.apache.commons.lang3.math.NumberUtils.isDigits(enteredPass);
                    Clerk salesAssociates = null;
                    if (isDigits) {
                        salesAssociates = ClerkDAO.getByEmpId(Integer.parseInt(enteredPass),true); //SalesAssociateHandler.getSalesAssociate(enteredPass);
                    }
                    long count = salesAssociates == null ? 0 : salesAssociates.getAssignedDinningTables().where().equalTo("number", myCursor.getString(myCursor.getColumnIndex("assignedTable"))).count();
                    if (salesAssociates != null && count > 0) {
                        validPassword = true;
                        new checkHoldStatus().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } else {
                        if (enteredPass.equals(myPref.getPosManagerPass())) // validate manager password
                        {
                            validPassword = true;
                            isUpdateOnHold = true;
                            new checkHoldStatus().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        } else {
                            validPassword = false;
                            askWaiterSignin();
                        }
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
    }

    @Override
    public void onDestroy() {
        myCursor.close();
        super.onDestroy();
        unregisterReceiver(messageReceiver);
    }

    @Override
    public void onResume() {
        if (global.isApplicationSentToBackground(activity))
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
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        boolean isScreenOn = powerManager.isScreenOn();
        if (!isScreenOn)
            Global.loggedIn = false;
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
                if (value.equals(myPref.getPosManagerPass())) // validate manager password
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
                if (myAdapter.isRestaurantHold(selectedPos)) {
                    askWaiterSignin();
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

//    public static void addOrderProducts(Activity activity, Cursor c) {
//        OrderProductsHandler orderProductsHandler = new OrderProductsHandler(activity);
//        c.moveToFirst();
//        List<OrderProduct> orderProducts = orderProductsHandler.getOrderProducts(c.getString(c.getColumnIndex("ord_id")));
//        ProductAddonsHandler prodAddonHandler = new ProductAddonsHandler(activity);
//        ProductsHandler prodHandler = new ProductsHandler(activity);
//        String[] discountInfo;
//        double total;
//        Global global = (Global) activity.getApplication();
////        global.order.setOrderProducts(new ArrayList<OrderProduct>());
//
//        for (OrderProduct ord : orderProducts) {
//            double discAmount = 0;
//            total = (Double.parseDouble(ord.getOrdprod_qty())) * Double.parseDouble(ord.getFinalPrice());
//            discountInfo = prodHandler.getDiscount(ord.getDiscount_id(), ord.getFinalPrice());
//            if (discountInfo != null) {
//                if (discountInfo[1] != null && discountInfo[1].equals("Fixed")) {
//                    ord.setDiscount_is_fixed("1");
//                }
//                if (discountInfo[2] != null) {
//                    discAmount = Double.parseDouble(discountInfo[4]);
//                }
//                if (discountInfo[3] != null) {
//                    ord.setDiscount_is_taxable(discountInfo[3]);
//                }
//                if (discountInfo[4] != null) {
//                    ord.setDisTotal(discountInfo[4]);
//                    discAmount = Double.parseDouble(discountInfo[4]);
//                    ord.setDiscount_value(discountInfo[4]);
//                }
//            }
//            ord.setDisAmount(ord.getDisAmount());
//            ord.setItemTotal(Double.toString(total - discAmount));
//            ord.setItemSubtotal(Double.toString(total));
//            if (ord.isAddon()) {
//                int pos = global.order.getOrderProducts().size();
//                if (pos > 0) {
//                    String[] tempVal = prodAddonHandler.getAddonDetails(ord.getAddon_ordprod_id(), ord.getProd_id());
//                }
//            } else {
//                global.order.getOrderProducts().add(ord);
//            }
//        }
//    }

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

    public class HoldsCursorAdapter extends CursorAdapter {
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
            String timeOnSite = String.format("%02d:%02d", map.get(TimeUnit.HOURS), map.get(TimeUnit.MINUTES));
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
            holder.holdID = (TextView) retView.findViewById(R.id.onHoldID);
            holder.tableTextView = (TextView) retView.findViewById(R.id.restaurantTabletextView);
            holder.holdName = (TextView) retView.findViewById(R.id.onHoldName);
            holder.guestsNumber = (TextView) retView.findViewById(R.id.guestNumbertextView);
            holder.orderTotal = (TextView) retView.findViewById(R.id.orderTotaltextView);
            holder.timeOnSite = (TextView) retView.findViewById(R.id.timeOnSitetextView);
            holder.offlineFlag = (TextView) retView.findViewById(R.id.onHoldOfflineFlag);
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
}
