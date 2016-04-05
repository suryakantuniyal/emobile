package com.android.support.fragmentactivity;

import android.app.ActionBar;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;

import com.android.emobilepos.R;
import com.android.emobilepos.mainmenu.MainMenu_FA;
import com.android.support.MyPreferences;

import java.util.Arrays;
import java.util.List;

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
                myBar.setDisplayShowTitleEnabled(showNavigationbar);
                myBar.setDisplayShowHomeEnabled(showNavigationbar);
                myBar.setHomeButtonEnabled(showNavigationbar);
                myBar.setStackedBackgroundDrawable(getResources().getDrawable(R.drawable.tabbar));
            }
        } else {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
        }
    }

    private boolean isNavigationBarModel() {
        for (String model : navigationbarByModels) {
            if(Build.MODEL.toLowerCase().startsWith(model)){
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
