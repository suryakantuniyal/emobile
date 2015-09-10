package com.android.emobilepos;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

import java.util.List;

import com.android.database.AddressHandler;
import com.android.database.CustomersHandler;
import com.android.database.PriceLevelHandler;
import com.android.database.SalesTaxCodesHandler;
import com.android.database.TaxesHandler;
import com.android.support.Address;
import com.android.support.Customer;
import com.android.support.Global;
import com.android.support.MyEditText;
import com.android.support.MyPreferences;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

public class AddNewCustFragment extends Fragment implements OnCheckedChangeListener {
	final static int[] fieldID = new int[] { R.id.newCustAlias, R.id.newCustfName, R.id.newCustlName, R.id.newCustCompany, R.id.newCustEmail,
			R.id.newCustPhone, R.id.newCustContact, R.id.newCustBillStr1, R.id.newCustBillStr2, R.id.newCustBillCity, R.id.newCustBillState,
			 R.id.newCustBillZip, R.id.newCustShipStr1, R.id.newCustShipStr2, R.id.newCustShipCity, R.id.newCustShipState,
			 R.id.newCustShipZip,R.id.newCustDOB };
	private static MyEditText[] field;
	//private Button[]multiButtons;
	//private final int BILL_RESIDENTIAL = 0 , BILL_BUSINESS = 1, SHIP_RESIDENTIAL = 2, SHIP_BUSINESS = 3;
	private String addr_b_type = "Residential", addr_s_type = "Residential";
	private static final int CUST_ALIAS = 0,CUST_NAME = 1,CUST_LASTNAME = 2,COMPANY_NAME = 3,EMAIL = 4,PHONE =5,CUST_CONTACT = 6,
			B_STR1 = 7,B_STR2 = 8, B_CITY = 9,B_STATE = 10,B_ZIPCODE=11,S_STR1 = 12,S_STR2 = 13, S_CITY = 14,S_STATE = 15,
			S_ZIPCODE=16,DOB = 17;
	
	private Spinner pricesList,taxesList,bCountrySpinner,sCountrySpinner;
	//private Spinner taxesList;
	private List<String[]> taxList;
	private List<String[]> priceLevelList;
	private List<String> isoCountryList = new ArrayList<String>(),nameCountryList = new ArrayList<String>();

	final Context thisContext = getActivity();
	static Activity activity;
	private int taxSelected = 0;
	private int priceLevelSelected = 0;
	private int bSelectedCountry = 0;
	private int sSelectedCountry = 0;
	private CustomAdapter taxAdapter, priceLevelAdapter;
	private String[] isoCountries,nameCountries;
	private static String dobDate = "";
	private RadioGroup billingRadioGroup,shippingRadioGroup;
	
	private DialogFragment newFrag;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.new_customer_layout, container, false);
		activity = getActivity();
		List<String> taxes = new ArrayList<String>();
		List<String> priceLevel = new ArrayList<String>();
		taxes.add("Select One");
		priceLevel.add("Select One");

		TaxesHandler handler = new TaxesHandler(getActivity());
		taxList = handler.getTaxes();
		PriceLevelHandler handler2 = new PriceLevelHandler(getActivity());
		priceLevelList = handler2.getPriceLevel();

		int size = taxList.size();
		int size2 = priceLevelList.size();
		int loopSize = size;
		if(size2>size)
			loopSize = size2;
		for (int i = 0; i < loopSize; i++) {
			if(i<size)
				taxes.add(taxList.get(i)[0]);
			if(i<size2)
				priceLevel.add(priceLevelList.get(i)[0]);
		}
		
		taxAdapter = new CustomAdapter(activity, android.R.layout.simple_spinner_item, taxes, taxList, true);
		priceLevelAdapter = new CustomAdapter(activity, android.R.layout.simple_spinner_item, priceLevel, priceLevelList, false);

		pricesList = (Spinner) view.findViewById(R.id.newCustList1);
		taxesList = (Spinner) view.findViewById(R.id.newCustList2);
		
		billingRadioGroup = (RadioGroup)view.findViewById(R.id.radioGroupBillingAddressType);
		billingRadioGroup.setOnCheckedChangeListener(this);
		shippingRadioGroup = (RadioGroup)view.findViewById(R.id.radioGroupShippingAddressType);
		shippingRadioGroup.setOnCheckedChangeListener(this);

		taxesList.setAdapter(taxAdapter);
		taxesList.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
				// your code here
				taxSelected = position;
			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView) {
				// your code here
				taxSelected = 0;
			}
		});

		pricesList.setAdapter(priceLevelAdapter);
		pricesList.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
				// your code here
				priceLevelSelected = position;
			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView) {
				// your code here
				priceLevelSelected = 0;
			}
		});
		
		
		bCountrySpinner = (Spinner)view.findViewById(R.id.newCustBillCountry);
		sCountrySpinner = (Spinner)view.findViewById(R.id.newCustShipCountry);
		
		bCountrySpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
				// your code here
				bSelectedCountry = position;
			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView) {
				// your code here
				bSelectedCountry = 0;
			}
		});
		sCountrySpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
				// your code here
				sSelectedCountry = position;
			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView) {
				// your code here
				sSelectedCountry = 0;
			}
		});
		
		//Get list of all countries
		
		this.isoCountries = Locale.getISOCountries();
		this.nameCountries = new String[isoCountries.length];
		nameCountryList.add("Select One");
		isoCountryList.add("");
		int i = 0;
		MyPreferences myPref = new MyPreferences(activity);
		String defaultCountry = myPref.defaultCountryCode(true, null);
		for(String country : this.isoCountries)
		{
			
			isoCountryList.add(country);
			Locale locale = new Locale(Locale.getDefault().getDisplayLanguage(),country);
			this.nameCountries[i] = locale.getDisplayCountry();
			nameCountryList.add(this.nameCountries[i]);
			if(defaultCountry.equals(country))
			{
				bSelectedCountry = i+1;
				sSelectedCountry = i+1;
			}
			i++;
			
		}
		
		CountrySpinnerAdapter billingAdapter =  new CountrySpinnerAdapter(activity, android.R.layout.simple_spinner_item, this.nameCountryList, this.isoCountryList,true);;
		CountrySpinnerAdapter shippingAdapter =  new CountrySpinnerAdapter(activity, android.R.layout.simple_spinner_item, this.nameCountryList, this.isoCountryList,false);;
		
		bCountrySpinner.setAdapter(billingAdapter);
		sCountrySpinner.setAdapter(shippingAdapter);
		
		bCountrySpinner.setSelection(bSelectedCountry);
		sCountrySpinner.setSelection(sSelectedCountry);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (activity != null) {
			int length = fieldID.length;
			field= new MyEditText[length];

			// for i = 0 - 6 (Customer Information)
			// for i = 7 - 12 (Billing Address)
			// for i = 13 - 18 (Shipping Address)
			for (int i = 0; i < length; i++) {
				field[i] = (MyEditText) activity.findViewById(fieldID[i]);
			}

			CheckBox sameAddress = (CheckBox) activity.findViewById(R.id.newCustCheckBox);

			sameAddress.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					// TODO Auto-generated method stub
					if (isChecked) {
						for (int i = B_STR1; i < B_ZIPCODE+1; i++) 
						{
							
							field[i + 5].setText(field[i].getText().toString());
						}
						sCountrySpinner.setSelection(bSelectedCountry);
					} else {
						for (int i = B_STR1; i < B_ZIPCODE+1; i++) {
							field[i + 5].setText("");
						}
					}
				}
			});

			Button save = (Button) activity.findViewById(R.id.newCustSaveBut);
			save.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					int allWhitespaced = field[0].getText().toString().trim().length();

					if (allWhitespaced == 0) {
						field[0].setBackgroundResource(R.drawable.edittext_wrong_input);

						Global.showPrompt(activity, R.string.dlog_title_error, activity.getString(R.string.card_validation_error));
						
					} else {
						field[0].setBackgroundResource(R.drawable.edittext_border);
						insertNewCustomer();
						getActivity().setResult(-1);
						getActivity().finish();
					}
				}
			});

			
			
			field[DOB].setOnTouchListener(new View.OnTouchListener() {
				
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					// TODO Auto-generated method stub
					
					if(newFrag == null)
					{
						newFrag = new DateDialog();
						newFrag.show(getFragmentManager(), "dialog");
					}
					return false;
				}
			});
		}
	}
	
	
	private void insertNewCustomer()
	{
		CustomersHandler custHandler = new CustomersHandler(activity);
		AddressHandler addressHandler = new AddressHandler(activity);
		Customer custData = new Customer();
		Address addrData = new Address();
		MyPreferences myPref = new MyPreferences(activity);
		
		//String lastCustID = custHandler.getLastCustID();
		//GenerateNewID idGenerator = new GenerateNewID(activity);
		
		//-----Start preparing customer for insert--------//
		//lastCustID = idGenerator.generate(lastCustID, 2);
		String lastCustID = UUID.randomUUID().toString().toUpperCase(Locale.getDefault());
		custData.getSetData("cust_id", false, lastCustID);
		custData.getSetData("cust_name", false, field[CUST_ALIAS].getText().toString());
		custData.getSetData("cust_firstName",false, field[CUST_NAME].getText().toString());
		custData.getSetData("cust_lastName", false, field[CUST_LASTNAME].getText().toString());
		custData.getSetData("CompanyName", false, field[COMPANY_NAME].getText().toString());
		custData.getSetData("cust_email", false, field[EMAIL].getText().toString());
		custData.getSetData("cust_phone", false, field[PHONE].getText().toString());
		custData.getSetData("cust_contact", false, field[CUST_CONTACT].getText().toString());
		custData.getSetData("qb_sync", false, "0");
		custData.getSetData("cust_dob", false, field[DOB].getText().toString());
		
		
		
		if(priceLevelSelected>0)
			custData.getSetData("pricelevel_id", false, priceLevelList.get(priceLevelSelected-1)[1]);
		
		if(taxSelected>0)
		{
			custData.getSetData("cust_salestaxcode", false, taxList.get(taxSelected-1)[1]);
			custData.getSetData("cust_taxable", false, taxList.get(taxSelected-1)[1]);
			myPref.setCustTaxCode(custData.getSetData("cust_salestaxcode", true, null));
		}

		
		
		
		
		
		
		String addrLastID = UUID.randomUUID().toString();
		addrData.getSetData("addr_id", false, addrLastID);
		addrData.getSetData("cust_id", false, lastCustID);
		//add zone id
		
		
		
		
		
		
		
//		int count1 = 0,count2 = 0;
//		for(int i = B_STR1;i<B_ZIPCODE+1;i++)
//		{
//			if(!field[i].getText().toString().isEmpty())
//				count1++;
//		}
//		for(int i = S_STR1;i<S_ZIPCODE+1;i++)
//		{
//			if(!field[i].getText().toString().isEmpty())
//				count2++;
//		}
		
//		if(count1>0||count2>0)
//		{
			//add addr_type
			addrData.getSetData("addr_b_str1", false, field[B_STR1].getText().toString());
			addrData.getSetData("addr_b_str2", false, field[B_STR2].getText().toString());
			//add addr_b_str3
			addrData.getSetData("addr_b_city", false, field[B_CITY].getText().toString());
			addrData.getSetData("addr_b_state", false, field[B_STATE].getText().toString());
			if(bSelectedCountry>0)
				addrData.getSetData("addr_b_country", false, isoCountryList.get(bSelectedCountry));
			addrData.getSetData("addr_b_zipcode", false, field[B_ZIPCODE].getText().toString());
			
			
			
			//add addr_s_name
			addrData.getSetData("addr_s_str1", false, field[S_STR1].getText().toString());
			addrData.getSetData("addr_s_str2", false, field[S_STR2].getText().toString());
			//add addr_s_str3
			addrData.getSetData("addr_s_city", false, field[S_CITY].getText().toString());
			addrData.getSetData("addr_s_state", false, field[S_STATE].getText().toString());
			if(sSelectedCountry>0)
				addrData.getSetData("addr_s_country", false,isoCountryList.get(sSelectedCountry));
			addrData.getSetData("addr_s_zipcode", false, field[S_ZIPCODE].getText().toString());
			
			
			addrData.getSetData("addr_b_type", false, addr_b_type);
			addrData.getSetData("addr_s_type", false, addr_s_type);
			//add qb_cust_id
		

			addressHandler.insertOneAddress(addrData);
//		}
		custHandler.insertOneCustomer(custData);//insert new customer to table
		
		
		
		
		//Set-up data for default selection of this newly created customer
		HashMap<String,String> custMap = custHandler.getCustomerInfo(lastCustID);
		SalesTaxCodesHandler taxHandler = new SalesTaxCodesHandler(activity);
		if(taxHandler.checkIfCustTaxable(custMap.get("cust_taxable")))
			myPref.setCustTaxCode(custMap.get("cust_salestaxcode"));
		else
			myPref.setCustTaxCode("");
		
		myPref.setCustID(custMap.get("cust_id"));
		myPref.setCustName(custMap.get("cust_name"));
		myPref.setCustPriceLevel(custData.getSetData("pricelevel_id", true, null));
		myPref.setCustSelected(true);
		myPref.setCustIDKey(custMap.get("custidkey"));
		myPref.setCustEmail(custMap.get("cust_email"));
	}
	
	
	
	public class CustomAdapter extends ArrayAdapter<String> {
		private Activity context;
		List<String> leftData = null;
		List<String[]> rightData = null;
		boolean isTax = false;

		public CustomAdapter(Activity activity, int resource, List<String> left, List<String[]> right, boolean isTax) {
			super(activity, resource, left);
			this.context = activity;
			this.leftData = left;
			this.rightData = right;
			this.isTax = isTax;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = super.getView(position, convertView, parent);

			// we know that simple_spinner_item has android.R.id.text1 TextView:

			TextView text = (TextView) view.findViewById(android.R.id.text1);
			text.setTextColor(Color.BLACK);// choose your color
			text.setPadding(35, 0, 0, 0);

			return view;

			// return super.getView(position,convertView,parent);
		}

		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent) {
			View row = convertView;
			if (row == null) {
				LayoutInflater inflater = context.getLayoutInflater();
				row = inflater.inflate(R.layout.spinner_layout, parent, false);
			}

			TextView taxName = (TextView) row.findViewById(R.id.taxName);
			TextView taxValue = (TextView) row.findViewById(R.id.taxValue);
			ImageView checked = (ImageView) row.findViewById(R.id.checkMark);
			checked.setVisibility(View.INVISIBLE);
			taxName.setText(leftData.get(position));
			int type = getItemViewType(position);
			switch (type) {
			case 0: {
				taxValue.setText("");
				break;
			}
			case 1: {
				taxValue.setText(rightData.get(position - 1)[2]);
				checked.setVisibility(View.VISIBLE);
				break;
			}
			case 2: {
				taxValue.setText(rightData.get(position - 1)[2]);
				break;
			}
			}

			return row;
		}

		@Override
		public int getItemViewType(int position) {
			if (position == 0) {
				return 0;
			} else if ((isTax && position == taxSelected) || (!isTax && position == priceLevelSelected)) {
				return 1;
			}
			return 2;

		}

	}

	
	public class CountrySpinnerAdapter extends ArrayAdapter<String> {
		private Activity context;
		List<String>leftData = null;
		List<String> rightData = null;
		boolean isBilling = false;

		public CountrySpinnerAdapter (Activity activity, int resource, List<String> left, List<String> right,boolean isBilling) {
			super(activity, resource, left);
			this.context = activity;
			this.leftData = left;
			this.rightData = right;
			this.isBilling = isBilling;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = super.getView(position, convertView, parent);

			// we know that simple_spinner_item has android.R.id.text1 TextView:

			TextView text = (TextView) view.findViewById(android.R.id.text1);
			text.setTextColor(Color.BLACK);// choose your color
			text.setPadding(35, 0, 0, 0);

			return view;

			// return super.getView(position,convertView,parent);
		}

		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent) {
			View row = convertView;
			if (row == null) {
				LayoutInflater inflater = context.getLayoutInflater();
				row = inflater.inflate(R.layout.spinner_layout, parent, false);
			}

			TextView leftView = (TextView) row.findViewById(R.id.taxName);
			TextView rightView = (TextView) row.findViewById(R.id.taxValue);
			ImageView checked = (ImageView) row.findViewById(R.id.checkMark);
			checked.setVisibility(View.INVISIBLE);
			leftView.setText(leftData.get(position));
			rightView.setText(rightData.get(position));
			int type = getItemViewType(position);
			switch (type) {
			case 0: {
				
				break;
			}
			case 1: {
				//rightView.setText(rightData.get(position-1));
				checked.setVisibility(View.VISIBLE);
				break;
			}
			case 2: {
				//rightView.setText(rightData.get(position-1));
				break;
			}
			}

			return row;
		}

		@Override
		public int getItemViewType(int position) {
			if (position == 0) {
				return 0;
			} else if ((isBilling && position == bSelectedCountry)||(!isBilling&&position==sSelectedCountry)) {
				return 1;
			}
			return 2;

		}
	}
	
	
	public static class DateDialog extends DialogFragment implements DatePickerDialog.OnDateSetListener {

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final Calendar c = Calendar.getInstance();
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DAY_OF_MONTH);

			return new DatePickerDialog(getActivity(), this, year, month, day);

		}

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

			// Do something after user selects the date...
			StringBuilder sb = new StringBuilder();
			sb.append(Integer.toString(year)).append(Integer.toString(monthOfYear+1)).append(Integer.toString(dayOfMonth));
			Calendar cal = Calendar.getInstance();
			cal.set(year, monthOfYear, dayOfMonth);
			TimeZone tz = cal.getTimeZone();
			
			
			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMd",Locale.getDefault());
			sdf1.setTimeZone(tz);
			SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",Locale.getDefault());
			
			try {
				dobDate = sdf2.format(sdf1.parse(sb.toString()));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				StringBuilder sb2 = new StringBuilder();
				sb2.append(e.getMessage()).append(" [").append("com.android.support.ReportsMenuActivity (at Class.DateDialog) ]");
				Tracker tracker = EasyTracker.getInstance(activity);
				tracker.send(MapBuilder.createException(sb2.toString(), false).build());
				//throw new RuntimeException(e);
			}
			field[DOB].setText(Global.formatToDisplayDate(dobDate, activity, 1));
			
		}
	}


	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		// TODO Auto-generated method stub
		switch(checkedId)
		{
		case R.id.radioBillingResidential:
			addr_b_type = "Residential";
			break;
		case R.id.radioBillingBusiness:
			addr_b_type = "Business";
			break;
		case R.id.radioShippingResidential:
			addr_s_type = "Residential";
			break;
		case R.id.radioShippingBusiness:
			addr_s_type = "Business";
			break;
		}
	}
	
	
}
