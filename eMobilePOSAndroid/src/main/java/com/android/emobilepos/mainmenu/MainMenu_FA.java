package com.android.emobilepos.mainmenu;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.database.DBManager;
import com.android.emobilepos.R;
import com.android.emobilepos.firebase.NotificationHandler;
import com.android.emobilepos.firebase.NotificationSettings;
import com.android.emobilepos.firebase.RegistrationIntentService;
import com.android.support.DeviceUtils;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.NetworkUtils;
import com.android.support.SynchMethods;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.microsoft.windowsazure.notifications.NotificationsManager;

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
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private ProgressDialog driversProgressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        ViewPager viewPager = (ViewPager) findViewById(R.id.main_menu_pager);
        synchTextView = (TextView) findViewById(R.id.synch_title);
        synchTextView.setVisibility(View.GONE);
        tvStoreForward = (TextView) findViewById(R.id.label_cc_offline);
        NotificationsManager.handleNotifications(this, new NotificationSettings().getSenderId(), NotificationHandler.class);
        myPref = new MyPreferences(this);
        registerWithNotificationHubs();

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
        new Thread(new Runnable() {
            @Override
            public void run() {
                global.getCurrLocation(MainMenu_FA.this, true);
            }
        }).start();

        hasBeenCreated = true;


    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i("checkPlayServices", "This device is not supported by Google Play Services.");
//                ToastNotify("This device is not supported by Google Play Services.");
//                finish();
            }
            return false;
        }
        return true;
    }

//    public void ToastNotify(final String notificationMessage) {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                Toast.makeText(MainMenu_FA.this, notificationMessage, Toast.LENGTH_LONG).show();
////                TextView helloText = (TextView) findViewById(R.id.);
////                helloText.setText(notificationMessage);
//            }
//        });
//    }

    public void registerWithNotificationHubs() {
        if (checkPlayServices()) {
//            String accountNumber = myPref.getAcctNumber();
//            FirebaseMessaging.getInstance().subscribeToTopic(accountNumber);
            // Start IntentService to register this application with FCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
    }

//    private void sendFirebaseMessage() {
//        FirebaseMessaging messaging = FirebaseMessaging.getInstance();
//        messaging.send(new RemoteMessage.Builder(new NotificationSettings().getSenderId() + "@gcm.googleapis.com")
//                .setMessageId(String.valueOf(SystemClock.currentThreadTimeMillis()))
//                .addData("my_message", "Hello world")
//                .addData("my_action", "SAY_HELLO")
//                .build()
//        );
//
//        oauthclient.HttpClient client = new oauthclient.HttpClient();
//        String json = "{\"to\":\"/topics/holds_sync\",\"notification\":{\"body\":\"Yellow\",\"title\":\"my title\"},\"priority\":10}";
//        String authorizationKey = "key=AAAAgT3tGUw:APA91bHti3tuO7EJvsqWiFF-YJil6fhDff67AorKTJzJ6ihWud7g-1roBfDuP21zAYTdgTdvlkEQQdp8mFPU9AT1LS_mIGg7y63SyZTaBFZZ8HnD0xea7vdg7Yr3VrGt0zK_WP6_ajGuSCJ71oI_lvQu67T8Yrs7qg";
//        try {
//            AssignEmployee assignEmployee = AssignEmployeeDAO.getAssignEmployee();
//            NotificationEvent event = new NotificationEvent();
//            event.setTo("/topics/" + myPref.getAcctNumber());
//            event.getNotification().setMerchantAccount(myPref.getAcctNumber());
//            event.getNotification().setDeviceId(myPref.getDeviceID());
//            event.getNotification().setEmployeeId(String.valueOf(assignEmployee.getEmpId()));
//            event.getNotification().setNotificationEventAction(NotificationEvent.NotificationEventAction.SYNC_HOLDS);
//            Gson gson = JsonUtils.getInstance();
//            json = gson.toJson(event);
//            client.postAuthorizationHeader("https://fcm.googleapis.com/fcm/send", json, authorizationKey);
//        } catch (Exception e) {
//            e.printStackTrace();
//
//        }
//    }

    @Override
    public void onResume() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                sendFirebaseMessage();
//            }
//        }).start();
        if (global.isApplicationSentToBackground(activity)) {
            global.loggedIn = false;
        }
        global.stopActivityTransitionTimer();

        if (hasBeenCreated && !global.loggedIn
                && (myPref.getPrinterType() != Global.POWA || (myPref.getPrinterType() == Global.POWA
                && (Global.mainPrinterManager != null && Global.mainPrinterManager.getCurrentDevice() != null)))) {
            if (global.getGlobalDlog() != null && global.getGlobalDlog().isShowing()) {
                global.getGlobalDlog().dismiss();
            }
            global.promptForMandatoryLogin(activity);
        }

        if (myPref.getPreferences(MyPreferences.pref_automatic_sync) && hasBeenCreated && NetworkUtils.isConnectedToInternet(activity)) {
            DBManager dbManager = new DBManager(activity, Global.FROM_SYNCH_ACTIVITY);
//            dbManager.synchSend(false, true, activity);
            SynchMethods sm = new SynchMethods(dbManager);
            sm.synchSend(Global.FROM_SYNCH_ACTIVITY, true, activity);
        }

        if (myPref.getPreferences(MyPreferences.pref_use_store_and_forward))
            tvStoreForward.setVisibility(View.VISIBLE);
        else
            tvStoreForward.setVisibility(View.GONE);

        new autoConnectPrinter().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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

    private void showProgressDialog() {
        if (driversProgressDialog == null) {
            driversProgressDialog = new ProgressDialog(MainMenu_FA.this);
            driversProgressDialog.setMessage(getString(R.string.connecting_devices));
            driversProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            driversProgressDialog.setCancelable(false);
        }
        driversProgressDialog.show();
    }

    private void dismissProgressDialog() {
        if (driversProgressDialog != null && driversProgressDialog.isShowing()) {
            driversProgressDialog.dismiss();
        }
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

    @Override
    protected void onDestroy() {
        dismissProgressDialog();
        super.onDestroy();
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

        @Override
        protected void onPreExecute() {
            activity.setRequestedOrientation(Global.getScreenOrientation(activity));
            loadMultiPrinter = (Global.multiPrinterManager == null
                    || Global.multiPrinterManager.size() == 0)
                    && (Global.mainPrinterManager == null
                    || Global.mainPrinterManager.getCurrentDevice() == null)
                    && (Global.btSwiper == null || Global.btSwiper.getCurrentDevice() == null);

            if (loadMultiPrinter) {
                showProgressDialog();
            }
        }

        @Override
        protected String doInBackground(String... params) {
            final String autoConnect = "";

//            activity.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    DeviceUtils.autoConnect(activity, loadMultiPrinter);
//                }
//            });
//            synchronized (activity) {
//                try {
//                    activity.wait(30000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
            DeviceUtils.autoConnect(activity, loadMultiPrinter);
            if (myPref.getPrinterType() == Global.POWA || myPref.getPrinterType() == Global.MEPOS
                    || myPref.getPrinterType() == Global.ELOPAYPOINT) {
                isUSB = true;
            }
            if (Global.mainPrinterManager != null && Global.mainPrinterManager.getCurrentDevice() != null &&
                    Global.mainPrinterManager.getCurrentDevice() instanceof EMSsnbc) {
                ((EMSsnbc) Global.mainPrinterManager.getCurrentDevice()).closeUsbInterface();
            }
            return autoConnect;
        }

        @Override
        protected void onPostExecute(String result) {
            if (!isUSB && result.toString().length() > 0)
                Toast.makeText(activity, result.toString(), Toast.LENGTH_LONG).show();
            else if (isUSB && (Global.mainPrinterManager == null ||
                    Global.mainPrinterManager.getCurrentDevice() == null)
                    || myPref.getPrinterType() == Global.MIURA) {
                if (global.getGlobalDlog() != null)
                    global.getGlobalDlog().dismiss();
                EMSDeviceManager edm = new EMSDeviceManager();
                Global.mainPrinterManager = edm.getManager();
                Global.mainPrinterManager.loadMultiDriver(activity, myPref.getPrinterType(), 0, true, "", "");
            }
            if (!activity.isFinishing()) {
                dismissProgressDialog();
            }
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
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
