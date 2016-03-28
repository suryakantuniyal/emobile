package com.android.emobilepos.ordering;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.database.ProductsHandler;
import com.android.database.TaxesGroupHandler;
import com.android.database.TaxesHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.models.DataTaxes;
import com.android.emobilepos.models.Discount;
import com.android.emobilepos.models.OrderProduct;
import com.android.emobilepos.models.Tax;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.TaxesCalculator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OrderTotalDetails_FR extends Fragment implements Receipt_FR.RecalculateCallback {
    private Spinner taxSpinner, discountSpinner;
    private List<Tax> taxList;
    private List<Discount> discountList;
    private int taxSelected, discountSelected;
    private EditText globalDiscount, globalTax, subTotal;
    private TextView granTotal, itemCount;
    public static String discountID = "", taxID = "";
    public static BigDecimal tax_amount = new BigDecimal("0"), discount_amount = new BigDecimal("0"),
            discount_rate = new BigDecimal("0"), discountable_sub_total = new BigDecimal("0"),
            sub_total = new BigDecimal("0"), gran_total = new BigDecimal("0");
    public static BigDecimal itemsDiscountTotal = new BigDecimal(0);

    private Activity activity;
    private MyPreferences myPref;
    private Global global;
    private BigDecimal taxableSubtotal;
    private static OrderTotalDetails_FR myFrag;

    private TaxesHandler taxHandler;
    private TaxesGroupHandler taxGroupHandler;

    public static OrderTotalDetails_FR init(int val) {
        OrderTotalDetails_FR frag = new OrderTotalDetails_FR();

        Bundle args = new Bundle();
        args.putInt("val", val);
        frag.setArguments(args);
        myFrag = frag;
        return frag;
    }

    public static OrderTotalDetails_FR getFrag() {
        return myFrag;
    }

    public static void resetView() {
        myFrag = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.order_total_details_layout, container, false);

        myFrag = this;
        taxSelected = 0;
        discountSelected = 0;
        subTotal = (EditText) view.findViewById(R.id.subtotalField);
        taxSpinner = (Spinner) view.findViewById(R.id.globalTaxSpinner);
        globalTax = (EditText) view.findViewById(R.id.globalTaxField);
        discountSpinner = (Spinner) view.findViewById(R.id.globalDiscountSpinner);
        globalDiscount = (EditText) view.findViewById(R.id.globalDiscountField);
        granTotal = (TextView) view.findViewById(R.id.grandTotalValue);
        LinearLayout leftHolder = (LinearLayout) view.findViewById(R.id.leftColumnHolder);
        activity = getActivity();
        global = (Global) activity.getApplication();
        myPref = new MyPreferences(activity);
        taxableSubtotal = new BigDecimal("0");

        if (!myPref.getIsTablet() && leftHolder != null) {
            leftHolder.setVisibility(View.GONE);
        } else if (myPref.getIsTablet() && leftHolder != null)
            itemCount = (TextView) view.findViewById(R.id.itemCount);
        initSpinners();
        return view;
    }

    public void initSpinners() {

        listMapTaxes = new ArrayList<HashMap<String, String>>();
        List<String> taxes = new ArrayList<String>();
        List<String> discount = new ArrayList<String>();
        String custTaxCode = "";
        if (myPref.isCustSelected()) {
            custTaxCode = myPref.getCustTaxCode();
            if (custTaxCode.isEmpty()) {
                custTaxCode = myPref.getEmployeeDefaultTax();
            }
        } else if (Global.isFromOnHold)
            custTaxCode = Global.taxID;
        else {
            custTaxCode = myPref.getEmployeeDefaultTax();
        }
        taxes.add("Global Tax");
        discount.add("Global Discount");

        taxHandler = new TaxesHandler(activity);
        taxGroupHandler = new TaxesGroupHandler(activity);
        taxList = taxHandler.getTaxes();
        ProductsHandler handler2 = new ProductsHandler(activity);
        discountList = handler2.getDiscounts();
        int size = taxList.size();
        int size2 = discountList.size();
        boolean custTaxWasFound = false;
        int mSize = size;
        if (size < size2)
            mSize = size2;
        for (int i = 0; i < mSize; i++) {
            if (i < size) {
                taxes.add(taxList.get(i).getTaxName());
                if (!custTaxCode.isEmpty() && custTaxCode.equals(taxList.get(i).getTaxId())) {
                    taxSelected = i + 1;
                    custTaxWasFound = true;
                }
            }
            if (i < size2)
                discount.add(discountList.get(i).getProductName());
        }

        if (!custTaxWasFound) {
            taxSelected = 0;
            Global.taxID = "";

        }
        List<String[]> taxArr = new ArrayList<String[]>();
        int i = 0;
        for (Tax tax : taxList) {
            String[] arr = new String[5];
            arr[0] = tax.getTaxName();
            arr[1] = tax.getTaxId();
            arr[2] = tax.getTaxRate();
            arr[3] = tax.getTaxType();
            taxArr.add(arr);
        }
        MySpinnerAdapter taxAdapter = new MySpinnerAdapter(activity, android.R.layout.simple_spinner_item, taxes, taxArr, true);
        List<String[]> discountArr = new ArrayList<String[]>();
        i = 0;
        for (Discount disc : discountList) {
            String[] arr = new String[5];
            arr[0] = disc.getProductName();
            arr[1] = disc.getProductDiscountType();
            arr[2] = disc.getProductPrice();
            arr[3] = disc.getTaxCodeIsTaxable();
            arr[4] = disc.getProductId();
            discountArr.add(arr);
        }
        MySpinnerAdapter discountAdapter = new MySpinnerAdapter(activity, android.R.layout.simple_spinner_item, discount, discountArr,
                false);

        taxSpinner.setAdapter(taxAdapter);
        taxSpinner.setOnItemSelectedListener(setSpinnerListener(false));
        taxSpinner.setSelection(taxSelected, false);

        discountSpinner.setAdapter(discountAdapter);
        discountSpinner.setOnItemSelectedListener(setSpinnerListener(true));
    }

    private class MySpinnerAdapter extends ArrayAdapter<String> {
        private Activity context;
        List<String> leftData = null;
        List<String[]> rightData = null;
        boolean isTax = false;

        public MySpinnerAdapter(Activity activity, int resource, List<String> left, List<String[]> right,
                                boolean isTax) {
            super(activity, resource, left);
            this.context = activity;
            this.leftData = left;
            this.rightData = right;
            this.isTax = isTax;
        }

        public int getDiscountIdPosition(String discountId) {
            for (int i = 0; i < rightData.size(); i++) {
                if (rightData.get(i)[4].equalsIgnoreCase(discountId)) {
                    return i;
                }
            }
            return -1;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);

            // we know that simple_spinner_item has android.R.id.text1 TextView:
            TextView text = (TextView) view.findViewById(android.R.id.text1);
            text.setTextAppearance(activity, R.style.black_text_appearance);// choose your color
            text.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    activity.getResources().getDimension(R.dimen.ordering_checkout_btn_txt_size));
            return view;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = context.getLayoutInflater();
                row = inflater.inflate(R.layout.spinner_layout, parent, false);
            }

            TextView taxName = (TextView) row.findViewById(R.id.taxName);
            TextView taxValue = (TextView) row.findViewById(R.id.taxValue);
            ImageView checked = (ImageView) row.findViewById(R.id.checkMark);
            checked.setVisibility(View.INVISIBLE);
            taxName.setText(leftData.get(position));
            int type = getItemViewType(position);
            switch (type) {
                case 0: {
                    taxValue.setText("");
                    break;
                }
                case 1: {
                    setValues(taxValue, position);
                    checked.setVisibility(View.VISIBLE);
                    break;
                }
                case 2: {
                    setValues(taxValue, position);
                    break;
                }
            }
            return row;
        }

        public void setValues(TextView taxValue, int position) {
            StringBuilder sb = new StringBuilder();
            if (isTax) {
                sb.append("%").append(rightData.get(position - 1)[2]);
                taxValue.setText(sb.toString());
            } else {
                if (rightData.get(position - 1)[1].equals("Fixed")) {
                    sb.append(Global.formatDoubleStrToCurrency(rightData.get(position - 1)[2]));
                    taxValue.setText(sb.toString());
                } else {
                    sb.append("%").append(rightData.get(position - 1)[2]);
                    taxValue.setText(sb.toString());
                }
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return 0;
            } else if ((isTax && position == taxSelected) || (!isTax && position == discountSelected)) {
                return 1;
            }
            return 2;
        }
    }

    private OnItemSelectedListener setSpinnerListener(final boolean isDiscount) {
        return new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int pos, long arg3) {
                if (isDiscount) {
                    discountSelected = pos;
                    setDiscountValue(pos);
                } else {
                    taxSelected = pos;
                    setTaxValue(pos);
                }
                reCalculate(global.orderProducts);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                if (isDiscount)
                    discountSelected = 0;
                else
                    taxSelected = 0;
            }

        };
    }

    public void setTaxValue(int position) {
        if (position == 0) {
            tax_amount = new BigDecimal("0");
            Global.taxAmount = new BigDecimal("0");
            taxID = "";
        } else {

            taxID = taxList.get(taxSelected - 1).getTaxId();
            Global.taxAmount = Global.getBigDecimalNum(taxList.get(taxSelected - 1).getTaxRate());
            if (!myPref.getPreferences(MyPreferences.pref_retail_taxes)) {
                listMapTaxes = taxHandler.getTaxDetails(taxID, "");
                if (listMapTaxes.size() > 0 && listMapTaxes.get(0).get("tax_type").equals("G")) {
                    listMapTaxes = taxGroupHandler.getIndividualTaxes(listMapTaxes.get(0).get("tax_id"),
                            listMapTaxes.get(0).get("tax_code_id"));
                    setupTaxesHolder();
                }
            } else {
                listMapTaxes.clear();
                HashMap<String, String> mapTax = new HashMap<String, String>();
                mapTax.put("tax_id", taxID);
                mapTax.put("tax_name", taxList.get(taxSelected - 1).getTaxName());
                mapTax.put("tax_rate", taxList.get(taxSelected - 1).getTaxRate());
                listMapTaxes.add(mapTax);
            }
        }
        taxSelected = position;
        Global.taxPosition = position;
        Global.taxID = taxID;
    }

    public void setDiscountValue(int position) {
        DecimalFormat frmt = new DecimalFormat("0.00");
        if (position == 0) {
            if (global.order == null || global.order.ord_discount_id.isEmpty()) {
                discount_rate = new BigDecimal("0");
                discount_amount = new BigDecimal("0");
                discountID = "";
            } else {
                discountSelected = ((MySpinnerAdapter) discountSpinner.getAdapter()).getDiscountIdPosition(global.order.ord_discount_id) + 1;
                discountID = discountList.get(discountSelected - 1).getProductId();
                if (discountList.get(discountSelected - 1).getProductDiscountType().equals("Fixed")) {
                    discount_rate = Global.getBigDecimalNum(discountList.get(discountSelected - 1).getProductPrice());
                    discount_amount = Global.getBigDecimalNum(discountList.get(discountSelected - 1).getProductPrice());

                } else {
                    discount_rate = Global.getBigDecimalNum(discountList.get(discountSelected - 1).getProductPrice())
                            .divide(new BigDecimal("100"));
                    BigDecimal total = discountable_sub_total.subtract(itemsDiscountTotal);
                    discount_amount = total.multiply(discount_rate).setScale(2, RoundingMode.HALF_UP);
                }
            }
        } else if (discountList != null && discountSelected > 0) {
            discountID = discountList.get(discountSelected - 1).getProductId();
            if (discountList.get(discountSelected - 1).getProductDiscountType().equals("Fixed")) {
                discount_rate = Global.getBigDecimalNum(discountList.get(discountSelected - 1).getProductPrice());
                discount_amount = Global.getBigDecimalNum(discountList.get(discountSelected - 1).getProductPrice());

            } else {
                discount_rate = Global.getBigDecimalNum(discountList.get(discountSelected - 1).getProductPrice())
                        .divide(new BigDecimal("100"));
                BigDecimal total = discountable_sub_total.subtract(itemsDiscountTotal);
                discount_amount = total.multiply(discount_rate).setScale(2, RoundingMode.HALF_UP);
            }
        }

        // if(discount_amount.compareTo(sub_total)!=-1)
        // {
        // discount_amount = sub_total;
        // }

        globalDiscount.setText(Global.getCurrencyFormat(frmt.format(discount_amount)));

        Global.discountPosition = position;
        Global.discountAmount = discount_amount;
    }

    private List<HashMap<String, String>> listMapTaxes = new ArrayList<HashMap<String, String>>();


    private void setupTaxesHolder() {
        int size = listMapTaxes.size();
        global.listOrderTaxes = new ArrayList<DataTaxes>();
        DataTaxes tempTaxes;
        for (int i = 0; i < size; i++) {
            tempTaxes = new DataTaxes();
            tempTaxes.setTax_name(listMapTaxes.get(i).get("tax_name"));
            tempTaxes.setOrd_id("");
            tempTaxes.setTax_amount("0");
            tempTaxes.setTax_rate(listMapTaxes.get(i).get("tax_rate"));
            global.listOrderTaxes.add(tempTaxes);
        }
    }

    private void calculateTaxes(OrderProduct orderProduct) {
        Discount dis = null;
        if (discountSelected > 0) {
            dis = discountList.get(discountSelected - 1);
        }
        TaxesCalculator taxesCalculator = new TaxesCalculator(activity, orderProduct, Global.taxID,
                taxSelected, dis, discountable_sub_total, itemsDiscountTotal, listMapTaxes);
        tempTaxableAmount = tempTaxableAmount.add(taxesCalculator.getTaxableAmount());

    }

    private BigDecimal getProductPrice(BigDecimal prod_with_tax_price, BigDecimal tax) {
        BigDecimal denom = new BigDecimal(1).add(tax);
        return prod_with_tax_price.divide(denom, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal tempTaxableAmount = new BigDecimal("0");
    private BigDecimal taxableDueAmount = new BigDecimal("0");

//    private void calculateGlobalTax(BigDecimal _subtotal, BigDecimal qty, boolean isVat) {
//
//        int size = listMapTaxes.size();
//        String val = "0";
//        BigDecimal temp = new BigDecimal("0");
//        BigDecimal _total_tax = new BigDecimal("0");
//        for (int j = 0; j < size; j++) {
//            val = listMapTaxes.get(j).get("tax_rate");
//            if (val == null || val.isEmpty())
//                val = "0";
//            temp = new BigDecimal(listMapTaxes.get(j).get("tax_rate")).divide(new BigDecimal("100")).setScale(4,
//                    RoundingMode.HALF_UP);
//            BigDecimal tax_amount = _subtotal.multiply(temp).setScale(4, RoundingMode.HALF_UP);
//
//            _total_tax = _total_tax.add(tax_amount);
//            DataTaxes tempTaxes = global.listOrderTaxes.get(j);
//            BigDecimal orderTaxesTotal = tempTaxes.getTax_amount().isEmpty() ? new BigDecimal(0.00) : new BigDecimal(tempTaxes.getTax_amount());
//            if (_subtotal.compareTo(new BigDecimal("0.00")) != 0) {
//                if (isVat)
//                    orderTaxesTotal.add(tax_amount.multiply(qty.abs()).setScale(2, RoundingMode.HALF_UP));
//                else
//                    orderTaxesTotal.add(tax_amount).setScale(4, RoundingMode.HALF_UP);
//            }
//            tempTaxes.setTax_amount(orderTaxesTotal.toString());
//            global.listOrderTaxes.set(j, tempTaxes);
//        }
//
//        if (isVat)
//            _total_tax = _total_tax.setScale(2, RoundingMode.HALF_UP).multiply(qty.abs());
//        tempTaxableAmount = tempTaxableAmount.add(_total_tax).setScale(4, RoundingMode.HALF_UP);
//    }
//
//    private void calculateRetailGlobalTax(BigDecimal _sub_total, String tax_rate, BigDecimal qty, boolean isVat) {
//        int size = listMapTaxes.size();
//        String val = "0";
//        BigDecimal temp = new BigDecimal("0");
//        BigDecimal _total_tax = new BigDecimal("0");
//        for (int j = 0; j < size; j++) {
//            val = listMapTaxes.get(j).get("tax_rate");
//            if (val == null || val.isEmpty())
//                val = "0";
//            temp = new BigDecimal(tax_rate).divide(new BigDecimal("100")).setScale(4, RoundingMode.HALF_UP);
//            BigDecimal tax_amount = _sub_total.multiply(temp).setScale(4, RoundingMode.HALF_UP);
//
//            _total_tax = _total_tax.add(tax_amount);
//            DataTaxes tempTaxes = global.listOrderTaxes.get(j);
//            BigDecimal orderTaxesTotal = tempTaxes.getTax_amount().isEmpty() ? new BigDecimal(0.00) : new BigDecimal(tempTaxes.getTax_amount());
//            if (_sub_total.compareTo(new BigDecimal("0.00")) != 0) {
//                if (isVat)
//                    orderTaxesTotal.add(tax_amount.multiply(qty)).setScale(2, RoundingMode.HALF_UP);
//                else
//                    orderTaxesTotal.add(tax_amount).setScale(4, RoundingMode.HALF_UP);
//
//            }
//            tempTaxes.setTax_amount(orderTaxesTotal.toString());
//            global.listOrderTaxes.set(j, tempTaxes);
//        }
//
//        if (isVat)
//            _total_tax = _total_tax.setScale(2, RoundingMode.HALF_UP).multiply(qty);
//        tempTaxableAmount = tempTaxableAmount.add(_total_tax).setScale(4, RoundingMode.HALF_UP);
//    }

    public void reCalculate(List<OrderProduct> orderProducts) {
    //TODO Temporary fix. Need verify why SDK 5.0 calls with null global and why sdk 4.3 not
        if (global == null) {
            return;
        }
        int size = orderProducts.size();
        taxableSubtotal = new BigDecimal("0.00");
        taxableDueAmount = new BigDecimal("0.00");
        tempTaxableAmount = new BigDecimal("0");
        itemsDiscountTotal = new BigDecimal(0);
        setupTaxesHolder();
        boolean isVAT = myPref.getIsVAT();

        if (size > 0) {
            BigDecimal amount = new BigDecimal("0.00");
            BigDecimal discountableAmount = new BigDecimal("0");
            BigDecimal prodPrice;
            String val;
            int pointsSubTotal = 0, pointsInUse = 0, pointsAcumulable = 0;
            for (int i = 0; i < size; i++) {
                calculateTaxes(orderProducts.get(i));
                if (myPref.getPreferences(MyPreferences.pref_show_removed_void_items_in_printout)) {
                    String temp = orderProducts.get(i).item_void;

                    if (temp.equals("1"))
                        val = "0.00";
                    else {
                        if (isVAT) {
                            val = orderProducts.get(i).itemTotalVatExclusive;
                        } else
                            val = orderProducts.get(i).itemTotal;
                    }
                } else {
                    if (isVAT) {
                        val = orderProducts.get(i).itemTotalVatExclusive;
                    } else
                        val = orderProducts.get(i).itemSubtotal;
                }
                if (val == null || val.isEmpty())
                    val = "0.00";
                prodPrice = new BigDecimal(val);
                discountableAmount = discountableAmount.add(prodPrice);
                try {
                    if (orderProducts.get(i).discount_value != null
                            && !orderProducts.get(i).discount_value.isEmpty())
                        itemsDiscountTotal = itemsDiscountTotal.add(new BigDecimal(orderProducts.get(i).discount_value));
                } catch (NumberFormatException e) {
                }
                amount = amount.add(prodPrice);
                pointsSubTotal += Double.parseDouble(orderProducts.get(i).prod_price_points);
                pointsAcumulable += Double.parseDouble(orderProducts.get(i).prod_value_points);
                if (Boolean.parseBoolean(orderProducts.get(i).payWithPoints))
                    pointsInUse += Double.parseDouble(orderProducts.get(i).prod_price_points);
            }
            if (itemCount != null)
                itemCount.setText(String.valueOf(size));
            discountable_sub_total = discountableAmount.subtract(Global.rewardChargeAmount);
            sub_total = amount.subtract(Global.rewardChargeAmount);
            subTotal.setText(Global.getCurrencyFrmt(Global.getRoundBigDecimal(sub_total)));
            tax_amount = tempTaxableAmount;
            setDiscountValue(discountSelected);
            globalDiscount.setText(Global.getCurrencyFrmt(discount_amount.toString()));
            globalTax.setText(Global.getCurrencyFrmt(Global.getRoundBigDecimal(tax_amount)));
            gran_total = sub_total.subtract(discount_amount).add(tax_amount)
                    .subtract(itemsDiscountTotal);
//            if (OrderingMain_FA.returnItem && OrderingMain_FA.mTransType != Global.TransactionType.RETURN) {
//                gran_total = gran_total.negate();
//            }
            OrderLoyalty_FR.recalculatePoints(Integer.toString(pointsSubTotal), Integer.toString(pointsInUse),
                    Integer.toString(pointsAcumulable), gran_total.toString());
            OrderRewards_FR.setRewardSubTotal(discountable_sub_total.toString());
            granTotal.setText(Global.getCurrencyFrmt(Global.getRoundBigDecimal(gran_total)));
        } else {
            discountable_sub_total = new BigDecimal(getString(R.string.amount_zero_lbl));
            sub_total = new BigDecimal(getString(R.string.amount_zero_lbl));
            gran_total = new BigDecimal(getString(R.string.amount_zero_lbl));
            this.subTotal.setText(getString(R.string.amount_zero_lbl));
            globalTax.setText(getString(R.string.amount_zero_lbl));
            setDiscountValue(discountSelected);
            granTotal.setText(getString(R.string.amount_zero_lbl));
            OrderLoyalty_FR.recalculatePoints("0", "0", "0", gran_total.toString());
        }
    }

    @Override
    public void recalculateTotal() {
        reCalculate(global.orderProducts);
    }
}