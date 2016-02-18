package com.android.emobilepos.ordering;

import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Spinner;

import com.android.emobilepos.R;
import com.android.emobilepos.adapters.OrderProductListAdapter;
import com.android.emobilepos.adapters.SplittedOrderSummaryAdapter;
import com.android.emobilepos.models.Order;
import com.android.emobilepos.models.OrderProduct;
import com.android.emobilepos.models.OrderSeatProduct;
import com.android.emobilepos.models.SplitedOrder;
import com.android.support.Global;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static com.android.emobilepos.ordering.SplittedOrderSummary_FA.SalesReceiptSplitTypes.*;

/**
 * Created by Guarionex on 2/8/2016.
 */
public class SplittedOrderSummary_FA extends BaseFragmentActivityActionBar implements AdapterView.OnItemSelectedListener {

    private Global global;
    List<OrderSeatProduct> orderSeatProducts;
    Spinner splitTypeSpinner;
    private GridView gridView;


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
            orderSeatProducts = gson.fromJson(json, listType);
        }
        splitTypeSpinner = (Spinner) findViewById(R.id.splitTypesSpinner);
        splitTypeSpinner.setOnItemSelectedListener(this);
        global = (Global) getApplication();
        gridView = (GridView) findViewById(R.id.splitedOrderSummarygridView);
//        SplittedOrderSummaryAdapter summaryAdapter = new SplittedOrderSummaryAdapter(this, new ArrayList<SplitedOrder>());
//        gridView.setAdapter(summaryAdapter);
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
                        List<OrderProduct> orderProducts = getProductsBySeats(seatProduct.seatNumber);
                        SplitedOrder splitedOrder = new SplitedOrder(this, order);
                        splitedOrder.setOrderProducts(orderProducts);
                        splitedOrders.add(splitedOrder);
                    }
                }
                SplittedOrderSummaryAdapter summaryAdapter = new SplittedOrderSummaryAdapter(this, splitedOrders);
                gridView.setAdapter(summaryAdapter);
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
