package com.android.emobilepos.ordering;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
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
import com.android.support.Global;
import com.android.support.MyPreferences;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OrderTotalDetails_FR extends Fragment implements Receipt_FR.RecalculateCallback {
    private Spinner taxSpinner, discountSpinner;
    private List<String[]> taxList, discountList;
    private MySpinnerAdapter taxAdapter, discountAdapter;
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
                taxes.add(taxList.get(i)[0]);
                if (!custTaxCode.isEmpty() && custTaxCode.equals(taxList.get(i)[1])) {
                    taxSelected = i + 1;
                    custTaxWasFound = true;
                }
            }
            if (i < size2)
                discount.add(discountList.get(i)[0]);
        }

        if (!custTaxWasFound) {
            taxSelected = 0;
            Global.taxID = "";

        }

        taxAdapter = new MySpinnerAdapter(activity, android.R.layout.simple_spinner_item, taxes, taxList, true);
        discountAdapter = new MySpinnerAdapter(activity, android.R.layout.simple_spinner_item, discount, discountList,
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
        OnItemSelectedListener listener = new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int pos, long arg3) {
                // TODO Auto-generated method stub
                if (isDiscount) {
                    discountSelected = pos;
                    setDiscountValue(pos);
                } else {
                    taxSelected = pos;
                    setTaxValue(pos);
                }
                reCalculate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
                if (isDiscount)
                    discountSelected = 0;
                else
                    taxSelected = 0;
            }

        };
        return listener;
    }

    public void setTaxValue(int position) {
        if (position == 0) {
            tax_amount = new BigDecimal("0");
            Global.taxAmount = new BigDecimal("0");
            taxID = "";
        } else {

            taxID = taxList.get(taxSelected - 1)[1];
            Global.taxAmount = Global.getBigDecimalNum(taxList.get(taxSelected - 1)[2]);
            if (!myPref.getPreferences(MyPreferences.pref_retail_taxes)) {
                listMapTaxes = taxHandler.getTaxDetails(taxID, "");
                if (listMapTaxes.size() > 0 && listMapTaxes.get(0).get("tax_type").equals("G")) {
                    listMapTaxes = taxGroupHandler.getIndividualTaxes(listMapTaxes.get(0).get("tax_id"),
                            listMapTaxes.get(0).get("tax_code_id"));
                    setupTaxesHolder();
                }
            } else {
                HashMap<String, String> mapTax = new HashMap<String, String>();
                mapTax.put("tax_id", taxID);
                mapTax.put("tax_name", taxList.get(taxSelected - 1)[0]);
                mapTax.put("tax_rate", taxList.get(taxSelected - 1)[2]);
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
            discount_rate = new BigDecimal("0");
            discount_amount = new BigDecimal("0");
            discountID = "";
        } else if (discountList != null && discountSelected > 0) {
            discountID = discountList.get(discountSelected - 1)[4];
            if (discountList.get(discountSelected - 1)[1].equals("Fixed")) {
                discount_rate = Global.getBigDecimalNum(discountList.get(discountSelected - 1)[2]);
                discount_amount = Global.getBigDecimalNum(discountList.get(discountSelected - 1)[2]);

            } else {
                discount_rate = Global.getBigDecimalNum(discountList.get(discountSelected - 1)[2])
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

    private List<BigDecimal> listOrderTaxesTotal;

    private void setupTaxesHolder() {
        int size = listMapTaxes.size();
        listOrderTaxesTotal = new ArrayList<BigDecimal>();
        global.listOrderTaxes = new ArrayList<DataTaxes>();
        BigDecimal tempBD;
        DataTaxes tempTaxes;
        for (int i = 0; i < size; i++) {
            tempBD = new BigDecimal("0");
            listOrderTaxesTotal.add(tempBD);

            tempTaxes = new DataTaxes();
            tempTaxes.set("tax_name", listMapTaxes.get(i).get("tax_name"));
            tempTaxes.set("ord_id", "");
            tempTaxes.set("tax_amount", "0");
            tempTaxes.set("tax_rate", listMapTaxes.get(i).get("tax_rate"));
            global.listOrderTaxes.add(tempTaxes);
        }
    }

    private void calculateTaxes(int i) {
        String taxAmount = "0.00";
        String prod_taxId = "";
        if (myPref.getPreferences(MyPreferences.pref_retail_taxes)) {
            if (!Global.taxID.isEmpty()) {
                taxAmount = Global.formatNumToLocale(
                        Double.parseDouble(taxHandler.getTaxRate(Global.taxID, global.orderProducts.get(i).prod_taxtype,
                                Double.parseDouble(global.orderProducts.get(i).overwrite_price))));
                prod_taxId = global.orderProducts.get(i).prod_taxtype;
            } else {
                taxAmount = Global.formatNumToLocale(Double.parseDouble(taxHandler.getTaxRate(
                        global.orderProducts.get(i).prod_taxcode, global.orderProducts.get(i).prod_taxtype,
                        Double.parseDouble(global.orderProducts.get(i).overwrite_price))));
                prod_taxId = global.orderProducts.get(i).prod_taxcode;
            }
        } else {
            if (!Global.taxID.isEmpty()) {
                taxAmount = taxList.get(taxSelected - 1)[2];
                prod_taxId = Global.taxID;
            }
        }

        BigDecimal tempSubTotal = new BigDecimal(global.orderProducts.get(i).itemSubtotal);
        BigDecimal prodQty = new BigDecimal(global.orderProducts.get(i).ordprod_qty);
        BigDecimal _temp_subtotal = tempSubTotal;
        boolean isVAT = myPref.getIsVAT();
        if (isVAT) {
            if (global.orderProducts.get(i).prod_istaxable.equals("1")) {
                if (global.orderProducts.get(i).prod_price_updated.equals("0")) {
                    BigDecimal _curr_prod_price = new BigDecimal(global.orderProducts.get(i).overwrite_price);
                    BigDecimal _new_prod_price = getProductPrice(_curr_prod_price,
                            new BigDecimal(taxAmount).divide(new BigDecimal("100")).setScale(4, RoundingMode.HALF_UP));
                    _new_prod_price = _new_prod_price.setScale(4, RoundingMode.HALF_UP);
                    tempSubTotal = _new_prod_price.multiply(prodQty).setScale(2, RoundingMode.HALF_UP);
                    // global.orderProducts.get(i).getSetData("itemSubtotal",

                    global.orderProducts.get(i).price_vat_exclusive = _new_prod_price.setScale(2, RoundingMode.HALF_UP)
                            .toString();
                    global.orderProducts.get(i).prod_price_updated = "1";

                    BigDecimal disc = new BigDecimal("0");
                    if (global.orderProducts.get(i).discount_is_fixed.equals("0")) {
                        BigDecimal val = tempSubTotal
                                .multiply(Global.getBigDecimalNum(global.orderProducts.get(i).disAmount))
                                .setScale(4, RoundingMode.HALF_UP);
                        disc = val.divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                    } else {
                        disc = new BigDecimal(global.orderProducts.get(i).disAmount);
                    }
                    // BigDecimal disc = new
                    // BigDecimal(global.orderProducts.get(i).getSetData("discount_value",
                    // true, null));
                    global.orderProducts.get(i).discount_value = Global.getRoundBigDecimal(disc);
                    global.orderProducts.get(i).disTotal = Global.getRoundBigDecimal(disc);
                    // global.orderProducts.get(i).getSetData("itemTotal",
                    // false,
                    // Global.getRoundBigDecimal(tempSubTotal.subtract(disc)));
                    global.orderProducts.get(i).itemTotalVatExclusive = Global
                            .getRoundBigDecimal(tempSubTotal.subtract(disc));
                }

                if (prodQty.compareTo(new BigDecimal("1")) == 1) {
                    _temp_subtotal = new BigDecimal(global.orderProducts.get(i).price_vat_exclusive).setScale(2,
                            RoundingMode.HALF_UP);
                } else {
                    // _new_prod_price = _new_prod_price.setScale(4,
                    // RoundingMode.HALF_UP);
                    tempSubTotal = new BigDecimal(global.orderProducts.get(i).price_vat_exclusive).multiply(prodQty)
                            .setScale(2, RoundingMode.HALF_UP);
                    _temp_subtotal = tempSubTotal;
                }
            } else
                global.orderProducts.get(i).itemTotalVatExclusive = _temp_subtotal.toString();
        }
        BigDecimal tempTaxTotal = new BigDecimal("0");
        String taxTotal = "0";

        if (global.orderProducts.get(i).prod_istaxable.equals("1") &&
                (global.orderProducts.get(i).item_void.isEmpty() || global.orderProducts.get(i).item_void.equals("0"))) {
            taxableDueAmount = taxableDueAmount.add(tempSubTotal);

            if (global.orderProducts.get(i).discount_is_taxable.equals("1")) {
                BigDecimal temp = new BigDecimal(taxAmount).divide(new BigDecimal("100")).setScale(4,
                        RoundingMode.HALF_UP);
                tempSubTotal = tempSubTotal.abs().subtract(new BigDecimal(global.orderProducts.get(i).discount_value).abs());
                if (global.orderProducts.get(i).isReturned && OrderingMain_FA.mTransType != Global.TransactionType.RETURN) {
                    tempSubTotal = tempSubTotal.negate();
                }
                BigDecimal tax1 = tempSubTotal.multiply(temp);
                tempTaxTotal = tax1;
                taxTotal = tax1.toString();
                _temp_subtotal = tempSubTotal;
                if (discountSelected > 0) {
                    if (discountList.get(discountSelected - 1)[1].equals("Fixed")) {
                        if (discount_rate.compareTo(tempSubTotal) == -1) {
                            taxableSubtotal = taxableSubtotal.add(tempSubTotal.subtract(discount_rate).multiply(temp)
                                    .setScale(4, RoundingMode.HALF_UP));
                            // discount_rate = new BigDecimal("0");
                            if (discountList.get(discountSelected - 1)[3].equals("1")) {
                                _temp_subtotal = _temp_subtotal.subtract(discount_rate);
                            }
                        } else {
                            discount_amount = tempSubTotal;
                            taxableSubtotal = new BigDecimal("0");
                            _temp_subtotal = taxableSubtotal;
                        }
                    } else {
                        BigDecimal temp2 = tempSubTotal.multiply(discount_rate).setScale(4, RoundingMode.HALF_UP);
                        taxableSubtotal = taxableSubtotal
                                .add(tempSubTotal.subtract(temp2).multiply(temp).setScale(4, RoundingMode.HALF_UP));
                        if (discountList.get(discountSelected - 1)[3].equals("1")) {
                            _temp_subtotal = _temp_subtotal.subtract(temp2);
                        }
                    }
                } else {
                    taxableSubtotal = taxableSubtotal
                            .add(tempSubTotal.multiply(temp).setScale(4, RoundingMode.HALF_UP));
                }
            } else {
                BigDecimal temp = new BigDecimal(taxAmount).divide(new BigDecimal("100")).setScale(4,
                        RoundingMode.HALF_UP);
                BigDecimal tax1 = tempSubTotal.multiply(temp).setScale(2, RoundingMode.HALF_UP);
                tempTaxTotal = tax1;
                taxTotal = tax1.toString();

                if (!global.orderProducts.get(i).isReturned) {
                    if (discountSelected > 0) {
                        if (discountList.get(discountSelected - 1)[1].equals("Fixed")) {
                            if (discount_rate.compareTo(tempSubTotal) == -1) {
                                taxableSubtotal = taxableSubtotal.add(tempSubTotal.subtract(discount_rate)
                                        .multiply(temp).setScale(4, RoundingMode.HALF_UP));
                                // discount_rate = new BigDecimal("0");
                                if (discountList.get(discountSelected - 1)[3].equals("1")) {
                                    _temp_subtotal = tempSubTotal.subtract(discount_rate);
                                }
                            } else {
                                // discount_amount = tempSubTotal;
                                taxableSubtotal = new BigDecimal("0");
                                _temp_subtotal = taxableSubtotal;
                            }
                        } else {
                            BigDecimal temp2 = tempSubTotal.multiply(discount_rate).setScale(4, RoundingMode.HALF_UP);
                            taxableSubtotal = taxableSubtotal
                                    .add(tempSubTotal.subtract(temp2).multiply(temp).setScale(4, RoundingMode.HALF_UP));
                            if (discountList.get(discountSelected - 1)[3].equals("1")) {
                                _temp_subtotal = tempSubTotal.subtract(temp2);
                            }
                        }
                    } else {
                        taxableSubtotal = taxableSubtotal
                                .add(tempSubTotal.multiply(temp).setScale(4, RoundingMode.HALF_UP));
                    }
                }
            }

            if (myPref.getPreferences(MyPreferences.pref_retail_taxes)) {
                calculateRetailGlobalTax(_temp_subtotal, taxAmount, prodQty, isVAT);
            } else {
                calculateGlobalTax(_temp_subtotal, prodQty, isVAT);
            }

        }

        if (tempTaxTotal.compareTo(new BigDecimal("0")) < -1)
            taxTotal = Double.toString(0.0);

        global.orderProducts.get(i).prod_taxValue = taxTotal;
        global.orderProducts.get(i).prod_taxId = prod_taxId;
    }

    private BigDecimal getProductPrice(BigDecimal prod_with_tax_price, BigDecimal tax) {
        BigDecimal denom = new BigDecimal(1).add(tax);
        BigDecimal prodPrice = prod_with_tax_price.divide(denom, 2, RoundingMode.HALF_UP);
        return prodPrice;
    }

    private BigDecimal tempTaxableAmount = new BigDecimal("0");
    private BigDecimal taxableDueAmount = new BigDecimal("0");

    private void calculateGlobalTax(BigDecimal _subtotal, BigDecimal qty, boolean isVat) {
        int size = listMapTaxes.size();
        String val = "0";
        BigDecimal temp = new BigDecimal("0");
        BigDecimal _total_tax = new BigDecimal("0");
        for (int j = 0; j < size; j++) {
            val = listMapTaxes.get(j).get("tax_rate");
            if (val == null || val.isEmpty())
                val = "0";
            temp = new BigDecimal(listMapTaxes.get(j).get("tax_rate")).divide(new BigDecimal("100")).setScale(4,
                    RoundingMode.HALF_UP);
            BigDecimal tax_amount = _subtotal.multiply(temp).setScale(4, RoundingMode.HALF_UP);

            _total_tax = _total_tax.add(tax_amount);

            if (_subtotal.compareTo(new BigDecimal("0.00")) != 0) {
                if (isVat)
                    listOrderTaxesTotal.set(j,
                            listOrderTaxesTotal.get(j).add(tax_amount.multiply(qty.abs()).setScale(2, RoundingMode.HALF_UP)));
                else
                    listOrderTaxesTotal.set(j,
                            listOrderTaxesTotal.get(j).add(tax_amount).setScale(4, RoundingMode.HALF_UP));
            }
            DataTaxes tempTaxes = global.listOrderTaxes.get(j);
            tempTaxes.set("tax_amount", listOrderTaxesTotal.get(j).toString());
            global.listOrderTaxes.set(j, tempTaxes);
        }

        if (isVat)
            _total_tax = _total_tax.setScale(2, RoundingMode.HALF_UP).multiply(qty.abs());
        tempTaxableAmount = tempTaxableAmount.add(_total_tax).setScale(4, RoundingMode.HALF_UP);
    }

    private void calculateRetailGlobalTax(BigDecimal _sub_total, String tax_rate, BigDecimal qty, boolean isVat) {
        int size = listMapTaxes.size();
        String val = "0";
        BigDecimal temp = new BigDecimal("0");
        BigDecimal _total_tax = new BigDecimal("0");
        for (int j = 0; j < size; j++) {
            val = listMapTaxes.get(j).get("tax_rate");
            if (val == null || val.isEmpty())
                val = "0";
            temp = new BigDecimal(tax_rate).divide(new BigDecimal("100")).setScale(4, RoundingMode.HALF_UP);
            BigDecimal tax_amount = _sub_total.multiply(temp).setScale(4, RoundingMode.HALF_UP);

            _total_tax = _total_tax.add(tax_amount);

            if (_sub_total.compareTo(new BigDecimal("0.00")) != 0) {
                if (isVat)
                    listOrderTaxesTotal.set(j,
                            listOrderTaxesTotal.get(j).add(tax_amount.multiply(qty)).setScale(2, RoundingMode.HALF_UP));
                else
                    listOrderTaxesTotal.set(j,
                            listOrderTaxesTotal.get(j).add(tax_amount).setScale(4, RoundingMode.HALF_UP));
                // listOrderTaxesTotal.set(j,
                // listOrderTaxesTotal.get(j).add(tax_amount).setScale(2,RoundingMode.HALF_UP));
            }
            DataTaxes tempTaxes = global.listOrderTaxes.get(j);
            tempTaxes.set("tax_amount", listOrderTaxesTotal.get(j).toString());
            global.listOrderTaxes.set(j, tempTaxes);
        }

        if (isVat)
            _total_tax = _total_tax.setScale(2, RoundingMode.HALF_UP).multiply(qty);
        tempTaxableAmount = tempTaxableAmount.add(_total_tax).setScale(4, RoundingMode.HALF_UP);
    }

    public void reCalculate() {
        int size = global.orderProducts.size();
        taxableSubtotal = new BigDecimal("0.00");
        taxableDueAmount = new BigDecimal("0.00");
        tempTaxableAmount = new BigDecimal("0");
        itemsDiscountTotal = new BigDecimal(0);
        setupTaxesHolder();
        boolean isVAT = myPref.getIsVAT();

        if (size > 0) {
            BigDecimal amount = new BigDecimal("0.00");
            BigDecimal discountableAmount = new BigDecimal("0");
            // int qty = 0;
            BigDecimal prodPrice = new BigDecimal("0.00");
            String val;
            int pointsSubTotal = 0, pointsInUse = 0, pointsAcumulable = 0;
            for (int i = 0; i < size; i++) {

                calculateTaxes(i);
                if (myPref.getPreferences(MyPreferences.pref_show_removed_void_items_in_printout)) {
                    String temp = global.orderProducts.get(i).item_void;

                    if (temp.equals("1"))
                        val = "0.00";
                    else {
                        if (isVAT) {
                            val = global.orderProducts.get(i).itemTotalVatExclusive;
                        } else
                            val = global.orderProducts.get(i).itemTotal;
                    }
                } else {
                    if (isVAT) {
                        val = global.orderProducts.get(i).itemTotalVatExclusive;
                    } else
                        val = global.orderProducts.get(i).itemSubtotal;

                }

                if (val == null || val.isEmpty())
                    val = "0.00";

                prodPrice = new BigDecimal(val);

                // val = global.orderProducts.get(i).getSetData("prod_taxValue",
                // true, null);

                if (!global.orderProducts.get(i).isReturned)
                    discountableAmount = discountableAmount.add(prodPrice);
                try {
                    if (global.orderProducts.get(i).discount_value != null
                            && !global.orderProducts.get(i).discount_value.isEmpty())
                        itemsDiscountTotal = itemsDiscountTotal.add(new BigDecimal(global.orderProducts.get(i).discount_value));
                } catch (NumberFormatException e) {

                }
                amount = amount.add(prodPrice);
                // amount = amount.add(new
                // BigDecimal(global.orderProducts.get(i).item));
                pointsSubTotal += Double.parseDouble(global.orderProducts.get(i).prod_price_points);
                pointsAcumulable += Double.parseDouble(global.orderProducts.get(i).prod_value_points);

                if (Boolean.parseBoolean(global.orderProducts.get(i).payWithPoints))
                    pointsInUse += Double.parseDouble(global.orderProducts.get(i).prod_price_points);
            }

            if (itemCount != null)
                itemCount.setText(Integer.toString(size));

            discountable_sub_total = discountableAmount.subtract(Global.rewardChargeAmount);
            // discountable_sub_total = discountable_sub_total.subtract(new
            // BigDecimal(itemsDiscountTotal));
            sub_total = amount.subtract(Global.rewardChargeAmount);
            subTotal.setText(Global.getCurrencyFrmt(Global.getRoundBigDecimal(sub_total)));

            tax_amount = tempTaxableAmount;
            setDiscountValue(discountSelected);
            globalDiscount.setText(Global.getCurrencyFrmt(discount_amount.toString()));
            globalTax.setText(Global.getCurrencyFrmt(Global.getRoundBigDecimal(tax_amount)));

            gran_total = sub_total.abs().subtract(discount_amount.abs()).add(tax_amount.abs())
                    .subtract(itemsDiscountTotal.abs());
            if (OrderingMain_FA.returnItem && OrderingMain_FA.mTransType != Global.TransactionType.RETURN) {
                gran_total = gran_total.negate();
            }
            // gran_total =
            // discountable_sub_total.subtract(discount_amount).add(tax_amount);
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
        // TODO Auto-generated method stub
        reCalculate();
    }
}