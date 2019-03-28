package com.android.emobilepos.ordering;


import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.database.ProductAddonsHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.models.orders.OrderProduct;
import com.android.support.Global;
import com.android.support.OrderProductUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

public class PickerAddonLV_Adapter extends CursorAdapter implements OnClickListener {
    LayoutInflater inflater;
    private ImageLoader imageLoader;
    private OrderProduct orderProduct;
    private DisplayImageOptions options;
    private final int SELECT_EMPTY = 0, SELECT_CHECKED = 1, SELECT_CROSS = 2;
    private final int COLOR_GREEN = Color.rgb(0, 112, 60), COLOR_RED = Color.RED, COLOR_BLUE = Color.rgb(24, 136, 161);
    private boolean itHasAddonProducts = true;
    private Cursor c;
    private ProductAddonsHandler prodAddonsHandler;

    public interface ProductClickedCallback {
        void productClicked(int position);
    }

    public PickerAddonLV_Adapter(Activity context, Cursor _c, int flags, ImageLoader _imageLoader, OrderProduct orderProduct) {
        super(context, _c, flags);
        inflater = LayoutInflater.from(context);
        imageLoader = _imageLoader;
        prodAddonsHandler = new ProductAddonsHandler(context);
        this.orderProduct = orderProduct;
        options = new DisplayImageOptions.Builder().resetViewBeforeLoading(true).displayer(new FadeInBitmapDisplayer(800)).cacheOnDisc(true).
                imageScaleType(ImageScaleType.IN_SAMPLE_INT).showImageOnLoading(R.drawable.loading_image)
                .showImageForEmptyUri(R.drawable.no_image).build();
        c = _c;
        c.moveToFirst();
        int cursorSize = c.getCount();
        if (cursorSize == 1) {
            String prodID = c.getString(c.getColumnIndex("_id"));
            if (prodID == null)
                itHasAddonProducts = false;
        }
    }

    @Override
    public void bindView(View view, Context context, final Cursor c) {
        final int position = c.getPosition();
        c.moveToPosition(position);
        ViewHolder holder = (ViewHolder) view.getTag();
        if (position == 0)
            holder.prod_image.requestFocus();
        holder.itemPosition = position;
        holder.name.setText(c.getString(holder.i_prod_name));
        holder.price.setText(Global.getCurrencyFormat(c.getString(holder.i_prod_price)));
        holder.prod_image.setTag(holder);
        holder.prod_image.setOnTouchListener(Global.opaqueImageOnClick());
        holder.prod_image.setOnClickListener(this);
        int status = 0;
        OrderProduct addon = null;
        if (!orderProduct.addonsProducts.isEmpty()) {
            List<OrderProduct> productList = OrderProductUtils.getOrderProducts(orderProduct.addonsProducts, c.getString(holder.i_prod_id));
            if (productList != null && !productList.isEmpty()) {
                addon = productList.get(0);
            }
        }
        if (addon == null) {
            status = 0;
        } else {
            if (addon.isAdded()) {
                status = 1;
            } else if (!addon.isAdded()) {
                status = 2;
            }
        }
        switch (status) {
            case SELECT_EMPTY:
                holder.name.setBackgroundColor(COLOR_BLUE);
                holder.price.setBackgroundColor(COLOR_BLUE);
                holder.state_image.setVisibility(View.INVISIBLE);
                break;
            case SELECT_CHECKED:
                holder.name.setBackgroundColor(COLOR_GREEN);
                holder.price.setBackgroundColor(COLOR_GREEN);
                holder.state_image.setImageResource(R.drawable.check_button_green);
                holder.state_image.setVisibility(View.VISIBLE);
                break;
            case SELECT_CROSS:
                holder.name.setBackgroundColor(COLOR_RED);
                holder.price.setBackgroundColor(COLOR_RED);
                holder.state_image.setImageResource(R.drawable.cross_button_red);
                holder.state_image.setVisibility(View.VISIBLE);
                break;
        }
        if (!itHasAddonProducts) {
            view.setVisibility(View.GONE);
        }
        imageLoader.displayImage(c.getString(holder.i_prod_img_name), holder.prod_image, options);
    }


    @Override
    public View newView(Context context, Cursor c, ViewGroup parent) {
        View retView;
        ViewHolder holder;
        holder = new ViewHolder();
        holder.i_prod_img_name = c.getColumnIndex("prod_img_name");
        holder.i_prod_price = c.getColumnIndex("master_price");
        holder.i_prod_name = c.getColumnIndex("prod_name");
        holder.i_prod_id = c.getColumnIndex("_id");
        holder.i_cat_id = c.getColumnIndex("cat_id");
        retView = inflater.inflate(R.layout.addon_picker_item_adapter, parent, false);
        holder.name = (TextView) retView.findViewById(R.id.data_item_text_top);
        holder.price = (TextView) retView.findViewById(R.id.data_item_text_bottom);
        holder.state_image = (ImageView) retView.findViewById(R.id.data_item_image_icon);
        holder.prod_image = (ImageView) retView.findViewById(R.id.data_item_image);
        retView.setTag(holder);
        return retView;
    }

    private class ViewHolder {
        public TextView name, price;
        public ImageView prod_image, state_image;
        public int itemPosition;
        public int i_prod_name, i_prod_price, i_prod_img_name, i_prod_id, i_cat_id;
    }

    @Override
    public void onClick(View view) {
        ViewHolder holder = (ViewHolder) view.getTag();
        if (holder != null) {
            c.moveToPosition(holder.itemPosition);
            int status = 0;
            OrderProduct addon = null;
            if (!orderProduct.addonsProducts.isEmpty()) {
                List<OrderProduct> productList = OrderProductUtils.getOrderProducts(orderProduct.addonsProducts, c.getString(holder.i_prod_id));
                if (productList != null && !productList.isEmpty()) {
                    addon = productList.get(0);
                }
            }
            if (addon == null) {
                status = 0;
            } else {
                if (addon.isAdded()) {
                    status = 1;
                } else if (!addon.isAdded()) {
                    status = 2;
                }
            }
            switch (status) {
                case SELECT_EMPTY:
                    holder.name.setBackgroundColor(COLOR_GREEN);
                    holder.price.setBackgroundColor(COLOR_GREEN);
                    holder.state_image.setImageResource(R.drawable.check_button_green);
                    holder.state_image.setVisibility(View.VISIBLE);
                    addon = generateAddon(holder.itemPosition, true);
                    orderProduct.addonsProducts.add(addon);
                    break;
                case SELECT_CHECKED:
                    holder.name.setBackgroundColor(COLOR_RED);
                    holder.price.setBackgroundColor(COLOR_RED);
                    holder.state_image.setImageResource(R.drawable.cross_button_red);
                    holder.state_image.setVisibility(View.VISIBLE);
                    orderProduct.addonsProducts.remove(addon);
                    addon = generateAddon(holder.itemPosition, false);
                    orderProduct.addonsProducts.add(addon);
                    break;
                case SELECT_CROSS:
                    holder.name.setBackgroundColor(COLOR_BLUE);
                    holder.price.setBackgroundColor(COLOR_BLUE);
                    holder.state_image.setVisibility(View.INVISIBLE);
                    orderProduct.addonsProducts.remove(addon);
                    break;
            }
        }
    }

    private OrderProduct generateAddon(int cursorPos, boolean isAdded) {
        if (c != null && c.moveToPosition(cursorPos)) {
            OrderProduct ord = new OrderProduct();
            ord.setProd_istaxable(c.getString(c.getColumnIndex("prod_istaxable")));
            ord.setOrdprod_qty("1");
            ord.setAddon_ordprod_id(orderProduct.getOrdprod_id());
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
            if (tempPrice == null || tempPrice.isEmpty() || (!isAdded))
                tempPrice = "0";
            if (isAdded) {
                Global.addonTotalAmount += Double.parseDouble(tempPrice);
            } else {
                Global.addonTotalAmount -= Double.parseDouble(tempPrice);
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
            ord.setAddon(true);
            ord.setAdded(isAdded);
            orderProduct.setOrdprod_desc(orderProduct.getOrdprod_desc());
            ord.setItemTotal(tempPrice);
            if (!Global.isFromOnHold) {
            }
            ord.setOrd_id(orderProduct.getOrd_id());

            UUID uuid = UUID.randomUUID();
            ord.setOrdprod_id(uuid.toString());
            return ord;
        }
        return null;
    }
}
