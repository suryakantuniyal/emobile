package com.android.emobilepos.ordering;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.CursorAdapter;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.database.OrdersHandler;
import com.android.database.ProductAddonsHandler;
import com.android.database.ProductsHandler;
import com.android.emobilepos.models.OrderProducts;
import com.emobilepos.app.R;
import com.android.support.DBManager;
import com.android.support.GenerateNewID;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

public class PickerAddon_FA  extends FragmentActivity  implements OnClickListener
{
	private boolean hasBeenCreated = false;
	
	private GridView myGridView;
	private Activity activity;
	private Cursor c;
	//private SQLiteDatabase db;
	private DBManager dbManager;
	private PickerAddonLV_Adapter adapter;
	
	private final int SELECT_EMPTY = 0, SELECT_CHECKED = 1, SELECT_CROSS = 2;
	private Bundle extras;
	private Global global;
	private boolean isEditAddon = false;
	private MyPreferences myPref;
	private int item_position = 0;
	private String _prod_id = "";
	private StringBuilder _ord_desc = new StringBuilder();
	private BigDecimal addedAddon = new BigDecimal("0"),removedAddon = new BigDecimal("0");
	
	private ImageLoader imageLoader;
	private DisplayImageOptions options;
	
	private ProductAddonsHandler prodAddonsHandler;
	
	private int tempSize = 0;
	public static PickerAddon_FA instance;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		myPref = new MyPreferences(this);
		instance = this;
		
		if(!myPref.getIsTablet())						//reset to default layout (not as dialog)
			super.setTheme(R.style.AppTheme);
		
		
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		
		
		setContentView(R.layout.addon_picker_layout);
		
		activity = this;
		global = (Global)getApplication();
		
		if(myPref.getIsTablet())
		{
			DisplayMetrics metrics = getResources().getDisplayMetrics();
	        int screenWidth = (int) (metrics.widthPixels * 0.80);
	        int screenHeight = (int) (metrics.heightPixels*0.80);
	        getWindow().setLayout(screenWidth, screenHeight);
		}
		
		
		
		Global.addonTotalAmount = 0;
		extras =activity.getIntent().getExtras();
        global = (Global) activity.getApplication();
        prodAddonsHandler = new ProductAddonsHandler(activity);
		dbManager = new DBManager(activity);
		//db = dbManager.openReadableDB();
		_prod_id = extras.getString("prod_id");
		//c = prodAddonsHandler.getChildAddons2(db,_prod_id,Global.productParentAddons);
		c = prodAddonsHandler.getSpecificChildAddons(_prod_id, Global.productParentAddons.get(0).get("cat_id"));
        myGridView = (GridView)findViewById(R.id.asset_grid);
        isEditAddon = extras.getBoolean("isEditAddon",false);
        item_position = extras.getInt("item_position");

        
		File cacheDir = new File(myPref.getCacheDir());
		if(!cacheDir.exists())
			cacheDir.mkdirs();
		
		imageLoader = ImageLoader.getInstance();
		ImageLoaderConfiguration config = new ImageLoaderConfiguration
				.Builder(activity).memoryCacheExtraOptions(100, 100).discCacheExtraOptions(1000, 1000, CompressFormat.JPEG, 100, null).discCache(new UnlimitedDiscCache(cacheDir))
				.build();
		imageLoader.init(config);
		imageLoader.handleSlowNetwork(true);
		options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.loading_image).showImageForEmptyUri(R.drawable.no_image).resetViewBeforeLoading(true).displayer(new FadeInBitmapDisplayer(800)).cacheOnDisc(true).
				imageScaleType(ImageScaleType.IN_SAMPLE_INT).build();
        
		adapter = new PickerAddonLV_Adapter(this,c,CursorAdapter.NO_SELECTION,imageLoader);
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
		if (!c.isClosed())
			c.close();
//		if (db.isOpen())
//			db.close();
		instance = null;
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.addonDoneButton:
			String[] keys = global.addonSelectionType.keySet().toArray(new String[global.addonSelectionType.size()]);
			int size = keys.length;
			String[] values;
			for (int i = 0; i < size; i++) {
				values = global.addonSelectionType.get(keys[i]);
				switch (Integer.parseInt(values[0])) {
				case SELECT_EMPTY:
					break;
				case SELECT_CHECKED:
					generateAddon(Integer.parseInt(values[1]),values[2], true);
					break;
				case SELECT_CROSS:
					generateAddon(Integer.parseInt(values[1]),values[2], false);
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
		tempSize = Global.productParentAddons.size();
		LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout addonParentLL = (LinearLayout) findViewById(R.id.addonParentHolder);
		
		if (tempSize >= 1) {
			listParentViews = new ArrayList<View>();
			int pos = 0;
			for(HashMap<String,String> map:Global.productParentAddons)
			{
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
						// TODO Auto-generated method stub
						int _curr_pos = (Integer)v.getTag();
						if(_curr_pos != index_selected_parent)
						{
							TextView temp1 = (TextView)listParentViews.get(index_selected_parent).findViewById(R.id.gridViewImageTitle);
							temp1.setBackgroundResource(R.drawable.gridview_title_bar);
							TextView temp2 = (TextView)listParentViews.get(_curr_pos).findViewById(R.id.gridViewImageTitle);
							temp2.setBackgroundColor(Color.rgb(0, 112, 60));
							
							
							index_selected_parent = _curr_pos;
							c = prodAddonsHandler.getSpecificChildAddons( _prod_id, Global.productParentAddons.get(_curr_pos).get("cat_id"));
							adapter = new PickerAddonLV_Adapter(activity,c,CursorAdapter.NO_SELECTION,imageLoader);
					        myGridView.setAdapter(adapter);
					        
						}
						
					}
				});
				
				listParentViews.add(view);
				addonParentLL.addView(view);
				if(pos==0)
				{
					tv.setBackgroundColor(Color.rgb(0, 112, 60));
				}
				pos++;
				
			}
		}
	}
	
	private void generateAddon(int pos,String _cat_id, boolean isAdded) {
		c = prodAddonsHandler.getSpecificChildAddons(_prod_id, _cat_id);
		if (c != null && c.moveToPosition(pos)) {
			OrderProducts ord = new OrderProducts();
			ord.prod_istaxable = c.getString(c.getColumnIndex("prod_istaxable"));
			ord.ordprod_qty = "1";
			ord.ordprod_name = c.getString(c.getColumnIndex("prod_name"));
			ord.ordprod_desc = c.getString(c.getColumnIndex("prod_desc"));
			ord.prod_id = c.getString(c.getColumnIndex("_id"));

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

			ord.overwrite_price = tempPrice;
			ord.onHand = c.getString(c.getColumnIndex("master_prod_onhand"));
			ord.imgURL = c.getString(c.getColumnIndex("prod_img_name"));

			if (ord.prod_istaxable.equals("1")) {
				BigDecimal temp1 = Global.taxAmount.divide(new BigDecimal("100"));
				BigDecimal temp2 = temp1.multiply(Global.getBigDecimalNum(tempPrice)).setScale(2, RoundingMode.HALF_UP);
				ord.prod_taxValue = temp2.toString();
				ord.prod_taxId = Global.taxID;
			}
			ord.taxAmount = "";
			ord.taxTotal = "";
			ord.prod_price = tempPrice;
			ord.prod_type = c.getString(c.getColumnIndex("prod_type"));

			ord.addon = "1";
			if (isAdded)
				ord.isAdded = "1";
			else
				ord.isAdded = "0";

			_ord_desc.append("<br/>");
			if (!isAdded)// Not added
				_ord_desc.append("[NO ").append(c.getString(c.getColumnIndex("prod_name"))).append("]");
			else
				_ord_desc.append("[").append(c.getString(c.getColumnIndex("prod_name"))).append("]");

			ord.itemTotal = tempPrice;
			ord.itemSubtotal = tempPrice;

			//OrdersHandler handler = new OrdersHandler(activity);

			

			if (!Global.isFromOnHold) {
				
				GenerateNewID generator = new GenerateNewID(activity);
				//myPref.setLastOrdID(generator.getNextID(myPref.getLastOrdID()));
				Global.lastOrdID = generator.getNextID(myPref.getLastOrdID());
//				if (handler.getDBSize() == 0)
//					Global.lastOrdID = generator.generate("", 0);
//				else
//					Global.lastOrdID = generator.generate(handler.getLastOrdID(), 0);

			}

			ord.ord_id = Global.lastOrdID;

			if (global.orderProductsAddons == null) {
				global.orderProductsAddons = new ArrayList<OrderProducts>();
			}

			UUID uuid = UUID.randomUUID();
			String randomUUIDString = uuid.toString();

			ord.ordprod_id = randomUUIDString;
			global.orderProductsAddons.add(ord);

		}
	}

	private void updateLineItem(int position) {
		OrderProducts ordProd = global.orderProducts.get(position);

		// BigDecimal temp = new
		// BigDecimal(ordProd.getSetData("overwrite_price", true, null));

		ProductsHandler prodHandler = new ProductsHandler(activity);
		String[] itemData = prodHandler.getProductDetails(_prod_id);

		BigDecimal temp = new BigDecimal(itemData[2]);

		temp = temp.add(addedAddon);
		if (temp.compareTo(new BigDecimal("0")) == -1)
			temp = new BigDecimal("0");
		ordProd.overwrite_price = Global.getRoundBigDecimal(temp);
		ordProd.itemSubtotal = Global.getRoundBigDecimal(temp);
		ordProd.itemTotal = Global.getRoundBigDecimal(temp);
		ordProd.ordprod_desc = itemData[3] + _ord_desc.toString();
		global.orderProducts.set(position, ordProd);
	}

	private void terminateAdditionProcess() {
		if (!isEditAddon) {
			String[] data = new String[13];
			data[0] = extras.getString("prod_id");
			data[1] = extras.getString("prod_name");
			data[2] = extras.getString("prod_price");
			data[3] = extras.getString("prod_desc");
			data[4] = extras.getString("prod_on_hand");
			data[5] = extras.getString("url");
			data[6] = extras.getString("prod_istaxable");
			data[7] = extras.getString("prod_type");
			data[8] = extras.getString("cat_id");
			data[12] = extras.getString("prod_taxcode");
			data[11] = extras.getString("prod_taxtype");
			data[9] = extras.getString("prod_price_points");
			data[10] = extras.getString("prod_value_points");

			if (!myPref.getPreferences(MyPreferences.pref_fast_scanning_mode)) {
				Intent intent = new Intent(activity, PickerProduct_FA.class);
				intent.putExtra("prod_id", data[0]);
				intent.putExtra("prod_name", data[1]);
				intent.putExtra("prod_price", data[2]);
				intent.putExtra("prod_desc", data[3]);
				intent.putExtra("prod_on_hand", data[4]);
				intent.putExtra("url", data[5]);
				intent.putExtra("prod_istaxable", data[6]);
				intent.putExtra("prod_type", data[7]);
				intent.putExtra("prod_taxcode", data[12]);
				intent.putExtra("prod_taxtype", data[11]);
				intent.putExtra("prod_price_points", data[9]);
				intent.putExtra("prod_value_points", data[10]);
				intent.putExtra("isFromAddon", true);
				intent.putExtra("cat_id", extras.getString("cat_id"));
				startActivityForResult(intent, 0);
			} else {
				global.automaticAddOrder(activity, true, global, data);
				activity.setResult(2);
				// activity.finish();
			}

		} else {
			updateLineItem(item_position);
			Global.addonSelectionMap.put(extras.getString("addon_map_key"), global.addonSelectionType);
			Global.orderProductAddonsMap.put(extras.getString("addon_map_key"), global.orderProductsAddons);
			global.addonSelectionType = new HashMap<String, String[]>();
			global.orderProductsAddons = new ArrayList<OrderProducts>();

			if (Receipt_FR.fragInstance != null)
				Receipt_FR.fragInstance.reCalculate();
		}
		activity.finish();
	}
}
