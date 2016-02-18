package com.android.emobilepos.consignment;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.android.database.ConsignmentSignaturesDBHandler;
import com.android.database.ConsignmentTransactionHandler;
import com.android.database.CustomerInventoryHandler;
import com.android.database.OrderProductsHandler;
import com.android.database.OrderTaxes_DB;
import com.android.database.OrdersHandler;
import com.android.database.ProductsHandler;
import com.android.database.TaxesHandler;
import com.android.emobilepos.DrawReceiptActivity;
import com.android.emobilepos.R;
import com.android.emobilepos.models.DataTaxes;
import com.android.emobilepos.models.Order;
import com.android.emobilepos.models.OrderProduct;
import com.android.emobilepos.payment.SelectPayMethod_FA;
import com.android.support.ConsignmentTransaction;
import com.android.support.GenerateNewID;
import com.android.support.GenerateNewID.IdType;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.TaxesCalculator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ConsignmentVisit_FR extends Fragment implements OnClickListener {
    private CustomAdapter_LV myAdapter;
    private ProgressDialog myProgressDialog;
    private CustomerInventoryHandler custInventoryHandler;
    private MyPreferences myPref;
    private Global global;
    private Activity activity;

    private List<ConsignmentTransaction> consTransactionList = new ArrayList<ConsignmentTransaction>();
    private OrdersHandler ordersHandler;
    private OrderProductsHandler orderProductsHandler;
    private boolean ifInvoice = false;
    private double ordTotal = 0;
    private ConsignmentTransactionHandler consTransDBHandler;
    private HashMap<String, String> signatureMap = new HashMap<String, String>();
    private String encodedImage = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.consign_fragment_layout, container, false);
        activity = getActivity();
        ListView myListview = (ListView) view.findViewById(R.id.consignSummaryListView);
        myAdapter = new CustomAdapter_LV(activity);

        custInventoryHandler = new CustomerInventoryHandler(activity);
        myPref = new MyPreferences(activity);

        global = (Global) getActivity().getApplication();
        myListview.setAdapter(myAdapter);


        Button process = (Button) view.findViewById(R.id.saveConsignButton);
        process.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.saveConsignButton:
                new processAsync().execute();
                break;
        }
    }

    public void notifyListViewChange() {
        myAdapter.notifyDataSetChanged();
    }

    private class processAsync extends AsyncTask<String, String, String> {
        ConsignmentTransaction consTransaction;


        @Override
        protected void onPreExecute() {
            myProgressDialog = new ProgressDialog(activity);
            myProgressDialog.setMessage("Processing...");
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.setCancelable(false);
            myProgressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            ordersHandler = new OrdersHandler(activity);
            orderProductsHandler = new OrderProductsHandler(activity);
            consTransDBHandler = new ConsignmentTransactionHandler(activity);
            ProductsHandler prodHandler = new ProductsHandler(activity);
            GenerateNewID generator = new GenerateNewID(activity);
            Global.lastOrdID = generator.getNextID(IdType.ORDER_ID);
            custInventoryHandler.insertUpdate(Global.custInventoryList);
            consTransaction = new ConsignmentTransaction();
            int size = Global.consignMapKey.size();
            String temp;
            int index;
            double tempQty;
            double returnQty = 0, fillupQty = 0;
            String consTransID;
            double onHandQty = -1;
            consTransID = generator.getNextID(IdType.CONSIGNMENT_ID);
            signatureMap.put("ConsTrans_ID", consTransID);
            for (int i = 0; i < size; i++) {
                consTransaction.ConsEmp_ID = myPref.getEmpID();
                consTransaction.ConsCust_ID = myPref.getCustID();
                consTransaction.ConsProd_ID = Global.consignMapKey.get(i);
                consTransaction.ConsTrans_ID = consTransID;
                temp = Global.consignSummaryMap.get(Global.consignMapKey.get(i)).get("original_qty");
                if (temp == null)
                    temp = "0.0";
                consTransaction.ConsOriginal_Qty = temp;


                temp = Global.consignSummaryMap.get(Global.consignMapKey.get(i)).get("rack");
                if (temp == null)
                    temp = "0.0";
                consTransaction.ConsStock_Qty = temp;


                temp = Global.consignSummaryMap.get(Global.consignMapKey.get(i)).get("return_index");
                if (temp != null) {
                    index = Integer.parseInt(temp);
                    temp = Global.cons_return_products.get(index).ordprod_qty;
                    onHandQty = Double.parseDouble(Global.cons_return_products.get(index).onHand);
                    returnQty = Double.parseDouble(temp);

                    consTransaction.ConsReturn_Qty = temp;
                    temp = Global.cons_return_products.get(index).ord_id;
                    consTransaction.ConsReturn_ID = temp;
                }

                temp = Global.consignSummaryMap.get(Global.consignMapKey.get(i)).get("fillup_index");
                if (temp != null) {
                    index = Integer.parseInt(temp);
                    temp = Global.cons_fillup_products.get(index).ordprod_qty;
                    onHandQty = Double.parseDouble(Global.cons_fillup_products.get(index).onHand);
                    fillupQty = Double.parseDouble(temp);

                    consTransaction.ConsDispatch_Qty = temp;
                    temp = Global.cons_fillup_products.get(index).ord_id;
                    consTransaction.ConsDispatch_ID = temp;

                }
                tempQty = Double.parseDouble(consTransaction.ConsStock_Qty) + Double.parseDouble(consTransaction.ConsDispatch_Qty);
                consTransaction.ConsNew_Qty = Double.toString(tempQty);

                if (onHandQty != -1) {
                    prodHandler.updateProductOnHandQty(Global.consignMapKey.get(i), onHandQty - fillupQty + returnQty);
                }


                temp = Global.consignSummaryMap.get(Global.consignMapKey.get(i)).get("invoice");
                if (temp != null) {
                    tempQty = Double.parseDouble(temp);
                    consTransaction.ConsInvoice_Qty = temp;
                    consTransaction.invoice_total = Global.consignSummaryMap.get(Global.consignMapKey.get(i)).get("invoice_total");
                    if (tempQty > 0) {
                        ifInvoice = true;
                        consTransaction.ConsInvoice_ID = Global.consignment_order.ord_id;
                        generateOrder(i, consTransaction);
                    }
                }
                consTransactionList.add(consTransaction);
                consTransaction = new ConsignmentTransaction();
            }
            return null;
        }


        @Override
        protected void onPostExecute(String unused) {
            myProgressDialog.dismiss();

            int orientation = getResources().getConfiguration().orientation;
            Intent intent = new Intent(getActivity(), DrawReceiptActivity.class);
            if (orientation == Configuration.ORIENTATION_PORTRAIT)
                intent.putExtra("inPortrait", true);
            else
                intent.putExtra("inPortrait", false);
            startActivityForResult(intent, Global.S_CONSIGNMENT_TRANSACTION);
        }


        private void generateOrder(int pos, ConsignmentTransaction consTransaction) {

            OrderProduct ord = new OrderProduct();
            double temp = Double.parseDouble(consTransaction.ConsInvoice_Qty);

            // add order to db
            ord.ordprod_qty = Double.toString(temp);
            ord.ordprod_name = Global.consignSummaryMap.get(Global.consignMapKey.get(pos)).get("ordprod_name");
            ord.ordprod_desc = Global.consignSummaryMap.get(Global.consignMapKey.get(pos)).get("ordprod_desc");
            ord.prod_id = Global.consignSummaryMap.get(Global.consignMapKey.get(pos)).get("prod_id");
            ord.overwrite_price = Global.consignSummaryMap.get(Global.consignMapKey.get(pos)).get("prod_price");
            ord.ord_id = Global.consignment_order.ord_id;


            if (global.orderProducts == null) {
                global.orderProducts = new ArrayList<OrderProduct>();
            }

            UUID uuid = UUID.randomUUID();
            String randomUUIDString = uuid.toString();

            global.orderProducts.add(ord);
            ord.ordprod_id = randomUUIDString;

            // end of adding to db;
            ordTotal += Double.parseDouble(Global.consignSummaryMap.get(Global.consignMapKey.get(pos)).get("invoice_total"));

        }

    }

    private void finishConsignment() {
        if (!signatureMap.get("encoded_signature").isEmpty()) {
            ConsignmentSignaturesDBHandler signHandler = new ConsignmentSignaturesDBHandler(activity);
            signHandler.insert(signatureMap);
        }
//        updateConsignmentTransactionIds(consTransactionList.get(0).ConsTrans_ID);
        consTransDBHandler.insert(consTransactionList);
        if (!ifInvoice)
            activity.finish();
        else
            showYesNoPrompt(false, R.string.dlog_title_confirm, R.string.take_payment_now);
    }

//    private void updateConsignmentTransactionIds(String consTrans_ID) {
//        for (ConsignmentTransaction transaction : consTransactionList) {
//            if (transaction.ConsTrans_ID.equalsIgnoreCase(consTrans_ID)) {
//                if (Global.consignment_order != null) {
//                    transaction.ConsInvoice_ID = Global.consignment_order.ord_id;
//                }
//                if (Global.cons_return_order != null) {
//                    transaction.ConsReturn_ID = Global.cons_return_order.ord_id;
//                }
//                if (Global.cons_fillup_order != null) {
//                    transaction.ConsDispatch_ID = Global.cons_fillup_order.ord_id;
//                }
//            }
//        }
//    }

    private void processOrder() {
        global.order = new Order(activity);
        TaxesHandler taxHandler = new TaxesHandler(activity);
        BigDecimal _order_total = BigDecimal.valueOf(ordTotal);

        BigDecimal _tax_amount = new BigDecimal(0);
        if (!myPref.getCustTaxCode().isEmpty()) {
            String _tax_id = myPref.getCustTaxCode();
            BigDecimal _tax_rate = new BigDecimal(taxHandler.getTaxRate(_tax_id, "", 0)).
                    divide(BigDecimal.valueOf(100)).setScale(4, RoundingMode.HALF_UP);
            global.order.tax_id = _tax_id;
            _tax_amount = BigDecimal.valueOf(ordTotal).multiply(_tax_rate).setScale(4, RoundingMode.HALF_UP);
            global.order.ord_taxamount = _tax_amount.setScale(2, RoundingMode.HALF_UP).toString();
            //_order_total+=_tax_amount;
            _order_total = _order_total.add(_tax_amount).setScale(4, RoundingMode.HALF_UP);
        } else {
            global.order.ord_taxamount = "0.00";
            for (DataTaxes taxes : global.listOrderTaxes) {
                _tax_amount = _tax_amount.add(new BigDecimal(taxes.getTax_rate()).
                        divide(new BigDecimal(100)).multiply(new BigDecimal(ordTotal))).setScale(4, RoundingMode.HALF_UP);
                taxes.setTax_amount(new BigDecimal(taxes.getTax_rate()).
                        divide(new BigDecimal(100)).multiply(new BigDecimal(ordTotal)).setScale(2, RoundingMode.HALF_UP).toString());
                _order_total = _order_total.add(new BigDecimal(taxes.getTax_rate()).
                        divide(new BigDecimal(100)).multiply(new BigDecimal(ordTotal))).setScale(4, RoundingMode.HALF_UP);
            }
            global.order.ord_taxamount = _tax_amount.setScale(2, RoundingMode.HALF_UP).toString();
        }


//        TaxesCalculator calculator;
//        List<HashMap<String, String>> listMapTaxes = new ArrayList<HashMap<String, String>>();
//        for (OrderProducts ordProd : global.orderProducts) {
//            listMapTaxes.clear();
//            mapTax.put("tax_id", global.listOrderTaxes.get(0).getOrd_tax_id());
//            mapTax.put("tax_name", global.listOrderTaxes.get(0).getTax_name());
//            mapTax.put("tax_rate", global.listOrderTaxes.get(0).getTax_rate());
//            listMapTaxes.add(mapTax);
//            calculator = new TaxesCalculator(activity, myPref, ordProd, Global.taxID,
//                    0, 0, new BigDecimal(ordProd.disTotal), new BigDecimal(ordProd.disAmount), listMapTaxes);
//        }
        global.order.ord_total = _order_total.setScale(2, RoundingMode.HALF_UP).toString();
        global.order.ord_subtotal = Double.toString(ordTotal);

        ordTotal = _order_total.setScale(2, RoundingMode.HALF_UP).doubleValue();

        global.order.ord_id = Global.consignment_order.ord_id;
        global.order.qbord_id = Global.lastOrdID.replace("-", "");

        global.order.cust_id = myPref.getCustID();

        global.order.ord_type = Global.OrderType.CONSIGNMENT_INVOICE.getCodeString();

        global.order.total_lines = Integer.toString(global.orderProducts.size());
        global.order.ord_signature = encodedImage;

        String[] location = Global.getCurrLocation(activity);
        global.order.ord_latitude = location[0];
        global.order.ord_longitude = location[1];
        global.order.processed = "1";
        ordersHandler.insert(global.order);

        orderProductsHandler.insert(global.orderProducts);
        if (global.listOrderTaxes != null
                && global.listOrderTaxes.size() > 0
                ) {
            OrderTaxes_DB ordTaxesDB = new OrderTaxes_DB();
            ordTaxesDB.insert(global.listOrderTaxes, global.order.ord_id);
        }
    }


    private void showYesNoPrompt(final boolean isPrintPrompt, int title, int msg) {
        final Dialog dlog = new Dialog(activity, R.style.Theme_TransparentTest);
        dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dlog.setCancelable(false);
        dlog.setContentView(R.layout.dlog_btn_left_right_layout);

        TextView viewTitle = (TextView) dlog.findViewById(R.id.dlogTitle);
        TextView viewMsg = (TextView) dlog.findViewById(R.id.dlogMessage);
        viewTitle.setText(title);
        viewMsg.setText(msg);
        Button btnYes = (Button) dlog.findViewById(R.id.btnDlogLeft);
        Button btnNo = (Button) dlog.findViewById(R.id.btnDlogRight);
        dlog.findViewById(R.id.btnDlogCancel).setVisibility(View.GONE);

        btnYes.setText(R.string.button_yes);
        btnNo.setText(R.string.button_no);

        btnYes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dlog.dismiss();
                if (isPrintPrompt) {
                    new printAsync().execute("");
                } else {
                    Intent intent = new Intent(getActivity(), SelectPayMethod_FA.class);
                    intent.putExtra("typeOfProcedure", Global.OrderType.INVOICE);
                    intent.putExtra("salesinvoice", true);
                    intent.putExtra("ord_subtotal", Double.toString(ordTotal));
                    intent.putExtra("ord_taxID", "");
                    intent.putExtra("amount", Double.toString(ordTotal));
                    intent.putExtra("paid", "0.00");
                    intent.putExtra("job_id", Global.consignment_order.ord_id);
                    intent.putExtra("ord_type", Global.OrderType.CONSIGNMENT_INVOICE);

                    intent.putExtra("cust_id", myPref.getCustID());
                    intent.putExtra("custidkey", myPref.getCustIDKey());

                    startActivityForResult(intent, 0);
                    activity.finish();
                }
            }
        });
        btnNo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dlog.dismiss();
                if (isPrintPrompt) {
                    finishConsignment();
                } else {
                    activity.finish();
                }
            }
        });
        dlog.show();
    }


    private class printAsync extends AsyncTask<String, String, String> {
        boolean printSuccessful = true;

        @Override
        protected void onPreExecute() {
            myProgressDialog = new ProgressDialog(activity);
            myProgressDialog.setMessage("Printing...");
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.setCancelable(false);
            myProgressDialog.show();

        }

        @Override
        protected String doInBackground(String... params) {
            if (Global.mainPrinterManager != null && Global.mainPrinterManager.currentDevice != null)
                printSuccessful = Global.mainPrinterManager.currentDevice.printConsignment(consTransactionList, encodedImage);
            return null;
        }

        @Override
        protected void onPostExecute(String unused) {
            myProgressDialog.dismiss();
            if (printSuccessful)
                showYesNoPrompt(true, R.string.dlog_title_confirm, R.string.dlog_msg_want_to_reprint);
            else
                showYesNoPrompt(true, R.string.dlog_title_error, R.string.dlog_msg_failed_print);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Global.FROM_DRAW_RECEIPT_PORTRAIT) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }

        encodedImage = global.encodedImage;
        global.encodedImage = "";
        signatureMap.put("encoded_signature", encodedImage);
        GenerateNewID idGen = new GenerateNewID(activity);

        if (ifInvoice) {
            processOrder();
            consTransactionList.get(0).ConsInvoice_ID = Global.consignment_order.ord_id;
        }
        if (Global.cons_return_products.size() > 0) {
            Global.cons_return_order.processed = "1";
            Global.cons_return_order.ord_id = idGen.getNextID(IdType.ORDER_ID);
            Global.cons_return_order.ord_signature = encodedImage;
            Global.cons_return_order.ord_type = Global.OrderType.CONSIGNMENT_RETURN.getCodeString();
            ordersHandler.insert(Global.cons_return_order);
            for (OrderProduct product : Global.cons_return_products) {
                product.ord_id = Global.cons_return_order.ord_id;
            }
            orderProductsHandler.insert(Global.cons_return_products);
            consTransactionList.get(0).ConsReturn_ID = Global.cons_return_order.ord_id;

        }
        if (Global.cons_fillup_products.size() > 0) {
            Global.cons_fillup_order.processed = "1";
            Global.cons_fillup_order.ord_id = idGen.getNextID(IdType.ORDER_ID);
            Global.cons_fillup_order.ord_signature = encodedImage;
            ordersHandler.insert(Global.cons_fillup_order);
            for (OrderProduct product : Global.cons_fillup_products) {
                product.ord_id = Global.cons_fillup_order.ord_id;
            }
            orderProductsHandler.insert(Global.cons_fillup_products);
            consTransactionList.get(0).ConsDispatch_ID = Global.cons_fillup_order.ord_id;

        }


        if (myPref.getPreferences(MyPreferences.pref_enable_printing)) {
            if (!myPref.getPreferences(MyPreferences.pref_automatic_printing))
                showYesNoPrompt(true, R.string.dlog_title_confirm, R.string.dlog_msg_want_to_print);
            else
                new printAsync().execute();
        } else
            finishConsignment();


        super.onActivityResult(requestCode, resultCode, data);
    }


    private class CustomAdapter_LV extends BaseAdapter {
        private LayoutInflater mInflater;

        private List<String> idList;


        public CustomAdapter_LV(Context context) {
            mInflater = LayoutInflater.from(context);

            idList = Global.consignMapKey;
        }


        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            ViewHolder holder;

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.consign_lisview_adapter, null);

                holder = new ViewHolder();

                holder.prodName = (TextView) convertView.findViewById(R.id.consignProdName);
                holder.prodID = (TextView) convertView.findViewById(R.id.consignProdID);
                holder.originalQty = (TextView) convertView.findViewById(R.id.consignOriginalQty);
                holder.rackQty = (TextView) convertView.findViewById(R.id.consignStackQty);
                holder.returnQty = (TextView) convertView.findViewById(R.id.consignReturnQty);
                holder.fillupQty = (TextView) convertView.findViewById(R.id.consignFillupQty);
                holder.issueQty = (TextView) convertView.findViewById(R.id.consignIssueQty);

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.prodName.setText(getContentValues(position, 0));
            holder.prodID.setText(getContentValues(position, 1));
            holder.rackQty.setText(getContentValues(position, 2));
            holder.returnQty.setText(getContentValues(position, 3));
            holder.fillupQty.setText(getContentValues(position, 4));
            holder.issueQty.setText(getContentValues(position, 5));
            holder.originalQty.setText(getContentValues(position, 6));

            return convertView;
        }


        public class ViewHolder {
            TextView prodName, prodID, rackQty, returnQty, fillupQty, issueQty, originalQty;
        }


        private String getContentValues(int position, int type) {
            String value = "";
            String empStr = "";
            switch (type) {
                case 0://Name
                    value = Global.consignSummaryMap.get(Global.consignMapKey.get(position)).get("ordprod_name");
                    if (value == null)
                        value = empStr;
                    break;
                case 1://ID
                    value = Global.consignSummaryMap.get(Global.consignMapKey.get(position)).get("prod_id");
                    if (value == null)
                        value = empStr;
                    else
                        value = " (" + value + ")";
                    break;
                case 2:    //Rack
                    value = Global.consignSummaryMap.get(Global.consignMapKey.get(position)).get("rack");
                    if (value == null)
                        value = "0";
                    break;
                case 3:
                    value = Global.consignSummaryMap.get(Global.consignMapKey.get(position)).get("return");
                    if (value == null)
                        value = "0";
                    break;
                case 4:
                    value = Global.consignSummaryMap.get(Global.consignMapKey.get(position)).get("fillup");
                    if (value == null)
                        value = "0";
                    break;
                case 5:
                    value = Global.consignSummaryMap.get(Global.consignMapKey.get(position)).get("invoice");
                    if (value == null)
                        value = "0";
                    break;
                case 6:
                    value = Global.consignSummaryMap.get(Global.consignMapKey.get(position)).get("original_qty");
                    if (value == null)
                        value = "0";
                    break;
            }
            return value;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public int getCount() {
            return idList.size();
        }

        @Override
        public Object getItem(int position) {
            return idList.get(position);
        }
    }
}