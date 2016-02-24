package com.android.emobilepos.ordering;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.android.database.ProductsHandler;
import com.android.database.TaxesHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.adapters.OrderProductListAdapter;
import com.android.emobilepos.adapters.SplittedOrderSummaryAdapter;
import com.android.emobilepos.models.Discount;
import com.android.emobilepos.models.Order;
import com.android.emobilepos.models.OrderProduct;
import com.android.emobilepos.models.OrderSeatProduct;
import com.android.emobilepos.models.SplitedOrder;
import com.android.emobilepos.models.Tax;
import com.android.support.Global;
import com.android.support.OrderCalculator;
import com.android.support.TaxesCalculator;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
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
    private int taxSelected;
    private Discount discount;
    private BigDecimal discountableSubtotal;
    private BigDecimal itemsDiscountTotal;
    private List<HashMap<String, String>> listMapTaxes;
    private Tax tax;


    public enum SalesReceiptSplitTypes {
        SPLIT_BY_SEATS(0), SPLIT_EQUALLY(1), SPLIT_BY_SEAT_GROUP(2);
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
                    return SPLIT_BY_SEAT_GROUP;
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
        Gson gson = new Gson();
        if (extras != null) {
            Type listType = new TypeToken<List<OrderSeatProduct>>() {
            }.getType();
            String json = extras.getString("orderSeatProductList");
            tableNumber = extras.getString("tableNumber");
            orderSeatProducts = gson.fromJson(json, listType);
            taxID = extras.getString("taxID");
            taxSelected = extras.getInt("taxSelected");
            int discountSelected = extras.getInt("discountSelected");
            TaxesHandler taxesHandler = new TaxesHandler(this);
            List<Tax> taxList = taxesHandler.getTaxes();
            tax = taxList.get(taxSelected);
            ProductsHandler handler2 = new ProductsHandler(this);
            List<Discount> discountList = handler2.getDiscounts();
            if (discountSelected >= 0) {
                discount = discountList.get(discountSelected);
            } else {
                discount = Discount.getDefaultInstance();
            }
//            discount = extras.getStringArray("discount");
//            discountableSubtotal = new BigDecimal(extras.getString("discountableSubtotal"));
//            itemsDiscountTotal = new BigDecimal(extras.getString("itemsDiscountTotal"));

        }
        splitTypeSpinner = (Spinner) findViewById(R.id.splitTypesSpinner);
        splitTypeSpinner.setOnItemSelectedListener(this);
        global = (Global) getApplication();
//        gridView = (GridView) findViewById(R.id.splitedOrderSummarygridView);
        orderSummaryFR = new SplittedOrderSummaryFR();
        orderDetailsFR = new SplittedOrderDetailsFR();


        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.splitedOrderSummaryFrameLayout, orderSummaryFR);
        ft.add(R.id.splitedOrderDetailFrameLayout, orderDetailsFR);
        ft.commit();
    }

    private List<OrderProduct> getProductsBySeats(String seatNumber) {
        List<OrderProduct> seatProducts = new ArrayList<OrderProduct>();
        for (OrderSeatProduct product : orderSeatProducts) {
            if (product.rowType == OrderProductListAdapter.RowType.TYPE_ITEM && product.orderProduct.assignedSeat.equalsIgnoreCase(seatNumber)) {
                seatProducts.add(product.orderProduct);
            }
        }
        return seatProducts;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        int code = Long.valueOf(position).intValue();
        List<SplitedOrder> splitedOrders = new ArrayList<SplitedOrder>();
        SalesReceiptSplitTypes splitType = SalesReceiptSplitTypes.getByCode(code);
        switch (splitType) {
            case SPLIT_BY_SEATS:
                for (OrderSeatProduct seatProduct : orderSeatProducts) {
                    if (seatProduct.rowType == OrderProductListAdapter.RowType.TYPE_HEADER) {
                        Order order = global.order;
                        SplitedOrder splitedOrder = new SplitedOrder(this, order);

                        List<OrderProduct> orderProducts = getProductsBySeats(seatProduct.seatNumber);
                        BigDecimal orderSubTotal = new BigDecimal(0);
                        for (OrderProduct product : orderProducts) {
//                            OrderCalculator orderCalculator = new OrderCalculator(this, product, Global.taxID, tax, discount, global.listOrderTaxes);
                            orderSubTotal = orderSubTotal.add(new BigDecimal(product.itemSubtotal)).setScale(4, RoundingMode.HALF_UP);
                        }
                        splitedOrder.ord_subtotal = orderSubTotal.toString();
                        splitedOrder.setOrderProducts(orderProducts);
                        splitedOrder.setTableNumber(tableNumber);
                        splitedOrders.add(splitedOrder);
                    }
                }
                SplittedOrderSummaryAdapter summaryAdapter = new SplittedOrderSummaryAdapter(this, splitedOrders);

                orderSummaryFR.getGridView().setAdapter(summaryAdapter);

                break;
            case SPLIT_EQUALLY:

                break;
            case SPLIT_BY_SEAT_GROUP:

                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
