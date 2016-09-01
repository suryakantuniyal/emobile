package com.android.emobilepos.settings;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.database.CategoriesHandler;
import com.android.database.DBManager;
import com.android.database.PayMethodsHandler;
import com.android.database.ShiftPeriodsDBHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.country.CountryPicker;
import com.android.emobilepos.country.CountryPickerListener;
import com.android.emobilepos.mainmenu.SettingsTab_FR;
import com.android.emobilepos.models.PaymentMethod;
import com.android.emobilepos.shifts.OpenShift_FA;
import com.android.emobilepos.shifts.ShiftExpensesList_FA;
import com.android.support.DeviceUtils;
import com.android.support.Global;
import com.android.support.HttpClient;
import com.android.support.MyPreferences;
import com.android.support.SynchMethods;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;
import com.viewpagerindicator.TitlePageIndicator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import main.EMSDeviceManager;

public class SettingsActivity extends BaseFragmentActivityActionBar {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private static int settingsType = 0;
    private static android.support.v4.app.FragmentManager supportFragmentManager;
    private TitlePageIndicator titlePageIndicator;
    private Global global;
    private boolean hasBeenCreated;
    public final static int CASE_ADMIN = 0, CASE_MANAGER = 1, CASE_GENERAL = 2;

    private enum SettingSection {
        GENERAL(0), RESTAURANT(1), GIFTCARD(2), PAYMENTS(3), PRINTING(4), PRODUCTS(5), OTHERS(6);
        int code;

        SettingSection(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static SettingSection getInstance(int code) {
            switch (code) {
                case 0:
                    return GENERAL;
                case 1:
                    return RESTAURANT;
                case 2:
                    return GIFTCARD;
                case 3:
                    return PAYMENTS;
                case 4:
                    return PRINTING;
                case 5:
                    return PRODUCTS;
                case 6:
                    return OTHERS;
                default:
                    return GENERAL;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        global = (Global) getApplication();
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());
        Bundle extras = this.getIntent().getExtras();
        settingsType = extras.getInt("settings_type");
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        titlePageIndicator = (TitlePageIndicator) findViewById(R.id.indicator);
        titlePageIndicator.setViewPager(mViewPager);
        supportFragmentManager = getSupportFragmentManager();
        hasBeenCreated = true;
    }

    @Override
    public void onResume() {

        if (global.isApplicationSentToBackground(this))
            global.loggedIn = false;
        global.stopActivityTransitionTimer();

        if (hasBeenCreated && !global.loggedIn) {
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
            global.loggedIn = false;
        global.startActivityTransitionTimer();
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
//            if (position == 0) {
            PrefsFragment fragment = new PrefsFragment();
            Bundle args = new Bundle();
            args.putInt("section", SettingSection.getInstance(position).getCode());
            fragment.setArguments(args);
            return fragment;
//            } else
//                return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            switch (settingsType) {
                case CASE_ADMIN:
                    return SettingSection.values().length;
                case CASE_MANAGER:
                case CASE_GENERAL:
                    return 1;
                default:
                    return 1;
            }

        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    switch (settingsType) {
                        case CASE_MANAGER:
                            return getString(R.string.config_manager_settings);
                        default:
                            return getString(R.string.config_general_settings);
                    }
                case 1:
                    return getString(R.string.config_restaurant_settings);
                case 2:
                    return getString(R.string.config_gift_card_settings);
                case 3:
                    return getString(R.string.config_payments_settings);
                case 4:
                    return getString(R.string.config_printing_settings);
                case 5:
                    return getString(R.string.config_product_settings);
                case 6:
                    return getString(R.string.config_other_settings);
            }
            return null;
        }
    }

    public static class PrefsFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener, HttpClient.DownloadFileCallBack {
        private Dialog promptDialog;
        private AlertDialog.Builder dialogBuilder;
        private MyPreferences myPref;
        private List<String> macAddressList = new ArrayList<>();
        private CheckBoxPreference storeForwardFlag;
        private Preference openShiftPref, defaultCountry, storeForwardTransactions;

        private int getLayoutId(SettingSection settingSection) {
            switch (settingSection) {
                case GENERAL:
                    return R.xml.settings_admin_general_layout;
                case RESTAURANT:
                    return R.xml.settings_admin_restaurant_layout;
                case GIFTCARD:
                    return R.xml.settings_admin_giftcard_layout;
                case PAYMENTS:
                    return R.xml.settings_admin_payments_layout;
                case PRINTING:
                    return R.xml.settings_admin_printing_layout;
                case PRODUCTS:
                    return R.xml.settings_admin_products_layout;
                case OTHERS:
                    return R.xml.settings_admin_others_layout;
                default:
                    return R.xml.settings_admin_general_layout;
            }
        }

        private void setPrefManager(SettingSection section, PreferenceManager prefManager) {
            switch (section) {
                case GENERAL:
                    prefManager.findPreference("pref_transaction_num_prefix").setOnPreferenceClickListener(this);

//                    addPreferencesFromResource(R.xml.settings_admin_layout);
//                    prefManager = getPreferenceManager();
//                    prefManager.findPreference("pref_change_password").setOnPreferenceClickListener(this);
//                    prefManager.findPreference("pref_open_cash_drawer").setOnPreferenceClickListener(this);
//                    prefManager.findPreference("pref_configure_cash_drawer").setOnPreferenceClickListener(this);
//                    prefManager.findPreference("pref_transaction_num_prefix").setOnPreferenceClickListener(this);
//                    prefManager.findPreference("pref_customer_display").setOnPreferenceClickListener(this);
//                    prefManager.findPreference("pref_clear_images_cache").setOnPreferenceClickListener(this);
//                    prefManager.findPreference("pref_printek_info").setOnPreferenceClickListener(this);
//                    prefManager.findPreference("pref_star_info").setOnPreferenceClickListener(this);
//                    prefManager.findPreference("pref_snbc_setup").setOnPreferenceClickListener(this);
//                    prefManager.findPreference("pref_configure_ingenico_settings").setOnPreferenceClickListener(this);
//                    prefManager.findPreference("pref_connect_to_bluetooth_peripheral").setOnPreferenceClickListener(this);
//                    prefManager.findPreference("pref_connect_to_usb_peripheral").setOnPreferenceClickListener(this);
//                    prefManager.findPreference("pref_redetect_peripherals").setOnPreferenceClickListener(this);
//                    prefManager.findPreference("pref_delete_saved_peripherals").setOnPreferenceClickListener(this);
//                    prefManager.findPreference("pref_force_upload").setOnPreferenceClickListener(this);
//                    prefManager.findPreference("pref_backup_data").setOnPreferenceClickListener(this);
//                    prefManager.findPreference("pref_send_handpoint_log").setOnPreferenceClickListener(this);
//                    prefManager.findPreference("pref_handpoint_update").setOnPreferenceClickListener(this);
//                    prefManager.findPreference("pref_check_updates").setOnPreferenceClickListener(this);
//                    prefManager.findPreference("pref_units_name").setOnPreferenceClickListener(this);
//                    prefManager.findPreference(MyPreferences.pref_config_genius_peripheral)
//                            .setOnPreferenceClickListener(this);
//                    configureDefaultCategory();
//                    configureDefaultPaymentMethod();
//
//                    prefManager.findPreference("pref_attribute_to_display").setOnPreferenceClickListener(this);
//
//                    openShiftPref = getPreferenceManager().findPreference("pref_open_shift");
//                    if (!myPref.getShiftIsOpen()) {
//                        CharSequence c = "\t\t" + getString(R.string.admin_close_shift) + " <" + myPref.getShiftClerkName() + ">";
//                        openShiftPref.setSummary(c);
//                    }
//                    openShiftPref.setOnPreferenceClickListener(this);
//
//                    prefManager.findPreference("pref_expenses").setOnPreferenceClickListener(this);
//
//                    defaultCountry = prefManager.findPreference("pref_default_country");
//                    CharSequence temp = "\t\t" + myPref.defaultCountryName(true, null);
//                    defaultCountry.setSummary(temp);
//                    defaultCountry.setOnPreferenceClickListener(this);
//
//                    CheckBoxPreference _cbp_use_location_inv = (CheckBoxPreference) prefManager
//                            .findPreference("pref_enable_location_inventory");
//
//                    storeForwardTransactions = prefManager
//                            .findPreference("pref_store_and_forward_transactions");
//                    storeForwardFlag = (CheckBoxPreference) prefManager.findPreference("pref_use_store_and_forward");
//                    storeForwardTransactions.setOnPreferenceClickListener(this);
//                    if (!myPref.isPrefUseStoreForward()) {
//                        ((PreferenceGroup) prefManager.findPreference("payment_section"))
//                                .removePreference(storeForwardTransactions);
//                        ((PreferenceGroup) prefManager.findPreference("payment_section"))
//                                .removePreference(storeForwardFlag);
//                    }
//                    // ((PreferenceGroup)prefManager.findPreference("payment_section")).removePreference(storeForwardTransactions);
//                    _cbp_use_location_inv.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
//
//                        @Override
//                        public boolean onPreferenceChange(Preference preference, Object newValue) {
//                            if (newValue instanceof Boolean) {
//                                if ((Boolean) newValue) {
//                                    // sync Position Inventory
//                                    DBManager dbManager = new DBManager(activity);
//                                    SynchMethods sm = new SynchMethods(dbManager);
//                                    sm.getLocationsInventory();
//                                }
//                            }
//                            return true;
//                        }
//                    });
                    break;
                case RESTAURANT:

                    break;
                case GIFTCARD:
                    prefManager.findPreference("pref_units_name").setOnPreferenceClickListener(this);
                    break;
                case PAYMENTS:
                    prefManager.findPreference(MyPreferences.pref_config_genius_peripheral)
                            .setOnPreferenceClickListener(this);
                    configureDefaultPaymentMethod();
                    storeForwardTransactions = prefManager
                            .findPreference("pref_store_and_forward_transactions");
                    storeForwardFlag = (CheckBoxPreference) prefManager.findPreference("pref_use_store_and_forward");
                    storeForwardTransactions.setOnPreferenceClickListener(this);
                    if (!myPref.isPrefUseStoreForward()) {
                        ((PreferenceGroup) prefManager.findPreference("payment_section"))
                                .removePreference(storeForwardTransactions);
                        ((PreferenceGroup) prefManager.findPreference("payment_section"))
                                .removePreference(storeForwardFlag);
                    }
                    break;
                case PRINTING:
                    prefManager.findPreference("pref_printek_info").setOnPreferenceClickListener(this);
                    prefManager.findPreference("pref_star_info").setOnPreferenceClickListener(this);
                    prefManager.findPreference("pref_snbc_setup").setOnPreferenceClickListener(this);
                    prefManager.findPreference("pref_configure_ingenico_settings").setOnPreferenceClickListener(this);
                    prefManager.findPreference("pref_connect_to_bluetooth_peripheral").setOnPreferenceClickListener(this);
                    prefManager.findPreference("pref_connect_to_usb_peripheral").setOnPreferenceClickListener(this);
                    prefManager.findPreference("pref_redetect_peripherals").setOnPreferenceClickListener(this);
                    prefManager.findPreference("pref_delete_saved_peripherals").setOnPreferenceClickListener(this);
                    break;
                case PRODUCTS:
                    configureDefaultCategory();
                    prefManager.findPreference("pref_attribute_to_display").setOnPreferenceClickListener(this);
                    break;
                case OTHERS:
                    prefManager.findPreference("pref_change_password").setOnPreferenceClickListener(this);
                    prefManager.findPreference("pref_open_cash_drawer").setOnPreferenceClickListener(this);
                    prefManager.findPreference("pref_configure_cash_drawer").setOnPreferenceClickListener(this);
                    prefManager.findPreference("pref_customer_display").setOnPreferenceClickListener(this);
                    prefManager.findPreference("pref_clear_images_cache").setOnPreferenceClickListener(this);
                    CheckBoxPreference _cbp_use_location_inv = (CheckBoxPreference) prefManager
                            .findPreference("pref_enable_location_inventory");
                    _cbp_use_location_inv.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

                        @Override
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            if (newValue instanceof Boolean) {
                                if ((Boolean) newValue) {
                                    // sync Position Inventory
                                    DBManager dbManager = new DBManager(getActivity());
                                    SynchMethods sm = new SynchMethods(dbManager);
                                    sm.getLocationsInventory();
                                }
                            }
                            return true;
                        }
                    });
                    openShiftPref = getPreferenceManager().findPreference("pref_open_shift");
                    if (!myPref.getShiftIsOpen()) {
                        CharSequence c = "\t\t" + getString(R.string.admin_close_shift) + " <" + myPref.getShiftClerkName() + ">";
                        openShiftPref.setSummary(c);
                    }
                    openShiftPref.setOnPreferenceClickListener(this);
                    prefManager.findPreference("pref_expenses").setOnPreferenceClickListener(this);
                    defaultCountry = prefManager.findPreference("pref_default_country");
                    CharSequence temp = "\t\t" + myPref.defaultCountryName(true, null);
                    defaultCountry.setSummary(temp);
                    defaultCountry.setOnPreferenceClickListener(this);
                    prefManager.findPreference("pref_force_upload").setOnPreferenceClickListener(this);
                    prefManager.findPreference("pref_backup_data").setOnPreferenceClickListener(this);
                    prefManager.findPreference("pref_send_handpoint_log").setOnPreferenceClickListener(this);
                    prefManager.findPreference("pref_handpoint_update").setOnPreferenceClickListener(this);
                    prefManager.findPreference("pref_check_updates").setOnPreferenceClickListener(this);

                    break;
            }

        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            myPref = new MyPreferences(getActivity());
            SettingSection section = SettingSection.getInstance(getArguments().getInt("section"));
            int layoutId = getLayoutId(section);
            PreferenceManager prefManager;
            // Load the preferences from an XML resource
            switch (settingsType) {
                case SettingsTab_FR.CASE_ADMIN:
                    addPreferencesFromResource(layoutId);
                    prefManager = getPreferenceManager();
                    setPrefManager(section, prefManager);
                    break;
                case SettingsTab_FR.CASE_MANAGER:
                    addPreferencesFromResource(R.xml.settings_manager_layout);
                    prefManager = getPreferenceManager();
                    prefManager.findPreference("pref_open_cash_drawer").setOnPreferenceClickListener(this);
                    prefManager.findPreference("pref_clear_images_cache").setOnPreferenceClickListener(this);
                    prefManager.findPreference("pref_connect_to_bluetooth_peripheral").setOnPreferenceClickListener(this);
                    prefManager.findPreference("pref_connect_to_usb_peripheral").setOnPreferenceClickListener(this);
                    prefManager.findPreference("pref_redetect_peripherals").setOnPreferenceClickListener(this);
                    prefManager.findPreference("pref_delete_saved_peripherals").setOnPreferenceClickListener(this);
                    openShiftPref = getPreferenceManager().findPreference("pref_open_shift");
                    if (!myPref.getShiftIsOpen()) {
                        CharSequence c = "\t\t" + getString(R.string.admin_close_shift) + " <" + myPref.getShiftClerkName() + ">";
                        openShiftPref.setSummary(c);
                    }
                    openShiftPref.setOnPreferenceClickListener(this);
                    prefManager.findPreference("pref_expenses").setOnPreferenceClickListener(this);

                    break;
                case SettingsTab_FR.CASE_GENERAL:
                    addPreferencesFromResource(R.xml.settings_general_layout);
                    prefManager = getPreferenceManager();
                    prefManager.findPreference("pref_clear_images_cache").setOnPreferenceClickListener(this);
                    prefManager.findPreference("pref_connect_to_bluetooth_peripheral").setOnPreferenceClickListener(this);
                    prefManager.findPreference("pref_connect_to_usb_peripheral").setOnPreferenceClickListener(this);
                    prefManager.findPreference("pref_redetect_peripherals").setOnPreferenceClickListener(this);
                    prefManager.findPreference("pref_delete_saved_peripherals").setOnPreferenceClickListener(this);
                    prefManager.findPreference("pref_toggle_elo_bcr").setOnPreferenceClickListener(this);
                    prefManager.findPreference("pref_use_navigationbar").setOnPreferenceClickListener(this);
                    break;

            }
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            Intent intent;
            switch (preference.getTitleRes()) {
                case R.string.config_use_navigationbar:
                    getActivity().finish();
                    getActivity().startActivity(getActivity().getIntent());
                    break;
                case R.string.config_toggle_elo_bcr:
                    if (Global.mainPrinterManager != null && Global.mainPrinterManager.currentDevice != null)
                        Global.mainPrinterManager.currentDevice.toggleBarcodeReader();
                    break;
                case R.string.config_change_password:
                    changePassword(false, null);
                    break;
                case R.string.config_open_cash_drawer:
                    if (Global.mainPrinterManager != null && Global.mainPrinterManager.currentDevice != null)
                        Global.mainPrinterManager.currentDevice.openCashDrawer();
                    break;
                case R.string.config_configure_cash_drawer:
                    break;
                case R.string.config_transaction_num_prefix:
                    break;
                case R.string.config_customer_display:
                    configureCustomerDisplayTerminal();
                    break;
                case R.string.config_units_name:
                    setDefaultUnitsName();
                    break;
                case R.string.config_clear_images_cache:
                    clearCache();
                    break;
                case R.string.config_printek_info:
                    break;
                case R.string.config_genius_peripheral:
                    configureGeniusPeripheral();
                    break;
                case R.string.config_star_info:
                    promptStarPrinter();
                    break;
                case R.string.config_snbc_setup:
                    promptSNBCSetup();
                    break;
                case R.string.config_configure_ingenico_settings:
                    break;
                case R.string.config_connect_to_bluetooth_peripheral:
                    connectBTDevice();
                    break;
                case R.string.config_connect_to_usb_peripheral:
                    connectUSBDevice();
                    break;
                case R.string.config_redetect_peripherals:
                    new autoConnectPrinter().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    break;
                case R.string.config_store_and_forward_transactions:
                    intent = new Intent(getActivity(), ViewStoreForwardTrans_FA.class);
                    startActivity(intent);
                    break;
                case R.string.config_delete_saved_peripherals:
                    myPref.forgetPeripherals();
                    Toast.makeText(getActivity(), "Peripherals have been erased", Toast.LENGTH_LONG).show();
                    break;
                case R.string.config_attribute_to_display:
                    break;
                case R.string.config_open_shift:
                    if (myPref.getShiftIsOpen()) {
                        intent = new Intent(getActivity(), OpenShift_FA.class);
                        startActivityForResult(intent, 0);
                    } else
                        promptCloseShift(true, 0);
                    break;
                case R.string.config_expenses:
                    String spID = myPref.getShiftID();
                    //if shift is open then show the expenses option
                    if (spID.isEmpty()) {
                        Toast.makeText(getActivity(), "A shift must be opened before an expense can be added!", Toast.LENGTH_LONG).show();
                    } else {
                        intent = new Intent(getActivity(), ShiftExpensesList_FA.class);
                        startActivity(intent);
                    }
                    break;
                case R.string.config_default_country:
                    CountryPicker picker = new CountryPicker();
                    final DialogFragment newFrag = picker;
                    picker.setListener(new CountryPickerListener() {

                        @Override
                        public void onSelectCountry(String name, String code) {
                            myPref.defaultCountryCode(false, code);
                            myPref.defaultCountryName(false, name);

                            CharSequence temp = "\t\t" + name;
                            defaultCountry.setSummary(temp);

                            newFrag.dismiss();
                        }
                    });

                    newFrag.show(supportFragmentManager, "dialog");
                    break;
                case R.string.config_force_upload:
                    confirmTroubleshoot(R.string.config_force_upload);
                    break;
                case R.string.config_check_updates:
                    new HttpClient().downloadFileAsync(getString(R.string.check_update_url), Environment.getExternalStorageDirectory().getAbsolutePath() + "/emobilepos.apk", this, getActivity());
                    break;
                case R.string.config_backup_data:
                    confirmTroubleshoot(R.string.config_backup_data);
                    break;
                case R.string.config_send_handpoint_log:
                    if (myPref.getSwiperType() == Global.HANDPOINT && Global.btSwiper.currentDevice != null) {
                        Global.btSwiper.currentDevice.sendEmailLog();
                    }
                    break;
                case R.string.config_handpoint_update:
                    if (myPref.getSwiperType() == Global.HANDPOINT && Global.btSwiper.currentDevice != null) {
                        Global.btSwiper.currentDevice.updateFirmware();
                    }
                    break;
            }
            return false;
        }


        private void changePassword(final boolean isReenter, final String origPwd) {
            final Dialog globalDlog = new Dialog(getActivity(), R.style.Theme_TransparentTest);
            globalDlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            globalDlog.setCancelable(true);
            globalDlog.setCanceledOnTouchOutside(true);
            globalDlog.setContentView(R.layout.dlog_field_single_layout);

            final EditText viewField = (EditText) globalDlog.findViewById(R.id.dlogFieldSingle);
            viewField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            TextView viewTitle = (TextView) globalDlog.findViewById(R.id.dlogTitle);
            TextView viewMsg = (TextView) globalDlog.findViewById(R.id.dlogMessage);
            viewTitle.setText(R.string.dlog_title_confirm);
            if (!isReenter) {
                viewTitle.setText(R.string.enter_password);
                viewMsg.setText(R.string.password_five_char_long);
            } else {
                viewTitle.setText(R.string.reenter_password);
                viewMsg.setVisibility(View.GONE);
            }
            Button btnCancel = (Button) globalDlog.findViewById(R.id.btnCancelDlogSingle);
            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    globalDlog.dismiss();
                }
            });
            Button btnOk = (Button) globalDlog.findViewById(R.id.btnDlogSingle);
            btnOk.setText(R.string.button_ok);
            btnOk.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    globalDlog.dismiss();

                    String value = viewField.getText().toString().trim();
                    if (!isReenter && value.length() >= 5) {
                        changePassword(true, value);
                    } else if (isReenter && origPwd != null && value.equals(origPwd)) {
                        myPref.setApplicationPassword(value);
                    }
                }
            });
            globalDlog.show();
        }

        private void setDefaultUnitsName() {
            final Dialog globalDlog = new Dialog(getActivity(), R.style.Theme_TransparentTest);
            globalDlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            globalDlog.setCancelable(true);
            globalDlog.setCanceledOnTouchOutside(true);
            globalDlog.setContentView(R.layout.dlog_field_single_layout);

            final EditText viewField = (EditText) globalDlog.findViewById(R.id.dlogFieldSingle);
            viewField.setInputType(InputType.TYPE_CLASS_TEXT);
            if (!TextUtils.isEmpty(myPref.getDefaultUnitsName())) {
                viewField.setText(myPref.getDefaultUnitsName());
            }
            TextView viewTitle = (TextView) globalDlog.findViewById(R.id.dlogTitle);
            TextView viewMsg = (TextView) globalDlog.findViewById(R.id.dlogMessage);
            viewTitle.setText(R.string.dlog_title_confirm);
            viewTitle.setText(R.string.enter_default_units_name);
            viewMsg.setVisibility(View.GONE);
            Button btnCancel = (Button) globalDlog.findViewById(R.id.btnCancelDlogSingle);
            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    globalDlog.dismiss();
                }
            });
            Button btnOk = (Button) globalDlog.findViewById(R.id.btnDlogSingle);
            btnOk.setText(R.string.button_ok);
            btnOk.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    globalDlog.dismiss();
                    String value = viewField.getText().toString().trim();
                    myPref.setDefaultUnitsName(value);
                }
            });
            globalDlog.show();
        }


        private void configureDefaultCategory() {
            ListPreference lp = (ListPreference) getPreferenceManager()
                    .findPreference(MyPreferences.pref_default_category);
            CategoriesHandler handler = new CategoriesHandler(getActivity());
            List<String[]> categories = handler.getCategories();
            int size = categories.size();
            CharSequence[] catEntries = new String[size + 1];
            CharSequence[] catEntriesValues = new String[size + 1];

            if (size > 0) {
                catEntries[0] = "None";
                catEntriesValues[0] = "0";
                for (int i = 0; i < size; i++) {
                    catEntries[i + 1] = categories.get(i)[0];
                    catEntriesValues[i + 1] = categories.get(i)[1];
                }
            }

            if (catEntries[0] == null || catEntriesValues[0] == null) {
                catEntries[0] = "None";
                catEntriesValues[0] = "0";
            }

            lp.setEntries(catEntries);
            lp.setEntryValues(catEntriesValues);
        }

        private void configureDefaultPaymentMethod() {
            ListPreference lp = (ListPreference) getPreferenceManager()
                    .findPreference(MyPreferences.pref_default_payment_method);
            PayMethodsHandler handler = new PayMethodsHandler(getActivity());
            List<PaymentMethod> list = handler.getPayMethod();
            int size = list.size();
            CharSequence[] entries = new String[size + 1];
            CharSequence[] entriesValues = new String[size + 1];
            if (size > 0) {
                entries[0] = "None";
                entriesValues[0] = "0";
                for (int i = 0; i < size; i++) {
                    entries[i + 1] = list.get(i).getPaymethod_name();
                    entriesValues[i + 1] = list.get(i).getPaymethod_id();
                }
            }
            if (entries[0] == null || entriesValues[0] == null) {
                entries[0] = "None";
                entriesValues[0] = "0";
            }
            lp.setEntries(entries);
            lp.setEntryValues(entriesValues);
        }

        private void configureCustomerDisplayTerminal() {
            final Dialog globalDlog = new Dialog(getActivity(), R.style.Theme_TransparentTest);
            globalDlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            globalDlog.setCancelable(true);
            globalDlog.setCanceledOnTouchOutside(true);
            globalDlog.setContentView(R.layout.dlog_field_double_layout);

            final EditText row1 = (EditText) globalDlog.findViewById(R.id.dlogFieldRow1);
            final EditText row2 = (EditText) globalDlog.findViewById(R.id.dlogFieldRow2);

            row1.setHint(myPref.cdtLine1(true, null));
            row2.setHint(myPref.cdtLine2(true, null));
            TextView viewTitle = (TextView) globalDlog.findViewById(R.id.dlogTitle);
            TextView viewMsg = (TextView) globalDlog.findViewById(R.id.dlogMessage);
            viewTitle.setText(R.string.dlog_title_customer_display);
            viewMsg.setText(R.string.dlog_msg_enter_data);

            Button btnOk = (Button) globalDlog.findViewById(R.id.btnDlogSingle);
            btnOk.setText(R.string.button_ok);
            btnOk.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    String value1 = row1.getText().toString().trim();
                    String value2 = row2.getText().toString().trim();
                    int size1 = value1.length();
                    int size2 = value2.length();
                    if (size1 > 20 || size2 > 20) {
                        if (size1 > 20)
                            row1.setText("");
                        if (size2 > 20)
                            row2.setText("");

                        Toast.makeText(getActivity(), "Only 20 characters are allowed per line", Toast.LENGTH_LONG).show();
                    } else {
                        myPref.cdtLine1(false, value1);
                        myPref.cdtLine2(false, value2);

//                        if (myPref.isSam4s(true, true)) {
                        Global.showCDTDefault(getActivity());
//                        }

                        globalDlog.dismiss();
                    }
                }
            });
            globalDlog.show();
        }

        private void clearCache() {
            File cacheDir = new File(myPref.getCacheDir());
            File[] files = cacheDir.listFiles();
            if (files != null) {
                for (File file : files)
                    file.delete();
            }
            Toast.makeText(getActivity(), "Cache cleared", Toast.LENGTH_LONG).show();
        }

        private void confirmTroubleshoot(final int type) {
            promptDialog = new Dialog(getActivity(), R.style.Theme_TransparentTest);
            promptDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            promptDialog.setCancelable(false);
            promptDialog.setContentView(R.layout.dlog_btn_left_right_layout);

            TextView viewTitle = (TextView) promptDialog.findViewById(R.id.dlogTitle);
            TextView viewMsg = (TextView) promptDialog.findViewById(R.id.dlogMessage);
            viewTitle.setText(R.string.dlog_title_confirm);
            if (type == R.string.config_force_upload)
                viewMsg.setText(R.string.dlog_msg_confirm_force_upload);
            else
                viewMsg.setText(R.string.dlog_msg_confirm_backup_data);
            promptDialog.findViewById(R.id.btnDlogCancel).setVisibility(View.GONE);

            Button btnYes = (Button) promptDialog.findViewById(R.id.btnDlogLeft);
            Button btnNo = (Button) promptDialog.findViewById(R.id.btnDlogRight);
            btnYes.setText(R.string.button_yes);
            btnNo.setText(R.string.button_no);

            btnYes.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    promptDialog.dismiss();
                    switch (type) {
                        case R.string.config_force_upload:
                            DBManager dbManager = new DBManager(getActivity(), Global.FROM_SYNCH_ACTIVITY);
                            // SQLiteDatabase db = dbManager.openWritableDB();
                            dbManager.forceSend();
                            break;
                        case R.string.config_backup_data:
                            DBManager manag = new DBManager(getActivity());
                            manag.exportDBFile();
                            break;
                    }
                }
            });
            btnNo.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    promptDialog.dismiss();
                }
            });
            promptDialog.show();
        }

        private void configureGeniusPeripheral() {
            final MyPreferences myPref = new MyPreferences(getActivity());
            final EditText input = new EditText(getActivity());
            input.setText(myPref.getGeniusIP());
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            dialogBuilder = new AlertDialog.Builder(getActivity());
            input.setSelection(input.getText().length());
            dialogBuilder.setView(input);

            promptDialog = dialogBuilder.setTitle("Enter Genius IP Address")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface thisDialog, int which) {
                            final String firstValue = input.getText().toString();
                            if (firstValue.length() > 0) {
                                myPref.setGeniusIP(firstValue);
                                thisDialog.dismiss();
                            } else
                                promptDialog.setTitle("Try again...");

                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface thisDialog, int which) {
                            thisDialog.dismiss();

                        }
                    }).create();

            promptDialog.show();
        }

        private void promptStarPrinter() {
            final EditText ipAddress = new EditText(getActivity());
            final EditText portNumber = new EditText(getActivity());

            ipAddressFilter(ipAddress);

            ipAddress.setHint(R.string.dlog_star_ip);
            portNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
            portNumber.setHint(R.string.dlog_star_port);

            ipAddress.setText(myPref.getStarIPAddress());
            portNumber.setText(myPref.getStarPort());
            LinearLayout ll = new LinearLayout(getActivity());
            ll.setOrientation(LinearLayout.VERTICAL);
            ll.addView(ipAddress);
            ll.addView(portNumber);

            dialogBuilder = new AlertDialog.Builder(getActivity());
            dialogBuilder.setView(ll);
            dialogBuilder.setTitle(R.string.dlog_star_congifure);

            dialogBuilder.setCancelable(true);
            dialogBuilder.setPositiveButton(R.string.button_connect, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                    myPref.setStarIPAddress(ipAddress.getText().toString());
                    myPref.setStarPort(portNumber.getText().toString());
                    myPref.setPrinterMACAddress("TCP:" + ipAddress.getText().toString());

                    EMSDeviceManager edm = new EMSDeviceManager();
                    Global.mainPrinterManager = edm.getManager();
                    Global.mainPrinterManager.loadDrivers(getActivity(), Global.STAR, true);

                }
            }).create();
            promptDialog = dialogBuilder.create();

            promptDialog.show();
        }

        private void promptSNBCSetup() {
            final Dialog dlog = new Dialog(getActivity(), R.style.Theme_TransparentTest);
            dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dlog.setCancelable(true);
            dlog.setCanceledOnTouchOutside(false);
            dlog.setContentView(R.layout.config_snbc_setup_layout);

            final Button btnConnect = (Button) dlog.findViewById(R.id.btnConnect);

            btnConnect.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    myPref.setPrinterType(Global.SNBC);
                    // myPref.printerMACAddress(false, macAddressList.get(pos));

                    EMSDeviceManager edm = new EMSDeviceManager();
                    Global.mainPrinterManager = edm.getManager();
                    Global.mainPrinterManager.loadDrivers(getActivity(), Global.SNBC, false);
                    dlog.dismiss();
                }
            });
            dlog.show();
        }

        private void ipAddressFilter(EditText et) {
            et.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            // et.setInputType(InputType.TYPE_CLASS_PHONE);
            et.setFilters(new InputFilter[]{new InputFilter() {
                @Override
                public CharSequence filter(CharSequence source, int start, int end, android.text.Spanned dest,
                                           int dstart, int dend) {
                    if (end > start) {
                        String destTxt = dest.toString();
                        String resultingTxt = destTxt.substring(0, dstart) + source.subSequence(start, end)
                                + destTxt.substring(dend);
                        if (!resultingTxt.matches("^\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3})?)?)?)?)?)?")) {
                            return "";
                        } else {
                            String[] splits = resultingTxt.split("\\.");
                            for (String split : splits) {
                                if (Integer.valueOf(split) > 255) {
                                    return "";
                                }
                            }
                        }
                    }
                    return null;
                }
            }});

            et.addTextChangedListener(new TextWatcher() {
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
                        }
                    }
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    deleting = lastCount >= count;
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // Nothing happens here
                }
            });
        }

        private void promptCloseShift(final boolean askAmount, final double amount) {
            final MyPreferences myPref = new MyPreferences(getActivity());
            final Dialog dlog = new Dialog(getActivity(), R.style.Theme_TransparentTest);
            dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dlog.setCancelable(false);
            dlog.setCanceledOnTouchOutside(false);
            dlog.setContentView(R.layout.dlog_field_single_two_btn);

            final EditText viewField = (EditText) dlog.findViewById(R.id.dlogFieldSingle);
            TextView viewTitle = (TextView) dlog.findViewById(R.id.dlogTitle);
            TextView viewMsg = (TextView) dlog.findViewById(R.id.dlogMessage);
            if (askAmount) {
                viewField.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                viewTitle.setText(R.string.enter_cash_close_amount);
                viewMsg.setVisibility(View.GONE);

            } else {
                viewField.setVisibility(View.GONE);
                viewTitle.setText(R.string.dlog_title_confirm);
                viewMsg.setText(
                        getActivity().getString(R.string.close_amount_is) + " " + Global.formatDoubleToCurrency(amount));

            }
            Button btnYes = (Button) dlog.findViewById(R.id.btnDlogLeft);
            Button btnNo = (Button) dlog.findViewById(R.id.btnDlogRight);
            btnYes.setText(R.string.button_ok);
            btnNo.setText(R.string.button_cancel);

            btnYes.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    dlog.dismiss();
                    if (askAmount) {
                        promptCloseShift(false, Global.formatNumFromLocale(viewField.getText().toString()));
                    } else {

                        ShiftPeriodsDBHandler handler = new ShiftPeriodsDBHandler(getActivity());
                        handler.updateShift(myPref.getShiftID(), "entered_close_amount", Double.toString(amount));
                        handler.updateShift(myPref.getShiftID(), "endTime", Global.getCurrentDate());
                        handler.updateShift(myPref.getShiftID(), "endTimeLocal", Global.getCurrentDate());

                        myPref.setShiftIsOpen(true);
                        myPref.setShiftID(""); //erase the shift ID
                        openShiftPref.setSummary("");
                    }
                }
            });
            btnNo.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    dlog.dismiss();
                }
            });
            dlog.show();
        }

        private void connectBTDevice() {
            ListView listViewPairedDevices = new ListView(getActivity());
            ArrayAdapter<String> bondedAdapter;
            dialogBuilder = new AlertDialog.Builder(getActivity());

            List<String> pairedDevicesList = getListPairedDevices();
            final String[] val = pairedDevicesList.toArray(new String[pairedDevicesList.size()]);
            bondedAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, val);
            listViewPairedDevices.setAdapter(bondedAdapter);

            listViewPairedDevices.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

                @Override
                public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int pos, long arg3) {
                    promptDialog.dismiss();

                    dialogBuilder = new AlertDialog.Builder(getActivity());
                    dialogBuilder.setTitle(R.string.dlog_title_connect_to_device);
                    dialogBuilder.setMessage(val[pos]);
                    dialogBuilder.setNegativeButton(R.string.button_no, null);
                    dialogBuilder.setPositiveButton(R.string.button_yes, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            MyPreferences myPref = new MyPreferences(getActivity());
                            String strDeviceName = val[pos];

                            if (val[pos].toUpperCase(Locale.getDefault()).contains("MAGTEK")) // magtek
                            // swiper
                            {
                                myPref.setSwiperType(Global.MAGTEK);
                                myPref.setSwiperMACAddress(macAddressList.get(pos));

                                EMSDeviceManager edm = new EMSDeviceManager();
                                Global.btSwiper = edm.getManager();
                                Global.btSwiper.loadDrivers(getActivity(), Global.MAGTEK, false);

                            } else if (val[pos].toUpperCase(Locale.getDefault()).contains("STAR")) // star
                            // micronics
                            {
                                myPref.setPrinterType(Global.STAR);
                                myPref.setPrinterMACAddress("BT:" + macAddressList.get(pos));
                                myPref.setPrinterName(strDeviceName);

                                EMSDeviceManager edm = new EMSDeviceManager();
                                Global.mainPrinterManager = edm.getManager();
                                Global.mainPrinterManager.loadDrivers(getActivity(), Global.STAR, false);

                            } else if (val[pos].toUpperCase(Locale.getDefault()).contains("SPP-R")) { // Bixolon
                                myPref.setPrinterType(Global.BIXOLON);
                                myPref.setPrinterMACAddress("BT:" + macAddressList.get(pos));
                                myPref.setPrinterName(strDeviceName);
                                EMSDeviceManager edm = new EMSDeviceManager();
                                Global.mainPrinterManager = edm.getManager();
                                Global.mainPrinterManager.loadDrivers(getActivity(), Global.BIXOLON, false);
                            } else if (val[pos].toUpperCase(Locale.getDefault()).contains("P25")) // bamboo
                            {
                                myPref.setPrinterType(Global.BAMBOO);
                                myPref.setPrinterMACAddress(macAddressList.get(pos));
                                myPref.setPrinterName(strDeviceName);

                                EMSDeviceManager edm = new EMSDeviceManager();
                                Global.mainPrinterManager = edm.getManager();
                                Global.mainPrinterManager.loadDrivers(getActivity(), Global.BAMBOO, false);

                            } else if (val[pos].toUpperCase(Locale.getDefault()).contains("ISMP")
                                    || (val[pos].toUpperCase(Locale.getDefault()).contains("ICM") &&
                                    !getActivity().getPackageName().equalsIgnoreCase(Global.EVOSNAP_PACKAGE_NAME))) {
                                myPref.setSwiperType(Global.ISMP);
                                myPref.setSwiperMACAddress(macAddressList.get(pos));
                                myPref.setSwiperName(strDeviceName);
                                EMSDeviceManager edm = new EMSDeviceManager();
                                Global.btSwiper = edm.getManager();
                                Global.btSwiper.loadDrivers(getActivity(), Global.ISMP, false);
                            } else if (val[pos].toUpperCase(Locale.getDefault()).contains("EM220")) // Zebra
                            {
                                myPref.setPrinterType(Global.ZEBRA);
                                myPref.setPrinterMACAddress(macAddressList.get(pos));
                                myPref.setPrinterName(strDeviceName);

                                EMSDeviceManager edm = new EMSDeviceManager();
                                Global.mainPrinterManager = edm.getManager();
                                Global.mainPrinterManager.loadDrivers(getActivity(), Global.ZEBRA, false);
                            } else if (val[pos].toUpperCase(Locale.getDefault()).contains("MP")) // Oneil
                            {
                                myPref.setPrinterType(Global.ONEIL);
                                myPref.setPrinterMACAddress(macAddressList.get(pos));
                                myPref.setPrinterName(strDeviceName);

                                EMSDeviceManager edm = new EMSDeviceManager();
                                Global.mainPrinterManager = edm.getManager();
                                Global.mainPrinterManager.loadDrivers(getActivity(), Global.ONEIL, false);
                            } else if (val[pos].toUpperCase(Locale.getDefault()).contains("KDC500")) // KDC500
                            {
                                myPref.setPrinterType(Global.KDC500);
                                myPref.setPrinterMACAddress(macAddressList.get(pos));
                                myPref.setSwiperType(Global.KDC500);
                                EMSDeviceManager edm = new EMSDeviceManager();
                                Global.mainPrinterManager = edm.getManager();
                                Global.mainPrinterManager.loadDrivers(getActivity(), Global.KDC500, false);
                            } else if (val[pos].toUpperCase(Locale.getDefault()).contains("PP0615")) {
                                myPref.setSwiperType(Global.HANDPOINT);
                                myPref.setSwiperMACAddress(macAddressList.get(pos));
                                myPref.setSwiperName(strDeviceName);

                                EMSDeviceManager edm = new EMSDeviceManager();
                                Global.btSwiper = edm.getManager();
                                Global.btSwiper.loadDrivers(getActivity(), Global.HANDPOINT, false);

                            } else if (val[pos].toUpperCase(Locale.getDefault()).contains("ICM") &&
                                    getActivity().getPackageName().equalsIgnoreCase(Global.EVOSNAP_PACKAGE_NAME)) {
                                myPref.setSwiperType(Global.ICMPEVO);
                                myPref.setPrinterMACAddress(macAddressList.get(pos));
                                myPref.setSwiperName(strDeviceName);
                                EMSDeviceManager edm = new EMSDeviceManager();
                                Global.btSwiper = edm.getManager();
                                Global.btSwiper.loadDrivers(getActivity(), Global.ICMPEVO, false);
                                Global.btSwiper = edm.getManager();

                            } else {
                                Toast.makeText(getActivity(), R.string.err_invalid_device, Toast.LENGTH_LONG).show();
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

        private void connectUSBDevice() {

            MyPreferences myPref = new MyPreferences(getActivity());
            EMSDeviceManager edm = new EMSDeviceManager();

            if (myPref.isAsura(true, false)) {
                myPref.setPrinterType(Global.ASURA);
                Global.mainPrinterManager = edm.getManager();
                Global.mainPrinterManager.loadDrivers(getActivity(), Global.ASURA, false);

            } else if (myPref.isPAT100()) {
                myPref.setPrinterType(Global.PAT100);
                Global.mainPrinterManager = edm.getManager();
                Global.mainPrinterManager.loadDrivers(getActivity(), Global.PAT100, false);
            } else if (myPref.isPAT215()) {
                edm = new EMSDeviceManager();
                myPref.setPrinterType(Global.PAT215);
                Global.embededMSR = edm.getManager();
                Global.embededMSR.loadDrivers(getActivity(), Global.PAT215, false);
            } else if (myPref.isEM100()) {

            } else if (myPref.isEM70()) {
                myPref.setPrinterType(Global.EM70);
                Global.mainPrinterManager = edm.getManager();
                Global.mainPrinterManager.loadDrivers(getActivity(), Global.EM70, false);
            } else if (myPref.isESY13P1()) {
                myPref.setPrinterType(Global.ESY13P1);
                Global.mainPrinterManager = edm.getManager();
                Global.mainPrinterManager.loadDrivers(getActivity(), Global.ESY13P1, false);
            } else if (myPref.isOT310()) {
                myPref.setPrinterType(Global.OT310);
                Global.mainPrinterManager = edm.getManager();
                Global.mainPrinterManager.loadDrivers(getActivity(), Global.OT310, false);
            } else {
                myPref.setPrinterType(Global.POWA);
                Global.mainPrinterManager = edm.getManager();
                Global.mainPrinterManager.loadDrivers(getActivity(), Global.POWA, false);
            }
        }

        private List<String> getListPairedDevices() {
            List<String> nameList = new ArrayList<>();
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
                StringBuilder sb = new StringBuilder();
                sb.append(e.getMessage())
                        .append(" [com.android.emobilepos.SettingsMenuActiv (at Class.getPairedDevices)]");

            }
            return null;
        }

        @Override
        public void downloadCompleted(String path) {
            final File file = new File(path);
            final Intent i = new Intent(Intent.ACTION_VIEW);
            i.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            this.startActivity(i);
        }

        @Override
        public void downloadFail() {
            Global.showPrompt(getActivity(), R.string.dlog_title_error, getString(R.string.check_update_fail));
        }

        private class autoConnectPrinter extends AsyncTask<Void, Void, String> {
            private ProgressDialog progressDlog;

            @Override
            protected void onPreExecute() {
                progressDlog = new ProgressDialog(getActivity());
                progressDlog.setMessage("Connecting...");
                progressDlog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDlog.setCancelable(false);
                progressDlog.show();
            }

            @Override
            protected String doInBackground(Void... params) {
                return DeviceUtils.autoConnect(getActivity(), true);

            }

            @Override
            protected void onPostExecute(String result) {
                progressDlog.dismiss();
                if (result.length() > 0)
                    Global.showPrompt(getActivity(), R.string.dlog_title_confirm, result);
            }
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {

            if (resultCode == 1) {
                if (!myPref.getShiftIsOpen()) {
                    CharSequence c = "\t\t" + getString(R.string.admin_close_shift) + " <" + myPref.getShiftClerkName() + ">";
                    openShiftPref.setSummary(c);
                }
            }
        }

    }
}
