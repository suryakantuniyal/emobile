package com.android.emobilepos.customer;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.widget.CursorAdapter;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.android.database.CustomersHandler;
import com.android.database.DBManager;
import com.android.database.SalesTaxCodesHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.history.HistoryTransactions_FA;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;

public class ViewCustomers_FA extends BaseFragmentActivityActionBar implements OnClickListener, OnItemClickListener {
    private ListView myListView;

    private Context thisContext = this;
    private Activity activity;

    private Cursor myCursor;
    private CustomCursorAdapter adap2;
    private CustomersHandler handler;

    private Global global;
    private boolean hasBeenCreated = false;
    private MyPreferences myPref;
    //private SQLiteDatabase db;
    private DBManager dbManager;
    private ViewCustomers_FA _thisActivity;
    private int selectedCustPosition = 0;
    private Dialog dlog;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custselec_listview_layout);

        activity = this;
        _thisActivity = this;
        myPref = new MyPreferences(activity);
        global = (Global) getApplication();
        myListView = (ListView) findViewById(R.id.customerSelectionLV);
        final EditText search = (EditText) findViewById(R.id.searchCustomer);

        dbManager = new DBManager(activity);
        //db = dbManager.openReadableDB();
        handler = new CustomersHandler(this);
        myCursor = handler.getCursorAllCust();
        adap2 = new CustomCursorAdapter(this, myCursor, CursorAdapter.NO_SELECTION);
        myListView.setAdapter(adap2);


        Button addNewCust = (Button) findViewById(R.id.addCustButton);

        if (myPref.getPreferences(MyPreferences.pref_allow_customer_creation))
            addNewCust.setOnClickListener(this);
        else
            addNewCust.setVisibility(View.GONE);

        search.setOnEditorActionListener(getSearchActionListener());
        search.addTextChangedListener(getSearchTextWatcher());

        myListView.setOnItemClickListener(this);
        hasBeenCreated = true;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    private TextWatcher getSearchTextWatcher() {

        return new TextWatcher() {

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
                    myCursor = handler.getCursorAllCust();

                    adap2 = new CustomCursorAdapter(thisContext, myCursor, CursorAdapter.NO_SELECTION);
                    myListView.setAdapter(adap2);
                }
            }
        };
    }

    private OnEditorActionListener getSearchActionListener() {

        return new OnEditorActionListener() {

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
        };
    }

    private void selectCustomer(int itemIndex) {
        Intent results = new Intent();
        myCursor.moveToPosition(itemIndex);

        String name = myCursor.getString(myCursor.getColumnIndex("cust_name"));
        results.putExtra("customer_name", name);

        SalesTaxCodesHandler taxHandler = new SalesTaxCodesHandler(activity);
        SalesTaxCodesHandler.TaxableCode taxable = taxHandler.checkIfCustTaxable(myCursor.getString(myCursor.getColumnIndex("cust_taxable")));
        myPref.setCustTaxCode(taxable, myCursor.getString(myCursor.getColumnIndex("cust_salestaxcode")));
//        if (taxable == SalesTaxCodesHandler.TaxableCode.TAXABLE)
//            myPref.setCustTaxCode(myCursor.getString(myCursor.getColumnIndex("cust_salestaxcode")));
//        else if (taxable == SalesTaxCodesHandler.TaxableCode.NON_TAXABLE)
//            myPref.setCustTaxCode("");
//        else
//            myPref.setCustTaxCode(null);

        myPref.setCustID(myCursor.getString(myCursor.getColumnIndex("_id")));    //getting cust_id as _id
        myPref.setCustName(name);
        myPref.setCustIDKey(myCursor.getString(myCursor.getColumnIndex("custidkey")));
        myPref.setCustSelected(true);

        myPref.setCustPriceLevel(myCursor.getString(myCursor.getColumnIndex("pricelevel_id")));

        myPref.setCustEmail(myCursor.getString(myCursor.getColumnIndex("cust_email")));

        setResult(1, results);
        finish();
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


    public void performSearch(String text) {
        if (myCursor != null)
            myCursor.close();
        myCursor = handler.getSearchCust(text);
        adap2 = new CustomCursorAdapter(thisContext, myCursor, CursorAdapter.NO_SELECTION);
        myListView.setAdapter(adap2);

    }

    public class CustomCursorAdapter extends CursorAdapter {
        private LayoutInflater inflater;
        private boolean displayCustAccountNum = false;

        public CustomCursorAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
            inflater = LayoutInflater.from(context);
            displayCustAccountNum = myPref.getPreferences(MyPreferences.pref_display_customer_account_number);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            final ViewHolder holder = (ViewHolder) view.getTag();
            String temp = cursor.getString(holder.i_cust_name);
            if (temp != null)
                holder.cust_name.setText(temp);

            temp = cursor.getString(holder.i_CompanyName);
            if (temp != null)
                holder.CompanyName.setText(temp);


            temp = cursor.getString(holder.i_cust_phone);
            if (temp != null)
                holder.cust_phone.setText(temp);
            temp = cursor.getString(holder.i_pricelevel_name);
            if (temp != null)
                holder.pricelevel_name.setText(temp);

            if (displayCustAccountNum)
                holder.cust_id.setText(cursor.getString(holder.i_account_number));
            else
                holder.cust_id.setText(cursor.getString(holder.i_cust_id));


            holder.moreInfoIcon.setTag(cursor.getString(holder.i_cust_id));
            holder.moreInfoIcon.setOnTouchListener(Global.opaqueImageOnClick());
            holder.moreInfoIcon.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    String _cust_id = (String) v.getTag();
                    Intent intent = new Intent(thisContext, ViewCustomerDetails_FA.class);
                    intent.putExtra("cust_id", _cust_id);
                    startActivity(intent);
                }
            });


        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            final View retView = inflater.inflate(R.layout.custselec_lvadapter, parent, false);
            ViewHolder holder = new ViewHolder();
            holder.cust_name = (TextView) retView.findViewById(R.id.custSelecName);
            holder.CompanyName = (TextView) retView.findViewById(R.id.custSelecCompanyName);
            holder.cust_id = (TextView) retView.findViewById(R.id.custSelecID);
            holder.cust_phone = (TextView) retView.findViewById(R.id.custSelecPhone);
            holder.pricelevel_name = (TextView) retView.findViewById(R.id.custSelecPriceLevel);
            holder.moreInfoIcon = (ImageView) retView.findViewById(R.id.custSelecIcon);
            holder.i_cust_id = cursor.getColumnIndex("_id");
            holder.i_account_number = cursor.getColumnIndex("AccountNumnber");
            holder.i_cust_name = cursor.getColumnIndex("cust_name");
            holder.i_CompanyName = cursor.getColumnIndex("CompanyName");
            holder.i_cust_phone = cursor.getColumnIndex("cust_phone");
            holder.i_pricelevel_name = cursor.getColumnIndex("pricelevel_name");

            retView.setTag(holder);

            return retView;
        }


        private class ViewHolder {
            TextView cust_name, CompanyName, cust_id, cust_phone, pricelevel_name;
            ImageView moreInfoIcon;

            int i_cust_id, i_account_number, i_cust_name, i_CompanyName, i_cust_phone, i_pricelevel_name;
        }

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == -1) {
            finish();
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.addCustButton:
                Intent intent2 = new Intent(thisContext, CreateCustomer_FA.class);
                startActivityForResult(intent2, 0);
                break;
            case R.id.btnDlogOne:
                dlog.dismiss();
                selectCustomer(selectedCustPosition);
                break;
            case R.id.btnDlogFour:
                dlog.dismiss();
                Intent intent = new Intent(activity, HistoryTransactions_FA.class);
                intent.putExtra("is_from_customers", true);

                myCursor.moveToPosition(selectedCustPosition);
                String id = myCursor.getString(myCursor.getColumnIndex("_id"));    //getting cust_id as _id
                intent.putExtra("cust_id", id);

                startActivity(intent);
                break;
        }
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (myPref.getPreferences(MyPreferences.pref_direct_customer_selection)) {
            selectCustomer(position);
        } else {
            selectedCustPosition = position;
            dlog = new Dialog(activity, R.style.Theme_TransparentTest);
            dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dlog.setCancelable(true);
            dlog.setCanceledOnTouchOutside(true);
            dlog.setContentView(R.layout.dlog_cust_select);

            TextView viewTitle = (TextView) dlog.findViewById(R.id.dlogTitle);
            TextView viewMsg = (TextView) dlog.findViewById(R.id.dlogMessage);
            viewTitle.setText(R.string.dlog_title_choose_action);
            viewMsg.setVisibility(View.GONE);
            Button btnSelectCust = (Button) dlog.findViewById(R.id.btnDlogOne);
            Button btnDialPhone = (Button) dlog.findViewById(R.id.btnDlogTwo);
            Button btnMapView = (Button) dlog.findViewById(R.id.btnDlogThree);
            Button btnTrans = (Button) dlog.findViewById(R.id.btnDlogFour);
            btnSelectCust.setText(R.string.cust_dlog_select_cust);
            btnDialPhone.setText(R.string.cust_dlog_dial);
            btnMapView.setText(R.string.cust_dlog_map);
            btnTrans.setText(R.string.cust_dlog_view_trans);

            btnSelectCust.setOnClickListener(_thisActivity);
            btnTrans.setOnClickListener(_thisActivity);
            dlog.show();
        }
    }
}
