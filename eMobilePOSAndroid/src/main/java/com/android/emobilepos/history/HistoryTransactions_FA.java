package com.android.emobilepos.history;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.widget.CursorAdapter;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.android.database.CustomersHandler;
import com.android.database.OrdersHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.history.details.HistoryTransactionDetails_FA;
import com.android.support.Global;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;
import com.crashlytics.android.Crashlytics;

public class HistoryTransactions_FA extends BaseFragmentActivityActionBar implements OnTabChangeListener {

    private static final String[] TABS = new String[]{"orders", "returns", "invoices", "estimates", "receipts"};
    private static String[] TABS_TAG;
    private static final int[] TABS_ID = new int[]{R.id.orders_tab, R.id.returns_tab, R.id.invoices_tab,
            R.id.estimates_tab, R.id.receipts_tab};

    private boolean hasBeenCreated = false;
    private Global global;
    private Activity activity;
    private TabHost tabHost;
    private CustomCursorAdapter myAdapter;


    private Cursor myCursor;
    private OrdersHandler ordersHandler;
    private ListView lView;
    private Global.OrderType[] orderTypes = {Global.OrderType.ORDER};

    private boolean isFromCustomers = false;            //will determine if it comes from the Customer Selection display
    private String receivedCustID;                        //cust id received from the Customer Selection window

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_transactions_layout);
        global = (Global) getApplication();
        activity = this;
        tabHost = (TabHost) findViewById(android.R.id.tabhost);
        TextView headTitle = (TextView) findViewById(R.id.transHeaderTitle);
        headTitle.setText(getString(R.string.hist_transac));
        lView = (ListView) findViewById(R.id.listView);
        ordersHandler = new OrdersHandler(this);
        final Bundle extras = getIntent().getExtras();
        if (extras != null)
            isFromCustomers = extras.getBoolean("is_from_customers", false);
        if (isFromCustomers) {
            receivedCustID = extras.getString("cust_id");
        }
        lView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                myCursor.moveToPosition(position);
                String ordID = myCursor.getString(myCursor.getColumnIndex("_id"));
                Intent intent = new Intent(arg0.getContext(), HistoryTransactionDetails_FA.class);
                intent.putExtra("ord_id", ordID);
                intent.putExtra("trans_type", myCursor.getString(myCursor.getColumnIndex("ord_type")));
                startActivityForResult(intent, 0);
            }
        });
        EditText field = (EditText) findViewById(R.id.searchField);
        field.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String text = v.getText().toString().trim();
                    if (!text.isEmpty())
                        performSearch(text);
                    return true;
                }
                return false;
            }
        });
        field.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable arg0) {

            }
            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

            }
            @Override
            public void onTextChanged(CharSequence s, int arg1, int arg2, int arg3) {
                String test = s.toString().trim();
                if (test.isEmpty()) {
                    if (myCursor != null)
                        myCursor.close();
                    ordersHandler = new OrdersHandler(activity);
                    if (isFromCustomers)
                        myCursor = ordersHandler.getReceipts1CustData(orderTypes, receivedCustID);
                    else
                        myCursor = ordersHandler.getReceipts1Data(orderTypes);
                    myAdapter = new CustomCursorAdapter(activity, myCursor, CursorAdapter.NO_SELECTION);
                    lView.setAdapter(myAdapter);
                }
            }
        });

        TABS_TAG = new String[]{getString(R.string.trans_tab_orders), getString(R.string.trans_tab_returns),
                getString(R.string.trans_tab_invoices), getString(R.string.trans_tab_estimates),
                getString(R.string.trans_tab_receipts)};
        initTabs();
        tabHost.setOnTabChangedListener(this);
        tabHost.setCurrentTab(0);
        updateMyTabs(TABS[0], TABS_ID[0]);
        hasBeenCreated = true;
    }


    @Override
    public void onResume() {
        if (global.isApplicationSentToBackground(this))
            global.loggedIn = false;
        global.stopActivityTransitionTimer();
        if (hasBeenCreated && !global.loggedIn) {
            if (global.getGlobalDlog() != null)
                global.getGlobalDlog().dismiss();
            global.promptForMandatoryLogin(this);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 100)
            finish();
    }

    private void initTabs() {
        tabHost.setup();
        int length = TABS.length;
        for (int i = 0; i < length; i++) {
            tabHost.addTab(newTab(TABS[i], TABS_TAG[i], TABS_ID[i]));
        }
    }

    private TabSpec newTab(String tag, String label, int tabView) {
        View indicator = LayoutInflater.from(activity).inflate(R.layout.tabs_layout, (ViewGroup) findViewById(android.R.id.tabs), false);
        TextView tabLabel = (TextView) indicator.findViewById(R.id.tabTitle);
        tabLabel.setText(label);
        TabSpec tabSpec = tabHost.newTabSpec(tag);
        tabSpec.setIndicator(indicator);
        tabSpec.setContent(tabView);
        return tabSpec;
    }

    private void getCursorData(int _tab_id) {
        switch (_tab_id) {
            case R.id.orders_tab:
                orderTypes = new Global.OrderType[]{Global.OrderType.ORDER};
                break;
            case R.id.returns_tab://1 = return, 8 = consignment return
                orderTypes = new Global.OrderType[]{Global.OrderType.RETURN, Global.OrderType.CONSIGNMENT_RETURN}; //"'1','8'";
                break;
            case R.id.invoices_tab: // 2 = invoice, 7 = consignment invoice
                orderTypes = new Global.OrderType[]{Global.OrderType.INVOICE, Global.OrderType.CONSIGNMENT_INVOICE};//"'2','7'";
                break;
            case R.id.estimates_tab:
                orderTypes = new Global.OrderType[]{Global.OrderType.ESTIMATE};//"'3'";
                break;
            case R.id.receipts_tab:
                orderTypes = new Global.OrderType[]{Global.OrderType.SALES_RECEIPT};//"'5'";
                break;
        }
        if (isFromCustomers)
            myCursor = ordersHandler.getReceipts1CustData(orderTypes, receivedCustID);
        else
            myCursor = ordersHandler.getReceipts1Data(orderTypes);

        myAdapter = new CustomCursorAdapter(activity, myCursor, CursorAdapter.NO_SELECTION);
        lView.setAdapter(myAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (myCursor != null)
            myCursor.close();
    }

    private void updateMyTabs(String tabID, int placeHolder) {
        getCursorData(placeHolder);
    }

    public enum Limiters {
        orders, returns, invoices, estimates, receipts;
        public static Limiters toLimit(String str) {
            try {
                return valueOf(str);
            } catch (Exception e) {
                e.printStackTrace();
                Crashlytics.logException(e);
                return null;
            }
        }
    }


    @Override
    public void onTabChanged(String tabID) {
        Limiters value = Limiters.toLimit(tabID);
        if (value != null) {
            switch (value) {
                case orders:
                    updateMyTabs(tabID, TABS_ID[0]);
                    break;
                case returns:
                    updateMyTabs(tabID, TABS_ID[1]);
                    break;
                case invoices:
                    updateMyTabs(tabID, TABS_ID[2]);
                    break;
                case estimates:
                    updateMyTabs(tabID, TABS_ID[3]);
                    break;
                case receipts:
                    updateMyTabs(tabID, TABS_ID[4]);
            }
        }
    }


    public void performSearch(String text) {
        if (myCursor != null)
            myCursor.close();

        if (isFromCustomers)
            myCursor = ordersHandler.getSearchOrder(orderTypes, text, receivedCustID);
        else
            myCursor = ordersHandler.getSearchOrder(orderTypes, text, null);
        myAdapter = new CustomCursorAdapter(this, myCursor, CursorAdapter.NO_SELECTION);
        lView.setAdapter(myAdapter);

    }


    private class CustomCursorAdapter extends CursorAdapter {
        LayoutInflater inflater;
        CustomersHandler custHandler = new CustomersHandler(activity);
        ViewHolder myHolder;
        String temp = "";
        public CustomCursorAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
            inflater = LayoutInflater.from(context);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            myHolder = (ViewHolder) view.getTag();
            myHolder.title.setText(cursor.getString(myHolder.i_ord_id));
            temp = cursor.getString(myHolder.i_cust_id);
            myHolder.clientName.setText(custHandler.getSpecificValue("cust_name", temp));
            temp = Global.formatDoubleStrToCurrency(cursor.getString(myHolder.i_ord_total));
            myHolder.amount.setText(temp);
            if (cursor.getString(myHolder.i_ord_issync).equals("1")) //it is synched
                myHolder.syncIcon.setImageResource(R.drawable.is_sync);
            else
                myHolder.syncIcon.setImageResource(R.drawable.is_not_sync);
            if (cursor.getString(myHolder.i_isVoid).equals("0"))//it is not void_payment
                myHolder.voidText.setVisibility(View.GONE);
            else
                myHolder.voidText.setVisibility(View.VISIBLE);
        }

        public String format(String text) {
            if (TextUtils.isEmpty(text))
                return Global.formatDoubleToCurrency(0.00);
            return Global.getCurrencyFormat(Global.formatNumToLocale(Double.parseDouble(text)));
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View retView = inflater.inflate(R.layout.trans_lvadapter, parent, false);
            ViewHolder holder = new ViewHolder();
            holder.title = (TextView) retView.findViewById(R.id.transLVtitle);
            holder.clientName = (TextView) retView.findViewById(R.id.transLVid);
            holder.amount = (TextView) retView.findViewById(R.id.transLVamount);
            holder.voidText = (TextView) retView.findViewById(R.id.transVoidText);
            holder.syncIcon = (ImageView) retView.findViewById(R.id.transIcon);
            holder.i_ord_id = cursor.getColumnIndex("_id");
            holder.i_cust_id = cursor.getColumnIndex("cust_id");
            holder.i_ord_total = cursor.getColumnIndex("ord_total");
            holder.i_ord_issync = cursor.getColumnIndex("ord_issync");
            holder.i_isVoid = cursor.getColumnIndex("isVoid");
            retView.setTag(holder);
            return retView;
        }

        private class ViewHolder {
            TextView title, clientName, amount, voidText;
            ImageView syncIcon;
            int i_ord_id, i_cust_id, i_ord_total, i_isVoid, i_ord_issync;
        }
    }
}
