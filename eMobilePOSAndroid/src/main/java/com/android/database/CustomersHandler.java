package com.android.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.android.emobilepos.models.Address;
import com.android.support.Customer;
import com.android.support.StringUtils;
import com.crashlytics.android.Crashlytics;

import net.sqlcipher.database.SQLiteStatement;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import util.StringUtil;

public class CustomersHandler {
    private static final String cust_id = "cust_id";
    private static final String cust_id_ref = "cust_id_ref";
    private static final String qb_sync = "qb_sync";
    private static final String zone_id = "zone_id";
    private static final String CompanyName = "CompanyName";
    private static final String Salutation = "Salutation";
    private static final String cust_contact = "cust_contact";
    private static final String cust_name = "cust_name";
    private static final String cust_chain = "cust_chain";
    private static final String cust_balance = "cust_balance";
    private static final String cust_limit = "cust_limit";
    private static final String cust_firstName = "cust_firstName";
    private static final String cust_middleName = "cust_middleName";
    private static final String cust_lastName = "cust_lastName";
    private static final String cust_phone = "cust_phone";
    private static final String cust_email = "cust_email";
    private static final String cust_fax = "cust_fax";
    private static final String cust_update = "cust_update";
    private static final String isactive = "isactive";
    private static final String cust_ordertype = "cust_ordertype";
    private static final String cust_taxable = "cust_taxable";
    private static final String cust_salestaxcode = "cust_salestaxcode";
    private static final String pricelevel_id = "pricelevel_id";
    private static final String cust_terms = "cust_terms";
    private static final String cust_pwd = "cust_pwd";
    private static final String cust_securityquestion = "cust_securityquestion";
    private static final String cust_securityanswer = "cust_securityanswer";
    private static final String cust_points = "cust_points";
    private static final String custidkey = "custidkey";
    private static final String cust_id_numeric = "cust_id_numeric";
    private static final String cust_dob = "cust_dob";
    private static final String AccountNumnber = "AccountNumnber";

    private static final String table_name = "Customers";
    private final List<String> attr = Arrays.asList(cust_id, cust_id_ref, qb_sync, zone_id, CompanyName,
            Salutation, cust_name, cust_chain, cust_balance, cust_limit, cust_contact, cust_firstName, cust_middleName,
            cust_lastName, cust_phone, cust_email, cust_fax, cust_update, isactive, cust_ordertype, cust_taxable,
            cust_salestaxcode, pricelevel_id, cust_terms, cust_pwd, cust_securityquestion, cust_securityanswer,
            cust_points, custidkey, cust_id_numeric, cust_dob, AccountNumnber);
    private StringBuilder sb1, sb2;
    private HashMap<String, Integer> attrHash;

//    private List<String[]> custData;
//    private List<HashMap<String, Integer>> dictionaryListMap;

    public CustomersHandler(Context activity) {
        attrHash = new HashMap<>();
//        custData = new ArrayList<>();
        sb1 = new StringBuilder();
        sb2 = new StringBuilder();
        new DBManager(activity);
        initDictionary();
    }

    private void initDictionary() {
        int size = attr.size();
        for (int i = 0; i < size; i++) {
            attrHash.put(attr.get(i), i + 1);
            if ((i + 1) < size) {
                sb1.append(attr.get(i)).append(",");
                sb2.append("?").append(",");
            } else {
                sb1.append(attr.get(i));
                sb2.append("?");
            }
        }
    }

//    private String getData(String tag, int record) {
//        Integer i = dictionaryListMap.get(record).get(tag);
//        if (i != null) {
//            return custData.get(record)[i];
//        }
//        return "";
//    }

    private int index(String tag) {
        return attrHash.get(tag);
    }

//    public void insert(List<String[]> data, List<HashMap<String, Integer>> dictionary) {
//        DBManager.getDatabase().beginTransaction();
//        try {
//
//            custData = data;
//            dictionaryListMap = dictionary;
//            SQLiteStatement insert;
//            String sb = "INSERT INTO " + table_name + " (" + sb1.toString() + ") " +
//                    "VALUES (" + sb2.toString() + ")";
//            insert = DBManager.getDatabase().compileStatement(sb);
//
//            int size = custData.size();
//
//            for (int j = 0; j < size; j++) {
//                insert.bindString(index(cust_id), getData(cust_id, j)); // cust_id
//                insert.bindString(index(cust_id_ref), getData(cust_id_ref, j)); // cust_id_ref
//                insert.bindString(index(qb_sync), getData(qb_sync, j)); // qb_sync
//                insert.bindString(index(zone_id), getData(zone_id, j)); // zone_id
//                insert.bindString(index(CompanyName), getData(CompanyName, j)); // CompanyName
//                insert.bindString(index(Salutation), getData(Salutation, j)); // Salutation
//                insert.bindString(index(cust_name), getData(cust_name, j)); // cust_name
//                insert.bindString(index(cust_chain), getData(cust_chain, j)); // cust_chain
//                insert.bindString(index(cust_balance), getData(cust_balance, j)); // cust_balance
//                insert.bindString(index(cust_limit), getData(cust_limit, j)); // cust_limit
//                insert.bindString(index(cust_contact), getData(cust_contact, j)); // cust_contact
//                insert.bindString(index(cust_firstName), getData(cust_firstName, j)); // cust_firstName
//                insert.bindString(index(cust_middleName), getData(cust_middleName, j)); // cust_middleName
//                insert.bindString(index(cust_lastName), getData(cust_lastName, j)); // cust_lastName
//                insert.bindString(index(cust_phone), getData(cust_phone, j)); // cust_phone
//                insert.bindString(index(cust_email), getData(cust_email, j)); // cust_email
//                insert.bindString(index(cust_fax), getData(cust_fax, j)); // cust_fax
//                insert.bindString(index(cust_update), getData(cust_update, j)); // cust_update
//                insert.bindString(index(isactive), getData(isactive, j)); // isactive
//                insert.bindString(index(cust_ordertype), getData(cust_ordertype, j)); // cust_ordertype
//                insert.bindString(index(cust_taxable), getData(cust_taxable, j)); // cust_taxable
//                insert.bindString(index(cust_salestaxcode), getData(cust_salestaxcode, j)); // cust_salestaxcode
//                insert.bindString(index(pricelevel_id), getData(pricelevel_id, j)); // pricelevel_id
//                insert.bindString(index(cust_terms), getData(cust_terms, j)); // cust_terms
//                insert.bindString(index(cust_pwd), getData(cust_pwd, j)); // cust_pwd
//                insert.bindString(index(cust_securityquestion), getData(cust_securityquestion, j)); // cust_securityquestion
//                insert.bindString(index(cust_securityanswer), getData(cust_securityanswer, j)); // cust_securityanswer
//                insert.bindString(index(cust_points), getData(cust_points, j)); // cust_points
//                insert.bindString(index(custidkey), getData(custidkey, j)); // custidkey
//                insert.bindString(index(cust_id_numeric), getData(cust_id_numeric, j)); // cust_id_numeric
//                insert.bindString(index(cust_dob), getData(cust_dob, j)); // cust_dob
//                insert.bindString(index(AccountNumnber), getData(AccountNumnber, j));
//
//                insert.execute();
//                insert.clearBindings();
//            }
//            insert.close();
//            DBManager.getDatabase().setTransactionSuccessful();
//        } catch (Exception e) {
//            Crashlytics.logException(e);
//        } finally {
//            DBManager.getDatabase().endTransaction();
//        }
//    }

    public void insertOneCustomer(Customer customer) {
        insert(Arrays.asList(customer));
    }

    public void emptyTable() {
        DBManager.getDatabase().execSQL("DELETE FROM " + table_name);
    }

    public Cursor getUnsynchCustomers() {

        return DBManager.getDatabase().rawQuery("SELECT cust_id,cust_name,cust_firstName,cust_lastName,CompanyName,cust_contact,cust_phone,cust_email,cust_dob FROM Customers WHERE qb_sync = '0'", null);
    }

    public long getNumUnsyncCustomers() {
        SQLiteStatement stmt = DBManager.getDatabase().compileStatement("SELECT Count(*) FROM " + table_name + " WHERE qb_sync = '0'");
        long count = stmt.simpleQueryForLong();
        stmt.close();
        return count;
    }

    public boolean unsyncCustomersLeft() {
        SQLiteStatement stmt = DBManager.getDatabase().compileStatement("SELECT Count(*) FROM " + table_name + " WHERE qb_sync = '0'");
        long count = stmt.simpleQueryForLong();
        stmt.close();
        return count != 0;

    }

    public void updateIsSync(List<String[]> list) {
        StringBuilder sb = new StringBuilder();
        sb.append(cust_id).append(" = ?");
        ContentValues args = new ContentValues();
        int size = list.size();
        for (int i = 0; i < size; i++) {
            if (list.get(i)[0].equals("0")) {
                args.put(qb_sync, "1");
                DBManager.getDatabase().update(table_name, args, sb.toString(), new String[]{list.get(i)[1]});
            }
        }
    }

    public void updateSyncStatus(String customerId, boolean isSync) {
        ContentValues args = new ContentValues();
        args.put(qb_sync, isSync ? 1 : 0);
        DBManager.getDatabase().update(table_name, args, cust_id + " = ?", new String[]{customerId});
    }

    public String getSpecificValue(String field, String param) {
        String data = "";
        String[] fields = new String[]{field};
        String[] arguments = new String[]{param};
        Cursor cursor = DBManager.getDatabase().query(true, table_name, fields, "cust_id=?", arguments, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                data = cursor.getString(cursor.getColumnIndex(field));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return data;
    }

    public HashMap<String, String> getCustomerMap(String id) {
        HashMap<String, String> tempMap = new HashMap<>();
        String[] fields = new String[]{cust_name, cust_phone, cust_email};
        String[] arguments = new String[]{id};

        Cursor cursor = DBManager.getDatabase().query(true, table_name, fields, "cust_id=?", arguments, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                tempMap.put(cust_name, cursor.getString(cursor.getColumnIndex(cust_name)));
                tempMap.put(cust_phone, cursor.getString(cursor.getColumnIndex(cust_phone)));
                tempMap.put(cust_email, cursor.getString(cursor.getColumnIndex(cust_email)));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return tempMap;
    }

    public Cursor getCursorAllCust() {
        String query = "SELECT cust_id as _id,AccountNumnber, custidkey, cust_name,c.pricelevel_id,pl.pricelevel_name," +
                "cust_taxable,cust_salestaxcode,cust_email,CompanyName,cust_phone " +
                "FROM Customers c LEFT OUTER JOIN PriceLevel pl ON c.pricelevel_id = pl.pricelevel_id ORDER BY cust_name";

        Cursor cursor = DBManager.getDatabase().rawQuery(query, null);
        cursor.moveToFirst();
        return cursor;
    }

    public HashMap<String, String> getCustomerInfo(String custID) {
        String query = "SELECT cust_id,cust_name,cust_taxable,cust_salestaxcode,custidkey,pricelevel_id,cust_email FROM Customers WHERE cust_id = ?";
        Cursor c = DBManager.getDatabase().rawQuery(query, new String[]{custID});
        HashMap<String, String> map = new HashMap<>();
        if (c.moveToFirst()) {
            map.put(cust_id, c.getString(c.getColumnIndex(cust_id)));
            map.put(cust_name, c.getString(c.getColumnIndex(cust_name)));
            map.put(cust_taxable, c.getString(c.getColumnIndex(cust_taxable)));
            map.put(cust_salestaxcode, c.getString(c.getColumnIndex(cust_salestaxcode)));
            map.put(custidkey, c.getString(c.getColumnIndex(custidkey)));
            map.put(pricelevel_id, c.getString(c.getColumnIndex(pricelevel_id)));
            map.put(cust_email, c.getString(c.getColumnIndex(cust_email)));
        }

        c.close();
        return map;
    }

    public List<String> getCustDetails(String custID) {
        List<String> list = new ArrayList<>();

        String subquery1 = "SELECT c.cust_name,c.cust_contact,c.cust_phone,c.cust_email,c.CompanyName,c.cust_balance," +
                "c.cust_limit,c.cust_taxable,c.cust_salestaxcode,"
                + "a.addr_b_str1,a.addr_b_str2,a.addr_b_str3,a.addr_b_city,a.addr_b_state,a.addr_b_country, " +
                "a.addr_b_zipcode, a.addr_s_str1,"
                + "a.addr_s_str2,a.addr_s_str3,a.addr_s_city,a.addr_s_state,a.addr_s_country, a.addr_s_zipcode " +
                "FROM Customers c LEFT OUTER JOIN Address "
                + "a ON c.cust_id = a.cust_id WHERE c.cust_id = ?";

        StringBuilder custAddress = new StringBuilder();
        String[] billingAddress = new String[7];
        String[] shippingAddress = new String[7];


        Cursor cursor = DBManager.getDatabase().rawQuery(subquery1, new String[]{custID});

        DecimalFormat frmt = new DecimalFormat("0.00");
        String tab = " ";
        String jump = "\n";

        if (cursor.moveToFirst()) {
            do {

                // ----------------------- 'Information' Section
                // ----------------------//
                String data = cursor.getString(cursor.getColumnIndex("cust_name"));
                list.add(data);
                data = cursor.getString(cursor.getColumnIndex("cust_contact"));
                list.add(data);
                data = cursor.getString(cursor.getColumnIndex("cust_phone"));
                list.add(data);
                data = cursor.getString(cursor.getColumnIndex("cust_email"));
                list.add(data);
                data = cursor.getString(cursor.getColumnIndex("CompanyName"));
                list.add(data);

                // ----------------------- 'Financial Info' Section
                // ----------------------//
                data = cursor.getString(cursor.getColumnIndex("cust_balance"));
                if (data == null || data.isEmpty())
                    data = "0";
                data = frmt.format(Double.parseDouble(data));
                list.add(data);

                data = cursor.getString(cursor.getColumnIndex("cust_limit"));
                if (data == null || data.isEmpty())
                    data = "0";
                data = frmt.format(Double.parseDouble(data));
                list.add(data);

                data = cursor.getString(cursor.getColumnIndex("cust_taxable"));
                list.add(data);

                data = cursor.getString(cursor.getColumnIndex("cust_salestaxcode"));
                list.add(data);

                // ----------------------- 'Address' Section
                // ----------------------//
                billingAddress[0] = cursor.getString(cursor.getColumnIndex("addr_b_str1"));
                billingAddress[1] = cursor.getString(cursor.getColumnIndex("addr_b_str2"));
                billingAddress[2] = cursor.getString(cursor.getColumnIndex("addr_b_str3"));
                billingAddress[3] = cursor.getString(cursor.getColumnIndex("addr_b_city"));
                billingAddress[4] = cursor.getString(cursor.getColumnIndex("addr_b_state"));
                billingAddress[5] = cursor.getString(cursor.getColumnIndex("addr_b_country"));
                billingAddress[6] = cursor.getString(cursor.getColumnIndex("addr_b_zipcode"));

                shippingAddress[0] = cursor.getString(cursor.getColumnIndex("addr_s_str1"));
                shippingAddress[1] = cursor.getString(cursor.getColumnIndex("addr_s_str2"));
                shippingAddress[2] = cursor.getString(cursor.getColumnIndex("addr_s_str3"));
                shippingAddress[3] = cursor.getString(cursor.getColumnIndex("addr_s_city"));
                shippingAddress[4] = cursor.getString(cursor.getColumnIndex("addr_s_state"));
                shippingAddress[5] = cursor.getString(cursor.getColumnIndex("addr_s_country"));
                shippingAddress[6] = cursor.getString(cursor.getColumnIndex("addr_s_zipcode"));

                custAddress.append(getCustAddr(billingAddress[0])).append(jump).append(getCustAddr(billingAddress[1]))
                        .append(jump);
                custAddress.append(getCustAddr(billingAddress[2])).append(jump).append(jump)
                        .append(getCustAddr(billingAddress[5])).append(jump);
                custAddress.append(getCustAddr(billingAddress[3])).append(tab).append(getCustAddr(billingAddress[4]))
                        .append(tab);
                custAddress.append(getCustAddr(billingAddress[6]));
                list.add(custAddress.toString().trim());

                custAddress.setLength(0);
                custAddress.append(getCustAddr(shippingAddress[0])).append(tab).append(getCustAddr(shippingAddress[1]))
                        .append(tab);
                custAddress.append(getCustAddr(shippingAddress[2])).append(tab).append(getCustAddr(shippingAddress[3]))
                        .append(tab);
                custAddress.append(getCustAddr(shippingAddress[4])).append(tab).append(getCustAddr(shippingAddress[6]))
                        .append(tab);
                custAddress.append(getCustAddr(shippingAddress[5]));
                list.add(custAddress.toString().trim());

            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    private String getCustAddr(String value) {
        if (value == null)
            return "";

        return value.trim();
    }

    public Cursor getSearchCust(String search) // Transactions Receipts first
    // listview
    {
        String sb = "SELECT cust_id as _id,c.AccountNumnber, custidkey, cust_name,c.pricelevel_id,pl.pricelevel_name,cust_taxable,cust_salestaxcode," +
                "cust_email,CompanyName,cust_phone FROM Customers c LEFT OUTER JOIN PriceLevel pl ON c.pricelevel_id = pl.pricelevel_id " +
                "WHERE c.cust_name LIKE ? OR c.cust_id LIKE ? OR c.AccountNumnber LIKE ? OR c.cust_email LIKE ? OR c.cust_phone LIKE ? OR c.CompanyName LIKE ? ORDER BY cust_name";

        Cursor cursor = DBManager.getDatabase().rawQuery(sb, new String[]{"%" + search + "%", "%" + search + "%",
                "%" + search + "%", "%" + search + "%", "%" + search + "%", "%" + search + "%"});
        cursor.moveToFirst();
        return cursor;
    }

    public HashMap<String, String> getXMLCustAddr(String custID) {
        HashMap<String, String> mapValues = new HashMap<>();
        if (custID != null && !custID.isEmpty()) {
            String sb = "SELECT c.cust_firstName,c.cust_lastName,b.addr_b_str1,b.addr_b_str2,b.addr_b_str3,b.addr_b_city,b.addr_b_state,b.addr_b_country," +
                    "b.addr_b_zipcode,b.addr_s_str1,b.addr_s_str2,b.addr_s_str3,b.addr_s_city,b.addr_s_state,b.addr_s_country,b.addr_s_zipcode " +
                    "FROM Customers c LEFT OUTER JOIN Address b ON c.cust_id = b.cust_id WHERE c.cust_id = ?";
            Cursor cursor = DBManager.getDatabase().rawQuery(sb, new String[]{custID});
            if (cursor.moveToFirst()) {
                mapValues.put("cust_fname", cursor.getString(cursor.getColumnIndex(cust_firstName)));
                mapValues.put("cust_lname", cursor.getString(cursor.getColumnIndex(cust_lastName)));
                mapValues.put("addr_b_str1", cursor.getString(cursor.getColumnIndex("addr_b_str1")));
                mapValues.put("addr_b_str2", cursor.getString(cursor.getColumnIndex("addr_b_str2")));
                mapValues.put("addr_b_str3", cursor.getString(cursor.getColumnIndex("addr_b_str3")));
                mapValues.put("addr_b_city", cursor.getString(cursor.getColumnIndex("addr_b_city")));
                mapValues.put("addr_b_state", cursor.getString(cursor.getColumnIndex("addr_b_state")));
                mapValues.put("addr_b_country", cursor.getString(cursor.getColumnIndex("addr_b_country")));
                mapValues.put("addr_b_zipcode", cursor.getString(cursor.getColumnIndex("addr_b_zipcode")));
                mapValues.put("addr_s_str1", cursor.getString(cursor.getColumnIndex("addr_s_str1")));
                mapValues.put("addr_s_str2", cursor.getString(cursor.getColumnIndex("addr_s_str2")));
                mapValues.put("addr_s_str3", cursor.getString(cursor.getColumnIndex("addr_s_str3")));
                mapValues.put("addr_s_city", cursor.getString(cursor.getColumnIndex("addr_s_city")));
                mapValues.put("addr_s_state", cursor.getString(cursor.getColumnIndex("addr_s_state")));
                mapValues.put("addr_s_country", cursor.getString(cursor.getColumnIndex("addr_s_country")));
                mapValues.put("addr_s_zipcode", cursor.getString(cursor.getColumnIndex("addr_s_zipcode")));
            }
            cursor.close();
        }
        return mapValues;
    }

    public Customer getCustomer(String customerId) {

        Customer customer = new Customer();
        customer.setShippingAddress(new Address());
        customer.setBillingAddress(new Address());
        if (customerId != null && !customerId.isEmpty()) {
            String sb = ("SELECT c.*, " +
                    " b.addr_b_str1,b.addr_b_str2,b.addr_b_str3,b.addr_b_city,b.cust_id," +
                    " b.addr_b_state,b.addr_b_country,") +
                    " b.addr_b_zipcode,b.addr_s_str1,b.addr_s_str2,b.addr_s_str3,b.addr_s_city,b.addr_s_state,b.addr_s_country," +
                    " b.addr_s_zipcode" +
                    " FROM Customers c LEFT OUTER JOIN Address b ON c.cust_id = b.cust_id WHERE c.cust_id = ?";

            Cursor cursor = DBManager.getDatabase().rawQuery(sb, new String[]{customerId});

            if (cursor.moveToFirst()) {
                customer.setCust_id(customerId);
                customer.setCust_name(cursor.getString(cursor.getColumnIndex(cust_name)));
                customer.setCust_firstName(cursor.getString(cursor.getColumnIndex(cust_firstName)));
                customer.setCust_lastName(cursor.getString(cursor.getColumnIndex(cust_lastName)));
                customer.setCust_middleName(cursor.getString(cursor.getColumnIndex(cust_middleName)));
                customer.setCompanyName(cursor.getString(cursor.getColumnIndex(CompanyName)));
                customer.setCust_balance(cursor.getString(cursor.getColumnIndex(cust_balance)));
                customer.setCust_chain(cursor.getString(cursor.getColumnIndex(cust_chain)));
                customer.setCust_contact(cursor.getString(cursor.getColumnIndex(cust_contact)));
                customer.setCust_dob(cursor.getString(cursor.getColumnIndex(cust_dob)));
                customer.setCust_email(cursor.getString(cursor.getColumnIndex(cust_email)));
                customer.setCust_fax(cursor.getString(cursor.getColumnIndex(cust_fax)));
                customer.setCustIdNumeric(cursor.getString(cursor.getColumnIndex(cust_id_numeric)));
                customer.setCust_id_ref(cursor.getString(cursor.getColumnIndex(cust_id_ref)));
                customer.setPricelevel_id(cursor.getString(cursor.getColumnIndex(pricelevel_id)));
                customer.setCust_limit(cursor.getString(cursor.getColumnIndex(cust_limit)));
                customer.setCust_ordertype(cursor.getString(cursor.getColumnIndex(cust_ordertype)));
                customer.setCust_phone(cursor.getString(cursor.getColumnIndex(cust_phone)));
                customer.setCust_points(cursor.getString(cursor.getColumnIndex(cust_points)));
                customer.setCust_pwd(cursor.getString(cursor.getColumnIndex(cust_pwd)));
                customer.setCust_salestaxcode(cursor.getString(cursor.getColumnIndex(cust_salestaxcode)));
                customer.setCust_securityanswer(cursor.getString(cursor.getColumnIndex(cust_securityanswer)));
                customer.setCust_securityquestion(cursor.getString(cursor.getColumnIndex(cust_securityquestion)));
                customer.setCust_taxable(cursor.getString(cursor.getColumnIndex(cust_taxable)));
                customer.setCust_terms(cursor.getString(cursor.getColumnIndex(cust_terms)));
                customer.setCust_update(cursor.getString(cursor.getColumnIndex(cust_update)));

                customer.getBillingAddress().setAddr_b_str1(cursor.getString(cursor.getColumnIndex("addr_b_str1")));
                customer.getBillingAddress().setAddr_b_str2(cursor.getString(cursor.getColumnIndex("addr_b_str2")));
                customer.getBillingAddress().setAddr_b_str3(cursor.getString(cursor.getColumnIndex("addr_b_str3")));
                customer.getBillingAddress().setAddr_b_city(cursor.getString(cursor.getColumnIndex("addr_b_city")));
                customer.getBillingAddress().setAddr_b_state(cursor.getString(cursor.getColumnIndex("addr_b_state")));
                customer.getBillingAddress().setAddr_b_country(cursor.getString(cursor.getColumnIndex("addr_b_country")));
                customer.getBillingAddress().setAddr_b_zipcode(cursor.getString(cursor.getColumnIndex("addr_b_zipcode")));
                customer.getShippingAddress().setAddr_s_str1(cursor.getString(cursor.getColumnIndex("addr_s_str1")));
                customer.getShippingAddress().setAddr_s_str2(cursor.getString(cursor.getColumnIndex("addr_s_str2")));
                customer.getShippingAddress().setAddr_s_str3(cursor.getString(cursor.getColumnIndex("addr_s_str3")));
                customer.getShippingAddress().setAddr_s_city(cursor.getString(cursor.getColumnIndex("addr_s_city")));
                customer.getShippingAddress().setAddr_s_state(cursor.getString(cursor.getColumnIndex("addr_s_state")));
                customer.getShippingAddress().setAddr_s_country(cursor.getString(cursor.getColumnIndex("addr_s_country")));
                customer.getShippingAddress().setAddr_s_zipcode(cursor.getString(cursor.getColumnIndex("addr_s_zipcode")));
                customer.setCust_taxable(cursor.getString(cursor.getColumnIndex("cust_taxable")));
                customer.setCust_salestaxcode(cursor.getString(cursor.getColumnIndex("cust_salestaxcode")));

            }

            cursor.close();
            // db.close();
        }
        return customer;
    }

    public String[] getContactInfoBlock(String custID) {
        Cursor cursor = DBManager.getDatabase().rawQuery("SELECT cust_phone,cust_email FROM Customers WHERE cust_id = ?", new String[]{custID});
        String[] data = new String[2];
        if (cursor.moveToFirst()) {
            data[0] = cursor.getString(cursor.getColumnIndex("cust_phone"));
            data[1] = cursor.getString(cursor.getColumnIndex("cust_email"));
        }
        cursor.close();
        return data;
    }

    public void insert(List<Customer> customers) {
        DBManager.getDatabase().beginTransaction();
        try {
            SQLiteStatement insert;
            String sb = "INSERT INTO " + table_name + " (" + sb1.toString() + ") " +
                    "VALUES (" + sb2.toString() + ")";
            insert = DBManager.getDatabase().compileStatement(sb);
            for (Customer customer : customers) {
                insert.bindString(index(cust_id), customer.getCust_id());
                insert.bindString(index(cust_id_ref), customer.getCust_id_ref());
                insert.bindString(index(qb_sync), customer.getQb_sync());
                insert.bindString(index(zone_id), customer.getZone_id());
                insert.bindString(index(CompanyName), customer.getCompanyName());
                insert.bindString(index(Salutation), customer.getSalutation());
                insert.bindString(index(cust_contact), customer.getCust_contact());
                insert.bindString(index(cust_name), customer.getCust_name());
                insert.bindString(index(cust_chain), customer.getCust_chain());
                insert.bindString(index(cust_balance), customer.getCust_balance());
                insert.bindString(index(cust_limit), customer.getCust_limit());
                insert.bindString(index(cust_firstName), customer.getCust_firstName());
                insert.bindString(index(cust_middleName), customer.getCust_middleName());
                insert.bindString(index(cust_lastName), customer.getCust_lastName());
                insert.bindString(index(cust_phone), customer.getCust_phone());
                insert.bindString(index(cust_email), customer.getCust_email());
                insert.bindString(index(cust_fax), customer.getCust_fax());
                insert.bindString(index(cust_update), customer.getCust_update());
                insert.bindString(index(isactive), customer.getIsactive());
                insert.bindString(index(cust_ordertype), customer.getCust_ordertype());
                insert.bindString(index(cust_taxable), customer.getCust_taxable());
                insert.bindString(index(cust_salestaxcode), customer.getCust_salestaxcode());
                insert.bindString(index(pricelevel_id), customer.getPricelevel_id());
                insert.bindString(index(cust_terms), customer.getCust_terms());
                insert.bindString(index(cust_pwd), customer.getCust_pwd());
                insert.bindString(index(cust_securityquestion), customer.getCust_securityquestion());
                insert.bindString(index(cust_securityanswer), customer.getCust_securityanswer());
                insert.bindString(index(cust_points), customer.getCust_points());
                insert.bindString(index(cust_dob), customer.getCust_dob());
                insert.bindString(index(custidkey), StringUtil.nullStringToEmpty(customer.getCustIdKey()));
                insert.bindString(index(cust_id_numeric), StringUtil.nullStringToEmpty(customer.getCustIdNumeric()));
                insert.bindString(index(AccountNumnber), StringUtil.nullStringToEmpty(customer.getCustAccountNumber()));

                insert.execute();
                insert.clearBindings();
            }
            insert.close();
            DBManager.getDatabase().setTransactionSuccessful();
        } catch (Exception e) {
            Crashlytics.logException(e);
            throw e;
        } finally {
            DBManager.getDatabase().endTransaction();
        }
    }
}