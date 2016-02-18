package com.android.emobilepos.customer;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
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

import com.android.database.AddressHandler;
import com.android.database.CustomersHandler;
import com.android.database.PriceLevelHandler;
import com.android.database.SalesTaxCodesHandler;
import com.android.database.TaxesHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.models.Address;
import com.android.support.Customer;
import com.android.support.Global;
import com.android.support.MyEditText;
import com.android.support.MyPreferences;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

public class CreateCustomer_FA extends BaseFragmentActivityActionBar implements OnCheckedChangeListener,CompoundButton.OnCheckedChangeListener
,OnClickListener {
	final static int[] fieldID = new int[] { R.id.newCustAlias, R.id.newCustfName, R.id.newCustlName, R.id.newCustCompany,
			R.id.newCustEmail, R.id.newCustPhone, R.id.newCustContact, R.id.newCustBillStr1, R.id.newCustBillStr2, R.id.newCustBillCity,
			R.id.newCustBillState, R.id.newCustBillZip, R.id.newCustShipStr1, R.id.newCustShipStr2, R.id.newCustShipCity,
			R.id.newCustShipState, R.id.newCustShipZip, R.id.newCustDOB };
	private static MyEditText[] field;

	
	private String addr_b_type = "Residential", addr_s_type = "Residential";
	private static final int CUST_ALIAS = 0, CUST_NAME = 1, CUST_LASTNAME = 2, COMPANY_NAME = 3, EMAIL = 4, PHONE = 5, CUST_CONTACT = 6,
			B_STR1 = 7, B_STR2 = 8, B_CITY = 9, B_STATE = 10, B_ZIPCODE = 11, S_STR1 = 12, S_STR2 = 13, S_CITY = 14, S_STATE = 15,
			S_ZIPCODE = 16, DOB = 17;

	private Spinner pricesList, taxesList, bCountrySpinner, sCountrySpinner;
	private final int SPINNER_PRICELEVEL = 0, SPINNER_TAXES = 1, SPINNER_BILL_COUNTRY = 2, SPINNER_SHIP_COUNTRY = 3;
	// private Spinner taxesList;
	private List<String[]> taxList;
	private List<String[]> priceLevelList;
	private List<String> isoCountryList = new ArrayList<String>(), nameCountryList = new ArrayList<String>();

	static Activity activity;
	private int taxSelected = 0;
	private int priceLevelSelected = 0;
	private int bSelectedCountry = 0;
	private int sSelectedCountry = 0;
	private CustomAdapter taxAdapter, priceLevelAdapter;
	private String[] isoCountries, nameCountries;
	private static String dobDate = "";
	private RadioGroup billingRadioGroup, shippingRadioGroup;

	private DialogFragment newFrag;
	
	private Global global;
	private boolean hasBeenCreated = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_customer_layout);
		global = (Global)getApplication();
		activity = this;
		
		setupSpinners();
		setupCountries();
		
		
		int length = fieldID.length;
		field = new MyEditText[length];

		// for i = 0 - 6 (Customer Information)
		// for i = 7 - 12 (Billing Address)
		// for i = 13 - 18 (Shipping Address)
		for (int i = 0; i < length; i++) {
			field[i] = (MyEditText) activity.findViewById(fieldID[i]);
		}

		CheckBox sameAddress = (CheckBox) activity.findViewById(R.id.newCustCheckBox);
		sameAddress.setOnCheckedChangeListener(this);

		Button save = (Button) activity.findViewById(R.id.newCustSaveBut);
		save.setOnClickListener(this);

		field[DOB].setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub

				if (newFrag == null) {
					newFrag = new DateDialog();
					newFrag.show(getSupportFragmentManager(), "dialog");
				}
				return false;
			}
		});
		
		
		hasBeenCreated = true;
	}
	
	@Override
	public void onResume() {

		if(global.isApplicationSentToBackground(activity))
			global.loggedIn = false;
		global.stopActivityTransitionTimer();
		
		if(hasBeenCreated&&!global.loggedIn)
		{
			if(global.getGlobalDlog()!=null)
				global.getGlobalDlog().dismiss();
			global.promptForMandatoryLogin(activity);
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
	


	private OnItemSelectedListener getItemSelectedListener(final int spinnerType)
	{
		OnItemSelectedListener listener = new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
				// your code here
				switch(spinnerType)
				{
				case SPINNER_PRICELEVEL:
					priceLevelSelected = position;
					break;
				case SPINNER_TAXES:
					taxSelected = position;
					break;
				case SPINNER_BILL_COUNTRY:
					bSelectedCountry = position;
					break;
				case SPINNER_SHIP_COUNTRY:
					sSelectedCountry = position;
					break;
				}
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView) {
				// your code here
				switch(spinnerType)
				{
				case SPINNER_PRICELEVEL:
					priceLevelSelected = 0;
					break;
				case SPINNER_TAXES:
					taxSelected = 0;
					break;
				case SPINNER_BILL_COUNTRY:
					bSelectedCountry = 0;
					break;
				case SPINNER_SHIP_COUNTRY:
					sSelectedCountry = 0;
					break;
				}
			}
		};
		
		return listener;
	}
	
	private void setupSpinners()
	{
		List<String> taxes = new ArrayList<String>();
		List<String> priceLevel = new ArrayList<String>();
		taxes.add("Select One");
		priceLevel.add("Select One");

		TaxesHandler handler = new TaxesHandler(this);
		taxList = handler.getTaxes();
		PriceLevelHandler handler2 = new PriceLevelHandler();
		priceLevelList = handler2.getPriceLevel();

		int size = taxList.size();
		int size2 = priceLevelList.size();
		int loopSize = size;
		if (size2 > size)
			loopSize = size2;
		for (int i = 0; i < loopSize; i++) {
			if (i < size)
				taxes.add(taxList.get(i)[0]);
			if (i < size2)
				priceLevel.add(priceLevelList.get(i)[0]);
		}

		taxAdapter = new CustomAdapter(activity, android.R.layout.simple_spinner_item, taxes, taxList, true);
		priceLevelAdapter = new CustomAdapter(activity, android.R.layout.simple_spinner_item, priceLevel, priceLevelList, false);

		pricesList = (Spinner) findViewById(R.id.newCustList1);
		taxesList = (Spinner) findViewById(R.id.newCustList2);

		billingRadioGroup = (RadioGroup) findViewById(R.id.radioGroupBillingAddressType);
		billingRadioGroup.setOnCheckedChangeListener(this);
		shippingRadioGroup = (RadioGroup) findViewById(R.id.radioGroupShippingAddressType);
		shippingRadioGroup.setOnCheckedChangeListener(this);

		taxesList.setAdapter(taxAdapter);
		taxesList.setOnItemSelectedListener(getItemSelectedListener(SPINNER_TAXES));

		pricesList.setAdapter(priceLevelAdapter);
		pricesList.setOnItemSelectedListener(getItemSelectedListener(SPINNER_PRICELEVEL));

		bCountrySpinner = (Spinner) findViewById(R.id.newCustBillCountry);
		sCountrySpinner = (Spinner) findViewById(R.id.newCustShipCountry);

		bCountrySpinner.setOnItemSelectedListener(getItemSelectedListener(SPINNER_BILL_COUNTRY));
		sCountrySpinner.setOnItemSelectedListener(getItemSelectedListener(SPINNER_SHIP_COUNTRY));
	}
	
	private void setupCountries()
	{
		this.isoCountries = Locale.getISOCountries();
		this.nameCountries = new String[isoCountries.length];
		nameCountryList.add("Select One");
		isoCountryList.add("");
		int i = 0;
		MyPreferences myPref = new MyPreferences(activity);
		String defaultCountry = myPref.defaultCountryCode(true, null);
		for (String country : this.isoCountries) {

			isoCountryList.add(country);
			Locale locale = new Locale(Locale.getDefault().getDisplayLanguage(), country);
			this.nameCountries[i] = locale.getDisplayCountry();
			nameCountryList.add(this.nameCountries[i]);
			if (defaultCountry.equals(country)) {
				bSelectedCountry = i + 1;
				sSelectedCountry = i + 1;
			}
			i++;

		}

		CountrySpinnerAdapter billingAdapter = new CountrySpinnerAdapter(activity, android.R.layout.simple_spinner_item,
				this.nameCountryList, this.isoCountryList, true);
		CountrySpinnerAdapter shippingAdapter = new CountrySpinnerAdapter(activity, android.R.layout.simple_spinner_item,
				this.nameCountryList, this.isoCountryList, false);

		bCountrySpinner.setAdapter(billingAdapter);
		sCountrySpinner.setAdapter(shippingAdapter);

		bCountrySpinner.setSelection(bSelectedCountry);
		sCountrySpinner.setSelection(sSelectedCountry);
	}
	
	private void insertNewCustomer() {
		CustomersHandler custHandler = new CustomersHandler(activity);
		AddressHandler addressHandler = new AddressHandler(activity);
		SalesTaxCodesHandler taxCodeHandler = new SalesTaxCodesHandler(activity);
		
		Customer custData = new Customer();
		Address addrData = new Address();
		MyPreferences myPref = new MyPreferences(activity);

		// String lastCustID = custHandler.getLastCustID();
		// GenerateNewID idGenerator = new GenerateNewID(activity);

		// -----Start preparing customer for insert--------//
		// lastCustID = idGenerator.generate(lastCustID, 2);
		String lastCustID = UUID.randomUUID().toString().toUpperCase(Locale.getDefault());
		custData.cust_id = lastCustID;
		custData.cust_name = field[CUST_ALIAS].getText().toString();
		custData.cust_firstName = field[CUST_NAME].getText().toString();
		custData.cust_lastName = field[CUST_LASTNAME].getText().toString();
		custData.CompanyName = field[COMPANY_NAME].getText().toString();
		custData.cust_email = field[EMAIL].getText().toString();
		custData.cust_phone = field[PHONE].getText().toString();
		custData.cust_contact = field[CUST_CONTACT].getText().toString();
		custData.qb_sync = "0";
		custData.cust_dob = field[DOB].getText().toString();

		if (priceLevelSelected > 0)
			custData.pricelevel_id = priceLevelList.get(priceLevelSelected - 1)[1];

		if (taxSelected > 0) {
			custData.cust_salestaxcode = taxList.get(taxSelected - 1)[1];
			custData.cust_taxable = taxCodeHandler.getTaxableTaxCode();
			myPref.setCustTaxCode(custData.cust_salestaxcode);
		}

		String addrLastID = UUID.randomUUID().toString();
		addrData.addr_id = addrLastID;
		addrData.cust_id = lastCustID;
		// add zone id

		// add addr_type
		addrData.addr_b_str1 = field[B_STR1].getText().toString();
		addrData.addr_b_str2 = field[B_STR2].getText().toString();
		// add addr_b_str3
		addrData.addr_b_city = field[B_CITY].getText().toString();
		addrData.addr_b_state = field[B_STATE].getText().toString();
		if (bSelectedCountry > 0)
			addrData.addr_b_country = isoCountryList.get(bSelectedCountry);
		addrData.addr_b_zipcode = field[B_ZIPCODE].getText().toString();

		// add addr_s_name
		addrData.addr_s_str1 = field[S_STR1].getText().toString();
		addrData.addr_s_str2 = field[S_STR2].getText().toString();
		// add addr_s_str3
		addrData.addr_s_city = field[S_CITY].getText().toString();
		addrData.addr_s_state = field[S_STATE].getText().toString();
		if (sSelectedCountry > 0)
			addrData.addr_s_country = isoCountryList.get(sSelectedCountry);
		addrData.addr_s_zipcode = field[S_ZIPCODE].getText().toString();

		addrData.addr_b_type = addr_b_type;
		addrData.addr_s_type = addr_s_type;
		// add qb_cust_id

		addressHandler.insertOneAddress(addrData);
		// }
		custHandler.insertOneCustomer(custData);// insert new customer to table

		// Set-up data for default selection of this newly created customer
		HashMap<String, String> custMap = custHandler.getCustomerInfo(lastCustID);
		
//		SalesTaxCodesHandler taxHandler = new SalesTaxCodesHandler(activity);
//		if (taxHandler.checkIfCustTaxable(custMap.get("cust_taxable")))
//			myPref.setCustTaxCode(custMap.get("cust_salestaxcode"));
//		else
//			myPref.setCustTaxCode("");

		myPref.setCustID(custMap.get("cust_id"));
		myPref.setCustName(custMap.get("cust_name"));
		myPref.setCustPriceLevel(custData.pricelevel_id);
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
		List<String> leftData = null;
		List<String> rightData = null;
		boolean isBilling = false;

		public CountrySpinnerAdapter(Activity activity, int resource, List<String> left, List<String> right, boolean isBilling) {
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
				// rightView.setText(rightData.get(position-1));
				checked.setVisibility(View.VISIBLE);
				break;
			}
			case 2: {
				// rightView.setText(rightData.get(position-1));
				break;
			}
			}

			return row;
		}

		@Override
		public int getItemViewType(int position) {
			if (position == 0) {
				return 0;
			} else if ((isBilling && position == bSelectedCountry) || (!isBilling && position == sSelectedCountry)) {
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
			sb.append(Integer.toString(year)).append(Integer.toString(monthOfYear + 1)).append(Integer.toString(dayOfMonth));
			Calendar cal = Calendar.getInstance();
			cal.set(year, monthOfYear, dayOfMonth);
			TimeZone tz = cal.getTimeZone();

			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMd", Locale.getDefault());
			sdf1.setTimeZone(tz);
			SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

			try {
				dobDate = sdf2.format(sdf1.parse(sb.toString()));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				StringBuilder sb2 = new StringBuilder();
				sb2.append(e.getMessage()).append(" [").append("com.android.support.ReportsMenuActivity (at Class.DateDialog) ]");
//				Tracker tracker = EasyTracker.getInstance(activity);
//				tracker.send(MapBuilder.createException(sb2.toString(), false).build());
				// throw new RuntimeException(e);
			}
			field[DOB].setText(Global.formatToDisplayDate(dobDate, activity, 1));

		}
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		// TODO Auto-generated method stub
		switch (checkedId) {
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

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
		case R.id.newCustSaveBut:
			int allWhitespaced = field[0].getText().toString().trim().length();

			if (allWhitespaced == 0) {
				field[0].setBackgroundResource(R.drawable.edittext_wrong_input);
				Global.showPrompt(activity, R.string.dlog_title_error, activity.getString(R.string.card_validation_error));
			}
			else if(!field[EMAIL].getText().toString().isEmpty()&&!validEmail(field[EMAIL].getText().toString()))
			{
				field[EMAIL].setBackgroundResource(R.drawable.edittext_wrong_input);
				Global.showPrompt(activity, R.string.dlog_title_error, activity.getString(R.string.card_validation_error));
			}
			else {
				field[0].setBackgroundResource(R.drawable.edittext_border);
				field[EMAIL].setBackgroundResource(R.drawable.edittext_border);
				insertNewCustomer();
				setResult(-1);
				finish();
			}
			break;
		}
	}
	
	private boolean validEmail(String paramString)
	  {
	    return Patterns.EMAIL_ADDRESS.matcher(paramString).matches();
	  }

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub
		if (isChecked) {
			for (int i = B_STR1; i < B_ZIPCODE + 1; i++) {

				field[i + 5].setText(field[i].getText().toString());
			}
			sCountrySpinner.setSelection(bSelectedCountry);
		} else {
			for (int i = B_STR1; i < B_ZIPCODE + 1; i++) {
				field[i + 5].setText("");
			}
		}
	}

}
