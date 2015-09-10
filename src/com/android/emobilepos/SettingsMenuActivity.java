package com.android.emobilepos;

import java.io.File;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import main.EMSDeviceManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Toast;

import android.widget.EditText;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragment;

import com.android.database.CategoriesHandler;
import com.android.database.PrintersHandler;
import com.android.database.ShiftPeriodsDBHandler;
import com.android.menuadapters.SettingsMenuAdapter;

import com.android.support.Global;
import com.android.support.MyPreferences;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Tracker;



public class SettingsMenuActivity extends SherlockFragment {
	
	
	private SettingsMenuAdapter myAdapter;
	private ListView myListview;

	private Dialog promptDialog;
	private AlertDialog.Builder dialogBuilder;
	private List<String> macAddressList = new ArrayList<String>();
	private Activity activity;
	private Global global;

	private String[] mainMenuList;
	private  Map<String, Boolean> mainMenuMap;
	private boolean validPassword = true;

	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.settings_layout, container, false);
		activity = getActivity();
		myListview = (ListView) view.findViewById(R.id.settingsListView);
		
		global = (Global)activity.getApplication();
		
		return view;

	}
	
	

	/* if update information is needed on layout */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);
		Activity activity = getActivity();

		if (activity != null) {

			myAdapter = new SettingsMenuAdapter(activity);

			if (myListview != null) {

				myListview.setAdapter(myAdapter);

				myListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
						// TODO Auto-generated method stub
						int settingID =(Integer) myAdapter.getItem(position);
						int type = myAdapter.getItemKeyValue(settingID);
						if (type == 2) 
						{
							performSetting(settingID);
						}
					}
				});
			}
		}
	}
	

	
	private void performSetting(int key) {

//		switch (key) {
//		case Global.change_pass:// change password
//		{
//			askForAdminPassDlg(0);
//			break;
//		}
//		case Global.print_preferences: // print_preferences
//		{
//			askForAdminPassDlg(3);
//			break;
//		}
//		case Global.configure_home:// configure home menu
//		{
//			askForAdminPassDlg(1);
//			break;
//		}
//		case Global.open_shifts:// open shift
//		{
//			askForAdminPassDlg(2);
//			break;
//		}
//		case Global.connect_to_bluetooth_device:// Bluetooth Peripheral
//		{
//			connectBTDevice();
//			// connectBluetoothDevices();
//			break;
//		}
//		case Global.clear_cache:// clear image cache
//		{
//			clearApplicationData();
//			break;
//		}
//
//		case Global.default_cat:// default category
//		{
//			configureDefaultCategory();
//			break;
//		}
//		case Global.config_genius:// genius peripheral
//		{
//			configureGeniusPeripheral();
//			break;
//		}
//		case Global.configure_hardpayments: {
//			askForAdminPassDlg(4);
//			break;
//		}
//		case Global.star_info: {
//			promptStarPrinter();
//			break;
//		}
//		case Global.choose_audio_card_reader: {
//			configureAudioCardReader();
//			break;
//		}
//		case Global.re_detect:
//			new autoConnectPrinter().execute();
//			break;
//		case Global.forget_peripheral:
//			MyPreferences myPref = new MyPreferences(activity);
//			myPref.forgetPeripherals();
//			Toast.makeText(activity, "Peripherals have been erased", Toast.LENGTH_LONG).show();
//			break;
//		default:
//			break;
//		}

	}

	
	private void clearApplicationData()
	{
		MyPreferences myPref = new MyPreferences(activity);
		
		File cacheDir = new File(myPref.getCacheDir());
		File[] files = cacheDir.listFiles();
		if (files != null) {
		    for (File file : files)
		       file.delete();
		}
	}
	
	
	private void configureGeniusPeripheral()
	{
		final MyPreferences myPref = new MyPreferences(activity);
		final EditText input = new EditText(activity);
		input.setText(myPref.getGeniusIP());
		input.setInputType(InputType.TYPE_CLASS_TEXT);
		dialogBuilder = new AlertDialog.Builder(activity);
		input.setSelection(input.getText().length());
		dialogBuilder.setView(input);
		
		promptDialog = dialogBuilder.setTitle("Enter Genius IP Address")
				.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface thisDialog, int which) {
						// TODO Auto-generated method stub
						final String firstValue = input.getText().toString();
						if(firstValue.length()>0)
						{
							myPref.setGeniusIP(firstValue);
							thisDialog.dismiss();
						}
						else
							promptDialog.setTitle("Try again...");
						
					}
				}).setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface thisDialog, int which) {
						// TODO Auto-generated method stub
						thisDialog.dismiss();
						
					}
				}).create();
		
		promptDialog.show();
	}
	
	
	private void askForAdminPassDlg(final int type)		//type 0 = Change Password, type 1 = Configure Home Menu
	{
		final MyPreferences myPref = new MyPreferences(activity);
		final EditText input = new EditText(activity);
		input.setTransformationMethod(PasswordTransformationMethod.getInstance());
		dialogBuilder = new AlertDialog.Builder(activity);
		dialogBuilder.setView(input);
		
		if(!validPassword)
			dialogBuilder.setTitle(activity.getResources().getString(R.string.dlog_title_invalid_admin_password));
		else
			dialogBuilder.setTitle(activity.getResources().getString(R.string.dlog_title_enter_admin_password));
		
		dialogBuilder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface thisDialog, int which) {
						// TODO Auto-generated method stub
						String value = input.getText().toString();
						
						if (value.equals(myPref.getPOSAdminPass())) // validate admin password
						{
							validPassword = true;
							thisDialog.dismiss();
							switch(type)
							{
							case 0:			//Change Password
								changePassword();
								break;
							case 1:			//Configure Home Menu
								//configureHomeMenu();
								break;
							case 2:			//Open Shift
								/*Intent intent = new Intent(activity,ShiftSettingMainActivity.class);
								startActivity(intent);*/
								if(myPref.getShiftIsOpen())
								{
									Intent intent = new Intent(activity,OpenShiftMainActivity.class);
									startActivityForResult(intent,0);
								}
								else
									promptCloseShift(true,0);
								break;
							case 3:
								configurePrintPref();
								break;
							case 4:
								configurePaymentMethod();
								break;
							}
						}
						else
						{
							thisDialog.dismiss();
							validPassword = false;
							askForAdminPassDlg(type);
						}
						
						
					}
				}).setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface thisDialog, int which) {
						// TODO Auto-generated method stub
						thisDialog.dismiss();
						
					}
				});
		
		promptDialog = dialogBuilder.create();
		promptDialog.show();
	}
	
	
	
	private void changePassword()
	{
		final MyPreferences myPref = new MyPreferences(activity);
		final EditText input = new EditText(activity);
		dialogBuilder = new AlertDialog.Builder(activity);
		dialogBuilder.setView(input);
		promptDialog = dialogBuilder.setTitle("Enter New Password").setMessage("Must be at least 5 characters long")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface thisDialog, int which) {
						// TODO Auto-generated method stub
						final String firstValue = input.getText().toString();
						if(firstValue.length()>0)
						{
							thisDialog.dismiss();
							
							final EditText input = new EditText(activity);
							dialogBuilder = new AlertDialog.Builder(activity);
							dialogBuilder.setView(input);
							promptDialog = dialogBuilder.setTitle("Re-enter Password")
									.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {

										@Override
										public void onClick(DialogInterface thisDialog, int which) {
											// TODO Auto-generated method stub
											String secondValue = input.getText().toString();
											if(firstValue.equals(secondValue))
											{
												myPref.setApplicationPassword(secondValue);
											}
											thisDialog.dismiss();
										}
									}).setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {

										@Override
										public void onClick(DialogInterface thisDialog, int which) {
											// TODO Auto-generated method stub
											thisDialog.dismiss();
											
										}
									}).create();
							
							promptDialog.show();
						}
						thisDialog.dismiss();
					}
				}).setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface thisDialog, int which) {
						// TODO Auto-generated method stub
						thisDialog.dismiss();
						
					}
				}).create();
		
		promptDialog.show();
	}
	
	
	
//	private void configureHomeMenu()
//	{	
//		final MyPreferences myPref = new MyPreferences(activity);
//		final ListView listHomeMenu = new ListView(activity);
//		ArrayAdapter<String>menuAdapter;
//		dialogBuilder = new AlertDialog.Builder(activity);
//		mainMenuList = global.getSalesMainMenuList();
//		mainMenuMap = global.getSalesMainMenuMap();
//		menuAdapter = new ArrayAdapter<String>(activity,android.R.layout.simple_list_item_multiple_choice,mainMenuList);
//		listHomeMenu.setCacheColorHint(Color.TRANSPARENT);
//		listHomeMenu.setAdapter(menuAdapter);
//		listHomeMenu.setItemsCanFocus(false);
//		listHomeMenu.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
//		
//		int size = mainMenuList.length;
//		boolean [] settingSaved = myPref.getMainMenuSettings();
//		for(int i = 0 ; i < size ; i++)
//		{
//			listHomeMenu.setItemChecked(i, settingSaved[i]);
//		}
//		
//		listHomeMenu.setOnItemClickListener(new OnItemClickListener() {
//
//			@Override
//			public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
//				// TODO Auto-generated method stub
//				if(listHomeMenu.isItemChecked(pos))
//				{
//					mainMenuMap.put(mainMenuList[pos], true);
//					myPref.updateMainMenuSettings(mainMenuList[pos], true);
//				}
//				else
//				{
//					mainMenuMap.put(mainMenuList[pos], false);
//					myPref.updateMainMenuSettings(mainMenuList[pos], false);
//				}
//					
//			}
//		});
//		
//		dialogBuilder.setView(listHomeMenu);
//		promptDialog = dialogBuilder.setCancelable(false).setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				// TODO Auto-generated method stub
//				dialog.dismiss();
//			}
//		}).create();
//		promptDialog.show();
//	}
	
	
	
	private void configureAudioCardReader()
	{
		final MyPreferences myPref = new MyPreferences(activity);
		final ListView listHomeMenu = new ListView(activity);
		ArrayAdapter<String>menuAdapter;
		dialogBuilder = new AlertDialog.Builder(activity);
		mainMenuList = new String[]{"None",getString(R.string.unimag_card_reader),getString(R.string.magtek_udynamo_card_reader)};
		
		menuAdapter = new ArrayAdapter<String>(activity,android.R.layout.simple_list_item_multiple_choice,mainMenuList);
		listHomeMenu.setCacheColorHint(Color.TRANSPARENT);
		listHomeMenu.setAdapter(menuAdapter);
		listHomeMenu.setItemsCanFocus(false);
		listHomeMenu.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		
		int size = mainMenuList.length;
		boolean _isUnimag = myPref.getSettings(R.string.unimag_card_reader);
		boolean _isMagtek = myPref.getSettings(R.string.magtek_udynamo_card_reader);
		
		boolean [] settingSaved;
		if(_isUnimag||_isMagtek)
			settingSaved = new boolean []{false,myPref.getSettings(R.string.unimag_card_reader),myPref.getSettings(R.string.magtek_udynamo_card_reader)};
		else
			settingSaved = new boolean []{true,myPref.getSettings(R.string.unimag_card_reader),myPref.getSettings(R.string.magtek_udynamo_card_reader)};
		
		for(int i = 0 ; i < size ; i++)
		{
			
			listHomeMenu.setItemChecked(i, settingSaved[i]);
		}
		
		listHomeMenu.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
				// TODO Auto-generated method stub
					switch(pos)
					{
					case 0:
						myPref.setSettings(R.string.unimag_card_reader, false);
						myPref.setSettings(R.string.magtek_udynamo_card_reader, false);
						break;
						case 1:
							myPref.setSettings(R.string.unimag_card_reader, true);
							myPref.setSettings(R.string.magtek_udynamo_card_reader, false);
							break;
						case 2:
							myPref.setSettings(R.string.unimag_card_reader, false);
							myPref.setSettings(R.string.magtek_udynamo_card_reader, true);
							break;
					}
					
			}
		});
		
		dialogBuilder.setView(listHomeMenu);
		promptDialog = dialogBuilder.setCancelable(false).setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		}).create();
		promptDialog.show();
	}
	
	
	private void configurePrintPref()
	{	
		final MyPreferences myPref = new MyPreferences(activity);
		final ListView listHomeMenu = new ListView(activity);
		ArrayAdapter<String>menuAdapter;
		dialogBuilder = new AlertDialog.Builder(activity);
		
		global.initPrintPrefMenu(activity);
		final List<String[]> menuList = global.getPrintPrefMenu();
		
		final SparseBooleanArray prefMenuMap = global.getPrintPrefMap();
		int size = menuList.size();
		mainMenuList = new String[size];
		for(int i = 0 ; i < size; i++)
			mainMenuList[i] = menuList.get(i)[0];
		
		menuAdapter = new ArrayAdapter<String>(activity,android.R.layout.simple_list_item_multiple_choice,mainMenuList);
		listHomeMenu.setCacheColorHint(Color.TRANSPARENT);
		listHomeMenu.setAdapter(menuAdapter);
		listHomeMenu.setItemsCanFocus(false);
		listHomeMenu.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		
		boolean [] settingSaved = myPref.getPrintingPref();
		for(int i = 0 ; i < size ; i++)
		{
			listHomeMenu.setItemChecked(i, settingSaved[i]);
		}
		
		listHomeMenu.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
				// TODO Auto-generated method stub
				if(listHomeMenu.isItemChecked(pos))
				{
					prefMenuMap.put(Integer.parseInt(menuList.get(pos)[1]), true);
					myPref.updateMainMenuSettings(menuList.get(pos)[1], true);
				}
				else
				{
					prefMenuMap.put(Integer.parseInt(menuList.get(pos)[1]), false);
					myPref.updateMainMenuSettings(menuList.get(pos)[1], false);
				}
					
			}
		});
		
		dialogBuilder.setView(listHomeMenu);
		promptDialog = dialogBuilder.setCancelable(false).setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				//MyPreferences myPref = new MyPreferences(activity);
				dialog.dismiss();
			}
		}).create();
		promptDialog.show();
	}
	
	
	private void promptCloseShift(final boolean askAmount,final double amount)
	{
		dialogBuilder = new AlertDialog.Builder(activity);
		final MyPreferences myPref = new MyPreferences(activity);
		final EditText input = new EditText(activity);
		if(askAmount)
		{
			input.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
			dialogBuilder.setView(input);
			dialogBuilder.setTitle("Enter Cash Close Amount");
		}
		else
		{
			dialogBuilder.setTitle("Are You Sure?");
			dialogBuilder.setMessage("Close Amount is "+Global.formatDoubleToCurrency(amount));
		}
		
		
		
		dialogBuilder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface thisDialog, int which) {
						// TODO Auto-generated method stub
						if(askAmount)
						{
							thisDialog.dismiss();
							promptCloseShift(false,Global.formatNumFromLocale(input.getText().toString()));
						}
						else
						{
							
							ShiftPeriodsDBHandler handler = new ShiftPeriodsDBHandler(activity);
							handler.updateShift(myPref.getShiftID(), "entered_close_amount", Double.toString(amount));
							handler.updateShift(myPref.getShiftID(), "endTime", Global.getCurrentDate());
							
							myPref.setShiftIsOpen(true);
							thisDialog.dismiss();
						}
						
						
					}
				}).setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface thisDialog, int which) {
						// TODO Auto-generated method stub
						thisDialog.dismiss();
						
					}
				});
		
		promptDialog = dialogBuilder.create();
		promptDialog.show();
	}
	
	
	private void configurePaymentMethod()
	{
		final MyPreferences myPref = new MyPreferences(activity);
		final ListView listHomeMenu = new ListView(activity);
		ArrayAdapter<String>menuAdapter;
		dialogBuilder = new AlertDialog.Builder(activity);
		//mainMenuList = global.getSalesMainMenuList();
		//mainMenuMap = global.getSalesMainMenuMap();
		Map<String,Boolean> tempMap = new HashMap<String,Boolean>();
		final int [] keyArray = new int[]{R.string.admin_paymethod_tupyx,R.string.admin_paymethod_genius};
		tempMap.put(Integer.toString(keyArray[0]), myPref.getSettings(keyArray[0]));
		tempMap.put(Integer.toString(keyArray[1]), myPref.getSettings(keyArray[1]));
		mainMenuMap = tempMap;
		
		
		mainMenuList = new String[]{activity.getResources().getString(keyArray[0]),activity.getResources().getString(keyArray[1])};
		menuAdapter = new ArrayAdapter<String>(activity,android.R.layout.simple_list_item_multiple_choice,mainMenuList);
		listHomeMenu.setCacheColorHint(Color.TRANSPARENT);
		listHomeMenu.setAdapter(menuAdapter);
		listHomeMenu.setItemsCanFocus(false);
		listHomeMenu.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		
		int size = mainMenuList.length;
		
		for(int i = 0 ; i < size ; i++)
		{
			listHomeMenu.setItemChecked(i, mainMenuMap.get(Integer.toString(keyArray[i])));
		}
		
		listHomeMenu.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
				// TODO Auto-generated method stub
				if(listHomeMenu.isItemChecked(pos))
				{
					mainMenuMap.put(Integer.toString(keyArray[pos]), true);
					
					myPref.setSettings(keyArray[pos], true);
				}
				else
				{
					mainMenuMap.put(Integer.toString(keyArray[pos]), false);
					myPref.setSettings(keyArray[pos], false);
				}
					
			}
		});
		
		dialogBuilder.setView(listHomeMenu);
		promptDialog = dialogBuilder.setCancelable(false).setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		}).create();
		promptDialog.show();
	}
	
	
//	private void configureDefaultCategory()
//	{
//		
//		final MyPreferences myPref = new MyPreferences(activity);
//		
//		final ListView categoryListView = new ListView(activity);
//		ArrayAdapter<String> listViewAdapter;
//		dialogBuilder = new AlertDialog.Builder(activity);
//		CategoriesHandler handler = new CategoriesHandler(activity);
//		final List<String[]> categories = handler.getCategories();
//		List<String> catName = new ArrayList<String>();
//		//categories = handler.getCategories();
//		int size = categories.size();
//		.String savedCategoryID = myPref.getDefaultCategorySetting();
//		int indexOfSavedCategory = 0;
//		catName.add("All");
//		for (int i = 0; i < size; i++) {
//			catName.add(categories.get(i)[0]);
//			if(categories.get(i)[1].equals(savedCategoryID))
//				indexOfSavedCategory = i+1;
//		}
//		
//		listViewAdapter = new ArrayAdapter<String>(activity,android.R.layout.simple_list_item_single_choice,catName);
//		
//		categoryListView.setCacheColorHint(Color.TRANSPARENT);
//		categoryListView.setItemsCanFocus(false);
//		categoryListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
//		categoryListView.setAdapter(listViewAdapter);
//		
//		categoryListView.setItemChecked(indexOfSavedCategory, true);
//		
//		categoryListView.setOnItemClickListener(new OnItemClickListener() {
//
//			@Override
//			public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
//				// TODO Auto-generated method stub
//				if(categoryListView.isItemChecked(pos))
//				{
//					if(pos==0)
//						myPref.setDefaultCategorySetting("0");
//					else
//						myPref.setDefaultCategorySetting(categories.get(pos - 1)[1]);
//				}
//					
//			}
//		});
//		
//		
//		dialogBuilder.setView(categoryListView);
//		promptDialog = dialogBuilder.setCancelable(false).setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
//			
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				// TODO Auto-generated method stub
//				dialog.dismiss();
//			}
//		}).create();
//		
//		promptDialog.show();
//	}
	
	
	
	
	public void promptStarPrinter()
	{
			final MyPreferences myPref = new MyPreferences(activity);
			final EditText ipAddress = new EditText(activity);
			final EditText portNumber = new EditText(activity);
			
			
			
			this.ipAddressTextPref(ipAddress);
			
			
			
			
			//ipAddress.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL|InputType.TYPE_CLASS_NUMBER);
			
			ipAddress.setHint(R.string.dlog_star_ip);
			portNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
			portNumber.setHint(R.string.dlog_star_port);
			
			ipAddress.setText(myPref.getStarIPAddress());
			portNumber.setText(myPref.getStarPort());
			LinearLayout ll=new LinearLayout(activity);
			ll.setOrientation(LinearLayout.VERTICAL);
		    ll.addView(ipAddress);
		    ll.addView(portNumber);
		    
			
			
			dialogBuilder = new AlertDialog.Builder(activity);
			
			
			
			dialogBuilder.setView(ll);
			
			dialogBuilder.setTitle(R.string.dlog_star_congifure);
			
			
			dialogBuilder.setCancelable(true);
			dialogBuilder.setPositiveButton(R.string.button_connect, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					
					myPref.setStarIPAddress(ipAddress.getText().toString());
					myPref.setStarPort(portNumber.getText().toString());
					myPref.printerMACAddress(false, "TCP:"+ipAddress.getText().toString());
					
					EMSDeviceManager edm = new EMSDeviceManager();
					Global.mainPrinterManager = edm.getManager();
					Global.mainPrinterManager.loadDrivers(activity,Global.STAR,true);
					
							
				}
			}).create();
			promptDialog = dialogBuilder.create();
			
			promptDialog.show();
	}
	
	
	
	
	
	private void connectBTDevice()
	{
		ListView listViewPairedDevices = new ListView(activity);
		ArrayAdapter<String> bondedAdapter;
		dialogBuilder = new AlertDialog.Builder(activity);
		
		List<String> pairedDevicesList = getListPairedDevices();
		final String[] val = pairedDevicesList.toArray(new String[pairedDevicesList.size()]);
		bondedAdapter = new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, val);
		listViewPairedDevices.setAdapter(bondedAdapter);

		listViewPairedDevices.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int pos, long arg3) {
				// TODO Auto-generated method stub
				promptDialog.dismiss();
				
				dialogBuilder = new AlertDialog.Builder(activity);
				dialogBuilder.setTitle(R.string.dlog_title_connect_to_device);
				dialogBuilder.setMessage(val[pos]);
				dialogBuilder.setNegativeButton(R.string.button_no, null);
				dialogBuilder.setPositiveButton(R.string.button_yes, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						MyPreferences myPref = new MyPreferences(activity);
						if(val[pos].toUpperCase(Locale.getDefault()).contains("MAGTEK")) //magtek swiper
						{
							myPref.swiperType(false, Global.MAGTEK);
							myPref.swiperMACAddress(false, macAddressList.get(pos));
							
							EMSDeviceManager edm = new EMSDeviceManager();
							Global.btSwiper = edm.getManager();
							Global.btSwiper.loadDrivers(activity, Global.MAGTEK, false);
							
						}
						else if(val[pos].toUpperCase(Locale.getDefault()).contains("STAR"))//star micronics
						{
							myPref.printerType(false, Global.STAR);
							myPref.printerMACAddress(false, "BT:"+macAddressList.get(pos));
							
							EMSDeviceManager edm = new EMSDeviceManager();
							Global.mainPrinterManager = edm.getManager();
							Global.mainPrinterManager.loadDrivers(activity,Global.STAR,false);
							
						}
						else if(val[pos].toUpperCase(Locale.getDefault()).contains("P25"))//bamboo
						{
							myPref.printerType(false, Global.BAMBOO);
							myPref.printerMACAddress(false, macAddressList.get(pos));
							
							EMSDeviceManager edm = new EMSDeviceManager();
							Global.mainPrinterManager = edm.getManager();
							Global.mainPrinterManager.loadDrivers(activity,Global.BAMBOO,false);
						}
						else if(val[pos].toUpperCase(Locale.getDefault()).contains("EM220"))	//Zebra
						{
							myPref.printerType(false, Global.ZEBRA);
							myPref.printerMACAddress(false, macAddressList.get(pos));
							
							EMSDeviceManager edm = new EMSDeviceManager();
							Global.mainPrinterManager = edm.getManager();
							Global.mainPrinterManager.loadDrivers(activity,Global.ZEBRA,false);
						}
						else if(val[pos].toUpperCase(Locale.getDefault()).contains("MP"))	//Oneil
						{
							myPref.printerType(false, Global.ONEIL);
							myPref.printerMACAddress(false, macAddressList.get(pos));
							
							EMSDeviceManager edm = new EMSDeviceManager();
							Global.mainPrinterManager = edm.getManager();
							Global.mainPrinterManager.loadDrivers(activity,Global.ONEIL,false);
						}
						else
						{
							Toast.makeText(activity, R.string.err_invalid_device, Toast.LENGTH_LONG).show();
						}
					}
				});
				
				promptDialog = dialogBuilder.create();
				promptDialog.show();
				

				return false;
			}
		});

		dialogBuilder.setView(listViewPairedDevices);
		dialogBuilder.setTitle(R.string.dlog_title_select_device_to_connect);
		dialogBuilder.setNegativeButton(R.string.button_cancel, null);
		promptDialog = dialogBuilder.create();
		promptDialog.show();
	}
	
	
	/*private void connectBluetoothDevices() {
		ListView listViewPairedDevices = new ListView(activity);
		ArrayAdapter<String> bondedAdapter;
		dialogBuilder = new AlertDialog.Builder(activity);
		List<String> pairedDevicesList = getListPairedDevices();

		final String[] val = pairedDevicesList.toArray(new String[pairedDevicesList.size()]);
		bondedAdapter = new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, val);

		listViewPairedDevices.setAdapter(bondedAdapter);

		listViewPairedDevices.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int position, long arg3) {
				// TODO Auto-generated method stub
				promptDialog.dismiss();
				dialogBuilder = new AlertDialog.Builder(activity);
				promptDialog = dialogBuilder.setTitle("Connect to Device?").setPositiveButton(R.string.button_yes, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub

						MyPreferences myPref = new MyPreferences(activity);
						if(macAddressList!=null&&macAddressList.size()>0)
						{
							if(val[position].contains("MAGTEK"))
							{
								myPref.setIsMagtekReader(true);
								myPref.setBluebambooMACAddress(macAddressList.get(position));
							}
							else
							{
								myPref.setIsMagtekReader(false);
								if(val[position].contains("Star Micronic"))
									myPref.setBluebambooMACAddress("BT:"+macAddressList.get(position));
								else
									myPref.setBluebambooMACAddress(macAddressList.get(position));
								//EMSDeviceManager.getManager().loadDrivers(activity,val[position],false);
								
								EMSDeviceManager edm = new EMSDeviceManager();
								Global.mainPrinterManager = edm.getManager();
								Global.mainPrinterManager.loadDrivers(activity,val[position],false);
							}
							
						}
						promptDialog.dismiss();

					}
				}).setNegativeButton(R.string.button_no, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub

						promptDialog.dismiss();
					}
				}).create();

				promptDialog.show();

				return false;
			}
		});

		dialogBuilder.setView(listViewPairedDevices);
		promptDialog = dialogBuilder.setTitle("Long Press an item to connect").setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		}).create();

		promptDialog.show();

	}*/

	
	
	
	private void ipAddressTextPref(EditText et)
	{
		et.setRawInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
		//et.setInputType(InputType.TYPE_CLASS_PHONE);
	    et.setFilters(new InputFilter[] { new InputFilter() {
	        @Override
	        public CharSequence filter(CharSequence source, int start, int end, android.text.Spanned dest, int dstart, int dend) {
	            if (end > start) {
	                String destTxt = dest.toString();
	                String resultingTxt = destTxt.substring(0, dstart) + source.subSequence(start, end) + destTxt.substring(dend);
	                if (!resultingTxt.matches("^\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3})?)?)?)?)?)?")) {
	                    return "";
	                } else {
	                    String[] splits = resultingTxt.split("\\.");
	                    for (int i = 0; i < splits.length; i++) {
	                        if (Integer.valueOf(splits[i]) > 255) {
	                            return "";
	                        }
	                    }
	                }
	            }
	            return null;
	        }
	    }
	    });

	    et.addTextChangedListener(new TextWatcher() 
	    {
	        boolean deleting = false;
	        int lastCount = 0;

	        @Override
	        public void afterTextChanged(Editable s) {
	            if (!deleting) {
	                String working = s.toString();
	                String[] split = working.split("\\.");
	                String string = split[split.length - 1];
	                if (string.length() == 3 || string.equalsIgnoreCase("0")
	                        || (string.length() == 2 && Character.getNumericValue(string.charAt(0)) > 1)) {                     
	                    s.append('.');
	                    return;
	                }
	            } 
	        }

	        @Override
	        public void onTextChanged(CharSequence s, int start, int before, int count) {
	            if (lastCount < count) {
	                deleting = false;
	            } else {
	                deleting = true;
	            }
	        }

	        @Override
	        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	            // Nothing happens here
	        }
	    });
	}
	
	
	private List<String> getListPairedDevices() {
		List<String> nameList = new ArrayList<String>();
		try {
			BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			Set<BluetoothDevice> bondedSet = bluetoothAdapter.getBondedDevices();

			if (bondedSet.size() > 0) {
				for (BluetoothDevice device : bondedSet) {
					nameList.add(device.getName());
					macAddressList.add(device.getAddress());
				}
				return nameList;
			} else {
				nameList.clear();
				nameList.add("No Devices");
				macAddressList.clear();
				return nameList;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			StringBuilder sb = new StringBuilder();
			sb.append(e.getMessage()).append(" [com.android.emobilepos.SettingsMenuActiv (at Class.getPairedDevices)]");
			
			EasyTracker.getInstance().setContext(getActivity());
			Tracker myTracker = EasyTracker.getTracker(); // Get a reference to tracker.
			myTracker.sendException(sb.toString(), false); // false indicates non-fatal exception.
		}
		return null;
	}
	
	
	private class autoConnectPrinter extends AsyncTask<String, String, String> {

		MyPreferences myPref;
		StringBuilder sb = new StringBuilder();
		private ProgressDialog progressDlog;

		@Override
		protected void onPreExecute() {
			progressDlog = new ProgressDialog(activity);
			progressDlog.setMessage("Connecting...");
			progressDlog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDlog.setCancelable(false);
			progressDlog.show();
			myPref = new MyPreferences(activity);
		}

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub

			PrintersHandler ph = new PrintersHandler(activity);
			Cursor c = ph.getPrinters();
			HashMap<String, Integer> tempMap = new HashMap<String, Integer>();
			EMSDeviceManager edm;

			Global.multiPrinterManager.clear();
			Global.multiPrinterMap.clear();
			int size = c.getCount();

			if (c != null && size > 0) {
				int i = 0;
				int i_printer_id = c.getColumnIndex("printer_id");
				int i_printer_type = c.getColumnIndex("printer_type");
				int i_cat_id = c.getColumnIndex("cat_id");
				int i_printer_ip = c.getColumnIndex("printer_ip");
				int i_printer_port = c.getColumnIndex("printer_port");
				do {
					if (tempMap.containsKey(c.getString(i_printer_id))) {
						Global.multiPrinterMap.put(c.getString(i_cat_id), tempMap.get(c.getString(i_printer_id)));
					} else {
						tempMap.put(c.getString(i_printer_id), i);
						Global.multiPrinterMap.put(c.getString(i_cat_id), i);

						edm = new EMSDeviceManager();
						Global.multiPrinterManager.add(edm);

						if (Global.multiPrinterManager.get(i).loadMultiDriver(activity, Global.STAR, 48, true, c.getString(i_printer_ip),
								c.getString(i_printer_port)))
							sb.append(c.getString(i_printer_ip)).append(": ").append("Connected\n");
						else
							sb.append(c.getString(i_printer_ip)).append(": ").append("Failed to connect\n");

						i++;
					}

				} while (c.moveToNext());
			}

			String _portName = "";
			String _peripheralName = "";
			if ((myPref.swiperType(true, -2) != -1)&&(Global.btSwiper==null ||Global.btSwiper.currentDevice==null))
																					// //connect
																					// last
																					// swiper
			{
				edm = new EMSDeviceManager();
				_portName = myPref.swiperMACAddress(true, null);
				_peripheralName = Global.getPeripheralName(myPref.swiperType(true, -2));
				Global.btSwiper = edm.getManager();
				// Global.btSwiper.loadDrivers(activity, myPref.swiperType(true,
				// -2), false);
				if (Global.btSwiper.loadMultiDriver(activity, myPref.swiperType(true, -2), 0, false, myPref.swiperMACAddress(true, null),
						null))
					sb.append(_peripheralName).append(": ").append("Connected\n");
				else
					sb.append(_peripheralName).append(": ").append("Failed to connect\n");
			}
			else if(myPref.swiperType(true, -2) != -1&&Global.btSwiper!=null &&Global.btSwiper.currentDevice!=null)
			{
				_peripheralName = Global.getPeripheralName(myPref.swiperType(true, -2));
				sb.append(_peripheralName).append(": ").append("Connected\n");
			}
			if ((myPref.printerType(true, -2) != -1)&&(Global.mainPrinterManager==null ||(Global.mainPrinterManager.currentDevice==null)))
			{
				edm = new EMSDeviceManager();
				Global.mainPrinterManager = edm.getManager();
				_peripheralName = Global.getPeripheralName(myPref.printerType(true, -2));
				_portName = myPref.printerMACAddress(true, null);
				String _portNumber = myPref.getStarPort();
				boolean isPOS = myPref.posPrinter(true, false);
				int txtAreaSize = myPref.printerAreaSize(true, -1);

				if (Global.mainPrinterManager.loadMultiDriver(activity, myPref.printerType(true, -2), txtAreaSize, isPOS, _portName,
						_portNumber))
					sb.append(_peripheralName).append(": ").append("Connected\n");
				else
					sb.append(_peripheralName).append(": ").append("Failed to connect\n");

			}
			else if(myPref.printerType(true, -2)!=-1&&Global.mainPrinterManager!=null&&Global.mainPrinterManager.currentDevice!=null)
			{
				_peripheralName = Global.getPeripheralName(myPref.printerType(true,-2));
				sb.append(_peripheralName).append(": ").append("Connected\n");
				
			}

			return null;
		}

		@Override
		protected void onPostExecute(String unused) {
			progressDlog.dismiss();
			if (sb.toString().length() > 0)
				Global.showPrompt(activity, R.string.dlog_title_confirm, sb.toString());
		}
	}
	
	
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		
		if(resultCode == 1)
			myAdapter.notifyDataSetChanged();
	}
	
}