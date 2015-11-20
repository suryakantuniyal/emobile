package com.android.emobilepos;


import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.widget.CursorAdapter;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.database.CustomersHandler;
import com.android.database.OrderProductsHandler;
import com.android.database.OrdersHandler;
import com.android.database.ProductAddonsHandler;
import com.android.database.ProductsHandler;
import com.android.database.SalesTaxCodesHandler;
import com.android.emobilepos.models.OrderProducts;
import com.android.emobilepos.ordering.OrderingMain_FA;
import com.android.saxhandler.SAXdownloadHandler;
import com.android.support.DBManager;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.Post;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


public class OnHoldActivity extends Activity {
    private Activity activity;
    private ListView listView;
    private ListViewCursorAdapter myAdapter;
    private Cursor myCursor;
    //private DBManager dbManager;
    //private SQLiteDatabase db;
    private Global global;
    private boolean isAddon = false;
    private Global.OrderType orderType = Global.OrderType.SALES_RECEIPT;
    private String ord_HoldName = "";
    private ProgressDialog myProgressDialog;
    boolean validPassword = true;
    private boolean isUpdateOnHold = false;
    private boolean hasBeenCreated = false;
    private MyPreferences myPref;
    private int selectedPos = 0;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.onhold_layout);
        activity = this;
        global = (Global) activity.getApplication();
        //dbManager = new DBManager(activity);
        //db = dbManager.openReadableDB();
        myPref = new MyPreferences(activity);
        listView = (ListView) findViewById(R.id.onHoldListView);
        OrdersHandler ordersHandler = new OrdersHandler(activity);
        myCursor = ordersHandler.getOrderOnHold();
        myAdapter = new ListViewCursorAdapter(activity, myCursor, CursorAdapter.NO_SELECTION);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
                                    long arg3) {
                // TODO Auto-generated method stub
                selectedPos = pos;
                openPrintOnHold();
            }
        });
        listView.setAdapter(myAdapter);
        hasBeenCreated = true;

    }


    @Override
    public void onDestroy() {
        myCursor.close();
        super.onDestroy();
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


    public class checkHoldStatus extends AsyncTask<Void, String, String> {

        private String[] returnedPost;
        boolean timedOut = false;
        boolean wasProcessed = false;


        @Override
        protected void onPreExecute() {
            myProgressDialog = new ProgressDialog(activity);
            myProgressDialog.setMessage("Loading...");
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.setCancelable(false);
            myProgressDialog.show();

        }

        @Override
        protected String doInBackground(Void... params) {
            // TODO Auto-generated method stub
            Post httpClient = new Post();

            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXdownloadHandler handler = new SAXdownloadHandler(activity);
            myCursor.moveToPosition(selectedPos);
            myPref.setCustSelected(false);

            String ordID = myCursor.getString(myCursor.getColumnIndex("ord_id"));
            global.setSelectedComments(myCursor.getString(myCursor.getColumnIndex("ord_comment")));


            if (Global.isConnectedToInternet(activity)) {
                //selectCustomer(myCursor.getString(myCursor.getColumnIndex("cust_id")));
                try {


                    if (!isUpdateOnHold) {
                        String xml = httpClient.postData(Global.S_CHECK_STATUS_ON_HOLD, activity, ordID);

                        if (xml.equals(Global.TIME_OUT)) {
                            //errorMsg = "TIME OUT, would you like to try again?";
                            timedOut = true;
                        } else if (xml.equals(Global.NOT_VALID_URL)) {
                            //errorMsg = "Can not proceed...";
                        } else {
                            InputSource inSource = new InputSource(new StringReader(xml));

                            SAXParser sp = spf.newSAXParser();
                            XMLReader xr = sp.getXMLReader();
                            xr.setContentHandler(handler);
                            xr.parse(inSource);
                            List<String[]> temp = handler.getEmpData();

                            if (temp != null && temp.size() > 0) {
                                returnedPost = new String[handler.getEmpData().size()];
                                returnedPost = handler.getEmpData().get(0);
                            }


                            if (returnedPost != null && returnedPost.length > 0 && returnedPost[1].equals("0")) {
                                wasProcessed = true;
                                httpClient.postData(Global.S_UPDATE_STATUS_ON_HOLD, activity, ordID);
                            }
                        }
                    } else {
                        wasProcessed = true;
                        httpClient.postData(Global.S_UPDATE_STATUS_ON_HOLD, activity, ordID);
                    }

                } catch (Exception e) {
                    // TODO Auto-generated catch block
//					Tracker tracker = EasyTracker.getInstance(activity);
//					tracker.send(MapBuilder.createException(e.getStackTrace().toString(), false).build());
                }

                return null;
            } else {
                OrdersHandler ordersHandler = new OrdersHandler(activity);
                if (ordersHandler.isOrderOffline(ordID))
                    wasProcessed = true;

                return null;
            }
        }

        @Override
        protected void onPostExecute(String unused) {
            myProgressDialog.dismiss();

            if (wasProcessed) {
                //executeOnHold(false);
                new executeOnHoldAsync().execute(false);
            } else {
                claimedTransactionPrompt();
            }
        }
    }


//	private void executeOnHold(boolean forPrinting) {
//		myCursor.moveToPosition(selectedPos);
//		OrderProductsHandler orderProdHandler = new OrderProductsHandler(activity);
//
//		Global.lastOrdID = myCursor.getString(myCursor.getColumnIndex("ord_id"));
//		Global.taxID = myCursor.getString(myCursor.getColumnIndex("tax_id"));
//
//		orderType = Integer.parseInt(myCursor.getString(myCursor.getColumnIndex("ord_type")));
//		ord_HoldName = myCursor.getString(myCursor.getColumnIndex("ord_HoldName"));
//		selectCustomer(myCursor.getString(myCursor.getColumnIndex("cust_id")));
//
//		Intent intent = null;
//		if (!forPrinting) {
//			intent = new Intent(activity, OrderingMain_FA.class);
//			// intent = new Intent(activity, SalesReceiptSplitActivity.class);
//
//			switch (orderType) {
//			case Global.INT_SALES_RECEIPT:
//				intent.putExtra("option_number", 0);
//				break;
//			case Global.INT_RETURN:
//				intent.putExtra("option_number", 2);
//				break;
//			case Global.INT_ORDER:
//				intent.putExtra("option_number", 1);
//				break;
//			case Global.INT_INVOICE:
//				intent.putExtra("option_number", 3);
//				break;
//			case Global.INT_ESTIMATE:
//				intent.putExtra("option_number", 4);
//				break;
//			}
//
//			intent.putExtra("ord_HoldName", ord_HoldName);
//			Global.isFromOnHold = true;
//
//		}
//		if (!Global.isConnectedToInternet(activity)) {
//			Cursor c = orderProdHandler.getOrderProductsOnHold(myCursor.getString(myCursor.getColumnIndex("ord_id")));
//			int size = c.getCount();
//			if (size > 0) {
//				if (!forPrinting) {
//					addOrder(c);
//					isAddon = false;
//
//					startActivityForResult(intent, 0);
//					activity.finish();
//				} else {
//					DBManager dbManager = new DBManager(activity);
//					dbManager.synchDownloadOnHoldDetails(intent, myCursor.getString(myCursor.getColumnIndex("ord_id")), 1);
//				}
//			} else
//				Toast.makeText(activity, "no available items", Toast.LENGTH_LONG).show();
//		} else {
//			DBManager dbManager = new DBManager(activity);
//			if (!forPrinting)
//				dbManager.synchDownloadOnHoldDetails(intent, myCursor.getString(myCursor.getColumnIndex("ord_id")), 0);
//			else
//				dbManager.synchDownloadOnHoldDetails(intent, myCursor.getString(myCursor.getColumnIndex("ord_id")), 1);
//		}
//	}


    private class executeOnHoldAsync extends AsyncTask<Boolean, Void, Void> {
        private boolean proceed = false;
        private Intent intent;
        private OrderProductsHandler orderProdHandler;
        private boolean forPrinting = false;

        @Override
        protected void onPreExecute() {
            myProgressDialog = new ProgressDialog(activity);
            myProgressDialog.setMessage("Please wait...");
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.setCancelable(false);
            myProgressDialog.show();

        }

        @Override
        protected Void doInBackground(Boolean... params) {
            // TODO Auto-generated method stub

            myCursor.moveToPosition(selectedPos);
            orderProdHandler = new OrderProductsHandler(activity);

            Global.lastOrdID = myCursor.getString(myCursor.getColumnIndex("ord_id"));
            Global.taxID = myCursor.getString(myCursor.getColumnIndex("tax_id"));

            orderType = Global.OrderType.getByCode(Integer.parseInt(myCursor.getString(myCursor.getColumnIndex("ord_type"))));
            ord_HoldName = myCursor.getString(myCursor.getColumnIndex("ord_HoldName"));
            selectCustomer(myCursor.getString(myCursor.getColumnIndex("cust_id")));

            forPrinting = params[0];
            if (!forPrinting) {
                intent = new Intent(activity, OrderingMain_FA.class);
                // intent = new Intent(activity, SalesReceiptSplitActivity.class);

                switch (orderType) {
                    case SALES_RECEIPT:
                        intent.putExtra("option_number", 0);
                        break;
                    case RETURN:
                        intent.putExtra("option_number", 2);
                        break;
                    case ORDER:
                        intent.putExtra("option_number", 1);
                        break;
                    case INVOICE:
                        intent.putExtra("option_number", 3);
                        break;
                    case ESTIMATE:
                        intent.putExtra("option_number", 4);
                        break;
                }

                intent.putExtra("ord_HoldName", ord_HoldName);
                Global.isFromOnHold = true;

            }

            if (Global.isConnectedToInternet(activity))
                proceed = true;

            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            myProgressDialog.dismiss();
            if (proceed) {
                DBManager dbManager = new DBManager(activity);
                if (!forPrinting)
                    dbManager.synchDownloadOnHoldDetails(intent, myCursor.getString(myCursor.getColumnIndex("ord_id")), 0);
                else
                    dbManager.synchDownloadOnHoldDetails(intent, myCursor.getString(myCursor.getColumnIndex("ord_id")), 1);
            } else {
                Cursor c = orderProdHandler.getOrderProductsOnHold(myCursor.getString(myCursor.getColumnIndex("ord_id")));
                int size = c.getCount();
                if (size > 0) {
                    if (!forPrinting) {
                        addOrder(c);
                        isAddon = false;

                        startActivityForResult(intent, 0);
                        activity.finish();
                    } else {
                        DBManager dbManager = new DBManager(activity);
                        dbManager.synchDownloadOnHoldDetails(intent, myCursor.getString(myCursor.getColumnIndex("ord_id")), 1);
                    }
                } else
                    Toast.makeText(activity, "no available items", Toast.LENGTH_LONG).show();
            }
        }
    }


    public void printOnHoldTransaction() {
        new printAsync().execute();
    }

    private class printAsync extends AsyncTask<Void, Void, Void> {
        private boolean printSuccessful = true;

        @Override
        protected void onPreExecute() {
            myProgressDialog = new ProgressDialog(activity);
            myProgressDialog.setMessage("Printing...");
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.setCancelable(false);
            myProgressDialog.show();

        }

        @Override
        protected Void doInBackground(Void... params) {
            // TODO Auto-generated method stub


            if (Global.mainPrinterManager != null && Global.mainPrinterManager.currentDevice != null) {
                printSuccessful = Global.mainPrinterManager.currentDevice.printTransaction(Global.lastOrdID, orderType, false, true);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
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

        Button btnYes = (Button) dlog.findViewById(R.id.btnDlogLeft);
        Button btnNo = (Button) dlog.findViewById(R.id.btnDlogRight);
        btnYes.setText(R.string.button_yes);
        btnNo.setText(R.string.button_no);

        btnYes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                dlog.dismiss();
                new printAsync().execute();

            }
        });
        btnNo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                dlog.dismiss();
            }
        });
        dlog.show();
    }


    private void claimedTransactionPrompt() {

        final Dialog dlog = new Dialog(activity, R.style.Theme_TransparentTest);
        dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dlog.setCancelable(true);
        dlog.setCanceledOnTouchOutside(true);
        dlog.setContentView(R.layout.dlog_btn_left_right_layout);

        TextView viewTitle = (TextView) dlog.findViewById(R.id.dlogTitle);
        TextView viewMsg = (TextView) dlog.findViewById(R.id.dlogMessage);
        viewTitle.setText(R.string.dlog_title_claimed_hold);
        viewMsg.setText(R.string.dlog_msg_claimed_hold);
        Button btnOpen = (Button) dlog.findViewById(R.id.btnDlogLeft);
        Button btnCancel = (Button) dlog.findViewById(R.id.btnDlogRight);
        btnOpen.setText(R.string.button_open);
        btnCancel.setText(R.string.button_cancel);

        btnOpen.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                dlog.dismiss();
                askForManagerPassDlg();

            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                dlog.dismiss();
            }
        });
        dlog.show();

    }


    private void askForManagerPassDlg()        //type 0 = Change Password, type 1 = Configure Home Menu
    {


        final Dialog globalDlog = new Dialog(activity, R.style.Theme_TransparentTest);
        globalDlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        globalDlog.setCancelable(true);
        globalDlog.setCanceledOnTouchOutside(true);
        globalDlog.setContentView(R.layout.dlog_field_single_layout);

        final EditText viewField = (EditText) globalDlog.findViewById(R.id.dlogFieldSingle);
        viewField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        TextView viewTitle = (TextView) globalDlog.findViewById(R.id.dlogTitle);
        TextView viewMsg = (TextView) globalDlog.findViewById(R.id.dlogMessage);
        viewTitle.setText(R.string.dlog_title_confirm);
        if (!validPassword)
            viewMsg.setText(R.string.invalid_password);
        else
            viewMsg.setText(R.string.dlog_title_enter_manager_password);

        Button btnOk = (Button) globalDlog.findViewById(R.id.btnDlogSingle);
        btnOk.setText(R.string.button_ok);
        btnOk.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                globalDlog.dismiss();
                String value = viewField.getText().toString();
                if (value.equals(myPref.posManagerPass(true, null))) // validate manager password
                {
                    validPassword = true;
                    isUpdateOnHold = true;
                    ;
                    new checkHoldStatus().execute();
                } else {
                    validPassword = false;
                    askForManagerPassDlg();
                }
            }
        });
        globalDlog.show();

    }


    private void openPrintOnHold()        //type 0 = Change Password, type 1 = Configure Home Menu
    {

        final Dialog dlog = new Dialog(activity, R.style.Theme_TransparentTest);
        dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dlog.setCancelable(true);
        dlog.setCanceledOnTouchOutside(true);
        dlog.setContentView(R.layout.dlog_btn_left_right_layout);

        TextView viewTitle = (TextView) dlog.findViewById(R.id.dlogTitle);
        TextView viewMsg = (TextView) dlog.findViewById(R.id.dlogMessage);
        viewTitle.setText(R.string.dlog_title_choose_action);
        viewMsg.setVisibility(View.GONE);
        Button btnOpen = (Button) dlog.findViewById(R.id.btnDlogLeft);
        Button btnPrint = (Button) dlog.findViewById(R.id.btnDlogRight);
        btnOpen.setText(R.string.button_open);
        btnPrint.setText(R.string.button_print);

        btnOpen.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                dlog.dismiss();
                new checkHoldStatus().execute();
//				if(Global.isConnectedToInternet(activity))
//					new checkHoldStatus().execute();
//				else
//					executeOnHold(false);

            }
        });
        btnPrint.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                dlog.dismiss();
                myCursor.moveToPosition(selectedPos);
                if (myCursor.getString(myCursor.getColumnIndex("ord_issync")).equals("0")) {
                    Global.lastOrdID = myCursor.getString(myCursor.getColumnIndex("ord_id"));
                    new printAsync().execute();
                } else {
                    //executeOnHold(true);
                    new executeOnHoldAsync().execute(true);
                }
            }
        });
        dlog.show();

    }


    public void addOrder(Cursor c) {
        int size = c.getCount();
        OrderProducts ord = new OrderProducts();
        ProductAddonsHandler prodAddonHandler = new ProductAddonsHandler(activity);
        ProductsHandler prodHandler = new ProductsHandler(activity);
        String[] discountInfo;
        double discAmount = 0;
        double total = 0;
        double itemTotal = 0;


        for (int i = 0; i < size; i++) {
            ord.prod_istaxable = c.getString(c.getColumnIndex("prod_istaxable"));
            global.qtyCounter.put(c.getString(c.getColumnIndex("prod_id")), c.getString(c.getColumnIndex("ordprod_qty")));


            ord.ordprod_qty = c.getString(c.getColumnIndex("ordprod_qty"));
            ord.ordprod_name = c.getString(c.getColumnIndex("ordprod_name"));
            ord.ordprod_desc = c.getString(c.getColumnIndex("ordprod_desc"));
            ord.prod_id = c.getString(c.getColumnIndex("prod_id"));
            ord.overwrite_price = c.getString(c.getColumnIndex("overwrite_price"));
            ord.onHand = c.getString(c.getColumnIndex("onHand"));
            ord.imgURL = c.getString(c.getColumnIndex("ordprod_qty"));
            ord.isPrinted = c.getString(c.getColumnIndex("isPrinted"));

            total = (Double.parseDouble(ord.ordprod_qty)) * Double.parseDouble(ord.overwrite_price);
            ord.prod_taxValue = c.getString(c.getColumnIndex("prod_taxValue"));
            ord.prod_istaxable = c.getString(c.getColumnIndex("prod_istaxable"));
            ord.prod_taxtype = c.getString(c.getColumnIndex("prod_taxtype"));
            //ord.getSetData("discount_value", false, c.getString(c.getColumnIndex("discount_value")));


            // for calculating taxes and discount at receipt
            ord.prod_taxId = c.getString(c.getColumnIndex("prod_taxId"));
            ord.discount_id = c.getString(c.getColumnIndex("discount_id"));
                /*ord.getSetData("taxAmount", false, taxAmount);
				ord.getSetData("taxTotal", false, taxTotal);
				ord.getSetData("disAmount", false, disAmount);
				ord.getSetData("disTotal", false, disTotal);*/

            ord.pricelevel_id = c.getString(c.getColumnIndex("pricelevel_id"));
            //ord.getSetData("priceLevelName", false, priceLevelName);

            ord.prod_price = c.getString(c.getColumnIndex("overwrite_price"));
            ord.overwrite_price = c.getString(c.getColumnIndex("overwrite_price"));
            //ord.getSetData("tax_position", false, Integer.toString(tax_position));
            //ord.getSetData("discount_position", false, Integer.toString(discount_position));
            //ord.getSetData("pricelevel_position", false, Integer.toString(pricelevel_position));
            //ord.getSetData("uom_position", false, Integer.toString(uom_position));

            ord.prod_type = c.getString(c.getColumnIndex("prod_type"));

            //Add UOM attributes to the order
            ord.uom_name = c.getString(c.getColumnIndex("uom_name"));
            ord.uom_id = c.getString(c.getColumnIndex("uom_id"));
            ord.uom_conversion = c.getString(c.getColumnIndex("uom_conversion"));
				
				
				/*if(discountIsTaxable)
				{
					ord.getSetData("discount_is_taxable", false, "1");
				}
				if(isFixed)
					ord.getSetData("discount_is_fixed", false, "1");
				*/
            discountInfo = prodHandler.getDiscount(ord.discount_id, ord.overwrite_price);

            if (discountInfo != null) {
                if (discountInfo[1] != null && discountInfo[1].equals("Fixed")) {
                    ord.discount_is_fixed = "1";
                }
                if (discountInfo[2] != null) {
                    discAmount = Double.parseDouble(discountInfo[4]);
                }
                if (discountInfo[3] != null) {
                    ord.discount_is_taxable = discountInfo[3];
                }
                if (discountInfo[4] != null) {
                    ord.disTotal = discountInfo[4];
                    discAmount = Double.parseDouble(discountInfo[4]);
                    ord.discount_value = discountInfo[4];
                }
            }


//				if(discountInfo!=null&&discountInfo[3]!=null)
//					ord.getSetData("discount_is_taxable", false, discountInfo[3]);
//				if(discountInfo!=null&&discountInfo[0]!=null)
//					ord.getSetData("discount_value", false, discountInfo[0]);
//				if(discountInfo!=null&&discountInfo[1]!=null&&discountInfo[1].equals("Fixed"))
//					ord.getSetData("discount_is_fixed", false, "1");
//				if(discountInfo!=null&&discountInfo[2]!=null)
//					discAmount = Double.parseDouble(discountInfo[2]);
//				else
//					discAmount = 0;
//				if(discountInfo!=null&&discountInfo[4]!=null)
//					ord.getSetData("disTotal", false, discountInfo[4]);

            ord.disAmount = ord.discount_value;


            if (itemTotal < 0)
                itemTotal = 0;

            ord.itemTotal = Double.toString(total - discAmount);
            ord.itemSubtotal = Double.toString(total);

				/*OrdersHandler handler = new OrdersHandler(activity);

				GenerateNewID gen9erator = new GenerateNewID(activity);

				if (handler.getDBSize() == 0)
					Global.lastOrdID = generator.generate("",0);
				else
					Global.lastOrdID = generator.generate(handler.getLastOrdID(),0);
					*/

            ord.ord_id = c.getString(c.getColumnIndex("ord_id"));


            if (global.orderProducts == null) {
                global.orderProducts = new ArrayList<OrderProducts>();
            }


            ord.ordprod_id = c.getString(c.getColumnIndex("ordprod_id"));

            ord.addon = c.getString(c.getColumnIndex("addon"));
            ord.isAdded = c.getString(c.getColumnIndex("isAdded"));
            ord.item_void = c.getString(c.getColumnIndex("item_void"));

            ord.addon_section_name = c.getString(c.getColumnIndex("addon_section_name"));
            ord.addon_position = c.getString(c.getColumnIndex("addon_position"));


            if (c.getString(c.getColumnIndex("addon")).equals("1"))        //is an addon
            {
                isAddon = true;
                String isAdded = "2";

                if (global.addonSelectionType == null)
                    global.addonSelectionType = new HashMap<String, String[]>();

                if (c.getString(c.getColumnIndex("isAdded")).equals("1"))
                    isAdded = "1";


                int pos = global.orderProducts.size();
                OrderProducts temp = null;
                if (pos > 0)
                    temp = global.orderProducts.get(pos - 1);


                String[] tempVal = prodAddonHandler.getAddonDetails(temp.prod_id, ord.prod_id);


                global.addonSelectionType.put(c.getString(c.getColumnIndex("prod_id")), new String[]{isAdded, tempVal[1]});


                global.orderProductsAddons.add(ord);


                if (i + 1 >= size) {
                    if (Global.addonSelectionMap == null)
                        Global.addonSelectionMap = new HashMap<String, HashMap<String, String[]>>();
                    if (Global.orderProductAddonsMap == null)
                        Global.orderProductAddonsMap = new HashMap<String, List<OrderProducts>>();

                    if (global.addonSelectionType.size() > 0 && temp != null) {
                        Global.addonSelectionMap.put(temp.ordprod_id, global.addonSelectionType);
                        Global.orderProductAddonsMap.put(temp.ordprod_id, global.orderProductsAddons);


                        global.orderProductsAddons = new ArrayList<OrderProducts>();
                        global.addonSelectionType = new HashMap<String, String[]>();
                    }
                }
            } else {

                if (isAddon) {
                    int pos = global.orderProducts.size();
                    OrderProducts temp = null;
                    if (pos > 0)
                        temp = global.orderProducts.get(pos - 1);


                    if (Global.addonSelectionMap == null)
                        Global.addonSelectionMap = new HashMap<String, HashMap<String, String[]>>();
                    if (Global.orderProductAddonsMap == null)
                        Global.orderProductAddonsMap = new HashMap<String, List<OrderProducts>>();

                    if (global.addonSelectionType.size() > 0 && temp != null) {
                        Global.addonSelectionMap.put(temp.ordprod_id, global.addonSelectionType);
                        Global.orderProductAddonsMap.put(temp.ordprod_id, global.orderProductsAddons);


                        global.orderProductsAddons = new ArrayList<OrderProducts>();
                        global.addonSelectionType = new HashMap<String, String[]>();

                    }
                }

                isAddon = false;
                global.orderProducts.add(ord);

            }
            ord = new OrderProducts();
            c.moveToNext();
        }
    }


    private void selectCustomer(String custID) {
        if (custID != null && !custID.isEmpty()) {
            CustomersHandler ch = new CustomersHandler(activity);
            HashMap<String, String> temp = ch.getCustomerInfo(custID);


            SalesTaxCodesHandler taxHandler = new SalesTaxCodesHandler(activity);
            if (taxHandler.checkIfCustTaxable(temp.get("cust_taxable")))
                myPref.setCustTaxCode(temp.get("cust_salestaxcode"));
            else
                myPref.setCustTaxCode("");


            myPref.setCustID(temp.get("cust_id"));    //getting cust_id as _id
            myPref.setCustName(temp.get("cust_name"));
            myPref.setCustIDKey(temp.get("custidkey"));
            myPref.setCustSelected(true);
            myPref.setCustPriceLevel(temp.get("pricelevel_id"));
            myPref.setCustEmail(temp.get("cust_email"));
        }
    }


    public class ListViewCursorAdapter extends CursorAdapter {
        private LayoutInflater inflater;


        public ListViewCursorAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
            // TODO Auto-generated constructor stub
            inflater = LayoutInflater.from(context);

            //imageLoaderTest = new ImageLoaderTest(activity);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            // TODO Auto-generated method stu


            final ViewHolder myHolder = (ViewHolder) view.getTag();


            //imageLoaderTest.DisplayImage(cursor.getString(holder.i_url_icon);, holder.itemImage, true);
            myHolder.holdID.setText(cursor.getString(myHolder.i_holdID));
            myHolder.holdName.setText(cursor.getString(myHolder.i_holdName));
            if (cursor.getString(myHolder.i_issync).equals("1"))//it is synched
            {
                myHolder.offlineFlag.setVisibility(View.GONE);
            } else {
                myHolder.offlineFlag.setVisibility(View.VISIBLE);
            }


        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            // TODO Auto-generated method stub

            View retView = null;
            retView = inflater.inflate(R.layout.onhold_listview_adapter, parent, false);

            ViewHolder holder = new ViewHolder();
            holder.holdID = (TextView) retView.findViewById(R.id.onHoldID);
            holder.holdName = (TextView) retView.findViewById(R.id.onHoldName);
            holder.offlineFlag = (TextView) retView.findViewById(R.id.onHoldOfflineFlag);


            holder.i_holdID = cursor.getColumnIndex("_id");
            holder.i_holdName = cursor.getColumnIndex("ord_HoldName");
            holder.i_issync = cursor.getColumnIndex("ord_issync");


            retView.setTag(holder);
            return retView;
        }

        private class ViewHolder {

            TextView holdID, holdName, offlineFlag;

            int i_issync, i_holdID, i_holdName;
        }
    }
}
