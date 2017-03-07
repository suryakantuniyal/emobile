package com.android.emobilepos.ordering;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.SystemClock;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.database.VolumePricesHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.ShowProductImageActivity;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.OrderProductUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

public class MenuProdGV_Adapter extends CursorAdapter {
    private final Global global;
    LayoutInflater inflater;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;

    private ProductClickedCallback callBack;
    private Activity activity;
    private boolean isPortrait;
    private ViewHolder holder;

    private VolumePricesHandler volPriceHandler;
    private String attrToDisplay = "";
    private long lastClickTime = 0;
    private boolean isFastScanning = false;
    private boolean isRestMode = false;

    public interface ProductClickedCallback {
        void productClicked(int position);
    }

    public MenuProdGV_Adapter(Catalog_FR _this, Activity context, Cursor c, int flags, ImageLoader _imageLoader) {
        super(context, c, flags);
        activity = context;
        inflater = LayoutInflater.from(context);
        callBack = _this;
        MyPreferences myPref = new MyPreferences(context);

        isPortrait = Global.isPortrait(context) || !myPref.getIsTablet();
        imageLoader = _imageLoader;
        attrToDisplay = myPref.getPreferencesValue(MyPreferences.pref_attribute_to_display);
        isFastScanning = myPref.getPreferences(MyPreferences.pref_fast_scanning_mode);
        isRestMode = myPref.getPreferences(MyPreferences.pref_restaurant_mode);
        global = (Global) activity.getApplication();
        if (isPortrait) {
            options = new DisplayImageOptions.Builder().resetViewBeforeLoading(true).displayer(new FadeInBitmapDisplayer(800)).cacheOnDisc(true)
                    .imageScaleType(ImageScaleType.IN_SAMPLE_INT).build();
            volPriceHandler = new VolumePricesHandler(activity);
        } else
            options = new DisplayImageOptions.Builder().resetViewBeforeLoading(true).displayer(new FadeInBitmapDisplayer(800)).cacheOnDisc(true)
                    .imageScaleType(ImageScaleType.IN_SAMPLE_INT).showImageOnLoading(R.drawable.loading_image).showImageForEmptyUri(R.drawable.no_image)
                    .build();
    }

    @Override
    public void bindView(View view, Context context, final Cursor cursor) {
        final int position = cursor.getPosition();

        if (cursor.isClosed()) return;

        holder = (ViewHolder) view.getTag();
        if (holder.i_prod_name != -1) {
            if (holder.title != null)
                holder.title.setText(Global.getValidString(cursor.getString(holder.i_prod_name)));

            String urlLink = cursor.getString(holder.i_prod_img_name);
            if ((holder.itemImage.getTag() != null && !holder.itemImage.getTag().equals(urlLink)) || holder.itemImage.getTag() == null) {
                holder.itemImage.setTag(urlLink);
                if (urlLink != null || TextUtils.isEmpty(cursor.getString(holder.i_prod_name)) || holder.productNameTxt == null) {
                    holder.itemImage.setScaleType(ImageView.ScaleType.FIT_XY);
                    imageLoader.displayImage(urlLink, holder.itemImage, options);
                    if (holder.productNameTxt != null) {
                        holder.productNameTxt.setVisibility(View.GONE);
                    }
                } else {
                    holder.itemImage.setImageDrawable(null);
                    holder.productNameTxt.setText(cursor.getString(holder.i_prod_name));
                    holder.productNameTxt.setVisibility(View.VISIBLE);
                }
            }

            holder.itemImage.setOnTouchListener(Global.opaqueImageOnClick());
            holder.itemImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ((!isFastScanning || (isFastScanning && isRestMode)) && SystemClock.elapsedRealtime() - lastClickTime < 1000) {
                        return;
                    }
                    lastClickTime = SystemClock.elapsedRealtime();
                    if (isPortrait) {
                        Intent intent = new Intent(activity, ShowProductImageActivity.class);
                        cursor.moveToPosition(position);
                        intent.putExtra("url", cursor.getString(holder.i_prod_img_name));
                        activity.startActivity(intent);
                    } else {
                        callBack.productClicked(position);
                    }
                }
            });

            if (isPortrait) {
                String prod_id = cursor.getString(holder.i_id);
                holder.qty.setText(OrderProductUtils.getOrderProductQty(global.order.getOrderProducts(), prod_id));//getQty(prod_id));
                String tempPrice = cursor.getString(holder.i_volume_price);
                if (tempPrice == null || tempPrice.isEmpty()) {
                    tempPrice = cursor.getString(holder.i_pricelevel_price);
                    if (tempPrice == null || tempPrice.isEmpty()) {
                        tempPrice = cursor.getString(holder.i_chain_price);
                        if (tempPrice == null || tempPrice.isEmpty())
                            tempPrice = cursor.getString(holder.i_master_price);
                    }
                } else {
                    String[] temp = volPriceHandler.getVolumePrice(OrderProductUtils.getOrderProductQty(global.order.getOrderProducts(), prod_id), prod_id);
                    if (temp[1] != null && !temp[1].isEmpty())
                        tempPrice = temp[1];
                }
                final String prod_price = tempPrice;
                final String prod_desc = cursor.getString(holder.i_prod_desc);
                holder.amount.setText(Global.formatDoubleStrToCurrency(prod_price));
                holder.detail.setText(prod_desc);

                if (Global.isConsignment && holder.i_consignment_qty != -1) {
                    String tempVal = cursor.getString(holder.i_consignment_qty);
                    tempVal = tempVal == null ? "0" : tempVal;
                    holder.consignment_qty.setText("Orig. Qty: " + tempVal);
                    holder.consignment_qty.setVisibility(View.VISIBLE);
                } else {
                    holder.consignment_qty.setVisibility(View.GONE);
                }

                holder.iconImage.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(activity, ViewProductDetails_FA.class);
                        cursor.moveToPosition(position);
                        intent.putExtra("url", cursor.getString(holder.i_prod_img_name));
                        intent.putExtra("prod_id", cursor.getString(holder.i_id));
                        activity.startActivity(intent);
                    }
                });

            }
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        View retView;
        ViewHolder holder = new ViewHolder();
        retView = inflater.inflate(R.layout.catalog_listview_adapter, parent, false);
        if (isPortrait) {
//            retView = inflater.inflate(R.layout.catalog_listview_adapter, parent, false);
            holder.title = (TextView) retView.findViewById(R.id.catalogItemName);
            holder.qty = (TextView) retView.findViewById(R.id.catalogItemQty);
            holder.consignment_qty = (TextView) retView.findViewById(R.id.catalogConsignmentQty);
            holder.amount = (TextView) retView.findViewById(R.id.catalogItemPrice);
            holder.detail = (TextView) retView.findViewById(R.id.catalogItemInfo);
            holder.iconImage = (ImageView) retView.findViewById(R.id.catalogRightIcon);
            holder.itemImage = (ImageView) retView.findViewById(R.id.catalogItemPic);
            holder.productNameTxt = (TextView) retView.findViewById(R.id.gridCatalogProducttNametextView);
            holder.i_id = cursor.getColumnIndex("_id");
            holder.i_prod_name = cursor.getColumnIndex(attrToDisplay);
            holder.i_master_price = cursor.getColumnIndex("master_price");
            holder.i_volume_price = cursor.getColumnIndex("volume_price");
            holder.i_pricelevel_price = cursor.getColumnIndex("pricelevel_price");
            holder.i_prod_desc = cursor.getColumnIndex("prod_desc");
            holder.i_prod_img_name = cursor.getColumnIndex("prod_img_name");
            holder.i_chain_price = cursor.getColumnIndex("chain_price");
            holder.i_consignment_qty = cursor.getColumnIndex("consignment_qty");

        } else {
//            retView = inflater.inflate(R.layout.catalog_gridview_adapter, parent, false);

            holder.title = (TextView) retView.findViewById(R.id.gridViewImageTitle);
            holder.itemImage = (ImageView) retView.findViewById(R.id.gridViewImage);
            holder.productNameTxt = (TextView) retView.findViewById(R.id.gridCatalogProducttNametextView);
            holder.i_prod_name = cursor.getColumnIndex(attrToDisplay);
            holder.i_prod_desc = cursor.getColumnIndex("prod_desc");
            holder.i_prod_img_name = cursor.getColumnIndex("prod_img_name");

        }
        retView.setTag(holder);
        return retView;
    }

    private class ViewHolder {
        TextView title, qty, amount, detail, consignment_qty, productNameTxt;
        ImageView iconImage, itemImage;

        int i_id, i_prod_name, i_chain_price, i_master_price, i_volume_price, i_pricelevel_price, i_prod_desc, i_prod_img_name, i_consignment_qty;
    }

//    public String getQty(String id) {
//        Global global = (Global) activity.getApplication();
//        String value = global.qtyCounter.get(id);
//
//        if (value == null) {
//            return "0";
//        }
//        return value;
//    }
}
