package com.android.emobilepos.history.details;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
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
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.database.InvoicePaymentsHandler;
import com.android.database.PaymentsHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.models.PaymentDetails;
import com.android.emobilepos.models.realms.Payment;
import com.android.emobilepos.security.SecurityManager;
import com.android.payments.EMSPayGate_Default;
import com.android.payments.EMSPayGate_Default.EAction;
import com.android.saxhandler.SAXProcessCardPayHandler;
import com.android.support.CreditCardInfo;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.Post;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;
import com.crashlytics.android.Crashlytics;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import interfaces.EMSCallBack;
import interfaces.EMSDeviceManagerPrinterDelegate;

public class HistoryPaymentDetails_FA extends BaseFragmentActivityActionBar implements EMSCallBack, OnClickListener {

    private boolean hasBeenCreated = false;
    private Global global;
    private Activity activity;

    private List<String> allInfoLeft;
    private List<String> allInfoRight = Arrays.asList("56-00021-2012", "Jul 20,2012 1:05PM",
            "Cash", "Not Specified", "56-00027-2012", "56-00061-2012");

    private String pay_id;
    private String paymethod_name;
    private ProgressDialog myProgressDialog;
    private PaymentsHandler payHandler;

    private Payment paymentToBeRefunded;
    private Drawable mapDrawable;
    private Button voidButton, printButton;
    private MyPreferences myPref;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.histpay_detailslv_layout);
        global = (Global) getApplication();
        activity = this;
        boolean hasRePrintPermissions = SecurityManager.hasPermissions(this, SecurityManager.SecurityAction.REPRINT_ORDER);
        boolean hasVoidPermissions = SecurityManager.hasPermissions(this, SecurityManager.SecurityAction.VOID_ORDER);

        Bundle extras = activity.getIntent().getExtras();
        myPref = new MyPreferences(activity);

        pay_id = extras.getString("pay_id");
        boolean isVoid = extras.getBoolean("isVoid");

        String pay_amount = extras.getString("pay_amount");
        String cust_name = extras.getString("cust_name");
        paymethod_name = extras.getString("paymethod_name");
        boolean isDeclined = Boolean.parseBoolean(extras.getString("isDeclined"));


        printButton = (Button) findViewById(R.id.printButton);
        voidButton = (Button) findViewById(R.id.histpayVoidBut);
        voidButton.setEnabled(!isDeclined && !isVoid);

        allInfoLeft = Arrays.asList(getString(R.string.pay_details_id), getString(R.string.pay_details_date),
                getString(R.string.pay_details_method), getString(R.string.pay_details_comment), getString(R.string.pay_details_inv_num),
                getString(R.string.pay_details_group_id), getString(R.string.pay_details_cc_num), getString(R.string.pay_details_auth_id),
                getString(R.string.pay_details_trans_id), getString(R.string.pay_details_clerk_id));

        payHandler = new PaymentsHandler(activity);
        PaymentDetails paymentDetails = payHandler.getPaymentDetails(pay_id, isDeclined);
        voidButton.setEnabled(hasVoidPermissions && !paymentDetails.isVoid() && TextUtils.isEmpty(paymentDetails.getJob_id()));
        if (extras.getBoolean("histpay")) {
            if (paymentDetails.getJob_id() != null && paymentDetails.getJob_id().isEmpty()) {
                if (paymentDetails.getInv_id().isEmpty()) {
                    InvoicePaymentsHandler invPayHandler = new InvoicePaymentsHandler(activity);
                    paymentDetails.setInv_id(invPayHandler.getInvoicePaymentsID(pay_id));
                }
            } else
                paymentDetails.setInv_id(paymentDetails.getJob_id());
            allInfoRight = Arrays.asList(pay_id, paymentDetails.getPay_date(), paymethod_name, paymentDetails.getPay_comment(),
                    paymentDetails.getInv_id(), paymentDetails.getGroup_pay_id(), "*" + paymentDetails.getCcnum_last4(),
                    paymentDetails.getAuthcode(), paymentDetails.getPay_transid(), paymentDetails.getClerk_id());
        } else {

            allInfoRight = Arrays.asList("", "", "", "", "", "");
        }

        ListView myListView = (ListView) findViewById(R.id.payDetailsLV);
        TextView headerTitle = (TextView) findViewById(R.id.HeaderTitle);
        headerTitle.setText(getString(R.string.pay_details_title));
        View headerView = getLayoutInflater().inflate(R.layout.orddetails_lvheader_adapter, (ViewGroup) findViewById(R.id.order_header_root));
        TextView name = (TextView) headerView.findViewById(R.id.ordLVHeaderTitle);
        TextView paid_amount = (TextView) headerView.findViewById(R.id.ordLVHeaderSubtitle);
        ImageView receipt = (ImageView) headerView.findViewById(R.id.ordTicketImg);
        name.setText(cust_name);
        paid_amount.setText(Global.formatDoubleStrToCurrency(pay_amount));
        String encodedImg = paymentDetails.getPay_signature();
        if (encodedImg != null && !encodedImg.isEmpty()) {
            Resources resources = activity.getResources();
            Drawable[] layers = new Drawable[2];
            layers[0] = resources.getDrawable(R.drawable.torn_paper);
            byte[] img = Base64.decode(encodedImg, Base64.DEFAULT);
            layers[1] = new BitmapDrawable(resources, BitmapFactory.decodeByteArray(img, 0, img.length));
            LayerDrawable layered = new LayerDrawable(layers);
            layered.setLayerInset(1, 50, 70, 40, 0);
            receipt.setImageDrawable(layered);
        }
        myListView.addHeaderView(headerView);
        View footerView = getLayoutInflater().inflate(R.layout.orddetails_lvfooter_adapter, (ViewGroup) findViewById(R.id.order_footer_root));
        final ImageView mapImg = (ImageView) footerView.findViewById(R.id.ordDetailsMapImg);
        loadMapImage(mapImg, paymentDetails.getPay_latitude(), paymentDetails.getPay_longitude());
        myListView.addFooterView(footerView);
        ListViewAdapter myAdapter = new ListViewAdapter(activity);
        myListView.setAdapter(myAdapter);

        //----------------------------------- Handle void_payment button -------------------------------------------//
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        String curDate = sdf.format(new Date());
        if (curDate.equals(paymentDetails.getPay_date()) && paymentDetails.getIsVoid().equals("0"))                //It was a payment done on the current date
        {
            voidButton.setOnClickListener(this);
        }
        //Handle the click event and begin the process for Printing the transaction
        MyPreferences myPref = new MyPreferences(activity);
        printButton.setEnabled(hasRePrintPermissions && myPref.getPreferences(MyPreferences.pref_enable_printing));
        if (hasRePrintPermissions && myPref.getPreferences(MyPreferences.pref_enable_printing)) {
            printButton.setOnClickListener(this);
        }
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
                printButton.setEnabled(false);
                new printAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                printButton.setEnabled(true);
                break;
            case R.id.histpayVoidBut:
                voidButton.setEnabled(false);
                if (myPref.getPreferences(MyPreferences.pref_require_manager_pass_to_void_trans))
                    promptManagerPassword();
                else
                    voidTransaction();
                break;
        }
    }

    private void loadMapImage(final ImageView mapImg, final String latitude, final String longitude) {
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
                // your logic
                StringBuilder sb = new StringBuilder();
                if (latitude != null && longitude != null && !latitude.isEmpty() && !longitude.isEmpty()) {
                    sb.append("https://maps.googleapis.com/maps/api/staticmap?center=");
                    sb.append(latitude).append(",").append(longitude);
                    sb.append("&markers=color:red|label:S|");
                    sb.append(latitude).append(",").append(longitude);
                    sb.append("&zoom=16&size=").append(width).append("x").append(width).append("&sensor=false");
                    mapDrawable = createDrawableFromURL(sb.toString());
                    Message msg = new Message();
                    mHandler.sendMessage(msg);
                }

            }
        }).start();
    }

    @Override
    public void cardWasReadSuccessfully(boolean read, CreditCardInfo cardManager) {
        if (myProgressDialog != null && myProgressDialog.isShowing()) {
            myProgressDialog.dismiss();
        }
        if (read) {
            payHandler.createVoidPayment(paymentToBeRefunded, false, null);
        } else {
            String errorMsg = getString(R.string.void_fail);
            Global.showPrompt(activity, R.string.payment, errorMsg);
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
            if (Global.mainPrinterManager != null && Global.mainPrinterManager.getCurrentDevice() != null) {
                printSuccessful = Global.mainPrinterManager.getCurrentDevice().printPaymentDetails(pay_id, 1, true, null);
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
        final Dialog dlog = new Dialog(this, R.style.Theme_TransparentTest);
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


    private void voidTransaction() {
        paymentToBeRefunded = payHandler.getPaymentForVoid(pay_id);
        if (myPref.getSwiperType() == Global.HANDPOINT || myPref.getSwiperType() == Global.ICMPEVO) {
            myProgressDialog = new ProgressDialog(activity);
            myProgressDialog.setMessage(getString(R.string.processing_refund));
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.setCancelable(false);
            myProgressDialog.show();
            EMSDeviceManagerPrinterDelegate device;
            if (Global.btSwiper != null && Global.btSwiper.getCurrentDevice() != null) {
                device = Global.btSwiper.getCurrentDevice();
            } else {
                device = Global.mainPrinterManager.getCurrentDevice();
            }
            if (device != null) {
                device.loadCardReader(this, false);
                device.saleReversal(paymentToBeRefunded, paymentToBeRefunded.getPay_transid());
            }
        } else if (paymethod_name.equals("Card")) {
            EMSPayGate_Default payGate = new EMSPayGate_Default(activity, paymentToBeRefunded);
            new processCardVoidAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, payGate.paymentWithAction(EAction.VoidCreditCardAction, false, paymentToBeRefunded.getCard_type(), null));
        } else if (paymethod_name.equals("GiftCard") || paymethod_name.equals("LoyaltyCard")) {
            EMSPayGate_Default payGate = new EMSPayGate_Default(activity, paymentToBeRefunded);
            new processCardVoidAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, payGate.paymentWithAction(EAction.VoidGiftCardAction, false, paymentToBeRefunded.getCard_type(), null));
        } else if (paymethod_name.equals("Check")) {
            if (paymentToBeRefunded.getPay_transid().isEmpty()) {
                payHandler.createVoidPayment(paymentToBeRefunded, false, null);
            } else {
                EMSPayGate_Default payGate = new EMSPayGate_Default(activity, paymentToBeRefunded);
                new processCardVoidAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, payGate.paymentWithAction(EAction.VoidCheckAction, false, paymentToBeRefunded.getCard_type(), null));
            }
        } else {
            payHandler.createVoidPayment(paymentToBeRefunded, false, null);
            Global.showPrompt(activity, R.string.payment_void_title, getString(R.string.payment_void_completed));
        }
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
                globalDlog.dismiss();
                String pass = viewField.getText().toString();
                if (!pass.isEmpty() && myPref.loginManager(pass.trim())) {
                    //Void transaction
                    globalDlog.dismiss();
                    voidTransaction();
                } else {
                    globalDlog.dismiss();
                    Global.showPrompt(activity, R.string.dlog_title_error, getString(R.string.invalid_password));
                }
            }
        });
        globalDlog.show();
    }


    public class processCardVoidAsync extends AsyncTask<String, String, String> {
        boolean wasProcessed = false;
        HashMap<String, String> parsedMap = new HashMap<>();
        private String errorMsg = getString(R.string.coundnot_proceess_payment);

        @Override
        protected void onPreExecute() {
            myProgressDialog = new ProgressDialog(activity);
            myProgressDialog.setMessage(getString(R.string.processing_refund));
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.setCancelable(false);
            myProgressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            Post post = new Post(activity);
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXProcessCardPayHandler handler = new SAXProcessCardPayHandler();

            try {
                String xml = post.postData(13, params[0]);
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
            } catch (Exception e) {
                e.printStackTrace();
                Crashlytics.logException(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String unused) {
            myProgressDialog.dismiss();
            voidButton.setEnabled(false);
            if (parsedMap != null && parsedMap.size() > 0 && parsedMap.get("epayStatusCode").equals("APPROVED")) //Void was successful
            {
                payHandler.createVoidPayment(paymentToBeRefunded, true, parsedMap);
            } else {
                Global.showPrompt(activity, R.string.dlog_title_error, errorMsg);
            }
        }
    }


    private Drawable createDrawableFromURL(String urlString) {
        Drawable image;
        try {
            URL url = new URL(urlString);
            InputStream is = (InputStream) url.getContent();
            image = Drawable.createFromStream(is, "src");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
            image = null;

        } catch (IOException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
            image = null;
        }
        return image;
    }


    public class ListViewAdapter extends BaseAdapter implements Filterable {
        private LayoutInflater myInflater;

        public ListViewAdapter(Context context) {
            myInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return (allInfoLeft.size() + 2); // the +2 is to include the
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
            if (convertView == null) {
                holder = new ViewHolder();
                switch (type) {
                    case 0: // divider
                    {
                        convertView = myInflater.inflate(R.layout.orddetails_lvdivider_adapter, null);
                        holder.textLine1 = (TextView) convertView.findViewById(R.id.orderDivLeft);
                        holder.textLine2 = (TextView) convertView.findViewById(R.id.orderDivRight);
                        if (position == 0) {
                            holder.textLine1.setText(getString(R.string.pay_details_infomation));
                        } else // if(position == allInfoLeft.size()+1)
                        {
                            holder.textLine1.setText(getString(R.string.pay_details_map));
                        }
                        break;
                    }
                    case 1: // content in divider
                    {
                        convertView = myInflater.inflate(R.layout.orddetails_lvinfo_adapter, null);
                        holder.textLine1 = (TextView) convertView.findViewById(R.id.ordInfoLeft);
                        holder.textLine2 = (TextView) convertView.findViewById(R.id.ordInfoRight);
                        holder.textLine1.setText(allInfoLeft.get(position - 1));
                        holder.textLine2.setText(allInfoRight.get(position - 1));
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
                if (position == 0) {
                    holder.textLine1.setText(getString(R.string.pay_details_infomation));
                } else {
                    holder.textLine1.setText(getString(R.string.pay_details_map));
                }
            } else if (type == 1) {
                holder.textLine1.setText(allInfoLeft.get(position - 1));
                holder.textLine2.setText(allInfoRight.get(position - 1));
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
            ImageView iconImage;

        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0 || (position == (allInfoLeft.size() + 1))) {
                return 0;
            }
            return 1;

        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }
    }

}
