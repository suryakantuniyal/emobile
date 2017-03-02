package com.android.emobilepos.mainmenu.restaurant;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.android.database.OrderProductsHandler;
import com.android.database.OrdersHandler;
import com.android.emobilepos.OnHoldActivity;
import com.android.emobilepos.R;
import com.android.emobilepos.models.Order;
import com.android.emobilepos.models.OrderProduct;
import com.android.emobilepos.models.realms.DinningTable;
import com.android.emobilepos.models.realms.DinningTableOrder;
import com.android.emobilepos.ordering.OrderingMain_FA;
import com.android.emobilepos.ordering.SplittedOrderSummary_FA;
import com.android.support.Global;
import com.android.support.NetworkUtils;
import com.android.support.OnHoldsManager;
import com.android.support.SynchMethods;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;
import com.viewpagerindicator.IconPagerAdapter;
import com.viewpagerindicator.PageIndicator;
import com.viewpagerindicator.TitlePageIndicator;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;

public class DinningTablesActivity extends BaseFragmentActivityActionBar {

    private SectionsPagerAdapter mSectionsPagerAdapter;

    public String associateId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        associateId = String.valueOf(extras.getInt("associateId"));
        setContentView(R.layout.activity_dinning_tables);
//        new SynchOnHoldOrders().execute();
        refresh(0);
    }

    public void refresh(int page) {
        setmSectionsPagerAdapter(new SectionsPagerAdapter(getFragmentManager()));
        ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
        PageIndicator titlePageIndicator = (TitlePageIndicator) findViewById(R.id.indicator);
        mViewPager.setAdapter(getmSectionsPagerAdapter());
        titlePageIndicator.setViewPager(mViewPager);
        mViewPager.setCurrentItem(page);
    }

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    public SectionsPagerAdapter getmSectionsPagerAdapter() {
        return mSectionsPagerAdapter;
    }

    public void setmSectionsPagerAdapter(SectionsPagerAdapter mSectionsPagerAdapter) {
        this.mSectionsPagerAdapter = mSectionsPagerAdapter;
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter implements IconPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case 0:
                    return new TablesMapFragment();
                default:
                    return new TablesGridFragment();
            }
        }

        @Override
        public int getIconResId(int i) {
            return 0;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.table_map);
                case 1:
                    return getString(R.string.table_list);
            }
            return null;
        }
    }

    @Override
    public void onResume() {
        Global global = (Global) getApplication();
        if (global.isApplicationSentToBackground(this))
            global.loggedIn = false;
        global.stopActivityTransitionTimer();

        if (!global.loggedIn) {
            if (global.getGlobalDlog() != null)
                global.getGlobalDlog().dismiss();
            global.promptForMandatoryLogin(this);
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        Global global = (Global) getApplication();
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        boolean isScreenOn = powerManager.isScreenOn();
        if (!isScreenOn)
            global.loggedIn = false;
        global.startActivityTransitionTimer();
    }
//
//    private class SynchOnHoldOrders extends AsyncTask<Void, String, Void> {
//        ProgressDialog progressDialog;
//
//        @Override
//        protected void onPreExecute() {
//            progressDialog = new ProgressDialog(DinningTablesActivity.this);
//            progressDialog.setMessage(getString(R.string.loading_orders));
//            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//            progressDialog.setCancelable(false);
//            progressDialog.show();
//        }
//
//        @Override
//        protected Void doInBackground(Void... params) {
//            try {
//                if (NetworkUtils.isConnectedToInternet(DinningTablesActivity.this)) {
//                    SynchMethods.synchSalesAssociateDinnindTablesConfiguration(DinningTablesActivity.this);
//                    updateProgress(getString(R.string.sync_dload_ordersonhold));
//                    SynchMethods.synchOrdersOnHoldList(DinningTablesActivity.this);
//                }
//            } catch (SAXException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            return null;
//        }
//
//        public void updateProgress(String msg) {
//            publishProgress(msg);
//        }
//
//        @Override
//        protected void onPostExecute(Void aVoid) {
//            progressDialog.dismiss();
//            refresh(0);
//        }
//    }

    public class OpenOnHoldOrderTask extends AsyncTask<Object, Void, Boolean> {

        private DinningTable table;
        DinningTableOrder tableOrder;

        @Override
        protected Boolean doInBackground(Object... params) {
            tableOrder = (DinningTableOrder) params[0];
            table = (DinningTable) params[1];
            if (NetworkUtils.isConnectedToInternet(DinningTablesActivity.this)) {
                boolean claimRequired = OnHoldsManager.isOnHoldAdminClaimRequired(tableOrder.getCurrentOrderId(), DinningTablesActivity.this);
                if (claimRequired) {
                    return false;
                } else {
                    try {
                        OnHoldsManager.synchOrdersOnHoldDetails(DinningTablesActivity.this, tableOrder.getCurrentOrderId());
//                        OrderProductsHandler orderProdHandler = new OrderProductsHandler(DinningTablesActivity.this);
//                        Cursor c = orderProdHandler.getOrderProductsOnHold(tableOrder.getCurrentOrderId());
//                        Global global = (Global) DinningTablesActivity.this.getApplication();
//                        global.order.setOrderProducts(new ArrayList<OrderProduct>());
//                        OnHoldActivity.addOrderProducts(DinningTablesActivity.this, c);
                        Global.isFromOnHold = true;

                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (SAXException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                OrdersHandler ordersHandler = new OrdersHandler(DinningTablesActivity.this);
                if (ordersHandler.isOrderOffline(tableOrder.getCurrentOrderId())) {
//                    OrderProductsHandler orderProdHandler = new OrderProductsHandler(DinningTablesActivity.this);
//                    Cursor c = orderProdHandler.getOrderProductsOnHold(tableOrder.getCurrentOrderId());
//                    Global global = (Global) DinningTablesActivity.this.getApplication();
//                    global.order.setOrderProducts(new ArrayList<OrderProduct>());
//                    OnHoldActivity.addOrderProducts(DinningTablesActivity.this, c);
                    Global.isFromOnHold = true;
                } else {
                    return false;
                }
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean willOpen) {
            if (willOpen) {
                if (Global.isFromOnHold) {
                    openOrderingMain();
                } else {
                    Intent result = new Intent();
                    result.putExtra("tableId", table.getId());
                    DinningTablesActivity.this.setResult(SplittedOrderSummary_FA.NavigationResult.TABLE_SELECTION.getCode(), result);
                    DinningTablesActivity.this.finish();
                }
            } else {
                Global.showPrompt(DinningTablesActivity.this, R.string.dlog_title_claimed_hold, getString(R.string.dlog_msg_cant_open_claimed_hold));
            }
        }

        private void openOrderingMain() {
            Global.lastOrdID = tableOrder.getCurrentOrderId();
            Order order = tableOrder.getOrder(DinningTablesActivity.this);
            Intent intent = new Intent(DinningTablesActivity.this, OrderingMain_FA.class);
            intent.putExtra("selectedDinningTableNumber", table.getNumber());
            intent.putExtra("onHoldOrderJson", order.toJson());
            intent.putExtra("openFromHold", true);
            intent.putExtra("RestaurantSaleType", Global.RestaurantSaleType.EAT_IN);
            intent.putExtra("option_number", Global.TransactionType.SALE_RECEIPT);
            intent.putExtra("ord_HoldName", order.ord_HoldName);
            intent.putExtra("associateId", order.associateID);
            startActivityForResult(intent, 0);
            DinningTablesActivity.this.finish();
        }
    }
}
