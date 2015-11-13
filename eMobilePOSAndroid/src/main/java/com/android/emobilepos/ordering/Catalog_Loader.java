package com.android.emobilepos.ordering;

import android.app.Activity;
import android.database.Cursor;
import android.support.v4.content.AsyncTaskLoader;

import com.android.database.CategoriesHandler;
import com.android.database.ProductsHandler;
import com.android.support.Global;

public class Catalog_Loader extends AsyncTaskLoader<Cursor>
{
	private Activity activity;

	public Catalog_Loader(Activity activity) {
		super(activity);
		this.activity = activity;
	}
	
	@Override
	public Cursor loadInBackground()
	{
		Cursor c = null;
		
		switch(Catalog_FR._typeCase)
		{
		case Catalog_FR.CASE_CATEGORY:
			CategoriesHandler categoriesHandler = new CategoriesHandler(activity);
			c = categoriesHandler.getCategoriesCursor();
			break;
		case Catalog_FR.CASE_SUBCATEGORY:
			CategoriesHandler catHandler = new CategoriesHandler(activity);
			c = catHandler.getSubcategoriesCursor(Global.cat_id);
			break;
		case Catalog_FR.CASE_PRODUCTS:
			ProductsHandler handler = new ProductsHandler(activity);
			c = handler.getCatalogData();
			break;
		case Catalog_FR.CASE_SEARCH_PROD:
			ProductsHandler prodHandler = new ProductsHandler(activity);
			c = prodHandler.searchProducts(Catalog_FR.search_text, Catalog_FR.search_type);
			break;
		}
		return c;
	}
	

}
