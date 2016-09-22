package com.android.emobilepos.ordering;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.widget.CursorAdapter;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.database.ProductAddonsHandler;
import com.android.database.ProductsHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.models.OrderProduct;
import com.android.emobilepos.models.Product;
import com.android.support.GenerateNewID;
import com.android.support.GenerateNewID.IdType;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import util.JsonUtils;

public class PickerAddon_FA extends BaseFragmentActivityActionBar implements OnClickListener {
    private boolean hasBeenCreated = false;

    private GridView myGridView;
    private Activity activity;
    //    private Cursor c;
    private PickerAddonLV_Adapter adapter;

    private final int SELECT_EMPTY = 0, SELECT_CHECKED = 1, SELECT_CROSS = 2;
    private Bundle extras;
    private Global global;
    private boolean isEditAddon = false;
    private MyPreferences myPref;
    private int item_position = 0;
    private String _prod_id = "";
    private StringBuilder _ord_desc = new StringBuilder();
    private BigDecimal addedAddon = new BigDecimal("0"), removedAddon = new BigDecimal("0");

    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private ProductAddonsHandler prodAddonsHandler;
    public static PickerAddon_FA instance;
    private String selectedSeatNumber;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        myPref = new MyPreferences(this);
        instance = this;

        if (!myPref.getIsTablet())                        //reset to default layout (not as dialog)
            super.setTheme(R.style.AppTheme);


        super.onCreate(savedInstanceState);


        setContentView(R.layout.addon_picker_layout);

        activity = this;
        global = (Global) getApplication();

        if (myPref.getIsTablet()) {
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            int screenWidth = (int) (metrics.widthPixels * 0.80);
            int screenHeight = (int) (metrics.heightPixels * 0.80);
            getWindow().setLayout(screenWidth, screenHeight);
        }


        Global.addonTotalAmount = 0;
        extras = activity.getIntent().getExtras();
        global = (Global) activity.getApplication();
        prodAddonsHandler = new ProductAddonsHandler(activity);
        _prod_id = extras.getString("prod_id");
        Cursor c = prodAddonsHandler.getSpecificChildAddons(_prod_id, Global.productParentAddons.get(0).get("cat_id"));
        myGridView = (GridView) findViewById(R.id.asset_grid);
        isEditAddon = extras.getBoolean("isEditAddon", false);
        item_position = extras.getInt("item_position");
        selectedSeatNumber = extras.getString("selectedSeatNumber");
        File cacheDir = new File(myPref.getCacheDir());
        if (!cacheDir.exists())
            cacheDir.mkdirs();

        imageLoader = ImageLoader.getInstance();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration
                .Builder(activity).memoryCacheExtraOptions(100, 100).discCacheExtraOptions(1000, 1000, CompressFormat.JPEG, 100, null).discCache(new UnlimitedDiscCache(cacheDir))
                .build();
        imageLoader.init(config);
        imageLoader.handleSlowNetwork(true);
        options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.loading_image).showImageForEmptyUri(R.drawable.no_image).resetViewBeforeLoading(true).displayer(new FadeInBitmapDisplayer(800)).cacheOnDisc(true).
                imageScaleType(ImageScaleType.IN_SAMPLE_INT).build();

        adapter = new PickerAddonLV_Adapter(this, c, CursorAdapter.NO_SELECTION, imageLoader);
        myGridView.setAdapter(adapter);


        Button btnDone = (Button) findViewById(R.id.addonDoneButton);
        btnDone.setOnClickListener(this);

        createParentAddons();

        hasBeenCreated = true;


    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 2) {
            setResult(2);
        }
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
//        if (!c.isClosed())
//            c.close();
//		if (db.isOpen())
//			db.close();
        instance = null;
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.addonDoneButton:
                String[] keys = global.addonSelectionType.keySet().toArray(new String[global.addonSelectionType.size()]);
                String[] values;
                for (String key : keys) {
                    values = global.addonSelectionType.get(key);
                    switch (Integer.parseInt(values[0])) {
                        case SELECT_EMPTY:
                            break;
                        case SELECT_CHECKED:
                            generateAddon(Integer.parseInt(values[1]), values[2], true);
                            break;
                        case SELECT_CROSS:
                            generateAddon(Integer.parseInt(values[1]), values[2], false);
                            break;
                    }
                }

                terminateAdditionProcess();
                break;
        }
    }


    private List<View> listParentViews;
    private int index_selected_parent = 0;

    private void createParentAddons() {
        int tempSize = Global.productParentAddons.size();
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout addonParentLL = (LinearLayout) findViewById(R.id.addonParentHolder);

        if (tempSize >= 1) {
            listParentViews = new ArrayList<View>();
            int pos = 0;
            for (HashMap<String, String> map : Global.productParentAddons) {
                final View view = inflater.inflate(R.layout.catalog_gridview_adapter, null);
                TextView tv = (TextView) view.findViewById(R.id.gridViewImageTitle);
                ImageView iv = (ImageView) view.findViewById(R.id.gridViewImage);
                tv.setText(map.get("cat_name"));
                imageLoader.displayImage(map.get("url"), iv, options);
                iv.setOnTouchListener(Global.opaqueImageOnClick());
                iv.setTag(pos);
                iv.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        int _curr_pos = (Integer) v.getTag();
                        if (_curr_pos != index_selected_parent) {
                            TextView temp1 = (TextView) listParentViews.get(index_selected_parent).findViewById(R.id.gridViewImageTitle);
                            temp1.setBackgroundResource(R.drawable.gridview_title_bar);
                            TextView temp2 = (TextView) listParentViews.get(_curr_pos).findViewById(R.id.gridViewImageTitle);
                            temp2.setBackgroundColor(Color.rgb(0, 112, 60));
                            index_selected_parent = _curr_pos;
                            Cursor c = prodAddonsHandler.getSpecificChildAddons(_prod_id, Global.productParentAddons.get(_curr_pos).get("cat_id"));
                            adapter = new PickerAddonLV_Adapter(activity, c, CursorAdapter.NO_SELECTION, imageLoader);
                            myGridView.setAdapter(adapter);
                        }
                    }
                });
                listParentViews.add(view);
                addonParentLL.addView(view);
                if (pos == 0) {
                    tv.setBackgroundColor(Color.rgb(0, 112, 60));
                }
                pos++;
            }
        }
    }

    private void generateAddon(int pos, String _cat_id, boolean isAdded) {
        Cursor c = prodAddonsHandler.getSpecificChildAddons(_prod_id, _cat_id);
        if (c != null && c.moveToPosition(pos)) {
            OrderProduct ord = new OrderProduct();
            ord.setAssignedSeat(selectedSeatNumber);
            ord.setProd_istaxable(c.getString(c.getColumnIndex("prod_istaxable")));
            ord.setOrdprod_qty("1");
            ord.setOrdprod_name(c.getString(c.getColumnIndex("prod_name")));
            ord.setOrdprod_desc(c.getString(c.getColumnIndex("prod_desc")));
            ord.setProd_id(c.getString(c.getColumnIndex("_id")));

            String tempPrice = c.getString(c.getColumnIndex("volume_price"));
            if (tempPrice == null || tempPrice.isEmpty()) {
                tempPrice = c.getString(c.getColumnIndex("pricelevel_price"));
                if (tempPrice == null || tempPrice.isEmpty()) {
                    tempPrice = c.getString(c.getColumnIndex("chain_price"));
                    if (tempPrice == null || tempPrice.isEmpty())
                        tempPrice = c.getString(c.getColumnIndex("master_price"));
                }
            }
            if (tempPrice == null || tempPrice.isEmpty() || (!isEditAddon && !isAdded))
                tempPrice = "0";

            if (isAdded) {
                Global.addonTotalAmount += Double.parseDouble(tempPrice);
                addedAddon = addedAddon.add(Global.getBigDecimalNum(tempPrice));
            } else {
                Global.addonTotalAmount -= Double.parseDouble(tempPrice);
                removedAddon = removedAddon.add(Global.getBigDecimalNum(tempPrice));
            }

            ord.setOverwrite_price(new BigDecimal(tempPrice));
            ord.setOnHand(c.getString(c.getColumnIndex("master_prod_onhand")));
            ord.setImgURL(c.getString(c.getColumnIndex("prod_img_name")));

            if (ord.getProd_istaxable().equals("1")) {
                BigDecimal temp1 = Global.taxAmount.divide(new BigDecimal("100"));
                BigDecimal temp2 = temp1.multiply(Global.getBigDecimalNum(tempPrice)).setScale(2, RoundingMode.HALF_UP);
                ord.setProd_taxValue(temp2);
                ord.setProd_taxId(Global.taxID);
            }
            ord.setTaxAmount("");
            ord.setTaxTotal("");
            ord.setProd_price(tempPrice);
            ord.setProd_type(c.getString(c.getColumnIndex("prod_type")));

            ord.setAddon("1");
            if (isAdded)
                ord.setIsAdded("1");
            else
                ord.setIsAdded("0");

            if (!isAdded)// Not added
                _ord_desc.append("\n[NO ").append(c.getString(c.getColumnIndex("prod_name"))).append("]");
            else
                _ord_desc.append("\n[").append(c.getString(c.getColumnIndex("prod_name"))).append("]");

            ord.setItemTotal(tempPrice);
            ord.setItemSubtotal(tempPrice);

            //OrdersHandler handler = new OrdersHandler(activity);


            if (!Global.isFromOnHold) {
                GenerateNewID generator = new GenerateNewID(activity);
                Global.lastOrdID = generator.getNextID(IdType.ORDER_ID);
            }

            ord.setOrd_id(Global.lastOrdID);

            if (global.orderProductAddons == null) {
                global.orderProductAddons = new ArrayList<OrderProduct>();
            }

            UUID uuid = UUID.randomUUID();

            ord.setOrdprod_id(uuid.toString());
            global.orderProductAddons.add(ord);

        }
    }

    private void updateLineItem(int position) {
        OrderProduct ordProd = global.orderProducts.get(position);


        ProductsHandler prodHandler = new ProductsHandler(activity);
        String[] itemData = prodHandler.getProductDetails(_prod_id);

        BigDecimal temp = new BigDecimal(itemData[2]);

        temp = temp.add(addedAddon);
        if (temp.compareTo(new BigDecimal("0")) == -1)
            temp = new BigDecimal("0");
        ordProd.setOverwrite_price(temp);
        ordProd.setItemSubtotal(Global.getRoundBigDecimal(temp));
        ordProd.setItemTotal(Global.getRoundBigDecimal(temp));
        ordProd.setOrdprod_desc(itemData[3] + _ord_desc.toString());
        ordProd.setProd_sku(itemData[13]);
        ordProd.setProd_upc(itemData[14]);
        ordProd.addonsProducts.clear();
        ordProd.addonsProducts = new ArrayList<OrderProduct>(global.orderProductAddons);
        global.orderProducts.set(position, ordProd);
    }

    private void terminateAdditionProcess() {
        if (!isEditAddon) {
            Product product = new Product();
            product.setId(extras.getString("prod_id"));
            product.setProdName(extras.getString("prod_name"));
            product.setAssignedSeat(extras.getString("selectedSeatNumber"));
            product.setProdPrice(extras.getString("prod_price"));
            product.setProdDesc(extras.getString("prod_desc"));
            product.setProdOnHand(extras.getString("prod_on_hand"));
            product.setProdImgName(extras.getString("url"));
            product.setProdIstaxable(extras.getString("prod_istaxable"));
            product.setProdType(extras.getString("prod_type"));
            product.setCatId(extras.getString("cat_id"));
            product.setProdTaxCode(extras.getString("prod_taxcode"));
            product.setProdTaxType(extras.getString("prod_taxtype"));
            product.setProdPricePoints(extras.getInt("prod_price_points"));
            product.setProdValuePoints(extras.getInt("prod_value_points"));

            if (!myPref.getPreferences(MyPreferences.pref_fast_scanning_mode)) {
                Intent intent = new Intent(activity, PickerProduct_FA.class);

                Gson gson = JsonUtils.getInstance();
                product.setAssignedSeat(product.getAssignedSeat());
                intent.putExtra("orderProduct", gson.toJson(new OrderProduct(product)));

//                intent.putExtra("prod_id", product.getId());
//                intent.putExtra("prod_name", product.getProdName());
//                intent.putExtra("selectedSeatNumber", product.getAssignedSeat());
//                intent.putExtra("prod_price", product.getProdPrice());
//                intent.putExtra("prod_desc", product.getProdDesc());
//                intent.putExtra("prod_on_hand", product.getProdOnHand());
//                intent.putExtra("url", product.getProdImgName());
//                intent.putExtra("prod_istaxable", product.getProdIstaxable());
//                intent.putExtra("prod_type", product.getProdType());
//                intent.putExtra("prod_taxcode", product.getProdTaxCode());
//                intent.putExtra("prod_taxtype", product.getProdTaxType());
//                intent.putExtra("prod_price_points", product.getProdPricePoints());
//                intent.putExtra("prod_value_points", product.getProdValuePoints());

                intent.putExtra("isFromAddon", true);
                intent.putExtra("cat_id", extras.getString("cat_id"));
                startActivityForResult(intent, 0);
            } else {
                OrderingMain_FA.automaticAddOrder(activity, true, global, product, selectedSeatNumber);
                activity.setResult(2);
                // activity.finish();
            }

        } else {
            updateLineItem(item_position);
            Global.addonSelectionMap.put(extras.getString("addon_map_key"), global.addonSelectionType);
            Global.orderProductAddonsMap.put(extras.getString("addon_map_key"), global.orderProductAddons);
            global.addonSelectionType = new HashMap<String, String[]>();
            global.orderProductAddons = new ArrayList<OrderProduct>();

            if (Receipt_FR.fragInstance != null)
                Receipt_FR.fragInstance.reCalculate();
        }
        activity.finish();
    }
}
