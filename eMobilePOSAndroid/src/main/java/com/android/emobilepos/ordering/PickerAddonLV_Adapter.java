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

import com.android.emobilepos.R;
import com.android.support.Global;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

public class PickerAddonLV_Adapter extends CursorAdapter implements OnClickListener{
	LayoutInflater inflater;
	private ImageLoader imageLoader;
	private DisplayImageOptions options;
	private Activity activity;
	private Global global;
	private final int SELECT_EMPTY = 0, SELECT_CHECKED = 1, SELECT_CROSS = 2;
	//private final int COLOR_GREEN = Color.rgb(0, 112, 60),COLOR_RED = Color.RED, COLOR_BLACK = Color.rgb(98, 105, 77);
	private final int COLOR_GREEN = Color.rgb(0, 112, 60),COLOR_RED = Color.RED, COLOR_BLUE = Color.rgb(24, 136, 161);
	private boolean itHasAddonProducts = true;
	private Cursor c;

	public interface ProductClickedCallback {
		void productClicked(int position);
	}

	public PickerAddonLV_Adapter(Activity context, Cursor _c, int flags, ImageLoader _imageLoader) {
		super(context, _c, flags);
		activity = context;
		inflater = LayoutInflater.from(context);
		imageLoader = _imageLoader;
		options = new DisplayImageOptions.Builder().resetViewBeforeLoading(true).displayer(new FadeInBitmapDisplayer(800)).cacheOnDisc(true).
				imageScaleType(ImageScaleType.IN_SAMPLE_INT).showImageOnLoading(R.drawable.loading_image)
				.showImageForEmptyUri(R.drawable.no_image).build();
		global = (Global) activity.getApplication();
		c = _c;
		c.moveToFirst();
		int cursorSize = c.getCount();
    	if(cursorSize ==1)
    	{
    		String prodID = c.getString(c.getColumnIndex("_id"));
    		if(prodID==null)
    			itHasAddonProducts = false;
    	}
	}

	@Override
	public void bindView(View view, Context context, final Cursor c) {
		final int position = c.getPosition();
		c.moveToPosition(position);
		ViewHolder holder = (ViewHolder) view.getTag();
        if(position == 0)
        	holder.prod_image.requestFocus();
        holder.itemPosition = position;
        holder.name.setText(c.getString(holder.i_prod_name));
        holder.price.setText(Global.formatDoubleStrToCurrency(c.getString(holder.i_prod_price)));
        holder.prod_image.setTag(holder);
        holder.prod_image.setOnTouchListener(Global.opaqueImageOnClick());
		holder.prod_image.setOnClickListener(this);
		String[] switchCase = new String[]{"0"};
		if(global.addonSelectionType.containsKey(c.getString(holder.i_prod_id)))
			switchCase = global.addonSelectionType.get(c.getString(holder.i_prod_id));
		else
			global.addonSelectionType.put(c.getString(holder.i_prod_id),new String[]{Integer.toString(SELECT_EMPTY),
				Integer.toString(position),c.getString(holder.i_cat_id)});
		switch(Integer.parseInt(switchCase[0]))
		{
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
		if(!itHasAddonProducts)
		{
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
		public TextView name,price;
        public ImageView prod_image,state_image;
        public int itemPosition;
        public int i_prod_name,i_prod_price,i_prod_img_name,i_prod_id,i_cat_id;
	}

	@Override
	public void onClick(View view) {
		ViewHolder holder = (ViewHolder)view.getTag();
		if(holder!=null)
		{
			c.moveToPosition(holder.itemPosition);
			String[] temp = global.addonSelectionType.get(c.getString(holder.i_prod_id));
			switch (Integer.parseInt(temp[0])) {
			case SELECT_EMPTY:
				holder.name.setBackgroundColor(COLOR_GREEN);
				holder.price.setBackgroundColor(COLOR_GREEN);
				holder.state_image.setImageResource(R.drawable.check_button_green);
				holder.state_image.setVisibility(View.VISIBLE);
				global.addonSelectionType.put(c.getString(holder.i_prod_id), new String[] {
						Integer.toString(SELECT_CHECKED), Integer.toString(holder.itemPosition),c.getString(holder.i_cat_id) });
				break;
			case SELECT_CHECKED:
				holder.name.setBackgroundColor(COLOR_RED);
				holder.price.setBackgroundColor(COLOR_RED);
				holder.state_image.setImageResource(R.drawable.cross_button_red);
				holder.state_image.setVisibility(View.VISIBLE);
				global.addonSelectionType.put(c.getString(holder.i_prod_id), new String[] {
						Integer.toString(SELECT_CROSS), Integer.toString(holder.itemPosition),c.getString(holder.i_cat_id) });
				break;
			case SELECT_CROSS:
				holder.name.setBackgroundColor(COLOR_BLUE);
				holder.price.setBackgroundColor(COLOR_BLUE);
				holder.state_image.setVisibility(View.INVISIBLE);
				global.addonSelectionType.put(c.getString(holder.i_prod_id), new String[] {
						Integer.toString(SELECT_EMPTY),Integer.toString(holder.itemPosition),c.getString(holder.i_cat_id) });
				break;
			}
		}
	}
}
