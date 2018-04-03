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

import com.android.dao.DinningTableOrderDAO;
import com.android.database.OrderProductsAttr_DB;
import com.android.database.OrderProductsHandler;
import com.android.database.OrderTaxes_DB;
import com.android.database.OrdersHandler;
import com.android.database.ProductsHandler;
import com.android.database.TaxesHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.adapters.OrderProductListAdapter;
import com.android.emobilepos.adapters.SplittedOrderSummaryAdapter;
import com.android.emobilepos.models.Discount;
import com.android.emobilepos.models.OrderSeatProduct;
import com.android.emobilepos.models.SplittedOrder;
import com.android.emobilepos.models.Tax;
import com.android.emobilepos.models.orders.Order;
import com.android.emobilepos.models.orders.OrderProduct;
import com.android.emobilepos.payment.SelectPayMethod_FA;
import com.android.emobilepos.security.SecurityManager;
import com.android.support.GenerateNewID;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.Post;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;
import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import util.json.JsonUtils;

/**
 * Created by Guarionex on 2/8/2016.
 */
public class SplittedOrderSummary_FA extends BaseFragmentActivityActionBar implements AdapterView.OnItemSelectedListener, View.OnClickListener {

    public int checkoutCount = 0;
    public SalesReceiptSplitTypes splitType;
    List<OrderSeatProduct> orderSeatProducts;
    Spinner splitTypeSpinner;
    MyPreferences preferences;
    GenerateNewID generateNewID;
    private Global global;
    private String tableNumber;
    private SplittedOrderSummaryFR orderSummaryFR;
    private SplittedOrderDetailsFR orderDetailsFR;
    private String taxID;
    private Discount discount;
    private Tax tax;
    private BigDecimal globalDiscountPercentge = new BigDecimal(0);
    private BigDecimal globalDiscountAmount = new BigDecimal(0);
    private Button splitEquallyQtyBtn;
    List<SplittedOrder> calculatedSplitedOrders = new ArrayList<>();
    public Global.TransactionType transType;


    public String getTaxID() {
        return taxID;
    }

    public void setTaxID(String taxID) {
        this.taxID = taxID;
    }

    public Tax getTax() {
        return tax;
    }

    public void setTax(Tax tax) {
        this.tax = tax;
    }

    public Discount getDiscount() {
        return discount;
    }

    public void setDiscount(Discount discount) {
        this.discount = discount;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.splitEquallyQtyeditButton: {
                PopupMenu popup = new PopupMenu(this, splitEquallyQtyBtn);
                for (int i = 0; i < 20; i++) {
                    popup.getMenu().add(0, i + 1, Menu.NONE, String.valueOf(i + 1));
                }
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int splitQty = item.getItemId();
                        splitEquallyQtyBtn.setText(String.valueOf(splitQty));
//                        setSplitEquallyReceipt(splitQty);
                        new SplitOrderTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, splitQty);
                        return true;
                    }
                });
                popup.show();

                break;
            }
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splitted_order_summary);
        Bundle extras = this.getIntent().getExtras();
        preferences = new MyPreferences(this);
        generateNewID = new GenerateNewID(this);

        Gson gson = JsonUtils.getInstance();
        if (extras != null) {
            transType = (Global.TransactionType) extras.get("transType");
            Type listType = new TypeToken<List<OrderSeatProduct>>() {
            }.getType();
            String json = extras.getString("orderSeatProductList");
            tableNumber = extras.getString("tableNumber");
            orderSeatProducts = gson.fromJson(json, listType);
            setTaxID(extras.getString("taxID"));
            String ordetTaxId = extras.getString("orderTaxId");
            int discountSelected = extras.getInt("discountSelected");
            TaxesHandler taxesHandler = new TaxesHandler(this);
            List<Tax> taxList = taxesHandler.getProductTaxes(preferences.getPreferences(MyPreferences.pref_show_only_group_taxes));
            int idx = taxList.indexOf(new Tax(ordetTaxId));
            setTax(idx == -1 ? null : taxList.get(idx));
            ProductsHandler handler2 = new ProductsHandler(this);
            List<Discount> discountList = handler2.getDiscounts();
            if (discountSelected >= 0) {
                setDiscount(discountList.get(discountSelected));
            } else {
                setDiscount(Discount.getDefaultInstance());
            }
        }
        boolean hasPermissions = SecurityManager.hasPermissions(this, SecurityManager.SecurityAction.SPLIT_ORDER);
        splitTypeSpinner = findViewById(R.id.splitTypesSpinner);
        splitTypeSpinner.setOnItemSelectedListener(this);
        splitTypeSpinner.setEnabled(hasPermissions);
        splitEquallyQtyBtn = findViewById(R.id.splitEquallyQtyeditButton);
        splitEquallyQtyBtn.setVisibility(View.GONE);
        splitEquallyQtyBtn.setOnClickListener(this);
        global = (Global) getApplication();
        setOrderSummaryFR(new SplittedOrderSummaryFR());
        setOrderDetailsFR(new SplittedOrderDetailsFR());
        if (global.order != null && global.order.ord_discount != null && !global.order.ord_discount.isEmpty()) {
            globalDiscountAmount = Global.getBigDecimalNum(global.order.ord_discount);
            if (Double.parseDouble(global.order.ord_subtotal) == 0) {
                setGlobalDiscountPercentge(new BigDecimal(0));
            } else {
                setGlobalDiscountPercentge(new BigDecimal(global.order.ord_discount).setScale(4, RoundingMode.HALF_UP)
                        .divide(new BigDecimal(global.order.ord_subtotal).setScale(4, RoundingMode.HALF_UP), 6, RoundingMode.HALF_UP)
                        .setScale(6, RoundingMode.HALF_UP));
            }
        }

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.splitedOrderSummaryFrameLayout, getOrderSummaryFR());
        ft.add(R.id.splitedOrderDetailFrameLayout, getOrderDetailsFR());
        ft.commit();
    }

    private List<OrderProduct> getProductsBySeats(String seatNumber) {
        List<OrderProduct> seatProducts = new ArrayList<>();
        for (OrderSeatProduct product : orderSeatProducts) {
            if (product.rowType == OrderProductListAdapter.RowType.TYPE_ITEM && product.orderProduct.getAssignedSeat().equalsIgnoreCase(seatNumber)) {
                try {
                    seatProducts.add((OrderProduct) product.orderProduct.clone());
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                    Crashlytics.logException(e);
                }
            }
        }
        return seatProducts;
    }

    private List<OrderProduct> getProductsBySeatsGroup(int groupId) {
        List<OrderProduct> seatProducts = new ArrayList<>();
        for (OrderSeatProduct product : orderSeatProducts) {
            if (product.rowType == OrderProductListAdapter.RowType.TYPE_ITEM && product.getSeatGroupId() == groupId) {
                try {
                    seatProducts.add((OrderProduct) product.orderProduct.clone());
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                    Crashlytics.logException(e);
                }
            }
        }
        return seatProducts;
    }

    private List<OrderProduct> getProductsSingleReceipt() {
        List<OrderProduct> seatProducts = new ArrayList<>();
        for (OrderSeatProduct product : orderSeatProducts) {
            if (product.rowType == OrderProductListAdapter.RowType.TYPE_ITEM) {
                try {
                    seatProducts.add((OrderProduct) product.orderProduct.clone());
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                    Crashlytics.logException(e);
                }
            }
        }
        return seatProducts;
    }

    private class SplitOrderTask extends AsyncTask<Integer, Void, SplittedOrderSummaryAdapter> {
        List<SplittedOrder> splitedOrders = new ArrayList<>();
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(SplittedOrderSummary_FA.this);
            dialog.setMessage(getString(R.string.processing_preview));
            dialog.setIndeterminate(true);
            dialog.show();
        }

        @Override
        protected SplittedOrderSummaryAdapter doInBackground(Integer... params) {
            SplittedOrderSummaryAdapter summaryAdapter = null;
            switch (splitType) {
                case SPLIT_SINGLE: {
                    for (OrderSeatProduct seatProduct : orderSeatProducts) {
                        if (seatProduct.rowType == OrderProductListAdapter.RowType.TYPE_HEADER) {
                            Order order = null;
                            try {
                                order = (Order) global.order.clone();
                            } catch (CloneNotSupportedException e) {
                                e.printStackTrace();
                                Crashlytics.logException(e);
                            }
                            SplittedOrder splitedOrder;
                            splitedOrder = new SplittedOrder(SplittedOrderSummary_FA.this, order);
                            List<OrderProduct> orderProducts = getProductsSingleReceipt();
                            BigDecimal orderSubTotal = new BigDecimal(0);
                            for (OrderProduct product : orderProducts) {
                                orderSubTotal = orderSubTotal.add(product.getItemSubtotalCalculated()
                                        .add(product.getAddonsTotalPrice())).setScale(4, RoundingMode.HALF_UP);
                            }
                            splitedOrder.ord_subtotal = orderSubTotal.toString();
                            splitedOrder.ord_total = orderSubTotal.subtract(globalDiscountAmount).toString();
                            splitedOrder.setOrderProducts(orderProducts);
                            splitedOrder.setTableNumber(tableNumber);
                            splitedOrders.add(splitedOrder);
                            break;
                        }
                    }
                    summaryAdapter = new SplittedOrderSummaryAdapter(SplittedOrderSummary_FA.this, splitedOrders);
                    break;
                }
                case SPLIT_EQUALLY: {
                    String nextID = null;
                    int splitQty = params[0];
                    for (OrderSeatProduct seatProduct : orderSeatProducts) {
                        if (seatProduct.rowType == OrderProductListAdapter.RowType.TYPE_HEADER) {
                            Order order = null;
                            try {
                                order = (Order) global.order.clone();
                            } catch (CloneNotSupportedException e) {
                                e.printStackTrace();
                                Crashlytics.logException(e);
                            }

                            for (int i = 0; i < splitQty; i++) {
                                SplittedOrder splitedOrder = new SplittedOrder(SplittedOrderSummary_FA.this, order);
                                if (i == 0) {
                                    nextID = order.ord_id;
                                } else if (i == 1) {
                                    nextID = generateNewID.getNextID(GenerateNewID.IdType.ORDER_ID);
                                } else {
                                    nextID = generateNewID.getNextID(nextID);
                                }
                                splitedOrder.ord_id = nextID;
                                List<OrderProduct> orderProducts = getProductsSingleReceipt();
                                BigDecimal orderSubTotal = new BigDecimal(0);
                                for (OrderProduct product : orderProducts) {
                                    BigDecimal itemSubtotal = new BigDecimal(product.getFinalPrice()).add(product.getAddonsTotalPrice());
                                    itemSubtotal = itemSubtotal.divide(new BigDecimal(splitQty), 4, RoundingMode.HALF_UP);
                                    for (OrderProduct addon : product.addonsProducts) {
                                        addon.setProd_price(new BigDecimal(addon.getProd_price()).divide(new BigDecimal(splitQty), 4, RoundingMode.HALF_UP).toString());
                                        addon.setOverwrite_price(new BigDecimal(addon.getProd_price()));
                                        addon.setItemTotal(Global.getBigDecimalNum(addon.getItemTotal()).divide(new BigDecimal(splitQty), 4, RoundingMode.HALF_UP).toString());
                                    }
                                    product.setProd_price(new BigDecimal(product.getFinalPrice()).divide(new BigDecimal(splitQty), 4, RoundingMode.HALF_UP).toString());
                                    product.setOverwrite_price(new BigDecimal(product.getProd_price()));
                                    product.setProd_taxValue(product.getProd_taxValue().divide(new BigDecimal(splitQty), 4, RoundingMode.HALF_UP));
                                    product.setDiscount_value(Global.getBigDecimalNum(product.getDiscount_value()).divide(new BigDecimal(splitQty), 4, RoundingMode.HALF_UP).toString());
                                    product.setItemTotal(Global.getBigDecimalNum(product.getItemTotal()).divide(new BigDecimal(splitQty), 4, RoundingMode.HALF_UP).toString());
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
                    summaryAdapter = new SplittedOrderSummaryAdapter(SplittedOrderSummary_FA.this, splitedOrders);
                    break;
                }
                case SPLIT_BY_SEATS: {
                    HashSet<Integer> joinedGroupIds = new HashSet<>();
                    String nextID = null;
                    int i = 0;
                    for (OrderSeatProduct seatProduct : orderSeatProducts) {
                        if (seatProduct.rowType == OrderProductListAdapter.RowType.TYPE_HEADER &&
                                !joinedGroupIds.contains(seatProduct.getSeatGroupId())) {
                            Order order = null;
                            try {
                                order = (Order) global.order.clone();
                            } catch (CloneNotSupportedException e) {
                                e.printStackTrace();
                                Crashlytics.logException(e);
                            }
                            SplittedOrder splitedOrder = new SplittedOrder(SplittedOrderSummary_FA.this, order);
                            if (i == 0) {
                                nextID = order.ord_id;
                            } else if (i == 1) {
                                nextID = generateNewID.getNextID(GenerateNewID.IdType.ORDER_ID);
                            } else {
                                nextID = generateNewID.getNextID(nextID);
                            }
                            splitedOrder.ord_id = nextID;
                            List<OrderProduct> orderProducts = getProductsBySeatsGroup(seatProduct.getSeatGroupId());
                            BigDecimal orderSubTotal = new BigDecimal(0);
                            for (OrderProduct product : orderProducts) {
                                orderSubTotal = orderSubTotal.add(product.getItemSubtotalCalculated().add(product.getAddonsTotalPrice())).setScale(4, RoundingMode.HALF_UP);
                            }
                            splitedOrder.ord_subtotal = orderSubTotal.toString();
                            splitedOrder.ord_total = orderSubTotal.subtract(orderSubTotal.multiply(globalDiscountPercentge)).toString();

                            splitedOrder.setOrderProducts(orderProducts);
                            splitedOrder.setTableNumber(tableNumber);
                            if (!splitedOrder.getOrderProducts().isEmpty()) {
                                splitedOrders.add(splitedOrder);
                            }
                            joinedGroupIds.add(seatProduct.getSeatGroupId());
                            i++;
                        }
                    }
                    summaryAdapter = new SplittedOrderSummaryAdapter(SplittedOrderSummary_FA.this, splitedOrders);
                    break;
                }
            }
            calculatedSplitedOrders = new ArrayList<>(splitedOrders);
            calculateSplitedOrder(calculatedSplitedOrders);
            return summaryAdapter;
        }

        @Override
        protected void onPostExecute(SplittedOrderSummaryAdapter summaryAdapter) {
            super.onPostExecute(summaryAdapter);
            getOrderSummaryFR().getGridView().setAdapter(summaryAdapter);
            setReceiptPreview();
            dialog.dismiss();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        int code = Long.valueOf(position).intValue();
        splitType = SalesReceiptSplitTypes.getByCode(code);
        splitEquallyQtyBtn.setVisibility(View.GONE);

        switch (splitType) {
            case SPLIT_SINGLE:
            case SPLIT_BY_SEATS:
                new SplitOrderTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                break;
            case SPLIT_EQUALLY:
                splitEquallyQtyBtn.setVisibility(View.VISIBLE);
                PopupMenu popup = new PopupMenu(this, splitEquallyQtyBtn);
                for (int i = 0; i < 20; i++) {
                    popup.getMenu().add(0, i + 1, Menu.NONE, String.valueOf(i + 1));
                }
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int splitQty = item.getItemId();
                        splitEquallyQtyBtn.setText(String.valueOf(splitQty));
//                        setSplitEquallyReceipt(splitQty);
                        new SplitOrderTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, splitQty);
                        return true;
                    }
                });
                popup.show();
                break;
        }
//            case SPLIT_SINGLE: {
//                for (OrderSeatProduct seatProduct : orderSeatProducts) {
//                    if (seatProduct.rowType == OrderProductListAdapter.RowType.TYPE_HEADER) {
//                        Order order = null;
//                        try {
//                            order = (Order) global.order.clone();
//                        } catch (CloneNotSupportedException e) {
//                            e.printStackTrace();
//                            Crashlytics.logException(e);
//                        }
//                        SplittedOrder splitedOrder;
//                        splitedOrder = new SplittedOrder(this, order);
//                        List<OrderProduct> orderProducts = getProductsSingleReceipt();
//                        BigDecimal orderSubTotal = new BigDecimal(0);
//                        for (OrderProduct product : orderProducts) {
//                            orderSubTotal = orderSubTotal.add(product.getItemSubtotalCalculated()
//                                    .add(product.getAddonsTotalPrice())).setScale(4, RoundingMode.HALF_UP);
//                        }
//                        splitedOrder.ord_subtotal = orderSubTotal.toString();
//                        splitedOrder.ord_total = orderSubTotal.subtract(globalDiscountAmount).toString();
//                        splitedOrder.setOrderProducts(orderProducts);
//                        splitedOrder.setTableNumber(tableNumber);
//                        splitedOrders.add(splitedOrder);
//                        break;
//                    }
//                }
//                SplittedOrderSummaryAdapter summaryAdapter = new SplittedOrderSummaryAdapter(this, splitedOrders);
//                getOrderSummaryFR().getGridView().setAdapter(summaryAdapter);
//                break;
//            }
//            case SPLIT_EQUALLY:
//                splitEquallyQtyBtn.setVisibility(View.VISIBLE);
//                PopupMenu popup = new PopupMenu(this, splitEquallyQtyBtn);
//                for (int i = 0; i < 20; i++) {
//                    popup.getMenu().add(0, i + 1, Menu.NONE, String.valueOf(i + 1));
//                }
//                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//                    @Override
//                    public boolean onMenuItemClick(MenuItem item) {
//                        int splitQty = item.getItemId();
//                        splitEquallyQtyBtn.setText(String.valueOf(splitQty));
//                        setSplitEquallyReceipt(splitQty);
//                        return true;
//                    }
//                });
//                popup.show();
//                break;
//            case SPLIT_BY_SEATS: {
//                HashSet<Integer> joinedGroupIds = new HashSet<>();
//                String nextID = null;// = assignEmployee.getMSLastOrderID();
//                int i = 0;
//                for (OrderSeatProduct seatProduct : orderSeatProducts) {
//                    if (seatProduct.rowType == OrderProductListAdapter.RowType.TYPE_HEADER &&
//                            !joinedGroupIds.contains(seatProduct.getSeatGroupId())) {
//                        Order order = null;
//                        try {
//                            order = (Order) global.order.clone();
//                        } catch (CloneNotSupportedException e) {
//                            e.printStackTrace();
//                            Crashlytics.logException(e);
//                        }
//                        SplittedOrder splitedOrder = new SplittedOrder(this, order);
//                        if (i == 0) {
//                            nextID = order.ord_id;
//                        } else if (i == 1) {
//                            nextID = generateNewID.getNextID(GenerateNewID.IdType.ORDER_ID);
//                        } else {
//                            nextID = generateNewID.getNextID(nextID);
//                        }
//                        splitedOrder.ord_id = nextID;
//                        List<OrderProduct> orderProducts = getProductsBySeatsGroup(seatProduct.getSeatGroupId());
//                        BigDecimal orderSubTotal = new BigDecimal(0);
//                        for (OrderProduct product : orderProducts) {
//                            orderSubTotal = orderSubTotal.add(product.getItemSubtotalCalculated().add(product.getAddonsTotalPrice())).setScale(4, RoundingMode.HALF_UP);
//                        }
//                        splitedOrder.ord_subtotal = orderSubTotal.toString();
//                        splitedOrder.ord_total = orderSubTotal.subtract(orderSubTotal.multiply(globalDiscountPercentge)).toString();
//
//                        splitedOrder.setOrderProducts(orderProducts);
//                        splitedOrder.setTableNumber(tableNumber);
//                        if (!splitedOrder.getOrderProducts().isEmpty()) {
//                            splitedOrders.add(splitedOrder);
//                        }
//                        joinedGroupIds.add(seatProduct.getSeatGroupId());
//                        i++;
//                    }
//                }
//                SplittedOrderSummaryAdapter summaryAdapter = new SplittedOrderSummaryAdapter(this, splitedOrders);
//                getOrderSummaryFR().getGridView().setAdapter(summaryAdapter);
//                break;
//            }
//        }
//        calculatedSplitedOrders = new ArrayList<>(splitedOrders);
//        calculateSplitedOrder(calculatedSplitedOrders);
//        setReceiptPreview();
    }


//    private void setSplitEquallyReceipt(int splitQty) {
//        String nextID = null;
//        final List<SplittedOrder> splitedOrders = new ArrayList<>();
//        for (OrderSeatProduct seatProduct : orderSeatProducts) {
//            if (seatProduct.rowType == OrderProductListAdapter.RowType.TYPE_HEADER) {
//                Order order = null;
//                try {
//                    order = (Order) global.order.clone();
//                } catch (CloneNotSupportedException e) {
//                    e.printStackTrace();
//                    Crashlytics.logException(e);
//                }
//                for (int i = 0; i < splitQty; i++) {
//                    SplittedOrder splitedOrder = new SplittedOrder(SplittedOrderSummary_FA.this, order);
//                    if (i == 0) {
//                        nextID = order.ord_id;
//                    } else if (i == 1) {
//                        nextID = generateNewID.getNextID(GenerateNewID.IdType.ORDER_ID);
//                    } else {
//                        nextID = generateNewID.getNextID(nextID);
//                    }
//                    splitedOrder.ord_id = nextID;
//                    List<OrderProduct> orderProducts = getProductsSingleReceipt();
//                    BigDecimal orderSubTotal = new BigDecimal(0);
//                    for (OrderProduct product : orderProducts) {
//                        BigDecimal itemSubtotal = new BigDecimal(product.getFinalPrice()).add(product.getAddonsTotalPrice());
//                        itemSubtotal = itemSubtotal.divide(new BigDecimal(splitQty), 4, RoundingMode.HALF_UP);
////                        product.setItemSubtotal(itemSubtotal.toString());
//                        for (OrderProduct addon : product.addonsProducts) {
//                            addon.setProd_price(new BigDecimal(addon.getProd_price()).divide(new BigDecimal(splitQty), 4, RoundingMode.HALF_UP).toString());
//                            addon.setOverwrite_price(new BigDecimal(addon.getProd_price()));
//                            addon.setItemTotal(Global.getBigDecimalNum(addon.getItemTotal()).divide(new BigDecimal(splitQty), 4, RoundingMode.HALF_UP).toString());
////                            addon.setItemSubtotal(Global.getBigDecimalNum(addon.getItemSubtotal()).divide(new BigDecimal(splitQty), 4, RoundingMode.HALF_UP).toString());
//                        }
//                        product.setProd_price(new BigDecimal(product.getFinalPrice()).divide(new BigDecimal(splitQty), 4, RoundingMode.HALF_UP).toString());
//                        product.setOverwrite_price(new BigDecimal(product.getProd_price()));
//                        product.setProd_taxValue(product.getProd_taxValue().divide(new BigDecimal(splitQty), 4, RoundingMode.HALF_UP));
//                        product.setDiscount_value(Global.getBigDecimalNum(product.getDiscount_value()).divide(new BigDecimal(splitQty), 4, RoundingMode.HALF_UP).toString());
//                        product.setItemTotal(Global.getBigDecimalNum(product.getItemTotal()).divide(new BigDecimal(splitQty), 4, RoundingMode.HALF_UP).toString());
//                        orderSubTotal = orderSubTotal.add(itemSubtotal).setScale(4, RoundingMode.HALF_UP);
//                    }
//                    splitedOrder.ord_subtotal = orderSubTotal.toString();
//                    splitedOrder.ord_total = orderSubTotal.subtract(orderSubTotal.multiply(globalDiscountPercentge)).toString();
//
//                    splitedOrder.setOrderProducts(orderProducts);
//                    splitedOrder.setTableNumber(tableNumber);
//                    splitedOrders.add(splitedOrder);
//                }
//                break;
//            }
//        }
//        SplittedOrderSummaryAdapter summaryAdapter = new SplittedOrderSummaryAdapter(SplittedOrderSummary_FA.this, splitedOrders);
//        getOrderSummaryFR().getGridView().setAdapter(summaryAdapter);
//        setReceiptPreview();
//    }

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
        SplittedOrder splitedOrder = null;
        try {
            splitedOrder = (SplittedOrder) calculatedSplitedOrders.get(0).clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
        getOrderDetailsFR().setReceiptOrder(splitedOrder);
    }

    @Override
    public void onResume() {

        if (global.isApplicationSentToBackground())
            Global.loggedIn = false;
        global.stopActivityTransitionTimer();

        if (!Global.loggedIn) {
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
            Global.loggedIn = false;
        global.startActivityTransitionTimer();
    }

    @Override
    public void onBackPressed() {
        if (splitType == SalesReceiptSplitTypes.SPLIT_EQUALLY && checkoutCount > 0) {
            promptVoidTransaction(true);
        } else {
            Global.isFromOnHold = true;
            setResult(0);
            finish();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void promptVoidTransaction(final boolean voidPayments) {
        final Dialog dialog = new Dialog(this, R.style.Theme_TransparentTest);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.void_dialog_layout);
        Button voidBut = dialog.findViewById(R.id.voidBut);
        Button notVoid = dialog.findViewById(R.id.notVoidBut);

        voidBut.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                MyPreferences myPref = new MyPreferences(SplittedOrderSummary_FA.this);
                if (myPref.getPreferences(MyPreferences.pref_require_manager_pass_to_void_trans)) {
                    dialog.dismiss();
                    promptManagerPassword(voidPayments);
                } else {
                    dialog.dismiss();
                    voidTransaction(voidPayments, getOrderDetailsFR().restaurantSplitedOrder.ord_id);
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

        final EditText viewField = globalDlog.findViewById(R.id.dlogFieldSingle);
        viewField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        TextView viewTitle = globalDlog.findViewById(R.id.dlogTitle);
        TextView viewMsg = globalDlog.findViewById(R.id.dlogMessage);
        viewTitle.setText(R.string.dlog_title_confirm);

        viewMsg.setText(R.string.dlog_title_enter_manager_password);
        Button btnCancel = globalDlog.findViewById(R.id.btnCancelDlogSingle);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                globalDlog.dismiss();
            }
        });
        Button btnOk = globalDlog.findViewById(R.id.btnDlogSingle);
        btnOk.setText(R.string.button_ok);
        btnOk.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                globalDlog.dismiss();
                MyPreferences myPref = new MyPreferences(SplittedOrderSummary_FA.this);
                String pass = viewField.getText().toString();
                if (!pass.isEmpty() && myPref.loginManager(pass.trim())) {
                    voidTransaction(voidPayments, getOrderDetailsFR().restaurantSplitedOrder.ord_id);
                } else {
                    promptManagerPassword(voidPayments);
                }
            }
        });
        globalDlog.show();
    }

    public void voidTransaction(boolean voidPayments, String orderId) {
        if (voidPayments) {
            SelectPayMethod_FA.voidTransaction(SplittedOrderSummary_FA.this, global.order.ord_id, global.order.ord_type);
        }

        OrdersHandler ordersHandler = new OrdersHandler(SplittedOrderSummary_FA.this);
        OrderProductsHandler orderProductsHandler = new OrderProductsHandler(SplittedOrderSummary_FA.this);
        OrderProductsAttr_DB productsAttrDb = new OrderProductsAttr_DB(SplittedOrderSummary_FA.this);
        OrderTaxes_DB ordTaxesDB = new OrderTaxes_DB();
        Order order = ordersHandler.getOrder(orderId);
        order.isVoid = "1";
        order.processed = "9";
        order.isOnHold = "0";
        List<OrderProduct> orderProducts = orderDetailsFR.restaurantSplitedOrder.getOrderProducts();
        ordersHandler.insert(order);
        DinningTableOrderDAO.deleteByOrderId(order.ord_id);
        global.encodedImage = "";
        orderProductsHandler.insert(orderProducts);
        productsAttrDb.insert(global.ordProdAttr);
        if (order.getListOrderTaxes() != null && order.getListOrderTaxes().size() > 0) {
            ordTaxesDB.insert(order.getListOrderTaxes(), order.ord_id);
        }
        new VoidTransaction().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, order.ord_id);
    }

    public enum NavigationResult {
        PAYMENT_COMPLETED(-1), PAYMENT_SELECTION_VOID(3), BACK_SELECT_PAYMENT(1901), PARTIAL_PAYMENT(1902), VOID_HOLD_TRANSACTION(1903),
        TABLE_SELECTION(1904), SEAT_SELECTION(1905);
        int code;

        NavigationResult(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
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
            Post httpClient = new Post(SplittedOrderSummary_FA.this);
            httpClient.postData(Global.S_CHECKOUT_ON_HOLD, params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            myProgressDialog.dismiss();
            SplittedOrderSummaryAdapter adapter = (SplittedOrderSummaryAdapter) getOrderSummaryFR().getGridView().getAdapter();
            if (adapter.getCount() == 0) {
                setResult(-1);
                finish();
            }
        }

    }

    public void calculateSplitedOrder(List<SplittedOrder> splitedOrders) {
        for (SplittedOrder splitedOrder : splitedOrders) {
            List<OrderProduct> products = splitedOrder.getOrderProducts();
            BigDecimal orderSubtotal = new BigDecimal(0);
            BigDecimal orderTaxes = new BigDecimal(0);
            BigDecimal orderGranTotal;
            BigDecimal itemDiscountTotal = new BigDecimal(0);
            BigDecimal globalDiscountTotal = new BigDecimal(0);
            BigDecimal qty;
            for (OrderProduct product : products) {
                qty = Global.getBigDecimalNum(product.getOrdprod_qty());
                orderSubtotal = orderSubtotal.add(product.getAddonsTotalPrice()).add(Global.getBigDecimalNum(product.getFinalPrice()).multiply(qty));
                globalDiscountTotal = globalDiscountTotal.add(Global.getBigDecimalNum(product.getFinalPrice()).setScale(4, RoundingMode.HALF_UP)
                        .multiply(getGlobalDiscountPercentge().setScale(6, RoundingMode.HALF_UP)));
                itemDiscountTotal = itemDiscountTotal.add(Global.getBigDecimalNum(product.getDiscount_value()));
                if (getTax() != null) {
//                    TaxesCalculator taxesCalculator = new TaxesCalculator(this, product, splitedOrder.tax_id,
//                            getTax(), getDiscount(), Global.getBigDecimalNum(splitedOrder.ord_subtotal),
//                            Global.getBigDecimalNum(splitedOrder.ord_discount), transType);
                    orderTaxes = orderTaxes.add(product.getProd_taxValue());
                    splitedOrder.setListOrderTaxes(splitedOrder.getListOrderTaxes());
                }
            }
            orderGranTotal = orderSubtotal.subtract(itemDiscountTotal).setScale(6, RoundingMode.HALF_UP)
                    .subtract(globalDiscountTotal).setScale(6, RoundingMode.HALF_UP).add(orderTaxes)
                    .setScale(6, RoundingMode.HALF_UP);
            splitedOrder.ord_total = orderGranTotal.toString();
            splitedOrder.gran_total = orderGranTotal.toString();
            splitedOrder.ord_subtotal = orderSubtotal.toString();
            splitedOrder.ord_taxamount = orderTaxes.toString();
            splitedOrder.ord_discount = globalDiscountTotal.toString();
            splitedOrder.ord_lineItemDiscount = itemDiscountTotal.toString();
        }
    }
}
