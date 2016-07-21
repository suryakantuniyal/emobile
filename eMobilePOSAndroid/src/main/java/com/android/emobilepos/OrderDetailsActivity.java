package com.android.emobilepos;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.android.database.AddressHandler;
import com.android.database.ShipMethodHandler;
import com.android.database.TermsHandler;
import com.android.support.Global;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class OrderDetailsActivity extends BaseFragmentActivityActionBar
{
	private CustomAdapter adapter;
	private String empStr = "";
	private final String defaultVal = "None";
	private List<String> leftMenuList;// = Arrays.asList(getString(R.string.details_shipping),getString(R.string.details_terms),getString(R.string.details_delivery),getString(R.string.details_address),getString(R.string.details_comments),getString(R.string.details_po));
	private Activity activity;
	private int shipmentSelected = 0,termsSelected = 0,addressSelected=0;
	
	private List<String[]>shippingMethodsDownloadedItems = new ArrayList<String[]>();
	private String[] shipMethodItems = new String[]{};
	
	private List<String[]> termsDownloadedItems = new ArrayList<String[]>();
	private String[] termsItems = new String[]{};
	
	private List<String[]> addressDownloadedItems = new ArrayList<String[]>();
	private String[] addressItems = new String[]{};
	
	private ListView myListView;
	private int currYear,currMonth,currDay;
	static final int DATE_DIALOG_ID = 0;
	private String deliveryDate = defaultVal;
	
	private String inputComment = empStr,inputPO = empStr;
	private Global global;
	private boolean hasBeenCreated = false;
	

	
	
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.order_details_layout);
		
		activity = this;
		global = (Global) this.getApplication();
		
		leftMenuList = Arrays.asList(getString(R.string.details_shipping),getString(R.string.details_terms),
				getString(R.string.details_delivery),getString(R.string.details_address),getString(R.string.details_comments),getString(R.string.details_po));
		
		if(global.getSelectedAddressMethod()!=-1)
			addressSelected = global.getSelectedAddressMethod();
		if(global.getSelectedShippingMethod()!=-1)
			shipmentSelected = global.getSelectedShippingMethod();
		if(global.getSelectedTermsMethod()!=-1)
			termsSelected = global.getSelectedTermsMethod();
		if(!global.getSelectedDeliveryDate().isEmpty())
			deliveryDate = global.getSelectedDeliveryDate();
		if(!global.getSelectedComments().isEmpty())
			inputComment = global.getSelectedComments();
		if(!global.getSelectedPO().isEmpty())
			inputPO = global.getSelectedPO();
		
		
		initAllMenuValues();
		myListView = (ListView) findViewById(R.id.orderDetailsListView);
		
		adapter = new CustomAdapter(this);
		myListView.setAdapter(adapter);
		
		
		myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				// TODO Auto-generated method stub
				
				executeItemAction(position);
			}
		});
		
		hasBeenCreated = true;
		
	}
	
	
	
	private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
		
		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			// TODO Auto-generated method stub
			currYear = year;
			currMonth = monthOfYear+1;
			currDay = dayOfMonth;
			
			StringBuilder sb = new StringBuilder();
			sb.append(Integer.toString(currMonth)).append("/").append(Integer.toString(currDay)).append("/").append(Integer.toString(currYear));
			deliveryDate = sb.toString();
			global.setSelectedDeliveryDate(deliveryDate);
			myListView.invalidateViews();
			
			
		}
	};
	
	
	private void executeItemAction(int pos)
	{
		switch (pos)
		{
			case 0:						//Shipping
				showPickerDialogBox(pos);
				break;
			case 1:						//Terms
				showPickerDialogBox(pos);
				break;
			case 2:						//Delivery
				showDialog(DATE_DIALOG_ID);
				break;
			case 3:						//Address
				showPickerDialogBox(pos);
				break;
			case 4:						//Comments
				showEditTextDialogBox(pos);
				break;
			case 5:						//PO
				showEditTextDialogBox(pos);
				break;
		}
	}
	
	@Override
	public void onResume() {

		if(global.isApplicationSentToBackground(this))
			global.loggedIn = false;
		global.stopActivityTransitionTimer();
		
		if(hasBeenCreated&&!global.loggedIn)
		{
			if(global.getGlobalDlog()!=null)
				global.getGlobalDlog().dismiss();
			global.promptForMandatoryLogin(this);
		}
		super.onResume();
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		PowerManager powerManager = (PowerManager)getSystemService(POWER_SERVICE);
		boolean isScreenOn = powerManager.isScreenOn();
		if(!isScreenOn)
			global.loggedIn = false;
		global.startActivityTransitionTimer();
	}
	
	
	private void initAllMenuValues()
	{
		
		// initialize Shipment Methods Items from database
		ShipMethodHandler shipMethodHandler = new ShipMethodHandler(activity);
		shippingMethodsDownloadedItems = shipMethodHandler.getShipmentMethods();
		
		int size = shippingMethodsDownloadedItems.size();
		shipMethodItems = new String[size+1];
		shipMethodItems[0] = defaultVal;
		for(int i = 0 ; i < size; i++)
		{
			shipMethodItems[i+1] = shippingMethodsDownloadedItems.get(i)[0];
		}
		
		
		// initialize Terms items from database
		TermsHandler termsHandler = new TermsHandler(activity);
		termsDownloadedItems = termsHandler.getAllTerms();
		
		size = termsDownloadedItems.size();
		termsItems = new String[size+1];
		termsItems[0] = defaultVal;
	
		for(int i = 0 ; i < size; i++)
		{
			termsItems[i+1] = termsDownloadedItems.get(i)[0];
		}
		
		// initialize Dates
		final Calendar cal = Calendar.getInstance();
		currYear = cal.get(Calendar.YEAR);
		currMonth = cal.get(Calendar.MONTH);
		currDay = cal.get(Calendar.DAY_OF_MONTH);
		
		
		//initialize Address from database
		AddressHandler addressHandler = new AddressHandler(activity);
		addressDownloadedItems = addressHandler.getAddress();
		size = addressDownloadedItems.size();
		addressItems = new String[size+1];
		addressItems[0] = defaultVal;
		StringBuilder sb = new StringBuilder();
		String temp = empStr;
		for(int i = 0 ; i<size;i++)
		{
			//sb.append("[").append(addressDownloadedItems.get(i)[0]).append("] ");
			temp = addressDownloadedItems.get(i)[1];
			if(!temp.isEmpty())							//address 1
				sb.append(temp).append(" ");
			temp = addressDownloadedItems.get(i)[2];
			if(!temp.isEmpty())							//address 2
				sb.append(temp).append(" ");
			temp = addressDownloadedItems.get(i)[3];
			if(!temp.isEmpty())							//address 3
				sb.append(temp).append("\t\t");
			temp = addressDownloadedItems.get(i)[4];
			if(!temp.isEmpty())							//address country
				sb.append(temp).append(" ");
			temp = addressDownloadedItems.get(i)[5];
			if(!temp.isEmpty())							//address city
				sb.append(temp).append(",");
			temp = addressDownloadedItems.get(i)[6];		//address state
			if(!temp.isEmpty())
				sb.append(temp).append(" ");
			
			temp = addressDownloadedItems.get(i)[7];		//address zip code
			if(!temp.isEmpty())
				sb.append(temp);
			
			addressItems[i+1] = sb.toString();
			sb.setLength(0);
		}
	}
	private void showPickerDialogBox(final int type)
	{
		//List<String[]> valueArrayList;
		String dialogTitle = "No Items";
		String [] menuItems = new String[]{};
		int selectedItem = 0;
		switch(type)
		{
		case 0:
			if(shipMethodItems.length>0)
			{
				menuItems = shipMethodItems;
				dialogTitle = "Shipment Methods";
				selectedItem = shipmentSelected;
				
			}
			break;
			
		case 1:
			if(termsItems.length>0)
			{
				menuItems = termsItems;
				dialogTitle = "Terms";
				selectedItem = termsSelected;
				
			}
			break;
			
		case 3:
			if(addressItems.length>0)
			{
				menuItems = addressItems;
				dialogTitle = "Select Address";
				selectedItem = addressSelected;
				
			}
			break;
		}

		
		AlertDialog.Builder adb = new AlertDialog.Builder(activity);
		adb.setSingleChoiceItems(menuItems, selectedItem, new OnClickListener(){
			
			@Override
			public void onClick(DialogInterface d, int position )
			{
				if(type == 0)
				{
					shipmentSelected = position;
					global.setSelectedShippingMethod(shipmentSelected);
					if(position!=0)
						global.setSelectedShippingMethodString(shippingMethodsDownloadedItems.get(position-1)[1]);
						
				}
				else if(type == 1)
				{
					termsSelected = position;
					global.setSelectedTermsMethod(termsSelected);
					if(position!=0)
					{
						global.setSelectedTermsMethodString(termsDownloadedItems.get(position-1)[1]);
						//global.setSelectedTermsMethodString(termsItems[position]);
					}
				}
				else if(type==3)
				{
					addressSelected = position;
					global.setSelectedAddress(addressSelected);
					if(position!=0)
						global.setSelectedAddressString(addressDownloadedItems.get(position-1)[0]);
				}
				myListView.invalidateViews();
				d.dismiss();
			}
		});
		
		adb.setNegativeButton("Cancel", null);
		adb.setTitle(dialogTitle);
		adb.show();
		
		
	}
	
	private void showEditTextDialogBox(final int type)
	{
		//String inputText = empStr;
		String dialogTitle = empStr;
		
		final EditText editTextField = new EditText(activity);
		int orientation = getResources().getConfiguration().orientation;
//		DisplayMetrics metrics = new DisplayMetrics();
//		getWindowManager().getDefaultDisplay().getMetrics(metrics);
//		int size = 200;
//		if(orientation == Configuration.ORIENTATION_PORTRAIT)
//			size = metrics.heightPixels/3;
//		else
//			size = metrics.widthPixels/3;
		
//		editTextField.setHeight(size);
		editTextField.setSingleLine(false);
		editTextField.setGravity(Gravity.TOP);
		
		switch (type)
		{
		case 4: 							//Comments
			dialogTitle = "Comments";
			if(!inputComment.equals(defaultVal))
			{
				editTextField.setText(inputComment);
				editTextField.setSelection(inputComment.length());
			}
			
			
			
			break;
		case 5:								//PO
			dialogTitle = "PO";
			if(!inputPO.equals(defaultVal))
			{
				editTextField.setText(inputPO);
				editTextField.setSelection(inputPO.length());
			}
			break;
		}
		
		AlertDialog.Builder adb = new AlertDialog.Builder(activity);
		

		
		adb.setView(editTextField);
		adb.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				if(type==4)
				{
					inputComment = editTextField.getText().toString();
					if(inputComment.trim().length()>0)
					{
						//global.order.getSetData("ord_comment", false, inputComment);
						global.setSelectedComments(inputComment.trim());
					}
				}
				else
				{
					inputPO = editTextField.getText().toString();
					if(inputPO.trim().length()>0)
					{
						//global.order.getSetData("ord_po", false, inputPO.trim());
						global.setSelectedPO(inputPO.trim());
					}
				}
				
				myListView.invalidateViews();
				dialog.dismiss();
			}
		});
		adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		});
		adb.setTitle(dialogTitle);
		
		adb.show();
	}

    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case DATE_DIALOG_ID:
            return new DatePickerDialog(this,
                        datePickerListener,
                        currYear, currMonth, currDay);
        }
        return null;
    }
	private class CustomAdapter extends BaseAdapter
	{
		private LayoutInflater inflater;
		
		public CustomAdapter(Activity activity)
		{
			inflater = LayoutInflater.from(activity);
		}
		
		@Override
		public int getViewTypeCount() {
			return 6;
		}

		@Override
		public int getItemViewType(int position) 
		{
			if(position == 0)
				return 0;
			else if(position == 1)
				return 1;
			else if(position == 2)
				return 2;
			else if(position == 3)
				return 3;
			else if(position == 4)
				return 4;
			return 5;
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return leftMenuList.size();
		}
		@Override
		public Object getItem(int pos) {
			// TODO Auto-generated method stub
			return leftMenuList.get(pos);
		}
		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return 0;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ViewHolder holder;
			int type = getItemViewType(position);
			
			if(convertView == null)
			{
				holder = new ViewHolder();
				
				convertView = inflater.inflate(R.layout.order_details_lvadapter, null);
				
				holder.leftText = (TextView) convertView.findViewById(R.id.orderDetailsLVText);
				holder.rightText = (TextView) convertView.findViewById(R.id.orderDetailsLVRightText);
				
				holder.leftText.setText(leftMenuList.get(position));
				setHolderValues(type,holder);
				
				
				convertView.setTag(holder);
			}
			else
			{
				holder = (ViewHolder) convertView.getTag();
				
				holder.leftText.setText(leftMenuList.get(position));
				
				setHolderValues(position,holder);
			}
			return convertView;
		}
		
		private void setHolderValues(int type,ViewHolder holder)
		{
			switch(type)
			{
			case 0:																	//Shipping
				if(shipMethodItems.length>0)
					holder.rightText.setText(shipMethodItems[shipmentSelected]);
				break;
			case 1:																	//Terms
				if(termsItems.length>0)
					holder.rightText.setText(termsItems[termsSelected]);
				break;
			case 2:																	//Delivery
				holder.rightText.setText(deliveryDate);
				break;
			case 3:																	//Address
				holder.rightText.setText(addressItems[addressSelected]);
				break;
			case 4:																	//Comments
				if(!inputComment.isEmpty())
					holder.rightText.setText(inputComment);
				else
					holder.rightText.setText(defaultVal);
				
				break;
			case 5:																	//PO
				if(!inputPO.isEmpty())
					holder.rightText.setText(inputPO);
				else
					holder.rightText.setText(defaultVal);
				break;
				
			}
		}
		public class ViewHolder
		{
			TextView leftText;
			TextView rightText;
		}
	}
}
