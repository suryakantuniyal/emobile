package com.android.emobilepos.mainmenu.restaurant;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;

import com.android.dao.ClerkDAO;
import com.android.dao.DinningTableDAO;
import com.android.database.OrdersHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.models.orders.Order;
import com.android.emobilepos.models.realms.Clerk;
import com.android.emobilepos.models.realms.DinningTable;
import com.android.emobilepos.models.realms.DinningTableOrder;
import com.android.emobilepos.ordering.OrderingMain_FA;
import com.android.emobilepos.ordering.SplittedOrderSummary_FA;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.NetworkUtils;
import com.android.support.OnHoldsManager;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;
import com.viewpagerindicator.IconPagerAdapter;
import com.viewpagerindicator.PageIndicator;
import com.viewpagerindicator.TitlePageIndicator;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class DinningTablesActivity extends BaseFragmentActivityActionBar {

    public String associateId;
    protected List<DinningTable> dinningTables;
    protected Clerk associate;
    private SectionsPagerAdapter mSectionsPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        associateId = String.valueOf(extras.getInt("associateId"));
        setContentView(R.layout.activity_dinning_tables);
//        new SynchOnHoldOrders().execute();
        dinningTables = DinningTableDAO.getAll("number");//DinningTablesProxy.getDinningTables(getContext());
        if (!TextUtils.isEmpty(associateId)) {
            associate = ClerkDAO.getByEmpId(Integer.parseInt(associateId));
        }
        refresh(0);
    }

    public void refresh(int page) {
        if(findViewById(R.id.indicator)!=null) {
            setmSectionsPagerAdapter(new SectionsPagerAdapter(getFragmentManager()));
            ViewPager mViewPager = findViewById(R.id.container);
            PageIndicator titlePageIndicator = (TitlePageIndicator) findViewById(R.id.indicator);
            mViewPager.setAdapter(getmSectionsPagerAdapter());
            titlePageIndicator.setViewPager(mViewPager);
            mViewPager.setCurrentItem(page);
        }else{
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.add(R.id.container, new TablesGridFragment());
            ft.commit();
        }
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

    @Override
    public void onResume() {
        Global global = (Global) getApplication();
        if (global.isApplicationSentToBackground())
            Global.loggedIn = false;
        global.stopActivityTransitionTimer();

        if (!Global.loggedIn) {
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
//        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
//        boolean isScreenOn = powerManager.isScreenOn();
//        MyPreferences myPref = new MyPreferences(this);
//        if (!isScreenOn && myPref.isExpireUserSession())
//            Global.loggedIn = false;
        global.startActivityTransitionTimer();
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
    public class OpenOnHoldOrderTask extends AsyncTask<Object, Void, Boolean> {

        DinningTableOrder tableOrder;
        ProgressDialog dialog;
        private DinningTable table;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Global.lockOrientation(DinningTablesActivity.this);
            dialog = new ProgressDialog(DinningTablesActivity.this);
            dialog.setIndeterminate(true);
            dialog.setMessage(getString(R.string.loading_orders));
            dialog.show();
        }

        @Override
        protected Boolean doInBackground(Object... params) {
            tableOrder = (DinningTableOrder) params[0];
            table = (DinningTable) params[1];
            if (NetworkUtils.isConnectedToInternet(DinningTablesActivity.this)) {
                boolean claimRequired = false;
                try {
                    claimRequired = OnHoldsManager.isOnHoldAdminClaimRequired(tableOrder.getCurrentOrderId(), DinningTablesActivity.this);
                } catch (NoSuchAlgorithmException e) {

                } catch (IOException e) {

                } catch (KeyManagementException e) {

                }
                if (claimRequired) {
                    return false;
                } else {
                    Global.isFromOnHold = true;
                }
            } else {
                OrdersHandler ordersHandler = new OrdersHandler(DinningTablesActivity.this);
                if (ordersHandler.isOrderOffline(tableOrder.getCurrentOrderId())) {
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
            Global.dismissDialog(DinningTablesActivity.this, dialog);
            Global.releaseOrientation(DinningTablesActivity.this);
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
