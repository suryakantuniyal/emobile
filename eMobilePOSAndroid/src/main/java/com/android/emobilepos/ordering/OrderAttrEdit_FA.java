package com.android.emobilepos.ordering;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.android.emobilepos.R;
import com.android.soundmanager.SoundManager;
import com.android.support.CreditCardInfo;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;
import com.android.support.textwatcher.GiftCardTextWatcher;
import com.android.support.Global;
import com.android.support.MyEditText;
import com.android.support.MyPreferences;
import com.honeywell.decodemanager.DecodeManager;
import com.honeywell.decodemanager.DecodeManager.SymConfigActivityOpeartor;
import com.honeywell.decodemanager.SymbologyConfigs;
import com.honeywell.decodemanager.barcode.CommonDefine;
import com.honeywell.decodemanager.barcode.DecodeResult;
import com.honeywell.decodemanager.symbologyconfig.SymbologyConfigCodeUPCA;

import java.io.IOException;

import drivers.EMSIDTechUSB;
import drivers.EMSMagtekAudioCardReader;
import drivers.EMSRover;
import drivers.EMSUniMagDriver;
import protocols.EMSCallBack;

public class OrderAttrEdit_FA extends BaseFragmentActivityActionBar
        implements OnClickListener, OnCheckedChangeListener, EMSCallBack {

    private MyEditText fieldCardNum, fieldComment;
    private EditText fieldHiddenSwiper, fieldHiddenScan;
    private static CheckBox checkBox;
    private MyPreferences myPref;
    private CreditCardInfo cardInfoManager;
    private Activity activity;
    private Global global;

    // ET1 Motorola
    private String msrET1IntentAction = "";

    private final String DATA_STRING_TAG = "com.motorolasolutions.emdk.datawedge.data_string";

    private boolean hasBeenCreated = false, isCardInfo = false, isRequired = false;
    private EMSCallBack callBack;
    private static boolean cardReaderConnected = false;
    private EMSUniMagDriver uniMagReader;
    public EMSIDTechUSB _msrUsbSams;
    private EMSMagtekAudioCardReader magtekReader;
    private EMSRover roverReader;
    private Switch switchView;
    private LinearLayout cardLayoutHolder, commentLayoutHolder, lastSavedHolder;
    private String ordprodattr_id, attr_id, attr_name, attr_value, ordprod_id;
    private boolean isModify = false;
    private int modifyPosition = -1;

    // Honeywell Dolphin black
    private DecodeManager mDecodeManager = null;
    private final int SCANTIMEOUT = 500000;
    private boolean scannerInDecodeMode = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        activity = this;
        callBack = this;
        global = (Global) getApplication();
        myPref = new MyPreferences(this);
        Global.isEncryptSwipe = false; // will not encrypt number of card after
        // swipe

        if (!myPref.getIsTablet()) // reset to default layout (not as dialog)
            super.setTheme(R.style.LightTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_attributes_edit_layout);

        cardLayoutHolder = (LinearLayout) findViewById(R.id.cardInfoLayoutHolder);
        commentLayoutHolder = (LinearLayout) findViewById(R.id.commentLayoutHolder);
        lastSavedHolder = (LinearLayout) findViewById(R.id.lastSavedLayoutHolder);

        checkBox = (CheckBox) findViewById(R.id.checkboxCardSwipe);
        Button btnSave = (Button) findViewById(R.id.btnSave);
        btnSave.setOnClickListener(this);
        // isCardInfo = this.getIntent().getExtras().getBoolean("isCard");

        Bundle extras = getIntent().getExtras();
        attr_id = extras.getString("attr_id", "");
        attr_name = extras.getString("attr_name", "");
        ordprodattr_id = extras.getString("ordprodattr_id", "");
        isModify = extras.getBoolean("isModify", false);
        ordprod_id = extras.getString("ordprod_id", "");
        isRequired = extras.getBoolean("required", false);

        fieldHiddenScan = (EditText) findViewById(R.id.hiddenFieldScan);
        fieldHiddenSwiper = (EditText) findViewById(R.id.hiddenFieldSwiper);
        switchView = (Switch) findViewById(R.id.switchView);
        switchView.setOnCheckedChangeListener(this);

        fieldCardNum = (MyEditText) findViewById(R.id.fieldCardNumber);
        fieldCardNum.setIsForSearching(this, fieldHiddenSwiper);
        cardLayoutHolder.setVisibility(View.GONE);

        fieldComment = (MyEditText) findViewById(R.id.fieldComments);
        fieldComment.setIsForSearching(this, fieldHiddenScan);

        fieldHiddenScan.addTextChangedListener(textWatcher());
        fieldHiddenScan.requestFocus();

        fieldHiddenSwiper.addTextChangedListener(new GiftCardTextWatcher(activity, fieldHiddenSwiper, fieldCardNum, cardInfoManager, Global.isEncryptSwipe));

        setUpCardReader();
        setUpIfModify();

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

        if (mDecodeManager == null) {
            mDecodeManager = new DecodeManager(this, ScanResultHandler);
            try {
                mDecodeManager.disableSymbology(CommonDefine.SymbologyID.SYM_CODE39);
                mDecodeManager.setSymbologyDefaults(CommonDefine.SymbologyID.SYM_UPCA);
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if (mDecodeManager != null) {
            SoundManager.getInstance();
            SoundManager.initSounds(this);
            SoundManager.loadSounds();
        }

        super.onResume();
    }

    @Override
    public void onPause() {
        if (mDecodeManager != null) {
            try {
                mDecodeManager.release();
                mDecodeManager = null;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        boolean isScreenOn = powerManager.isScreenOn();
        if (!isScreenOn)
            global.loggedIn = false;
        global.startActivityTransitionTimer();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        cardReaderConnected = false;

        if (mDecodeManager != null) {
            try {
                mDecodeManager.release();
                mDecodeManager = null;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        if (uniMagReader != null)
            uniMagReader.release();
        else if (magtekReader != null)
            magtekReader.closeDevice();
        else if (Global.btSwiper != null && Global.btSwiper.currentDevice != null)
            Global.btSwiper.currentDevice.releaseCardReader();
        else if (Global.mainPrinterManager != null && Global.mainPrinterManager.currentDevice != null) {
            Global.mainPrinterManager.currentDevice.releaseCardReader();
            // Global.mainPrinterManager.currentDevice.loadScanner(null);
            Global.mainPrinterManager.currentDevice.loadScanner(OrderingMain_FA.instance.callBackMSR);
        }
        if (_msrUsbSams != null && _msrUsbSams.isDeviceOpen()) {
            _msrUsbSams.CloseTheDevice();
        }

        super.onDestroy();
    }

    private void setUpIfModify() {
        if (isModify) {
            int size = global.ordProdAttr.size();
            for (int i = 0; i < size; i++) {
                if (global.ordProdAttr.get(i).ordprod_id.equals(ordprod_id)
                        && global.ordProdAttr.get(i).Attrid.equals(attr_id)) {
                    modifyPosition = i;
                    attr_value = global.ordProdAttr.get(i).value;
                    break;
                }
            }
            TextView lastSaved = (TextView) findViewById(R.id.lastSavedTxt);
            lastSaved.setText(attr_value == null ? "" : attr_value);
        } else
            lastSavedHolder.setVisibility(View.GONE);
    }

    @SuppressWarnings("deprecation")
    private void setUpCardReader() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (audioManager.isWiredHeadsetOn()) {
            String _audio_reader_type = myPref.getPreferencesValue(MyPreferences.pref_audio_card_reader);
            if (_audio_reader_type != null && !_audio_reader_type.isEmpty() && !_audio_reader_type.equals("-1")) {
                if (_audio_reader_type.equals(Global.AUDIO_MSR_UNIMAG)) {
                    uniMagReader = new EMSUniMagDriver();
                    uniMagReader.initializeReader(activity);
                } else if (_audio_reader_type.equals(Global.AUDIO_MSR_MAGTEK)) {
                    magtekReader = new EMSMagtekAudioCardReader(activity);
                    new Thread(new Runnable() {
                        public void run() {
                            magtekReader.connectMagtek(true, callBack);
                        }
                    }).start();
                } else if (_audio_reader_type.equals(Global.AUDIO_MSR_ROVER)) {
                    roverReader = new EMSRover();
                    roverReader.initializeReader(activity, false);
                }
            }
        } else {
            int _swiper_type = myPref.swiperType(true, -2);
            int _printer_type = myPref.getPrinterType();
            if (_swiper_type != -1 && Global.btSwiper != null && Global.btSwiper.currentDevice != null
                    && !cardReaderConnected) {
                Global.btSwiper.currentDevice.loadCardReader(callBack, false);
            } else if (_printer_type != -1 && Global.deviceHasMSR(_printer_type)) {
                if (Global.mainPrinterManager != null && Global.mainPrinterManager.currentDevice != null
                        && !cardReaderConnected)
                    Global.mainPrinterManager.currentDevice.loadCardReader(callBack, false);
            }
        }

        if (Global.deviceHasBarcodeScanner(myPref.getPrinterType())
                || Global.deviceHasBarcodeScanner(myPref.sledType(true, -2))) {
            if (Global.mainPrinterManager != null && Global.mainPrinterManager.currentDevice != null)
                Global.mainPrinterManager.currentDevice.loadScanner(callBack);
            else if (Global.btSled != null && Global.btSled.currentDevice != null)
                Global.btSled.currentDevice.loadScanner(callBack);
        }

        if (myPref.isET1(true, false) || myPref.isMC40(true, false)) // swiper
        {
            msrET1IntentAction = getString(R.string.intentAction4);
            Intent i = getIntent();
            handleDecodeData(i);
            checkBox.setChecked(true);
        } else if (myPref.isSam4s(true, false)) {
            checkBox.setChecked(true);
            _msrUsbSams = new EMSIDTechUSB(activity, callBack);
            if (_msrUsbSams.OpenDevice())
                _msrUsbSams.StartReadingThread();
        } else if (myPref.isESY13P1()) {
            if (Global.mainPrinterManager != null && Global.mainPrinterManager.currentDevice != null) {
                Global.mainPrinterManager.currentDevice.loadCardReader(callBack, false);
                checkBox.setChecked(true);
            }
        } else if (myPref.isEM100() || myPref.isEM70() || myPref.isOT310() || myPref.isKDC5000()|| myPref.isHandpoint()) {
            checkBox.setChecked(true);
        }
    }

    private TextWatcher textWatcher() {

        TextWatcher tw = new TextWatcher() {
            boolean doneScanning = false;

            @Override
            public void afterTextChanged(Editable s) {
                if (doneScanning) {
                    doneScanning = false;
                    String data = fieldHiddenScan.getText().toString().trim().replace("\n", "");

                    if (!isCardInfo)
                        fieldComment.setText(data);

                    fieldHiddenScan.setText("");
                    fieldHiddenScan.requestFocus();
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
                if (s.toString().contains("\n"))
                    doneScanning = true;
            }
        };
        return tw;
    }

    private TextWatcher textWatcherSwiper(final EditText hiddenField) {

        TextWatcher tw = new TextWatcher() {
            boolean doneScanning = false;

            @Override
            public void afterTextChanged(Editable s) {
                if (doneScanning) {
                    doneScanning = false;
                    String data = hiddenField.getText().toString().trim().replace("\n", "");
                    // hiddenField.setText("");
                    cardInfoManager = Global.parseSimpleMSR(activity, data);
                    updateViewAfterSwipe();
                    hiddenField.setText("");
                    // fieldHiddenScan.requestFocus();

                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
                if (s.toString().contains("\n"))
                    doneScanning = true;
            }
        };
        return tw;
    }

    // SAM4s
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // special case for "quit the app"
        if (event.getDevice() == null) {
            return super.dispatchKeyEvent(event);
        }

        // retrieve the input device of current event
        String device_desc = event.getDevice().getName();
        if (device_desc.equals("Sam4s SPT-4000 USB MCR")) {
            if (getCurrentFocus() != fieldHiddenSwiper) {
                fieldHiddenSwiper.setText("");
                fieldHiddenSwiper.setFocusable(true);
                fieldHiddenSwiper.requestFocus();
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onNewIntent(Intent i) {
        super.onNewIntent(i);
        handleDecodeData(i);
    }

    private void handleDecodeData(Intent i) {
        // check the intent action is for us
        if (i.getAction() != null && i.getAction().contentEquals(msrET1IntentAction)) {

            // get the data from the intent
            String data = i.getStringExtra(DATA_STRING_TAG);
            if (isCardInfo) {
                this.cardInfoManager = Global.parseSimpleMSR(this, data);
                updateViewAfterSwipe();
            } else
                fieldComment.setText(data);
        }
    }

    @Override
    public void cardWasReadSuccessfully(boolean read, CreditCardInfo cardManager) {
        // TODO Auto-generated method stub
        this.cardInfoManager = cardManager;
        updateViewAfterSwipe();
        if (uniMagReader != null && uniMagReader.readerIsConnected()) {
            uniMagReader.startReading();
        } else if (magtekReader == null && Global.btSwiper == null && _msrUsbSams == null
                && Global.mainPrinterManager != null)
            Global.mainPrinterManager.currentDevice.loadCardReader(callBack, false);
    }

    @Override
    public void readerConnectedSuccessfully(boolean didConnect) {
        // TODO Auto-generated method stub
        if (didConnect) {
            cardReaderConnected = true;
            if (uniMagReader != null && uniMagReader.readerIsConnected())
                uniMagReader.startReading();
            if (!checkBox.isChecked())
                checkBox.setChecked(true);
        } else {
            cardReaderConnected = false;
            if (checkBox.isChecked())
                checkBox.setChecked(false);
        }
    }

    @Override
    public void scannerWasRead(String data) {
        // TODO Auto-generated method stub
        if (!data.isEmpty()) {
            if (!isCardInfo)
                fieldComment.setText(data);
        }
    }

    private void updateViewAfterSwipe() {
        if (isCardInfo) {
            fieldCardNum.setText(cardInfoManager.getCardNumUnencrypted());
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        // TODO Auto-generated method stub
        if (isChecked) {
            isCardInfo = true;
            commentLayoutHolder.setVisibility(View.GONE);
            cardLayoutHolder.setVisibility(View.VISIBLE);
            fieldComment.setText("");
            fieldHiddenSwiper.requestFocus();
            // setUpCardReader();
        } else {
            isCardInfo = false;
            cardLayoutHolder.setVisibility(View.GONE);
            commentLayoutHolder.setVisibility(View.VISIBLE);
            fieldHiddenScan.requestFocus();
            fieldCardNum.setText("");
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == 0) {
            fragOnKeyDown(keyCode);
            return true;

        }
        return super.onKeyUp(keyCode, event);
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

    private Handler ScanResultHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DecodeManager.MESSAGE_DECODER_COMPLETE:
                    String strDecodeResult = "";
                    DecodeResult decodeResult = (DecodeResult) msg.obj;

                    strDecodeResult = decodeResult.barcodeData.trim();
                    if (!strDecodeResult.isEmpty()) {
                        if (!isCardInfo)
                            fieldComment.setText(strDecodeResult);
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

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.btnSave:
                if (isCardInfo) {
                    attr_value = fieldCardNum.getText().toString();
                } else {
                    attr_value = fieldComment.getText().toString();
                }

                if (attr_value.isEmpty())
                    Global.showPrompt(this, R.string.dlog_title_error, getString(R.string.validation_failed));
                else {
                    if (!isModify) {
                        OrdProdAttrHolder temp = new OrdProdAttrHolder();
                        temp.Attrid = attr_id;
                        temp.ordprod_attr_name = attr_name;
                        temp.value = attr_value;
                        temp.ordprod_id = ordprod_id;
                        global.ordProdAttr.add(temp);
                    } else {
                        if (modifyPosition == -1) {
                            OrdProdAttrHolder temp = new OrdProdAttrHolder();
                            temp.Attrid = attr_id;
                            temp.ordprod_attr_name = attr_name;
                            temp.value = attr_value;
                            temp.ordprod_id = ordprod_id;
                            global.ordProdAttr.add(temp);
                        } else
                            global.ordProdAttr.get(modifyPosition).value = attr_value;
                    }
                    if (isRequired) {
                        Intent result = new Intent();
                        result.putExtra("ordprodattr_id", ordprodattr_id);
                        setResult(0, result);
                        finish();
                    } else
                        finish();
                }
                break;
        }
    }

    @Override
    public void startSignature() {
        // TODO Auto-generated method stub

    }
}
