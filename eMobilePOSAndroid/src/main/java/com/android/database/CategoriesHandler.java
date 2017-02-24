package com.android.database;

import android.content.Context;
import android.database.Cursor;

import com.android.support.Global;
import com.android.support.MyPreferences;

import net.sqlcipher.database.SQLiteStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class CategoriesHandler {

    private static final String cat_id = "cat_id";
    private static final String cat_name = "cat_name";
    private static final String cat_update = "cat_update";
    private static final String isactive = "isactive";
    private static final String parentID = "parentID";
    private static final String url_icon = "url_icon";

    private final List<String> attr = Arrays.asList(cat_id, cat_name, cat_update, isactive, parentID, url_icon);
    private StringBuilder sb1, sb2;
    private HashMap<String, Integer> attrHash;

    private List<String[]> catData;
    private MyPreferences myPref;
    private List<HashMap<String, Integer>> dictionaryListMap;
    private static final String table_name = "Categories";

	public CategoriesHandler(Context activity) {
		attrHash = new HashMap<>();
		catData = new ArrayList<>();
		sb1 = new StringBuilder();
		sb2 = new StringBuilder();
		myPref = new MyPreferences(activity);
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

    private String getData(String tag, int record) {
        Integer i = dictionaryListMap.get(record).get(tag);
        if (i != null) {
            return catData.get(record)[i];
        }
        return "";
    }


    private int index(String tag) {
        return attrHash.get(tag);
    }


    public void insert(List<String[]> data, List<HashMap<String, Integer>> dictionary) {
        DBManager.getDatabase().beginTransaction();
        try {
            catData = data;
            dictionaryListMap = dictionary;
            SQLiteStatement insert;
            insert = DBManager.getDatabase().compileStatement("INSERT INTO " + table_name + " (" + sb1.toString() + ") " + "VALUES (" + sb2.toString() + ")");
            int size = catData.size();
            for (int j = 0; j < size; j++) {
                insert.bindString(index(cat_id), getData(cat_id, j));
                insert.bindString(index(cat_name), getData(cat_name, j));
                insert.bindString(index(cat_update), getData(cat_update, j));
                insert.bindString(index(isactive), getData(isactive, j));
                insert.bindString(index(parentID), getData(parentID, j));
                insert.bindString(index(url_icon), getData(url_icon, j));
                insert.execute();
                insert.clearBindings();
            }
            insert.close();
            DBManager.getDatabase().setTransactionSuccessful();
        } catch (Exception e) {
        } finally {
            DBManager.getDatabase().endTransaction();
        }
    }


    public void emptyTable() {
        DBManager.getDatabase().execSQL("DELETE FROM " + table_name);
    }


    public List<String[]> getSubcategories(String name) {
        List<String[]> list = new ArrayList<>();
        String[] data;
        String[] fields = new String[]{cat_name, cat_id};
        Cursor cursor, cursor2;
        StringBuilder sb = new StringBuilder();
        cursor = DBManager.getDatabase().query(true, table_name, fields, "parentID=?", new String[]{name}, null, null, cat_name, null);
        if (!myPref.getPreferences(MyPreferences.pref_enable_multi_category)) {
            data = new String[2];
        } else {
            data = new String[3];
        }

        if (cursor.moveToFirst()) {
            do {
                data[0] = cursor.getString(cursor.getColumnIndex(cat_name));
                data[1] = cursor.getString(cursor.getColumnIndex(cat_id));
                list.add(data);
                if (!myPref.getPreferences(MyPreferences.pref_enable_multi_category))
                    data = new String[2];
                else {
                    sb.append("SELECT Count(*) AS count FROM Categories WHERE parentID='").append(data[1]).append("'");
                    cursor2 = DBManager.getDatabase().rawQuery(sb.toString(), null);
                    cursor2.moveToFirst();
                    data[2] = cursor2.getString(cursor2.getColumnIndex("count"));
                    data = new String[3];
                    sb.setLength(0);
                    cursor2.close();
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public String[] getCategory(String categoryId) {
        String[] data = new String[2];
        String[] fields = new String[]{cat_name, cat_id};
        Cursor cursor = DBManager.getDatabase().query(true, table_name, fields, null, null, null, null, cat_name, null);
        if (cursor.moveToFirst()) {
            data[0] = cursor.getString(cursor.getColumnIndex(cat_name));
            data[1] = cursor.getString(cursor.getColumnIndex(cat_id));
        }
        cursor.close();
        return data;
    }

    public List<String[]> getCategories() {
        List<String[]> list = new ArrayList<>();
        String[] data;
        String[] fields = new String[]{cat_name, cat_id};
        Cursor cursor, cursor2;
        StringBuilder sb = new StringBuilder();
        if (myPref.getPreferences(MyPreferences.pref_enable_multi_category))
            cursor = DBManager.getDatabase().query(true, table_name, fields, "parentID='' AND cat_id!=''", null, null, null, cat_name, null);
        else
            cursor = DBManager.getDatabase().query(true, table_name, fields, null, null, null, null, cat_name, null);
        if (!myPref.getPreferences(MyPreferences.pref_enable_multi_category)) {
            data = new String[2];
        } else {
            data = new String[3];
        }
        if (cursor.moveToFirst()) {
            do {
                data[0] = cursor.getString(cursor.getColumnIndex(cat_name));
                data[1] = cursor.getString(cursor.getColumnIndex(cat_id));
                list.add(data);
                if (!myPref.getPreferences(MyPreferences.pref_enable_multi_category))
                    data = new String[2];
                else {
                    sb.append("SELECT Count(*) AS count FROM Categories WHERE parentID='").append(data[1]).append("'");
                    cursor2 = DBManager.getDatabase().rawQuery(sb.toString(), null);
                    cursor2.moveToFirst();
                    data[2] = cursor2.getString(cursor2.getColumnIndex("count"));
                    data = new String[3];
                    sb.setLength(0);
                    cursor2.close();
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }


    public Cursor getCategoriesCursor() {
        StringBuilder sb = new StringBuilder();

        sb.append("SELECT cat_id as '_id',cat_name,url_icon,(SELECT Count(*)  " +
                "FROM Categories c2 WHERE c2.parentID = c1.cat_id) AS num_subcategories " +
                "FROM Categories c1 ");
        if (!Global.cat_id.equals("0"))
            sb.append("  WHERE  c1.parentID='' AND c1.cat_id ='").append(Global.cat_id).append("' ");
        else if (myPref.getPreferences(MyPreferences.pref_enable_multi_category))
            sb.append("  WHERE c1.parentID='' AND c1.cat_id !='' ");
        else {
            sb.append("  WHERE c1.parentID='' ");
        }
        sb.append(" ORDER BY c1.cat_name");
        Cursor cursor = DBManager.getDatabase().rawQuery(sb.toString(), null);
        cursor.moveToFirst();
        return cursor;
    }

    public Cursor getSubcategoriesCursor(String name) {
        String sb = "SELECT cat_id as '_id',cat_name,url_icon,(SELECT Count(*)  FROM Categories c2 WHERE c2.parentID = c1.cat_id) AS num_subcategories FROM Categories c1 " +
                "  WHERE c1.parentID=? ORDER BY c1.cat_name";
        Cursor cursor = DBManager.getDatabase().rawQuery(sb, new String[]{name});
        cursor.moveToFirst();
        return cursor;
    }

    public List<EMSCategory> getMainCategories() {
        List<EMSCategory> categories = new ArrayList<>();
        Cursor cursor = getCategoriesCursor();

        int categoryIdIndex = cursor.getColumnIndex("_id");
        int categoryNameIndex = cursor.getColumnIndex("cat_name");
        int iconUrlIndex = cursor.getColumnIndex("url_icon");
        int numberOfSubCategoriesIndex = cursor.getColumnIndex("num_subcategories");

        String categoryId;
        String categoryName;
        String iconUrl;
        int numberOfSubCategories = 0;

        if (!cursor.isAfterLast()) {
            do {
                categoryId = cursor.getString(categoryIdIndex);
                categoryName = cursor.getString(categoryNameIndex);
                iconUrl = cursor.getString(iconUrlIndex);
                numberOfSubCategories = cursor.getInt(numberOfSubCategoriesIndex);

                categories.add(new EMSCategory(categoryId, categoryName, iconUrl, numberOfSubCategories));
            } while (cursor.moveToNext());
        }

        return categories;
    }

    public class EMSCategory {
        private String categoryId;
        private String categoryName;
        private String iconUrl;
        private int numberOfSubCategories;

        public String getCategoryId() {
            return categoryId;
        }

        public void setCategoryId(String categoryId) {
            this.categoryId = categoryId;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public void setCategoryName(String categoryName) {
            this.categoryName = categoryName;
        }

        public String getIconUrl() {
            return iconUrl;
        }

        public void setIconUrl(String iconUrl) {
            this.iconUrl = iconUrl;
        }

        public int getNumberOfSubCategories() {
            return numberOfSubCategories;
        }

        public void setNumberOfSubCategories(int numberOfSubCategories) {
            this.numberOfSubCategories = numberOfSubCategories;
        }

        private EMSCategory(String categoryId, String categoryName, String iconUrl, int numberOfSubCategories) {
            this.categoryId = categoryId;
            this.categoryName = categoryName;
            this.iconUrl = iconUrl;
            this.numberOfSubCategories = numberOfSubCategories;
        }
    }
}