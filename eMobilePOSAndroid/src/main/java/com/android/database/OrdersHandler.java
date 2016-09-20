package com.android.database;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import com.android.dao.DinningTableDAO;
import com.android.dao.DinningTableOrderDAO;
import com.android.emobilepos.models.DinningTable;
import com.android.emobilepos.models.DinningTableOrder;
import com.android.emobilepos.models.Order;
import com.android.emobilepos.models.ProductAttribute;
import com.android.support.DateUtils;
import com.android.support.Global;
import com.android.support.MyPreferences;

import net.sqlcipher.database.SQLiteStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class OrdersHandler {

    public static final String ord_id = "ord_id";
    public static final String qbord_id = "qbord_id";
    public static final String emp_id = "emp_id";
    public static final String cust_id = "cust_id";
    public static final String custidkey = "custidkey";
    public static final String ord_po = "ord_po";
    public static final String total_lines = "total_lines";
    public static final String total_lines_pay = "total_lines_pay";
    public static final String ord_total = "ord_total";
    public static final String ord_signature = "ord_signature";
    public static final String ord_comment = "ord_comment";
    public static final String ord_delivery = "ord_delivery";
    public static final String ord_timecreated = "ord_timecreated";
    public static final String ord_timesync = "ord_timesync";
    public static final String qb_synctime = "qb_synctime";
    public static final String emailed = "emailed";
    public static final String processed = "processed";
    public static final String ord_type = "ord_type";
    public static final String ord_claimnumber = "ord_claimnumber";
    public static final String ord_rganumber = "ord_rganumber";
    public static final String ord_returns_pu = "ord_returns_pu";
    public static final String ord_inventory = "ord_inventory";
    public static final String ord_issync = "ord_issync";
    public static final String tax_id = "tax_id";
    public static final String ord_shipvia = "ord_shipvia";
    public static final String ord_shipto = "ord_shipto";
    public static final String ord_terms = "ord_terms";
    public static final String ord_custmsg = "ord_custmsg";
    public static final String ord_class = "ord_class";
    public static final String ord_subtotal = "ord_subtotal";
    public static final String ord_taxamount = "ord_taxamount";
    public static final String ord_discount = "ord_discount";
    public static final String c_email = "c_email";
    public static final String isOnHold = "isOnHold";
    public static final String ord_HoldName = "ord_HoldName";

    // added
    public static final String clerk_id = "clerk_id";
    public static final String ord_discount_id = "ord_discount_id";
    public static final String ord_latitude = "ord_latitude";
    public static final String ord_longitude = "ord_longitude";
    public static final String tipAmount = "tipAmount";
    public static final String isVoid = "isVoid";
    public static final String assignedTable = "assignedTable";
    public static final String numberOfSeats = "numberOfSeats";
    public static final String associateID = "associateID";
    public static final String ord_timeStarted = "ord_timeStarted";

    public static final String is_stored_fwd = "is_stored_fwd";

    public static final String VAT = "VAT";

    public static final List<String> attr = Arrays.asList(ord_id, qbord_id, emp_id, cust_id, clerk_id, c_email,
            ord_signature, ord_po, total_lines, total_lines_pay, ord_total, ord_comment, ord_delivery, ord_timecreated,
            ord_timesync, qb_synctime, emailed, processed, ord_type, ord_claimnumber, ord_rganumber, ord_returns_pu,
            ord_inventory, ord_issync, tax_id, ord_shipvia, ord_shipto, ord_terms, ord_custmsg, ord_class, ord_subtotal,
            ord_taxamount, ord_discount, ord_discount_id, ord_latitude, ord_longitude, tipAmount, isVoid, custidkey,
            isOnHold, ord_HoldName, is_stored_fwd, VAT, assignedTable, numberOfSeats, associateID, ord_timeStarted);

    private StringBuilder sb1, sb2;
    private HashMap<String, Integer> attrHash;
    private MyPreferences myPref;

    public static final String table_name = "Orders";
    private Activity activity;

    public static OrdersHandler getInstance(Activity activity) {
        return new OrdersHandler(activity);
    }

    public OrdersHandler(Activity activity) {
        myPref = new MyPreferences(activity);
        attrHash = new HashMap<>();
        this.activity = activity;
        sb1 = new StringBuilder();
        sb2 = new StringBuilder();
        initDictionary();
    }

    public void initDictionary() {
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

    public int index(String tag) {
        return attrHash.get(tag);
    }


    public void insert(List<Order> orders) {

        DBManager._db.beginTransaction();
        try {
            for (Order order : orders) {
//                Order o = getOrder(order.ord_id);
                SQLiteStatement insert;
                String sb = "INSERT OR REPLACE INTO " + table_name + " (" + sb1.toString() + ") " +
                        "VALUES (" + sb2.toString() + ")";
                insert = DBManager._db.compileStatement(sb);

                insert.bindString(index(ord_id), order.ord_id == null ? "" : order.ord_id); // cust_id
                insert.bindString(index(qbord_id), order.qbord_id == null ? "" : order.qbord_id); // cust_id
                insert.bindString(index(emp_id), order.emp_id == null ? "" : order.emp_id); // cust_id
                insert.bindString(index(cust_id), order.cust_id == null ? "" : order.cust_id); // cust_id
                insert.bindString(index(clerk_id), order.clerk_id == null ? "" : order.clerk_id); // cust_id
                insert.bindString(index(c_email), order.c_email == null ? "" : order.c_email); // cust_id
                insert.bindString(index(ord_signature), order.ord_signature == null ? "" : order.ord_signature); // cust_id
                insert.bindString(index(ord_po), order.ord_po == null ? "" : order.ord_po); // cust_id
                insert.bindString(index(total_lines), TextUtils.isEmpty(order.total_lines) ? "0" : order.total_lines); // cust_id
                insert.bindString(index(total_lines_pay),
                        TextUtils.isEmpty(order.total_lines_pay) ? "0" : order.total_lines_pay); // cust_id
                insert.bindString(index(ord_total), TextUtils.isEmpty(order.ord_total) ? "0" : order.ord_total); // cust_id
                insert.bindString(index(ord_comment), order.ord_comment == null ? "" : order.ord_comment); // cust_id
                insert.bindString(index(ord_delivery), order.ord_delivery == null ? "" : order.ord_delivery); // cust_id
                insert.bindString(index(ord_timecreated), order.ord_timecreated == null ? "" : order.ord_timecreated); // cust_id
                insert.bindString(index(ord_timesync), order.ord_timesync == null ? "" : order.ord_timesync); // cust_id
                insert.bindString(index(qb_synctime), order.qb_synctime == null ? "" : order.qb_synctime); // cust_id
                insert.bindString(index(emailed), TextUtils.isEmpty(order.emailed) ? "0" : order.emailed); // cust_id
                insert.bindString(index(processed), TextUtils.isEmpty(order.processed) ? "1" : order.processed); // cust_id
                insert.bindString(index(ord_type), order.ord_type == null ? "" : order.ord_type); // cust_id
                insert.bindString(index(ord_claimnumber), order.ord_claimnumber == null ? "" : order.ord_claimnumber); // cust_id
                insert.bindString(index(ord_rganumber), order.ord_rganumber == null ? "" : order.ord_rganumber); // cust_id
                insert.bindString(index(ord_returns_pu), order.ord_returns_pu == null ? "" : order.ord_returns_pu); // cust_id
                insert.bindString(index(ord_inventory), order.ord_inventory == null ? "" : order.ord_inventory); // cust_id
                insert.bindString(index(ord_issync), TextUtils.isEmpty(order.ord_issync) ? "0" : order.ord_issync); // cust_id
                insert.bindString(index(tax_id), order.tax_id == null ? "" : order.tax_id); // cust_id
                insert.bindString(index(ord_shipvia), order.ord_shipvia == null ? "" : order.ord_shipvia); // cust_id
                insert.bindString(index(ord_shipto), order.ord_shipto == null ? "" : order.ord_shipto); // cust_id
                insert.bindString(index(ord_terms), order.ord_terms == null ? "" : order.ord_terms); // cust_id
                insert.bindString(index(ord_custmsg), order.ord_custmsg == null ? "" : order.ord_custmsg); // cust_id
                insert.bindString(index(ord_class), order.ord_class == null ? "" : order.ord_class); // cust_id
                insert.bindString(index(ord_subtotal), TextUtils.isEmpty(order.ord_subtotal) ? "0" : order.ord_subtotal); // cust_id
                insert.bindString(index(ord_taxamount), TextUtils.isEmpty(order.ord_taxamount) ? "0" : order.ord_taxamount); // cust_id
                insert.bindString(index(ord_discount), TextUtils.isEmpty(order.ord_discount) ? "0" : order.ord_discount); // cust_id
                insert.bindString(index(ord_discount_id), order.ord_discount_id == null ? "" : order.ord_discount_id); // cust_id
                insert.bindString(index(ord_latitude), order.ord_latitude == null ? "" : order.ord_latitude); // cust_id
                insert.bindString(index(ord_longitude), order.ord_longitude == null ? "" : order.ord_longitude); // cust_id
                insert.bindString(index(tipAmount), TextUtils.isEmpty(order.tipAmount) ? "0" : order.tipAmount); // cust_id
                insert.bindString(index(custidkey), order.custidkey == null ? "" : order.custidkey);
                insert.bindString(index(isOnHold), TextUtils.isEmpty(order.isOnHold) ? "0" : order.isOnHold);
                insert.bindString(index(ord_HoldName), order.ord_HoldName == null ? "" : order.ord_HoldName);
                insert.bindString(index(is_stored_fwd), TextUtils.isEmpty(order.is_stored_fwd) ? "0" : order.is_stored_fwd);
                insert.bindString(index(assignedTable), order.assignedTable == null ? "" : order.assignedTable);
                insert.bindString(index(associateID), order.associateID == null ? "" : order.associateID);
                insert.bindLong(index(numberOfSeats), order.numberOfSeats);
                insert.bindString(index(ord_timeStarted), order.ord_timeStarted == null ? DateUtils.getDateAsString(new Date(), DateUtils.DATE_yyyy_MM_ddTHH_mm_ss) : order.ord_timeStarted);
                insert.bindString(index(isVoid), TextUtils.isEmpty(order.isVoid) ? "0" : order.isVoid);
                insert.bindString(index(VAT), TextUtils.isEmpty(order.VAT) ? "0" : order.VAT);

                insert.execute();
                insert.clearBindings();
                insert.close();
                Log.d("Order Insert:", order.toString());
                DinningTableOrderDAO.createDinningTableOrder(order);
                myPref.setLastOrdID(order.ord_id);
            }
            DBManager._db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBManager._db.endTransaction();
        }
    }

//    public void insertOnHold(List<Order> orders) {
//        for (Order order : orders) {
//            order.ord_issync = "1";
//            order.isOnHold = "1";
//        }
//        insert(orders);

//        DBManager._db.beginTransaction();
//        try {
//            SQLiteStatement insert;
//            String sb = "INSERT INTO " + table_name + " (" + sb1.toString() + ") " +
//                    "VALUES (" + sb2.toString() + ")";
//            insert = DBManager._db.compileStatement(sb);
//            for (Order order : orders) {
//                if (checkIfExist(order.ord_id)) {
//                    updateOnHoldSync(order.ord_id);
//                } else {
//                    insert.bindString(index(ord_id), order.ord_id); // ord_id
//                    insert.bindString(index(qbord_id), order.qbord_id); // qbord_id
//                    insert.bindString(index(emp_id), order.emp_id); // emp_id
//                    insert.bindString(index(cust_id), order.cust_id); // cust_id
//                    insert.bindString(index(clerk_id), order.clerk_id); // clerk_id
//                    insert.bindString(index(c_email), order.c_email); // c_email
//                    insert.bindString(index(ord_signature), order.ord_signature); // ord_signature
//                    insert.bindString(index(ord_po), order.ord_po); // ord_po
//                    insert.bindString(index(total_lines), order.total_lines); // total_lines
//                    insert.bindString(index(total_lines_pay), order.total_lines_pay); // total_lines_pay
//                    insert.bindString(index(ord_total), order.ord_total); // ord_total
//                    insert.bindString(index(ord_comment), order.ord_comment); // ord_comment
//                    insert.bindString(index(ord_delivery), order.ord_delivery); // ord_delivery
//                    insert.bindString(index(ord_timecreated), order.ord_timecreated); // ord_timecreated
//                    insert.bindString(index(ord_timesync), order.ord_timesync); // ord_timesync
//                    insert.bindString(index(qb_synctime), order.qb_synctime); // qb_synctime
//                    insert.bindString(index(emailed), order.emailed); // emailed
//                    insert.bindString(index(processed), order.processed); // processed
//                    insert.bindString(index(ord_type), order.ord_type); // ord_type
//                    insert.bindString(index(ord_claimnumber), order.ord_claimnumber); // ord_claimnumber
//                    insert.bindString(index(ord_rganumber), order.ord_rganumber); // ord_rganumber
//                    insert.bindString(index(ord_returns_pu), order.ord_returns_pu); // ord_returns_pu
//                    insert.bindString(index(ord_inventory), order.ord_inventory); // ord_inventory
//                    insert.bindString(index(ord_issync), "1"); // ord_issync
//                    insert.bindString(index(tax_id), order.tax_id); // tax_id
//                    insert.bindString(index(ord_shipvia), order.ord_shipvia); // ord_shipvia
//                    insert.bindString(index(ord_shipto), order.ord_shipto); // ord_shipto
//                    insert.bindString(index(ord_terms), order.ord_terms); // ord_terms
//                    insert.bindString(index(ord_custmsg), order.ord_custmsg); // ord_custmsg
//                    insert.bindString(index(ord_class), order.ord_class); // ord_class
//                    insert.bindString(index(ord_subtotal), order.ord_subtotal); // ord_subtotal
//                    insert.bindString(index(ord_taxamount), order.ord_taxamount); // ord_taxamount
//                    insert.bindString(index(ord_discount), order.ord_discount); // ord_discount
//                    insert.bindString(index(ord_discount_id), order.ord_discount_id); // ord_discount_id
//                    insert.bindString(index(ord_latitude), order.ord_latitude); // ord_latitude
//                    insert.bindString(index(ord_longitude), order.ord_longitude); // ord_longitude
//                    insert.bindString(index(tipAmount), order.tipAmount); // tipAmount
//                    insert.bindString(index(custidkey), order.custidkey); // custidkey
//                    insert.bindString(index(isOnHold), "1"); // isOnHold
//                    insert.bindString(index(ord_HoldName), order.ord_HoldName); // ord_HoldName
//                    insert.bindString(index(assignedTable), order.assignedTable); // ord_HoldName
//                    insert.bindString(index(numberOfSeats), String.valueOf(order.numberOfSeats)); // ord_HoldName
//                    insert.bindString(index(associateID), order.associateID); // ord_HoldName
//                    insert.bindString(index(VAT), order.VAT);
//                    insert.bindString(index(ord_timeStarted), o == null || o.ord_timeStarted == null ? Global.getCurrentDate() : o.ord_timeStarted);
//                    insert.execute();
//                    insert.clearBindings();
//                }
//            }
//            insert.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//
//            DBManager._db.setTransactionSuccessful();
//            DBManager._db.endTransaction();
//        }
//    }

    public void emptyTable() {
        DBManager._db.execSQL("DELETE FROM " + table_name);
        DinningTableOrderDAO.truncate();
    }

    public static void deleteTransaction(Activity activity, String orderId) {
        if (!orderId.isEmpty()) {
            Global global = (Global) activity.getApplication();
            OrdersHandler dbOrders = new OrdersHandler(activity);
            OrderProductsHandler dbOrdProd = new OrderProductsHandler(activity);
            OrderProductsAttr_DB dbOrdAttr = new OrderProductsAttr_DB(activity);
            dbOrders.deleteOrder(orderId);
            dbOrdProd.deleteAllOrdProd(orderId);
            for (ProductAttribute val : global.ordProdAttr)
                dbOrdAttr.deleteOrderProduct(String.valueOf(val.getId()));
            DinningTableOrderDAO.deleteByOrderId(orderId);
        }
    }

    public void deleteOrder(String ord_id) {
        DBManager._db.delete(table_name, "ord_id = ?", new String[]{ord_id});
        Log.d("Delete order:", ord_id);
        DinningTableOrderDAO.deleteByOrderId(ord_id);
    }

    public void emptyTableOnHold() {
        DBManager._db.delete("OrderProduct",
                "OrderProduct.ord_id IN (SELECT op.ord_id FROM OrderProduct op LEFT JOIN Orders o ON op.ord_id=o.ord_id WHERE o.isOnHold = '1' AND o.emp_id != ?)",
                new String[]{myPref.getEmpID()});
        DBManager._db.delete(table_name, "isOnHold = '1' AND emp_id != ?", new String[]{myPref.getEmpID()});
        DinningTableOrderDAO.truncate();
    }

    private boolean checkIfExist(String ordID) {
        String sb = "SELECT 1 FROM " + table_name + " WHERE ord_id = '" +
                ordID + "'";
        Cursor c = DBManager._db.rawQuery(sb, null);
        boolean exists = (c.getCount() > 0);
        c.close();

        return exists;
    }

    public static Order getOrder(Cursor cursor, Activity activity) {
        Order order = new Order(activity);
        try {
            order.numberOfSeats = cursor.getInt(cursor.getColumnIndex("numberOfSeats"));
            order.assignedTable = cursor.getString(cursor.getColumnIndex("assignedTable"));
            order.associateID = cursor.getString(cursor.getColumnIndex("associateID"));
            order.isOnHold = cursor.getString(cursor.getColumnIndex("isOnHold"));
            order.ord_HoldName = cursor.getString(cursor.getColumnIndex("ord_HoldName"));
            order.ord_id = cursor.getString(cursor.getColumnIndex("ord_id"));
            order.qbord_id = cursor.getString(cursor.getColumnIndex("qbord_id"));
            order.emp_id = cursor.getString(cursor.getColumnIndex("emp_id"));
            order.cust_id = cursor.getString(cursor.getColumnIndex("cust_id"));
            order.clerk_id = cursor.getString(cursor.getColumnIndex("clerk_id"));
            order.c_email = cursor.getString(cursor.getColumnIndex("c_email"));
            order.ord_signature = cursor.getString(cursor.getColumnIndex("ord_signature"));
            order.ord_po = cursor.getString(cursor.getColumnIndex("ord_po"));
            order.total_lines = cursor.getString(cursor.getColumnIndex("total_lines"));
            order.total_lines_pay = cursor.getString(cursor.getColumnIndex("total_lines_pay"));
            order.ord_total = cursor.getString(cursor.getColumnIndex("ord_total"));
            order.ord_comment = cursor.getString(cursor.getColumnIndex("ord_comment"));
            order.ord_delivery = cursor.getString(cursor.getColumnIndex("ord_delivery"));
            order.ord_timecreated = cursor.getString(cursor.getColumnIndex("ord_timecreated"));
            order.ord_timesync = cursor.getString(cursor.getColumnIndex("ord_timesync"));
            order.qb_synctime = cursor.getString(cursor.getColumnIndex("qb_synctime"));
            order.emailed = cursor.getString(cursor.getColumnIndex("emailed"));
            order.processed = cursor.getString(cursor.getColumnIndex("processed"));
            order.ord_type = cursor.getString(cursor.getColumnIndex("ord_type"));
            order.ord_claimnumber = cursor.getString(cursor.getColumnIndex("ord_claimnumber"));
            order.ord_rganumber = cursor.getString(cursor.getColumnIndex("ord_rganumber"));
            order.ord_returns_pu = cursor.getString(cursor.getColumnIndex("ord_returns_pu"));
            order.ord_inventory = cursor.getString(cursor.getColumnIndex("ord_inventory"));
            order.ord_issync = cursor.getString(cursor.getColumnIndex("ord_issync"));
            order.tax_id = cursor.getString(cursor.getColumnIndex("tax_id"));
            order.ord_shipvia = cursor.getString(cursor.getColumnIndex("ord_shipvia"));
            order.ord_shipto = cursor.getString(cursor.getColumnIndex("ord_shipto"));
            order.ord_terms = cursor.getString(cursor.getColumnIndex("ord_terms"));
            order.ord_custmsg = cursor.getString(cursor.getColumnIndex("ord_custmsg"));
            order.ord_class = cursor.getString(cursor.getColumnIndex("ord_class"));
            order.ord_subtotal = cursor.getString(cursor.getColumnIndex("ord_subtotal"));
            order.ord_taxamount = cursor.getString(cursor.getColumnIndex("ord_taxamount"));
            order.ord_discount = cursor.getString(cursor.getColumnIndex("ord_discount"));
            order.ord_discount_id = cursor.getString(cursor.getColumnIndex("ord_discount_id"));
            order.ord_latitude = cursor.getString(cursor.getColumnIndex("ord_latitude"));
            order.ord_longitude = cursor.getString(cursor.getColumnIndex("ord_longitude"));
            order.tipAmount = cursor.getString(cursor.getColumnIndex("tipAmount"));
            order.ord_timeStarted = cursor.getString(cursor.getColumnIndex(ord_timeStarted));
            CustomersHandler custHandler = new CustomersHandler(activity);
            order.customer = custHandler.getCustomer(order.cust_id);
            order.VAT = Boolean.toString(cursor.getString(cursor.getColumnIndex("VAT")).equals("1"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return order;
    }

    public Order getOrder(String orderId) // Will populate all unsynchronized
    // orders
    // for XML post
    {
        String sb = "SELECT " + sb1.toString() + " FROM " + table_name + " WHERE ord_id = '" +
                orderId + "'";

        Cursor cursor = DBManager._db.rawQuery(sb, null);
        Order order = new Order(this.activity);
        if (cursor.moveToFirst()) {
            order = getOrder(cursor, activity);

        }
        cursor.close();
        return order;
    }

    private List<Order> getOrders(Cursor cursor) {
        List<Order> orders = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                orders.add(getOrder(cursor, activity));
            } while (cursor.moveToNext());
        }
        return orders;
    }

    public List<Order> getUnsyncOrders() // Will populate all unsynchronized orders
    // for XML post
    {
        StringBuilder sb = new StringBuilder();
        if (Global.isForceUpload)
            sb.append("SELECT * FROM ").append(table_name).append(" WHERE ord_issync = '0'");
        else
            sb.append("SELECT ").append(sb1.toString()).append(" FROM ").append(table_name)
                    .append(" WHERE ord_issync = '0' AND processed != '0' AND is_stored_fwd = '0'");

        net.sqlcipher.Cursor cursor = DBManager._db.rawQuery(sb.toString(), null);
        List<Order> orders = getOrders(cursor);
        cursor.close();
        return orders;
    }

    public Cursor getTupyxOrders() {
        return DBManager._db.rawQuery("SELECT * FROM Orders o LEFT OUTER JOIN Payments p ON o.ord_id = p.job_id LEFT OUTER JOIN Customers c ON o.cust_id = c.cust_id WHERE p.paymethod_id = 'Wallet' AND ord_issync = '0'", null);
    }

    public long getNumUnsyncTupyxOrders() {
        SQLiteStatement stmt = DBManager._db.compileStatement("SELECT Count(*) FROM " + table_name + " o LEFT OUTER JOIN Payments p ON o.ord_id = p.job_id WHERE p.paymethod_id = 'Wallet' AND o.ord_issync = '0'");
        long count = stmt.simpleQueryForLong();
        stmt.close();
        return count;
    }

    public List<Order> getUnsyncOrdersOnHold() {
        net.sqlcipher.Cursor cursor = DBManager._db.rawQuery("SELECT * FROM " + table_name + " WHERE ord_issync = '0' AND isOnHold = '1'", null);
        List<Order> orders = getOrders(cursor);
        cursor.close();
        return orders;
    }

    public long getNumUnsyncOrdersOnHold() {
        SQLiteStatement stmt = DBManager._db.compileStatement("SELECT Count(*) FROM " + table_name + " WHERE ord_issync = '0' AND isOnHold = '1'");
        long count = stmt.simpleQueryForLong();
        stmt.close();
        return count;
    }

    public Cursor getOrderOnHold() {
        Cursor c = DBManager._db
                .rawQuery("SELECT ord_id as '_id',* FROM Orders WHERE isOnHold = '1' ORDER BY ord_id ASC", null);
        c.moveToFirst();
        return c;
    }

    public long getNumUnsyncOrders() {

        SQLiteStatement stmt = DBManager._db.compileStatement("SELECT Count(*) FROM " + table_name + " WHERE ord_issync = '0'");
        long count = stmt.simpleQueryForLong();
        stmt.close();
        return count;
    }

    public String getLastOrderId(int deviceId, int year) {
        String lastOrdID = myPref.getLastOrdID();
        boolean getIdFromDB = false;
        StringBuilder sb = new StringBuilder();
        if (TextUtils.isEmpty(lastOrdID) || lastOrdID.length() <= 4) {
            getIdFromDB = true;
        } else {
            String[] tokens = myPref.getLastOrdID().split("-");
            if (!tokens[2].equalsIgnoreCase(String.valueOf(year))) {
                getIdFromDB = true;
            }
        }

        if (getIdFromDB) {
            sb.append("select max(ord_id) from ").append(table_name).append(" WHERE ord_id like '").append(deviceId)
                    .append("-%-").append(year).append("'");

            SQLiteStatement stmt = DBManager._db.compileStatement(sb.toString());
            Cursor cursor = DBManager._db.rawQuery(sb.toString(), null);
            cursor.moveToFirst();
            lastOrdID = cursor.getString(0);
            cursor.close();
            stmt.close();
            if (TextUtils.isEmpty(lastOrdID)) {
                lastOrdID = myPref.getEmpID() + "-" + "00001" + "-" + year;
            }
            myPref.setLastOrdID(lastOrdID);
        }

        return lastOrdID;

    }

    public long getNumUnsyncProcessedOrders() {

        SQLiteStatement stmt = DBManager._db.compileStatement("SELECT Count(*) FROM " + table_name + " WHERE ord_issync = '0' AND processed != '0'");
        long count = stmt.simpleQueryForLong();
        stmt.close();
        return count;
    }

    public long getNumUnsyncOrdersStoredFwd() {
        SQLiteStatement stmt = DBManager._db.compileStatement("SELECT Count(*) FROM " + table_name + " WHERE is_stored_fwd = '1'");
        long count = stmt.simpleQueryForLong();
        stmt.close();
        return count;
    }

    public boolean unsyncOrdersLeft() {

        SQLiteStatement stmt = DBManager._db.compileStatement("SELECT Count(*) FROM " + table_name + " WHERE ord_issync = '0' AND processed != '0'");
        long count = stmt.simpleQueryForLong();
        stmt.close();
        return count != 0;
    }

    private String getOrderTypesAsSQLArray(Global.OrderType[] orderTypes) {
        StringBuilder sb = new StringBuilder();
        for (Global.OrderType orderType : orderTypes) {
            sb.append("'").append(orderType.getCode()).append("',");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    public Cursor getReceipts1Data(Global.OrderType[] orderTypes) // Transactions Receipts first
    // listview
    {


        String subquery1 = "SELECT ord_id as _id,ord_total,ord_issync,cust_id,isVoid,ord_type" +
                " FROM Orders WHERE ord_type IN (";
        String subquery2 = ") AND isOnHold = '0' ORDER BY rowid DESC";
        Cursor cursor = DBManager._db.rawQuery(subquery1 + getOrderTypesAsSQLArray(orderTypes) + subquery2, null);
        cursor.moveToFirst();
        return cursor;
    }

    public Cursor getReceipts1CustData(Global.OrderType[] orderTypes, String custID) {

        String subquery1 = "SELECT ord_id as _id,ord_total,ord_issync,cust_id,isVoid,ord_type FROM Orders WHERE ord_type IN (";
        String subquery2 = ") AND cust_id = ?";
        String subquery3 = " AND isOnHold = '0' ORDER BY rowid DESC";
        Cursor cursor = DBManager._db.rawQuery(subquery1 + getOrderTypesAsSQLArray(orderTypes) + subquery2 + subquery3, new String[]{custID});

        cursor.moveToFirst();
        return cursor;

    }

    public Cursor getSearchOrder(Global.OrderType[] orderTypes, String search, String customerID) // Transactions
    // Receipts
    // first
    // listview
    {

        String subqueries[] = new String[4];
        StringBuilder sb = new StringBuilder();
        String[] params;
        if (customerID == null) {
            subqueries[0] = "SELECT Orders.ord_id as _id,Orders.ord_total,Orders.ord_issync,Customers.cust_id,Orders.isVoid,Orders.ord_type FROM Orders JOIN Customers WHERE Orders.ord_type IN(";
            subqueries[1] = ") AND Orders.cust_id = Customers.cust_id AND Orders.ord_id LIKE ? ORDER BY Orders.rowid DESC";
            sb.append(subqueries[0]).append(getOrderTypesAsSQLArray(orderTypes)).append(subqueries[1]);// .append(search).append(subqueries[2]);
            params = new String[]{"%" + search + "%"};
        } else {
            subqueries[0] = "SELECT ord_id as _id,ord_total,ord_issync,cust_id,isVoid,ord_type FROM Orders WHERE ord_type IN(";
            subqueries[1] = ") AND cust_id = ?";
            subqueries[2] = " AND Orders.ord_id LIKE ? ORDER BY Orders.rowid DESC";

            sb.append(subqueries[0]).append(getOrderTypesAsSQLArray(orderTypes)).append(subqueries[1]).append(subqueries[2]);// .append(search).append(subqueries[3]);
            params = new String[]{customerID, "%" + search + "%"};
        }

        Cursor cursor = DBManager._db.rawQuery(sb.toString(), params);
        cursor.moveToFirst();
        return cursor;
    }

    public void updateIsSync(List<String[]> list) {

        StringBuilder sb = new StringBuilder();
        sb.append(ord_id).append(" = ?");

        ContentValues args = new ContentValues();

        int size = list.size();
        for (int i = 0; i < size; i++) {
            if (list.get(i)[0].equals("0"))
                args.put(ord_issync, "1");
            else
                args.put(ord_issync, "0");
            DBManager._db.update(table_name, args, sb.toString(), new String[]{list.get(i)[1]});
        }
    }

    private void updateOnHoldSync(String ordID) {
        ContentValues args = new ContentValues();

        args.put(ord_issync, "1");
        DBManager._db.update(table_name, args, ord_id + " = ?", new String[]{ordID});
    }

    public String updateFinishOnHold(String ordID) {
//        StringBuilder sb2 = new StringBuilder();
//        StringBuilder sb = new StringBuilder();

        Cursor c = DBManager._db.rawQuery("SELECT ord_timecreated FROM Orders WHERE ord_id = ?",
                new String[]{ordID});
        String dateCreated = DateUtils.getDateAsString(new Date(), DateUtils.DATE_yyyy_MM_ddTHH_mm_ss);

        if (c.moveToFirst())
            dateCreated = c.getString(c.getColumnIndex(ord_timecreated));

//        sb.append("DELETE FROM ").append(table_name).append(" WHERE ord_id = '").append(ordID).append("'");
//        sb2.append("DELETE FROM OrderProduct WHERE ord_id = '").append(ordID).append("'");

        DBManager._db.delete(table_name, "ord_id = ?", new String[]{ordID});
        DBManager._db.delete("OrderProduct", "ord_id = ?", new String[]{ordID});
        c.close();
        return dateCreated;
    }

    public void updateIsProcessed(String orderID, String updateValue) {
        ContentValues args = new ContentValues();
        args.put(processed, updateValue);
        args.put(isOnHold, "0");
        DBManager._db.update(table_name, args, ord_id + " = ?", new String[]{orderID});
        DinningTableOrderDAO.deleteByNumber(getOrder(orderID).assignedTable);
    }

    public void updateOrderTypeToInvoice(String orderID) {
        ContentValues args = new ContentValues();
        args.put(ord_type, Global.OrderType.INVOICE.getCodeString());
        DBManager._db.update(table_name, args, ord_id + " = ?", new String[]{orderID});
    }

    public void updateOrderComment(String orderID, String value) {
        ContentValues args = new ContentValues();
        args.put(ord_comment, value);
        DBManager._db.update(table_name, args, ord_id + " = ?", new String[]{orderID});
    }

    public void updateOrderStoredFwd(String _order_id, String value) {
        ContentValues args = new ContentValues();
        args.put(is_stored_fwd, value);
        DBManager._db.update(table_name, args, ord_id + " = ?", new String[]{_order_id});
    }

    public void updateIsTotalLinesPay(String orderID, String updateValue) {
        ContentValues args = new ContentValues();
        args.put(total_lines_pay, updateValue);
        DBManager._db.update(table_name, args, ord_id + " = ?", new String[]{orderID});
    }

    public void updateIsVoid(String param) {
        ContentValues args = new ContentValues();
        args.put(isVoid, "1");
        args.put(ord_issync, "0");
        args.put(processed, "9");
        DBManager._db.update(table_name, args, ord_id + " = ?", new String[]{param});
    }

    public String getColumnValue(String key, String _ord_id) {
        Cursor c = DBManager._db.rawQuery("SELECT " + key + " FROM " + table_name + " WHERE ord_id = ?", new String[]{_ord_id});
        String value = "";
        if (c.moveToFirst()) {
            value = c.getString(c.getColumnIndex(key));
        }

        return value;
    }

    public boolean isOrderOffline(String ordID) {
        Cursor c = DBManager._db.rawQuery("SELECT ord_issync FROM Orders WHERE ord_id = ?", new String[]{ordID});

        boolean offline = false;
        if (c.moveToFirst()) {
            offline = c.getString(c.getColumnIndex("ord_issync")).equals("0");
        }
        c.close();
        return offline;

    }


//    public HashMap<String, String> getOrderDetails(String ordID) {
//        HashMap<String, String> map = new HashMap<String, String>();
//        String subquery1 = "SELECT o.ord_id as _id,o.ord_total ,o.ord_timecreated,o.ord_type,o.isVoid,o.clerk_id," +
//                "o.ord_comment,o.ord_shipvia,o.ord_terms,o.ord_delivery,"
//                + "o.c_email,o.cust_id, o.ord_signature,o.ord_po,o.ord_latitude,o.ord_longitude " +
//                "FROM Orders o  WHERE o.ord_id ='";
//        String subquery2 = "'";
//        Cursor cursor = DBManager._db.rawQuery(subquery1 + ordID + subquery2, null);
//        if (cursor.moveToFirst()) {
//            do {
//                String data = cursor.getString(cursor.getColumnIndex(ord_total));
//                map.put(ord_total, data);
//
//                data = Global.formatToDisplayDate(cursor.getString(cursor.getColumnIndex(ord_timecreated)), activity,
//                        0);
//                map.put(ord_timecreated, data);
//
//                data = cursor.getString(cursor.getColumnIndex(clerk_id));
//                map.put(clerk_id, data);
//
//                data = cursor.getString(cursor.getColumnIndex(ord_comment));
//                map.put(ord_comment, data);
//
//                data = cursor.getString(cursor.getColumnIndex(ord_shipvia));
//                map.put(ord_shipvia, data);
//
//                data = cursor.getString(cursor.getColumnIndex(ord_terms));
//                map.put(ord_terms, data);
//
//                data = cursor.getString(cursor.getColumnIndex(ord_delivery));
//                map.put(ord_delivery, data);
//
//                data = cursor.getString(cursor.getColumnIndex(c_email));
//                map.put(c_email, data);
//
//                data = cursor.getString(cursor.getColumnIndex(cust_id));
//                map.put(cust_id, data);
//
//                data = cursor.getString(cursor.getColumnIndex(ord_signature));
//                map.put(ord_signature, data);
//
//                data = cursor.getString(cursor.getColumnIndex(ord_po));
//                map.put(ord_po, data);
//
//                data = cursor.getString(cursor.getColumnIndex(ord_latitude));
//                map.put(ord_latitude, data);
//
//                data = cursor.getString(cursor.getColumnIndex(ord_longitude));
//                map.put(ord_longitude, data);
//
//                data = cursor.getString(cursor.getColumnIndex(ord_type));
//                map.put(ord_type, data);
//
//                data = cursor.getString(cursor.getColumnIndex(isVoid));
//                map.put(isVoid, data);
//            } while (cursor.moveToNext());
//        }
//        cursor.close();
//        return map;
//    }

    public Order getPrintedOrder(String ordID) {
        Order anOrder = new Order(activity);
        String sb = ("SELECT o.ord_id,o.ord_timecreated,o.ord_total,o.ord_subtotal,o.ord_discount,o.ord_taxamount,c.cust_name,c.AccountNumnber,o.cust_id, "
                + "o.ord_total AS 'gran_total', tipAmount, ord_signature,o.ord_HoldName,o.clerk_id,o.ord_comment,o.isVoid FROM Orders o LEFT OUTER JOIN Customers c ON "
                + "o.cust_id = c.cust_id WHERE o.ord_id = '") +
                ordID + "'";

        Cursor cursor = DBManager._db.rawQuery(sb, null);
        if (cursor.moveToFirst()) {
            do {
                anOrder.ord_id = getValue(cursor.getString(cursor.getColumnIndex(ord_id)));
                anOrder.ord_timecreated = cursor.getString(cursor.getColumnIndex(ord_timecreated));
                anOrder.ord_total = getValue(cursor.getString(cursor.getColumnIndex(ord_total)));
                anOrder.ord_subtotal = getValue(cursor.getString(cursor.getColumnIndex(ord_subtotal)));
                anOrder.ord_discount = getValue(cursor.getString(cursor.getColumnIndex(ord_discount)));
                anOrder.ord_taxamount = getValue(cursor.getString(cursor.getColumnIndex(ord_taxamount)));
                anOrder.cust_name = getValue(cursor.getString(cursor.getColumnIndex("cust_name")));
                anOrder.gran_total = getValue(cursor.getString(cursor.getColumnIndex("gran_total")));
                anOrder.tipAmount = getValue(cursor.getString(cursor.getColumnIndex(tipAmount)));
                anOrder.ord_signature = getValue(cursor.getString(cursor.getColumnIndex(ord_signature)));
                anOrder.ord_HoldName = getValue(cursor.getString(cursor.getColumnIndex(ord_HoldName)));
                anOrder.clerk_id = getValue(cursor.getString(cursor.getColumnIndex(clerk_id)));
                anOrder.ord_comment = getValue(cursor.getString(cursor.getColumnIndex(ord_comment)));
                anOrder.cust_id = getValue(cursor.getString(cursor.getColumnIndex("AccountNumnber")));
                anOrder.isVoid = getValue(cursor.getString(cursor.getColumnIndex(isVoid)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return anOrder;
    }

    private String getValue(String value) {
        if (value == null)
            value = "";
        return value;
    }

    public List<Order> getOrderDayReport(String clerk_id, String date) {
        List<Order> listOrder = new ArrayList<>();

        StringBuilder query = new StringBuilder();
        query.append(
                "SELECT ord_type,sum(ord_subtotal) as 'ord_subtotal',sum(ord_discount) as 'ord_discount', sum(ord_taxamount) as 'ord_taxamount' ,  ");
        query.append("sum(ord_total) as 'ord_total',date(ord_timecreated,'localtime') as 'date' FROM Orders ");

        String[] where_values = null;
        if (clerk_id != null && !clerk_id.isEmpty()) {
            query.append("WHERE clerk_id = ? AND isVoid = '0' ");
            where_values = new String[]{clerk_id};

            if (date != null && !date.isEmpty()) {
                query.append(" AND date = ? ");
                where_values = new String[]{clerk_id, date};
            }
        } else if (date != null && !date.isEmpty()) {
            query.append(" WHERE  date = ? AND isVoid = '0' ");
            where_values = new String[]{date};
        } else {
            query.append(" WHERE  isVoid = '0' ");
        }
        query.append(" GROUP BY ord_type");

        Cursor c = DBManager._db.rawQuery(query.toString(), where_values);
        if (c.moveToFirst()) {
            int i_ord_type = c.getColumnIndex(ord_type);
            int i_ord_subtotal = c.getColumnIndex(ord_subtotal);
            int i_ord_discount = c.getColumnIndex(ord_discount);
            int i_ord_taxamount = c.getColumnIndex(ord_taxamount);
            int i_ord_total = c.getColumnIndex(ord_total);
            do {
                Order ord = new Order(activity);
                ord.ord_type = c.getString(i_ord_type);
                ord.ord_subtotal = c.getString(i_ord_subtotal);
                ord.ord_discount = c.getString(i_ord_discount);
                ord.ord_taxamount = c.getString(i_ord_taxamount);
                ord.ord_total = c.getString(i_ord_total);

                listOrder.add(ord);
            } while (c.moveToNext());
        }

        c.close();
        return listOrder;
    }

    public List<Order> getARTransactionsDayReport(String clerk_id, String date) {
        List<Order> listOrder = new ArrayList<>();

        StringBuilder query = new StringBuilder();
        query.append(
                "SELECT o.ord_id, c.cust_name , sum(o.ord_total) as 'ord_total'," +
                        "date(o.ord_timecreated,'localtime') as 'date' FROM Orders o LEFT JOIN Customers c ");
        query.append("ON o.cust_id = c.cust_id WHERE o.ord_type = '2' ");

        String[] where_values = null;
        if (clerk_id != null && !clerk_id.isEmpty()) {
            query.append("AND clerk_id = ? ");
            where_values = new String[]{clerk_id};

            if (date != null && !date.isEmpty()) {
                query.append(" AND date = ? ");
                where_values = new String[]{clerk_id, date};
            }
        } else if (date != null && !date.isEmpty()) {
            query.append(" AND date = ? ");
            where_values = new String[]{date};
        }

        Cursor c = DBManager._db.rawQuery(query.toString(), where_values);
        if (c.moveToFirst()) {
            int i_ord_id = c.getColumnIndex(ord_id);
            int i_cust_name = c.getColumnIndex("cust_name");
            int i_ord_total = c.getColumnIndex(ord_total);
            int i_ord_timecreated = c.getColumnIndex("date");

            do {
                if (c.getString(i_ord_id) != null) {
                    Order ord = new Order(activity);
                    ord.ord_id = c.getString(i_ord_id);
                    ord.cust_name = c.getString(i_cust_name);
                    ord.ord_total = c.getString(i_ord_total);
                    ord.ord_timecreated = c.getString(i_ord_timecreated);
                    listOrder.add(ord);
                }
            } while (c.moveToNext());
        }

        c.close();
        return listOrder;
    }

    public void insert(Order order) {
        insert(Arrays.asList(order));
    }
}
