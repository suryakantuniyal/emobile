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
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.android.dao.OrderProductAttributeDAO;
import com.android.database.AddressHandler;
import com.android.database.CustomerInventoryHandler;
import com.android.database.DBManager;
import com.android.database.OrderProductsAttr_DB;
import com.android.database.OrderProductsHandler;
import com.android.database.OrdersHandler;
import com.android.database.PayMethodsHandler;
import com.android.database.ProductsHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.adapters.OrderProductListAdapter;
import com.android.emobilepos.mainmenu.MainMenu_FA;
import com.android.emobilepos.mainmenu.SalesTab_FR;
import com.android.emobilepos.models.Order;
import com.android.emobilepos.models.OrderProduct;
import com.android.emobilepos.models.OrderSeatProduct;
import com.android.emobilepos.models.Orders;
import com.android.emobilepos.models.Payment;
import com.android.emobilepos.models.Product;
import com.android.emobilepos.models.ProductAttribute;
import com.android.emobilepos.payment.SelectPayMethod_FA;
import com.android.payments.EMSPayGate_Default;
import com.android.saxhandler.SAXProcessCardPayHandler;
import com.android.soundmanager.SoundManager;
import com.android.support.CreditCardInfo;
import com.android.support.Encrypt;
import com.android.support.GenerateNewID;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.NetworkUtils;
import com.android.support.OrderProductUtils;
import com.android.support.Post;
import com.android.support.TerminalDisplay;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;
import com.google.gson.Gson;
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
import interfaces.EMSCallBack;
import io.realm.RealmResults;
import util.json.JsonUtils;

public class OrderingMain_FA extends BaseFragmentActivityActionBar implements Receipt_FR.AddProductBtnCallback,
        Receipt_FR.UpdateHeaderTitleCallback, OnClickListener, Catalog_FR.RefreshReceiptViewCallback,
        OrderLoyalty_FR.SwiperLoyaltyCallback, OrderRewards_FR.SwiperRewardCallback, EMSCallBack {

    private static String ourIntentAction = "";
    private static final String DATA_STRING_TAG = "com.motorolasolutions.emdk.datawedge.data_string";
    private int orientation;
    private LinearLayout catalogContainer, receiptContainer;
    public static OrderingMain_FA instance;
    private Catalog_FR rightFragment;
    private Receipt_FR leftFragment;
    private MyPreferences myPref;
    private Global global;
    private boolean hasBeenCreated = false;
    private ProductsHandler handler;
    public static EditText invisibleSearchMain;
    private static TextView headerTitle;

    // Honeywell Dolphin black
    private DecodeManager mDecodeManager = null;
    private final int SCANTIMEOUT = 500000;
    private boolean scannerInDecodeMode = false;

    public EMSIDTechUSB _msrUsbSams;
    public EMSCallBack callBackMSR;
    private EMSUniMagDriver uniMagReader;
    private EMSMagtekAudioCardReader magtekReader;
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
    private Global.RestaurantSaleType restaurantSaleType = Global.RestaurantSaleType.TO_GO;
    public boolean isToGo = true;
    private int selectedSeatsAmount;
    private String selectedDinningTableNumber;
    private String selectedSeatNumber = "1";
    public boolean openFromHold;
    OrderingAction orderingAction = OrderingAction.NONE;
    private String associateId;


    public enum OrderingAction {
        HOLD, CHECKOUT, NONE, BACK_PRESSED
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        rewardsWasRead = savedInstanceState.getBoolean("rewardsWasRead");
        if (rewardsWasRead) {
            OrderRewards_FR.getFrag().hideTapButton();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("rewardsWasRead", rewardsWasRead);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_main_layout);
        instance = this;
        callBackMSR = this;
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
        setRestaurantSaleType((Global.RestaurantSaleType) extras.get("RestaurantSaleType"));
        selectedSeatsAmount = extras.getInt("selectedSeatsAmount", 0);
        selectedDinningTableNumber = extras.getString("selectedDinningTableNumber");
        setAssociateId(extras.getString("associateId", ""));

        openFromHold = extras.getBoolean("openFromHold", false);
        String onHoldOrderJson = extras.getString("onHoldOrderJson");
        Order onHoldOrder = null;
        if (onHoldOrderJson != null && !onHoldOrderJson.isEmpty()) {
            Gson gson = JsonUtils.getInstance();
            onHoldOrder = gson.fromJson(onHoldOrderJson, Order.class);
        }
        isToGo = getRestaurantSaleType() == Global.RestaurantSaleType.TO_GO;

        returnItem = mTransType == Global.TransactionType.RETURN;
        if (!myPref.getIsTablet())
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (savedInstanceState == null) {
            if (!myPref.getIsTablet())
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            orientation = getResources().getConfiguration().orientation;
            rightFragment = new Catalog_FR();
            if (onHoldOrder == null) {
                leftFragment = new Receipt_FR();
            } else {
                leftFragment = Receipt_FR.getInstance(onHoldOrder);//new Receipt_FR(onHoldOrder);
            }

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
                    String strDecodeResult;
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

        headerContainer = (LinearLayout) findViewById(R.id.headerTitleContainer);
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
                        CustomerInventoryHandler custInventoryHandler = new CustomerInventoryHandler(this);
                        custInventoryHandler.getCustomerInventory();
                        Global.consignSummaryMap = new HashMap<>();
                        Global.consignMapKey = new ArrayList<>();
                        Global.isConsignment = true;

                        // consignmentType 0 = Rack, 1 = Returns, 2 = Fill-up, 3 =
                        // Pick-up
                        Global.OrderType consignmentType = (Global.OrderType) extras.get("consignmentType");

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
    private static LinearLayout headerContainer;

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
            if (_orientation == Configuration.ORIENTATION_PORTRAIT) {
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


    @Override
    public void onBackPressed() {
        boolean tablet = myPref.getIsTablet();
        orderingAction = OrderingAction.BACK_PRESSED;
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
    }

    @Override
    public void addProductServices() {
        catalogContainer.startAnimation(AnimationUtils.loadAnimation(this, R.anim.anim_right_left));
        catalogContainer.setVisibility(View.VISIBLE);
        receiptContainer.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        if (SystemClock.elapsedRealtime() - Receipt_FR.lastClickTime < 1000) {
            return;
        }
        Receipt_FR.lastClickTime = SystemClock.elapsedRealtime();
        switch (v.getId()) {
            case R.id.btnCheckOut:
                orderingAction = OrderingAction.CHECKOUT;
                btnCheckout.setEnabled(false);
                if (leftFragment != null) {
                    leftFragment.checkoutOrder();
                }
                btnCheckout.setEnabled(true);
                break;
            case R.id.headerMenubutton:
                showSeatHeaderPopMenu(v);
                break;
        }
    }


    private void showSeatHeaderPopMenu(final View v) {
        final OrderSeatProduct orderSeatProduct = (OrderSeatProduct) v.getTag();
        PopupMenu popup = new PopupMenu(this, v);
        popup.getMenuInflater().inflate(R.menu.receiptlist_header_menu, popup.getMenu());
        final HashMap<Integer, String> subMenus = new HashMap<>();
        final HashMap<Integer, String> subMenusJoinSeat = new HashMap<>();
        Receipt_FR.receiptListView.smoothScrollToPosition(leftFragment.mainLVAdapter.orderSeatProductList.indexOf(orderSeatProduct));
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.deleteSeat:
                        removeSeat(orderSeatProduct.seatNumber);
                        if (leftFragment.mainLVAdapter.orderSeatProductList.size() > 0) {
                            setSelectedSeatNumber(leftFragment.mainLVAdapter.orderSeatProductList.get(0).seatNumber);
                        }
                        break;
                    case R.id.moveSeatItems:
                        int id = 0;
                        for (OrderSeatProduct seatProduct : leftFragment.mainLVAdapter.orderSeatProductList) {
                            if (seatProduct.rowType == OrderProductListAdapter.RowType.TYPE_HEADER) {
                                if (!seatProduct.seatNumber.equalsIgnoreCase(orderSeatProduct.seatNumber)) {
                                    item.getSubMenu().add(0, id, SubMenu.NONE, getString(R.string.move_items_to_seat) + seatProduct.seatNumber);
                                    subMenus.put(id, seatProduct.seatNumber);
                                    id++;
                                }
                            }
                        }
                        break;
                    case R.id.joinSeats:
                        int jid = 100;
                        for (OrderSeatProduct seatProduct : leftFragment.mainLVAdapter.orderSeatProductList) {
                            if (seatProduct.rowType == OrderProductListAdapter.RowType.TYPE_HEADER) {
                                if (!seatProduct.seatNumber.equalsIgnoreCase(orderSeatProduct.seatNumber)) {
                                    item.getSubMenu().add(0, jid, SubMenu.NONE, getString(R.string.join_with_seat) + seatProduct.seatNumber);
                                    subMenusJoinSeat.put(jid, seatProduct.seatNumber);
                                    jid++;
                                }
                            }
                        }
                        break;
                    default:
                        if (subMenus.containsKey(item.getItemId())) {
                            String targetSeat = subMenus.get(item.getItemId());
                            setSelectedSeatNumber(targetSeat);
                            leftFragment.mainLVAdapter.moveSeatItems(leftFragment.mainLVAdapter.getOrderProducts(orderSeatProduct.seatNumber), targetSeat);
                        } else if (subMenusJoinSeat.containsKey(item.getItemId())) {
                            String targetSeatNumber = subMenusJoinSeat.get(item.getItemId());
                            OrderSeatProduct targetSeat = leftFragment.mainLVAdapter.getSeat(targetSeatNumber);
                            leftFragment.mainLVAdapter.joinSeatsGroupId(orderSeatProduct.getSeatGroupId(), targetSeat.getSeatGroupId());
                            leftFragment.mainLVAdapter.notifyDataSetChanged();
                        }

                        break;
                }
                return true;
            }
        });
        List<OrderProduct> orderProducts = leftFragment.mainLVAdapter.getOrderProducts(orderSeatProduct.seatNumber);
        popup.getMenu().findItem(R.id.deleteSeat).setEnabled(orderProducts.isEmpty() && leftFragment.mainLVAdapter.getSeatsAmount() > 1);
        popup.getMenu().findItem(R.id.moveSeatItems).setEnabled(!orderProducts.isEmpty() && leftFragment.mainLVAdapter.getSeatsAmount() > 1);
        popup.getMenu().findItem(R.id.joinSeats).setEnabled(!orderProducts.isEmpty() && leftFragment.mainLVAdapter.getSeatsAmount() > 1);
        popup.show();
    }


    private void removeSeat(String seatNumber) {
        leftFragment.mainLVAdapter.removeSeat(seatNumber);
        leftFragment.mainLVAdapter.notifyDataSetChanged();
        leftFragment.reCalculate();
    }


    @Override
    public void refreshView() {
        if (leftFragment != null)
            leftFragment.refreshView();
        if (rightFragment != null)
            rightFragment.refreshListView();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Global.FROM_DRAW_RECEIPT_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
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
            Global.showCDTDefault(this);
            reloadDefaultTransaction();
        } else if (resultCode == 9) {

        } else if (resultCode == 2 || resultCode == 0)
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
            if (myPref.getPreferences(MyPreferences.pref_automatic_sync) && NetworkUtils.isConnectedToInternet(this)) {
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
                && orderingAction == OrderingAction.BACK_PRESSED)) {
            if (mDecodeManager != null) {
                try {
                    mDecodeManager.release();
                    mDecodeManager = null;
                } catch (IOException e) {
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
            String temp;
            for (int i = 0; i < size; i++) {

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

        btnLeft.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dlog.dismiss();
                if (isFromOnHold)
                    leftFragment.voidCancelOnHold(1);
                else {

                    if (mTransType == Global.TransactionType.SALE_RECEIPT) // is sales receipt
                        voidTransaction(OrderingMain_FA.this, global.order, global.orderProducts, global.ordProdAttr);
                    else if (mTransType == Global.TransactionType.CONSIGNMENT) {
                        if (Global.consignment_order != null && !Global.consignment_order.ord_id.isEmpty()) {
                            OrdersHandler.deleteTransaction(OrderingMain_FA.this, Global.consignment_order.ord_id);
                        }
                        if (Global.cons_return_order != null && !Global.cons_return_order.ord_id.isEmpty()) {
                            OrdersHandler.deleteTransaction(OrderingMain_FA.this, Global.cons_return_order.ord_id);
                        }
                        if (Global.cons_fillup_order != null && !Global.cons_fillup_order.ord_id.isEmpty()) {
                            OrdersHandler.deleteTransaction(OrderingMain_FA.this, Global.cons_fillup_order.ord_id);
                        }
                        if (Global.consignment_order != null && !Global.consignment_order.ord_id.isEmpty()) {
                            OrdersHandler.deleteTransaction(OrderingMain_FA.this, Global.consignment_order.ord_id);
                        }
                    } else {
                        OrdersHandler.deleteTransaction(OrderingMain_FA.this, global.order.ord_id);
                    }
                    global.resetOrderDetailsValues();
                    global.clearListViewData();
                    msrWasLoaded = false;
                    cardReaderConnected = false;
                    leftFragment.mainLVAdapter.notifyDataSetChanged();
                    Receipt_FR.receiptListView.invalidateViews();
                    finish();
                }
            }
        });
        btnRight.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dlog.dismiss();
                if (isFromOnHold)
                    leftFragment.voidCancelOnHold(2);
            }
        });
        dlog.show();
    }

    public Receipt_FR getLeftFragment() {
        return leftFragment;
    }

    private TextWatcher textWatcher() {

        return new TextWatcher() {
            boolean doneScanning = false;

            @Override
            public void afterTextChanged(Editable s) {
                if (doneScanning) {
                    doneScanning = false;
                    if (Global.mainPrinterManager != null && Global.mainPrinterManager.currentDevice != null) {
                        Global.mainPrinterManager.currentDevice.playSound();
                    }
                    String upc = invisibleSearchMain.getText().toString().trim().replace("\n", "").replace("\r", "");
//                    upc = invisibleSearchMain.getText().toString().trim().replace("\r", "");
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
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().contains("\n") || s.toString().contains("\r"))
                    doneScanning = true;
            }
        };
    }

    @Override
    public void updateHeaderTitle(String val) {
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
        List<OrderProduct> list = OrderProductUtils.getOrderProducts(global.orderProducts, product.getId());
        String addedQty = list.isEmpty() ? "0" : list.get(0).getOrdprod_qty();
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

    @Override
    public void startLoyaltySwiper() {
        showMSRprompt(true);
        loyaltySwiped = true;
    }

    @Override
    public void startRewardSwiper() {
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
                switch (_audio_reader_type) {
                    case Global.AUDIO_MSR_UNIMAG:
                        uniMagReader = new EMSUniMagDriver();
                        uniMagReader.initializeReader(this);
                        break;
                    case Global.AUDIO_MSR_MAGTEK:
                        magtekReader = new EMSMagtekAudioCardReader(this);
                        new Thread(new Runnable() {
                            public void run() {
                                magtekReader.connectMagtek(true, callBackMSR);
                            }
                        }).start();
                        break;
                    case Global.AUDIO_MSR_ROVER:
                        EMSRover roverReader = new EMSRover();
                        roverReader.initializeReader(this, false);
                        break;
                }
            }

        } else {
            int _swiper_type = myPref.getSwiperType();
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
        EditText swiperHiddenField = (EditText) dlogMSR.findViewById(R.id.hiddenField);
        swiperHiddenField.addTextChangedListener(hiddenTxtWatcher(swiperHiddenField));
        swiperLabel.setText(R.string.loading);
        swiperLabel.setTextColor(Color.DKGRAY);

        btnOK.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dlogMSR.dismiss();

                String temp = swiperField.getText().toString().trim();
                if (temp.length() > 0) {
                    processBalanceInquiry(isLoyaltyCard);
                }
            }
        });
        btnCancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
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

        return new TextWatcher() {
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
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                temp = s.toString();

                if (temp.contains(";") && temp.contains("?") && temp.contains("\n"))
                    doneScanning = true;

            }
        };
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

        if (isLoyaltyCard)
            payment.setPaymethod_id(PayMethodsHandler.getPayMethodID("LoyaltyCard"));
        else
            payment.setPaymethod_id(PayMethodsHandler.getPayMethodID("Reward"));

        payment.setPay_name(cardInfoManager.getCardOwnerName());
        payment.setPay_ccnum(cardInfoManager.getCardNumAESEncrypted());

        payment.setCcnum_last4(cardInfoManager.getCardLast4());
        payment.setPay_expmonth(cardInfoManager.getCardExpMonth());
        payment.setPay_expyear(cardInfoManager.getCardExpYear());
        payment.setPay_seccode(cardInfoManager.getCardEncryptedSecCode());

        payment.setTrack_one(cardInfoManager.getEncryptedAESTrack1());
        payment.setTrack_two(cardInfoManager.getEncryptedAESTrack2());

        String cardType = "LoyaltyCard";
        if (!isLoyaltyCard)
            cardType = "Reward";

        payment.setCard_type(cardType);
        payment.setPay_type("0");

        EMSPayGate_Default payGate = new EMSPayGate_Default(this, payment);
        String generatedURL;

        if (isLoyaltyCard) {
            generatedURL = payGate.paymentWithAction(EMSPayGate_Default.EAction.BalanceLoyaltyCardAction, wasReadFromReader, cardType,
                    cardInfoManager);
        } else {
            generatedURL = payGate.paymentWithAction(EMSPayGate_Default.EAction.BalanceRewardAction, wasReadFromReader, cardType,
                    cardInfoManager);
        }

        new processAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, generatedURL);
    }

    public int getSelectedSeatsAmount() {
        return selectedSeatsAmount;
    }


    public String getSelectedDinningTableNumber() {
        return selectedDinningTableNumber;
    }

    public String getSelectedSeatNumber() {
        return selectedSeatNumber;
    }

    public void setSelectedSeatNumber(String seatNumber) {
        selectedSeatNumber = seatNumber;

    }

    public Global.RestaurantSaleType getRestaurantSaleType() {
        return restaurantSaleType;
    }

    public void setRestaurantSaleType(Global.RestaurantSaleType restaurantSaleType) {
        if (restaurantSaleType == null) {
            this.restaurantSaleType = Global.RestaurantSaleType.TO_GO;
        } else {
            this.restaurantSaleType = restaurantSaleType;
        }
    }

    public void setSelectedDinningTableNumber(String tableNumber) {
        selectedDinningTableNumber = tableNumber;
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
        private HashMap<String, String> parsedMap = new HashMap<>();
        private String urlToPost;
        private boolean wasProcessed = false;
        private String errorMsg = "Request could not be processed.";

        @Override
        protected void onPreExecute() {
            int orientation = Global.getScreenOrientation(OrderingMain_FA.this);
            setRequestedOrientation(orientation);
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
                switch (xml) {
                    case Global.TIME_OUT:
                        errorMsg = getString(R.string.timeout_try_again);
                        break;
                    case Global.NOT_VALID_URL:
                        errorMsg = getString(R.string.can_not_proceed);
                        break;
                    default:
                        InputSource inSource = new InputSource(new StringReader(xml));

                        SAXParser sp = spf.newSAXParser();
                        XMLReader xr = sp.getXMLReader();
                        xr.setContentHandler(handler);
                        xr.parse(inSource);
                        parsedMap = handler.getData();

                        if (parsedMap != null && parsedMap.size() > 0 && parsedMap.get("epayStatusCode").equals("APPROVED"))
                            wasProcessed = true;
                        else if (parsedMap != null && parsedMap.size() > 0) {
                            errorMsg = "statusCode = " + parsedMap.get("statusCode") + "\n" + parsedMap.get("statusMessage");
                        } else
                            errorMsg = xml;
                        break;
                }

            } catch (Exception ignored) {
            }

            return null;
        }

        @Override
        protected void onPostExecute(String unused) {
            if (Global.isTablet(OrderingMain_FA.this))
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

            if (myProgressDialog != null && myProgressDialog.isShowing()) {
                myProgressDialog.dismiss();
            }

            if (wasProcessed) // payment processing succeeded
            {
                String temp = (parsedMap.get("CardBalance") == null ? "0.0" : parsedMap.get("CardBalance"));
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


    public static void voidTransaction(Activity activity, Order order, List<OrderProduct> orderProducts, List<ProductAttribute> ordProdAttr) {
        if (!Global.lastOrdID.isEmpty()) {

            OrdersHandler dbOrders = new OrdersHandler(activity);
            if (order.ord_id.isEmpty()) {
                Global global = (Global) activity.getApplication();
                order = Receipt_FR.buildOrder(activity, global, "", "", ((OrderingMain_FA) activity).getSelectedDinningTableNumber(), ((OrderingMain_FA) activity).getAssociateId());
                OrderProductsHandler dbOrdProd = new OrderProductsHandler(activity);
                OrderProductsAttr_DB dbOrdAttr = new OrderProductsAttr_DB(activity);
                dbOrders.insert(order);
                dbOrdProd.insert(orderProducts);
                dbOrdAttr.insert(ordProdAttr);
            }
            new VoidTransactionTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, activity, order);

        }
    }

    private static class VoidTransactionTask extends AsyncTask<Object, Void, Void> {

        @Override
        protected Void doInBackground(Object... params) {
            Order order = (Order) params[1];
            SelectPayMethod_FA.voidTransaction((Activity) params[0], order.ord_id, order.ord_type);

            return null;
        }
    }

    private void deleteTransaction() {
        if (!Global.lastOrdID.isEmpty()) {
            OrdersHandler dbOrders = new OrdersHandler(this);
            OrderProductsHandler dbOrdProd = new OrderProductsHandler(this);
            OrderProductsAttr_DB dbOrdAttr = new OrderProductsAttr_DB(this);
            dbOrders.deleteOrder(Global.lastOrdID);
            dbOrdProd.deleteAllOrdProd(Global.lastOrdID);
            for (ProductAttribute val : global.ordProdAttr)
                dbOrdAttr.deleteOrderProduct(String.valueOf(val.getProductId()));
        }
    }

    @Override
    public void startSignature() {
    }

    @Override
    public void nfcWasRead(String nfcUID) {

    }

    public static void automaticAddOrder(Activity activity, boolean isFromAddon, Global global, Product product, String selectedSeatNumber) {
        Orders order = new Orders();
        OrderProduct ord = new OrderProduct();
        int sum = Double.valueOf(OrderProductUtils.getOrderProductQty(global.orderProducts, product.getId())).intValue();//Integer.parseInt(global.qtyCounter.get(product.getId()));
        if (OrderingMain_FA.returnItem)
            ord.setReturned(true);
        order.setName(product.getProdName());
        order.setValue(product.getProdPrice());
        order.setProdID(product.getId());
        order.setDiscount("0.00");
        order.setTax("0.00");
        order.setDistQty("0");
        order.setTaxQty("0");

        String val = product.getProdPrice();
        if (val == null || val.isEmpty())
            val = "0.00";

        BigDecimal total = Global.getBigDecimalNum(Global.formatNumToLocale(Double.parseDouble(val)));
        if (isFromAddon) {
            total = total.add(Global.getBigDecimalNum(Global.formatNumToLocale(Global.addonTotalAmount)));
        }
        ord.addonsProducts = new ArrayList<>(global.orderProductAddons);
        ord.setPricesXGroupid(product.getPricesXGroupid());
        ord.setProd_price(total.toString());
        ord.setAssignedSeat(selectedSeatNumber);
        total = total.multiply(OrderingMain_FA.returnItem && OrderingMain_FA.mTransType != Global.TransactionType.RETURN ? new BigDecimal(-1) : new BigDecimal(1));

        DecimalFormat frmt = new DecimalFormat("0.00");
        order.setTotal(frmt.format(total));

        ord.setProd_istaxable(product.getProdIstaxable());
        ord.setProd_taxtype(product.getProdTaxType());
        ord.setProd_taxcode(product.getProdTaxCode());

        // add order to db
        ord.setOrdprod_qty(OrderingMain_FA.returnItem && OrderingMain_FA.mTransType != Global.TransactionType.RETURN ? "-1" : "1");
        ord.setOrdprod_name(product.getProdName());
        ord.setOrdprod_desc(product.getProdDesc());
        ord.setProd_id(product.getId());

        ord.setOnHand(product.getProdOnHand());
        ord.setImgURL(product.getProdImgName());
        ord.setCat_id(product.getCatId());

        try {
            ord.setProd_price_points(String.valueOf(product.getProdPricePoints()));
            ord.setProd_value_points(String.valueOf(product.getProdValuePoints()));
        } catch (Exception ignored) {

        }

        // Still need to do add the appropriate tax/discount value
        ord.setProd_taxValue(new BigDecimal("0.00"));
        ord.setDiscount_value("0.00");

        ord.setTaxAmount("0");
        ord.setTaxTotal("0.00");
        ord.setDisAmount("0");
        ord.setDisTotal("0.00");
        ord.setItemTotal(total.toString());
        ord.setItemSubtotal(total.toString());

        ord.setTax_position("0");
        ord.setDiscount_position("0");
        ord.setPricelevel_position("0");
        ord.setUom_position("0");

        ord.setProd_price_updated("0");
        GenerateNewID generator = new GenerateNewID(activity);

        MyPreferences myPref = new MyPreferences(activity);

        if (!Global.isFromOnHold && Global.lastOrdID.isEmpty()) {
            Global.lastOrdID = generator.getNextID(GenerateNewID.IdType.ORDER_ID);
        }

        ord.setOrd_id(Global.lastOrdID);

        if (global.orderProducts == null) {
            global.orderProducts = new ArrayList<>();
        }

        UUID uuid = UUID.randomUUID();
        String randomUUIDString = uuid.toString();

        ord.setOrdprod_id(randomUUIDString);

        if (isFromAddon) {
            Global.addonTotalAmount = 0;

            if (Global.addonSelectionMap == null)
                Global.addonSelectionMap = new HashMap<>();
            if (Global.orderProductAddonsMap == null)
                Global.orderProductAddonsMap = new HashMap<>();

            if (global.addonSelectionType.size() > 0) {
                StringBuilder sb = new StringBuilder();
                Global.addonSelectionMap.put(randomUUIDString, global.addonSelectionType);
                Global.orderProductAddonsMap.put(randomUUIDString, global.orderProductAddons);

                sb.append(ord.getOrdprod_desc());
                int tempSize = global.orderProductAddons.size();
                for (int i = 0; i < tempSize; i++) {

                    sb.append("<br/>");
                    if (global.orderProductAddons.get(i).getIsAdded().equals("0")) // Not
                        // added
                        sb.append("[NO ").append(global.orderProductAddons.get(i).getOrdprod_name()).append("]");
                    else
                        sb.append("[").append(global.orderProductAddons.get(i).getOrdprod_name()).append("]");

                }
                ord.setOrdprod_desc(sb.toString());
                ord.setHasAddons("1");

                global.orderProductAddons = new ArrayList<>();

            }
        }
        String row1 = ord.getOrdprod_name();
        String row2 = Global.formatDoubleStrToCurrency(product.getProdPrice());
        TerminalDisplay.setTerminalDisplay(myPref, row1, row2);
        global.orderProducts.add(ord);
    }

    public String getAssociateId() {
        return associateId;
    }

    public void setAssociateId(String associateId) {
        this.associateId = associateId;
    }

    public static boolean isRequiredAttributeConmpleted(List<OrderProduct> products) {
        for (OrderProduct product : products) {
            RealmResults<ProductAttribute> attributes = OrderProductAttributeDAO.getByProdId(product.getProd_id());
            for (ProductAttribute attribute : attributes) {
                if (!product.getRequiredProductAttributes().contains(attribute)) {
                    return false;
                }
            }
        }
        return true;

    }
}
