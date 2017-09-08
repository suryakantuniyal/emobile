package com.android.emobilepos.mainmenu;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.dao.ClerkDAO;
import com.android.database.DBManager;
import com.android.emobilepos.R;
import com.android.emobilepos.firebase.NotificationHandler;
import com.android.emobilepos.firebase.NotificationSettings;
import com.android.emobilepos.firebase.PollingNotificationService;
import com.android.emobilepos.firebase.RegistrationIntentService;
import com.android.emobilepos.models.realms.Clerk;
import com.android.emobilepos.security.SecurityManager;
import com.android.support.DeviceUtils;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.SynchMethods;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.microsoft.windowsazure.notifications.NotificationsManager;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;

import android_serialport_api.SerialPort;
import drivers.EMSsnbc;
import main.EMSDeviceManager;

import static com.android.emobilepos.models.firebase.NotificationEvent.NotificationEventAction;

public class MainMenu_FA extends BaseFragmentActivityActionBar {
    public static final String NOTIFICATION_RECEIVED = "NOTIFICATION_RECEIVED";
    public static final String NOTIFICATION_MESSAGE = "NOTIFICATION_MESSAGE";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static Activity activity;
    private static MyPreferences myPref;

    static {
        System.loadLibrary("serial_port");
    }

    private Global global;
    private boolean hasBeenCreated = false;
    private TextView synchTextView, tvStoreForward;
    private AdapterTabs tabsAdapter;
    private ProgressDialog driversProgressDialog;
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
    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Handler handler = new Handler();
            String eventAction = intent.getStringExtra(NOTIFICATION_MESSAGE);
            NotificationEventAction action = NotificationEventAction.getNotificationEventByCode(Integer.parseInt(eventAction));
            switch (action) {
                case SYNC_HOLDS:
                    getSynchTextView().setText(getString(R.string.sync_dload_ordersonhold));
                    getSynchTextView().setVisibility(View.VISIBLE);
                    break;
                case SYNC_MESAS_CONFIG:
                    getSynchTextView().setText(getString(R.string.sync_dload_dinnertables));
                    getSynchTextView().setVisibility(View.VISIBLE);
                    break;
            }
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    getSynchTextView().setVisibility(View.GONE);
                }
            };
            handler.postDelayed(runnable, 5000);
        }
    };

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
                Global.getCurrLocation(MainMenu_FA.this, true);
            }
        }).start();

        hasBeenCreated = true;

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
//                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
//                        .show();
            } else {
                Log.i("checkPlayServices", "This device is not supported by Google Play Services.");
//                ToastNotify("This device is not supported by Google Play Services.");
//                finish();
            }
            return false;
        }
        return true;
    }

    public void registerWithNotificationHubs() {
        if (checkPlayServices()) {
//            if (false) {
//            String accountNumber = myPref.getAcctNumber();
//            FirebaseMessaging.getInstance().subscribeToTopic(accountNumber);
            // Start IntentService to register this application with FCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        } else {
            if ((myPref.isPollingHoldsEnable() || myPref.isAutoSyncEnable()) && !PollingNotificationService.isServiceRunning(this)) {
                startPollingService();
            }
        }
    }

    public void setLogoutButtonClerkname() {
        if (myPref.isUseClerks()) {
            Clerk clerk = ClerkDAO.getByEmpId(Integer.parseInt(myPref.getClerkID()));
            if (clerk != null) {
                Menu menu = ((MainMenu_FA) activity).menu;
                if (menu != null) {
                    MenuItem menuItem = menu.findItem(R.id.logoutMenuItem);
                    if (menuItem != null) {
                        menuItem.setTitle(String.format("%s (%s)", getString(R.string.logout_menu), clerk.getEmpName()));
                    }
                }
            }
        }
    }

    public void hideLogoutButton() {
        invalidateOptionsMenu();
    }

    @Override
    public void onResume() {
        if ((myPref.isPollingHoldsEnable() || myPref.isAutoSyncEnable()) && !PollingNotificationService.isServiceRunning(this)) {
            startPollingService();
        }
        registerReceiver(messageReceiver, new IntentFilter(NOTIFICATION_RECEIVED));
        if (global.isApplicationSentToBackground()) {
            Global.loggedIn = false;
        }
        setLogoutButtonClerkname();
        global.stopActivityTransitionTimer();
        if (hasBeenCreated && !Global.loggedIn
                && (myPref.getPrinterType() != Global.POWA || (myPref.getPrinterType() == Global.POWA
                && (Global.mainPrinterManager != null && Global.mainPrinterManager.getCurrentDevice() != null)))) {
            Global.dismissDialog(this, global.getGlobalDlog());
            //            if (global.getGlobalDlog() != null && global.getGlobalDlog().isShowing()) {
//                global.getGlobalDlog().dismiss();
//            }
            global.promptForMandatoryLogin(activity);
        }

//        if (myPref.isAutoSyncEnable() && hasBeenCreated) {
//            DBManager dbManager = new DBManager(activity, Global.FROM_SYNCH_ACTIVITY);
//            SynchMethods sm = new SynchMethods(dbManager);
//            sm.synchSend(Global.FROM_SYNCH_ACTIVITY, true, activity);
//            getSynchTextView().setText(getString(R.string.sync_inprogress));
//            getSynchTextView().setVisibility(View.VISIBLE);
//        }

        if (myPref.getPreferences(MyPreferences.pref_use_store_and_forward))
            tvStoreForward.setVisibility(View.VISIBLE);
        else
            tvStoreForward.setVisibility(View.GONE);

        new AutoConnectPrinter().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        super.onResume();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        forceTabs();
    }

    private void startPollingService() {
        int flags = 0;
        if (myPref.isPollingHoldsEnable()) {
            flags = PollingNotificationService.PollingServicesFlag.ONHOLDS.getCode() | PollingNotificationService.PollingServicesFlag.DINING_TABLES.getCode();
        }
        if (myPref.isAutoSyncEnable()) {
            flags = flags | PollingNotificationService.PollingServicesFlag.AUTO_SYNC.getCode();
        }
        PollingNotificationService.start(this, flags);
    }

    private void stopPollingService() {
        PollingNotificationService.stop(this);
    }

    public void forceTabs() {
        try {
            final ActionBar actionBar = getActionBar();
            final Method setHasEmbeddedTabsMethod = actionBar.getClass().getDeclaredMethod("setHasEmbeddedTabs",
                    boolean.class);
            setHasEmbeddedTabsMethod.setAccessible(true);
            setHasEmbeddedTabsMethod.invoke(actionBar, false);
        } catch (final Exception e) {
            Crashlytics.logException(e);
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
            driversProgressDialog.setCancelable(true);
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
        unregisterReceiver(messageReceiver);
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        boolean isScreenOn = powerManager.isScreenOn();
        if (!isScreenOn)
            Global.loggedIn = false;
        global.startActivityTransitionTimer();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Global.loggedIn = false;
        this.finish();
    }

    @Override
    protected void onDestroy() {
        dismissProgressDialog();
        if (global.getGlobalDlog() != null && global.getGlobalDlog().isShowing()) {
            global.getGlobalDlog().dismiss();
        }
        super.onDestroy();
    }

    public AdapterTabs getTabsAdapter() {
        return tabsAdapter;
    }

    public void setTabsAdapter(AdapterTabs tabsAdapter) {
        this.tabsAdapter = tabsAdapter;
    }

    public TextView getSynchTextView() {
        return synchTextView;
    }

    private class AutoConnectPrinter extends AsyncTask<String, String, String> {
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
            String autoConnect = "";

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
            autoConnect = DeviceUtils.autoConnect(activity, loadMultiPrinter);
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

    private class AdapterTabs extends FragmentPagerAdapter
            implements ActionBar.TabListener, ViewPager.OnPageChangeListener {

        private final Context myContext;
        private final ViewPager myViewPager;
        private final ActionBar myActionBar;
        private final ArrayList<TabInfo> myTabs = new ArrayList<TabInfo>();

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

            if (myTabs.get(0) == tag && hasBeenCreated && SecurityManager.hasPermissions(myContext, SecurityManager.SecurityAction.OPEN_ORDER)) {
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

        final class TabInfo {
            private final Class<?> clazz;
            private final Bundle args;

            TabInfo(Class<?> _clazz, Bundle _args) {
                clazz = _clazz;
                args = _args;
            }
        }

    }

}
