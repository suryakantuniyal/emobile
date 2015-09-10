package com.android.emobilepos.ordering;


import java.io.File;

import com.android.emobilepos.R;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap.CompressFormat;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class CatalogALV_Adapter extends CursorAdapter {

	private boolean isPortrait;
	private LayoutInflater inflater;
	private Activity activity;
	private ImageLoader imageLoader;
	private DisplayImageOptions options;
	private MyPreferences myPref;
	private String tempPrice;
	
	public CatalogALV_Adapter(Activity _activity, Cursor c, int flags) {
		super(_activity, c, flags);
		// TODO Auto-generated constructor stub
		this.activity = _activity;
		
		inflater = LayoutInflater.from(_activity);
		isPortrait = Global.isPortrait(_activity);
		
		myPref = new MyPreferences(activity);
		
		File cacheDir = new File(myPref.getCacheDir());
		if(!cacheDir.exists())
			cacheDir.mkdirs();
		
		imageLoader = ImageLoader.getInstance();
		ImageLoaderConfiguration config = new ImageLoaderConfiguration
				.Builder(activity).memoryCacheExtraOptions(100, 100).discCacheExtraOptions(1000, 1000, CompressFormat.JPEG, 100,null).discCache(new UnlimitedDiscCache(cacheDir))
				.build();
		
		
		imageLoader.init(config);
		imageLoader.handleSlowNetwork(true);
		options = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT).build();
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		// TODO Auto-generated method stub
		
		ViewHolder holder = (ViewHolder)view.getTag();

		

		holder.prod_name.setText(cursor.getString(holder.i_prod_name)!=null?cursor.getString(holder.i_prod_name):"");
		imageLoader.displayImage(cursor.getString(holder.i_prod_img_name), holder.product_img, options);
		holder.product_img.setOnTouchListener(Global.opaqueImageOnClick());
		holder.product_img.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				/*Intent intent = new Intent(getActivity(), ShowProductImageActivity2.class);
				intent.putExtra("url", urlLink);
				startActivity(intent);*/
			}
		});
		
		
		
		
		if(isPortrait)
		{
			holder.prod_qty.setText("0");	
			
			
			tempPrice = cursor.getString(holder.i_volume_price);
			if(tempPrice == null||tempPrice.isEmpty())
			{
				tempPrice = cursor.getString(holder.i_pricelevel_price);
				if(tempPrice == null||tempPrice.isEmpty())
				{
					tempPrice = cursor.getString(holder.i_chain_price);
					if(tempPrice == null || tempPrice.isEmpty())
						tempPrice = cursor.getString(holder.i_master_price);
				}
			}
	
			
			holder.prod_price.setText(tempPrice);
			holder.prod_desc.setText(cursor.getString(holder.i_prod_desc)!=null?cursor.getString(holder.i_prod_desc):"");
			
			//imageLoaderTest.DisplayImage(urlLink, holder.itemImage,false);
			
	
			holder.product_detail_img.setOnClickListener(new View.OnClickListener() {
	
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
//					Intent intent = new Intent(activity,ProductDetails_FA.class);
//					activity.startActivity(intent);
				}
			});
		}
		
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		// TODO Auto-generated method stub

		
		View retView = null;
		ViewHolder holder = new ViewHolder();
		if(isPortrait)
		{
			retView = inflater.inflate(R.layout.lv_adapter_catalog_portrait, parent, false);
			
			
			holder.prod_qty = (TextView)retView.findViewById(R.id.productQuantity);
			holder.prod_price = (TextView)retView.findViewById(R.id.productPrice);
			holder.prod_desc = (TextView)retView.findViewById(R.id.productDescription);
			holder.product_detail_img = (ImageView)retView.findViewById(R.id.productMoreDetailsImg);
			
			holder.i_master_price = cursor.getColumnIndex("master_price");
			holder.i_volume_price = cursor.getColumnIndex("volume_price");
			holder.i_pricelevel_price = cursor.getColumnIndex("pricelevel_price");
			holder.i_prod_desc = cursor.getColumnIndex("prod_desc");
			holder.i_chain_price = cursor.getColumnIndex("chain_price");
			
		}
		else
		{
			retView = inflater.inflate(R.layout.lv_adapter_catalog_landscape, parent, false);
		}
		
		
		holder.i_id = cursor.getColumnIndex("_id");
		holder.i_prod_name = cursor.getColumnIndex("prod_name");
		holder.i_prod_img_name = cursor.getColumnIndex("prod_img_name");
		
		holder.prod_name = (TextView)retView.findViewById(R.id.productName);
		holder.product_img = (ImageView)retView.findViewById(R.id.productImage);
		
		retView.setTag(holder);
		
		
		return retView;
	}
	
	private class ViewHolder
	{
		TextView prod_name,prod_qty,prod_price,prod_desc;
		ImageView product_img,product_detail_img;
		

		
		int i_id,i_prod_name,i_chain_price,i_master_price,i_volume_price,i_pricelevel_price,i_prod_desc,i_prod_img_name;
	}

}
