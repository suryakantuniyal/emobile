package com.android.emobilepos.customer;

import android.app.Activity;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.Menu;
import android.view.View;
import android.widget.AbsSpinner;
import android.widget.AdapterView;
import android.widget.EditText;
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
import com.android.emobilepos.models.Address;
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

public class ViewCustomerDetails_FA extends BaseFragmentActivityActionBar implements AdapterView.OnItemSelectedListener {

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customer_moreinfo_layout);
        activity = this;
        global = (Global) getApplication();
        Bundle extras = getIntent().getExtras();
        CustomersHandler custHandler = new CustomersHandler(this);

        cust_id = extras.getString("cust_id");
        customFields = CustomerCustomFieldsDAO.getCustomFields(cust_id);
        customer = custHandler.getCustomer(cust_id);
        setUI();
        setupCountries();

        hasBeenCreated = true;
    }

    private void setUI() {
        List<CustomerCustomField> customFields = CustomerCustomFieldsDAO.getCustomFields(customer.getCust_id());
        ((TextView) findViewById(R.id.customerNametextView341)).setText(String.format("%s %s %s", customer.getCust_firstName(), customer.getCust_middleName(), customer.getCust_lastName()));
        ((TextView) findViewById(R.id.customerContacttextView342)).setText(customer.getCust_contact());
        ((TextView) findViewById(R.id.customerPhonetextView343)).setText(customer.getCust_phone());
        ((TextView) findViewById(R.id.customerCompanytextView34)).setText(customer.getCompanyName());
        ((TextView) findViewById(R.id.customerBalancetextView371)).setText(customer.getCust_balance());
        ((TextView) findViewById(R.id.customerLimittextView372)).setText(customer.getCust_limit());
        ((TextView) findViewById(R.id.customerTaxabletextView373)).setText(customer.getCust_taxable());
        ((TextView) findViewById(R.id.customerTaxIdtextView37)).setText(customer.getCust_salestaxcode());
        ((TextView) findViewById(R.id.customerEmailtextView344)).setText(customer.getCust_email());
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

        ((TextView) findViewById(R.id.newCustBillStr1)).setText(customer.getBillingAddress().getAddr_b_str1());
        ((TextView) findViewById(R.id.newCustBillStr2)).setText(customer.getBillingAddress().getAddr_b_str2());
        ((TextView) findViewById(R.id.newCustBillCity)).setText(customer.getBillingAddress().getAddr_b_city());
        ((TextView) findViewById(R.id.newCustBillState)).setText(customer.getBillingAddress().getAddr_b_state());
        ((TextView) findViewById(R.id.newCustBillZip)).setText(customer.getBillingAddress().getAddr_b_zipcode());

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
        CreateCustomer_FA.CustomAdapter taxAdapter = new CreateCustomer_FA.CustomAdapter(this, android.R.layout.simple_spinner_item, taxes, taxArr, true);
        CreateCustomer_FA.CustomAdapter priceLevelAdapter = new CreateCustomer_FA.CustomAdapter(this, android.R.layout.simple_spinner_item, priceLevel, priceLevelList, false);

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

//    private void promptGiftCardNumber(String currentValue) {
//        final Dialog globalDlog = new Dialog(this, R.style.Theme_TransparentTest);
//        globalDlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        globalDlog.setCancelable(true);
//        globalDlog.setContentView(R.layout.dlog_field_single_layout);
//        final EditText viewField = (EditText) globalDlog.findViewById(R.id.dlogFieldSingle);
//        viewField.setInputType(InputType.TYPE_CLASS_NUMBER);
//        viewField.setText(currentValue);
//        TextView viewTitle = (TextView) globalDlog.findViewById(R.id.dlogTitle);
//        TextView viewMsg = (TextView) globalDlog.findViewById(R.id.dlogMessage);
//        viewTitle.setText(R.string.header_title_gift_card);
//
//        viewMsg.setText(R.string.dlog_title_enter_giftcard_number);
//        Button btnCancel = (Button) globalDlog.findViewById(R.id.btnCancelDlogSingle);
//        btnCancel.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                globalDlog.dismiss();
//            }
//        });
//        Button btnOk = (Button) globalDlog.findViewById(R.id.btnDlogSingle);
//        btnOk.setText(R.string.button_ok);
//        btnOk.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                globalDlog.dismiss();
//                String cardNumber = viewField.getText().toString();
//                CustomerCustomField customField = CustomerCustomFieldsDAO.findEMWSCardIdByCustomerId(cust_id);
//                if (customField == null) {
//                    customField = new CustomerCustomField();
//                }
//                customField.setCustId(cust_id);
//                customField.setCustFieldId("EMS_CARD_ID_NUM");
//                customField.setCustFieldName("ID");
//                customField.setCustValue(cardNumber);
//                CustomerCustomFieldsDAO.upsert(customField);
//                CustomersHandler handler = new CustomersHandler(ViewCustomerDetails_FA.this);
//                handler.updateSyncStatus(cust_id, false);
//                customFields = CustomerCustomFieldsDAO.getCustomFields(cust_id);
////                myAdapter.notifyDataSetChanged();
//
//            }
//        });
//        globalDlog.show();
//    }

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


//    private void showAddressDialog(int type) {
//        AddressHandler addressHandler = new AddressHandler(activity);
//        List<String[]> addressDownloadedItems = new ArrayList<>();
//        AlertDialog.Builder adb = new AlertDialog.Builder(activity);
//        String dialogTitle = "";
//        switch (type) {
//            case CASE_BILLING:
//                addressDownloadedItems = addressHandler.getSpecificAddress(cust_id, CASE_BILLING);
//                dialogTitle = "Billing Address";
//                break;
//            case CASE_SHIPPING:
//                addressDownloadedItems = addressHandler.getSpecificAddress(cust_id, CASE_SHIPPING);
//                dialogTitle = "Shipping Address";
//                break;
//        }
//
//        int size = addressDownloadedItems.size();
//        String[] addressItems = new String[size];
//        StringBuilder sb = new StringBuilder();
//        String temp;
//        for (int i = 0; i < size; i++) {
//            temp = addressDownloadedItems.get(i)[0];
//            if (!temp.isEmpty())                            //address 1
//                sb.append(temp).append(" ");
//            temp = addressDownloadedItems.get(i)[1];
//            if (!temp.isEmpty())                            //address 2
//                sb.append(temp).append(" ");
//            temp = addressDownloadedItems.get(i)[2];
//            if (!temp.isEmpty())                            //address 3
//                sb.append(temp).append("\t\t");
//            temp = addressDownloadedItems.get(i)[3];
//            if (!temp.isEmpty())                            //address country
//                sb.append(temp).append(" ");
//            temp = addressDownloadedItems.get(i)[4];
//            if (!temp.isEmpty())                            //address city
//                sb.append(temp).append(" ");
//            temp = addressDownloadedItems.get(i)[5];        //address state
//            if (!temp.isEmpty())
//                sb.append(temp).append(" ");
//            temp = addressDownloadedItems.get(i)[6];    //address zipcode
//            if (!temp.isEmpty())
//                sb.append(temp).append(" ");
//            addressItems[i] = sb.toString();
//            sb.setLength(0);
//        }
//
//        adb.setItems(addressItems, new DialogInterface.OnClickListener() {
//
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//            }
//        });
//
//        adb.setNegativeButton("OK", null);
//        adb.setTitle(dialogTitle);
//        adb.show();
//    }

//
//    public class ListViewAdapter extends BaseAdapter implements Filterable {
//        private LayoutInflater myInflater;
//
//        public ListViewAdapter(Context context) {
//            myInflater = LayoutInflater.from(context);
//        }
//
//        @Override
//        public int getCount() {
//            //+3 for the dividers +2 for the actual address
//            int count = allInfoLeft.size() + allFinancialLeft.size() + 3 + 2 + customFields.size();
//            if (!customFields.isEmpty()) {
//                count++;
//            }
//            return count;
//        }
//
//        @Override
//        public Object getItem(int position) {
//            return null;
//        }
//
//        @Override
//        public long getItemId(int position) {
//            return 0;
//        }
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//
//            ViewHolder holder;
//            int type = getItemViewType(position);
//
//            if (convertView == null) {
//                holder = new ViewHolder();
//
//                switch (type) {
//                    case 0: {
//                        convertView = myInflater.inflate(R.layout.orddetails_lvdivider_adapter, null);
//                        holder.left = (TextView) convertView.findViewById(R.id.orderDivLeft);
//                        holder.right = (TextView) convertView.findViewById(R.id.orderDivRight);
//
//                        if (position == 0) {
//                            holder.left.setText(getString(R.string.cust_detail_info));
//                        } else if (position == (allInfoLeft.size() + 1)) {
//                            holder.left.setText(getString(R.string.cust_detail_financial_info));
//                        } else if (position == (allInfoLeft.size() + allFinancialLeft.size() + 2)) {
//                            holder.left.setText(getString(R.string.cust_detail_address));
//                        } else if ((position == (allInfoLeft.size() + allFinancialLeft.size() + 2 + 3)) && !customFields.isEmpty()) {
//                            holder.left.setText(getString(R.string.header_title_customer_custom_fields));
//                        }
//                        break;
//                    }
//                    case 1: {
//                        convertView = myInflater.inflate(R.layout.orddetails_lvinfo_adapter, null);
//                        holder.left = (TextView) convertView.findViewById(R.id.ordInfoLeft);
//                        holder.right = (TextView) convertView.findViewById(R.id.ordInfoRight);
//
//                        int length2 = allInfoLeft.size() + 2 + allFinancialLeft.size();
//                        if (position > 0 && position <= allInfoLeft.size()) {
//                            holder.left.setText(allInfoLeft.get(position - 1));
//                            holder.right.setText(allInfoRight.get(position - 1));
//                        } else if (position > allInfoLeft.size() + 1 && position < length2) {
//                            int ind = position - allInfoLeft.size() - 2;
//                            holder.left.setText(allFinancialLeft.get(ind));
//                            holder.right.setText(allFinancialRight.get(ind));
//                        } else if (position == allFinancialLeft.size() + allInfoLeft.size() + 3)                //Billing Address
//                        {
//                            holder.left.setText(getString(R.string.cust_detail_bill));
//                            holder.right.setText("");
//                        } else                                                                            //Shipping Address
//                        {
//                            holder.left.setText(getString(R.string.cust_detail_ship));
//                            holder.right.setSingleLine(true);
//                            holder.right.setText("");
//                        }
//                        break;
//                    }
//                }
//                convertView.setTag(holder);
//            } else {
//                holder = (ViewHolder) convertView.getTag();
//            }
//
//            if (type != 0) {
//                int length2 = allInfoLeft.size() + 2 + allFinancialLeft.size();
//                if (position > 0 && position <= allInfoLeft.size()) {
//                    holder.left.setText(allInfoLeft.get(position - 1));
//                    holder.right.setText(allInfoRight.get(position - 1));
//                } else if (position > allInfoLeft.size() + 1 && position < length2) {
//                    int ind = position - allInfoLeft.size() - 2;
//                    holder.left.setText(allFinancialLeft.get(ind));
//                    holder.right.setText(allFinancialRight.get(ind));
//                } else if (position == length2 + 1) {
//                    holder.left.setText(getString(R.string.cust_detail_bill));
//                    if (address != null && !address.isEmpty()) {
//                        Address addr = ViewCustomerDetails_FA.this.address.get(0);
//                        String str = String.format("%s %s %s%s %s %s %s",
//                                StringUtil.nullStringToEmpty(addr.addr_b_str1),
//                                StringUtil.nullStringToEmpty(addr.addr_b_str2),
//                                StringUtil.nullStringToEmpty(addr.addr_b_str3),
//                                StringUtil.nullStringToEmpty(addr.addr_b_city),
//                                StringUtil.nullStringToEmpty(addr.addr_b_state),
//                                StringUtil.nullStringToEmpty(addr.addr_b_zipcode),
//                                StringUtil.nullStringToEmpty(addr.addr_b_country));
//                        holder.right.setText(str);
//                    } else {
//                        holder.right.setText("");
//                    }
//                } else if (position == length2 + 2) {
//                    holder.left.setText(getString(R.string.cust_detail_ship));
//                    holder.right.setSingleLine(true);
//                    if (address != null && !address.isEmpty()) {
//                        Address addr = ViewCustomerDetails_FA.this.address.get(0);
//                        String str = String.format("%s %s %s%s %s %s %s",
//                                StringUtil.nullStringToEmpty(addr.addr_s_str1),
//                                StringUtil.nullStringToEmpty(addr.addr_s_str2),
//                                StringUtil.nullStringToEmpty(addr.addr_s_str3),
//                                StringUtil.nullStringToEmpty(addr.addr_s_city),
//                                StringUtil.nullStringToEmpty(addr.addr_s_state),
//                                StringUtil.nullStringToEmpty(addr.addr_s_zipcode),
//                                StringUtil.nullStringToEmpty(addr.addr_s_country));
//                        holder.right.setText(str);
//                    } else {
//                        holder.right.setText("");
//                    }
//
//                } else if (position >= length2 + 4) {
//                    CustomerCustomField customField = customFields.get(position - length2 - 4);//CustomerCustomFieldsDAO.findEMWSCardIdByCustomerId(cust_id);
//                    holder.left.setText(customField.getCustFieldName());
//                    holder.right.setSingleLine(true);
//                    holder.right.setText(customField.getCustValue());
//                }
//            }
//            return convertView;
//        }
//
//        @Override
//        public Filter getFilter() {
//            return null;
//        }
//
//        @Override
//        public int getItemViewType(int position) {
//            if (position == 0 || (position == (allInfoLeft.size() + 1)) || (position == (allFinancialLeft.size() + allInfoLeft.size() + 2))
//                    || (position == (allFinancialLeft.size() + allInfoLeft.size() + 2 + 3))) {
//                return 0;
//            }
//
//            return 1;
//
//        }
//
//        @Override
//        public int getViewTypeCount() {
//            return 2;
//        }
//
//        public class ViewHolder {
//            TextView left;
//            TextView right;
//        }
//
//    }
}
