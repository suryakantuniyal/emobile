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

import com.android.emobilepos.R;
import com.android.emobilepos.ShowProductImageActivity;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;


public class MenuCatGV_Adapter extends CursorAdapter {

    private LayoutInflater inflater;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private boolean isPortrait;
    private Activity activity;

    private ItemClickedCallback callBack;
    private ViewHolder holder;
    private String urlLink;
    private long lastClickTime = 0;
    private boolean isFastScanning = false;

    public interface ItemClickedCallback {
        void itemClicked(int position, boolean showAllProducts);
    }

    public MenuCatGV_Adapter(Catalog_FR _this, Activity context, Cursor c, int flags, ImageLoader _imageLoader) {
        super(context, c, flags);
        activity = context;
        inflater = LayoutInflater.from(context);
        callBack = _this;
        isPortrait = Global.isPortrait(context);
        imageLoader = _imageLoader;
        MyPreferences myPref = new MyPreferences(context);
        isFastScanning = myPref.getPreferences(MyPreferences.pref_fast_scanning_mode);
        if (isPortrait)
            options = new DisplayImageOptions.Builder().resetViewBeforeLoading(true).displayer(new FadeInBitmapDisplayer(800)).cacheOnDisc(true).
                    imageScaleType(ImageScaleType.IN_SAMPLE_INT).build();
        else
            options = new DisplayImageOptions.Builder().resetViewBeforeLoading(true).displayer(new FadeInBitmapDisplayer(800)).cacheOnDisc(true).
                    imageScaleType(ImageScaleType.IN_SAMPLE_INT).showImageOnLoading(R.drawable.loading_image)
                    .showImageForEmptyUri(R.drawable.no_image).build();

    }

    @Override
    public void bindView(View view, Context context, final Cursor c) {
        final int position = c.getPosition();
        holder = (ViewHolder) view.getTag();
        if (holder.i_cat_name != -1 && holder.i_url_icon != -1) {
            urlLink = c.getString(holder.i_url_icon);
            holder.title.setText(c.getString(holder.i_cat_name) != null ? c.getString(holder.i_cat_name) : "");
            if ((holder.itemImage.getTag() != null && !holder.itemImage.getTag().equals(urlLink)) || holder.itemImage.getTag() == null) {
                holder.itemImage.setTag(urlLink);
                if (urlLink != null || TextUtils.isEmpty(c.getString(holder.i_cat_name)) || holder.productNameTxt == null) {
                    holder.itemImage.setScaleType(ImageView.ScaleType.FIT_XY);
                    imageLoader.displayImage(urlLink, holder.itemImage, options);
                    if (holder.productNameTxt != null) {
                        holder.productNameTxt.setVisibility(View.GONE);
                    }
                } else {
                    holder.itemImage.setImageDrawable(null);
                    holder.productNameTxt.setText(c.getString(holder.i_cat_name));
                    holder.productNameTxt.setVisibility(View.VISIBLE);
                }
            }

            holder.itemImage.setOnTouchListener(Global.opaqueImageOnClick());
            holder.itemImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isFastScanning && SystemClock.elapsedRealtime() - lastClickTime < 1000) {
                        return;
                    }
                    lastClickTime = SystemClock.elapsedRealtime();
                    if (isPortrait) {
                        c.moveToPosition(position);
                        Intent intent = new Intent(activity, ShowProductImageActivity.class);
                        intent.putExtra("url", c.getString(holder.i_url_icon));
                        activity.startActivity(intent);
                    } else {
                        callBack.itemClicked(position, false);
                    }
                }
            });


            if (isPortrait) {
                holder.qty.setVisibility(View.GONE);
                holder.amount.setVisibility(View.GONE);
                holder.detail.setVisibility(View.GONE);

                holder.iconImage.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        callBack.itemClicked(position, true);
                    }
                });

                holder.consignment_qty.setVisibility(View.GONE);
            } else {
                holder.itemImage.setOnLongClickListener(new View.OnLongClickListener() {

                    @Override
                    public boolean onLongClick(View v) {
                        callBack.itemClicked(position, true);
                        return true;
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
            holder.itemImage = (ImageView) retView.findViewById(R.id.catalogItemPic);
            holder.iconImage = (ImageView) retView.findViewById(R.id.catalogRightIcon);
            holder.productNameTxt = (TextView) retView.findViewById(R.id.gridCatalogProducttNametextView);
            holder.consignment_qty = (TextView) retView.findViewById(R.id.catalogConsignmentQty);
            holder.qty = (TextView) retView.findViewById(R.id.catalogItemQty);
            holder.amount = (TextView) retView.findViewById(R.id.catalogItemPrice);
            holder.detail = (TextView) retView.findViewById(R.id.catalogItemInfo);
            holder.productNameTxt = (TextView) retView.findViewById(R.id.gridCatalogProducttNametextView);
            holder.i_id = cursor.getColumnIndex("_id");
            holder.i_cat_name = cursor.getColumnIndex("cat_name");
            holder.i_url_icon = cursor.getColumnIndex("url_icon");
            holder.i_consignment_qty = cursor.getColumnIndex("consignment_qty");
        } else {
//            retView = inflater.inflate(R.layout.catalog_gridview_adapter, parent, false);
            holder.title = (TextView) retView.findViewById(R.id.gridViewImageTitle);
            holder.itemImage = (ImageView) retView.findViewById(R.id.gridViewImage);
            holder.productNameTxt = (TextView) retView.findViewById(R.id.gridCatalogProducttNametextView);
            holder.i_id = cursor.getColumnIndex("_id");
            holder.i_cat_name = cursor.getColumnIndex("cat_name");
            holder.i_url_icon = cursor.getColumnIndex("url_icon");
            holder.i_num_subcategories = cursor.getColumnIndex("num_subcategories");

        }

        retView.setTag(holder);
        return retView;
    }

    private class ViewHolder {
        TextView title, qty, amount, detail, consignment_qty, productNameTxt;
        ImageView itemImage;
        ImageView iconImage;

        int i_id, i_cat_name, i_url_icon, i_num_subcategories, i_consignment_qty;
    }
}
