package com.android.database;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import com.android.emobilepos.models.OrderProduct;
import com.android.emobilepos.models.Orders;
import com.android.support.Global;
import com.android.support.MyPreferences;

import net.sqlcipher.database.SQLiteStatement;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class OrderProductsHandler {

    private final String ord_id = "ord_id";
    private final String addon = "addon";
    private final String isAdded = "isAdded";
    private final String isPrinted = "isPrinted";
    private final String cardIsActivated = "cardIsActivated";
    private final String item_void = "item_void";
    private final String ordprod_id = "ordprod_id";

    private final String prod_id = "prod_id";
    private final String prod_sku = "prod_sku";
    private final String prod_upc = "prod_upc";
    private final String ordprod_qty = "ordprod_qty";
    private final String overwrite_price = "overwrite_price";
    private final String reason_id = "reason_id";
    private final String ordprod_name = "ordprod_name";// add
    private final String ordprod_comment = "ordprod_comment";
    private final String ordprod_desc = "ordprod_desc";
    private final String pricelevel_id = "pricelevel_id";
    private final String prod_seq = "prod_seq";
    private final String uom_name = "uom_name";
    private final String uom_conversion = "uom_conversion";
    private final String uom_id = "uom_id";
    private final String prod_taxId = "prod_taxId"; // add
    private final String prod_taxValue = "prod_taxValue"; // add
    private final String discount_id = "discount_id";
    private final String discount_value = "discount_value";
    private final String cat_id = "cat_id";

    private final String prod_istaxable = "prod_istaxable";
    private final String discount_is_taxable = "discount_is_taxable";
    private final String discount_is_fixed = "discount_is_fixed";
    private final String onHand = "onHand";
    private final String imgURL = "imgURL";
    private final String prod_price = "prod_price";
    private final String prod_type = "prod_type";
    private final String itemTotal = "itemTotal";
    private final String itemSubtotal = "itemSubtotal";

    private final String addon_section_name = "addon_section_name";
    private final String addon_position = "addon_position";
    private final String hasAddons = "hasAddons";

    public final List<String> attr = Arrays.asList(addon, isAdded, isPrinted, item_void, ordprod_id,
            ord_id, prod_id, prod_sku, prod_upc, ordprod_qty, overwrite_price, reason_id, ordprod_name, ordprod_comment, ordprod_desc,
            pricelevel_id, prod_seq, uom_name, uom_conversion, uom_id, prod_taxId, prod_taxValue, discount_id,
            discount_value, prod_istaxable, discount_is_taxable, discount_is_fixed, onHand, imgURL, prod_price,
            prod_type, itemTotal, itemSubtotal, addon_section_name, addon_position, hasAddons, cat_id);

    public StringBuilder sb1, sb2, sb3;
    public final String empStr = "";
    public HashMap<String, Integer> attrHash;
    public Global global;
    private List<String[]> data;
    private List<HashMap<String, Integer>> dictionaryListMap;
    public static final String table_name = "OrderProduct";
    private Activity activity;
    private MyPreferences myPref;

    public OrderProductsHandler(Activity activity) {
        global = (Global) activity.getApplication();
        this.activity = activity;
        attrHash = new HashMap<String, Integer>();
        sb1 = new StringBuilder();
        sb2 = new StringBuilder();
        sb3 = new StringBuilder();
        myPref = new MyPreferences(activity);
        initDictionary();
    }

    public void initDictionary() {
        int size = attr.size();
        for (int i = 0; i < size; i++) {
            attrHash.put(attr.get(i), i + 1);
            if ((i + 1) < size) {
                sb1.append(attr.get(i)).append(",");
                sb3.append("op.").append(attr.get(i)).append(",");
                sb2.append("?").append(",");
            } else {
                sb1.append(attr.get(i));
                sb3.append("op.").append(attr.get(i));
                sb2.append("?");
            }
        }
    }

    private String getData(String tag, int record) {
        Integer i = dictionaryListMap.get(record).get(tag);
        if (i != null) {
            return data.get(record)[i];
        }
        return empStr;
    }

    private int index(String tag) {
        return attrHash.get(tag);
    }

    public void insert(List<OrderProduct> order) {

        // SQLiteDatabase db = dbManager.openWritableDB();
        DBManager._db.beginTransaction();
        try {

            boolean isRestaurantMode = myPref.getPreferences(MyPreferences.pref_restaurant_mode);
            SQLiteStatement insert = null;
            StringBuilder sb = new StringBuilder();
            sb.append("INSERT OR REPLACE INTO ").append(table_name).append(" (").append(sb1.toString()).append(") ")
                    .append("VALUES (").append(sb2.toString()).append(")");
            insert = DBManager._db.compileStatement(sb.toString());

            int size = order.size();

            for (int i = 0; i < size; i++) {
                OrderProduct prod = order.get(i);
                insert.bindString(index(addon), TextUtils.isEmpty(prod.addon) ? "0" : prod.addon); // addon
                insert.bindString(index(isAdded), prod.isAdded == null ? "" : prod.isAdded); // isAdded
                insert.bindString(index(isPrinted), TextUtils.isEmpty(prod.isPrinted) ? "0" : prod.isPrinted); // isPrinted
                insert.bindString(index(item_void), TextUtils.isEmpty(prod.item_void) ? "0" : prod.item_void); // item_void
                insert.bindString(index(ordprod_id), prod.ordprod_id == null ? "" : prod.ordprod_id); // ordprod_id
                insert.bindString(index(ord_id), prod.ord_id == null ? "" : prod.ord_id); // ord_id
                insert.bindString(index(prod_id), prod.prod_id == null ? "" : prod.prod_id); // prod_id
                insert.bindString(index(prod_sku), prod.prod_sku == null ? "" : prod.prod_sku); // prod_sku
                insert.bindString(index(prod_upc), prod.prod_upc == null ? "" : prod.prod_upc); // prod_upc
                insert.bindString(index(ordprod_qty), TextUtils.isEmpty(prod.ordprod_qty) ? "0" : prod.ordprod_qty); // ordprod_qty
                insert.bindString(index(overwrite_price),
                        TextUtils.isEmpty(prod.overwrite_price) ? "0" : prod.overwrite_price); // overwrite_price
                insert.bindString(index(reason_id), prod.reason_id == null ? "" : prod.reason_id); // reason_id
                insert.bindString(index(ordprod_name), prod.ordprod_name == null ? "" : prod.ordprod_name); // ordprod_name
                if (prod.ordprod_comment != null && !prod.ordprod_comment.isEmpty())
                    insert.bindString(index(ordprod_desc),
                            prod.ordprod_desc == null ? "" : prod.ordprod_desc + "-" + prod.ordprod_comment); // ordprod_desc
                else
                    insert.bindString(index(ordprod_desc), prod.ordprod_desc == null ? "" : prod.ordprod_desc);
                insert.bindString(index(ordprod_comment), prod.ordprod_comment == null ? "" : prod.ordprod_comment);
                insert.bindString(index(pricelevel_id), prod.pricelevel_id == null ? "" : prod.pricelevel_id); // pricelevel_id
                insert.bindString(index(prod_seq), TextUtils.isEmpty(prod.prod_seq) ? "1" : prod.prod_seq); // prod_seq
                insert.bindString(index(uom_name), prod.uom_name == null ? "" : prod.uom_name); // uom_name
                insert.bindString(index(uom_conversion), prod.uom_conversion == null ? "" : prod.uom_conversion); // uom_conversion
                insert.bindString(index(uom_id), prod.uom_id == null ? "" : prod.uom_id); // uom_id
                insert.bindString(index(prod_taxId), prod.prod_taxId == null ? "" : prod.prod_taxId); // prod_taxId
                insert.bindString(index(prod_taxValue),
                        TextUtils.isEmpty(prod.prod_taxValue) ? "0" : prod.prod_taxValue); // prod_taxValue
                insert.bindString(index(discount_id), prod.discount_id == null ? "" : prod.discount_id); // discount_id
                insert.bindString(index(discount_value),
                        TextUtils.isEmpty(prod.discount_value) ? "0" : prod.discount_value); // discount_value
                insert.bindString(index(prod_istaxable),
                        TextUtils.isEmpty(prod.prod_istaxable) ? "0" : prod.prod_istaxable); // prod_istaxable
                insert.bindString(index(discount_is_taxable),
                        TextUtils.isEmpty(prod.discount_is_taxable) ? "0" : prod.discount_is_taxable); // discount_is_taxable
                insert.bindString(index(discount_is_fixed),
                        TextUtils.isEmpty(prod.discount_is_fixed) ? "0" : prod.discount_is_fixed); // discount_is_fixed
                insert.bindString(index(onHand), TextUtils.isEmpty(prod.onHand) ? "0" : prod.onHand); // onHand
                insert.bindString(index(imgURL), prod.imgURL == null ? "" : prod.imgURL); // imgURL
                insert.bindString(index(prod_price), TextUtils.isEmpty(prod.prod_price) ? "0" : prod.prod_price); // prod_price
                insert.bindString(index(prod_type), prod.prod_type == null ? "" : prod.prod_type); // prod_type
                insert.bindString(index(itemTotal), TextUtils.isEmpty(prod.itemTotal) ? "0" : prod.itemTotal); // itemTotal
                insert.bindString(index(itemSubtotal), TextUtils.isEmpty(prod.itemSubtotal) ? "0" : prod.itemSubtotal); // itemSubtotal
                insert.bindString(index(hasAddons), TextUtils.isEmpty(prod.hasAddons) ? "0" : prod.hasAddons); // hasAddons
                insert.bindString(index(addon_section_name),
                        TextUtils.isEmpty(prod.addon_section_name) ? "" : prod.addon_section_name);
                insert.bindString(index(addon_position),
                        TextUtils.isEmpty(prod.addon_position) ? "0" : prod.addon_position);
                insert.bindString(index(cat_id), prod.cat_id == null ? "" : prod.cat_id);

                insert.execute();
                insert.clearBindings();

                if (isRestaurantMode && Global.orderProductAddonsMap != null
                        && Global.orderProductAddonsMap.containsKey(prod.ordprod_id == null ? "" : prod.ordprod_id)) {
                    insertAddon(insert, prod.ordprod_id);
                }

            }
            insert.close();
            DBManager._db.setTransactionSuccessful();

        } catch (Exception e) {
            StringBuilder sb = new StringBuilder();
            sb.append(e.getMessage()).append(" [com.android.emobilepos.OrderProductsHandler (at Class.insert)]");

//			Tracker tracker = EasyTracker.getInstance(activity);
//			tracker.send(MapBuilder.createException(sb.toString(), false).build());
        } finally {
            DBManager._db.endTransaction();
        }
        // db.close();
    }

    private void insertAddon(SQLiteStatement insert, String ordID) {
        global.orderProductAddons = Global.orderProductAddonsMap.get(ordID);
        int size = global.orderProductAddons.size();
        for (int i = 0; i < size; i++) {
            OrderProduct prod = global.orderProductAddons.get(i);
            insert.bindString(index(addon), TextUtils.isEmpty(prod.addon) ? "0" : prod.addon); // addon
            insert.bindString(index(isAdded), prod.isAdded == null ? "" : prod.isAdded); // isAdded
            insert.bindString(index(isPrinted), TextUtils.isEmpty(prod.isPrinted) ? "0" : prod.isPrinted); // isPrinted
            insert.bindString(index(item_void), TextUtils.isEmpty(prod.item_void) ? "0" : prod.item_void); // item_void
            insert.bindString(index(ordprod_id), prod.ordprod_id == null ? "" : prod.ordprod_id); // ordprod_id
            insert.bindString(index(ord_id), prod.ord_id == null ? "" : prod.ord_id); // ord_id
            insert.bindString(index(prod_id), prod.prod_id == null ? "" : prod.prod_id); // prod_id
            insert.bindString(index(ordprod_qty), TextUtils.isEmpty(prod.ordprod_qty) ? "0" : prod.ordprod_qty); // ordprod_qty
            insert.bindString(index(overwrite_price),
                    TextUtils.isEmpty(prod.overwrite_price) ? "0" : prod.overwrite_price); // overwrite_price
            insert.bindString(index(reason_id), prod.reason_id == null ? "" : prod.reason_id); // reason_id
            insert.bindString(index(ordprod_name), prod.ordprod_name == null ? "" : prod.ordprod_name); // ordprod_name
            insert.bindString(index(ordprod_desc), prod.ordprod_desc == null ? "" : prod.ordprod_desc);
            insert.bindString(index(ordprod_comment), prod.ordprod_comment == null ? "" : prod.ordprod_comment);
            insert.bindString(index(pricelevel_id), prod.pricelevel_id == null ? "" : prod.pricelevel_id); // pricelevel_id
            insert.bindString(index(prod_seq), TextUtils.isEmpty(prod.prod_seq) ? "1" : prod.prod_seq); // prod_seq
            insert.bindString(index(uom_name), prod.uom_name == null ? "" : prod.uom_name); // uom_name
            insert.bindString(index(uom_conversion), prod.uom_conversion == null ? "" : prod.uom_conversion); // uom_conversion
            insert.bindString(index(uom_id), prod.uom_id == null ? "" : prod.uom_id); // uom_id
            insert.bindString(index(prod_taxId), prod.prod_taxId == null ? "" : prod.prod_taxId); // prod_taxId
            insert.bindString(index(prod_taxValue), TextUtils.isEmpty(prod.prod_taxValue) ? "0" : prod.prod_taxValue); // prod_taxValue
            insert.bindString(index(discount_id), prod.discount_id == null ? "" : prod.discount_id); // discount_id
            insert.bindString(index(discount_value),
                    TextUtils.isEmpty(prod.discount_value) ? "0" : prod.discount_value); // discount_value
            insert.bindString(index(prod_istaxable),
                    TextUtils.isEmpty(prod.prod_istaxable) ? "0" : prod.prod_istaxable); // prod_istaxable
            insert.bindString(index(discount_is_taxable),
                    TextUtils.isEmpty(prod.discount_is_taxable) ? "0" : prod.discount_is_taxable); // discount_is_taxable
            insert.bindString(index(discount_is_fixed),
                    TextUtils.isEmpty(prod.discount_is_fixed) ? "0" : prod.discount_is_fixed); // discount_is_fixed
            insert.bindString(index(onHand), TextUtils.isEmpty(prod.onHand) ? "0" : prod.onHand); // onHand
            insert.bindString(index(imgURL), prod.imgURL == null ? "" : prod.imgURL); // imgURL
            insert.bindString(index(prod_price), TextUtils.isEmpty(prod.prod_price) ? "0" : prod.prod_price); // prod_price
            insert.bindString(index(prod_type), TextUtils.isEmpty(prod.prod_type) ? "" : prod.prod_type); // prod_type
            insert.bindString(index(itemTotal), TextUtils.isEmpty(prod.itemTotal) ? "0" : prod.itemTotal); // itemTotal
            insert.bindString(index(itemSubtotal), TextUtils.isEmpty(prod.itemSubtotal) ? "0" : prod.itemSubtotal); // itemSubtotal
            insert.bindString(index(hasAddons), TextUtils.isEmpty(prod.hasAddons) ? "0" : prod.hasAddons); // hasAddons
            insert.bindString(index(addon_section_name),
                    prod.addon_section_name == null ? "" : prod.addon_section_name);
            insert.bindString(index(addon_position),
                    TextUtils.isEmpty(prod.addon_position) ? "0" : prod.addon_position);
            insert.bindString(index(cat_id), prod.cat_id == null ? "" : prod.cat_id);
            insert.execute();
            insert.clearBindings();
        }
    }

    public void insertOnHold(List<String[]> data, List<HashMap<String, Integer>> dictionary) {
        DBManager._db.beginTransaction();
        try {

            this.data = data;
            dictionaryListMap = dictionary;
            SQLiteStatement insert = null;
            StringBuilder sb = new StringBuilder();
            sb.append("INSERT INTO ").append(table_name).append(" (").append(sb1.toString()).append(") ")
                    .append("VALUES (").append(sb2.toString()).append(")");
            insert = DBManager._db.compileStatement(sb.toString());

            int size = this.data.size();

            for (int i = 0; i < size; i++) {

                if (!checkIfExist(getData(ordprod_id, i))) {
                    if (getData(addon, i).equals("false"))
                        insert.bindString(index(addon), "0"); // cust_id
                    else
                        insert.bindString(index(addon), "1"); // cust_id

                    if (getData(isAdded, i).equals("false"))
                        insert.bindString(index(isAdded), "0"); // cust_id
                    else
                        insert.bindString(index(isAdded), "1"); // cust_id

                    if (getData(isPrinted, i).equals("false"))
                        insert.bindString(index(isPrinted), "0");
                    else
                        insert.bindString(index(isPrinted), "1");

                    // insert.bindString(index(isPrinted), getData(isPrinted,
                    // i)); // cust_id
                    insert.bindString(index(item_void), getData(item_void, i)); // cust_id
                    insert.bindString(index(ordprod_id), getData(ordprod_id, i)); // cust_id
                    insert.bindString(index(ord_id), getData(ord_id, i)); // cust_id
                    insert.bindString(index(prod_id), getData(prod_id, i)); // cust_id
                    insert.bindString(index(prod_sku), getData(prod_sku, i)); // cust_id
                    insert.bindString(index(prod_upc), getData(prod_upc, i)); // cust_id
                    insert.bindString(index(ordprod_qty), getData(ordprod_qty, i)); // cust_id
                    insert.bindString(index(overwrite_price), getData(overwrite_price, i)); // cust_id
                    insert.bindString(index(reason_id), getData(reason_id, i)); // cust_id
                    insert.bindString(index(ordprod_name), getData("prod_name", i)); // cust_id
                    insert.bindString(index(ordprod_desc), getData(ordprod_desc, i)); // cust_id
                    insert.bindString(index(pricelevel_id), getData(pricelevel_id, i)); // cust_id
                    insert.bindString(index(prod_seq), getData(prod_seq, i)); // cust_id
                    insert.bindString(index(uom_name), getData(uom_name, i)); // cust_id
                    insert.bindString(index(uom_conversion), getData(uom_conversion, i)); // cust_id
                    insert.bindString(index(uom_id), getData(uom_id, i));
                    insert.bindString(index(prod_taxId), getData(prod_taxId, i)); // cust_id
                    insert.bindString(index(prod_taxValue), getData(prod_taxValue, i)); // cust_id
                    insert.bindString(index(discount_id), getData(discount_id, i)); // cust_id
                    insert.bindString(index(discount_value), getData(discount_value, i)); // cust_id
                    insert.bindString(index(prod_istaxable), getData(prod_istaxable, i));
                    insert.bindString(index(discount_is_taxable), getData(discount_is_taxable, i));
                    insert.bindString(index(discount_is_fixed), getData(discount_is_fixed, i));
                    insert.bindString(index(onHand), getData(onHand, i));
                    insert.bindString(index(imgURL), getData(imgURL, i));
                    insert.bindString(index(prod_price), getData(prod_price, i));
                    insert.bindString(index(prod_type), getData(prod_type, i));
                    insert.bindString(index(itemTotal), getData(itemTotal, i));
                    insert.bindString(index(itemSubtotal), getData(itemSubtotal, i));
                    insert.bindString(index(addon_section_name), getData(addon_section_name, i));
                    insert.bindString(index(addon_position), getData(addon_position, i));

                    insert.execute();
                    insert.clearBindings();
                }
            }
            insert.close();
            DBManager._db.setTransactionSuccessful();
        } catch (Exception e) {
            StringBuilder sb = new StringBuilder();
            sb.append(e.getMessage())
                    .append(" [com.android.emobilepos.OrderProductsHandler.insertOnHold (at Class.insert)]");

//			Tracker tracker = EasyTracker.getInstance(activity);
//			tracker.send(MapBuilder.createException(sb.toString(), false).build());
        } finally {

            DBManager._db.endTransaction();
        }
    }

    public void deleteOrderProduct(String _ordprod_id) {
        DBManager._db.delete(table_name, "ordprod_id = ?", new String[]{_ordprod_id});
    }

    public void deleteAllOrdProd(String _ord_id) {
        DBManager._db.delete(table_name, "ord_id = ?", new String[]{_ord_id});
    }

    public void emptyTable() {
        StringBuilder sb = new StringBuilder();
        sb.append("DELETE FROM ").append(table_name);
        DBManager._db.execSQL(sb.toString());
    }

    // public void emptyTable() {
    // StringBuilder sb = new StringBuilder();
    // SQLiteDatabase db = dbManager.openWritableDB();
    // sb.append("DELETE FROM ").append(table_name);
    // db.execSQL(sb.toString());
    // db.close();
    // }

    private boolean checkIfExist(String ordID) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT 1 FROM ").append(table_name).append(" WHERE ordprod_id = '");
        sb.append(ordID).append("'");
        Cursor c = DBManager._db.rawQuery(sb.toString(), null);
        boolean exists = (c.getCount() > 0);
        c.close();

        return exists;

    }

    public void updateIsPrinted(String ordprodID) {
        StringBuilder sb = new StringBuilder();
        sb.append(ordprod_id).append(" = ?");

        ContentValues args = new ContentValues();

        args.put(isPrinted, "1");
        DBManager._db.update(table_name, args, sb.toString(), new String[]{ordprodID});
    }

    public Cursor getCursorData(String parameter) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ").append(sb1.toString()).append(",");
        if (myPref.getIsVAT())
            sb.append("itemTotal AS 'totalLineValue' FROM ");
        else
            sb.append("(itemTotal+prod_taxValue) AS 'totalLineValue' FROM ");
        sb.append(table_name).append(" WHERE ord_id = ?");
        // String query =
        // "SELECT prod_id as _id,prod_name,prod_price,prod_desc,prod_onhand
        // FROM Products ORDER BY prod_name COLLATE NOCASE";

		/*
         * String [] fields = new
		 * String[]{prod_id,prod_name,prod_onhand,prod_price,prod_desc};
		 * 
		 * Cursor cursor = db.query(true,table_name,fields, null,null, null,
		 * null, null, null);
		 */
        Cursor cursor = DBManager._db.rawQuery(sb.toString(), new String[]{parameter});
        // int i = cursor.getCount();
        return cursor;
    }

    public List<OrderProduct> getOrderProducts(String orderId) {
        List<OrderProduct> products = new ArrayList<OrderProduct>();
        Cursor cursor = getCursorData(orderId);
        if (cursor.moveToFirst()) {
            do {
                OrderProduct prod = new OrderProduct();

                products.add(prod);
            } while (cursor.moveToNext());
        }
        return products;
    }

    public Cursor getWalletOrdProd(String ordID) {
        StringBuilder sb = new StringBuilder();
        sb.append(
                "SELECT op.*,pi.prod_img_name FROM " + table_name + " op LEFT OUTER JOIN Products_Images pi ON op.prod_id = pi.prod_id ");
        sb.append("AND pi.type = 'I' WHERE ord_id = ?");
        Cursor c = DBManager._db.rawQuery(sb.toString(), new String[]{ordID});
        return c;
    }

    public long getDBSize() {
        // SQLiteDatabase db =
        // SQLiteDatabase.openDatabase(myPref.getDBpath(),Global.dbPass, null,
        // SQLiteDatabase.NO_LOCALIZED_COLLATORS|
        // SQLiteDatabase.OPEN_READWRITE);
        // SQLiteDatabase db = dbManager.openReadableDB();

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT Count(*) FROM ").append(table_name);

        SQLiteStatement stmt = DBManager._db.compileStatement(sb.toString());
        long count = stmt.simpleQueryForLong();
        stmt.close();
        // db.close();
        return count;
    }

    public List<Orders> getPrintOrderedProducts(String ordID) {
        // SQLiteDatabase db =
        // SQLiteDatabase.openDatabase(myPref.getDBpath(),Global.dbPass, null,
        // SQLiteDatabase.NO_LOCALIZED_COLLATORS|
        // SQLiteDatabase.OPEN_READWRITE);
        /// SQLiteDatabase db = dbManager.openReadableDB();

        List<Orders> list = new ArrayList<Orders>();
        StringBuilder sb = new StringBuilder();
        // sb.append("SELECT
        // ordprod_name,ordprod_desc,overwrite_price,(overwrite_price*ordprod_qty)
        // AS 'total', ordprod_qty,addon,isAdded,hasAddons FROM OrderProduct
        // WHERE ord_id = '");
        sb.append(
                "SELECT ordprod_name,ordprod_desc,overwrite_price, CASE WHEN discount_value = '' THEN (overwrite_price*ordprod_qty) ELSE ((overwrite_price*ordprod_qty)-discount_value) END AS 'total', ordprod_qty,addon,isAdded,hasAddons,discount_id,discount_value FROM " + table_name + " WHERE ord_id = '");
        sb.append(ordID).append("'");

        Cursor cursor = DBManager._db.rawQuery(sb.toString(), null);

        Orders[] orders = new Orders[cursor.getCount()];

        if (cursor.moveToFirst()) {
            int i = 0;
            do {
                orders[i] = new Orders();
                orders[i].setName(cursor.getString(cursor.getColumnIndex(ordprod_name)));
                orders[i].setProdDescription(cursor.getString(cursor.getColumnIndex(ordprod_desc)));
                orders[i].setOverwritePrice(format(cursor.getString(cursor.getColumnIndex(overwrite_price))));
                orders[i].setTotal(format(cursor.getString(cursor.getColumnIndex("total"))));
                orders[i].setQty(cursor.getString(cursor.getColumnIndex(ordprod_qty)));
                orders[i].setAddon(cursor.getString(cursor.getColumnIndex(addon)));
                orders[i].setIsAdded(cursor.getString(cursor.getColumnIndex(isAdded)));
                orders[i].setHasAddon(cursor.getString(cursor.getColumnIndex(hasAddons)));
                orders[i].setDiscountID(cursor.getString(cursor.getColumnIndex(discount_id)));
                orders[i].setItemDiscount(cursor.getString(cursor.getColumnIndex(discount_value)));
                list.add(orders[i]);
                i++;
            } while (cursor.moveToNext());
        }
        cursor.close();
        // db.close();

        return list;
    }

    public HashMap<String, List<Orders>> getStationPrinterProducts(String ordID) {
        // SQLiteDatabase db = dbManager.openReadableDB();
        List<Orders> list = new ArrayList<Orders>();
        StringBuilder sb = new StringBuilder();

		/*
         * sb.append(
		 * "SELECT ordprod_id,ordprod_name,ordprod_desc,overwrite_price,(overwrite_price*ordprod_qty) AS 'total', ordprod_qty,addon,isAdded,hasAddons,cat_id FROM OrderProduct WHERE ord_id = '"
		 * ); sb.append(ordID).append("' AND isPrinted = '0'");
		 */

        sb.append(
                "SELECT op.ordprod_id,op.ordprod_name,op.ordprod_desc,op.overwrite_price,(op.overwrite_price*op.ordprod_qty) AS 'total', ");
        sb.append(
                "op.ordprod_qty,op.ordprod_comment,op.addon,op.isAdded,op.hasAddons,op.cat_id,IFNULL(pa.attr_desc,'') as 'attr_desc' FROM " + table_name + " op ");
        sb.append("LEFT OUTER JOIN ProductsAttr pa ON op.prod_id = pa.prod_id WHERE ord_id = '");
        sb.append(ordID).append("' AND isPrinted = '0'");
        Cursor c = DBManager._db.rawQuery(sb.toString(), null);

        Orders[] orders = new Orders[c.getCount()];
        HashMap<String, List<Orders>> tempMap = new HashMap<String, List<Orders>>();
        if (c.moveToFirst()) {
            int i_ordprod_id = c.getColumnIndex(ordprod_id);
            int i_ordprod_name = c.getColumnIndex(ordprod_name);
            int i_ordprod_desc = c.getColumnIndex(ordprod_desc);
            int i_overwrite_price = c.getColumnIndex(overwrite_price);
            int i_total = c.getColumnIndex("total");
            int i_ordprod_qty = c.getColumnIndex(ordprod_qty);
            int i_addon = c.getColumnIndex(addon);
            int i_isAdded = c.getColumnIndex(isAdded);
            int i_hasAddons = c.getColumnIndex(hasAddons);
            int i_cat_id = c.getColumnIndex(cat_id);
            int i_attr_desc = c.getColumnIndex("attr_desc");
            int i_ordprod_comment = c.getColumnIndex(ordprod_comment);
            int i = 0;
            boolean itHasAddons = false;
            String parentCatID = "";
            boolean inAddons = false;
            String tempCatID = "";
            do {
                if (itHasAddons && !c.getString(i_addon).equals("1"))
                    inAddons = false;

                orders[i] = new Orders();
                orders[i].setOrdprodID(c.getString(i_ordprod_id));
                orders[i].setName(c.getString(i_ordprod_name));
                orders[i].setProdDescription(c.getString(i_ordprod_desc));
                orders[i].setOverwritePrice(format(c.getString(i_overwrite_price)));
                orders[i].setTotal(format(c.getString(i_total)));
                orders[i].setQty(c.getString(i_ordprod_qty));
                orders[i].setAddon(c.getString(i_addon));
                orders[i].setIsAdded(c.getString(i_isAdded));
                orders[i].setHasAddon(c.getString(i_hasAddons));
                orders[i].setAttrDesc(c.getString(i_attr_desc));
                orders[i].setOrdProdComment(c.getString(i_ordprod_comment));

                if (tempMap.containsKey(c.getString(i_cat_id)) || (itHasAddons && inAddons)) {

                    if (itHasAddons && inAddons)
                        tempCatID = parentCatID;

                    else
                        tempCatID = c.getString(i_cat_id);

                    list = tempMap.get(tempCatID);
                    list.add(orders[i]);

                    tempMap.put(tempCatID, list);
                } else {
                    list = new ArrayList<Orders>();
                    list.add(orders[i]);
                    tempMap.put(c.getString(i_cat_id), list);
                }

                if (c.getString(i_hasAddons).equals("1") && !inAddons) {
                    parentCatID = c.getString(i_cat_id);
                    itHasAddons = true;
                    inAddons = true;
                } else if (c.getString(i_hasAddons).equals("0") && !inAddons) {
                    itHasAddons = false;
                }

                i++;
            } while (c.moveToNext());
        }
        c.close();
        // db.close();

        return tempMap;
    }

    private String format(String text) {
        DecimalFormat frmt = new DecimalFormat("0.00");

        if (text.isEmpty())
            return "0.00";

        return frmt.format(Double.parseDouble(text));
    }

    public Cursor getOrderProductsOnHold(String ordID) {
        // SQLiteDatabase db = dbManager.openReadableDB();
        StringBuilder sb = new StringBuilder();

        sb.append("SELECT ").append(sb3.toString())
                .append(",CASE WHEN p.prod_taxcode='' THEN '0' ELSE IFNULL(s.taxcode_istaxable,'1')  END AS 'prod_istaxable',p.prod_taxtype FROM ")
                .append(table_name);
        sb.append(
                " op LEFT OUTER JOIN Products p ON op.prod_id = p.prod_id LEFT OUTER JOIN SalesTaxCodes s ON p.prod_taxcode = s.taxcode_id WHERE op.ord_id = '")
                .append(ordID).append("' ");
        sb.append("ORDER BY prod_seq ASC");
        Cursor c = DBManager._db.rawQuery(sb.toString(), null);

        c.moveToFirst();
        // db.close();
        return c;

    }

    public HashMap<String, String> getOrdProdGiftCard(String cardNumber) {
        // SQLiteDatabase db = dbManager.openReadableDB();
        StringBuilder sb = new StringBuilder();
        HashMap<String, String> map = new HashMap<String, String>();
        // String amount = "";
        sb.append("SELECT * FROM ").append(table_name)
                .append(" op LEFT JOIN OrderProductsAttr at ON op.ordprod_id = at.ordprod_id WHERE ");
        sb.append("at.value = ? AND op.cardIsActivated = '0' ORDER BY at.ordprodattr_id DESC LIMIT 1");

        Cursor c = DBManager._db.rawQuery(sb.toString(), new String[]{cardNumber});

        if (c.moveToFirst()) {
            // map.put("overwrite_price",
            // c.getString(c.getColumnIndex("overwrite_price")));
            map.put("overwrite_price", c.getString(c.getColumnIndex("overwrite_price")));
            map.put("ordprod_id", c.getString(c.getColumnIndex("ordprod_id")));
        }
        c.close();
        // db.close();
        return map;
    }

    public void updateOrdProdCardActivated(String ordProdID) {
        // SQLiteDatabase db = dbManager.openWritableDB();
        StringBuilder sb = new StringBuilder();
        sb.append(ordprod_id).append(" = ?");

        ContentValues args = new ContentValues();

        args.put(cardIsActivated, "1");
        DBManager._db.update(table_name, args, sb.toString(), new String[]{ordProdID});
        // db.close();
    }

    public List<OrderProduct> getOrderedProducts(String ordID) {
        List<OrderProduct> list = new ArrayList<OrderProduct>();
        // SQLiteDatabase db = dbManager.openReadableDB();

        String subquery1 = "SELECT ordprod_id as _id, ordprod_name, ordprod_desc, prod_id, prod_sku, prod_upc, ordprod_qty,overwrite_price FROM " + table_name + " WHERE ord_id = '";
        // String subquery2="'";

        Cursor cursor = DBManager._db.rawQuery(subquery1 + ordID + "'", null);
        OrderProduct products;
        if (cursor.moveToFirst()) {
            do {
                products = new OrderProduct();
                String data = cursor.getString(cursor.getColumnIndex(ordprod_name));
                products.ordprod_name = data;

                data = cursor.getString(cursor.getColumnIndex(ordprod_desc));
                products.ordprod_desc = data;

                data = cursor.getString(cursor.getColumnIndex(prod_id));
                products.prod_id = data;

                data = cursor.getString(cursor.getColumnIndex(prod_sku));
                products.prod_sku = data;

                data = cursor.getString(cursor.getColumnIndex(prod_upc));
                products.prod_upc = data;

                data = cursor.getString(cursor.getColumnIndex(ordprod_qty));
                products.ordprod_qty = data;

                data = cursor.getString(cursor.getColumnIndex(overwrite_price));
                products.overwrite_price = data;

                list.add(products);
            } while (cursor.moveToNext());
        }

        cursor.close();
        // db.close();
        return list;
    }

    public List<OrderProduct> getProductsDayReport(boolean isSales, String clerk_id, String date) {
        StringBuilder query = new StringBuilder();
        List<OrderProduct> listOrdProd = new ArrayList<OrderProduct>();

        query.append(
                "SELECT ordprod_name, prod_id,prod_sku, prod_upc, sum(ordprod_qty) as 'ordprod_qty', " +
                        " sum(overwrite_price) 'overwrite_price',date(o.ord_timecreated,'localtime') as 'date' " +
                        "FROM OrderProducts op ");
        query.append("LEFT JOIN Orders o ON op.ord_id = o.ord_id WHERE o.isVoid = '0' AND o.ord_type IN ");

        if (isSales)
            query.append("('2','5') ");
        else// returned items
            query.append("('1') ");

        String[] where_values = null;
        if (clerk_id != null && !clerk_id.isEmpty()) {
            query.append(" AND o.clerk_id = ? ");
            where_values = new String[]{clerk_id};
            if (date != null && !date.isEmpty()) {
                query.append(" AND date = ? ");
                where_values = new String[]{clerk_id, date};
            }
        } else if (date != null && !date.isEmpty()) {
            query.append(" AND date = ? ");
            where_values = new String[]{date};
        }

        query.append(" GROUP BY op.prod_id");

        Cursor c = DBManager._db.rawQuery(query.toString(), where_values);

        if (c.moveToFirst()) {
            int i_ordprod_name = c.getColumnIndex(ordprod_name);
            int i_prod_id = c.getColumnIndex(prod_id);
            int i_prod_sku = c.getColumnIndex(prod_sku);
            int i_prod_upc = c.getColumnIndex(prod_upc);
            int i_ordprod_qty = c.getColumnIndex(ordprod_qty);
            int i_overwrite_price = c.getColumnIndex(overwrite_price);

            do {
                OrderProduct ordProd = new OrderProduct();

                ordProd.ordprod_name = c.getString(i_ordprod_name);
                ordProd.prod_id = c.getString(i_prod_id);
                ordProd.prod_sku = c.getString(i_prod_sku);
                ordProd.prod_upc = c.getString(i_prod_upc);
                ordProd.ordprod_qty = c.getString(i_ordprod_qty);
                ordProd.overwrite_price = c.getString(i_overwrite_price);

                listOrdProd.add(ordProd);
            } while (c.moveToNext());
        }

        c.close();
        return listOrdProd;
    }

    public List<OrderProduct> getDepartmentDayReport(boolean isSales, String clerk_id, String date) {
        StringBuilder query = new StringBuilder();
        List<OrderProduct> listOrdProd = new ArrayList<OrderProduct>();

        query.append(
                "SELECT c.cat_name,op.cat_id, sum(ordprod_qty) as 'ordprod_qty',  sum(overwrite_price) 'overwrite_price'," +
                        "date(o.ord_timecreated,'localtime') as 'date'  " +
                        "FROM OrderProducts op ");
        query.append(
                "LEFT JOIN Categories c ON op.cat_id = c.cat_id " +
                        "LEFT JOIN Orders o ON op.ord_id = o.ord_id " +
                        "WHERE o.isVoid = '0' AND o.ord_type IN ");

        if (isSales)
            query.append("('2','5') ");
        else// returned items
            query.append("('1') ");

        String[] where_values = null;
        if (clerk_id != null && !clerk_id.isEmpty()) {
            query.append(" AND o.clerk_id = ? ");
            where_values = new String[]{clerk_id};
            if (date != null && !date.isEmpty()) {
                query.append(" AND date = ? ");
                where_values = new String[]{clerk_id, date};
            }
        } else if (date != null && !date.isEmpty()) {
            query.append(" AND date = ? ");
            where_values = new String[]{date};
        }

        query.append(" GROUP BY op.cat_id");

        Cursor c = DBManager._db.rawQuery(query.toString(), where_values);

        if (c.moveToFirst()) {
            int i_cat_name = c.getColumnIndex("cat_name");
            int i_cat_id = c.getColumnIndex(cat_id);
            int i_ordprod_qty = c.getColumnIndex(ordprod_qty);
            int i_overwrite_price = c.getColumnIndex(overwrite_price);

            do {
                OrderProduct ordProd = new OrderProduct();

                ordProd.cat_name = c.getString(i_cat_name);
                ordProd.cat_id = c.getString(i_cat_id);
                ordProd.ordprod_qty = c.getString(i_ordprod_qty);
                ordProd.overwrite_price = c.getString(i_overwrite_price);

                listOrdProd.add(ordProd);
            } while (c.moveToNext());
        }

        c.close();
        return listOrdProd;
    }
}