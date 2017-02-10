
package com.android.support;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.dao.AssignEmployeeDAO;
import com.android.dao.DeviceTableDAO;
import com.android.dao.DinningTableDAO;
import com.android.dao.MixMatchDAO;
import com.android.dao.OrderAttributesDAO;
import com.android.dao.OrderProductAttributeDAO;
import com.android.dao.SalesAssociateDAO;
import com.android.dao.ShiftDAO;
import com.android.dao.UomDAO;
import com.android.database.ConsignmentTransactionHandler;
import com.android.database.CustomerInventoryHandler;
import com.android.database.CustomersHandler;
import com.android.database.DBManager;
import com.android.database.OrderProductsHandler;
import com.android.database.OrdersHandler;
import com.android.database.PayMethodsHandler;
import com.android.database.PaymentsHandler;
import com.android.database.PaymentsXML_DB;
import com.android.database.PriceLevelHandler;
import com.android.database.PriceLevelItemsHandler;
import com.android.database.ProductAddonsHandler;
import com.android.database.ProductAliases_DB;
import com.android.database.ProductsHandler;
import com.android.database.ShiftPeriodsDBHandler;
import com.android.database.TemplateHandler;
import com.android.database.TimeClockHandler;
import com.android.database.TransferLocations_DB;
import com.android.database.VoidTransactionsHandler;
import com.android.emobilepos.BuildConfig;
import com.android.emobilepos.OnHoldActivity;
import com.android.emobilepos.R;
import com.android.emobilepos.mainmenu.MainMenu_FA;
import com.android.emobilepos.mainmenu.SyncTab_FR;
import com.android.emobilepos.models.ItemPriceLevel;
import com.android.emobilepos.models.Order;
import com.android.emobilepos.models.OrderProduct;
import com.android.emobilepos.models.PriceLevel;
import com.android.emobilepos.models.Product;
import com.android.emobilepos.models.ProductAddons;
import com.android.emobilepos.models.ProductAlias;
import com.android.emobilepos.models.realms.AssignEmployee;
import com.android.emobilepos.models.realms.DinningTable;
import com.android.emobilepos.models.realms.MixMatch;
import com.android.emobilepos.models.realms.OrderAttributes;
import com.android.emobilepos.models.realms.PaymentMethod;
import com.android.emobilepos.models.realms.SalesAssociate;
import com.android.emobilepos.models.realms.Shift;
import com.android.emobilepos.models.salesassociates.DinningLocationConfiguration;
import com.android.emobilepos.ordering.OrderingMain_FA;
import com.android.saxhandler.SAXParserPost;
import com.android.saxhandler.SAXPostHandler;
import com.android.saxhandler.SAXPostTemplates;
import com.android.saxhandler.SAXProcessCardPayHandler;
import com.android.saxhandler.SAXSendConsignmentTransaction;
import com.android.saxhandler.SAXSendCustomerInventory;
import com.android.saxhandler.SAXSendInventoryTransfer;
import com.android.saxhandler.SAXSyncNewCustomerHandler;
import com.android.saxhandler.SAXSyncPayPostHandler;
import com.android.saxhandler.SAXSyncVoidTransHandler;
import com.android.saxhandler.SAXSynchHandler;
import com.android.saxhandler.SAXSynchOrdPostHandler;
import com.android.saxhandler.SaxLoginHandler;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import oauthclient.OAuthClient;
import oauthclient.OAuthManager;
import util.json.JsonUtils;

public class SynchMethods {
    private Post post;
    private Context context;
    private String xml;
    private InputSource inSource;
    private SAXParser sp;
    private XMLReader xr;
    private List<String[]> data;
    private String tempFilePath;
    private boolean checkoutOnHold = false, downloadHoldList = false;

    private ProgressDialog myProgressDialog;
    private int type;

    private boolean didSendData = true;
    private boolean isFromMainMenu = false;

    private Intent onHoldIntent;
    private HttpClient client;
    private Gson gson = JsonUtils.getInstance();

    private static OAuthManager getOAuthManager(Context activity) {
        MyPreferences preferences = new MyPreferences(activity);
        return OAuthManager.getInstance(activity, preferences.getAcctNumber(), preferences.getAcctPassword());
    }

    public SynchMethods(DBManager managerInst) {
        post = new Post();
        client = new HttpClient();
        SAXParserFactory spf = SAXParserFactory.newInstance();
        context = managerInst.getContext();
        data = new ArrayList<>();
        if (OAuthManager.isExpired(context) && NetworkUtils.isConnectedToInternet(context)) {
            OAuthManager oAuthManager = getOAuthManager(context);
            try {
                oAuthManager.requestToken();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        tempFilePath = context.getApplicationContext().getFilesDir().getAbsolutePath() + "/temp.xml";
        try {
            sp = spf.newSAXParser();
            xr = sp.getXMLReader();
        } catch (ParserConfigurationException e) {
        } catch (SAXException e) {
        }
    }

    private void showProgressDialog() {
        if (myProgressDialog == null) {
            myProgressDialog = new ProgressDialog(context);
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.setCancelable(false);
        }
        myProgressDialog.show();
    }

    private void dismissProgressDialog() {
        if (myProgressDialog != null && myProgressDialog.isShowing()) {
            myProgressDialog.dismiss();
        }
    }

    private boolean isReceive = false;

    public void synchReceive(int type, Activity activity) {
        this.type = type;
        isReceive = true;
        new ResynchAsync(activity).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
    }

    public void getLocationsInventory(Activity activity) {
        new AsyncGetLocationsInventory(activity).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class ResynchAsync extends AsyncTask<String, String, String> {
        MyPreferences myPref = new MyPreferences(context);
        private Activity activity;

        private ResynchAsync(Activity activity) {
            this.activity = activity;
        }

        @Override
        protected void onPreExecute() {
            int orientation = context.getResources().getConfiguration().orientation;
            activity.setRequestedOrientation(Global.getScreenOrientation(context));
            showProgressDialog();
        }

        @Override
        protected void onProgressUpdate(String... params) {
            myProgressDialog.setMessage(params[0]);
        }

        public void updateProgress(String msg) {
            publishProgress(msg);
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                updateProgress("Getting Server Time");
                synchGetServerTime();
                updateProgress(context.getString(R.string.sync_dload_employee_data));
                synchEmployeeData();
                updateProgress(context.getString(R.string.sync_dload_address));
                synchAddresses();
                updateProgress(context.getString(R.string.sync_dload_categories));
                synchCategories();
                updateProgress(context.getString(R.string.sync_dload_cust));
                synchCustomers();
                updateProgress(context.getString(R.string.sync_dload_emp_inv));
                synchEmpInv();
                updateProgress(context.getString(R.string.sync_dload_prod_inv));
                synchProdInv();
                updateProgress(context.getString(R.string.sync_dload_invoices));
                synchInvoices();
                updateProgress(context.getString(R.string.sync_dload_pay_methods));
                synchPaymentMethods();
                updateProgress(context.getString(R.string.sync_dload_price_levels));
                synchPriceLevel();
                updateProgress(context.getString(R.string.sync_dload_item_price_levels));
                synchItemsPriceLevel();
                updateProgress(context.getString(R.string.sync_dload_printers));
                synchPrinters();
                updateProgress(context.getString(R.string.sync_dload_prodcatxref));
                synchProdCatXref();
                updateProgress(context.getString(R.string.sync_dload_productchainxref));
                synchProdChain();
                updateProgress(context.getString(R.string.sync_dload_product_addons));
                synchProdAddon();
                updateProgress(context.getString(R.string.sync_dload_products));
                synchProducts();
                updateProgress(context.getString(R.string.sync_dload_product_aliases));
                synchOrderAttributes();
                synchProductAliases();
                updateProgress(context.getString(R.string.sync_dload_products_images));
                synchProductImages();
                updateProgress(context.getString(R.string.sync_dload_products_attributes));
                synchDownloadProductsAttr();
                updateProgress(context.getString(R.string.sync_dload_ordprodattr));
                synchGetOrdProdAttr();
                updateProgress(context.getString(R.string.sync_dload_salestaxcodes));
                synchSalesTaxCode();
                updateProgress(context.getString(R.string.sync_dload_shipmethod));
                synchShippingMethods();
                updateProgress(context.getString(R.string.sync_dload_taxes));
                synchTaxes();
                updateProgress(context.getString(R.string.sync_dload_taxes_group));
                synchTaxGroup();
                updateProgress(context.getString(R.string.sync_dload_terms));
                synchTerms();
                updateProgress(context.getString(R.string.sync_dload_memotext));
                synchMemoText();
                updateProgress(context.getString(R.string.sync_dload_logo));
                synchAccountLogo();
                updateProgress(context.getString(R.string.sync_dload_device_default_values));
                synchDeviceDefaultValues();
                updateProgress(context.getString(R.string.sync_dload_last_pay_id));
                synchDownloadLastPayID();
                updateProgress(context.getString(R.string.sync_dload_volume_prices));
                synchVolumePrices();
                updateProgress(context.getString(R.string.sync_dload_uom));
                synchUoM();
                updateProgress(context.getString(R.string.sync_dload_templates));
                synchGetTemplates();

                if (Global.isIvuLoto) {
                    updateProgress(context.getString(R.string.sync_dload_ivudrawdates));
                    synchIvuLottoDrawDates();
                }
                updateProgress(context.getString(R.string.sync_dload_customer_inventory));
                synchDownloadCustomerInventory();
                updateProgress(context.getString(R.string.sync_dload_consignment_transaction));
                synchDownloadConsignmentTransaction();
                updateProgress(context.getString(R.string.sync_dload_shifts));
                synchShifts();
                updateProgress(context.getString(R.string.sync_dload_clerks));
                synchDownloadClerks();
                updateProgress(context.getString(R.string.sync_dload_salesassociate));
                synchDownloadSalesAssociate();
                updateProgress(context.getString(R.string.sync_dload_dinnertables));
                synchDownloadDinnerTable();
                synchSalesAssociateDinnindTablesConfiguration(context);
                updateProgress(context.getString(R.string.sync_dload_mixmatch));
                synchDownloadMixMatch();
                updateProgress(context.getString(R.string.sync_dload_termsandconditions));
                synchDownloadTermsAndConditions();
                if (myPref.getPreferences(MyPreferences.pref_enable_location_inventory)) {
                    if (isReceive)
                        updateProgress(context.getString(R.string.sync_dload_locations));
                    else
                        updateProgress(context.getString(R.string.sync_dload_locations));
                    synchLocations();
                    if (isReceive)
                        updateProgress(context.getString(R.string.sync_dload_locations_inventory));
                    else
                        updateProgress(context.getString(R.string.sync_dload_locations_inventory));
                    synchLocationsInventory();
                }
                updateProgress("Updating Sync Time");
                synchUpdateSyncTime();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String unused) {
            isReceive = false;
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy h:mm a", Locale.getDefault());
            String date = sdf.format(new Date());

            myPref.setLastReceiveSync(date);

            if (!activity.isFinishing() && !activity.isDestroyed()) {
                dismissProgressDialog();
            }
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            if (type == Global.FROM_LOGIN_ACTIVITTY) {
                Intent intent = new Intent(context, MainMenu_FA.class);
                context.startActivity(intent);
                activity.finish();
            } else if (type == Global.FROM_REGISTRATION_ACTIVITY) {
                Intent intent = new Intent(context, MainMenu_FA.class);
                activity.setResult(-1);
                context.startActivity(intent);
                activity.finish();
            } else if (type == Global.FROM_SYNCH_ACTIVITY) {
                SyncTab_FR.syncTabHandler.sendEmptyMessage(0);
            }
        }

    }

    private boolean isSending = false;

    public void synchSend(int type, boolean isFromMainMenu, Activity activity) {
        Global.isForceUpload = false;
        this.type = type;
        this.isFromMainMenu = isFromMainMenu;
        if (!isSending)
            new SendAsync(activity).execute("");

    }

    public void synchForceSend(Activity activity) {
        Global.isForceUpload = true;
        if (!isSending)
            new ForceSendAsync(activity).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class SendAsync extends AsyncTask<String, String, String> {
        boolean proceed = false;
        MyPreferences myPref = new MyPreferences(context);
        String synchStage = "";
        TextView synchTextView;
        private Activity activity;

        private SendAsync(Activity activity) {
            this.activity = activity;
        }

        @Override
        protected void onPreExecute() {
            isSending = true;
            int orientation = context.getResources().getConfiguration().orientation;

            activity.setRequestedOrientation(Global.getScreenOrientation(context));

            if (isFromMainMenu) {
                MainMenu_FA synchActivity = (MainMenu_FA) context;
                synchTextView = synchActivity.getSynchTextView();
            }

            myProgressDialog = new ProgressDialog(context);

            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.setCancelable(false);
            myProgressDialog.setMax(100);
            myProgressDialog.show();

        }

        @Override
        protected void onProgressUpdate(String... params) {
            if (!isFromMainMenu) {
                if (!myProgressDialog.isShowing())
                    myProgressDialog.show();
                myProgressDialog.setMessage(params[0]);
            } else {
                if (!synchTextView.isShown())
                    synchTextView.setVisibility(View.VISIBLE);
                synchTextView.setText(params[0]);
            }
        }

        public void updateProgress(String msg) {
            publishProgress(msg);
        }

        @Override
        protected String doInBackground(String... params) {

            updateProgress("Please Wait...");
            if (NetworkUtils.isConnectedToInternet(context)) {
                try {

                    synchStage = context.getString(R.string.sync_sending_reverse);
                    sendReverse(this);

                    synchStage = context.getString(R.string.sync_sending_payment);
                    sendPayments(this);

                    if (didSendData) {
                        synchStage = context.getString(R.string.sync_sending_void);
                        sendVoidTransactions(this);

                    }

                    // add signatures
                    if (didSendData) {
                        synchStage = context.getString(R.string.sync_sending_templates);
                        sendTemplates(this);
                    }

                    if (didSendData) {
                        synchStage = context.getString(R.string.sync_sending_cust);
                        sendNewCustomers(this);
                    }

                    // add shifts

                    if (didSendData) {
                        synchStage = context.getString(R.string.sync_sending_wallet_order);
                        sendWalletOrders(this);
                    }

                    if (didSendData) {
                        synchStage = context.getString(R.string.sync_sending_orders);
                        sendOrders(this);
                    }

                    if (didSendData) {
                        synchStage = context.getString(R.string.sync_sending_inventory_transfer);
                        sendInventoryTransfer(this);
                    }

                    if (didSendData) {
                        synchStage = context.getString(R.string.sync_sending_customer_inventory);
                        sendCustomerInventory(this);
                    }

                    if (didSendData) {
                        synchStage = context.getString(R.string.sync_sending_consignment_transaction);
                        sendConsignmentTransaction(this);
                    }

                    if (didSendData) {
                        synchStage = context.getString(R.string.sync_sending_shifts);
                        postShift(activity);
                    }

                    if (didSendData) {
                        synchStage = context.getString(R.string.sync_sending_time_clock);
                        sendTimeClock(this);
                    }

                    if (didSendData)
                        proceed = true;

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else
                xml = context.getString(R.string.dlog_msg_no_internet_access);
            return null;
        }

        @Override
        protected void onPostExecute(String unused) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy h:mm a", Locale.getDefault());
            String date = sdf.format(new Date());
            myPref.setLastSendSync(date);

            isSending = false;
            myProgressDialog.dismiss();

            if (type == Global.FROM_SYNCH_ACTIVITY) {
                if (!isFromMainMenu) {
                    SyncTab_FR.syncTabHandler.sendEmptyMessage(0);
                } else {
                    synchTextView.setVisibility(View.GONE);
                }

            }

//            if (proceed && dbManager.isSendAndReceive()) {
//                dbManager.updateDB();
//            } else
            if (!proceed) {
                // failed to synch....
                if (TextUtils.isEmpty(xml)) {
                    xml = context.getString(R.string.sync_fail);
                }
                Global.showPrompt(context, R.string.dlog_title_error, xml);
            }

            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
    }

    private class ForceSendAsync extends AsyncTask<Void, String, Void> {

        private Activity activity;

        private ForceSendAsync(Activity activity) {
            this.activity = activity;
        }

        @Override
        protected void onPreExecute() {
            isSending = true;
            int orientation = context.getResources().getConfiguration().orientation;

            activity.setRequestedOrientation(Global.getScreenOrientation(context));

            myProgressDialog = new ProgressDialog(context);

            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.setCancelable(false);
            myProgressDialog.setMax(100);
        }

        public void updateProgress(String msg) {
            publishProgress(msg);
        }

        @Override
        protected void onProgressUpdate(String... params) {

            if (!myProgressDialog.isShowing())
                myProgressDialog.show();
            myProgressDialog.setMessage(params[0]);

        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                sendReverse(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                sendPayments(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                sendVoidTransactions(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                sendTemplates(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                sendNewCustomers(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                sendWalletOrders(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                sendOrders(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                sendInventoryTransfer(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                sendCustomerInventory(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                sendConsignmentTransaction(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                postShift(activity);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                sendTimeClock(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            Global.isForceUpload = false;
//            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy h:mm a", Locale.getDefault());
            String date = DateUtils.getDateAsString(new Date(), DateUtils.DATE_MMM_dd_yyyy_h_mm_a);
            MyPreferences myPref = new MyPreferences(context);
            myPref.setLastSendSync(date);

            isSending = false;
            myProgressDialog.dismiss();

            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
    }


    public void synchGetOnHoldDetails(int type, Intent intent, String ordID, Activity activity) {
        onHoldIntent = intent;
        this.type = type;
        new SynchDownloadOnHoldDetails(activity).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ordID);
    }

    public void synchSendOnHold(boolean downloadHoldList, boolean checkoutOnHold, Activity activity) {
        this.downloadHoldList = downloadHoldList;
        this.checkoutOnHold = checkoutOnHold;
        new SynchSendOrdersOnHold(activity).execute();
    }

    private class SynchDownloadOnHoldProducts extends AsyncTask<String, String, String> {
        MyPreferences myPref = new MyPreferences(context);
        private Activity activity;

        private SynchDownloadOnHoldProducts(Activity activity) {
            this.activity = activity;
        }

        @Override
        protected void onPreExecute() {

            int orientation = context.getResources().getConfiguration().orientation;
            activity.setRequestedOrientation(Global.getScreenOrientation(context));

            myProgressDialog = new ProgressDialog(context);
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.setCancelable(false);
            myProgressDialog.show();
        }

        @Override
        protected void onProgressUpdate(String... params) {
            myProgressDialog.setMessage(params[0]);
        }

        public void updateProgress(String msg) {
            publishProgress(msg);
        }

        @Override
        protected String doInBackground(String... params) {
            if (NetworkUtils.isConnectedToInternet(context)) {
                try {
                    updateProgress(context.getString(R.string.sync_dload_ordersonhold));
                    synchOrdersOnHoldList(context);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        protected void onPostExecute(String unused) {
//            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy h:mm a", Locale.getDefault());
            String date = DateUtils.getDateAsString(new Date(), DateUtils.DATE_MMM_dd_yyyy_h_mm_a);
            myPref.setLastReceiveSync(date);
            myProgressDialog.dismiss();
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            Intent intent = new Intent(context, OnHoldActivity.class);
            context.startActivity(intent);
        }

    }

    private class SynchDownloadOnHoldDetails extends AsyncTask<String, String, String> {
        boolean proceedToView = false;
        private Activity activity;

        private SynchDownloadOnHoldDetails(Activity activity) {
            this.activity = activity;
        }

        @Override
        protected void onPreExecute() {

            int orientation = context.getResources().getConfiguration().orientation;
            activity.setRequestedOrientation(Global.getScreenOrientation(context));
            myProgressDialog = new ProgressDialog(context);
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.setCancelable(false);
            myProgressDialog.show();
        }

        @Override
        protected void onProgressUpdate(String... params) {
            myProgressDialog.setMessage(params[0]);
        }

        public void updateProgress(String msg) {
            publishProgress(msg);
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                updateProgress(context.getString(R.string.sync_dload_ordersonhold));
                synchOrdersOnHoldDetails(context, params[0]);
                OrderProductsHandler orderProdHandler = new OrderProductsHandler(context);
                Cursor c = orderProdHandler.getOrderProductsOnHold(params[0]);
                if (BuildConfig.DELETE_INVALID_HOLDS || (c != null && c.getCount() > 0)) {
                    proceedToView = true;
                    if (type == 0)
                        ((OnHoldActivity) context).addOrderProducts(activity, c);
                } else
                    proceedToView = false;
                if (c != null) {
                    c.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String unused) {
            myProgressDialog.dismiss();
            if (proceedToView) {
                if (type == 0) {
                    activity.startActivityForResult(onHoldIntent, 0);
                    activity.finish();
                } else//print
                {
                    ((OnHoldActivity) context).printOnHoldTransaction();
                }
            } else
                Toast.makeText(context, "Failed to download...", Toast.LENGTH_LONG).show();
        }

    }

    private class SynchSendOrdersOnHold extends AsyncTask<Void, String, String> {
        boolean isError = false;
        String err_msg = "";
        private Activity activity;

        private SynchSendOrdersOnHold(Activity activity) {
            this.activity = activity;
        }

        @Override
        protected void onPreExecute() {
            int orientation = context.getResources().getConfiguration().orientation;
            activity.setRequestedOrientation(Global.getScreenOrientation(context));
            if (myProgressDialog != null && myProgressDialog.isShowing())
                myProgressDialog.dismiss();
            myProgressDialog = new ProgressDialog(context);
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.setCancelable(false);

            if (!checkoutOnHold) {
                myProgressDialog.show();
            }

        }

        @Override
        protected void onProgressUpdate(String... params) {
            myProgressDialog.setMessage(params[0]);
        }

        public void updateProgress(String msg) {
            publishProgress(msg);
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                if (NetworkUtils.isConnectedToInternet(context)) {
                    err_msg = sendOrdersOnHold(this);
                    if (err_msg.isEmpty()) {
                        if (checkoutOnHold)
                            post.postData(Global.S_CHECKOUT_ON_HOLD, context, Global.lastOrdID);
                    } else
                        isError = true;
                } else {
                    isError = true;
                    err_msg = context.getString(R.string.dlog_msg_no_internet_access);
                }
            } catch (Exception e) {
                isError = true;
                err_msg = "Unhandled Exception";
            }
            return null;
        }

        protected void onPostExecute(String unused) {
            if (!activity.isFinishing() && myProgressDialog != null && myProgressDialog.isShowing())
                myProgressDialog.dismiss();
            if (!downloadHoldList) {
                boolean closeActivity = true;
                if (context instanceof OrderingMain_FA &&
                        ((OrderingMain_FA) context).getRestaurantSaleType() == Global.RestaurantSaleType.EAT_IN) {
                    closeActivity = false;
                }
                if (!checkoutOnHold && closeActivity) {
                    activity.finish();
                }
            } else if (!isError) {
                new SynchDownloadOnHoldProducts(activity).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
            } else {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                Intent intent = new Intent(context, OnHoldActivity.class);
                context.startActivity(intent);
            }
        }

    }

    private class AsyncGetLocationsInventory extends AsyncTask<Void, String, Void> {
        private Activity activity;

        private AsyncGetLocationsInventory(Activity activity) {
            this.activity = activity;
        }

        @Override
        protected void onPreExecute() {

            int orientation = context.getResources().getConfiguration().orientation;
            activity.setRequestedOrientation(Global.getScreenOrientation(context));
            myProgressDialog = new ProgressDialog(context);
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.setCancelable(false);
            myProgressDialog.show();
        }

        @Override
        protected void onProgressUpdate(String... params) {
            myProgressDialog.setMessage(params[0]);
        }

        public void updateProgress(String msg) {
            publishProgress(msg);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (isReceive)
                    updateProgress(context.getString(R.string.sync_dload_locations));
                else
                    updateProgress(context.getString(R.string.sync_dload_locations));
                synchLocations();
                if (isReceive)
                    updateProgress(context.getString(R.string.sync_dload_locations_inventory));
                else
                    updateProgress(context.getString(R.string.sync_dload_locations_inventory));
                synchLocationsInventory();
            } catch (Exception e) {
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            myProgressDialog.dismiss();
        }

    }

    /************************************
     * Send Methods
     ************************************/

    private void sendReverse(Object task) {
        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXProcessCardPayHandler handler = new SAXProcessCardPayHandler();
            HashMap<String, String> parsedMap;

            PaymentsXML_DB _paymentsXML_DB = new PaymentsXML_DB(context);
            Cursor c = _paymentsXML_DB.getReversePayments();
            int size = c.getCount();
            if (size > 0) {
                if (Global.isForceUpload)
                    ((ForceSendAsync) task).updateProgress(context.getString(R.string.sync_sending_reverse));
                else
                    ((SendAsync) task).updateProgress(context.getString(R.string.sync_sending_reverse));

                do {

                    String xml = post.postData(13, context,
                            c.getString(c.getColumnIndex("payment_xml")));

                    if (!xml.equals(Global.TIME_OUT)
                            && !xml.equals(Global.NOT_VALID_URL)) {
                        InputSource inSource = new InputSource(
                                new StringReader(xml));

                        SAXParser sp = spf.newSAXParser();
                        XMLReader xr = sp.getXMLReader();
                        xr.setContentHandler(handler);
                        xr.parse(inSource);
                        parsedMap = handler.getData();

                        if (parsedMap != null
                                && parsedMap.size() > 0
                                && (parsedMap.get("epayStatusCode").equals(
                                "APPROVED") || parsedMap.get(
                                "epayStatusCode").equals("DECLINE"))) {
                            _paymentsXML_DB.deleteRow(c.getString(c
                                    .getColumnIndex("app_id")));
                        }
                    }

                } while (c.moveToNext());
            }
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendNewCustomers(Object task) throws IOException, SAXException {
        SAXSyncNewCustomerHandler custSaxHandler = new SAXSyncNewCustomerHandler();
        CustomersHandler custHandler = new CustomersHandler(context);
        if (custHandler.getNumUnsyncCustomers() > 0) {
            if (Global.isForceUpload)
                ((ForceSendAsync) task).updateProgress(context.getString(R.string.sync_sending_cust));
            else
                ((SendAsync) task).updateProgress(context.getString(R.string.sync_sending_cust));
            xml = post.postData(Global.S_SUBMIT_CUSTOMER, context, "");
            inSource = new InputSource(new StringReader(xml));
            xr.setContentHandler(custSaxHandler);
            xr.parse(inSource);
            data = custSaxHandler.getEmpData();
            custHandler.updateIsSync(data);
            if (data.isEmpty())
                didSendData = false;
            data.clear();
        }

    }

    private void sendPayments(Object task) throws IOException, SAXException, ParserConfigurationException {
        SAXSyncPayPostHandler handler2 = new SAXSyncPayPostHandler();
        PaymentsHandler payHandler = new PaymentsHandler(context);
        if (payHandler.getNumUnsyncPayments() > 0) {
            if (Global.isForceUpload)
                ((ForceSendAsync) task).updateProgress(context.getString(R.string.sync_sending_payment));
            else
                ((SendAsync) task).updateProgress(context.getString(R.string.sync_sending_payment));
            xml = post.postData(Global.S_SUBMIT_PAYMENTS, context, "");
            inSource = new InputSource(new StringReader(xml));
            xr.setContentHandler(handler2);
            xr.parse(inSource);
            data = handler2.getEmpData();
            payHandler.updateIsSync(data);
            if (data.isEmpty())
                didSendData = false;
            data.clear();
        }
    }

    private void sendVoidTransactions(Object task) throws IOException, SAXException, ParserConfigurationException {
        SAXSyncVoidTransHandler voidHandler = new SAXSyncVoidTransHandler();
        VoidTransactionsHandler voidTrans = new VoidTransactionsHandler();

        if (voidTrans.getNumUnsyncVoids() > 0) {
            if (Global.isForceUpload)
                ((ForceSendAsync) task).updateProgress(context.getString(R.string.sync_sending_void));
            else
                ((SendAsync) task).updateProgress(context.getString(R.string.sync_sending_void));
            xml = post.postData(Global.S_SUBMIT_VOID_TRANSACTION, context, "");
            inSource = new InputSource(new StringReader(xml));
            xr.setContentHandler(voidHandler);
            xr.parse(inSource);
            data = voidHandler.getEmpData();
            voidTrans.updateIsSync(data);
            if (data.isEmpty())
                didSendData = false;
            data.clear();
        }
    }

    private void sendOrders(Object task) throws IOException, SAXException, ParserConfigurationException {
        SAXSynchOrdPostHandler handler = new SAXSynchOrdPostHandler();
        OrdersHandler ordersHandler = new OrdersHandler(context);
        while ((Global.isForceUpload && ordersHandler.getNumUnsyncOrders() > 0) ||
                (!Global.isForceUpload && ordersHandler.getNumUnsyncProcessedOrders() > ordersHandler.getNumUnsyncOrdersStoredFwd())) {
            if (Global.isForceUpload)
                ((ForceSendAsync) task).updateProgress(context.getString(R.string.sync_sending_orders));
            else
                ((SendAsync) task).updateProgress(context.getString(R.string.sync_sending_orders));
            xml = post.postData(Global.S_GET_XML_ORDERS, context, "");
            inSource = new InputSource(new StringReader(xml));
            xr.setContentHandler(handler);
            xr.parse(inSource);
            data = handler.getEmpData();
            ordersHandler.updateIsSync(data);
            if (data.isEmpty())
                didSendData = false;
            data.clear();
        }
    }

    private void sendInventoryTransfer(Object task) throws IOException, SAXException, ParserConfigurationException {
        SAXSendInventoryTransfer saxHandler = new SAXSendInventoryTransfer();
        TransferLocations_DB dbHandler = new TransferLocations_DB(context);
        if (dbHandler.getNumUnsyncTransfers() > 0) {
            if (Global.isForceUpload)
                ((ForceSendAsync) task).updateProgress(context.getString(R.string.sync_sending_inventory_transfer));
            else
                ((SendAsync) task).updateProgress(context.getString(R.string.sync_sending_inventory_transfer));
            xml = post.postData(Global.S_SUBMIT_LOCATIONS_INVENTORY, context, "");
            inSource = new InputSource(new StringReader(xml));
            xr.setContentHandler(saxHandler);
            xr.parse(inSource);
            data = saxHandler.getEmpData();
            dbHandler.updateIsSync(data);
            if (data.isEmpty())
                didSendData = false;
            data.clear();
        }
    }

    private void sendTimeClock(Object task) throws IOException, SAXException, ParserConfigurationException {
        SAXPostHandler handler = new SAXPostHandler();
        TimeClockHandler timeClockHandler = new TimeClockHandler(context);

        if (timeClockHandler.getNumUnsyncTimeClock() > 0) {
            if (Global.isForceUpload)
                ((ForceSendAsync) task).updateProgress(context.getString(R.string.sync_sending_time_clock));
            else
                ((SendAsync) task).updateProgress(context.getString(R.string.sync_sending_time_clock));
            xml = post.postData(Global.S_SUBMIT_TIME_CLOCK, context, null);
            inSource = new InputSource(new StringReader(xml));
            xr.setContentHandler(handler);
            xr.parse(inSource);
            int size = handler.getSize();

            if (size > 0) {

                for (int i = 0; i < size; i++) {
                    if (!handler.getData("timeclockid", i).isEmpty())
                        timeClockHandler.updateIsSync(handler.getData("timeclockid", i), handler.getData("status", i));
                }

            }
        }
    }

    private void sendCustomerInventory(Object task) throws IOException, SAXException, ParserConfigurationException {
        SAXSendCustomerInventory handler = new SAXSendCustomerInventory();
        CustomerInventoryHandler custInventoryHandler = new CustomerInventoryHandler(context);
        if (custInventoryHandler.getNumUnsyncItems() > 0) {
            if (Global.isForceUpload)
                ((ForceSendAsync) task).updateProgress(context.getString(R.string.sync_sending_customer_inventory));
            else
                ((SendAsync) task).updateProgress(context.getString(R.string.sync_sending_customer_inventory));
            xml = post.postData(Global.S_SUBMIT_CUSTOMER_INVENTORY, context, "");
            inSource = new InputSource(new StringReader(xml));
            xr.setContentHandler(handler);
            xr.parse(inSource);
            data = handler.getData();
            custInventoryHandler.updateIsSync(data);
            if (data.isEmpty())
                didSendData = false;
            data.clear();
        }
    }

    private void sendConsignmentTransaction(Object task) throws IOException, SAXException, ParserConfigurationException {
        SAXSendConsignmentTransaction handler = new SAXSendConsignmentTransaction();
        ConsignmentTransactionHandler consTransDBHandler = new ConsignmentTransactionHandler(context);
        if (consTransDBHandler.getNumUnsyncItems() > 0) {
            if (Global.isForceUpload)
                ((ForceSendAsync) task).updateProgress(context.getString(R.string.sync_sending_consignment_transaction));
            else
                ((SendAsync) task).updateProgress(context.getString(R.string.sync_sending_consignment_transaction));
            xml = post.postData(Global.S_SUBMIT_CONSIGNMENT_TRANSACTION, context, "");
            inSource = new InputSource(new StringReader(xml));
            xr.setContentHandler(handler);
            xr.parse(inSource);
            data = handler.getData();
            consTransDBHandler.updateIsSync(data);
            if (data.isEmpty())
                didSendData = false;
            data.clear();
        }
    }

    private void synchShifts() throws IOException, SAXException {
        try {
            ProductAliases_DB productAliasesDB = new ProductAliases_DB(context);
            Gson gson = JsonUtils.getInstance();
            GenerateXML xml = new GenerateXML(context);
            InputStream inputStream = client.httpInputStreamRequest(context.getString(R.string.sync_enablermobile_deviceasxmltrans) +
                    xml.downloadAll("Shifts"));
            JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
            List<ProductAlias> productAliases = new ArrayList<>();
            productAliasesDB.emptyTable();
            reader.beginArray();
            int i = 0;
            while (reader.hasNext()) {
                ProductAlias alias = gson.fromJson(reader, Product.class);
                productAliases.add(alias);
                i++;
                if (i == 1000) {
                    productAliasesDB.insert(productAliases);
                    productAliases.clear();
                    i = 0;
                }
            }
            productAliasesDB.insert(productAliases);
            reader.endArray();
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    private void sendShifts(Object task) throws IOException, SAXException, ParserConfigurationException {
//        SAXParserPost handler = new SAXParserPost();
//        ShiftPeriodsDBHandler dbHandler = new ShiftPeriodsDBHandler(context);
//
//        if (dbHandler.getNumUnsyncShifts() > 0) {
//            if (Global.isForceUpload)
//                ((ForceSendAsync) task).updateProgress(context.getString(R.string.sync_sending_shifts));
//            else
//                ((SendAsync) task).updateProgress(context.getString(R.string.sync_sending_shifts));
//            xml = post.postData(Global.S_SUBMIT_SHIFT, context, "");
//            inSource = new InputSource(new StringReader(xml));
//            xr.setContentHandler(handler);
//            xr.parse(inSource);
//            data = handler.getData();
//            dbHandler.updateIsSync(data);
//            if (data.isEmpty())
//                didSendData = false;
//            data.clear();
//        }
//    }

    private void sendWalletOrders(Object task) throws IOException, SAXException, ParserConfigurationException {
        SAXParserPost handler = new SAXParserPost();
        OrdersHandler dbHandler = new OrdersHandler(context);

        if (dbHandler.getNumUnsyncTupyxOrders() > 0) {

            if (Global.isForceUpload)
                ((ForceSendAsync) task).updateProgress(context.getString(R.string.sync_sending_wallet_order));
            else
                ((SendAsync) task).updateProgress(context.getString(R.string.sync_sending_wallet_order));
            xml = post.postData(Global.S_SUBMIT_WALLET_RECEIPTS, context, "");
            inSource = new InputSource(new StringReader(xml));
            xr.setContentHandler(handler);
            xr.parse(inSource);
            data = handler.getData();
            if (data.isEmpty())
                didSendData = false;
            data.clear();
        }
    }

    private void sendTemplates(Object task) throws IOException, SAXException, ParserConfigurationException {
        SAXPostTemplates handler = new SAXPostTemplates();

        TemplateHandler templateHandler = new TemplateHandler(context);
        if (templateHandler.getNumUnsyncTemplates() > 0) {
            if (Global.isForceUpload)
                ((ForceSendAsync) task).updateProgress(context.getString(R.string.sync_sending_templates));
            else
                ((SendAsync) task).updateProgress(context.getString(R.string.sync_sending_templates));
            xml = post.postData(Global.S_SUBMIT_TEMPLATES, context, "");
            inSource = new InputSource(new StringReader(xml));
            xr.setContentHandler(handler);
            xr.parse(inSource);
            data = handler.getEmpData();
            templateHandler.updateIsSync(data);
            if (data.isEmpty())
                didSendData = false;
            data.clear();
        }
    }

    /************************************
     * Send On Holds
     ************************************/

    private String sendOrdersOnHold(SynchSendOrdersOnHold task) throws IOException, SAXException, ParserConfigurationException {
        SAXSynchOrdPostHandler handler = new SAXSynchOrdPostHandler();
        OrdersHandler ordersHandler = new OrdersHandler(context);
        if (ordersHandler.getNumUnsyncOrdersOnHold() > 0) {
            task.updateProgress(context.getString(R.string.sync_sending_orders));
            xml = post.postData(Global.S_SUBMIT_ON_HOLD, context, "");
            if (xml.contains("error")) {
                return getTagValue(xml, "error");
            } else {
                inSource = new InputSource(new StringReader(xml));
                xr.setContentHandler(handler);
                xr.parse(inSource);
                data = handler.getEmpData();
                ordersHandler.updateIsSync(data);
                if (data.isEmpty())
                    didSendData = false;
                data.clear();
            }
        }
        return "";
    }

    public static String getTagValue(String xml, String tagName) {
        return xml.split("<" + tagName + ">")[1].split("</" + tagName + ">")[0];
    }

    public static void synchOrdersOnHoldList(Context activity) throws SAXException, IOException {
        try {
            Gson gson = JsonUtils.getInstance();
            GenerateXML xml = new GenerateXML(activity);
            InputStream inputStream = new HttpClient().httpInputStreamRequest(activity.getString(R.string.sync_enablermobile_deviceasxmltrans) +
                    xml.downloadAll("GetOrdersOnHoldList"));
            JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
            List<Order> orders = new ArrayList<>();
            OrdersHandler ordersHandler = new OrdersHandler(activity);
            ordersHandler.emptyTable();
            reader.beginArray();
            int i = 0;
            while (reader.hasNext()) {
                Order order = gson.fromJson(reader, Order.class);
                order.ord_issync = "1";
                order.isOnHold = "1";
                orders.add(order);
                i++;
                if (i == 1000) {
                    ordersHandler.insert(orders);
                    orders.clear();
                    i = 0;
                }
            }
            ordersHandler.insert(orders);
            reader.endArray();
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void synchOrdersOnHoldDetails(Context activity, String ordID) throws SAXException, IOException {
        try {
            HttpClient client = new HttpClient();
            Gson gson = JsonUtils.getInstance();
            GenerateXML xml = new GenerateXML(activity);
            InputStream inputStream = client.httpInputStreamRequest(activity.getString(R.string.sync_enablermobile_deviceasxmltrans) +
                    xml.getOnHold(Global.S_ORDERS_ON_HOLD_DETAILS, ordID));
            JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
            List<OrderProduct> orderProducts = new ArrayList<>();
            OrderProductsHandler orderProductsHandler = new OrderProductsHandler(activity);
            reader.beginArray();
            int i = 0;
            while (reader.hasNext()) {
                OrderProduct product = gson.fromJson(reader, OrderProduct.class);
                orderProducts.add(product);
                i++;
                if (i == 1000) {
                    OrderProductUtils.assignAddonsOrderProduct(orderProducts);
                    orderProductsHandler.insert(orderProducts);
                    orderProducts.clear();
                    i = 0;
                }
            }
            OrderProductUtils.assignAddonsOrderProduct(orderProducts);
            orderProductsHandler.completeProductFields(orderProducts, activity);
            orderProductsHandler.insert(orderProducts);
            reader.endArray();
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /************************************
     * Receive Methods
     ************************************/

    private void synchAddresses() throws SAXException, IOException {
        post.postData(7, context, "Address");
        SAXSynchHandler synchHandler = new SAXSynchHandler(context, Global.S_ADDRESS);
        File tempFile = new File(tempFilePath);
        sp.parse(tempFile, synchHandler);
        tempFile.delete();
    }

    private void synchCategories() throws IOException, SAXException {
        post.postData(7, context, "Categories");
        SAXSynchHandler synchHandler = new SAXSynchHandler(context, Global.S_CATEGORIES);
        File tempFile = new File(tempFilePath);
        sp.parse(tempFile, synchHandler);
        tempFile.delete();
    }

    private void synchCustomers() throws IOException, SAXException {
        post.postData(7, context, "Customers");
        SAXSynchHandler synchHandler = new SAXSynchHandler(context, Global.S_CUSTOMERS);
        File tempFile = new File(tempFilePath);
        sp.parse(tempFile, synchHandler);
        tempFile.delete();
    }

    private void synchEmpInv() throws IOException, SAXException {
        post.postData(7, context, "EmpInv");
        SAXSynchHandler synchHandler = new SAXSynchHandler(context, Global.S_EMPLOYEE_INVOICES);
        File tempFile = new File(tempFilePath);
        sp.parse(tempFile, synchHandler);
        tempFile.delete();
    }

    private void synchProdInv() throws IOException, SAXException {
        post.postData(7, context, "InvProducts");
        SAXSynchHandler synchHandler = new SAXSynchHandler(context, Global.S_PRODUCTS_INVOICES);
        File tempFile = new File(tempFilePath);
        sp.parse(tempFile, synchHandler);
        tempFile.delete();
    }

    private void synchInvoices() throws IOException, SAXException {
        post.postData(7, context, "Invoices");
        SAXSynchHandler synchHandler = new SAXSynchHandler(context, Global.S_INVOICES);
        File tempFile = new File(tempFilePath);
        sp.parse(tempFile, synchHandler);
        tempFile.delete();
    }

    private void synchPaymentMethods() throws IOException, SAXException {
        try {
            Gson gson = JsonUtils.getInstance();
            GenerateXML xml = new GenerateXML(context);
            InputStream inputStream = client.httpInputStreamRequest(context.getString(R.string.sync_enablermobile_deviceasxmltrans) +
                    xml.downloadAll("PayMethods"));
            JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
            List<PaymentMethod> methods = new ArrayList<>();
            PayMethodsHandler payMethodsHandler = new PayMethodsHandler(context);
            payMethodsHandler.emptyTable();
            reader.beginArray();
            int i = 0;
            while (reader.hasNext()) {
                PaymentMethod method = gson.fromJson(reader, PaymentMethod.class);
                methods.add(method);
                i++;
                if (i == 1000) {
                    payMethodsHandler.insert(methods);
                    methods.clear();
                    i = 0;
                }
            }
            payMethodsHandler.insert(methods);
            reader.endArray();
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void synchPriceLevel() throws IOException, SAXException {
        try {
            Gson gson = JsonUtils.getInstance();
            GenerateXML xml = new GenerateXML(context);
            InputStream inputStream = client.httpInputStreamRequest(context.getString(R.string.sync_enablermobile_deviceasxmltrans) +
                    xml.downloadAll("PriceLevel"));
            JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
            List<PriceLevel> priceLevels = new ArrayList<>();
            PriceLevelHandler priceLevelHandler = new PriceLevelHandler();
            priceLevelHandler.emptyTable();
            reader.beginArray();
            int i = 0;
            while (reader.hasNext()) {
                PriceLevel priceLevel = gson.fromJson(reader, PriceLevel.class);
                priceLevels.add(priceLevel);
                i++;
                if (i == 1000) {
                    priceLevelHandler.insert(priceLevels);
                    priceLevels.clear();
                    i = 0;
                }
            }
            priceLevelHandler.insert(priceLevels);
            reader.endArray();
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void synchItemsPriceLevel() throws IOException, SAXException {
        try {
            Gson gson = JsonUtils.getInstance();
            GenerateXML xml = new GenerateXML(context);
            InputStream inputStream = client.httpInputStreamRequest(context.getString(R.string.sync_enablermobile_deviceasxmltrans) +
                    xml.downloadAll("PriceLevelItems"));
            JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
            List<ItemPriceLevel> itemPriceLevels = new ArrayList<>();
            PriceLevelItemsHandler levelItemsHandler = new PriceLevelItemsHandler(context);
            levelItemsHandler.emptyTable();
            reader.beginArray();
            int i = 0;
            while (reader.hasNext()) {
                ItemPriceLevel itemPriceLevel = gson.fromJson(reader, ItemPriceLevel.class);
                itemPriceLevels.add(itemPriceLevel);
                i++;
                if (i == 1000) {
                    levelItemsHandler.insert(itemPriceLevels);
                    itemPriceLevels.clear();
                    i = 0;
                }
            }
            levelItemsHandler.insert(itemPriceLevels);
            reader.endArray();
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void synchPrinters() throws IOException, SAXException {
        client = new HttpClient();
        GenerateXML xml = new GenerateXML(context);
        String jsonRequest = client.httpJsonRequest(context.getString(R.string.sync_enablermobile_deviceasxmltrans) +
                xml.downloadAll("Printers"));
        try {
            DeviceTableDAO.truncate();
            DeviceTableDAO.insert(jsonRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void synchProdCatXref() throws IOException, SAXException {
        post.postData(7, context, "ProdCatXref");
        SAXSynchHandler synchHandler = new SAXSynchHandler(context, Global.S_PRODCATXREF);
        File tempFile = new File(tempFilePath);
        sp.parse(tempFile, synchHandler);
        tempFile.delete();
    }

    private void synchProdChain() throws IOException, SAXException {
        post.postData(7, context, "ProductChainXRef");
        SAXSynchHandler synchHandler = new SAXSynchHandler(context, Global.S_PROD_CHAIN);
        File tempFile = new File(tempFilePath);
        sp.parse(tempFile, synchHandler);
        tempFile.delete();
    }

    private void synchProdAddon() throws IOException, SAXException {
        try {
            Gson gson = JsonUtils.getInstance();
            GenerateXML xml = new GenerateXML(context);
            InputStream inputStream = client.httpInputStreamRequest(context.getString(R.string.sync_enablermobile_deviceasxmltrans) +
                    xml.downloadAll("Product_addons"));
            JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
            List<ProductAddons> addonses = new ArrayList<>();
            ProductAddonsHandler addonsHandler = new ProductAddonsHandler(context);
            addonsHandler.emptyTable();
            reader.beginArray();
            int i = 0;
            while (reader.hasNext()) {
                ProductAddons addons = gson.fromJson(reader, ProductAddons.class);
                addonses.add(addons);
                i++;
                if (i == 1000) {
                    addonsHandler.insert(addonses);
                    addonses.clear();
                    i = 0;
                }
            }
            addonsHandler.insert(addonses);
            reader.endArray();
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void synchProducts() throws IOException, SAXException {
        try {
            ProductsHandler productsHandler = new ProductsHandler(context);
            Gson gson = JsonUtils.getInstance();
            GenerateXML xml = new GenerateXML(context);
            InputStream inputStream = client.httpInputStreamRequest(context.getString(R.string.sync_enablermobile_deviceasxmltrans) +
                    xml.downloadAll("Products"));
            JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
            List<Product> products = new ArrayList<>();
            productsHandler.emptyTable();
            reader.beginArray();
            int i = 0;
            while (reader.hasNext()) {
                Product product = gson.fromJson(reader, Product.class);
                products.add(product);
                i++;
                if (i == 1000) {
                    productsHandler.insert(products);
                    products.clear();
                    i = 0;
                }
            }
            productsHandler.insert(products);
            reader.endArray();
            reader.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void synchOrderAttributes() throws IOException, SAXException {
        try {
            Gson gson = JsonUtils.getInstance();
            GenerateXML xml = new GenerateXML(context);
            InputStream inputStream = client.httpInputStreamRequest(context.getString(R.string.sync_enablermobile_deviceasxmltrans) +
                    xml.downloadAll("OrderAttributes"));
            JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
            List<OrderAttributes> orderAttributes = new ArrayList<>();
            reader.beginArray();
            int i = 0;
            while (reader.hasNext()) {
                OrderAttributes attributes = gson.fromJson(reader, OrderAttributes.class);
                orderAttributes.add(attributes);
                i++;
                if (i == 1000) {
                    OrderAttributesDAO.insert(orderAttributes);
                    orderAttributes.clear();
                    i = 0;
                }
            }
            OrderAttributesDAO.insert(orderAttributes);
            reader.endArray();
            reader.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void postShift(Context context) throws Exception {
        SAXParserPost handler = new SAXParserPost();
        ShiftPeriodsDBHandler dbHandler = new ShiftPeriodsDBHandler(context);
        List<Shift> pendingSyncShifts = ShiftDAO.getPendingSyncShifts();
        if (pendingSyncShifts!=null && !pendingSyncShifts.isEmpty()) {
            xml = post.postData(Global.S_SUBMIT_SHIFT, context, "");
            inSource = new InputSource(new StringReader(xml));
            xr.setContentHandler(handler);
            xr.parse(inSource);
            data = handler.getData();
            dbHandler.updateIsSync(data);
            if (data.isEmpty())
                didSendData = false;
            data.clear();
        }
    }

    public static void postSalesAssociatesConfiguration(Activity activity, List<SalesAssociate> salesAssociates) throws Exception {
        List<DinningLocationConfiguration> configurations = new ArrayList<>();

        HashMap<String, List<SalesAssociate>> locations = SalesAssociateDAO.getSalesAssociatesByLocation();
        for (Map.Entry<String, List<SalesAssociate>> location : locations.entrySet()) {
            DinningLocationConfiguration configuration = new DinningLocationConfiguration();
            configuration.setLocationId(location.getKey());
            configuration.setSalesAssociates(location.getValue());
            configurations.add(configuration);
        }
        AssignEmployee assignEmployee = AssignEmployeeDAO.getAssignEmployee();

        MyPreferences preferences = new MyPreferences(activity);
        StringBuilder url = new StringBuilder(activity.getString(R.string.sync_enablermobile_mesasconfig));
        url.append("/").append(URLEncoder.encode(String.valueOf(assignEmployee.getEmpId()), GenerateXML.UTF_8));
        url.append("/").append(URLEncoder.encode(preferences.getDeviceID(), GenerateXML.UTF_8));
        url.append("/").append(URLEncoder.encode(preferences.getActivKey(), GenerateXML.UTF_8));
        url.append("/").append(URLEncoder.encode(preferences.getBundleVersion(), GenerateXML.UTF_8));

        if (OAuthManager.isExpired(activity)) {
            getOAuthManager(activity);
        }
        OAuthClient authClient = OAuthManager.getOAuthClient(activity);
        Gson gson = JsonUtils.getInstance();
        String json = gson.toJson(configurations);
        oauthclient.HttpClient httpClient = new oauthclient.HttpClient();
        httpClient.post(url.toString(), json, authClient);
    }

    public static void synchSalesAssociateDinnindTablesConfiguration(Context activity) throws IOException, SAXException {
        try {
            oauthclient.HttpClient client = new oauthclient.HttpClient();
            Gson gson = JsonUtils.getInstance();
            if (OAuthManager.isExpired(activity)) {
                getOAuthManager(activity);
            }
            OAuthClient oauthClient = OAuthManager.getOAuthClient(activity);
//            String s = client.getString(activity.getString(R.string.sync_enablermobile_mesasconfig), oauthClient);
            InputStream inputStream = client.get(activity.getString(R.string.sync_enablermobile_mesasconfig), oauthClient);
            JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
            List<DinningLocationConfiguration> configurations = new ArrayList<>();
            reader.beginArray();
            String defaultLocation = AssignEmployeeDAO.getAssignEmployee().getDefaultLocation();
            while (reader.hasNext()) {
                DinningLocationConfiguration configuration = gson.fromJson(reader, DinningLocationConfiguration.class);
                if (configuration.getLocationId().equalsIgnoreCase(defaultLocation)) {
                    configurations.add(configuration);
                }
            }

            reader.endArray();
            reader.close();
            for (DinningLocationConfiguration configuration : configurations) {
                for (SalesAssociate associate : configuration.getSalesAssociates()) {
                    SalesAssociateDAO.clearAllAssignedTable(associate);
                    for (DinningTable table : associate.getAssignedDinningTables()) {
                        DinningTable dinningTable = DinningTableDAO.getById(table.getId());
                        SalesAssociateDAO.addAssignedTable(associate, dinningTable);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void synchProductAliases() throws IOException, SAXException {
        try {
            ProductAliases_DB productAliasesDB = new ProductAliases_DB(context);
            Gson gson = JsonUtils.getInstance();
            GenerateXML xml = new GenerateXML(context);
            InputStream inputStream = client.httpInputStreamRequest(context.getString(R.string.sync_enablermobile_deviceasxmltrans) +
                    xml.downloadAll("ProductAliases"));
            JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
            List<ProductAlias> productAliases = new ArrayList<>();
            productAliasesDB.emptyTable();
            reader.beginArray();
            int i = 0;
            while (reader.hasNext()) {
                ProductAlias alias = gson.fromJson(reader, Product.class);
                productAliases.add(alias);
                i++;
                if (i == 1000) {
                    productAliasesDB.insert(productAliases);
                    productAliases.clear();
                    i = 0;
                }
            }
            productAliasesDB.insert(productAliases);
            reader.endArray();
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void synchProductImages() throws IOException, SAXException {
        post.postData(7, context, "Products_Images");
        SAXSynchHandler synchHandler = new SAXSynchHandler(context, Global.S_PROD_IMG);
        File tempFile = new File(tempFilePath);
        sp.parse(tempFile, synchHandler);
        tempFile.delete();
    }

    private void synchSalesTaxCode() throws IOException, SAXException {
        post.postData(7, context, "SalesTaxCodes");
        SAXSynchHandler synchHandler = new SAXSynchHandler(context, Global.S_SALES_TAX_CODE);
        File tempFile = new File(tempFilePath);
        sp.parse(tempFile, synchHandler);
        tempFile.delete();
    }

    private void synchShippingMethods() throws IOException, SAXException {
        post.postData(7, context, "ShipMethod");
        SAXSynchHandler synchHandler = new SAXSynchHandler(context, Global.S_SHIP_METHODS);
        File tempFile = new File(tempFilePath);
        sp.parse(tempFile, synchHandler);
        tempFile.delete();
    }

    private void synchTaxes() throws IOException, SAXException {
        post.postData(7, context, "Taxes");
        SAXSynchHandler synchHandler = new SAXSynchHandler(context, Global.S_TAXES);
        File tempFile = new File(tempFilePath);
        sp.parse(tempFile, synchHandler);
        tempFile.delete();
    }

    private void synchTaxGroup() throws IOException, SAXException {
        post.postData(7, context, "Taxes_Group");
        SAXSynchHandler synchHandler = new SAXSynchHandler(context, Global.S_TAX_GROUP);
        File tempFile = new File(tempFilePath);
        sp.parse(tempFile, synchHandler);
        tempFile.delete();
    }

    private void synchTerms() throws IOException, SAXException {
        post.postData(7, context, "Terms");
        SAXSynchHandler synchHandler = new SAXSynchHandler(context, Global.S_TERMS);
        File tempFile = new File(tempFilePath);
        sp.parse(tempFile, synchHandler);
        tempFile.delete();
    }

    private void synchMemoText() throws IOException, SAXException {
        post.postData(7, context, "memotext");
        SAXSynchHandler synchHandler = new SAXSynchHandler(context, Global.S_MEMO_TXT);
        File tempFile = new File(tempFilePath);
        sp.parse(tempFile, synchHandler);
        tempFile.delete();
    }

    private void synchGetTemplates() throws IOException, SAXException {
        post.postData(7, context, "Templates");
        SAXSynchHandler synchHandler = new SAXSynchHandler(context, Global.S_TEMPLATES);
        File tempFile = new File(tempFilePath);
        sp.parse(tempFile, synchHandler);
        tempFile.delete();
    }

    private void synchDownloadCustomerInventory() throws IOException, SAXException {
        post.postData(7, context, "GetCustomerInventory");
        SAXSynchHandler synchHandler = new SAXSynchHandler(context, Global.S_CUSTOMER_INVENTORY);
        File tempFile = new File(tempFilePath);
        sp.parse(tempFile, synchHandler);
        tempFile.delete();
    }

    private void synchDownloadConsignmentTransaction() throws IOException, SAXException {
        post.postData(7, context, "GetConsignmentTransaction");
        SAXSynchHandler synchHandler = new SAXSynchHandler(context, Global.S_CONSIGNMENT_TRANSACTION);
        File tempFile = new File(tempFilePath);
        sp.parse(tempFile, synchHandler);
        tempFile.delete();
    }

    private void synchIvuLottoDrawDates() throws IOException, SAXException {
        post.postData(7, context, "ivudrawdates");
        SAXSynchHandler synchHandler = new SAXSynchHandler(context, Global.S_IVU_LOTTO);
        File tempFile = new File(tempFilePath);
        sp.parse(tempFile, synchHandler);
        tempFile.delete();
    }

    private void synchAccountLogo() {
        GenerateXML generator = new GenerateXML(context);
        MyPreferences myPref = new MyPreferences(context);
        URL url;
        InputStream is;
        try {
            url = new URL(generator.getAccountLogo());
            is = url.openStream();
            Bitmap bmp = BitmapFactory.decodeStream(is);

            int width = bmp.getWidth();
            int height = bmp.getHeight();
            float scale = 0;
            if (width > 300) {
                scale = (float) 300 / width;
                width = (int) (width * scale);
                height = (int) (height * scale);
            }
            Bitmap newBitmap = Bitmap.createScaledBitmap(bmp, width, height, false);
            String externalPath = context.getApplicationContext().getFilesDir().getAbsolutePath() + "/";
            myPref.setAccountLogoPath(externalPath + "logo.png");
            File file = new File(externalPath, "logo.png");
            OutputStream os = new FileOutputStream(file);
            newBitmap.compress(CompressFormat.PNG, 0, os);
            is.close();
            os.close();
            bmp.recycle();
            newBitmap.recycle();

        } catch (MalformedURLException e) {
        } catch (IOException e) {
        } catch (Exception e) {
        }
    }

    private void synchDownloadProductsAttr() throws SAXException, IOException {
        post.postData(7, context, "ProductsAttr");
        SAXSynchHandler synchHandler = new SAXSynchHandler(context, Global.S_PRODUCTS_ATTRIBUTES);
        File tempFile = new File(tempFilePath);
        sp.parse(tempFile, synchHandler);
        tempFile.delete();
    }

    private void synchDownloadClerks() throws SAXException, IOException {
        post.postData(7, context, "Clerks");
        SAXSynchHandler synchHandler = new SAXSynchHandler(context, Global.S_CLERKS);
        File tempFile = new File(tempFilePath);
        sp.parse(tempFile, synchHandler);
        tempFile.delete();
    }

    private String readTempFile(File file) {
        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        } catch (IOException e) {
        }
        return text.toString();
    }

    private void synchDownloadDinnerTable() throws SAXException, IOException {
        client = new HttpClient();
        GenerateXML xml = new GenerateXML(context);
        String jsonRequest = client.httpJsonRequest(context.getString(R.string.sync_enablermobile_deviceasxmltrans) +
                xml.getDinnerTables());
        try {
            DinningTableDAO.truncate();
            DinningTableDAO.insert(jsonRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void synchDownloadMixMatch() throws SAXException, IOException {
        try {
            client = new HttpClient();
            GenerateXML xml = new GenerateXML(context);
            InputStream inputStream = client.httpInputStreamRequest(context.getString(R.string.sync_enablermobile_deviceasxmltrans) +
                    xml.getMixMatch());
            JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
            List<MixMatch> mixMatches = new ArrayList<MixMatch>();
            MixMatchDAO.truncate();
            reader.beginArray();
            int i = 0;
            while (reader.hasNext()) {
                MixMatch mixMatch = gson.fromJson(reader, MixMatch.class);
                mixMatches.add(mixMatch);
                i++;
                if (i == 1000) {
                    MixMatchDAO.insert(mixMatches);
                    mixMatches.clear();
                    i = 0;
                }
            }
            MixMatchDAO.insert(mixMatches);
            reader.endArray();
            reader.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void synchDownloadSalesAssociate() throws SAXException, IOException {
        try {
            client = new HttpClient();
            GenerateXML xml = new GenerateXML(context);
            String jsonRequest = client.httpJsonRequest(context.getString(R.string.sync_enablermobile_deviceasxmltrans) +
                    xml.getSalesAssociate());
            try {
                SalesAssociateDAO.truncate();
                SalesAssociateDAO.insert(jsonRequest);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void synchDownloadTermsAndConditions() throws SAXException, IOException {
        post.postData(7, context, "TermsAndConditions");
        SAXSynchHandler synchHandler = new SAXSynchHandler(context, Global.S_TERMS_AND_CONDITIONS);
        File tempFile = new File(tempFilePath);
        sp.parse(tempFile, synchHandler);
        tempFile.delete();
    }

    private void synchUoM() throws IOException, SAXException {
        try {
            client = new HttpClient();
            GenerateXML xml = new GenerateXML(context);
            String jsonRequest = client.httpJsonRequest(context.getString(R.string.sync_enablermobile_deviceasxmltrans) +
                    xml.downloadAll("UoM"));
            try {
                UomDAO.truncate();
                UomDAO.insert(jsonRequest);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void synchGetOrdProdAttr() throws IOException, SAXException {
        try {
            client = new HttpClient();
            GenerateXML xml = new GenerateXML(context);
            String jsonRequest = client.httpJsonRequest(context.getString(R.string.sync_enablermobile_deviceasxmltrans) +
                    xml.downloadAll("GetOrderProductsAttr"));
            try {
                OrderProductAttributeDAO.truncate();
                OrderProductAttributeDAO.insert(jsonRequest);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String _server_time = "";

    private void synchGetServerTime() throws IOException, SAXException {
        xml = post.postData(Global.S_GET_SERVER_TIME, context, null);
        SAXPostHandler handler = new SAXPostHandler();
        inSource = new InputSource(new StringReader(xml));
        xr.setContentHandler(handler);
        xr.parse(inSource);
        _server_time = handler.getData("serverTime", 0);
    }

    private void synchUpdateSyncTime() throws IOException, SAXException {
        post.postData(Global.S_UPDATE_SYNC_TIME, context, _server_time);
    }

    private void synchEmployeeData() throws IOException, SAXException {
        try {
            String xml = post.postData(4, context, "");
            Gson gson = JsonUtils.getInstance();
            Type listType = new com.google.gson.reflect.TypeToken<List<AssignEmployee>>() {
            }.getType();
            List<AssignEmployee> assignEmployees = gson.fromJson(xml, listType);
            AssignEmployeeDAO.insertAssignEmployee(assignEmployees);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void synchDownloadLastPayID() {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        SaxLoginHandler handler = new SaxLoginHandler();
        try {
            String xml = post.postData(6, context, "");
            InputSource inSource = new InputSource(new StringReader(xml));
            SAXParser sp = spf.newSAXParser();
            XMLReader xr = sp.getXMLReader();
            xr.setContentHandler(handler);
            xr.parse(inSource);
            if (!handler.getData().isEmpty()) {
                MyPreferences myPref = new MyPreferences(context);
                myPref.setLastPayID(handler.getData());
            }

        } catch (Exception e) {
        }
    }

    private void synchDeviceDefaultValues() throws IOException, SAXException {
        post.postData(7, context, "DeviceDefaultValues");
        SAXSynchHandler synchHandler = new SAXSynchHandler(context, Global.S_DEVICE_DEFAULT_VAL);
        File tempFile = new File(tempFilePath);
        sp.parse(tempFile, synchHandler);
        tempFile.delete();

    }

    private void synchVolumePrices() throws IOException, SAXException {
        post.postData(7, context, "VolumePrices");
        SAXSynchHandler synchHandler = new SAXSynchHandler(context, Global.S_VOLUME_PRICES);
        File tempFile = new File(tempFilePath);
        sp.parse(tempFile, synchHandler);
        tempFile.delete();
    }

    private void synchLocations() throws IOException, SAXException {
        post.postData(7, context, "GetLocations");
        SAXSynchHandler synchHandler = new SAXSynchHandler(context, Global.S_LOCATIONS);
        File tempFile = new File(tempFilePath);
        sp.parse(tempFile, synchHandler);
        tempFile.delete();
    }

    private void synchLocationsInventory() throws IOException, SAXException {
        post.postData(7, context, "GetLocationsInventory");
        SAXSynchHandler synchHandler = new SAXSynchHandler(context, Global.S_LOCATIONS_INVENTORY);
        File tempFile = new File(tempFilePath);
        sp.parse(tempFile, synchHandler);
        tempFile.delete();
    }

}