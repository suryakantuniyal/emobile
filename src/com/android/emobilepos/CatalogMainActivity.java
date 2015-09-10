package com.android.emobilepos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import com.actionbarsherlock.app.SherlockFragment;
import com.android.catalog.fragments.CatalogFragTwo;

import com.android.catalog.fragments.CatalogLandscapeTwo;
import com.android.database.CategoriesHandler;


import com.android.soundmanager.SoundManager;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.honeywell.decodemanager.DecodeManager;
import com.honeywell.decodemanager.SymbologyConfigs;
import com.honeywell.decodemanager.DecodeManager.SymConfigActivityOpeartor;
import com.honeywell.decodemanager.barcode.CommonDefine;
import com.honeywell.decodemanager.barcode.DecodeResult;
import com.honeywell.decodemanager.symbologyconfig.SymbologyConfigCodeUPCA;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;

import android.widget.TextView;

import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;


public class CatalogMainActivity extends SherlockFragment {
	private View myRoot;
	private Button category;
	private Spinner filterSpinner;
	private Dialog dialog;
	private boolean hasBeenInitialized = false;
	private CategoriesHandler handler;
	private String[] filterValues;	//"Name", "Description", "Type", "UPC"
	private CustomAdapter filterAdapter;

	private boolean isSubcategory = false;
	
	private List<String[]> categories,mainCategories = new ArrayList<String[]>();
	private List<String> catName,mainCatName = new ArrayList<String>();
	private List<String> catIDs,mainCatIDs= new ArrayList<String>();
	
	private ListViewAdapter myAdapter;
	private MyPreferences myPref;
	private Global global;
	private EditText search;
	
	
	//Honeywell Dolphin black
	private boolean scannerInDecodeMode = false;
	private DecodeManager mDecodeManager = null;
	private final int SCANTIMEOUT = 500000;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		myRoot = inflater.inflate(R.layout.catalog_fragment_container, null);

		category = (Button) myRoot.findViewById(R.id.categoryButton);
		filterSpinner = (Spinner) myRoot.findViewById(R.id.filterButton);
		//clearDataIcon = (ImageView) myRoot.findViewById(R.id.clearSearchField);
		//searchButton = (ImageView) myRoot.findViewById(R.id.searchButton);
		search = (EditText) myRoot.findViewById(R.id.catalogSearchField);
		handler = new CategoriesHandler(getActivity());
		myPref = new MyPreferences(getActivity());
		global = (Global) getActivity().getApplication();
		Resources resources = getActivity().getResources();
		filterValues = new String[]{resources.getString(R.string.catalog_name),resources.getString(R.string.catalog_description),
				resources.getString(R.string.catalog_type),resources.getString(R.string.catalog_upc)};
		
		

		
		if(Global.cat_id=="0")
			Global.cat_id = myPref.getPreferencesValue(MyPreferences.pref_default_category);

		if (!hasBeenInitialized) {
			mainCategories = handler.getCategories();
			int size = mainCategories.size();
			mainCatName.add("All");
			mainCatIDs.add("0");
			for (int i = 0; i < size; i++) {
				mainCatName.add(mainCategories.get(i)[0]);
				mainCatIDs.add(mainCategories.get(i)[1]);
			}
		}
		
		category.setOnClickListener(new View.OnClickListener() 
		{
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				categories = new ArrayList<String[]> (mainCategories);
				catName = new ArrayList<String>(mainCatName);
				catIDs = new ArrayList<String>(mainCatIDs);
				isSubcategory = false;
				setupCategoryView();
				dialog.show();
				
			}
		});
		

		filterAdapter = new CustomAdapter(getActivity(), android.R.layout.simple_spinner_item, filterValues);
		filterSpinner.setAdapter(filterAdapter);

		filterSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
				// TODO Auto-generated method stub
				global.searchType = position;
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}
		});
		
		search.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI|EditorInfo.IME_ACTION_SEARCH);
		search.setOnEditorActionListener(new TextView.OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				// TODO Auto-generated method stub
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					String text = v.getText().toString().trim();
					if (!text.isEmpty()) {
						InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(search.getWindowToken(), 0);
						int orientation = getResources().getConfiguration().orientation;
						if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
							CatalogLandscapeTwo frag = (CatalogLandscapeTwo) getActivity().getSupportFragmentManager().findFragmentByTag("Landscape");
							frag.performSearch(text);
						} else {
							CatalogFragTwo frag = (CatalogFragTwo) getActivity().getSupportFragmentManager().findFragmentByTag("Portrait");
							frag.performSearch(text);
						}
					}
					return true;
				}
				return false;
			}
		});
		
		
		search.addTextChangedListener(new TextWatcher() 
		{

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onTextChanged(CharSequence s, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub
				String test = s.toString().trim();
				if (test.isEmpty()) 
				{
					int orientation = getResources().getConfiguration().orientation;
					if (orientation == Configuration.ORIENTATION_LANDSCAPE) 
					{
						CatalogLandscapeTwo frag = (CatalogLandscapeTwo) getActivity().getSupportFragmentManager().findFragmentByTag("Landscape");
						if (frag != null && frag.adapter!=null) {
							if (frag.getCursor() != null)
								frag.getCursor().close();
							frag.initAllProducts();
							frag.gridView.setAdapter(frag.adapter);
						}

					} 
					else {
						CatalogFragTwo frag = (CatalogFragTwo) getActivity().getSupportFragmentManager().findFragmentByTag("Portrait");
						if (frag != null && frag.adapter!=null) {
							if (frag.getCursor() != null)
								frag.getCursor().close();
							frag.initAllProducts();
							frag.lView.setAdapter(frag.adapter);
						}

					}
				}
			}
		});
		
		search.setOnFocusChangeListener(viewFocusListener());

		updateFragment();
		hasBeenInitialized = true;
		
		if(myPref.getPreferences(MyPreferences.pref_restaurant_mode))
			category.setVisibility(View.GONE);
		
		return myRoot;
	}
	
	
	
	@Override
	public void onResume() {
		super.onResume();
		if (mDecodeManager == null) 
		{
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
	public void onDestroy() {
		super.onDestroy();

		//SoundManager.cleanup();
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
	
	
	
	
	
	private OnFocusChangeListener viewFocusListener()
	{
		OnFocusChangeListener t = new View.OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				if(!search.isFocused())
				{					
					String upc = search.getText().toString().trim();
					upc.replace("\n", "");
					
					if(!upc.isEmpty())
					{
						int orientation = getResources().getConfiguration().orientation;
	
						if (orientation == Configuration.ORIENTATION_PORTRAIT) 
						{
							CatalogFragTwo frag = (CatalogFragTwo) getActivity().getSupportFragmentManager().findFragmentByTag("Portrait");
							if(frag!=null)
							{
								frag.performSearch(upc);
								search.requestFocus();
							}
							
							
						}
						else
						{
							CatalogLandscapeTwo frag = (CatalogLandscapeTwo) getActivity().getSupportFragmentManager().findFragmentByTag("Landscape");
							frag.performSearch(upc);
							
							SalesReceiptSplitActivity temp;
							try
							{
								temp = (SalesReceiptSplitActivity)getActivity();
								if(temp!=null)
								{
									EditText test = temp.invisibleSearchView();
									if(test!=null)
										test.requestFocus();
									else
										search.requestFocus();
								}
							}
							catch(Exception e)
							{
								search.requestFocus();
							}
						}
					}
				}
			}
		};
		
		return t;
	}

	
	
	private void setupCategoryView()
	{
		if(dialog!=null&&dialog.isShowing())
		{
			dialog.dismiss();
			myAdapter.notifyDataSetChanged();
		}
		
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		final ListView list = new ListView(getActivity());
		myAdapter = new ListViewAdapter(getActivity());
		list.setCacheColorHint(Color.TRANSPARENT);
		list.setAdapter(myAdapter);
		builder.setView(list);

		
		
		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				if (position == 0&&!isSubcategory) {
					Global.cat_id = "0";
				} else {

					int index = position;
					if(!isSubcategory)
						index -=1;
					Global.cat_id = categories.get(index)[1];
					if (myPref.getPreferences(MyPreferences.pref_enable_multi_category) && !categories.get(index)[2].equals("0")) // it has sub-category
					{
						global.hasSubcategory = true;

					} else
						global.hasSubcategory = false;
					global.isSubcategory = false;
				}
				dialog.dismiss();
				updateFragment();
			}
		});
		
		dialog = builder.create();

		
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
					SoundManager.playSound(1, 1);
					int orientation = getResources().getConfiguration().orientation;
					if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
						CatalogLandscapeTwo frag = (CatalogLandscapeTwo) getActivity().getSupportFragmentManager().findFragmentByTag("Landscape");
						frag.performSearch(strDecodeResult);
					} else {
						CatalogFragTwo frag = (CatalogFragTwo) getActivity().getSupportFragmentManager().findFragmentByTag("Portrait");
						frag.performSearch(strDecodeResult);
					}
				}
				try{
					  Thread.currentThread();
					  Thread.sleep(1000);//sleep for 1000 ms
					  DoScan();
					}
					catch(Exception ie){
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
	
	
	

	public void updateFragment() 
	{
		
		int orientation = getResources().getConfiguration().orientation;
		FragmentManager fragManager = getFragmentManager();
		
		
		
		FragmentTransaction trans = fragManager.beginTransaction();
		if (orientation == Configuration.ORIENTATION_LANDSCAPE) 
		{
			CatalogLandscapeTwo landscapeFrag = (CatalogLandscapeTwo) fragManager.findFragmentByTag("Landscape");
			if(landscapeFrag!=null)
			{
				landscapeFrag.initAllProducts();
			}
			else
			{
				trans.replace(R.id.fragment_catalog_container, new CatalogLandscapeTwo(), "Landscape");
				trans.commitAllowingStateLoss();
				getActivity().setResult(2);
				System.gc();
			}
			
			
		} 
		else 
		{
			CatalogFragTwo portraitFrag = (CatalogFragTwo) fragManager.findFragmentByTag("Portrait");
			if(portraitFrag!=null)
			{
				portraitFrag.initAllProducts();
			}
			else
			{
				trans.replace(R.id.fragment_catalog_container, new CatalogFragTwo(), "Portrait");
				trans.commitAllowingStateLoss();
				getActivity().setResult(2);
				System.gc();
			}
		}
		
		
	}
	
	

	public class ListViewAdapter extends BaseAdapter {

		private LayoutInflater myInflater;
		

		public ListViewAdapter(Context context) {
			
			myInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return catName.size();
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
		public View getView(final int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub

			ViewHolder holder;
			int type = getItemViewType(position);
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = myInflater.inflate(R.layout.categories_layout_adapter, null);
				holder.categoryName = (TextView) convertView.findViewById(R.id.categoryName);
				holder.icon = (ImageView) convertView.findViewById(R.id.subcategoryIcon);

				holder.categoryName.setText(catName.get(position));
				setHolderValues(holder, position, type);
				
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();

				holder.categoryName.setText(catName.get(position));

				setHolderValues(holder, position, type);
			}
			return convertView;
		}

		@Override
		public int getItemViewType(int position) {
			if ((position>0&&!isSubcategory)||(position>=0&&isSubcategory)) 
			{
				int index = position-1;
				if(position>=0&&isSubcategory)
					index = position;
					
					
				if (myPref.getPreferences(MyPreferences.pref_enable_multi_category)) // check for available sub-categories
				{
					if (!categories.get(index)[2].equals("0")) // there are sub-categories available
					{
						return 0;
					} else
						return 1;
				}
			}
			
			return 1;

		}

		@Override
		public int getViewTypeCount() {
			return 2;
		}

		public void setHolderValues(ViewHolder holder, final int position, int type) {
			switch (type) {
			case 0: {
				holder.icon.setVisibility(View.VISIBLE);
				holder.icon.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						int index = position - 1;
						if(isSubcategory)
							index = position;
						showSubcategories(categories.get(index)[1]);
					}
				});
				break;
			}
			case 1: {
				holder.icon.setVisibility(View.INVISIBLE);
				break;
			}
			}
		}

		public class ViewHolder {
			TextView categoryName;
			ImageView icon;
		}
	}
	

	public void showSubcategories(String subCategoryName) 
	{

		categories = handler.getSubcategories(subCategoryName);
		int size = categories.size();
		catName.clear();
		catIDs.clear();
		for (int i = 0; i < size; i++) 
		{
			catName.add(categories.get(i)[0]);
			catIDs.add(categories.get(i)[1]);
		}
		
		isSubcategory = true;
		setupCategoryView();
		dialog.show();
	}

	public class CustomAdapter extends ArrayAdapter<String> {
		private Activity context;
		String[] leftData = null;

		public CustomAdapter(Activity activity, int resource, String[] left) {
			super(activity, resource, left);
			this.context = activity;
			this.leftData = left;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = super.getView(position, convertView, parent);

			// we know that simple_spinner_item has android.R.id.text1 TextView:

			TextView text = (TextView) view.findViewById(android.R.id.text1);
			text.setTextColor(Color.WHITE);// choose your color
			text.setTextSize(11);
			return view;
		}

		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent) {
			View row = convertView;
			if (row == null) {
				LayoutInflater inflater = context.getLayoutInflater();
				row = inflater.inflate(R.layout.spinner_layout, parent, false);
			}
			ImageView checked = (ImageView) row.findViewById(R.id.checkMark);
			TextView taxName = (TextView) row.findViewById(R.id.taxName);
			checked.setVisibility(View.INVISIBLE);
			taxName.setText(leftData[position]);

			return row;
		}
	}

	
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == 2)// add item to listview
		{
			getActivity().setResult(2);
		}
		if (resultCode == -2)
			getActivity().setResult(-2);
	}
}
