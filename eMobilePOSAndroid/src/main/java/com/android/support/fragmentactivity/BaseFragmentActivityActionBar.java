package com.android.support.fragmentactivity;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import com.android.dao.ClerkDAO;
import com.android.emobilepos.BuildConfig;
import com.android.emobilepos.OnHoldActivity;
import com.android.emobilepos.R;
import com.android.emobilepos.mainmenu.MainMenu_FA;
import com.android.emobilepos.models.realms.Clerk;
import com.android.emobilepos.ordering.OrderingMain_FA;
import com.android.support.DeviceUtils;
import com.android.support.Global;
import com.android.support.MyPreferences;

import drivers.EMSsnbc;
import io.realm.Realm;
import main.EMSDeviceManager;

/**
 * Created by Guarionex on 12/9/2015.
 */
public class BaseFragmentActivityActionBar extends FragmentActivity {
    static Clerk clerk;
    private static MyPreferences myPref;
    private static String[] navigationbarByModels;
    public Menu menu;
    protected ActionBar myBar;
    private boolean showNavigationbar = false;

    protected void setActionBar() {
        showNavigationbar = myPref.getPreferences(MyPreferences.pref_use_navigationbar) || isNavigationBarModel() || (this instanceof MainMenu_FA && myPref.isUseClerks());
        if (showNavigationbar || this instanceof MainMenu_FA || this instanceof OnHoldActivity) {
            myBar = this.getActionBar();
            if (myBar != null) {
                myBar.setDisplayShowTitleEnabled(true);
                myBar.setDisplayShowHomeEnabled(true);
                myBar.setHomeButtonEnabled(true);
                myBar.setStackedBackgroundDrawable(getResources().getDrawable(R.drawable.tabbar));
            }
        } else {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
        }
    }

    private boolean isNavigationBarModel() {
        for (String model : navigationbarByModels) {
            if (Build.MODEL.toLowerCase().contains(model.toLowerCase())) {
                return true;
            }
        }
        return false;
    }


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (navigationbarByModels == null || navigationbarByModels.length == 0) {
            navigationbarByModels = getResources().getStringArray(R.array.navigationbarByModels);
        }
        if (myPref == null) {
            myPref = new MyPreferences(this);
        }
        if (clerk == null || !myPref.getClerkID().equalsIgnoreCase(String.valueOf(clerk.getEmpId()))) {
            clerk = ClerkDAO.getByEmpId(Integer.parseInt(myPref.getClerkID()));
        }
        setActionBar();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_menu, menu);
        if (this instanceof OnHoldActivity) {
            menu.findItem(R.id.refreshHolds).setVisible(true);
        } else {
            menu.findItem(R.id.refreshHolds).setVisible(false);
        }
        if (menu.findItem(R.id.logoutMenuItem) != null) {
            menu.findItem(R.id.logoutMenuItem).setVisible(false);
            menu.findItem(R.id.menu_back).setVisible(false);
        }
        if (this instanceof MainMenu_FA && myPref.getIsPersistClerk()) {
            menu.findItem(R.id.logoutMenuItem).setVisible(true);
        } else if (showNavigationbar) {
            menu.findItem(R.id.menu_back).setVisible(true);
        }
        if (this instanceof OrderingMain_FA) {
            menu.findItem(R.id.toggleEloBCR).setVisible(true);
        } else {
            menu.findItem(R.id.toggleEloBCR).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.findItem(R.id.logoutMenuItem);
        if (myPref.isUseClerks()) {
            if (clerk == null || !myPref.getClerkID().equalsIgnoreCase(String.valueOf(clerk.getEmpId()))) {
                clerk = ClerkDAO.getByEmpId(Integer.parseInt(myPref.getClerkID()));
            }
        } else {
            if (!myPref.isUseClerks()) {
                clerk = null;
            }
        }
        if (menuItem != null && clerk != null) {
            menuItem.setTitle(String.format("%s (%s)", getString(R.string.logout_menu), clerk.getEmpName()));
        }
        this.menu = menu;
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_back: {
                onBackPressed();
                break;
            }
            case R.id.logoutMenuItem: {
                myPref.setIsUseClerks(true);
                Global global = (Global) this.getApplication();
                Global.loggedIn = false;
                global.promptForMandatoryLogin(this);
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    @Override
    protected void onResume() {
        if (BuildConfig.DEBUG) {
            int count = 0;
            if (Realm.getDefaultConfiguration() != null) {
                count = Realm.getGlobalInstanceCount(Realm.getDefaultConfiguration());

                Toast.makeText(this, "Realms count: " + String.valueOf(count), Toast.LENGTH_LONG).show();

            }
        }
        invalidateOptionsMenu();
        DeviceUtils.registerFingerPrintReader(this);
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        DeviceUtils.unregisterFingerPrintReader(this);
    }


    public class AutoConnectPrinter extends AsyncTask<String, String, String> {
        boolean isUSB = false;
        ProgressDialog driversProgressDialog;
        private boolean loadMultiPrinter;

        @Override
        protected void onPreExecute() {
            setRequestedOrientation(Global.getScreenOrientation(BaseFragmentActivityActionBar.this));
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
            String autoConnect;
            autoConnect = DeviceUtils.autoConnect(BaseFragmentActivityActionBar.this, loadMultiPrinter);
            if (myPref.getPrinterType() == Global.POWA ||
                    myPref.getPrinterType() == Global.MEPOS ||
                    myPref.getPrinterType() == Global.ELOPAYPOINT ||
                    myPref.getPrinterType() == Global.SNBC ||
                    myPref.getPrinterType() == Global.HP_EONEPRIME) {
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
                Toast.makeText(BaseFragmentActivityActionBar.this, result.toString(), Toast.LENGTH_LONG).show();
            else if (isUSB && (Global.mainPrinterManager == null ||
                    Global.mainPrinterManager.getCurrentDevice() == null)) {
//                if (global.getGlobalDlog() != null)
//                    global.getGlobalDlog().dismiss();
                EMSDeviceManager edm = new EMSDeviceManager();
                Global.mainPrinterManager = edm.getManager();
                Global.mainPrinterManager.loadMultiDriver(BaseFragmentActivityActionBar.this, myPref.getPrinterType(), 0, true, "", "");
            }
            Global.dismissDialog(BaseFragmentActivityActionBar.this, driversProgressDialog);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }

        private void showProgressDialog() {
            if (driversProgressDialog == null) {
                driversProgressDialog = new ProgressDialog(BaseFragmentActivityActionBar.this);
                driversProgressDialog.setMessage(getString(R.string.connecting_devices));
                driversProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                driversProgressDialog.setCancelable(true);
            }
            driversProgressDialog.show();

        }
    }

}
