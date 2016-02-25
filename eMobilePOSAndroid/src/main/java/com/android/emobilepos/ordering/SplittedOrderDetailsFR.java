package com.android.emobilepos.ordering;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.database.MemoTextHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.models.OrderProduct;
import com.android.emobilepos.models.SplitedOrder;
import com.android.support.DateUtils;
import com.android.support.GenerateNewID;
import com.android.support.Global;
import com.android.support.MyPreferences;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.http.client.utils.CloneUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Created by Guarionex on 2/19/2016.
 */
public class SplittedOrderDetailsFR extends Fragment {


    private View detailView;
    private TextView orderId;
    private TextView orderDate;
    private TextView deviceName;
    private TextView subtotal;
    private MyPreferences myPref;
    private TextView lineItemDiscountTotal;
    private TextView taxTotal;
    private TextView granTotal;
    private TextView footer1;
    private TextView footer2;
    private TextView footer3;
    private LinearLayout orderProductSection;
    private LayoutInflater inflater;
    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        detailView = inflater.inflate(R.layout.splitted_order_detail_fragment,
                container, false);
        this.inflater = inflater;
        myPref = new MyPreferences(getActivity());
        GenerateNewID idGen = new GenerateNewID(getActivity());
        MemoTextHandler handler = new MemoTextHandler(getActivity());
        String[] header = handler.getHeader();
        String[] footer = handler.getFooter();

        TextView header1 = (TextView) detailView.findViewById(R.id.memo_headerLine1textView);
        TextView header2 = (TextView) detailView.findViewById(R.id.memo_headerLine2textView16);
        TextView header3 = (TextView) detailView.findViewById(R.id.memo_headerLine3textView18);
        orderId = (TextView) detailView.findViewById(R.id.orderIdtextView16);
        orderDate = (TextView) detailView.findViewById(R.id.orderDatetextView21);
        deviceName = (TextView) detailView.findViewById(R.id.deviceNametextView23);
        subtotal = (TextView) detailView.findViewById(R.id.subtotaltextView);
        lineItemDiscountTotal = (TextView) detailView.findViewById(R.id.lineitem_discounttextView);
        taxTotal = (TextView) detailView.findViewById(R.id.taxtotaltextView14a);
        granTotal = (TextView) detailView.findViewById(R.id.granTotaltextView16);
        footer1 = (TextView) detailView.findViewById(R.id.footerLine1textView);
        footer2 = (TextView) detailView.findViewById(R.id.footerLine2textView);
        footer3 = (TextView) detailView.findViewById(R.id.footerLine3textView);
        orderProductSection = (LinearLayout) detailView.findViewById(R.id.order_products_section_linearlayout);


        deviceName.setText(myPref.getEmpName() + "(" + myPref.getEmpID() + ")");
        orderDate.setText(DateUtils.getDateAsString(new Date(), "MMM/dd/yyyy"));
        orderId.setText(idGen.getNextID(GenerateNewID.IdType.ORDER_ID));

        if (header[0] != null && !header[0].isEmpty()) {
            header1.setText(header[0]);
        } else {
            header1.setVisibility(View.GONE);
        }
        if (header[1] != null && !header[1].isEmpty()) {
            header2.setText(header[1]);
        } else {
            header2.setVisibility(View.GONE);
        }
        if (header[2] != null && !header[2].isEmpty()) {
            header3.setText(header[2]);
        } else {
            header3.setVisibility(View.GONE);
        }

        if (footer[0] != null && !footer[0].isEmpty()) {
            footer1.setText(footer[0]);
        } else {
            footer1.setVisibility(View.GONE);
        }
        if (footer[1] != null && !footer[1].isEmpty()) {
            footer2.setText(footer[1]);
        } else {
            footer2.setVisibility(View.GONE);
        }
        if (footer[2] != null && !footer[2].isEmpty()) {
            footer3.setText(footer[2]);
        } else {
            footer3.setVisibility(View.GONE);
        }
        return detailView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void addProductLine(String leftString, String rightString, boolean indented) {

        LinearLayout itemLL = (LinearLayout) inflater.inflate(R.layout.twocols_leftweight_layout_item, null, false);
        TextView leftText = (TextView) itemLL.findViewById(R.id.lefttextView);
        TextView rightText = (TextView) itemLL.findViewById(R.id.righttextView);
        if (indented) {
            layoutParams.setMargins(10, 0, 0, 0);
            itemLL.setLayoutParams(layoutParams);
        }

        if (rightString == null) {
            rightText.setVisibility(View.GONE);
        } else {
            rightText.setVisibility(View.VISIBLE);
            rightText.setText(rightString);
        }

        if (leftString == null) {
            leftText.setVisibility(View.GONE);
        } else {
            leftText.setVisibility(View.VISIBLE);
            leftText.setText(leftString);
        }

        orderProductSection.addView(itemLL);
    }

    public void setReceiptOrder(SplitedOrder splitedOrder) {
        List<OrderProduct> products = splitedOrder.getOrderProducts();

        if (orderProductSection.getChildCount() > 0) {
            orderProductSection.removeAllViewsInLayout();
        }
        BigDecimal orderSubtotal = new BigDecimal(0);
        BigDecimal orderTaxes = new BigDecimal(0);
        BigDecimal orderGranTotal = new BigDecimal(0);
        BigDecimal itemDiscountTotal = new BigDecimal(0);
        for (OrderProduct product : products) {
          
            orderSubtotal = orderSubtotal.add(Global.getBigDecimalNum(product.overwrite_price));
            orderTaxes = orderTaxes.add(Global.getBigDecimalNum(product.taxTotal));
            itemDiscountTotal = itemDiscountTotal.add(Global.getBigDecimalNum(product.discount_value));
            orderGranTotal = orderGranTotal.add(Global.getBigDecimalNum(product.itemTotal)).add(Global.getBigDecimalNum(product.taxTotal));
            addProductLine(product.ordprod_qty + "x " + product.ordprod_name, null, false);
            addProductLine(getString(R.string.receipt_price), Global.getCurrencyFormat(product.overwrite_price), true);
            addProductLine(getString(R.string.receipt_discount), Global.getCurrencyFormat(product.discount_value), true);
            addProductLine(getString(R.string.receipt_total), Global.getCurrencyFormat(product.itemTotal), true);
            if (product.ordprod_desc != null && !product.ordprod_desc.isEmpty()) {
                addProductLine(getString(R.string.receipt_description), null, true);
                addProductLine(product.ordprod_desc, null, true);
            }
        }
        subtotal.setText(Global.formatDoubleStrToCurrency(orderSubtotal.toString()));
        lineItemDiscountTotal.setText(Global.formatDoubleStrToCurrency(itemDiscountTotal.toString()));
        taxTotal.setText(Global.formatDoubleStrToCurrency(orderTaxes.toString()));
        granTotal.setText(Global.formatDoubleStrToCurrency(orderGranTotal.toString()));

    }
}
