package com.android.support.fragmentactivity;

import android.app.ActionBar;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import com.android.emobilepos.R;
import com.android.emobilepos.mainmenu.MainMenu_FA;
import com.android.support.MyPreferences;
import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

/**
 * Created by Guarionex on 12/9/2015.
 */
public class BaseFragmentActivityActionBar extends FragmentActivity {
    protected ActionBar myBar;
    private static MyPreferences myPref;
    private boolean showNavigationbar = false;
    private static String[] navigationbarByModels;

    protected void setActionBar() {
        showNavigationbar = myPref.getPreferences(MyPreferences.pref_use_navigationbar) || isNavigationBarModel();
        if (this instanceof MainMenu_FA || showNavigationbar) {
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
            if (Build.MODEL.toLowerCase().startsWith(model)) {
                return true;
            }
        }
        return false;
    }

    private void setCrashliticAditionalInfo() {
        // You can call any combination of these three methods
        if (myPref != null) {
            Crashlytics.setUserIdentifier(myPref.getAcctNumber());
        }
//        Crashlytics.setUserEmail("user@fabric.io");
//        Crashlytics.setUserName("Test User");
    }


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        if (navigationbarByModels == null || navigationbarByModels.length == 0) {
            navigationbarByModels = getResources().getStringArray(R.array.navigationbarByModels);
        }
        if (myPref == null) {
            myPref = new MyPreferences(this);
        }
        setCrashliticAditionalInfo();
        setActionBar();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (showNavigationbar)
            getMenuInflater().inflate(R.menu.activity_main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_back: {
                onBackPressed();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
