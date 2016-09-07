package com.android.emobilepos.ordering;

import android.app.Activity;
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

import com.android.dao.MixMatchDAO;
import com.android.database.PriceLevelHandler;
import com.android.database.ProductsHandler;
import com.android.database.TaxesGroupHandler;
import com.android.database.TaxesHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.models.DataTaxes;
import com.android.emobilepos.models.Discount;
import com.android.emobilepos.models.MixAndMatchDiscount;
import com.android.emobilepos.models.MixMatch;
import com.android.emobilepos.models.MixMatchProductGroup;
import com.android.emobilepos.models.MixMatchXYZProduct;
import com.android.emobilepos.models.OrderProduct;
import com.android.emobilepos.models.PriceLevel;
import com.android.emobilepos.models.Tax;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.TaxesCalculator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.RealmResults;
import io.realm.Sort;

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
        if (taxSelected > 0) {
            TaxesCalculator taxesCalculator = new TaxesCalculator(activity, orderProduct, Global.taxID,
                    taxList.get(taxSelected - 1), dis, discountable_sub_total, itemsDiscountTotal);
            tempTaxableAmount = tempTaxableAmount.add(taxesCalculator.getTaxableAmount());
        }
    }

    private BigDecimal getProductPrice(BigDecimal prod_with_tax_price, BigDecimal tax) {
        BigDecimal denom = new BigDecimal(1).add(tax);
        return prod_with_tax_price.divide(denom, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal tempTaxableAmount = new BigDecimal("0");


    private void calculateMixAndMatch(List<OrderProduct> orderProducts) {
        List<OrderProduct> noMixMatchProducts = new ArrayList<OrderProduct>();
        HashMap<String, MixMatchProductGroup> mixMatchProductGroupHashMap = new HashMap<String, MixMatchProductGroup>();
        for (OrderProduct product : orderProducts) {
            BigDecimal overwrite = product.getOverwrite_price();
            product.resetMixMatch();

            PriceLevelHandler priceLevelHandler = new PriceLevelHandler();
            if (TextUtils.isEmpty(product.getPricesXGroupid()) || product.isVoid()) {
                noMixMatchProducts.add(product);
                continue;
            } else if (overwrite != null || !TextUtils.isEmpty(product.getDiscount_id())) {
                noMixMatchProducts.add(product);
                continue;
            } else {

                product.setMixAndMatchDiscounts(new ArrayList<MixAndMatchDiscount>());
                if (product.getMixMatchOriginalPrice() == null || product.getMixMatchOriginalPrice().compareTo(new BigDecimal(0)) == 0) {
                    product.setMixMatchOriginalPrice(new BigDecimal(product.getProd_price()));
                }
                List<PriceLevel> fixedPriceLevel = priceLevelHandler.getFixedPriceLevel(product.getProd_id());
                MixMatchProductGroup mixMatchProductGroup = mixMatchProductGroupHashMap.get(product.getPricesXGroupid());
                if (mixMatchProductGroup != null) {
                    mixMatchProductGroup.getOrderProducts().add(product);
                    mixMatchProductGroup.setQuantity(mixMatchProductGroup.getQuantity() + new Double(product.getOrdprod_qty()).intValue());
                } else {
                    mixMatchProductGroup = new MixMatchProductGroup();
                    mixMatchProductGroup.setOrderProducts(new ArrayList<OrderProduct>());
                    mixMatchProductGroup.getOrderProducts().add(product);
                    mixMatchProductGroup.setGroupId(product.getPricesXGroupid());
                    mixMatchProductGroup.setPriceLevelId(product.getPricelevel_id());
                    mixMatchProductGroup.setQuantity(new Double(product.getOrdprod_qty()).intValue());
                    mixMatchProductGroupHashMap.put(product.getPricesXGroupid(), mixMatchProductGroup);
                }
            }
        }
        for (Map.Entry<String, MixMatchProductGroup> mixMatchProductGroupEntry : mixMatchProductGroupHashMap.entrySet()) {
            MixMatchProductGroup group = mixMatchProductGroupEntry.getValue();
            RealmResults<MixMatch> mixMatches = MixMatchDAO.getDiscountsBygroupId(group);
            mixMatches.sort("qty", Sort.DESCENDING);
            if (!mixMatches.isEmpty()) {
                MixMatch mixMatch = mixMatches.get(0);
                int mixMatchType = mixMatch.getMixMatchType();
                if (mixMatchType == 1) {
                    applyMixMatch(group, mixMatches);
                } else {
                    if (mixMatches.size() == 2) {
                        orderProducts.clear();
                        orderProducts.addAll(applyXYZMixMatchToGroup(group, mixMatches));
                        orderProducts.addAll(noMixMatchProducts);
                    }
                }
            }
        }
    }

    private List<OrderProduct> applyXYZMixMatchToGroup(MixMatchProductGroup group, RealmResults<MixMatch> mixMatches) {

        List<OrderProduct> orderProducts = new ArrayList<OrderProduct>();
        MixMatch mixMatch1 = mixMatches.get(0);
        MixMatch mixMatch2 = mixMatches.get(1);
        int qtyRequired = mixMatch1.getQty();
        int qtyDiscounted = mixMatch2.getQty();
        Double amount = mixMatch2.getPrice();
        boolean isPercent = mixMatch2.isPercent();
        if (group.getQuantity() < qtyRequired) {
            return group.getOrderProducts();
        }

        int qtyAtRegularPrice;
        int qtyAtDiscountPrice;
        int groupQty = group.getQuantity();
        int completeGroupSize = qtyRequired + qtyDiscounted;
        int numberOfCompletedGroups = groupQty / completeGroupSize;
        int remainingItems = groupQty % completeGroupSize;

        qtyAtRegularPrice = numberOfCompletedGroups * qtyRequired;
        qtyAtDiscountPrice = numberOfCompletedGroups * qtyDiscounted;

        if (remainingItems > qtyRequired) {
            qtyAtRegularPrice += qtyRequired;
            qtyAtDiscountPrice += (remainingItems - qtyRequired);
        } else {
            qtyAtRegularPrice += remainingItems;
        }
        List<MixMatchXYZProduct> mixMatchXYZProducts = new ArrayList<MixMatchXYZProduct>();

        for (OrderProduct product : group.getOrderProducts()) {
            if (mixMatchXYZProducts.contains(product.getProd_id())) {
                int indexOf = mixMatchXYZProducts.indexOf(product.getProd_id());
                MixMatchXYZProduct mmxyz = mixMatchXYZProducts.get(indexOf);
                mmxyz.setQuantity(mmxyz.getQuantity() + Double.valueOf(product.getOrdprod_qty()).intValue());
                mmxyz.getOrderProducts().add(product);
                mmxyz.setPrice(product.getMixMatchOriginalPrice());
                mixMatchXYZProducts.set(indexOf, mmxyz);
            } else {
                MixMatchXYZProduct mmxyz = new MixMatchXYZProduct();
                mmxyz.setProductId(product.getProd_id());
                mmxyz.setQuantity(Double.valueOf(product.getOrdprod_qty()).intValue());
                mmxyz.getOrderProducts().add(product);
                mmxyz.setPrice(product.getMixMatchOriginalPrice());
                mixMatchXYZProducts.add(mmxyz);
            }
        }
        Collections.sort(mixMatchXYZProducts, new Comparator<MixMatchXYZProduct>() {
            @Override
            public int compare(MixMatchXYZProduct a, MixMatchXYZProduct b) {
                return a.getPrice().compareTo(b.getPrice());
            }
        });
        orderProducts.clear();


        boolean isGroupBySKU = myPref.getPreferences(MyPreferences.pref_group_receipt_by_sku);
        for (MixMatchXYZProduct xyzProduct : mixMatchXYZProducts) {
            int prodQty = xyzProduct.getQuantity();
            if (prodQty <= qtyAtRegularPrice) {
                if (isGroupBySKU) {
                    OrderProduct orderProduct = null;
                    try {
                        orderProduct = (OrderProduct) xyzProduct.getOrderProducts().get(0).clone();
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }
                    orderProduct.setProd_price(String.valueOf(xyzProduct.getPrice()));
                    orderProduct.setOrdprod_qty(String.valueOf(prodQty));
                    orderProduct.setMixMatchQtyApplied(prodQty);
                    orderProduct.setItemTotal(String.valueOf(xyzProduct.getPrice().multiply(Global.getBigDecimalNum(orderProduct.getOrdprod_qty()))));
                    orderProduct.setItemSubtotal(String.valueOf(xyzProduct.getPrice().multiply(Global.getBigDecimalNum(orderProduct.getOrdprod_qty()))));
                    orderProducts.add(orderProduct);
                } else {
                    for (OrderProduct orderProduct : xyzProduct.getOrderProducts()) {
                        OrderProduct clone = null;
                        try {
                            clone = (OrderProduct) orderProduct.clone();
                        } catch (CloneNotSupportedException e) {
                            e.printStackTrace();
                        }
                        clone.setOrdprod_qty("1");
                        clone.setProd_price(String.valueOf(xyzProduct.getPrice()));
                        clone.setMixMatchQtyApplied(1);
                        clone.setItemTotal(clone.getProd_price());
                        clone.setItemSubtotal(clone.getProd_price());
                        orderProducts.add(clone);
                    }
                }
                qtyAtRegularPrice -= prodQty;
            } else {
                int regularPriced = qtyAtRegularPrice;
                int discountPriced = prodQty - qtyAtRegularPrice;
                if (regularPriced > 0) {
                    if (isGroupBySKU) {
                        OrderProduct orderProduct = null;
                        try {
                            orderProduct = (OrderProduct) xyzProduct.getOrderProducts().get(0).clone();
                        } catch (CloneNotSupportedException e) {
                            e.printStackTrace();
                        }
                        orderProduct.setProd_price(String.valueOf(xyzProduct.getPrice()));
                        orderProduct.setOrdprod_qty(String.valueOf(regularPriced));
                        orderProduct.setMixMatchQtyApplied(regularPriced);
                        orderProduct.setItemTotal(String.valueOf(xyzProduct.getPrice()
                                .multiply(Global.getBigDecimalNum(orderProduct.getOrdprod_qty()))));
                        orderProduct.setItemSubtotal(String.valueOf(xyzProduct.getPrice()
                                .multiply(Global.getBigDecimalNum(orderProduct.getOrdprod_qty()))));

                        orderProducts.add(orderProduct);
                    } else {
                        for (int i = 0; i < regularPriced; i++) {
                            try {
                                OrderProduct clone = (OrderProduct) xyzProduct.getOrderProducts().get(0).clone();
                                clone.setProd_price(String.valueOf(xyzProduct.getPrice()));
                                clone.setOrdprod_qty("1");
                                clone.setMixMatchQtyApplied(1);
                                clone.setItemTotal(clone.getProd_price());
                                clone.setItemSubtotal(clone.getProd_price());
                                orderProducts.add(clone);
                            } catch (CloneNotSupportedException e) {
                                e.printStackTrace();
                            }
                        }
//                        for (OrderProduct orderProduct : xyzProduct.getOrderProducts()) {
//                            orderProduct.setOrdprod_qty("1");
//                            orderProduct.setMixMatchQtyApplied(1);
//                            global.orderProducts.add(orderProduct);
//                        }
                    }
                    qtyAtRegularPrice -= regularPriced;
                }
                if (discountPriced > 0) {
                    if (isGroupBySKU) {
                        OrderProduct orderProduct = null;
                        try {
                            orderProduct = (OrderProduct) xyzProduct.getOrderProducts().get(0).clone();
                        } catch (CloneNotSupportedException e) {
                            e.printStackTrace();
                        }
                        orderProduct.setOrdprod_qty(String.valueOf(discountPriced));
                        orderProduct.setMixMatchQtyApplied(discountPriced);
                        BigDecimal discountPrice;
                        if (isPercent) {
                            BigDecimal hundred = new BigDecimal(100);
                            BigDecimal percent = (hundred.subtract(new BigDecimal(amount))).divide(hundred);
                            discountPrice = xyzProduct.getPrice().multiply(percent);
                        } else {
                            discountPrice = BigDecimal.valueOf(amount);
                        }
                        orderProduct.setProd_price(String.valueOf(discountPrice));
                        orderProduct.setItemTotal(String.valueOf(Global.getBigDecimalNum(orderProduct.getProd_price()).multiply(Global.getBigDecimalNum(orderProduct.getOrdprod_qty()))));
                        orderProduct.setItemSubtotal(String.valueOf(Global.getBigDecimalNum(orderProduct.getProd_price()).multiply(Global.getBigDecimalNum(orderProduct.getOrdprod_qty()))));

                        orderProducts.add(orderProduct);
                    } else {
                        for (int i = 0; i < discountPriced; i++) {
                            try {
                                OrderProduct clone = (OrderProduct) xyzProduct.getOrderProducts().get(0).clone();
                                clone.setOrdprod_qty("1");
                                clone.setMixMatchQtyApplied(1);

                                BigDecimal discountPrice;
                                if (isPercent) {
                                    BigDecimal hundred = new BigDecimal(100);
                                    BigDecimal percent = (hundred.subtract(new BigDecimal(amount))).divide(hundred);
                                    discountPrice = xyzProduct.getPrice().multiply(percent);
                                } else {
                                    discountPrice = BigDecimal.valueOf(amount);
                                }
                                clone.setProd_price(String.valueOf(discountPrice));
                                clone.setItemTotal(clone.getProd_price());
                                clone.setItemSubtotal(clone.getProd_price());
                                orderProducts.add(clone);
                            } catch (CloneNotSupportedException e) {
                                e.printStackTrace();
                            }
                        }

//                        for (OrderProduct orderProduct : xyzProduct.getOrderProducts()) {
//                            orderProduct.setOrdprod_qty("1");
//                            orderProduct.setMixMatchQtyApplied(1);
//                            BigDecimal discountPrice;
//                            if (isPercent) {
//                                BigDecimal hundred = new BigDecimal(100);
//                                BigDecimal percent = (hundred.subtract(new BigDecimal(amount))).divide(hundred);
//                                discountPrice = new BigDecimal(orderProduct.getProd_price()).multiply(percent);
//                            } else {
//                                discountPrice = BigDecimal.valueOf(amount);
//                            }
//                            orderProduct.setProd_price(String.valueOf(discountPrice));
//                            global.orderProducts.add(orderProduct);
//                        }
                    }
                    qtyAtDiscountPrice -= discountPriced;
                }

            }
        }

        return orderProducts;
    }

    private void applyMixMatch(MixMatchProductGroup group, RealmResults<MixMatch> mixMatches) {
        MixMatch firstMixMatch = mixMatches.get(0);
        if (group.getQuantity() >= firstMixMatch.getQty() && firstMixMatch.isDiscountOddsItems()) {
            for (OrderProduct product : group.getOrderProducts()) {
                if (firstMixMatch.isFixed()) {
                    product.setProd_price(String.valueOf(firstMixMatch.getPrice()));
                } else {
                    double percent = (100 - firstMixMatch.getPrice()) / 100;
                    String prod_price = Global.getRoundBigDecimal(product.getMixMatchOriginalPrice()
                            .multiply(new BigDecimal(percent))).toString();
                    product.setPrices(prod_price, product.getOrdprod_qty());
//
//                    product.itemTotal = Global.getBigDecimalNum(product.prod_price).multiply(new BigDecimal(product.ordprod_qty)).toString();
//                    product.itemSubtotal = Global.getBigDecimalNum(product.prod_price).multiply(new BigDecimal(product.ordprod_qty)).toString();
                }
            }
        } else {
            int itemsRemaining = group.getQuantity();
            for (MixMatch mixMatch : mixMatches) {
                int volumeQty = mixMatch.getQty();
                if (volumeQty <= itemsRemaining) {
                    int itemsToDiscount = volumeQty * (itemsRemaining / volumeQty);
                    for (OrderProduct product : group.getOrderProducts()) {
                        if (product.getMixMatchQtyApplied() < Integer.parseInt(product.getOrdprod_qty())) {
                            int qtyRemainning = Integer.parseInt(product.getOrdprod_qty()) - product.getMixMatchQtyApplied();
                            if (qtyRemainning < itemsToDiscount) {
                                MixAndMatchDiscount mixAndMatchDiscount = new MixAndMatchDiscount();
                                mixAndMatchDiscount.setQty(qtyRemainning);
                                mixAndMatchDiscount.setMixMatch(mixMatch);
                                product.getMixAndMatchDiscounts().add(mixAndMatchDiscount);
                                product.setMixMatchQtyApplied(product.getMixMatchQtyApplied() + qtyRemainning);
                                itemsRemaining -= qtyRemainning;
                                itemsToDiscount -= qtyRemainning;
                            } else {
                                qtyRemainning = itemsToDiscount;
                                MixAndMatchDiscount mixAndMatchDiscount = new MixAndMatchDiscount();
                                mixAndMatchDiscount.setQty(qtyRemainning);
                                mixAndMatchDiscount.setMixMatch(mixMatch);
                                product.getMixAndMatchDiscounts().add(mixAndMatchDiscount);
                                product.setMixMatchQtyApplied(product.getMixMatchQtyApplied() + qtyRemainning);
                                itemsRemaining -= qtyRemainning;
                                itemsToDiscount = 0;
                            }
                            if (itemsRemaining == 0 || itemsToDiscount == 0) {
                                break;
                            }
                        }
                    }
                }
            }
            for (OrderProduct product : group.getOrderProducts()) {
                BigDecimal lineTotal = new BigDecimal(0);
                for (MixAndMatchDiscount discount : product.getMixAndMatchDiscounts()) {
                    MixMatch mixMatch = discount.getMixMatch();
                    BigDecimal discountSplitAmount;
                    if (mixMatch.isFixed()) {
                        discountSplitAmount = new BigDecimal(mixMatch.getPrice())
                                .multiply(BigDecimal.valueOf(discount.getQty()));
                    } else {
                        BigDecimal percent = new BigDecimal(100 - mixMatch.getPrice()).divide(new BigDecimal(100));
                        discountSplitAmount = product.getMixMatchOriginalPrice()
                                .multiply(percent)
                                .multiply(BigDecimal.valueOf(discount.getQty()));
                    }
                    lineTotal = lineTotal.add(discountSplitAmount);
                }
                if (product.getMixMatchQtyApplied() != Integer.parseInt(product.getOrdprod_qty())) {
                    int qtyNotDicounted = Integer.parseInt(product.getOrdprod_qty()) - product.getMixMatchQtyApplied();
                    BigDecimal amountNotDiscounted = new BigDecimal(qtyNotDicounted)
                            .multiply(product.getMixMatchOriginalPrice());
                    lineTotal = lineTotal.add(amountNotDiscounted);
                }
                String prod_price = lineTotal.divide(new BigDecimal(product.getOrdprod_qty()), 4, RoundingMode.HALF_UP).toString();
                product.setPrices(prod_price, product.getOrdprod_qty());
            }
        }
    }

    public void reCalculate(List<OrderProduct> orderProducts) {
        //TODO Temporary fix. Need verify why SDK 5.0 calls with null global and why sdk 4.3 not

        if (global == null) {
            return;
        }

        if (myPref.isMixAnMatch() && orderProducts != null && !orderProducts.isEmpty()) {
            calculateMixAndMatch(orderProducts);
        }

        int size = orderProducts.size();
        taxableSubtotal = new BigDecimal("0.00");
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
                    String temp = orderProducts.get(i).getItem_void();

                    if (temp.equals("1"))
                        val = "0.00";
                    else {
                        if (isVAT) {
                            val = orderProducts.get(i).getItemTotalVatExclusive();
                        } else
                            val = orderProducts.get(i).getItemTotal();
                    }
                } else {
                    if (isVAT) {
                        val = orderProducts.get(i).getItemTotalVatExclusive();
                    } else
                        val = orderProducts.get(i).getItemSubtotal();
                }
                if (val == null || val.isEmpty())
                    val = "0.00";
                prodPrice = new BigDecimal(val);
                discountableAmount = discountableAmount.add(prodPrice);
                try {
                    if (orderProducts.get(i).getDiscount_value() != null
                            && !orderProducts.get(i).getDiscount_value().isEmpty())
                        itemsDiscountTotal = itemsDiscountTotal.add(new BigDecimal(orderProducts.get(i).getDiscount_value()));
                } catch (NumberFormatException e) {
                }
                amount = amount.add(prodPrice);
                pointsSubTotal += Double.parseDouble(orderProducts.get(i).getProd_price_points());
                pointsAcumulable += Double.parseDouble(orderProducts.get(i).getProd_value_points());
                if (Boolean.parseBoolean(orderProducts.get(i).getPayWithPoints()))
                    pointsInUse += Double.parseDouble(orderProducts.get(i).getProd_price_points());
            }
            if (itemCount != null)
                itemCount.setText(String.valueOf(size));
            discountable_sub_total = discountableAmount.subtract(Global.rewardChargeAmount);
            sub_total = amount.subtract(Global.rewardChargeAmount);
            subTotal.setText(Global.getCurrencyFrmt(Global.getRoundBigDecimal(sub_total)));
            tax_amount = new BigDecimal(Global.getRoundBigDecimal(tempTaxableAmount, 2));
            setDiscountValue(discountSelected);
            globalDiscount.setText(Global.getCurrencyFrmt(discount_amount.toString()));
            globalTax.setText(Global.getCurrencyFrmt(Global.getRoundBigDecimal(tax_amount, 2)));
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
            discountable_sub_total = new BigDecimal(0.00);
            sub_total = new BigDecimal(0.00);
            gran_total = new BigDecimal(0.00);
            this.subTotal.setText(activity.getString(R.string.amount_zero_lbl));
            globalTax.setText(activity.getString(R.string.amount_zero_lbl));
            setDiscountValue(discountSelected);
            granTotal.setText(activity.getString(R.string.amount_zero_lbl));
            OrderLoyalty_FR.recalculatePoints("0", "0", "0", gran_total.toString());
        }
    }

    @Override
    public void recalculateTotal() {
        reCalculate(global.orderProducts);
    }
}