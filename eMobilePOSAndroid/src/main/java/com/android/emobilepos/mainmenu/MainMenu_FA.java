package com.android.emobilepos.mainmenu;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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

import com.android.dao.ClerkDAO;
import com.android.emobilepos.R;
import com.android.emobilepos.firebase.NotificationHandler;
import com.android.emobilepos.firebase.NotificationSettings;
import com.android.emobilepos.firebase.PollingNotificationService;
import com.android.emobilepos.firebase.RegistrationIntentService;
import com.android.emobilepos.models.realms.Clerk;
import com.android.emobilepos.security.SecurityManager;
import com.android.emobilepos.service.SyncConfigServerService;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.OrderRecoveryUtils;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.microsoft.windowsazure.notifications.NotificationsManager;

import java.lang.reflect.Method;
import java.util.ArrayList;

import static com.android.emobilepos.models.firebase.NotificationEvent.NotificationEventAction;

public class MainMenu_FA extends BaseFragmentActivityActionBar {
    public static final String NOTIFICATION_RECEIVED = "NOTIFICATION_RECEIVED";
    public static final String NOTIFICATION_MESSAGE = "NOTIFICATION_MESSAGE";
    public static final String NOTIFICATION_DEVICES_LOADED = "NOTIFICATION_DEVICES_LOADED";
    public static final String NOTIFICATION_LOGIN_STATECHANGE = "NOTIFICATION_LOGIN_STATECHANGE";

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private MyPreferences myPref;
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

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    public static boolean checkPlayServices(Context context) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(context);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
            } else {
                Log.i("checkPlayServices", "This device is not supported by Google Play Services.");
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

    private static void startPollingService(Context context) {
        MyPreferences myPref = new MyPreferences(context);
        int flags = 0;
        if (myPref.isPollingHoldsEnable()) {
            flags = PollingNotificationService.PollingServicesFlag.ONHOLDS.getCode() | PollingNotificationService.PollingServicesFlag.DINING_TABLES.getCode();
        }
        if (myPref.isAutoSyncEnable()) {
            flags = flags | PollingNotificationService.PollingServicesFlag.AUTO_SYNC.getCode();
        }
        PollingNotificationService.start(context, flags);
    }

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
        global = (Global) getApplication();

        setTabsAdapter(new AdapterTabs(this, viewPager));

        getTabsAdapter().addTab(myBar.newTab().setText(R.string.sales_title), SalesTab_FR.class, null);
        myBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        getTabsAdapter().addTab(myBar.newTab().setText(R.string.hist_title), HistoryTab_FR.class, null);
        myBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        getTabsAdapter().addTab(myBar.newTab().setText(R.string.sync_title), SyncTab_FR.class, null);
        myBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
//        getTabsAdapter().addTab(myBar.newTab().setText(R.string.routes_title), RoutesTab_FR.class, null);
//        myBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
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

        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.getBoolean("unsynched_items", false))
            myBar.setSelectedNavigationItem(2);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Global.getCurrLocation(MainMenu_FA.this, true);
            }
        }).start();

        hasBeenCreated = true;

        OrderRecoveryUtils orderRecoveryUtils = new OrderRecoveryUtils(this);
        Intent intent = orderRecoveryUtils.getRecoveryIntent();
        if (intent != null) {
            // there is an order that needs recovery
            startActivity(intent);
        }
    }

    public void registerWithNotificationHubs() {
        if (checkPlayServices(this)) {
//            if (false) {
//            String accountNumber = myPref.getAcctNumber();
//            FirebaseMessaging.getInstance().subscribeToTopic(accountNumber);
            // Start IntentService to register this application with FCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        } else {
            if ((myPref.isPollingHoldsEnable() || myPref.isAutoSyncEnable()) && !PollingNotificationService.isServiceRunning(this)) {
                startPollingService(this);
            }
        }
    }

    public void setLogoutButtonClerkname() {
        if (myPref.isUseClerks()) {
            Clerk clerk = ClerkDAO.getByEmpId(Integer.parseInt(myPref.getClerkID()));
            if (clerk != null) {
                Menu menu = this.menu;
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
        global.resetOrderDetailsValues();
        if ((myPref.isPollingHoldsEnable() || myPref.isAutoSyncEnable()) && !PollingNotificationService.isServiceRunning(this)) {
            startPollingService(this);
        }
        if (myPref.isUse_syncplus_services() && myPref.isSyncplus_AutoScan()) {
            SyncConfigServerService.startService(this);
        }
//        Intent service = new Intent(this, SyncConfigServerService.class);
//        startService(service);
        registerReceiver(messageReceiver, new IntentFilter(NOTIFICATION_RECEIVED));
//        DeviceUtils.registerFingerPrintReader(this);
        if (global.isApplicationSentToBackground()) {
            Global.loggedIn = false;
        }
        setLogoutButtonClerkname();
        global.stopActivityTransitionTimer();
        if (hasBeenCreated && !Global.loggedIn
                && (myPref.getPrinterType() != Global.POWA || (myPref.getPrinterType() == Global.POWA
                && (Global.mainPrinterManager != null && Global.mainPrinterManager.getCurrentDevice() != null)))) {
            Global.dismissDialog(this, global.getGlobalDlog());
            global.promptForMandatoryLogin(this);
        }

        if (myPref.getPreferences(MyPreferences.pref_use_store_and_forward))
            tvStoreForward.setVisibility(View.VISIBLE);
        else
            tvStoreForward.setVisibility(View.GONE);

        new AutoConnectPrinter().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
        super.onResume();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        forceTabs();
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


    private void dismissProgressDialog() {
        if (driversProgressDialog != null && driversProgressDialog.isShowing()) {
            driversProgressDialog.dismiss();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(messageReceiver);
//        DeviceUtils.unregisterFingerPrintReader(this);
//        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
//        boolean isScreenOn = powerManager.isScreenOn();
//        if (!isScreenOn && myPref.isExpireUserSession())
//            Global.loggedIn = false;
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
//                SalesTab_FR.startDefault(MainMenu_FA.this, myPref.getPreferencesValue(MyPreferences.pref_default_transaction));
                String value = myPref.getPreferencesValue(MyPreferences.pref_default_transaction);
                Global.TransactionType type = Global.TransactionType.getByCode(Integer.parseInt(value));
                SalesTab_FR.startDefault(MainMenu_FA.this, type);
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
