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

/**
 * Created by Guarionex on 12/9/2015.
 */
public class BaseFragmentActivityActionBar extends FragmentActivity {
    protected ActionBar myBar;


    private void setActionBar() {
        if (this instanceof MainMenu_FA || Build.MODEL.equalsIgnoreCase("PayPoint ESY13P1")) {
            myBar = this.getActionBar();
            if (myBar != null) {
                myBar.setDisplayShowTitleEnabled(Build.MODEL.equalsIgnoreCase("PayPoint ESY13P1"));
                myBar.setDisplayShowHomeEnabled(Build.MODEL.equalsIgnoreCase("PayPoint ESY13P1"));
                myBar.setHomeButtonEnabled(Build.MODEL.equalsIgnoreCase("PayPoint ESY13P1"));
                myBar.setStackedBackgroundDrawable(getResources().getDrawable(R.drawable.tabbar));
            }
        } else {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
        }
    }


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActionBar();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (Build.MODEL.equalsIgnoreCase("PayPoint ESY13P1"))
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
