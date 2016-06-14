package com.android.emobilepos.mainmenu;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.dao.DeviceTableDAO;
import com.android.database.DBManager;
import com.android.emobilepos.R;
import com.android.emobilepos.models.Device;
import com.android.support.DeviceUtils;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.NetworkUtils;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import io.realm.RealmResults;
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
//		myBar = this.getActionBar();
//
//		myBar.setDisplayShowTitleEnabled(false);
//		myBar.setDisplayShowHomeEnabled(false);
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
            // SQLiteDatabase db = dbManager.openWritableDB();
            dbManager.synchSend(false, true);
        }

        if (myPref.getPreferences(MyPreferences.pref_use_store_and_forward))
            tvStoreForward.setVisibility(View.VISIBLE);
        else
            tvStoreForward.setVisibility(View.GONE);

        new autoConnectPrinter().execute("");
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

        //        StringBuilder sb = new StringBuilder();
        boolean isUSB = false;

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected String doInBackground(String... params) {
            boolean loadMultiPrinter = Global.multiPrinterManager == null || Global.multiPrinterManager.size() == 0;
            String autoConnect = DeviceUtils.autoConnect(activity, loadMultiPrinter);
            return autoConnect;
//            RealmResults<Device> devices = DeviceTableDAO.getAll();
//            HashMap<String, Integer> tempMap = new HashMap<String, Integer>();
//            EMSDeviceManager edm;
//            if (Global.multiPrinterManager == null || Global.multiPrinterManager.size() == 0) {
//                int i=0;
//                for (Device device : devices) {
//                    if (tempMap.containsKey(device.getId())) {
//                        Global.multiPrinterMap.put(device.getCategoryId(), tempMap.get(device.getId()));
//                    } else {
//                        tempMap.put(device.getId(), i);
//                        Global.multiPrinterMap.put(device.getCategoryId(), i);
//
//                        edm = new EMSDeviceManager();
//                        Global.multiPrinterManager.add(edm);
//
//                        if (Global.multiPrinterManager.get(i).loadMultiDriver(activity, Global.STAR, 48, true,
//                                "TCP:" + device.getIpAddress(),device.getTcpPort()))
//                            sb.append(device.getIpAddress()).append(": ").append("Connected\n");
//                        else
//                            sb.append(device.getIpAddress()).append(": ").append("Failed to connect\n");
//
//                        i++;
//                    }
//                }
//            }
//
//            String _portName;
//            String _peripheralName;
//            if ((myPref.getSwiperType() != -1) && (Global.btSwiper == null)) {
//                edm = new EMSDeviceManager();
//                _portName = myPref.swiperMACAddress(true, null);
//                _peripheralName = Global.getPeripheralName(myPref.getSwiperType());
//                Global.btSwiper = edm.getManager();
//                // Global.btSwiper.loadDrivers(activity, myPref.swiperType(true,
//                // -2), false);
//                if (Global.btSwiper.loadMultiDriver(activity, myPref.getSwiperType(), 0, false,
//                        myPref.swiperMACAddress(true, null), null))
//                    sb.append(_peripheralName).append(": ").append("Connected\n");
//                else
//                    sb.append(_peripheralName).append(": ").append("Failed to connect\n");
//            }
//            if ((myPref.sledType(true, -2) != -1) && (Global.btSled == null)) {
//                edm = new EMSDeviceManager();
//                Global.btSled = edm.getManager();
//                _peripheralName = Global.getPeripheralName(myPref.sledType(true, -2));
//                // Global.btSwiper.loadDrivers(activity, myPref.swiperType(true,
//                // -2), false);
//                if (Global.btSled.loadMultiDriver(activity, myPref.sledType(true, -2), 0, false, null, null))
//                    sb.append(_peripheralName).append(": ").append("Connected\n");
//                else
//                    sb.append(_peripheralName).append(": ").append("Failed to connect\n");
//            }
//            if ((myPref.getPrinterType() != -1) && (Global.mainPrinterManager == null)) // ||(Global.mainPrinterManager!=null&&Global.mainPrinterManager.currentDevice==null)))
//            {
//
//                _peripheralName = Global.getPeripheralName(myPref.getPrinterType());
//                _portName = myPref.getPrinterMACAddress();
//                String _portNumber = myPref.getStarPort();
//                boolean isPOS = myPref.posPrinter(true, false);
//                int txtAreaSize = myPref.printerAreaSize(true, -1);
//                if (myPref.isPAT215()) {
//                    edm = new EMSDeviceManager();
//                    Global.embededMSR = edm.getManager();
//                    if (Global.embededMSR.loadMultiDriver(activity, Global.PAT215, 0, false, "", "")) {
//                        sb.append(Global.BuildModel.PAT215.name()).append(": ").append("Connected\n");
//                    } else {
//                        sb.append(Global.BuildModel.PAT215.name()).append(": ").append("Failed to connect\n");
//                    }
//                }
//                if (myPref.getPrinterType() != Global.POWA) {
//                    edm = new EMSDeviceManager();
//                    Global.mainPrinterManager = edm.getManager();
//                    if (Global.mainPrinterManager.loadMultiDriver(activity, myPref.getPrinterType(), txtAreaSize,
//                            isPOS, _portName, _portNumber))
//                        sb.append(_peripheralName).append(": ").append("Connected\n");
//                    else
//                        sb.append(_peripheralName).append(": ").append("Failed to connect\n");
//                } else
//                    isUSB = true;
//
//            } else if (!TextUtils.isEmpty(myPref.getStarIPAddress())) {
//                edm = new EMSDeviceManager();
//                Global.mainPrinterManager = edm.getManager();
//
//                if (Global.mainPrinterManager.loadMultiDriver(activity, Global.STAR, 48, true,
//                        "TCP:" + myPref.getStarIPAddress(), myPref.getStarPort()))
//                    sb.append(myPref.getStarIPAddress()).append(": ").append("Connected\n");
//                else
//                    sb.append(myPref.getStarIPAddress()).append(": ").append("Failed to connect\n");
//            }

//            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (!isUSB && result.toString().length() > 0)
                Toast.makeText(activity, result.toString(), Toast.LENGTH_LONG).show();
            else if (isUSB && Global.mainPrinterManager.currentDevice == null) {
                if (global.getGlobalDlog() != null)
                    global.getGlobalDlog().dismiss();
                Global.mainPrinterManager.loadMultiDriver(activity, myPref.getPrinterType(), 0, true, "", "");
            }
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
                // Toast.makeText(activity, "launch default trans",
                // Toast.LENGTH_LONG).show();
                SalesTab_FR.startDefault(activity, myPref.getPreferencesValue(MyPreferences.pref_default_transaction));
            }
//            else if (selectedPage == 1) // Sync tab
//            {
//                childViewPager = myViewPager;
//                ListView listView = (ListView) myViewPager.findViewById(R.id.synchListView);
//                if (listView != null) {
//                    SynchMenuAdapter adapter = (SynchMenuAdapter) listView.getAdapter();
//                    adapter.notifyDataSetChanged();
//                }
//            }
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
