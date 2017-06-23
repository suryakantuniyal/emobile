package com.android.emobilepos.ordering;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
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

import com.android.dao.AssignEmployeeDAO;
import com.android.dao.MixMatchDAO;
import com.android.database.ProductsHandler;
import com.android.database.TaxesGroupHandler;
import com.android.database.TaxesHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.models.DataTaxes;
import com.android.emobilepos.models.Discount;
import com.android.emobilepos.models.MixAndMatchDiscount;
import com.android.emobilepos.models.MixMatchProductGroup;
import com.android.emobilepos.models.MixMatchXYZProduct;
import com.android.emobilepos.models.Tax;
import com.android.emobilepos.models.orders.Order;
import com.android.emobilepos.models.orders.OrderProduct;
import com.android.emobilepos.models.orders.OrderTotalDetails;
import com.android.emobilepos.models.realms.AssignEmployee;
import com.android.emobilepos.models.realms.MixMatch;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.crashlytics.android.Crashlytics;

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
    public static String discountID = "", taxID = "";
    public static BigDecimal tax_amount = new BigDecimal("0"), discount_amount = new BigDecimal("0"),
            discount_rate = new BigDecimal("0"), discountable_sub_total = new BigDecimal("0"),
            sub_total = new BigDecimal("0"), gran_total = new BigDecimal("0");
    public static BigDecimal itemsDiscountTotal = new BigDecimal(0);
    private static OrderTotalDetails_FR myFrag;
    private Spinner taxSpinner, discountSpinner;
    private List<Tax> taxList;
    private List<Discount> discountList;
    private int taxSelected, discountSelected;
    private EditText globalDiscount, globalTax, subTotal;
    private TextView granTotal, itemCount;
    private List<HashMap<String, String>> listMapTaxes = new ArrayList<>();
    private Activity activity;
    private MyPreferences myPref;
    private Global global;
    private TaxesHandler taxHandler;
    private TaxesGroupHandler taxGroupHandler;
    private AssignEmployee assignEmployee;
    private boolean isToGo;
    private BigDecimal tempTaxableAmount = new BigDecimal("0");

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

    private static void calculateMixAndMatch(List<OrderProduct> orderProducts, boolean isGroupBySKU) {
        List<OrderProduct> noMixMatchProducts = new ArrayList<>();
        HashMap<String, MixMatchProductGroup> mixMatchProductGroupHashMap = new HashMap<>();
        for (OrderProduct product : orderProducts) {
            BigDecimal overwrite = product.getOverwrite_price();
            product.resetMixMatch();

            if (TextUtils.isEmpty(product.getPricesXGroupid()) || product.isVoid()) {
                noMixMatchProducts.add(product);
            } else if (overwrite != null || !TextUtils.isEmpty(product.getDiscount_id())) {
                noMixMatchProducts.add(product);
            } else {
                product.setMixAndMatchDiscounts(new ArrayList<MixAndMatchDiscount>());
                if (product.getMixMatchOriginalPrice() == null || product.getMixMatchOriginalPrice().compareTo(new BigDecimal(0)) == 0) {
                    product.setMixMatchOriginalPrice(Global.getBigDecimalNum(product.getProd_price()));
                }
                MixMatchProductGroup mixMatchProductGroup = mixMatchProductGroupHashMap.get(product.getPricesXGroupid());
                if (mixMatchProductGroup != null) {
                    mixMatchProductGroup.getOrderProducts().add(product);
                    mixMatchProductGroup.setQuantity(mixMatchProductGroup.getQuantity() + Double.valueOf(product.getOrdprod_qty()).intValue());
                } else {
                    mixMatchProductGroup = new MixMatchProductGroup();
                    mixMatchProductGroup.setOrderProducts(new ArrayList<OrderProduct>());
                    mixMatchProductGroup.getOrderProducts().add(product);
                    mixMatchProductGroup.setGroupId(product.getPricesXGroupid());
                    mixMatchProductGroup.setPriceLevelId(product.getPricelevel_id());
                    mixMatchProductGroup.setQuantity(Double.valueOf(product.getOrdprod_qty()).intValue());
                    mixMatchProductGroupHashMap.put(product.getPricesXGroupid(), mixMatchProductGroup);
                }
            }
        }
        orderProducts.clear();
        for (Map.Entry<String, MixMatchProductGroup> mixMatchProductGroupEntry : mixMatchProductGroupHashMap.entrySet()) {
            MixMatchProductGroup group = mixMatchProductGroupEntry.getValue();
            RealmResults<MixMatch> mixMatches = MixMatchDAO.getDiscountsBygroupId(group);
            if (!mixMatches.isEmpty()) {
                MixMatch mixMatch = mixMatches.get(0);
                int mixMatchType = mixMatch.getMixMatchType();
                if (mixMatchType == 1) {
                    mixMatches = mixMatches.sort("qty", Sort.DESCENDING);
                    orderProducts.addAll(applyMixMatch(group, mixMatches));
                } else {
                    if (mixMatches.size() == 2) {
                        mixMatches = mixMatches.sort("xyzSequence", Sort.ASCENDING);
                        orderProducts.addAll(applyXYZMixMatchToGroup(group, mixMatches, isGroupBySKU));
                    } else {
                        orderProducts.addAll(group.getOrderProducts());
                    }
                }
            } else if (!group.getOrderProducts().isEmpty()) {
                orderProducts.addAll(group.getOrderProducts());
            }
        }
        orderProducts.addAll(noMixMatchProducts);
    }

    private static List<OrderProduct> applyXYZMixMatchToGroup(MixMatchProductGroup group, RealmResults<MixMatch> mixMatches, boolean isGroupBySKU) {

        List<OrderProduct> orderProducts = new ArrayList<>();
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
        List<MixMatchXYZProduct> mixMatchXYZProducts = new ArrayList<>();

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


        for (MixMatchXYZProduct xyzProduct : mixMatchXYZProducts) {
            int prodQty = xyzProduct.getQuantity();
            if (prodQty <= qtyAtRegularPrice) {
                if (isGroupBySKU) {
                    OrderProduct orderProduct = null;
                    try {
                        orderProduct = (OrderProduct) xyzProduct.getOrderProducts().get(0).clone();
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                        Crashlytics.logException(e);
                    }
                    orderProduct.setProd_price(String.valueOf(xyzProduct.getPrice()));
                    orderProduct.setOrdprod_qty(String.valueOf(prodQty));
                    orderProduct.setMixMatchQtyApplied(prodQty);
                    orderProduct.setItemTotal(String.valueOf(xyzProduct.getPrice().multiply(Global.getBigDecimalNum(orderProduct.getOrdprod_qty()))));
//                    orderProduct.setItemSubtotal(String.valueOf(xyzProduct.getPrice().multiply(Global.getBigDecimalNum(orderProduct.getOrdprod_qty()))));
                    orderProducts.add(orderProduct);
                } else {
                    for (OrderProduct orderProduct : xyzProduct.getOrderProducts()) {
                        OrderProduct clone = null;
                        try {
                            clone = (OrderProduct) orderProduct.clone();
                        } catch (CloneNotSupportedException e) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                        }
                        clone.setOrdprod_qty("1");
                        clone.setProd_price(String.valueOf(xyzProduct.getPrice()));
                        clone.setMixMatchQtyApplied(1);
                        clone.setItemTotal(clone.getProd_price());
//                        clone.setItemSubtotal(clone.getProd_price());
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
                            Crashlytics.logException(e);
                        }
                        orderProduct.setProd_price(String.valueOf(xyzProduct.getPrice()));
                        orderProduct.setOrdprod_qty(String.valueOf(regularPriced));
                        orderProduct.setMixMatchQtyApplied(regularPriced);
                        orderProduct.setItemTotal(String.valueOf(xyzProduct.getPrice()
                                .multiply(Global.getBigDecimalNum(orderProduct.getOrdprod_qty()))));
//                        orderProduct.setItemSubtotal(String.valueOf(xyzProduct.getPrice()
//                                .multiply(Global.getBigDecimalNum(orderProduct.getOrdprod_qty()))));

                        orderProducts.add(orderProduct);
                    } else {
                        for (int i = 0; i < regularPriced; i++) {
                            try {
                                OrderProduct clone = (OrderProduct) xyzProduct.getOrderProducts().get(0).clone();
                                clone.setProd_price(String.valueOf(xyzProduct.getPrice()));
                                clone.setOrdprod_qty("1");
                                clone.setMixMatchQtyApplied(1);
                                clone.setItemTotal(clone.getProd_price());
//                                clone.setItemSubtotal(clone.getProd_price());
                                orderProducts.add(clone);
                            } catch (CloneNotSupportedException e) {
                                e.printStackTrace();
                                Crashlytics.logException(e);
                            }
                        }

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
                            Crashlytics.logException(e);
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
//                        orderProduct.setItemSubtotal(String.valueOf(Global.getBigDecimalNum(orderProduct.getProd_price()).multiply(Global.getBigDecimalNum(orderProduct.getOrdprod_qty()))));

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
//                                clone.setItemSubtotal(clone.getProd_price());
                                orderProducts.add(clone);
                            } catch (CloneNotSupportedException e) {
                                e.printStackTrace();
                                Crashlytics.logException(e);
                            }
                        }

                    }
                    qtyAtDiscountPrice -= discountPriced;
                }

            }
        }

        return orderProducts;
    }

    private static List<OrderProduct> applyMixMatch(MixMatchProductGroup group, RealmResults<MixMatch> mixMatches) {
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
                        BigDecimal percent = new BigDecimal(100 - mixMatch.getPrice()).divide(new BigDecimal(100), 4, RoundingMode.HALF_UP);
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
                String prod_price = lineTotal.divide(new BigDecimal(product.getOrdprod_qty()), 2, RoundingMode.HALF_UP).toString();
                product.setPrices(prod_price, product.getOrdprod_qty());
            }
        }
        return group.getOrderProducts();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.order_total_details_layout, container, false);
        assignEmployee = AssignEmployeeDAO.getAssignEmployee(false);
        isToGo = ((OrderingMain_FA) getActivity()).isToGo;
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

        if (!myPref.getIsTablet() && leftHolder != null) {
            leftHolder.setVisibility(View.GONE);
        } else if (myPref.getIsTablet() && leftHolder != null)
            itemCount = (TextView) view.findViewById(R.id.itemCount);
        initSpinners();
        return view;
    }

    public void initSpinners() {

        listMapTaxes = new ArrayList<>();
        List<String> taxes = new ArrayList<>();
        List<String> discount = new ArrayList<>();
        String custTaxCode;
        if (myPref.isCustSelected()) {
            custTaxCode = myPref.getCustTaxCode();
            if (custTaxCode == null) {
                custTaxCode = assignEmployee.getTaxDefault();
            }
        } else if (Global.isFromOnHold && !TextUtils.isEmpty(Global.taxID))
            custTaxCode = Global.taxID;
        else {
            custTaxCode = assignEmployee.getTaxDefault();
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
                if (!TextUtils.isEmpty(custTaxCode) && custTaxCode.equals(taxList.get(i).getTaxId())) {
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
        List<String[]> taxArr = new ArrayList<>();
        for (Tax tax : taxList) {
            String[] arr = new String[5];
            arr[0] = tax.getTaxName();
            arr[1] = tax.getTaxId();
            arr[2] = tax.getTaxRate();
            arr[3] = tax.getTaxType();
            taxArr.add(arr);
        }
        MySpinnerAdapter taxAdapter = new MySpinnerAdapter(activity, android.R.layout.simple_spinner_item, taxes, taxArr, true);
        List<String[]> discountArr = new ArrayList<>();
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
                if (global.order != null && global.order.getOrderProducts() != null) {
                    reCalculate(global.order.getOrderProducts());
                }
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
            if (!myPref.isRetailTaxes()) {
                listMapTaxes = taxHandler.getTaxDetails(taxID, "");
                if (listMapTaxes.size() > 0 && listMapTaxes.get(0).get("tax_type").equals("G")) {
                    listMapTaxes = taxGroupHandler.getIndividualTaxes(listMapTaxes.get(0).get("tax_id"),
                            listMapTaxes.get(0).get("tax_code_id"));
                    setupTaxesHolder();
                }
            } else {
                listMapTaxes.clear();
                HashMap<String, String> mapTax = new HashMap<>();
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
                            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
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
                        .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                BigDecimal total = discountable_sub_total.subtract(itemsDiscountTotal);
                discount_amount = total.multiply(discount_rate).setScale(2, RoundingMode.HALF_UP);
            }
        }


        globalDiscount.setText(Global.getCurrencyFormat(frmt.format(discount_amount)));

        Global.discountPosition = position;
        Global.discountAmount = discount_amount;
    }

//    private void calculateTaxes(OrderProduct orderProduct) {
//        Discount dis = null;
//        if (discountSelected > 0) {
//            dis = discountList.get(discountSelected - 1);
//        }
//        if (taxSelected > 0) {
//            TaxesCalculator taxesCalculator = new TaxesCalculator(activity, orderProduct, Global.taxID,
//                    taxList.get(taxSelected - 1), dis, discountable_sub_total, itemsDiscountTotal);
//            tempTaxableAmount = tempTaxableAmount.add(taxesCalculator.getTaxableAmount());
//            getOrderingMainFa().setListOrderTaxes(taxesCalculator.getListOrderTaxes());
//        }
//    }

    private OrderingMain_FA getOrderingMainFa() {
        return (OrderingMain_FA) getActivity();
    }

    private void setupTaxesHolder() {
        int size = listMapTaxes.size();
        getOrderingMainFa().setListOrderTaxes(new ArrayList<DataTaxes>());
        DataTaxes tempTaxes;
        for (int i = 0; i < size; i++) {
            tempTaxes = new DataTaxes();
            tempTaxes.setTax_name(listMapTaxes.get(i).get("tax_name"));
            tempTaxes.setOrd_id("");
            tempTaxes.setTax_amount("0");
            tempTaxes.setTax_rate(listMapTaxes.get(i).get("tax_rate"));
            getOrderingMainFa().getListOrderTaxes().add(tempTaxes);
        }
    }

    public synchronized void reCalculate(List<OrderProduct> orderProducts) {
        //TODO Temporary fix. Need verify why SDK 5.0 calls with null global and why sdk 4.3 not

        if (global == null) {
            return;
        }
        new ReCalculate().execute(orderProducts);
//        if (myPref.isMixAnMatch() && orderProducts != null && !orderProducts.isEmpty()) {
//            boolean isGroupBySKU = myPref.isGroupReceiptBySku(isToGo);//myPref.getPreferences(MyPreferences.pref_group_receipt_by_sku) && isToGo;
//            calculateMixAndMatch(orderProducts, isGroupBySKU);
//        }
//        Discount discount = discountSelected > 0 ? discountList.get(discountSelected - 1) : null;
//        global.order.setRetailTaxes(myPref.isRetailTaxes());
//        global.order.ord_globalDiscount = String.valueOf(discount_amount);
//        global.order.setListOrderTaxes(getOrderingMainFa().getListOrderTaxes());
//        Tax tax = taxSelected > 0 ? taxList.get(taxSelected - 1) : null;
//        if (myPref.isRetailTaxes()) {
//            global.order.setRetailTax(getActivity(), taxID);
//        }
//        OrderTotalDetails totalDetails = global.order.getOrderTotalDetails(discount, tax, assignEmployee.isVAT(), getActivity());
//        gran_total = Global.getRoundBigDecimal(totalDetails.getGranTotal(), 2);
//        sub_total = totalDetails.getSubtotal();
//        tax_amount = Global.getRoundBigDecimal(totalDetails.getTax(), 2);
//        discount_amount = totalDetails.getGlobalDiscount();
//        subTotal.setText(Global.getCurrencyFrmt(String.valueOf(sub_total)));
//        granTotal.setText(Global.getCurrencyFrmt(String.valueOf(gran_total)));
//        globalTax.setText(Global.getCurrencyFrmt(String.valueOf(tax_amount)));
//        globalDiscount.setText(Global.getCurrencyFrmt(String.valueOf(discount_amount)));
    }

    private class ReCalculate extends AsyncTask<List<OrderProduct>, Void, Void> {
        private List<OrderProduct> orderProducts;

        @Override
        protected synchronized Void doInBackground(List<OrderProduct>... params) {
            orderProducts = params[0];
                List<OrderProduct> orderProducts = params[0];
                if (myPref.isMixAnMatch() && orderProducts != null && !orderProducts.isEmpty()) {
                    boolean isGroupBySKU = myPref.isGroupReceiptBySku(isToGo);//myPref.getPreferences(MyPreferences.pref_group_receipt_by_sku) && isToGo;
                    calculateMixAndMatch(orderProducts, isGroupBySKU);
                }
                Discount discount = discountSelected > 0 ? discountList.get(discountSelected - 1) : null;
                global.order.setRetailTaxes(myPref.isRetailTaxes());
                global.order.ord_globalDiscount = String.valueOf(discount_amount);
                global.order.setListOrderTaxes(getOrderingMainFa().getListOrderTaxes());
                Tax tax = taxSelected > 0 ? taxList.get(taxSelected - 1) : null;
                if (myPref.isRetailTaxes()) {
                    global.order.setRetailTax(getActivity(), taxID);
                }
                OrderTotalDetails totalDetails = global.order.getOrderTotalDetails(discount, tax, assignEmployee.isVAT(), getActivity());
                gran_total = Global.getRoundBigDecimal(totalDetails.getGranTotal(), 2);
                sub_total = totalDetails.getSubtotal();
                tax_amount = Global.getRoundBigDecimal(totalDetails.getTax(), 2);
                discount_amount = totalDetails.getGlobalDiscount();
                return null;
        }

        @Override
        protected synchronized void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            subTotal.setText(Global.getCurrencyFrmt(String.valueOf(sub_total)));
            granTotal.setText(Global.getCurrencyFrmt(String.valueOf(gran_total)));
            globalTax.setText(Global.getCurrencyFrmt(String.valueOf(tax_amount)));
            globalDiscount.setText(Global.getCurrencyFrmt(String.valueOf(discount_amount)));
            OrderingMain_FA mainFa = (OrderingMain_FA) getActivity();
            mainFa.enableCheckoutButton();
        }
    }

    @Override
    public void recalculateTotal() {
        reCalculate(global.order.getOrderProducts());
    }

    private class MySpinnerAdapter extends ArrayAdapter<String> {
        List<String> leftData = null;
        List<String[]> rightData = null;
        boolean isTax = false;
        private Activity context;

        MySpinnerAdapter(Activity activity, int resource, List<String> left, List<String[]> right,
                         boolean isTax) {
            super(activity, resource, left);
            this.context = activity;
            this.leftData = left;
            this.rightData = right;
            this.isTax = isTax;
        }

        int getDiscountIdPosition(String discountId) {
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

        void setValues(TextView taxValue, int position) {
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
}