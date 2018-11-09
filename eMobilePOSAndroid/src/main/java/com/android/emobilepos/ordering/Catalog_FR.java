package com.android.emobilepos.ordering;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.database.CategoriesHandler;
import com.android.database.ProductAddonsHandler;
import com.android.database.ProductsHandler;
import com.android.database.VolumePricesHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.models.EMSCategory;
import com.android.emobilepos.models.ParentAddon;
import com.android.emobilepos.models.Product;
import com.android.emobilepos.models.orders.OrderProduct;
import com.android.support.Global;
import com.android.support.MyEditText;
import com.android.support.MyPreferences;
import com.android.support.OrderProductUtils;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import util.json.JsonUtils;
import util.json.UIUtils;

public class Catalog_FR extends Fragment implements OnItemClickListener, OnClickListener, LoaderCallbacks<Cursor>,
        MenuProdGV_Adapter.ProductClickedCallback, CatalogCategories_Adapter.CategoriesCallback {

    private static final int CURSOR_LOADER_ID = 0x01;
    private static String BUNDLE_CATEGORY_STACK = "BUNDLE_CATEGORY_STACK";
    private static String BUNDLE_SELECTED_CATEGORY = "BUNDLE_SELECTED_CATEGORY";
    private static String BUNDLE_SEARCH_TEXT = "BUNDLE_SEARCH_TEXT";
    private static String BUNDLE_SEARCH_TYPE = "BUNDLE_SEARCH_TYPE";
    private static String BUNDLE_SEARCH_TYPE_ENUM = "BUNDLE_SEARCH_TYPE_ENUM";
    private String search_text = "", search_type = "";
    private AbsListView catalogList;
    private ImageLoader imageLoader;
    private Cursor myCursor;
    private MyPreferences myPref;
    private Global global;
    private boolean onRestaurantMode = false;
    private VolumePricesHandler volPriceHandler;
    private MenuProdGV_Adapter prodListAdapter;
    private RefreshReceiptViewCallback callBackRefreshView;
    private MyEditText searchField;
    private ListViewAdapter tempLVAdapter;
    private Dialog dialog;
    private boolean isSubcategory = false;
    private String[] searchFilters;
    private List<String> spinnerCatName = new ArrayList<>(), catName = new ArrayList<>();
    private List<String> spinnerCatID = new ArrayList<>(), catIDs = new ArrayList<>();
    private List<String[]> spinnerCategories = new ArrayList<>(), categories = new ArrayList<>();
    private CategoriesHandler catHandler;
    private boolean catalogIsPortrait = false;
    private boolean isFastScanning = false;
    //    private long lastClickTime = 0;
    private int page = 1;
    private boolean isToGo;
    private LinearLayout categoriesInnerWrapLayout;
    private RecyclerView catalogRecyclerView;
    private CatalogCategories_Adapter categoriesAdapter;
    private Button categoriesBackButton;
    private TextView categoriesBannerTextView;
    private List<EMSCategory> categoryStack = new ArrayList<>();
    private EMSCategory selectedSubcategory;
    private SearchType searchType = SearchType.NAME;
    private boolean isRestoringSelectedCategory = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.order_catalog_layout, container, false);
        isToGo = ((OrderingMain_FA) getActivity()).isToGo;
        searchField = (MyEditText) view.findViewById(R.id.catalogSearchField);
        searchField.setIsForSearching(getActivity(), getOrderingMainFa().invisibleSearchMain);
        catalogList = (AbsListView) view.findViewById(R.id.catalogListview);
        catalogList.setOnItemClickListener(this);
        catalogIsPortrait = Global.isPortrait(getActivity());
        callBackRefreshView = (RefreshReceiptViewCallback) getActivity();
        volPriceHandler = new VolumePricesHandler(getActivity());
        myPref = new MyPreferences(getActivity());
        imageLoader = ImageLoader.getInstance();
        File cacheDir = new File(myPref.getCacheDir());
        if (!cacheDir.exists())
            cacheDir.mkdirs();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getActivity()).memoryCacheExtraOptions(100, 100)
                .discCacheExtraOptions(1000, 1000, CompressFormat.JPEG, 100, null).discCache(new UnlimitedDiscCache(cacheDir)).build();
        imageLoader.init(config);
        imageLoader.handleSlowNetwork(true);
        isFastScanning = myPref.getPreferences(MyPreferences.pref_fast_scanning_mode);

        global = (Global) getActivity().getApplication();
        catHandler = new CategoriesHandler(getActivity());

        prodListAdapter = new MenuProdGV_Adapter(this, getActivity(), null, CursorAdapter.NO_SELECTION, imageLoader);

        if (myPref.isRestaurantMode()) {
            onRestaurantMode = true;
        }

        catalogList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (totalItemCount > 0 && myCursor.getCount() >= page * Integer.parseInt(getString(R.string.sqlLimit))) {
                    int lastInScreen = firstVisibleItem + visibleItemCount;
                    if (lastInScreen == totalItemCount) {
                        page++;
                        new CatalogProductLoader().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, totalItemCount);
                    }
                }
            }
        });

        categoriesInnerWrapLayout = (LinearLayout) view.findViewById(R.id.categoriesInnerWrapLayout);
        categoriesInnerWrapLayout.setVisibility(onRestaurantMode ? View.VISIBLE : View.GONE);

        setupSpinners(view);
        setupSearchField();

        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);

        categoriesBannerTextView = (TextView) view.findViewById(R.id.categoriesBannerTextView);
        categoriesBackButton = (Button) view.findViewById(R.id.categoriesBackButton);
        categoriesBackButton.setVisibility(View.GONE);
        categoriesBackButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                search_text = "";

                // Go Back one level
                selectedSubcategory = null;
                if (categoryStack.size() > 0) {
                    int lastIndex = categoryStack.size() - 1;
                    categoryStack.remove(lastIndex);

                    if (categoryStack.size() == 0) {
                        loadRootCategories();
                    } else {
                        loadSubCategories(categoryStack.get(categoryStack.size() - 1));
                    }
                } else {
                    loadRootCategories();
                }
                loadCursor();
            }
        });

        catalogRecyclerView = (RecyclerView) view.findViewById(R.id.categoriesRecyclerView);
        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        catalogRecyclerView.setLayoutManager(horizontalLayoutManager);

        if (savedInstanceState != null) {
            search_text = savedInstanceState.getString(BUNDLE_SEARCH_TEXT);
            search_type = savedInstanceState.getString(BUNDLE_SEARCH_TYPE);
            searchType = (SearchType) savedInstanceState.getSerializable(BUNDLE_SEARCH_TYPE_ENUM);

            categoryStack = savedInstanceState.getParcelableArrayList(BUNDLE_CATEGORY_STACK);
            categoriesBackButton.setVisibility(categoryStack.size() > 0 ? View.VISIBLE : View.GONE);

            if (categoryStack.size() > 0) {
                // Load last subcategory
                loadSubCategories(categoryStack.get(categoryStack.size() - 1));
            } else {
                // Load root categories
                loadRootCategories();
            }

            selectedSubcategory = savedInstanceState.getParcelable(BUNDLE_SELECTED_CATEGORY); // DO NOT MOVE THIS ABOVE
            if (selectedSubcategory != null) {
                isRestoringSelectedCategory = true;
                categoriesAdapter.selectItemWithCategoryId(selectedSubcategory.getCategoryId());
                isRestoringSelectedCategory = false;
            } else {
                loadCursor();
            }
        } else {
            loadRootCategories();

            // Check for default category in settings
            String defaultCategoryId = myPref.getPreferencesValue(MyPreferences.pref_default_category);

            if (!"0".equals(defaultCategoryId) && !TextUtils.isEmpty(defaultCategoryId)) {
                if (onRestaurantMode) {
                    selectedSubcategory = categoriesAdapter.getCategoryWithId(defaultCategoryId);
                    if (selectedSubcategory != null) {
                        isRestoringSelectedCategory = true;
                        categoriesAdapter.selectItemWithCategoryId(selectedSubcategory.getCategoryId());
                        isRestoringSelectedCategory = false;
                    } else {
                        loadCursor();
                    }
                } else {
                    for (String[] cat : spinnerCategories) {
                        if (defaultCategoryId.equals(cat[1])) {
                            selectedSubcategory = new EMSCategory(defaultCategoryId, cat[0], "", 0);
                            break;
                        }
                    }
                    loadCursor();
                }
            } else {
                loadCursor();
            }
        }

        return view;
    }

    private OrderingMain_FA getOrderingMainFa() {
        return (OrderingMain_FA) getActivity();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(BUNDLE_CATEGORY_STACK, (ArrayList<? extends Parcelable>) categoryStack);
        outState.putParcelable(BUNDLE_SELECTED_CATEGORY, selectedSubcategory);
        outState.putString(BUNDLE_SEARCH_TEXT, search_text);
        outState.putString(BUNDLE_SEARCH_TYPE, search_type);
        outState.putSerializable(BUNDLE_SEARCH_TYPE_ENUM, searchType);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void categorySelected(EMSCategory category) {

        if (!isRestoringSelectedCategory) {
            search_text = "";
        }

        selectedSubcategory = null;

        if (EMSCategory.ROOT_CATEGORY_ID.equals(category.getCategoryId())) {
            loadRootCategories();
        } else if (category.getNumberOfSubCategories() > 0) {
            categoryStack.add(category);
            loadSubCategories(category);
        } else {
            selectedSubcategory = category;
            refreshCategoriesBanner();
        }

        loadCursor();

        categoriesBackButton.setVisibility(categoryStack.size() > 0 ? View.VISIBLE : View.GONE);
//        categoriesBannerTextView.setVisibility(categoryStack.size() > 0 ? View.VISIBLE : View.GONE);
    }

    private void loadRootCategories() {
        categoryStack.clear();
        selectedSubcategory = null;
        categoriesBackButton.setVisibility(View.GONE);


        List<EMSCategory> cats = catHandler.getMainCategories();
        categoriesAdapter = new CatalogCategories_Adapter(getActivity(), this, cats, imageLoader);
        catalogRecyclerView.swapAdapter(categoriesAdapter, true);

        refreshCategoriesBanner();
    }

    private void loadSubCategories(EMSCategory category) {
        List<EMSCategory> cats = catHandler.getSubCategories(category.getCategoryId());
//        EMSCategory rootCategory = new EMSCategory("0", "All", "", 0);
//        cats.add(0, rootCategory);

        categoriesAdapter = new CatalogCategories_Adapter(getActivity(), this, cats, imageLoader);
        catalogRecyclerView.swapAdapter(categoriesAdapter, true);

        refreshCategoriesBanner();
    }

    private void refreshCategoriesBanner() {
        StringBuilder sb = new StringBuilder();

        if (categoryStack.size() > 0) {
            for (int i = 0; i < categoryStack.size(); i++) {
                if (i > 0) {
                    sb.append(" > ");
                }
                sb.append(categoryStack.get(i).getCategoryName());
            }

            if (selectedSubcategory != null) {
                sb.append(" > ");
            }
        }

        if (selectedSubcategory != null) {
            sb.append(selectedSubcategory.getCategoryName());
        }

        categoriesBannerTextView.setText(sb.toString());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.categoryButton:
                categories = new ArrayList<>(spinnerCategories);
                catName = new ArrayList<>(spinnerCatName);
                catIDs = new ArrayList<>(spinnerCatID);
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
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void onTextChanged(CharSequence s, int arg1, int arg2, int arg3) {
                String test = s.toString().trim();
                if (test.isEmpty() && !TextUtils.isEmpty(search_text)) {
                    search_text = "";
                    search_type = "";
                    loadCursor();
                }
            }
        });
    }

    private void setupSpinners(View v) {

        Button btnCategory = (Button) v.findViewById(R.id.categoryButton);
        if (onRestaurantMode)
            btnCategory.setVisibility(View.INVISIBLE);
        else
            btnCategory.setOnClickListener(this);

        Spinner spinnerFilter = (Spinner) v.findViewById(R.id.filterButton);

        Resources resources = getActivity().getResources();
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
//                global.searchType = position;
                searchType = SearchType.valueOf(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

    }

    private void setupCategoryView() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            tempLVAdapter.notifyDataSetChanged();
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final ListView list = new ListView(getActivity());
        tempLVAdapter = new ListViewAdapter(getActivity());
        list.setCacheColorHint(Color.TRANSPARENT);
        list.setAdapter(tempLVAdapter);
        builder.setView(list);


        list.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                if (position == 0 && !isSubcategory) {
                    selectedSubcategory = null;
                } else {

                    int index = position;
                    if (!isSubcategory) {
                        index -= 1;
                    }

                    selectedSubcategory = new EMSCategory(categories.get(index)[1], categories.get(index)[0], "", 0);

                }
                dialog.dismiss();
                loadCursor();
            }
        });

        dialog = builder.create();


    }

    public void loadCursor() {
        getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, this).forceLoad();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        EMSCategory categoryToLoad = selectedSubcategory;

        if (onRestaurantMode && categoryToLoad == null && categoryStack.size() > 0) {
            categoryToLoad = categoryStack.get(categoryStack.size() - 1);
        }

        return new Catalog_Loader(getActivity(), Integer.parseInt(getString(R.string.sqlLimit)), 0, categoryToLoad, search_text, search_type, onRestaurantMode);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        closeCursor();
    }

    public void closeCursor() {
        if (myCursor != null) {
            myCursor.close();
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor c) {
        myCursor = c;
        prodListAdapter = new MenuProdGV_Adapter(this, getActivity(), c, CursorAdapter.NO_SELECTION, imageLoader);
        catalogList.setAdapter(prodListAdapter);

        if (!onRestaurantMode) {
            categoriesBannerTextView.setVisibility(selectedSubcategory != null ? View.VISIBLE : View.GONE);
            refreshCategoriesBanner();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        if (catalogList != null) {
            catalogList.setAdapter(null);
        }
    }

    @Override
    public void productClicked(int position) {
        myCursor.moveToPosition(position);
        itemClicked();
    }

    public void searchUPC(String upc) {
        search_text = upc;
        search_type = "prod_upc";
        loadCursor();
    }

    public void performSearch(String text) {
        search_text = text;

        switch (searchType) {
            case NAME: // search by Name
                search_type = "prod_name";
                break;
            case DESCRIPTION: // search by Description
                search_type = "prod_desc";
                break;
            case TYPE: // search by Type
                search_type = "prod_type";
                break;
            case UPC: // search by UPC
                search_type = "prod_upc";
                break;
            case SKU: // search by SKU
                search_type = "prod_sku";
                break;
        }

        loadCursor();
        getOrderingMainFa().invisibleSearchMain.requestFocus();
    }

    public void automaticAddOrder(Product product) {
        getOrderingMainFa().disableCheckoutButton();
        OrderingMain_FA.automaticAddOrder(getActivity(), false, global, new OrderProduct(product), ((OrderingMain_FA) getActivity()).getSelectedSeatNumber(), ((OrderingMain_FA) getActivity()).mTransType);
        refreshListView();
        callBackRefreshView.refreshView();
    }

    public Product populateDataForIntent(Cursor c) {
        Product product = new Product();
        product.setId(c.getString(myCursor.getColumnIndex("_id")));
        product.setAssignedSeat(((OrderingMain_FA) getActivity()).getSelectedSeatNumber());
        String val = myPref.getPreferencesValue(MyPreferences.pref_attribute_to_display);
        product.setProdDesc(c.getString(c.getColumnIndex("prod_desc")));
        product.setProdName(c.getString(c.getColumnIndex("prod_name")));
        product.setGC(Boolean.parseBoolean(c.getString(c.getColumnIndex("isGC"))));
        product.setProdExtraDesc(c.getString(c.getColumnIndex("prod_extradesc")));
        product.setPricesXGroupid(c.getString(c.getColumnIndex(ProductsHandler.prod_prices_group_id)));
        String tempPrice = c.getString(c.getColumnIndex("volume_price"));
        if (tempPrice == null || tempPrice.isEmpty()) {
            tempPrice = c.getString(c.getColumnIndex("pricelevel_price"));
            if (tempPrice == null || tempPrice.isEmpty()) {
                tempPrice = c.getString(c.getColumnIndex("chain_price"));

                if (tempPrice == null || tempPrice.isEmpty())
                    tempPrice = c.getString(c.getColumnIndex("master_price"));
            }
        } else if (global.order.getOrderProducts().contains(product.getId())) {
            BigDecimal origQty = Global.getBigDecimalNum(OrderProductUtils.getOrderProductQty(global.order.getOrderProducts(), product.getId()));
            BigDecimal newQty = origQty.add(Global.getBigDecimalNum("1"));
            String[] temp = volPriceHandler.getVolumePrice(newQty.toString(), product.getId());
            if (temp[1] != null && !temp[1].isEmpty())
                tempPrice = temp[1];
        }
        product.setProdPrice(tempPrice);
        product.setProdDesc(c.getString(c.getColumnIndex("prod_desc")));
        tempPrice = c.getString(c.getColumnIndex("local_prod_onhand"));
        if (tempPrice == null || tempPrice.isEmpty())
            tempPrice = c.getString(c.getColumnIndex("master_prod_onhand"));
        if (tempPrice.isEmpty())
            tempPrice = "0";
        product.setProdOnHand(tempPrice);
        if (Global.isInventoryTransfer) {
            String locationQuantity = "0";
            try {
                locationQuantity = c.getString(c.getColumnIndex("location_qty"));
                if (locationQuantity == null || locationQuantity.isEmpty())
                    locationQuantity = "0";
            } catch (Exception e) {
                // no quantity
            } finally {
                product.setProdOnHand(locationQuantity);
            }
        }
        product.setProdImgName(c.getString(c.getColumnIndex("prod_img_name")));
        product.setProdIstaxable(c.getString(c.getColumnIndex("prod_istaxable")));
        product.setProdType(c.getString(c.getColumnIndex("prod_type")));
        product.setCatId(c.getString(c.getColumnIndex("cat_id")));
        product.setCategoryName(c.getString(c.getColumnIndex("cat_name")));
        product.setProdPricePoints(c.getInt(c.getColumnIndex("prod_price_points")));
        product.setProdValuePoints(c.getInt(c.getColumnIndex("prod_value_points")));
        product.setProdTaxType(c.getString(c.getColumnIndex("prod_taxtype")));
        product.setProdTaxCode(c.getString(c.getColumnIndex("prod_taxcode")));
        product.setProd_sku(c.getString(c.getColumnIndex("prod_sku")));
        product.setProd_upc(c.getString(c.getColumnIndex("prod_upc")));
        return product;
    }

    private void performClickEvent() {
        Product product = populateDataForIntent(myCursor);
        if (myPref.isGroupReceiptBySku(isToGo)) {//(myPref.getPreferences(MyPreferences.pref_group_receipt_by_sku)) {
            List<OrderProduct> orderProductsGroupBySKU = OrderProductUtils.getOrderProductsGroupBySKU(global.order.getOrderProducts());
            global.order.getOrderProducts().clear();
            global.order.getOrderProducts().addAll(orderProductsGroupBySKU);
        }
        if (!isFastScanning) {
            Intent intent = new Intent(getActivity(), PickerProduct_FA.class);
            Gson gson = JsonUtils.getInstance();
            product.setAssignedSeat(((OrderingMain_FA) getActivity()).getSelectedSeatNumber());
            String json = gson.toJson(new OrderProduct(product));
            intent.putExtra("orderProduct", json);
            intent.putExtra("isToGo", isToGo);
            intent.putExtra("transType", getOrderingMainFa().mTransType);

            if (Global.isConsignment)
                intent.putExtra("consignment_qty", myCursor.getString(myCursor.getColumnIndex("consignment_qty")));

            startActivityForResult(intent, 0);
        } else {
            OrderingMain_FA orderingMain = (OrderingMain_FA) getActivity();
            if (!orderingMain.validAutomaticAddQty(product)) {
                Global.showPrompt(getActivity(), R.string.dlog_title_error, getActivity().getString(R.string.limit_onhand));
            } else {
                if (myPref.isGroupReceiptBySku(isToGo)) {//(myPref.getPreferences(MyPreferences.pref_group_receipt_by_sku)) {
                    int orderIndex = global.checkIfGroupBySKU(getActivity(), product.getId(), "1");
                    if (orderIndex != -1 && !OrderingMain_FA.returnItem) {
                        global.refreshParticularOrder(getActivity(), orderIndex, product);
                        refreshListView();
                        callBackRefreshView.refreshView();
                    } else
                        automaticAddOrder(product);
                } else
                    automaticAddOrder(product);

            }

            if (OrderingMain_FA.returnItem) {
                OrderingMain_FA.returnItem = getOrderingMainFa().mTransType == Global.TransactionType.RETURN || !OrderingMain_FA.returnItem;
                getOrderingMainFa().switchHeaderTitle(OrderingMain_FA.returnItem, "Return", getOrderingMainFa().mTransType);
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == 2) {
            refreshListView();
            getActivity().setResult(-2);
        }
    }

    // Called in landscape
    private void itemClicked() {
        OrderingMain_FA mainFa = (OrderingMain_FA) getActivity();
        mainFa.disableCheckoutButton();
        if (!onRestaurantMode) {
            performClickEvent();
        } else {
            ProductAddonsHandler prodAddonsHandler = new ProductAddonsHandler(getActivity());
            List<ParentAddon> parentAddons = prodAddonsHandler.getParentAddons(
                    myCursor.getString(myCursor.getColumnIndex("_id")));
            if (parentAddons != null && parentAddons.size() > 0) {
                Intent intent = new Intent(getActivity(), PickerAddon_FA.class);

                Product product = populateDataForIntent(myCursor);
                intent.putExtra("selectedSeatNumber", ((OrderingMain_FA) getActivity()).getSelectedSeatNumber());
                intent.putExtra("prod_id", product.getId());
                intent.putExtra("orderProduct", new OrderProduct(product).toJson());
                intent.putExtra("prod_name", product.getProdName());
                intent.putExtra("prod_on_hand", product.getProdOnHand());
                intent.putExtra("prod_price", product.getProdPrice());
                intent.putExtra("prod_desc", product.getProdDesc());
                intent.putExtra("url", product.getProdImgName());
                intent.putExtra("prod_istaxable", product.getProdIstaxable());
                intent.putExtra("prod_type", product.getProdType());
                intent.putExtra("prod_taxcode", product.getProdTaxCode());
                intent.putExtra("prod_taxtype", product.getProdTaxType());
                intent.putExtra("cat_id", product.getCatId());
                intent.putExtra("prod_sku", product.getProd_sku());
                intent.putExtra("prod_upc", product.getProd_upc());
                intent.putExtra("prod_price_points", product.getProdPricePoints());
                intent.putExtra("prod_value_points", product.getProdValuePoints());
                intent.putExtra("transType", getOrderingMainFa().mTransType);

                startActivityForResult(intent, 0);
            } else {
                performClickEvent();
            }
        }
    }

    // Called in portrait
    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {

//        if (!isFastScanning && SystemClock.elapsedRealtime() - lastClickTime < 1000) {
//            return;
//        }
//        lastClickTime = SystemClock.elapsedRealtime();
        if (isFastScanning || UIUtils.singleOnClick(arg1)) {
            if (catalogIsPortrait && myCursor.moveToPosition(pos)) {
                if (!onRestaurantMode) {
                    performClickEvent();
                } else {
                    ProductAddonsHandler prodAddonsHandler = new ProductAddonsHandler(getActivity());
                    List<ParentAddon> parentAddons = prodAddonsHandler.getParentAddons(
                            myCursor.getString(myCursor.getColumnIndex("_id")));
                    if (parentAddons != null && parentAddons.size() > 0) {
                        Intent intent = new Intent(getActivity(), PickerAddon_FA.class);

                        Product product = populateDataForIntent(myCursor);
                        intent.putExtra("orderProduct", new OrderProduct(product).toJson());
                        intent.putExtra("selectedSeatNumber", ((OrderingMain_FA) getActivity()).getSelectedSeatNumber());
                        intent.putExtra("prod_id", product.getId());
                        intent.putExtra("prod_name", product.getProdName());
                        intent.putExtra("prod_on_hand", product.getProdOnHand());
                        intent.putExtra("prod_price", product.getProdPrice());
                        intent.putExtra("prod_desc", product.getProdDesc());
                        intent.putExtra("url", product.getProdImgName());
                        intent.putExtra("prod_istaxable", product.getProdIstaxable());
                        intent.putExtra("prod_type", product.getProdType());
                        intent.putExtra("prod_taxcode", product.getProdTaxCode());
                        intent.putExtra("prod_taxtype", product.getProdType());
                        intent.putExtra("cat_id", product.getCatId());
                        intent.putExtra("prod_sku", product.getProd_sku());
                        intent.putExtra("prod_upc", product.getProd_upc());
                        intent.putExtra("prod_price_points", product.getProdPricePoints());
                        intent.putExtra("prod_value_points", product.getProdValuePoints());
                        intent.putExtra("transType", getOrderingMainFa().mTransType);

                        startActivityForResult(intent, 0);
                    } else {
                        performClickEvent();
                    }
                }
            }
        }
    }

    public void refreshListView() {
        prodListAdapter.notifyDataSetChanged();
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

    private enum SearchType {
        NAME(0), DESCRIPTION(1), TYPE(2), UPC(3), SKU(4);

        public int id;

        SearchType(int id) {
            this.id = id;
        }

        public static SearchType valueOf(int code) {
            switch (code) {
                case 0:
                    return NAME;
                case 1:
                    return DESCRIPTION;
                case 2:
                    return TYPE;
                case 3:
                    return UPC;
                case 4:
                    return SKU;
                default:
                    return UPC;
            }
        }

    }

    public interface RefreshReceiptViewCallback {
        void refreshView();
    }

    public class CatalogProductLoader extends AsyncTask<Integer, Void, Catalog_Loader> {

        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(Catalog_FR.this.getActivity());
            progressDialog.setMessage("Loading...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Catalog_Loader doInBackground(Integer... params) {
            return new Catalog_Loader(getActivity(), params[0] + Integer.parseInt(getString(R.string.sqlLimit)), 1, selectedSubcategory, search_text, search_type, onRestaurantMode);
        }

        @Override
        protected void onPostExecute(Catalog_Loader catalog_loader) {
            if (myCursor != null && !myCursor.isClosed()) {
                myCursor.close();
            }
            myCursor = catalog_loader.loadInBackground();
            prodListAdapter.swapCursor(myCursor);
            prodListAdapter.notifyDataSetChanged();
            progressDialog.dismiss();
        }
    }

    private class CustomSpinnerAdapter extends ArrayAdapter<String> {
        String[] leftData = null;
        private Activity context;

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
            text.setTextAppearance(getActivity(), R.style.black_text_appearance);// choose your color
            text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getActivity().getResources().getDimension(R.dimen.ordering_checkout_btn_txt_size));
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
            return catName.size();
        }

        @Override
        public Object getItem(int arg0) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
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
}