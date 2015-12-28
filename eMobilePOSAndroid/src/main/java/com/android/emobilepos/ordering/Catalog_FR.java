package com.android.emobilepos.ordering;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.database.CategoriesHandler;
import com.android.database.ProductAddonsHandler;
import com.android.database.VolumePricesHandler;
import com.android.emobilepos.R;
import com.android.support.DBManager;
import com.android.support.Global;
import com.android.support.MyEditText;
import com.android.support.MyPreferences;
import com.android.support.TerminalDisplay;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.zzzapi.uart.uart;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import drivers.EMSPAT100;

public class Catalog_FR extends Fragment implements OnItemClickListener, OnClickListener, LoaderCallbacks<Cursor>,
        MenuCatGV_Adapter.ItemClickedCallback, MenuProdGV_Adapter.ProductClickedCallback {


    public static Catalog_FR instance;

    private Activity activity;
    private static final int THE_LOADER = 0x01;
    private AbsListView catalogList;
    //private SQLiteDatabase db;
    private DBManager dbManager;
    public static int _typeCase = -1;
    public static String search_text = "", search_type = "";
    public static final int CASE_CATEGORY = 1, CASE_SUBCATEGORY = 2, CASE_PRODUCTS = 0, CASE_SEARCH_PROD = 3;
    private ImageLoader imageLoader;

    private Cursor myCursor;
    public static List<String> btnListID = new ArrayList<String>();
    public static List<String> btnListName = new ArrayList<String>();

    private LinearLayout catButLayout;
    private MyPreferences myPref;
    private Global global;
    private boolean onRestaurantMode = false;
    private boolean restModeViewingProducts = false;


    private VolumePricesHandler volPriceHandler;
    private MenuCatGV_Adapter categoryListAdapter;
    private MenuProdGV_Adapter prodListAdapter;
    private RefreshReceiptViewCallback callBackRefreshView;
    private MyEditText searchField;


    private ListViewAdapter tempLVAdapter;
    private Dialog dialog;
    private boolean isSubcategory = false;
    private String[] searchFilters;
    private List<String> spinnerCatName = new ArrayList<String>(), catName = new ArrayList<String>();
    private List<String> spinnerCatID = new ArrayList<String>(), catIDs = new ArrayList<String>();
    private List<String[]> spinnerCategories = new ArrayList<String[]>(), categories = new ArrayList<String[]>();
    private CategoriesHandler catHandler;
    private boolean catalogIsPortrait = false;
    private boolean isFastScanning = false;
    private long lastClickTime = 0;


    public interface RefreshReceiptViewCallback {
        public void refreshView();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.order_catalog_layout, container, false);
        activity = getActivity();
        instance = this;
        catButLayout = (LinearLayout) view.findViewById(R.id.categoriesButtonLayoutHolder);
        searchField = (MyEditText) view.findViewById(R.id.catalogSearchField);
        searchField.setIsForSearching(activity, OrderingMain_FA.invisibleSearchMain);
       // searchField.setText("58187869354"); //test upc
        catalogList = (AbsListView) view.findViewById(R.id.catalogListview);
        catalogList.setOnItemClickListener(this);
        catalogIsPortrait = Global.isPortrait(activity);
        callBackRefreshView = (RefreshReceiptViewCallback) activity;
        dbManager = new DBManager(getActivity());
        //db = dbManager.openReadableDB();
        volPriceHandler = new VolumePricesHandler(activity);
        myPref = new MyPreferences(activity);
        imageLoader = ImageLoader.getInstance();
        File cacheDir = new File(myPref.getCacheDir());
        if (!cacheDir.exists())
            cacheDir.mkdirs();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(activity).memoryCacheExtraOptions(100, 100)
                .discCacheExtraOptions(1000, 1000, CompressFormat.JPEG, 100, null).discCache(new UnlimitedDiscCache(cacheDir)).build();
        imageLoader.init(config);
        imageLoader.handleSlowNetwork(true);
        isFastScanning = myPref.getPreferences(MyPreferences.pref_fast_scanning_mode);

        global = (Global) activity.getApplication();


        if (Global.cat_id == "0")
            Global.cat_id = myPref.getPreferencesValue(MyPreferences.pref_default_category);

        if (myPref.getPreferences(MyPreferences.pref_restaurant_mode)) {
            if (_typeCase == -1)
                _typeCase = CASE_CATEGORY;
            else if (_typeCase == CASE_PRODUCTS || _typeCase == CASE_SEARCH_PROD)
                restModeViewingProducts = true;

            onRestaurantMode = true;
        } else {
            if (_typeCase == -1)
                _typeCase = CASE_PRODUCTS;
        }



        setupSpinners(view);
        setupCategoriesButtons();

        setupSearchField();
        loadCursor();
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }


    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.categoryButton:
                categories = new ArrayList<String[]>(spinnerCategories);
                catName = new ArrayList<String>(spinnerCatName);
                catIDs = new ArrayList<String>(spinnerCatID);
                isSubcategory = false;
                setupCategoryView();
                dialog.show();
                break;
        }
    }


    private void setupSearchField() {
        searchField.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_ACTION_SEARCH);
        searchField.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // TODO Auto-generated method stub
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String text = v.getText().toString().trim();
                    if (!text.isEmpty()) {
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(searchField.getWindowToken(), 0);

                        performSearch(text);
                    }
                    return true;
                }
                return false;
            }
        });

        searchField.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable arg0) {
                // TODO Auto-generated method stub
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onTextChanged(CharSequence s, int arg1, int arg2, int arg3) {
                // TODO Auto-generated method stub
                String test = s.toString().trim();
                if (test.isEmpty() && _typeCase == CASE_SEARCH_PROD) {
                    if (onRestaurantMode) {
                        restModeViewingProducts = false;
                        _typeCase = CASE_CATEGORY;
                    } else
                        _typeCase = CASE_PRODUCTS;
                    loadCursor();

                }
            }
        });
        if (_typeCase == CASE_SEARCH_PROD)
            performSearch(search_text);
    }


    private void setupSpinners(View v) {

        Button btnCategory = (Button) v.findViewById(R.id.categoryButton);
        if (onRestaurantMode)
            btnCategory.setVisibility(View.INVISIBLE);
        else
            btnCategory.setOnClickListener(this);


        catHandler = new CategoriesHandler(activity);
        Spinner spinnerFilter = (Spinner) v.findViewById(R.id.filterButton);

        Resources resources = activity.getResources();
        searchFilters = new String[]{resources.getString(R.string.catalog_name), resources.getString(R.string.catalog_description),
                resources.getString(R.string.catalog_type), resources.getString(R.string.catalog_upc), resources.getString(R.string.catalog_sku)};


        spinnerCategories = catHandler.getCategories();
        int size = spinnerCategories.size();
        spinnerCatName.add("All");
        spinnerCatID.add("0");
        for (int i = 0; i < size; i++) {
            spinnerCatName.add(spinnerCategories.get(i)[0]);
            spinnerCatID.add(spinnerCategories.get(i)[1]);
        }


        CustomSpinnerAdapter filterAdapter = new CustomSpinnerAdapter(getActivity(), android.R.layout.simple_spinner_item, searchFilters);
        spinnerFilter.setAdapter(filterAdapter);

        spinnerFilter.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // TODO Auto-generated method stub
                global.searchType = position;

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });

    }


    private void setupCategoryView() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            tempLVAdapter.notifyDataSetChanged();
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final ListView list = new ListView(activity);
        tempLVAdapter = new ListViewAdapter(activity);
        list.setCacheColorHint(Color.TRANSPARENT);
        list.setAdapter(tempLVAdapter);
        builder.setView(list);


        list.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                _typeCase = CASE_PRODUCTS;
                if (position == 0 && !isSubcategory) {
                    Global.cat_id = "0";

                } else {

                    int index = position;
                    if (!isSubcategory)
                        index -= 1;
                    Global.cat_id = categories.get(index)[1];
                    if (myPref.getPreferences(MyPreferences.pref_enable_multi_category) && !categories.get(index)[2].equals("0")) // it has sub-category
                    {
                        global.hasSubcategory = true;
                        //_typeCase = CASE_CATEGORY;

                    } else
                        global.hasSubcategory = false;
                    global.isSubcategory = false;
                }
                dialog.dismiss();
                loadCursor();
            }
        });

        dialog = builder.create();


    }

    private void setupCategoriesButtons() {
        if (!onRestaurantMode)
            catButLayout.setVisibility(View.GONE);
        else {
            Button but = (Button) catButLayout.findViewById(R.id.buttonAllCategories);
            but.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    int size = btnListID.size();
                    if (size > 0) {
                        for (int i = 0; i < size; i++)
                            removeCategoryButton(btnListID.get(i));
                        btnListID.clear();
                        btnListName.clear();


                    }
                    _typeCase = CASE_CATEGORY;
                    Global.cat_id = "0";
                    restModeViewingProducts = false;
                    loadCursor();
                }
            });

            int size = btnListID.size();
            for (int i = 0; i < size; i++) {
                addCategoryButton(btnListName.get(i), btnListID.get(i));
            }
        }
    }


    public void loadCursor() {
        //OrderingMain_FA.invisibleSearchMain.requestFocus();
        getLoaderManager().initLoader(THE_LOADER, null, this).forceLoad();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        // TODO Auto-generated method stub
        Catalog_Loader loader = new Catalog_Loader(activity);
        return loader;
    }


    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor c) {
        // TODO Auto-generated method stub

        myCursor = c;
        if (_typeCase != CASE_PRODUCTS && _typeCase != CASE_SEARCH_PROD) {
            categoryListAdapter = new MenuCatGV_Adapter(this, activity, c, CursorAdapter.NO_SELECTION, imageLoader);
            catalogList.setAdapter(categoryListAdapter);
            if (myPref.getPreferences(MyPreferences.pref_restaurant_mode) && myCursor.getCount() == 1 && _typeCase == CASE_CATEGORY)
                itemClicked(false);
        } else {
            prodListAdapter = new MenuProdGV_Adapter(this, activity, c, CursorAdapter.NO_SELECTION, imageLoader);
            catalogList.setAdapter(prodListAdapter);
        }

    }

    public int measureCellWidth(Context context, View cell) {
        // We need a fake parent
        FrameLayout buffer = new FrameLayout(context);
        android.widget.AbsListView.LayoutParams layoutParams = new android.widget.AbsListView.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        buffer.addView(cell, layoutParams);

        cell.forceLayout();
        cell.measure(1000, 1000);

        int width = cell.getMeasuredWidth();

        buffer.removeAllViews();

        return width;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        // TODO Auto-generated method stub
        catalogList.setAdapter(null);
    }

    @Override
    public void productClicked(int position) {
        // TODO Auto-generated method stub
        myCursor.moveToPosition(position);
        itemClicked(false);
    }

    @Override
    public void itemClicked(int position, boolean showAllProducts) {
        // TODO Auto-generated method stub

        myCursor.moveToPosition(position);
        itemClicked(showAllProducts);
    }


    public void searchUPC(String upc) {
        search_text = upc;
        _typeCase = CASE_SEARCH_PROD;
        search_type = "prod_upc";

        loadCursor();
        restModeViewingProducts = true;
    }


    public void performSearch(String text) {


        search_text = text;
        _typeCase = CASE_SEARCH_PROD;

        switch (global.searchType) {
            case 0: // search by Name
            {
                search_type = "prod_name";
                break;
            }
            case 1: // search by Description
            {
                search_type = "prod_desc";
                break;
            }
            case 2: // search by Type
            {
                search_type = "prod_type";
                break;
            }
            case 3: // search by UPC
            {
                search_type = "prod_upc";
                break;
            }
            case 4:
                search_type = "prod_sku";
                break;
        }

        loadCursor();
        restModeViewingProducts = true;
        OrderingMain_FA.invisibleSearchMain.requestFocus();
    }


    private void getCategoryCursor(int i_id, int i_cat_name, int i_num_subcategories, boolean showAllProducts) {
        restModeViewingProducts = false;
        String catID = myCursor.getString(myCursor.getColumnIndex("_id"));
        boolean found_category_name = myCursor.getColumnIndex("cat_name") == -1 ? false : true;
        if (found_category_name) {
            String catName = myCursor.getString(myCursor.getColumnIndex("cat_name"));
            //Global.cat_id = new String(_catID);
            Global.cat_id = catID;

            if (!myPref.getPreferences(MyPreferences.pref_enable_multi_category)) {
                restModeViewingProducts = true;
                btnListID.add(new String(catID));
                btnListName.add(new String(catName));
                addCategoryButton(catName, new String(catID));

                _typeCase = CASE_PRODUCTS;
                loadCursor();
            } else {


                int num_subcategories = Integer.parseInt(myCursor.getString(myCursor.getColumnIndex("num_subcategories")));
                if (num_subcategories > 0 && !showAllProducts) {

                    btnListID.add(new String(catID));
                    btnListName.add(new String(catName));
                    addCategoryButton(catName, new String(catID));
                    _typeCase = CASE_SUBCATEGORY;
                    loadCursor();
                } else {
                    restModeViewingProducts = true;
                    btnListID.add(new String(catID));
                    btnListName.add(new String(catName));
                    addCategoryButton(catName, new String(catID));

                    _typeCase = CASE_PRODUCTS;
                    loadCursor();
                }
            }
        }
    }

    private void addCategoryButton(String categoryName, String cat_id) {
        Button btn = new Button(activity);
        btn.setTag(cat_id);
        btn.setText(categoryName);
        //btn.setPadding(18, 0, 18, 0);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        catButLayout.addView(btn, params);
        btn.setTextAppearance(activity, R.style.black_text_appearance);
        btn.setPadding(5, 0, 5, 0);
        btn.setTextSize(TypedValue.COMPLEX_UNIT_PX, activity.getResources().getDimension(R.dimen.ordering_checkout_btn_txt_size));
        //btn.setEms(6);
        btn.setBackgroundResource(R.drawable.blue_btn_selector);
        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                int size1 = btnListID.size();
                int temp = btnListID.indexOf((String) v.getTag());
                List<String> tempList = new ArrayList<String>(btnListID);
                for (int i = temp + 1; i < size1; i++) {
                    removeCategoryButton(tempList.get(i));
                    btnListName.remove(btnListID.indexOf(tempList.get(i)));
                    btnListID.remove(btnListID.indexOf(tempList.get(i)));

                }

                int size2 = btnListID.size();
                if (size2 < size1) {
                    Global.cat_id = btnListID.get(size2 - 1);
                    _typeCase = CASE_SUBCATEGORY;
                    loadCursor();

                }
            }
        });
    }

    private void removeCategoryButton(String cat_id) {
        Button temp = (Button) catButLayout.findViewWithTag(cat_id);
        if (temp != null) {
            restModeViewingProducts = false;
            catButLayout.removeView(temp);
        }
    }

    public void automaticAddOrder(String[] data) {


        global.automaticAddOrder(getActivity(), false, global, data);
        refreshListView();
        callBackRefreshView.refreshView();
    }


    public String[] populateDataForIntent(Cursor c) {
        String[] data = new String[13];
        data[0] = c.getString(myCursor.getColumnIndex("_id"));

        String val = myPref.getPreferencesValue(MyPreferences.pref_attribute_to_display);

        if (val.equals("prod_desc"))
            data[1] = c.getString(c.getColumnIndex("prod_desc"));
        else if (val.equals("prod_name"))
            data[1] = c.getString(c.getColumnIndex("prod_name"));
        else
            data[1] = c.getString(c.getColumnIndex("prod_extradesc"));


        String tempPrice = c.getString(c.getColumnIndex("volume_price"));
        if (tempPrice == null || tempPrice.isEmpty()) {
            tempPrice = c.getString(c.getColumnIndex("pricelevel_price"));
            if (tempPrice == null || tempPrice.isEmpty()) {
                tempPrice = c.getString(c.getColumnIndex("chain_price"));

                if (tempPrice == null || tempPrice.isEmpty())
                    tempPrice = c.getString(c.getColumnIndex("master_price"));
            }
        } else if (global.qtyCounter.containsKey(data[0])) {
            BigDecimal origQty = new BigDecimal(global.qtyCounter.get(data[0]));
            BigDecimal newQty = origQty.add(Global.getBigDecimalNum("1"));
            //String [] temp = volPriceHandler.getVolumePrice(global.qtyCounter.get(data[0]),data[0]);
            String[] temp = volPriceHandler.getVolumePrice(newQty.toString(), data[0]);
            if (temp[1] != null && !temp[1].isEmpty())
                tempPrice = temp[1];
        }

        data[2] = tempPrice;
        data[3] = c.getString(c.getColumnIndex("prod_desc"));

        tempPrice = new String();
        tempPrice = c.getString(c.getColumnIndex("local_prod_onhand"));
        if (tempPrice == null || tempPrice.isEmpty())
            tempPrice = c.getString(c.getColumnIndex("master_prod_onhand"));
        if (tempPrice.isEmpty())
            tempPrice = "0";
        data[4] = tempPrice;
        if (Global.isInventoryTransfer) {
            tempPrice = c.getString(c.getColumnIndex("location_qty"));
            if (tempPrice == null || tempPrice.isEmpty())
                tempPrice = "0";
            data[4] = tempPrice;
        }

        data[5] = c.getString(c.getColumnIndex("prod_img_name"));
        data[6] = c.getString(c.getColumnIndex("prod_istaxable"));
        data[7] = c.getString(c.getColumnIndex("prod_type"));
        data[8] = c.getString(c.getColumnIndex("cat_id"));
        data[9] = c.getString(c.getColumnIndex("prod_price_points"));
        if (data[9] == null || data[9].isEmpty())
            data[9] = "0";
        data[10] = c.getString(c.getColumnIndex("prod_value_points"));
        if (data[10] == null || data[10].isEmpty())
            data[10] = "0";

        data[11] = c.getString(c.getColumnIndex("prod_taxtype"));
        data[12] = c.getString(c.getColumnIndex("prod_taxcode"));
        return data;
    }

    private void performClickEvent() {
        String[] data = populateDataForIntent(myCursor);

        if (!isFastScanning) {
            Intent intent = new Intent(getActivity(), PickerProduct_FA.class);
            intent.putExtra("prod_id", data[0]);
            intent.putExtra("prod_name", data[1]);
            intent.putExtra("prod_on_hand", data[4]);
            intent.putExtra("prod_price", data[2]);
            intent.putExtra("prod_desc", data[3]);
            intent.putExtra("url", data[5]);
            intent.putExtra("prod_istaxable", data[6]);
            intent.putExtra("prod_type", data[7]);
            intent.putExtra("prod_taxcode", data[12]);
            intent.putExtra("prod_taxtype", data[11]);
            intent.putExtra("cat_id", myCursor.getString(myCursor.getColumnIndex("cat_id")));
            intent.putExtra("prod_price_points", data[9]);
            intent.putExtra("prod_value_points", data[10]);

            if (Global.isConsignment)
                intent.putExtra("consignment_qty", myCursor.getString(myCursor.getColumnIndex("consignment_qty")));

            startActivityForResult(intent, 0);
        } else {
            if (!OrderingMain_FA.instance.validAutomaticAddQty(data)) {
                Global.showPrompt(getActivity(), R.string.dlog_title_error, getActivity().getString(R.string.limit_onhand));
            } else {
                if (myPref.getPreferences(MyPreferences.pref_group_receipt_by_sku)) {
                    int orderIndex = global.checkIfGroupBySKU(getActivity(), data[0], "1");
                    if (orderIndex != -1 && !OrderingMain_FA.returnItem) {
                        global.refreshParticularOrder(myPref, orderIndex, data);
                        refreshListView();
                        callBackRefreshView.refreshView();
                    } else
                        automaticAddOrder(data);
                } else
                    automaticAddOrder(data);

            }

            if (OrderingMain_FA.returnItem) {
                if (OrderingMain_FA.mTransType == Global.TransactionType.RETURN) {
                    OrderingMain_FA.returnItem = true;
                } else {
                    OrderingMain_FA.returnItem = !OrderingMain_FA.returnItem;
                }
                OrderingMain_FA.switchHeaderTitle(OrderingMain_FA.returnItem, "Return");
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == 2) {
            refreshListView();
            getActivity().setResult(-2);
        } else if (resultCode == 9) {
        }
    }

    private void itemClicked(boolean showAllProducts) {
        if (!onRestaurantMode)
            performClickEvent();
        else if (onRestaurantMode && !restModeViewingProducts) {
            int i_id = myCursor.getColumnIndex("_id");
            int i_cat_name = myCursor.getColumnIndex("cat_name");
            int i_num_subcategories = myCursor.getColumnIndex("num_subcategories");
            getCategoryCursor(i_id, i_cat_name, i_num_subcategories, showAllProducts);
        } else {
            ProductAddonsHandler prodAddonsHandler = new ProductAddonsHandler(getActivity());
            List<HashMap<String, String>> tempListMap = prodAddonsHandler.getParentAddons(
                    myCursor.getString(myCursor.getColumnIndex("_id")));
            if (tempListMap != null && tempListMap.size() > 0) {
                Intent intent = new Intent(getActivity(), PickerAddon_FA.class);


                String data[] = populateDataForIntent(myCursor);
                intent.putExtra("prod_id", data[0]);
                intent.putExtra("prod_name", data[1]);
                intent.putExtra("prod_on_hand", data[4]);
                intent.putExtra("prod_price", data[2]);
                intent.putExtra("prod_desc", data[3]);
                intent.putExtra("url", data[5]);
                intent.putExtra("prod_istaxable", data[6]);
                intent.putExtra("prod_type", data[7]);
                intent.putExtra("prod_taxcode", data[12]);
                intent.putExtra("prod_taxtype", data[11]);
                intent.putExtra("cat_id", myCursor.getString(myCursor.getColumnIndex("cat_id")));

                intent.putExtra("prod_price_points", data[9]);
                intent.putExtra("prod_value_points", data[10]);

                Global.productParentAddons = tempListMap;

                global.addonSelectionType = new HashMap<String, String[]>();
                startActivityForResult(intent, 0);
            } else
                performClickEvent();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
        // TODO Auto-generated method stub

        if (!isFastScanning && SystemClock.elapsedRealtime() - lastClickTime < 1000) {
            return;
        }
        lastClickTime = SystemClock.elapsedRealtime();

        if (catalogIsPortrait && myCursor.moveToPosition(pos)) {
            if (!onRestaurantMode)
                performClickEvent();
            else if (onRestaurantMode && !restModeViewingProducts) {
                int i_id = myCursor.getColumnIndex("_id");
                int i_cat_name = myCursor.getColumnIndex("cat_name");
                int i_num_subcategories = myCursor.getColumnIndex("num_subcategories");
                getCategoryCursor(i_id, i_cat_name, i_num_subcategories, false);
            } else {
                ProductAddonsHandler prodAddonsHandler = new ProductAddonsHandler(getActivity());
                List<HashMap<String, String>> tempListMap = prodAddonsHandler.getParentAddons(
                        myCursor.getString(myCursor.getColumnIndex("_id")));
                if (tempListMap != null && tempListMap.size() > 0) {
                    Intent intent = new Intent(getActivity(), PickerAddon_FA.class);
                    // intent.putExtra("prod_id",
                    // myCursor.getString(myCursor.getColumnIndex("_id")));
                    String data[] = populateDataForIntent(myCursor);
                    intent.putExtra("prod_id", data[0]);
                    intent.putExtra("prod_name", data[1]);
                    intent.putExtra("prod_on_hand", data[4]);
                    intent.putExtra("prod_price", data[2]);
                    intent.putExtra("prod_desc", data[3]);
                    intent.putExtra("url", data[5]);
                    intent.putExtra("prod_istaxable", data[6]);
                    intent.putExtra("prod_type", data[7]);
                    intent.putExtra("prod_taxcode", data[12]);
                    intent.putExtra("prod_taxtype", data[11]);
                    intent.putExtra("cat_id", myCursor.getString(myCursor.getColumnIndex("cat_id")));

                    intent.putExtra("prod_price_points", data[9]);
                    intent.putExtra("prod_value_points", data[10]);

                    Global.productParentAddons = tempListMap;

                    global.addonSelectionType = new HashMap<String, String[]>();
                    startActivityForResult(intent, 0);
                } else
                    performClickEvent();
            }
        }
    }

    public void refreshListView() {
        if (_typeCase != CASE_PRODUCTS && _typeCase != CASE_SEARCH_PROD) {
            categoryListAdapter.notifyDataSetChanged();

        } else {
            prodListAdapter.notifyDataSetChanged();
        }
    }


    private class CustomSpinnerAdapter extends ArrayAdapter<String> {
        private Activity context;
        String[] leftData = null;

        public CustomSpinnerAdapter(Activity activity, int resource, String[] left) {
            super(activity, resource, left);
            this.context = activity;
            this.leftData = left;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);

            // we know that simple_spinner_item has android.R.id.text1 TextView:

            TextView text = (TextView) view.findViewById(android.R.id.text1);
            text.setTextAppearance(activity, R.style.black_text_appearance);// choose your color
            text.setTextSize(TypedValue.COMPLEX_UNIT_PX, activity.getResources().getDimension(R.dimen.ordering_checkout_btn_txt_size));
            return view;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = context.getLayoutInflater();
                row = inflater.inflate(R.layout.spinner_layout, parent, false);
            }
            ImageView checked = (ImageView) row.findViewById(R.id.checkMark);
            TextView taxName = (TextView) row.findViewById(R.id.taxName);
            checked.setVisibility(View.INVISIBLE);
            taxName.setText(leftData[position]);

            return row;
        }
    }


    private class ListViewAdapter extends BaseAdapter {

        private LayoutInflater myInflater;


        public ListViewAdapter(Context context) {

            myInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return catName.size();
        }

        @Override
        public Object getItem(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub

            ViewHolder holder;
            int type = getItemViewType(position);
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = myInflater.inflate(R.layout.categories_layout_adapter, null);
                holder.categoryName = (TextView) convertView.findViewById(R.id.categoryName);
                holder.icon = (ImageView) convertView.findViewById(R.id.subcategoryIcon);

                holder.categoryName.setText(catName.get(position));
                setHolderValues(holder, position, type);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();

                holder.categoryName.setText(catName.get(position));

                setHolderValues(holder, position, type);
            }
            return convertView;
        }

        @Override
        public int getItemViewType(int position) {
            if ((position > 0 && !isSubcategory) || (position >= 0 && isSubcategory)) {
                int index = position - 1;
                if (position >= 0 && isSubcategory)
                    index = position;


                if (myPref.getPreferences(MyPreferences.pref_enable_multi_category)) // check for available sub-categories
                {
                    if (!categories.get(index)[2].equals("0")) // there are sub-categories available
                    {
                        return 0;
                    } else
                        return 1;
                }
            }

            return 1;

        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        public void setHolderValues(ViewHolder holder, final int position, int type) {
            switch (type) {
                case 0: {
                    holder.icon.setVisibility(View.VISIBLE);
                    holder.icon.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            // TODO Auto-generated method stub
                            int index = position - 1;
                            if (isSubcategory)
                                index = position;
                            showSubcategories(categories.get(index)[1]);
                        }
                    });
                    break;
                }
                case 1: {
                    holder.icon.setVisibility(View.INVISIBLE);
                    break;
                }
            }
        }

        public class ViewHolder {
            TextView categoryName;
            ImageView icon;
        }
    }


    public void showSubcategories(String subCategoryName) {

        categories = catHandler.getSubcategories(subCategoryName);
        int size = categories.size();
        catName.clear();
        catIDs.clear();
        for (int i = 0; i < size; i++) {
            catName.add(categories.get(i)[0]);
            catIDs.add(categories.get(i)[1]);
        }

        isSubcategory = true;
        setupCategoryView();
        dialog.show();
    }
}
