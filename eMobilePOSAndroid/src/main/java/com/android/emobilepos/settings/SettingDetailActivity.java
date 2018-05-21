package com.android.emobilepos.settings;

import android.os.Bundle;
import android.os.PowerManager;

import com.android.emobilepos.R;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;


public class SettingDetailActivity extends BaseFragmentActivityActionBar {

    private boolean hasBeenCreated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_detail);
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            SettingListActivity.PrefsFragment fragment = new SettingListActivity.PrefsFragment();
            Bundle extras = getIntent().getExtras();
            int section = extras.getInt("section");
            arguments.putInt("section", section);
            fragment.setArguments(arguments);
            getFragmentManager().beginTransaction()
                    .add(R.id.setting_detail_container, fragment)
                    .commit();
        }
        hasBeenCreated = true;
    }


    @Override
    public void onResume() {
        Global global = (Global) getApplication();
        if (global.isApplicationSentToBackground())
            Global.loggedIn = false;
        global.stopActivityTransitionTimer();

        if (hasBeenCreated && !Global.loggedIn) {
            if (global.getGlobalDlog() != null)
                global.getGlobalDlog().dismiss();
            global.promptForMandatoryLogin(this);
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        Global global = (Global) getApplication();
//        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
//        boolean isScreenOn = powerManager.isScreenOn();
//        MyPreferences myPref = new MyPreferences(this);
//        if (!isScreenOn && myPref.isExpireUserSession())
//            Global.loggedIn = false;
        global.startActivityTransitionTimer();
    }

}
