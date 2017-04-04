package com.android.emobilepos.history.details;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.text.InputType;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.dao.ShiftDAO;
import com.android.dao.StoredPaymentsDAO;
import com.android.database.CustomersHandler;
import com.android.database.OrderProductsHandler;
import com.android.database.OrdersHandler;
import com.android.database.PaymentsHandler;
import com.android.database.ProductsImagesHandler;
import com.android.database.VoidTransactionsHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.models.orders.Order;
import com.android.emobilepos.models.orders.OrderProduct;
import com.android.emobilepos.models.realms.Payment;
import com.android.emobilepos.security.SecurityManager;
import com.android.payments.EMSPayGate_Default;
import com.android.saxhandler.SAXProcessCardPayHandler;
import com.android.support.CreditCardInfo;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.NumberUtils;
import com.android.support.Post;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import interfaces.EMSCallBack;

public class HistoryTransactionDetails_FA extends BaseFragmentActivityActionBar implements EMSCallBack, OnClickListener, OnItemClickListener {
    private boolean hasBeenCreated = false;
    private Global global;
    private final int CASE_TOTAL = 0;
    private final int CASE_OVERALL_PAID_AMOUNT = 1;
    private final int CASE_TIP_AMOUNT = 2;
    private final int CASE_CLERK_ID = 3;
    private final int CASE_ORD_COMMENT = 4;
    private final int CASE_SHIPVIA = 5;
    private final int CASE_ORD_TERMS = 6;
    private final int CASE_ORD_DELIVERY = 7;
    private final int CASE_CUST_EMAIL = 8;
    private final int CASE_PO = 9;
    private final int CASE_PAYMETHOD_NAME = 10;
    private final int CASE_PAY_ID = 11;
    private final int CASE_PAID_AMOUNT = 12;
    private final int CASE_PAID_AMOUNT_NO_CURRENCY = 13;
    private static List<String> allInfoLeft;
    private String order_id;
    private List<OrderProduct> orderedProd;
    private Drawable mapDrawable;
    private ProgressDialog myProgressDialog;
    private List<Payment> paymentMapList = new ArrayList<>();
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private int offsetForPayment = 0;
    private TextView custNameView;
    private Button btnPrint, btnVoid;
    private Activity activity;
    private MyPreferences myPref;
    private Order order;
    private List<Payment> paymentsToVoid;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_detailslv_layout);
        global = (Global) getApplication();
        activity = this;
        myPref = new MyPreferences(activity);
        ListView myListView = (ListView) findViewById(R.id.orderDetailsLV);
        btnPrint = (Button) findViewById(R.id.printButton);
        boolean hasRePrintPermissions = SecurityManager.hasPermissions(this, SecurityManager.SecurityAction.REPRINT_ORDER);
        boolean hasVoidPermissions = SecurityManager.hasPermissions(this, SecurityManager.SecurityAction.VOID_ORDER);
        btnPrint.setEnabled(hasRePrintPermissions);
        btnVoid = (Button) findViewById(R.id.btnVoid);
        btnVoid.setOnClickListener(this);
        TextView headerTitle = (TextView) findViewById(R.id.ordDetailsHeaderTitle);
        headerTitle.setText(getString(R.string.trans_details_title));
        View headerView = getLayoutInflater().inflate(R.layout.orddetails_lvheader_adapter, (ViewGroup) findViewById(R.id.order_header_root));
        custNameView = (TextView) headerView.findViewById(R.id.ordLVHeaderTitle);
        TextView date = (TextView) headerView.findViewById(R.id.ordLVHeaderSubtitle);
        ImageView receipt = (ImageView) headerView.findViewById(R.id.ordTicketImg);
        allInfoLeft = Arrays.asList(getString(R.string.trans_details_total), getString(R.string.trans_details_amount_paid),
                getString(R.string.trans_details_tip), getString(R.string.trans_details_clerk_id), getString(R.string.trans_details_comment),
                getString(R.string.trans_details_ship_via), getString(R.string.trans_details_terms), getString(R.string.trans_details_delivery),
                getString(R.string.trans_details_email), getString(R.string.trans_details_po));
        final Bundle extras = activity.getIntent().getExtras();
        OrdersHandler ordersHandler = new OrdersHandler(activity);
        order_id = extras.getString("ord_id");
        order = ordersHandler.getOrder(order_id);
        OrderProductsHandler orderProductsHandler = new OrderProductsHandler(activity);
        orderedProd = orderProductsHandler.getOrderProducts(order_id);
        CustomersHandler customersHandler = new CustomersHandler(activity);
        PaymentsHandler paymentHandler = new PaymentsHandler(activity);
        paymentMapList = paymentHandler.getPaymentDetailsForTransactions(order_id);
        String encodedImg = order.ord_signature;
        if (!encodedImg.isEmpty()) {
            Resources resources = activity.getResources();
            Drawable[] layers = new Drawable[2];
            layers[0] = resources.getDrawable(R.drawable.torn_paper);
            byte[] img = Base64.decode(encodedImg, Base64.DEFAULT);
            layers[1] = new BitmapDrawable(resources, BitmapFactory.decodeByteArray(img, 0, img.length));
            LayerDrawable layered = new LayerDrawable(layers);
            layered.setLayerInset(1, 100, 30, 50, 60);
            receipt.setImageDrawable(layered);
        }
        custNameView.setText(customersHandler.getSpecificValue("cust_name", order.cust_id));
        date.setText(getCaseData(CASE_TOTAL, 0) + " on " + order.ord_timecreated);
        myListView.addHeaderView(headerView);
        View footerView = getLayoutInflater().inflate(R.layout.orddetails_lvfooter_adapter, (ViewGroup) findViewById(R.id.order_footer_root));
        final ImageView mapImg = (ImageView) footerView.findViewById(R.id.ordDetailsMapImg);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        final int width = displayMetrics.widthPixels;
        final Handler mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (mapDrawable == null)
                    mapImg.setImageResource(R.drawable.map_no_image);
                else
                    mapImg.setImageDrawable(mapDrawable);
                //call setText here
            }
        };
        new Thread(new Runnable() {
            public void run() {
                Message msg = new Message();
                StringBuilder sb = new StringBuilder();
                String latitude = order.ord_latitude;//orderHashMap.get("ord_latitude");
                String longitude = order.ord_longitude;//orderHashMap.get("ord_longitude");
                if (!latitude.isEmpty() && !longitude.isEmpty()) {
                    sb.append("https://maps.googleapis.com/maps/api/staticmap?center=");
                    sb.append(latitude).append(",").append(longitude);
                    sb.append("&markers=color:red|label:S|");
                    sb.append(latitude).append(",").append(longitude);
                    sb.append("&zoom=16&size=").append(width).append("x").append(width).append("&sensor=false");
                    mapDrawable = createDrawableFromURL(sb.toString());
                    mHandler.sendMessage(msg);
                } else
                    mHandler.sendMessage(msg);

            }
        }).start();
        myListView.addFooterView(footerView);
        //Handle the click even and begin the process for Printing the transaction
        btnPrint.setEnabled(myPref.getPreferences(MyPreferences.pref_enable_printing));
        if (myPref.getPreferences(MyPreferences.pref_enable_printing)) {
            btnPrint.setOnClickListener(this);
        }
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        String curDate = sdf.format(new Date());
        if (order.isVoid != null && (order.isVoid.equals("1") ||
                !curDate.equals(Global.formatToDisplayDate(order.ord_timecreated, 0)))) {
            btnVoid.setEnabled(false && hasVoidPermissions);
            btnVoid.setClickable(false && hasVoidPermissions);
        } else {
            btnVoid.setEnabled(true && hasVoidPermissions);
            btnVoid.setClickable(true && hasVoidPermissions);
        }
        myPref = new MyPreferences(activity);
        ListViewAdapter myAdapter = new ListViewAdapter(activity);
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(ImageLoaderConfiguration.createDefault(activity));
        options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.emobile_icon).cacheInMemory(true).cacheOnDisc(true)
                .showImageForEmptyUri(R.drawable.ic_launcher).build();
        myListView.setAdapter(myAdapter);
        myListView.setOnItemClickListener(this);
        hasBeenCreated = true;
    }

    @Override
    public void onResume() {
        if (global.isApplicationSentToBackground(activity))
            global.loggedIn = false;
        global.stopActivityTransitionTimer();
        if (hasBeenCreated && !global.loggedIn) {
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
            global.loggedIn = false;
        global.startActivityTransitionTimer();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.printButton:
                btnPrint.setClickable(false);
                new printAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
                btnPrint.setClickable(true);
                break;
            case R.id.btnVoid:
                if (myPref.getPreferences(MyPreferences.pref_require_manager_pass_to_void_trans)) {
                    promptManagerPassword();
                } else {
                    confirmVoid();
                }
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
        if (pos > offsetForPayment && pos < offsetForPayment + paymentMapList.size() + 1) {
            int listIndex = pos - offsetForPayment - 1;
            String paymethodName = getCaseData(CASE_PAYMETHOD_NAME, listIndex);
            if (paymethodName != null && !paymethodName.isEmpty()) {
                Intent intent = new Intent(parent.getContext(), HistoryPaymentDetails_FA.class);
                intent.putExtra("histpay", true);
                intent.putExtra("pay_id", getCaseData(CASE_PAY_ID, listIndex));
                intent.putExtra("pay_amount", getCaseData(CASE_PAID_AMOUNT_NO_CURRENCY, listIndex));
                intent.putExtra("cust_name", custNameView.getText().toString());
                intent.putExtra("paymethod_name", paymethodName);
                startActivity(intent);
            }
        }
    }

    @Override
    public void cardWasReadSuccessfully(boolean read, CreditCardInfo cardManager) {
        if (read) {
            if (paymentsToVoid.size() > 0) {
                String voidAmount = NumberUtils.cleanCurrencyFormatedNumber(paymentsToVoid.get(0).getPay_amount());
                BigInteger voidAmountInt = new BigInteger(voidAmount.replace(".", ""));
                Global.mainPrinterManager.getCurrentDevice().saleReversal(paymentsToVoid.get(0), paymentsToVoid.get(0).getPay_transid());
                payHandler.createVoidPayment(paymentsToVoid.get(0), false, null);
                paymentsToVoid.remove(0);
            } else {
                Global.showPrompt(activity, R.string.dlog_title_success, getString(R.string.dlog_msg_transaction_voided));
            }
        } else {
            String errorMsg = getString(R.string.void_fail);
            Global.showPrompt(activity, R.string.payment, errorMsg);
            btnVoid.setEnabled(true);
            btnVoid.setClickable(true);
        }
    }

    @Override
    public void readerConnectedSuccessfully(boolean value) {

    }

    @Override
    public void scannerWasRead(String data) {

    }

    @Override
    public void startSignature() {

    }

    @Override
    public void nfcWasRead(String nfcUID) {

    }

    private class printAsync extends AsyncTask<String, String, String> {
        private boolean printSuccessful = true;

        @Override
        protected void onPreExecute() {
            myProgressDialog = new ProgressDialog(activity);
            myProgressDialog.setMessage("Printing...");
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.setCancelable(false);
            if (myProgressDialog.isShowing())
                myProgressDialog.dismiss();
            myProgressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            Bundle extras = activity.getIntent().getExtras();
            String trans_type = extras.getString("trans_type");
            if (Global.mainPrinterManager != null && Global.mainPrinterManager.getCurrentDevice() != null) {
                if (Global.OrderType.getByCode(Integer.parseInt(trans_type)) != Global.OrderType.CONSIGNMENT_FILLUP
                        && Global.OrderType.getByCode(Integer.parseInt(trans_type)) != Global.OrderType.CONSIGNMENT_PICKUP) {
                    printSuccessful = Global.mainPrinterManager.getCurrentDevice().printTransaction(order_id, Global.OrderType.getByCode(Integer.parseInt(trans_type)), true, false);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String unused) {
            myProgressDialog.dismiss();
            if (!printSuccessful)
                showPrintDlg();
        }
    }

    private void showPrintDlg() {
        final Dialog dlog = new Dialog(activity, R.style.Theme_TransparentTest);
        dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dlog.setCancelable(false);
        dlog.setContentView(R.layout.dlog_btn_left_right_layout);
        TextView viewTitle = (TextView) dlog.findViewById(R.id.dlogTitle);
        TextView viewMsg = (TextView) dlog.findViewById(R.id.dlogMessage);
        viewTitle.setText(R.string.dlog_title_error);
        viewMsg.setText(R.string.dlog_msg_failed_print);
        dlog.findViewById(R.id.btnDlogCancel).setVisibility(View.GONE);
        Button btnYes = (Button) dlog.findViewById(R.id.btnDlogLeft);
        Button btnNo = (Button) dlog.findViewById(R.id.btnDlogRight);
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

    private String getCaseData(int type, int position) {
        String data = "";

        switch (type) {
            case CASE_TOTAL:                //total
                data = Global.formatDoubleStrToCurrency(order.ord_total);
                break;
            case CASE_OVERALL_PAID_AMOUNT:                //amount paid
                int size = paymentMapList.size();
                String temp = "0.00";
                String otherAmount = "0";
                if (size > 0) {

                    if (paymentMapList.get(0).getPaymentMethod() != null &&
                            paymentMapList.get(0).getPaymentMethod().getPaymethod_name().equalsIgnoreCase("LoyaltyCard"))
                        otherAmount = Global.addSubsStrings(true, otherAmount, Global.formatNumToLocale(Double.parseDouble(paymentMapList.get(0).getPay_amount())));
                    else
                        temp = Global.formatNumToLocale(Double.parseDouble(paymentMapList.get(0).getPay_amount()));

                    for (int i = 1; i < size; i++) {
                        if (paymentMapList.get(i).getPaymentMethod().getPaymethod_name().equalsIgnoreCase("LoyaltyCard"))
                            otherAmount = Global.addSubsStrings(true, otherAmount, Global.formatNumToLocale(Double.parseDouble(paymentMapList.get(i).getPay_amount())));
                        else
                            temp = Global.addSubsStrings(true, temp, Global.formatNumToLocale(Double.parseDouble(paymentMapList.get(i).getPay_amount())));
                    }
                }
                temp = Double.toString(Global.formatNumFromLocale(temp));
                if (otherAmount.equals("0"))
                    data = Global.formatDoubleStrToCurrency(temp);
                else
                    data = Global.formatDoubleStrToCurrency(temp) + " + " + otherAmount + " Points";
                break;
            case CASE_TIP_AMOUNT:                //Tip
                int size1 = paymentMapList.size();
                String temp1 = "0.00";
                String storedVal;
                if (size1 > 0) {
                    temp1 = paymentMapList.get(0).getPay_tip();
                    if (temp1 == null || temp1.isEmpty())
                        temp1 = "0.00";
                    for (int i = 1; i < size1; i++) {
                        storedVal = paymentMapList.get(i).getPay_tip();
                        if (storedVal == null || storedVal.isEmpty())
                            storedVal = "0.00";
                        temp1 = Global.addSubsStrings(true, temp1, Global.formatNumToLocale(Double.parseDouble(storedVal)));
                    }
                }
                temp1 = Double.toString(Global.formatNumFromLocale(temp1));
                data = Global.formatDoubleStrToCurrency(temp1);
                break;
            case CASE_CLERK_ID:
                data = order.clerk_id;
                break;
            case CASE_ORD_COMMENT:
                data = order.ord_comment;
                break;
            case CASE_SHIPVIA:
                data = order.ord_shipvia;
                break;
            case CASE_ORD_TERMS:
                data = order.ord_terms;
                break;
            case CASE_ORD_DELIVERY:
                data = order.ord_delivery;
                break;
            case CASE_CUST_EMAIL:
                data = order.c_email;
                break;
            case CASE_PAYMETHOD_NAME:
                data = paymentMapList.get(position).getPaymentMethod().getPaymethod_name();
                break;
            case CASE_PAY_ID:
                data = paymentMapList.get(position).getPay_id();
                break;
            case CASE_PAID_AMOUNT:
                data = Global.formatDoubleStrToCurrency(paymentMapList.get(position).getPay_amount());
                break;
            case CASE_PO:
                data = order.ord_po;
                break;
            case CASE_PAID_AMOUNT_NO_CURRENCY:
                data = paymentMapList.get(position).getPay_amount();
                ;
                break;
        }
        return data;
    }

    private Drawable createDrawableFromURL(String urlString) {
        Drawable image;
        try {
            URL url = new URL(urlString);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();

            image = Drawable.createFromStream(is, "src");

        } catch (MalformedURLException e) {
            image = null;

        } catch (IOException e) {
            image = null;

        }
        return image;
    }

    private void promptManagerPassword() {
        final Dialog globalDlog = new Dialog(activity, R.style.Theme_TransparentTest);
        globalDlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        globalDlog.setCancelable(true);
        globalDlog.setContentView(R.layout.dlog_field_single_layout);

        final EditText viewField = (EditText) globalDlog.findViewById(R.id.dlogFieldSingle);
        viewField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        TextView viewTitle = (TextView) globalDlog.findViewById(R.id.dlogTitle);
        TextView viewMsg = (TextView) globalDlog.findViewById(R.id.dlogMessage);
        viewTitle.setText(R.string.dlog_title_confirm);
        viewMsg.setText(R.string.dlog_title_enter_manager_password);

        Button btnOk = (Button) globalDlog.findViewById(R.id.btnDlogSingle);
        btnOk.setText(R.string.button_ok);
        btnOk.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                globalDlog.dismiss();
                String pass = viewField.getText().toString();
                if (!pass.isEmpty() && myPref.loginManager(pass.trim())) {
                    //Void transaction
                    globalDlog.dismiss();
                    startVoidingTransaction();
                } else {
                    globalDlog.dismiss();
                    Global.showPrompt(activity, R.string.dlog_title_error, getString(R.string.invalid_password));
                }
            }
        });
        globalDlog.show();
    }

    private void confirmVoid() {

        final Dialog dlog = new Dialog(activity, R.style.Theme_TransparentTest);
        dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dlog.setCancelable(true);
        dlog.setCanceledOnTouchOutside(true);
        dlog.setContentView(R.layout.dlog_btn_left_right_layout);

        TextView viewTitle = (TextView) dlog.findViewById(R.id.dlogTitle);
        TextView viewMsg = (TextView) dlog.findViewById(R.id.dlogMessage);
        viewTitle.setText(R.string.dlog_title_confirm);
        viewMsg.setVisibility(View.GONE);
        Button btnVoid = (Button) dlog.findViewById(R.id.btnDlogRight);
        Button btnCancel = (Button) dlog.findViewById(R.id.btnDlogLeft);
        dlog.findViewById(R.id.btnDlogCancel).setVisibility(View.GONE);

        btnVoid.setText(R.string.button_void);
        btnCancel.setText(R.string.button_cancel);

        btnVoid.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dlog.dismiss();
                startVoidingTransaction();
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

    private void startVoidingTransaction() {
        btnVoid.setEnabled(false);
        btnVoid.setClickable(false);

        if (myPref.getPreferences(MyPreferences.pref_use_store_and_forward)) {
            if (StoredPaymentsDAO.getRetryTransCount(order_id) > 0) {
                //There are pending stored&forward cannot void_payment
                Global.showPrompt(activity, R.string.dlog_title_error, getString(R.string.dlog_msg_pending_stored_forward));
                btnVoid.setEnabled(true);
                btnVoid.setClickable(true);
            } else {
                voidTransaction();
            }
        } else if (myPref.getSwiperType() == Global.HANDPOINT) {
            if (Global.mainPrinterManager.getCurrentDevice() != null) {
                Global.mainPrinterManager.getCurrentDevice().loadCardReader(this, false);
                voidTransaction();
            }
        } else {
            voidTransaction();
        }
    }

    private List<Payment> listVoidPayments;
    private PaymentsHandler payHandler;

    private void voidTransaction() {
        double amountToBeSubstracted;
        OrdersHandler handler = new OrdersHandler(activity);
        handler.updateIsVoid(order_id);
        handler.updateIsProcessed(order_id, "9");

        VoidTransactionsHandler voidHandler = new VoidTransactionsHandler();

        Order ord = new Order(activity);
        ord.ord_id = order_id;
        ord.ord_type = order.ord_type;
        voidHandler.insert(ord);
        //Section to update the local ShiftPeriods database to reflect the VOID
//        ShiftPeriodsDBHandler handlerSP = new ShiftPeriodsDBHandler(activity);
        amountToBeSubstracted = Double.parseDouble(NumberUtils.cleanCurrencyFormatedNumber(order.ord_total)); //find total to be credited
        //update ShiftPeriods (isReturn set to true)
        ShiftDAO.updateShiftAmounts(Integer.parseInt(myPref.getShiftID()), amountToBeSubstracted, true);
        //Check if Stored&Forward active and delete from record if any payment were made
        if (myPref.getPreferences(MyPreferences.pref_use_store_and_forward)) {
            handler.updateOrderStoredFwd(order_id, "0");
            StoredPaymentsDAO.deletePaymentFromJob(order_id);
        }
        payHandler = new PaymentsHandler(activity);
        listVoidPayments = payHandler.getOrderPayments(order_id);
        int size = listVoidPayments.size();
        if (size > 0) {
            if (myPref.getSwiperType() == Global.HANDPOINT) {
                paymentsToVoid = new ArrayList<>();
                paymentsToVoid.addAll(listVoidPayments);
                String voidAmount = NumberUtils.cleanCurrencyFormatedNumber(paymentsToVoid.get(0).getPay_amount());
                Global.mainPrinterManager.getCurrentDevice().saleReversal(paymentsToVoid.get(0), paymentsToVoid.get(0).getPay_transid());
                payHandler.createVoidPayment(paymentsToVoid.get(0), false, null);
                paymentsToVoid.remove(0);
            } else {
                new voidPaymentAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        } else
            Global.showPrompt(activity, R.string.dlog_title_success, getString(R.string.dlog_msg_transaction_voided));
        activity.setResult(100);
    }

    public class voidPaymentAsync extends AsyncTask<Void, Void, Void> {
        HashMap<String, String> parsedMap = new HashMap<String, String>();

        @Override
        protected void onPreExecute() {
            myProgressDialog = new ProgressDialog(activity);
            myProgressDialog.setMessage(getString(R.string.voiding_payments));
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.setCancelable(false);
            myProgressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            int size = listVoidPayments.size();
            EMSPayGate_Default payGate;
            Post post = new Post();
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXProcessCardPayHandler handler = new SAXProcessCardPayHandler();
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
                    if (paymentType.equals("GIFTCARD")) {
                        payGate = new EMSPayGate_Default(activity, listVoidPayments.get(i));
                        xml = post.postData(13, activity, payGate.paymentWithAction(EMSPayGate_Default.EAction.VoidGiftCardAction, false, listVoidPayments.get(i).getCard_type(), null));
                        inSource = new InputSource(new StringReader(xml));
                        xr.setContentHandler(handler);
                        xr.parse(inSource);
                        parsedMap = handler.getData();
                        if (parsedMap != null && parsedMap.size() > 0 && parsedMap.get("epayStatusCode").equals("APPROVED"))
                            payHandler.createVoidPayment(listVoidPayments.get(i), true, parsedMap);

                        if (parsedMap != null) {
                            parsedMap.clear();
                        }
                    } else if (paymentType.equals("CASH")) {
                        payHandler.createVoidPayment(listVoidPayments.get(i), false, null);
                    } else if (!paymentType.equals("CHECK") && !paymentType.equals("WALLET")) {
                        payGate = new EMSPayGate_Default(activity, listVoidPayments.get(i));
                        xml = post.postData(13, activity, payGate.paymentWithAction(EMSPayGate_Default.EAction.VoidCreditCardAction, false, listVoidPayments.get(i).getCard_type(), null));
                        inSource = new InputSource(new StringReader(xml));
                        xr.setContentHandler(handler);
                        xr.parse(inSource);
                        parsedMap = handler.getData();
                        if (parsedMap != null && parsedMap.size() > 0 && parsedMap.get("epayStatusCode").equals("APPROVED"))
                            payHandler.createVoidPayment(listVoidPayments.get(i), true, parsedMap);
                        if (parsedMap != null) {
                            parsedMap.clear();
                        }
                    }
                }
            } catch (Exception e) {

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            myProgressDialog.dismiss();
            btnVoid.setEnabled(false);
            btnVoid.setClickable(false);
            Global.showPrompt(activity, R.string.dlog_title_success, getString(R.string.dlog_msg_transaction_voided));
        }
    }

    public class ListViewAdapter extends BaseAdapter implements Filterable {
        private LayoutInflater myInflater;
        private ProductsImagesHandler imgHandler;
        private Context context;

        ListViewAdapter(Context context) {
            this.context = context;
            imgHandler = new ProductsImagesHandler(activity);
            myInflater = LayoutInflater.from(context);
            offsetForPayment = allInfoLeft.size() + orderedProd.size() + 3;
        }

        @Override
        public int getCount() {
            return (allInfoLeft.size() + orderedProd.size() + paymentMapList.size() + 4); //the +4 is to include the dividers,+1 for the map
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            int type = getItemViewType(position);
            int iconId;
            if (convertView == null) {
                holder = new ViewHolder();
                switch (type) {
                    case 0: // divider
                    {
                        convertView = myInflater.inflate(R.layout.orddetails_lvdivider_adapter, null);
                        holder.textLine1 = (TextView) convertView.findViewById(R.id.orderDivLeft);
                        holder.textLine2 = (TextView) convertView.findViewById(R.id.orderDivRight);
                        if (position == 0)
                            holder.textLine1.setText(R.string.info);
                        else if (position == allInfoLeft.size() + 1)
                            holder.textLine1.setText(R.string.items);
                        else if (position == (orderedProd.size() + allInfoLeft.size() + 2))
                            holder.textLine1.setText(R.string.payments);
                        else
                            holder.textLine1.setText(R.string.map);
                        break;
                    }
                    case 1: // content in info divider
                    {
                        convertView = myInflater.inflate(R.layout.orddetails_lvinfo_adapter, null);
                        holder.textLine1 = (TextView) convertView.findViewById(R.id.ordInfoLeft);
                        holder.textLine2 = (TextView) convertView.findViewById(R.id.ordInfoRight);
                        holder.textLine1.setText(allInfoLeft.get(position - 1));
                        String temp = getCaseData((position - 1), 0);
                        if (temp != null && !temp.isEmpty())
                            holder.textLine2.setText(getCaseData((position - 1), 0));
                        break;
                    }
                    case 2: {
                        convertView = myInflater.inflate(R.layout.orddetails_lvproducts_adapter, null);
                        holder.textLine1 = (TextView) convertView.findViewById(R.id.ordProdTitle);
                        holder.textLine2 = (TextView) convertView.findViewById(R.id.ordProdSubtitle);
                        holder.ordProdPrice = (TextView) convertView.findViewById(R.id.ordProdPrice);
                        holder.ordProdQty = (TextView) convertView.findViewById(R.id.ordProdQty);
                        holder.iconImage = (ImageView) convertView.findViewById(R.id.prodIcon);
                        int ind = position - allInfoLeft.size() - 2;
                        holder.textLine1.setText(orderedProd.get(ind).getOrdprod_name());
                        holder.textLine2.setText(orderedProd.get(ind).getOrdprod_desc());
                        holder.ordProdQty.setText(String.format("%s x", orderedProd.get(ind).getOrdprod_qty()));
                        holder.ordProdPrice.setText(Global.formatDoubleStrToCurrency(orderedProd.get(ind).getFinalPrice()));
                        break;
                    }
                    case 3: {
                        convertView = myInflater.inflate(R.layout.orddetails_lvpayment_adapter, null);
                        holder.textLine1 = (TextView) convertView.findViewById(R.id.paidAmount);
                        holder.moreDetails = (ImageView) convertView.findViewById(R.id.paymentMoreDetailsIcon);
                        int listIndex = position - offsetForPayment;
                        String paymethodName = getCaseData(CASE_PAYMETHOD_NAME, listIndex);
                        if (paymethodName != null && !paymethodName.isEmpty()) {
                            holder.textLine1.setText(getCaseData(CASE_PAID_AMOUNT, listIndex));
                            String iconName = Global.paymentIconsMap.get(paymethodName);
                            Drawable voidDrawable = null;
                            if (paymentMapList.get(listIndex).getIsVoid().equalsIgnoreCase("1")) {
                                voidDrawable = context.getResources().getDrawable(R.drawable.void_payment);
                            }
                            if (iconName == null)
                                iconId = R.drawable.debitcard;// context.getResources().getIdentifier("debitcard", "drawable", context.getString(R.string.pkg_name));
                            else
                                iconId = context.getResources().getIdentifier(iconName, "drawable", context.getPackageName());

                            holder.textLine1.setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(iconId), null, voidDrawable, null);
                            holder.textLine1.setCompoundDrawablePadding(5);
                            holder.textLine1.setGravity(Gravity.CENTER_VERTICAL);
                        }
                        break;
                    }
                }
                if (convertView != null) {
                    convertView.setTag(holder);
                }
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            if (type == 0) {
                if (position == 0)
                    holder.textLine1.setText(getString(R.string.info));
                else if (position == allInfoLeft.size() + 1)
                    holder.textLine1.setText(getString(R.string.items));
                else if (position == (orderedProd.size() + allInfoLeft.size() + 2))
                    holder.textLine1.setText(getString(R.string.payments));
                else
                    holder.textLine1.setText(getString(R.string.map));
            } else if (type == 1) {
                holder.textLine1.setText(allInfoLeft.get(position - 1));
                holder.textLine2.setText(getCaseData((position - 1), 0));
            } else if (type == 2) {
                int ind = position - allInfoLeft.size() - 2;
                holder.textLine1.setText(orderedProd.get(ind).getOrdprod_name());
                holder.textLine2.setText(orderedProd.get(ind).getOrdprod_desc());
                holder.ordProdQty.setText(String.format("%s x", orderedProd.get(ind).getOrdprod_qty()));
                holder.ordProdPrice.setText(Global.formatDoubleStrToCurrency(orderedProd.get(ind).getFinalPrice()));
                imageLoader.displayImage(imgHandler.getSpecificLink("I", orderedProd.get(ind).getProd_id()), holder.iconImage, options);
            }

            return convertView;
        }

        @Override
        public Filter getFilter() {
            return null;
        }

        public class ViewHolder {
            TextView textLine1;
            TextView textLine2;
            TextView ordProdQty;
            TextView ordProdPrice;
            ImageView iconImage;
            ImageView moreDetails;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0 || (position == (allInfoLeft.size() + 1)) || (position == (orderedProd.size() + allInfoLeft.size() + 2)) || (position == (orderedProd.size() + allInfoLeft.size() + paymentMapList.size() + 3))) //divider
            //info				//items											//payments														//map
            {
                return 0;
            } else if (position > 0 && position <= allInfoLeft.size())            //info content
            {
                return 1;
            } else if (position > (allInfoLeft.size() + 1) && position <= orderedProd.size() + allInfoLeft.size() + 1)        //items content
            {
                return 2;
            }
            return 3;                    //PAYMENTS
        }

        @Override
        public int getViewTypeCount() {
            return 4;
        }
    }
}
