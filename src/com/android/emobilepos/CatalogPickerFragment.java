package com.android.emobilepos;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;


import com.android.database.OrdProdAttrList_DB;
import com.android.database.OrdersHandler;
import com.android.database.PriceLevelHandler;
import com.android.database.PriceLevelItemsHandler;
import com.android.database.ProductsAttrHandler;
import com.android.database.ProductsHandler;
import com.android.database.TaxesGroupHandler;
import com.android.database.TaxesHandler;
import com.android.database.UOMHandler;
import com.android.database.VolumePricesHandler;
import com.android.emobilepos.ordering.OrderAttributes_FA;
import com.android.support.GenerateNewID;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.OrderProducts;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.zzzapi.uart.uart;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import drivers.EMSPAT100;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import android.widget.ListView;
import android.widget.TextView;

public class CatalogPickerFragment extends Fragment {

	private String[] leftTitle;// = new String[] { "UOM","Comments", "Price Level", "Discount", "Special Tax" };
	private String[] leftTitle2;// = new String[] { "Attributes", "View other types" };
	private String[] rightTitle = new String[] { "ONE (Default)","", "", "0.00 <No Discount>"}; // 0=UOM,1=Comments,2=Price Level,3=Discount,4=Tax
	private final int UOM_IND = 0,CMT_IND = 1, PRICE_LEV=2,DISC_IND = 3,OFFSET = 4,MAIN_OFFSET = 3;


	private List<String[]> dialogText = new ArrayList<String[]>();
	
	private ListViewAdapter myAdapter;
	private Bundle extras;
	private ImageLoader imageLoader;
	private DisplayImageOptions options;

	private String pickedQty = "1";

	private String defaultVal = "0.00";
	private String defVal = "0";
	private String _ordprod_comment = "";
	private String taxTotal = defaultVal,prod_taxId;
	private String prLevTotal;
	private String taxAmount = defVal,disAmount = defVal, disTotal = defaultVal,discount_id;
	private String uomName="",uomID="";
	private boolean isFixed = true;
	private Global global;
	private Activity activity;
	private String prodID;
	private boolean isModify;
	private int modifyOrderPosition = 0;
	private String imgURL;

	public Fragment thisFragment;
	private MyPreferences myPref;
	
	private BigDecimal uomMultiplier = new BigDecimal("1.0");
	private boolean discountIsTaxable =false,discountWasSelected = false;
	private String basePrice;
	private ListView lView;
	private String priceLevelID = "",priceLevelName = "";
	private int pricelevel_position = 0 ,discount_position = 0,tax_position = 0,uom_position;
	private String prod_type = "";
	
	private VolumePricesHandler volPriceHandler;
	private ProductsAttrHandler prodAttrHandler;
	private AlertDialog promptDialog;
	private AlertDialog.Builder dialogBuilder;
	
	private LinkedHashMap<String,List<String>>attributesMap;
	private String[] attributesKey;
	private LinkedHashMap<String,String>attributesSelected;
	
	
	private TextView headerProductID,headerOnHand;
	private ImageView headerImage;
	private Button headerAddButton;
	private String ordProdAttr = new String();
	private boolean isFromAddon = false;
	private HashMap<String,String>extrasMap = new HashMap<String,String>();
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.catalog_picker_layout, container, false);
		lView = (ListView) view.findViewById(R.id.pickerLV);
		
		
		activity = getActivity();
		global = (Global) activity.getApplication();
		myPref = new MyPreferences(activity);
		thisFragment = this;
		extras = activity.getIntent().getExtras();
		
		View header = inflater.inflate(R.layout.catalog_picker_header, (ViewGroup) activity.findViewById(R.id.header_layout_root));
		headerProductID = (TextView) header.findViewById(R.id.pickerHeaderID);
		headerOnHand = (TextView) header.findViewById(R.id.pickerHeaderQty);
		headerImage = (ImageView) header.findViewById(R.id.itemHeaderImg);
		headerAddButton = (Button) header.findViewById(R.id.pickerHeaderButton);
		
		
		volPriceHandler = new VolumePricesHandler(activity);
		
		leftTitle = new String[]{getAString(R.string.cat_picker_uom),getAString(R.string.cat_picker_comments),getAString(R.string.cat_picker_price_level),
				getAString(R.string.cat_picker_discount)};
		
		
		leftTitle2 = new String[]{getAString(R.string.cat_picker_attributes)
				,getAString(R.string.cat_picker_view_other_types),getAString(R.string.additional_info)};
		
		
		imageLoader = ImageLoader.getInstance();
		imageLoader.init(ImageLoaderConfiguration.createDefault(activity));
		options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.loading_image).cacheInMemory(false).cacheOnDisc(true)
				.showImageForEmptyUri(R.drawable.no_image).build();

		
		
		isModify = extras.getBoolean("isModify", false);
		isFromAddon = extras.getBoolean("isFromAddon",false);
		setExtrasMapValues();
		
		if(isModify)
		{
			headerAddButton.setText("Modify");
			modifyOrderPosition = extras.getInt("modify_position");
			imgURL = global.orderProducts.get(modifyOrderPosition).getSetData("imgURL", true, null);
			prodID = global.orderProducts.get(modifyOrderPosition).getSetData("prod_id", true, null);
			headerOnHand.setText(global.orderProducts.get(modifyOrderPosition).getSetData("onHand", true, null));
			basePrice = global.orderProducts.get(modifyOrderPosition).getSetData("overwrite_price", true, null);
			prod_type = global.orderProducts.get(modifyOrderPosition).getSetData("prod_type", true, null);
		}
		else
		{
			imgURL = extrasMap.get("prod_img_url");	
			headerOnHand.setText(extrasMap.get("prod_on_hand"));
			prodID = extrasMap.get("prod_id");
			prod_type = extrasMap.get("prod_type");
			basePrice = extrasMap.get("prod_price");
			if(basePrice == null ||basePrice.isEmpty())
				basePrice = "0.0";
			prLevTotal = Global.formatNumToLocale(Double.parseDouble(basePrice));
		}

		headerProductID.setText(prodID);
		imageLoader.displayImage(imgURL, headerImage, options);
		
		
		
		
		if(!isModify)		//Get Product Attributes if any
		{
			prodAttrHandler = new ProductsAttrHandler(activity);
			attributesMap = prodAttrHandler.getAttributesMap(extrasMap.get("prod_name"));
			attributesKey = attributesMap.keySet().toArray(new String[attributesMap.size()]);
			attributesSelected = prodAttrHandler.getDefaultAttributes(prodID);
			int attributesSize = attributesMap.size();
			for(int i = 0 ; i < attributesSize; i++)
			{
				addAttributeButton(header,attributesKey[i]);
			}
		}
		
		
		
		OrdProdAttrList_DB ordProdAttrDB = new OrdProdAttrList_DB(activity);
		ordProdAttr = ordProdAttrDB.getRequiredOrdAttr(prodID);
		
		
		TaxesHandler taxHandler = new TaxesHandler(activity);
		if(myPref.getPreferences(MyPreferences.pref_retail_taxes))
		{
			if(!Global.taxID.isEmpty())
			{		
				taxAmount = Global.formatNumToLocale(Double.parseDouble(taxHandler.getTaxRate(Global.taxID, extrasMap.get("prod_taxtype"))));
				prod_taxId = extrasMap.get("prod_taxtype");
			}
			else
			{
				taxAmount = Global.formatNumToLocale(Double.parseDouble(taxHandler.getTaxRate(extrasMap.get("prod_taxcode"), extrasMap.get("prod_taxtype"))));
				prod_taxId = extrasMap.get("prod_taxcode");
			}
		}
		else
		{
			if(!Global.taxID.isEmpty())
			{		
				taxAmount = Global.formatNumToLocale(Double.parseDouble(taxHandler.getTaxRate(Global.taxID, "")));
				prod_taxId = Global.taxID;
			}
			else
			{		
				taxAmount = Global.formatNumToLocale(Double.parseDouble(taxHandler.getTaxRate(extrasMap.get("prod_taxcode"), "")));
				prod_taxId = extrasMap.get("prod_taxcode");
			}
		}
		
		
		
		headerImage.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(activity, ShowProductImageActivity2.class);
				intent.putExtra("url", imgURL);
				startActivity(intent);
			}
		});
		
		headerAddButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				if(global.ordProdAttrPending.size()>0)
				{
					StringBuilder sb = new StringBuilder();
					sb.append(activity.getString(R.string.dlog_msg_required_attributes)).append("\n\n").append(ordProdAttr);
					Global.showPrompt(activity,R.string.dlog_title_error,sb.toString());
				}
				else
				{
					double onHandQty = 0;
					if(!headerOnHand.getText().toString().isEmpty())
						onHandQty = Double.parseDouble(headerOnHand.getText().toString());
						
					
					double selectedQty = Double.parseDouble(pickedQty);
					double newQty = 0;
					String addedQty = global.qtyCounter.get(prodID); 
					
					
					if(addedQty!=null&&!addedQty.isEmpty())
						newQty = Double.parseDouble(addedQty)+selectedQty;
					
		
					if((myPref.getPreferences(MyPreferences.pref_limit_products_on_hand)&&!prod_type.equals("Service")
							&&((Global.ord_type==Global.IS_SALES_RECEIPT||Global.ord_type==Global.IS_INVOICE)&&
									((!isModify&&(selectedQty>onHandQty||newQty>onHandQty))||(isModify&&selectedQty>onHandQty))
									))||(Global.isConsignment&&!prod_type.equals("Service")&&!validConsignment(selectedQty,onHandQty)))
					
					{
						Global.showPrompt(activity,R.string.dlog_title_error,activity.getString(R.string.limit_onhand));
					}
					else
					{
						if(!isModify)
							preValidateSettings();
						else
							modifyOrder(modifyOrderPosition);
						
						activity.setResult(2);
						activity.finish();
					}
				}
			}
		});
		
		
		lView.addHeaderView(header);

		lView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
				// TODO Auto-generated method stub
				if (position == 3) // Select the quantity
				{
					
					final Dialog globalDlog = new Dialog(activity,R.style.Theme_TransparentTest);
					globalDlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
					globalDlog.setCancelable(true);
					globalDlog.setContentView(R.layout.dlog_field_single_layout);
					
					final EditText viewField = (EditText)globalDlog.findViewById(R.id.dlogFieldSingle);
					
					if (myPref.getPreferences(MyPreferences.pref_allow_decimal_quantities))
						viewField.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
					else
						viewField.setInputType(InputType.TYPE_CLASS_NUMBER);
					final TextView qty = (TextView) v.findViewById(R.id.pickerQty);
					viewField.setText(qty.getText().toString());
					viewField.setSelection(qty.getText().toString().length());
					
					TextView viewTitle = (TextView)globalDlog.findViewById(R.id.dlogTitle);
					TextView viewMsg = (TextView)globalDlog.findViewById(R.id.dlogMessage);
					viewTitle.setText(R.string.dlog_title_confirm);
					viewMsg.setText(R.string.dlog_msg_enter_qty);
					
					Button btnOk = (Button)globalDlog.findViewById(R.id.btnDlogSingle);
					btnOk.setText(R.string.button_ok);
					btnOk.setOnClickListener(new View.OnClickListener() {
						
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							String txt = viewField.getText().toString();
							if (!txt.isEmpty() && Double.parseDouble(txt) > 0) {
								qty.setText(txt);
								pickedQty = txt;
							}
							globalDlog.dismiss();
						}
					});
					globalDlog.show();
					
				} else if (position == 5) // Add a comment to the product
				{
					final Dialog dialog = new Dialog(activity,R.style.Theme_TransparentTest);
					dialog.setContentView(R.layout.comments_dialog_layout);
					dialog.setCancelable(true);
					EditText cmt = (EditText) dialog.findViewById(R.id.commentEditText);
					final TextView txt = (TextView) v.findViewWithTag(leftTitle[CMT_IND]);
					Button done = (Button) dialog.findViewById(R.id.doneButton);
					Button clear = (Button) dialog.findViewById(R.id.clearButton);
					cmt.setText(txt.getText().toString());
					cmt.setSelection(txt.getText().toString().length());
					done.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							EditText curComment = (EditText) dialog.findViewById(R.id.commentEditText);
							rightTitle[CMT_IND] = curComment.getText().toString();
							_ordprod_comment = curComment.getText().toString().trim();
							txt.setText(rightTitle[CMT_IND]);
							dialog.dismiss();
						}
					});
					clear.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							EditText curComment = (EditText) dialog.findViewById(R.id.commentEditText);
							curComment.setText("");
							rightTitle[CMT_IND] = curComment.getText().toString();
							txt.setText(rightTitle[CMT_IND]);
							_ordprod_comment = "";
						}
					});
					dialog.show();
				}
				else if (position == 4||position == 6 || position == 7) // display Price Level, Discount, Special Tax
				{
					final Dialog dlg = new Dialog(activity);
					LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
					View view = inflater.inflate(R.layout.dialog_listview_layout, null, false);
					dlg.setContentView(view);
					ListView dlgListView = (ListView) dlg.findViewById(R.id.dlgListView);
					switch (position) {
					case 4://UoM
					{
						UOMHandler uomHandler = new UOMHandler(activity);
						dialogText = uomHandler.getUOMList(prodID);
						break;
					}
					case 6: // Price Level
					{
						if(!myPref.getPreferences(MyPreferences.pref_block_price_level_change))
						{
							PriceLevelHandler handler1 = new PriceLevelHandler(activity);
							dialogText = handler1.getFixedPriceLevel(prodID);
							if (myPref.isCustSelected()) {
								PriceLevelItemsHandler handler = new PriceLevelItemsHandler(activity);
								List<String[]> temp = handler.getPriceLevel(prodID);
								int size = temp.size();
								for (int i = 0; i < size; i++) {
									dialogText.add(temp.get(i));
								}
							}
						}
						else
						{
							dialogText.clear();
							Toast.makeText(activity, "Changing the Price Level is currently not allowed.", Toast.LENGTH_LONG).show();
						}
						break;
					}
					case 7: // Discount
					{
						ProductsHandler handler = new ProductsHandler(activity);
						dialogText = handler.getDiscounts();
						break;
					}
					}
					DialogLVAdapter dlgAdapter = new DialogLVAdapter(activity, position);
					dlgListView.setAdapter(dlgAdapter);
					dlg.show();
					final int type = position;
					dlgListView.setOnItemClickListener(new OnItemClickListener() {

						@Override
						public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
							// TODO Auto-generated method stub
							setTextView(position, type);
							lView.invalidateViews();
							dlg.dismiss();
						}
					});
				}
				else if(position==8)
				{
					
				}
				else if(position==9)
				{
					Intent results = new Intent();
					results.putExtra("prod_name", extras.getString("prod_name"));
					activity.setResult(9,results);
					
					activity.finish();
				}
				else if(position == 10)
				{
					//Toast.makeText(activity, "Additional Info", Toast.LENGTH_LONG).show();
					Intent intent = new Intent(activity,OrderAttributes_FA.class);
					intent.putExtra("prod_id", prodID);
					if(isModify)
					{
						intent.putExtra("isModify", isModify);
						intent.putExtra("ordprod_id", global.orderProducts.get(modifyOrderPosition).getSetData("ordprod_id", true, null));
					}
					startActivity(intent);
				}
			}
		});
		
		myAdapter = new ListViewAdapter(activity);
		lView.setAdapter(myAdapter);
		
		
		
		
		if(isModify)
		{
//			int temp = Integer.parseInt(global.orderProducts.get(modifyOrderPosition).getSetData("pricelevel_position", true, null));
//						
//			PriceLevelHandler handler1 = new PriceLevelHandler(activity);
//			dialogText = handler1.getFixedPriceLevel(prodID);
//			if (myPref.isCustSelected()) {
//				PriceLevelItemsHandler handler = new PriceLevelItemsHandler(activity);
//				List<String[]> tempList = handler.getPriceLevel(prodID);
//				int size = tempList.size();
//				for (int i = 0; i < size; i++) {
//					dialogText.add(tempList.get(i));
//				}
//			}
//			setTextView(temp,PRICE_LEV+OFFSET);
//			
//			
//			temp = Integer.parseInt(global.orderProducts.get(modifyOrderPosition).getSetData("discount_position", true, null));
//			ProductsHandler handler = new ProductsHandler(activity);
//			dialogText = handler.getDiscounts();
//			setTextView(temp,DISC_IND+OFFSET);
//			
//			
//			temp = Integer.parseInt(global.orderProducts.get(modifyOrderPosition).getSetData("uom_position", true, null));
//			UOMHandler uomHandler = new UOMHandler(activity);
//			dialogText = uomHandler.getUOMList(prodID);
//			setTextView(temp,UOM_IND+OFFSET);
			
			updateSavedDetails();
			
			_ordprod_comment = global.orderProducts.get(modifyOrderPosition).getSetData("ordprod_comment", true, null);
			rightTitle[CMT_IND] = _ordprod_comment;
			
		}

		return view;
	}
	
	private void updateSavedDetails()
	{
		PriceLevelHandler plHandler = new PriceLevelHandler(activity);
		ProductsHandler prodHandler = new ProductsHandler(activity);
		UOMHandler uomHandler = new UOMHandler(activity);
		
		List<String[]>_listPriceLevel = plHandler.getFixedPriceLevel(prodID);
		if (myPref.isCustSelected()) {
			PriceLevelItemsHandler handler = new PriceLevelItemsHandler(activity);
			List<String[]> tempList = handler.getPriceLevel(prodID);
			int size = tempList.size();
			for (int i = 0; i < size; i++) {
				_listPriceLevel.add(tempList.get(i));
			}
		}
		
		List<String[]>_listDiscounts = prodHandler.getDiscounts();
		List<String[]> _listUOM = uomHandler.getUOMList(prodID);
		
		int plSize = _listPriceLevel.size();
		int disSize = _listDiscounts.size();
		int uomSize = _listUOM.size();
		
		int maxSize = plSize;
		if(maxSize<disSize)
			maxSize = disSize;
		if(maxSize < uomSize)
			maxSize = uomSize;
		
		int _plIndex = 0,_disIndex = 0,_uomIndex = 0;
		for(int i = 0 ; i < maxSize;i++)
		{
			if(i<plSize&&_listPriceLevel.get(i)[1].equals(global.orderProducts.get(modifyOrderPosition).getSetData("pricelevel_id", true, null)))
			{
				_plIndex = i+1;
			}
			if(i<disSize&&_listDiscounts.get(i)[4].equals(global.orderProducts.get(modifyOrderPosition).getSetData("discount_id", true, null)))
			{
				_disIndex = i+1;
			}
			if(i<uomSize &&_listUOM.get(i)[1].equals(global.orderProducts.get(modifyOrderPosition).getSetData("uom_id", true, null)) )
			{
				_uomIndex = i+1;
			}
		}
		
		dialogText = _listPriceLevel;
		setTextView(_plIndex,PRICE_LEV+OFFSET);
		dialogText = _listDiscounts;
		setTextView(_disIndex,DISC_IND+OFFSET);
		dialogText = _listUOM;
		setTextView(_uomIndex,UOM_IND+OFFSET);
	}

	private boolean validConsignment(double selectedQty,double onHandQty)
	{
		if(Global.isConsignment)
		{
			String temp = global.qtyCounter.get(prodID);
			if(temp!=null&&!isModify)
			{
				double val = Double.parseDouble(temp);
				selectedQty+=val;
			}
			
			if(Global.consignmentType==Global.IS_CONS_FILLUP&&(onHandQty<=0||selectedQty>onHandQty))
				return false;
			else if(Global.consignmentType!=Global.IS_CONS_FILLUP&&!Global.custInventoryMap.containsKey(prodID))
				return false;
			else if(Global.consignmentType!=Global.IS_CONS_FILLUP)
			{
				if(Global.consignmentType == Global.IS_CONS_RACK&&selectedQty>Double.parseDouble(Global.custInventoryMap.get(prodID)[2]))
					return false;
				else if(Global.consignmentType == Global.IS_CONS_RETURN)
				{
					if(Global.consignment_qtyCounter!=null&&Global.consignment_qtyCounter.containsKey(prodID))//verify rack
					{
						double rackQty = Double.parseDouble(Global.consignment_qtyCounter.get(prodID));
						double origQty = Double.parseDouble(Global.custInventoryMap.get(prodID)[2]);
						if(rackQty==origQty||(rackQty+selectedQty>origQty))
							return false;
					}
				}
				else if(Global.consignmentType == Global.IS_CONS_PICKUP&&selectedQty>Double.parseDouble(Global.custInventoryMap.get(prodID)[2]))
					return false;
				
			}
				
		}
		return true;
	}
	
	
	private String getAString(int id )
	{
		Resources resources = getActivity().getResources();
		return (resources.getString(id));
	}
	
	
	private void setExtrasMapValues()
	{
		extrasMap.put("prod_id", extras.getString("prod_id"));
		extrasMap.put("prod_name",extras.getString("prod_name"));
		extrasMap.put("prod_price",extras.getString("prod_price"));
		extrasMap.put("prod_img_url",extras.getString("url"));
		extrasMap.put("prod_type",extras.getString("prod_type"));
		extrasMap.put("prod_on_hand", extras.getString("prod_on_hand"));
		extrasMap.put("prod_is_taxable", extras.getString("prod_istaxable"));
		extrasMap.put("prod_desc", extras.getString("prod_desc"));
		extrasMap.put("prod_taxcode", extras.getString("prod_taxcode"));
		extrasMap.put("prod_taxtype", extras.getString("prod_taxtype"));
		extrasMap.put("cat_id", extras.getString("cat_id"));
		
		extrasMap.put("prod_price_points", extras.getString("prod_price_points")==null?"0":extras.getString("prod_price_points"));
		extrasMap.put("prod_value_points", extras.getString("prod_value_points")==null?"0":extras.getString("prod_value_points"));
		if(Global.isConsignment)
			extrasMap.put("consignment_qty", extras.getString("consignment_qty"));
	}
	
	
	
	private void refreshAttributeProduct(Cursor myCursor)
	{
		
		String[] data = new String[10];
		data[0] = myCursor.getString(myCursor.getColumnIndex("_id"));
		data[1] = myCursor.getString(myCursor.getColumnIndex("prod_name"));
		
		String tempPrice = myCursor.getString(myCursor.getColumnIndex("volume_price"));
		if(tempPrice == null||tempPrice.isEmpty())
		{
			tempPrice = myCursor.getString(myCursor.getColumnIndex("pricelevel_price"));
			if(tempPrice == null||tempPrice.isEmpty())
			{
				tempPrice = myCursor.getString(myCursor.getColumnIndex("chain_price"));
				
				if(tempPrice == null || tempPrice.isEmpty())
					tempPrice = myCursor.getString(myCursor.getColumnIndex("master_price"));
			}
		}

		data[2] = tempPrice;
		data[3] = myCursor.getString(myCursor.getColumnIndex("prod_desc"));
		
		tempPrice = new String();
		tempPrice = myCursor.getString(myCursor.getColumnIndex("local_prod_onhand"));
		if(tempPrice == null || tempPrice.isEmpty())
			tempPrice = myCursor.getString(myCursor.getColumnIndex("master_prod_onhand"));
		if(tempPrice.isEmpty())
			tempPrice = "0";
		data[4] = tempPrice;
		
		data[5] = myCursor.getString(myCursor.getColumnIndex("prod_img_name"));
		data[6] = myCursor.getString(myCursor.getColumnIndex("prod_istaxable"));
		data[7] = myCursor.getString(myCursor.getColumnIndex("prod_type"));
		
		data[8] = myCursor.getString(myCursor.getColumnIndex("prod_price_points"));
		if(data[8]==null||data[8].isEmpty())
			data[8] = "0";
		data[9] = myCursor.getString(myCursor.getColumnIndex("prod_value_points"));
		if(data[9]==null||data[9].isEmpty())
			data[9] = "0";
		
		
		extrasMap.clear();
		
		extrasMap.put("prod_id", data[0]);
		extrasMap.put("prod_name",data[1]);
		extrasMap.put("prod_price",data[2]);
		extrasMap.put("prod_img_url",data[5]);
		extrasMap.put("prod_type",data[7]);
		extrasMap.put("prod_on_hand", data[4]);
		extrasMap.put("prod_is_taxable", data[6]);
		extrasMap.put("prod_desc", data[3]);
		//extrasMap.put("cat_id", myCursor.getString(myCursor.getColumnIndex("cat_id")));
		extrasMap.put("prod_price_points", data[8]);
		extrasMap.put("prod_value_points", data[9]);
		
		
		
		imgURL = extrasMap.get("prod_img_url");	
		headerOnHand.setText(extrasMap.get("prod_on_hand"));
		prodID = extrasMap.get("prod_id");
		prod_type = extrasMap.get("prod_type");
		basePrice = extrasMap.get("prod_price");
		if(basePrice == null ||basePrice.isEmpty())
			basePrice = "0.0";
		
		prLevTotal = Global.formatNumToLocale(Double.parseDouble(basePrice));
		

		headerProductID.setText(prodID);
		imageLoader.displayImage(imgURL, headerImage, options);
		myAdapter = new ListViewAdapter(activity);
		lView.setAdapter(myAdapter);
	}
	
	
	
	private void addAttributeButton(View header,String tag)
	{

		LinearLayout test = (LinearLayout)header.findViewById(R.id.catalog_picker_attributes_holder);
		LayoutInflater inf = LayoutInflater.from(activity);
		
        View vw = inf.inflate(R.layout.catalog_picker_attributes_adapter, null);
        vw.setTag(tag);
        vw.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        
        
        TextView attributeTitle = (TextView)vw.findViewById(R.id.attribute_title);
        TextView attributeValue = (TextView)vw.findViewById(R.id.attribute_value);
        
        attributeTitle.setText(tag);
        attributeValue.setText(attributesSelected.get(tag));
        
        test.addView(vw);
		
        vw.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				generateAttributePrompt(v);
			}
		});
	}
	
	
	private void generateAttributePrompt(View view)
	{
		final String key = (String)view.getTag();
		
        final TextView attributeValue = (TextView)view.findViewById(R.id.attribute_value);
		
		ListView listView = new ListView(activity);
		dialogBuilder = new AlertDialog.Builder(activity);

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
				// TODO Auto-generated method stub
				
				attributeValue.setText(val[position]);
				attributesSelected.put(key, val[position]);
				refreshAttributeProduct(prodAttrHandler.getNewAttributeProduct(extrasMap.get("prod_name"), attributesKey, attributesSelected));
				promptDialog.dismiss();
			}
		});
		

		dialogBuilder.setView(listView);
		
		dialogBuilder.setInverseBackgroundForced(true);
		promptDialog = dialogBuilder.create();

		promptDialog.show();
	}
	

	private void modifyOrder(int position)
	{
		OrderProducts orderedProducts = global.orderProducts.get(position);
		
		String val = pickedQty;
		BigDecimal sum = Global.getBigDecimalNum(val);
		//double sum = Global.formatNumFromLocale(val);
		
		if(myPref.getPreferences(MyPreferences.pref_allow_decimal_quantities))
			global.qtyCounter.put(prodID, sum.setScale(2, RoundingMode.HALF_UP).toString());
		else
			global.qtyCounter.put(prodID, sum.setScale(0, RoundingMode.HALF_UP).toString());
		extrasMap.put("prod_is_taxable", orderedProducts.getSetData("prod_istaxable", true, null));
		BigDecimal total = sum.multiply(Global.getBigDecimalNum(prLevTotal).multiply(uomMultiplier)).setScale(2,RoundingMode.HALF_UP);
		//double total = Global.formatNumFromLocale(val) * Global.formatNumFromLocale(prLevTotal)*uomMultiplier;
		calculateAll(total);
		
		BigDecimal productPriceLevelTotal = Global.getBigDecimalNum(prLevTotal);
		orderedProducts.getSetData("ordprod_qty", false, val);
		orderedProducts.getSetData("overwrite_price", false, Global.getRoundBigDecimal(productPriceLevelTotal.multiply(uomMultiplier)).toString());
		
		orderedProducts.getSetData("prod_taxValue", false, taxTotal);
		orderedProducts.getSetData("discount_value", false, disAmount);

		
		orderedProducts.getSetData("pricelevel_id", false, priceLevelID);
		orderedProducts.getSetData("priceLevelName", false, priceLevelName);
		
		
		// for calculating taxes and discount at receipt
		//orderedProducts.getSetData("prod_taxId", false, prod_taxId);
		orderedProducts.getSetData("discount_id", false, discount_id);
		orderedProducts.getSetData("taxAmount", false, taxAmount);
		orderedProducts.getSetData("taxTotal", false, taxTotal);
		orderedProducts.getSetData("disAmount", false, disAmount);
		orderedProducts.getSetData("disTotal", false,disTotal);
		
		orderedProducts.getSetData("tax_position", false, Integer.toString(tax_position));
		orderedProducts.getSetData("discount_position", false, Integer.toString(discount_position));
		orderedProducts.getSetData("pricelevel_position", false, Integer.toString(pricelevel_position));
		orderedProducts.getSetData("uom_position", false, Integer.toString(uom_position));
		orderedProducts.getSetData("ordprod_comment", false, _ordprod_comment);
		
		BigDecimal itemTotal = total.subtract(Global.getBigDecimalNum(disTotal));
		//double itemTotal = total - toDouble(disTotal);
	
		
		if(discountIsTaxable)
		{
			orderedProducts.getSetData("discount_is_taxable", false, "1");
		}
		else
			orderedProducts.getSetData("discount_is_taxable", false, "0");
		
		if(isFixed)
			orderedProducts.getSetData("discount_is_fixed", false, "1");
		
		orderedProducts.getSetData("itemTotal", false,itemTotal.toString());
		orderedProducts.getSetData("itemSubtotal", false,total.toString());
		
	}
	
	
	private void refreshParticularOrder(int position)
	{
		OrderProducts orderedProducts = global.orderProducts.get(position);
		
		String newPickedOrders = orderedProducts.getSetData("ordprod_qty", true, null);
		BigDecimal sum = new BigDecimal("1");
		//double sum = 1.0;
		if(myPref.getPreferences(MyPreferences.pref_allow_decimal_quantities))
			sum = Global.getBigDecimalNum(newPickedOrders);
		else
			sum = Global.getBigDecimalNum(newPickedOrders);
		
		//prLevTotal = orderedProducts.getSetData("overwrite_price", true, null);
		BigDecimal total = sum.multiply(Global.getBigDecimalNum(prLevTotal)).setScale(2, RoundingMode.HALF_UP);
		//double total = sum*Double.parseDouble(prLevTotal);
		calculateAll(total);
		
		orderedProducts.getSetData("overwrite_price", false, prLevTotal);
		orderedProducts.getSetData("prod_taxValue", false, taxTotal);
		orderedProducts.getSetData("discount_value", false, disTotal);

		
		// for calculating taxes and discount at receipt
		orderedProducts.getSetData("taxAmount", false, taxAmount);
		orderedProducts.getSetData("taxTotal", false,taxTotal);
		orderedProducts.getSetData("disAmount", false, disAmount);
		orderedProducts.getSetData("disTotal", false,  disTotal);
		
		BigDecimal itemTotal = total.subtract(Global.getBigDecimalNum(disTotal));
		//double itemTotal = total - toDouble(disTotal);
		
		
		
		if(discountIsTaxable)
		{
			orderedProducts.getSetData("discount_is_taxable", false, "1");
		}
		else
			orderedProducts.getSetData("discount_is_taxable", false, "0");
		
		
		if(isFixed)
			orderedProducts.getSetData("discount_is_fixed", false, "1");
		
		orderedProducts.getSetData("itemTotal", false, itemTotal.toString());
		orderedProducts.getSetData("itemSubtotal", false, total.toString());
	}
	
	
	
	
	private void preValidateSettings()
	{		
		MyPreferences myPref = new MyPreferences(activity);
		
		if(myPref.getPreferences(MyPreferences.pref_group_receipt_by_sku))
		{
			int size = global.orderProducts.size();
			int index = 0;
			boolean found = false;
			
			for(int i = 0 ; i < size; i++)
			{
				if(global.orderProducts.get(i).getSetData("prod_id", true, null).equals(prodID))
				{
					index = i;
					found = true;
					break;
				}
			}
			
			if(found)
			{
				String value = global.qtyCounter.get(prodID);
				double previousQty = 0.0;
				if(value!=null&&!value.isEmpty())
					previousQty = Double.parseDouble(value);
				double sum = Global.formatNumFromLocale(pickedQty)+previousQty;
				value = new String();
				if(myPref.getPreferences(MyPreferences.pref_allow_decimal_quantities))
				{
					value = Global.formatNumber(true, sum);
					global.orderProducts.get(index).getSetData("ordprod_qty", false,value);
					//global.cur_orders.get(0).setQty(value);
					global.qtyCounter.put(prodID, Double.toString(sum));
				}
				else
				{
					value = Global.formatNumber(false, sum);
					global.orderProducts.get(index).getSetData("ordprod_qty", false, value);
					//global.cur_orders.get(0).setQty(value);
					global.qtyCounter.put(prodID, Integer.toString((int)sum));
				}
				refreshParticularOrder(index);
			}
			else
			{
				addOrder();
			}
		}
		else
		{
			addOrder();
		}
	}
	
	
	
	
	private void addOrder()
	{

		//Orders order = new Orders();
		OrderProducts ord = new OrderProducts();

		String val = pickedQty;
		BigDecimal num = Global.getBigDecimalNum(val);
		
		BigDecimal sum = num.add(getQty(prodID)).setScale(4,RoundingMode.HALF_EVEN);
		
		//double sum = Global.formatNumFromLocale(val) + getQty(prodID);
		
		BigDecimal productPriceLevelTotal = Global.getBigDecimalNum(prLevTotal);
		
		
		//double productPriceLevelTotal = Global.formatNumFromLocale(prLevTotal);
		if(isFromAddon)
		{
			productPriceLevelTotal.add(new BigDecimal(Double.toString(Global.addonTotalAmount)));
		}
		
		BigDecimal total = num.multiply(productPriceLevelTotal.multiply(uomMultiplier)).setScale(2,RoundingMode.HALF_UP);

		calculateAll(total); 					// calculate taxes and discount
		
		
		ord.getSetData("prod_istaxable", false, extrasMap.get("prod_is_taxable"));
		
		
		if(!myPref.getPreferences(MyPreferences.pref_allow_decimal_quantities))
		{
			val = Integer.toString((int)Double.parseDouble(val));
			global.qtyCounter.put(prodID, sum.setScale(0, RoundingMode.HALF_UP).toString());
		}
		else
		{
			global.qtyCounter.put(prodID, sum.setScale(2, RoundingMode.HALF_UP).toString());
		}
				
		
		// add order to db
		ord.getSetData("ordprod_qty", false, val);
		ord.getSetData("ordprod_name", false, extrasMap.get("prod_name"));
		ord.getSetData("ordprod_desc", false, extrasMap.get("prod_desc"));
		ord.getSetData("prod_id", false, prodID);
		ord.getSetData("overwrite_price", false, Global.getRoundBigDecimal(productPriceLevelTotal.multiply(uomMultiplier)).toString());
		ord.getSetData("onHand", false, extrasMap.get("prod_on_hand"));
		ord.getSetData("imgURL", false, extrasMap.get("prod_img_url"));
		ord.getSetData("cat_id", false, extrasMap.get("cat_id"));
		
		
		BigDecimal pricePoints = new BigDecimal(extrasMap.get("prod_price_points"));
		BigDecimal valuePoints = new BigDecimal(extrasMap.get("prod_value_points"));
		
		pricePoints = pricePoints.multiply(num);
		valuePoints = valuePoints.multiply(num);
		
		ord.getSetData("prod_price_points", false, pricePoints.toString());
		ord.getSetData("prod_value_points", false, valuePoints.toString());
		
		// Still need to do add the appropriate tax/discount value
		ord.getSetData("prod_taxValue", false, taxTotal);
		ord.getSetData("discount_value", false, disTotal);
		ord.getSetData("prod_taxtype", false, extrasMap.get("prod_taxtype"));

		
		// for calculating taxes and discount at receipt
		ord.getSetData("prod_taxId", false, prod_taxId);
		ord.getSetData("discount_id", false, discount_id);
		ord.getSetData("taxAmount", false, taxAmount);
		ord.getSetData("taxTotal", false, taxTotal);
		ord.getSetData("disAmount", false, disAmount);
		ord.getSetData("disTotal", false, disTotal);
		
		ord.getSetData("pricelevel_id", false, priceLevelID);
		ord.getSetData("priceLevelName", false, priceLevelName);
		
		ord.getSetData("prod_price", false, productPriceLevelTotal.toString());
		ord.getSetData("tax_position", false, Integer.toString(tax_position));
		ord.getSetData("discount_position", false, Integer.toString(discount_position));
		ord.getSetData("pricelevel_position", false, Integer.toString(pricelevel_position));
		ord.getSetData("uom_position", false, Integer.toString(uom_position));
		ord.getSetData("ordprod_comment", false, _ordprod_comment);
		
		ord.getSetData("prod_type", false, prod_type);
		
		//Add UOM attributes to the order
		ord.getSetData("uom_name", false, uomName);
		ord.getSetData("uom_id", false, uomID);
		ord.getSetData("uom_conversion", false, uomMultiplier.toString());
		
		if(discountIsTaxable)
		{
			ord.getSetData("discount_is_taxable", false, "1");
		}
		if(isFixed)
			ord.getSetData("discount_is_fixed", false, "1");
		
		

		BigDecimal itemTotal = total.subtract(Global.getBigDecimalNum(disTotal));
		//double itemTotal = total - toDouble(disTotal);
		
		ord.getSetData("itemTotal", false, itemTotal.toString());
		ord.getSetData("itemSubtotal", false, total.toString());

		OrdersHandler handler = new OrdersHandler(activity);

		GenerateNewID generator = new GenerateNewID(activity);

		if(!Global.isFromOnHold)
		{
			if (handler.getDBSize() == 0)
				Global.lastOrdID = generator.generate("",0);
			else
				Global.lastOrdID = generator.generate(handler.getLastOrdID(),0);
		}
		ord.getSetData("ord_id", false, Global.lastOrdID);


		if (global.orderProducts == null) {
			global.orderProducts = new ArrayList<OrderProducts>();
		}

		UUID uuid = UUID.randomUUID();
		String randomUUIDString = uuid.toString();

		
		ord.getSetData("ordprod_id", false, randomUUIDString);
		
		
		int size = global.ordProdAttr.size();
		for(int i = 0;i<size;i++)
		{
			if(global.ordProdAttr.get(i).ordprod_id==null)
				global.ordProdAttr.get(i).ordprod_id = randomUUIDString;
		}
		

		if(isFromAddon)
		{
			Global.addonTotalAmount = 0;
			
			if(Global.addonSelectionMap==null)
				Global.addonSelectionMap = new HashMap<String,HashMap<String,String[]>>();
			if(Global.orderProductAddonsMap==null)
				Global.orderProductAddonsMap = new HashMap<String,List<OrderProducts>>();
			
			if(global.addonSelectionType.size()>0)
			{
				StringBuilder sb = new StringBuilder();
				Global.addonSelectionMap.put(randomUUIDString, global.addonSelectionType);
				Global.orderProductAddonsMap.put(randomUUIDString, global.orderProductsAddons);
				
				
				sb.append(ord.getSetData("ordprod_desc", true, null));
				int tempSize = global.orderProductsAddons.size();
				for(int i = 0; i < tempSize;i++)
				{
					
					sb.append("<br/>");
					if(global.orderProductsAddons.get(i).getSetData("isAdded", true, null).equals("0"))//Not added
						sb.append("[NO ").append(global.orderProductsAddons.get(i).getSetData("ordprod_name", true, null)).append("]");
					else
						sb.append("[").append(global.orderProductsAddons.get(i).getSetData("ordprod_name", true, null)).append("]");
					
				}
				ord.getSetData("ordprod_desc", false, sb.toString());
				ord.getSetData("hasAddons", false, "1");
				
				global.orderProductsAddons = new ArrayList<OrderProducts>();
				
			}
		}
		global.orderProducts.add(ord);
		
		
		if(myPref.isSam4s(true, true))
		{
			StringBuilder sb = new StringBuilder();
			String row1 = ord.getSetData("ordprod_name", true, null);
			String row2 = sb.append(Global.formatDoubleStrToCurrency(ord.getSetData("overwrite_price", true, null))).toString();
			uart uart_tool = new uart();
			uart_tool.config(3, 9600, 8, 1);
			uart_tool.write(3, Global.emptySpaces(40, 0, false));
			uart_tool.write(3, Global.formatSam4sCDT(row1, row2));
		} else if (myPref.isPAT100(true, true)) {
			StringBuilder sb = new StringBuilder();
			String row1 = ord.getSetData("ordprod_name", true, null);
			String row2 = sb.append(Global.formatDoubleStrToCurrency(ord.getSetData("overwrite_price", true, null)))
					.toString();
			EMSPAT100.getTerminalDisp().clearText();
			EMSPAT100.getTerminalDisp().displayText(Global.formatSam4sCDT(row1.toString(), row2.toString()));
		}
	}
	


	
	private void calculateAll(BigDecimal total) {
		if (!isFixed) {
			BigDecimal val = total.multiply(Global.getBigDecimalNum(disAmount)).setScale(4, RoundingMode.HALF_UP);
			val = val.divide(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP);
			disTotal = val.toString();
			// val = (val / 100);
			// disTotal = Double.toString(val);
		} else {
			disTotal = Double.toString(Global.formatNumFromLocale(disAmount));
		}

		BigDecimal tempSubTotal = total, tempTaxTotal = new BigDecimal("0");

		if (extrasMap.get("prod_is_taxable").equals("1")) {
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

	
	public void setTextView(int position, int type) 
	{
		switch (type) {
		case UOM_IND+OFFSET:
		{
			uom_position = position;
			StringBuilder sb = new StringBuilder();
			if(position == 0)
			{
				sb.append("ONE (Default)");
				uomMultiplier = new BigDecimal("1");
				uomName = "";
				uomID = "";
			}
			else
			{
				sb.append(dialogText.get(position-1)[0]).append(" <").append(dialogText.get(position-1)[2]).append(">");
				uomMultiplier = Global.getBigDecimalNum(dialogText.get(position-1)[2]);
				uomName = dialogText.get(position-1)[0];
				uomID = dialogText.get(position-1)[1];
				//uomMultiplier = Double.parseDouble(dialogText.get(position-1)[2]);
			} 
			
			rightTitle[UOM_IND] = sb.toString();
			
			break;
		}
		case PRICE_LEV+OFFSET: // Price Level
		{
			pricelevel_position = position;
			StringBuilder sb = new StringBuilder();
			if (position == 0) 
			{
				priceLevelID = "";
				priceLevelName = "";
				sb.append(Global.formatDoubleToCurrency(Double.parseDouble(basePrice))).append(" <Base Price>");
				rightTitle[PRICE_LEV] = sb.toString();
				prLevTotal = Global.formatNumToLocale(Double.parseDouble(basePrice));

			} else {
				priceLevelName =dialogText.get(position-1)[0];
				priceLevelID = dialogText.get(position-1)[1];
				sb.append(Global.formatDoubleStrToCurrency(dialogText.get(position-1)[2]));
				sb.append(" <").append(dialogText.get(position-1)[0]).append(">");
				rightTitle[PRICE_LEV] = sb.toString();
				prLevTotal = Global.formatNumToLocale(Double.parseDouble(dialogText.get(position - 1)[2]));
			}
			break;
		}
		case DISC_IND+OFFSET: // Discount
		{
			discount_position = position;
			StringBuilder sb = new StringBuilder();
			if (position == 0) {
				rightTitle[DISC_IND] = "$0.00 <No Discount>";
				disAmount = "0";
				disTotal = "0.00";
				isFixed = true;
				discountWasSelected = false;
				discount_id = "";
			} else if (dialogText.get(position - 1)[1].equals("Fixed")) {
				
				discount_id = dialogText.get(position-1)[4];
				sb.append(Global.formatDoubleStrToCurrency(dialogText.get(position-1)[2])).append(" <").append(dialogText.get(position-1)[0]).append(">");
				rightTitle[DISC_IND] = sb.toString();

				disAmount = Global.formatNumToLocale(Double.parseDouble(dialogText.get(position-1)[2]));
				isFixed = true;
				discountWasSelected = true;
				if(dialogText.get(position-1)[3].equals("1"))
					discountIsTaxable = true;
				else
					discountIsTaxable = false;
			} else {
				discount_id = dialogText.get(position-1)[4];
				sb.append(dialogText.get(position - 1)[2]).append("%").append(" <").append(dialogText.get(position - 1)[0]).append(">");
				rightTitle[DISC_IND] = sb.toString();

				disAmount = Global.formatNumToLocale(Double.parseDouble(dialogText.get(position - 1)[2]));
				isFixed = false;
				discountWasSelected = true;
				if(dialogText.get(position-1)[3].equals("1"))
					discountIsTaxable = true;
				else
					discountIsTaxable = false;
			}		
			break;
		}
		/*case TAX_IND+OFFSET: // Special Tax
		{
			tax_position = position;
			StringBuilder sb = new StringBuilder();
			prod_taxId = "";
			if (position == 0) {
				
				rightTitle[TAX_IND] = "$0.00 <No Tax>";
				taxAmount = "0";
				taxTotal = "0.00";
			} else {
				prod_taxId = dialogText.get(position-1)[1];
				sb.append(dialogText.get(position - 1)[2]).append("%").append(" <").append(dialogText.get(position - 1)[0]).append(">");
				rightTitle[TAX_IND] = sb.toString();

				taxAmount = Global.formatNumToLocale(Double.parseDouble(dialogText.get(position - 1)[2]));
			}
			break;
		}*/
		}
	}
	

	
	public BigDecimal getQty(String id) {
		Global global = (Global) activity.getApplication();
		String value = global.qtyCounter.get(id);
		return Global.getBigDecimalNum(value);
//		if (value == null) {
//			return 0;
//		}
//		return Double.parseDouble(value);
	}

	

	
	
	
	
	
	
	private class DialogLVAdapter extends BaseAdapter {

		private LayoutInflater myInflater;
		private int listType;

		public DialogLVAdapter(Activity activity, int pos) {
			myInflater = LayoutInflater.from(activity);
			listType = pos;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return dialogText.size() + 1;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
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
			case 0: 
			{
				if (listType == PRICE_LEV+OFFSET) // Price Level
				{
					holder.leftText.setText("Base Price");
					holder.rightText.setText(Global.formatDoubleStrToCurrency(basePrice));

				} else if (listType == DISC_IND+OFFSET) // Discount
				{
					holder.leftText.setText("No Discount");

					holder.rightText.setText("0.00");
				} /*else if(listType == TAX_IND+OFFSET) {
					holder.leftText.setText("No Tax");
					holder.rightText.setText("0.00");

				}*/
				else if(listType == UOM_IND+OFFSET)
				{
					holder.leftText.setText("NONE");
					holder.rightText.setText("1");
					uomMultiplier = new BigDecimal("1");
					uomName = "";
					uomID = "";
				}
				break;
			}
			case 1: {
				holder.leftText.setText(dialogText.get(position - 1)[0]);

				if (listType == PRICE_LEV+OFFSET) // Price Level
				{
					String total = Global.formatDoubleStrToCurrency(dialogText.get(position - 1)[2]);
					holder.rightText.setText(total);
				}

				else if (listType == DISC_IND+OFFSET) // discount
				{
					if (dialogText.get(position - 1)[1].equals("Fixed")) {
						StringBuilder sb = new StringBuilder();
						sb.append(Global.formatDoubleStrToCurrency(dialogText.get(position-1)[2]));
						holder.rightText.setText(sb.toString());

					} else {
						StringBuilder sb = new StringBuilder();
						sb.append(dialogText.get(position - 1)[2]).append("%");
						holder.rightText.setText(sb.toString());
					}
					
				} /*else if(listType == TAX_IND+OFFSET) // tax
				{
					StringBuilder sb = new StringBuilder();
					sb.append(dialogText.get(position - 1)[2]).append("%");
					holder.rightText.setText(sb.toString());
				}*/
				else if(listType == UOM_IND+OFFSET)
				{
					String multiplier = dialogText.get(position-1)[2];
					
					uomName = dialogText.get(position-1)[0];
					uomID = dialogText.get(position-1)[1];
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
			if(!isModify)
			{
				itemName = extrasMap.get("prod_name");
				itemConsignmentQty = extrasMap.containsKey("consignment_qty")?"Orig. Qty: "+extrasMap.get("consignment_qty"):"Orig. Qty: "+"0";
				rightTitle[PRICE_LEV] =Global.formatDoubleToCurrency(Double.parseDouble(basePrice)) + " <Base Price>";
			}
			else
			{
				int pos = modifyOrderPosition;
				itemName = global.orderProducts.get(pos).getSetData("ordprod_name", true, null);
				
				rightTitle[PRICE_LEV] = global.orderProducts.get(pos).getSetData("overwrite_price",true, null);
				taxTotal = global.orderProducts.get(pos).getSetData("taxTotal", true, null);
				disTotal = global.orderProducts.get(pos).getSetData("disTotal", true, null);
				
				disAmount = global.orderProducts.get(pos).getSetData("disAmount", true, null);
				taxAmount = global.orderProducts.get(pos).getSetData("taxAmount", true, null);
				rightTitle[DISC_IND] = disAmount;
				//rightTitle[TAX_IND] = taxAmount;
			}
		}

		@Override
		public int getViewTypeCount() {
			return 4;
		}

		@Override
		public int getItemViewType(int position) 
		{
			if (position == 0||position==1) 								//Product name field,consignment
			{
				return 0;
			} 
			else if (position == 2) 						// +/- quantity
			{
				return 1;
			} 
			else if (position > 2 && position < (leftTitle.length + MAIN_OFFSET)) 		//comments-price level - discount - special tax
			{
				return 2;
			} 
			else if ((position >= (leftTitle.length + MAIN_OFFSET)) && (position <= (leftTitle.length + leftTitle2.length + MAIN_OFFSET))) // Attributes - View types
			{
				return 3;
			}
			return 4;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return leftTitle.length + leftTitle2.length + MAIN_OFFSET;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			final ViewHolder holder;
			int type = getItemViewType(position);
			if (convertView == null) {
				holder = new ViewHolder();
				switch (type) {
				case 0: {
					convertView = myInflater.inflate(R.layout.catalog_picker_adapter1, null);
					holder.leftText = (TextView)convertView.findViewById(R.id.itemNameLabel);
					holder.leftSubtitle = (TextView) convertView.findViewById(R.id.itemName);

					if(position==0)
					{
						holder.leftText.setText(R.string.catalog_name);
						holder.leftSubtitle.setText(itemName);	
						holder.leftText.setVisibility(View.VISIBLE);
						holder.leftSubtitle.setVisibility(View.VISIBLE);
					}
					else if(Global.isConsignment)
					{
						holder.leftText.setText(R.string.consignment);
						holder.leftSubtitle.setText(itemConsignmentQty);
					}
					else
					{
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
					updateVolumePrice(holder);
					if(isModify)
					{
						pickedQty = global.orderProducts.get(modifyOrderPosition).getSetData("ordprod_qty", true, null);
						holder.rightText.setText(pickedQty);
					}

					holder.add.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							String val = holder.rightText.getText().toString();
							int qty = Integer.parseInt(val);
							qty += 1;
							pickedQty = Integer.toString(qty);
							holder.rightText.setText(Integer.toString(qty));
							
							updateVolumePrice(holder);
							notifyDataSetChanged();
						}
					});
					holder.delete.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							String val = holder.rightText.getText().toString();
							int qty = Integer.parseInt(val);
							qty -= 1;
							if (qty >= 1) {
								pickedQty = Integer.toString(qty);
								holder.rightText.setText(Integer.toString(qty));
								
								updateVolumePrice(holder);
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
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
				switch (type) {
				case 0: {
					if(position==0)
					{
						holder.leftText.setText(R.string.catalog_name);
						holder.leftSubtitle.setText(itemName);	
						holder.leftText.setVisibility(View.VISIBLE);
						holder.leftSubtitle.setVisibility(View.VISIBLE);
					}
					else if(Global.isConsignment)
					{
						holder.leftText.setText(R.string.consignment);
						holder.leftSubtitle.setText(itemConsignmentQty);
					}
					else
					{
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
							// TODO Auto-generated method stub
							String val = holder.rightText.getText().toString();
							int qty = Integer.parseInt(val);
							qty += 1;
							pickedQty = Integer.toString(qty);
							holder.rightText.setText(Integer.toString(qty));
							
							updateVolumePrice(holder);
							notifyDataSetChanged();				
						}
					});
					holder.delete.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							String val = holder.rightText.getText().toString();
							int qty = Integer.parseInt(val);
							qty -= 1;
							if (qty >= 1) {
								pickedQty = Integer.toString(qty);
								holder.rightText.setText(Integer.toString(qty));
								updateVolumePrice(holder);
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
			// TODO Auto-generated method stub
			return null;
		}

		public class ViewHolder {
			TextView leftText;
			TextView leftSubtitle;
			TextView rightText;
			Button add;
			Button delete;
		}
		
		
		private void updateVolumePrice(ViewHolder holder) {

			String[] temp;
			if(global.qtyCounter!=null&&global.qtyCounter.containsKey(prodID))
			{
				BigDecimal origQty = new BigDecimal(global.qtyCounter.get(prodID));
				BigDecimal newQty = origQty.add(Global.getBigDecimalNum(holder.rightText.getText().toString()));
				temp = volPriceHandler.getVolumePrice(newQty.toString(), prodID);
			}
			else
				temp = volPriceHandler.getVolumePrice(holder.rightText.getText().toString(), prodID);
			if (temp[1] != null && !temp[1].isEmpty()) {

				basePrice = temp[1];

				rightTitle[PRICE_LEV] = Global.formatDoubleToCurrency(Double.parseDouble(basePrice)) + " <Base Price>";
				prLevTotal = Global.formatNumToLocale(Double.parseDouble(basePrice));
			} else if (pricelevel_position == 0) {
				if (!isModify)
					basePrice = extrasMap.get("prod_price");
				else
					basePrice = global.orderProducts.get(modifyOrderPosition).getSetData("prod_price", true, null);

				if (basePrice == null || basePrice.isEmpty())
					basePrice = "0.0";

				rightTitle[PRICE_LEV] = Global.formatDoubleToCurrency(Double.parseDouble(basePrice)) + " <Base Price>";
				prLevTotal = Global.formatNumToLocale(Double.parseDouble(basePrice));
			}
		}
	}
}
