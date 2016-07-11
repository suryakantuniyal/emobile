package com.android.emobilepos.mainmenu;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.database.DBManager;
import com.android.emobilepos.R;
import com.android.support.DeviceUtils;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.NetworkUtils;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;

import java.lang.reflect.Method;
import java.util.ArrayList;

import drivers.EMSsnbc;
import main.EMSDeviceManager;

public class MainMenu_FA extends BaseFragmentActivityActionBar {

    public static Activity activity;
    private Global global;
    private boolean hasBeenCreated = false;
    private static MyPreferences myPref;
    private TextView synchTextView, tvStoreForward;
    private AdapterTabs tabsAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        ViewPager viewPager = (ViewPager) findViewById(R.id.main_menu_pager);
        synchTextView = (TextView) findViewById(R.id.synch_title);
        synchTextView.setVisibility(View.GONE);
        tvStoreForward = (TextView) findViewById(R.id.label_cc_offline);

        myPref = new MyPreferences(this);

        activity = this;
        global = (Global) getApplication();

        setTabsAdapter(new AdapterTabs(this, viewPager));

        getTabsAdapter().addTab(myBar.newTab().setText(R.string.sales_title), SalesTab_FR.class, null);
        myBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        getTabsAdapter().addTab(myBar.newTab().setText(R.string.sync_title), SyncTab_FR.class, null);
        myBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        getTabsAdapter().addTab(myBar.newTab().setText(R.string.hist_title), HistoryTab_FR.class, null);
        myBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        getTabsAdapter().addTab(myBar.newTab().setText(R.string.routes_title), RoutesTab_FR.class, null);
        myBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        // tabsAdapter.addTab(myBar.newTab().setText(R.string.admin_title),
        // SettingsMenuActivity.class, null);
        getTabsAdapter().addTab(myBar.newTab().setText(R.string.admin_title), SettingsTab_FR.class, null);
        myBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        getTabsAdapter().addTab(myBar.newTab().setText(R.string.report_title).setTag("Reports Fragment"), ReportTab_FR.class,
                null);
        myBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        getTabsAdapter().addTab(myBar.newTab().setText(R.string.clock_title), ClockTab_FR.class, null);
        myBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        getTabsAdapter().addTab(myBar.newTab().setText(R.string.about_title), AboutTab_FR.class, null);
        myBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        forceTabs();

        Bundle extras = activity.getIntent().getExtras();
        if (extras != null && extras.getBoolean("unsynched_items", false))
            myBar.setSelectedNavigationItem(1);

        hasBeenCreated = true;
    }


    @Override
    public void onResume() {
        if (global.isApplicationSentToBackground(activity)) {
            global.loggedIn = false;
        }
        global.stopActivityTransitionTimer();

        if (hasBeenCreated && !global.loggedIn
                && (myPref.getPrinterType() != Global.POWA || (myPref.getPrinterType() == Global.POWA
                && (Global.mainPrinterManager != null && Global.mainPrinterManager.currentDevice != null)))) {
            if (global.getGlobalDlog() != null)
                global.getGlobalDlog().dismiss();
            global.promptForMandatoryLogin(activity);
        }

        if (myPref.getPreferences(MyPreferences.pref_automatic_sync) && hasBeenCreated && NetworkUtils.isConnectedToInternet(activity)) {
            DBManager dbManager = new DBManager(activity, Global.FROM_SYNCH_ACTIVITY);
            dbManager.synchSend(false, true);
        }

        if (myPref.getPreferences(MyPreferences.pref_use_store_and_forward))
            tvStoreForward.setVisibility(View.VISIBLE);
        else
            tvStoreForward.setVisibility(View.GONE);

        new autoConnectPrinter().execute();
        super.onResume();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        forceTabs();
    }

    public void forceTabs() {
        try {
            final ActionBar actionBar = getActionBar();
            final Method setHasEmbeddedTabsMethod = actionBar.getClass().getDeclaredMethod("setHasEmbeddedTabs",
                    boolean.class);
            setHasEmbeddedTabsMethod.setAccessible(true);
            setHasEmbeddedTabsMethod.invoke(actionBar, false);
        } catch (final Exception e) {
            // Handle issues as needed: log, warn user, fallback etc
            // This error is safe to ignore, standard tabs will appear.
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            this.moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onPause() {
        super.onPause();
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        boolean isScreenOn = powerManager.isScreenOn();
        if (!isScreenOn)
            global.loggedIn = false;
        global.startActivityTransitionTimer();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        global.loggedIn = false;
        this.finish();
    }

    public AdapterTabs getTabsAdapter() {
        return tabsAdapter;
    }

    public void setTabsAdapter(AdapterTabs tabsAdapter) {
        this.tabsAdapter = tabsAdapter;
    }

    private class autoConnectPrinter extends AsyncTask<String, String, String> {
        boolean isUSB = false;
        private boolean loadMultiPrinter;
        private ProgressDialog myProgressDialog;

        @Override
        protected void onPreExecute() {
            loadMultiPrinter = (Global.multiPrinterManager == null
                    || Global.multiPrinterManager.size() == 0)
                    && (Global.mainPrinterManager == null
                    || Global.mainPrinterManager.currentDevice == null);

            myProgressDialog = new ProgressDialog(activity);
            myProgressDialog.setMessage(getString(R.string.connecting_devices));
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.setCancelable(false);
            if (myProgressDialog.isShowing())
                myProgressDialog.dismiss();
            if (loadMultiPrinter) {
                myProgressDialog.show();
            }
        }

        @Override
        protected String doInBackground(String... params) {
            String autoConnect = DeviceUtils.autoConnect(activity, loadMultiPrinter);
            if (myPref.getPrinterType() == Global.POWA) {
                isUSB = true;
            }
            if (Global.mainPrinterManager != null && Global.mainPrinterManager.currentDevice != null &&
                    Global.mainPrinterManager.currentDevice instanceof EMSsnbc) {
                ((EMSsnbc) Global.mainPrinterManager.currentDevice).closeUsbInterface();
            }
            return autoConnect;
        }

        @Override
        protected void onPostExecute(String result) {
            if (!isUSB && result.toString().length() > 0)
                Toast.makeText(activity, result.toString(), Toast.LENGTH_LONG).show();
            else if (isUSB && (Global.mainPrinterManager == null || Global.mainPrinterManager.currentDevice == null)) {
                if (global.getGlobalDlog() != null)
                    global.getGlobalDlog().dismiss();
                EMSDeviceManager edm = new EMSDeviceManager();
                Global.mainPrinterManager = edm.getManager();
                Global.mainPrinterManager.loadMultiDriver(activity, myPref.getPrinterType(), 0, true, "", "");
            }
            if (myProgressDialog != null && myProgressDialog.isShowing())
                myProgressDialog.dismiss();
        }
    }


    public TextView getSynchTextView() {
        return synchTextView;
    }

    private class AdapterTabs extends FragmentPagerAdapter
            implements ActionBar.TabListener, ViewPager.OnPageChangeListener {

        private final Context myContext;
        private final ViewPager myViewPager;
        private final ActionBar myActionBar;
        private final ArrayList<TabInfo> myTabs = new ArrayList<TabInfo>();

        final class TabInfo {
            private final Class<?> clazz;
            private final Bundle args;

            TabInfo(Class<?> _clazz, Bundle _args) {
                clazz = _clazz;
                args = _args;
            }
        }

        public AdapterTabs(FragmentActivity activity, ViewPager pager) {
            super(activity.getSupportFragmentManager());
            myContext = activity;
            myActionBar = activity.getActionBar();
            myViewPager = pager;
            myViewPager.setAdapter(this);
            myViewPager.addOnPageChangeListener(this);//setOnPageChangeListener(this);
        }

        public void addTab(ActionBar.Tab tab, Class<?> clzz, Bundle args) {
            TabInfo info = new TabInfo(clzz, args);
            tab.setTag(info);
            tab.setTabListener(this);
            myTabs.add(info);

            myActionBar.addTab(tab);
            notifyDataSetChanged();
        }

        @Override
        public void onTabSelected(Tab tab, FragmentTransaction ft) {
            Object tag = tab.getTag();
            int size = myTabs.size();
            for (int i = 0; i < size; i++) {
                if (myTabs.get(i) == tag) {
                    myViewPager.setCurrentItem(i);
                }
            }

            if (myTabs.get(0) == tag && hasBeenCreated) {
                SalesTab_FR.startDefault(activity, myPref.getPreferencesValue(MyPreferences.pref_default_transaction));
            }
        }

        @Override
        public void onTabUnselected(Tab tab, FragmentTransaction ft) {

        }

        @Override
        public void onTabReselected(Tab tab, FragmentTransaction ft) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageSelected(int index) {
            myActionBar.setSelectedNavigationItem(index);

        }

        @Override
        public Fragment getItem(int index) {
            TabInfo info = myTabs.get(index);
            return Fragment.instantiate(myContext, info.clazz.getName(), info.args);
        }

        @Override
        public int getCount() {
            return myTabs.size();
        }

    }

}
