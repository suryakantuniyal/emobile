package com.android.emobilepos;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.actionbarsherlock.app.SherlockFragment;
import com.android.database.CustomerInventoryHandler;
import com.android.database.EmpInvHandler;
import com.android.database.OrderProductsHandler;
import com.android.database.OrdersHandler;
import com.android.database.ProductAddonsHandler;
import com.android.database.ProductsHandler;
import com.android.database.TemplateHandler;
import com.android.emobilepos.ordering.OrderLoyalty_FR;
import com.android.emobilepos.ordering.OrderRewards_FR;
import com.android.emobilepos.ordering.OrderTotalDetails_FR;
import com.android.soundmanager.SoundManager;
import com.android.support.CustomerInventory;
import com.android.support.DBManager;
import com.android.support.GenerateNewID;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.Order;
import com.android.support.OrderProducts;
import com.android.support.Orders;
import com.android.support.Post;
import com.android.support.SemiClosedSlidingDrawer;
import com.android.support.SemiClosedSlidingDrawer.OnDrawerCloseListener;
import com.android.support.SemiClosedSlidingDrawer.OnDrawerOpenListener;
import com.honeywell.decodemanager.DecodeManager;
import com.honeywell.decodemanager.SymbologyConfigs;
import com.honeywell.decodemanager.DecodeManager.SymConfigActivityOpeartor;
import com.honeywell.decodemanager.barcode.CommonDefine;
import com.honeywell.decodemanager.barcode.DecodeResult;
import com.honeywell.decodemanager.symbologyconfig.SymbologyConfigCodeUPCA;
import com.viewpagerindicator.CirclePageIndicator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;




public class SalesReceiptFragment extends SherlockFragment implements OnClickListener,OnDrawerOpenListener,OnDrawerCloseListener {
	
	
	private final int REMOVE_ITEM = 0, OVERWRITE_PRICE = 1,UPDATE_HOLD_STATUS = 1,CHECK_OUT_HOLD = 2;
	private Activity activity;
	private SemiClosedSlidingDrawer slidingDrawer;
	private EditText custName,invisibleSearch;
	private ListViewAdapter myAdapter;
	private ListView myListView;

	private TextView title;
	private final String empstr = "";
	private int caseSelected;
	private boolean custSelected;
	private int consignmentType;
	private Global global;
	private int typeOfProcedure = 0;
	private MyPreferences myPref;
	
	private int orientation = 0;
	
	//Honeywell Dolphin black
	private DecodeManager mDecodeManager = null;
	private final int SCANTIMEOUT = 500000;
	private boolean scannerInDecodeMode = false;
	private boolean validPassword = true;
	private CustomerInventoryHandler custInventoryHandler;
	private ProductsHandler prodHandler;
	
	private String ord_HoldName = "";
	private boolean voidOnHold = false;
	
	private ProgressDialog myProgressDialog;
	
	private ViewPager viewPager;
	private CirclePageIndicator pagerIndicator;
	private MyPagerAdapter pagerAdapter;
	private RecalculateCallback callBack;
	
	public interface RecalculateCallback{
		public void recalculateTotal();
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.add_orders_layout, container, false);
		global = (Global) getActivity().getApplication();
		activity = getActivity();
		
		
		myPref = new MyPreferences(activity);
		global.order = new Order(activity);
		final Bundle extras = activity.getIntent().getExtras();
		myAdapter = new ListViewAdapter(activity);

		prodHandler = new ProductsHandler(activity);
		
		

		title = (TextView) view.findViewById(R.id.add_main_title);
		custName = (EditText) view.findViewById(R.id.membersField);
		myListView = (ListView) view.findViewById(R.id.receiptListView);
		slidingDrawer = (SemiClosedSlidingDrawer)view.findViewById(R.id.slideDrawer);
		invisibleSearch = (EditText)view.findViewById(R.id.invisibleEditText);
		
		pagerAdapter = new MyPagerAdapter(getFragmentManager());
		viewPager = (ViewPager) view.findViewById(R.id.orderViewPager);
		viewPager.setAdapter(pagerAdapter);
		pagerIndicator = (CirclePageIndicator)view.findViewById(R.id.indicator);
		pagerIndicator.setViewPager(viewPager);
		pagerIndicator.setCurrentItem(0);
		
		
		
		orientation = getResources().getConfiguration().orientation;
		
		
		Button addProd = (Button) view.findViewById(R.id.addProdButton);
		addProd.setOnClickListener(this);
		if(myPref.getIsTablet()&&orientation==Configuration.ORIENTATION_LANDSCAPE)
		{
			addProd.setVisibility(View.GONE);
		}
			
		
		Button checkout = (Button) view.findViewById(R.id.checkoutButton);
		checkout.setOnClickListener(this);

		ImageView plusBut = (ImageView) view.findViewById(R.id.plusButton);
		plusBut.setOnClickListener(this);



		Button templateBut = (Button) view.findViewById(R.id.templateButton);
		templateBut.setOnClickListener(this);

		Button holdBut = (Button) view.findViewById(R.id.holdButton);
		holdBut.setOnClickListener(this);

		Button detailsBut = (Button) view.findViewById(R.id.detailsButton);
		detailsBut.setOnClickListener(this);

		Button signBut = (Button) view.findViewById(R.id.signButton);
		signBut.setOnClickListener(this);
		
		
		
		ord_HoldName = extras.getString("ord_HoldName");
		custSelected = myPref.isCustSelected();
		
		if (custSelected) 
		{
			typeOfProcedure = extras.getInt("option_number");
			switch (typeOfProcedure) 
			{
				case 0: {
					title.setText("Sales Receipt");
					custName.setText(myPref.getCustName());
					caseSelected = 0;
					Global.ord_type = Global.IS_SALES_RECEIPT;
					break;
				}
				case 1: {
					title.setText("Order");
					custName.setText(myPref.getCustName());
					caseSelected = 1;
					Global.ord_type = Global.IS_ORDER;
					break;
				}
				case 2: {
					title.setText("Return");
					custName.setText(myPref.getCustName());
					caseSelected = 2;
					Global.ord_type = Global.IS_RETURN;
					break;
				}
				case 3: {
					title.setText("Invoice");
					custName.setText(myPref.getCustName());
					caseSelected = 3;
					Global.ord_type = Global.IS_INVOICE;
					break;
				}
				case 4: 
				{
					title.setText("Estimate");
					custName.setText(myPref.getCustName());
					caseSelected = 4;
					Global.ord_type = Global.IS_ESTIMATE;
					break;
				}
				case 9:
				{
					
					Global.isConsignment = true;
					//myPref.setSettings(Global.group_sku, true);
					custInventoryHandler = new CustomerInventoryHandler(activity);
					
					custName.setText(myPref.getCustName());
					plusBut.setVisibility(View.INVISIBLE);
					templateBut.setBackgroundResource(R.drawable.disabled_gloss_button_selector);
					templateBut.setOnClickListener(null);
					holdBut.setBackgroundResource(R.drawable.disabled_gloss_button_selector);
					holdBut.setOnClickListener(null);
					signBut.setBackgroundResource(R.drawable.disabled_gloss_button_selector);
					signBut.setOnClickListener(null);
					
					
					custInventoryHandler.getCustomerInventory();
					
					Global.consignSummaryMap = new HashMap<String,HashMap<String,String>>();
					Global.consignMapKey = new ArrayList<String>();
					caseSelected = 9;
					
					// consignmentType 0 = Rack, 1 = Returns, 2 = Fill-up, 3 = Pick-up
					consignmentType = extras.getInt("consignmentType",0);
					
					if(Global.custInventoryKey==null||Global.custInventoryKey.size()<=0)
					{
						consignmentType = 2;
					}
					Global.consignmentType = consignmentType;
					
					switch(consignmentType)
					{
						case 0:
							title.setText("Rack");
							break;
						case 1:
							title.setText("Return");
							Global.ord_type = Global.IS_RETURN;
							break;
						case 2:
							title.setText("Fill-up");
							Global.ord_type = Global.IS_CONSIGNMENT_FILLUP;
							break;
						case 3:
							Global.ord_type = Global.IS_CONSIGNMENT_PICKUP;
							title.setText("Pick-up");
							break;
					}
					break;
				}	
			}
		} 
		else 
		{
			switch (extras.getInt("option_number")) 
			{
				case 0: {
					title.setText("Sales Receipt");
					caseSelected = 0;
					Global.ord_type = Global.IS_SALES_RECEIPT;
					break;
				}
				case 1: {
					title.setText("Return");
					caseSelected = 1;
					Global.ord_type = Global.IS_RETURN;
					break;
				}
				case 2:
				{
					title.setText("Sales Receipt"); // Without Customer was chosen in Sales Receipts
					caseSelected = 0;
					getActivity().setResult(2);
					Global.ord_type = Global.IS_SALES_RECEIPT;
					break;
				}
			}
		}


		

		myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				// TODO Auto-generated method stub
				
				String isVoidedItem = global.orderProducts.get(position).getSetData("item_void", true, empstr);
				
				if(!isVoidedItem.equals("1"))
				{
					final Dialog dialog = new Dialog(activity,R.style.Theme_TransparentTest);
					dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
					dialog.setCancelable(true);
					dialog.setCanceledOnTouchOutside(false);
					dialog.setContentView(R.layout.picked_item_dialog);
	
					TextView itemName = (TextView) dialog.findViewById(R.id.itemName);
					itemName.setText(global.orderProducts.get(position).getSetData("ordprod_name", true, empstr));
	
					Button remove = (Button) dialog.findViewById(R.id.removeButton);
					Button cancel = (Button) dialog.findViewById(R.id.cancelButton);
					Button modify = (Button) dialog.findViewById(R.id.modifyButton);
					Button overridePrice = (Button) dialog.findViewById(R.id.overridePriceButton);
	
					final int pos = position;
					remove.setOnClickListener(new View.OnClickListener() {
	
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							if (myPref.getPreferences(MyPreferences.pref_require_password_to_remove_void)) 
							{
								showPromptManagerPassword(REMOVE_ITEM,pos);
							}
							else 
							{
								proceedToRemove(pos);
							}
	
							dialog.dismiss();
						}
					});
	
					cancel.setOnClickListener(new View.OnClickListener() {
	
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							dialog.dismiss();
	
						}
					});
	
					modify.setOnClickListener(new View.OnClickListener() {
	
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							Intent intent = new Intent(getActivity(), CatalogPickerFragActivity.class);
							intent.putExtra("isModify", true);
							intent.putExtra("modify_position", pos);
							startActivityForResult(intent, 0);
	
							dialog.dismiss();
						}
					});
	
					overridePrice.setOnClickListener(new View.OnClickListener() {
	
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
	
							dialog.dismiss();
	
							if (myPref.getPreferences(MyPreferences.pref_skip_manager_price_override)) {
								overridePrice(pos);
							} else {
								showPromptManagerPassword(OVERWRITE_PRICE,pos);
							}
						}
					});
					dialog.show();
				}
			}
		});
		
		invisibleSearch.addTextChangedListener(textWatcher());
		invisibleSearch.requestFocus();
		
		
		myListView.setAdapter(myAdapter);
		slidingDrawer.setOnDrawerOpenListener(this);
		slidingDrawer.setOnDrawerCloseListener(this);
		slidingDrawer.close();

		return view;
	}


	
	@Override
	public void onResume() {
		
		if (myListView != null) {
			global = (Global) getActivity().getApplication();
			myListView.invalidateViews();

			if(callBack!=null)
				callBack.recalculateTotal();

		}
		
		if (mDecodeManager == null) {
			mDecodeManager = new DecodeManager(getActivity(),ScanResultHandler);
			try {
				mDecodeManager.disableSymbology(CommonDefine.SymbologyID.SYM_CODE39);
				mDecodeManager.setSymbologyDefaults(CommonDefine.SymbologyID.SYM_UPCA);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		SoundManager.getInstance();
		SoundManager.initSounds(getActivity());
		SoundManager.loadSounds();
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();

		if (mDecodeManager != null) {
			try {
				mDecodeManager.release();
				mDecodeManager = null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onDestroy() 
	{
		if (mDecodeManager != null) {
			try {
				mDecodeManager.release();
				mDecodeManager = null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		super.onDestroy();
	}
	
	
	private TextWatcher textWatcher() {

		TextWatcher tw = new TextWatcher() {
			boolean doneScanning = false;
			private ProductsHandler handler = new ProductsHandler(activity);

			@Override
			public void afterTextChanged(Editable s) {
				if (doneScanning) {
					doneScanning = false;
					String upc = invisibleSearch.getText().toString().trim().replace("\n", "");
					SalesReceiptSplitActivity temp = (SalesReceiptSplitActivity) getActivity();
					if (!myPref.getIsTablet()) {
						invisibleSearch.setText("");
					} else {
						int orientation = getResources().getConfiguration().orientation;
						if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
							EditText test = temp.visibleSearchView();
							if (test != null && !test.isFocused())
								invisibleSearch.setText("");
						} else
							invisibleSearch.setText("");
					}
					if (!upc.isEmpty()) {
						String[] listData = handler.getUPCProducts(upc);
						if (temp != null && listData[0] != null) {
							if (myPref.getPreferences(MyPreferences.pref_group_receipt_by_sku)) {
								int foundPosition = global.checkIfGroupBySKU(activity, listData[0], "1");
								if (foundPosition != -1) // product already
															// exist in list
								{
									global.refreshParticularOrder(myPref, foundPosition);
									myAdapter.notifyDataSetChanged();
								} else
									temp.automaticAddOrder(listData);
							} else
								temp.automaticAddOrder(listData);

							reCalculate();
						}
					}

				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				if (s.toString().contains("\n"))
					doneScanning = true;
			}
		};
		return tw;
	}
	
	
	private  class MyPagerAdapter extends FragmentPagerAdapter {
        public MyPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }
 
        @Override
        public int getCount() {
            return 3;
        }
 
        @Override
        public Fragment getItem(int position) {
            switch (position) {
            case 0: // Fragment # 0 - This will show image
            	Fragment frag = OrderTotalDetails_FR.init(position);
            	callBack = (RecalculateCallback)frag;
                return frag;
            case 1: // Fragment # 1 - This will show image
                return OrderLoyalty_FR.init(position);
            default:// Fragment # 2-9 - Will show list
                return OrderRewards_FR.init(position);

            }
        }
    }
	
	

	private void overridePrice(final int position) {
		final EditText input = new EditText(activity);
		input.setInputType(InputType.TYPE_CLASS_NUMBER);
		
		input.addTextChangedListener(new TextWatcher() 
		{
	        public void afterTextChanged(Editable s) {				
	        }
	        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

	        public void onTextChanged(CharSequence s, int start, int before, int count) {parseInputedCurrency(s,input);}
	    });
			
		new AlertDialog.Builder(activity).setTitle(getString(R.string.enter_price)).setView(input).setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface thisDialog, int which) {
				// TODO Auto-generated method stub
				String value = input.getText().toString().replaceAll("[^\\d\\,\\.]", "");
				if (!value.isEmpty()) {
					
					String temp = Double.toString(Global.formatNumFromLocale(value));
					global.orderProducts.get(position).getSetData("overwrite_price", false, temp);
					global.orderProducts.get(position).getSetData("pricelevel_id", false, "");

					global.recalculateOrderProduct(position);
					myListView.invalidateViews();
					reCalculate();
				}
				thisDialog.dismiss();
			}
		}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface thisDialog, int which) {
				// TODO Auto-generated method stub
				thisDialog.dismiss();
			}
		}).show();
		
	}

	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent intent;
		switch (v.getId()) {
		case R.id.checkoutButton:
			if (myListView.getCount() == 0 && caseSelected!=9) 
			{
				Toast.makeText(activity, getString(R.string.warning_empty_products), Toast.LENGTH_SHORT).show();
			}
			else if(myPref.getPreferences(MyPreferences.pref_signature_required_mode)&&global.encodedImage.isEmpty())
			{
				Toast.makeText(activity, R.string.warning_signature_required, Toast.LENGTH_LONG).show();
			}
			else if(myPref.getPreferences(MyPreferences.pref_require_address)&&global.getSelectedAddressString().isEmpty())
			{
				Toast.makeText(activity, R.string.warning_ship_address_required, Toast.LENGTH_LONG).show();
			}
			else
			{
				
				if(myPref.getPreferences(MyPreferences.pref_skip_want_add_more_products))
				{
					if(myPref.getPreferences(MyPreferences.pref_skip_email_phone))
						processOrder("","",false);
					else
						showEmailDlog();
				}
				else
				{
					showAddMoreProductsDlg();
				}
			}
			
			break;
		case R.id.plusButton:
			intent = new Intent(getActivity(), CustomerSelectionMenuActivity.class);
			startActivityForResult(intent, 0);
			break;
		case R.id.addProdButton:
			intent = new Intent(getActivity(), CatalogMenuFragmentActivity.class);
			startActivityForResult(intent, 0);
			break;
		case R.id.templateButton:
			loadSaveTemplate();
			break;
		case R.id.holdButton:
			
			if(global.orderProducts!=null&&global.orderProducts.size()>0)
			{
				processOrder("","",true);

			}
			else
				Toast.makeText(activity, getString(R.string.warning_empty_products), Toast.LENGTH_SHORT).show();
			break;
		case R.id.detailsButton:
			intent = new Intent(getActivity(), OrderDetailsActivity.class);
			startActivity(intent);
			break;
		case R.id.signButton:
			orientation = getResources().getConfiguration().orientation;
			intent = new Intent(getActivity(), DrawReceiptActivity.class);
			if(orientation == Configuration.ORIENTATION_PORTRAIT)
				intent.putExtra("inPortrait", true);
			else
				intent.putExtra("inPortrait", false);
			activity.startActivityForResult(intent,0);
			break;
		}
	}


	
	private void parseInputedCurrency(CharSequence s,EditText field)
	{
    	DecimalFormat format = (DecimalFormat) DecimalFormat.getInstance(Locale.getDefault());
        DecimalFormatSymbols sym = format.getDecimalFormatSymbols();
    	StringBuilder sb = new StringBuilder();
    	sb.append("^\\").append(sym.getCurrencySymbol()).append("\\s(\\d{1,3}(\\").append(sym.getGroupingSeparator()).append("\\d{3})*|(\\d+))(");
    	sb.append(sym.getDecimalSeparator()).append("\\d{2})?$");
    	
        if(!s.toString().matches(sb.toString()))
        {
            String userInput= ""+s.toString().replaceAll("[^\\d]", "");
            StringBuilder cashAmountBuilder = new StringBuilder(userInput);

            while (cashAmountBuilder.length() > 3 && cashAmountBuilder.charAt(0) == '0') {
                cashAmountBuilder.deleteCharAt(0);
            }
            while (cashAmountBuilder.length() < 3) {
                cashAmountBuilder.insert(0, '0');
            }

            cashAmountBuilder.insert(cashAmountBuilder.length()-2, sym.getDecimalSeparator());
            cashAmountBuilder.insert(0, sym.getCurrencySymbol()+" ");

            field.setText(cashAmountBuilder.toString());
            
        }
            // keeps the cursor always to the right

        Selection.setSelection(field.getText(), field.getText().length());
	}

	public void showEmailDlog() {

		final Dialog dialog = new Dialog(activity,R.style.Theme_TransparentTest);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setCancelable(true);
		dialog.setCanceledOnTouchOutside(false);
		dialog.setContentView(R.layout.checkout_dialog_layout);
		final EditText input = (EditText) dialog.findViewById(R.id.emailTxt);
		final EditText phoneNum = (EditText)dialog.findViewById(R.id.phoneNumField);
		Button done = (Button) dialog.findViewById(R.id.OKButton);

		if(myPref.isCustSelected())
			input.setText(myPref.getCustEmail());
		
		done.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dialog.dismiss();

				
					if(!input.getText().toString().isEmpty())
					{
						if(checkEmail(input.getText().toString()))
							processOrder(input.getText().toString(),phoneNum.getText().toString(),false);
						else
							Toast.makeText(activity, getString(R.string.warning_email_invalid), Toast.LENGTH_LONG).show();
					}
					else
						processOrder(input.getText().toString(),phoneNum.getText().toString(),false);
			}
		});
		dialog.show();
	}
	
	
	
	private void showAddMoreProductsDlg()
	{
		
		final Dialog dlog = new Dialog(activity,R.style.Theme_TransparentTest);
		dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dlog.setCancelable(true);
		dlog.setCanceledOnTouchOutside(true);
		dlog.setContentView(R.layout.dlog_btn_left_right_layout);
		
		TextView viewTitle = (TextView)dlog.findViewById(R.id.dlogTitle);
		TextView viewMsg = (TextView)dlog.findViewById(R.id.dlogMessage);
		viewTitle.setText(R.string.dlog_title_confirm);
		viewMsg.setText(R.string.dlog_msg_add_more_products);
		Button btnYes = (Button)dlog.findViewById(R.id.btnDlogLeft);
		Button btnNo = (Button)dlog.findViewById(R.id.btnDlogRight);
		btnYes.setText(R.string.button_yes);
		btnNo.setText(R.string.button_no);
		
		btnYes.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dlog.dismiss();
			}
		});
		btnNo.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dlog.dismiss();
				
				if(myPref.getPreferences(MyPreferences.pref_skip_email_phone))
					processOrder("","",false);
				else
					showEmailDlog();
			}
		});
		dlog.show();
	}
	
	
	
	private boolean checkEmail(String paramString)
	{
		String[] emails = paramString.split(";");
		int size = emails.length;
		for(int i = 0 ; i < size;i++)
		{
			if(!Patterns.EMAIL_ADDRESS.matcher(emails[i].trim()).matches())
				return false;
		}
		return true;
	}
	

	
	
	private void processOrder(String emailHolder,String phoneNumber,boolean buttonOnHold) {
		
		OrdersHandler handler = new OrdersHandler(activity);
		global.order = new Order(activity);

		//double temp =Global.formatNumWithCurrFromLocale(OrderTotalDetails_FR.granTotal.getText().toString());
		global.order.getSetData("ord_total", false, OrderTotalDetails_FR.gran_total.toString());
		//temp =Global.formatNumWithCurrFromLocale(subTotal.getText().toString());
		global.order.getSetData("ord_subtotal", false, OrderTotalDetails_FR.sub_total.toString());

		
		global.order.getSetData("ord_id", false, Global.lastOrdID);
		global.order.getSetData("ord_signature", false, global.encodedImage);
		global.order.getSetData("qbord_id", false, Global.lastOrdID.replace("-", empstr));
		String email = emailHolder;
		if (email == null)
			global.order.getSetData("c_email", false, "");
		else
			global.order.getSetData("c_email", false, email);

		global.order.getSetData("cust_id", false, myPref.getCustID());
		global.order.getSetData("custidkey", false, myPref.getCustIDKey());

		global.order.getSetData("ord_type", false, Global.ord_type);
		
		global.order.getSetData("tax_id", false, OrderTotalDetails_FR.taxID);
		global.order.getSetData("ord_discount_id", false, OrderTotalDetails_FR.discountID);
		

		int totalLines = global.orderProducts.size();
		if(myPref.getPreferences(MyPreferences.pref_restaurant_mode)&&Global.orderProductAddonsMap!=null&&Global.orderProductAddonsMap.size()>0)
		{
			String[] keys = Global.orderProductAddonsMap.keySet().toArray(new String[ Global.orderProductAddonsMap.size()]);
			int tempSize = keys.length;
			for(int i = 0 ; i < tempSize;i++)
			{
				
				totalLines +=  Global.orderProductAddonsMap.get(keys[i]).size();
			}
		}
		
		if(!myPref.getShiftIsOpen())
			global.order.getSetData("clerk_id", false, myPref.getShiftClerkID());
		else if(myPref.getPreferences(MyPreferences.pref_use_clerks))
			global.order.getSetData("clerk_id", false, myPref.getClerkID());
		
		global.order.getSetData("total_lines", false, Integer.toString(totalLines));
		global.order.getSetData("ord_taxamount", false, OrderTotalDetails_FR.tax_amount.toString());
		global.order.getSetData("ord_discount", false, OrderTotalDetails_FR.discount_amount.toString());
		
		
		global.order.getSetData("ord_shipvia", false, global.getSelectedShippingMethodString());
		global.order.getSetData("ord_delivery", false, global.getSelectedDeliveryDate());
		global.order.getSetData("ord_terms", false, global.getSelectedTermsMethodsString());
		global.order.getSetData("ord_shipto", false, global.getSelectedAddressString());
		global.order.getSetData("ord_comment", false, global.getSelectedComments());
		global.order.getSetData("ord_po", false, global.getSelectedPO());
		
		String[] location = Global.getCurrLocation(activity);
		global.order.getSetData("ord_latitude", false, location[0]);
		global.order.getSetData("ord_longitude", false, location[1]);


		OrderProductsHandler handler2 = new OrderProductsHandler(activity);
		if(caseSelected!=9)
		{
			if(buttonOnHold)
			{
				global.order.getSetData("isOnHold", false, "1");
				
				if(Global.isFromOnHold)
				{
					global.order.getSetData("ord_HoldName", false, ord_HoldName);
					global.order.getSetData("ord_timecreated", false, handler.updateFinishOnHold(Global.lastOrdID));
					
					handler.insert(global.order);
					global.encodedImage = new String();
					handler2.insert(global.orderProducts);
					
					
					if(myPref.getPreferences(MyPreferences.pref_restaurant_mode))
						new printAsync().execute(true);
					
								
					DBManager dbManager = new DBManager(activity);
					dbManager.synchSendOrdersOnHold(false,false);
				}
				else
					showOnHoldPromptName(handler,handler2);
			}
			else if(Global.isFromOnHold)
			{
				if(!voidOnHold)
				{
					global.order.getSetData("ord_timecreated", false, handler.updateFinishOnHold(Global.lastOrdID));
					
					handler.insert(global.order);
					global.encodedImage = new String();
					handler2.insert(global.orderProducts);
					
					if(myPref.getPreferences(MyPreferences.pref_restaurant_mode))
						new printAsync().execute(true);
					
					DBManager dbManager = new DBManager(activity);
					dbManager.synchSendOrdersOnHold(false,true);
				}
				else
				{
					global.order.getSetData("ord_timecreated", false, handler.updateFinishOnHold(Global.lastOrdID));
					
					global.order.getSetData("isVoid", false, "1");
					handler.insert(global.order);
					global.encodedImage = new String();
					handler2.insert(global.orderProducts);
					
					new onHoldAsync().execute(CHECK_OUT_HOLD);

				}
			}
			else
			{
				handler.insert(global.order);
				global.encodedImage = new String();
				handler2.insert(global.orderProducts);
				
				if(myPref.getPreferences(MyPreferences.pref_restaurant_mode))
					new printAsync().execute(true);
				
			}
		}
		
		if(!buttonOnHold&&!voidOnHold)
		{
			switch (caseSelected) 
			{
				case 0: // is Sales Receipt
				{
					this.updateLocalInventory(false);
					typeOfProcedure = 5;
					isSalesReceipt();
					break;
				}
				case 1: {
					if (custSelected) // is Order
					{
						typeOfProcedure = 0;
						if (myPref.getPreferences(MyPreferences.pref_enable_printing))
							showPrintDlg();
						else if(!Global.isFromOnHold)
							getActivity().finish();
					} 
					else// is Return
					{
						this.updateLocalInventory(true);
						typeOfProcedure = 1;
						if(myPref.getPreferences(MyPreferences.pref_return_require_refund))
							proceedToRefund();
						else
							showRefundDlg();
					}
					break;
				}
				case 2: 
				{
					if (custSelected)			//Return 
					{
						this.updateLocalInventory(true);
						typeOfProcedure = 1;
						if(myPref.getPreferences(MyPreferences.pref_return_require_refund))
							proceedToRefund();
						else
							showRefundDlg();
					}
					break;
				}
				case 3:
				{
					if (custSelected)			//Invoice
					{
						this.updateLocalInventory(false);
						typeOfProcedure = 2;
						showPaymentDlg();
					}
					break;
				}
				case 4:
				{
					if (custSelected)			//Estimate
					{
						typeOfProcedure = 3;
						if (myPref.getPreferences(MyPreferences.pref_enable_printing))
							showPrintDlg();
						else
							getActivity().finish();
					}
		
					break;
				}
				case 9:			//Consignment
				{
					
					if(consignmentType==3 || consignmentType == 2)
					{
						processConsignment(consignmentType);
						Intent intent = new Intent(activity,ConsignFragmentActivity.class);
						intent.putExtra("consignmentType", consignmentType);
						intent.putExtra("ord_signature", global.encodedImage);
						global.encodedImage = new String();
						startActivity(intent);
						activity.finish();
					}
					else
					{
						updateConsignmentType(true);
					}
					break;
				}
			}
			global.orderProducts = new ArrayList<OrderProducts>();
			global.qtyCounter = new HashMap<String,String>();
		}
		
	}

	
	
	private void processConsignment(int type)
	{
		GenerateNewID idGenerator = new GenerateNewID(activity);
		HashMap<String,HashMap<String,String>> summaryMap = new HashMap<String,HashMap<String,String>>();
		HashMap<String,String>tempMap = new HashMap<String,String>();
		CustomerInventory custInventory;
		int size = 0,size2 = 0;
		String[] temp;
		
		switch(consignmentType)
		{
		
		case 0://Rack
			Global.consignment_order = global.order;
			Global.consignment_products = global.orderProducts;
			Global.consignment_qtyCounter = new HashMap<String,String>(global.qtyCounter);
			
			size2 = Global.custInventoryKey.size();
			size = Global.consignment_products.size();
			
			double sold = 0;
			for(int i = 0 ; i < size;i++)
			{
				Global.consignMapKey.add(Global.consignment_products.get(i).getSetData("prod_id", true, null));
				tempMap.put("rack", Global.consignment_products.get(i).getSetData("ordprod_qty", true, null));
				tempMap.put("rack_index", Integer.toString(i));
				
				temp = Global.custInventoryMap.get(Global.consignment_products.get(i).getSetData("prod_id", true, null));
				if(temp!=null)
				{
					sold = Double.parseDouble(temp[2])-Double.parseDouble(tempMap.get("rack"));
					tempMap.put("original_qty", temp[2]);
					tempMap.put("invoice", Double.toString(sold));
					
				}
				
				
				tempMap.put("prod_id", Global.consignment_products.get(i).getSetData("prod_id", true, null));
				tempMap.put("ordprod_name", Global.consignment_products.get(i).getSetData("ordprod_name", true, null));
				
				tempMap.put("invoice_total", Double.toString(sold*Double.parseDouble(Global.consignment_products.get(i).getSetData("overwrite_price", true, null))));
				
				tempMap.put("prod_price", Global.consignment_products.get(i).getSetData("overwrite_price", true, null));
				summaryMap.put(Global.consignment_products.get(i).getSetData("prod_id", true, null), tempMap);
				tempMap = new HashMap<String,String>();
			}
			
			
			
			for(int i = 0 ; i < size2; i++)
			{
				if(!Global.consignMapKey.contains(Global.custInventoryKey.get(i)))
				{
					temp = Global.custInventoryMap.get(Global.custInventoryKey.get(i));
					if(temp[3]==null||temp[3].isEmpty())
						temp[3] = prodHandler.getProductPrice(Global.consignment_products.get(i).getSetData("prod_id", true, null));
					tempMap.put("invoice", temp[2]);
					tempMap.put("invoice_total",Double.toString(Double.parseDouble(temp[2])*Double.parseDouble(temp[3])));
					tempMap.put("original_qty", temp[2]);
					tempMap.put("prod_id", temp[0]);
					tempMap.put("ordprod_name", temp[1]);
					
					tempMap.put("prod_price", temp[3]);
					summaryMap.put(temp[0], tempMap);
					Global.consignMapKey.add(temp[0]);
					tempMap = new HashMap<String,String>();
				}
			}
			
			
			Global.consignSummaryMap = summaryMap;
			Global.lastOrdID = "";
			
			break;
			
		case 1://Returns
			Global.cons_return_order = global.order;
			Global.cons_return_products = global.orderProducts;
			Global.cons_return_qtyCounter = global.qtyCounter;
			
			size = Global.cons_return_products.size();
			double invoiceTotal,invoiceQty;
			
			
			for(int i = 0 ; i < size; i++)
			{
				tempMap = Global.consignSummaryMap.get(Global.cons_return_products.get(i).getSetData("prod_id", true, null));
				if(tempMap == null)
				{
					tempMap = new HashMap<String,String>();
					Global.consignMapKey.add(Global.cons_return_products.get(i).getSetData("prod_id", true, null));
					tempMap.put("prod_id", Global.cons_return_products.get(i).getSetData("prod_id", true, null));
					tempMap.put("ordprod_name", Global.cons_return_products.get(i).getSetData("ordprod_name", true, null));
					tempMap.put("prod_price", Global.cons_return_products.get(i).getSetData("overwrite_price", true, null));
				}
				else
				{
					invoiceTotal = Double.parseDouble(tempMap.get("invoice_total"));
					invoiceQty = Double.parseDouble(tempMap.get("invoice"));
					invoiceTotal -= Double.parseDouble(tempMap.get("prod_price"))*Double.parseDouble(Global.cons_return_products.get(i).getSetData("ordprod_qty", true, null));
					invoiceQty -= Double.parseDouble(Global.cons_return_qtyCounter.get(Global.cons_return_products.get(i).getSetData("prod_id", true, null)));
					tempMap.put("invoice", Double.toString(invoiceQty));
					tempMap.put("invoice_total", Double.toString(invoiceTotal));
				}
				tempMap.put("return", Global.cons_return_products.get(i).getSetData("ordprod_qty", true, null));
				tempMap.put("return_index", Integer.toString(i));
				
				Global.consignSummaryMap.put(Global.cons_return_products.get(i).getSetData("prod_id", true, null), tempMap);
				tempMap = new HashMap<String,String>();
			}
			

			break;
			
		case 2://Fill-up
			Global.cons_fillup_order = global.order;
			Global.cons_fillup_products = global.orderProducts;
			Global.cons_fillup_qtyCounter = global.qtyCounter;
			
			Global.custInventoryList = new ArrayList<CustomerInventory>();
			custInventory = new CustomerInventory();
			double invoiceTotalTemp = 0;
			size = Global.cons_fillup_products.size();
			
			if(Global.cons_return_products.size()>0&&size>0)
			{
				Global.lastOrdID  = idGenerator.generate(Global.lastOrdID, 0);
			}
			
			Global.cons_fillup_order.getSetData("ord_id", false, Global.lastOrdID);
		
			for(int i = 0 ; i < size; i++)
			{
				tempMap = Global.consignSummaryMap.get(Global.cons_fillup_products.get(i).getSetData("prod_id", true, null));
				if(tempMap==null)
				{
					tempMap = new HashMap<String,String>();
					Global.consignMapKey.add(Global.cons_fillup_products.get(i).getSetData("prod_id", true, null));
					tempMap.put("prod_id", Global.cons_fillup_products.get(i).getSetData("prod_id", true, null));
					tempMap.put("ordprod_name", Global.cons_fillup_products.get(i).getSetData("ordprod_name", true, null));
					//tempMap.put("prod_price", Global.cons_fillup_products.get(i).getSetData("overwrite_price", true, null));
				}

				tempMap.put("fillup", Global.cons_fillup_products.get(i).getSetData("ordprod_qty", true, null));
				tempMap.put("prod_price", Global.cons_fillup_products.get(i).getSetData("overwrite_price", true, null));
				if(tempMap.get("invoice")!=null)
					invoiceTotalTemp = Double.parseDouble(tempMap.get("invoice"))*Double.parseDouble(tempMap.get("prod_price"));
				else
					invoiceTotalTemp = 0;
				
				tempMap.put("invoice_total", Double.toString(invoiceTotalTemp));
				tempMap.put("fillup_index", Integer.toString(i));
				
				
				
				Global.cons_fillup_products.get(i).getSetData("ord_id", false, Global.lastOrdID);
				Global.consignSummaryMap.put(Global.cons_fillup_products.get(i).getSetData("prod_id", true, null), tempMap);
				tempMap = new HashMap<String,String>();
			}
			
			
			
			String tempProdID;
			String fillUpQty; 
			String rackQty;
			double newStockQty;
			size2 = Global.consignMapKey.size();
			for(int i = 0 ; i < size2; i++)
			{
				tempProdID = Global.consignMapKey.get(i);
				custInventory.getSetData("prod_id", false, tempProdID);
				custInventory.getSetData("prod_name", false, Global.consignSummaryMap.get(tempProdID).get("ordprod_name"));
				custInventory.getSetData("price", false, Global.consignSummaryMap.get(tempProdID).get("prod_price"));
				custInventory.getSetData("cust_id", false, myPref.getCustID());
				
				if(Global.consignSummaryMap.get(tempProdID).get("fillup")!=null||Global.consignSummaryMap.get(tempProdID).get("rack")!=null)
				{
					fillUpQty = Global.consignSummaryMap.get(tempProdID).get("fillup");
					if(fillUpQty==null)
						fillUpQty = "0";
					rackQty =  Global.consignSummaryMap.get(tempProdID).get("rack");
					if(rackQty == null)
						rackQty = "0";
					
					newStockQty = Double.parseDouble(fillUpQty)+ Double.parseDouble(rackQty);
					custInventory.getSetData("qty", false, Double.toString(newStockQty));
				}
				else
				{
					custInventory.getSetData("qty", false, "0");
				}
				
				Global.custInventoryList.add(custInventory);
				custInventory = new CustomerInventory();	
			}
			break;
			
			
		case 3://pickup
			Global.consignment_order = global.order;
			Global.consignment_products = global.orderProducts;
			Global.consignment_qtyCounter = global.qtyCounter;
			
			size = Global.consignment_products.size();
			
			Global.custInventoryList = new ArrayList<CustomerInventory>();
			custInventory = new CustomerInventory();
			
			
			for(int i = 0 ; i < size;i++)
			{
				Global.consignMapKey.add(Global.consignment_products.get(i).getSetData("prod_id", true, null));
				tempMap.put("pickup", Global.consignment_products.get(i).getSetData("ordprod_qty", true, null));
				tempMap.put("prod_id", Global.consignment_products.get(i).getSetData("prod_id", true, null));
				tempMap.put("ordprod_name", Global.consignment_products.get(i).getSetData("ordprod_name", true, null));
				tempMap.put("prod_price", Global.consignment_products.get(i).getSetData("overwrite_price", true, null));
				
				temp = Global.custInventoryMap.get(Global.consignment_products.get(i).getSetData("prod_id", true, null));
				if(temp!=null)
				{
					tempMap.put("original_qty", temp[2]);
					custInventory.getSetData("prod_id", false,tempMap.get("prod_id"));
					custInventory.getSetData("prod_name", false, tempMap.get("ordprod_name"));
					custInventory.getSetData("price", false, tempMap.get("prod_price"));
					custInventory.getSetData("cust_id", false, myPref.getCustID());
					custInventory.getSetData("qty", false, Double.toString(Double.parseDouble(tempMap.get("original_qty"))-Double.parseDouble(tempMap.get("pickup"))));

					Global.custInventoryList.add(custInventory);
					custInventory = new CustomerInventory();
				}
				
				summaryMap.put(Global.consignment_products.get(i).getSetData("prod_id", true, null), tempMap);
					
				tempMap = new HashMap<String,String>();				
			}
			
			Global.consignSummaryMap = summaryMap;
			break;
		}
	}
	
	
	
	
	private void updateConsignmentType(boolean shouldProcess)
	{
		if(shouldProcess)
			processConsignment(consignmentType);
		
		consignmentType+=1;
		
		Global.consignmentType = consignmentType;
		switch(consignmentType)
		{
		case 0:
			title.setText("Rack");
			break;
		case 1:
			title.setText("Return");
			Global.ord_type = Global.IS_RETURN;
			break;
		case 2:
			title.setText("Fill-up");
			Global.ord_type = Global.IS_CONSIGNMENT_FILLUP;
			break;
		case 3:
			title.setText("Pick-up");
			Global.ord_type = Global.IS_CONSIGNMENT_PICKUP;
			break;
		}
		
		global.orderProducts = new ArrayList<OrderProducts>();
		global.qtyCounter.clear();
		myAdapter.notifyDataSetChanged();
	}
	
	
	
	
	private void isSalesReceipt() 
	{
		Intent intent = new Intent(activity, RefundMenuActivity.class);
		intent.putExtra("typeOfProcedure", typeOfProcedure);
		intent.putExtra("salesreceipt", true);
		intent.putExtra("amount", global.order.getSetData("ord_total", true, empstr));
		intent.putExtra("paid", "0.00");
		intent.putExtra("is_receipt", true);
		intent.putExtra("job_id", global.order.getSetData("ord_id", true, empstr));
		intent.putExtra("ord_subtotal", global.order.getSetData("ord_subtotal", true, empstr));
		intent.putExtra("ord_taxID", OrderTotalDetails_FR.taxID);
		intent.putExtra("ord_type", Global.ord_type);
		
		if(myPref.isCustSelected())
		{
			intent.putExtra("cust_id", myPref.getCustID());
			intent.putExtra("custidkey", myPref.getCustIDKey());
		}
		startActivityForResult(intent, 0);
	}

	
	
	
	private void updateLocalInventory(boolean isIncrement)
	{
		EmpInvHandler eiHandler = new EmpInvHandler(activity);
		int size = global.orderProducts.size();
		for(int i = 0 ; i<size;i++)
		{
			eiHandler.updateOnHand(global.orderProducts.get(i).getSetData("prod_id", true, null),
					global.orderProducts.get(i).getSetData("ordprod_qty", true, null), isIncrement);
		}
	}
	
	
	
	private void showPromptManagerPassword(final int type,final int position)
	{
		
		final Dialog globalDlog = new Dialog(activity,R.style.Theme_TransparentTest);
		globalDlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		globalDlog.setCancelable(true);
		globalDlog.setContentView(R.layout.dlog_field_single_layout);
		
		final EditText viewField = (EditText)globalDlog.findViewById(R.id.dlogFieldSingle);
		viewField.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
		TextView viewTitle = (TextView)globalDlog.findViewById(R.id.dlogTitle);
		TextView viewMsg = (TextView)globalDlog.findViewById(R.id.dlogMessage);
		viewTitle.setText(R.string.dlog_title_confirm);
		if(!validPassword)
			viewMsg.setText(R.string.invalid_password);
		else
			viewMsg.setText(R.string.dlog_title_enter_manager_password);
		
		Button btnOk = (Button)globalDlog.findViewById(R.id.btnDlogSingle);
		btnOk.setText(R.string.button_ok);
		btnOk.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				globalDlog.dismiss();
				String value = viewField.getText().toString().trim();
				if (value.equals(myPref.posManagerPass(true,null))) // validate admin password
				{
					validPassword = true;
					switch(type)
					{
					case REMOVE_ITEM:
						proceedToRemove(position);
						break;
					case OVERWRITE_PRICE:
						overridePrice(position);
						break;
					}
					
				}
				else
				{
					globalDlog.dismiss();
					validPassword = false;
					showPromptManagerPassword(type,position);
				}
			}
		});
		globalDlog.show();
	}
	
	
	
	private void showOnHoldPromptName(final OrdersHandler handler,final OrderProductsHandler handler2 )
	{
		final Dialog globalDlog = new Dialog(activity,R.style.Theme_TransparentTest);
		globalDlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		globalDlog.setCancelable(false);
		globalDlog.setContentView(R.layout.dlog_field_single_layout);
		
		final EditText viewField = (EditText)globalDlog.findViewById(R.id.dlogFieldSingle);
		viewField.setInputType(InputType.TYPE_CLASS_TEXT);
		TextView viewTitle = (TextView)globalDlog.findViewById(R.id.dlogTitle);
		TextView viewMsg = (TextView)globalDlog.findViewById(R.id.dlogMessage);
		viewTitle.setText(R.string.dlog_title_confirm);
		viewMsg.setText(R.string.enter_name);
		
		Button btnOk = (Button)globalDlog.findViewById(R.id.btnDlogSingle);
		btnOk.setText(R.string.button_ok);
		btnOk.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				globalDlog.dismiss();
				String value = viewField.getText().toString().trim();
				if(!value.isEmpty())
				{
					global.order.getSetData("ord_HoldName", false, value);
					handler.insert(global.order);
					global.encodedImage = new String();
					handler2.insert(global.orderProducts);
					
					
					new printAsync().execute(true);
					
					
					global.orderProducts = new ArrayList<OrderProducts>();
					global.qtyCounter.clear();
					
					
					DBManager dbManager = new DBManager(activity);
					dbManager.synchSendOrdersOnHold(false,false);
				}
				else
				{
					showOnHoldPromptName(handler,handler2);
				}
			}
		});
		globalDlog.show();
	}
	
	
	
	
	public void voidCancelOnHold(int type)
	{
		switch(type)
		{
		case 1://void hold
			voidOnHold = true;
			processOrder("","",false);
			break;
		case 2://cancel hold
			voidOnHold = false;
			processOrder("","",true);
			break;
		}
	}
	
	
	private class onHoldAsync extends AsyncTask<Integer, Integer,String>
	{
		@Override
		protected void onPreExecute() {
			myProgressDialog = new ProgressDialog(getActivity());
			myProgressDialog.setMessage("Sending...");
			myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			myProgressDialog.setCancelable(false);
			if(myProgressDialog.isShowing())
				myProgressDialog.dismiss();
			

		}
		@Override
		protected String doInBackground(Integer... arg0) {
			// TODO Auto-generated method stub
			
			
			if(Global.isConnectedToInternet(activity))
			{
				Post httpClient = new Post();
				switch(arg0[0])
				{
				case UPDATE_HOLD_STATUS:
					httpClient.postData(Global.S_UPDATE_STATUS_ON_HOLD, activity, Global.lastOrdID);
					break;
				case CHECK_OUT_HOLD:
					httpClient.postData(Global.S_CHECKOUT_ON_HOLD, activity, Global.lastOrdID);
					break;
				}
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(String unused) {
			myProgressDialog.dismiss(); 
			if(voidOnHold)
				getActivity().finish();

		}
		
	}
	
	
	
	
	private void showPrintDlg() 
	{
		
		final Dialog dlog = new Dialog(activity,R.style.Theme_TransparentTest);
		dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dlog.setCancelable(false);
		dlog.setContentView(R.layout.dlog_btn_left_right_layout);
		
		TextView viewTitle = (TextView)dlog.findViewById(R.id.dlogTitle);
		TextView viewMsg = (TextView)dlog.findViewById(R.id.dlogMessage);
		viewTitle.setText(R.string.dlog_title_confirm);
		viewMsg.setText(R.string.dlog_msg_want_to_print);
		Button btnYes = (Button)dlog.findViewById(R.id.btnDlogLeft);
		Button btnNo = (Button)dlog.findViewById(R.id.btnDlogRight);
		btnYes.setText(R.string.button_yes);
		btnNo.setText(R.string.button_no);
		
		btnYes.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dlog.dismiss();
				new printAsync().execute(false);
				
			}
		});
		btnNo.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dlog.dismiss();
				getActivity().finish();
			}
		});
		dlog.show();
		
	}

	
	
	
	
	private class printAsync extends AsyncTask<Boolean, Integer, String> 
	{
		boolean isPrintStationPrinter = false;
		@Override
		protected void onPreExecute() {
			myProgressDialog = new ProgressDialog(getActivity());
			myProgressDialog.setMessage("Printing...");
			myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			myProgressDialog.setCancelable(false);
			if(myProgressDialog.isShowing())
				myProgressDialog.dismiss();
			

		}

		@Override
		protected void onProgressUpdate(Integer... params) 
		{
			if(!myProgressDialog.isShowing())
				myProgressDialog.show();
		}

		
		@Override
		protected String doInBackground(Boolean... params) {
			// TODO Auto-generated method stub

			isPrintStationPrinter = params[0];
			if(!isPrintStationPrinter)
			{
				publishProgress();
				int type = Integer.parseInt(Global.ord_type);
				if(Global.mainPrinterManager!=null&&Global.mainPrinterManager.currentDevice!=null)
				{
					Global.mainPrinterManager.currentDevice.printTransaction(global.order.getSetData("ord_id", true, empstr),type,false,false);
				}
			}
			else
			{
				OrderProductsHandler handler2 = new OrderProductsHandler(activity);
				HashMap<String,List<Orders>>temp = handler2.getStationPrinterProducts(global.order.getSetData("ord_id", true, empstr));
				
				String[] sArr = temp.keySet().toArray(new String[temp.keySet().size()]);
				int size = sArr.length;
				int printMap = 0;
				for(int i = 0;i<size;i++)
				{
					if(Global.multiPrinterMap.containsKey(sArr[i]))
					{
						printMap = Global.multiPrinterMap.get(sArr[i]);

						if(Global.multiPrinterManager.get(printMap)!=null&&Global.multiPrinterManager.get(printMap).currentDevice!=null)
							Global.multiPrinterManager.get(printMap).currentDevice.printStationPrinter(temp.get(sArr[i]), global.order.getSetData("ord_id", true, empstr));
					}
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(String unused) {
			if(!isPrintStationPrinter)
			{
				myProgressDialog.dismiss();	
				getActivity().finish();
			}
			
			
		}
	}
	
	
	private void proceedToRefund()
	{
		Intent intent = new Intent(activity, RefundMenuActivity.class);
		intent.putExtra("typeOfProcedure", typeOfProcedure);
		intent.putExtra("salesrefund", true);
		intent.putExtra("amount", global.order.getSetData("ord_total", true, empstr));
		intent.putExtra("paid", "0.00");
		intent.putExtra("job_id", global.order.getSetData("ord_id", true, empstr));
		intent.putExtra("ord_type", Global.ord_type);
		
		if(myPref.isCustSelected())
		{
			intent.putExtra("cust_id", myPref.getCustID());
			intent.putExtra("custidkey", myPref.getCustIDKey());
		}
		
		startActivityForResult(intent, 0);
	}
	
	
	private void showRefundDlg() 
	{
		final Dialog dlog = new Dialog(activity,R.style.Theme_TransparentTest);
		dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dlog.setCancelable(false);
		dlog.setContentView(R.layout.dlog_btn_left_right_layout);
		
		TextView viewTitle = (TextView)dlog.findViewById(R.id.dlogTitle);
		TextView viewMsg = (TextView)dlog.findViewById(R.id.dlogMessage);
		viewTitle.setText(R.string.dlog_title_confirm);
		viewMsg.setText(R.string.dlog_msg_want_to_make_refund);
		Button btnYes = (Button)dlog.findViewById(R.id.btnDlogLeft);
		Button btnNo = (Button)dlog.findViewById(R.id.btnDlogRight);
		btnYes.setText(R.string.button_yes);
		btnNo.setText(R.string.button_no);
		
		btnYes.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dlog.dismiss();
				proceedToRefund();
				
			}
		});
		btnNo.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dlog.dismiss();
				if(myPref.getPreferences(MyPreferences.pref_enable_printing))
					showPrintDlg();
				else
					getActivity().finish();
			}
		});
		dlog.show();
	}

	
	private void showPaymentDlg() 
	{
		
		final Dialog dlog = new Dialog(activity,R.style.Theme_TransparentTest);
		dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dlog.setCancelable(false);
		dlog.setContentView(R.layout.dlog_btn_left_right_layout);
		
		TextView viewTitle = (TextView)dlog.findViewById(R.id.dlogTitle);
		TextView viewMsg = (TextView)dlog.findViewById(R.id.dlogMessage);
		viewTitle.setText(R.string.dlog_title_confirm);
		viewMsg.setText(R.string.take_payment_now);
		Button btnYes = (Button)dlog.findViewById(R.id.btnDlogLeft);
		Button btnNo = (Button)dlog.findViewById(R.id.btnDlogRight);
		btnYes.setText(R.string.button_yes);
		btnNo.setText(R.string.button_no);
		
		btnYes.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dlog.dismiss();
				Intent intent = new Intent(activity, RefundMenuActivity.class);
				intent.putExtra("typeOfProcedure",typeOfProcedure);
				intent.putExtra("salesinvoice", true);
				intent.putExtra("ord_subtotal", global.order.getSetData("ord_subtotal", true, empstr));
				intent.putExtra("ord_taxID", OrderTotalDetails_FR.taxID);
				intent.putExtra("amount",global.order.getSetData("ord_total", true, empstr));
				intent.putExtra("paid", "0.00");
				intent.putExtra("job_id", global.order.getSetData("ord_id", true, empstr));
				intent.putExtra("ord_type", Global.ord_type);
				
				
				if(myPref.isCustSelected())
				{
					intent.putExtra("cust_id", myPref.getCustID());
					intent.putExtra("custidkey", myPref.getCustIDKey());
				}
				
				startActivityForResult(intent, 0);
				
			}
		});
		btnNo.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dlog.dismiss();
				if(myPref.getPreferences(MyPreferences.pref_enable_printing))
					showPrintDlg();
				else
					getActivity().finish();
			}
		});
		dlog.show();
	}

	
	private void proceedToRemove(int pos) {
		String quant = global.orderProducts.get(pos).getSetData("ordprod_qty", true, empstr);
		String prodID = global.orderProducts.get(pos).getSetData("prod_id", true, empstr);

		if (myPref.getPreferences(MyPreferences.pref_allow_decimal_quantities)) 
		{
			double totalQty =(Double) Global.getFormatedNumber(true,global.qtyCounter.get(prodID));
			double qty = Double.parseDouble(quant);
			double sum = totalQty - qty;
			global.qtyCounter.put(prodID, Double.toString(sum));
		} 
		else 
		{
			int totalQty = (Integer) Global.getFormatedNumber(false,global.qtyCounter.get(prodID));
			int qty = Integer.parseInt(quant);
			int sum = totalQty - qty;

			global.qtyCounter.put(prodID, Integer.toString(sum));
		}
		
		
		if(myPref.getPreferences(MyPreferences.pref_show_removed_void_items_in_printout))
		{
			global.orderProducts.get(pos).getSetData("item_void", false, "1");
			String val = global.orderProducts.get(pos).getSetData("ordprod_name", true, null);
			
			global.orderProducts.get(pos).getSetData("ordprod_name", false, val+" [VOIDED]");
			global.orderProducts.get(pos).getSetData("overwrite_price", false, "0");
		}
		else
		{
			if(Global.addonSelectionMap!=null)
				Global.addonSelectionMap.remove(global.orderProducts.get(pos).getSetData("ordprod_id", true, null));
			if(Global.orderProductAddonsMap!=null)
				Global.orderProductAddonsMap.remove(global.orderProducts.get(pos).getSetData("ordprod_id", true, null));
			global.orderProducts.remove(pos);
		}
								
		myListView.invalidateViews();
		reCalculate();
	}
	
	
	
	public class ListViewAdapter extends BaseAdapter {

		private LayoutInflater myInflater;

		public ListViewAdapter(Context context) {

			myInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			if (global.orderProducts != null) {
				return global.orderProducts.size();
			}
			return 0;
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		
		@Override
		public int getItemViewType(int position) {
			String t = global.orderProducts.get(position).getSetData("item_void", true, empstr);
			if (t.equals("")||t.equals("0")) //divider
			{
				return 0;
			}
			return 1;
		}
		

		@Override
		public int getViewTypeCount() {
			return 2;
		}
		
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub

			ViewHolder holder;
			int type = getItemViewType(position);
			if (convertView == null) {
				holder = new ViewHolder();

				switch(type)
				{
					case 0:
						convertView = myInflater.inflate(R.layout.product_receipt_adapter, null);
						break;
					case 1:
						convertView = myInflater.inflate(R.layout.product_receiptvoid_adapter, null);
						break;
				}
				
				holder.itemQty = (TextView) convertView.findViewById(R.id.itemQty);
				holder.itemName = (TextView) convertView.findViewById(R.id.itemName);
				holder.itemAmount = (TextView) convertView.findViewById(R.id.itemAmount);
				holder.distQty = (TextView) convertView.findViewById(R.id.distQty);
				holder.distAmount = (TextView) convertView.findViewById(R.id.distAmount);
				holder.granTotal = (TextView) convertView.findViewById(R.id.granTotal);
				
				holder.addonButton = (Button)convertView.findViewById(R.id.addonButton);
				if(holder.addonButton!=null)
					holder.addonButton.setFocusable(false);
				
				

				setHolderValues(holder, position);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
				setHolderValues(holder, position);
			}

			return convertView;
		}

		public void setHolderValues(ViewHolder holder, int position) {

			final int pos = position;
			final String tempId = global.orderProducts.get(pos).getSetData("ordprod_id", true, null);
			
			if(!myPref.getPreferences(MyPreferences.pref_restaurant_mode)||(myPref.getPreferences(MyPreferences.pref_restaurant_mode)&&(Global.addonSelectionMap==null||(Global.addonSelectionMap!=null&&!Global.addonSelectionMap.containsKey(tempId)))))
			{
				if(holder.addonButton!=null)
					holder.addonButton.setVisibility(View.INVISIBLE);
			}
			else
			{
				if(holder.addonButton!=null)
				{
					holder.addonButton.setVisibility(View.VISIBLE);
					holder.addonButton.setOnClickListener(new View.OnClickListener() {
						
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							Intent intent = new Intent(activity, AddonsPickerFragActivity.class);
							String prodID = global.orderProducts.get(pos).getSetData("prod_id", true, null);
							global.addonSelectionType = Global.addonSelectionMap.get(tempId);
							
							intent.putExtra("addon_map_key", tempId);
							intent.putExtra("isEditAddon", true);						
							intent.putExtra("prod_id",prodID);
							
							
							ProductAddonsHandler prodAddonsHandler = new ProductAddonsHandler(getActivity());
							Global.productParentAddons  = prodAddonsHandler.getParentAddons(null, prodID);
							
							
							startActivityForResult(intent, 0);
							//activity.finish();
						}
					});
				}
			}
			
			holder.itemQty.setText(global.orderProducts.get(position).getSetData("ordprod_qty", true, empstr));
			holder.itemName.setText(global.orderProducts.get(position).getSetData("ordprod_name", true, empstr));
			
			String temp = Global.formatNumToLocale(Double.parseDouble(global.orderProducts.get(position).getSetData("overwrite_price", true, empstr)));
			holder.itemAmount.setText(Global.getCurrencyFormat(temp));
			
			
			holder.distQty.setText(global.orderProducts.get(position).getSetData("disAmount", true, empstr));
			temp = Global.formatNumToLocale(Double.parseDouble(global.orderProducts.get(position).getSetData("disTotal", true, empstr)));
			holder.distAmount.setText(Global.getCurrencyFormat(temp));

			// to-do calculate tax

			temp = Global.formatNumToLocale(Double.parseDouble(global.orderProducts.get(position).getSetData("itemTotal", true, empstr)));
			holder.granTotal.setText(Global.getCurrencyFormat(temp));

		}

		
		public class ViewHolder 
		{
			TextView itemQty;
			TextView itemName;
			TextView itemAmount;
			TextView distQty;
			TextView distAmount;
			TextView granTotal;
			
			Button addonButton;
		}
	}
	
	public void reCalculate()
	{
		callBack.recalculateTotal();
	}
	

	private void loadSaveTemplate()
	{
		if(myPref.isCustSelected())
		{
			final TemplateHandler handleTemplate = new TemplateHandler(activity);
			final Dialog dlog = new Dialog(activity,R.style.Theme_TransparentTest);
			dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dlog.setCancelable(false);
			dlog.setContentView(R.layout.dlog_btn_left_right_layout);
			
			TextView viewTitle = (TextView)dlog.findViewById(R.id.dlogTitle);
			TextView viewMsg = (TextView)dlog.findViewById(R.id.dlogMessage);
			viewTitle.setText(R.string.dlog_title_confirm);
			viewMsg.setText(R.string.dlog_msg_want_to_make_refund);
			Button btnSave = (Button)dlog.findViewById(R.id.btnDlogLeft);
			Button btnLoad = (Button)dlog.findViewById(R.id.btnDlogRight);
			btnSave.setText(R.string.button_save);
			btnLoad.setText(R.string.button_load);
			
			btnSave.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					handleTemplate.insert(myPref.getCustID());
					dlog.dismiss();
				}
			});
			btnLoad.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					// TODO Auto-generated method stub
					List<HashMap<String,String>> mapList = handleTemplate.getTemplate(myPref.getCustID());
					int size = mapList.size();
					global.orderProducts = new ArrayList<OrderProducts>();
					
					OrderProducts ordProd = new OrderProducts();
					Orders anOrder = new Orders();
					for(int i = 0 ; i<size; i++)
					{
						ordProd.getSetData("ordprod_id", false, mapList.get(i).get("ordprod_id"));
						ordProd.getSetData("prod_id", false, mapList.get(i).get("prod_id"));
						ordProd.getSetData("ordprod_name", false, mapList.get(i).get("prod_name"));
						ordProd.getSetData("overwrite_price", false, mapList.get(i).get("overwrite_price"));
						ordProd.getSetData("ordprod_qty", false, mapList.get(i).get("ordprod_qty"));
						ordProd.getSetData("itemTotal", false, mapList.get(i).get("itemTotal"));
						ordProd.getSetData("ord_id", false, mapList.get(i).get("ord_id"));
						
						anOrder.setValue(mapList.get(i).get("prod_price"));
						anOrder.setQty(mapList.get(i).get("ordprod_qty"));
						
						ordProd.getSetData("tax_position", false, "0");
						ordProd.getSetData("discount_position", false, "0");
						ordProd.getSetData("pricelevel_position", false, "0");
						ordProd.getSetData("uom_position", false, "0");
						
						global.orderProducts.add(ordProd);
						global.qtyCounter.put(mapList.get(i).get("prod_id"), mapList.get(i).get("ordprod_qty"));
						
						ordProd = new OrderProducts();
						anOrder = new Orders();
					}
					myListView.invalidateViews();
					reCalculate();
					dlog.dismiss();
				}
			});
			dlog.show();
		}
		else
			Toast.makeText(activity, getString(R.string.warning_no_customer_selected), Toast.LENGTH_LONG).show();
		
	}

	
	
	@Override
	public void onDrawerClosed() {
		// TODO Auto-generated method stub
		ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) myListView
		        .getLayoutParams();
		
		int dp = (int) (getResources().getDimension(R.dimen.add_orders_slider_semiclosed_size) / getResources().getDisplayMetrics().density);
		mlp.setMargins(mlp.leftMargin, 0, mlp.rightMargin, dp);
		float scale = getResources().getDisplayMetrics().density;
		int dpAsPixels = (int) (20*scale + 0.5f);
		myListView.setPadding(0, 0, 0, dpAsPixels);
		myListView.invalidateViews();
	}

	
	
	
	@Override
	public void onDrawerOpened() {
		// TODO Auto-generated method stub
		ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) myListView
		        .getLayoutParams();
		mlp.setMargins(mlp.leftMargin, 0, mlp.rightMargin,slidingDrawer.getHeight());
		myListView.invalidateViews();
	}
	
	
	
	
	public void fragOnKeyDown(int key_code)
	{
		if(key_code == 0)
		{
			if(scannerInDecodeMode)
			{
				try {
					mDecodeManager.cancelDecode();
					scannerInDecodeMode = false;
					if(!myPref.getPreferences(MyPreferences.pref_fast_scanning_mode))
						DoScan();
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else
				DoScan();
		}
	}

	
	
	//-----Honeywell scanner
		private void DoScan(){
			try {
				if(mDecodeManager!=null)
				{
					mDecodeManager.doDecode(SCANTIMEOUT);
					scannerInDecodeMode = true;
				}
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		private Handler ScanResultHandler = new Handler() {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case DecodeManager.MESSAGE_DECODER_COMPLETE:
					String strDecodeResult = "";
					DecodeResult decodeResult = (DecodeResult) msg.obj;

					strDecodeResult =decodeResult.barcodeData.trim();
					if(!strDecodeResult.isEmpty())
					{
						scanAddItem(strDecodeResult);
					}
					if(myPref.getPreferences(MyPreferences.pref_fast_scanning_mode))
					{
						try{
							  Thread.currentThread();
							  Thread.sleep(1000);//sleep for 1000 ms
							  DoScan();
							}
							catch(Exception ie){
							}
					}
					
					break;

				case DecodeManager.MESSAGE_DECODER_FAIL: {
					SoundManager.playSound(2, 1);
				}
				break;
				case DecodeManager.MESSAGE_DECODER_READY:
				{
					if(mDecodeManager!=null)
					{
						SymConfigActivityOpeartor operator = mDecodeManager.getSymConfigActivityOpeartor();
						operator.removeAllSymFromConfigActivity();
						SymbologyConfigCodeUPCA upca = new SymbologyConfigCodeUPCA();
						upca.enableSymbology(true);
						upca.enableCheckTransmit(true);
						upca.enableSendNumSys(true);
						
						SymbologyConfigs symconfig = new SymbologyConfigs();
						symconfig.addSymbologyConfig(upca);
						
						try {
							mDecodeManager.setSymbologyConfigs(symconfig);
						} catch (RemoteException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				break;
				default:
					super.handleMessage(msg);
					break;
				}
			}
		};
		
		
		
		private void scanAddItem(String upc)
		{
			ProductsHandler handler = new ProductsHandler(activity);
			SalesReceiptSplitActivity temp = (SalesReceiptSplitActivity)getActivity();
					String[] listData = handler.getUPCProducts(upc);
					if (temp !=null && listData[0] != null)
					{
						SoundManager.playSound(1, 1);
						if(myPref.getPreferences(MyPreferences.pref_group_receipt_by_sku))
						{
							int foundPosition = global.checkIfGroupBySKU(activity, listData[0], "1");
							if(foundPosition!=-1)			//product already exist in list
							{
								global.refreshParticularOrder(myPref,foundPosition);
								myAdapter.notifyDataSetChanged();
							}
							else
								temp.automaticAddOrder(listData);
						}
						else
							temp.automaticAddOrder(listData);
						
						reCalculate();
					}
					else
						SoundManager.playSound(2, 1);
		}
}