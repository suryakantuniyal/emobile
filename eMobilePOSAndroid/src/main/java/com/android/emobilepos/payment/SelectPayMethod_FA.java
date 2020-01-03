package com.android.emobilepos.payment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.dao.AssignEmployeeDAO;
import com.android.dao.CustomerCustomFieldsDAO;
import com.android.dao.PayMethodsDAO;
import com.android.dao.PaymentMethodDAO;
import com.android.dao.StoredPaymentsDAO;
import com.android.database.DrawInfoHandler;
import com.android.database.OrdersHandler;
import com.android.database.PaymentsHandler;
import com.android.database.VoidTransactionsHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.cardmanager.CardManager_FA;
import com.android.emobilepos.mainmenu.SalesTab_FR;
import com.android.emobilepos.models.EMVContainer;
import com.android.emobilepos.models.orders.Order;
import com.android.emobilepos.models.realms.AssignEmployee;
import com.android.emobilepos.models.realms.CustomerCustomField;
import com.android.emobilepos.models.realms.Device;
import com.android.emobilepos.models.realms.Payment;
import com.android.emobilepos.models.realms.PaymentMethod;
import com.android.emobilepos.ordering.SplittedOrderSummary_FA;
import com.android.emobilepos.security.SecurityManager;
import com.android.ivu.MersenneTwisterFast;
import com.android.payments.EMSPayGate_Default;
import com.android.saxhandler.SAXProcessCardPayHandler;
import com.android.support.CreditCardInfo;
import com.android.support.DeviceUtils;
import com.android.support.GenerateNewID;
import com.android.support.GenerateNewID.IdType;
import com.android.support.Global;
import com.android.support.MyEditText;
import com.android.support.MyPreferences;
import com.android.support.NumberUtils;
import com.android.support.OrderProductUtils;
import com.android.support.Post;
import com.android.support.TerminalDisplay;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;
import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import interfaces.EMSCallBack;
import interfaces.TipsCallback;
import main.EMSDeviceManager;
import util.json.UIUtils;

public class SelectPayMethod_FA extends BaseFragmentActivityActionBar implements OnClickListener, OnItemClickListener {

    private CardsListAdapter myAdapter;
    private GridView myListview;
    private String total;
    private String paid;
    private String pay_id;
    private String job_id = ""; // invoice #
    private List<PaymentMethod> payTypeList;
    private Bundle extras;
    private String reqChargeLoyaltyReward;
    private Payment loyaltyRewardPayment;
    private Global global;
    private boolean hasBeenCreated = false;
    private boolean isFromMainMenu = false; // It was called from the main menu
    // (display no Invoice#)
    private int typeOfProcedure = 0;

    private double overAllRemainingBalance = 0.00;
    private double tipPaidAmount = 0.00;
    private MyPreferences myPref;
    private ProgressDialog myProgressDialog;
    private int selectedPosition = 0;
    private PaymentsHandler paymentHandlerDB;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private int totalPayCount = 0;
    private String order_email = "";
    private String order_phone = "";
    private int splitPaymentsCount = 1;
    private Global.OrderType orderType;
    private boolean skipLogin;
    private Dialog dlog;
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (dlog != null && dlog.isShowing())
                dlog.dismiss();
        }
    };
    private Order order;

    public static void voidTransaction(Activity activity, String job_id, String orderType) {
        OrdersHandler handler = new OrdersHandler(activity);
        handler.updateIsVoid(job_id);
        handler.updateIsProcessed(job_id, "9");

        VoidTransactionsHandler voidHandler = new VoidTransactionsHandler();
        Order order = new Order(activity);
        order.ord_id = job_id;
        order.ord_type = orderType;
        voidHandler.insert(order);
        // Check if Stored&Forward active and delete from record if any payment were made
        MyPreferences myPref = new MyPreferences(activity);
        if (myPref.getPreferences(MyPreferences.pref_use_store_and_forward)) {
            handler.updateOrderStoredFwd(job_id, "0");
            StoredPaymentsDAO.deletePaymentFromJob(job_id);
        }
        HashMap<String, String> parsedMap;
        PaymentsHandler payHandler = new PaymentsHandler(activity);
        List<Payment> listVoidPayments = payHandler.getOrderPayments(job_id);
        int size = listVoidPayments.size();
        if (size > 0) {
            EMSPayGate_Default payGate;

            Post post = new Post(activity);
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXProcessCardPayHandler processCardPayHandler = new SAXProcessCardPayHandler();
            String xml;
            InputSource inSource;
            SAXParser sp;
            XMLReader xr;

            try {
                sp = spf.newSAXParser();
                xr = sp.getXMLReader();
                String paymentType;
                for (int i = 0; i < size; i++) {
                    paymentType = listVoidPayments.get(i).getCard_type().toUpperCase(Locale.getDefault()).trim();
                    if (paymentType.equals("GIFTCARD") || paymentType.equals("REWARD")) {
                        payGate = new EMSPayGate_Default(activity, listVoidPayments.get(i));
                        if (paymentType.equals("GIFTCARD")) {
                            xml = post.postData(13, payGate.paymentWithAction(EMSPayGate_Default.EAction.VoidGiftCardAction, false,
                                    listVoidPayments.get(i).getCard_type(), null));
                        } else {
                            xml = post.postData(13, payGate.paymentWithAction(EMSPayGate_Default.EAction.VoidRewardCardAction, false,
                                    listVoidPayments.get(i).getCard_type(), null));
                        }
                        inSource = new InputSource(new StringReader(xml));

                        xr.setContentHandler(processCardPayHandler);
                        xr.parse(inSource);
                        parsedMap = processCardPayHandler.getData();

                        if (parsedMap != null && parsedMap.size() > 0
                                && parsedMap.get("epayStatusCode").equals("APPROVED"))
                            payHandler.createVoidPayment(listVoidPayments.get(i), true, parsedMap);

                        if (parsedMap != null) {
                            parsedMap.clear();
                        }
                    } else if (paymentType.equals("CASH")) {
                        payHandler.createVoidPayment(listVoidPayments.get(i), false, null);
                    } else if (!paymentType.equals("CHECK") && !paymentType.equals("WALLET")) {
                        payGate = new EMSPayGate_Default(activity, listVoidPayments.get(i));
                        xml = post.postData(13, payGate.paymentWithAction(EMSPayGate_Default.EAction.VoidCreditCardAction, false,
                                listVoidPayments.get(i).getCard_type(), null));
                        inSource = new InputSource(new StringReader(xml));

                        xr.setContentHandler(processCardPayHandler);
                        xr.parse(inSource);
                        parsedMap = processCardPayHandler.getData();

                        if (parsedMap != null && parsedMap.size() > 0
                                && parsedMap.get("epayStatusCode").equals("APPROVED"))
                            payHandler.createVoidPayment(listVoidPayments.get(i), true, parsedMap);

                        if (parsedMap != null) {
                            parsedMap.clear();
                        }
                    }
                }
            } catch (SAXException e) {
                e.printStackTrace();
                Crashlytics.logException(e);
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
                Crashlytics.logException(e);
            } catch (IOException e) {
                e.printStackTrace();
                Crashlytics.logException(e);
            }
            activity.setResult(3);
            activity.finish();
        } else {
            activity.setResult(3);
            activity.finish();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.card_list_layout);
        myListview = findViewById(R.id.cardsListview);
        global = (Global) getApplication();
        extras = this.getIntent().getExtras();
        myPref = new MyPreferences(this);
        total = extras.getString("amount");
        paid = extras.getString("paid");
        order_email = extras.getString("ord_email", "");
        order_phone = extras.getString("ord_phone", "");
        isFromMainMenu = extras.getBoolean("isFromMainMenu");
        overAllRemainingBalance = Double.parseDouble(total);
        if (!isFromMainMenu) {
            job_id = extras.getString("job_id");
            if (extras.containsKey("typeOfProcedure")) {
                if (extras.get("typeOfProcedure") instanceof Global.OrderType) {
                    typeOfProcedure = ((Global.OrderType) extras.get("typeOfProcedure")).getCode();
                } else if (extras.get("typeOfProcedure") instanceof Global.TransactionType) {
                    typeOfProcedure = ((Global.TransactionType) extras.get("typeOfProcedure")).getCode();
                } else {
                    typeOfProcedure = extras.getInt("typeOfProcedure");
                }
            }
        }
        orderType = (Global.OrderType) extras.get("ord_type");
        splitPaymentsCount = extras.getInt("splitPaymentsCount", 1);
        paymentHandlerDB = new PaymentsHandler(this);
        GenerateNewID generator = new GenerateNewID(this);
        pay_id = generator.getNextID(IdType.PAYMENT_ID);

        myAdapter = new CardsListAdapter(this);
        myListview.setAdapter(myAdapter);
        myListview.setOnItemClickListener(this);

        hasBeenCreated = true;

        options = new DisplayImageOptions.Builder().resetViewBeforeLoading(true)
                .displayer(new FadeInBitmapDisplayer(200)).cacheOnDisc(true)
                .imageScaleType(ImageScaleType.EXACTLY).build();

        if (ImageLoader.getInstance().isInited())
            ImageLoader.getInstance().destroy();

        imageLoader = ImageLoader.getInstance();
        File cacheDir = new File(myPref.getCacheDir());
        if (!cacheDir.exists())
            cacheDir.mkdirs();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .discCache(new UnlimitedDiscCache(cacheDir)).build();
        imageLoader.init(config);
        imageLoader.handleSlowNetwork(true);

        if (Double.parseDouble(total) == 0) // total to be paid is 0
        {
            setResult(-1);

            if (Global.loyaltyCardInfo != null && !Global.loyaltyCardInfo.getCardNumUnencrypted().isEmpty()) {
                showPaymentSuccessDlog(true, null, false);
            } else if (Global.rewardCardInfo != null && !Global.rewardCardInfo.getCardNumUnencrypted().isEmpty()) {
                showPaymentSuccessDlog(true, null, false);
            }
        }

        String row1 = "Grand Total";
        String row2 = Global.getCurrencyFormat(total);
        TerminalDisplay.setTerminalDisplay(myPref, row1, row2);

        if (Double.parseDouble(total) != 0) { // Only go to default payment method if there is a balance pending.
            if (!myPref.getPreferencesValue(MyPreferences.pref_default_payment_method).isEmpty()
                    && !myPref.getPreferencesValue(MyPreferences.pref_default_payment_method).equals("0")) {
                String default_paymethod_id = myPref.getPreferencesValue(MyPreferences.pref_default_payment_method);
                int i = 0;
                for (PaymentMethod pm : payTypeList) {
                    if (pm.getPaymethod_id().equals(default_paymethod_id)) {
                        selectPayment(i);
                        break;
                    }
                    i++;
                }
            }
        }

        initHeaderSection();
    }

    private void initHeaderSection() {
        TextView totalView = findViewById(R.id.totalValue);
        TextView paidView = findViewById(R.id.paidValue);
        TextView tipView = findViewById(R.id.tipValue);
        TextView dueView = findViewById(R.id.dueValue);
        totalView.setText(Global.getCurrencyFormat(Global.formatNumToLocale(Double.parseDouble(total))));
        paidView.setText(Global.getCurrencyFormat(Global.formatNumToLocale(Global.overallPaidAmount)));
        dueView.setText(Global.getCurrencyFormat(Global.formatNumToLocale(overAllRemainingBalance < 0 ? 0 : overAllRemainingBalance)));
        tipView.setText(Global.getCurrencyFormat(Global.formatNumToLocale(tipPaidAmount)));
    }

    @Override
    public void onResume() {
        if (global.isApplicationSentToBackground() && !skipLogin)
            Global.loggedIn = false;
        global.stopActivityTransitionTimer();

        if (hasBeenCreated && !Global.loggedIn) {
            if (global.getGlobalDlog() != null && global.getGlobalDlog().isShowing()) {
                global.getGlobalDlog().dismiss();
            }
            global.promptForMandatoryLogin(this);
        }
        skipLogin = false;
        initHeaderSection();
        this.myListview.postDelayed(new Runnable() {
            @Override
            public void run() {
                myListview.setOnItemClickListener(SelectPayMethod_FA.this);
            }
        }, 1000);

        super.onResume();
    }

    @Override
    public void onPause() {
        myListview.setOnItemClickListener(null);
        super.onPause();
        global.startActivityTransitionTimer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dlog != null)
            dlog.dismiss();
    }

    @Override
    public void onBackPressed() {
        if (Global.overallPaidAmount == 0) {
            if (orderType == Global.OrderType.INVOICE &&
                    !myPref.isInvoiceRequirePayment() &&
                    !myPref.isRequireFullPayment()) {
                setResult(-1);
                finish();
                return;
            }
            if (orderType == Global.OrderType.INVOICE &&
                    myPref.isInvoiceRequirePayment()) {
                showVoidDialog();
                return;
            }
            setResult(SplittedOrderSummary_FA.NavigationResult.BACK_SELECT_PAYMENT.getCode());
            finish();
        } else {
            if (orderType == Global.OrderType.SALES_RECEIPT || (orderType == Global.OrderType.INVOICE && myPref.isRequireFullPayment())) {
                showVoidDialog();
            } else {

                if (job_id != null) {
                    OrdersHandler handler = new OrdersHandler(this);
                    handler.updateIsProcessed(job_id, "1");
                }

                if ((typeOfProcedure == Global.FROM_JOB_INVOICE
                        || typeOfProcedure == Integer.parseInt(Global.OrderType.INVOICE.getCodeString()))
                        && myPref.getPreferences(MyPreferences.pref_enable_printing)) {
                    if (Global.overallPaidAmount == 0)
                        setResult(-1);
                    if (!myPref.getPreferences(MyPreferences.pref_automatic_printing))
                        showPrintDlg(false, false, null);
                    else
                        new PrintAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, false, false);
                } else {
                    if (Global.overallPaidAmount == 0)
                        setResult(-1);
                    else
                        setResult(3);
                    finish();
                }
            }
        }
    }

    private void showVoidDialog() {
        final Dialog dialog = new Dialog(this, R.style.Theme_TransparentTest);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.void_dialog_layout);
        TextView msg1 = dialog.findViewById(R.id.message1textView);
        TextView msg2 = dialog.findViewById(R.id.message2textView2);
        String to = orderType.toTitleCase();
        msg1.setText(String.format(getString(R.string.void_confirmation_message1), to));
        msg2.setText(String.format(getString(R.string.void_confirmation_message2), to));

        Button voidBut = dialog.findViewById(R.id.voidBut);
        Button notVoid = dialog.findViewById(R.id.notVoidBut);

        voidBut.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if (myPref.getPreferences(MyPreferences.pref_require_manager_pass_to_void_trans)) {
                    dialog.dismiss();
                    promptManagerPassword();
                } else {
                    dialog.dismiss();
                    voidTransaction(SelectPayMethod_FA.this, job_id, orderType.name());
                }
            }
        });
        notVoid.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void initIntents(Bundle extras, final Intent intent) {
        String payingAmount = total;
        int requestCode = 0;
        boolean isReceipt = false;

        String drawDate = "";
        String ivuLottoNum = "";

        if (Global.isIvuLoto) {
            DrawInfoHandler drawDateInfo = new DrawInfoHandler(this);
            MersenneTwisterFast mersenneTwister = new MersenneTwisterFast();
            drawDate = drawDateInfo.getDrawDate();
            ivuLottoNum = mersenneTwister.generateIVULoto();
        }

        intent.putExtra("custidkey", Global.getValidString(extras.getString("custidkey")));
        intent.putExtra("cust_id", Global.getValidString(extras.getString("cust_id")));
        intent.putExtra("order_email", order_email);
        intent.putExtra("order_phone", order_phone);

        if (overAllRemainingBalance != 0)
            payingAmount = Global.formatNumber(true, overAllRemainingBalance);

        if (extras.getBoolean("salespayment")) {
            intent.putExtra("salespayment", true);
        } else if (extras.getBoolean("salesrefund")) {
            intent.putExtra("salesrefund", true);
            drawDate = "";
            ivuLottoNum = "";
        } else if (extras.getBoolean("salesreceipt")) {
            intent.putExtra("salesreceipt", true);
            isReceipt = true;
            requestCode = Global.FROM_JOB_SALES_RECEIPT;
        } else if (extras.getBoolean("histinvoices")) {
            intent.putExtra("histinvoices", true);
            requestCode = Global.FROM_OPEN_INVOICES;

            if (!extras.getBoolean("isMultipleInvoice")) {
                intent.putExtra("isMultipleInvoice", false);
                intent.putExtra("inv_id", extras.getString("inv_id"));

            } else {
                intent.putExtra("isMultipleInvoice", true);
                intent.putExtra("inv_id_array", extras.getStringArray("inv_id_array"));
                intent.putExtra("balance_array", extras.getDoubleArray("balance_array"));
                intent.putExtra("txnID_array", extras.getStringArray("txnID_array"));

            }
        } else if (extras.getBoolean("salesinvoice")) {
            intent.putExtra("salesinvoice", true);
            requestCode = Global.FROM_JOB_INVOICE;
        }

        if (Global.isIvuLoto) {
            intent.putExtra("IvuLottoNumber", ivuLottoNum);
            intent.putExtra("IvuLottoDrawDate", drawDate);
        }

        intent.putExtra("amount", payingAmount);
        intent.putExtra("job_id", job_id);
        intent.putExtra("pay_id", pay_id);
        intent.putExtra("isFromSalesReceipt", isReceipt);
        intent.putExtra("isFromMainMenu", isFromMainMenu);

        startActivityForResult(intent, requestCode);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void showPrintDlg(final boolean isReprint, final boolean isRetry, final EMVContainer emvContainer) {
        final Dialog dlog = new Dialog(this, R.style.Theme_TransparentTest);
        dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dlog.setCancelable(false);
        dlog.setContentView(R.layout.dlog_btn_left_right_layout);

        TextView viewTitle = dlog.findViewById(R.id.dlogTitle);
        TextView viewMsg = dlog.findViewById(R.id.dlogMessage);
        viewTitle.setText(R.string.dlog_title_confirm);

        if (isRetry) {
            viewTitle.setText(R.string.dlog_title_error);
            viewMsg.setText(R.string.dlog_msg_failed_print);
        } else {
            if (isReprint)
                viewMsg.setText(R.string.dlog_msg_want_to_reprint);
            else
                viewMsg.setText(R.string.dlog_msg_want_to_print);
        }

        Button btnYes = dlog.findViewById(R.id.btnDlogLeft);
        Button btnNo = dlog.findViewById(R.id.btnDlogRight);
        dlog.findViewById(R.id.btnDlogCancel).setVisibility(View.GONE);

        btnYes.setText(R.string.button_yes);
        btnNo.setText(R.string.button_no);

        btnYes.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dlog.dismiss();
                new PrintAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, isReprint, isRetry, emvContainer);

            }
        });
        btnNo.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dlog.dismiss();
                if (!openGiftCardAddBalance()) {
                    resetCustomer();
                }
                if (overAllRemainingBalance <= 0 || ((typeOfProcedure == Global.FROM_JOB_INVOICE
                        || typeOfProcedure == Integer.parseInt(Global.OrderType.INVOICE.getCodeString())))) {
                    finish();
                    if (splitPaymentsCount == 1) {
                        SalesTab_FR.checkAutoLogout(SelectPayMethod_FA.this);
                    }
                }
            }
        });
        dlog.show();
    }

    private void promptManagerPassword() {
        final Dialog globalDlog = new Dialog(this, R.style.Theme_TransparentTest);
        globalDlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        globalDlog.setCancelable(true);
        globalDlog.setContentView(R.layout.dlog_field_single_layout);

        final EditText viewField = globalDlog.findViewById(R.id.dlogFieldSingle);
        viewField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        TextView viewTitle = globalDlog.findViewById(R.id.dlogTitle);
        TextView viewMsg = globalDlog.findViewById(R.id.dlogMessage);
        viewTitle.setText(R.string.dlog_title_confirm);

        viewMsg.setText(R.string.dlog_title_enter_manager_password);
        Button btnCancel = globalDlog.findViewById(R.id.btnCancelDlogSingle);
        btnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                globalDlog.dismiss();
            }
        });
        Button btnOk = globalDlog.findViewById(R.id.btnDlogSingle);
        btnOk.setText(R.string.button_ok);
        btnOk.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                globalDlog.dismiss();
                String pass = viewField.getText().toString();
                if (!pass.isEmpty() && myPref.loginManager(pass.trim())) {
                    voidTransaction(SelectPayMethod_FA.this, job_id, extras.getString("ord_type"));
                } else {
                    promptManagerPassword();
                }
            }
        });
        globalDlog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        myListview.setSelection(0);
        myListview.setSelected(false);
        skipLogin = data != null && data.hasExtra("LocalGeniusResponse");

        EMVContainer emvContainer = null;
        if (data != null && data.hasExtra("emvcontainer"))
            emvContainer = new Gson().fromJson(data.getStringExtra("emvcontainer"), EMVContainer.class);

        myAdapter.notifyDataSetChanged();
        if (resultCode == ProcessGenius_FA.REOPEN_PROCESS_GENIUS_SCREEN) {
            Intent intent = new Intent(this, ProcessGenius_FA.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            intent.putExtras(data);
            startActivityForResult(intent, 0);
        } else if (resultCode == -1) {
            if (requestCode == Global.FROM_OPEN_INVOICES)
                setResult(Global.FROM_PAYMENT);
            else
                setResult(-1);
            showPaymentSuccessDlog(true, emvContainer, false);
        } else if (resultCode == -2) {
            totalPayCount++;
            OrdersHandler ordersHandler = new OrdersHandler(this);
            if (!TextUtils.isEmpty(job_id)) {
                ordersHandler.updateIsTotalLinesPay(job_id, Integer.toString(totalPayCount));
            }
            if(myPref.getPreferences(MyPreferences.pref_use_pax_signature)){
                PaymentsHandler payHandler = new PaymentsHandler(this);
                Global.amountPaid = payHandler.updateSignaturePayment(pay_id, global.encodedImage);
            }

            Global.overallPaidAmount += Double.parseDouble(Global.amountPaid);
            tipPaidAmount += Double.parseDouble(Global.tipPaid);
            String overallPaidAmount = Global.formatNumber(true, Global.overallPaidAmount);

            if (NumberUtils.cleanCurrencyFormatedNumber(total).equals("0.00") && data != null) {
                total = data.getStringExtra("total_amount");
            }
            overAllRemainingBalance = Global.formatNumFromLocale(
                    Global.addSubsStrings(false, Global.formatNumToLocale(Double.parseDouble(total)),
                            Global.formatNumToLocale(Double.parseDouble(overallPaidAmount))));

            myAdapter.notifyDataSetChanged();

            if (requestCode == Global.FROM_OPEN_INVOICES)
                setResult(Global.FROM_PAYMENT);
            else
                setResult(-1);

            if (overAllRemainingBalance > 0) {

                GenerateNewID generator = new GenerateNewID(this);
                pay_id = generator.getNextID(IdType.PAYMENT_ID);

                if (isFromMainMenu || extras.getBoolean("histinvoices"))
                    showPaymentSuccessDlog(true, emvContainer, false);
                else
                    showPaymentSuccessDlog(false, emvContainer, false);

            } else {
                boolean isReturn = false;
                if (job_id != null && !job_id.isEmpty()) {
                    ordersHandler.updateIsProcessed(job_id, "1");
                    if (orderType == Global.OrderType.RETURN) {
                        isReturn = true;
                    }
                }
                boolean refundPaymentType = PaymentsHandler.getLastPaymentInserted().isRefundPaymentType();
                showPaymentSuccessDlog(true, emvContainer, isReturn || refundPaymentType);
            }
        } else {
            if (emvContainer != null && emvContainer.getGeniusResponse() != null &&
                    emvContainer.getGeniusResponse().getStatus().equalsIgnoreCase("DECLINED")) {
                showPaymentSuccessDlog(true, emvContainer, false);
            }
        }
    }

    private void showBoloroDlog() {
        dlog = new Dialog(this, R.style.Theme_TransparentTest);
        dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dlog.setCancelable(true);
        dlog.setCanceledOnTouchOutside(true);
        dlog.setContentView(R.layout.dlog_btn_top_bottom_layout);

        TextView title = dlog.findViewById(R.id.dlogTitle);
        TextView msg = dlog.findViewById(R.id.dlogMessage);
        Button btnTapPay = dlog.findViewById(R.id.btnDlogTop);
        Button btnManual = dlog.findViewById(R.id.btnDlogBottom);
        btnTapPay.setOnClickListener(this);
        btnManual.setOnClickListener(this);
        btnTapPay.setText(R.string.tap_and_pay);
        btnManual.setText(R.string.manual);

        title.setText(R.string.dlog_title_choose_action);
        msg.setText(R.string.boloro_payment_method);
        dlog.show();
    }

    private void showPaymentSuccessDlog(final boolean withPrintRequest, final EMVContainer emvContainer, boolean isRetun) {
        String message;
        if (emvContainer != null && emvContainer.getGeniusResponse() != null) {
            if (emvContainer.getGeniusResponse().getStatus().equalsIgnoreCase("APPROVED")) {
                if (isRetun) {
                    message = getString(R.string.payment_return_saved_successfully);
                } else {
                    message = getString(R.string.payment_saved_successfully);
                }
            } else {
                message = getString(R.string.payment_save_declined);
            }
        } else {
            if (isRetun) {
                message = getString(R.string.payment_return_saved_successfully);
            } else {
                message = getString(R.string.payment_saved_successfully);
            }
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        if (Global.loyaltyCardInfo != null && !Global.loyaltyCardInfo.getCardNumUnencrypted().isEmpty()) {
            processInquiry(true);
        } else if (Global.rewardCardInfo != null && !Global.rewardCardInfo.getCardNumUnencrypted().isEmpty()) {
            processInquiry(false);
        }

        if (withPrintRequest && myPref.getPreferences(MyPreferences.pref_enable_printing)
                && myPref.getPreferences(MyPreferences.pref_automatic_printing)) {
            if ((emvContainer != null && emvContainer.getGeniusResponse() != null &&
                    emvContainer.getGeniusResponse().getStatus().equalsIgnoreCase("APPROVED")) ||
                    emvContainer == null || emvContainer.getGeniusResponse() == null) {
                new PrintAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, false, false);
            }
        } else if (withPrintRequest) {
            if (Global.loyaltyCardInfo != null && !Global.loyaltyCardInfo.getCardNumUnencrypted().isEmpty()) {
                showPrintDlg(false, false, emvContainer);
            } else if (Global.rewardCardInfo != null && !Global.rewardCardInfo.getCardNumUnencrypted().isEmpty()) {
                showPrintDlg(false, false, emvContainer);
            } else {
                if (myPref.getPreferences(MyPreferences.pref_enable_printing)
                        && !myPref.getPreferences(MyPreferences.pref_automatic_printing)) {
                    showPrintDlg(false, false, emvContainer);
                } else if (overAllRemainingBalance <= 0) {
                    boolean addBalance = openGiftCardAddBalance();
                    Global.overallPaidAmount = 0;
                    finish();
                    if (splitPaymentsCount == 1) {
                        SalesTab_FR.checkAutoLogout(this);
                    }
                    if (!addBalance) {
                        resetCustomer();
                    }
                }
            }
        } else if (overAllRemainingBalance <= 0) {
            if (!openGiftCardAddBalance()) {
                resetCustomer();
            }
            finish();
            SalesTab_FR.checkAutoLogout(this);
        }
        handler.removeCallbacks(runnable);
    }

    private boolean openGiftCardAddBalance() {
        if (global.order != null && !global.order.getOrderProducts().isEmpty()) {
            CustomerCustomField customField = CustomerCustomFieldsDAO.findEMWSCardIdByCustomerId(myPref.getCustID());
            boolean containsGiftCard = customField != null && OrderProductUtils.containsGiftCard(global.order.getOrderProducts(), customField.getCustValue());
            if (containsGiftCard) {
                Intent intent = new Intent(this, CardManager_FA.class);
                intent.putExtra("CARD_TYPE", CardManager_FA.CASE_GIFT);
                intent.putExtra("amount", total);
                intent.putExtra("cardNumber", customField.getCustValue());
                boolean hasPermissions = SecurityManager.hasPermissions(this, SecurityManager.SecurityAction.MANUAL_ADD_BALANCE_LOYALTY);
                if (hasPermissions) {
                    intent.putExtra("PROCESS_TYPE", CardManager_FA.GiftCardActions.CASE_MANUAL_ADD.getCode());
                    startActivity(intent);
                    return true;
                } else {
                    Global.showPrompt(this, R.string.security_alert, getString(R.string.permission_denied));
                }
            }
        }
        return false;
    }

    private void processInquiry(boolean isLoyalty) {
        AssignEmployee assignEmployee = AssignEmployeeDAO.getAssignEmployee();

        loyaltyRewardPayment = new Payment(this);
        Bundle extras = getIntent().getExtras();

        String cardType = "LoyaltyCard";
        CreditCardInfo cardInfoManager;
        if (isLoyalty)
            cardInfoManager = Global.loyaltyCardInfo;
        else {
            cardInfoManager = Global.rewardCardInfo;
            cardType = "Reward";
        }

        GenerateNewID generator = new GenerateNewID(this);
        String tempPay_id;
        tempPay_id = generator.getNextID(IdType.PAYMENT_ID);
        loyaltyRewardPayment.setPay_id(tempPay_id);
        loyaltyRewardPayment.setCust_id(Global.getValidString(extras.getString("cust_id")));
        loyaltyRewardPayment.setCustidkey(Global.getValidString(extras.getString("custidkey")));
        loyaltyRewardPayment.setEmp_id(String.valueOf(assignEmployee.getEmpId()));
        loyaltyRewardPayment.setJob_id(job_id);
        loyaltyRewardPayment.setPay_name(cardInfoManager.getCardOwnerName());
        loyaltyRewardPayment.setPay_ccnum(cardInfoManager.getCardNumAESEncrypted());
        loyaltyRewardPayment.setCcnum_last4(cardInfoManager.getCardLast4());
        loyaltyRewardPayment.setPay_expmonth(cardInfoManager.getCardExpMonth());
        loyaltyRewardPayment.setPay_expyear(cardInfoManager.getCardExpYear());
        loyaltyRewardPayment.setPay_seccode(cardInfoManager.getCardEncryptedSecCode());
        loyaltyRewardPayment.setTrack_one(cardInfoManager.getEncryptedAESTrack1());
        loyaltyRewardPayment.setTrack_two(cardInfoManager.getEncryptedAESTrack2());
        cardInfoManager.setRedeemAll("0");
        cardInfoManager.setRedeemType("Only");
        loyaltyRewardPayment.setPaymethod_id(cardType + "Balance");
        loyaltyRewardPayment.setCard_type(cardType);
        loyaltyRewardPayment.setPay_type("0");
        if (isLoyalty) {
            if (new BigDecimal(Global.loyaltyCharge).compareTo(BigDecimal.valueOf(0)) == 1) {
                loyaltyRewardPayment.setPay_amount(Global.loyaltyCharge);
                EMSPayGate_Default payGate = new EMSPayGate_Default(this, loyaltyRewardPayment);
                boolean wasSwiped = cardInfoManager.getWasSwiped();
                reqChargeLoyaltyReward = payGate.paymentWithAction(EMSPayGate_Default.EAction.ChargeLoyaltyCardAction, wasSwiped, cardType,
                        cardInfoManager);
                payGate = new EMSPayGate_Default(this, loyaltyRewardPayment);
                String reqAddLoyalty = payGate.paymentWithAction(EMSPayGate_Default.EAction.AddValueLoyaltyCardAction, wasSwiped, cardType,
                        cardInfoManager);
                new ProcessLoyaltyAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        } else {
            BigDecimal bdOrigAmount = new BigDecimal(cardInfoManager.getOriginalTotalAmount());
            if (Global.rewardChargeAmount.compareTo(new BigDecimal("0")) == 1) {
                loyaltyRewardPayment.setOriginalTotalAmount(Global.rewardAccumulableSubtotal.add(bdOrigAmount)
                        .toString());
            } else {
                loyaltyRewardPayment.setOriginalTotalAmount(Global.rewardAccumulableSubtotal.toString());
            }
            if (Global.rewardChargeAmount.compareTo(BigDecimal.valueOf(0)) == 1) {
                loyaltyRewardPayment.setPay_amount(Global.rewardChargeAmount.toString());
                EMSPayGate_Default payGate = new EMSPayGate_Default(this, loyaltyRewardPayment);
                boolean wasSwiped = cardInfoManager.getWasSwiped();
                reqChargeLoyaltyReward = payGate.paymentWithAction(EMSPayGate_Default.EAction.ChargeRewardAction, wasSwiped, cardType,
                        cardInfoManager);
                new ProcessRewardAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else if (overAllRemainingBalance == 0 && Global.rewardChargeAmount.compareTo(BigDecimal.valueOf(0)) == 0) {
                loyaltyRewardPayment.setPay_amount(Global.rewardChargeAmount.toString());
                EMSPayGate_Default payGate = new EMSPayGate_Default(this, loyaltyRewardPayment);
                boolean wasSwiped = cardInfoManager.getWasSwiped();
                reqChargeLoyaltyReward = payGate.paymentWithAction(EMSPayGate_Default.EAction.AddValueRewardAction, wasSwiped, cardType, cardInfoManager);
                new ProcessRewardAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }

    }

    public void showBalancePrompt(String msg) {
        if (Global.isActivityDestroyed(this)) {
            return;
        }
        final Dialog dlog = new Dialog(this, R.style.Theme_TransparentTest);
        dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dlog.setContentView(R.layout.dlog_btn_single_layout);
        dlog.setCancelable(false);
        TextView viewTitle = dlog.findViewById(R.id.dlogTitle);
        TextView viewMsg = dlog.findViewById(R.id.dlogMessage);
        viewTitle.setText(R.string.dlog_title_confirm);
        viewMsg.setText(msg);
        Button btnOk = dlog.findViewById(R.id.btnDlogSingle);
        btnOk.setText(R.string.button_ok);
        btnOk.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dlog.dismiss();
                if (myPref.getPreferences(MyPreferences.pref_enable_printing)) {
                    if (!myPref.getPreferences(MyPreferences.pref_automatic_printing))
                        showPrintDlg(false, false, null);
                    else
                        new PrintAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, false, false);
                } else {
                    finish();
                }
            }
        });
        dlog.show();
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, ProcessBoloro_FA.class);
        intent.putExtra("paymethod_id", payTypeList.get(selectedPosition).getPaymethod_id());
        switch (v.getId()) {
            case R.id.btnDlogTop:
                dlog.dismiss();
                intent.putExtra("isNFC", true);
                initIntents(extras, intent);
                break;
            case R.id.btnDlogBottom:
                dlog.dismiss();
                intent.putExtra("isNFC", false);
                initIntents(extras, intent);
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (UIUtils.singleOnClick(view)) {
            selectPayment(position);
        } else {
            Toast.makeText(this, "Multiple click detected", Toast.LENGTH_LONG).show();
        }
    }

    private void selectPayment(int position) {
        selectedPosition = position;
        PaymentMethodDAO.incrementPriority(payTypeList.get(position));
        if (payTypeList.get(position).getPaymentmethod_type().equals("Cash")) {
            Intent intent = new Intent(this, ProcessCash_FA.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            intent.putExtra("paymethod_id", payTypeList.get(position).getPaymethod_id());
            intent.putExtras(extras);

            initIntents(extras, intent);
        } else if (payTypeList.get(position).getPaymentmethod_type().equals("Check")) {
            Intent intent = new Intent(this, ProcessCheck_FA.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            intent.putExtra("paymethod_id", payTypeList.get(position).getPaymethod_id());
            intent.putExtras(extras);
            initIntents(extras, intent);
        } else if (payTypeList.get(position).getPaymentmethod_type().equals("PAX")) {
            Intent intent = new Intent(this, ProcessPax_FA.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            intent.putExtra("paymethod_id", payTypeList.get(position).getPaymethod_id());
            intent.putExtras(extras);
            initIntents(extras, intent);
        } else if (payTypeList.get(position).getPaymentmethod_type().equals("SoundPayments")) {
            Intent intent = new Intent(this, ProcessSP_FA.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            intent.putExtra("paymethod_id", payTypeList.get(position).getPaymethod_id());
            intent.putExtras(extras);
            initIntents(extras, intent);
        } else if (payTypeList.get(position).getPaymentmethod_type().equals("Genius")) {
            Intent intent = new Intent(this, ProcessGenius_FA.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            intent.putExtra("paymethod_id", payTypeList.get(position).getPaymethod_id());
            intent.putExtras(extras);
            initIntents(extras, intent);
        } else if (payTypeList.get(position).getPaymentmethod_type().equals("CardOnFile")) {
            Intent intent = new Intent(this, ProcessGenius_FA.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            intent.putExtra("paymethod_id", payTypeList.get(position).getPaymethod_id());
            intent.putExtras(extras);
            initIntents(extras, intent);
        } else if (payTypeList.get(position).getPaymentmethod_type().equals("Wallet")) {
            Intent intent = new Intent(this, ProcessTupyx_FA.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            intent.putExtra("paymethod_id", payTypeList.get(position).getPaymethod_id());
            intent.putExtras(extras);
            initIntents(extras, intent);
        } else if (payTypeList.get(position).getPaymentmethod_type().equals("Boloro")) {
            //If store & forward is selected then boloro only accept NFC payments
            if (myPref.isPrefUseStoreForward()) {
                Intent intent = new Intent(this, ProcessBoloro_FA.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.putExtra("paymethod_id", payTypeList.get(selectedPosition).getPaymethod_id());
                intent.putExtra("isNFC", true);
                intent.putExtras(extras);
                initIntents(extras, intent);
            } else {
                showBoloroDlog();
            }
        } else if (payTypeList.get(position).getPaymentmethod_type().toUpperCase(Locale.getDefault()).contains("GIFT") ||
                payTypeList.get(position).getPaymentmethod_type().toUpperCase(Locale.getDefault()).contains("REWARD") ||
                payTypeList.get(position).getPaymentmethod_type().toUpperCase(Locale.getDefault()).contains("LOYALTYCARD")) {
            Intent intent = new Intent(this, ProcessGiftCard_FA.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            intent.putExtra("paymethod_id", payTypeList.get(position).getPaymethod_id());
            intent.putExtra("paymentmethod_type", payTypeList.get(position).getPaymentmethod_type());
            intent.putExtras(extras);
            initIntents(extras, intent);
        } else {
            boolean isDebit = payTypeList.get(position).getPaymentmethod_type().toUpperCase(Locale.getDefault()).trim().contains("DEBIT");
            if (myPref.isPrefUseStoreForward() && isDebit) {
                Global.showPrompt(this, R.string.invalid_payment_type, getString(R.string.invalid_storeforward_payment_type));
            } else {
                Intent intent = new Intent(this, ProcessCreditCard_FA.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.putExtra("paymethod_id", payTypeList.get(position).getPaymethod_id());
                intent.putExtras(extras);
                intent.putExtra("paymentmethod_type", payTypeList.get(position).getPaymentmethod_type());
                intent.putExtra("requireTransID", payTypeList.get(position).getOriginalTransid().equalsIgnoreCase("1"));
                if (payTypeList.get(position).getPaymentmethod_type().toUpperCase(Locale.getDefault()).trim().contains("DEBIT"))
                    intent.putExtra("isDebit", true);
                else
                    intent.putExtra("isDebit", false);
                initIntents(extras, intent);
            }
        }
    }

    private void resetCustomer() {
        if (myPref.isClearCustomerAfterTransaction()) {
            myPref.resetCustInfo(getString(R.string.no_customer));
        }
    }

    private class CardsListAdapter extends BaseAdapter implements Filterable {
        private Context context;
        private LayoutInflater myInflater;

        public CardsListAdapter(Activity activity) {
            this.context = activity.getApplicationContext();
            myInflater = LayoutInflater.from(context);

            payTypeList = PayMethodsDAO.getAllSortByName();

            if (myPref.getPreferences(MyPreferences.pref_use_pax) ||
                    myPref.getPreferences(MyPreferences.pref_use_sound_payments)) {
                List<PaymentMethod> itemsToDelete = new ArrayList<>();
                for (PaymentMethod method : payTypeList) {
                    if (!method.getPaymentmethod_type().equalsIgnoreCase("Cash") &&
                            !method.getPaymentmethod_type().equalsIgnoreCase("GiftCard") &&
                            !method.getPaymentmethod_type().equalsIgnoreCase("Check") &&
                            !method.getPaymentmethod_type().equalsIgnoreCase("Loyalty") &&
                            !method.getPaymentmethod_type().equalsIgnoreCase("Reward")) {
                        itemsToDelete.add(method);
                    }
                }
                payTypeList.removeAll(itemsToDelete);

                PaymentMethod paymentMethod;

                if (myPref.getPreferences(MyPreferences.pref_use_pax)) {
                    paymentMethod = new PaymentMethod();
                    paymentMethod.setOriginalTransid("true");
                    paymentMethod.setIsactive("1");
                    paymentMethod.setPaymentmethod_type("PAX");
                    paymentMethod.setPaymethod_id("PAX");
                    paymentMethod.setPaymethod_name("Credit/Debit Cards Processing (PAX)");
                    paymentMethod.setPaymethod_showOnline("0");
                    paymentMethod.setPriority(0);
                    payTypeList.add(paymentMethod);
                }

                if (myPref.getPreferences(MyPreferences.pref_pay_with_card_on_file)) {
                    paymentMethod = new PaymentMethod();
                    paymentMethod.setOriginalTransid("true");
                    paymentMethod.setIsactive("1");
                    paymentMethod.setPaymentmethod_type("CardOnFile");
                    paymentMethod.setPaymethod_id("CardOnFile");
                    paymentMethod.setPaymethod_name("CardOnFile");
                    paymentMethod.setPaymethod_showOnline("0");
                    paymentMethod.setPriority(0);
                    payTypeList.add(paymentMethod);
                }

                if (myPref.getPreferences(MyPreferences.pref_use_sound_payments)) {
                    paymentMethod = new PaymentMethod();
                    paymentMethod.setOriginalTransid("true");
                    paymentMethod.setIsactive("1");
                    paymentMethod.setPaymentmethod_type("SoundPayments");
                    paymentMethod.setPaymethod_id("SoundPayments");
                    paymentMethod.setPaymethod_name("SP");
                    paymentMethod.setPaymethod_showOnline("0");
                    paymentMethod.setPriority(0);
                    payTypeList.add(paymentMethod);
                }
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder;
            int iconId;

            if (convertView == null) {

                holder = new ViewHolder();

                convertView = myInflater.inflate(R.layout.card_listrow2_adapter, null);

                holder.textLine2 = convertView.findViewById(R.id.cardsListname);
                holder.ivPayIcon = convertView.findViewById(R.id.ivCardIcon);
                String key = payTypeList.get(position).getPaymentmethod_type();
                String name = payTypeList.get(position).getPaymethod_name();
                String img_url = payTypeList.get(position).getImage_url();


                if (TextUtils.isEmpty(img_url)) {
                    if (key == null) {
                        iconId = R.drawable.debitcard;
                    } else {
                        Log.d("Logo Name", key);
                        iconId = context.getResources().getIdentifier(key.toLowerCase(), "drawable",
                                context.getPackageName());
                    }

                    holder.ivPayIcon.setImageResource(iconId);
                } else {
                    Log.d("Logo Name", img_url);
                    imageLoader.displayImage(img_url, holder.ivPayIcon, options);
                }
                holder.textLine2.setTag(name);
                holder.textLine2.setText(name);

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
                String key = payTypeList.get(position).getPaymentmethod_type();
                String name = payTypeList.get(position).getPaymethod_name();
                String img_url = payTypeList.get(position).getImage_url();

                if (TextUtils.isEmpty(img_url)) {
                    if (key == null) {
                        iconId = R.drawable.debitcard;
                    } else {
                        Log.d("Logo Name", key);
                        iconId = context.getResources().getIdentifier(key.toLowerCase(), "drawable",
                                context.getPackageName());
                    }

                    holder.ivPayIcon.setImageResource(iconId);
                } else {
                    imageLoader.displayImage(img_url, holder.ivPayIcon, options);
                }
                holder.textLine2.setTag(name);
                holder.textLine2.setText(name);

            }
            return convertView;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return 0;
            }
            return 1;

        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getCount() {
            return payTypeList.size();
        }

        @Override
        public Object getItem(int position) {
            return 0;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public Filter getFilter() {
            return null;
        }

        private class ViewHolder {
            TextView textLine2;
            ImageView ivPayIcon;

        }

    }

    private class PrintAsync extends AsyncTask<Object, String, String> {
        private boolean wasReprint = false;
        private boolean printSuccessful = true;

        @Override
        protected void onPreExecute() {
            if (Global.mainPrinterManager != null && Global.mainPrinterManager.getCurrentDevice() != null) {
                Global.mainPrinterManager.getCurrentDevice().loadScanner(null);
            }
            myProgressDialog = new ProgressDialog(SelectPayMethod_FA.this);
            myProgressDialog.setMessage(getString(R.string.printing_message));
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.setCancelable(true);
            myProgressDialog.setCanceledOnTouchOutside(true);
            myProgressDialog.show();
            myProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    cancel(true);
                }
            });

        }

        @Override
        protected String doInBackground(Object... params) {
            EMSDeviceManager emsDeviceManager;
            wasReprint = (boolean) params[0];
            if (wasReprint) {
                emsDeviceManager = DeviceUtils.getEmsDeviceManager(order == null ? Device.Printables.PAYMENT_RECEIPT_REPRINT :
                        Device.Printables.TRANSACTION_RECEIPT_REPRINT, Global.printerDevices);
            } else {
                emsDeviceManager = DeviceUtils.getEmsDeviceManager(order == null ? Device.Printables.PAYMENT_RECEIPT :
                        Device.Printables.TRANSACTION_RECEIPT, Global.printerDevices);
            }
            EMVContainer emvContainer = params.length > 2 ? (EMVContainer) params[2] : null;
            try {
                if (emsDeviceManager != null && emsDeviceManager.getCurrentDevice() != null) {
                    if (wasReprint || isFromMainMenu || extras.getBoolean("histinvoices") ||
                            (emvContainer != null && emvContainer.getGeniusResponse() != null &&
                                    emvContainer.getGeniusResponse().getStatus().equalsIgnoreCase("DECLINED")))
                        printSuccessful = emsDeviceManager.getCurrentDevice().printPaymentDetails(PaymentsHandler.getLastPaymentInserted().getPay_id(), 1,
                                wasReprint, emvContainer);
                    else {
                        if (order == null) {
                            printSuccessful = emsDeviceManager.getCurrentDevice().printTransaction(job_id, orderType,
                                    wasReprint, false, emvContainer);
                        } else {
                            printSuccessful = emsDeviceManager.getCurrentDevice().printTransaction(order, orderType,
                                    wasReprint, false, emvContainer);
                        }
                    }
                }
            } catch (Exception e) {
                Crashlytics.logException(e);
                printSuccessful = false;
            }
            return null;
        }

        @Override
        protected void onCancelled() {
            finishTransaction();
        }

        @Override
        protected void onPostExecute(String unused) {
            finishTransaction();
        }

        private void finishTransaction() {
            Global.dismissDialog(SelectPayMethod_FA.this, myProgressDialog);
            if (printSuccessful) {
                if (overAllRemainingBalance <= 0 || (typeOfProcedure == Global.FROM_JOB_INVOICE
                        || typeOfProcedure == Integer.parseInt(Global.OrderType.INVOICE.getCodeString()))) {
                    if (!openGiftCardAddBalance()) {
                        resetCustomer();
                    }
                    if (myPref.isMultiplePrints()) {
                        showPrintDlg(true, false, null);
                    } else {
                        finish();
                        if (splitPaymentsCount == 1) {
                            SalesTab_FR.checkAutoLogout(SelectPayMethod_FA.this);
                        }
                    }
                } else {
                    showPrintDlg(wasReprint, false, null);
                }
            } else {
                showPrintDlg(wasReprint, true, null);
            }

        }
    }

    private class ProcessLoyaltyAsync extends AsyncTask<Void, Void, Void> {

        private HashMap<String, String> parsedMap = new HashMap<>();
        private boolean wasProcessed = false;
        private String errorMsg = "Loyalty could not be processed.";

        @Override
        protected void onPreExecute() {
            myProgressDialog = new ProgressDialog(SelectPayMethod_FA.this);
            myProgressDialog.setMessage(getString(R.string.processing_loyalty_card));
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.setCancelable(false);
            myProgressDialog.show();

        }

        @Override
        protected Void doInBackground(Void... params) {
            Post httpClient = new Post(SelectPayMethod_FA.this);

            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXProcessCardPayHandler handler = new SAXProcessCardPayHandler();

            try {
                String xml = httpClient.postData(13, reqChargeLoyaltyReward);
                switch (xml) {
                    case Global.TIME_OUT:
                        errorMsg = "TIME OUT, would you like to try again?";
                        break;
                    case Global.NOT_VALID_URL:
                        errorMsg = "Loyalty could not be processed....";
                        break;
                    default:
                        InputSource inSource = new InputSource(new StringReader(xml));

                        SAXParser sp = spf.newSAXParser();
                        XMLReader xr = sp.getXMLReader();
                        xr.setContentHandler(handler);
                        xr.parse(inSource);
                        parsedMap = handler.getData();

                        if (parsedMap != null && parsedMap.size() > 0
                                && parsedMap.get("epayStatusCode").equals("APPROVED")) {
                            wasProcessed = true;
                        } else if (parsedMap != null && parsedMap.size() > 0) {
                            errorMsg = "statusCode = " + parsedMap.get("statusCode") + "\n" + parsedMap.get("statusMessage");
                        } else
                            errorMsg = xml;
                        break;
                }

            } catch (SAXException e) {
                e.printStackTrace();
                Crashlytics.logException(e);
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
                Crashlytics.logException(e);
            } catch (IOException e) {
                e.printStackTrace();
                Crashlytics.logException(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            Global.dismissDialog(SelectPayMethod_FA.this, myProgressDialog);
            if (wasProcessed) {
                loyaltyRewardPayment.setPay_issync("1");
                paymentHandlerDB.insert(loyaltyRewardPayment);
            } else {
                showBalancePrompt(errorMsg);
            }
        }
    }

    private class ProcessRewardAsync extends AsyncTask<Void, Void, HashMap<String, String>> {

        private boolean wasProcessed = false;
        private String errorMsg = "Reward could not be processed.";

        @Override
        protected void onPreExecute() {
            myProgressDialog = new ProgressDialog(SelectPayMethod_FA.this);
            myProgressDialog.setMessage(getString(R.string.processing_reward));
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.setCancelable(false);
            myProgressDialog.show();

        }

        @Override
        protected HashMap<String, String> doInBackground(Void... params) {
            Post httpClient = new Post(SelectPayMethod_FA.this);

            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXProcessCardPayHandler handler = new SAXProcessCardPayHandler();
            HashMap<String, String> parsedMap = new HashMap<>();
            try {
                String xml = httpClient.postData(13, reqChargeLoyaltyReward);
                Global.generateDebugFile(reqChargeLoyaltyReward);
                switch (xml) {
                    case Global.TIME_OUT:
                        errorMsg = "TIME OUT, would you like to try again?";
                        break;
                    case Global.NOT_VALID_URL:
                        errorMsg = "Loyalty could not be processed....";
                        break;
                    default:
                        InputSource inSource = new InputSource(new StringReader(xml));

                        SAXParser sp = spf.newSAXParser();
                        XMLReader xr = sp.getXMLReader();
                        xr.setContentHandler(handler);
                        xr.parse(inSource);
                        parsedMap = handler.getData();

                        if (parsedMap != null && parsedMap.size() > 0
                                && parsedMap.get("epayStatusCode").equals("APPROVED")) {
                            wasProcessed = true;
                        } else if (parsedMap != null && parsedMap.size() > 0) {
                            errorMsg = "statusCode = " + parsedMap.get("statusCode") + "\n" + parsedMap.get("statusMessage");
                        } else
                            errorMsg = xml;
                        break;
                }

            } catch (SAXException e) {
                e.printStackTrace();
                Crashlytics.logException(e);
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
                Crashlytics.logException(e);
            } catch (IOException e) {
                e.printStackTrace();
                Crashlytics.logException(e);
            }
            return parsedMap;
        }

        @Override
        protected void onPostExecute(HashMap<String, String> parsedMap) {
            Global.dismissDialog(SelectPayMethod_FA.this, myProgressDialog);
            if (wasProcessed) // payment processing succeeded
            {
                String balance = (parsedMap.get("CardBalance") == null ? "0.0" : parsedMap.get("CardBalance"));
                Global.rewardCardInfo.setOriginalTotalAmount(balance);
                loyaltyRewardPayment.setPay_issync("1");
                paymentHandlerDB.insert(loyaltyRewardPayment);
                showBalancePrompt("Card was processed");
            } else // payment processing failed
            {
                showBalancePrompt(errorMsg);
            }
        }
    }

    public static class GratuityManager{

        private Activity activity;
        private MyPreferences myPref;
        private Global global;
        private TipsCallback callBack;
        private double grandTotalAmount;
        private double amountToTip;
        private boolean isFrom_MainMenu;

        private LayoutInflater inflater;
        private View dialogLayout;
        private Button tenPercent;
        private Button fifteenPercent;
        private Button twentyPercent;
        private TextView dlogGrandTotal;
        private AlertDialog dialog;
        private EditText promptTipField;

        public GratuityManager(TipsCallback callBack, Activity activity, MyPreferences myPref, Global global, boolean isFrom_MainMenu){
            this.callBack = callBack;
            this.myPref = myPref;
            this.global = global;
            this.isFrom_MainMenu = isFrom_MainMenu;
            this.activity = activity;
        }

        private void setUpGratuityDialog(){
            inflater = LayoutInflater.from(activity);
            dialogLayout = inflater.inflate(R.layout.tip_dialog_layout, null);

            final AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.DialogLargeArea);
            dialog = builder.create();
            dialog.setView(dialogLayout, 0, 0, 0, 0);
            dialog.setInverseBackgroundForced(true);
            dialog.setCancelable(false);

            tenPercent = dialogLayout.findViewById(R.id.tenPercent);
            fifteenPercent = dialogLayout.findViewById(R.id.fifteenPercent);
            twentyPercent = dialogLayout.findViewById(R.id.twentyPercent);
        }

        public void showTipsForCreditCardPayments(EditText amountDueField, EditText amountPaidField, String orderSubTotal){
            final double subTotal;
            setUpGratuityDialog();

            if (isFrom_MainMenu) {
                subTotal = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(amountDueField));
            } else {
                if (!TextUtils.isEmpty(orderSubTotal) && Double.parseDouble(orderSubTotal) > 0) {
                    subTotal = Double.parseDouble(orderSubTotal);
                } else {
                    subTotal = Math.abs(Double.parseDouble(global.order.ord_subtotal));
                }
            }

            double amountToBePaid = Global
                    .formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(amountPaidField));
            grandTotalAmount = amountToBePaid + amountToTip;
            final TextView totalAmountView = dialogLayout.findViewById(R.id.totalAmountView);
            totalAmountView.setText(String.format(Locale.getDefault(), activity.getString(R.string.total_plus_tip),
                    Global.formatDoubleToCurrency(subTotal), Global.formatDoubleToCurrency(0)));
            if(!(myPref.getGratuityOne().isEmpty()))
                tenPercent.setText((myPref.getGratuityOne()+"%"));
            if(!(myPref.getGratuityTwo().isEmpty()))
                fifteenPercent.setText((myPref.getGratuityTwo()+"%"));
            if(!(myPref.getGratuityThree().isEmpty()))
                twentyPercent.setText((myPref.getGratuityThree()+"%"));

            dlogGrandTotal = dialogLayout.findViewById(R.id.grandTotalView);

            dlogGrandTotal.setText(Global.formatDoubleToCurrency(subTotal));

            final EditText promptTipField = dialogLayout.findViewById(R.id.otherTipAmountField);
            promptTipField.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
            promptTipField.clearFocus();
            promptTipField.setText("");

            Button cancelTip = dialogLayout.findViewById(R.id.cancelTipButton);
            Button saveTip = dialogLayout.findViewById(R.id.acceptTipButton);
            Button noneButton = dialogLayout.findViewById(R.id.noneButton);

            promptTipField.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(s.toString())) > 0) {
                        amountToTip = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(s.toString()));
                        grandTotalAmount = subTotal + amountToTip;
                        dlogGrandTotal.setText(Global.formatDoubleToCurrency(grandTotalAmount));
                        totalAmountView.setText(String.format(Locale.getDefault(), activity.getString(R.string.total_plus_tip),
                                Global.formatDoubleToCurrency(subTotal), Global.formatDoubleToCurrency(amountToTip)));
                    }
                    NumberUtils.parseInputedCurrency(s, promptTipField);
                }
            });

            promptTipField.setOnFocusChangeListener(new View.OnFocusChangeListener() {

                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (v.hasFocus()) {
                        Selection.setSelection(promptTipField.getText(), promptTipField.getText().length());
                    }

                }
            });

            tenPercent.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    amountToTip = (float)(subTotal * ((Double.valueOf(tenPercent.getText().toString().replace("%",""))) / 100));
                    grandTotalAmount = subTotal + amountToTip;
                    dlogGrandTotal.setText(Global.formatDoubleToCurrency(grandTotalAmount));
                    promptTipField.setText("");
                    totalAmountView.setText(String.format(Locale.getDefault(), activity.getString(R.string.total_plus_tip),
                            Global.formatDoubleToCurrency(subTotal), Global.formatDoubleToCurrency(amountToTip)));
                }
            });

            fifteenPercent.setOnClickListener(new Button.OnClickListener() {

                @Override
                public void onClick(View v) {
                    amountToTip = (float)(subTotal * ((Double.valueOf(fifteenPercent.getText().toString().replace("%",""))) / 100));
                    grandTotalAmount = subTotal + amountToTip;
                    dlogGrandTotal.setText(Global.formatDoubleToCurrency(grandTotalAmount));
                    promptTipField.setText("");
                    totalAmountView.setText(String.format(Locale.getDefault(), activity.getString(R.string.total_plus_tip),
                            Global.formatDoubleToCurrency(subTotal), Global.formatDoubleToCurrency(amountToTip)));
                }
            });

            twentyPercent.setOnClickListener(new Button.OnClickListener() {

                @Override
                public void onClick(View v) {
                    amountToTip = (float)(subTotal * ((Double.valueOf(twentyPercent.getText().toString().replace("%",""))) / 100));
                    grandTotalAmount = subTotal + amountToTip;
                    dlogGrandTotal.setText(Global.formatDoubleToCurrency(grandTotalAmount));
                    promptTipField.setText("");
                    totalAmountView.setText(String.format(Locale.getDefault(), activity.getString(R.string.total_plus_tip),
                            Global.formatDoubleToCurrency(subTotal), Global.formatDoubleToCurrency(amountToTip)));
                }
            });

            noneButton.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callBack.noneTipGratuityWasPressed(totalAmountView,dlogGrandTotal,subTotal);
                }
            });

            cancelTip.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callBack.cancelTipGratuityWasPressed(dialog);
                }
            });

            saveTip.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callBack.saveTipGratuityWasPressed(dialog,amountToTip);
                }
            });
            dialog.show();
        }

        public void showTipsForCashPayments(EditText amountDue,EditText promptTip, EditText paid, String orderSubTotal){
            this.promptTipField = promptTip;
            setUpGratuityDialog();

            double amountToBePaid = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(paid));
            grandTotalAmount = amountToBePaid + amountToTip;
            final double subTotal;
            if (isFrom_MainMenu) {
                subTotal = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(amountDue));
            } else {
                subTotal = Double.parseDouble(orderSubTotal);
            }
            final Button tenPercent = dialogLayout.findViewById(R.id.tenPercent);
            final Button fifteenPercent = dialogLayout.findViewById(R.id.fifteenPercent);
            final Button twentyPercent = dialogLayout.findViewById(R.id.twentyPercent);
            if(!(myPref.getGratuityOne().isEmpty()))
                tenPercent.setText((myPref.getGratuityOne()+"%"));
            if(!(myPref.getGratuityTwo().isEmpty()))
                fifteenPercent.setText((myPref.getGratuityTwo()+"%"));
            if(!(myPref.getGratuityThree().isEmpty()))
                twentyPercent.setText((myPref.getGratuityThree()+"%"));

            dlogGrandTotal = dialogLayout.findViewById(R.id.grandTotalView);
            dlogGrandTotal.setText(Global.formatDoubleToCurrency(grandTotalAmount));


            promptTipField = dialogLayout.findViewById(R.id.otherTipAmountField);
            promptTipField.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
            promptTipField.setText("");

            Button cancelTip = dialogLayout.findViewById(R.id.cancelTipButton);
            Button saveTip = dialogLayout.findViewById(R.id.acceptTipButton);
            Button noneButton = dialogLayout.findViewById(R.id.noneButton);
            final TextView totalAmountView = dialogLayout.findViewById(R.id.totalAmountView);
            totalAmountView.setText(String.format(Locale.getDefault(), activity.getString(R.string.total_plus_tip),
                    Global.formatDoubleToCurrency(subTotal), Global.formatDoubleToCurrency(0)));
            promptTipField.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {

                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(s.toString())) > 0) {
                        amountToTip = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(s.toString()));
                        grandTotalAmount = subTotal + amountToTip;
                        dlogGrandTotal.setText(Global.formatDoubleToCurrency(grandTotalAmount));
                        totalAmountView.setText(String.format(Locale.getDefault(), activity.getString(R.string.total_plus_tip),
                                Global.formatDoubleToCurrency(subTotal), Global.formatDoubleToCurrency(amountToTip)));
                    }
                    NumberUtils.parseInputedCurrency(s, promptTipField);
                }
            });


            promptTipField.setOnFocusChangeListener(new View.OnFocusChangeListener() {

                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (v.hasFocus()) {
                        Selection.setSelection(promptTipField.getText(), promptTipField.getText().toString().length());
                    }

                }
            });

            tenPercent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    amountToTip = (float)(subTotal * ((Double.valueOf(tenPercent.getText().toString().replace("%",""))) / 100));
                    grandTotalAmount = subTotal + amountToTip;
                    dlogGrandTotal.setText(Global.formatDoubleToCurrency(grandTotalAmount));
                    promptTipField.setText("");
                    totalAmountView.setText(String.format(Locale.getDefault(), activity.getString(R.string.total_plus_tip),
                            Global.formatDoubleToCurrency(subTotal), Global.formatDoubleToCurrency(amountToTip)));
                }
            });

            fifteenPercent.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    amountToTip = (float)(subTotal * ((Double.valueOf(fifteenPercent.getText().toString().replace("%",""))) / 100));
                    grandTotalAmount = subTotal + amountToTip;
                    dlogGrandTotal.setText(Global.formatDoubleToCurrency(grandTotalAmount));
                    promptTipField.setText("");
                    totalAmountView.setText(String.format(Locale.getDefault(), activity.getString(R.string.total_plus_tip),
                            Global.formatDoubleToCurrency(subTotal), Global.formatDoubleToCurrency(amountToTip)));
                }
            });

            twentyPercent.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    amountToTip = (float)(subTotal * ((Double.valueOf(twentyPercent.getText().toString().replace("%",""))) / 100));
                    grandTotalAmount = subTotal + amountToTip;
                    dlogGrandTotal.setText(Global.formatDoubleToCurrency(grandTotalAmount));
                    promptTipField.setText("");
                    totalAmountView.setText(String.format(Locale.getDefault(), activity.getString(R.string.total_plus_tip),
                            Global.formatDoubleToCurrency(subTotal), Global.formatDoubleToCurrency(amountToTip)));
                }
            });


            noneButton.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callBack.noneTipGratuityWasPressed(totalAmountView,dlogGrandTotal,subTotal);
                }
            });


            cancelTip.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callBack.cancelTipGratuityWasPressed(dialog);
                }
            });

            saveTip.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callBack.saveTipGratuityWasPressed(dialog,amountToTip);
                }
            });
            dialog.show();
        }

        public void showTipsForPaxPayments(EditText amountTextView){
            setUpGratuityDialog();

            final double subTotal;
            subTotal = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(amountTextView.getText().toString()));

            double amountToBePaid = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(amountTextView.getText().toString()));
            grandTotalAmount = amountToBePaid + amountToTip;
            final TextView totalAmountView = dialogLayout.findViewById(R.id.totalAmountView);
            totalAmountView.setText(String.format(Locale.getDefault(), activity.getString(R.string.total_plus_tip),
                    Global.formatDoubleToCurrency(subTotal), Global.formatDoubleToCurrency(0)));

            dlogGrandTotal = dialogLayout.findViewById(R.id.grandTotalView);
            dlogGrandTotal.setText(Global.formatDoubleToCurrency(grandTotalAmount));

            final MyEditText promptTipField = dialogLayout.findViewById(R.id.otherTipAmountField);
            promptTipField.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
            promptTipField.clearFocus();
            promptTipField.setText("");

            final Button tenPercent = dialogLayout.findViewById(R.id.tenPercent);
            final Button fifteenPercent = dialogLayout.findViewById(R.id.fifteenPercent);
            final Button twentyPercent = dialogLayout.findViewById(R.id.twentyPercent);
            Button cancelTip = dialogLayout.findViewById(R.id.cancelTipButton);
            Button saveTip = dialogLayout.findViewById(R.id.acceptTipButton);
            Button noneButton = dialogLayout.findViewById(R.id.noneButton);

            if(!(myPref.getGratuityOne().isEmpty()))
                tenPercent.setText((myPref.getGratuityOne()+"%"));
            if(!(myPref.getGratuityTwo().isEmpty()))
                fifteenPercent.setText((myPref.getGratuityTwo()+"%"));
            if(!(myPref.getGratuityThree().isEmpty()))
                twentyPercent.setText((myPref.getGratuityThree()+"%"));

            promptTipField.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(s.toString())) > 0) {
                        amountToTip = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(s.toString()));
                        grandTotalAmount = subTotal + amountToTip;
                        dlogGrandTotal.setText(Global.formatDoubleToCurrency(grandTotalAmount));
                        totalAmountView.setText(String.format(Locale.getDefault(), activity.getString(R.string.total_plus_tip),
                                Global.formatDoubleToCurrency(subTotal), Global.formatDoubleToCurrency(amountToTip)));
                    }
                    NumberUtils.parseInputedCurrency(s, promptTipField);
                }
            });

            promptTipField.setOnFocusChangeListener(new View.OnFocusChangeListener() {

                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (v.hasFocus()) {
                        Selection.setSelection(promptTipField.getText(), promptTipField.getText().length());
                    }

                }
            });

            tenPercent.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    amountToTip = Double.valueOf(tenPercent.getText().toString().replace("%","")) / 100;
                    grandTotalAmount = subTotal + amountToTip;
                    dlogGrandTotal.setText(Global.formatDoubleToCurrency(grandTotalAmount));
                    promptTipField.setText("");
                    totalAmountView.setText(String.format(Locale.getDefault(), activity.getString(R.string.total_plus_tip),
                            Global.formatDoubleToCurrency(subTotal), Global.formatDoubleToCurrency(amountToTip)));
                }
            });

            fifteenPercent.setOnClickListener(new Button.OnClickListener() {

                @Override
                public void onClick(View v) {
                    amountToTip = Double.valueOf(fifteenPercent.getText().toString().replace("%","")) / 100;
                    grandTotalAmount = subTotal + amountToTip;
                    dlogGrandTotal.setText(Global.formatDoubleToCurrency(grandTotalAmount));
                    promptTipField.setText("");
                    totalAmountView.setText(String.format(Locale.getDefault(), activity.getString(R.string.total_plus_tip),
                            Global.formatDoubleToCurrency(subTotal), Global.formatDoubleToCurrency(amountToTip)));
                }
            });

            twentyPercent.setOnClickListener(new Button.OnClickListener() {

                @Override
                public void onClick(View v) {
                    amountToTip = Double.valueOf(twentyPercent.getText().toString().replace("%","")) / 100;
                    grandTotalAmount = subTotal + amountToTip;
                    dlogGrandTotal.setText(Global.formatDoubleToCurrency(grandTotalAmount));
                    promptTipField.setText("");
                    totalAmountView.setText(String.format(Locale.getDefault(), activity.getString(R.string.total_plus_tip),
                            Global.formatDoubleToCurrency(subTotal), Global.formatDoubleToCurrency(amountToTip)));
                }
            });

            noneButton.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callBack.noneTipGratuityWasPressed(totalAmountView,dlogGrandTotal,subTotal);
                }
            });
            cancelTip.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                   callBack.cancelTipGratuityWasPressed(dialog);
                }
            });
            saveTip.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callBack.saveTipGratuityWasPressed(dialog,amountToTip);
                }
            });
            dialog.show();
        }
    }
}
