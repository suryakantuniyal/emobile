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
import com.android.emobilepos.models.ParentAddon;
import com.android.emobilepos.models.Product;
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
import java.util.ArrayList;
import java.util.List;

import util.json.JsonUtils;

public class PickerAddon_FA extends BaseFragmentActivityActionBar implements OnClickListener {
    private boolean hasBeenCreated = false;

    private GridView myGridView;
    private Activity activity;
    private PickerAddonLV_Adapter adapter;

    private Bundle extras;
    private Global global;
    private boolean isEditAddon = false;
    private MyPreferences myPref;
    private int item_position = 0;
    private String _prod_id = "";
    private StringBuilder _ord_desc = new StringBuilder();
    private BigDecimal addedAddon = new BigDecimal("0");

    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private ProductAddonsHandler prodAddonsHandler;
    public static PickerAddon_FA instance;
    private String selectedSeatNumber;
    private OrderProduct orderProduct;
    private List<ParentAddon> parentAddons;


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
        Gson gson = JsonUtils.getInstance();
        extras = activity.getIntent().getExtras();
        global = (Global) activity.getApplication();
        prodAddonsHandler = new ProductAddonsHandler(activity);
        _prod_id = extras.getString("prod_id");
        orderProduct = gson.fromJson(extras.getString("orderProduct"), OrderProduct.class);
        parentAddons = prodAddonsHandler.getParentAddons(orderProduct.getProd_id());
        Cursor c = prodAddonsHandler.getSpecificChildAddons(_prod_id, parentAddons.get(0).getCategoryId());
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

        adapter = new PickerAddonLV_Adapter(this, c, CursorAdapter.NO_SELECTION, imageLoader, orderProduct);
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
        instance = null;
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.addonDoneButton:
                terminateAdditionProcess();
                break;
        }
    }


    private List<View> listParentViews;
    private int index_selected_parent = 0;

    private void createParentAddons() {
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout addonParentLL = (LinearLayout) findViewById(R.id.addonParentHolder);

        if (parentAddons.size() >= 1) {
            listParentViews = new ArrayList<View>();
            int pos = 0;
            for (ParentAddon parentAddon : parentAddons) {
                final View view = inflater.inflate(R.layout.catalog_gridview_adapter, null);
                TextView tv = (TextView) view.findViewById(R.id.gridViewImageTitle);
                ImageView iv = (ImageView) view.findViewById(R.id.gridViewImage);
                tv.setText(parentAddon.getCategoryName());
                imageLoader.displayImage(parentAddon.getUrl(), iv, options);
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
                            Cursor c = prodAddonsHandler.getSpecificChildAddons(_prod_id, parentAddons.get(_curr_pos).getCategoryId());
                            adapter = new PickerAddonLV_Adapter(activity, c, CursorAdapter.NO_SELECTION, imageLoader, orderProduct);
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

    private void updateLineItem() {
        ProductsHandler prodHandler = new ProductsHandler(activity);
        Product product = prodHandler.getProductDetails(_prod_id);
        BigDecimal temp = new BigDecimal(product.getFinalPrice());
        temp = temp.add(addedAddon);
        if (temp.compareTo(new BigDecimal("0")) == -1)
            temp = new BigDecimal("0");
        orderProduct.setOverwrite_price(temp);
        orderProduct.setItemSubtotal(Global.getRoundBigDecimal(temp));
        orderProduct.setItemTotal(Global.getRoundBigDecimal(temp));
        orderProduct.setOrdprod_desc(product.getProdDesc() + _ord_desc.toString());
        orderProduct.setProd_sku(product.getProd_sku());
        orderProduct.setProd_upc(product.getProd_upc());
        int idx = global.orderProducts.indexOf(orderProduct);
        if (idx > -1) {
            global.orderProducts.remove(idx);
            global.orderProducts.add(orderProduct);
        }
    }

    private void terminateAdditionProcess() {
        if (!isEditAddon) {
            if (!myPref.getPreferences(MyPreferences.pref_fast_scanning_mode)) {
                Intent intent = new Intent(activity, PickerProduct_FA.class);
                intent.putExtra("orderProduct", orderProduct.toJson());
                intent.putExtra("isFromAddon", true);
                intent.putExtra("cat_id", extras.getString("cat_id"));
                startActivityForResult(intent, 0);
            } else {
                OrderingMain_FA.automaticAddOrder(activity, true, global, orderProduct, selectedSeatNumber);
                activity.setResult(2);
            }

        } else {
            updateLineItem();
            if (Receipt_FR.fragInstance != null)
                Receipt_FR.fragInstance.reCalculate();
        }
        activity.finish();
    }
}
