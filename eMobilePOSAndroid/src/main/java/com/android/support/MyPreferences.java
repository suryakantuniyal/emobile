package com.android.support;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Base64;

import com.android.dao.AssignEmployeeDAO;
import com.android.database.SalesTaxCodesHandler;

import java.security.AccessControlException;
import java.security.Guard;
import java.security.GuardedObject;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.PropertyPermission;
import java.util.Set;

import util.AESCipher;
import util.json.UIUtils;

public class MyPreferences {
    public static final String pref_enable_togo_eatin = "pref_enable_togo_eatin";
    //    public static final String pref_require_waiter_signin = "pref_require_waiter_signin";
    //    private Global global;

    public static final String pref_syncplus_ip = "pref_syncplus_ip";
    public static final String pref_syncplus_port = "pref_syncplus_port";
    public static final String pref_enable_table_selection = "pref_enable_table_selection";
    public static final String pref_ask_seats = "pref_ask_seats";
    public static final String pref_use_navigationbar = "pref_use_navigationbar";
    public static final String pref_expire_usersession_time = "pref_expire_usersession_time";
    public static final String pref_expire_session = "pref_expire_session";

    public static final String pref_use_permitreceipt_printing = "pref_use_permitreceipt_printing";
    public static final String pref_fast_scanning_mode = "pref_fast_scanning_mode";
    public static final String pref_signature_required_mode = "pref_signature_required_mode";
    public static final String pref_qr_code_reading = "pref_qr_code_reading";
    public static final String pref_enable_multi_category = "pref_enable_multi_category";
    public static final String pref_ask_order_comments = "pref_ask_order_comments";
    //    private final String zone_id = "zone_id";
//    private final String VAT = "VAT";
    public static final String pref_show_only_group_taxes = "pref_show_only_group_taxes";
    public static final String pref_remove_leading_zeros = "pref_remove_leading_zeros";
    public static final String pref_mix_match = "pref_mix_match";
    public static final String pref_holds_polling_service = "pref_holds_polling_service";
    public static final String pref_clear_customer = "pref_clear_customer";
    public static final String pref_show_confirmation_screen = "pref_show_confirmation_screen";
    public static final String pref_direct_customer_selection = "pref_direct_customer_selection";
    public static final String pref_display_customer_account_number = "pref_display_customer_account_number";
    public static final String pref_skip_want_add_more_products = "pref_skip_want_add_more_products";
    //    private final String MSLastTransferID = "MSLastTransferID";
    public static final String pref_require_shift_transactions = "pref_require_shift_transactions";
    public static final String pref_allow_customer_creation = "pref_allow_customer_creation";
    public static final String pref_scope_bar_in_restaurant_mode = "pref_scope_bar_in_restaurant_mode";
    public static final String pref_display_also_redeem = "pref_display_also_redeem";
    public static final String pref_display_redeem_all = "pref_display_redeem_all";
    public static final String pref_use_loyal_patron = "pref_use_loyal_patron";
    private static final String pref_use_stadis_iv = "pref_use_stadis_iv";
    public static final String pref_print_raster_mode = "pref_print_raster_mode";

    public static final String pref_giftcard_auto_balance_request = "pref_giftcard_auto_balance_request";
    public static final String pref_giftcard_show_balance = "pref_giftcard_show_balance";
    public static final String pref_cash_show_change = "pref_cash_show_change";
    public static final String pref_pay_with_tupyx = "pref_pay_with_tupyx";
    public static final String pref_pay_with_card_on_file = "pref_pay_with_card_on_file";
    public static final String pref_use_pax = "pref_use_pax";
    public static final String pref_use_pax_signature = "pref_use_pax_signature";
    public static final String pref_use_sound_payments = "pref_use_sound_payments";
    public static final String pref_mw_with_genius = "pref_mw_with_genius";
    public static final String pref_config_genius_peripheral = "pref_config_genius_peripheral";
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
    public static final String pref_pax_close_batch_hour = "pref_pax_close_batch_hour";
    public static final String pref_payment_device = "pref_payment_device";
    public static final String pref_payment_device_ip = "pref_payment_device_ip";

    public static final String pref_use_store_and_forward = "pref_use_store_and_forward";
    public static final String pref_return_require_refund = "pref_return_require_refund";
    public static final String pref_convert_to_reward = "pref_convert_to_reward";
    public static final String pref_invoice_require_payment = "pref_invoice_require_payment";
    public static final String pref_invoice_require_full_payment = "pref_invoice_require_full_payment";
    public static final String pref_printek_info = "pref_printek_info";
    public static final String pref_automatic_printing = "pref_automatic_printing";
    public static final String pref_enable_multiple_prints = "pref_enable_multiple_prints";
    public static final String pref_split_stationprint_by_categories = "pref_split_stationprint_by_categories";
    public static final String pref_enable_printing = "pref_enable_printing";
    public static final String pref_wholesale_printout = "pref_wholesale_printout";
    public static final String pref_handwritten_signature = "pref_handwritten_signature";
    public static final String pref_prompt_customer_copy = "pref_prompt_customer_copy";
    public static final String pref_print_receipt_transaction_payment = "pref_print_receipt_transaction_payment";
    public static final String pref_print_taxes_breakdown = "pref_print_taxes_breakdown";
    public static final String pref_allow_decimal_quantities = "pref_allow_decimal_quantities";
    public static final String pref_group_receipt_by_sku = "pref_group_receipt_by_sku";
    public static final String pref_require_password_to_remove_void = "pref_require_password_to_remove_void";
    public static final String pref_show_removed_void_items_in_printout = "pref_show_removed_void_items_in_printout";
    public static final String pref_limit_products_on_hand = "pref_limit_products_on_hand";
    public static final String pref_attribute_to_display = "pref_attribute_to_display";
    public static final String pref_default_customer_display_name = "pref_default_customer_display_name";

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
    private static final String pref_require_customer = "pref_require_customer";
    private static final String pref_skip_email_phone = "pref_skip_email_phone";
    private static final String pref_prefill_total_amount = "pref_prefill_total_amount";
    private static final String pref_automatic_sync = "pref_automatic_sync";
    private static final String pref_restaurant_mode = "pref_restaurant_mode";
    private static final String pref_retail_taxes = "pref_retail_taxes";
    private static final String pref_use_clerks = "pref_use_clerks";
    private static final String pref_use_clerks_autologout = "pref_use_clerks_autologout";
    private static final String pref_use_syncplus_services = "pref_use_syncplus_services";
    private static final String pref_syncplus_mode = "pref_syncplus_mode";
    private final String MY_SHARED_PREF = "MY_SHARED_PREF";
    private final String db_path = "db_path";
    //    private final String emp_id = "emp_id";
    private final String ActivationKey = "ActivationKey";
    private final String DeviceID = "DeviceID";
    private final String BundleVersion = "BundleVersion";
    private final String ApplicationPassword = "ApplicationPassword";
    private final String AccountNumber = "AccountNumber";
    private final String AccountPassword = "AccountPassword";
    private final String LoginPass = "LoginPass";
    //    private final String emp_name = "emp_name";
    //    private final String emp_lastlogin = "emp_lastlogin";
    //    private final String emp_pos = "emp_pos";
    //    private final String MSOrderEntry = "MSOrderEntry";
    //    private final String MSCardProcessor = "MSCardProcessor";
    //    private final String GatewayURL = "GatewayURL";
    //    private final String approveCode = "approveCode";
    //    private final String MSLastOrderID = "MSLastOrderID";
    //    private final String tax_default = "tax_default";
    //    private final String pricelevel_id = "pricelevel_id";
    private final String pay_id = "pay_id";
    private final String isLoggedIn = "isLoggedIn";
    private final String cust_id = "cust_id";
    private final String cust_selected = "cust_selected";
    private final String cust_name = "cust_name";
    private final String cust_pricelevel_id = "cust_pricelevel_id";
    private final String cust_email = "cust_email";
    private final String ConsTrans_ID = "ConsTrans_ID";
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
    private final String epsonModel = "Epson_Model";
    private final String epsonTarget = "Epson_Target";
    //Weight Scales
    private final String selected_BT_weight = "BT_weight";

    public Context context;
    private SharedPreferences.Editor prefEditor;
    private SharedPreferences prefs;
    private SharedPreferences sharedPref;
    private String defaultUnitsName;
    private String batchCloseTime;
    // Gratuities
    public static final String suggested_gratuity = "suggested_gratuity";
    public static final String gratuity_one     = "gratuity_one";
    public static final String gratuity_two     = "gratuity_two";
    public static final String gratuity_three   = "gratuity_three";

    // Embedded Barcode
    private static final String pref_embedded_barcodes = "pref_embedded_barcodes";
    private static final String pref_embedded_barcode_type = "pref_embedded_barcode_type";

    public MyPreferences(Context context) {
        this.context = context;
        // prefEditor =
        // PreferenceManager.getDefaultSharedPreferences(context).edit();
        // prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefEditor = context.getSharedPreferences(this.MY_SHARED_PREF, Context.MODE_PRIVATE).edit();
        prefs = context.getSharedPreferences(this.MY_SHARED_PREF, Context.MODE_PRIVATE);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

//        global = (Global) activity.getApplication();

        // prefEditor.putString(BundleVersion, appVersion);
        // prefEditor.commit();
    }

    public static boolean isTeamSable() {
        return Build.MODEL.toUpperCase().startsWith("SABRESD") ||
                Build.MODEL.toUpperCase().equalsIgnoreCase("TR") ||
                Build.MODEL.toUpperCase().equalsIgnoreCase("15N-A-RM") ||
                Build.MODEL.toUpperCase().equalsIgnoreCase("15N-RM");
    }

    public static boolean isPaxA920() {
        return Build.MODEL.toUpperCase().equals("A920");
    }

    public String getApplicationPassword() {
        return (prefs.getString(ApplicationPassword, ""));
    }

    public void setApplicationPassword(String pass) {
        prefEditor.putString(ApplicationPassword, pass);
        prefEditor.commit();
    }

    public void setSelectedBTweight(int index){
        prefEditor.putInt(selected_BT_weight,index);
        prefEditor.commit();
    }
    public int getSelectedBTweight(){
        return (prefs.getInt(selected_BT_weight, -1));
    }
    public String getAccountLogoPath() {
        return (prefs.getString("logo_path", ""));
    }

    public void setAccountLogoPath(String path) {
        prefEditor.putString("logo_path", path);
        prefEditor.commit();
    }

    public String getDBpath() {
        return (prefs.getString(db_path, ""));
    }

    public void setDBpath(String path) {
        prefEditor.putString(db_path, path);
        prefEditor.commit();
    }

    public String getCacheDir() {
        return (prefs.getString("cache_dir", ""));
    }

    public void setCacheDir(String path) {
        prefEditor.putString("cache_dir", path);
        prefEditor.commit();
    }

    public String getPaymentDevice(){
        return getPreferencesValue(pref_payment_device);
    }

    public void setPaymentDevice(String device){
        setPreferencesValue(pref_payment_device,device);
        prefEditor.putString(pref_payment_device, device);
        prefEditor.commit();
    }

    public String getPaymentDeviceIP(){
        return getPreferencesValue(pref_payment_device_ip);
    }

    public void setPaymentDeviceIP(String ip){
        setPreferencesValue(pref_payment_device_ip,ip);
        prefEditor.putString(pref_payment_device_ip, ip);
        prefEditor.commit();
    }

    public String getDefaultCountryCode() {
        String key = "default_country_code";
        return prefs.getString(key, "-1");
    }

    public void setDefaultCountryCode(String val) {
        String key = "default_country_code";
        prefEditor.putString(key, val);
        prefEditor.commit();
    }

    public String getDefaultCountryName() {
        String key = "default_country_name";
        return prefs.getString(key, "NONE");
    }

    public void setDefaultCountryName(String val) {
        String key = "default_country_name";
        prefEditor.putString(key, val);
        prefEditor.commit();
    }
//    public String getZoneID() {
//        // return "1";
//        return (prefs.getString(zone_id, ""));
//    }

//    /* Set/Get Employee ID */
//    public void setEmpID(String id) {
//        prefEditor.putString(emp_id, id);
//        prefEditor.commit();
//    }
//
//    public String getEmpID() {
//        return (prefs.getString(emp_id, ""));
//    }

    public String getAcctNumber() {
        // return "150255140221";
        return (prefs.getString(AccountNumber, ""));
    }

    /* Set/Get Password */
    public void setAcctNumber(String number) {
        prefEditor.putString(AccountNumber, number);
        prefEditor.commit();
    }

    public String getActivKey() {
        return (prefs.getString(ActivationKey, ""));
    }

    /* Set/Get Activation Key */
    public void setActivKey(String key) {
        prefEditor.putString(ActivationKey, key);
        prefEditor.commit();
    }

    public String getDeviceID() {
        return (prefs.getString(DeviceID, ""));
    }

    /* Set/Get Device ID */
    public void setDeviceID(String id) {
        prefEditor.putString(DeviceID, id);
        prefEditor.commit();
    }

    public String getBundleVersion() {
        PackageInfo pInfo = null;
        try {
            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        String version = String.format("%s,%s", pInfo.versionName, Build.VERSION.RELEASE);
        return (version);
    }

    /* Set/Get Bundle Version */
    public void setBundleVersion(String version) {
        prefEditor.putString(BundleVersion, version);
        prefEditor.commit();
    }

    public String getAcctPassword() {
        return (prefs.getString("AccountPassword", ""));
    }

    public void setAcctPassword(String pass) {
        prefEditor.putString(AccountPassword, pass);
        prefEditor.commit();
    }

    public String getLoginPass() {
        return (prefs.getString(LoginPass, ""));
    }

    public void setLoginPass(String password) {
        prefEditor.putString(LoginPass, "");
        prefEditor.commit();
    }

//    public void setAllEmpData(List<String[]> emp_data) {
//
//        prefEditor.putString(emp_id, getData(emp_id, 0, emp_data));
//        prefEditor.putString(zone_id, getData(zone_id, 0, emp_data));
//        prefEditor.putString(emp_name, getData(emp_name, 0, emp_data));
//        prefEditor.putString(emp_lastlogin, getData(emp_lastlogin, 0, emp_data));
//
//        prefEditor.putString(emp_pos, getData(emp_pos, 0, emp_data));
//        prefEditor.putString(MSOrderEntry, getData(MSOrderEntry, 0, emp_data));
//        prefEditor.putString(MSCardProcessor, getData(MSCardProcessor, 0, emp_data));
//        prefEditor.putString(GatewayURL, getData(GatewayURL, 0, emp_data));
//        prefEditor.putString(approveCode, getData(approveCode, 0, emp_data));
//        prefEditor.putString(MSLastOrderID, getValidID(getLastOrdID(), getData(MSLastOrderID, 0, emp_data)));
//        prefEditor.putString(MSLastTransferID, getValidID(getLastTransferID(), getData(MSLastTransferID, 0, emp_data)));
//
//        prefEditor.putString(tax_default, getData(tax_default, 0, emp_data));
//        prefEditor.putString(pricelevel_id, getData(pricelevel_id, 0, emp_data));
//        String val = getData(VAT, 0, emp_data);
//        prefEditor.putBoolean(VAT, Boolean.parseBoolean(val));
//
//        prefEditor.commit();
//    }

//    public String getData(String tag, int record, List<String[]> data) {
//        Integer i = global.dictionary.get(record).get(tag);
//        if (i != null) {
//            return data.get(record)[i];
//        }
//        return "";
//    }
//
//    public String getEmployeePriceLevel() {
//        return prefs.getString(pricelevel_id, "");
//    }
//
//    public String getEmployeeDefaultTax() {
//        return prefs.getString(tax_default, "");
//    }
//
//    public String getEmpName() {
//        return (prefs.getString(emp_name, ""));
//    }
//
//    public String getCardProcessor() {
//        return (prefs.getString(MSCardProcessor, "-1"));
//    }
//
//    public String getGatewayURL() {
//        return (prefs.getString(GatewayURL, ""));
//    }
//
//    public String getApproveCode() {
//        return (prefs.getString(approveCode, ""));
//    }

    public String getLastPayID() {
        return (prefs.getString(pay_id, ""));
    }

    public void setLastPayID(String id) {
        prefEditor.putString(pay_id, getValidID(getLastPayID(), id));
        prefEditor.commit();
    }

    public String getLastConsTransID() {
        return (prefs.getString(ConsTrans_ID, ""));
    }

    public void setLastConsTransID(String id) {
        prefEditor.putString(ConsTrans_ID, getValidID(getLastConsTransID(), id));
        prefEditor.commit();
    }

//    public void setLastTransferID(String id) {
//        prefEditor.putString(MSLastTransferID, getValidID(getLastTransferID(), id));
//        prefEditor.commit();
//    }
//
//    public String getLastTransferID() {
//        return (prefs.getString(MSLastTransferID, ""));
//    }
//
//    public void setLastOrdID(String id) {
//        prefEditor.putString(MSLastOrderID, getValidID(getLastOrdID(), id));
//        prefEditor.commit();
//    }

//    public String getLastOrdID() {
//        return (prefs.getString(MSLastOrderID, ""));
//    }

    private String getValidID(String curr_id, String new_id) {
        if (new_id == null) {
            new_id = "";
        }
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
            } else if (tokens[0].equals(String.valueOf(AssignEmployeeDAO.getAssignEmployee().getEmpId()))) {
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

    public boolean getLogIn() {
        return prefs.getBoolean(isLoggedIn, false);
    }

    public void setLogIn(boolean val) {
        prefEditor.putBoolean(isLoggedIn, val);
        prefEditor.commit();
    }

    public String getCustID() {
        return prefs.getString(cust_id, "");
    }

    public void setCustID(String id) {
        prefEditor.putString(cust_id, id);
        prefEditor.commit();
    }

    public String getCustIDKey() {
        return prefs.getString("custidkey", "");
    }

    public void setCustIDKey(String id) {
        prefEditor.putString("custidkey", id);
        prefEditor.commit();
    }

    public boolean isCustSelected() {
        return prefs.getBoolean(cust_selected, false);
    }

    public void setCustSelected(boolean val) {
        prefEditor.putBoolean(cust_selected, val);
        prefEditor.commit();
    }

    public String getCustName() {
        return prefs.getString(cust_name, "");
    }

    public void setCustName(String name) {
        prefEditor.putString(cust_name, name);
        prefEditor.commit();
    }

//    public boolean getIsVAT() {
//        return prefs.getBoolean(VAT, false);
//    }

    public int getPrintPreviewLayoutWidth() {
        String width = sharedPref.getString(pref_printer_width, "MEDIUM");
        PrinterPreviewWidth previewWidth = PrinterPreviewWidth.valueOf(width);
        switch (previewWidth) {
            case SMALL:
                return (int) UIUtils.convertDpToPixel(300, context);
            case MEDIUM:
                return (int) UIUtils.convertDpToPixel(400, context);
            case LARGE:
                return (int) UIUtils.convertDpToPixel(500, context);
            default:
                return (int) UIUtils.convertDpToPixel(400, context);
        }
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

    public void setCustPriceLevel(String id) {
        prefEditor.putString(cust_pricelevel_id, id);
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

    public void setCustTaxCode(String id) {
        prefEditor.putString("cust_salestaxcode", id);
        prefEditor.commit();
    }

    public String getCustEmail() {
        return prefs.getString(cust_email, "");
    }

    public void setCustEmail(String value) {
        prefEditor.putString(cust_email, value);
        prefEditor.commit();
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

    public boolean getPreferences(String key, boolean defaultValue) {
        return sharedPref.getBoolean(key, defaultValue);
    }

    public boolean isGroupReceiptBySku(boolean isToGo) {
        return getPreferences(pref_group_receipt_by_sku) && isToGo;
    }

    public boolean getPreferences(String key) {
        return sharedPref.getBoolean(key, false);
    }

//    public boolean requiresWaiterLogin() {
//        return getPreferences(MyPreferences.pref_require_waiter_signin);
//    }

    public void setPreferences(String key, boolean value) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public String getPreferencesValue(String key) {
        return sharedPref.getString(key, "");
    }

    public void setPreferencesValue(String key, String value) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public String getEmpIdFromPreferences() {
        return prefs.getString("emp_id", "");
    }

    public void setEmpIdFromPreferences(String value) {
        prefEditor.putString("emp_id", value);
        prefEditor.commit();
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

    public String getPrinterMACAddress() {
        String printer_mac_address = "printer_mac_address";
        return prefs.getString(printer_mac_address, "");
    }

    public void setPrinterMACAddress(String value) {
        String printer_mac_address = "printer_mac_address";
        prefEditor.putString(printer_mac_address, value);
        prefEditor.commit();
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

    public String getPrinterName() {
        return prefs.getString("printer_name", "");
    }

    public void setPrinterName(String value) {
        prefEditor.putString("printer_name", value);
        prefEditor.commit();
    }

    public String getSwiperName() {
        return prefs.getString("swiper_name", "");
    }

    public void setSwiperName(String value) {
        prefEditor.putString("swiper_name", value);
        prefEditor.commit();
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
        setIsPOWA(false);
        setIsMEPOS(false);
        setIsBixolonRD(false);
        setIsEM70(false);
        setIsEM100(false);
        setIsESY13P1(false);
        setIsHandpoint(false);
        setIsICMPEVO(false);
        setIsKDC425(false);
        setIsOT310(false);
        setHPEOnePrime(false);

        setPrinterName(""); //clean the printer name
        prefEditor.putInt(sled_type, -1);
        prefEditor.putInt(printer_type, -1);
        prefEditor.putInt(swiper_type, -1);
        prefEditor.commit();
    }

    public void setEpsonTarget(String value) {
        prefEditor.putString(epsonTarget, value);
        prefEditor.commit();
    }
    public String getEpsonTarget() {
        return prefs.getString(epsonTarget,"");
    }
    public void setEpsonModel(int value) {
        prefEditor.putInt(epsonModel, value);
        prefEditor.commit();
    }
    public int getEpsonModel() {
        return prefs.getInt(epsonModel, -1);
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

    public static boolean isAPT50(){
        return Build.MODEL.toUpperCase().contains("WPOS-3");
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
    public boolean isHPEOnePrime() {
        String device_hp = "device_HP_EOnePrime";
        return prefs.getBoolean(device_hp, false);
    }

    public boolean setHPEOnePrime(boolean value) {
        String device_hp = "device_HP_EOnePrime";
        prefEditor.putBoolean(device_hp, value);
        prefEditor.commit();
        return false;
    }

    public boolean isEpson() {
        String device_hp = "device_Epson";
        return prefs.getBoolean(device_hp, false);
    }

    public boolean setEpson(boolean value) {
        String device_hp = "device_Epson";
        prefEditor.putBoolean(device_hp, value);
        prefEditor.commit();
        return false;
    }

    public boolean isBixolonRD() {
        String device_bixolon = "device_bixolon_rd";
        return prefs.getBoolean(device_bixolon, false);
    }

    public boolean setIsBixolonRD(boolean value) {
        String device_bixolon = "device_bixolon_rd";
        prefEditor.putBoolean(device_bixolon, value);
        prefEditor.commit();
        return false;
    }

    public boolean isPOWA() {
        String device_powa = "device_powa";
        return prefs.getBoolean(device_powa, false);
    }

    public void setIsPOWA(boolean value) {
        String device_powa = "device_powa";
        prefEditor.putBoolean(device_powa, value);
        prefEditor.commit();
    }

    public boolean isKDC425() {
        String device_kdc425 = "device_kdc425";
        return prefs.getBoolean(device_kdc425, false);
    }

    public boolean setIsKDC425(boolean value) {
        String device_kdc425 = "device_kdc425";
        prefEditor.putBoolean(device_kdc425, value);
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
    public boolean isAPT120() {
        String device_APT120 = "device_APT120";
        return prefs.getBoolean(device_APT120, false);
    }

    public boolean setIsAPT120(boolean value) {
        String device_APT120 = "device_APT120";
        prefEditor.putBoolean(device_APT120, value);
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

    public boolean isSam4s() {
        String key = "device_sam4s";
        return prefs.getBoolean(key, false);
    }

    public void setSams4s(boolean value) {
        String key = "device_sam4s";
        prefEditor.putBoolean(key, value);
        prefEditor.commit();
    }

    public String getStarIPAddress() {
        return prefs.getString("star_ip_address", "");
    }

    public void setStarIPAddress(String val) {
        prefEditor.putString("star_ip_address", val);
        prefEditor.commit();
    }

    public String getStarPort() {
        return prefs.getString("star_port", "9100");
    }

    public void setStarPort(String value) {
        prefEditor.putString("star_port", value);
        prefEditor.commit();
    }

    public boolean isTablet() {
        return (prefs.getBoolean(is_tablet, false));
    }

    public void setIsTablet(boolean val) {
        prefEditor.putBoolean(is_tablet, val);
        prefEditor.commit();
    }

    public String getLastSendSync() {
        return (prefs.getString(last_send_sync, "N/A"));
    }

    public void setLastSendSync(String date) {
        prefEditor.putString(last_send_sync, date);
        prefEditor.commit();
    }

    public String getLastReceiveSync() {
        return (prefs.getString(last_receive_sync, "N/A"));
    }

    public void setLastReceiveSync(String date) {
        prefEditor.putString(last_receive_sync, date);
        prefEditor.commit();
    }

    public void updateMainMenuSettings(String keyVal, boolean isPicked) {
        prefEditor.putBoolean(keyVal, isPicked);
        prefEditor.commit();
    }

    public void setMainMenuSettings(Collection<String> values) {
//        global.initSalesMenuTab(this.context);
//        String[] mainMenuList = global.getSalesMainMenuList();
        SharedPreferences.Editor editor = sharedPref.edit();
        Set<String> mValues = new HashSet<>();
//        String[] mainMenuList = context.getResources().getStringArray(R.array.mainMenuArray);
//        for (int i = 0; i < values.size(); i++) {
//        }
        if(values.size() > 0){
            mValues.addAll(values);
            editor.putStringSet("pref_configure_home_menu", mValues);
        }
        editor.commit();
    }

    public boolean[] getMainMenuPreference() {
        int NUM_OF_ITEMS = 17;
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
                    true, true, true, true};

        return values;

    }

    public List<String> getPrintingPreferences() {
        Set<String> selections = sharedPref.getStringSet("pref_set_printing_preferences", null);
        List<String> list = Arrays.asList(selections.toArray(new String[]{}));
        return list;
    }

    public void setPrintingPreferences(Set<String> selections) {
         sharedPref.getStringSet("pref_set_printing_preferences", selections);
    }

    public boolean loginAdmin(String password) {
        try {
            return getPOSAdminPass().equals(AESCipher.getSha256Hash(password));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getPOSAdminPass() {
        return (prefs.getString("posAdminPassword", ""));
    }

    public void setPOSAdminPass(String pass) {
        try {
            prefEditor.putString("posAdminPassword", AESCipher.getSha256Hash(pass));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        prefEditor.commit();
    }

    public String getPosManagerPass() {
        String posManagerPassword = "posManagerPassword";
        return prefs.getString(posManagerPassword, "");
    }

    public void setPosManagerPass(String value) {
        String posManagerPassword = "posManagerPassword";
        try {
            prefEditor.putString(posManagerPassword, AESCipher.getSha256Hash(value));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        prefEditor.commit();
    }

    public boolean loginManager(String password) {
        try {
            return getPosManagerPass().equals(AESCipher.getSha256Hash(password));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isPrefUseStoreForward() {
        return getPreferences(MyPreferences.pref_use_store_and_forward);
    }

    public boolean isStoredAndForward() {
        String is_store_forward = "is_store_forward";
        return prefs.getBoolean(is_store_forward, false);
    }

    public void setStoredAndForward(boolean storeAndForward) {
        String is_store_forward = "is_store_forward";
        prefEditor.putBoolean(is_store_forward, storeAndForward);
        prefEditor.commit();
    }

    public boolean isShiftOpenRequired() {
        return getPreferences(MyPreferences.pref_require_shift_transactions);
    }

    public boolean isGiftCardAutoBalanceRequest() {
        return getPreferences(MyPreferences.pref_giftcard_auto_balance_request);
    }

    public boolean isClearCustomerAfterTransaction() {
        return getPreferences(MyPreferences.pref_clear_customer);
    }

    public boolean isShowGiftCardBalanceAfterPayments() {
        return getPreferences(MyPreferences.pref_giftcard_show_balance);
    }

    public boolean isPrefillTotalAmount() {
        return getPreferences(MyPreferences.pref_prefill_total_amount);
    }

    public void setIsUseClerks(boolean value) {
        setPreferences(MyPreferences.pref_use_clerks, value);
    }

    public boolean isUseClerks() {
        return getPreferences(MyPreferences.pref_use_clerks);
    }

    public boolean isUseClerksAutoLogout() {
        return getPreferences(MyPreferences.pref_use_clerks_autologout);
    }

    public String getGeniusIP() {
        return (prefs.getString("genius_ip", ""));
    }

    public void setGeniusIP(String ip) {
        prefEditor.putString("genius_ip", ip);
        prefEditor.commit();
    }

//    public boolean getShiftIsOpen() {
//        return prefs.getBoolean("open_shift", true);
//    }
//
//    public void setShiftIsOpen(boolean value) {
//        prefEditor.putBoolean("open_shift", value);
//        prefEditor.commit();
//    }

    public String getShiftClerkName() {
        return prefs.getString("shift_clerk_name", "");
    }

    public void setShiftClerkName(String value) {
        prefEditor.putString("shift_clerk_name", value);
        prefEditor.commit();
    }

    public String getShiftID() {
        return prefs.getString("shift_id", "0");
    }

    public void setShiftID(String value) {
        prefEditor.putString("shift_id", value);
        prefEditor.commit();
    }
//
//    public String getShiftClerkID() {
//        return prefs.getString("shift_clerk_id", "");
//    }
//
//    public void setShiftClerkID(String value) {
//        prefEditor.putString("shift_clerk_id", value);
//        prefEditor.commit();
//    }

    public String getClerkName() {
        return prefs.getString("clerk_name", "");
    }

    public void setClerkName(String value) {
        prefEditor.putString("clerk_name", value);
        prefEditor.commit();
    }

    public String getClerkID() {
        String id = prefs.getString("clerk_id", "0");
        if (TextUtils.isEmpty(id)) {
            id = "0";
        }
        return id;
    }

    public void setClerkID(String value) {
        prefEditor.putString("clerk_id", value);
        prefEditor.commit();
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

    public GuardedObject getAESKey() {
        Guard guard = new PropertyPermission("java.home", "read");
        GuardedObject gobj = new GuardedObject(Base64.decode(prefs.getString(aes_key, ""), Base64.DEFAULT), guard);

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

    public GuardedObject getAESIV() {
        Guard guard = new PropertyPermission("java.home", "read");
        GuardedObject gobj = new GuardedObject(Base64.decode(prefs.getString(aes_iv, ""), Base64.DEFAULT), guard);

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

    public boolean isPrintWebSiteFooterEnabled() {
        return getPrintingPreferences().contains("print_emobilepos_website");
    }

    public String getDefaultUnitsName() {
        return prefs.getString("defaultUnitsName", "");
    }

    public void setDefaultUnitsName(String defaultUnitsName) {
        prefEditor.putString("defaultUnitsName", defaultUnitsName);
        prefEditor.commit();
    }

    public String getBatchCloseTime(){ return prefs.getString("batchCloseTime",""); }

    public void setBatchCloseTime(String batchCloseTime){
        prefEditor.putString("batchCloseTime", batchCloseTime);
        prefEditor.commit();
    }

    public boolean isRestaurantMode() {
        return getPreferences(MyPreferences.pref_restaurant_mode);
    }

    public boolean isRetailTaxes() {
        return getPreferences(MyPreferences.pref_retail_taxes);
    }

    public boolean isPollingHoldsEnable() {
        return getPreferences(pref_holds_polling_service);
    }

    public boolean isAutoSyncEnable() {
        return getPreferences(pref_automatic_sync);
    }

    public boolean getIsPersistClerk() {
        return (prefs.getBoolean("persistClerk", false));
    }

    public void setIsPersistClerk(boolean persistClerk) {
        prefEditor.putBoolean("persistClerk", persistClerk);
        prefEditor.commit();
    }


    public boolean isSkipEmailPhone() {
        return getPreferences(MyPreferences.pref_skip_email_phone);
    }

    public boolean isShowCashChangeAmount() {
        return getPreferences(MyPreferences.pref_cash_show_change);
    }

    public boolean isCustomerRequired() {
        return getPreferences(MyPreferences.pref_require_customer);
    }

    public boolean isDirectCustomerSelection() {
        return getPreferences(MyPreferences.pref_direct_customer_selection);

    }

    public boolean isMultiplePrints() {
        return getPreferences(MyPreferences.pref_enable_multiple_prints);
    }

    public boolean isRasterModePrint() {
        return getPreferences(MyPreferences.pref_print_raster_mode);
    }

    public boolean isPayWithCardOnFile() {
        return getPreferences(MyPreferences.pref_pay_with_card_on_file);
    }

    public void setSNBC(boolean SNBC) {
        String device_snbc = "device_snbc";
        prefEditor.putBoolean(device_snbc, SNBC);
        prefEditor.commit();
    }

    public boolean isSNBC() {
        String device_snbc = "device_snbc";
        return prefs.getBoolean(device_snbc, false);
    }

    public boolean isUseStadisV4() {
        return getPreferences(MyPreferences.pref_use_stadis_iv);
    }

    public enum PrinterPreviewWidth {SMALL, MEDIUM, LARGE}

    public String getSyncPlusIPAddress() {
        return getPreferencesValue(pref_syncplus_ip);
    }

    public void setSyncPlusIPAddress(String val) {
        setPreferencesValue(pref_syncplus_ip, val);
    }

    public String getSyncPlusPort() {
        return getPreferencesValue(pref_syncplus_port);
    }

    public String getCustomerDisplayName() {
        return getPreferencesValue(pref_default_customer_display_name);
    }

    public void setSyncPlusPort(String value) {
        setPreferencesValue(pref_syncplus_port, value);
    }

    public boolean isUse_syncplus_services() {
        return getPreferences(MyPreferences.pref_use_syncplus_services);
    }

    public boolean isSyncplus_AutoScan() {
        return !getPreferences(MyPreferences.pref_syncplus_mode);
    }

    public boolean isRemoveLeadingZerosFromUPC() {
        return getPreferences(MyPreferences.pref_remove_leading_zeros);
    }

    public boolean isUsePermitReceipt() {
        return getPreferences(MyPreferences.pref_use_permitreceipt_printing);
    }

    public boolean isExpireUserSession() {
        return getPreferences(MyPreferences.pref_expire_session);
    }

    public int getSessionExpirationTime() {
        String value = getPreferencesValue(pref_expire_usersession_time);
        if (TextUtils.isEmpty(value))
            return 15;
        else
            return Integer.parseInt(value);
    }

    public String getGratuityOne() {
        if(cleanGratuity(getPreferencesValue(gratuity_one)).equals("0.0")){
            setGratuityOne("10");
        }
        return cleanGratuity(getPreferencesValue(gratuity_one));
    }

    private String cleanGratuity(String gratuity){
        double gratResult;
        try{
            gratResult = Double.parseDouble(gratuity);
        }catch (Exception x){
            gratResult = 0;
        }
        return new Double(gratResult).toString();
    }
    public void setGratuityOne(String gratuity) {

        setPreferencesValue(gratuity_one, cleanGratuity(gratuity));
    }

    public String getGratuityTwo() {
        if(cleanGratuity(getPreferencesValue(gratuity_two)).equals("0.0")){
            setGratuityTwo("15");
        }
        return cleanGratuity( getPreferencesValue(gratuity_two));
    }

    public void setGratuityTwo(String gratuity) {
        setPreferencesValue(gratuity_two, cleanGratuity(gratuity));
    }

    public String getGratuityThree() {
        if(cleanGratuity(getPreferencesValue(gratuity_three)).equals("0.0")){
            setGratuityThree("20");
        }
        return cleanGratuity(getPreferencesValue(gratuity_three));
    }

    public void setGratuityThree(String gratuity) {
        setPreferencesValue(gratuity_three, cleanGratuity(gratuity));
    }

    public boolean isGratuitySelected(){
        return getPreferences(suggested_gratuity) ;
    }
    public Integer getOpenDiscount() {
        String key = "open_discount";
        return prefs.getInt(key, 0);
    }

    public void setOpenDiscount(Integer openDiscount) {
        String key = "open_discount";
        prefEditor.putInt(key, openDiscount);
        prefEditor.commit();
    }
    public boolean isManagerPasswordRequiredForOpenDiscount(){
        String key = "manager_password_required_for_open_discount";
        return getPreferences(key, false);
    }
//    public void setManagerPasswordRequiredForOpenDiscount(int isRequiredForOpenDiscount){
//        String key = "manager_password_for_open_discount";
//        prefEditor.putInt(key, isRequiredForOpenDiscount);
//        prefEditor.commit();
//    }
    public boolean isEmbeddedBarcodeEnabled(){
        return getPreferences(pref_embedded_barcodes,false);
    }
    public String getPrefEmbeddedBarcodeType(){
        return getPreferencesValue(pref_embedded_barcode_type);
    }
}
