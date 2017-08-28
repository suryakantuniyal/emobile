package com.android.emobilepos.customer;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;

import com.android.dao.CustomerCustomFieldsDAO;
import com.android.database.CustomersHandler;
import com.android.database.PriceLevelHandler;
import com.android.database.TaxesHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.adapters.CountrySpinnerAdapter;
import com.android.emobilepos.models.Country;
import com.android.emobilepos.models.Tax;
import com.android.emobilepos.models.realms.CustomerCustomField;
import com.android.support.Customer;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ViewCustomerDetails_FA extends BaseFragmentActivityActionBar implements AdapterView.OnItemSelectedListener, RadioGroup.OnCheckedChangeListener, View.OnClickListener {

    private final int SPINNER_PRICELEVEL = 0, SPINNER_TAXES = 1;
    private Global global;
    private boolean hasBeenCreated = false;
    private Activity activity;
    private String cust_id;
    private List<CustomerCustomField> customFields;
    private Customer customer;
    private ArrayList<Country> countries;
    private Spinner billingCountrySpinner;
    private Spinner shippingCountrySpinner;
    public int billingSelectedCountry;
    public int shippingSelectedCountry;
    private int taxSelected;
    private int priceLevelSelected;
    private TextView customerNameTextView;
    private TextView contacTextView;
    private TextView phoneTextView;
    private TextView companyTextView;
    private TextView balanceTextView;
    private TextView limitTextView;
    private TextView taxableTextView;
    private TextView taxidTextView;
    private TextView emailTextView;
    boolean isCustomerEdit = false;
    private TextView billingStr1;
    private TextView billingStr2;
    private TextView billingCity;
    private TextView billingState;
    private TextView billingZip;
    private TextView shippingStr1;
    private TextView shippingStr2;
    private TextView shippingCity;
    private TextView shippingState;
    private TextView shippingZip;
    private Spinner pricesList;
    private Spinner taxesList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customer_moreinfo_layout);
        activity = this;
        global = (Global) getApplication();
        Bundle extras = getIntent().getExtras();
        CustomersHandler custHandler = new CustomersHandler(this);
        if (extras.containsKey("cust_id")) {
            isCustomerEdit = true;
            cust_id = extras.getString("cust_id");
            customFields = CustomerCustomFieldsDAO.getCustomFields(cust_id);
            customer = custHandler.getCustomer(cust_id);
        } else {
            isCustomerEdit = false;
            customer = new Customer();
        }

        hasBeenCreated = true;
        contacTextView = (TextView) findViewById(R.id.customerContacttextView342);
        phoneTextView = ((TextView) findViewById(R.id.customerPhonetextView343));
        companyTextView = ((TextView) findViewById(R.id.customerCompanytextView34));
        balanceTextView = ((TextView) findViewById(R.id.customerBalancetextView371));
        limitTextView = ((TextView) findViewById(R.id.customerLimittextView372));
        taxableTextView = ((TextView) findViewById(R.id.customerTaxabletextView373));
        taxidTextView = ((TextView) findViewById(R.id.customerTaxIdtextView37));
        emailTextView = ((TextView) findViewById(R.id.customerEmailtextView344));

        billingStr1 = (TextView) findViewById(R.id.newCustBillStr1);
        billingStr2 = ((TextView) findViewById(R.id.newCustBillStr2));
        billingCity = ((TextView) findViewById(R.id.newCustBillCity));
        billingState = ((TextView) findViewById(R.id.newCustBillState));
        billingZip = ((TextView) findViewById(R.id.newCustBillZip));
        shippingStr1 = ((TextView) findViewById(R.id.newCustShippingStr1));
        shippingStr2 = ((TextView) findViewById(R.id.newCustShippingStr2));
        shippingCity = ((TextView) findViewById(R.id.newCustShippingCity));
        shippingState = (TextView) findViewById(R.id.newCustShippingState);
        shippingZip = ((TextView) findViewById(R.id.newCustShippingZip));
        customerNameTextView = (TextView) findViewById(R.id.customerNametextView341);
        setUI();
        setupCountries();
        setupSpinners();
        if (isCustomerEdit) {
            disableFields();
        }
    }

    private void setUI() {
        if (isCustomerEdit) {
            List<CustomerCustomField> customFields = CustomerCustomFieldsDAO.getCustomFields(customer.getCust_id());
            ((TextView) findViewById(R.id.customerNametextView341)).setText(String.format("%s %s %s", customer.getCust_firstName(), customer.getCust_middleName(), customer.getCust_lastName()));
            contacTextView.setText(customer.getCust_contact());
            phoneTextView.setText(customer.getCust_phone());
            companyTextView.setText(customer.getCompanyName());
            balanceTextView.setText(customer.getCust_balance());
            limitTextView.setText(customer.getCust_limit());
            taxableTextView.setText(customer.getCust_taxable());
            taxidTextView.setText(customer.getCust_salestaxcode());
            emailTextView.setText(customer.getCust_email());
            billingStr1.setText(customer.getBillingAddress().getAddr_b_str1());
            billingStr2.setText(customer.getBillingAddress().getAddr_b_str2());
            billingCity.setText(customer.getBillingAddress().getAddr_b_city());
            billingState.setText(customer.getBillingAddress().getAddr_b_state());
            billingZip.setText(customer.getBillingAddress().getAddr_b_zipcode());

            shippingStr1.setText(customer.getBillingAddress().getAddr_s_str1());
            shippingStr2.setText(customer.getBillingAddress().getAddr_s_str2());
            shippingCity.setText(customer.getBillingAddress().getAddr_s_city());
            shippingState.setText(customer.getBillingAddress().getAddr_s_state());
            shippingZip.setText(customer.getBillingAddress().getAddr_s_zipcode());

            TableLayout tableLayout = (TableLayout) findViewById(R.id.customerFinancialInfoTableLayout);
            for (CustomerCustomField field : customFields) {
                View row = View.inflate(this, R.layout.customercustomfields_tablerow_layout, null);
                row.setTag(field);
                ((TextView) row.findViewById(R.id.customerCustomFieldLabelTextView)).setText(field.getCustFieldName());
                ((EditText) row.findViewById(R.id.customerCustomFieldValueEditText)).setText(field.getCustValue());
                tableLayout.addView(row);
            }

            billingCountrySpinner = (Spinner) findViewById(R.id.newCustBillCountry);
            shippingCountrySpinner = (Spinner) findViewById(R.id.newCustShippingCountry);
        }
        findViewById(R.id.btnSaveCustomer).setOnClickListener(this);

    }

    private void disableFields() {
        customerNameTextView.setEnabled(!isCustomerEdit);
        contacTextView.setEnabled(!isCustomerEdit);
        phoneTextView.setEnabled(!isCustomerEdit);
        emailTextView.setEnabled(!isCustomerEdit);
        companyTextView.setEnabled(!isCustomerEdit);
        balanceTextView.setEnabled(!isCustomerEdit);
        limitTextView.setEnabled(!isCustomerEdit);
        taxableTextView.setEnabled(!isCustomerEdit);
        taxidTextView.setEnabled(!isCustomerEdit);
        pricesList.setEnabled(!isCustomerEdit);
        billingStr1.setEnabled(!isCustomerEdit);
        billingStr2.setEnabled(!isCustomerEdit);
        billingCity.setEnabled(!isCustomerEdit);
        billingState.setEnabled(!isCustomerEdit);
        billingZip.setEnabled(!isCustomerEdit);
        billingCountrySpinner.setEnabled(!isCustomerEdit);
        shippingStr1.setEnabled(!isCustomerEdit);
        shippingStr2.setEnabled(!isCustomerEdit);
        shippingCity.setEnabled(!isCustomerEdit);
        shippingState.setEnabled(!isCustomerEdit);
        shippingZip.setEnabled(!isCustomerEdit);
        shippingCountrySpinner.setEnabled(!isCustomerEdit);
        taxesList.setEnabled(!isCustomerEdit);

    }

    private void setupSpinners() {
        List<String> taxes = new ArrayList<>();
        List<String> priceLevel = new ArrayList<>();
        taxes.add("Select One");
        priceLevel.add("Select One");

        TaxesHandler handler = new TaxesHandler(this);
        List<Tax> taxList = handler.getTaxes();
        PriceLevelHandler handler2 = new PriceLevelHandler();
        List<String[]> priceLevelList = handler2.getPriceLevel();

//        int size = taxList.size();
//        int size2 = priceLevelList.size();
//        int loopSize = size;
//        if (size2 > size)
//            loopSize = size2;
//        for (int i = 0; i < loopSize; i++) {
////            if (i < size)
////                taxes.add(taxList.get(i).getTaxName());
//            if (i < size2)
//                priceLevel.add(priceLevelList.get(i)[0]);
//        }

        int i = 0;
        for (String[] strings : priceLevelList) {
            priceLevel.add(strings[0]);
            if (customer.getPricelevel_id().equalsIgnoreCase(strings[1])) {
                priceLevelSelected = i;
            }
            i++;
        }
        i = 0;
        List<String[]> taxArr = new ArrayList<>();
        for (Tax tax : taxList) {
            if (customer.getCust_salestaxcode().equalsIgnoreCase(tax.getTaxId())) {
                taxSelected = i;
            }
            i++;
            taxes.add(tax.getTaxName());
            String[] arr = new String[5];
            arr[0] = tax.getTaxName();
            arr[1] = tax.getTaxId();
            arr[2] = tax.getTaxRate();
            arr[3] = tax.getTaxType();
            taxArr.add(arr);
        }
        CustomAdapter taxAdapter = new CustomAdapter(this, android.R.layout.simple_spinner_item, taxes, taxArr, true);
        CustomAdapter priceLevelAdapter = new CustomAdapter(this, android.R.layout.simple_spinner_item, priceLevel, priceLevelList, false);

        pricesList = (Spinner) findViewById(R.id.newCustList1);
        taxesList = (Spinner) findViewById(R.id.newCustList2);

        RadioGroup billingRadioGroup = (RadioGroup) findViewById(R.id.radioGroupBillingAddressType);
        billingRadioGroup.setOnCheckedChangeListener(this);
        RadioGroup shippingRadioGroup = (RadioGroup) findViewById(R.id.radioGroupShippingAddressType);
        shippingRadioGroup.setOnCheckedChangeListener(this);

        taxesList.setAdapter(taxAdapter);
        taxesList.setOnItemSelectedListener(getItemSelectedListener(SPINNER_TAXES));

        pricesList.setAdapter(priceLevelAdapter);
        pricesList.setOnItemSelectedListener(getItemSelectedListener(SPINNER_PRICELEVEL));

        pricesList.setSelection(priceLevelSelected);
        taxesList.setSelection(taxSelected);
    }

    private void setupCountries() {
        countries = new ArrayList<>();
        String[] isoCountries = Locale.getISOCountries();
        String[] nameCountries = new String[isoCountries.length];
        countries.add(new Country("Select One", "", false));
        int i = 0;
        MyPreferences myPref = new MyPreferences(this);
        String defaultCountry = myPref.getDefaultCountryCode();
        for (String country : isoCountries) {
            Country c = new Country();
            c.setIsoCode(country);
            Locale locale = new Locale(Locale.getDefault().getDisplayLanguage(), country);
            nameCountries[i] = locale.getDisplayCountry();
            c.setName(nameCountries[i]);
            if (defaultCountry.equals(country)) {
                billingSelectedCountry = i + 1;
                shippingSelectedCountry = i + 1;
                c.setDefaultCountry(true);
            }
            i++;
            countries.add(c);
        }

        CountrySpinnerAdapter billingAdapter = new CountrySpinnerAdapter(this, countries);
        CountrySpinnerAdapter shippingAdapter = new CountrySpinnerAdapter(this, countries);

        billingCountrySpinner.setAdapter(billingAdapter);
        shippingCountrySpinner.setAdapter(shippingAdapter);
        billingCountrySpinner.setOnItemSelectedListener(this);
        shippingCountrySpinner.setOnItemSelectedListener(this);
        billingCountrySpinner.setSelection(billingSelectedCountry);
        shippingCountrySpinner.setSelection(shippingSelectedCountry);
    }

    private AdapterView.OnItemSelectedListener getItemSelectedListener(final int spinnerType) {

        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                switch (spinnerType) {
                    case SPINNER_PRICELEVEL:
                        priceLevelSelected = position;
                        break;
                    case SPINNER_TAXES:
                        taxSelected = position;
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                switch (spinnerType) {
                    case SPINNER_PRICELEVEL:
                        priceLevelSelected = 0;
                        break;
                    case SPINNER_TAXES:
                        taxSelected = 0;
                        break;
                }
            }
        };
    }

    @Override
    public void onResume() {

        if (global.isApplicationSentToBackground())
            Global.loggedIn = false;
        global.stopActivityTransitionTimer();

        if (hasBeenCreated && !Global.loggedIn) {
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
            Global.loggedIn = false;
        global.startActivityTransitionTimer();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.newCustBillCountry:
                billingSelectedCountry = position;
                billingCountrySpinner.setSelection(position);
                break;
            case R.id.newCustShippingCountry:
                shippingSelectedCountry = position;
                shippingCountrySpinner.setSelection(position);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSaveCustomer:
                saveCustomer();
                break;
        }
    }

    private void saveCustomer() {
        customer.setCust_phone(phoneTextView.getText().toString());
        customer.setCust_email(emailTextView.getText().toString());
        customer.setCust_contact(contacTextView.getText().toString());
        customer.setCust_limit(limitTextView.getText().toString());
        customer.setCompanyName(companyTextView.getText().toString());

        CustomersHandler handler = new CustomersHandler(this);
        handler.insertOneCustomer(customer);
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
            TextView text = (TextView) view.findViewById(android.R.id.text1);
            text.setTextColor(Color.BLACK);
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
}
