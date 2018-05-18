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

import com.android.database.PaymentsHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.history.details.HistoryPaymentDetails_FA;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;
import com.crashlytics.android.Crashlytics;

public class HistoryPayments_FA extends BaseFragmentActivityActionBar implements OnTabChangeListener {
    private static final String[] TABS = new String[]{"cash", "check", "card", "other"};
    private static final int[] TABS_ID = new int[]{R.id.cash_tab, R.id.check_tab, R.id.card_tab, R.id.other_tab};
    private static String[] TABS_TAG;
    private TabHost tabHost;
    private Activity activity;
    private String paymethod_name = "Cash";


    private Cursor myCursor;
    private PaymentsHandler handler;

    private ListView lView;
    private CustomCursorAdapter adapter;

    private boolean isRefunds = false;
    private int currSelectedTab = R.id.cash_tab;
    private boolean hasBeenCreated = false;
    private Global global;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_payment_layout);
        activity = this;

        global = (Global) getApplication();
        isRefunds = getIntent().getExtras().getBoolean("isRefunds", false);
        tabHost = findViewById(android.R.id.tabhost);

        TextView headTitle = findViewById(R.id.transHeaderTitle);

        if (!isRefunds)
            headTitle.setText(getString(R.string.hist_payments));
        else
            headTitle.setText(getString(R.string.hist_refunds));

        lView = findViewById(R.id.listView);
        handler = new PaymentsHandler(this);


        lView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
                Intent intent = new Intent(arg0.getContext(), HistoryPaymentDetails_FA.class);
                intent.putExtra("histpay", true);
                CustomCursorAdapter.ViewHolder myHolder = (CustomCursorAdapter.ViewHolder) view.getTag();
                myCursor.moveToPosition(position);
                String pay_id = myCursor.getString(myCursor.getColumnIndex("_id"));                //pay_id is returned as _id
                intent.putExtra("pay_id", pay_id);
                intent.putExtra("job_id", myCursor.getString(myCursor.getColumnIndex("job_id")));
                intent.putExtra("pay_amount", myCursor.getString(myCursor.getColumnIndex("pay_amount")));
                intent.putExtra("cust_name", myCursor.getString(myCursor.getColumnIndex("cust_name")));
                intent.putExtra("isDeclined", myHolder.isDeclined);
                intent.putExtra("isVoid", myCursor.getString(myHolder.i_isVoid).equalsIgnoreCase("1"));
                intent.putExtra("paymethod_name", paymethod_name);

                startActivity(intent);
            }
        });


        EditText field = findViewById(R.id.searchField);
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
                    getCursorData(currSelectedTab);
                }
            }
        });


        TABS_TAG = new String[]{getString(R.string.pay_tab_cash), getString(R.string.pay_tab_check),
                getString(R.string.pay_tab_card), getString(R.string.pay_tab_other)};


        initTabs();


        tabHost.setOnTabChangedListener(this);
        tabHost.setCurrentTab(0);

        updateMyTabs(TABS[0], TABS_ID[0]);

        hasBeenCreated = true;
    }


    @Override
    public void onResume() {
        getCursorData(currSelectedTab);
        if (global.isApplicationSentToBackground()) {
            Global.loggedIn = false;
        }
        global.stopActivityTransitionTimer();

        if (hasBeenCreated && !Global.loggedIn) {
            if (global.getGlobalDlog() != null)
                global.getGlobalDlog().dismiss();
            global.promptForMandatoryLogin(activity);
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
//        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
//        boolean isScreenOn = powerManager.isScreenOn();
//        MyPreferences myPref = new MyPreferences(this);
//        if (!isScreenOn && myPref.isExpireUserSession())
//            Global.loggedIn = false;
        global.startActivityTransitionTimer();
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

        TextView tabLabel = indicator.findViewById(R.id.tabTitle);

        tabLabel.setText(label);

        TabSpec tabSpec = tabHost.newTabSpec(tag);
        tabSpec.setIndicator(indicator);

        tabSpec.setContent(tabView);

        return tabSpec;
    }


    private void getCursorData(int _tab_id) {
        switch (_tab_id) {
            case R.id.cash_tab:
                paymethod_name = "Cash";
                myCursor = handler.getCashCheckGiftPayment("Cash", isRefunds);
                break;
            case R.id.check_tab:
                paymethod_name = "Check";
                myCursor = handler.getCashCheckGiftPayment("Check", isRefunds);
                break;
            case R.id.card_tab:
                paymethod_name = "Card";
                myCursor = handler.getCardPayments(isRefunds);
                break;
            case R.id.giftcard_tab:
                paymethod_name = "GiftCard";
                myCursor = handler.getCashCheckGiftPayment("GiftCard", isRefunds);
                break;
            case R.id.loyaltycard_tab:
                paymethod_name = "LoyaltyCard";
                myCursor = handler.getLoyaltyPayments();
                break;
            case R.id.rewardcard_tab:
                break;
            case R.id.other_tab:
                paymethod_name = "Other";
                myCursor = handler.getOtherPayments(isRefunds);
                break;
        }
        adapter = new CustomCursorAdapter(activity, myCursor, CursorAdapter.NO_SELECTION);
        lView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void updateMyTabs(String tabID, int placeHolder) {

        currSelectedTab = placeHolder;
        getCursorData(currSelectedTab);
    }

    @Override
    public void onTabChanged(String tabID) {

        Limiters value = Limiters.toLimit(tabID);


        switch (value) {
            case cash:
                updateMyTabs(tabID, TABS_ID[0]);
                break;
            case check:
                updateMyTabs(tabID, TABS_ID[1]);
                break;
            case card:
                updateMyTabs(tabID, TABS_ID[2]);
                break;
            case other:
                updateMyTabs(tabID, TABS_ID[3]);
                break;
        }
    }

    public void performSearch(String text) {
        if (myCursor != null)
            myCursor.close();

        switch (currSelectedTab) {
            case R.id.cash_tab:
                myCursor = handler.searchCashCheckGift("Cash", text);
                break;
            case R.id.check_tab:
                myCursor = handler.searchCashCheckGift("Check", text);
                break;
            case R.id.card_tab:
                myCursor = handler.searchCards(text);
                break;
            case R.id.giftcard_tab:
                myCursor = handler.searchCashCheckGift("GiftCard", text);
                break;
            case R.id.loyaltycard_tab:
                break;
            case R.id.rewardcard_tab:
                break;
            case R.id.other_tab:
                myCursor = handler.searchOther(text);
                break;
        }

		/*if(isFromCustomers)
            myCursor = handler.getSearchOrder(type, text, receivedCustID);
		else
			myCursor = handler.getSearchOrder(type, text, receivedCustID);*/


        adapter = new CustomCursorAdapter(activity, myCursor, CursorAdapter.NO_SELECTION);
        lView.setAdapter(adapter);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (myCursor != null && !myCursor.isClosed()) {
            myCursor.close();
        }
    }

    public enum Limiters {
        cash, check, card, other;

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

    public class CustomCursorAdapter extends CursorAdapter {
        LayoutInflater inflater;
        ViewHolder myHolder;
        String temp = "";

        public CustomCursorAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
            inflater = LayoutInflater.from(context);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            myHolder = (ViewHolder) view.getTag();

            temp = cursor.getString(myHolder.i_cust_name);
            if (temp == null)
                temp = "";
            if (!temp.isEmpty())
                temp = " (" + temp + ")";
            myHolder.title.setText(String.format("%s %s", cursor.getString(myHolder.i_id), temp));

            temp = cursor.getString(myHolder.i_pay_amount);
            if (TextUtils.isEmpty(temp))
                temp = "";
            else
                temp = Global.getCurrencyFormat(temp);

            myHolder.amount.setText(temp);

            if (cursor.getString(myHolder.i_pay_issync).equals("1"))//it is synch
                myHolder.iconImage.setImageResource(R.drawable.is_sync);
            else
                myHolder.iconImage.setImageResource(R.drawable.is_not_sync);

            if (cursor.getString(myHolder.i_isVoid).equals("0"))//is not VOID
                myHolder.voidText.setVisibility(View.INVISIBLE);
            else {
                myHolder.voidText.setVisibility(View.VISIBLE);
                myHolder.voidText.setText(getString(R.string.void_label));
            }

            if (cursor.getColumnIndex("DECLINED") > -1 && cursor.getString(cursor.getColumnIndex("DECLINED")).equalsIgnoreCase("TRUE"))//is DECLINED EMV
            {
                myHolder.voidText.setVisibility(View.VISIBLE);
                myHolder.voidText.setText(getString(R.string.declined));
            }
            myHolder.tip.setText(String.format("(Tip: %s)", Global.getCurrencyFormat(cursor.getString(myHolder.i_pay_tip))));


        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {

            View retView = inflater.inflate(R.layout.histpay_lvadapter, parent, false);

            ViewHolder holder = new ViewHolder();
            holder.title = retView.findViewById(R.id.histpayTitle);
            holder.amount = retView.findViewById(R.id.histpaySubtitle);
            holder.voidText = retView.findViewById(R.id.histpayVoidText);
            holder.iconImage = retView.findViewById(R.id.histpayIcon);
            holder.tip = retView.findViewById(R.id.histpayTipText);

            holder.i_id = cursor.getColumnIndex("_id");
            holder.i_cust_name = cursor.getColumnIndex("cust_name");
            holder.i_pay_amount = cursor.getColumnIndex("pay_amount");
            holder.i_pay_issync = cursor.getColumnIndex("pay_issync");
            holder.i_isVoid = cursor.getColumnIndex("isVoid");
            holder.i_pay_tip = cursor.getColumnIndex("pay_tip");
            if (cursor.getColumnIndex("DECLINED") > -1)
                holder.isDeclined = cursor.getString(cursor.getColumnIndex("DECLINED"));
            else
                holder.isDeclined = "";
            retView.setTag(holder);

            return retView;
        }


        private class ViewHolder {
            TextView title, amount, voidText, tip;

            ImageView iconImage;

            int i_id, i_cust_name, i_pay_amount, i_pay_issync, i_isVoid, i_pay_tip;
            String isDeclined;
        }
    }

}
