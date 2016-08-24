package com.android.emobilepos.settings;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.android.database.OrdersHandler;
import com.android.database.PaymentsHandler;
import com.android.database.StoredPayments_DB;
import com.android.emobilepos.R;
import com.android.emobilepos.models.Payment;
import com.android.emobilepos.models.storedAndForward.StoreAndForward;
import com.android.emobilepos.storedforward.BoloroPayment;
import com.android.payments.EMSPayGate_Default;
import com.android.saxhandler.SAXProcessCardPayHandler;
import com.android.support.GenerateNewID;
import com.android.support.GenerateNewID.IdType;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.NetworkUtils;
import com.android.support.Post;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;

public class ViewStoreForwardTrans_FA extends BaseFragmentActivityActionBar implements OnItemClickListener, OnClickListener {
    private Activity activity;
    private Global global;
    private boolean hasBeenCreated = false;
    //    private Cursor myCursor;
    //private SQLiteDatabase db;
    private StoredPayments_DB dbStoredPay;
    private CustomCursorAdapter adapter;
    private RecyclerView listView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_store_forward_trans_layout);
        activity = this;
        global = (Global) getApplication();

        Button btnProcessAll = (Button) findViewById(R.id.btnProcessAll);
        btnProcessAll.setOnClickListener(this);

        //DBManager dbManager = new DBManager(this);
        //db = dbManager.openWritableDB();
        dbStoredPay = new StoredPayments_DB(this);
//        myCursor = dbStoredPay.getStoredPayments();
        listView = (RecyclerView) findViewById(R.id.listView);
        adapter = new CustomCursorAdapter(Realm.getDefaultInstance().where(StoreAndForward.class).findAll());
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        listView.setLayoutManager(mLayoutManager);
        listView.setItemAnimator(new DefaultItemAnimator());
        listView.setAdapter(adapter);
//        listView.setOnItemClickListener(this);

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
    public void onDestroy() {
        super.onDestroy();
//        myCursor.close();
        //db.close();
    }


    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnProcessAll:
                new processLivePaymentAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                break;
        }
    }


    private boolean livePaymentRunning = false;
    private String _charge_xml = "";
    private String _verify_payment_xml = "";
    private ProgressDialog myProgressDialog;

    private class processLivePaymentAsync extends AsyncTask<Void, Void, Void> {

        private HashMap<String, String> parsedMap = new HashMap<String, String>();
        private int _count_decline = 0, _count_conn_error = 0, _count_merch_account = 0;


        private void checkPaymentStatus(StoreAndForward storeAndForward, String verify_payment_xml, String charge_xml) throws SAXException, ParserConfigurationException, IOException {
            OrdersHandler dbOrdHandler = new OrdersHandler(activity);
            Post httpClient = new Post();
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXProcessCardPayHandler handler = new SAXProcessCardPayHandler(activity);
            String xml = httpClient.postData(13, activity, verify_payment_xml);

            if (xml.equals(Global.TIME_OUT) || xml.equals(Global.NOT_VALID_URL) || xml.isEmpty()) {
                //do nothing
                _count_conn_error++;
            } else {
                InputSource inSource = new InputSource(new StringReader(xml));

                SAXParser sp = spf.newSAXParser();
                XMLReader xr = sp.getXMLReader();
                xr.setContentHandler(handler);
                xr.parse(inSource);
                parsedMap = handler.getData();


                if (parsedMap != null && parsedMap.size() > 0) {
                    String _job_id = storeAndForward.getPayment().getJob_id();
                    String _pay_uuid = storeAndForward.getPayment().getPay_uuid();//myCursor.getString(myCursor.getColumnIndex("pay_uuid"));

                    if (parsedMap.get("epayStatusCode").equals("APPROVED")) {
                        //Create Payment and delete from StoredPayment
                        saveApprovedPayment(storeAndForward, parsedMap);
                        //Remove as pending stored & forward if no more payments are pending to be processed.
                        if (dbStoredPay.getCountPendingStoredPayments(_job_id) <= 0)
                            dbOrdHandler.updateOrderStoredFwd(_job_id, "0");
                    } else if (parsedMap.get("epayStatusCode").equals("DECLINE")) {
                        if (parsedMap.get("statusCode").equals("102")) {
                            _count_merch_account++;
                            dbStoredPay.updateStoredPaymentForRetry(_pay_uuid);
                        } else {
                            //remove from StoredPayment and change order to Invoice
                            StringBuilder sb = new StringBuilder();
                            sb.append(dbOrdHandler.getColumnValue("ord_comment", _job_id)).append("  ");
                            sb.append("(Card Holder: ").append(storeAndForward.getPayment().getPay_name());//myCursor.getString(myCursor.getColumnIndex("pay_name")));
                            sb.append("; Last 4: ").append(storeAndForward.getPayment().getCcnum_last4());//myCursor.getString(myCursor.getColumnIndex("ccnum_last4")));
                            sb.append("; Exp date: ").append(storeAndForward.getPayment().getPay_expmonth());//myCursor.getString(myCursor.getColumnIndex("pay_expmonth")));
                            sb.append("/").append(storeAndForward.getPayment().getPay_expyear());//myCursor.getString(myCursor.getColumnIndex("pay_expyear")));
                            sb.append("; Status Msg: ").append(parsedMap.get("statusMessage"));
                            sb.append("; Status Code: ").append(parsedMap.get("statusCode"));
                            sb.append("; TransID: ").append(parsedMap.get("CreditCardTransID"));
                            sb.append("; Auth Code: ").append(parsedMap.get("AuthorizationCode")).append(")");

                            dbStoredPay.deleteStoredPaymentRow(_pay_uuid);
                            if (dbOrdHandler.getColumnValue("ord_type", _job_id).equals(Global.OrderType.SALES_RECEIPT.getCodeString()))
                                dbOrdHandler.updateOrderTypeToInvoice(_job_id);
                            dbOrdHandler.updateOrderComment(_job_id, sb.toString());

                            //Remove as pending stored & forward if no more payments are pending to be processed.
                            if (dbStoredPay.getCountPendingStoredPayments(_job_id) <= 0)
                                dbOrdHandler.updateOrderStoredFwd(_job_id, "0");

                            _count_decline++;
                        }
                    } else {
                        //Payment doesn't exist try to process the payment
                        processPayment(storeAndForward, charge_xml);
                    }
                } else {
                    //mark StoredPayment for retry
                    _count_conn_error++;
                }
            }
        }

        private void processPayment(StoreAndForward storeAndForward, String charge_xml) throws ParserConfigurationException, SAXException, IOException {
            OrdersHandler dbOrdHandler = new OrdersHandler(activity);
            Post httpClient = new Post();
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXProcessCardPayHandler handler = new SAXProcessCardPayHandler(activity);
            String xml = httpClient.postData(13, activity, charge_xml);
            if (xml.equals(Global.TIME_OUT) || xml.equals(Global.NOT_VALID_URL) || xml.isEmpty()) {
                //mark StoredPayment for retry
                updateStoreForwardPaymentToRetry(storeAndForward);
//                dbStoredPay.updateStoredPaymentForRetry(myCursor.getString(myCursor.getColumnIndex("pay_uuid")));
                _count_conn_error++;
            } else {
                InputSource inSource = new InputSource(new StringReader(xml));

                SAXParser sp = spf.newSAXParser();
                XMLReader xr = sp.getXMLReader();
                xr.setContentHandler(handler);
                xr.parse(inSource);
                parsedMap = handler.getData();
                Realm realm = Realm.getDefaultInstance();
                if (parsedMap != null && parsedMap.size() > 0) {
//                    String _job_id = //myCursor.getString(myCursor.getColumnIndex("job_id"));
//                    String _pay_uuid = myCursor.getString(myCursor.getColumnIndex("pay_uuid"));

                    if (parsedMap.get("epayStatusCode").equals("APPROVED")) {
                        //Create Payment and delete from StoredPayment
                        saveApprovedPayment(storeAndForward, parsedMap);
                        //Remove as pending stored & forward if no more payments are pending to be processed.
                        realm.beginTransaction();
                        storeAndForward.deleteFromRealm();
                        realm.commitTransaction();

//                        if (dbStoredPay.getCountPendingStoredPayments(_job_id) <= 0)
//                            dbOrdHandler.updateOrderStoredFwd(_job_id, "0");
                    } else if (parsedMap.get("epayStatusCode").equals("DECLINE")) {
                        if (parsedMap.get("statusCode").equals("102")) {
                            _count_merch_account++;
                            updateStoreForwardPaymentToRetry(storeAndForward);
//                            dbStoredPay.updateStoredPaymentForRetry(_pay_uuid);
                        } else {
                            //remove from StoredPayment and change order to Invoice
                            StringBuilder sb = new StringBuilder();
                            sb.append(dbOrdHandler.getColumnValue("ord_comment", storeAndForward.getPayment().getJob_id())).append("  ");
                            sb.append("(Card Holder: ").append(storeAndForward.getPayment().getPay_name());
                            sb.append("; Last 4: ").append(storeAndForward.getPayment().getCcnum_last4());
                            sb.append("; Exp date: ").append(storeAndForward.getPayment().getPay_expmonth());
                            sb.append("/").append(storeAndForward.getPayment().getPay_expyear());
                            sb.append("; Status Msg: ").append(parsedMap.get("statusMessage"));
                            sb.append("; Status Code: ").append(parsedMap.get("statusCode"));
                            sb.append("; TransID: ").append(parsedMap.get("CreditCardTransID"));
                            sb.append("; Auth Code: ").append(parsedMap.get("AuthorizationCode")).append(")");

                            realm.beginTransaction();
                            StoreAndForward norealmStrFwd = realm.copyFromRealm(storeAndForward);
                            storeAndForward.deleteFromRealm();
                            realm.commitTransaction();
//                            dbStoredPay.deleteStoredPaymentRow(_pay_uuid);
                            if (dbOrdHandler.getColumnValue("ord_type", norealmStrFwd.getPayment().getJob_id())
                                    .equals(Global.OrderType.SALES_RECEIPT.getCodeString()))
                                dbOrdHandler.updateOrderTypeToInvoice(norealmStrFwd.getPayment().getJob_id());
                            dbOrdHandler.updateOrderComment(norealmStrFwd.getPayment().getJob_id(), sb.toString());

                            //Remove as pending stored & forward if no more payments are pending to be processed.
                            if (dbStoredPay.getCountPendingStoredPayments(norealmStrFwd.getPayment().getJob_id()) <= 0)
                                dbOrdHandler.updateOrderStoredFwd(norealmStrFwd.getPayment().getJob_id(), "0");

                            _count_decline++;
                        }
                    } else {
                        //mark StoredPayment for retry
                        updateStoreForwardPaymentToRetry(storeAndForward);
//                        dbStoredPay.updateStoredPaymentForRetry(_pay_uuid);
                    }
                } else {
                    //mark StoredPayment for retry
                    updateStoreForwardPaymentToRetry(storeAndForward);
//                    dbStoredPay.updateStoredPaymentForRetry(myCursor.getString(myCursor.getColumnIndex("pay_uuid")));
                    _count_conn_error++;
                }
            }
        }

        @Override
        protected void onPreExecute() {
            myProgressDialog = new ProgressDialog(activity);
            myProgressDialog.setMessage("Please wait...");
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.setCancelable(false);
            myProgressDialog.show();

            _count_merch_account = 0;
            _count_conn_error = 0;
            _count_decline = 0;

        }

        @Override
        protected Void doInBackground(Void... params) {
            RealmResults<StoreAndForward> storeAndForwards = Realm.getDefaultInstance().where(StoreAndForward.class).findAll();
            for (StoreAndForward storeAndForward : storeAndForwards) {
                if (NetworkUtils.isConnectedToInternet(activity) && !livePaymentRunning) {
                    livePaymentRunning = true;
                    _charge_xml = storeAndForward.getPaymentXml();//myCursor.getString(i_payment_xml);
                    _verify_payment_xml = _charge_xml.replaceAll("<action>.*?</action>", "<action>" + EMSPayGate_Default.getPaymentAction("CheckTransactionStatus") + "</action>");

                    try {

                        if (storeAndForward.isRetry()) {
                            switch (storeAndForward.getPaymentType()) {
                                case BOLORO:
                                    BoloroPayment.executeNFCCheckout(activity, storeAndForward.getPaymentXml(), storeAndForward.getPayment());
                                    break;
                                case CREDIT_CARD:
                                    break;
                            }
                            checkPaymentStatus(storeAndForward, _verify_payment_xml, _charge_xml);
                        } else {
                            switch (storeAndForward.getPaymentType()) {
                                case BOLORO:
                                    BoloroPayment.executeNFCCheckout(activity, storeAndForward.getPaymentXml(), storeAndForward.getPayment());
                                    break;
                                case CREDIT_CARD:
                                    break;
                            }
                            processPayment(storeAndForward, _charge_xml);
                        }

                    } catch (ParserConfigurationException e) {
                        e.printStackTrace();
                    } catch (SAXException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        updateStoreForwardPaymentToRetry(storeAndForward);
//                            dbStoredPay.updateStoredPaymentForRetry(myCursor.getString(myCursor.getColumnIndex("pay_uuid")));
                    }

                    livePaymentRunning = false;
                } else {
                    _count_conn_error++;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            myProgressDialog.dismiss();

            //refresh the list view;
            //adapter.notifyDataSetChanged();
//            myCursor = dbStoredPay.getStoredPayments();
            adapter.notifyDataSetChanged();
//            adapter = new CustomCursorAdapter(Realm.getDefaultInstance().where(StoreAndForward.class).findAll());
//            listView.setAdapter(adapter);
            StringBuilder sb = new StringBuilder();
            if (_count_conn_error > 0) {
                sb.append("\t -").append("Connection Error (").append(Integer.toString(_count_conn_error)).append("): ");
                sb.append(getString(R.string.dlog_msg_please_try_again)).append("\n");
            }
            if (_count_merch_account > 0) {
                sb.append("\t -").append("Merchant Account (").append(Integer.toString(_count_merch_account)).append("): ");
                sb.append(getString(R.string.dlog_msg_contact_support)).append("\n");
            }
            if (_count_decline > 0) {
                sb.append("\t -").append("Decline (").append(Integer.toString(_count_decline)).append("): ");
                sb.append(getString(R.string.dlog_msg_orders_changed_invoice)).append("\n");
            }

            if (!sb.toString().isEmpty())
                Global.showPrompt(activity, R.string.dlog_title_transaction_failed_to_process, sb.toString());
        }
    }

    private void updateStoreForwardPaymentToRetry(StoreAndForward storeAndForward) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        storeAndForward.setRetry(true);
        realm.commitTransaction();
    }


    private void saveApprovedPayment(StoreAndForward storeAndForward, HashMap<String, String> parsedMap) {
        Realm realm = Realm.getDefaultInstance();
//        Payment newPayment = realm.copyFromRealm(storeAndForward.getPayment());

        GenerateNewID generator = new GenerateNewID(this);
        MyPreferences myPref = new MyPreferences(this);

//        newPayment.emp_id = myCursor.getString(myCursor.getColumnIndex("emp_id"));
//        newPayment.job_id = myCursor.getString(myCursor.getColumnIndex("job_id"));
//        newPayment.inv_id = myCursor.getString(myCursor.getColumnIndex("inv_id"));
//        newPayment.clerk_id = myCursor.getString(myCursor.getColumnIndex("clerk_id"));
//        newPayment.cust_id = myCursor.getString(myCursor.getColumnIndex("cust_id"));
//        newPayment.custidkey = myCursor.getString(myCursor.getColumnIndex("custidkey"));
//        newPayment.ref_num = myCursor.getString(myCursor.getColumnIndex("ref_num"));
//        newPayment.paymethod_id = myCursor.getString(myCursor.getColumnIndex("paymethod_id"));
//        newPayment.pay_dueamount = myCursor.getString(myCursor.getColumnIndex("pay_dueamount"));
//        newPayment.pay_amount = myCursor.getString(myCursor.getColumnIndex("pay_amount"));
//        newPayment.pay_name = myCursor.getString(myCursor.getColumnIndex("pay_name"));
//        newPayment.pay_phone = myCursor.getString(myCursor.getColumnIndex("pay_phone"));
//        newPayment.pay_email = myCursor.getString(myCursor.getColumnIndex("pay_email"));
//        newPayment.pay_ccnum = myCursor.getString(myCursor.getColumnIndex("pay_ccnum"));
//        newPayment.ccnum_last4 = myCursor.getString(myCursor.getColumnIndex("ccnum_last4"));
//        newPayment.pay_expmonth = myCursor.getString(myCursor.getColumnIndex("pay_expmonth"));
//        newPayment.pay_expyear = myCursor.getString(myCursor.getColumnIndex("pay_expyear"));
//        newPayment.pay_poscode = myCursor.getString(myCursor.getColumnIndex("pay_poscode"));
//        newPayment.pay_seccode = myCursor.getString(myCursor.getColumnIndex("pay_seccode"));
//        newPayment.pay_tip = myCursor.getString(myCursor.getColumnIndex("pay_tip"));
//        newPayment.pay_latitude = myCursor.getString(myCursor.getColumnIndex("pay_latitude"));
//        newPayment.pay_longitude = myCursor.getString(myCursor.getColumnIndex("pay_longitude"));
//        newPayment.card_type = myCursor.getString(myCursor.getColumnIndex("card_type"));
//        if (Global.isIvuLoto) {
//            newPayment.IvuLottoNumber = myCursor.getString(myCursor.getColumnIndex("IvuLottoNumber"));
//            newPayment.IvuLottoDrawDate = myCursor.getString(myCursor.getColumnIndex("IvuLottoDrawDate"));
//            newPayment.IvuLottoQR = myCursor.getString(myCursor.getColumnIndex("IvuLottoQR"));
//            newPayment.Tax1_amount = myCursor.getString(myCursor.getColumnIndex("Tax1_amount"));
//            newPayment.Tax1_name = myCursor.getString(myCursor.getColumnIndex("Tax1_name"));
//            newPayment.Tax2_amount = myCursor.getString(myCursor.getColumnIndex("Tax2_amount"));
//            newPayment.Tax2_name = myCursor.getString(myCursor.getColumnIndex("Tax2_name"));
//        }
//        newPayment.is_refund = myCursor.getString(myCursor.getColumnIndex("is_refund"));
//        newPayment.pay_type = myCursor.getString(myCursor.getColumnIndex("pay_type"));
//        newPayment.pay_transid = myCursor.getString(myCursor.getColumnIndex("pay_transid"));
//        newPayment.authcode = myCursor.getString(myCursor.getColumnIndex("authcode"));
//        newPayment.pay_resultcode = parsedMap.get("pay_resultcode");
//        newPayment.pay_resultmessage = parsedMap.get("pay_resultmessage");
//        newPayment.pay_transid = parsedMap.get("CreditCardTransID");
//        newPayment.authcode = parsedMap.get("AuthorizationCode");

        realm.beginTransaction();
        storeAndForward.getPayment().setPay_id(generator.getNextID(IdType.PAYMENT_ID));
        storeAndForward.getPayment().setProcessed("9");//newPayment.processed = "9";
        PaymentsHandler payHandler = new PaymentsHandler(this);
        payHandler.insert(storeAndForward.getPayment());
        storeAndForward.deleteFromRealm();
        realm.commitTransaction();
//        dbStoredPay.deleteStoredPaymentRow(myCursor.getString(myCursor.getColumnIndex("pay_uuid")));
    }


    private class CustomCursorAdapter extends RecyclerView.Adapter<CustomCursorAdapter.ViewHolder> {
        private List<StoreAndForward> storeAndForwards;

        public CustomCursorAdapter(List<StoreAndForward> storeAndForwards) {
            this.storeAndForwards = storeAndForwards;
        }

//        public CustomCursorAdapter(@NonNull Context context, @Nullable OrderedRealmCollection data) {
//            super(context, data);
//            inflater = LayoutInflater.from(context);
//        }

//        public CustomCursorAdapter(Context context, RealmResults realmResults, boolean automaticUpdate) {
//            super(context, realmResults, automaticUpdate);
//            inflater = LayoutInflater.from(context);
//        }


//        @Override
//        public void bindView(View view, Context context, Cursor c) {
//            myHolder = (ViewHolder) view.getTag();
//            myHolder.title.setText(c.getString(myHolder.i_card_type) + "  (" + Global.formatDoubleStrToCurrency(c.getString(myHolder.i_pay_amount)) + ")");
//            myHolder.subtitle.setText(c.getString(myHolder.i_pay_name));
//        }
//
//        @Override
//        public View newView(Context context, Cursor cursor, ViewGroup parent) {
//            View view = inflater.inflate(R.layout.view_store_forward_layout, parent, false);
//            ViewHolder holder = new ViewHolder();
//            holder.title = (TextView) view.findViewById(R.id.tvTitle);
//            holder.subtitle = (TextView) view.findViewById(R.id.tvSubtitle);
//
//            holder.i_card_type = cursor.getColumnIndex("card_type");
//            holder.i_pay_amount = cursor.getColumnIndex("pay_amount");
//            holder.i_pay_name = cursor.getColumnIndex("pay_name");
//
//            view.setTag(holder);
//
//            return view;
//        }

//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//            ViewHolder holder;
//            if (convertView == null) {
//                holder = new ViewHolder();
//                convertView = inflater.inflate(R.layout.view_store_forward_layout, parent, false);
//                holder.title = (TextView) convertView.findViewById(R.id.tvTitle);
//                holder.subtitle = (TextView) convertView.findViewById(R.id.tvSubtitle);
////                holder.i_card_type = storeAndForward.getPayment().getCard_type();//cursor.getColumnIndex("card_type");
////                holder.i_pay_amount = cursor.getColumnIndex("pay_amount");
////                holder.i_pay_name = cursor.getColumnIndex("pay_name");
//
//                convertView.setTag(holder);
//            } else {
//                holder = (ViewHolder) convertView.getTag();
//            }
//            StoreAndForward storeAndForward = (StoreAndForward) adapterData.get(position);
//            Payment p = storeAndForward.getPayment();
//            myHolder.title.setText(p.getCard_type() + "  (" + Global.formatDoubleStrToCurrency(p.getPay_amount()) + ")");
//            myHolder.subtitle.setText(p.getPay_name());
//            return null;
//        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.view_store_forward_layout, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            StoreAndForward storeAndForward = storeAndForwards.get(position);
            Payment p = storeAndForward.getPayment();
            String cardName = TextUtils.isEmpty(p.getCard_type()) ? getString(R.string.card_credit_card) : p.getCard_type();
            holder.title.setText(cardName +
                    "  (" +
                    Global.formatDoubleStrToCurrency(p.getPay_amount()) +
                    ")");
            holder.subtitle.setText(p.getPay_name());
        }

        @Override
        public int getItemCount() {
            return storeAndForwards.size();
        }


        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView title, subtitle;

            public ViewHolder(View v) {
                super(v);
                title = (TextView) v.findViewById(R.id.tvTitle);
                subtitle = (TextView) v.findViewById(R.id.tvSubtitle);
            }
        }
    }
}
