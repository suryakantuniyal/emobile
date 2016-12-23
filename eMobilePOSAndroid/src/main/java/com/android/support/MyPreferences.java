package com.android.support;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;
import android.util.Base64;

import com.android.database.SalesTaxCodesHandler;

import java.security.AccessControlException;
import java.security.Guard;
import java.security.GuardedObject;
import java.util.Arrays;
import java.util.List;
import java.util.PropertyPermission;
import java.util.Set;

import util.json.UIUtils;

public class MyPreferences {
    private SharedPreferences.Editor prefEditor;
    private SharedPreferences prefs;
    private Activity activity;
    private Global global;

    private final String MY_SHARED_PREF = "MY_SHARED_PREF";

    public enum PrinterPreviewWidth {SMALL, MEDIUM, LARGE}

    private final String db_path = "db_path";
    private final String emp_id = "emp_id";
    private final String ActivationKey = "ActivationKey";
    private final String DeviceID = "DeviceID";
    private final String BundleVersion = "BundleVersion";
    private final String ApplicationPassword = "ApplicationPassword";
    private final String AccountNumber = "AccountNumber";
    private final String AccountPassword = "AccountPassword";
    private final String LoginPass = "LoginPass";
    private final String zone_id = "zone_id";
    private final String VAT = "VAT";

    private final String emp_name = "emp_name";
    private final String emp_lastlogin = "emp_lastlogin";
    private final String emp_pos = "emp_pos";
    private final String MSOrderEntry = "MSOrderEntry";
    private final String MSCardProcessor = "MSCardProcessor";
    private final String GatewayURL = "GatewayURL";
    private final String approveCode = "approveCode";
    private final String MSLastOrderID = "MSLastOrderID";
    private final String tax_default = "tax_default";
    private final String pricelevel_id = "pricelevel_id";
    private final String pay_id = "pay_id";
    private final String isLoggedIn = "isLoggedIn";
    private final String cust_id = "cust_id";
    private final String cust_selected = "cust_selected";
    private final String cust_name = "cust_name";
    private final String cust_pricelevel_id = "cust_pricelevel_id";
    private final String cust_email = "cust_email";
    private final String ConsTrans_ID = "ConsTrans_ID";
    private final String MSLastTransferID = "MSLastTransferID";

    // Settings from the 'Settings' app menu
    private final String fast_scanning = "fast_scanning";
    private final String signa_required = "signa_required";
    private final String allow_decimal = "allow_decimal";
    private final String group_sku = "group_sku";
    private final String enable_printing = "enable_printing";
    private final String maps_inside = "maps_inside";
    private final String block_pricelevel = "block_pricelevel";
    private final String require_addr = "require_addr";
    private final String admin_override = "admin_override";

    private final String is_tablet = "is_tablet";
    private final String bluebamboo_mac_address = "bluebamboo_mac_address";

    // Synch window
    private final String last_send_sync = "last_send_sync";
    private final String last_receive_sync = "last_receive_sync";

    // keys
    private final String rsa_priv_key = "rsa_priv_key";
    private final String rsa_pub_key = "rsa_pub_key";
    private final String aes_key = "aes_key";
    private final String aes_iv = "aes_iv";

    public static final String pref_restaurant_mode = "pref_restaurant_mode";
    public static final String pref_enable_togo_eatin = "pref_enable_togo_eatin";
    public static final String pref_require_waiter_signin = "pref_require_waiter_signin";
    public static final String pref_enable_table_selection = "pref_enable_table_selection";
    public static final String pref_ask_seats = "pref_ask_seats";
    public static final String pref_use_navigationbar = "pref_use_navigationbar";


    public static final String pref_automatic_sync = "pref_automatic_sync";
    public static final String pref_fast_scanning_mode = "pref_fast_scanning_mode";
    public static final String pref_signature_required_mode = "pref_signature_required_mode";
    public static final String pref_qr_code_reading = "pref_qr_code_reading";
    public static final String pref_enable_multi_category = "pref_enable_multi_category";
    public static final String pref_ask_order_comments = "pref_ask_order_comments";
    public static final String pref_skip_email_phone = "pref_skip_email_phone";
    public static final String pref_show_only_group_taxes = "pref_show_only_group_taxes";
    public static final String pref_retail_taxes = "pref_retail_taxes";
    public static final String pref_mix_match = "pref_mix_match";
    public static final String pref_require_customer = "pref_require_customer";
    public static final String pref_show_confirmation_screen = "pref_show_confirmation_screen";
    public static final String pref_direct_customer_selection = "pref_direct_customer_selection";
    public static final String pref_display_customer_account_number = "pref_display_customer_account_number";
    public static final String pref_skip_want_add_more_products = "pref_skip_want_add_more_products";
    public static final String pref_require_shift_transactions = "pref_require_shift_transactions";
    public static final String pref_allow_customer_creation = "pref_allow_customer_creation";
    public static final String pref_scope_bar_in_restaurant_mode = "pref_scope_bar_in_restaurant_mode";
    public static final String pref_display_also_redeem = "pref_display_also_redeem";
    public static final String pref_display_redeem_all = "pref_display_redeem_all";
    public static final String pref_use_loyal_patron = "pref_use_loyal_patron";
    public static final String pref_pay_with_tupyx = "pref_pay_with_tupyx";
    public static final String pref_mw_with_genius = "pref_mw_with_genius";
    public static final String pref_config_genius_peripheral = "pref_config_genius_peripheral";
    public static final String pref_use_clerks = "pref_use_clerks";
    public static final String pref_enable_location_inventory = "pref_enable_location_inventory";

    public static final String pref_block_price_level_change = "pref_block_price_level_change";
    public static final String pref_require_address = "pref_require_address";
    public static final String pref_require_po = "pref_require_po";
    public static final String pref_skip_manager_price_override = "pref_skip_manager_price_override";
    public static final String pref_require_password_to_clockout = "pref_require_password_to_clockout";
    public static final String pref_maps_inside_app = "pref_maps_inside_app";
    public static final String pref_process_check_online = "pref_process_check_online";
    public static final String pref_allow_manual_credit_card = "pref_allow_manual_credit_card";
    public static final String pref_show_tips_for_cash = "pref_show_tips_for_cash";
    public static final String pref_audio_card_reader = "pref_audio_card_reader";
    public static final String pref_prefill_total_amount = "pref_prefill_total_amount";
    public static final String pref_use_store_and_forward = "pref_use_store_and_forward";

    public static final String pref_return_require_refund = "pref_return_require_refund";
    public static final String pref_convert_to_reward = "pref_convert_to_reward";
    public static final String pref_invoice_require_payment = "pref_invoice_require_payment";
    public static final String pref_invoice_require_full_payment = "pref_invoice_require_full_payment";
    public static final String pref_printek_info = "pref_printek_info";
    public static final String pref_automatic_printing = "pref_automatic_printing";
    public static final String pref_split_stationprint_by_categories = "pref_split_stationprint_by_categories";
    public static final String pref_enable_printing = "pref_enable_printing";

    public static final String pref_wholesale_printout = "pref_wholesale_printout";
    public static final String pref_handwritten_signature = "pref_handwritten_signature";
    public static final String pref_prompt_customer_copy = "pref_prompt_customer_copy";
    public static final String pref_print_receipt_transaction_payment = "pref_print_receipt_transaction_payment";

    public static final String pref_allow_decimal_quantities = "pref_allow_decimal_quantities";
    public static final String pref_group_receipt_by_sku = "pref_group_receipt_by_sku";
    public static final String pref_require_password_to_remove_void = "pref_require_password_to_remove_void";
    public static final String pref_show_removed_void_items_in_printout = "pref_show_removed_void_items_in_printout";
    public static final String pref_limit_products_on_hand = "pref_limit_products_on_hand";
    public static final String pref_attribute_to_display = "pref_attribute_to_display";
    public static final String pref_printer_width = "pref_printer_width";

    public static final String pref_group_in_catalog_by_name = "pref_group_in_catalog_by_name";
    public static final String pref_filter_products_by_customer = "pref_filter_products_by_customer";
    public static final String pref_use_nexternal = "pref_use_nexternal";
    public static final String pref_require_manager_pass_to_void_trans = "pref_require_manager_pass_to_void_trans";
    public static final String pref_default_country = "pref_default_country";

    public static final String pref_default_transaction = "pref_default_transaction";
    public static final String pref_default_category = "pref_default_category";
    public static final String pref_default_payment_method = "pref_default_payment_method";

    public static final String print_header = "print_header";
    public static final String print_shiptoinfo = "print_shiptoinfo";
    public static final String print_terms = "print_terms";
    public static final String print_customer_id = "print_customer_id";
    public static final String print_order_comments = "print_order_comments";
    public static final String print_addons = "print_addons";
    public static final String print_tax_details = "print_tax_details";
    public static final String print_discount_details = "print_discount_details";
    public static final String print_descriptions = "print_descriptions";
    public static final String print_prod_comments = "print_prod_comments";
    public static final String print_sale_attributes = "print_sale_attributes";
    public static final String print_payment_comments = "print_payment_comments";
    public static final String print_footer = "print_footer";
    public static final String print_terms_conditions = "print_terms_conditions";
    public static final String print_emobilepos_website = "print_emobilepos_website";
    public static final String print_ivuloto_qr = "print_ivuloto_qr";

    private SharedPreferences sharedPref;
    private String defaultUnitsName;

    public MyPreferences(Activity activity) {
        this.activity = activity;
        // prefEditor =
        // PreferenceManager.getDefaultSharedPreferences(context).edit();
        // prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefEditor = activity.getSharedPreferences(this.MY_SHARED_PREF, Context.MODE_PRIVATE).edit();
        prefs = activity.getSharedPreferences(this.MY_SHARED_PREF, Context.MODE_PRIVATE);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);

        global = (Global) activity.getApplication();

        // prefEditor.putString(BundleVersion, appVersion);
        // prefEditor.commit();
    }

    public void setApplicationPassword(String pass) {
        prefEditor.putString(ApplicationPassword, pass);
        prefEditor.commit();
    }

    public String getApplicationPassword() {
        return (prefs.getString(ApplicationPassword, ""));
    }

    public void setAccountLogoPath(String path) {
        prefEditor.putString("logo_path", path);
        prefEditor.commit();
    }

    public String getAccountLogoPath() {
        return (prefs.getString("logo_path", ""));
    }

    public void setDBpath(String path) {
        prefEditor.putString(db_path, path);
        prefEditor.commit();
    }

    public String getDBpath() {
        return (prefs.getString(db_path, ""));
    }

    public void setCacheDir(String path) {
        prefEditor.putString("cache_dir", path);
        prefEditor.commit();
    }

    public String getCacheDir() {
        return (prefs.getString("cache_dir", ""));
    }

    public String defaultCountryCode(boolean isGet, String val) {
        String key = "default_country_code";
        if (isGet)
            return prefs.getString(key, "-1");
        else {
            prefEditor.putString(key, val);
            prefEditor.commit();
        }
        return "";
    }

    public String defaultCountryName(boolean isGet, String val) {
        String key = "default_country_name";
        if (isGet)
            return prefs.getString(key, "NONE");
        else {
            prefEditor.putString(key, val);
            prefEditor.commit();
        }
        return "";
    }

    public String getZoneID() {
        // return "1";
        return (prefs.getString(zone_id, ""));
    }

    /* Set/Get Employee ID */
    public void setEmpID(String id) {
        prefEditor.putString(emp_id, id);
        prefEditor.commit();
    }

    public String getEmpID() {
        return (prefs.getString(emp_id, ""));
    }

    /* Set/Get Password */
    public void setAcctNumber(String number) {
        prefEditor.putString(AccountNumber, number);
        prefEditor.commit();
    }

    public String getAcctNumber() {
        // return "150255140221";
        return (prefs.getString(AccountNumber, ""));
    }

    /* Set/Get Activation Key */
    public void setActivKey(String key) {
        prefEditor.putString(ActivationKey, key);
        prefEditor.commit();
    }

    public String getActivKey() {
        return (prefs.getString(ActivationKey, ""));
    }

    /* Set/Get Device ID */
    public void setDeviceID(String id) {
        prefEditor.putString(DeviceID, id);
        prefEditor.commit();
    }

    public String getDeviceID() {
        return (prefs.getString(DeviceID, ""));
    }

    /* Set/Get Bundle Version */
    public void setBundleVersion(String version) {
        prefEditor.putString(BundleVersion, version);
        prefEditor.commit();
    }

    public String getBundleVersion() {
        PackageInfo pInfo = null;
        try {
            pInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String version = pInfo.versionName;
        return (version);
    }

    public void setAcctPassword(String pass) {
        prefEditor.putString(AccountPassword, pass);
        prefEditor.commit();
    }

    public String getAcctPassword() {
        return (prefs.getString("AccountPassword", ""));
    }

    public void setLoginPass(String password) {
        prefEditor.putString(LoginPass, "");
        prefEditor.commit();
    }

    public String getLoginPass() {
        return (prefs.getString(LoginPass, ""));
    }

    public void setAllEmpData(List<String[]> emp_data) {

        prefEditor.putString(emp_id, getData(emp_id, 0, emp_data));
        prefEditor.putString(zone_id, getData(zone_id, 0, emp_data));
        prefEditor.putString(emp_name, getData(emp_name, 0, emp_data));
        prefEditor.putString(emp_lastlogin, getData(emp_lastlogin, 0, emp_data));

        prefEditor.putString(emp_pos, getData(emp_pos, 0, emp_data));
        prefEditor.putString(MSOrderEntry, getData(MSOrderEntry, 0, emp_data));
        prefEditor.putString(MSCardProcessor, getData(MSCardProcessor, 0, emp_data));
        prefEditor.putString(GatewayURL, getData(GatewayURL, 0, emp_data));
        prefEditor.putString(approveCode, getData(approveCode, 0, emp_data));
        prefEditor.putString(MSLastOrderID, getValidID(getLastOrdID(), getData(MSLastOrderID, 0, emp_data)));
        prefEditor.putString(MSLastTransferID, getValidID(getLastTransferID(), getData(MSLastTransferID, 0, emp_data)));

        prefEditor.putString(tax_default, getData(tax_default, 0, emp_data));
        prefEditor.putString(pricelevel_id, getData(pricelevel_id, 0, emp_data));
        String val = getData(VAT, 0, emp_data);
        prefEditor.putBoolean(VAT, Boolean.parseBoolean(val));

        prefEditor.commit();
    }

    public String getData(String tag, int record, List<String[]> data) {
        Integer i = global.dictionary.get(record).get(tag);
        if (i != null) {
            return data.get(record)[i];
        }
        return "";
    }

    public String getEmployeePriceLevel() {
        return prefs.getString(pricelevel_id, "");
    }

    public String getEmployeeDefaultTax() {
        return prefs.getString(tax_default, "");
    }

    public String getEmpName() {
        return (prefs.getString(emp_name, ""));
    }

    public String getCardProcessor() {
        return (prefs.getString(MSCardProcessor, "-1"));
    }

    public String getGatewayURL() {
        return (prefs.getString(GatewayURL, ""));
    }

    public String getApproveCode() {
        return (prefs.getString(approveCode, ""));
    }

    public void setLastPayID(String id) {
        prefEditor.putString(pay_id, getValidID(getLastPayID(), id));
        prefEditor.commit();
    }

    public String getLastPayID() {
        return (prefs.getString(pay_id, ""));
    }

    public void setLastConsTransID(String id) {
        prefEditor.putString(ConsTrans_ID, getValidID(getLastConsTransID(), id));
        prefEditor.commit();
    }

    public String getLastConsTransID() {
        return (prefs.getString(ConsTrans_ID, ""));
    }

    public void setLastTransferID(String id) {
        prefEditor.putString(MSLastTransferID, getValidID(getLastTransferID(), id));
        prefEditor.commit();
    }

    public String getLastTransferID() {
        return (prefs.getString(MSLastTransferID, ""));
    }

    public void setLastOrdID(String id) {
        prefEditor.putString(MSLastOrderID, getValidID(getLastOrdID(), id));
        prefEditor.commit();
    }

    public String getLastOrdID() {
        return (prefs.getString(MSLastOrderID, ""));
    }

    private String getValidID(String curr_id, String new_id) {
        if (new_id.length() > 4) {
            String delims = "[\\-]";
            String[] tokens = new_id.split(delims);
            int new_seq = Integer.parseInt(tokens[1]);
            int new_year = Integer.parseInt(tokens[2]);

            if (curr_id != null && !curr_id.isEmpty() && curr_id.length() > 4) {
                String[] curr_tokens = curr_id.split(delims);
                int curr_seq = Integer.parseInt(curr_tokens[1]);
                int curr_year = Integer.parseInt(curr_tokens[2]);

                // Validate the emp id and the year between the last saved id
                // and the one received from the service
                // If year returned by the service is greater than the one
                // locally stored, replace the stored id.
                // if the year is the same then validate the sequence number
                if (!tokens[0].equals(curr_tokens[0]))
                    return curr_id;
                if (new_year > curr_year)
                    return new_id;
                else if (new_seq > curr_seq)
                    return new_id;
            } else if (tokens[0].equals(getEmpID())) {
                return new_id;
            }
        }

        return curr_id;
    }

    // private String getValidID(String curr_id, String new_id) {
    // if (new_id.length() > 4) {
    // String delims = "[\\-]";
    // String[] tokens = new_id.split(delims);
    // int new_seq = Integer.parseInt(tokens[1]);
    // int new_year = Integer.parseInt(tokens[2]);
    //
    // if (curr_id != null && !curr_id.isEmpty() && curr_id.length() > 4) {
    // String[] curr_tokens = curr_id.split(delims);
    // int curr_seq = Integer.parseInt(curr_tokens[1]);
    // int curr_year = Integer.parseInt(curr_tokens[2]);
    //
    // // Validate the emp id and the year between the last saved id
    // // and the one received from the service
    // // If year returned by the service is greater than the one
    // // locally stored, replace the stored id.
    // // if the year is the same then validate the sequence number
    // // if (!tokens[0].equals(curr_tokens[0]))
    // // return curr_id;
    // // if (new_year > curr_year)
    // // return new_id;
    // // if (new_year <= curr_year) {
    // String lastOrderId =
    // OrdersHandler.getLastOrderId(Integer.parseInt(tokens[0]), new_year);
    // tokens = lastOrderId.split(delims);
    // int seq = Integer.parseInt(tokens[1]);
    //// seq++;
    // tokens[1] = String.format("%05d", seq);
    // StringBuffer sb = new StringBuffer();
    // sb.append(tokens[0]);
    // sb.append("-");
    // sb.append(tokens[1]);
    // sb.append("-");
    // sb.append(tokens[2]);
    // return sb.toString();
    // // } else if (new_seq > curr_seq)
    // // return new_id;
    //
    // } else if (tokens[0].equals(getEmpID())) {
    // return new_id;
    // }
    // }
    //
    // return curr_id;
    // }

    public void setLogIn(boolean val) {
        prefEditor.putBoolean(isLoggedIn, val);
        prefEditor.commit();
    }

    public boolean getLogIn() {
        return prefs.getBoolean(isLoggedIn, false);
    }

    public void setCustID(String id) {
        prefEditor.putString(cust_id, id);
        prefEditor.commit();
    }

    public String getCustID() {
        return prefs.getString(cust_id, "");
    }

    public void setCustIDKey(String id) {
        prefEditor.putString("custidkey", id);
        prefEditor.commit();
    }

    public String getCustIDKey() {
        return prefs.getString("custidkey", "");
    }

    public void setCustSelected(boolean val) {
        prefEditor.putBoolean(cust_selected, val);
        prefEditor.commit();
    }

    public boolean isCustSelected() {
        return prefs.getBoolean(cust_selected, false);
    }

    public void setCustName(String name) {
        prefEditor.putString(cust_name, name);
        prefEditor.commit();
    }

    public String getCustName() {
        return prefs.getString(cust_name, "");
    }

    public boolean getIsVAT() {
        return prefs.getBoolean(VAT, false);
    }

    public int getPrintPreviewLayoutWidth() {
        String width = sharedPref.getString(pref_printer_width, "MEDIUM");
        PrinterPreviewWidth previewWidth = PrinterPreviewWidth.valueOf(width);
        switch (previewWidth) {
            case SMALL:
                return (int) UIUtils.convertDpToPixel(300, activity);
            case MEDIUM:
                return (int) UIUtils.convertDpToPixel(400, activity);
            case LARGE:
                return (int) UIUtils.convertDpToPixel(500, activity);
            default:
                return (int) UIUtils.convertDpToPixel(400, activity);
        }
    }

    public void setCustPriceLevel(String id) {
        prefEditor.putString(cust_pricelevel_id, id);
        prefEditor.commit();
    }

    public boolean isMixAnMatch() {
        return getPreferences(pref_mix_match);
    }

    public boolean isInvoiceRequirePayment() {
        return getPreferences(pref_invoice_require_payment);
    }

    public boolean isRequireFullPayment() {
        return getPreferences(pref_invoice_require_full_payment);
    }

    public String getCustPriceLevel() {
        return prefs.getString(cust_pricelevel_id, "");
    }

    public void setCustTaxCode(String id) {
        prefEditor.putString("cust_salestaxcode", id);
        prefEditor.commit();
    }

    public void setCustTaxCode(SalesTaxCodesHandler.TaxableCode taxableCode, String custTaxCode) {
        switch (taxableCode) {
            case TAXABLE:
                prefEditor.putString("cust_salestaxcode", custTaxCode);
                prefEditor.commit();
                break;
            case NON_TAXABLE:
                prefEditor.putString("cust_salestaxcode", "");
                prefEditor.commit();
                break;
            case NONE:
                prefEditor.putString("cust_salestaxcode", null);
                prefEditor.commit();
                break;
        }
    }

    public String getCustTaxCode() {
        return prefs.getString("cust_salestaxcode", null);
    }

    public void setCustEmail(String value) {
        prefEditor.putString(cust_email, value);
        prefEditor.commit();
    }

    public String getCustEmail() {
        return prefs.getString(cust_email, "");
    }

    public void resetCustInfo(String value) {
        prefEditor.putString(cust_email, "");
        prefEditor.putString(cust_pricelevel_id, "");
        prefEditor.putString(cust_id, "");
        prefEditor.putBoolean(cust_selected, false);
        prefEditor.putString("cust_salestaxode", "");
        prefEditor.putString(cust_name, value);
        prefEditor.putString("custidkey", "");
        prefEditor.commit();
    }

    public String getXMLAction(String key) {
        return global.xmlActions.get(key);
    }

    public boolean getPreferences(String key, boolean defaultValue) {
        return sharedPref.getBoolean(key, defaultValue);
    }

    public boolean getPreferences(String key) {
        return sharedPref.getBoolean(key, false);
    }

    public boolean requiresWaiterLogin() {
        return getPreferences(MyPreferences.pref_require_waiter_signin);
    }

    public String getPreferencesValue(String key) {
        return sharedPref.getString(key, "");
    }

    public boolean posPrinter(boolean isGet, boolean value) {
        String is_pos_printer = "is_pos_printer";
        if (isGet)
            return prefs.getBoolean(is_pos_printer, false);
        else {
            prefEditor.putBoolean(is_pos_printer, value);
            prefEditor.commit();
        }
        return false;
    }

    public int printerAreaSize(boolean isGet, int value) {
        String printer_area_size = "printer_area_size";
        if (isGet)
            return prefs.getInt(printer_area_size, 32);
        else {
            prefEditor.putInt(printer_area_size, value);
            prefEditor.commit();
        }
        return 32;
    }

    public String getSwiperMACAddress() {
        String swiper_mac_address = "swiper_mac_address";
        return prefs.getString(swiper_mac_address, "");
    }

    public void setSwiperMACAddress(String macAddress) {
        String swiper_mac_address = "swiper_mac_address";
        prefEditor.putString(swiper_mac_address, macAddress);
        prefEditor.commit();
    }

//    public String swiperMACAddress(boolean isGet, String value) {
//        String swiper_mac_address = "swiper_mac_address";
//        if (isGet)
//            return prefs.getString(swiper_mac_address, "");
//        else {
//            prefEditor.putString(swiper_mac_address, value);
//            prefEditor.commit();
//        }
//        return "";
//    }

    public void setPrinterMACAddress(String value) {
        String printer_mac_address = "printer_mac_address";
        prefEditor.putString(printer_mac_address, value);
        prefEditor.commit();
    }

    public String getPrinterMACAddress() {
        String printer_mac_address = "printer_mac_address";
        return prefs.getString(printer_mac_address, "");
    }

//    public String printerMACAddress(boolean isGet, String value) {
//        String printer_mac_address = "printer_mac_address";
//        if (isGet)
//            return prefs.getString(printer_mac_address, "");
//        else {
//            prefEditor.putString(printer_mac_address, value);
//            prefEditor.commit();
//        }
//        return "";
//    }

    public String sledMACAddress(boolean isGet, String value) {
        String sled_mac_address = "sled_mac_address";
        if (isGet)
            return prefs.getString(sled_mac_address, "");
        else {
            prefEditor.putString(sled_mac_address, value);
            prefEditor.commit();
        }
        return "";
    }

    public int getSwiperType() {
        String swiper_type = "swiper_type";
        return prefs.getInt(swiper_type, -1);
    }

    public void setSwiperType(int type) {
        String swiper_type = "swiper_type";
        prefEditor.putInt(swiper_type, type);
        prefEditor.commit();
    }
//    public int swiperType(boolean isGet, int value) {
//        String swiper_type = "swiper_type";
//        if (isGet)
//            return prefs.getInt(swiper_type, -1);
//        else {
//            prefEditor.putInt(swiper_type, value);
//            prefEditor.commit();
//        }
//        return -1;
//    }

    public String cdtLine1(boolean get, String value) {
        String cdt_line1 = "cdt_line1";
        if (get)
            return prefs.getString(cdt_line1, "Welcome to");
        else {
            prefEditor.putString(cdt_line1, value);
            prefEditor.commit();
        }
        return "Welcome to";
    }

    public String cdtLine2(boolean get, String value) {
        String cdt_line2 = "cdt_line2";
        if (get)
            return prefs.getString(cdt_line2, "eMobilePOS");
        else {
            prefEditor.putString(cdt_line2, value);
            prefEditor.commit();
        }
        return "eMobilePOS";
    }

    public int getPrinterType() {
        String printer_type = "printer_type";
        return prefs.getInt(printer_type, -1);
    }

    public void setPrinterType(int value) {
        String printer_type = "printer_type";
        prefEditor.putInt(printer_type, value);
        prefEditor.commit();
    }

    public void setPrinterName(String value) {
        prefEditor.putString("printer_name", value);
        prefEditor.commit();
    }

    public String getPrinterName() {
        return prefs.getString("printer_name", "");
    }

    public void setSwiperName(String value) {
        prefEditor.putString("swiper_name", value);
        prefEditor.commit();
    }

    public String getSwiperName() {
        return prefs.getString("swiper_name", "");
    }

    public int sledType(boolean isGet, int value) {
        String sled_type = "sled_type";
        if (isGet)
            return prefs.getInt(sled_type, -1);
        else {
            prefEditor.putInt(sled_type, value);
            prefEditor.commit();
        }
        return -1;
    }

    public void forgetPeripherals() {
        String sled_type = "sled_type";
        String printer_type = "printer_type";
        String swiper_type = "swiper_type";

        setPrinterName(""); //clean the printer name
        prefEditor.putInt(sled_type, -1);
        prefEditor.putInt(printer_type, -1);
        prefEditor.putInt(swiper_type, -1);
        prefEditor.commit();
    }

	/*
     * public void setIsMagtekReader(boolean val) {
	 * prefEditor.putBoolean("isMagtekReader", val); prefEditor.commit(); }
	 * public boolean getisMagtekReader() { return
	 * prefs.getBoolean("isMagtekReader", false); }
	 */

    public boolean isET1(boolean isGet, boolean value) {
        String device_et1 = "device_et1";
        if (isGet)
            return prefs.getBoolean(device_et1, false);
        else {
            prefEditor.putBoolean(device_et1, value);
            prefEditor.commit();
        }
        return false;
    }

    public boolean isMC40(boolean isGet, boolean value) {
        String device_mc40 = "device_mc40";
        if (isGet)
            return prefs.getBoolean(device_mc40, false);
        else {
            prefEditor.putBoolean(device_mc40, value);
            prefEditor.commit();
        }
        return false;
    }

    public boolean isAsura(boolean isGet, boolean value) {
        String device_asura = "device_asura";
        if (isGet)
            return prefs.getBoolean(device_asura, false);
        else {
            prefEditor.putBoolean(device_asura, value);
            prefEditor.commit();
        }
        return false;
    }

    public boolean isPAT100() {
        String device_pat100 = "device_pat100";
        return prefs.getBoolean(device_pat100, false);
    }

    public boolean setIsPAT100(boolean value) {
        String device_em100 = "device_pat100";
        prefEditor.putBoolean(device_em100, value);
        prefEditor.commit();
        return false;
    }

    public boolean isPAT215() {
        String device_pat215 = "device_pat215";
        return prefs.getBoolean(device_pat215, false);
    }

    public boolean setIsPAT215(boolean value) {
        String device_pat215 = "device_pat215";
        prefEditor.putBoolean(device_pat215, value);
        prefEditor.commit();
        return false;
    }

    public boolean isEM100() {
        String device_pat100 = "device_em100";
        return prefs.getBoolean(device_pat100, false);
    }

    public boolean setIsEM100(boolean value) {
        String device_em100 = "device_em100";
        prefEditor.putBoolean(device_em100, value);
        prefEditor.commit();
        return false;
    }

    public boolean isHandpoint() {
        String device_handpoint = "device_handpoint";
        return prefs.getBoolean(device_handpoint, false);
    }

    public boolean setIsHandpoint(boolean value) {
        String device_handpoint = "device_handpoint";
        prefEditor.putBoolean(device_handpoint, value);
        prefEditor.commit();
        return false;
    }


    public boolean isICMPEVO() {
        String device_icmpevo = "device_icmpevo";
        return prefs.getBoolean(device_icmpevo, false);
    }

    public boolean setIsICMPEVO(boolean value) {
        String device_icmpevo = "device_icmpevo";
        prefEditor.putBoolean(device_icmpevo, value);
        prefEditor.commit();
        return false;
    }


    public boolean isEM70() {
        String device_em70 = "device_em70";
        return prefs.getBoolean(device_em70, false);
    }

    public boolean setIsEM70(boolean value) {
        String device_em70 = "device_em70";
        prefEditor.putBoolean(device_em70, value);
        prefEditor.commit();
        return false;
    }

    public boolean isOT310() {
        String device_ot310 = "device_ot310";
        return prefs.getBoolean(device_ot310, false);
    }

    public boolean setIsOT310(boolean value) {
        String device_ot310 = "device_ot310";
        prefEditor.putBoolean(device_ot310, value);
        prefEditor.commit();
        return false;
    }

    public boolean isMEPOS() {
        String device_mepos = "device_mepos";
        return prefs.getBoolean(device_mepos, false);
    }

    public boolean setIsMEPOS(boolean value) {
        String device_mepos = "device_mepos";
        prefEditor.putBoolean(device_mepos, value);
        prefEditor.commit();
        return false;
    }

    public boolean isPOWA() {
        String device_powa = "device_powa";
        return prefs.getBoolean(device_powa, false);
    }

    public boolean setIsPOWA(boolean value) {
        String device_powa = "device_powa";
        prefEditor.putBoolean(device_powa, value);
        prefEditor.commit();
        return false;
    }

    public boolean isKDC5000() {
        String device_kdc500 = "device_kdc500";
        return prefs.getBoolean(device_kdc500, false);
    }

    public boolean setIsKDC500(boolean value) {
        String device_kdc500 = "device_kdc500";
        prefEditor.putBoolean(device_kdc500, value);
        prefEditor.commit();
        return false;
    }

    public boolean isESY13P1() {
        String device_ESY13P1 = "device_ESY13P1";
        return prefs.getBoolean(device_ESY13P1, false);
    }

    public boolean setIsESY13P1(boolean value) {
        String device_ESY13P1 = "device_ESY13P1";
        prefEditor.putBoolean(device_ESY13P1, value);
        prefEditor.commit();
        return false;
    }

    public boolean isDolphin(boolean isGet, boolean value) {
        String device_dolphin = "device_dolphin";
        if (isGet)
            return prefs.getBoolean(device_dolphin, false);
        else {
            prefEditor.putBoolean(device_dolphin, value);
            prefEditor.commit();
        }
        return false;
    }

    public boolean isSam4s(boolean isGet, boolean value) {
        String key = "device_sam4s";
        if (isGet)
            return prefs.getBoolean(key, false);
        else {
            prefEditor.putBoolean(key, value);
            prefEditor.commit();
        }
        return false;
    }

    public void setStarIPAddress(String val) {
        prefEditor.putString("star_ip_address", val);
        prefEditor.commit();
    }

    public String getStarIPAddress() {
        return prefs.getString("star_ip_address", "");
    }

    public void setStarPort(String value) {
        prefEditor.putString("star_port", value);
        prefEditor.commit();
    }

    public String getStarPort() {
        return prefs.getString("star_port", "9100");
    }

    public void setIsTablet(boolean val) {
        prefEditor.putBoolean(is_tablet, val);
        prefEditor.commit();
    }

    public boolean getIsTablet() {
        return (prefs.getBoolean(is_tablet, false));
    }

    public void setLastSendSync(String date) {
        prefEditor.putString(last_send_sync, date);
        prefEditor.commit();
    }

    public String getLastSendSync() {
        return (prefs.getString(last_send_sync, "N/A"));
    }

    public void setLastReceiveSync(String date) {
        prefEditor.putString(last_receive_sync, date);
        prefEditor.commit();
    }

    public String getLastReceiveSync() {
        return (prefs.getString(last_receive_sync, "N/A"));
    }

    // public void setMainMenuSettings(boolean[] values)
    // {
    // global.initSalesMenuTab(this.activity);
    // String[] mainMenuList = global.getSalesMainMenuList();
    //
    // int size = values.length;
    //
    // for(int i = 0 ; i < size ;i++)
    // {
    // prefEditor.putBoolean(mainMenuList[i], values[i]);
    // }
    // prefEditor.commit();
    // }

    public void updateMainMenuSettings(String keyVal, boolean isPicked) {
        prefEditor.putBoolean(keyVal, isPicked);
        prefEditor.commit();
    }

    // public boolean[] getMainMenuSettings(){
    // String[] mainMenuList = global.getSalesMainMenuList();
    // int size = mainMenuList.length;
    // boolean[] values = new boolean[size];
    //
    // for(int i = 0 ; i < size; i++)
    // values[i] = prefs.getBoolean(mainMenuList[i], true);
    //
    // return values;
    // }

    public boolean[] getMainMenuPreference() {
        int NUM_OF_ITEMS = 15;
        boolean[] values = new boolean[NUM_OF_ITEMS];
        Set<String> selections = sharedPref.getStringSet("pref_configure_home_menu", null);
        if (selections != null) {
            String[] selected = selections.toArray(new String[]{});

            Arrays.sort(selected);
            List<String> list = Arrays.asList(selected);

            for (int i = 0; i < NUM_OF_ITEMS; i++) {
                values[i] = list.contains(Integer.toString(i));
            }
        } else
            values = new boolean[]{true, true, true, true, true, true, true, true, true, true, true, true, true,
                    true, true};

        return values;

    }

    public List<String> getPrintingPreferences() {
        Set<String> selections = sharedPref.getStringSet("pref_set_printing_preferences", null);
        List<String> list = Arrays.asList(selections.toArray(new String[]{}));
        return list;
    }

    public void setPOSAdminPass(String pass) {
        prefEditor.putString("posAdminPassword", pass);
        prefEditor.commit();
    }

    public String getPOSAdminPass() {
        return (prefs.getString("posAdminPassword", ""));
    }

    public String posManagerPass(boolean get, String value) {
        String posManagerPassword = "posManagerPassword";
        if (get)
            return prefs.getString(posManagerPassword, "");
        else {
            prefEditor.putString(posManagerPassword, value);
            prefEditor.commit();
        }
        return "";

    }

    public void setStoredAndForward(boolean storeAndForward) {
        String is_store_forward = "is_store_forward";
        prefEditor.putBoolean(is_store_forward, storeAndForward);
        prefEditor.commit();
    }

    public boolean isPrefUseStoreForward() {
        return getPreferences(MyPreferences.pref_use_store_and_forward);
    }

    public boolean isStoredAndForward() {
        String is_store_forward = "is_store_forward";
        return prefs.getBoolean(is_store_forward, false);
    }


    public void setGeniusIP(String ip) {
        prefEditor.putString("genius_ip", ip);
        prefEditor.commit();
    }

    public String getGeniusIP() {
        return (prefs.getString("genius_ip", ""));
    }

    public void setShiftIsOpen(boolean value) {
        prefEditor.putBoolean("open_shift", value);
        prefEditor.commit();
    }

    public boolean getShiftIsOpen() {
        return prefs.getBoolean("open_shift", true);
    }

    public void setShiftClerkName(String value) {
        prefEditor.putString("shift_clerk_name", value);
        prefEditor.commit();
    }

    public String getShiftClerkName() {
        return prefs.getString("shift_clerk_name", "");
    }

    public void setShiftClerkID(String value) {
        prefEditor.putString("shift_clerk_id", value);
        prefEditor.commit();
    }

    public void setShiftID(String value) {
        prefEditor.putString("shift_id", value);
        prefEditor.commit();
    }

    public String getShiftID() {
        return prefs.getString("shift_id", "");
    }

    public String getShiftClerkID() {
        return prefs.getString("shift_clerk_id", "");
    }

    public void setClerkID(String value) {
        prefEditor.putString("clerk_id", value);
        prefEditor.commit();
    }

    public void setClerkName(String value) {
        prefEditor.putString("clerk_name", value);
        prefEditor.commit();

    }

    public String getClerkName() {
        return prefs.getString("clerk_name", "");
    }

    public String getClerkID() {
        return (prefs.getString("clerk_id", ""));
    }

    public void deleteStoredEncryptionKeys() {
        prefEditor.putString(rsa_priv_key, "");
        prefEditor.putString(aes_key, "");
        prefEditor.putString(aes_iv, "");
        prefEditor.commit();
    }

    public void setRSAKeys(GuardedObject privKey, GuardedObject pubKey) {
        try {
            prefEditor.putString(rsa_priv_key, Base64.encodeToString(((byte[]) privKey.getObject()), Base64.DEFAULT));
            prefEditor.putString(rsa_priv_key, Base64.encodeToString(((byte[]) pubKey.getObject()), Base64.DEFAULT));
            prefEditor.commit();
        } catch (AccessControlException e) {

        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public GuardedObject getRSAPrivKey() {
        Guard guard = new PropertyPermission("java.home", "read");
        GuardedObject gobj = new GuardedObject(prefs.getString(rsa_priv_key, ""), guard);

        return gobj;
    }

    public GuardedObject getRSAPubKey() {
        Guard guard = new PropertyPermission("java.home", "read");
        GuardedObject gobj = new GuardedObject(prefs.getString(rsa_pub_key, ""), guard);

        return gobj;
    }

    public void setAESKey(GuardedObject gobj) {
        try {
            prefEditor.putString(aes_key, (String) gobj.getObject()); // Stored
            // as
            // received
            // (RSA
            // Encrypted
            // +
            // Base64)
            prefEditor.commit();
        } catch (AccessControlException e) {

        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public GuardedObject getAESKey() {
        Guard guard = new PropertyPermission("java.home", "read");
        GuardedObject gobj = new GuardedObject(Base64.decode(prefs.getString(aes_key, ""), Base64.DEFAULT), guard);

        return gobj;
    }

    public void setAESIV(GuardedObject gobj) {
        try {

            prefEditor.putString(aes_iv, (String) gobj.getObject()); // Stored
            // as
            // received
            // (RSA
            // Encrypted
            // +
            // Base64)
            prefEditor.commit();
        } catch (AccessControlException e) {

        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public GuardedObject getAESIV() {
        Guard guard = new PropertyPermission("java.home", "read");
        GuardedObject gobj = new GuardedObject(Base64.decode(prefs.getString(aes_iv, ""), Base64.DEFAULT), guard);

        return gobj;
    }

    public boolean isPrintWebSiteFooterEnabled() {
        return getPrintingPreferences().contains("print_emobilepos_website");
    }

    public void setDefaultUnitsName(String defaultUnitsName) {
        prefEditor.putString("defaultUnitsName", defaultUnitsName);
        prefEditor.commit();
    }

    public String getDefaultUnitsName() {
        return prefs.getString("defaultUnitsName", "");
    }

}
