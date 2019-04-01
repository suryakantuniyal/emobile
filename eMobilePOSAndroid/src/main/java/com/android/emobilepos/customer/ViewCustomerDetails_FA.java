package com.android.emobilepos.customer;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.usb.UsbDevice;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.dao.CustomerCustomFieldsDAO;
import com.android.dao.EmobileBiometricDAO;
import com.android.database.AddressHandler;
import com.android.database.CustomersHandler;
import com.android.database.PriceLevelHandler;
import com.android.database.SalesTaxCodesHandler;
import com.android.database.TaxesHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.adapters.CountrySpinnerAdapter;
import com.android.emobilepos.models.Address;
import com.android.emobilepos.models.Country;
import com.android.emobilepos.models.Tax;
import com.android.emobilepos.models.realms.BiometricFid;
import com.android.emobilepos.models.realms.CustomerCustomField;
import com.android.emobilepos.models.realms.EmobileBiometric;
import com.android.support.Customer;
import com.android.support.DeviceUtils;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;
import com.crashlytics.android.Crashlytics;
import com.digitalpersona.uareu.Engine;
import com.digitalpersona.uareu.Fid;
import com.digitalpersona.uareu.Fmd;
import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.Reader.ReaderStatus;
import com.digitalpersona.uareu.ReaderCollection;
import com.digitalpersona.uareu.UareUException;
import com.digitalpersona.uareu.UareUGlobal;
import com.digitalpersona.uareu.dpfpddusbhost.DPFPDDUsbException;
import com.digitalpersona.uareu.dpfpddusbhost.DPFPDDUsbHost;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

import static com.android.emobilepos.R.id.fingerPrintimageView;
import static com.android.emobilepos.R.id.unregisterFingerprintbutton2;

public class ViewCustomerDetails_FA extends BaseFragmentActivityActionBar implements AdapterView.OnItemSelectedListener, RadioGroup.OnCheckedChangeListener, View.OnClickListener {

    public static final String ACTION_USB_PERMISSION = "com.digitalpersona.uareu.dpfpddusbhost.USB_PERMISSION";
    private final int SPINNER_PRICELEVEL = 0, SPINNER_TAXES = 1;
    public int billingSelectedCountry;
    public int shippingSelectedCountry;
    boolean isCustomerEdit = false;
    Button fingerLeft1;
    Button fingerLeft2;
    Button fingerLeft3;
    Button fingerLeft4;
    Button fingerRight1;
    Button fingerRight2;
    Button fingerRight3;
    Button fingerRight4;
    EditText cardIdEditText;
    EmobileBiometric biometric = new EmobileBiometric();
    TextView fingerPrintScanningNotesTextView;
    Handler handler;
    List<String> taxes = new ArrayList<>();
    List<String> priceLevel = new ArrayList<>();
    RadioButton radioBillingResidential;
    RadioButton radioBillingBusiness;
    RadioButton radioShippingResidential;
    RadioButton radioShippingBusiness;
    private Global global;
    private boolean hasBeenCreated = false;
    private Activity activity;
    private String cust_id;
    private Customer customer;
    private ArrayList<Country> countries;
    private Spinner billingCountrySpinner;
    private Spinner shippingCountrySpinner;
    private int taxSelected;
    private int priceLevelSelected;
    private TextView customerNameTextView;
    private TextView customerLastNameTextView;
    private TextView customerDOBTextView;
    private TextView contacTextView;
    private TextView phoneTextView;
    private TextView companyTextView;
    private TextView balanceTextView;
    private TextView limitTextView;
    private TextView taxableTextView;
    private TextView taxidTextView;
    private TextView emailTextView;
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
    private Reader reader;
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
    private Engine.EnrollmentCallback enrollThread;
    private ProgressBar progressBar;
    private int progress;
    private ImageView fingerPrintimage;
    private MyPreferences preferences;
    private boolean isReaderConnected = false;
    private List<String[]> priceLevelList;
    private List<Tax> taxList;
    private String[] isoCountries;
    private String addr_b_type = "Business";
    private String addr_s_type = "Residential";
    private DateDialog newFrag;
    private RadioGroup billingRadioGroup;
    private RadioGroup shippingRadioGroup;
    private MyPreferences myPref;

    public static String QualityToString(Reader.CaptureResult result) {
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

    public static int GetFirstDPI(Reader reader) {
        Reader.Capabilities caps = reader.GetCapabilities();
        return caps.resolutions[0];
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customer_moreinfo_layout);
        activity = this;
        global = (Global) getApplication();
        setHandler();
        myPref = new MyPreferences(this);
        preferences = new MyPreferences(this);
        Collection<UsbDevice> usbDevices = DeviceUtils.getUSBDevices(this);
        isReaderConnected = usbDevices.size() > 0;
        Bundle extras = getIntent().getExtras();
        CustomersHandler custHandler = new CustomersHandler(this);
        if (extras != null && extras.containsKey("cust_id")) {
            isCustomerEdit = true;
            cust_id = extras.getString("cust_id");
//            customFields = CustomerCustomFieldsDAO.getCustomFields(cust_id);
            customer = custHandler.getCustomer(cust_id);
        } else {
            isCustomerEdit = false;
            cust_id = UUID.randomUUID().toString().toUpperCase(Locale.getDefault());
            customer = new Customer();
            customer.setCust_id(cust_id);
        }

        hasBeenCreated = true;
        contacTextView = findViewById(R.id.customerContacttextView342);
        phoneTextView = findViewById(R.id.customerPhonetextView343);
        companyTextView = findViewById(R.id.customerCompanytextView34);
        balanceTextView = findViewById(R.id.customerBalancetextView371);
        limitTextView = findViewById(R.id.customerLimittextView372);
        taxableTextView = findViewById(R.id.customerTaxabletextView373);
        taxidTextView = findViewById(R.id.customerTaxIdtextView37);
        emailTextView = findViewById(R.id.customerEmailtextView344);
        radioBillingBusiness = findViewById(R.id.radioBillingBusiness);
        radioBillingResidential = findViewById(R.id.radioBillingResidential);
        radioShippingBusiness = findViewById(R.id.radioShippingBusiness);
        radioShippingResidential = findViewById(R.id.radioShippingResidential);

        billingStr1 = findViewById(R.id.newCustBillStr1);
        billingStr2 = findViewById(R.id.newCustBillStr2);
        billingCity = findViewById(R.id.newCustBillCity);
        billingState = findViewById(R.id.newCustBillState);
        billingZip = findViewById(R.id.newCustBillZip);
        shippingStr1 = findViewById(R.id.newCustShippingStr1);
        shippingStr2 = findViewById(R.id.newCustShippingStr2);
        shippingCity = findViewById(R.id.newCustShippingCity);
        shippingState = findViewById(R.id.newCustShippingState);
        shippingZip = findViewById(R.id.newCustShippingZip);
        billingRadioGroup = findViewById(R.id.radioGroupBillingAddressType);
        billingRadioGroup.setOnCheckedChangeListener(this);
        shippingRadioGroup = findViewById(R.id.radioGroupShippingAddressType);
        shippingRadioGroup.setOnCheckedChangeListener(this);
        fingerLeft1 = findViewById(R.id.fingerOneLeftbutton6);
        fingerLeft2 = findViewById(R.id.fingerTwoLeftbutton5);
        fingerLeft3 = findViewById(R.id.fingerThreeLeftbutton4);
        fingerLeft4 = findViewById(R.id.fingerFourLeftbutton3);
        fingerRight1 = findViewById(R.id.fingerOneRightbutton6);
        fingerRight2 = findViewById(R.id.fingerTwoRightbutton5);
        fingerRight3 = findViewById(R.id.fingerThreeRightbutton4);
        fingerRight4 = findViewById(R.id.fingerFourRightbutton3);
        pricesList = findViewById(R.id.newCustList1);
        taxesList = findViewById(R.id.newCustList2);
        billingCountrySpinner = findViewById(R.id.newCustBillCountry);
        shippingCountrySpinner = findViewById(R.id.newCustShippingCountry);
        fingerLeft1.setOnClickListener(this);

        fingerLeft2.setOnClickListener(this);
        fingerLeft3.setOnClickListener(this);
        fingerLeft4.setOnClickListener(this);
        fingerRight1.setOnClickListener(this);
        fingerRight2.setOnClickListener(this);
        fingerRight3.setOnClickListener(this);
        fingerRight4.setOnClickListener(this);
        customerNameTextView = findViewById(R.id.customerNametextView341);
        customerLastNameTextView = findViewById(R.id.customerLastNametextView341);
        customerDOBTextView = findViewById(R.id.customerDOBtextView371);

        biometric = EmobileBiometricDAO.getBiometrics(cust_id, EmobileBiometric.UserType.CUSTOMER);
        setUI();
        setupCountries();
        setupSpinners();
        loadFingerPrintReader(this);
        setFingerPrintUI();
    }

    private void setHandler() {
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        Global.showPrompt(ViewCustomerDetails_FA.this, R.string.fingerprint_enrollment_title, getString(R.string.duplicated_fingerprint));
                        break;
                }
                return true;
            }
        });
    }

    private void loadFingerPrintReader(Context context) {
        if (isReaderConnected) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ReaderCollection readers;
                    readers = UareUGlobal.GetReaderCollection(context);
                    readers.GetReaders();
                    if (readers.size() > 0) {
                        this.reader = readers.get(0);
                    } else {
                        return;
                    }
                } else {
                    return;
                }
                PendingIntent mPermissionIntent;
                Context applContext = getApplicationContext();
                mPermissionIntent = PendingIntent.getBroadcast(applContext, 0, new Intent(ViewCustomerDetails_FA.ACTION_USB_PERMISSION), 0);
                IntentFilter filter = new IntentFilter(ViewCustomerDetails_FA.ACTION_USB_PERMISSION);
//                registerReceiver(mUsbReceiver, filter);

                DPFPDDUsbHost.DPFPDDUsbCheckAndRequestPermissions(applContext, mPermissionIntent, reader.GetDescription().name);
                reader.Open(Reader.Priority.EXCLUSIVE);
                Reader.Status status = reader.GetStatus();
                if (status.status == ReaderStatus.BUSY) {
                    reader.CancelCapture();
                }
                dpi = GetFirstDPI(reader);
                engine = UareUGlobal.GetEngine();
            } catch (UareUException e) {
                Crashlytics.logException(e);
                e.printStackTrace();
            } catch (DPFPDDUsbException e) {
                Crashlytics.logException(e);
                e.printStackTrace();
            } catch (UnsatisfiedLinkError e) {
                Crashlytics.logException(e);
                e.printStackTrace();
            }
        }
    }

    private void releaseReader() {
        if (isReaderConnected && reader != null) {
            try {
                reader.CancelCapture();
                reader.Close();
                reader = null;
            } catch (UareUException e) {
                e.printStackTrace();
            }
        }
    }

    private void setUI() {
        disableFields();
        if (isCustomerEdit) {
            List<CustomerCustomField> customFields = CustomerCustomFieldsDAO.getCustomFields(customer.getCust_id());
            customerNameTextView.setText(customer.getCust_firstName());
            customerLastNameTextView.setText(customer.getCust_lastName());
            customerDOBTextView.setText(customer.getCust_dob());
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

            shippingStr1.setText(customer.getShippingAddress().getAddr_s_str1());
            shippingStr2.setText(customer.getShippingAddress().getAddr_s_str2());
            shippingCity.setText(customer.getShippingAddress().getAddr_s_city());
            shippingState.setText(customer.getShippingAddress().getAddr_s_state());
            shippingZip.setText(customer.getShippingAddress().getAddr_s_zipcode());

            TableLayout tableLayout = findViewById(R.id.customerFinancialInfoTableLayout);
            for (CustomerCustomField field : customFields) {
                View row = View.inflate(this, R.layout.customercustomfields_tablerow_layout, null);
                row.setTag(field);
                ((TextView) row.findViewById(R.id.customerCustomFieldLabelTextView)).setText(field.getCustFieldName());
                ((EditText) row.findViewById(R.id.customerCustomFieldValueEditText)).setText(field.getCustValue());
                tableLayout.addView(row);
                if (field.getCustFieldId().equalsIgnoreCase(getString(R.string.ems_card_id_num))) {
                    cardIdEditText = row.findViewById(R.id.customerCustomFieldValueEditText);
                }
            }


        } else {
            TableLayout tableLayout = findViewById(R.id.customerFinancialInfoTableLayout);
            CustomerCustomField field = new CustomerCustomField();
            field.setCustId(customer.getCust_id());
            field.setCustFieldId(getString(R.string.ems_card_id_num));
            field.setCustValue("");
            field.setCustFieldName(getString(R.string.cardId));
            View row = View.inflate(this, R.layout.customercustomfields_tablerow_layout, null);
            row.setTag(field);
            ((TextView) row.findViewById(R.id.customerCustomFieldLabelTextView)).setText(R.string.cardId);
            ((EditText) row.findViewById(R.id.customerCustomFieldValueEditText)).setText(field.getCustValue());
            tableLayout.addView(row);
            cardIdEditText = row.findViewById(R.id.customerCustomFieldValueEditText);

            findViewById(R.id.customerDOBtextView371).setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (newFrag == null) {
                        newFrag = new DateDialog();
                        newFrag.show(getSupportFragmentManager(), "dialog");
                    }
                    return false;
                }
            });
        }
        findViewById(R.id.btnSaveCustomer).setOnClickListener(this);

    }

    private void disableFields() {
        customerNameTextView.setEnabled(!isCustomerEdit);
        customerLastNameTextView.setEnabled(!isCustomerEdit);
        contacTextView.setEnabled(!isCustomerEdit);
        phoneTextView.setEnabled(!isCustomerEdit);
        customerDOBTextView.setEnabled(!isCustomerEdit);
        emailTextView.setEnabled(!isCustomerEdit);
        companyTextView.setEnabled(!isCustomerEdit);
        balanceTextView.setEnabled(!isCustomerEdit);
        limitTextView.setEnabled(!isCustomerEdit);
        taxableTextView.setEnabled(false);
        taxidTextView.setEnabled(false);
        pricesList.setEnabled(!isCustomerEdit);
        billingStr1.setEnabled(!isCustomerEdit);
        billingStr2.setEnabled(!isCustomerEdit);
        billingCity.setEnabled(!isCustomerEdit);
        billingState.setEnabled(!isCustomerEdit);
        billingZip.setEnabled(!isCustomerEdit);
        radioBillingBusiness.setEnabled(!isCustomerEdit);
        radioBillingResidential.setEnabled(!isCustomerEdit);
        radioShippingBusiness.setEnabled(!isCustomerEdit);
        radioShippingResidential.setEnabled(!isCustomerEdit);
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
        taxes.add("Select One");
        priceLevel.add("Select One");
        TaxesHandler handler = new TaxesHandler(this);
        taxList = handler.getProductTaxes(preferences.getPreferences(MyPreferences.pref_show_only_group_taxes));
        PriceLevelHandler handler2 = new PriceLevelHandler();
        priceLevelList = handler2.getPriceLevel();

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

        taxesList.setAdapter(taxAdapter);
        taxesList.setOnItemSelectedListener(getItemSelectedListener(SPINNER_TAXES));

        pricesList.setAdapter(priceLevelAdapter);
        pricesList.setOnItemSelectedListener(getItemSelectedListener(SPINNER_PRICELEVEL));

        pricesList.setSelection(priceLevelSelected);
        taxesList.setSelection(taxSelected);
    }

    private void setupCountries() {
        countries = new ArrayList<>();
        isoCountries = Locale.getISOCountries();
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
//        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
//        boolean isScreenOn = powerManager.isScreenOn();
//        if (!isScreenOn && myPref.isExpireUserSession())
//            Global.loggedIn = false;
        global.startActivityTransitionTimer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseReader();
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
            case R.id.btnSaveCustomer:
                if (isCustomerEdit) {
                    saveCustomer();
                } else {
                    insertCustomer();
                }
                saveBiometrics();
                break;
            case R.id.fingerOneLeftbutton6:
                startFingerPrintScanner(Finger.FINGER_ONE_LEFT);
                break;
            case R.id.fingerTwoLeftbutton5:
                startFingerPrintScanner(Finger.FINGER_TWO_LEFT);
                break;
            case R.id.fingerThreeLeftbutton4:
                startFingerPrintScanner(Finger.FINGER_THREE_LEFT);
                break;
            case R.id.fingerFourLeftbutton3:
                startFingerPrintScanner(Finger.FINGER_FOUR_LEFT);
                break;
            case R.id.fingerOneRightbutton6:
                startFingerPrintScanner(Finger.FINGER_ONE_RIGHT);
                break;
            case R.id.fingerTwoRightbutton5:
                startFingerPrintScanner(Finger.FINGER_TWO_RIGHT);
                break;
            case R.id.fingerThreeRightbutton4:
                startFingerPrintScanner(Finger.FINGER_THREE_RIGHT);
                break;
            case R.id.fingerFourRightbutton3:
                startFingerPrintScanner(Finger.FINGER_FOUR_RIGHT);
                break;

        }
    }

    private void insertCustomer() {
        CustomersHandler custHandler = new CustomersHandler(this);
        AddressHandler addressHandler = new AddressHandler(this);
        SalesTaxCodesHandler taxCodeHandler = new SalesTaxCodesHandler(this);
        Address addrData = new Address();
        MyPreferences myPref = new MyPreferences(this);
        customer.setCust_name(String.format("%s %s", getEditText(R.id.customerNametextView341).getText().toString(),
                getEditText(R.id.customerLastNametextView341).getText().toString())); //field[CUST_ALIAS].getText().toString();
//        customer.setCust_name(getEditText(R.id.customerNametextView341).getText().toString());
        customer.setCust_firstName(getEditText(R.id.customerNametextView341).getText().toString());
        customer.setCust_lastName(getEditText(R.id.customerLastNametextView341).getText().toString());
        customer.setCompanyName(getEditText(R.id.customerCompanytextView34).getText().toString());
        customer.setCust_email(getEditText(R.id.customerEmailtextView344).getText().toString());
        customer.setCust_phone(getEditText(R.id.customerPhonetextView343).getText().toString());
        customer.setCust_contact(getEditText(R.id.customerContacttextView342).getText().toString());
        customer.setCust_balance(getEditText(R.id.customerBalancetextView371).getText().toString());
        customer.setCust_limit(getEditText(R.id.customerLimittextView372).getText().toString());

        customer.setQb_sync("0");
        customer.setCust_dob(getEditText(R.id.customerDOBtextView371).getText().toString());
        if (priceLevelSelected > 0)
            customer.setPricelevel_id(priceLevelList.get(priceLevelSelected - 1)[1]);

        if (taxSelected > 0) {
            customer.setCust_salestaxcode(taxList.get(taxSelected - 1).getTaxId());
            customer.setCust_taxable(taxCodeHandler.getTaxableTaxCode());
            myPref.setCustTaxCode(customer.getCust_salestaxcode());
        }

        addrData.setAddr_id(UUID.randomUUID().toString());
        addrData.setCust_id(customer.getCust_id());
        // add zone id

        // add addr_type
        addrData.setAddr_b_str1(getEditText(R.id.newCustBillStr1).getText().toString());
        addrData.setAddr_b_str2(getEditText(R.id.newCustBillStr2).getText().toString());
        // add addr_b_str3
        addrData.setAddr_b_city(getEditText(R.id.newCustBillCity).getText().toString());
        addrData.setAddr_b_state(getEditText(R.id.newCustBillState).getText().toString());
        if (billingSelectedCountry > 0)
            addrData.setAddr_b_country(isoCountries[billingSelectedCountry - 1]);
        addrData.setAddr_b_zipcode(getEditText(R.id.newCustBillZip).getText().toString());

        // add addr_s_name
        addrData.setAddr_s_str1(getEditText(R.id.newCustShippingStr1).getText().toString());
        addrData.setAddr_s_str2(getEditText(R.id.newCustShippingStr2).getText().toString());
        // add addr_s_str3
        addrData.setAddr_s_city(getEditText(R.id.newCustShippingCity).getText().toString());
        addrData.setAddr_s_state(getEditText(R.id.newCustShippingState).getText().toString());
        if (shippingSelectedCountry > 0)
            addrData.setAddr_s_country(isoCountries[shippingSelectedCountry - 1]);
        addrData.setAddr_s_zipcode(getEditText(R.id.newCustShippingZip).getText().toString());

        addrData.setAddr_b_type(addr_b_type);
        addrData.setAddr_s_type(addr_s_type);
        // add qb_cust_id

        addressHandler.insertOneAddress(addrData);
        // }
        custHandler.insertOneCustomer(customer);

        String cardNumber = cardIdEditText == null ? "" : cardIdEditText.getText().toString();

        CustomerCustomField customField = new CustomerCustomField();
        customField.setCustId(customer.getCust_id());
        customField.setCustFieldId(getString(R.string.ems_card_id_num));
        customField.setCustFieldName("ID");
        customField.setCustValue(cardNumber);
        CustomerCustomFieldsDAO.upsert(customField);

        // Set-up data for default selection of this newly created customer
//        HashMap<String, String> custMap = custHandler.getCustomerInfo(lastCustID);

        myPref.setCustID(customer.getCust_id());
        myPref.setCustName(customer.getCust_name());
        myPref.setCustPriceLevel(customer.getPricelevel_id());
        myPref.setCustSelected(true);
        myPref.setCustIDKey(customer.getCust_id());
        myPref.setCustEmail(customer.getCust_email());
        setResult(-1);
        finish();
    }

    private EditText getEditText(int id) {
        return (EditText) findViewById(id);
    }

    private void saveBiometrics() {
        EmobileBiometricDAO.delete(cust_id, EmobileBiometric.UserType.CUSTOMER);
        EmobileBiometricDAO.upsert(biometric);
    }

    private Dialog showScanningDialog(final Finger finger) {
        final Dialog dialog = new Dialog(activity, R.style.DialogLargeArea);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.fingerprint_scanning_layout);
        fingerPrintimage = dialog.findViewById(fingerPrintimageView);
        Button fingerPrintCancelButton = dialog.findViewById(R.id.cancelScanningButton);
        Button unregisterButton = dialog.findViewById(unregisterFingerprintbutton2);
        fingerPrintCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_reset = true;
                try {
                    if (reader.GetStatus().status == ReaderStatus.BUSY) {
                        reader.CancelCapture();
                    }
                } catch (UareUException e) {

                }
                dialog.dismiss();
            }
        });

        unregisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_reset = true;
                EmobileBiometricDAO.deleteFinger(cust_id, EmobileBiometric.UserType.CUSTOMER, finger);
                biometric = EmobileBiometricDAO.getBiometrics(cust_id, EmobileBiometric.UserType.CUSTOMER);
                setFingerPrintUI();
                try {
                    if (reader.GetStatus().status == ReaderStatus.BUSY) {
                        reader.CancelCapture();
                    }
                } catch (UareUException e) {

                }
                dialog.dismiss();
            }
        });


        dialog.show();
        startAnimation(fingerPrintimage, 0);
        progressBar = dialog.findViewById(R.id.fingerprintScanningprogressBar3);
        progressBar.setMax(5);
        progressBar.setProgress(progress);
        fingerPrintScanningNotesTextView = dialog.findViewById(R.id.fingerPrintNotestextView);
        fingerPrintScanningNotesTextView.setText(R.string.fingerprint_enrollment);
        return dialog;
    }

    private void startAnimation(final ImageView imageView, int step) {
        android.os.Handler handler = new android.os.Handler();
        switch (step) {
            case 1:
                fingerPrintimage.setBackgroundResource(R.drawable.fingertscanner_ok);
                AnimationDrawable animation = (AnimationDrawable) imageView.getBackground();
                animation.start();
                fingerPrintScanningNotesTextView.setText(R.string.processing);
                fingerPrintScanningNotesTextView.setTextColor(Color.RED);
                startAnimation(imageView, 2);
                break;
            case 2:
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        fingerPrintScanningNotesTextView.setText(R.string.rescan_fingerprint);
                        fingerPrintScanningNotesTextView.setTextColor(Color.RED);
                        fingerPrintimage.setBackgroundResource(R.drawable.fingertscanner_scanning);
                        AnimationDrawable animation = (AnimationDrawable) imageView.getBackground();
                        animation.start();
                    }
                }, 500);
                break;
        }

    }

    private void startFingerPrintScanner(final Finger finger) {
        try {
            if (reader != null) {
                final Dialog scanningDialog = showScanningDialog(finger);
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
                                Log.d("Engine", "Engine Enrollment progress");
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
                            progress = 0;
                            scanningDialog.dismiss();
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
            } else {
                Toast.makeText(this, getString(R.string.fingerreadernotfound), Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void setFingerPrintUI() {
        fingerLeft1.setBackgroundResource(R.color.black_transparency);
        fingerLeft2.setBackgroundResource(R.color.black_transparency);
        fingerLeft3.setBackgroundResource(R.color.black_transparency);
        fingerLeft4.setBackgroundResource(R.color.black_transparency);
        fingerRight1.setBackgroundResource(R.color.black_transparency);
        fingerRight2.setBackgroundResource(R.color.black_transparency);
        fingerRight3.setBackgroundResource(R.color.black_transparency);
        fingerRight4.setBackgroundResource(R.color.black_transparency);
        for (BiometricFid fid : biometric.getFids()) {
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
//        customFields = CustomerCustomFieldsDAO.getCustomFields(cust_id);
        Toast.makeText(this, R.string.information_saved, Toast.LENGTH_LONG).show();

//        customer.setCust_phone(phoneTextView.getText().toString());
//        customer.setCust_email(emailTextView.getText().toString());
//        customer.setCust_contact(contacTextView.getText().toString());
//        customer.setCust_limit(limitTextView.getText().toString());
//        customer.setCompanyName(companyTextView.getText().toString());
//        CustomersHandler handler = new CustomersHandler(this);
//        handler.insertOneCustomer(customer);
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
            ((EditText) getActivity().findViewById(R.id.customerDOBtextView371)).setText(Global.formatToDisplayDate(dobDate, 1));

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
            TextView text = view.findViewById(android.R.id.text1);
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

            TextView taxName = row.findViewById(R.id.taxName);
            TextView taxValue = row.findViewById(R.id.taxValue);
            ImageView checked = row.findViewById(R.id.checkMark);
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
                Log.d("Enrollment", "Enrollment Capture in progress");
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
                    progress++;
                    progressBar.setProgress(progress);

                    result = prefmd;
                    break;
                } catch (Exception e) {
                    m_enginError = e.toString();
                    Log.w("UareUSampleJava", "Engine error: " + e.toString());
                }
            }

            m_text_conclusionString = QualityToString(cap_result);

            if (!TextUtils.isEmpty(m_enginError)) {
                m_text_conclusionString = "Engine: " + m_enginError;
            }

            if (m_enrollment_fmd != null || m_current_fmds_count == 0) {
                if (!m_first) {
                    if (m_text_conclusionString.length() == 0) {
                        try {
//                            Fid importFid = UareUGlobal.GetImporter().ImportFid(cap_result.image.getData(), Fid.Format.ANSI_381_2004);
//                            Fmd importFmd = UareUGlobal.GetImporter().ImportFmd(m_enrollment_fmd.getData(), Fmd.Format.ANSI_378_2004, Fmd.Format.ANSI_378_2004);
//                            Fmd fmd = m_engine.CreateFmd(m_enrollment_fmd.getData(), cap_result.image.getViews()[0].getWidth(), cap_result.image.getViews()[0].getHeight(),
//                                    cap_result.image.getViews()[0].getQuality(), cap_result.image.getViews()[0].getFingerPosition(),
//                                    cap_result.image.getCbeffId(), Fmd.Format.ANSI_378_2004);
                            Fmd[] fmds = EmobileBiometricDAO.getFmds(EmobileBiometric.UserType.CUSTOMER);
                            Engine.Candidate[] candidates = new Engine.Candidate[0];
                            if (fmds.length > 0) {
                                candidates = engine.Identify(m_enrollment_fmd, 0, fmds, 100000, 2);
                            }
                            m_reset = true;
                            if (candidates.length == 0) {
                                EmobileBiometricDAO.deleteFinger(cust_id, EmobileBiometric.UserType.CUSTOMER, finger);
                                BiometricFid biometricFid = new BiometricFid(engine, cap_result.image, finger);
                                biometric.setEntityid(cust_id);
                                biometric.getFids().add(biometricFid);
                                biometric.setRegid(preferences.getAcctNumber());
                                EmobileBiometricDAO.upsert(biometric);
                            } else {
                                BiometricFid biometricFid = new BiometricFid(engine, cap_result.image, finger);
                                int fmd_index = candidates[0].fmd_index;
                                final EmobileBiometric emobileBiometric = EmobileBiometricDAO.getBiometrics(fmds[fmd_index]);
                                if ((emobileBiometric.getUserType() == EmobileBiometric.UserType.CUSTOMER
                                        && emobileBiometric.getEntityid().equalsIgnoreCase(String.valueOf(cust_id)))
                                        || emobileBiometric.getUserType() != EmobileBiometric.UserType.CUSTOMER) {
                                    boolean alreadyRegistered = false;
                                    for (BiometricFid fid : emobileBiometric.getFids()) {
                                        if (fid.getFingerCode() != biometricFid.getFingerCode()) {
                                            alreadyRegistered = true;
                                            break;
                                        }
                                    }
                                    if (!alreadyRegistered) {
                                        EmobileBiometricDAO.deleteFinger(cust_id, EmobileBiometric.UserType.CUSTOMER, finger);
                                        biometric.setEntityid(cust_id);
                                        biometric.getFids().add(biometricFid);
                                        biometric.setRegid(preferences.getAcctNumber());
                                        EmobileBiometricDAO.upsert(biometric);
                                    } else {
                                        handler.sendEmptyMessage(0);
                                    }
                                } else {
                                    if (!emobileBiometric.getEntityid().equalsIgnoreCase(String.valueOf(cust_id))) {
                                        handler.sendEmptyMessage(0);
                                    }
                                }
                            }
                        } catch (UareUException e) {

                        }
                        m_text_conclusionString = m_success ? "Enrollment template created, size: " + m_templateSize : "Enrollment template failed. Please try again";
                    }
                }
                m_enrollment_fmd = null;
            } else {
                m_first = false;
                m_success = false;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        startAnimation(fingerPrintimage, 1);
                    }
                });
            }

            return result;
        }
    }
}
