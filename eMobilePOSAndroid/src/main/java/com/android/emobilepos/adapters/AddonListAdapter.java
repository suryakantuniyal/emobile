package com.android.emobilepos.adapters;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.database.OrderProductsHandler;
import com.android.emobilepos.R;
import com.android.support.Global;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.tonicartos.widget.stickygridheaders.StickyGridHeadersSimpleAdapter;

import java.util.HashMap;

public class AddonListAdapter extends BaseAdapter implements StickyGridHeadersSimpleAdapter, View.OnClickListener {


    private LayoutInflater mInflater;

    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    OrderProductsHandler orderProductsHandler;
    private Cursor c;

    private Global global;
    private final int SELECT_EMPTY = 0, SELECT_CHECKED = 1, SELECT_CROSS = 2;

    private final int COLOR_GREEN = Color.rgb(0, 112, 60), COLOR_RED = Color.RED, COLOR_BLACK = Color.BLACK;
    private int cursorSize = 0;
    private boolean itHasAddonProducts = true;

    private ViewHolder holder;
    private HeaderViewHolder headerHolder;
    private HashMap<String, String> mapTemp;


    public AddonListAdapter(Activity activity, Cursor cursor, ImageLoader _image_loader, DisplayImageOptions _options) {
        mInflater = LayoutInflater.from(activity);

        imageLoader = _image_loader;
        options = _options;

        global = (Global) activity.getApplication();
        c = cursor;
        c.moveToFirst();

        cursorSize = c.getCount();
        if (cursorSize == 1) {
            String prodID = c.getString(c.getColumnIndex("_id"));
            if (prodID == null)
                itHasAddonProducts = false;
        }
    }


    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return cursorSize;
    }

    @Override
    public Object getItem(int arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return 0;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        c.moveToPosition(position);
        if (convertView == null) {

            holder = new ViewHolder();
            holder.i_prod_img_name = c.getColumnIndex("prod_img_name");
            holder.i_prod_price = c.getColumnIndex("master_price");
            holder.i_prod_name = c.getColumnIndex("prod_name");
            holder.i_prod_id = c.getColumnIndex("_id");

            convertView = mInflater.inflate(R.layout.addon_picker_item_adapter, parent, false);

            holder.name = (TextView) convertView.findViewById(R.id.data_item_text_top);
            holder.price = (TextView) convertView.findViewById(R.id.data_item_text_bottom);
            holder.state_image = (ImageView) convertView.findViewById(R.id.data_item_image_icon);
            holder.prod_image = (ImageView) convertView.findViewById(R.id.data_item_image);


            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (position == 0)
            holder.prod_image.requestFocus();


        holder.itemPosition = position;
        holder.name.setText(c.getString(holder.i_prod_name));
        holder.price.setText(Global.formatDoubleStrToCurrency(c.getString(holder.i_prod_price)));


        holder.prod_image.setTag(holder);
        holder.prod_image.setOnClickListener(this);


        String[] switchCase = new String[]{"0"};

//        if (global.addonSelectionType.containsKey(c.getString(holder.i_prod_id)))
//            switchCase = global.addonSelectionType.get(c.getString(holder.i_prod_id));
//        else
//            global.addonSelectionType.put(c.getString(holder.i_prod_id), new String[]{Integer.toString(SELECT_EMPTY), Integer.toString(position)});


        switch (Integer.parseInt(switchCase[0])) {
            case SELECT_EMPTY:
                holder.name.setBackgroundColor(COLOR_BLACK);
                holder.price.setBackgroundColor(COLOR_BLACK);
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
            convertView.setVisibility(View.GONE);
        }

        imageLoader.displayImage(c.getString(holder.i_prod_img_name), holder.prod_image, options);
        return convertView;
    }

    @Override
    public long getHeaderId(int position) {
        // TODO Auto-generated method stub
        c.moveToPosition(position);
        return c.getString(c.getColumnIndex("cat_id")).hashCode();
    }


    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        String cat_id;
        c.moveToPosition(position);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.addon_picker_header_adapter, parent, false);
            headerHolder = new HeaderViewHolder();
            headerHolder.name = (TextView) convertView.findViewById(R.id.section_header_title);
            headerHolder.image = (ImageView) convertView.findViewById(R.id.section_header_image);
            headerHolder.i_cat_id = c.getColumnIndex("cat_id");
            convertView.setTag(headerHolder);
        } else {
            headerHolder = (HeaderViewHolder) convertView.getTag();
        }
        cat_id = c.getString(headerHolder.i_cat_id);
        if (cat_id != null) {
            String test = (String) headerHolder.name.getTag();
//        	mapTemp = Global.productParentAddons.get(Global.productParentAddonsDictionary.get(cat_id));
            if (test == null || !test.equals(mapTemp.get("cat_name"))) {
                headerHolder.name.setText(mapTemp.get("cat_name"));
                headerHolder.name.setTag(mapTemp.get("cat_name"));
            }
            imageLoader.displayImage(mapTemp.get("url"), headerHolder.image, options);
        }
        return convertView;
    }

    protected class HeaderViewHolder {
        public TextView name;
        public ImageView image;
        public int i_cat_id;
    }

    protected class ViewHolder {
        public TextView name, price;
        public ImageView prod_image, state_image;
        public int itemPosition;
        public int i_prod_name, i_prod_price, i_prod_img_name, i_prod_id;
    }

    @Override
    public void onClick(View view) {
        ViewHolder holder = (ViewHolder) view.getTag();
        if (holder != null) {

            c.moveToPosition(holder.itemPosition);
            String[] temp = {"0"};//global.addonSelectionType.get(c.getString(holder.i_prod_id));

            switch (Integer.parseInt(temp[0])) {
                case SELECT_EMPTY:
                    holder.name.setBackgroundColor(COLOR_GREEN);
                    holder.price.setBackgroundColor(COLOR_GREEN);
                    holder.state_image.setImageResource(R.drawable.check_button_green);
                    holder.state_image.setVisibility(View.VISIBLE);
//                    global.addonSelectionType.put(c.getString(holder.i_prod_id), new String[]{
//                            Integer.toString(SELECT_CHECKED), Integer.toString(holder.itemPosition)});
                    break;
                case SELECT_CHECKED:
                    holder.name.setBackgroundColor(COLOR_RED);
                    holder.price.setBackgroundColor(COLOR_RED);
                    holder.state_image.setImageResource(R.drawable.cross_button_red);
                    holder.state_image.setVisibility(View.VISIBLE);
//                    global.addonSelectionType.put(c.getString(holder.i_prod_id), new String[]{
//                            Integer.toString(SELECT_CROSS), Integer.toString(holder.itemPosition)});
                    break;
                case SELECT_CROSS:
                    holder.name.setBackgroundColor(COLOR_BLACK);
                    holder.price.setBackgroundColor(COLOR_BLACK);
                    holder.state_image.setVisibility(View.INVISIBLE);
//                    global.addonSelectionType.put(c.getString(holder.i_prod_id), new String[]{
//                            Integer.toString(SELECT_EMPTY), Integer.toString(holder.itemPosition)});
                    break;
            }
            //this.notifyDataSetChanged();
        }
    }


}
