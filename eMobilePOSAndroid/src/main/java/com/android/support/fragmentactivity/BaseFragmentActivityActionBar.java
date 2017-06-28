package com.android.support.fragmentactivity;

import android.app.ActionBar;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import com.android.dao.ClerkDAO;
import com.android.emobilepos.OnHoldActivity;
import com.android.emobilepos.R;
import com.android.emobilepos.mainmenu.MainMenu_FA;
import com.android.emobilepos.models.realms.Clerk;
import com.android.support.Global;
import com.android.support.MyPreferences;

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
            if (Build.MODEL.toLowerCase().startsWith(model)) {
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
        clerk = ClerkDAO.getByEmpId(Integer.parseInt(myPref.getClerkID()));
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
        } else if (showNavigationbar)
            menu.findItem(R.id.menu_back).setVisible(true);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.findItem(R.id.logoutMenuItem);
        if (myPref.isUseClerks() && clerk == null) {
            clerk = ClerkDAO.getByEmpId(Integer.parseInt(myPref.getClerkID()));
        } else {
            clerk = null;
        }
        if (menuItem != null && clerk != null) {
            menuItem.setTitle(String.format("%s (%s)", getString(R.string.logout_menu), clerk.getEmpName()));
        }
        this.menu = menu;
        return super.onPrepareOptionsMenu(menu);
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
    protected void onResume() {
        invalidateOptionsMenu();
        super.onResume();
    }
}
