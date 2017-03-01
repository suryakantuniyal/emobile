package com.android.emobilepos.ordering;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.AsyncTaskLoader;
import android.text.TextUtils;

import com.android.database.ProductsHandler;

public class Catalog_Loader extends AsyncTaskLoader<Cursor> {
    private Context context;
    private int limit;
    private int offset;
    private String searchText;
    private String searchType;

    public Catalog_Loader(Context context, int limit, int offset) {
        super(context);
        this.context = context;
        this.limit = limit;
        this.offset = offset;
    }

    @Override
    public Cursor loadInBackground() {
        Cursor cursor;

        ProductsHandler productsHandler = new ProductsHandler(context);

        if (!TextUtils.isEmpty(Catalog_FR.search_text) && !TextUtils.isEmpty(Catalog_FR.search_type)) {
            cursor = productsHandler.searchProducts(Catalog_FR.search_text, Catalog_FR.search_type);
        } else {
            cursor = productsHandler.getCatalogData(limit, offset);
        }

        return cursor;

//        switch (Catalog_FR._typeCase) {
////            case Catalog_FR.CASE_CATEGORY:
////                CategoriesHandler categoriesHandler = new CategoriesHandler(activity);
////                c = categoriesHandler.getCategoriesCursor();
////                break;
////            case Catalog_FR.CASE_SUBCATEGORY:
////                CategoriesHandler catHandler = new CategoriesHandler(activity);
////                c = catHandler.getSubcategoriesCursor(Global.cat_id);
////                break;
//            case Catalog_FR.CASE_PRODUCTS:
//                ProductsHandler handler = new ProductsHandler(context);
//                c = handler.getCatalogData(limit, offset);
//                break;
//            case Catalog_FR.CASE_SEARCH_PROD:
//                ProductsHandler prodHandler = new ProductsHandler(context);
//                c = prodHandler.searchProducts(Catalog_FR.search_text, Catalog_FR.search_type);
//                break;
//        }
//        return c;
    }


}
