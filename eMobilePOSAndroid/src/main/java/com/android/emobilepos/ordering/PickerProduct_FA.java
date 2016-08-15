package com.android.emobilepos.ordering;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.FragmentActivity;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.dao.OrderProductAttributeDAO;
import com.android.dao.UomDAO;
import com.android.database.PriceLevelHandler;
import com.android.database.ProductsAttrHandler;
import com.android.database.ProductsHandler;
import com.android.database.TaxesHandler;
import com.android.database.VolumePricesHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.ShowProductImageActivity;
import com.android.emobilepos.models.Discount;
import com.android.emobilepos.models.OrderProduct;
import com.android.emobilepos.models.ProductAttribute;
import com.android.emobilepos.models.UOM;
import com.android.support.GenerateNewID;
import com.android.support.GenerateNewID.IdType;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.TerminalDisplay;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import io.realm.RealmObject;
import io.realm.RealmResults;

public class PickerProduct_FA extends FragmentActivity implements OnClickListener, OnItemClickListener {

    private boolean hasBeenCreated = false;
    private Activity activity;
    private Global global;


    private final int SEC_QTY = 3, SEC_CMT = 5, SEC_UOM = 4, SEC_PRICE_LEV = 6, SEC_DISCOUNT = 7, SEC_OTHER_TYPES = 9, SEC_ADDITIONAL_INFO = 10;
    private String[] leftTitle, leftTitle2;
    private String[] rightTitle = new String[]{"ONE (Default)", "", "", "0.00 <No Discount>"};
    private final int INDEX_UOM = 0, INDEX_CMT = 1, INDEX_PRICE_LEVEL = 2, INDEX_DISCOUNT = 3, OFFSET = 4, MAIN_OFFSET = 3;


    private ListViewAdapter lv_adapter;
    private Bundle extras;

    //Loading image library
    private ImageLoader imageLoader;
    private DisplayImageOptions options;


    private String qty_picked = "1";

    private String defaultVal = "0.00";
    private String defVal = "0";
    private String _ordprod_comment = "";
    private String taxTotal = defaultVal, prod_taxId;
    private String prLevTotal;
    private String taxAmount = defVal, disAmount = defVal, disTotal = defaultVal, discount_id;
    private String uomName = "", uomID = "";
    private boolean isFixed = true;
    private String prodID;
    private boolean isModify;
    private int modifyOrderPosition = 0;
    private String imgURL;

    private MyPreferences myPref;

    private BigDecimal uomMultiplier = new BigDecimal("1.0");
    private boolean discountIsTaxable = false, discountWasSelected = false;
    private String basePrice;
    private ListView lView;
    private String priceLevelID = "", priceLevelName = "";
    private int pricelevel_position = 0, discount_position = 0, tax_position = 0, uom_position;
    private String prod_type = "";

    private VolumePricesHandler volPriceHandler;
    private ProductsAttrHandler prodAttrHandler;
    private AlertDialog promptDialog;

    private LinkedHashMap<String, List<String>> attributesMap;
    private String[] attributesKey;
    private LinkedHashMap<String, String> attributesSelected;


    private TextView headerProductID, headerOnHand;
    private ImageView headerImage;


    private String ordProdAttr = "";
    private boolean isFromAddon = false;
    OrderProduct orderProduct = new OrderProduct();
    public static PickerProduct_FA instance;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        myPref = new MyPreferences(this);
        if (!myPref.getIsTablet())                        //reset to default layout (not as dialog)
        {
            super.setTheme(R.style.AppTheme);
        }


        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        instance = this;


        setContentView(R.layout.catalog_picker_layout);

        this.setFinishOnTouchOutside(true);
        if (OrderingMain_FA.returnItem) {
            TextView tvHeaderTitle = (TextView) findViewById(R.id.HeaderTitle);
            tvHeaderTitle.setText(R.string.return_title);
            tvHeaderTitle.setBackgroundColor(Color.RED);
        }


        activity = this;
        global = (Global) getApplication();


        myPref = new MyPreferences(activity);
        Intent intent = activity.getIntent();
        extras = intent.getExtras();
        //extras = activity.getIntent().getExtras();

        LayoutInflater inflater = LayoutInflater.from(this);
        View header = inflater.inflate(R.layout.catalog_picker_header, (ViewGroup) activity.findViewById(R.id.header_layout_root));
        lView = (ListView) findViewById(R.id.pickerLV);
        headerProductID = (TextView) header.findViewById(R.id.pickerHeaderID);
        headerOnHand = (TextView) header.findViewById(R.id.pickerHeaderQty);
        headerImage = (ImageView) header.findViewById(R.id.itemHeaderImg);
        headerImage.setOnClickListener(this);

        Button headerAddButton = (Button) header.findViewById(R.id.pickerHeaderButton);
        headerAddButton.setOnClickListener(this);


        volPriceHandler = new VolumePricesHandler(activity);

        leftTitle = new String[]{getString(R.string.cat_picker_uom), getString(R.string.cat_picker_comments), getString(R.string.cat_picker_price_level),
                getString(R.string.cat_picker_discount)};


        leftTitle2 = new String[]{getString(R.string.cat_picker_attributes)
                , getString(R.string.cat_picker_view_other_types), getString(R.string.additional_info)};


        imageLoader = ImageLoader.getInstance();
        imageLoader.init(ImageLoaderConfiguration.createDefault(activity));
        options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.loading_image).cacheInMemory(false).cacheOnDisc(true)
                .showImageForEmptyUri(R.drawable.no_image).build();


        isModify = extras.getBoolean("isModify", false);
        isFromAddon = extras.getBoolean("isFromAddon", false);
        setOrderProductValues();
        verifyIfModify(headerAddButton, header);

        headerProductID.setText(prodID);
        imageLoader.displayImage(imgURL, headerImage, options);

//        OrdProdAttrList_DB ordProdAttrDB = new OrdProdAttrList_DB(activity);
        ordProdAttr = "";
        global.ordProdAttrPending = new ArrayList<ProductAttribute>(OrderProductAttributeDAO.getByProdId(prodID, true));

        for (ProductAttribute attribute : global.ordProdAttrPending) {
            ordProdAttr += attribute.getAttributeName() + "\n";
        }
        setupTax();
        lView.addHeaderView(header);
        lv_adapter = new ListViewAdapter(activity);
        lView.setAdapter(lv_adapter);
        lView.setOnItemClickListener(this);
        hasBeenCreated = true;
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.itemHeaderImg: //View item image
                Intent intent = new Intent(activity, ShowProductImageActivity.class);
                intent.putExtra("url", imgURL);
                startActivity(intent);
                break;
            case R.id.pickerHeaderButton: //Add product
                addProductToOrder();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case SEC_QTY:
                showQtyDlog(view);
                break;
            case SEC_CMT:
                showCMTDlog(view);
                break;
            case SEC_UOM:
            case SEC_PRICE_LEV:
            case SEC_DISCOUNT:
                showListViewDlog(position);
                break;
            case SEC_OTHER_TYPES:
                Intent results = new Intent();
                results.putExtra("prod_name", extras.getString("prod_name"));
                activity.setResult(9, results);

                activity.finish();
                break;
            case SEC_ADDITIONAL_INFO:
                Gson gson = new GsonBuilder()
                        .setExclusionStrategies(new ExclusionStrategy() {
                            @Override
                            public boolean shouldSkipField(FieldAttributes f) {
                                return f.getDeclaringClass().equals(RealmObject.class);
                            }

                            @Override
                            public boolean shouldSkipClass(Class<?> clazz) {
                                return false;
                            }
                        })
                        .create();
                Intent intent = new Intent(activity, OrderAttributes_FA.class);
                intent.putExtra("prod_id", prodID);
                if (isModify) {
                    intent.putExtra("isModify", isModify);
                    intent.putExtra("ordprod_id", global.orderProducts.get(modifyOrderPosition).ordprod_id);
                }
                startActivity(intent);
                break;

        }
    }

    private void setOrderProductValues() {
        orderProduct.prod_id = extras.getString("prod_id");
        orderProduct.prod_sku = extras.getString("prod_sku");
        orderProduct.prod_upc = extras.getString("prod_upc");
        orderProduct.ordprod_name = extras.getString("prod_name");
        orderProduct.prod_price = extras.getString("prod_price");
        orderProduct.imgURL = extras.getString("url");
        orderProduct.prod_type = extras.getString("prod_type");
        orderProduct.onHand = extras.getString("prod_on_hand");
        orderProduct.assignedSeat = extras.getString("selectedSeatNumber");
        orderProduct.prod_istaxable = extras.getString("prod_istaxable");
        orderProduct.ordprod_desc = extras.getString("prod_desc");
        orderProduct.prod_taxcode = extras.getString("prod_taxcode");
        orderProduct.tax_type = extras.getString("prod_taxtype");
        orderProduct.cat_id = extras.getString("cat_id");
        orderProduct.assignedSeat = extras.getString("selectedSeatNumber");
        orderProduct.prod_price_points = String.valueOf(extras.getInt("prod_price_points"));
        orderProduct.prod_value_points = String.valueOf(extras.getInt("prod_value_points"));

        if (Global.isConsignment) {
            orderProduct.consignment_qty = extras.getString("consignment_qty");

        }
    }

    private void verifyIfModify(Button headerAddButton, View header) {
        if (isModify) {
            headerAddButton.setText(R.string.modify);
            modifyOrderPosition = extras.getInt("modify_position");
            imgURL = global.orderProducts.get(modifyOrderPosition).imgURL;
            prodID = global.orderProducts.get(modifyOrderPosition).prod_id;
            headerOnHand.setText(global.orderProducts.get(modifyOrderPosition).onHand);
            basePrice = global.orderProducts.get(modifyOrderPosition).overwrite_price;
            prod_type = global.orderProducts.get(modifyOrderPosition).prod_type;

            updateSavedDetails();

            _ordprod_comment = global.orderProducts.get(modifyOrderPosition).ordprod_comment;
            rightTitle[INDEX_CMT] = _ordprod_comment;
        } else {
            imgURL = orderProduct.imgURL;
            headerOnHand.setText(orderProduct.onHand);
            prodID = orderProduct.prod_id;
            prod_type = orderProduct.prod_type;
            basePrice = orderProduct.prod_price;
            if (basePrice == null || basePrice.isEmpty())
                basePrice = "0.0";
            prLevTotal = Global.formatNumToLocale(Double.parseDouble(basePrice));

            prodAttrHandler = new ProductsAttrHandler(activity);
            attributesMap = prodAttrHandler.getAttributesMap(orderProduct.ordprod_name);
            attributesKey = attributesMap.keySet().toArray(new String[attributesMap.size()]);
            attributesSelected = prodAttrHandler.getDefaultAttributes(prodID);
            int attributesSize = attributesMap.size();
            for (int i = 0; i < attributesSize; i++) {
                addAttributeButton(header, attributesKey[i]);
            }


            if (myPref.isCustSelected()) {
                PriceLevelHandler plHandler = new PriceLevelHandler();
                List<String[]> _listPriceLevel = plHandler.getFixedPriceLevel(prodID);

                int i = 0;
                for (String[] arr : _listPriceLevel) {
                    if (arr[1].equals(myPref.getCustPriceLevel())) {
                        pricelevel_position = i;
                        priceLevelName = arr[0];
                        priceLevelID = arr[1];
                    }
                    i++;
                }
            }
        }
    }


    private void setupTax() {
        TaxesHandler taxHandler = new TaxesHandler(activity);
        if (myPref.getPreferences(MyPreferences.pref_retail_taxes)) {
            if (!Global.taxID.isEmpty()) {
                taxAmount = taxHandler.getTaxRate(Global.taxID, orderProduct.tax_type, Double.parseDouble(basePrice));
                prod_taxId = orderProduct.tax_type;
            } else {
                taxAmount = taxHandler.getTaxRate(orderProduct.prod_taxcode, orderProduct.tax_type, Double.parseDouble(basePrice));
                prod_taxId = orderProduct.prod_taxcode;
            }
        } else {
            if (!Global.taxID.isEmpty()) {
                taxAmount = taxHandler.getTaxRate(Global.taxID, "", Double.parseDouble(basePrice));
                prod_taxId = Global.taxID;
            } else {
                taxAmount = taxHandler.getTaxRate(orderProduct.prod_taxcode, "", Double.parseDouble(basePrice));
                prod_taxId = orderProduct.prod_taxcode;
            }
        }
    }


    private void updateSavedDetails() {
        PriceLevelHandler plHandler = new PriceLevelHandler();

        List<String[]> _listPriceLevel = plHandler.getFixedPriceLevel(prodID);

        ProductsHandler handler = new ProductsHandler(activity);
        List<Discount> discounts = handler.getDiscounts();
        ArrayList<String[]> _listDiscounts = new ArrayList<String[]>();
        for (Discount discount : discounts) {
            String[] arr = new String[5];
            arr[0] = discount.getProductName();
            arr[1] = discount.getProductDiscountType();
            arr[2] = discount.getProductPrice();
            arr[3] = discount.getTaxCodeIsTaxable();
            arr[4] = discount.getProductId();
            _listDiscounts.add(arr);
        }

        List<UOM> uoms = UomDAO.getByProdId(prodID);
        ArrayList<String[]> _listUOM = new ArrayList<String[]>();
        for (UOM uom : uoms) {
            String[] arr = new String[3];
            arr[0] = uom.getUomName();
            arr[1] = uom.getUomId();
            arr[2] = uom.getUomConversion();
            _listUOM.add(arr);
        }
        int plSize = _listPriceLevel.size();
        int disSize = _listDiscounts.size();
        int uomSize = uoms.size();

        int maxSize = plSize;
        if (maxSize < disSize)
            maxSize = disSize;
        if (maxSize < uomSize)
            maxSize = uomSize;

        int _plIndex = 0, _disIndex = 0, _uomIndex = 0;
        for (int i = 0; i < maxSize; i++) {
            if (i < plSize && _listPriceLevel.get(i)[1].equals(global.orderProducts.get(modifyOrderPosition).pricelevel_id)) {
                _plIndex = i + 1;
            }
            if (i < disSize && _listDiscounts.get(i)[4].equals(global.orderProducts.get(modifyOrderPosition).discount_id)) {
                _disIndex = i + 1;
            }
            if (i < uomSize && uoms.get(i).getUomId().equals(global.orderProducts.get(modifyOrderPosition).uom_id)
                    ) {
                _uomIndex = i + 1;
            }
        }

        setTextView(_plIndex, INDEX_PRICE_LEVEL + OFFSET, _listPriceLevel);
        setTextView(_disIndex, INDEX_DISCOUNT + OFFSET, _listDiscounts);
        setTextView(_uomIndex, INDEX_UOM + OFFSET, _listUOM);
    }

    private void addAttributeButton(View header, String tag) {

        LinearLayout test = (LinearLayout) header.findViewById(R.id.catalog_picker_attributes_holder);
        LayoutInflater inf = LayoutInflater.from(activity);

        View vw = inf.inflate(R.layout.catalog_picker_attributes_adapter, null);
        vw.setTag(tag);
        vw.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));


        TextView attributeTitle = (TextView) vw.findViewById(R.id.attribute_title);
        TextView attributeValue = (TextView) vw.findViewById(R.id.attribute_value);

        attributeTitle.setText(tag);
        attributeValue.setText(attributesSelected.get(tag));

        test.addView(vw);

        vw.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                generateAttributePrompt(v);
            }
        });
    }


    private void generateAttributePrompt(View view) {
        final String key = (String) view.getTag();

        final TextView attributeValue = (TextView) view.findViewById(R.id.attribute_value);

        ListView listView = new ListView(activity);
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);

        final String[] val = attributesMap.get(key).toArray(new String[attributesMap.get(key).size()]);

        listView.setAdapter(new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, val) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View row = super.getView(position, convertView, parent);
                // Here we get the textview and set the color
                TextView tv = (TextView) row.findViewById(android.R.id.text1);
                tv.setTextColor(Color.BLACK);
                return row;
            }
        });

        listView.setBackgroundColor(Color.WHITE);
        listView.setCacheColorHint(Color.WHITE);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                                    long arg3) {

                attributeValue.setText(val[position]);
                attributesSelected.put(key, val[position]);
                refreshAttributeProduct(prodAttrHandler.getNewAttributeProduct(orderProduct.ordprod_name, attributesKey, attributesSelected));
                promptDialog.dismiss();
            }
        });


        dialogBuilder.setView(listView);

        dialogBuilder.setInverseBackgroundForced(true);
        promptDialog = dialogBuilder.create();

        promptDialog.show();
    }

    private void refreshAttributeProduct(Cursor myCursor) {

        OrderProduct orderProduct = new OrderProduct();
        orderProduct.prod_id = myCursor.getString(myCursor.getColumnIndex("_id"));
        orderProduct.ordprod_name = myCursor.getString(myCursor.getColumnIndex("prod_name"));

        String tempPrice = myCursor.getString(myCursor.getColumnIndex("volume_price"));
        if (tempPrice == null || tempPrice.isEmpty()) {
            tempPrice = myCursor.getString(myCursor.getColumnIndex("pricelevel_price"));
            if (tempPrice == null || tempPrice.isEmpty()) {
                tempPrice = myCursor.getString(myCursor.getColumnIndex("chain_price"));

                if (tempPrice == null || tempPrice.isEmpty())
                    tempPrice = myCursor.getString(myCursor.getColumnIndex("master_price"));
            }
        }

        orderProduct.prod_price = tempPrice;
        orderProduct.ordprod_desc = myCursor.getString(myCursor.getColumnIndex("prod_desc"));

        tempPrice = myCursor.getString(myCursor.getColumnIndex("local_prod_onhand"));
        if (tempPrice == null || tempPrice.isEmpty())
            tempPrice = myCursor.getString(myCursor.getColumnIndex("master_prod_onhand"));
        if (tempPrice.isEmpty())
            tempPrice = "0";
        orderProduct.onHand = tempPrice;

        orderProduct.imgURL = myCursor.getString(myCursor.getColumnIndex("prod_img_name"));
        orderProduct.prod_istaxable = myCursor.getString(myCursor.getColumnIndex("prod_istaxable"));
        orderProduct.prod_type = myCursor.getString(myCursor.getColumnIndex("prod_type"));

        orderProduct.prod_price_points = myCursor.getString(myCursor.getColumnIndex("prod_price_points"));
        if (orderProduct.prod_price_points == null || orderProduct.prod_price_points.isEmpty())
            orderProduct.prod_price_points = "0";
        orderProduct.prod_value_points = myCursor.getString(myCursor.getColumnIndex("prod_value_points"));
        if (orderProduct.prod_value_points == null || orderProduct.prod_value_points.isEmpty())
            orderProduct.prod_value_points = "0";


        imgURL = orderProduct.imgURL;
        headerOnHand.setText(orderProduct.onHand);
        prodID = orderProduct.prod_id;
        prod_type = orderProduct.prod_type;
        basePrice = orderProduct.prod_price;
        if (basePrice == null || basePrice.isEmpty())
            basePrice = "0.0";

        prLevTotal = Global.formatNumToLocale(Double.parseDouble(basePrice));


        headerProductID.setText(prodID);
        imageLoader.displayImage(imgURL, headerImage, options);
        lv_adapter = new ListViewAdapter(activity);
        lView.setAdapter(lv_adapter);
    }


    private void addProductToOrder() {
        OrderProduct product = global.orderProducts.size() == 0 ? null : global.orderProducts.get(modifyOrderPosition);
        List<OrderProduct> products = new ArrayList<OrderProduct>();
        if (product != null) {
            products.add(product);
        }
        if (!OrderingMain_FA.isRequiredAttributeConmpleted(global, products)) {
            Global.showPrompt(activity, R.string.dlog_title_error, activity.getString(R.string.dlog_msg_required_attributes) + "\n\n" + ordProdAttr);
        } else {
            double onHandQty = 0;
            if (!headerOnHand.getText().toString().isEmpty())
                onHandQty = Double.parseDouble(headerOnHand.getText().toString());

            if (OrderingMain_FA.mTransType != Global.TransactionType.RETURN) {
                if (OrderingMain_FA.returnItem || (isModify && global.orderProducts.get(modifyOrderPosition).isReturned)) {
                    qty_picked = new BigDecimal(qty_picked).negate().toString();
                }
            }
            double selectedQty = Double.parseDouble(qty_picked);
            double newQty = 0;
            String addedQty = global.qtyCounter.get(prodID);


            if (addedQty != null && !addedQty.isEmpty())
                newQty = Double.parseDouble(addedQty) + selectedQty;


            if ((myPref.getPreferences(MyPreferences.pref_limit_products_on_hand) && !prod_type.equals("Service")
                    && ((Global.ord_type == Global.OrderType.SALES_RECEIPT || Global.ord_type == Global.OrderType.INVOICE) &&
                    ((!isModify && (selectedQty > onHandQty || newQty > onHandQty)) || (isModify && selectedQty > onHandQty))
            )) || (Global.isConsignment && !prod_type.equals("Service") && !validConsignment(selectedQty, onHandQty)))

            {
                Global.showPrompt(activity, R.string.dlog_title_error, activity.getString(R.string.limit_onhand));
            } else {
                if (!isModify)
                    preValidateSettings();
                else
                    modifyProduct(modifyOrderPosition);

                activity.setResult(2);
                activity.finish();
            }
        }
    }


    private void modifyProduct(int position) {
        OrderProduct orderedProducts = global.orderProducts.get(position);

        String val = qty_picked;
        BigDecimal sum = new BigDecimal(val);

        if (myPref.getPreferences(MyPreferences.pref_allow_decimal_quantities))
            global.qtyCounter.put(prodID, sum.setScale(2, RoundingMode.HALF_UP).toString());
        else
            global.qtyCounter.put(prodID, sum.setScale(0, RoundingMode.HALF_UP).toString());
        orderProduct.prod_istaxable = orderedProducts.prod_istaxable;
        BigDecimal total = sum.multiply(Global.getBigDecimalNum(prLevTotal).multiply(uomMultiplier)).setScale(2, RoundingMode.HALF_UP);
        calculateTaxDiscount(total);

        BigDecimal productPriceLevelTotal = Global.getBigDecimalNum(prLevTotal);
        orderedProducts.ordprod_qty = val;
        orderedProducts.overwrite_price = Global.getRoundBigDecimal(productPriceLevelTotal.multiply(uomMultiplier));

        orderedProducts.prod_taxValue = new BigDecimal(taxTotal);
        if (Double.parseDouble(orderedProducts.overwrite_price) <= Double.parseDouble(disTotal)) {
            disTotal = orderedProducts.overwrite_price;
        }
        orderedProducts.discount_value = disTotal;


        orderedProducts.pricelevel_id = priceLevelID;
        orderedProducts.priceLevelName = priceLevelName;


        // for calculating taxes and discount at receipt
        orderedProducts.discount_id = discount_id;
        orderedProducts.taxAmount = taxAmount;
        orderedProducts.taxTotal = taxTotal;
        orderedProducts.disAmount = disAmount;
        orderedProducts.disTotal = disTotal;

        orderedProducts.tax_position = Integer.toString(tax_position);
        orderedProducts.discount_position = Integer.toString(discount_position);
        orderedProducts.pricelevel_position = Integer.toString(pricelevel_position);
        orderedProducts.uom_position = Integer.toString(uom_position);
        orderedProducts.ordprod_comment = _ordprod_comment;
        orderedProducts.prod_price_updated = "0";

        BigDecimal itemTotal = total.subtract(new BigDecimal(disTotal));


        if (discountIsTaxable) {
            orderedProducts.discount_is_taxable = "1";
        } else
            orderedProducts.discount_is_taxable = "0";


        if (isFixed)
            orderedProducts.discount_is_fixed = "1";
        else
            orderedProducts.discount_is_fixed = "0";

        orderedProducts.itemTotal = itemTotal.toString();
        orderedProducts.itemSubtotal = total.toString();

        if (OrderingMain_FA.returnItem) {
            OrderingMain_FA.returnItem = !OrderingMain_FA.returnItem;
            OrderingMain_FA.switchHeaderTitle(OrderingMain_FA.returnItem, "Return");
        }

    }

    private void showQtyDlog(View v) {
        final Dialog dlog = new Dialog(activity, R.style.Theme_TransparentTest);
        dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dlog.setCancelable(true);
        dlog.setContentView(R.layout.dlog_field_single_layout);

        final EditText viewField = (EditText) dlog.findViewById(R.id.dlogFieldSingle);

        if (myPref.getPreferences(MyPreferences.pref_allow_decimal_quantities))
            viewField.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        else
            viewField.setInputType(InputType.TYPE_CLASS_NUMBER);
        final TextView qty = (TextView) v.findViewById(R.id.pickerQty);
        viewField.setText(qty.getText().toString());
        viewField.setSelection(qty.getText().toString().length());

        TextView viewTitle = (TextView) dlog.findViewById(R.id.dlogTitle);
        TextView viewMsg = (TextView) dlog.findViewById(R.id.dlogMessage);
        viewTitle.setText(R.string.dlog_title_confirm);
        viewMsg.setText(R.string.dlog_msg_enter_qty);
        Button btnCancel = (Button) dlog.findViewById(R.id.btnCancelDlogSingle);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlog.dismiss();
            }
        });
        Button btnOk = (Button) dlog.findViewById(R.id.btnDlogSingle);
        btnOk.setText(R.string.button_ok);
        btnOk.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String txt = viewField.getText().toString();
                if (!txt.isEmpty() && Double.parseDouble(txt) > 0) {
                    qty.setText(txt);
                    qty_picked = txt;
                    lv_adapter.updateVolumePrice(new BigDecimal(txt));
                    lv_adapter.notifyDataSetChanged();
                }
                dlog.dismiss();
            }
        });
        dlog.show();
    }

    private void showListViewDlog(final int type) {
        final Dialog dlg = new Dialog(activity);
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(R.layout.dialog_listview_layout, null, false);
        dlg.setContentView(view);
        ListView dlgListView = (ListView) dlg.findViewById(R.id.dlgListView);
        List<String[]> listData_LV = new ArrayList<String[]>();
        RealmResults<UOM> uoms;
        switch (type) {
            case SEC_UOM://UoM
            {
                listData_LV = new ArrayList<String[]>();
                uoms = UomDAO.getByProdId(prodID);
                for (UOM uom : uoms) {
                    String[] arr = new String[3];
                    arr[0] = uom.getUomName();
                    arr[1] = uom.getUomId();
                    arr[2] = uom.getUomConversion();
                    listData_LV.add(arr);
                }
                break;
            }
            case SEC_PRICE_LEV: // Price Level
            {
                if (!myPref.getPreferences(MyPreferences.pref_block_price_level_change)) {
                    PriceLevelHandler handler1 = new PriceLevelHandler();
                    listData_LV = handler1.getFixedPriceLevel(prodID);

                } else {
                    listData_LV.clear();
                    Toast.makeText(activity, "Changing the Price Level is currently not allowed.", Toast.LENGTH_LONG).show();
                }
                break;
            }
            case SEC_DISCOUNT: // Discount
            {
                ProductsHandler handler = new ProductsHandler(activity);
                List<Discount> discounts = handler.getDiscounts();
                listData_LV = new ArrayList<String[]>();
                for (Discount discount : discounts) {
                    String[] arr = new String[5];
                    arr[0] = discount.getProductName();
                    arr[1] = discount.getProductDiscountType();
                    arr[2] = discount.getProductPrice();
                    arr[3] = discount.getTaxCodeIsTaxable();
                    arr[4] = discount.getProductId();
                    listData_LV.add(arr);
                }
                break;
            }
        }
        DialogLVAdapter dlgAdapter = new DialogLVAdapter(activity, type, listData_LV);
        dlgListView.setAdapter(dlgAdapter);
        dlg.show();
        final List<String[]> finalListData_LV = listData_LV;
        dlgListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                setTextView(position, type, finalListData_LV);
                lView.invalidateViews();
                dlg.dismiss();
            }
        });
    }

    private void showCMTDlog(View v) {
        final Dialog dialog = new Dialog(activity, R.style.Theme_TransparentTest);
        dialog.setContentView(R.layout.comments_dialog_layout);
        dialog.setCancelable(true);
        EditText cmt = (EditText) dialog.findViewById(R.id.commentEditText);
        final TextView txt = (TextView) v.findViewWithTag(leftTitle[INDEX_CMT]);
        Button done = (Button) dialog.findViewById(R.id.doneButton);
        Button clear = (Button) dialog.findViewById(R.id.clearButton);
        cmt.setText(txt.getText().toString());
        cmt.setSelection(txt.getText().toString().length());
        done.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                EditText curComment = (EditText) dialog.findViewById(R.id.commentEditText);
                rightTitle[INDEX_CMT] = curComment.getText().toString();
                _ordprod_comment = curComment.getText().toString().trim();
                txt.setText(rightTitle[INDEX_CMT]);
                dialog.dismiss();
            }
        });
        clear.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                EditText curComment = (EditText) dialog.findViewById(R.id.commentEditText);
                curComment.setText("");
                rightTitle[INDEX_CMT] = curComment.getText().toString();
                txt.setText(rightTitle[INDEX_CMT]);
                _ordprod_comment = "";
            }
        });
        dialog.show();
    }

    private boolean validConsignment(double selectedQty, double onHandQty) {
        if (Global.isConsignment) {
            String temp = global.qtyCounter.get(prodID);
            if (temp != null && !isModify) {
                double val = Double.parseDouble(temp);
                selectedQty += val;
            }

            if (Global.consignmentType == Global.OrderType.CONSIGNMENT_FILLUP && (onHandQty <= 0 || selectedQty > onHandQty))
                return false;
            else if (Global.consignmentType != Global.OrderType.CONSIGNMENT_FILLUP && !Global.custInventoryMap.containsKey(prodID))
                return false;
            else if (Global.consignmentType != Global.OrderType.CONSIGNMENT_FILLUP) {
                if (Global.consignmentType == Global.OrderType.ORDER && selectedQty > Double.parseDouble(Global.custInventoryMap.get(prodID)[2]))
                    return false;
                else if (Global.consignmentType == Global.OrderType.CONSIGNMENT_RETURN) {
                    if (Global.consignment_qtyCounter != null && Global.consignment_qtyCounter.containsKey(prodID))//verify rack
                    {
                        double rackQty = Double.parseDouble(Global.consignment_qtyCounter.get(prodID));
                        double origQty = Double.parseDouble(Global.custInventoryMap.get(prodID)[2]);
                        if (rackQty == origQty || (rackQty + selectedQty > origQty))
                            return false;
                    }
                } else if (Global.consignmentType == Global.OrderType.CONSIGNMENT_PICKUP && selectedQty > Double.parseDouble(Global.custInventoryMap.get(prodID)[2]))
                    return false;

            }

        }
        return true;
    }

    private void preValidateSettings() {
        MyPreferences myPref = new MyPreferences(activity);

        if (myPref.getPreferences(MyPreferences.pref_group_receipt_by_sku)) {
            int size = global.orderProducts.size();
            int index = 0;
            boolean found = false;

            for (int i = 0; i < size; i++) {
                if (global.orderProducts.get(i).prod_id.equals(prodID)) {
                    index = i;
                    found = true;
                    break;
                }
            }

            if (found) {
                String value = global.qtyCounter.get(prodID);
                double previousQty = 0.0;
                if (value != null && !value.isEmpty())
                    previousQty = Double.parseDouble(value);
                double sum = Global.formatNumFromLocale(qty_picked) + previousQty;
                if (myPref.getPreferences(MyPreferences.pref_allow_decimal_quantities)) {
                    value = Global.formatNumber(true, sum);
                    global.orderProducts.get(index).ordprod_qty = value;
                    global.qtyCounter.put(prodID, Double.toString(sum));
                } else {
                    value = Global.formatNumber(false, sum);
                    global.orderProducts.get(index).ordprod_qty = value;
                    global.qtyCounter.put(prodID, Integer.toString((int) sum));
                }
                updateSKUProduct(index);
            } else {
                generateNewProduct();
            }
        } else {
            generateNewProduct();
        }
    }

    private void generateNewProduct() {
        OrderProduct ord = new OrderProduct();

        String val = qty_picked;
        BigDecimal num = new BigDecimal(val);
        BigDecimal sum = num.add(getQty(prodID)).setScale(4, RoundingMode.HALF_EVEN);
        BigDecimal productPriceLevelTotal = Global.getBigDecimalNum(prLevTotal);

        if (OrderingMain_FA.returnItem)
            ord.isReturned = true;

        if (isFromAddon) {
            productPriceLevelTotal = productPriceLevelTotal.add(new BigDecimal(Double.toString(Global.addonTotalAmount)));
        }

        BigDecimal total = num.multiply(productPriceLevelTotal.multiply(uomMultiplier)).setScale(2, RoundingMode.HALF_UP);

        calculateTaxDiscount(total);                    // calculate taxes and discount


        ord.prod_istaxable = orderProduct.prod_istaxable;


        if (!myPref.getPreferences(MyPreferences.pref_allow_decimal_quantities)) {
            val = Integer.toString((int) Double.parseDouble(val));
            global.qtyCounter.put(prodID, sum.setScale(0, RoundingMode.HALF_UP).toString());
        } else {
            global.qtyCounter.put(prodID, sum.setScale(2, RoundingMode.HALF_UP).toString());
        }


        // add order to db
        ord.ordprod_qty = val;
        ord.ordprod_name = orderProduct.ordprod_name;
        ord.ordprod_desc = orderProduct.ordprod_desc;
        ord.prod_id = prodID;
        ord.overwrite_price = Global.getRoundBigDecimal(productPriceLevelTotal);
        ord.onHand = orderProduct.onHand;
        ord.imgURL = orderProduct.imgURL;
        ord.cat_id = orderProduct.cat_id;
        ord.assignedSeat = orderProduct.assignedSeat;
        ord.prod_sku = orderProduct.prod_sku;
        ord.prod_upc = orderProduct.prod_upc;


        BigDecimal pricePoints = new BigDecimal(orderProduct.prod_price_points);
        BigDecimal valuePoints = new BigDecimal(orderProduct.prod_value_points);

        pricePoints = pricePoints.multiply(num);
        valuePoints = valuePoints.multiply(num);

        ord.prod_price_points = pricePoints.toString();
        ord.prod_value_points = valuePoints.toString();

        // Still need to do add the appropriate tax/discount value
        ord.prod_taxValue = new BigDecimal(taxTotal);
        if (Double.parseDouble(ord.overwrite_price) <= Double.parseDouble(disTotal)) {
            disTotal = ord.overwrite_price;
        }
        ord.discount_value = disTotal;
        ord.prod_taxtype = orderProduct.tax_type;


        // for calculating taxes and discount at receipt
        ord.prod_taxId = prod_taxId;
        ord.discount_id = discount_id;
        ord.taxAmount = taxAmount;
        ord.taxTotal = taxTotal;
        ord.disAmount = disAmount;
        ord.disTotal = disTotal;

        ord.pricelevel_id = priceLevelID;
        ord.priceLevelName = priceLevelName;

        ord.prod_price = productPriceLevelTotal.toString();
        ord.tax_position = Integer.toString(tax_position);
        ord.discount_position = Integer.toString(discount_position);
        ord.pricelevel_position = Integer.toString(pricelevel_position);
        ord.uom_position = Integer.toString(uom_position);
        ord.ordprod_comment = _ordprod_comment;

        ord.prod_type = prod_type;

        //Add UOM attributes to the order
        ord.uom_name = uomName;
        ord.uom_id = uomID;
        ord.uom_conversion = uomMultiplier.toString();

        if (discountIsTaxable) {
            ord.discount_is_taxable = "1";
        }
        if (isFixed)
            ord.discount_is_fixed = "1";
        else
            ord.discount_is_fixed = "0";

        BigDecimal itemTotal = total.abs().subtract(Global.getBigDecimalNum(disTotal).abs());
        if (OrderingMain_FA.returnItem && OrderingMain_FA.mTransType != Global.TransactionType.RETURN) {
            itemTotal = itemTotal.negate();
        }
        //double itemTotal = total - toDouble(disTotal);

        ord.itemTotal = itemTotal.toString();
        ord.itemSubtotal = total.toString();

        GenerateNewID generator = new GenerateNewID(activity);

        if (!Global.isFromOnHold && Global.lastOrdID.isEmpty()) {
            Global.lastOrdID = generator.getNextID(IdType.ORDER_ID);

        }
        ord.ord_id = Global.lastOrdID;


        if (global.orderProducts == null) {
            global.orderProducts = new ArrayList<OrderProduct>();
        }

        UUID uuid = UUID.randomUUID();
        String randomUUIDString = uuid.toString();


        ord.ordprod_id = randomUUIDString;

        ord.requiredProductAttributes = new ArrayList<>();
        int size = global.ordProdAttr.size();
        for (int i = 0; i < size; i++) {
            if (global.ordProdAttr.get(i).getProductId() == null || global.ordProdAttr.get(i).getProductId().isEmpty()) {
                global.ordProdAttr.get(i).setProductId(randomUUIDString);
                ord.requiredProductAttributes.add(global.ordProdAttr.get(i));
            }
        }

        if (isFromAddon) {
            Global.addonTotalAmount = 0;

            if (Global.addonSelectionMap == null)
                Global.addonSelectionMap = new HashMap<String, HashMap<String, String[]>>();
            if (Global.orderProductAddonsMap == null)
                Global.orderProductAddonsMap = new HashMap<String, List<OrderProduct>>();

            if (global.addonSelectionType.size() > 0) {
                StringBuilder sb = new StringBuilder();
                Global.addonSelectionMap.put(randomUUIDString, global.addonSelectionType);
                Global.orderProductAddonsMap.put(randomUUIDString, global.orderProductAddons);


                sb.append(ord.ordprod_desc);
                int tempSize = global.orderProductAddons.size();
                ord.addonsProducts = new ArrayList<OrderProduct>(global.orderProductAddons);

                for (int i = 0; i < tempSize; i++) {

                    if (global.orderProductAddons.get(i).isAdded.equals("0"))//Not added
                        sb.append("\n[NO ").append(global.orderProductAddons.get(i).ordprod_name).append("]");
                    else
                        sb.append("\n[").append(global.orderProductAddons.get(i).ordprod_name).append("]");

                }
                ord.ordprod_desc = sb.toString();
                ord.hasAddons = "1";

                global.orderProductAddons = new ArrayList<OrderProduct>();

            }
        }
        global.orderProducts.add(ord);


//        if (myPref.isSam4s(true, true)) {
        String row1 = ord.ordprod_name;
        String row2 = Global.formatDoubleStrToCurrency(ord.overwrite_price);
        TerminalDisplay.setTerminalDisplay(myPref, row1, row2);

//        } else if (myPref.isPAT100()) {
//            String row1 = ord.ordprod_name;
//            String row2 = Global.formatDoubleStrToCurrency(ord.overwrite_price);
//            TerminalDisplay.setTerminalDisplay(myPref, row1, row2);
//        } else if (myPref.isESY13P1()) {
//            String row1 = ord.ordprod_name;
//            String row2 = Global.formatDoubleStrToCurrency(ord.overwrite_price);
//            TerminalDisplay.setTerminalDisplay(myPref, row1, row2);
//        }

        if (OrderingMain_FA.returnItem) {
            OrderingMain_FA.returnItem = !OrderingMain_FA.returnItem;
            OrderingMain_FA.switchHeaderTitle(OrderingMain_FA.returnItem, "Return");
        }
    }

    private void calculateTaxDiscount(BigDecimal total) {
        if (!isFixed) {
            BigDecimal val = total.multiply(Global.getBigDecimalNum(disAmount)).setScale(4, RoundingMode.HALF_UP);
            val = val.divide(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP);
            disTotal = val.toString();
        } else {
            disTotal = Double.toString(Global.formatNumFromLocale(disAmount));
        }

        BigDecimal tempSubTotal = total, tempTaxTotal = new BigDecimal("0");

        if (orderProduct.prod_istaxable.equals("1")) {
            if (discountWasSelected) // discount has been selected verify if it
            // is taxable or not
            {
                if (discountIsTaxable) {
                    BigDecimal temp = new BigDecimal(taxAmount).divide(new BigDecimal("100")).setScale(4, RoundingMode.HALF_UP);
                    BigDecimal tax1 = tempSubTotal.subtract(new BigDecimal(disTotal)).multiply(temp).setScale(2, RoundingMode.HALF_UP);
                    tempTaxTotal = tax1;
                    taxTotal = tax1.toString();

                } else {
                    BigDecimal temp = new BigDecimal(taxAmount).divide(new BigDecimal("100")).setScale(4, RoundingMode.HALF_UP);
                    BigDecimal tax1 = tempSubTotal.multiply(temp).setScale(2, RoundingMode.HALF_UP);
                    tempTaxTotal = tax1;
                    taxTotal = tax1.toString();

                }
            } else {
                BigDecimal temp = new BigDecimal(taxAmount).divide(new BigDecimal("100")).setScale(4, RoundingMode.HALF_UP);
                BigDecimal tax1 = tempSubTotal.multiply(temp).setScale(2, RoundingMode.HALF_UP);
                taxTotal = tax1.toString();
            }
        }
        if (tempTaxTotal.compareTo(new BigDecimal("0")) < -1)
            taxTotal = Double.toString(0.0);
    }


    public void setTextView(int position, int type, List<String[]> listData_LV) {
        switch (type) {
            case INDEX_UOM + OFFSET: {
                uom_position = position;
                StringBuilder sb = new StringBuilder();
                if (position == 0) {
                    sb.append("ONE (Default)");
                    uomMultiplier = new BigDecimal("1");
                    uomName = "";
                    uomID = "";
                } else {
                    sb.append(listData_LV.get(position - 1)[0]).append(" <").append(listData_LV.get(position - 1)[2]).append(">");
                    uomMultiplier = Global.getBigDecimalNum(listData_LV.get(position - 1)[2]);
                    uomName = listData_LV.get(position - 1)[0];
                    uomID = listData_LV.get(position - 1)[1];
                }

                rightTitle[INDEX_UOM] = sb.toString();
                break;
            }
            case INDEX_PRICE_LEVEL + OFFSET: // Price Level
            {
                pricelevel_position = position;
                StringBuilder sb = new StringBuilder();
                if (position == 0) {
                    priceLevelID = "";
                    priceLevelName = "";
                    sb.append(Global.formatDoubleToCurrency(Double.parseDouble(basePrice))).append(" <Base Price>");
                    rightTitle[INDEX_PRICE_LEVEL] = sb.toString();
                    prLevTotal = Global.formatNumToLocale(Double.parseDouble(basePrice));

                } else {
                    priceLevelName = listData_LV.get(position - 1)[0];
                    priceLevelID = listData_LV.get(position - 1)[1];
                    sb.append(Global.formatDoubleStrToCurrency(listData_LV.get(position - 1)[2]));
                    sb.append(" <").append(listData_LV.get(position - 1)[0]).append(">");
                    rightTitle[INDEX_PRICE_LEVEL] = sb.toString();
                    prLevTotal = Global.formatNumToLocale(Double.parseDouble(listData_LV.get(position - 1)[2]));
                }
                break;
            }
            case INDEX_DISCOUNT + OFFSET: // Discount
            {
                discount_position = position;
                StringBuilder sb = new StringBuilder();
                if (position == 0) {
                    rightTitle[INDEX_DISCOUNT] = "$0.00 <No Discount>";
                    disAmount = "0";
                    disTotal = "0.00";
                    isFixed = true;
                    discountWasSelected = false;
                    discount_id = "";
                } else if (listData_LV.get(position - 1)[1].equals("Fixed")) {

                    discount_id = listData_LV.get(position - 1)[4];
                    sb.append(Global.formatDoubleStrToCurrency(listData_LV.get(position - 1)[2])).append(" <")
                            .append(listData_LV.get(position - 1)[0]).append(">");
                    rightTitle[INDEX_DISCOUNT] = sb.toString();

                    disAmount = Global.formatNumToLocale(Double.parseDouble(listData_LV.get(position - 1)[2]));
                    isFixed = true;
                    discountWasSelected = true;
                    discountIsTaxable = listData_LV.get(position - 1)[3].equals("1");
                } else {
                    discount_id = listData_LV.get(position - 1)[4];
                    sb.append(listData_LV.get(position - 1)[2]).append("%").append(" <").append(listData_LV.get(position - 1)[0]).append(">");
                    rightTitle[INDEX_DISCOUNT] = sb.toString();

                    disAmount = Global.formatNumToLocale(Double.parseDouble(listData_LV.get(position - 1)[2]));
                    isFixed = false;
                    discountWasSelected = true;
                    discountIsTaxable = listData_LV.get(position - 1)[3].equals("1");
                }
                break;
            }
        }
    }

    private void updateSKUProduct(int position) {
        OrderProduct orderedProducts = global.orderProducts.get(position);

        String newPickedOrders = orderedProducts.ordprod_qty;
        BigDecimal sum;
        if (myPref.getPreferences(MyPreferences.pref_allow_decimal_quantities))
            sum = Global.getBigDecimalNum(newPickedOrders);
        else
            sum = Global.getBigDecimalNum(newPickedOrders);

        if (global.orderProducts.get(position).isReturned)
            sum = sum.negate();

        BigDecimal total = sum.multiply(Global.getBigDecimalNum(prLevTotal)).setScale(2, RoundingMode.HALF_UP);
        calculateTaxDiscount(total);

        orderedProducts.overwrite_price = prLevTotal;
        orderedProducts.prod_taxValue = new BigDecimal(taxTotal);
        orderedProducts.discount_value = disTotal;


        // for calculating taxes and discount at receipt
        orderedProducts.taxAmount = taxAmount;
        orderedProducts.taxTotal = taxTotal;
        orderedProducts.disAmount = disAmount;
        orderedProducts.disTotal = disTotal;

        BigDecimal itemTotal = total.subtract(Global.getBigDecimalNum(disTotal));


        if (discountIsTaxable) {
            orderedProducts.discount_is_taxable = "1";
        } else
            orderedProducts.discount_is_taxable = "0";


        if (isFixed)
            orderedProducts.discount_is_fixed = "1";
        else
            orderedProducts.discount_is_fixed = "0";

        orderedProducts.prod_price_updated = "0";

        orderedProducts.itemTotal = itemTotal.toString();
        orderedProducts.itemSubtotal = total.toString();

        if (OrderingMain_FA.returnItem) {
            OrderingMain_FA.returnItem = !OrderingMain_FA.returnItem;
            OrderingMain_FA.switchHeaderTitle(OrderingMain_FA.returnItem, "Return");
        }
    }

    public BigDecimal getQty(String id) {
        Global global = (Global) activity.getApplication();
        String value = global.qtyCounter.get(id);
        return Global.getBigDecimalNum(value);
    }


    //------------------------Custom adapter for Dialog and ListView------------------------
    private class DialogLVAdapter extends BaseAdapter {

        private LayoutInflater myInflater;
        private int listType;
        private List<String[]> listData_LV;

        public DialogLVAdapter(Activity activity, int pos, List<String[]> listData_lv) {
            listData_LV = listData_lv;
            myInflater = LayoutInflater.from(activity);
            listType = pos;
        }

        @Override
        public int getCount() {
            return listData_LV.size() + 1;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            int type = getItemViewType(position);

            if (convertView == null) {
                holder = new ViewHolder();
                convertView = myInflater.inflate(R.layout.dialog_listview_adapter, null);

                holder.leftText = (TextView) convertView.findViewById(R.id.leftText);
                holder.rightText = (TextView) convertView.findViewById(R.id.rightText);

                setValues(holder, position, type);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
                setValues(holder, position, type);
            }
            return convertView;
        }

        public void setValues(ViewHolder holder, int position, int type) {
            switch (type) {
                case 0: {
                    if (listType == INDEX_PRICE_LEVEL + OFFSET) // Price Level
                    {
                        holder.leftText.setText(R.string.base_price_lbl);
                        holder.rightText.setText(Global.formatDoubleStrToCurrency(basePrice));

                    } else if (listType == INDEX_DISCOUNT + OFFSET) // Discount
                    {
                        holder.leftText.setText(R.string.no_discount_lbl);

                        holder.rightText.setText(R.string.amount_zero_lbl);
                    } else if (listType == INDEX_UOM + OFFSET) {
                        holder.leftText.setText(R.string.none_uppercase_lbl);
                        holder.rightText.setText("1");
                        uomMultiplier = new BigDecimal("1");
                        uomName = "";
                        uomID = "";
                    }
                    break;
                }
                case 1: {
                    holder.leftText.setText(listData_LV.get(position - 1)[0]);

                    if (listType == INDEX_PRICE_LEVEL + OFFSET) // Price Level
                    {
                        String total = Global.formatDoubleStrToCurrency(listData_LV.get(position - 1)[2]);
                        holder.rightText.setText(total);
                    } else if (listType == INDEX_DISCOUNT + OFFSET) // discount
                    {
                        if (listData_LV.get(position - 1)[1].equals("Fixed")) {
                            holder.rightText.setText(Global.formatDoubleStrToCurrency(listData_LV.get(position - 1)[2]));

                        } else {
                            holder.rightText.setText(listData_LV.get(position - 1)[2] + "%");
                        }

                    } else if (listType == INDEX_UOM + OFFSET) {
                        String multiplier = listData_LV.get(position - 1)[2];

                        uomName = listData_LV.get(position - 1)[0];
                        uomID = listData_LV.get(position - 1)[1];
                        holder.rightText.setText(multiplier);
                    }
                    break;
                }
            }
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return 0;
            }
            return 1;
        }

        public class ViewHolder {
            TextView leftText;
            TextView rightText;
        }

    }


    public class ListViewAdapter extends BaseAdapter implements Filterable {
        private String itemName;
        private String itemConsignmentQty;
        private LayoutInflater myInflater;

        public ListViewAdapter(Context context) {
            myInflater = LayoutInflater.from(context);
            if (!isModify) {
                itemName = orderProduct.ordprod_name;
                itemConsignmentQty = orderProduct.consignment_qty != null && !orderProduct.consignment_qty.isEmpty() ? "Orig. Qty: " + orderProduct.consignment_qty : "Orig. Qty: " + "0";
                rightTitle[INDEX_PRICE_LEVEL] = Global.formatDoubleToCurrency(Double.parseDouble(basePrice)) + " <Base Price>";
            } else {
                int pos = modifyOrderPosition;
                itemName = global.orderProducts.get(pos).ordprod_name;

                rightTitle[INDEX_PRICE_LEVEL] = global.orderProducts.get(pos).overwrite_price;
                taxTotal = global.orderProducts.get(pos).taxTotal;
                disTotal = global.orderProducts.get(pos).disTotal;

                disAmount = global.orderProducts.get(pos).disAmount;
                taxAmount = global.orderProducts.get(pos).taxAmount;
                rightTitle[INDEX_DISCOUNT] = disAmount;
            }
        }

        @Override
        public int getViewTypeCount() {
            return 4;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0 || position == 1)                                //Product name field,consignment
            {
                return 0;
            } else if (position == 2)                        // +/- quantity
            {
                return 1;
            } else if (position > 2 && position < (leftTitle.length + MAIN_OFFSET))        //comments-price level - discount - special tax
            {
                return 2;
            } else if ((position >= (leftTitle.length + MAIN_OFFSET)) && (position <= (leftTitle.length + leftTitle2.length + MAIN_OFFSET))) // Attributes - View types
            {
                return 3;
            }
            return 4;
        }

        @Override
        public int getCount() {
            return leftTitle.length + leftTitle2.length + MAIN_OFFSET;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            int type = getItemViewType(position);
            if (convertView == null) {
                holder = new ViewHolder();
                switch (type) {
                    case 0: {
                        convertView = myInflater.inflate(R.layout.catalog_picker_adapter1, null);
                        holder.leftText = (TextView) convertView.findViewById(R.id.itemNameLabel);
                        holder.leftSubtitle = (TextView) convertView.findViewById(R.id.itemName);

                        if (position == 0) {
                            holder.leftText.setText(R.string.catalog_name);
                            holder.leftSubtitle.setText(itemName);
                            holder.leftText.setVisibility(View.VISIBLE);
                            holder.leftSubtitle.setVisibility(View.VISIBLE);
                        } else if (Global.isConsignment) {
                            holder.leftText.setText(R.string.consignment);
                            holder.leftSubtitle.setText(itemConsignmentQty);
                        } else {
                            holder.leftText.setVisibility(View.GONE);
                            holder.leftSubtitle.setVisibility(View.GONE);
                        }

                        break;
                    }
                    case 1: {
                        convertView = myInflater.inflate(R.layout.catalog_picker_adapter2, null);

                        holder.rightText = (TextView) convertView.findViewById(R.id.pickerQty);
                        holder.add = (Button) convertView.findViewById(R.id.addItemQty);
                        holder.delete = (Button) convertView.findViewById(R.id.deleteItemQty);

                        holder.add.setFocusable(false);
                        holder.delete.setFocusable(false);
                        BigDecimal newQty = Global.getBigDecimalNum(holder.rightText.getText().toString());

                        updateVolumePrice(newQty);
                        if (isModify) {
                            if (global.orderProducts.get(modifyOrderPosition).isReturned)
                                qty_picked = new BigDecimal(global.orderProducts.get(modifyOrderPosition).ordprod_qty).negate().toString();
                            else
                                qty_picked = global.orderProducts.get(modifyOrderPosition).ordprod_qty;
                            holder.rightText.setText(qty_picked);
                        }

                        holder.add.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                String val = holder.rightText.getText().toString();
                                int qty = Integer.parseInt(val);
                                qty += 1;
                                qty_picked = Integer.toString(qty);
                                holder.rightText.setText(String.valueOf(qty));
                                BigDecimal newQty = Global.getBigDecimalNum(holder.rightText.getText().toString());
                                updateVolumePrice(newQty);
                                notifyDataSetChanged();
                            }
                        });
                        holder.delete.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                String val = holder.rightText.getText().toString();
                                int qty = Integer.parseInt(val);
                                qty -= 1;
                                if (qty >= 1) {
                                    qty_picked = Integer.toString(qty);
                                    holder.rightText.setText(String.valueOf(qty));
                                    BigDecimal newQty = Global.getBigDecimalNum(holder.rightText.getText().toString());

                                    updateVolumePrice(newQty);
                                    notifyDataSetChanged();
                                }

                            }
                        });
                        break;
                    }
                    case 2: {
                        convertView = myInflater.inflate(R.layout.catalog_picker_adapter3, null);
                        holder.leftText = (TextView) convertView.findViewById(R.id.leftText);
                        holder.rightText = (TextView) convertView.findViewById(R.id.rightText);

                        holder.rightText.setTag(leftTitle[position - MAIN_OFFSET]);

                        holder.leftText.setText(leftTitle[position - MAIN_OFFSET]);
                        holder.rightText.setText(rightTitle[position - MAIN_OFFSET]);
                        break;
                    }
                    case 3: {
                        convertView = myInflater.inflate(R.layout.catalog_picker_adapter4, null);
                        holder.leftText = (TextView) convertView.findViewById(R.id.leftText);
                        holder.leftText.setText(leftTitle2[position - (leftTitle.length + MAIN_OFFSET)]);
                        break;
                    }
                }
                if (convertView != null) {
                    convertView.setTag(holder);
                }
            } else {
                holder = (ViewHolder) convertView.getTag();
                switch (type) {
                    case 0: {
                        if (position == 0) {
                            holder.leftText.setText(R.string.catalog_name);
                            holder.leftSubtitle.setText(itemName);
                            holder.leftText.setVisibility(View.VISIBLE);
                            holder.leftSubtitle.setVisibility(View.VISIBLE);
                        } else if (Global.isConsignment) {
                            holder.leftText.setText(R.string.consignment);
                            holder.leftSubtitle.setText(itemConsignmentQty);
                        } else {
                            holder.leftText.setVisibility(View.GONE);
                            holder.leftSubtitle.setVisibility(View.GONE);
                        }
                        break;
                    }
                    case 1: // quantity
                    {
                        holder.add.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                String val = holder.rightText.getText().toString();
                                int qty = Integer.parseInt(val);
                                qty += 1;
                                qty_picked = Integer.toString(qty);
                                holder.rightText.setText(String.valueOf(qty));
                                BigDecimal newQty = Global.getBigDecimalNum(holder.rightText.getText().toString());

                                updateVolumePrice(newQty);
                                notifyDataSetChanged();
                            }
                        });
                        holder.delete.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                String val = holder.rightText.getText().toString();
                                int qty = Integer.parseInt(val);
                                qty -= 1;
                                if (qty >= 1) {
                                    qty_picked = Integer.toString(qty);
                                    holder.rightText.setText(String.valueOf(qty));
                                    BigDecimal newQty = Global.getBigDecimalNum(holder.rightText.getText().toString());

                                    updateVolumePrice(newQty);
                                    notifyDataSetChanged();
                                }
                            }
                        });
                        break;
                    }
                    case 2: {
                        holder.rightText.setTag(leftTitle[position - MAIN_OFFSET]);

                        holder.leftText.setText(leftTitle[position - MAIN_OFFSET]);
                        holder.rightText.setText(rightTitle[position - MAIN_OFFSET]);
                        break;
                    }
                    case 3: {
                        holder.leftText.setText(leftTitle2[position - (leftTitle.length + MAIN_OFFSET)]);
                        break;
                    }
                }
            }

            return convertView;
        }

        @Override
        public Filter getFilter() {
            return null;
        }

        public class ViewHolder {
            TextView leftText;
            TextView leftSubtitle;
            TextView rightText;
            Button add;
            Button delete;
        }


        public void updateVolumePrice(BigDecimal qty) {
            String[] temp;
            if (global.qtyCounter != null && global.qtyCounter.containsKey(prodID)) {
                temp = volPriceHandler.getVolumePrice(qty.toString(), prodID);
            } else
                temp = volPriceHandler.getVolumePrice(String.valueOf(qty), prodID);
            if (temp[1] != null && !temp[1].isEmpty()) {
                basePrice = temp[1];
                rightTitle[INDEX_PRICE_LEVEL] = Global.formatDoubleToCurrency(Double.parseDouble(basePrice)) + " <Base Price>";
                prLevTotal = Global.formatNumToLocale(Double.parseDouble(basePrice));
            } else if (pricelevel_position == 0) {
                if (!isModify)
                    basePrice = orderProduct.prod_price;
                else
                    basePrice = global.orderProducts.get(modifyOrderPosition).prod_price;

                if (basePrice == null || basePrice.isEmpty())
                    basePrice = "0.0";

                rightTitle[INDEX_PRICE_LEVEL] = Global.formatDoubleToCurrency(Double.parseDouble(basePrice)) + " <Base Price>";
                prLevTotal = Global.formatNumToLocale(Double.parseDouble(basePrice));
            }
        }
    }


}
