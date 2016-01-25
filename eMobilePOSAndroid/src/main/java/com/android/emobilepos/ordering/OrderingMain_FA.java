package com.android.emobilepos.ordering;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.database.AddressHandler;
import com.android.database.CustomerInventoryHandler;
import com.android.database.DBManager;
import com.android.database.OrderProductsAttr_DB;
import com.android.database.OrderProductsHandler;
import com.android.database.OrdersHandler;
import com.android.database.PayMethodsHandler;
import com.android.database.ProductsHandler;
import com.android.database.VoidTransactionsHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.mainmenu.MainMenu_FA;
import com.android.emobilepos.mainmenu.SalesTab_FR;
import com.android.emobilepos.models.DinningTable;
import com.android.emobilepos.models.Order;
import com.android.emobilepos.models.OrderProduct;
import com.android.emobilepos.models.Orders;
import com.android.emobilepos.models.Payment;
import com.android.emobilepos.models.Product;
import com.android.payments.EMSPayGate_Default;
import com.android.saxhandler.SAXProcessCardPayHandler;
import com.android.soundmanager.SoundManager;
import com.android.support.CreditCardInfo;
import com.android.support.Encrypt;
import com.android.support.GenerateNewID;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.Post;
import com.android.support.TerminalDisplay;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;
import com.honeywell.decodemanager.DecodeManager;
import com.honeywell.decodemanager.DecodeManager.SymConfigActivityOpeartor;
import com.honeywell.decodemanager.SymbologyConfigs;
import com.honeywell.decodemanager.barcode.CommonDefine;
import com.honeywell.decodemanager.barcode.DecodeResult;
import com.honeywell.decodemanager.symbologyconfig.SymbologyConfigCodeUPCA;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import drivers.EMSIDTechUSB;
import drivers.EMSMagtekAudioCardReader;
import drivers.EMSRover;
import drivers.EMSUniMagDriver;
import protocols.EMSCallBack;

public class OrderingMain_FA extends BaseFragmentActivityActionBar implements Receipt_FR.AddProductBtnCallback,
        Receipt_FR.UpdateHeaderTitleCallback, OnClickListener, Catalog_FR.RefreshReceiptViewCallback,
        OrderLoyalty_FR.SwiperLoyaltyCallback, OrderRewards_FR.SwiperRewardCallback, EMSCallBack {

    private static String ourIntentAction = "";
    private static final String DATA_STRING_TAG = "com.motorolasolutions.emdk.datawedge.data_string";
    private int orientation;
    private LinearLayout catalogContainer, receiptContainer;
    public static OrderingMain_FA instance;
    private DinningTable selectedDinningTable;
    private Catalog_FR rightFragment;
    private Receipt_FR leftFragment;
    private MyPreferences myPref;
    private Global global;
    private boolean hasBeenCreated = false;
    private ProductsHandler handler;
    public static EditText invisibleSearchMain;
    private static TextView headerTitle;
    private CustomerInventoryHandler custInventoryHandler;

    // Honeywell Dolphin black
    private DecodeManager mDecodeManager = null;
    private final int SCANTIMEOUT = 500000;
    private boolean scannerInDecodeMode = false;

    public EMSIDTechUSB _msrUsbSams;
    public EMSCallBack callBackMSR;
    private EMSUniMagDriver uniMagReader;
    private EMSMagtekAudioCardReader magtekReader;
    private EMSRover roverReader;
    private static boolean msrWasLoaded = false;
    private static boolean cardReaderConnected = false;
    private TextView swiperLabel;
    private EditText swiperField;
    public static boolean rewardsWasRead = false;
    private static boolean wasReadFromReader = false;
    private ProgressDialog myProgressDialog;
    private CreditCardInfo cardInfoManager;
    private Button btnCheckout;
    public static Global.TransactionType mTransType = null;
    public static boolean returnItem = false;
    private Bundle extras;
    private Global.RestaurantSaleType restaurantSaleType = Global.RestaurantSaleType.EAT_IN;
    private boolean isToGo;
    private String selectedSeatsAmount;
    private String selectedDinningTableNumber;
    private static String selectedSeatNumber = "1";


//    CustomKeyboard mCustomKeyboard;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_main_layout);
        callBackMSR = this;
        instance = this;
        handler = new ProductsHandler(this);
        receiptContainer = (LinearLayout) findViewById(R.id.order_receipt_frag_container);
        catalogContainer = (LinearLayout) findViewById(R.id.order_catalog_frag_container);
        invisibleSearchMain = (EditText) findViewById(R.id.invisibleSearchMain);
        global = (Global) getApplication();
        btnCheckout = (Button) findViewById(R.id.btnCheckOut);
        btnCheckout.setOnClickListener(this);

        myPref = new MyPreferences(this);
        extras = getIntent().getExtras();
        mTransType = (Global.TransactionType) extras.get("option_number");
        restaurantSaleType = (Global.RestaurantSaleType) extras.get("RestaurantSaleType");
        selectedSeatsAmount = extras.getString("selectedSeatsAmount");
        selectedDinningTableNumber = extras.getString("selectedDinningTableNumber");

        isToGo = restaurantSaleType == Global.RestaurantSaleType.TO_GO;
        returnItem = mTransType == Global.TransactionType.RETURN;
        if (!myPref.getIsTablet())
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (savedInstanceState == null) {
            if (!myPref.getIsTablet())
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            orientation = getResources().getConfiguration().orientation;
            rightFragment = new Catalog_FR();
            leftFragment = new Receipt_FR();

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.order_receipt_frag_container, leftFragment);
            ft.add(R.id.order_catalog_frag_container, rightFragment);
            ft.commit();

            msrWasLoaded = false;
            cardReaderConnected = false;
            wasReadFromReader = false;
        }

        handleFragments();
        setupTitle();
        setCustomerShipToAddress();

        invisibleSearchMain.addTextChangedListener(textWatcher());
        invisibleSearchMain.requestFocus();

        ourIntentAction = getString(R.string.intentAction);
        // in case we have been launched by the DataWedge intent plug-in
        // using the StartActivity method let's handle the intent
        Intent i = getIntent();
        handleDecodeData(i);

        if (Global.deviceHasBarcodeScanner(myPref.getPrinterType())
                || Global.deviceHasBarcodeScanner(myPref.sledType(true, -2))) {
            if (Global.mainPrinterManager != null && Global.mainPrinterManager.currentDevice != null)
                Global.mainPrinterManager.currentDevice.loadScanner(callBackMSR);
            if (Global.btSled != null && Global.btSled.currentDevice != null)
                Global.btSled.currentDevice.loadScanner(callBackMSR);
        }


        hasBeenCreated = true;
    }


    private Handler ScanResultHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DecodeManager.MESSAGE_DECODER_COMPLETE:
                    String strDecodeResult = "";
                    DecodeResult decodeResult = (DecodeResult) msg.obj;

                    strDecodeResult = decodeResult.barcodeData.trim();
                    if (!strDecodeResult.isEmpty()) {
                        SoundManager.playSound(1, 1);
                        scanAddItem(strDecodeResult);
                    }
                    break;

                case DecodeManager.MESSAGE_DECODER_FAIL: {
                    SoundManager.playSound(2, 1);
                }
                break;
                case DecodeManager.MESSAGE_DECODER_READY: {
                    if (mDecodeManager != null) {
                        SymConfigActivityOpeartor operator = mDecodeManager.getSymConfigActivityOpeartor();
                        operator.removeAllSymFromConfigActivity();
                        SymbologyConfigCodeUPCA upca = new SymbologyConfigCodeUPCA();
                        upca.enableSymbology(true);
                        upca.enableCheckTransmit(true);
                        upca.enableSendNumSys(true);

                        SymbologyConfigs symconfig = new SymbologyConfigs();
                        symconfig.addSymbologyConfig(upca);

                        try {
                            mDecodeManager.setSymbologyConfigs(symconfig);
                        } catch (RemoteException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
                break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    };

    private Handler SearchFieldHandler = new Handler() {
        public void handleMessage(Message msg) {
            Bundle theBundle = msg.getData();
            if (theBundle.getString("message").equals("PerformSearch")) {
                //call performSearch
                String text = theBundle.getString("searchfield");
                if (!text.isEmpty()) {
                    rightFragment.performSearch(text);
                }
            }
        }
    };

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    private void setupTitle() {
        headerTitle = (TextView) findViewById(R.id.headerTitle);

        headerContainer = (RelativeLayout) findViewById(R.id.headerTitleContainer);
        if (myPref.isCustSelected()) {

            switch (mTransType) {
                case SALE_RECEIPT: {
                    headerTitle.setText(R.string.sales_receipt);
                    break;
                }
                case ORDERS: {
                    headerTitle.setText(R.string.order);
                    break;
                }
                case RETURN: {
                    setReturnConfiguration(R.string.return_title);
                    break;
                }
                case INVOICE: {
                    headerTitle.setText(R.string.invoice);
                    break;
                }
                case ESTIMATE: {
                    headerTitle.setText(R.string.estimate);
                    break;
                }
                case CONSIGNMENT: {
                    if (!Global.isConsignment) {
                        custInventoryHandler = new CustomerInventoryHandler(this);
                        custInventoryHandler.getCustomerInventory();
                        Global.consignSummaryMap = new HashMap<String, HashMap<String, String>>();
                        Global.consignMapKey = new ArrayList<String>();
                        Global.isConsignment = true;

                        // consignmentType 0 = Rack, 1 = Returns, 2 = Fill-up, 3 =
                        // Pick-up
                        Global.OrderType consignmentType = Global.OrderType.getByCode(extras.getInt("consignmentType", 0));

                        if (Global.custInventoryKey == null || Global.custInventoryKey.size() <= 0) {
                            consignmentType = Global.OrderType.CONSIGNMENT_FILLUP;
                        }
                        Global.consignmentType = consignmentType;
                    }
                    switch (Global.consignmentType) {
                        case ORDER:
                            headerTitle.setText(R.string.consignment_stacked);
                            break;
                        case CONSIGNMENT_RETURN:
                            setReturnConfiguration(R.string.consignment_returned);
                            break;
                        case CONSIGNMENT_FILLUP:
                            headerTitle.setText(R.string.consignment_filledup);
                            break;
                        case CONSIGNMENT_PICKUP:
                            headerTitle.setText(R.string.consignment_pickup);
                            break;
                    }

                    break;
                }
                case LOCATION:// Inventory Transfer
                    headerTitle.setText(R.string.inventory_transfer);
                    break;
            }
        } else {
            switch (mTransType) {
                case SALE_RECEIPT: {
                    headerTitle.setText(R.string.sales_receipt);
                    break;
                }
                case ORDERS: {
                    headerTitle.setText(R.string.return_tag);
                    break;
                }
                case RETURN: {
                    setReturnConfiguration(R.string.return_title);
                    break;
                }
                case LOCATION:// Inventory Transfer
                    headerTitle.setText(R.string.inventory_transfer);
                    break;
            }
        }
    }

    private static String savedHeaderTitle = "";
    private static RelativeLayout headerContainer;

    public static void switchHeaderTitle(boolean newTitle, String title) {
        if (mTransType == Global.TransactionType.RETURN || newTitle) {
            savedHeaderTitle = headerTitle.getText().toString();
            headerTitle.setText(title);
            headerContainer.setBackgroundColor(Color.RED);

        } else {
            headerTitle.setText(savedHeaderTitle);
            headerContainer.setBackgroundResource(R.drawable.blue_gradient_header_horizontal);
        }
    }

    private void setReturnConfiguration(int titleResId) {
        headerTitle.setText(getString(titleResId));
        headerContainer.setBackgroundColor(Color.RED);
        OrderingMain_FA.returnItem = true;
    }

    private void handleFragments() {
        int _orientation = getResources().getConfiguration().orientation;
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        if (leftFragment == null)
            leftFragment = (Receipt_FR) fm.findFragmentById(R.id.order_receipt_frag_container);
        if (rightFragment == null)
            rightFragment = (Catalog_FR) fm.findFragmentById(R.id.order_catalog_frag_container);
        if (orientation != _orientation) // screen orientation occurred
        {
            if (_orientation == Configuration.ORIENTATION_PORTRAIT) // changing
            // from Land
            // to
            // Portrait
            {
                catalogContainer.setVisibility(View.GONE);
            } else // changing from Portrait to Landscape
            {
                catalogContainer.setVisibility(View.VISIBLE);

            }
            ft.commit();
        } else {
            if (_orientation == Configuration.ORIENTATION_PORTRAIT) {
                catalogContainer.setVisibility(View.GONE);
            } else {
                catalogContainer.setVisibility(View.VISIBLE);

            }
        }
        orientation = _orientation;
    }

    private boolean isBackPressed = false;

    @Override
    public void onBackPressed() {
        boolean tablet = myPref.getIsTablet();
        isBackPressed = true;
        if (catalogContainer.getVisibility() == View.VISIBLE
                && (!tablet || (tablet && orientation == Configuration.ORIENTATION_PORTRAIT))) {
            catalogContainer.setVisibility(View.GONE);
            receiptContainer.startAnimation(AnimationUtils.loadAnimation(this, R.anim.anim_left_right));
            receiptContainer.setVisibility(View.VISIBLE);
        } else {
            if (Global.isFromOnHold)
                showDlog(true);
            else
                showDlog(false);
        }

        // NOTE Trap the back key: when the CustomKeyboard is still visible hide it, only when it is invisible, finish activity
//        if (mCustomKeyboard.isCustomKeyboardVisible()) {
//            mCustomKeyboard.hideCustomKeyboard();
//        }
    }

    @Override
    public void addProductServices() {

        catalogContainer.startAnimation(AnimationUtils.loadAnimation(this, R.anim.anim_right_left));
        catalogContainer.setVisibility(View.VISIBLE);
        receiptContainer.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (SystemClock.elapsedRealtime() - Receipt_FR.lastClickTime < 1000) {
            return;
        }
        Receipt_FR.lastClickTime = SystemClock.elapsedRealtime();
        switch (v.getId()) {
            case R.id.btnCheckOut:
                btnCheckout.setEnabled(false);
                if (leftFragment != null) {
                    leftFragment.checkoutOrder();
                }
                btnCheckout.setEnabled(true);
                break;
        }
    }

    @Override
    public void refreshView() {
        // TODO Auto-generated method stub
        if (leftFragment != null)
            leftFragment.refreshView();
        if (rightFragment != null)
            rightFragment.refreshListView();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == Global.FROM_DRAW_RECEIPT_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        } else if (resultCode == 1) {

            Bundle extras = data.getExtras();
            String newName = extras.getString("customer_name");

            Global.taxID = "";
            leftFragment.custName.setText(newName);
            OrderTotalDetails_FR.getFrag().initSpinners();
            if (rightFragment != null) {
                rightFragment.loadCursor();
            }
        } else if (resultCode == -1 || resultCode == 3) // Void transaction from
        // Sales Receipt
        {
            OrderTotalDetails_FR.resetView();
            global.resetOrderDetailsValues();
            global.clearListViewData();

            if (myPref.isSam4s(true, true) || myPref.isPAT100()) {
                Global.showCDTDefault(this);
            }

            reloadDefaultTransaction();
        } else if (resultCode == 9) {

        } else if (resultCode == 2)
            this.refreshView();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == 0) {
            fragOnKeyDown(keyCode);
            return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    private void reloadDefaultTransaction() {
        if (myPref.getPreferencesValue(MyPreferences.pref_default_transaction).equals("-1"))
            finish();
        else {
            DBManager dbManager = new DBManager(MainMenu_FA.activity, Global.FROM_SYNCH_ACTIVITY);
            if (myPref.getPreferences(MyPreferences.pref_automatic_sync)) {
                dbManager.synchSend(false, true);
            }

            OrderTotalDetails_FR.resetView();
            finish();
            myPref.resetCustInfo(getString(R.string.no_customer));
            SalesTab_FR.startDefault(MainMenu_FA.activity,
                    myPref.getPreferencesValue(MyPreferences.pref_default_transaction));
        }
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

        if (myPref.isDolphin(true, false) && mDecodeManager == null) {
            mDecodeManager = new DecodeManager(this, ScanResultHandler);
            try {
                SoundManager.getInstance();
                SoundManager.initSounds(this);
                SoundManager.loadSounds();
                mDecodeManager.disableSymbology(CommonDefine.SymbologyID.SYM_CODE39);
                mDecodeManager.setSymbologyDefaults(CommonDefine.SymbologyID.SYM_UPCA);
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (myPref.getPreferencesValue(MyPreferences.pref_default_transaction).equals("-1")
                || (!myPref.getPreferencesValue(MyPreferences.pref_default_transaction).equals("-1")
                && isBackPressed)) {
            if (mDecodeManager != null) {
                try {
                    mDecodeManager.release();
                    mDecodeManager = null;
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            resetMSRValues();

            if (uniMagReader != null)
                uniMagReader.release();
            else if (magtekReader != null)
                magtekReader.closeDevice();
            else if (Global.btSwiper != null && Global.btSwiper.currentDevice != null)
                Global.btSwiper.currentDevice.releaseCardReader();
            else if (Global.mainPrinterManager != null && Global.mainPrinterManager.currentDevice != null) {
                Global.mainPrinterManager.currentDevice.releaseCardReader();
                Global.mainPrinterManager.currentDevice.loadScanner(null);
            } else if (Global.btSled != null && Global.btSled.currentDevice != null)
                Global.btSled.currentDevice.releaseCardReader();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        boolean isScreenOn = powerManager.isScreenOn();
        if (!isScreenOn)
            global.loggedIn = false;

        if (PickerAddon_FA.instance == null && PickerProduct_FA.instance == null)
            global.startActivityTransitionTimer();
    }

    private void resetMSRValues() {
        cardReaderConnected = false;
        msrWasLoaded = false;
        rewardsWasRead = false;
        wasReadFromReader = false;
    }

    @Override
    public void onNewIntent(Intent i) {
        super.onNewIntent(i);
        handleDecodeData(i);
    }

    private void setCustomerShipToAddress() {
        AddressHandler addressHandler = new AddressHandler(this);
        List<String[]> addressDownloadedItems = addressHandler.getAddress();
        int size = addressDownloadedItems.size();
        if (size == 1) {
            StringBuilder sb = new StringBuilder();
            String temp = "";
            for (int i = 0; i < size; i++) {
                // sb.append("[").append(addressDownloadedItems.get(i)[0]).append("]
                // ");
                temp = addressDownloadedItems.get(i)[1];
                if (!temp.isEmpty()) // address 1
                    sb.append(temp).append(" ");
                temp = addressDownloadedItems.get(i)[2];
                if (!temp.isEmpty()) // address 2
                    sb.append(temp).append(" ");
                temp = addressDownloadedItems.get(i)[3];
                if (!temp.isEmpty()) // address 3
                    sb.append(temp).append("\t\t");
                temp = addressDownloadedItems.get(i)[4];
                if (!temp.isEmpty()) // address country
                    sb.append(temp).append(" ");
                temp = addressDownloadedItems.get(i)[5];
                if (!temp.isEmpty()) // address city
                    sb.append(temp).append(",");
                temp = addressDownloadedItems.get(i)[6]; // address state
                if (!temp.isEmpty())
                    sb.append(temp).append(" ");

                temp = addressDownloadedItems.get(i)[7]; // address zip code
                if (!temp.isEmpty())
                    sb.append(temp);
            }
            global.setSelectedAddressString(sb.toString());
            global.setSelectedAddress(1);
        }
    }

    private void handleDecodeData(Intent i) {
        // check the intent action is for us
        if (i.getAction() != null && i.getAction().contentEquals(ourIntentAction)) {

            // get the data from the intent
            String data = i.getStringExtra(DATA_STRING_TAG);

            if (dlogMSR != null && dlogMSR.isShowing()) {
                this.cardInfoManager = Global.parseSimpleMSR(this, data);
                wasReadFromReader = true;
                cardInfoManager.setWasSwiped(true);
                if (loyaltySwiped)
                    Global.loyaltyCardInfo = cardInfoManager;
                else
                    Global.rewardCardInfo = cardInfoManager;
                swiperField.setText(cardInfoManager.getCardNumUnencrypted());
            } else {
                Product product = handler.getUPCProducts(data);

                if (product.getId() != null) {

                    if (myPref.getPreferences(MyPreferences.pref_fast_scanning_mode)) {
                        if (validAutomaticAddQty(product)) {
                            if (myPref.getPreferences(MyPreferences.pref_group_receipt_by_sku)) {
                                int foundPosition = global.checkIfGroupBySKU(this, product.getId(), "1");
                                if (foundPosition != -1) // product already
                                // exist in list
                                {
                                    global.refreshParticularOrder(OrderingMain_FA.this, foundPosition, product);
                                } else
                                    Catalog_FR.instance.automaticAddOrder(product);// temp.automaticAddOrder(listData);
                            } else
                                Catalog_FR.instance.automaticAddOrder(product);
                            refreshView();
                        } else {
                            Global.showPrompt(this, R.string.dlog_title_error,
                                    this.getString(R.string.limit_onhand));
                        }
                    } else {
                        Catalog_FR.instance.searchUPC(data);
                    }
                }
            }
        }
    }

    private void showDlog(final boolean isFromOnHold) {
        final Dialog dlog = new Dialog(this, R.style.Theme_TransparentTest);
        dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dlog.setCancelable(false);
        dlog.setCanceledOnTouchOutside(false);
        dlog.setContentView(R.layout.dlog_btn_left_right_layout);

        TextView viewTitle = (TextView) dlog.findViewById(R.id.dlogTitle);
        TextView viewMsg = (TextView) dlog.findViewById(R.id.dlogMessage);

        Button btnLeft = (Button) dlog.findViewById(R.id.btnDlogLeft);
        Button btnRight = (Button) dlog.findViewById(R.id.btnDlogRight);
        dlog.findViewById(R.id.btnDlogCancel).setVisibility(View.GONE);

        if (isFromOnHold) {
            viewMsg.setVisibility(View.GONE);
            viewTitle.setText(R.string.cust_dlog_choose_action);
            btnLeft.setText(R.string.button_void);
            btnRight.setText(R.string.button_cancel);
        } else {
            viewTitle.setText(R.string.warning_title);
            viewMsg.setText(R.string.warning_exit_now);
            btnLeft.setText(R.string.button_yes);
            btnRight.setText(R.string.button_no);
        }

        btnLeft.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                dlog.dismiss();
                if (isFromOnHold)
                    leftFragment.voidCancelOnHold(1);
                else {

                    if (mTransType == Global.TransactionType.SALE_RECEIPT) // is sales receipt
                        voidTransaction();
                    else
                        deleteTransaction();
                    global.resetOrderDetailsValues();
                    global.clearListViewData();
                    msrWasLoaded = false;
                    cardReaderConnected = false;
                    Receipt_FR.mainLVAdapter.notifyDataSetChanged();
                    Receipt_FR.receiptListView.invalidateViews();
                    finish();
                }
            }
        });
        btnRight.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                dlog.dismiss();
                if (isFromOnHold)
                    leftFragment.voidCancelOnHold(2);
            }
        });
        dlog.show();
    }

    private TextWatcher textWatcher() {

        TextWatcher tw = new TextWatcher() {
            boolean doneScanning = false;

            @Override
            public void afterTextChanged(Editable s) {
                if (doneScanning) {
                    doneScanning = false;
                    if (Global.mainPrinterManager != null && Global.mainPrinterManager.currentDevice != null) {
                        Global.mainPrinterManager.currentDevice.playSound();
                    }
                    String upc = invisibleSearchMain.getText().toString().trim().replace("\n", "");
                    upc = invisibleSearchMain.getText().toString().trim().replace("\r", "");
                    Product product = handler.getUPCProducts(upc);
                    if (product.getId() != null) {
                        if (myPref.getPreferences(MyPreferences.pref_fast_scanning_mode)) {
                            if (validAutomaticAddQty(product)) {
                                if (myPref.getPreferences(MyPreferences.pref_group_receipt_by_sku)) {
                                    int foundPosition = global.checkIfGroupBySKU(OrderingMain_FA.this, product.getId(), "1");
                                    if (foundPosition != -1 && !OrderingMain_FA.returnItem) // product
                                    // already
                                    // exist
                                    // in
                                    // list
                                    {
                                        global.refreshParticularOrder(OrderingMain_FA.this, foundPosition, product);
                                    } else
                                        Catalog_FR.instance.automaticAddOrder(product);// temp.automaticAddOrder(listData);
                                } else
                                    Catalog_FR.instance.automaticAddOrder(product);
                                refreshView();
                                if (OrderingMain_FA.returnItem) {
                                    OrderingMain_FA.returnItem = !OrderingMain_FA.returnItem;
                                    OrderingMain_FA.switchHeaderTitle(OrderingMain_FA.returnItem, "Return");
                                }
                            } else {
                                Global.showPrompt(OrderingMain_FA.this, R.string.dlog_title_error,
                                        OrderingMain_FA.this.getString(R.string.limit_onhand));
                            }
                        } else {
                            Catalog_FR.instance.searchUPC(upc);
                        }
                    } else {
                        Global.showPrompt(OrderingMain_FA.this, R.string.dlog_title_error,
                                getString(R.string.dlog_msg_item_not_found));
                    }
                    invisibleSearchMain.setText("");
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
                if (s.toString().contains("\n") || s.toString().contains("\r"))
                    doneScanning = true;
            }
        };
        return tw;
    }

    @Override
    public void updateHeaderTitle(String val) {
        // TODO Auto-generated method stub
        headerTitle.setText(val);
        if ((Global.consignmentType == Global.OrderType.CONSIGNMENT_FILLUP || Global.consignmentType == Global.OrderType.CONSIGNMENT_RETURN) && rightFragment != null) {
            rightFragment.loadCursor();
        }
    }

    private void fragOnKeyDown(int key_code) {
        if (key_code == 0) {
            if (scannerInDecodeMode) {
                try {
                    mDecodeManager.cancelDecode();
                    scannerInDecodeMode = false;
                    DoScan();
                } catch (RemoteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else
                DoScan();
        }
    }

    // -----Honeywell scanner
    private void DoScan() {
        try {
            if (mDecodeManager != null) {
                mDecodeManager.doDecode(SCANTIMEOUT);
                scannerInDecodeMode = true;
            }
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    private void scanAddItem(String upc) {
        ProductsHandler handler = new ProductsHandler(this);
        Product product = handler.getUPCProducts(upc);
        if (product.getId() != null) {

            if (myPref.getPreferences(MyPreferences.pref_fast_scanning_mode)) {
                if (validAutomaticAddQty(product)) {
                    if (myPref.getPreferences(MyPreferences.pref_group_receipt_by_sku)) {
                        int foundPosition = global.checkIfGroupBySKU(this, product.getId(), "1");
                        if (foundPosition != -1) // product already exist in
                        // list
                        {
                            global.refreshParticularOrder(OrderingMain_FA.this, foundPosition, product);
                        } else
                            Catalog_FR.instance.automaticAddOrder(product);// temp.automaticAddOrder(listData);
                    } else
                        Catalog_FR.instance.automaticAddOrder(product);
                    refreshView();
                } else {
                    Global.showPrompt(this, R.string.dlog_title_error, this.getString(R.string.limit_onhand));
                }

            } else {
                Catalog_FR.instance.searchUPC(upc);
            }
        }
    }

    public boolean validAutomaticAddQty(Product product) {
        String addedQty = global.qtyCounter.get(product.getId()) == null ? "0" : global.qtyCounter.get(product.getId());
        double newQty = Double.parseDouble(addedQty) + 1;
        double onHandQty = Double.parseDouble(product.getProdOnHand());
        return !((myPref.getPreferences(MyPreferences.pref_limit_products_on_hand) && !product.getProdType().equals("Service")
                && (((Global.ord_type == Global.OrderType.SALES_RECEIPT || Global.ord_type == Global.OrderType.INVOICE)
                && (newQty > onHandQty))))
                || (Global.isConsignment && !product.getProdType().equals("Service")
                && !validConsignment(newQty, onHandQty, product.getId())));
    }

    private boolean validConsignment(double selectedQty, double onHandQty, String prodID) {
        if (Global.isConsignment) {
            if (Global.consignmentType == Global.OrderType.CONSIGNMENT_FILLUP && (onHandQty <= 0 || selectedQty > onHandQty))
                return false;
            else if (Global.consignmentType != Global.OrderType.CONSIGNMENT_FILLUP && !Global.custInventoryMap.containsKey(prodID))
                return false;
            else if (Global.consignmentType != Global.OrderType.CONSIGNMENT_FILLUP) {
                if (Global.consignmentType == Global.OrderType.ORDER
                        && selectedQty > Double.parseDouble(Global.custInventoryMap.get(prodID)[2]))
                    return false;
                else if (Global.consignmentType == Global.OrderType.CONSIGNMENT_RETURN) {
                    if (Global.consignment_qtyCounter != null && Global.consignment_qtyCounter.containsKey(prodID)) // verify
                    // rack
                    {
                        double rackQty = Double.parseDouble(Global.consignment_qtyCounter.get(prodID));
                        double origQty = Double.parseDouble(Global.custInventoryMap.get(prodID)[2]);
                        if (rackQty == origQty || (rackQty + selectedQty > origQty))
                            return false;
                    }
                } else if (Global.consignmentType == Global.OrderType.CONSIGNMENT_PICKUP
                        && selectedQty > Double.parseDouble(Global.custInventoryMap.get(prodID)[2]))
                    return false;
            }

        }
        return true;
    }

    private boolean loyaltySwiped = false;
    private Dialog dlogMSR;
    private EditText swiperHiddenField;

    @Override
    public void startLoyaltySwiper() {
        // TODO Auto-generated method stub
        showMSRprompt(true);
        loyaltySwiped = true;
    }

    @Override
    public void startRewardSwiper() {
        // TODO Auto-generated method stub
        showMSRprompt(false);
        loyaltySwiped = false;
    }

    @SuppressWarnings("deprecation")
    private void setUpCardReader() {
        Global.isEncryptSwipe = false;
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (audioManager.isWiredHeadsetOn()) {
            String _audio_reader_type = myPref.getPreferencesValue(MyPreferences.pref_audio_card_reader);
            if (_audio_reader_type != null && !_audio_reader_type.isEmpty() && !_audio_reader_type.equals("-1")) {
                if (_audio_reader_type.equals(Global.AUDIO_MSR_UNIMAG)) {
                    uniMagReader = new EMSUniMagDriver();
                    uniMagReader.initializeReader(this);
                } else if (_audio_reader_type.equals(Global.AUDIO_MSR_MAGTEK)) {
                    magtekReader = new EMSMagtekAudioCardReader(this);
                    new Thread(new Runnable() {
                        public void run() {
                            magtekReader.connectMagtek(true, callBackMSR);
                        }
                    }).start();
                } else if (_audio_reader_type.equals(Global.AUDIO_MSR_ROVER)) {
                    roverReader = new EMSRover();
                    roverReader.initializeReader(this, false);
                }
            }

        } else {
            int _swiper_type = myPref.swiperType(true, -2);
            int _printer_type = myPref.getPrinterType();
            if (_swiper_type != -1 && Global.btSwiper != null && Global.btSwiper.currentDevice != null
                    && !cardReaderConnected) {
                Global.btSwiper.currentDevice.loadCardReader(callBackMSR, false);
            } else if (_printer_type != -1 && Global.deviceHasMSR(_printer_type)) {
                if (Global.mainPrinterManager != null && Global.mainPrinterManager.currentDevice != null
                        && !cardReaderConnected)
                    Global.mainPrinterManager.currentDevice.loadCardReader(callBackMSR, false);
            } else {
                swiperLabel.setText(R.string.disconnected);
                swiperLabel.setTextColor(Color.RED);
            }
        }
        // }
        if (myPref.isET1(true, false) || myPref.isMC40(true, false)) {
            swiperLabel.setText(R.string.connected);
            swiperLabel.setTextColor(Color.BLUE);
            msrWasLoaded = true;
        } else if (myPref.isSam4s(true, false)) {
            _msrUsbSams = new EMSIDTechUSB(this, callBackMSR);
            _msrUsbSams.OpenDevice();
            if (_msrUsbSams.isDeviceOpen() && !_msrUsbSams.isDeviceReading())
                _msrUsbSams.StartReadingThread();

            swiperLabel.setText(R.string.connected);
            swiperLabel.setTextColor(Color.BLUE);
            msrWasLoaded = true;
        }
    }

    private void showMSRprompt(final boolean isLoyaltyCard) {
        if (_msrUsbSams != null && _msrUsbSams.isDeviceOpen() && !_msrUsbSams.isDeviceReading())
            _msrUsbSams.StartReadingThread();

        dlogMSR = new Dialog(this, R.style.Theme_TransparentTest);
        dlogMSR.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dlogMSR.setCancelable(false);
        dlogMSR.setCanceledOnTouchOutside(false);
        dlogMSR.setContentView(R.layout.dlog_swiper_layout);

        swiperLabel = (TextView) dlogMSR.findViewById(R.id.dlogMessage);
        Button btnOK = (Button) dlogMSR.findViewById(R.id.btnDlogLeft);
        Button btnCancel = (Button) dlogMSR.findViewById(R.id.btnDlogRight);
        swiperField = (EditText) dlogMSR.findViewById(R.id.dlogFieldSingle);
        swiperHiddenField = (EditText) dlogMSR.findViewById(R.id.hiddenField);
        swiperHiddenField.addTextChangedListener(hiddenTxtWatcher(swiperHiddenField));
        swiperLabel.setText(R.string.loading);
        swiperLabel.setTextColor(Color.DKGRAY);

        btnOK.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                dlogMSR.dismiss();

                String temp = swiperField.getText().toString().trim();
                if (temp.length() > 0) {
                    processBalanceInquiry(isLoyaltyCard);
                }
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                dlogMSR.dismiss();
                if (_msrUsbSams != null && _msrUsbSams.isDeviceOpen() && _msrUsbSams.isDeviceReading())
                    _msrUsbSams.StopReadingThread();
            }
        });
        dlogMSR.show();
        if (!msrWasLoaded)
            setUpCardReader();
        else {
            swiperLabel.setText(R.string.connected);
            swiperLabel.setTextColor(Color.BLUE);
        }
    }

    private TextWatcher hiddenTxtWatcher(final EditText hiddenField) {

        TextWatcher tw = new TextWatcher() {
            boolean doneScanning = false;
            String temp;

            @Override
            public void afterTextChanged(Editable s) {
                if (doneScanning) {
                    doneScanning = false;
                    String data = hiddenField.getText().toString().replace("\n", "");
                    hiddenField.setText("");
                    cardInfoManager = Global.parseSimpleMSR(OrderingMain_FA.this, data);
                    swiperField.setText(cardInfoManager.getCardNumUnencrypted());
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
                temp = s.toString();
                if (temp.contains(";") && temp.contains("?") && temp.contains("\n"))
                    doneScanning = true;

            }
        };
        return tw;
    }

    private void populateCardInfo() {
        if (!wasReadFromReader) {
            Encrypt encrypt = new Encrypt(this);
            cardInfoManager = new CreditCardInfo();
            int size = swiperField.getText().toString().length();
            if (size > 4) {
                String last4Digits = (String) swiperField.getText().toString().subSequence(size - 4, size);
                cardInfoManager.setCardLast4(last4Digits);
            }
            cardInfoManager.setCardNumAESEncrypted(encrypt.encryptWithAES(swiperField.getText().toString()));
            cardInfoManager.setCardNumUnencrypted(swiperField.getText().toString());
            cardInfoManager.setWasSwiped(false);
            if (loyaltySwiped)
                Global.loyaltyCardInfo = cardInfoManager;
            else
                Global.rewardCardInfo = cardInfoManager;
        }
    }

    private void processBalanceInquiry(boolean isLoyaltyCard) {
        Payment payment = new Payment(this);
        populateCardInfo();

        PayMethodsHandler payHandler = new PayMethodsHandler(this);

        if (isLoyaltyCard)
            payment.paymethod_id = PayMethodsHandler.getPayMethodID("LoyaltyCard");
        else
            payment.paymethod_id = PayMethodsHandler.getPayMethodID("Reward");

        payment.pay_name = cardInfoManager.getCardOwnerName();
        payment.pay_ccnum = cardInfoManager.getCardNumAESEncrypted();

        payment.ccnum_last4 = cardInfoManager.getCardLast4();
        payment.pay_expmonth = cardInfoManager.getCardExpMonth();
        payment.pay_expyear = cardInfoManager.getCardExpYear();
        payment.pay_seccode = cardInfoManager.getCardEncryptedSecCode();

        payment.track_one = cardInfoManager.getEncryptedAESTrack1();
        payment.track_two = cardInfoManager.getEncryptedAESTrack2();

        String cardType = "LoyaltyCard";
        if (!isLoyaltyCard)
            cardType = "Reward";

        payment.card_type = cardType;
        payment.pay_type = "0";

        EMSPayGate_Default payGate = new EMSPayGate_Default(this, payment);
        String generatedURL;

        if (isLoyaltyCard) {
            generatedURL = payGate.paymentWithAction("BalanceLoyaltyCardAction", wasReadFromReader, cardType,
                    cardInfoManager);
        } else {
            generatedURL = payGate.paymentWithAction("BalanceRewardAction", wasReadFromReader, cardType,
                    cardInfoManager);
        }

        new processAsync().execute(generatedURL);
    }

    public int getSelectedSeatsAmount() {
        return Integer.parseInt(selectedSeatsAmount);
    }

    public String getSelectedDinningTableNumber() {
        return selectedDinningTableNumber;
    }

    public static String getSelectedSeatNumber() {
        return selectedSeatNumber;
    }

    private class DeviceLoad extends AsyncTask<EMSCallBack, Void, Void> {

        @Override
        protected void onPreExecute() {
            myProgressDialog = new ProgressDialog(OrderingMain_FA.this);
            myProgressDialog.setMessage("Processing Balance Inquiry...");
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.setCancelable(false);
            myProgressDialog.show();

        }

        @Override
        protected Void doInBackground(EMSCallBack... params) {
            Global.mainPrinterManager.currentDevice.loadScanner(params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            myProgressDialog.dismiss();
        }
    }

    private class processAsync extends AsyncTask<String, String, String> {

        private HashMap<String, String> parsedMap = new HashMap<String, String>();
        private String urlToPost;
        private boolean wasProcessed = false;
        private String errorMsg = "Request could not be processed.";

        @Override
        protected void onPreExecute() {
            myProgressDialog = new ProgressDialog(OrderingMain_FA.this);
            myProgressDialog.setMessage("Processing Balance Inquiry...");
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.setCancelable(false);
            myProgressDialog.show();

            if (_msrUsbSams != null && _msrUsbSams.isDeviceOpen()) {
                _msrUsbSams.CloseTheDevice();
            }
        }

        @Override
        protected String doInBackground(String... params) {
            Post httpClient = new Post();

            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXProcessCardPayHandler handler = new SAXProcessCardPayHandler(OrderingMain_FA.this);
            urlToPost = params[0];

            try {
                String xml = httpClient.postData(13, OrderingMain_FA.this, urlToPost);
                if (xml.equals(Global.TIME_OUT)) {
                    errorMsg = "TIME OUT, would you like to try again?";
                } else if (xml.equals(Global.NOT_VALID_URL)) {
                    errorMsg = "Can not proceed...";
                } else {
                    InputSource inSource = new InputSource(new StringReader(xml));

                    SAXParser sp = spf.newSAXParser();
                    XMLReader xr = sp.getXMLReader();
                    xr.setContentHandler(handler);
                    xr.parse(inSource);
                    parsedMap = handler.getData();

                    if (parsedMap != null && parsedMap.size() > 0 && parsedMap.get("epayStatusCode").equals("APPROVED"))
                        wasProcessed = true;
                    else if (parsedMap != null && parsedMap.size() > 0) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("statusCode = ").append(parsedMap.get("statusCode")).append("\n");
                        sb.append(parsedMap.get("statusMessage"));
                        errorMsg = sb.toString();
                    } else
                        errorMsg = xml;
                }

            } catch (Exception e) {

            }

            return null;
        }

        @Override
        protected void onPostExecute(String unused) {
            myProgressDialog.dismiss();

            if (wasProcessed) // payment processing succeeded
            {
                StringBuilder sb = new StringBuilder();
                String temp = (parsedMap.get("CardBalance") == null ? "0.0" : parsedMap.get("CardBalance"));
                sb.append("Card Balance: ").append(Global.getCurrencyFrmt(temp));
                if (loyaltySwiped) {
                    OrderLoyalty_FR.getFrag().hideTapButton();
                    OrderLoyalty_FR.setPointBalance(temp);
                } else {
                    OrderRewards_FR.getFrag().hideTapButton();
                    OrderRewards_FR.setRewardBalance(temp);
                    rewardsWasRead = true;
                }

            } else // payment processing failed
            {
                Global.showPrompt(OrderingMain_FA.this, R.string.dlog_title_error, errorMsg);
            }
        }
    }

    @Override
    public void cardWasReadSuccessfully(boolean read, CreditCardInfo cardManager) {
        wasReadFromReader = true;
        this.cardInfoManager = cardManager;
        cardInfoManager.setWasSwiped(true);
        if (loyaltySwiped)
            Global.loyaltyCardInfo = cardInfoManager;
        else
            Global.rewardCardInfo = cardInfoManager;
        swiperField.setText(cardManager.getCardNumUnencrypted());
        if (uniMagReader != null && uniMagReader.readerIsConnected()) {
            uniMagReader.startReading();
        } else if (magtekReader == null && Global.btSwiper == null && _msrUsbSams == null
                && Global.mainPrinterManager != null)
            Global.mainPrinterManager.currentDevice.loadCardReader(callBackMSR, false);
    }

    @Override
    public void readerConnectedSuccessfully(boolean didConnect) {
        if (didConnect) {
            msrWasLoaded = true;
            cardReaderConnected = true;
            if (uniMagReader != null && uniMagReader.readerIsConnected())
                uniMagReader.startReading();
            swiperLabel.setText(R.string.connected);
            swiperLabel.setTextColor(Color.BLUE);
        } else {
            cardReaderConnected = false;
            swiperLabel.setText(R.string.disconnected);
            swiperLabel.setTextColor(Color.RED);
        }
    }

    @Override
    public void scannerWasRead(String data) {
        if (!data.isEmpty()) {
            scanAddItem(data);
        }
    }

    private void voidTransaction() {
        if (!Global.lastOrdID.isEmpty()) {
            OrdersHandler dbOrders = new OrdersHandler(this);
            if (global.order.ord_id.isEmpty()) {
                global.order = Receipt_FR.buildOrder(this, global, myPref, "");
                OrderProductsHandler dbOrdProd = new OrderProductsHandler(this);
                OrderProductsAttr_DB dbOrdAttr = new OrderProductsAttr_DB(this);
                dbOrders.insert(global.order);
                dbOrdProd.insert(global.orderProducts);
                dbOrdAttr.insert(global.ordProdAttr);
            }

            dbOrders.updateIsVoid(Global.lastOrdID);

            VoidTransactionsHandler voidHandler = new VoidTransactionsHandler(this);

            Order order = new Order(this);
            order.ord_id = Global.lastOrdID;
            order.ord_type = global.order.ord_type;
            voidHandler.insert(order);

        }
    }

    private void deleteTransaction() {
        if (!Global.lastOrdID.isEmpty()) {
            OrdersHandler dbOrders = new OrdersHandler(this);
            OrderProductsHandler dbOrdProd = new OrderProductsHandler(this);
            OrderProductsAttr_DB dbOrdAttr = new OrderProductsAttr_DB(this);

            dbOrders.deleteOrder(Global.lastOrdID);
            dbOrdProd.deleteAllOrdProd(Global.lastOrdID);
            for (OrdProdAttrHolder val : global.ordProdAttr)
                dbOrdAttr.deleteOrderProduct(val.ordprod_id);
        }
    }

    @Override
    public void startSignature() {
    }

    public static void automaticAddOrder(Activity activity, boolean isFromAddon, Global global, Product product) {
        Orders order = new Orders();
        OrderProduct ord = new OrderProduct();

        int sum = 0;
        if (global.qtyCounter.containsKey(product.getId()))
            sum = Integer.parseInt(global.qtyCounter.get(product.getId()));

        if (!OrderingMain_FA.returnItem || OrderingMain_FA.mTransType == Global.TransactionType.RETURN)
            global.qtyCounter.put(product.getId(), Integer.toString(sum + 1));
        else
            global.qtyCounter.put(product.getId(), Integer.toString(sum - 1));
        if (OrderingMain_FA.returnItem)
            ord.isReturned = true;

        order.setName(product.getProdName());
        order.setValue(product.getProdPrice());
        order.setProdID(product.getId());
        order.setDiscount("0.00");
        order.setTax("0.00");
        order.setDistQty("0");
        order.setTaxQty("0");

        String val = product.getProdPrice();
        if (val.isEmpty() || val == null)
            val = "0.00";

        BigDecimal total = Global.getBigDecimalNum(Global.formatNumToLocale(Double.parseDouble(val)));
        if (isFromAddon) {
            total = total.add(Global.getBigDecimalNum(Global.formatNumToLocale(Global.addonTotalAmount)));
        }

        ord.overwrite_price = total.toString();
        ord.prod_price = total.toString();
        ord.assignedSeat= getSelectedSeatNumber();
        total = total.multiply(OrderingMain_FA.returnItem && OrderingMain_FA.mTransType != Global.TransactionType.RETURN ? new BigDecimal(-1) : new BigDecimal(1));

        DecimalFormat frmt = new DecimalFormat("0.00");
        order.setTotal(frmt.format(total));

        ord.prod_istaxable = product.getProdIstaxable();
        ord.prod_taxtype = product.getProdTaxType();
        ord.prod_taxcode = product.getProdTaxCode();

        // add order to db
        ord.ordprod_qty = OrderingMain_FA.returnItem && OrderingMain_FA.mTransType != Global.TransactionType.RETURN ? "-1" : "1";
        ord.ordprod_name = product.getProdName();
        ord.ordprod_desc = product.getProdDesc();
        ord.prod_id = product.getId();

        ord.onHand = product.getProdOnHand();
        ord.imgURL = product.getProdImgName();
        ord.cat_id = product.getCatId();
        try {
            ord.prod_price_points = product.getProdPricePoints();
            ord.prod_value_points = product.getProdValuePoints();
        } catch (Exception e) {

        }

        // Still need to do add the appropriate tax/discount value
        ord.prod_taxValue = "0.00";
        ord.discount_value = "0.00";

        ord.taxAmount = "0";
        ord.taxTotal = "0.00";
        ord.disAmount = "0";
        ord.disTotal = "0.00";
        ord.itemTotal = total.toString();
        ord.itemSubtotal = total.toString();

        ord.tax_position = "0";
        ord.discount_position = "0";
        ord.pricelevel_position = "0";
        ord.uom_position = "0";

        ord.prod_price_updated = "0";
        // OrdersHandler handler = new OrdersHandler(activity);

        GenerateNewID generator = new GenerateNewID(activity);

        MyPreferences myPref = new MyPreferences(activity);
        // myPref.setLastOrdID(generator.getNextID(myPref.getLastOrdID()));

        // if(!Global.isFromOnHold)
        // {
        // if (handler.getDBSize() == 0)
        // Global.lastOrdID = generator.generate("",0);
        // else
        // Global.lastOrdID = generator.generate(handler.getLastOrdID(),0);
        // }

        if (!Global.isFromOnHold && Global.lastOrdID.isEmpty()) {
            Global.lastOrdID = generator.getNextID(GenerateNewID.IdType.ORDER_ID);
        }

        ord.ord_id = Global.lastOrdID;

        if (global.orderProducts == null) {
            global.orderProducts = new ArrayList<OrderProduct>();
        }

        UUID uuid = UUID.randomUUID();
        String randomUUIDString = uuid.toString();

        ord.ordprod_id = randomUUIDString;

        if (isFromAddon) {
            Global.addonTotalAmount = 0;

            if (Global.addonSelectionMap == null)
                Global.addonSelectionMap = new HashMap<String, HashMap<String, String[]>>();
            if (Global.orderProductAddonsMap == null)
                Global.orderProductAddonsMap = new HashMap<String, List<OrderProduct>>();

            if (global.addonSelectionType.size() > 0) {
                StringBuilder sb = new StringBuilder();
                Global.addonSelectionMap.put(randomUUIDString, global.addonSelectionType);
                Global.orderProductAddonsMap.put(randomUUIDString, global.orderProductAddons);

                sb.append(ord.ordprod_desc);
                int tempSize = global.orderProductAddons.size();
                for (int i = 0; i < tempSize; i++) {

                    sb.append("<br/>");
                    if (global.orderProductAddons.get(i).isAdded.equals("0")) // Not
                        // added
                        sb.append("[NO ").append(global.orderProductAddons.get(i).ordprod_name).append("]");
                    else
                        sb.append("[").append(global.orderProductAddons.get(i).ordprod_name).append("]");

                }
                ord.ordprod_desc = sb.toString();
                ord.hasAddons = "1";

                global.orderProductAddons = new ArrayList<OrderProduct>();

            }
        }
        String row1 = ord.ordprod_name;
        String row2 = Global.formatDoubleStrToCurrency(product.getProdPrice());
        TerminalDisplay.setTerminalDisplay(myPref, row1, row2);
        global.orderProducts.add(ord);
    }

}
