package com.android.emobilepos.mainmenu;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.database.ClerksHandler;
import com.android.database.CustomersHandler;
import com.android.database.Locations_DB;
import com.android.database.SalesTaxCodesHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.adapters.DinningTableSeatsAdapter;
import com.android.emobilepos.adapters.DinningTablesAdapter;
import com.android.emobilepos.adapters.SalesMenuAdapter;
import com.android.emobilepos.cardmanager.GiftCard_FA;
import com.android.emobilepos.cardmanager.LoyaltyCard_FA;
import com.android.emobilepos.cardmanager.RewardCard_FA;
import com.android.emobilepos.consignment.ConsignmentMain_FA;
import com.android.emobilepos.customer.ViewCustomers_FA;
import com.android.emobilepos.history.HistoryOpenInvoices_FA;
import com.android.emobilepos.holders.Locations_Holder;
import com.android.emobilepos.locations.LocationsPickerDlog_FR;
import com.android.emobilepos.locations.LocationsPicker_Listener;
import com.android.emobilepos.models.DinningTable;
import com.android.emobilepos.ordering.OrderingMain_FA;
import com.android.emobilepos.payment.SelectPayMethod_FA;
import com.android.database.DBManager;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SalesTab_FR extends Fragment {
    private SalesMenuAdapter myAdapter;
    private GridView myListview;
    private Context thisContext;
    private boolean isCustomerSelected = false;
    private TextView selectedCust;
    private MyPreferences myPref;
    private Button salesInvoices;
    public static Activity activity;
    private EditText hiddenField;
    private DinningTable selectedDinningTable;
    private String selectedSeatsAmount;
//    GridView gridView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.sales_layout, container, false);
        activity = getActivity();
        myPref = new MyPreferences(activity);
        myPref.setLogIn(true);
        PreferenceManager.setDefaultValues(activity, R.xml.settings_admin_layout, false);
        myListview = (GridView) view.findViewById(R.id.salesGridLayout);
//        gridView = (GridView) view.findViewById(R.id.salesGridLayout);

        thisContext = getActivity();
        selectedCust = (TextView) view.findViewById(R.id.salesCustomerName);
        salesInvoices = (Button) view.findViewById(R.id.invoiceButton);
        hiddenField = (EditText) view.findViewById(R.id.hiddenField);
        hiddenField.addTextChangedListener(textWatcher());

        if (isTablet())
            myPref.setIsTablet(true);
        else
            myPref.setIsTablet(false);


        if (myPref.isCustSelected()) {
            isCustomerSelected = true;
            selectedCust.setText(myPref.getCustName());
        } else {
            salesInvoices.setVisibility(View.GONE);
            isCustomerSelected = false;
            selectedCust.setText(getString(R.string.no_customer));
        }
        myAdapter = new SalesMenuAdapter(getActivity(), isCustomerSelected);

        LinearLayout customersBut = (LinearLayout) view.findViewById(R.id.salesCustomerBut);

        customersBut.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(thisContext, ViewCustomers_FA.class);
                startActivityForResult(intent, 0);
            }
        });

        ImageButton clear = (ImageButton) view.findViewById(R.id.clearCustomerBut);
        clear.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                salesInvoices.setVisibility(View.GONE);
                myPref.resetCustInfo(getString(R.string.no_customer));


                isCustomerSelected = false;
                selectedCust.setText(getString(R.string.no_customer));
                myAdapter = new SalesMenuAdapter(getActivity(), false);
                myListview.setAdapter(myAdapter);
                myListview.setOnItemClickListener(new MyListener());

            }
        });


        salesInvoices.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (myPref.isCustSelected()) {
                    Intent intent = new Intent(getActivity(), HistoryOpenInvoices_FA.class);
                    intent.putExtra("isFromMainMenu", true);
                    startActivity(intent);
                }
            }
        });

        return view;

    }


    @Override
    public void onResume() {


        if (myPref.isCustSelected()) {
            isCustomerSelected = true;
            selectedCust.setText(myPref.getCustName());
            myAdapter = new SalesMenuAdapter(getActivity(), true);
            myListview.setAdapter(myAdapter);
            myListview.setOnItemClickListener(new MyListener());
            myListview.invalidateViews();
        } else {
            isCustomerSelected = false;
            selectedCust.setText(getString(R.string.no_customer));
            myAdapter = new SalesMenuAdapter(getActivity(), false);
            myListview.setAdapter(myAdapter);
            myListview.setOnItemClickListener(new MyListener());
        }

        super.onResume();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == 1) {

            salesInvoices.setVisibility(View.VISIBLE);
            Bundle extras = data.getExtras();
            selectedCust.setText(extras.getString("customer_name"));


            myPref.setCustName(extras.getString("customer_name"));
            myPref.setCustSelected(true);

            isCustomerSelected = true;
            myAdapter = new SalesMenuAdapter(getActivity(), true);
            myListview.setAdapter(myAdapter);

            myListview.setOnItemClickListener(new MyListener());

        } else if (resultCode == 2) {
            salesInvoices.setVisibility(View.GONE);
            isCustomerSelected = false;
            selectedCust.setText(getString(R.string.no_customer));
            myAdapter = new SalesMenuAdapter(getActivity(), false);
            myListview.setAdapter(myAdapter);
            myListview.setOnItemClickListener(new MyListener());

        }
    }


    public class MyListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
            // TODO Auto-generated method stub

            final int adapterPos = (Integer) myAdapter.getItem(position);

            if (myPref.getPreferences(MyPreferences.pref_use_clerks)) {
                promptClerkPassword(adapterPos);
            } else if (myPref.getPreferences(MyPreferences.pref_require_shift_transactions) && myPref.getShiftIsOpen()) {
                Global.showPrompt(activity, R.string.dlog_title_error, getString(R.string.dlog_msg_error_shift_needs_to_be_open));
            } else {
                performListViewClick(adapterPos);
            }
        }
    }

    public static void startDefault(Activity activity, String type) {
        if (activity != null) {
            int transType;
            try {
                if (type == null || type.isEmpty())
                    type = "-1";
                transType = Integer.parseInt(type);
            } catch (NumberFormatException e) {
                transType = -1;
            }

            Intent intent;
            if (transType != -1) {
                switch (transType) {
                    case 0:
                        intent = new Intent(activity, OrderingMain_FA.class);
                        intent.putExtra("option_number", Global.TransactionType.SALE_RECEIPT);
                        activity.startActivityForResult(intent, 0);
                        break;
                    case 2:
                        intent = new Intent(activity, OrderingMain_FA.class);
                        intent.putExtra("option_number", Global.TransactionType.RETURN);
                        activity.startActivityForResult(intent, 0);
                        break;
                }
            }
        }

    }


    private void performListViewClick(final int pos) {
        Global global = (Global) activity.getApplication();
        global.resetOrderDetailsValues();
        global.clearListViewData();
        Intent intent;
        if (isCustomerSelected) // customer is currently selected
        {
            switch (Global.TransactionType.getByCode(pos)) {
                case SALE_RECEIPT: // Sales Receipt
                {
                    if (myPref.getPreferences(MyPreferences.pref_require_customer)) {
                        if (myPref.getPreferences(MyPreferences.pref_restaurant_mode) &&
                                myPref.getPreferences(MyPreferences.pref_enable_togo_eatin)) {
                            askEatInToGo();
                        } else {
                            intent = new Intent(activity, OrderingMain_FA.class);
                            intent.putExtra("option_number", Global.TransactionType.SALE_RECEIPT);
                            startActivityForResult(intent, 0);
                        }

                    } else {
                        promptWithCustomer();
                    }
                    break;
                }
                case ORDERS: // Orders
                {
                    //EasyTracker.getTracker().sendEvent("ui_action", "button_press", "Orders", null);
                    intent = new Intent(activity, OrderingMain_FA.class);
                    //intent = new Intent(activity, SalesReceiptSplitActivity.class);
                    intent.putExtra("option_number", Global.TransactionType.ORDERS);
                    startActivityForResult(intent, 0);

                    break;
                }
                case RETURN: // Return
                {
                    //EasyTracker.getTracker().sendEvent("ui_action", "button_press", "Return", null);
                    intent = new Intent(activity, OrderingMain_FA.class);
                    intent.putExtra("option_number", Global.TransactionType.RETURN);
                    startActivityForResult(intent, 0);
                    break;
                }
                case INVOICE: // Invoice
                {
                    //EasyTracker.getTracker().sendEvent("ui_action", "button_press", "Invoice", null);
                    intent = new Intent(activity, OrderingMain_FA.class);
                    //intent = new Intent(activity, SalesReceiptSplitActivity.class);

                    intent.putExtra("option_number", Global.TransactionType.INVOICE);
                    startActivityForResult(intent, 0);
                    break;
                }
                case ESTIMATE: // Estimate
                {
                    //EasyTracker.getTracker().sendEvent("ui_action", "button_press", "Estimate", null);
                    intent = new Intent(activity, OrderingMain_FA.class);
                    //intent = new Intent(activity, SalesReceiptSplitActivity.class);
                    intent.putExtra("option_number", Global.TransactionType.ESTIMATE);
                    startActivityForResult(intent, 0);
                    break;
                }
                case PAYMENT: // Payment
                {
                    //EasyTracker.getTracker().sendEvent("ui_action", "button_press", "Payment", null);
                    intent = new Intent(activity, SelectPayMethod_FA.class);
                    intent.putExtra("salespayment", true);
                    intent.putExtra("amount", "0.00");
                    intent.putExtra("paid", "0.00");
                    intent.putExtra("isFromMainMenu", true);

                    if (isCustomerSelected) {
                        intent.putExtra("cust_id", myPref.getCustID());
                        intent.putExtra("custidkey", myPref.getCustIDKey());
                    }

                    startActivity(intent);
                    break;
                }
                case GIFT_CARD:    //Gift Card
                    intent = new Intent(activity, GiftCard_FA.class);
                    startActivity(intent);
                    break;
                case LOYALTY_CARD:    //Loyalty Card
                    intent = new Intent(activity, LoyaltyCard_FA.class);
                    startActivity(intent);
                    break;
                case REWARD_CARD:    //Reward Card
                    intent = new Intent(activity, RewardCard_FA.class);
                    startActivity(intent);
                    break;
                case REFUND: // Refund
                {
                    //EasyTracker.getTracker().sendEvent("ui_action", "button_press", "Refund", null);
                    intent = new Intent(activity, SelectPayMethod_FA.class);
                    intent.putExtra("salesrefund", true);
                    intent.putExtra("amount", "0.00");
                    intent.putExtra("paid", "0.00");
                    intent.putExtra("isFromMainMenu", true);

                    if (myPref.isCustSelected()) {
                        intent.putExtra("cust_id", myPref.getCustID());
                        intent.putExtra("custidkey", myPref.getCustIDKey());
                    }

                    startActivity(intent);
                    break;
                }
                case ROUTE:                //Route
                {
                    break;
                }
                case ON_HOLD:            //On Hold
                {
                    DBManager dbManager = new DBManager(activity);
                    dbManager.synchSendOrdersOnHold(true, false);
                    break;
                }
                case CONSIGNMENT:                //Consignment
                {
                    intent = new Intent(activity, ConsignmentMain_FA.class);
                    startActivity(intent);
                    break;
                }
                case LOCATION:
                    pickLocations(true);
                    break;
            }

        } else {
            switch (pos) {
                case 0: // Sales Receipt
                {
                    if (myPref.getPreferences(MyPreferences.pref_require_customer)) {
                        Global.showPrompt(activity, R.string.dlog_title_error, activity.getString(R.string.dlog_msg_select_customer));
                    } else {
                        if (myPref.getPreferences(MyPreferences.pref_restaurant_mode) &&
                                myPref.getPreferences(MyPreferences.pref_enable_togo_eatin)) {
                            askEatInToGo();
                        } else {
                            intent = new Intent(activity, OrderingMain_FA.class);
                            intent.putExtra("option_number", Global.TransactionType.SALE_RECEIPT);
                            startActivityForResult(intent, 0);
                        }
                    }

                    break;
                }
                case 2: // Return
                {
                    if (myPref.getPreferences(MyPreferences.pref_require_customer)) {
                        Global.showPrompt(activity, R.string.dlog_title_error, activity.getString(R.string.dlog_msg_select_customer));
                    } else {
                        intent = new Intent(activity, OrderingMain_FA.class);
                        intent.putExtra("option_number", Global.TransactionType.RETURN);
                        startActivityForResult(intent, 0);
                    }
                    break;
                }
                case 5: // Payment
                {

                    if (myPref.getPreferences(MyPreferences.pref_require_customer)) {
                        Global.showPrompt(activity, R.string.dlog_title_error, activity.getString(R.string.dlog_msg_select_customer));
                    } else {
                        intent = new Intent(activity, SelectPayMethod_FA.class);
                        intent.putExtra("salespayment", true);
                        intent.putExtra("amount", "0.00");
                        intent.putExtra("paid", "0.00");
                        intent.putExtra("isFromMainMenu", true);

                        startActivity(intent);
                    }
                    break;
                }
                case 6:        //Gift Card
                    intent = new Intent(activity, GiftCard_FA.class);
                    startActivity(intent);
                    break;
                case 7:    //Loyalty Card
                    intent = new Intent(activity, LoyaltyCard_FA.class);
                    startActivity(intent);
                    break;
                case 8:    //Reward Card
                    intent = new Intent(activity, RewardCard_FA.class);
                    startActivity(intent);
                    break;
                case 9: // Refund
                {
                    intent = new Intent(activity, SelectPayMethod_FA.class);
                    intent.putExtra("salesrefund", true);
                    intent.putExtra("amount", "0.00");
                    intent.putExtra("paid", "0.00");
                    intent.putExtra("isFromMainMenu", true);
                    if (myPref.isCustSelected()) {
                        intent.putExtra("cust_id", myPref.getCustID());
                        intent.putExtra("custidkey", myPref.getCustIDKey());
                    }
                    startActivity(intent);
                    break;
                }
                case 11://on Hold
                    DBManager dbManager = new DBManager(activity);
                    dbManager.synchSendOrdersOnHold(true, false);
                    break;
                case 13:
                    pickLocations(true);
                    break;
            }
        }
    }

    private void askEatInToGo() {
        final Dialog popDlog = new Dialog(activity, R.style.TransparentDialog);
        popDlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        popDlog.setCancelable(true);
        popDlog.setCanceledOnTouchOutside(true);
        popDlog.setContentView(R.layout.dlog_ask_togo_eatin_layout);
        Button toGoBtn = (Button) popDlog.findViewById(R.id.toGobutton);
        Button eatInBtn = (Button) popDlog.findViewById(R.id.eatInbutton);
        toGoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popDlog.dismiss();
                Intent intent = new Intent(activity, OrderingMain_FA.class);
                intent.putExtra("option_number", Global.TransactionType.SALE_RECEIPT);
                intent.putExtra("RestaurantSaleType", Global.RestaurantSaleType.TO_GO);
                startActivityForResult(intent, 0);
            }
        });
        eatInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popDlog.dismiss();
                if (myPref.getPreferences(MyPreferences.pref_enable_table_selection)) {
                    selectDinnerTable();
                } else {
                    selectSeatAmount();
                }

            }
        });
        popDlog.show();
    }

    private void selectSeatAmount() {
        final String[] seats = this.getResources().getStringArray(R.array.dinningTableSeatsArray);
        final Dialog popDlog = new Dialog(getActivity(), R.style.TransparentDialogFullScreen);
        popDlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        popDlog.setCancelable(false);
        popDlog.setCanceledOnTouchOutside(false);
        popDlog.setContentView(R.layout.dlog_ask_table_number_layout);
        GridView gridView = (GridView) popDlog.findViewById(R.id.tablesGridLayout);
        final DinningTableSeatsAdapter adapter = new DinningTableSeatsAdapter(getActivity(), seats);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedSeatsAmount = seats[position];
                popDlog.dismiss();
                startSaleRceipt(Global.RestaurantSaleType.EAT_IN, selectedSeatsAmount, selectedDinningTable.getNumber());
//                Intent intent = new Intent(activity, OrderingMain_FA.class);
//                intent.putExtra("option_number", Global.TransactionType.SALE_RECEIPT);
//                intent.putExtra("RestaurantSaleType", Global.RestaurantSaleType.EAT_IN);
//                intent.putExtra("selectedSeatsAmount", selectedSeatsAmount);
//                intent.putExtra("selectedDinningTableNumber", selectedDinningTable.getNumber());
//                startActivityForResult(intent, 0);
            }
        });
        popDlog.show();
    }

    public void selectDinnerTable() {
        Gson gson = new Gson();
        Type listType = new TypeToken<ArrayList<DinningTable>>() {
        }.getType();
        final List<DinningTable> dinningTables = gson.fromJson(this.getResources().getString(R.string.dinningTables), listType);
        final Dialog popDlog = new Dialog(getActivity(), R.style.TransparentDialogFullScreen);
        popDlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        popDlog.setCancelable(false);
        popDlog.setCanceledOnTouchOutside(false);
        popDlog.setContentView(R.layout.dlog_ask_table_number_layout);
        GridView gridView = (GridView) popDlog.findViewById(R.id.tablesGridLayout);
        final DinningTablesAdapter adapter = new DinningTablesAdapter(getActivity(), dinningTables);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedDinningTable = dinningTables.get(position);
                popDlog.dismiss();
                selectSeatAmount();
            }
        });
        popDlog.show();
    }

    private void pickLocations(final boolean showOrigin) {
        LocationsPickerDlog_FR picker = new LocationsPickerDlog_FR();
        Bundle args = new Bundle();
        args.putBoolean("showOrigin", showOrigin);
        picker.setArguments(args);
        final DialogFragment newFrag = picker;
        picker.setListener(new LocationsPicker_Listener() {
            @Override
            public void onSelectLocation(Locations_Holder location) {
                // TODO Auto-generated method stub
                newFrag.dismiss();
                if (showOrigin) {
                    Global.locationFrom = location;
                    pickLocations(false);
                } else {
                    Global.locationTo = location;
                    confirmSelectedLocations();
                }
            }
        });
        newFrag.show(this.getFragmentManager(), "dialog");
    }

    /* if update information is needed on layout */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Activity activity = getActivity();

        if (activity != null) {
            myAdapter = new SalesMenuAdapter(activity, isCustomerSelected);

            if (myListview != null) {
                myListview.setAdapter(myAdapter);
                myListview.setOnItemClickListener(new MyListener());
            }

            if (myPref.isSam4s(true, true) || myPref.isPAT100()) {
                Global.showCDTDefault(activity);
            }
        }
    }

    private void confirmSelectedLocations() {
        final Dialog globalDlog = new Dialog(activity, R.style.Theme_TransparentTest);
        globalDlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        globalDlog.setCancelable(true);
        globalDlog.setCanceledOnTouchOutside(false);
        globalDlog.setContentView(R.layout.dlog_btn_left_right_layout);


        TextView viewTitle = (TextView) globalDlog.findViewById(R.id.dlogTitle);
        TextView viewMsg = (TextView) globalDlog.findViewById(R.id.dlogMessage);
        viewTitle.setText(R.string.dlog_title_confirm);

        String sb = "Locations selected for transfer:\n" +
                "From: " + Global.locationFrom.get(Locations_DB.loc_name) + "\n" +
                "To: " + Global.locationTo.get(Locations_DB.loc_name);
        viewMsg.setText(sb);
        globalDlog.findViewById(R.id.btnDlogCancel).setVisibility(View.GONE);

        Button btnOk = (Button) globalDlog.findViewById(R.id.btnDlogLeft);
        btnOk.setText(R.string.button_ok);
        Button btnCancel = (Button) globalDlog.findViewById(R.id.btnDlogRight);
        btnCancel.setText(R.string.button_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                globalDlog.dismiss();
            }
        });
        btnOk.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                globalDlog.dismiss();
                Global.isInventoryTransfer = true;
                Intent intent = new Intent(activity, OrderingMain_FA.class);
                intent.putExtra("option_number", Global.TransactionType.LOCATION);
                startActivityForResult(intent, 0);
            }
        });
        globalDlog.show();
    }

    private void promptClerkPassword(final int adapterPos) {

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

        viewMsg.setText(R.string.dlog_title_enter_clerk_password);
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
                // TODO Auto-generated method stub
                globalDlog.dismiss();
                String enteredPass = viewField.getText().toString().trim();
                ClerksHandler clerkHandler = new ClerksHandler(activity);
                String[] clerkData = clerkHandler.getClerkID(enteredPass);
                if (clerkData != null && !clerkData[0].isEmpty()) {
                    myPref.setClerkID(clerkData[0]);
                    myPref.setClerkName(clerkData[1]);
                    performListViewClick(adapterPos);
                } else
                    promptClerkPassword(adapterPos);
            }
        });
        globalDlog.show();
    }

    private void startSaleRceipt(Global.RestaurantSaleType restaurantSaleType, String selectedSeatsAmount, int tableNumber) {
        Intent intent = new Intent(activity, OrderingMain_FA.class);
        intent.putExtra("option_number", Global.TransactionType.SALE_RECEIPT);
        intent.putExtra("RestaurantSaleType", restaurantSaleType);
        if (restaurantSaleType == Global.RestaurantSaleType.EAT_IN) {
            intent.putExtra("selectedSeatsAmount", selectedSeatsAmount);
            intent.putExtra("selectedDinningTableNumber", tableNumber);
        }
        startActivityForResult(intent, 0);
    }

    private void promptWithCustomer() {
        //final Intent intent = new Intent(activity, SalesReceiptSplitActivity.class);
        final Intent intent = new Intent(activity, OrderingMain_FA.class);
        final Dialog dialog = new Dialog(activity, R.style.Theme_TransparentTest);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(R.layout.pre_dialog_layout);
        Button withCust = (Button) dialog.findViewById(R.id.withCustBut);
        Button withoutCust = (Button) dialog.findViewById(R.id.withOutCustBut);
        Button cancel = (Button) dialog.findViewById(R.id.cancelPredialogBut);

        withCust.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (myPref.getPreferences(MyPreferences.pref_restaurant_mode) &&
                        myPref.getPreferences(MyPreferences.pref_enable_togo_eatin)) {
                    askEatInToGo();
                }
                dialog.dismiss();
            }
        });
        withoutCust.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                salesInvoices.setVisibility(View.GONE);
                intent.putExtra("option_number", Global.TransactionType.RETURN);
                myPref.resetCustInfo(getString(R.string.no_customer));

                isCustomerSelected = false;

                startActivityForResult(intent, 0);
                dialog.dismiss();
            }
        });

        cancel.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private boolean isTablet() {

        String model = Build.MODEL;
        if (model.equals("ET1")) {
            myPref.isET1(false, true);
            return true;
        } else if (model.equals("MC40N0")) {
            myPref.isMC40(false, true);
            return false;
        } else if (model.equals("M2MX60P") || model.equals("M2MX6OP")) {
            myPref.isSam4s(false, true);
            return true;
        } else if (model.equals("JE971")) {
            return true;
        } else if (model.equals("Asura")) {
            myPref.isAsura(false, true);
            return true;
        } else if (model.equals("Dolphin Black 70e")) {
            myPref.isDolphin(false, true);
            return false;
        } else if (model.equals("PAT100")) {
            myPref.setIsPAT100(true);
            return true;
        } else if (model.equals("EM100")) {
            myPref.setIsEM100(true);
            return true;
        } else if (model.equals("EM70")) {
            myPref.setIsEM70(true);
            return true;
        } else if (model.equals("OT-310")) {
            myPref.setIsOT310(true);
            return true;
        } else if (model.equals("PayPoint ESY13P1")) {
            myPref.setIsESY13P1(true);
            return true;
        } else {
            return (activity.getResources().getConfiguration().screenLayout
                    & Configuration.SCREENLAYOUT_SIZE_MASK)
                    >= Configuration.SCREENLAYOUT_SIZE_LARGE;
        }
    }

    private TextWatcher textWatcher() {

        TextWatcher tw = new TextWatcher() {
            private boolean doneScanning = false;
            private String val = "";
            private CustomersHandler custHandler = new CustomersHandler(activity);
            private HashMap<String, String> map = new HashMap<String, String>();

            @Override
            public void afterTextChanged(Editable s) {
                if (doneScanning) {
                    doneScanning = false;
                    hiddenField.setText("");
                    map = custHandler.getCustomerInfo(val.replace("\n", "").trim());

                    if (map.size() > 0) {
                        SalesTaxCodesHandler taxHandler = new SalesTaxCodesHandler(activity);
                        if (taxHandler.checkIfCustTaxable(map.get("cust_taxable")))
                            myPref.setCustTaxCode(map.get("cust_salestaxcode"));
                        else
                            myPref.setCustTaxCode("");

                        myPref.setCustID(map.get("cust_id"));    //getting cust_id as _id
                        myPref.setCustName(map.get("cust_name"));
                        myPref.setCustIDKey(map.get("custidkey"));
                        myPref.setCustSelected(true);

                        myPref.setCustPriceLevel(map.get("pricelevel_id"));

                        myPref.setCustEmail(map.get("cust_email"));


                        selectedCust.setText(map.get("cust_name"));

                        salesInvoices.setVisibility(View.VISIBLE);
                        isCustomerSelected = true;
                        myAdapter = new SalesMenuAdapter(getActivity(), true);
                        myListview.setAdapter(myAdapter);

                        myListview.setOnItemClickListener(new MyListener());

                    } else {
                        isCustomerSelected = false;
                        myPref.resetCustInfo(getString(R.string.no_customer));
                        myPref.setCustSelected(false);

                        selectedCust.setText(getString(R.string.no_customer));
                        myAdapter = new SalesMenuAdapter(getActivity(), false);
                        myListview.setAdapter(myAdapter);
                        myListview.setOnItemClickListener(new MyListener());
                        salesInvoices.setVisibility(View.GONE);
                    }

                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
                if (s.toString().contains("\n")) {
                    val = s.toString();
                    doneScanning = true;
                }
            }
        };
        return tw;
    }

}
