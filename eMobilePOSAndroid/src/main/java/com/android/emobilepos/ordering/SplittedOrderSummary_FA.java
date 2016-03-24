package com.android.emobilepos.ordering;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.FragmentTransaction;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.database.OrderProductsAttr_DB;
import com.android.database.OrderProductsHandler;
import com.android.database.OrderTaxes_DB;
import com.android.database.OrdersHandler;
import com.android.database.ProductsHandler;
import com.android.database.TaxesHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.adapters.GiftLoyaltyRewardLV_Adapter;
import com.android.emobilepos.adapters.OrderProductListAdapter;
import com.android.emobilepos.adapters.SplittedOrderSummaryAdapter;
import com.android.emobilepos.models.Discount;
import com.android.emobilepos.models.Order;
import com.android.emobilepos.models.OrderProduct;
import com.android.emobilepos.models.OrderSeatProduct;
import com.android.emobilepos.models.SplitedOrder;
import com.android.emobilepos.models.Tax;
import com.android.emobilepos.payment.SelectPayMethod_FA;
import com.android.support.GenerateNewID;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.Post;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Guarionex on 2/8/2016.
 */
public class SplittedOrderSummary_FA extends BaseFragmentActivityActionBar implements AdapterView.OnItemSelectedListener {

    private Global global;
    List<OrderSeatProduct> orderSeatProducts;
    Spinner splitTypeSpinner;
    private String tableNumber;
    private SplittedOrderSummaryFR orderSummaryFR;
    private SplittedOrderDetailsFR orderDetailsFR;
    private String taxID;
    private Discount discount;
    private BigDecimal discountableSubtotal;
    private BigDecimal itemsDiscountTotal;
    private List<HashMap<String, String>> listMapTaxes;
    private Tax tax;
    public int checkoutCount = 0;
    private BigDecimal globalDiscountPercentge = new BigDecimal(0);
    private BigDecimal globalDiscountAmount = new BigDecimal(0);

    MyPreferences preferences;
    GenerateNewID generateNewID;
    public SalesReceiptSplitTypes splitType;


    public enum NavigationResult {
        PAYMENT_COMPLETED(-1), PAYMENT_SELECTION_VOID(3), BACK_SELECT_PAYMENT(1901), PARTIAL_PAYMENT(1902), VOID_HOLD_TRANSACTION(1903);
        int code;

        NavigationResult(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }

    public SplittedOrderDetailsFR getOrderDetailsFR() {
        return orderDetailsFR;
    }

    public void setOrderDetailsFR(SplittedOrderDetailsFR orderDetailsFR) {
        this.orderDetailsFR = orderDetailsFR;
    }

    public SplittedOrderSummaryFR getOrderSummaryFR() {
        return orderSummaryFR;
    }

    public void setOrderSummaryFR(SplittedOrderSummaryFR orderSummaryFR) {
        this.orderSummaryFR = orderSummaryFR;
    }


    public enum SalesReceiptSplitTypes {
        SPLIT_BY_SEATS(0), SPLIT_EQUALLY(1), SPLIT_SINGLE(2); //, SPLIT_BY_SEAT_GROUP(2);
        private int code;

        SalesReceiptSplitTypes(int code) {
            this.code = code;
        }

        public static SalesReceiptSplitTypes getSpinnerSelection(String name) {
            return valueOf(name.toUpperCase());
        }

        public static SalesReceiptSplitTypes getByCode(int code) {
            switch (code) {
                case 0:
                    return SPLIT_BY_SEATS;
                case 1:
                    return SPLIT_EQUALLY;
                case 2:
                    return SPLIT_SINGLE;
                default:
                    return SPLIT_BY_SEATS;
            }
        }

        public int getByCode() {
            return code;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splitted_order_summary);
        Bundle extras = this.getIntent().getExtras();
        preferences = new MyPreferences(this);
        generateNewID = new GenerateNewID(this);
        Gson gson = new Gson();
        if (extras != null) {
            Type listType = new TypeToken<List<OrderSeatProduct>>() {
            }.getType();
            String json = extras.getString("orderSeatProductList");
            tableNumber = extras.getString("tableNumber");
            orderSeatProducts = gson.fromJson(json, listType);
            taxID = extras.getString("taxID");
            String ordetTaxId = extras.getString("orderTaxId");
            int discountSelected = extras.getInt("discountSelected");
            TaxesHandler taxesHandler = new TaxesHandler(this);
            List<Tax> taxList = taxesHandler.getTaxes();
            int idx = taxList.indexOf(new Tax(ordetTaxId));
            tax = taxList.get(idx);

            ProductsHandler handler2 = new ProductsHandler(this);
            List<Discount> discountList = handler2.getDiscounts();
            if (discountSelected >= 0) {
                discount = discountList.get(discountSelected);
            } else {
                discount = Discount.getDefaultInstance();
            }
        }
        splitTypeSpinner = (Spinner) findViewById(R.id.splitTypesSpinner);
        splitTypeSpinner.setOnItemSelectedListener(this);
        global = (Global) getApplication();
        setOrderSummaryFR(new SplittedOrderSummaryFR());
        setOrderDetailsFR(new SplittedOrderDetailsFR());
        if (global.order.ord_discount != null && !global.order.ord_discount.isEmpty()) {
            globalDiscountAmount = Global.getBigDecimalNum(global.order.ord_discount);
            setGlobalDiscountPercentge(new BigDecimal(global.order.ord_discount).setScale(4, RoundingMode.HALF_UP)
                    .divide(new BigDecimal(global.order.ord_subtotal).setScale(4, RoundingMode.HALF_UP), 6, RoundingMode.HALF_UP)
                    .setScale(6, RoundingMode.HALF_UP));
        }
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.splitedOrderSummaryFrameLayout, getOrderSummaryFR());
        ft.add(R.id.splitedOrderDetailFrameLayout, getOrderDetailsFR());
        ft.commit();
    }

    private List<OrderProduct> getProductsBySeats(String seatNumber) {
        List<OrderProduct> seatProducts = new ArrayList<OrderProduct>();
        for (OrderSeatProduct product : orderSeatProducts) {
            if (product.rowType == OrderProductListAdapter.RowType.TYPE_ITEM && product.orderProduct.assignedSeat.equalsIgnoreCase(seatNumber)) {
                try {
                    seatProducts.add((OrderProduct) product.orderProduct.clone());
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
            }
        }
        return seatProducts;
    }

    private List<OrderProduct> getProductsBySeatsGroup(int groupId) {
        List<OrderProduct> seatProducts = new ArrayList<OrderProduct>();
        for (OrderSeatProduct product : orderSeatProducts) {
            if (product.rowType == OrderProductListAdapter.RowType.TYPE_ITEM && product.getSeatGroupId() == groupId) {
                try {
                    seatProducts.add((OrderProduct) product.orderProduct.clone());
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
            }
        }
        return seatProducts;
    }

    private List<OrderProduct> getProductsSingleReceipt() {
        List<OrderProduct> seatProducts = new ArrayList<OrderProduct>();
        for (OrderSeatProduct product : orderSeatProducts) {
            if (product.rowType == OrderProductListAdapter.RowType.TYPE_ITEM) {
                try {
                    seatProducts.add((OrderProduct) product.orderProduct.clone());
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
            }
        }
        return seatProducts;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        int code = Long.valueOf(position).intValue();
        final List<SplitedOrder> splitedOrders = new ArrayList<SplitedOrder>();
        splitType = SalesReceiptSplitTypes.getByCode(code);
        switch (splitType) {
            case SPLIT_SINGLE: {
                String nextID = preferences.getLastOrdID();
                for (OrderSeatProduct seatProduct : orderSeatProducts) {
                    if (seatProduct.rowType == OrderProductListAdapter.RowType.TYPE_HEADER) {
                        Order order = null;
                        try {
                            order = (Order) global.order.clone();

                        } catch (CloneNotSupportedException e) {
                            e.printStackTrace();
                        }
                        SplitedOrder splitedOrder;
                        splitedOrder = new SplitedOrder(this, order);
                        nextID = generateNewID.getNextID(nextID);
                        splitedOrder.ord_id = nextID;
                        List<OrderProduct> orderProducts = getProductsSingleReceipt();
                        BigDecimal orderSubTotal = new BigDecimal(0);
                        for (OrderProduct product : orderProducts) {
                            orderSubTotal = orderSubTotal.add(new BigDecimal(product.itemSubtotal)).setScale(4, RoundingMode.HALF_UP);
                        }
                        splitedOrder.ord_subtotal = orderSubTotal.toString();
                        splitedOrder.ord_total = orderSubTotal.subtract(globalDiscountAmount).toString();
                        splitedOrder.setOrderProducts(orderProducts);
                        splitedOrder.setTableNumber(tableNumber);
                        splitedOrders.add(splitedOrder);
                        break;
                    }
                }
                SplittedOrderSummaryAdapter summaryAdapter = new SplittedOrderSummaryAdapter(this, splitedOrders);
                getOrderSummaryFR().getGridView().setAdapter(summaryAdapter);
                break;
            }
            case SPLIT_EQUALLY:
                PopupMenu popup = new PopupMenu(this, view);
                for (int i = 0; i < 20; i++) {
                    popup.getMenu().add(0, i + 1, Menu.NONE, String.valueOf(i + 1));
                }
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int splitQty = item.getItemId();
                        String nextID = preferences.getLastOrdID();

                        for (OrderSeatProduct seatProduct : orderSeatProducts) {
                            if (seatProduct.rowType == OrderProductListAdapter.RowType.TYPE_HEADER) {
                                Order order = null;
                                try {
                                    order = (Order) global.order.clone();

                                } catch (CloneNotSupportedException e) {
                                    e.printStackTrace();
                                }
                                for (int i = 0; i < splitQty; i++) {
                                    SplitedOrder splitedOrder = new SplitedOrder(SplittedOrderSummary_FA.this, order);
                                    nextID = generateNewID.getNextID(nextID);
                                    splitedOrder.ord_id = nextID;
                                    List<OrderProduct> orderProducts = getProductsSingleReceipt();
                                    BigDecimal orderSubTotal = new BigDecimal(0);
                                    for (OrderProduct product : orderProducts) {
                                        BigDecimal itemSubtotal = new BigDecimal(product.itemSubtotal);
                                        itemSubtotal = itemSubtotal.divide(new BigDecimal(splitQty), 4, RoundingMode.HALF_UP);

                                        product.itemSubtotal = itemSubtotal.toString();
                                        product.overwrite_price = Global.getBigDecimalNum(product.overwrite_price).divide(new BigDecimal(splitQty), 4, RoundingMode.HALF_UP).toString();
                                        product.taxTotal = Global.getBigDecimalNum(product.taxTotal).divide(new BigDecimal(splitQty), 4, RoundingMode.HALF_UP).toString();
                                        product.discount_value = Global.getBigDecimalNum(product.discount_value).divide(new BigDecimal(splitQty), 4, RoundingMode.HALF_UP).toString();
                                        product.itemTotal = Global.getBigDecimalNum(product.itemTotal).divide(new BigDecimal(splitQty), 4, RoundingMode.HALF_UP).toString();

                                        orderSubTotal = orderSubTotal.add(itemSubtotal).setScale(4, RoundingMode.HALF_UP);
                                    }
                                    splitedOrder.ord_subtotal = orderSubTotal.toString();
                                    splitedOrder.ord_total = orderSubTotal.subtract(orderSubTotal.multiply(globalDiscountPercentge)).toString();

                                    splitedOrder.setOrderProducts(orderProducts);
                                    splitedOrder.setTableNumber(tableNumber);
                                    splitedOrders.add(splitedOrder);
                                }
                                break;
                            }
                        }
                        SplittedOrderSummaryAdapter summaryAdapter = new SplittedOrderSummaryAdapter(SplittedOrderSummary_FA.this, splitedOrders);
                        getOrderSummaryFR().getGridView().setAdapter(summaryAdapter);
                        setReceiptPreview();
                        return true;
                    }
                });
                popup.show();
                break;
            case SPLIT_BY_SEATS: {
                HashSet<Integer> joinedGroupIds = new HashSet<Integer>();
                String nextID = preferences.getLastOrdID();

                for (OrderSeatProduct seatProduct : orderSeatProducts) {
                    if (seatProduct.rowType == OrderProductListAdapter.RowType.TYPE_HEADER &&
                            !joinedGroupIds.contains(seatProduct.getSeatGroupId())) {
                        Order order = null;
                        try {
                            order = (Order) global.order.clone();

                        } catch (CloneNotSupportedException e) {
                            e.printStackTrace();
                        }
                        SplitedOrder splitedOrder = new SplitedOrder(this, order);
                        nextID = generateNewID.getNextID(nextID);
                        splitedOrder.ord_id = nextID;
                        List<OrderProduct> orderProducts = getProductsBySeatsGroup(seatProduct.getSeatGroupId());
                        BigDecimal orderSubTotal = new BigDecimal(0);
                        for (OrderProduct product : orderProducts) {
                            orderSubTotal = orderSubTotal.add(new BigDecimal(product.itemSubtotal)).setScale(4, RoundingMode.HALF_UP);
                        }
                        splitedOrder.ord_subtotal = orderSubTotal.toString();
                        splitedOrder.ord_total = orderSubTotal.subtract(orderSubTotal.multiply(globalDiscountPercentge)).toString();

                        splitedOrder.setOrderProducts(orderProducts);
                        splitedOrder.setTableNumber(tableNumber);
                        if (!splitedOrder.getOrderProducts().isEmpty()) {
                            splitedOrders.add(splitedOrder);
                        }
                        joinedGroupIds.add(seatProduct.getSeatGroupId());
                    }
                }
                SplittedOrderSummaryAdapter summaryAdapter = new SplittedOrderSummaryAdapter(this, splitedOrders);
                getOrderSummaryFR().getGridView().setAdapter(summaryAdapter);
                break;
            }
        }
        setReceiptPreview();
    }

    public BigDecimal getGlobalDiscountPercentge() {
        return globalDiscountPercentge;
    }

    public void setGlobalDiscountPercentge(BigDecimal globalDiscountPercentge) {
        this.globalDiscountPercentge = globalDiscountPercentge;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void setReceiptPreview() {
        SplitedOrder splitedOrder = null;
        try {
            splitedOrder = (SplitedOrder) ((SplitedOrder) getOrderSummaryFR().getGridView().getAdapter().getItem(0)).clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        getOrderDetailsFR().setReceiptOrder(splitedOrder);
    }

    @Override
    public void onResume() {

        if (global.isApplicationSentToBackground(this))
            global.loggedIn = false;
        global.stopActivityTransitionTimer();

        if (!global.loggedIn) {
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
    public void onBackPressed() {
        if (splitType == SalesReceiptSplitTypes.SPLIT_EQUALLY && checkoutCount > 0) {
            promptVoidTransaction(true);

        } else {
            setResult(0);
            finish();
        }
    }

    public void promptVoidTransaction(final boolean voidPayments) {
        final Dialog dialog = new Dialog(this, R.style.Theme_TransparentTest);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.void_dialog_layout);
        Button voidBut = (Button) dialog.findViewById(R.id.voidBut);
        Button notVoid = (Button) dialog.findViewById(R.id.notVoidBut);

        voidBut.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                MyPreferences myPref = new MyPreferences(SplittedOrderSummary_FA.this);
                if (myPref.getPreferences(MyPreferences.pref_require_manager_pass_to_void_trans)) {
                    dialog.dismiss();
                    promptManagerPassword(voidPayments);
                } else {
                    dialog.dismiss();
                    voidTransaction(voidPayments);
                }
            }
        });
        notVoid.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void promptManagerPassword(final boolean voidPayments) {
        final Dialog globalDlog = new Dialog(this, R.style.Theme_TransparentTest);
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
                MyPreferences myPref = new MyPreferences(SplittedOrderSummary_FA.this);
                String pass = viewField.getText().toString();
                if (!pass.isEmpty() && myPref.posManagerPass(true, null).equals(pass.trim())) {
                    voidTransaction(voidPayments);
                } else {
                    promptManagerPassword(voidPayments);
                }
            }
        });
        globalDlog.show();
    }

    public void voidTransaction(boolean voidPayments) {
        if (voidPayments) {
            SelectPayMethod_FA.voidTransaction(SplittedOrderSummary_FA.this, global.order.ord_id, global.order.ord_type);
        }

        OrdersHandler ordersHandler = new OrdersHandler(SplittedOrderSummary_FA.this);
        OrderProductsHandler orderProductsHandler = new OrderProductsHandler(SplittedOrderSummary_FA.this);
        OrderProductsAttr_DB productsAttrDb = new OrderProductsAttr_DB(SplittedOrderSummary_FA.this);
        OrderTaxes_DB ordTaxesDB = new OrderTaxes_DB();
        ordersHandler.updateFinishOnHold(global.order.ord_id);
        global.order.isVoid = "1";
        global.order.processed = "9";
        global.order.isOnHold = "0";
        global.orderProducts = orderDetailsFR.restaurantSplitedOrder.getOrderProducts();
        ordersHandler.insert(global.order);
        global.encodedImage = "";
        orderProductsHandler.insert(global.orderProducts);
        productsAttrDb.insert(global.ordProdAttr);
        if (global.listOrderTaxes != null && global.listOrderTaxes.size() > 0) {
            ordTaxesDB.insert(global.listOrderTaxes, global.order.ord_id);
        }
        new VoidTransaction().execute(global.order.ord_id);
    }

    private class VoidTransaction extends AsyncTask<String, Void, Void> {

        private ProgressDialog myProgressDialog;

        @Override
        protected void onPreExecute() {
            myProgressDialog = new ProgressDialog(SplittedOrderSummary_FA.this);
            myProgressDialog.setMessage("Sending...");
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.setCancelable(false);
            if (myProgressDialog.isShowing())
                myProgressDialog.dismiss();

        }

        @Override
        protected Void doInBackground(String... params) {
            Post httpClient = new Post();
            httpClient.postData(Global.S_CHECKOUT_ON_HOLD, SplittedOrderSummary_FA.this, params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            myProgressDialog.dismiss();
            setResult(-1);
            finish();
        }

    }
}
