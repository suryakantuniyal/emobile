package com.android.emobilepos.customer;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.dao.CustomerCustomFieldsDAO;
import com.android.database.AddressHandler;
import com.android.database.CustomersHandler;
import com.android.database.PriceLevelHandler;
import com.android.database.SalesTaxCodesHandler;
import com.android.database.TaxesHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.models.Address;
import com.android.emobilepos.models.Tax;
import com.android.emobilepos.models.realms.CustomerCustomField;
import com.android.support.Customer;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;
import com.crashlytics.android.Crashlytics;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

public class CreateCustomer_FA extends BaseFragmentActivityActionBar implements OnCheckedChangeListener, CompoundButton.OnCheckedChangeListener
        , OnClickListener {
    //    final int[] fieldID = new int[]{R.id.newCustfName, R.id.newCustlName, R.id.newCustCompany,
//            R.id.newCustEmail, R.id.newCustPhone, R.id.newCustContact, R.id.newCustBillStr1, R.id.newCustBillStr2, R.id.newCustBillCity,
//            R.id.newCustBillState, R.id.newCustBillZip, R.id.newCustShipStr1, R.id.newCustShipStr2, R.id.newCustShipCity,
//            R.id.newCustShipState, R.id.newCustShipZip, R.id.newCustDOB};
//    private final int CUST_ALIAS = 0, CUST_NAME = 1, CUST_LASTNAME = 2, COMPANY_NAME = 3, EMAIL = 4, PHONE = 5, CUST_CONTACT = 6,
//            B_STR1 = 7, B_STR2 = 8, B_CITY = 9, B_STATE = 10, B_ZIPCODE = 11, S_STR1 = 12, S_STR2 = 13, S_CITY = 14, S_STATE = 15,
//            S_ZIPCODE = 16, DOB = 17;
    private final int SPINNER_PRICELEVEL = 0, SPINNER_TAXES = 1, SPINNER_BILL_COUNTRY = 2, SPINNER_SHIP_COUNTRY = 3;
    //    private MyEditText[] field;
//    private String dobDate = "";
    private String addr_b_type = "Residential", addr_s_type = "Residential";
    private Spinner bCountrySpinner;
    private Spinner sCountrySpinner;
    // private Spinner taxesList;
    private List<Tax> taxList;
    private List<String[]> priceLevelList;
    private List<String> isoCountryList = new ArrayList<>(), nameCountryList = new ArrayList<>();
    private int taxSelected = 0;
    private int priceLevelSelected = 0;
    private int bSelectedCountry = 0;
    private int sSelectedCountry = 0;

    private DialogFragment newFrag;

    private Global global;
    private boolean hasBeenCreated = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_customer_layout);
        global = (Global) getApplication();

        setupSpinners();
        setupCountries();


//        int length = fieldID.length;
//        field = new MyEditText[length];

        // for i = 0 - 6 (Customer Information)
        // for i = 7 - 12 (Billing Address)
        // for i = 13 - 18 (Shipping Address)
//        for (int i = 0; i < length; i++) {
//            field[i] = (MyEditText) findViewById(fieldID[i]);
//        }

        CheckBox sameAddress = (CheckBox) findViewById(R.id.newCustCheckBox);
        sameAddress.setOnCheckedChangeListener(this);

        Button save = (Button) findViewById(R.id.newCustSaveBut);
        save.setOnClickListener(this);

        ((EditText) findViewById(R.id.newCustDOB)).setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
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
        if (global.isApplicationSentToBackground())
            Global.loggedIn = false;
        global.stopActivityTransitionTimer();

        if (hasBeenCreated && !Global.loggedIn) {
            if (global.getGlobalDlog() != null)
                global.getGlobalDlog().dismiss();
            global.promptForMandatoryLogin(this);
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        boolean isScreenOn = powerManager.isScreenOn();
        if (!isScreenOn)
            Global.loggedIn = false;
        global.startActivityTransitionTimer();
    }


    private OnItemSelectedListener getItemSelectedListener(final int spinnerType) {

        return new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // your code here
                switch (spinnerType) {
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
                switch (spinnerType) {
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
    }

    private void setupSpinners() {
        List<String> taxes = new ArrayList<>();
        List<String> priceLevel = new ArrayList<>();
        taxes.add("Select One");
        priceLevel.add("Select One");
        MyPreferences myPref = new MyPreferences(this);
        TaxesHandler handler = new TaxesHandler(this);
        taxList = handler.getTaxes(myPref.getPreferences(MyPreferences.pref_show_only_group_taxes));
        PriceLevelHandler handler2 = new PriceLevelHandler();
        priceLevelList = handler2.getPriceLevel();

        int size = taxList.size();
        int size2 = priceLevelList.size();
        int loopSize = size;
        if (size2 > size)
            loopSize = size2;
        for (int i = 0; i < loopSize; i++) {
            if (i < size)
                taxes.add(taxList.get(i).getTaxName());
            if (i < size2)
                priceLevel.add(priceLevelList.get(i)[0]);
        }

        List<String[]> taxArr = new ArrayList<>();
        for (Tax tax : taxList) {
            String[] arr = new String[5];
            arr[0] = tax.getTaxName();
            arr[1] = tax.getTaxId();
            arr[2] = tax.getTaxRate();
            arr[3] = tax.getTaxType();
            taxArr.add(arr);
        }
        CustomAdapter taxAdapter = new CustomAdapter(this, android.R.layout.simple_spinner_item, taxes, taxArr, true);
        CustomAdapter priceLevelAdapter = new CustomAdapter(this, android.R.layout.simple_spinner_item, priceLevel, priceLevelList, false);

        Spinner pricesList = (Spinner) findViewById(R.id.newCustList1);
        Spinner taxesList = (Spinner) findViewById(R.id.newCustList2);

        RadioGroup billingRadioGroup = (RadioGroup) findViewById(R.id.radioGroupBillingAddressType);
        billingRadioGroup.setOnCheckedChangeListener(this);
        RadioGroup shippingRadioGroup = (RadioGroup) findViewById(R.id.radioGroupShippingAddressType);
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

    private void setupCountries() {
        String[] isoCountries = Locale.getISOCountries();
        String[] nameCountries = new String[isoCountries.length];
        nameCountryList.add("Select One");
        isoCountryList.add("");
        int i = 0;
        MyPreferences myPref = new MyPreferences(this);
        String defaultCountry = myPref.getDefaultCountryCode();
        for (String country : isoCountries) {
            isoCountryList.add(country);
            Locale locale = new Locale(Locale.getDefault().getDisplayLanguage(), country);
            nameCountries[i] = locale.getDisplayCountry();
            nameCountryList.add(nameCountries[i]);
            if (defaultCountry.equals(country)) {
                bSelectedCountry = i + 1;
                sSelectedCountry = i + 1;
            }
            i++;
        }

        CountrySpinnerAdapter billingAdapter = new CountrySpinnerAdapter(this, android.R.layout.simple_spinner_item,
                this.nameCountryList, this.isoCountryList, true);
        CountrySpinnerAdapter shippingAdapter = new CountrySpinnerAdapter(this, android.R.layout.simple_spinner_item,
                this.nameCountryList, this.isoCountryList, false);

        bCountrySpinner.setAdapter(billingAdapter);
        sCountrySpinner.setAdapter(shippingAdapter);

        bCountrySpinner.setSelection(bSelectedCountry);
        sCountrySpinner.setSelection(sSelectedCountry);
    }

    private EditText getEditText(int id) {
        return (EditText) findViewById(id);
    }

    private void insertNewCustomer() {
        CustomersHandler custHandler = new CustomersHandler(this);
        AddressHandler addressHandler = new AddressHandler(this);
        SalesTaxCodesHandler taxCodeHandler = new SalesTaxCodesHandler(this);

        Customer custData = new Customer();
        Address addrData = new Address();
        MyPreferences myPref = new MyPreferences(this);

        String lastCustID = UUID.randomUUID().toString().toUpperCase(Locale.getDefault());
        custData.setCust_id(lastCustID);
        custData.setCust_name(String.format("%s %s", getEditText(R.id.newCustfName).getText().toString(),
                getEditText(R.id.newCustlName).getText().toString()));//field[CUST_ALIAS].getText().toString();
        custData.setCust_firstName(getEditText(R.id.newCustfName).getText().toString());
        custData.setCust_lastName(getEditText(R.id.newCustlName).getText().toString());
        custData.setCompanyName(getEditText(R.id.newCustCompany).getText().toString());
        custData.setCust_email(getEditText(R.id.newCustEmail).getText().toString());
        custData.setCust_phone(getEditText(R.id.newCustPhone).getText().toString());
        custData.setCust_contact(getEditText(R.id.newCustContact).getText().toString());
        custData.setQb_sync("0");
        custData.setCust_dob(getEditText(R.id.newCustDOB).getText().toString());
        if (priceLevelSelected > 0)
            custData.setPricelevel_id(priceLevelList.get(priceLevelSelected - 1)[1]);

        if (taxSelected > 0) {
            custData.setCust_salestaxcode(taxList.get(taxSelected - 1).getTaxId());
            custData.setCust_taxable(taxCodeHandler.getTaxableTaxCode());
            myPref.setCustTaxCode(custData.getCust_salestaxcode());
        }

        addrData.setAddr_id(UUID.randomUUID().toString());
        addrData.setCust_id(lastCustID);
        // add zone id

        // add addr_type
        addrData.setAddr_b_str1(getEditText(R.id.newCustBillStr1).getText().toString());
        addrData.setAddr_b_str2(getEditText(R.id.newCustBillStr2).getText().toString());
        // add addr_b_str3
        addrData.setAddr_b_city(getEditText(R.id.newCustBillCity).getText().toString());
        addrData.setAddr_b_state(getEditText(R.id.newCustBillState).getText().toString());
        if (bSelectedCountry > 0)
            addrData.setAddr_b_country(isoCountryList.get(bSelectedCountry));
        addrData.setAddr_b_zipcode(getEditText(R.id.newCustBillZip).getText().toString());

        // add addr_s_name
        addrData.setAddr_s_str1(getEditText(R.id.newCustShipStr1).getText().toString());
        addrData.setAddr_s_str2(getEditText(R.id.newCustShipStr2).getText().toString());
        // add addr_s_str3
        addrData.setAddr_s_city(getEditText(R.id.newCustShipCity).getText().toString());
        addrData.setAddr_s_state(getEditText(R.id.newCustShipState).getText().toString());
        if (sSelectedCountry > 0)
            addrData.setAddr_s_country(isoCountryList.get(sSelectedCountry));
        addrData.setAddr_s_zipcode(getEditText(R.id.newCustShipZip).getText().toString());

        addrData.setAddr_b_type(addr_b_type);
        addrData.setAddr_s_type(addr_s_type);
        // add qb_cust_id

        addressHandler.insertOneAddress(addrData);
        // }
        custHandler.insertOneCustomer(custData);// insert new customer to table

        CustomerCustomField customField = new CustomerCustomField();
        customField.setCustId(lastCustID);
        customField.setCustFieldId("EMS_CARD_ID_NUM");
        customField.setCustFieldName("ID");
        customField.setCustValue(((EditText) findViewById(R.id.giftCardNumber)).getText().toString());
        CustomerCustomFieldsDAO.upsert(customField);

        // Set-up data for default selection of this newly created customer
        HashMap<String, String> custMap = custHandler.getCustomerInfo(lastCustID);

        myPref.setCustID(custMap.get("cust_id"));
        myPref.setCustName(custMap.get("cust_name"));
        myPref.setCustPriceLevel(custData.getPricelevel_id());
        myPref.setCustSelected(true);
        myPref.setCustIDKey(custMap.get("custidkey"));
        myPref.setCustEmail(custMap.get("cust_email"));
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
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
        switch (v.getId()) {
            case R.id.newCustSaveBut:
//                int allWhitespaced = field[0].getText().toString().trim().length();

                if (getEditText(R.id.newCustfName).getText().toString().isEmpty()) {
                    getEditText(R.id.newCustfName).setBackgroundResource(R.drawable.edittext_wrong_input);
                    Global.showPrompt(this, R.string.dlog_title_error, getString(R.string.card_validation_error));
                } else if (getEditText(R.id.newCustlName).getText().toString().isEmpty()) {
                    getEditText(R.id.newCustlName).setBackgroundResource(R.drawable.edittext_wrong_input);
                    Global.showPrompt(this, R.string.dlog_title_error, getString(R.string.card_validation_error));
                } else if (!getEditText(R.id.newCustEmail).getText().toString().isEmpty() &&
                        !validEmail(getEditText(R.id.newCustEmail).getText().toString())) {
                    getEditText(R.id.newCustEmail).setBackgroundResource(R.drawable.edittext_wrong_input);
                    Global.showPrompt(this, R.string.dlog_title_error, getString(R.string.card_validation_error));
                } else {
                    getEditText(R.id.newCustEmail).setBackgroundResource(R.drawable.edittext_border);
                    insertNewCustomer();
                    setResult(-1);
                    finish();
                }
                break;
        }
    }

    private boolean validEmail(String paramString) {
        return Patterns.EMAIL_ADDRESS.matcher(paramString).matches();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            getEditText(R.id.newCustShipStr1).setText(getEditText(R.id.newCustBillStr1).getText().toString());
            getEditText(R.id.newCustShipStr2).setText(getEditText(R.id.newCustBillStr2).getText().toString());
            getEditText(R.id.newCustShipCity).setText(getEditText(R.id.newCustBillCity).getText().toString());
            getEditText(R.id.newCustShipState).setText(getEditText(R.id.newCustBillState).getText().toString());
            getEditText(R.id.newCustShipZip).setText(getEditText(R.id.newCustBillZip).getText().toString());

//            for (int i = B_STR1; i < B_ZIPCODE + 1; i++) {
//                field[i + 5].setText(field[i].getText().toString());
//            }
            sCountrySpinner.setSelection(bSelectedCountry);
        } else {
            getEditText(R.id.newCustShipStr1).setText("");
            getEditText(R.id.newCustShipStr2).setText("");
            getEditText(R.id.newCustShipCity).setText("");
            getEditText(R.id.newCustShipState).setText("");
            getEditText(R.id.newCustShipZip).setText("");
//            for (int i = B_STR1; i < B_ZIPCODE + 1; i++) {
//                field[i + 5].setText("");
//            }
        }
    }

    public static class DateDialog extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @NonNull
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

            String dobDate = "";
            try {

                dobDate = sdf2.format(sdf1.parse(sb.toString()));
            } catch (ParseException e) {
                e.printStackTrace();
                Crashlytics.logException(e);
            }
            ((EditText) getActivity().findViewById(R.id.newCustDOB)).setText(Global.formatToDisplayDate(dobDate, 1));

        }
    }

    private class CustomAdapter extends ArrayAdapter<String> {
        List<String> leftData = null;
        List<String[]> rightData = null;
        boolean isTax = false;
        private Activity context;

        CustomAdapter(Activity activity, int resource, List<String> left, List<String[]> right, boolean isTax) {
            super(activity, resource, left);
            this.context = activity;
            this.leftData = left;
            this.rightData = right;
            this.isTax = isTax;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);

            // we know that simple_spinner_item has android.R.id.text1 TextView:

            TextView text = (TextView) view.findViewById(android.R.id.text1);
            text.setTextColor(Color.BLACK);// choose your color
            text.setPadding(35, 0, 0, 0);
            return view;
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

    private class CountrySpinnerAdapter extends ArrayAdapter<String> {
        List<String> leftData = null;
        List<String> rightData = null;
        boolean isBilling = false;
        private Activity context;

        CountrySpinnerAdapter(Activity activity, int resource, List<String> left, List<String> right, boolean isBilling) {
            super(activity, resource, left);
            this.context = activity;
            this.leftData = left;
            this.rightData = right;
            this.isBilling = isBilling;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);

            // we know that simple_spinner_item has android.R.id.text1 TextView:

            TextView text = (TextView) view.findViewById(android.R.id.text1);
            text.setTextColor(Color.BLACK);// choose your color
            text.setPadding(35, 0, 0, 0);
            return view;
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
                    checked.setVisibility(View.VISIBLE);
                    break;
                }
                case 2: {
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

}
