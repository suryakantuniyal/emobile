package com.android.emobilepos.customer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.dao.CustomerBiometricDAO;
import com.android.dao.CustomerCustomFieldsDAO;
import com.android.database.CustomersHandler;
import com.android.database.PriceLevelHandler;
import com.android.database.TaxesHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.adapters.CountrySpinnerAdapter;
import com.android.emobilepos.models.Country;
import com.android.emobilepos.models.Tax;
import com.android.emobilepos.models.realms.CustomerBiometric;
import com.android.emobilepos.models.realms.CustomerCustomField;
import com.android.emobilepos.models.realms.CustomerFid;
import com.android.support.Customer;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;
import com.crashlytics.android.Crashlytics;
import com.digitalpersona.uareu.Engine;
import com.digitalpersona.uareu.Fid;
import com.digitalpersona.uareu.Fmd;
import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.ReaderCollection;
import com.digitalpersona.uareu.UareUException;
import com.digitalpersona.uareu.UareUGlobal;

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
    Button fingerLeft1;
    Button fingerLeft2;
    Button fingerLeft3;
    Button fingerLeft4;
    Button fingerRight1;
    Button fingerRight2;
    Button fingerRight3;
    Button fingerRight4;
    EditText cardIdEditText;
    private Reader reader;
    private static final String ACTION_USB_PERMISSION = "com.digitalpersona.uareu.dpfpddusbhost.USB_PERMISSION";
    private Engine engine;
    private int dpi;
    private int m_current_fmds_count;
    private boolean m_reset;
    private Reader.CaptureResult cap_result;
    private String m_enginError;
    private String m_text_conclusionString;
    private Fmd m_enrollment_fmd;
    private boolean m_first;
    private boolean m_success;
    private int m_templateSize;
    private String m_textString;
    private Engine.EnrollmentCallback enrollThread;
    CustomerBiometric biometric = new CustomerBiometric();
    public static final String QualityToString(Reader.CaptureResult result) {
        if (result == null) {
            return "";
        }
        if (result.quality == null) {
            return "An error occurred";
        }
        switch (result.quality) {
            case FAKE_FINGER:
                return "Fake finger";
            case NO_FINGER:
                return "No finger";
            case CANCELED:
                return "Capture cancelled";
            case TIMED_OUT:
                return "Capture timed out";
            case FINGER_TOO_LEFT:
                return "Finger too left";
            case FINGER_TOO_RIGHT:
                return "Finger too right";
            case FINGER_TOO_HIGH:
                return "Finger too high";
            case FINGER_TOO_LOW:
                return "Finger too low";
            case FINGER_OFF_CENTER:
                return "Finger off center";
            case SCAN_SKEWED:
                return "Scan skewed";
            case SCAN_TOO_SHORT:
                return "Scan too short";
            case SCAN_TOO_LONG:
                return "Scan too long";
            case SCAN_TOO_SLOW:
                return "Scan too slow";
            case SCAN_TOO_FAST:
                return "Scan too fast";
            case SCAN_WRONG_DIRECTION:
                return "Wrong direction";
            case READER_DIRTY:
                return "Reader dirty";
            case GOOD:
                return "";
            default:
                return "An error occurred";
        }
    }

    public enum Finger {
        FINGER_ONE_LEFT(0), FINGER_TWO_LEFT(1), FINGER_THREE_LEFT(2), FINGER_FOUR_LEFT(3),
        FINGER_ONE_RIGHT(4), FINGER_TWO_RIGHT(5), FINGER_THREE_RIGHT(6), FINGER_FOUR_RIGHT(7);

        private int code;

        Finger(int code) {
            this.code = code;
        }

        public static Finger getByCode(int code) {
            switch (code) {
                case 0:
                    return FINGER_ONE_LEFT;
                case 1:
                    return FINGER_TWO_LEFT;
                case 2:
                    return FINGER_THREE_LEFT;
                case 3:
                    return FINGER_FOUR_LEFT;
                case 4:
                    return FINGER_ONE_RIGHT;
                case 5:
                    return FINGER_TWO_RIGHT;
                case 6:
                    return FINGER_THREE_RIGHT;
                case 7:
                    return FINGER_FOUR_RIGHT;
            }
            return null;
        }

        public int getCode() {
            return code;
        }
    }

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

        fingerLeft1 = (Button) findViewById(R.id.fingerOneLeftbutton6);
        fingerLeft2 = (Button) findViewById(R.id.fingerTwoLeftbutton5);
        fingerLeft3 = (Button) findViewById(R.id.fingerThreeLeftbutton4);
        fingerLeft4 = (Button) findViewById(R.id.fingerFourLeftbutton3);
        fingerRight1 = (Button) findViewById(R.id.fingerOneRightbutton6);
        fingerRight2 = (Button) findViewById(R.id.fingerTwoRightbutton5);
        fingerRight3 = (Button) findViewById(R.id.fingerThreeRightbutton4);
        fingerRight4 = (Button) findViewById(R.id.fingerFourRightbutton3);
        fingerLeft1.setOnClickListener(this);
        fingerLeft2.setOnClickListener(this);
        fingerLeft3.setOnClickListener(this);
        fingerLeft4.setOnClickListener(this);
        fingerRight1.setOnClickListener(this);
        fingerRight2.setOnClickListener(this);
        fingerRight3.setOnClickListener(this);
        fingerRight4.setOnClickListener(this);
        customerNameTextView = (TextView) findViewById(R.id.customerNametextView341);
        setUI();
        setupCountries();
        setupSpinners();
        if (isCustomerEdit) {
            disableFields();
        }
        loadFingerPrintReader(this);
    }

    private void loadFingerPrintReader(Context context) {
        ReaderCollection readers;
        try {
            readers = UareUGlobal.GetReaderCollection(context);
            readers.GetReaders();
            if (readers.size() > 0) {
                this.reader = readers.get(0);
            }
        } catch (UareUException e) {
            Crashlytics.logException(e);
            e.printStackTrace();
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
                if (field.getCustFieldId().equalsIgnoreCase("EMS_CARD_ID_NUM")) {
                    cardIdEditText = ((EditText) row.findViewById(R.id.customerCustomFieldValueEditText));
                }
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
                saveBiometrics();
                break;
            case R.id.fingerOneLeftbutton6:
                showFingerPrintScanner(Finger.FINGER_ONE_LEFT);
                break;
            case R.id.fingerTwoLeftbutton5:
                showFingerPrintScanner(Finger.FINGER_TWO_LEFT);
                break;
            case R.id.fingerThreeLeftbutton4:
                showFingerPrintScanner(Finger.FINGER_THREE_LEFT);
                break;
            case R.id.fingerFourLeftbutton3:
                showFingerPrintScanner(Finger.FINGER_FOUR_LEFT);
                break;
            case R.id.fingerOneRightbutton6:
                showFingerPrintScanner(Finger.FINGER_ONE_RIGHT);
                break;
            case R.id.fingerTwoRightbutton5:
                showFingerPrintScanner(Finger.FINGER_TWO_RIGHT);
                break;
            case R.id.fingerThreeRightbutton4:
                showFingerPrintScanner(Finger.FINGER_THREE_RIGHT);
                break;
            case R.id.fingerFourRightbutton3:
                showFingerPrintScanner(Finger.FINGER_FOUR_RIGHT);
                break;

        }
    }

    private void saveBiometrics() {
        CustomerBiometricDAO.delete(cust_id);
        CustomerBiometricDAO.upsert(biometric);
    }

    private void showFingerPrintScanner(final Finger finger) {
        try {
            reader.Open(Reader.Priority.EXCLUSIVE);
            dpi = GetFirstDPI(reader);
            engine = UareUGlobal.GetEngine();
            m_reset = false;
            // loop capture on a separate thread to avoid freezing the UI
            new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        m_current_fmds_count = 0;
                        m_reset = false;
                        enrollThread = new EnrollmentCallback(reader, engine, finger);
                        while (!m_reset) {
                            try {
                                m_enrollment_fmd = engine.CreateEnrollmentFmd(Fmd.Format.ANSI_378_2004, enrollThread);
                                if (m_success = (m_enrollment_fmd != null)) {
                                    m_templateSize = m_enrollment_fmd.getData().length;
                                    m_current_fmds_count = 0;    // reset count on success
                                }
                            } catch (Exception e) {
                                // template creation failed, reset count
                                m_current_fmds_count = 0;
                            }
                        }
                        reader.Close();
                        ViewCustomerDetails_FA.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setFingerPrintUI();
                            }
                        });
                    } catch (Exception e) {
                        if (!m_reset) {
                            Log.w("UareUSampleJava", "error during capture");
                            onBackPressed();
                        }
                    }
                }
            }).start();
        } catch (UareUException e) {
            e.printStackTrace();
        }
    }

    private void setFingerPrintUI() {
        for (CustomerFid fid : biometric.getFids()) {
            Finger finger = Finger.getByCode(fid.getFingerCode());
            switch (finger) {
                case FINGER_ONE_LEFT:
                    fingerLeft1.setBackgroundColor(Color.GREEN);
                    break;
                case FINGER_TWO_LEFT:
                    fingerLeft2.setBackgroundColor(Color.GREEN);
                    break;
                case FINGER_THREE_LEFT:
                    fingerLeft3.setBackgroundColor(Color.GREEN);
                    break;
                case FINGER_FOUR_LEFT:
                    fingerLeft4.setBackgroundColor(Color.GREEN);
                    break;
                case FINGER_ONE_RIGHT:
                    fingerRight1.setBackgroundColor(Color.GREEN);
                    break;
                case FINGER_TWO_RIGHT:
                    fingerRight2.setBackgroundColor(Color.GREEN);
                    break;
                case FINGER_THREE_RIGHT:
                    fingerRight3.setBackgroundColor(Color.GREEN);
                    break;
                case FINGER_FOUR_RIGHT:
                    fingerRight4.setBackgroundColor(Color.GREEN);
                    break;
            }
        }
    }

    public static int GetFirstDPI(Reader reader) {
        Reader.Capabilities caps = reader.GetCapabilities();
        return caps.resolutions[0];
    }

    private void saveCustomer() {
        String cardNumber = cardIdEditText == null ? "" : cardIdEditText.getText().toString();
        CustomerCustomField customField = CustomerCustomFieldsDAO.findEMWSCardIdByCustomerId(cust_id);
        if (customField == null) {
            customField = new CustomerCustomField();
        }
        customField.setCustId(cust_id);
        customField.setCustFieldId("EMS_CARD_ID_NUM");
        customField.setCustFieldName("ID");
        customField.setCustValue(cardNumber);
        CustomerCustomFieldsDAO.upsert(customField);
        CustomersHandler handler = new CustomersHandler(ViewCustomerDetails_FA.this);
        handler.updateSyncStatus(cust_id, false);
        customFields = CustomerCustomFieldsDAO.getCustomFields(cust_id);
        Toast.makeText(this, R.string.information_saved, Toast.LENGTH_LONG).show();

//        customer.setCust_phone(phoneTextView.getText().toString());
//        customer.setCust_email(emailTextView.getText().toString());
//        customer.setCust_contact(contacTextView.getText().toString());
//        customer.setCust_limit(limitTextView.getText().toString());
//        customer.setCompanyName(companyTextView.getText().toString());
//        CustomersHandler handler = new CustomersHandler(this);
//        handler.insertOneCustomer(customer);
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

    public class EnrollmentCallback
            extends Thread
            implements Engine.EnrollmentCallback {
        public int m_current_index = 0;

        private Reader m_reader = null;
        private Engine m_engine = null;
        private Finger finger;

        public EnrollmentCallback(Reader reader, Engine engine, Finger finger) {
            m_reader = reader;
            m_engine = engine;
            this.finger = finger;
        }

        // callback function is called by dp sdk to retrieve fmds until a null is returned
        @Override
        public Engine.PreEnrollmentFmd GetFmd(Fmd.Format format) {
            Engine.PreEnrollmentFmd result = null;
            while (!m_reset) {
                try {
                    cap_result = m_reader.Capture(Fid.Format.ANSI_381_2004, Reader.ImageProcessing.IMG_PROC_DEFAULT, dpi, -1);
                } catch (Exception e) {
                    Log.w("UareUSampleJava", "error during capture: " + e.toString());
                }

                // an error occurred
                if (cap_result == null || cap_result.image == null) continue;
                try {
                    m_enginError = "";
                    Engine.PreEnrollmentFmd prefmd = new Engine.PreEnrollmentFmd();

                    prefmd.fmd = m_engine.CreateFmd(cap_result.image, Fmd.Format.ANSI_378_2004);
//                    Fmd fmd = m_engine.CreateFmd(cap_result.image.getData(), cap_result.image.getViews()[0].getWidth(), cap_result.image.getViews()[0].getHeight(),
//                            cap_result.image.getViews()[0].getQuality(), cap_result.image.getViews()[0].getFingerPosition(), cap_result.image.getCbeffId(), Fmd.Format.ANSI_378_2004);
                    prefmd.view_index = 0;
                    m_current_fmds_count++;


                    result = prefmd;
                    break;
                } catch (Exception e) {
                    m_enginError = e.toString();
                    Log.w("UareUSampleJava", "Engine error: " + e.toString());
                }
            }

            m_text_conclusionString = QualityToString(cap_result);

            if (!m_enginError.isEmpty()) {
                m_text_conclusionString = "Engine: " + m_enginError;
            }

            if (m_enrollment_fmd != null || m_current_fmds_count == 0) {
                if (!m_first) {
                    if (m_text_conclusionString.length() == 0) {
                        try {
                            Fid importFid = UareUGlobal.GetImporter().ImportFid(cap_result.image.getData(), Fid.Format.ANSI_381_2004);
                            Fmd importFmd = UareUGlobal.GetImporter().ImportFmd(m_enrollment_fmd.getData(), Fmd.Format.ANSI_378_2004, Fmd.Format.ANSI_378_2004);
//                            Fmd fmd = m_engine.CreateFmd(m_enrollment_fmd.getData(), cap_result.image.getViews()[0].getWidth(), cap_result.image.getViews()[0].getHeight(),
//                                    cap_result.image.getViews()[0].getQuality(), cap_result.image.getViews()[0].getFingerPosition(),
//                                    cap_result.image.getCbeffId(), Fmd.Format.ANSI_378_2004);
                            m_reset = true;
                            CustomerFid customerFid = new CustomerFid(cap_result.image, finger);
                            biometric.setCustomerId(cust_id);
                            biometric.getFids().add(customerFid);
//                            biometric.setFingerFid(finger, cap_result.image);
                        } catch (UareUException e) {

                        }
                        m_text_conclusionString = m_success ? "Enrollment template created, size: " + m_templateSize : "Enrollment template failed. Please try again";
                    }
                }
                m_textString = "Place any finger on the reader";
                m_enrollment_fmd = null;
            } else {
                m_first = false;
                m_success = false;
                m_textString = "Continue to place the same finger on the reader";
            }


            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                }
            });

            return result;
        }
    }
}
