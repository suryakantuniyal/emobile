package com.android.emobilepos.customer;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.widget.CursorAdapter;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.android.database.CustomersHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.history.HistoryTransactions_FA;
import com.android.emobilepos.models.realms.BiometricFid;
import com.android.emobilepos.models.realms.EmobileBiometric;
import com.android.emobilepos.ordering.BBPosShelpaDeviceDriver;
import com.android.emobilepos.security.SecurityManager;
import com.android.soundmanager.SoundManager;
import com.android.support.CreditCardInfo;
import com.android.support.Customer;
import com.android.support.DeviceUtils;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;
import com.bbpos.bbdevice.BBDeviceController;

import java.util.Collection;

import drivers.digitalpersona.DigitalPersona;
import interfaces.BCRCallbacks;
import interfaces.BiometricCallbacks;
import interfaces.EMSCallBack;
import util.json.UIUtils;

public class ViewCustomers_FA extends BaseFragmentActivityActionBar implements BiometricCallbacks, OnClickListener, OnItemClickListener, BCRCallbacks, EMSCallBack {
    boolean isManualEntry = true;
    private ListView myListView;
    private Context thisContext = this;
    private Activity activity;
    private Cursor myCursor;
    private CustomCursorAdapter adap2;
    private CustomersHandler handler;
    private Global global;
    private boolean hasBeenCreated = false;
    private MyPreferences myPref;
    private int selectedCustPosition = 0;
    private Dialog dlog;
    private EditText search;

    private boolean isReaderConnected;
    private DigitalPersona digitalPersona;
    private BBDeviceController bbDeviceController;
    private BBPosShelpaDeviceDriver listener;
    private SoundManager soundManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custselec_listview_layout);
        digitalPersona = new DigitalPersona(getApplicationContext(), this, EmobileBiometric.UserType.CUSTOMER);
        soundManager = SoundManager.getInstance();
        soundManager.initSounds(this);
        soundManager.loadSounds();
        activity = this;
        myPref = new MyPreferences(activity);
        global = (Global) getApplication();
        myListView = findViewById(R.id.customerSelectionLV);
        search = findViewById(R.id.searchCustomer);
        Collection<UsbDevice> usbDevices = DeviceUtils.getUSBDevices(this);
        isReaderConnected = usbDevices.size() > 0;
        handler = new CustomersHandler(this);
        myCursor = handler.getCursorAllCust();
        adap2 = new CustomCursorAdapter(this, myCursor, CursorAdapter.NO_SELECTION);
        myListView.setAdapter(adap2);

        Button addNewCust = findViewById(R.id.addCustButton);
        if (myPref.getPreferences(MyPreferences.pref_allow_customer_creation))
            addNewCust.setOnClickListener(this);
        else
            addNewCust.setVisibility(View.GONE);

        search.setOnEditorActionListener(getSearchActionListener());
        search.addTextChangedListener(getSearchTextWatcher());
        search.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                isManualEntry = false;
                UIUtils.startBCR(v, search, ViewCustomers_FA.this);
                return false;
            }
        });
        myListView.setOnItemClickListener(this);
        hasBeenCreated = true;
        listener = new BBPosShelpaDeviceDriver(this, this);
        bbDeviceController = BBDeviceController.getInstance(
                this, listener);
        if (bbDeviceController != null) {
            bbDeviceController.startBarcodeReader();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == 138) {
            event.startTracking();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (keyCode == 138) {
            if (bbDeviceController != null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        bbDeviceController.startBarcodeReader();
                        bbDeviceController.getBarcode();
                    }
                }).start();
            }
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == 138) {
            if (bbDeviceController != null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        bbDeviceController.stopBarcodeReader();
                        bbDeviceController.startBarcodeReader();
                    }
                }).start();
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onDestroy() {
        releaseReader();
        super.onDestroy();
    }


    private TextWatcher getSearchTextWatcher() {

        return new TextWatcher() {

            @Override
            public void afterTextChanged(Editable arg0) {
                String s = arg0.toString();
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
                    if (!isManualEntry) {
                        if (adap2.getCount() == 1) {
                            selectCustomer(0);
                        }
                    }
                }
            }
        };
    }

    private OnEditorActionListener getSearchActionListener() {

        return new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                        actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
                    String text = v.getText().toString().trim();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    if (!text.isEmpty())
                        performSearch(text);
                    return true;
                }
                return false;
            }
        };
    }

    private void releaseReader() {
        if (isReaderConnected) {
            digitalPersona.releaseReader();
        }
    }

    private void selectCustomer(int itemIndex) {
        myCursor.moveToPosition(itemIndex);
        Intent results = new Intent();
        String name = myCursor.getString(myCursor.getColumnIndex("cust_name"));
        String lastname = myCursor.getString(myCursor.getColumnIndex("cust_lastName"));
        results.putExtra("customer_name", String.format("%s %s", name, lastname));
        myPref.setCustID(myCursor.getString(myCursor.getColumnIndex("_id")));    //getting cust_id as _id
        myPref.setCustName(name);
        myPref.setCustIDKey(myCursor.getString(myCursor.getColumnIndex("custidkey")));
        myPref.setCustSelected(true);
        myPref.setCustTaxCode(myCursor.getString(myCursor.getColumnIndex("cust_salestaxcode")));
        myPref.setCustPriceLevel(myCursor.getString(myCursor.getColumnIndex("pricelevel_id")));
        myPref.setCustEmail(myCursor.getString(myCursor.getColumnIndex("cust_email")));
        setResult(1, results);
        finish();
    }

    private void selectCustomer() {
        Intent results = new Intent();
        CustomersHandler handler = new CustomersHandler(this);
        Customer customer = handler.getCustomer(myPref.getCustID());
        results.putExtra("customer_name", String.format("%s %s", customer.getCust_name()
                , customer.getCust_lastName()));
        setResult(1, results);
        finish();
    }

    @Override
    public void onResume() {
        if (isReaderConnected) {
            digitalPersona.loadForScan();
        }
        if (global.isApplicationSentToBackground())
            Global.loggedIn = false;
        global.stopActivityTransitionTimer();

        if (hasBeenCreated && !Global.loggedIn) {
            if (global.getGlobalDlog() != null)
                global.getGlobalDlog().dismiss();
            global.promptForMandatoryLogin(activity);
        }
        super.onResume();

        // set focus on search
        search.requestFocus();
        search.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.showSoftInput(search, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 100);
    }

    @Override
    public void onBackPressed() {
        if (!myPref.isCustSelected()) {
            Intent data = getIntent();
            data.putExtra("GOTO_MAIN", true);
            setResult(Global.FROM_CUSTOMER_SELECTION_ACTIVITY, data);
        }
        finish();
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == -1) {
            selectCustomer();
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.addCustButton:
                boolean hasPermissions = SecurityManager.hasPermissions(this, SecurityManager.SecurityAction.CREATE_CUSTOMERS);
                if (hasPermissions) {
                    releaseReader();
                    Intent intent = new Intent(thisContext, ViewCustomerDetails_FA.class);
                    startActivityForResult(intent, 0);
                } else {
                    Global.showPrompt(this, R.string.security_alert, getString(R.string.permission_denied));
                }
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
        if (myPref.isDirectCustomerSelection()) {
            selectCustomer(position);
        } else {
            selectedCustPosition = position;
            dlog = new Dialog(activity, R.style.Theme_TransparentTest);
            dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dlog.setCancelable(true);
            dlog.setCanceledOnTouchOutside(true);
            dlog.setContentView(R.layout.dlog_cust_select);

            TextView viewTitle = dlog.findViewById(R.id.dlogTitle);
            TextView viewMsg = dlog.findViewById(R.id.dlogMessage);
            viewTitle.setText(R.string.dlog_title_choose_action);
            viewMsg.setVisibility(View.GONE);
            Button btnSelectCust = dlog.findViewById(R.id.btnDlogOne);
            Button btnDialPhone = dlog.findViewById(R.id.btnDlogTwo);
            Button btnMapView = dlog.findViewById(R.id.btnDlogThree);
            Button btnTrans = dlog.findViewById(R.id.btnDlogFour);
            btnSelectCust.setText(R.string.cust_dlog_select_cust);
            btnDialPhone.setText(R.string.cust_dlog_dial);
            btnMapView.setText(R.string.cust_dlog_map);
            btnTrans.setText(R.string.cust_dlog_view_trans);

            btnSelectCust.setOnClickListener(ViewCustomers_FA.this);
            btnTrans.setOnClickListener(ViewCustomers_FA.this);
            dlog.show();
        }
    }

    @Override
    public void executeBCR() {
        myCursor = handler.getSearchCust(search.getText().toString());
        if (myCursor.getCount() == 1) {
            selectCustomer(0);
        }
    }

    @Override
    public void biometricsWasRead(final EmobileBiometric biometric) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                search.setText(biometric.getEntityid());
                executeBCR();
            }
        });
    }

    @Override
    public void biometricsReadNotFound() {

    }

    @Override
    public void biometricsWasEnrolled(BiometricFid biometricFid) {

    }

    @Override
    public void biometricsDuplicatedEnroll(EmobileBiometric emobileBiometric, BiometricFid biometricFid) {

    }


    @Override
    public void biometricsUnregister(ViewCustomerDetails_FA.Finger finger) {

    }

    @Override
    public void cardWasReadSuccessfully(boolean read, CreditCardInfo cardManager) {

    }

    @Override
    public void readerConnectedSuccessfully(boolean value) {

    }

    @Override
    public void scannerWasRead(String data) {
        soundManager.playSound(1, 1);
        search.setText(data);
        executeBCR();
    }

    @Override
    public void startSignature() {

    }

    @Override
    public void nfcWasRead(String nfcUID) {

    }


    public class CustomCursorAdapter extends CursorAdapter {
        private LayoutInflater inflater;
        private boolean displayCustAccountNum;

        CustomCursorAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
            inflater = LayoutInflater.from(context);
            displayCustAccountNum = myPref.getPreferences(MyPreferences.pref_display_customer_account_number);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            final ViewHolder holder = (ViewHolder) view.getTag();
            String temp = cursor.getString(holder.i_cust_name);
            String lastname = cursor.getString(holder.i_cust_lastName);
            if (!TextUtils.isEmpty(temp)) {
                holder.cust_name.setText(String.format("%s %s", temp, lastname));
            }

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
                    releaseReader();
                    String _cust_id = (String) v.getTag();
                    Intent intent = new Intent(thisContext, ViewCustomerDetails_FA.class);
                    intent.putExtra("cust_id", _cust_id);
                    startActivity(intent);
                }
            });


        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            final View retView = inflater.inflate(R.layout.custselec_lvadapter, null);
            ViewHolder holder = new ViewHolder();
            holder.cust_name = retView.findViewById(R.id.custSelecName);
            holder.CompanyName = retView.findViewById(R.id.custSelecCompanyName);
            holder.cust_id = retView.findViewById(R.id.custSelecID);
            holder.cust_phone = retView.findViewById(R.id.custSelecPhone);
            holder.pricelevel_name = retView.findViewById(R.id.custSelecPriceLevel);
            holder.moreInfoIcon = retView.findViewById(R.id.custSelecIcon);
            holder.i_cust_id = cursor.getColumnIndex("_id");
            holder.i_account_number = cursor.getColumnIndex("AccountNumnber");
            holder.i_cust_name = cursor.getColumnIndex("cust_firstName");
            holder.i_cust_lastName = cursor.getColumnIndex("cust_lastName");


            holder.i_CompanyName = cursor.getColumnIndex("CompanyName");
            holder.i_cust_phone = cursor.getColumnIndex("cust_phone");
            holder.i_pricelevel_name = cursor.getColumnIndex("pricelevel_name");

            retView.setTag(holder);

            return retView;
        }


        private class ViewHolder {
            TextView cust_name, CompanyName, cust_id, cust_phone, pricelevel_name;
            ImageView moreInfoIcon;

            int i_cust_id, i_account_number, i_cust_name, i_cust_lastName, i_CompanyName, i_cust_phone, i_pricelevel_name;
        }

    }
}
