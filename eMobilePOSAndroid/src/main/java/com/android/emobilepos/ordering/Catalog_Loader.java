package com.android.emobilepos.ordering;

import android.content.ContentResolver;
import android.content.Context;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;
import android.text.TextUtils;

import com.android.database.ProductsHandler;
import com.android.emobilepos.models.EMSCategory;

public class Catalog_Loader extends AsyncTaskLoader<Cursor> {
    private Context context;
    private int limit;
    private int offset;
    private EMSCategory category;
    private String searchText;
    private String searchType;
    private boolean onRestaurantMode;

    public Catalog_Loader(Context context, int limit, int offset, EMSCategory category, String searchText, String searchType, boolean onRestaurantMode) {
        super(context);
        this.context = context;
        this.limit = limit;
        this.offset = offset;
        this.category = category;
        this.searchText = searchText;
        this.searchType = searchType;
        this.onRestaurantMode = onRestaurantMode;
    }

    @Override
    public Cursor loadInBackground() {
        // Quick bail if on restaurant mode and no parameters were supplied
        if (onRestaurantMode && category == null && TextUtils.isEmpty(searchText)) {
            return getEmptyCursor();
        }
        if (offset == 0 && context instanceof OrderingMain_FA && ((OrderingMain_FA) context).getRightFragment() != null) {
            ((OrderingMain_FA) context).getRightFragment().closeCursor();
        }

        Cursor cursor;

        ProductsHandler productsHandler = new ProductsHandler(context);
        if (!TextUtils.isEmpty(searchText) && !TextUtils.isEmpty(searchType)) {
            cursor = productsHandler.searchProducts(searchText, searchType);
        } else {
            String categoryId = null;
            if (category != null) {
                categoryId = category.getCategoryId();
            }
            cursor = productsHandler.getCatalogData(categoryId, limit, offset);
        }
        return cursor;
    }

    private Cursor getEmptyCursor() {
        return new Cursor() {
            @Override
            public int getCount() {
                return 0;
            }

            @Override
            public int getPosition() {
                return 0;
            }

            @Override
            public boolean move(int offset) {
                return false;
            }

            @Override
            public boolean moveToPosition(int position) {
                return false;
            }

            @Override
            public boolean moveToFirst() {
                return false;
            }

            @Override
            public boolean moveToLast() {
                return false;
            }

            @Override
            public boolean moveToNext() {
                return false;
            }

            @Override
            public boolean moveToPrevious() {
                return false;
            }

            @Override
            public boolean isFirst() {
                return false;
            }

            @Override
            public boolean isLast() {
                return false;
            }

            @Override
            public boolean isBeforeFirst() {
                return false;
            }

            @Override
            public boolean isAfterLast() {
                return false;
            }

            @Override
            public int getColumnIndex(String columnName) {
                return 0;
            }

            @Override
            public int getColumnIndexOrThrow(String columnName) throws IllegalArgumentException {
                return 0;
            }

            @Override
            public String getColumnName(int columnIndex) {
                return null;
            }

            @Override
            public String[] getColumnNames() {
                return new String[0];
            }

            @Override
            public int getColumnCount() {
                return 0;
            }

            @Override
            public byte[] getBlob(int columnIndex) {
                return new byte[0];
            }

            @Override
            public String getString(int columnIndex) {
                return null;
            }

            @Override
            public void copyStringToBuffer(int columnIndex, CharArrayBuffer buffer) {

            }

            @Override
            public short getShort(int columnIndex) {
                return 0;
            }

            @Override
            public int getInt(int columnIndex) {
                return 0;
            }

            @Override
            public long getLong(int columnIndex) {
                return 0;
            }

            @Override
            public float getFloat(int columnIndex) {
                return 0;
            }

            @Override
            public double getDouble(int columnIndex) {
                return 0;
            }

            @Override
            public int getType(int columnIndex) {
                return 0;
            }

            @Override
            public boolean isNull(int columnIndex) {
                return false;
            }

            @Override
            public void deactivate() {

            }

            @Override
            public boolean requery() {
                return false;
            }

            @Override
            public void close() {

            }

            @Override
            public boolean isClosed() {
                return false;
            }

            @Override
            public void registerContentObserver(ContentObserver observer) {

            }

            @Override
            public void unregisterContentObserver(ContentObserver observer) {

            }

            @Override
            public void registerDataSetObserver(DataSetObserver observer) {

            }

            @Override
            public void unregisterDataSetObserver(DataSetObserver observer) {

            }

            @Override
            public void setNotificationUri(ContentResolver cr, Uri uri) {

            }

            @Override
            public Uri getNotificationUri() {
                return null;
            }

            @Override
            public boolean getWantsAllOnMoveCalls() {
                return false;
            }

            @Override
            public void setExtras(Bundle extras) {

            }

            @Override
            public Bundle getExtras() {
                return null;
            }

            @Override
            public Bundle respond(Bundle extras) {
                return null;
            }
        };
    }
}
